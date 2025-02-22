package com.lasagnerd.odin.codeInsight;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.lasagnerd.odin.codeInsight.evaluation.EvOdinValue;
import com.lasagnerd.odin.codeInsight.evaluation.EvOdinValues;
import com.lasagnerd.odin.codeInsight.evaluation.OdinExpressionEvaluator;
import com.lasagnerd.odin.codeInsight.imports.OdinImportService;
import com.lasagnerd.odin.codeInsight.imports.OdinImportUtils;
import com.lasagnerd.odin.codeInsight.sdk.OdinSdkService;
import com.lasagnerd.odin.codeInsight.symbols.*;
import com.lasagnerd.odin.codeInsight.typeInference.OdinInferenceEngine;
import com.lasagnerd.odin.codeInsight.typeInference.OdinTypeResolver;
import com.lasagnerd.odin.codeInsight.typeSystem.*;
import com.lasagnerd.odin.lang.psi.*;
import groovy.json.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.lasagnerd.odin.codeInsight.symbols.OdinSymbolType.*;

@SuppressWarnings("unused")
public class OdinInsightUtils {

    public static final List<Class<?>> OPERAND_BOUNDARY_CLASSES = List.of(
            OdinOperation.class,

            OdinArgument.class,
            OdinDeclaration.class,
            OdinStatement.class,

            OdinLhsExpressions.class,
            OdinRhsExpressions.class,
            OdinLhs.class,
            OdinRhs.class

    );
    public static final char[] RGBA = {'r', 'g', 'b', 'a'};
    public static final char[] RGB = {'r', 'g', 'b'};

    public static final char[] RG = {'r', 'g',};
    public static final char[] R = {'r'};
    public static final char[] XYZW = {'x', 'y', 'z', 'w'};
    public static final char[] XYZ = {'x', 'y', 'z'};

    public static final char[] XY = {'x', 'y'};
    public static final char[] X = {'x'};

    public static final char[][][] ARRAY_SWIZZLE_FIELDS = {
            {X, R},
            {XY, RG},
            {XYZ, RGB},
            {XYZW, RGBA}
    };

    public static List<OdinSymbol> generateArraySwizzleFields(int arraySize, TsOdinType elementType) {
        if (arraySize < 1 || arraySize > 4) {
            return Collections.emptyList();
        }

        List<OdinSymbol> symbols = new ArrayList<>();
        char[][] arraySwizzleField = ARRAY_SWIZZLE_FIELDS[arraySize - 1];
        for (char[] combinations : arraySwizzleField) {
            symbols.addAll(generateSwizzleFields(combinations, elementType));
        }

        return symbols;
    }

    public static String getQualifiedCanonicalName(OdinDeclaration declaration) {
        if (declaration.getDeclaredIdentifiers().size() == 1) {
            if (!isLocal(declaration)) {
                String packageClauseName = OdinInsightUtils.getPackageClauseName(declaration);
                return packageClauseName + "." + declaration.getName();
            }
        }
        return null;
    }

    public static String getStringLiteralValue(OdinExpression odinExpression) {
        if (odinExpression instanceof OdinLiteralExpression literalExpression) {
            if (literalExpression.getBasicLiteral() instanceof OdinStringLiteral stringLiteral) {
                return getStringLiteralValue(stringLiteral);
            }
        }
        return null;
    }

    public static String getStringLiteralValue(OdinStringLiteral stringLiteral) {
        if (stringLiteral.getDqStringLiteral() != null || stringLiteral.getSqStringLiteral() != null) {
            String text = stringLiteral.getText();

            if (text.length() >= 2) {
                text = text.substring(1, text.length() - 1);
                return StringEscapeUtils.unescapeJava(text);
            }
        }
        return null;
    }

    public static OdinSymbolTable getTypeElements(OdinContext context, Project project, TsOdinType type) {
        return getTypeElements(context, project, type, false);
    }

    public static OdinSymbolTable getTypeElements(OdinContext context,
                                                  Project project,
                                                  TsOdinType type,
                                                  boolean includeReferenceableSymbols) {

        type = type.baseType(true);

        if (type == null)
            return OdinSymbolTable.EMPTY;

        if (type instanceof TsOdinPackageReferenceType packageType) {
            return getPackageReferenceSymbols(context, project, packageType, true);
        }
        OdinContext typeContext = type.getContext();
        OdinSymbolTable symbolTable = new OdinSymbolTable();
        switch (type) {
            case TsOdinPointerType pointerType -> {
                return getTypeElements(context, project, pointerType.getDereferencedType(), includeReferenceableSymbols);
            }
            case TsOdinConstrainedType constrainedType -> {
                return getTypeElements(context, project, constrainedType.getSpecializedType(), includeReferenceableSymbols);
            }
            case TsOdinAnyType anyType -> {
                return getTypeElements(context, project, anyType.getBackingType(), includeReferenceableSymbols);
            }
            case TsOdinTypeReference typeReference when typeReference.referencedType() instanceof TsOdinPolymorphicType polymorphicType -> {
                OdinType psiType = polymorphicType.getPsiType();
                if (psiType != null) {
                    if (psiType.getParent() instanceof OdinConstrainedType constrainedType) {
                        OdinType specializedType = constrainedType.getTypeList().get(1);
                        TsOdinType tsOdinType = OdinTypeResolver.resolveType(typeContext, specializedType);
                        if (tsOdinType instanceof TsOdinGenericType tsOdinGenericType) {
                            List<TsOdinParameter> typeParameters = getTypeParameters(tsOdinGenericType.genericType());
                            for (TsOdinParameter typeParameter : typeParameters) {
                                OdinSymbol symbol = new OdinSymbol();
                                symbol.setName(typeParameter.getName());
                                symbol.setSymbolType(PARAMETER);
                                symbol.setPsiType(typeParameter.getPsiType());
                                symbol.setDeclaredIdentifier(typeParameter.getIdentifier());
                                symbolTable.add(symbol);
                            }
                        }
                    }
                }
            }
            default -> {
            }
        }

        if (!(type instanceof TsOdinTypeReference typeReference)) {
            if (type.baseType(true) instanceof TsOdinObjcClass tsOdinObjcClass) {
                symbolTable.merge(getObjcClassMembers(tsOdinObjcClass, TsOdinObjcMemberInfo::isValidInstanceMember));
                return symbolTable;
            } else if (type.getPsiType() instanceof OdinStructType structType) {
                var structFields = OdinInsightUtils.getStructFields(type.getContext()
                        .withSymbolValueStore(context.getSymbolValueStore())
                        .withUseKnowledge(context.isUseKnowledge()), structType);

                symbolTable.addAll(structFields);
                return symbolTable;
            }

            if (type instanceof TsOdinSoaStructType tsOdinSoaStructType) {
                for (Map.Entry<String, TsOdinType> entry : tsOdinSoaStructType.getFields().entrySet()) {
                    TsOdinType sliceType = entry.getValue();
                    OdinSymbol symbol = new OdinSymbol();
                    symbol.setName(entry.getKey());
                    symbol.setVisibility(OdinVisibility.NONE);
                    symbol.setSymbolType(SOA_FIELD);
                    symbol.setPsiType(sliceType.getPsiType());
                    symbol.setImplicitlyDeclared(true);
                    symbol.setScope(OdinScope.TYPE);
                    symbolTable.add(symbol);
                }

                return symbolTable;
            }

            if (type.getPsiType() instanceof OdinEnumType enumType) {
                return symbolTable.with(getEnumFields(enumType));
            }

            if (type.getPsiType() instanceof OdinBitFieldType bitFieldType) {
                return symbolTable.with(getBitFieldFields(bitFieldType));
            }

            if (includeReferenceableSymbols && type instanceof TsOdinArrayType arrayType) {
                return OdinSymbolTable.from(getSwizzleFields(arrayType));
            }

            if (includeReferenceableSymbols && (type instanceof TsOdinDynamicArray || type instanceof TsOdinMapType)) {
                OdinSymbol implicitStructSymbol = OdinSdkService.createAllocatorSymbol(project);
                return OdinSymbolTable.from(Collections.singletonList(implicitStructSymbol));
            }

            if (includeReferenceableSymbols && TsOdinBuiltInTypes.getComplexTypes().contains(type)) {
                if (type == TsOdinBuiltInTypes.COMPLEX32) {
                    List<OdinSymbol> odinSymbols = generateSwizzleFields(RG, TsOdinBuiltInTypes.F16);
                    odinSymbols.addAll(generateSwizzleFields(XY, TsOdinBuiltInTypes.F16));
                    return OdinSymbolTable.from(odinSymbols);
                }

                if (type == TsOdinBuiltInTypes.COMPLEX64) {
                    List<OdinSymbol> odinSymbols = generateSwizzleFields(RG, TsOdinBuiltInTypes.F32);
                    odinSymbols.addAll(generateSwizzleFields(XY, TsOdinBuiltInTypes.F32));
                    return OdinSymbolTable.from(odinSymbols);
                }

                if (type == TsOdinBuiltInTypes.COMPLEX128) {
                    List<OdinSymbol> odinSymbols = generateSwizzleFields(RG, TsOdinBuiltInTypes.F64);
                    odinSymbols.addAll(generateSwizzleFields(XY, TsOdinBuiltInTypes.F64));
                    return OdinSymbolTable.from(odinSymbols);
                }
            }

            if (includeReferenceableSymbols && TsOdinBuiltInTypes.getQuaternionTypes().contains(type)) {
                if (type == TsOdinBuiltInTypes.QUATERNION64) {
                    List<OdinSymbol> odinSymbols = generateSwizzleFields(RGBA, TsOdinBuiltInTypes.F16);
                    odinSymbols.addAll(generateSwizzleFields(XYZW, TsOdinBuiltInTypes.F16));
                    return OdinSymbolTable.from(odinSymbols);
                }

                if (type == TsOdinBuiltInTypes.QUATERNION128) {
                    List<OdinSymbol> odinSymbols = generateSwizzleFields(RGBA, TsOdinBuiltInTypes.F32);
                    odinSymbols.addAll(generateSwizzleFields(XYZW, TsOdinBuiltInTypes.F32));
                    return OdinSymbolTable.from(odinSymbols);
                }

                if (type == TsOdinBuiltInTypes.QUATERNION256) {
                    List<OdinSymbol> odinSymbols = generateSwizzleFields(RGBA, TsOdinBuiltInTypes.F64);
                    odinSymbols.addAll(generateSwizzleFields(XYZW, TsOdinBuiltInTypes.F64));
                    return OdinSymbolTable.from(odinSymbols);
                }
            }

        } else if (typeReference.referencedType().baseType(true) instanceof TsOdinBitSetType tsOdinBitSetType) {
            if (tsOdinBitSetType.getElementType() instanceof TsOdinEnumType tsOdinEnumType) {
                return symbolTable.with(getEnumFields((OdinEnumType) tsOdinEnumType.getPsiType()));
            }

        } else if (typeReference.referencedType().baseType() instanceof TsOdinObjcClass tsOdinObjcClass) {
            symbolTable.merge(getObjcClassMembers(tsOdinObjcClass, TsOdinObjcMemberInfo::isValidClassMethod));
        }
        return symbolTable;
    }

