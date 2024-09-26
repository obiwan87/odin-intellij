package com.lasagnerd.odin.codeInsight;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.LineColumn;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.codeInsight.imports.OdinImportUtils;
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

public class OdinInsightUtils {

    public static final List<Class<?>> OPERAND_BOUNDARY_CLASSES = List.of(
            OdinOperator.class,

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
            List<OdinSymbol> builtInSymbols = OdinBuiltinSymbolService.getInstance(project).getBuiltInSymbols();
            OdinSymbolTable odinBuiltinSymbolsTable = OdinSymbolTable.from(builtInSymbols);
            OdinSymbolTable symbolsOfImportedPackage = OdinImportUtils
                    .getSymbolsOfImportedPackage(packageType.getReferencingPackagePath(),
                            (OdinImportDeclarationStatement) packageType.getDeclaration());
            symbolsOfImportedPackage.setRoot(odinBuiltinSymbolsTable);
            return symbolsOfImportedPackage;
        }
        OdinSymbolTable typeSymbolTable = type.getSymbolTable();
        OdinSymbolTable symbolTable = new OdinSymbolTable();
        if (type instanceof TsOdinPointerType pointerType) {
            return getTypeElements(project, pointerType.getDereferencedType(), includeReferenceableSymbols);
        }

        if (type instanceof TsOdinConstrainedType constrainedType) {
            return getTypeElements(project, constrainedType.getSpecializedType(), includeReferenceableSymbols);
        }

