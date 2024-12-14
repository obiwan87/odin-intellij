package com.lasagnerd.odin.codeInsight;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.codeInsight.imports.OdinImportService;
import com.lasagnerd.odin.codeInsight.imports.OdinImportUtils;
import com.lasagnerd.odin.codeInsight.sdk.OdinSdkService;
import com.lasagnerd.odin.codeInsight.symbols.OdinScope;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbol;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolType;
import com.lasagnerd.odin.codeInsight.symbols.OdinVisibility;
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

    public static OdinSymbolTable getTypeElements(Project project, TsOdinType type) {
        return getTypeElements(project, type, false);
    }

    public static OdinSymbolTable getTypeElements(Project project, TsOdinType type, boolean includeReferenceableSymbols) {

        type = type.baseType(true);

        if (type instanceof TsOdinPackageReferenceType packageType) {
            return getPackageReferenceSymbols(project, packageType, true);
        }
        OdinContext typeContext = type.getContext();
        OdinSymbolTable symbolTable = new OdinSymbolTable();
        switch (type) {
            case TsOdinPointerType pointerType -> {
                return getTypeElements(project, pointerType.getDereferencedType(), includeReferenceableSymbols);
            }
            case TsOdinConstrainedType constrainedType -> {
                return getTypeElements(project, constrainedType.getSpecializedType(), includeReferenceableSymbols);
            }
            case TsOdinAnyType anyType -> {
                return getTypeElements(project, anyType.getBackingType(), includeReferenceableSymbols);
            }
            case TsOdinMetaType metaType when metaType.representedType() instanceof TsOdinPolymorphicType polymorphicType -> {
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

        if (type.getPsiType() instanceof OdinStructType structType) {
            var structFields = OdinInsightUtils.getStructFields(type.getContext(), structType);

            symbolTable.addAll(structFields);
//            context.addTypes(typeContext);
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

        if (type instanceof TsOdinMetaType metaType && metaType.representedType().baseType(true) instanceof TsOdinBitSetType tsOdinBitSetType) {
            if (tsOdinBitSetType.getElementType() instanceof TsOdinEnumType tsOdinEnumType) {
                return symbolTable.with(getEnumFields((OdinEnumType) tsOdinEnumType.getPsiType()));
            }
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
        return symbolTable;
    }

    public static @NotNull OdinSymbolTable getPackageReferenceSymbols(Project project,
                                                                      TsOdinPackageReferenceType packageType,
                                                                      boolean includeBuiltin) {
        OdinSymbolTable symbolTable = OdinImportUtils
                .getSymbolsOfImportedPackage(packageType.getReferencingPackagePath(),
                        (OdinImportDeclarationStatement) packageType.getDeclaration());
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
            if (sizeType instanceof TsOdinMetaType sizeMetaType && sizeMetaType.getRepresentedMetaType() == TsOdinMetaType.MetaType.ENUM) {
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
        List<OdinFieldDeclarationStatement> fieldDeclarationStatementList = getStructFieldsDeclarationStatements(structType);
        List<OdinSymbol> symbols = new ArrayList<>();
        for (OdinFieldDeclarationStatement field : fieldDeclarationStatementList) {
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
                                                  OdinFieldDeclarationStatement field,
                                                  List<OdinSymbol> symbols) {
        if (field.getType() == null) {
            return;
        }
        TsOdinType tsOdinType = OdinTypeResolver.resolveType(context, field.getType());
        TsOdinStructType structType = unwrapFromPointerType(tsOdinType);

        if (structType != null) {
            OdinType psiType = structType.getPsiType();
            if (psiType instanceof OdinStructType psiStructType) {// TODO This fails at models.odin in Engin3

                List<OdinSymbol> structFields = getStructFields(structType.getContext(), psiStructType);
                symbols.addAll(structFields);
            }
        }
    }

    private static @Nullable TsOdinStructType unwrapFromPointerType(TsOdinType tsOdinType) {
        TsOdinStructType structType;
        if (tsOdinType instanceof TsOdinPointerType tsOdinPointerType) {
            if (tsOdinPointerType.getDereferencedType() instanceof TsOdinStructType) {
                structType = (TsOdinStructType) tsOdinPointerType.getDereferencedType();
            } else {
                structType = null;
            }
        } else if (tsOdinType instanceof TsOdinStructType tsOdinStructType) {
            structType = tsOdinStructType;
        } else {
            structType = null;
        }
        return structType;
    }

    public static @NotNull List<OdinFieldDeclarationStatement> getStructFieldsDeclarationStatements(OdinStructType structType) {
        OdinStructBlock structBlock = structType
                .getStructBlock();

        if (structBlock == null)
            return Collections.emptyList();
        OdinStructBody structBody = structBlock
                .getStructBody();

        List<OdinFieldDeclarationStatement> fieldDeclarationStatementList;
        if (structBody == null) {
            fieldDeclarationStatementList = Collections.emptyList();
        } else {
            fieldDeclarationStatementList = structBody.getFieldDeclarationStatementList();
        }
        return fieldDeclarationStatementList;
    }

    public static List<OdinSymbol> getTypeElements(OdinContext context, OdinExpression expression) {
        TsOdinType tsOdinType = expression.getInferredType(context);
        if (tsOdinType instanceof TsOdinMetaType tsOdinMetaType) {
            return getTypeElements(OdinTypeResolver.resolveMetaType(context, tsOdinMetaType)
                    .baseType(true), context);
        }
        return getTypeElements(tsOdinType.baseType(true), context);
    }

    public static List<OdinSymbol> getTypeElements(OdinType type, OdinContext context) {
        TsOdinType tsOdinType = OdinTypeResolver.resolveType(context, type);
        return getTypeElements(tsOdinType, context);
    }

    public static @NotNull List<OdinSymbol> getTypeElements(TsOdinType tsOdinType, OdinContext context) {
        if (tsOdinType instanceof TsOdinStructType structType) {
            if (structType.getPsiType() instanceof OdinStructType psiStructType) {
                return getStructFields(context, psiStructType);
            }
        }

        if (tsOdinType instanceof TsOdinPointerType pointerType) {
            return getTypeElements(pointerType.getDereferencedType(), context);
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
                    .getSymbolsOfImportedPackage(packageReferenceType.getReferencingPackagePath(),
                            (OdinImportDeclarationStatement) packageReferenceType.getDeclaration());
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
        return PsiTreeUtil.getParentOfType(element, true, OdinVariableDeclarationStatement.class) != null
                || PsiTreeUtil.getParentOfType(element, true, OdinVariableInitializationStatement.class) != null;
    }

    public static boolean isProcedureDeclaration(PsiElement element) {
        return isTypeDeclaration(element, OdinProcedureType.class) || isTypeDeclaration(element, OdinProcedureLiteralType.class);
    }

    public static boolean isProcedureGroupDeclaration(PsiElement element) {
        return isTypeDeclaration(element, OdinProcedureGroupType.class);
    }

    public static boolean isConstantDeclaration(PsiElement element) {
        if (element.getParent() instanceof OdinConstantInitializationStatement constantInitializationStatement) {
            return constantInitializationStatement.getExpressionList().size() > 1
                    || (
                    constantInitializationStatement.getExpressionList().size() == 1 &&
                            !(
                                    constantInitializationStatement.getExpressionList().getFirst() instanceof OdinTypeDefinitionExpression
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
        if (declaration instanceof OdinConstantInitializationStatement constantInitializationStatement) {
            return getDeclaredType(constantInitializationStatement);
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

    public static @Nullable OdinType getDeclaredType(OdinConstantInitializationStatement constantInitializationStatement) {
        List<OdinExpression> expressionList = constantInitializationStatement.getExpressionList();
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
        return element.getParent() instanceof OdinFieldDeclarationStatement;
    }

    private static boolean isPackageDeclaration(PsiNamedElement element) {
        return element instanceof OdinImportDeclarationStatement
                || element.getParent() instanceof OdinImportDeclarationStatement;
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
            if (sizeType instanceof TsOdinMetaType sizeMetaType && sizeMetaType.getRepresentedMetaType() == TsOdinMetaType.MetaType.ENUM) {
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
            List<OdinSymbol> typeSymbols = getTypeElements(tsOdinStructType, context);
            elementSymbols.addAll(typeSymbols);
        }

        if (tsOdinType instanceof TsOdinBitFieldType tsOdinBitFieldType) {
            List<OdinSymbol> typeSymbols = getTypeElements(tsOdinBitFieldType, context);
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
                        v -> v
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
        return (parent instanceof OdinVariableInitializationStatement || parent instanceof OdinVariableDeclarationStatement)
                && !isLocalVariable(parent);
    }

    public static boolean isLocalVariable(@NotNull PsiElement o) {
        if (isVariable(o)) {
            return isLocal(o);
        }
        return false;
    }

    public static boolean isLocal(@NotNull PsiElement o) {
        OdinFileScopeStatementList fileScope = PsiTreeUtil.getParentOfType(o, OdinFileScopeStatementList.class,
                true,
                OdinProcedureBody.class);


        return fileScope == null;
    }

    public static boolean isForeign(PsiElement o) {
        return PsiTreeUtil.getParentOfType(o, OdinForeignStatementList.class, true) != null;
    }

    private static boolean isVariable(@NotNull PsiElement o) {
        return o instanceof OdinVariableInitializationStatement || o instanceof OdinVariableDeclarationStatement;
    }

    public static boolean isStaticVariable(OdinDeclaredIdentifier declaredIdentifier) {
        if (isVariable(declaredIdentifier.getParent())) {
            OdinVariableDeclaration declaration = (OdinVariableDeclaration) declaredIdentifier.getParent();
            return OdinAttributeUtils.containsAttribute(declaration.getAttributesDefinitionList(), "static");
        }
        return false;
    }

    public static boolean isStaticProcedure(OdinDeclaredIdentifier declaredIdentifier) {
        PsiElement parent = declaredIdentifier.getParent();
        if (isProcedureDeclaration(parent)) {
            OdinConstantInitializationStatement constant = (OdinConstantInitializationStatement) parent;
            return OdinAttributeUtils.containsAttribute(constant.getAttributesDefinitionList(), "static");
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

    public static @NotNull OdinCallInfo getCallInfo(OdinContext context, OdinArgument argument) {
        @Nullable OdinPsiElement callingElement = PsiTreeUtil.getParentOfType(argument, OdinCallExpression.class, OdinCallType.class);
        TsOdinType tsOdinType = TsOdinBuiltInTypes.UNKNOWN;
        List<OdinArgument> argumentList = Collections.emptyList();
        if (callingElement != null) {
            if (callingElement instanceof OdinCallExpression odinCallExpression) {
                tsOdinType = odinCallExpression.getExpression().getInferredType(context);
                // Here we have to get a meta type, otherwise the call expression does not make sense
                if (tsOdinType instanceof TsOdinMetaType tsOdinMetaType) {
                    tsOdinType = tsOdinMetaType.representedType();
                } else if (!(tsOdinType.baseType(true) instanceof TsOdinProcedureType)) {
                    tsOdinType = TsOdinBuiltInTypes.UNKNOWN;
                }
                argumentList = odinCallExpression.getArgumentList();
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

        VirtualFile referenceFile = OdinImportUtils.getContainingVirtualFile(reference);
        VirtualFile declarationFile = OdinImportUtils.getContainingVirtualFile(symbol.getDeclaration());

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
        if (type instanceof TsOdinMetaType metaType) {
            TsOdinType tsOdinType = metaType.representedType().baseType(true);
            if (tsOdinType instanceof TsOdinEnumType) {
                return getTypeElements(valueExpression.getProject(), tsOdinType);
            }
        }
        if (type instanceof TsOdinPackageReferenceType packageReferenceType) {
            return getPackageReferenceSymbols(valueExpression.getProject(),
                    packageReferenceType,
                    false);
        }
        return getTypeElements(valueExpression.getProject(), type, true);
    }

    // This is a variant  of getTypeElements()
    public static OdinSymbolTable getReferenceableSymbols(OdinContext context, OdinQualifiedType qualifiedType) {
        OdinIdentifier identifier = qualifiedType.getIdentifier();
        OdinSymbol odinSymbol = identifier.getReferencedSymbol(context);
        if (odinSymbol != null) {
            OdinDeclaration odinDeclaration = PsiTreeUtil.getParentOfType(odinSymbol.getDeclaredIdentifier(), false, OdinDeclaration.class);
            if (odinDeclaration instanceof OdinImportDeclarationStatement importDeclarationStatement) {
                return OdinImportUtils.getSymbolsOfImportedPackage(OdinImportService.getInstance(qualifiedType.getProject()).getPackagePath(qualifiedType), importDeclarationStatement);
            }
        }
        return OdinSymbolTable.EMPTY;
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