    public static List<TsOdinObjcMemberInfo> getObjcClassMembers(TsOdinObjcClass tsOdinObjcClass) {
        OdinSymbolTable fileScopeSymbolTable = getObjcClassSearchScope(tsOdinObjcClass);
        if (fileScopeSymbolTable == null)
            return Collections.emptyList();

        List<TsOdinObjcMemberInfo> members = new ArrayList<>();
        // Add the members of this class
        for (OdinSymbol symbol : fileScopeSymbolTable.flatten().getSymbols()) {
            if (symbol.getDeclaration() instanceof OdinConstantInitDeclaration odinConstantInitDeclaration) {
                TsOdinObjcMemberInfo objcMemberInfo = getObjcClassInfo(odinConstantInitDeclaration);
                if (objcMemberInfo.objcName() == null)
                    continue;

                if (objcMemberInfo.objcType() == null)
                    continue;

                if (!objcMemberInfo.isValidInstanceMember() && !objcMemberInfo.isValidClassMethod())
                    continue;

                TsOdinType referencedType = objcMemberInfo.objcType().asType();
                String name = objcMemberInfo.objcName().asString();
                if (referencedType != null && name != null) {
                    if (referencedType.getPsiType() == tsOdinObjcClass.getPsiType()) {
                        members.add(objcMemberInfo);
                    }
                }
            }
        }

        TsOdinStructType structType = tsOdinObjcClass.getStructType();
        OdinStructType psiStruct = structType.type();
        List<OdinFieldDeclaration> fields = OdinInsightUtils.getStructFieldsDeclarationStatements(psiStruct);
        for (OdinFieldDeclaration field : fields) {
            if (field.getUsing() != null && field.getType() != null) {
                List<TsOdinObjcMemberInfo> objcClassMembers = field.getType().getObjcClassMembers();
                members.addAll(objcClassMembers);
            }
        }

        return members;
    }

    private static OdinSymbolTable getObjcClassMembers(TsOdinObjcClass tsOdinObjcClass, Predicate<TsOdinObjcMemberInfo> predicate) {
        OdinSymbolTable symbolTable = new OdinSymbolTable();
        List<TsOdinObjcMemberInfo> objcClassMembers = getObjcClassMembers(tsOdinObjcClass);
        List<TsOdinObjcMemberInfo> filtered = objcClassMembers.stream().filter(predicate).toList();

        for (TsOdinObjcMemberInfo objcMemberInfo : filtered) {
            TsOdinType referencedType = objcMemberInfo.objcType().asType();
            String name = objcMemberInfo.objcName().asString();
            OdinAttributeNamedValue namedValue = OdinInsightUtils.getAttributeNamedValue(objcMemberInfo.attributes(), "objc_name");
            List<OdinSymbol> symbols = OdinDeclarationSymbolResolver.getSymbols(objcMemberInfo.declaration());
            if (symbols.size() == 1) {
                OdinSymbol symbol = symbols.getFirst();
                // Prioritize more specialized classes
                if (symbolTable.getSymbol(name) != null)
                    continue;
                OdinSymbol odinSymbol = symbol
                        .withName(name)
                        .withSymbolType(OBJC_MEMBER)
                        .withDeclaredIdentifier(namedValue);
                symbolTable.add(odinSymbol);
            }
        }

        return symbolTable;
    }

    private static @Nullable OdinSymbolTable getObjcClassSearchScope(TsOdinObjcClass tsOdinObjcClass) {
        if (!tsOdinObjcClass.getPsiType().isValid())
            return OdinSymbolTable.EMPTY;
        OdinFileScope fileScope = PsiTreeUtil.getParentOfType(tsOdinObjcClass.getPsiType(), OdinFileScope.class);
        OdinSymbolTable fileScopeSymbolTable;
        if (fileScope != null) {
            fileScopeSymbolTable = fileScope.getSymbolTable();
        } else {
            fileScopeSymbolTable = null;
        }
        return fileScopeSymbolTable;
    }

