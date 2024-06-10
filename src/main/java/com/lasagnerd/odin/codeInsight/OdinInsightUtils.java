package com.lasagnerd.odin.codeInsight;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.codeInsight.typeInference.OdinInferenceEngine;
import com.lasagnerd.odin.codeInsight.typeInference.OdinTypeResolver;
import com.lasagnerd.odin.codeInsight.typeSystem.*;
import com.lasagnerd.odin.lang.psi.*;
import org.apache.commons.text.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OdinInsightUtils {

    public static final List<Class<?>> OPERAND_BOUNDARY_CLASSES = List.of(
            OdinOperator.class,

            OdinArgument.class,
            OdinDeclaration.class,
            OdinStatement.class,

            OdinExpressionsList.class
    );

    public static String getStringLiteralValue(OdinExpression odinExpression) {
        if (odinExpression instanceof OdinLiteralExpression literalExpression) {
            if (literalExpression.getBasicLiteral() instanceof OdinStringLiteral stringLiteral) {
                if (stringLiteral.getDqStringLiteral() != null || stringLiteral.getSqStringLiteral() != null) {
                    String text = literalExpression.getText();

                    if (text.length() >= 2) {
                        text = text.substring(1, text.length() - 1);
                        return StringEscapeUtils.unescapeJava(text);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns the symbols provided by an expression of type `type` when it is referenced with "." or "->".
     *
     * @param type The type of the expression
     * @return The scope
     */
    public static OdinSymbolTable getScopeProvidedByType(TsOdinType type) {
        if (type instanceof TsOdinPackageReferenceType packageType) {
            return OdinImportUtils
                    .getSymbolsOfImportedPackage(packageType.getReferencingPackagePath(),
                            (OdinImportDeclarationStatement) packageType.getDeclaration());
        }
        OdinSymbolTable typeScope = type.getSymbolTable();
        OdinSymbolTable symbolTable = new OdinSymbolTable();
        if (type instanceof TsOdinPointerType pointerType) {
            type = pointerType.getDereferencedType();
        }
        OdinDeclaration odinDeclaration = type.getDeclaration();

        if (odinDeclaration instanceof OdinStructDeclarationStatement structDeclarationStatement) {
            List<OdinSymbol> structFields = getStructFields(structDeclarationStatement);
            for (OdinFieldDeclarationStatement odinFieldDeclarationStatement : getStructFieldsDeclarationStatements(structDeclarationStatement).stream()
                    .filter(f -> f.getUsing() != null)
                    .toList()) {
                if (odinFieldDeclarationStatement.getDeclaredIdentifiers().isEmpty())
                    continue;

                TsOdinType usedType = OdinTypeResolver.resolveType(typeScope, odinFieldDeclarationStatement.getType());
                OdinSymbolTable subScope = getScopeProvidedByType(usedType);
                symbolTable.putAll(subScope);
            }

            symbolTable.addAll(structFields);
            symbolTable.addTypes(typeScope);
            return symbolTable;
        }

        if (odinDeclaration instanceof OdinEnumDeclarationStatement enumDeclarationStatement) {
            return symbolTable.with(getEnumFields(enumDeclarationStatement));
        }

        return OdinSymbolTable.EMPTY;
    }

    @NotNull
    public static List<OdinSymbol> getEnumFields(OdinEnumDeclarationStatement enumDeclarationStatement) {
        return getEnumFields(enumDeclarationStatement.getEnumType());
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
            odinSymbol.setSymbolType(OdinSymbol.OdinSymbolType.ENUM_FIELD);
            odinSymbol.setPsiType(enumType);
            odinSymbol.setValueExpression(odinEnumValueDeclaration.getExpression());
            symbols.add(odinSymbol);
        }
        return symbols;
    }

    public static List<OdinSymbol> getStructFields(OdinStructDeclarationStatement structDeclarationStatement) {
        return getStructFields(OdinSymbolTable.EMPTY, structDeclarationStatement);
    }

    public static List<OdinSymbol> getStructFields(OdinSymbolTable symbolTable, OdinStructDeclarationStatement structDeclarationStatement) {
        List<OdinFieldDeclarationStatement> fieldDeclarationStatementList = getStructFieldsDeclarationStatements(structDeclarationStatement);

        List<OdinSymbol> symbols = new ArrayList<>();
        for (OdinFieldDeclarationStatement x : fieldDeclarationStatementList) {
            for (OdinDeclaredIdentifier odinDeclaredIdentifier : x.getDeclaredIdentifiers()) {
                OdinSymbol odinSymbol = new OdinSymbol(odinDeclaredIdentifier);
                odinSymbol.setSymbolType(OdinSymbol.OdinSymbolType.FIELD);
                symbols.add(odinSymbol);
                if (x.getUsing() != null) {
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

            }
        }
        return symbols;
    }

    public static @NotNull List<OdinFieldDeclarationStatement> getStructFieldsDeclarationStatements(OdinStructDeclarationStatement structDeclarationStatement) {
        OdinStructType structType = structDeclarationStatement
                .getStructType();
        return getStructFieldsDeclarationStatements(structType);
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

    public static OdinTypeType classify(PsiNamedElement element) {
        if (isStructDeclaration(element)) {
            return OdinTypeType.STRUCT;
        } else if (isEnumDeclaration(element)) {
            return OdinTypeType.ENUM;
        } else if (isUnionDeclaration(element)) {
            return OdinTypeType.UNION;
        } else if (isProcedureDeclaration(element)) {
            return OdinTypeType.PROCEDURE;
        } else if (isVariableDeclaration(element)) {
            return OdinTypeType.VARIABLE;
        } else if (isConstantDeclaration(element)) {
            return OdinTypeType.CONSTANT;
        } else if (isProcedureOverloadDeclaration(element)) {
            return OdinTypeType.PROCEDURE_OVERLOAD;
        } else if (isPackageDeclaration(element)) {
            return OdinTypeType.PACKAGE;
        } else if (isFieldDeclaration(element)) {
            return OdinTypeType.FIELD;
        } else if (isParameterDeclaration(element)) {
            return OdinTypeType.PARAMETER;
        } else {
            return OdinTypeType.UNKNOWN;
        }
    }

    public static boolean isParameterDeclaration(PsiElement element) {
        return PsiTreeUtil.getParentOfType(element, true, OdinDeclaration.class) instanceof OdinParameterDeclaration;
    }

    public static OdinProcedureDeclarationStatement getDeclaringProcedure(OdinDeclaredIdentifier element) {
        return element.getParent() instanceof OdinProcedureDeclarationStatement ? (OdinProcedureDeclarationStatement) element.getParent() : null;
    }

    public static List<OdinSymbol> getTypeSymbols(OdinExpression expression, OdinSymbolTable symbolTable) {
        TsOdinType tsOdinType = OdinInferenceEngine.inferType(symbolTable, expression);
        if (tsOdinType instanceof TsOdinMetaType tsOdinMetaType) {
            return getTypeSymbols(OdinTypeResolver.resolveMetaType(symbolTable, tsOdinMetaType), symbolTable);
        }
        return getTypeSymbols(tsOdinType, symbolTable);
    }

    public static List<OdinSymbol> getTypeSymbols(OdinType type, OdinSymbolTable symbolTable) {
        TsOdinType tsOdinType = OdinTypeResolver.resolveType(symbolTable, type);
        return getTypeSymbols(tsOdinType, symbolTable);
    }

    public static @NotNull List<OdinSymbol> getTypeSymbols(TsOdinType tsOdinType, OdinSymbolTable symbolTable) {
        if (tsOdinType instanceof TsOdinStructType structType) {
            return getStructFields(symbolTable, (OdinStructDeclarationStatement) structType.getDeclaration());
        }

        if (tsOdinType instanceof TsOdinEnumType enumType) {
            return getEnumFields((OdinEnumDeclarationStatement) enumType.getDeclaration());
        }

        if (tsOdinType instanceof TsOdinPackageReferenceType packageReferenceType) {
            OdinSymbolTable symbolsOfImportedPackage = OdinImportUtils
                    .getSymbolsOfImportedPackage(packageReferenceType.getReferencingPackagePath(),
                            (OdinImportDeclarationStatement) packageReferenceType.getDeclaration());
            return new ArrayList<>(symbolsOfImportedPackage.getSymbols());
        }

        return Collections.emptyList();
    }


    public static OdinExpression findOperand(PsiElement element) {
        PsiElement operand = PsiTreeUtil.findFirstParent(element, psiElement -> OPERAND_BOUNDARY_CLASSES.stream()
                .anyMatch(s -> s.isInstance(psiElement.getParent()) && psiElement instanceof OdinExpression)
        );

        if (operand != null)
            return (OdinExpression) operand;
        return null;
    }

    public static boolean isInstanceOfAny(Object obj, List<Class<?>> classes) {
        return classes.stream().anyMatch(s -> s.isInstance(obj));
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
                p -> isInstanceOfAny(p, OPERAND_BOUNDARY_CLASSES));
    }
}
