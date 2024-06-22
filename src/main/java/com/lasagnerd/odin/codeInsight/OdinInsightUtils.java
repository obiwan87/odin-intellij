package com.lasagnerd.odin.codeInsight;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.codeInsight.imports.OdinImportUtils;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbol;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolTable;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolType;
import com.lasagnerd.odin.codeInsight.typeInference.OdinInferenceEngine;
import com.lasagnerd.odin.codeInsight.typeInference.OdinTypeResolver;
import com.lasagnerd.odin.codeInsight.typeInference.OdinTypeSpecializer;
import com.lasagnerd.odin.codeInsight.typeSystem.*;
import com.lasagnerd.odin.lang.psi.*;
import org.apache.commons.text.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.lasagnerd.odin.codeInsight.symbols.OdinSymbolType.*;

public class OdinInsightUtils {

    public static final List<Class<?>> OPERAND_BOUNDARY_CLASSES = List.of(
            OdinOperator.class,

            OdinArgument.class,
            OdinDeclaration.class,
            OdinStatement.class,

            OdinExpressionsList.class
    );
    public static final char[] RGBA = {'r', 'g', 'b', 'a'};
    public static final char[] XYZW = {'x', 'y', 'z', 'w'};

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

    public static OdinSymbolTable getTypeElements(TsOdinType type) {
        return getTypeElements(type, false);
    }

    public static OdinSymbolTable getTypeElements(TsOdinType type, boolean includeReferenceableSymbols) {
        if (type instanceof TsOdinPackageReferenceType packageType) {
            return OdinImportUtils
                    .getSymbolsOfImportedPackage(packageType.getReferencingPackagePath(),
                            (OdinImportDeclarationStatement) packageType.getDeclaration());
        }
        OdinSymbolTable typeScope = type.getSymbolTable();
        OdinSymbolTable symbolTable = new OdinSymbolTable();
        if (type instanceof TsOdinPointerType pointerType) {
            return getTypeElements(pointerType.getDereferencedType(), includeReferenceableSymbols);
        }
        // TODO Check if this is correct!
        if(type instanceof TsOdinConstrainedType constrainedType) {
            return getTypeElements(constrainedType.getSpecializedType(), includeReferenceableSymbols);
        }
        OdinType odinDeclaration = type.getType();
        OdinDeclaration declaration = type.getDeclaration();
        if (declaration instanceof OdinStructDeclarationStatement structDeclarationStatement) {
            var structFields = OdinInsightUtils.getStructFields(type.getSymbolTable(), structDeclarationStatement);

            symbolTable.addAll(structFields);
            symbolTable.addTypes(typeScope);
            return symbolTable;
        }

        if (odinDeclaration instanceof OdinEnumType enumType) {
            return symbolTable.with(getEnumFields(enumType));
        }

        if(includeReferenceableSymbols && type instanceof TsOdinArrayType arrayType) {
            return OdinSymbolTable.from(getSwizzleFields(arrayType));
        }

        return symbolTable;
    }

    private static @NotNull List<OdinSymbol> getEnumFields(OdinEnumType enumType) {
        OdinEnumBody enumBody = enumType
                .getEnumBlock()
                .getEnumBody();

        if (enumBody == null)
            return Collections.emptyList();

        List<OdinSymbol> symbols = new ArrayList<>();
        for (OdinEnumValueDeclaration odinEnumValueDeclaration : enumBody
                .getEnumValueDeclarationList()) {
            // TODO move to SymbolResolver
            OdinDeclaredIdentifier identifier = odinEnumValueDeclaration.getDeclaredIdentifier();
            OdinSymbol odinSymbol = new OdinSymbol(identifier);
            odinSymbol.setSymbolType(ENUM_FIELD);
            odinSymbol.setPsiType(enumType);
            odinSymbol.setValueExpression(odinEnumValueDeclaration.getExpression());
            symbols.add(odinSymbol);
        }
        return symbols;
    }