    private static @NotNull TsOdinObjcMemberInfo getObjcClassInfo(OdinConstantInitDeclaration declaration) {
        List<OdinAttributesDefinition> attributes = declaration.getAttributesDefinitionList();
        EvOdinValue objcType = OdinInsightUtils.getAttributeValue(attributes,
                "objc_type");
        EvOdinValue objcName = OdinInsightUtils.getAttributeValue(attributes,
                "objc_name");
        EvOdinValue isClassMethod = OdinInsightUtils.getAttributeValue(attributes,
                "objc_is_class_method");
        return new TsOdinObjcMemberInfo(declaration, attributes, objcType, objcName, isClassMethod);
    }

    public static List<OdinSymbol> getBitFieldFields(TsOdinBitFieldType tsOdinBitFieldType) {
        if (tsOdinBitFieldType.getPsiType() instanceof OdinBitFieldType bitFieldType) {
            return getBitFieldFields(bitFieldType);
        }
        return Collections.emptyList();
    }

    public static @NotNull OdinSymbolTable getPackageReferenceSymbols(
            OdinContext context,
            Project project,
            TsOdinPackageReferenceType packageType,
            boolean includeBuiltin) {
        OdinSymbolTable symbolTable = OdinImportUtils
                .getSymbolsOfImportedPackage(context, packageType.getReferencingPackagePath(), (OdinImportDeclaration) packageType.getDeclaration());
        if (includeBuiltin) {
            List<OdinSymbol> builtInSymbols = OdinSdkService.getInstance(project).getBuiltInSymbols();
            OdinSymbolTable odinBuiltinSymbolsTable = OdinSymbolTable.from(builtInSymbols);
            symbolTable.setRoot(odinBuiltinSymbolsTable);
        }
        return symbolTable;
    }

    @SuppressWarnings("unused")
    public static boolean getEnumeratedArraySymbols(OdinContext context, TsOdinArrayType tsOdinArrayType) {
        var psiSizeElement = tsOdinArrayType.getPsiSizeElement();
        OdinExpression expression = psiSizeElement.getExpression();

        if (expression != null) {
            TsOdinType sizeType = expression.getInferredType(context);
            if (sizeType instanceof TsOdinTypeReference sizeTypeReference && sizeTypeReference.getTargetTypeKind() == TsOdinTypeKind.ENUM) {
                List<OdinSymbol> enumFields = getEnumFields((OdinEnumType) sizeType.getPsiType());
                context.getSymbolTable().addAll(enumFields, true);
                return true;
            }
        }
        return false;
    }

    private static List<TsOdinParameter> getTypeParameters(TsOdinType tsOdinType) {
        if (tsOdinType instanceof TsOdinStructType tsOdinStructType) {
            return tsOdinStructType.getParameters();
        }

        if (tsOdinType instanceof TsOdinUnionType tsOdinUnionType) {
            return tsOdinUnionType.getParameters();
        }

        return Collections.emptyList();
    }

    @Nullable
    public static String getTypeName(OdinType type) {
        OdinDeclaration declaration = PsiTreeUtil.getParentOfType(type, OdinDeclaration.class);
        if (declaration != null) {
            if (!declaration.getDeclaredIdentifiers().isEmpty()) {
                OdinDeclaredIdentifier declaredIdentifier = declaration.getDeclaredIdentifiers().getFirst();
                return declaredIdentifier.getText();
            }
        }
        return null;
    }

    public static @NotNull List<OdinSymbol> getEnumFields(OdinEnumType enumType) {
        OdinEnumBlock enumBlock = enumType
                .getEnumBlock();

        if (enumBlock == null)
            return Collections.emptyList();

        OdinEnumBody enumBody = enumBlock
                .getEnumBody();

        if (enumBody == null)
            return Collections.emptyList();

        List<OdinSymbol> symbols = new ArrayList<>();
        for (OdinEnumValueDeclaration odinEnumValueDeclaration : enumBody
                .getEnumValueDeclarationList()) {
            OdinDeclaredIdentifier identifier = odinEnumValueDeclaration.getDeclaredIdentifier();
            OdinSymbol odinSymbol = new OdinSymbol(identifier);
            odinSymbol.setSymbolType(ENUM_FIELD);
            odinSymbol.setScope(OdinScope.TYPE);
            odinSymbol.setVisibility(OdinVisibility.NONE);
            odinSymbol.setPsiType(enumType);
            symbols.add(odinSymbol);
        }
        return symbols;
    }

    public static List<OdinSymbol> getStructFields(OdinContext context, @NotNull OdinStructType structType) {
        List<OdinFieldDeclaration> fieldDeclarationStatementList = getStructFieldsDeclarationStatements(structType);
        List<OdinSymbol> symbols = new ArrayList<>();
        for (OdinFieldDeclaration field : fieldDeclarationStatementList) {
            for (OdinDeclaredIdentifier odinDeclaredIdentifier : field.getDeclaredIdentifiers()) {
                boolean hasUsing = field.getUsing() != null;
                OdinSymbol odinSymbol = new OdinSymbol(odinDeclaredIdentifier);
                odinSymbol.setSymbolType(STRUCT_FIELD);
                odinSymbol.setPsiType(field.getType());
                odinSymbol.setScope(OdinScope.TYPE);
                odinSymbol.setHasUsing(hasUsing);
                symbols.add(odinSymbol);
                if (field.getDeclaredIdentifiers().size() == 1 && hasUsing) {
                    getSymbolsOfFieldWithUsing(context, field, symbols);
                }
            }
        }
        return symbols;
    }

    public static void getSymbolsOfFieldWithUsing(OdinContext context,
                                                  OdinFieldDeclaration field,
                                                  List<OdinSymbol> symbols) {
        if (field.getType() == null) {
            return;
        }

        TsOdinType tsOdinType = OdinTypeResolver.resolveType(context, field.getType());
        TsOdinType baseType = tsOdinType.baseType(true);
        if (baseType != null) {
            TsOdinStructType structType = unwrapStructType(baseType);
            if (structType != null) {
                OdinType psiType = structType.getPsiType();
                if (psiType instanceof OdinStructType psiStructType) {
                    List<OdinSymbol> structFields = getStructFields(structType.getContext(), psiStructType);
                    symbols.addAll(structFields);
                }
            }
        }
    }

    private static @Nullable TsOdinStructType unwrapStructType(@NotNull TsOdinType tsOdinType) {
        TsOdinStructType structType;
        TsOdinType baseType = tsOdinType.baseType(true);
        if (baseType instanceof TsOdinPointerType tsOdinPointerType) {
            TsOdinType baseDereferencedType = tsOdinPointerType.getDereferencedType().baseType(true);
            if (baseDereferencedType instanceof TsOdinStructType) {
                structType = (TsOdinStructType) baseDereferencedType;
            } else {
                structType = null;
            }
        } else if (baseType instanceof TsOdinStructType tsOdinStructType) {
            structType = tsOdinStructType;
        } else {
            structType = null;
        }
        return structType;
    }

    public static @NotNull List<OdinFieldDeclaration> getStructFieldsDeclarationStatements(OdinStructType structType) {
        OdinStructBlock structBlock = structType
                .getStructBlock();

        if (structBlock == null)
            return Collections.emptyList();
        OdinStructBody structBody = structBlock
                .getStructBody();

        List<OdinFieldDeclaration> fieldDeclarationStatementList;
        if (structBody == null) {
            fieldDeclarationStatementList = Collections.emptyList();
        } else {
            fieldDeclarationStatementList = structBody.getFieldDeclarationList();
        }
        return fieldDeclarationStatementList;
    }

