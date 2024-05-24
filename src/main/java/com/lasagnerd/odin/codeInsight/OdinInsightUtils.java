package com.lasagnerd.odin.codeInsight;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.codeInsight.typeInference.OdinTypeResolver;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinPackageReferenceType;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinPointerType;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinType;
import com.lasagnerd.odin.lang.psi.*;
import org.apache.commons.text.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class OdinInsightUtils {

    public static OdinSymbol findSymbol(OdinIdentifier identifier) {
        OdinScope parentScope = OdinScopeResolver.resolveScope(identifier).with(OdinImportUtils.getPackagePath(identifier));
        OdinRefExpression refExpression = PsiTreeUtil.getParentOfType(identifier, true, OdinRefExpression.class);
        OdinScope scope;
        if (refExpression != null) {
            if (refExpression.getExpression() != null) {
                scope = OdinReferenceResolver.resolve(parentScope, refExpression.getExpression());
            } else {
                scope = parentScope;
            }
        } else {
            OdinQualifiedType qualifiedType = PsiTreeUtil.getParentOfType(identifier, true, OdinQualifiedType.class);
            if (qualifiedType != null) {
                scope = OdinReferenceResolver.resolve(parentScope, qualifiedType);
            } else {
                scope = parentScope;
            }
        }

        if (scope == OdinScope.EMPTY || scope == null) {
            scope = parentScope;
        }

        if (scope != null) {
            return scope.getSymbol(identifier.getIdentifierToken().getText());
        }

        return null;
    }

    public static boolean isStringLiteralWithValue(OdinExpression odinExpression, String val) {
        return Objects.equals(getStringLiteralValue(odinExpression), val);
    }

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
    public static OdinScope getScopeProvidedByType(TsOdinType type) {
        if (type instanceof TsOdinPackageReferenceType packageType) {
            return OdinImportUtils
                    .getSymbolsOfImportedPackage(packageType.getReferencingPackagePath(),
                            (OdinImportDeclarationStatement) packageType.getDeclaration());
        }
        OdinScope typeScope = type.getScope();
        OdinScope scope = new OdinScope();
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
                OdinScope subScope = getScopeProvidedByType(usedType);
                scope.putAll(subScope);
            }

            scope.addAll(structFields);
            scope.addTypes(typeScope);
            return scope;
        }

        if (odinDeclaration instanceof OdinEnumDeclarationStatement enumDeclarationStatement) {
            return scope.with(getEnumFields(enumDeclarationStatement));
        }

        return OdinScope.EMPTY;
    }

    @NotNull
    public static List<OdinSymbol> getEnumFields(OdinEnumDeclarationStatement enumDeclarationStatement) {
        OdinEnumType enumType = enumDeclarationStatement.getEnumType();

        return getEnumFields(enumType);
    }

    private static @NotNull List<OdinSymbol> getEnumFields(OdinEnumType enumType) {
        OdinEnumBody enumBody = enumType
                .getEnumBlock()
                .getEnumBody();

        if (enumBody == null)
            return Collections.emptyList();

        return enumBody
                .getEnumValueDeclarationList()
                .stream()
                .map(OdinEnumValueDeclaration::getDeclaredIdentifier)
                .map(OdinSymbol::new)
                .collect(Collectors.toList());
    }

    public static List<OdinSymbol> getStructFields(OdinStructDeclarationStatement structDeclarationStatement) {
        List<OdinFieldDeclarationStatement> fieldDeclarationStatementList = getStructFieldsDeclarationStatements(structDeclarationStatement);

        return fieldDeclarationStatementList.stream()
                .flatMap(x -> x.getDeclaredIdentifiers().stream())
                .map(OdinSymbol::new)
                .collect(Collectors.toList());
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


}