        if (type instanceof TsOdinMetaType metaType && metaType.representedType() instanceof TsOdinPolymorphicType polymorphicType) {
            OdinType psiType = polymorphicType.getPsiType();
            if (psiType != null) {
                if (psiType.getParent() instanceof OdinConstrainedType constrainedType) {
                    OdinType specializedType = constrainedType.getTypeList().get(1);
                    TsOdinType tsOdinType = OdinTypeResolver.resolveType(typeSymbolTable, specializedType);
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

        if (type.getPsiType() instanceof OdinStructType structType) {
            var structFields = OdinInsightUtils.getStructFields(type.getSymbolTable(), structType);

            symbolTable.addAll(structFields);
            symbolTable.addTypes(typeSymbolTable);
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
            OdinSymbol implicitStructSymbol = OdinBuiltinSymbolService.createAllocatorSymbol(project);
            return OdinSymbolTable.from(Collections.singletonList(implicitStructSymbol));
        }

        if (includeReferenceableSymbols && TsOdinBuiltInTypes.getComplexTypes().contains(type)) {
            if (type == TsOdinBuiltInTypes.COMPLEX32) {
                List<OdinSymbol> odinSymbols = generateSwizzleFields(RGB, TsOdinBuiltInTypes.F16);
                odinSymbols.addAll(generateSwizzleFields(XYZ, TsOdinBuiltInTypes.F16));
                return OdinSymbolTable.from(odinSymbols);
            }

            if (type == TsOdinBuiltInTypes.COMPLEX64) {
                List<OdinSymbol> odinSymbols = generateSwizzleFields(RGB, TsOdinBuiltInTypes.F32);
                odinSymbols.addAll(generateSwizzleFields(XYZ, TsOdinBuiltInTypes.F32));
                return OdinSymbolTable.from(odinSymbols);
            }

            if (type == TsOdinBuiltInTypes.COMPLEX128) {
                List<OdinSymbol> odinSymbols = generateSwizzleFields(RGB, TsOdinBuiltInTypes.F64);
                odinSymbols.addAll(generateSwizzleFields(XYZ, TsOdinBuiltInTypes.F64));
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

    @Nullable
    public static OdinDeclaration getTypeDeclaration(OdinType type) {
        OdinDeclaration declaration = PsiTreeUtil.getParentOfType(type, OdinDeclaration.class);
        if (declaration != null) {
            if (!declaration.getDeclaredIdentifiers().isEmpty()) {
                return declaration;
            }
        }
        return null;
    }

    public static @NotNull List<OdinSymbol> getEnumFields(OdinEnumType enumType) {
        OdinEnumBody enumBody = enumType
                .getEnumBlock()
                .getEnumBody();

        if (enumBody == null)
            return Collections.emptyList();

        List<OdinSymbol> symbols = new ArrayList<>();
        for (OdinEnumValueDeclaration odinEnumValueDeclaration : enumBody
                .getEnumValueDeclarationList()) {
            OdinDeclaredIdentifier identifier = odinEnumValueDeclaration.getDeclaredIdentifier();
            OdinSymbol odinSymbol = new OdinSymbol(identifier);
            odinSymbol.setSymbolType(ENUM_FIELD);
            odinSymbol.setPsiType(enumType);
            symbols.add(odinSymbol);
        }
        return symbols;
    }

    public static List<OdinSymbol> getStructFields(OdinSymbolTable symbolTable, @NotNull OdinStructType structType) {
        List<OdinFieldDeclarationStatement> fieldDeclarationStatementList = getStructFieldsDeclarationStatements(structType);
        List<OdinSymbol> symbols = new ArrayList<>();
        for (OdinFieldDeclarationStatement field : fieldDeclarationStatementList) {
            for (OdinDeclaredIdentifier odinDeclaredIdentifier : field.getDeclaredIdentifiers()) {
                boolean hasUsing = field.getUsing() != null;
                OdinSymbol odinSymbol = new OdinSymbol(odinDeclaredIdentifier);
                odinSymbol.setSymbolType(FIELD);
                odinSymbol.setPsiType(field.getType());
                odinSymbol.setScope(OdinSymbol.OdinScope.TYPE);
                odinSymbol.setHasUsing(hasUsing);
                symbols.add(odinSymbol);
                if (field.getDeclaredIdentifiers().size() == 1 && hasUsing) {
                    getSymbolsOfFieldWithUsing(symbolTable, field, symbols);
                }
            }
        }
        return symbols;
    }

    public static void getSymbolsOfFieldWithUsing(OdinSymbolTable symbolTable,
                                                  OdinFieldDeclarationStatement field,
                                                  List<OdinSymbol> symbols) {
        TsOdinType tsOdinType = OdinTypeResolver.resolveType(symbolTable, field.getType());
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

        if (structType != null) {
            OdinType psiType = structType.getPsiType();
            if (psiType instanceof OdinStructType psiStructType) {// TODO This fails at models.odin in Engin3

                List<OdinSymbol> structFields = getStructFields(structType.getSymbolTable(), psiStructType);
                symbols.addAll(structFields);
            }
        }
    }

    public static @NotNull List<OdinFieldDeclarationStatement> getStructFieldsDeclarationStatements(OdinStructType structType) {
        OdinStructBody structBody = structType
                .getStructBlock()
                .getStructBody();

        List<OdinFieldDeclarationStatement> fieldDeclarationStatementList;
        if (structBody == null) {
            fieldDeclarationStatementList = Collections.emptyList();
        } else {
            fieldDeclarationStatementList = structBody.getFieldDeclarationStatementList();
        }
        return fieldDeclarationStatementList;
    }

    public static List<OdinSymbol> getTypeElements(OdinExpression expression, OdinSymbolTable symbolTable) {
        TsOdinType tsOdinType = OdinInferenceEngine.inferType(symbolTable, expression);
        if (tsOdinType instanceof TsOdinMetaType tsOdinMetaType) {
            return getTypeElements(OdinTypeResolver.resolveMetaType(symbolTable, tsOdinMetaType)
                    .baseType(true), symbolTable);
        }
        return getTypeElements(tsOdinType.baseType(true), symbolTable);
    }

    public static List<OdinSymbol> getTypeElements(OdinType type, OdinSymbolTable symbolTable) {
        TsOdinType tsOdinType = OdinTypeResolver.resolveType(symbolTable, type);
        return getTypeElements(tsOdinType, symbolTable);
    }

    public static @NotNull List<OdinSymbol> getTypeElements(TsOdinType tsOdinType, OdinSymbolTable symbolTable) {
        if (tsOdinType instanceof TsOdinStructType structType) {
            if (structType.getPsiType() instanceof OdinStructType psiStructType) {
                return getStructFields(symbolTable, psiStructType);
            }
        }

        if (tsOdinType instanceof TsOdinPointerType pointerType) {
            return getTypeElements(pointerType.getDereferencedType(), symbolTable);
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
            OdinSymbolTable symbolsOfImportedPackage = OdinImportUtils
                    .getSymbolsOfImportedPackage(packageReferenceType.getReferencingPackagePath(),
                            (OdinImportDeclarationStatement) packageReferenceType.getDeclaration());
            return new ArrayList<>(symbolsOfImportedPackage.getSymbols());
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
        symbol.setSymbolType(FIELD);
        symbol.setName(declaredIdentifier.getName());
        symbol.setDeclaredIdentifier(declaredIdentifier);
        symbol.setImplicitlyDeclared(false);
        symbol.setScope(OdinSymbol.OdinScope.TYPE);
        symbol.setVisibility(OdinSymbol.OdinVisibility.NONE);
        symbol.setHasUsing(false);
        return symbol;
    }

    // Swizzle field generator

    public static @NotNull List<OdinSymbol> getSwizzleFields(TsOdinArrayType tsOdinArrayType) {
        List<OdinSymbol> symbols = new ArrayList<>();
        symbols.addAll(generateSwizzleFields(RGBA, tsOdinArrayType.getElementType()));
        symbols.addAll(generateSwizzleFields(XYZW, tsOdinArrayType.getElementType()));
        return symbols;
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
            odinSymbol.setScope(OdinSymbol.OdinScope.TYPE);
            odinSymbol.setVisibility(OdinSymbol.OdinVisibility.NONE);
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

    public static boolean isProcedureOverloadDeclaration(PsiElement element) {
        return isTypeDeclaration(element, OdinProcedureOverloadType.class);
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

    public static @Nullable OdinType getDeclaredType(PsiElement declaredIdentifier) {
        OdinDeclaration declaration
                = PsiTreeUtil.getParentOfType(declaredIdentifier,
                OdinDeclaration.class,
                false);
        if(declaration instanceof OdinConstantInitializationStatement constantInitializationStatement) {
            return getDeclaredType(constantInitializationStatement);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T extends OdinType> T getDeclaredType(PsiElement element, @NotNull Class<T> typeClass) {
        if(element == null)
            return null;
        OdinType declaredType = getDeclaredType(element);
        if(typeClass.isInstance(declaredType))
            return (T) declaredType;
        return null;
    }

    public static OdinProcedureType getProcedureType(PsiElement element) {
        OdinProcedureType procedureType = getDeclaredType(element, OdinProcedureType.class);
        if(procedureType != null)
            return procedureType;

        OdinProcedureLiteralType procedureLiteralType = getDeclaredType(element, OdinProcedureLiteralType.class);
        if(procedureLiteralType != null) {
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
            OdinType type = typeDefinitionExpression.getType();
            return type;
        }

        if (expressionList instanceof OdinProcedureExpression procedureExpression) {
            return procedureExpression.getProcedureDefinition().getProcedureSignature().getProcedureType();
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
        } else if (isProcedureOverloadDeclaration(element)) {
            return PROCEDURE_OVERLOAD;
        } else if (isPackageDeclaration(element)) {
            return PACKAGE_REFERENCE;
        } else if (isFieldDeclaration(element)) {
            return FIELD;
        } else if (isParameterDeclaration(element)) {
            return PARAMETER;
        } else {
            return UNKNOWN;
        }
    }

    public static boolean isParameterDeclaration(PsiElement element) {
        return PsiTreeUtil.getParentOfType(element, true, OdinDeclaration.class) instanceof OdinParameterDeclaration;
    }

    public static String getLineColumn(@NotNull PsiElement element) {
        PsiFile containingFile = element.getContainingFile();
        if (containingFile != null) {
            LineColumn lineColumn = StringUtil.offsetToLineColumn(containingFile.getText(), element.getTextOffset());
            return (lineColumn.line + 1) + ":" + (lineColumn.column + 1);
        }
        return "<unknown>:<unknown>";
    }

    public static boolean getEnumeratedArraySymbols(OdinSymbolTable symbolTable, TsOdinArrayType tsOdinArrayType) {
        OdinEnumType psiType = getEnumTypeOfArray(symbolTable, tsOdinArrayType);
        if (psiType != null) {
            List<OdinSymbol> enumFields = getEnumFields(psiType);
            symbolTable.addAll(enumFields);
            return true;
        }
        return false;
    }

    public static @Nullable OdinEnumType getEnumTypeOfArray(OdinSymbolTable symbolTable, TsOdinArrayType tsOdinArrayType) {
        var psiSizeElement = tsOdinArrayType.getPsiSizeElement();
        OdinExpression expression = psiSizeElement.getExpression();

        OdinEnumType psiType;
        if (expression != null) {
            TsOdinType sizeType = OdinInferenceEngine.inferType(symbolTable, expression);
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
            odinSymbol.setVisibility(OdinSymbol.OdinVisibility.NONE);
            odinSymbol.setImplicitlyDeclared(true);
            odinSymbol.setScope(OdinSymbol.OdinScope.TYPE);
            odinSymbol.setSymbolType(SWIZZLE_FIELD);
            swizzleSymbols.add(odinSymbol);
        }
        return swizzleSymbols;
    }

    public static List<OdinSymbol> getElementSymbols(TsOdinType tsOdinType, OdinSymbolTable symbolTable) {
        List<OdinSymbol> elementSymbols = new ArrayList<>();
        tsOdinType = tsOdinType.baseType(true);
        if (tsOdinType instanceof TsOdinStructType tsOdinStructType) {
            List<OdinSymbol> typeSymbols = getTypeElements(tsOdinStructType, symbolTable);
            elementSymbols.addAll(typeSymbols);
        }

        if (tsOdinType instanceof TsOdinBitFieldType tsOdinBitFieldType) {
            List<OdinSymbol> typeSymbols = getTypeElements(tsOdinBitFieldType, symbolTable);
            elementSymbols.addAll(typeSymbols);
        }

        if (tsOdinType instanceof TsOdinArrayType tsOdinArrayType) {
            OdinEnumType enumTypeOfArray = getEnumTypeOfArray(symbolTable, tsOdinArrayType);
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

        PsiElement firstParent = PsiTreeUtil.findFirstParent(psiElement, strict, p -> {
            for (Class<?> clazz : stopAt) {
                if (clazz.isInstance(p)) {
                    invalid[0] = true;
                    return true;
                }
            }

            for (Class<?> clazz : parentTypes) {
                if (clazz.isInstance(p)) {
                    invalid[0] = false;
                    return true;
                }
            }
            return false;
        });

        if (!invalid[0]) {
            return firstParent;
        }

        return null;
    }

    public static @Nullable OdinSymbol findBuiltinSymbolOfCallExpression(OdinSymbolTable symbolTable, OdinCallExpression callExpression, Predicate<String> identifierPredicate) {
        OdinSymbol symbol = null;
        if (callExpression.getExpression() instanceof OdinRefExpression callRefExpression
                && callRefExpression.getExpression() == null
                && callRefExpression.getIdentifier() != null) {

            String text = callRefExpression.getIdentifier().getText();
            if (identifierPredicate.test(text)) {
                symbol = symbolTable.getSymbol(text);
            }
        }
        return symbol;
    }

    public static @Nullable Map<OdinExpression, TsOdinParameter> getArgumentToParameterMap(List<TsOdinParameter> parameters,
                                                                                           @NotNull List<OdinArgument> argumentList) {
        return getArgumentToParameterMap(parameters, argumentList, false);
    }

    public static @Nullable Map<OdinExpression, TsOdinParameter> getArgumentToParameterMap(List<TsOdinParameter> parameters,
                                                                                           @NotNull List<OdinArgument> argumentList,
                                                                                           boolean includeDefaultParameters) {
        Map<String, TsOdinParameter> parametersByName = parameters.stream().collect(Collectors.toMap(
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
            }

            if (odinArgument instanceof OdinNamedArgument namedArgument) {
                TsOdinParameter tsOdinParameter = parametersByName.get(namedArgument.getIdentifier().getText());
                if (tsOdinParameter == null || !usedParameters.contains(tsOdinParameter)) {
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
            } else {
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
}