    public static List<OdinSymbol> getTypeElements(OdinContext context, OdinExpression expression) {
        TsOdinType tsOdinType = expression.getInferredType(context);
        if (tsOdinType instanceof TsOdinTypeReference tsOdinTypeReference) {
            return getTypeElements(context, OdinTypeResolver.resolveTypeReference(context, tsOdinTypeReference)
                    .baseType(true));
        }
        return getTypeElements(context, tsOdinType.baseType(true));
    }

    public static List<OdinSymbol> getTypeElements(OdinContext context, OdinType type) {
        TsOdinType tsOdinType = OdinTypeResolver.resolveType(context, type);
        return getTypeElements(context, tsOdinType);
    }

    public static @NotNull List<OdinSymbol> getTypeElements(OdinContext context, TsOdinType tsOdinType) {
        if (tsOdinType instanceof TsOdinStructType structType) {
            if (structType.getPsiType() instanceof OdinStructType psiStructType) {
                return getStructFields(context, psiStructType);
            }
        }

        if (tsOdinType instanceof TsOdinPointerType pointerType) {
            return getTypeElements(context, pointerType.getDereferencedType());
        }

        if (tsOdinType instanceof TsOdinEnumType enumType) {
            return getEnumFields((OdinEnumType) enumType.getPsiType());
        }

        if (tsOdinType instanceof TsOdinBitFieldType bitFieldType) {
            if (bitFieldType.getPsiType() instanceof OdinBitFieldType psiBitFieldType) {
                return getBitFieldFields(psiBitFieldType);
            }
            return Collections.emptyList();
        }

        if (tsOdinType instanceof TsOdinPackageReferenceType packageReferenceType) {
            OdinSymbolTable symbolTable = OdinImportUtils
                    .getSymbolsOfImportedPackage(context, packageReferenceType.getReferencingPackagePath(),
                            (OdinImportDeclaration) packageReferenceType.getDeclaration());
            return new ArrayList<>(symbolTable.getSymbols());
        }

        return Collections.emptyList();
    }

    private static List<OdinSymbol> getBitFieldFields(OdinBitFieldType psiBitFieldType) {
        List<OdinSymbol> odinSymbols = new ArrayList<>();
        OdinBitFieldBlock bitFieldBlock = psiBitFieldType.getBitFieldBlock();
        if (bitFieldBlock != null) {
            OdinBitFieldBody bitFieldBody = bitFieldBlock.getBitFieldBody();
            if (bitFieldBody != null) {
                for (OdinBitFieldFieldDeclaration odinBitFieldFieldDeclaration : bitFieldBody.getBitFieldFieldDeclarationList()) {
                    OdinSymbol symbol = createBitFieldSymbol(odinBitFieldFieldDeclaration);
                    odinSymbols.add(symbol);
                }

            }
        }
        return odinSymbols;
    }

    public static @NotNull OdinSymbol createBitFieldSymbol(OdinBitFieldFieldDeclaration odinBitFieldFieldDeclaration) {
        OdinDeclaredIdentifier declaredIdentifier = odinBitFieldFieldDeclaration.getDeclaredIdentifier();
        OdinSymbol symbol = new OdinSymbol();
        symbol.setSymbolType(BIT_FIELD_FIELD);
        symbol.setName(declaredIdentifier.getName());
        symbol.setDeclaredIdentifier(declaredIdentifier);
        symbol.setImplicitlyDeclared(false);
        symbol.setScope(OdinScope.TYPE);
        symbol.setVisibility(OdinVisibility.NONE);
        symbol.setHasUsing(false);
        symbol.setPsiType(odinBitFieldFieldDeclaration.getType());
        return symbol;
    }

    // Swizzle field generator

    public static @NotNull List<OdinSymbol> getSwizzleFields(TsOdinArrayType tsOdinArrayType) {
        if (tsOdinArrayType.getSize() == null) {
            List<OdinSymbol> symbols = new ArrayList<>();
            symbols.addAll(generateSwizzleFields(RGBA, tsOdinArrayType.getElementType()));
            symbols.addAll(generateSwizzleFields(XYZW, tsOdinArrayType.getElementType()));
            return symbols;
        }
        return generateArraySwizzleFields(tsOdinArrayType.getSize(), tsOdinArrayType.getElementType());
    }


    private static List<OdinSymbol> generateSwizzleFields(char[] input, TsOdinType elementType) {

        List<String> result = new ArrayList<>();
        // Generate combinations for lengths from 1 to maxLen
        for (int len = 1; len <= input.length; len++) {
            generateCombinations(input, "", len, result);
        }

        List<OdinSymbol> symbols = new ArrayList<>();
        // Print the results
        for (String s : result) {
            OdinSymbol odinSymbol = new OdinSymbol();
            odinSymbol.setSymbolType(SWIZZLE_FIELD);
            odinSymbol.setScope(OdinScope.TYPE);
            odinSymbol.setVisibility(OdinVisibility.NONE);
            odinSymbol.setName(s);
            odinSymbol.setImplicitlyDeclared(true);
            if (elementType != null) {
                odinSymbol.setPsiType(elementType.getPsiType());
            }
            symbols.add(odinSymbol);
        }

        return symbols;
    }
    // Recursive function to generate combinations

    private static void generateCombinations(char[] input, String current, int length, List<String> result) {
        if (length == 0) {
            result.add(current);
            return;
        }

        for (char c : input) {
            generateCombinations(input, current + c, length - 1, result);
        }
    }


    public static OdinRefExpression findTopMostRefExpression(PsiElement element) {
        List<OdinRefExpression> odinRefExpressions = unfoldRefExpressions(element);
        if (odinRefExpressions.isEmpty())
            return null;
        return odinRefExpressions.getLast();
    }

    public static @NotNull List<OdinRefExpression> unfoldRefExpressions(PsiElement element) {
        return PsiTreeUtil.collectParents(element,
                OdinRefExpression.class,
                true,
                p -> OPERAND_BOUNDARY_CLASSES.stream().anyMatch(s -> s.isInstance(p)));
    }

    public static boolean isVariableDeclaration(PsiElement element) {
        return PsiTreeUtil.getParentOfType(element, true, OdinVariableDeclaration.class) != null;
    }

    public static boolean isProcedureDeclaration(PsiElement element) {
        return isTypeDeclaration(element, OdinProcedureType.class) || isTypeDeclaration(element, OdinProcedureLiteralType.class);
    }

    public static boolean isProcedureGroupDeclaration(PsiElement element) {
        return isTypeDeclaration(element, OdinProcedureGroupType.class);
    }

    public static boolean isConstantDeclaration(PsiElement element) {
        if (element.getParent() instanceof OdinConstantInitDeclaration constantInitDeclaration) {
            return constantInitDeclaration.getExpressionList().size() > 1
                    || (
                    constantInitDeclaration.getExpressionList().size() == 1 &&
                            !(
                                    constantInitDeclaration.getExpressionList().getFirst() instanceof OdinTypeDefinitionExpression
                            ));
        }

        return false;
    }

    public static boolean isStructDeclaration(PsiElement element) {
        return isTypeDeclaration(element, OdinStructType.class);
    }

    public static boolean isEnumDeclaration(PsiElement element) {
        return isTypeDeclaration(element, OdinEnumType.class);
    }

    public static boolean isUnionDeclaration(PsiElement element) {
        return isTypeDeclaration(element, OdinUnionType.class);
    }

    public static @Nullable OdinType getDeclaredType(PsiElement element) {
        OdinDeclaration declaration
                = PsiTreeUtil.getParentOfType(element,
                OdinDeclaration.class,
                false);
        if (declaration instanceof OdinConstantInitDeclaration constantInitDeclaration) {
            return getDeclaredType(constantInitDeclaration);
        }
        return null;
    }