    // TODO This is kind of a duplicate of what OdinSymbolResolver does
    public static List<OdinSymbol> getStructFields(OdinSymbolTable symbolTable, OdinStructDeclarationStatement structDeclarationStatement) {
        OdinStructType structType = structDeclarationStatement
                .getStructType();
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

    public static void getSymbolsOfFieldWithUsing(OdinSymbolTable symbolTable, OdinFieldDeclarationStatement x, List<OdinSymbol> symbols) {
        TsOdinType tsOdinType = OdinTypeResolver.resolveType(symbolTable, x.getType());
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
            List<OdinSymbol> structFields = getStructFields(structType.getSymbolTable(), (OdinStructDeclarationStatement) structType.getDeclaration());
            symbols.addAll(structFields);
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
            return getTypeElements(OdinTypeResolver.resolveMetaType(symbolTable, tsOdinMetaType), symbolTable);
        }
        return getTypeElements(tsOdinType, symbolTable);
    }

    public static List<OdinSymbol> getTypeElements(OdinType type, OdinSymbolTable symbolTable) {
        TsOdinType tsOdinType = OdinTypeResolver.resolveType(symbolTable, type);
        return getTypeElements(tsOdinType, symbolTable);
    }

    public static @NotNull List<OdinSymbol> getTypeElements(TsOdinType tsOdinType, OdinSymbolTable symbolTable) {
        if (tsOdinType instanceof TsOdinStructType structType) {
            return getStructFields(symbolTable, (OdinStructDeclarationStatement) structType.getDeclaration());
        }

        if (tsOdinType instanceof TsOdinPointerType pointerType) {
            return getTypeElements(pointerType.getDereferencedType(), symbolTable);
        }

        if (tsOdinType instanceof TsOdinEnumType enumType) {
            return getEnumFields(((OdinEnumDeclarationStatement) enumType.getDeclaration()).getEnumType());
        }

        if (tsOdinType instanceof TsOdinPackageReferenceType packageReferenceType) {
            OdinSymbolTable symbolsOfImportedPackage = OdinImportUtils
                    .getSymbolsOfImportedPackage(packageReferenceType.getReferencingPackagePath(),
                            (OdinImportDeclarationStatement) packageReferenceType.getDeclaration());
            return new ArrayList<>(symbolsOfImportedPackage.getSymbols());
        }

        return Collections.emptyList();
    }

    // Swizzle field generator

    public static @NotNull List<OdinSymbol> getSwizzleFields(TsOdinArrayType tsOdinArrayType) {
        List<OdinSymbol> symbols = new ArrayList<>();
        symbols.addAll(generateSwizzleFields(tsOdinArrayType, RGBA));
        symbols.addAll(generateSwizzleFields(tsOdinArrayType, XYZW));
        return symbols;
    }

    private static List<OdinSymbol> generateSwizzleFields(TsOdinArrayType arrayType, char[] input) {

        List<String> result = new ArrayList<>();
        // Generate combinations for lengths from 1 to maxLen
        for (int len = 1; len <= 4; len++) {
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
            TsOdinType elementType = arrayType.getElementType();
            if (elementType != null) {
                odinSymbol.setPsiType(elementType.getType());
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
        return odinRefExpressions.get(odinRefExpressions.size() - 1);
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
        return element.getParent() instanceof OdinProcedureDeclarationStatement;
    }

    public static boolean isProcedureOverloadDeclaration(PsiElement element) {
        return element.getParent() instanceof OdinProcedureOverloadDeclarationStatement;
    }

    public static boolean isConstantDeclaration(PsiElement element) {
        return PsiTreeUtil.getParentOfType(element, true, OdinConstantInitializationStatement.class) != null;
    }

    public static boolean isStructDeclaration(PsiElement element) {
        return element.getParent() instanceof OdinStructDeclarationStatement;
    }

    public static boolean isEnumDeclaration(PsiElement element) {
        return element.getParent() instanceof OdinEnumDeclarationStatement;
    }

    public static boolean isUnionDeclaration(PsiElement element) {
        return element.getParent() instanceof OdinUnionDeclarationStatement;
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
}