    public static String getPackageClauseName(PsiElement element) {
        if (element instanceof OdinFile odinFile)
            return getPackageClauseName(odinFile);

        OdinFileScope fileScope = PsiTreeUtil.getParentOfType(element, false, OdinFileScope.class);
        if (fileScope != null) {
            OdinPackageClause packageClause = fileScope.getPackageClause();
            return packageClause.getName();
        }

        return null;
    }

    public static String getPackageClauseName(OdinFile file) {
        OdinFileScope fileScope = file.getFileScope();
        if (fileScope != null) {
            OdinPackageClause packageClause = fileScope.getPackageClause();
            return packageClause.getName();
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T extends OdinType> T getDeclaredType(PsiElement element, @NotNull Class<T> typeClass) {
        if (element == null)
            return null;
        OdinType declaredType = getDeclaredType(element);
        if (typeClass.isInstance(declaredType))
            return (T) declaredType;
        return null;
    }

    public static OdinProcedureType getProcedureType(PsiElement element) {
        OdinProcedureType procedureType = getDeclaredType(element, OdinProcedureType.class);
        if (procedureType != null)
            return procedureType;

        OdinProcedureLiteralType procedureLiteralType = getDeclaredType(element, OdinProcedureLiteralType.class);
        if (procedureLiteralType != null) {
            return procedureLiteralType.getProcedureDefinition().getProcedureSignature().getProcedureType();
        }
        return null;
    }

    public static @Nullable OdinType getDeclaredType(OdinConstantInitDeclaration constantInitDeclaration) {
        List<OdinExpression> expressionList = constantInitDeclaration.getExpressionList();
        if (expressionList.isEmpty())
            return null;

        OdinExpression expression = expressionList.getFirst();
        if (expression instanceof OdinTypeDefinitionExpression typeDefinitionExpression) {
            return typeDefinitionExpression.getType();
        }

        if (expression instanceof OdinProcedureExpression procedureExpression) {
            return procedureExpression.getProcedureLiteralType();
        }
        return null;
    }

    public static boolean isTypeDeclaration(PsiElement element, Class<? extends OdinType> typeClass) {
        OdinType declaredType = getDeclaredType(element);
        return typeClass.isInstance(declaredType);
    }

    private static boolean isFieldDeclaration(PsiNamedElement element) {
        return element.getParent() instanceof OdinFieldDeclaration;
    }

    private static boolean isPackageDeclaration(PsiNamedElement element) {
        return element instanceof OdinImportDeclaration
                || element.getParent() instanceof OdinImportDeclaration;
    }

    public static OdinSymbolType classify(PsiNamedElement element) {
        if (isStructDeclaration(element)) {
            return STRUCT;
        } else if (isEnumDeclaration(element)) {
            return ENUM;
        } else if (isUnionDeclaration(element)) {
            return UNION;
        } else if (isProcedureDeclaration(element)) {
            return PROCEDURE;
        } else if (isVariableDeclaration(element)) {
            return VARIABLE;
        } else if (isConstantDeclaration(element)) {
            return CONSTANT;
        } else if (isProcedureGroupDeclaration(element)) {
            return PROCEDURE_OVERLOAD;
        } else if (isPackageDeclaration(element)) {
            return PACKAGE_REFERENCE;
        } else if (isFieldDeclaration(element)) {
            return STRUCT_FIELD;
        } else if (isParameterDeclaration(element)) {
            return PARAMETER;
        } else {
            return UNKNOWN;
        }
    }

    public static boolean isParameterDeclaration(PsiElement element) {
        return PsiTreeUtil.getParentOfType(element, true, OdinParameterDeclaration.class) != null;
    }

    public static String getLineColumn(@NotNull PsiElement element) {
        PsiFile containingFile = element.getContainingFile();
        if (containingFile != null) {
            LineColumn lineColumn = offsetToLineColumn(containingFile.getText(), element.getTextOffset());
            return (lineColumn.line) + ":" + (lineColumn.column);
        }
        return "<unknown>:<unknown>";
    }

    public static @Nullable OdinEnumType getEnumTypeOfArray(OdinContext context, TsOdinArrayType tsOdinArrayType) {
        var psiSizeElement = tsOdinArrayType.getPsiSizeElement();
        OdinExpression expression = psiSizeElement.getExpression();

        OdinEnumType psiType;
        if (expression != null) {
            TsOdinType sizeType = expression.getInferredType(context);
            if (sizeType instanceof TsOdinTypeReference sizeTypeReference && sizeTypeReference.getTargetTypeKind() == TsOdinTypeKind.ENUM) {
                psiType = (OdinEnumType) sizeType.getPsiType();
            } else {
                psiType = null;
            }
        } else {
            psiType = null;
        }
        return psiType;
    }

    public static @NotNull List<OdinSymbol> getElementSwizzleFields(TsOdinArrayType tsOdinArrayType) {
        List<OdinSymbol> swizzleSymbols = new ArrayList<>();
        List<String> swizzleSymbolsNames = List.of("r", "g", "b", "a", "x", "y", "z", "w");
        for (String swizzleSymbol : swizzleSymbolsNames) {
            OdinSymbol odinSymbol = new OdinSymbol();
            odinSymbol.setName(swizzleSymbol);

            TsOdinType elementType = tsOdinArrayType.getElementType();
            if (elementType != null) {
                odinSymbol.setPsiType(elementType.getPsiType());
            }
            odinSymbol.setVisibility(OdinVisibility.NONE);
            odinSymbol.setImplicitlyDeclared(true);
            odinSymbol.setScope(OdinScope.TYPE);
            odinSymbol.setSymbolType(SWIZZLE_FIELD);
            swizzleSymbols.add(odinSymbol);
        }
        return swizzleSymbols;
    }

    public static List<OdinSymbol> getElementSymbols(TsOdinType tsOdinType, OdinContext context) {
        List<OdinSymbol> elementSymbols = new ArrayList<>();
        tsOdinType = tsOdinType.baseType(true);
        if (tsOdinType instanceof TsOdinStructType tsOdinStructType) {
            List<OdinSymbol> typeSymbols = getTypeElements(context, tsOdinStructType);
            elementSymbols.addAll(typeSymbols);
        }

        if (tsOdinType instanceof TsOdinBitFieldType tsOdinBitFieldType) {
            List<OdinSymbol> typeSymbols = getTypeElements(context, tsOdinBitFieldType);
            elementSymbols.addAll(typeSymbols);
        }

        if (tsOdinType instanceof TsOdinArrayType tsOdinArrayType) {
            OdinEnumType enumTypeOfArray = getEnumTypeOfArray(context, tsOdinArrayType);
            if (enumTypeOfArray != null) {
                elementSymbols.addAll(getEnumFields(enumTypeOfArray));

            } else {
                List<OdinSymbol> swizzleSymbols = getElementSwizzleFields(tsOdinArrayType);
                elementSymbols.addAll(swizzleSymbols);
            }
        }

        return elementSymbols;
    }

    public static PsiElement findParentOfType(PsiElement psiElement, boolean strict, Class<?>[] parentTypes, Class<?>[] stopAt) {
        boolean[] invalid = {false};
        final PsiElement[] lastFoundElement = {null};

        PsiElement firstParent = PsiTreeUtil.findFirstParent(psiElement, strict, p -> {
            for (Class<?> clazz : stopAt) {
                if (clazz.isInstance(p)) {
                    invalid[0] = true;
                    lastFoundElement[0] = p;
                    return true;
                }
            }

            for (Class<?> clazz : parentTypes) {
                if (clazz.isInstance(p)) {
                    invalid[0] = false;
                    lastFoundElement[0] = p;
                    return true;
                }
            }
            return false;
        });

        if (!invalid[0]) {
            return firstParent;
        }

        if (lastFoundElement[0] != null) {
            for (Class<?> parentType : parentTypes) {
                if (parentType.isInstance(lastFoundElement[0]))
                    return lastFoundElement[0];
            }
        }

        return null;
    }

    public static @Nullable OdinSymbol findBuiltinSymbolOfCallExpression(OdinContext context, OdinCallExpression callExpression, Predicate<String> identifierPredicate) {
        OdinSymbol symbol = null;
        if (callExpression.getExpression() instanceof OdinRefExpression callRefExpression
                && callRefExpression.getExpression() == null
                && callRefExpression.getIdentifier() != null) {

            String text = callRefExpression.getIdentifier().getText();
            if (identifierPredicate.test(text)) {
                return callRefExpression.getIdentifier().getReferencedSymbol(context);
            }
        }
        return null;
    }

    public static @Nullable Map<OdinExpression, TsOdinParameter> getArgumentToParameterMap(List<TsOdinParameter> parameters,
                                                                                           @NotNull List<OdinArgument> argumentList) {
        return getArgumentToParameterMap(parameters, argumentList, false);
    }

    public static @Nullable Map<OdinExpression, TsOdinParameter> getArgumentToParameterMap(List<TsOdinParameter> parameters,
                                                                                           @NotNull List<OdinArgument> argumentList,
                                                                                           boolean includeDefaultParameters) {

        Map<String, TsOdinParameter> parametersByName = parameters.stream()
                .filter(p -> p.getName() != null)
                .collect(Collectors.toMap(
                        TsOdinParameter::getName,
                        v -> v,
                        // NOTE: handles case where there are multiple parameters named "_". This only can happen in procedure
                        //  type definitions (as opposed to procedure literals).
                        (v1, v2) -> v1
                ));

        Map<Integer, TsOdinParameter> parametersByIndex = parameters.stream().collect(Collectors.toMap(
                TsOdinParameter::getIndex,
                v -> v
        ));

        Map<OdinExpression, TsOdinParameter> argumentExpressions = new HashMap<>();
        int index = 0;

        ArrayList<TsOdinParameter> usedParameters = new ArrayList<>(parameters);
        boolean nameOnly = false;
        boolean invalidArguments = false;
        boolean previousIsVariadic = false;
        for (OdinArgument odinArgument : argumentList) {
            if (odinArgument instanceof OdinSelfArgument selfArgument) {
                assert index == 0;
                TsOdinParameter tsOdinParameter = parametersByIndex.get(0);
                argumentExpressions.put(selfArgument.getExpression(), tsOdinParameter);
                usedParameters.remove(tsOdinParameter);
            }
            if (odinArgument instanceof OdinUnnamedArgument unnamedArgument) {
                if (nameOnly) {
                    invalidArguments = true;
                    break;
                }
                TsOdinParameter tsOdinParameter = parametersByIndex.get(index);
                if (tsOdinParameter == null) {
                    invalidArguments = true;
                    break;
                }

                argumentExpressions.put(unnamedArgument.getExpression(), tsOdinParameter);
                usedParameters.remove(tsOdinParameter);
                if (!(tsOdinParameter.getPsiType() instanceof OdinVariadicType)) {
                    previousIsVariadic = true;
                    index++;
                } else {
                    previousIsVariadic = false;
                }
                continue;
            }

            if (odinArgument instanceof OdinNamedArgument namedArgument) {
                if (previousIsVariadic) {
                    previousIsVariadic = false;
                    index++;
                }

                TsOdinParameter tsOdinParameter = parametersByName.get(namedArgument.getIdentifier().getText());
                if (tsOdinParameter == null || !usedParameters.contains(tsOdinParameter)) {
                    invalidArguments = true;
                    break;
                }

                if (tsOdinParameter.getPsiType() instanceof OdinVariadicType) {
                    invalidArguments = true;
                    break;
                }

                nameOnly = true;
                argumentExpressions.put(namedArgument.getExpression(), tsOdinParameter);
                usedParameters.remove(tsOdinParameter);
            }

            index++;
        }

        for (int i = usedParameters.size() - 1; i >= 0; i--) {
            TsOdinParameter tsOdinParameter = usedParameters.get(i);
            if (tsOdinParameter.getDefaultValueExpression() != null) {
                if (includeDefaultParameters) {
                    argumentExpressions.put(tsOdinParameter.getDefaultValueExpression(), tsOdinParameter);
                }
                usedParameters.remove(tsOdinParameter);
            } else if (!(tsOdinParameter.getPsiType() instanceof OdinVariadicType)) {
                invalidArguments = true;
                break;
            }
        }

        if (invalidArguments)
            return null;
        return argumentExpressions;
    }

    public static TsOdinType getReferenceableType(TsOdinType tsOdinRefExpressionType) {
        if (tsOdinRefExpressionType instanceof TsOdinTypeAlias typeAlias) {
            return getReferenceableType(typeAlias.getBaseType());
        }

        if (tsOdinRefExpressionType instanceof TsOdinPointerType pointerType) {
            return getReferenceableType(pointerType.getDereferencedType());
        }

        return tsOdinRefExpressionType;
    }

    public static boolean isDistinct(OdinExpression firstExpression) {
        boolean distinct;
        if (firstExpression instanceof OdinTypeDefinitionExpression typeDefinitionExpression) {
            distinct = typeDefinitionExpression.getDistinct() != null;
        } else if (firstExpression instanceof OdinProcedureExpression procedureExpression) {
            distinct = procedureExpression.getDistinct() != null;
        } else {
            distinct = false;
        }
        return distinct;
    }

    public static boolean isLocalVariable(OdinDeclaredIdentifier declaredIdentifier) {
        PsiElement parent = declaredIdentifier.getParent();
        return isLocalVariable(parent);
    }

    public static boolean isGlobalVariable(OdinDeclaredIdentifier declaredIdentifier) {
        PsiElement parent = declaredIdentifier.getParent();
        return (parent instanceof OdinVariableDeclaration)
                && !isLocalVariable(parent);
    }

    public static boolean isLocalVariable(@NotNull PsiElement o) {
        if (isVariable(o)) {
            return isLocal(o);
        }
        return false;
    }

    public static boolean isLocal(@NotNull PsiElement o) {
        OdinProcedureBody fileScope = PsiTreeUtil.getParentOfType(o, OdinProcedureBody.class);


        return fileScope != null;
    }

    public static boolean isForeign(PsiElement o) {
        return PsiTreeUtil.getParentOfType(o, OdinForeignStatementList.class, true) != null;
    }

    private static boolean isVariable(@NotNull PsiElement o) {
        return o instanceof OdinVariableDeclaration;
    }

    public static boolean isStaticVariable(OdinDeclaredIdentifier declaredIdentifier) {
        if (isVariable(declaredIdentifier.getParent())) {
            OdinVariableDeclaration variableDeclaration = (OdinVariableDeclaration) declaredIdentifier.getParent();
            return containsAttribute(variableDeclaration.getAttributesDefinitionList(), "static");
        }
        return false;
    }

    public static boolean isStaticProcedure(OdinDeclaredIdentifier declaredIdentifier) {
        PsiElement parent = declaredIdentifier.getParent();
        if (isProcedureDeclaration(parent)) {
            OdinConstantInitDeclaration constant = (OdinConstantInitDeclaration) parent;
            return containsAttribute(constant.getAttributesDefinitionList(), "static");
        }
        return false;
    }

    public static boolean isStructFieldDeclaration(OdinDeclaredIdentifier declaredIdentifier) {
        return isFieldDeclaration(declaredIdentifier);
    }

    public static boolean isBitFieldFieldDeclaration(OdinDeclaredIdentifier declaredIdentifier) {
        return declaredIdentifier.getParent() instanceof OdinBitFieldFieldDeclaration;
    }

    public static List<OdinSymbol> getStructFields(TsOdinStructType tsOdinStructType) {
        return getStructFields(tsOdinStructType.getContext(), (OdinStructType) tsOdinStructType.getPsiType());
    }

    public static @NotNull OdinCallInfo getCallInfo(OdinContext context, PsiElement argument) {
        @Nullable OdinPsiElement callingElement = PsiTreeUtil.getParentOfType(argument, OdinCallExpression.class, OdinCallType.class);
        TsOdinType tsOdinType = TsOdinBuiltInTypes.UNKNOWN;
        List<OdinArgument> argumentList = Collections.emptyList();
        if (callingElement != null) {
            if (callingElement instanceof OdinCallExpression odinCallExpression) {
                tsOdinType = odinCallExpression.getExpression().getInferredType(context);
                // Here we have to get a meta type, otherwise the call expression does not make sense
                if (tsOdinType instanceof TsOdinTypeReference tsOdinTypeReference) {
                    tsOdinType = tsOdinTypeReference.referencedType();
                } else if (!(tsOdinType.baseType(true) instanceof TsOdinProcedureType)
                        && !(tsOdinType instanceof TsOdinPseudoMethodType tsOdinPseudoMethodType)
                        && !(tsOdinType instanceof TsOdinObjcMember)) {
                    tsOdinType = TsOdinBuiltInTypes.UNKNOWN;
                }

                if (tsOdinType instanceof TsOdinPseudoMethodType || tsOdinType instanceof TsOdinObjcMember) {
                    argumentList = OdinInferenceEngine.createArgumentListWithSelf(odinCallExpression);
                } else {
                    argumentList = odinCallExpression.getArgumentList();
                }
            } else if (callingElement instanceof OdinCallType odinCallType) {
                tsOdinType = OdinTypeResolver.resolveType(context, odinCallType.getType());
                argumentList = odinCallType.getArgumentList();
            }
        }
        return new OdinCallInfo(callingElement, tsOdinType, argumentList);
    }

    public static <T extends PsiElement> T findPrevParent(PsiElement ancestor, PsiElement child, Class<T> clazz) {
        return findPrevParent(ancestor, child, true, clazz);
    }

    @SuppressWarnings("unchecked")
    public static <T extends PsiElement> T findPrevParent(PsiElement ancestor, PsiElement child, boolean strict, Class<T> clazz) {
        if (ancestor == child && !strict) {
            if (clazz.isInstance(ancestor)) {
                return (T) ancestor;
            }
        }

        PsiElement prevParent = PsiTreeUtil.findPrevParent(ancestor, child);
        if (clazz.isInstance(prevParent)) {
            return (T) prevParent;
        }

        return null;
    }

    public static boolean isVisible(PsiElement reference,
                                    OdinSymbol symbol) {
        if (symbol.isBuiltin()
                || symbol.getScope() == OdinScope.LOCAL
                || symbol.getVisibility() == OdinVisibility.PACKAGE_EXPORTED)
            return true;

        if (symbol.getDeclaration() == null)
            return true;

        VirtualFile referenceFile = getContainingVirtualFile(reference);
        VirtualFile declarationFile = getContainingVirtualFile(symbol.getDeclaration());

        if (referenceFile.equals(declarationFile))
            return true;

        if (symbol.getVisibility() == OdinVisibility.FILE_PRIVATE
                || symbol.getSymbolType() == PACKAGE_REFERENCE)
            return false;

        if (symbol.getVisibility() == OdinVisibility.PACKAGE_PRIVATE) {
            VirtualFile referencePackageFile = referenceFile.getParent();
            VirtualFile declarationPackageFile = declarationFile.getParent();
            return Objects.equals(referencePackageFile, declarationPackageFile);

        }
        return true;
    }

    public static void logStackOverFlowError(@NotNull PsiElement element, Logger log) {
        String text = element.getText();
        int textOffset = element.getTextOffset();
        PsiFile containingFile = element.getContainingFile();
        String fileName = "UNKNOWN";
        if (containingFile != null) {
            VirtualFile virtualFile = containingFile.getVirtualFile();
            if (virtualFile != null) {
                fileName = virtualFile.getCanonicalPath();
            }
            LineColumn lineColumn = offsetToLineColumn(containingFile.getText(), textOffset);
            log.error("Stack overflow caused by element with text '%s' in %s:%d:%d".formatted(text,
                    fileName,
                    lineColumn.line,
                    lineColumn.column));
        } else {
            log.error("Stack overflow caused by element with text '%s'".formatted(text));
        }
    }

    // This is a variant of getTypeElements()
    public static OdinSymbolTable getReferenceableSymbols(OdinContext context, OdinExpression valueExpression) {
        // Add filter for referenceable elements
        TsOdinType type = valueExpression.getInferredType(context);
        if (type instanceof TsOdinTypeReference typeReference) {
            TsOdinType tsOdinType = typeReference.referencedType().baseType(true);
            if (tsOdinType instanceof TsOdinEnumType) {
                return getTypeElements(context, valueExpression.getProject(), tsOdinType);
            }
        }
        if (type instanceof TsOdinPackageReferenceType packageReferenceType) {
            return getPackageReferenceSymbols(context, valueExpression.getProject(),
                    packageReferenceType,
                    false);
        }
        return getTypeElements(context, valueExpression.getProject(), type, true);
    }

    // This is a variant  of getTypeElements()
    public static OdinSymbolTable getReferenceableSymbols(OdinContext context, OdinQualifiedType qualifiedType) {
        OdinIdentifier identifier = qualifiedType.getIdentifier();
        OdinSymbol odinSymbol = identifier.getReferencedSymbol(context);
        if (odinSymbol != null) {
            OdinDeclaration odinDeclaration = PsiTreeUtil.getParentOfType(odinSymbol.getDeclaredIdentifier(), false, OdinDeclaration.class);
            if (odinDeclaration instanceof OdinImportDeclaration importDeclaration) {
                return OdinImportUtils.getSymbolsOfImportedPackage(context,
                        OdinImportService.getInstance(qualifiedType.getProject()).getPackagePath(qualifiedType),
                        importDeclaration);
            }
        }
        return OdinSymbolTable.EMPTY;
    }

    public static @NotNull String getLocation(PsiElement psiElement) {
        VirtualFile containingVirtualFile = getContainingVirtualFile(psiElement);
        String lineColumn = getLineColumn(psiElement);

        return "%s:%s".formatted(containingVirtualFile.getPath(), lineColumn);
    }

    public static OdinExpression parenthesesUnwrap(PsiElement element) {
        if (element instanceof OdinParenthesizedExpression par) {
            OdinExpression parExpression = par.getExpression();
            if (parExpression != null) {
                return parenthesesUnwrap(parExpression);
            }
        }

        if (element instanceof OdinExpression odinExpression) {
            return odinExpression;
        }
        return null;
    }

    public static OdinVisibility getGlobalFileVisibility(@NotNull PsiElement element) {
        if (isLocal(element))
            return OdinVisibility.NONE;
        OdinFileScope fileScope = PsiTreeUtil.getParentOfType(element, OdinFileScope.class, false);
        if (fileScope != null)
            return getGlobalFileVisibility(fileScope);
        return OdinVisibility.NONE;
    }

    public static OdinVisibility getGlobalFileVisibility(@NotNull OdinFileScope fileScope) {
        PsiElement lineComment = PsiTreeUtil.skipSiblingsBackward(fileScope, PsiWhiteSpace.class);
        if (lineComment != null) {
            IElementType elementType = PsiUtilCore.getElementType(lineComment.getNode());
            if (elementType == OdinTypes.LINE_COMMENT) {
                if (lineComment.getText().equals("//+private")) {
                    return OdinVisibility.PACKAGE_PRIVATE;
                }

                if (lineComment.getText().equals("//+private file")) {
                    return OdinVisibility.FILE_PRIVATE;
                }
            }
        }

        OdinBuildFlagClause[] buildFlagClauses = PsiTreeUtil.getChildrenOfType(fileScope, OdinBuildFlagClause.class);
        if (buildFlagClauses == null)
            return OdinVisibility.PACKAGE_EXPORTED;

        for (OdinBuildFlagClause buildFlagClause : buildFlagClauses) {

            String prefix = buildFlagClause.getBuildFlagPrefix().getText();
            if (prefix.equals("#+private")) {
                for (OdinBuildFlagArgument buildFlagArgument : buildFlagClause.getBuildFlagArgumentList()) {
                    if (buildFlagArgument.getBuildFlagList().size() > 1)
                        continue;

                    OdinBuildFlag buildFlag = buildFlagArgument.getBuildFlagList().getFirst();
                    if (!(buildFlag instanceof OdinBuildFlagIdentifier buildFlagIdentifier))
                        continue;
                    if (buildFlagIdentifier.getBuildFlagIdentifierToken()
                            .getText()
                            .trim()
                            .equals("file")) {
                        return OdinVisibility.FILE_PRIVATE;
                    }
                }
                return OdinVisibility.PACKAGE_PRIVATE;
            }
        }

        return OdinVisibility.PACKAGE_EXPORTED;
    }

    public static boolean containsAttribute(List<OdinAttributesDefinition> attributesDefinitions, String attributeName) {
        if (attributesDefinitions == null)
            return false;

        for (OdinAttributesDefinition attributesDefinition : attributesDefinitions) {

            for (OdinAttributeArgument odinArgument : attributesDefinition.getAttributeArgumentList()) {
                if (odinArgument instanceof OdinUnassignedAttribute unassignedAttribute) {
                    if (unassignedAttribute.getText().equals(attributeName)) {
                        return true;
                    }
                }

                if (odinArgument instanceof OdinAssignedAttribute assignedAttribute) {
                    if (assignedAttribute.getAttributeIdentifier().getText().equals(attributeName)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static EvOdinValue getAttributeValue(List<OdinAttributesDefinition> attributesDefinitions, String attributeName) {
        if (attributesDefinitions == null)
            return null;

        for (OdinAttributesDefinition attributesDefinition : attributesDefinitions) {
            for (OdinAttributeArgument odinArgument : attributesDefinition.getAttributeArgumentList()) {
                if (odinArgument instanceof OdinUnassignedAttribute unassignedAttribute) {
                    if (unassignedAttribute.getText().equals(attributeName)) {
                        return EvOdinValues.nullValue();
                    }
                }

                if (odinArgument instanceof OdinAssignedAttribute assignedAttribute) {
                    if (assignedAttribute.getAttributeIdentifier().getText().equals(attributeName)) {
                        return OdinExpressionEvaluator.evaluate(assignedAttribute.getAttributeNamedValue().getExpression());
                    }
                }
            }
        }

        return null;
    }

    public static OdinAttributeNamedValue getAttributeNamedValue(List<OdinAttributesDefinition> attributesDefinitions, String attributeName) {
        if (attributesDefinitions == null)
            return null;

        for (OdinAttributesDefinition attributesDefinition : attributesDefinitions) {
            for (OdinAttributeArgument odinArgument : attributesDefinition.getAttributeArgumentList()) {
                if (odinArgument instanceof OdinAssignedAttribute assignedAttribute) {
                    if (assignedAttribute.getAttributeIdentifier().getText().equals(attributeName)) {
                        return assignedAttribute.getAttributeNamedValue();
                    }
                }
            }
        }
        return null;
    }

    public static @NotNull OdinVisibility computeVisibility(@NotNull Collection<OdinAttributesDefinition> attributesDefinitions) {
        for (OdinAttributesDefinition odinAttributesDefinition : attributesDefinitions) {
            for (OdinAttributeArgument attributeArgument : odinAttributesDefinition.getAttributeArgumentList()) {
                if (attributeArgument instanceof OdinAssignedAttribute assignedAttribute) {
                    String text = assignedAttribute.getAttributeIdentifier().getText();
                    if (text.equals("private")) {
                        String attributeValue = getStringLiteralValue(assignedAttribute.getAttributeNamedValue().getExpression());
                        if (Objects.equals(attributeValue, "file")) {
                            return OdinVisibility.FILE_PRIVATE;
                        }

                        if (Objects.equals(attributeValue, "package")) {
                            return OdinVisibility.PACKAGE_PRIVATE;
                        }
                    }
                }

                if (attributeArgument instanceof OdinUnassignedAttribute unnamedArgument) {
                    if (unnamedArgument.getAttributeIdentifier().getText().equals("private")) {
                        return OdinVisibility.PACKAGE_PRIVATE;
                    }
                }
            }
        }

        return OdinVisibility.NONE;
    }

    public static OdinVisibility computeVisibility(OdinAttributesOwner attributesOwner) {
        if (isLocal(attributesOwner)) {
            return OdinVisibility.NONE;
        }

        OdinVisibility visibility = computeVisibility(attributesOwner.getAttributesDefinitionList());
        if (visibility == OdinVisibility.NONE) {
            OdinFileScope fileScope = PsiTreeUtil.getParentOfType(attributesOwner, OdinFileScope.class);
            if (fileScope != null) {
                return getGlobalFileVisibility(fileScope);
            }
        }
        return OdinVisibility.NONE;
    }

    public static @Nullable OdinSymbol createSymbol(OdinDeclaration declaration, String name) {
        if (declaration != null) {
            List<OdinSymbol> symbols = OdinDeclarationSymbolResolver.getSymbols(declaration);
            return symbols.stream()
                    .filter(s -> s.getName().equals(name))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    public static @NotNull VirtualFile getContainingVirtualFile(@NotNull PsiElement psiElement) {
        VirtualFile virtualFile = psiElement.getContainingFile().getVirtualFile();
        if (virtualFile == null) {
            virtualFile = psiElement.getContainingFile().getOriginalFile().getVirtualFile();
        }

        if (virtualFile == null) {
            virtualFile = psiElement.getContainingFile().getViewProvider().getVirtualFile();
        }
        return virtualFile;
    }

    // Record to hold the result
    public record LineColumn(int line, int column) {
    }

    /**
     * Finds the line and column of a given offset in the input string.
     *
     * @param input  The input string.
     * @param offset The offset within the string.
     * @return A LineColumn record containing the line and column of the offset.
     * @throws IllegalArgumentException If the offset is invalid.
     */
    public static LineColumn offsetToLineColumn(String input, int offset) {
        if (offset < 0 || offset > input.length()) {
            throw new IllegalArgumentException("Offset is out of bounds.");
        }

        int line = 1; // Line numbers start from 1
        int column = 1; // Column numbers start from 1

        for (int i = 0; i < offset; i++) {
            char currentChar = input.charAt(i);
            if (currentChar == '\n') {
                line++;
                column = 1; // Reset column to 1 after a newline
            } else {
                column++;
            }
        }

        return new LineColumn(line, column);
    }

    public record OdinCallInfo(OdinPsiElement callingElement, TsOdinType callingType, List<OdinArgument> argumentList) {
    }
}
