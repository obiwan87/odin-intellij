package com.lasagnerd.odin.insights;

import com.intellij.openapi.util.Conditions;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class OdinInsightUtils {
    @Nullable
    public static PsiElement findFirstDeclaration(PsiElement element, Predicate<PsiElement> matcher) {
        PsiElement entrance = element;
        PsiElement lastValidBlock = element;

        // Check all parent blocks
        while (entrance != null) {
            OdinBlock containingBlock = (OdinBlock) PsiTreeUtil.findFirstParent(entrance, true, parent -> parent instanceof OdinBlock);
            entrance = containingBlock;
            if (containingBlock == null) {
                break;
            }

            lastValidBlock = containingBlock;

            OdinStatementList statementList = containingBlock.getStatementList();
            if (statementList == null) return null;
            for (OdinStatement statement : statementList.getStatementList()) {
                List<OdinDeclaredIdentifier> matchingDeclarations = getMatchingDeclarations(matcher, statement);
                if (!matchingDeclarations.isEmpty()) return matchingDeclarations.get(0);
            }
        }

        // Check file scope
        OdinFileScope odinFileScope = (OdinFileScope) PsiTreeUtil.findFirstParent(lastValidBlock, psi -> psi instanceof OdinFileScope);
        if (odinFileScope != null) {
            OdinFileScopeStatementList fileScopeStatementList = odinFileScope.getFileScopeStatementList();

            for (OdinStatement odinStatement : fileScopeStatementList.getStatementList()) {
                List<OdinDeclaredIdentifier> matchingDeclarations = getMatchingDeclarations(matcher, odinStatement);
                if (!matchingDeclarations.isEmpty()) {
                    return matchingDeclarations.get(0);
                }
            }
        }
        return null;
    }

    @NotNull
    public static List<PsiElement> findDeclarations(PsiElement element, Predicate<PsiElement> matcher) {
        List<PsiElement> declarations = new ArrayList<>();
        PsiElement entrance = element;
        PsiElement lastValidBlock = element;

        // Check all parent blocks
        while (entrance != null) {
            OdinBlock containingBlock = (OdinBlock) PsiTreeUtil.findFirstParent(entrance, true, parent -> parent instanceof OdinBlock);
            entrance = containingBlock;
            if (containingBlock == null) {
                break;
            }

            lastValidBlock = containingBlock;

            OdinStatementList statementList = containingBlock.getStatementList();
            if (statementList == null) {
                continue;
            }

            for (OdinStatement statement : statementList.getStatementList()) {
                List<OdinDeclaredIdentifier> matchingDeclarations = getMatchingDeclarations(matcher, statement);
                declarations.addAll(matchingDeclarations);
            }
        }

        // Check file scope
        OdinFileScope odinFileScope = (OdinFileScope) PsiTreeUtil.findFirstParent(lastValidBlock, psi -> psi instanceof OdinFileScope);
        if (odinFileScope != null) {
            OdinFileScopeStatementList fileScopeStatementList = odinFileScope.getFileScopeStatementList();

            for (OdinStatement statement : fileScopeStatementList.getStatementList()) {
                List<OdinDeclaredIdentifier> matchedIdentifiers = getMatchingDeclarations(matcher, statement);
                declarations.addAll(matchedIdentifiers);
            }
        }
        return declarations;
    }

    private static List<OdinDeclaredIdentifier> getMatchingDeclarations(Predicate<PsiElement> matcher, OdinStatement statement) {
        List<OdinDeclaredIdentifier> odinDeclaredIdentifiers = new ArrayList<>();
        List<OdinDeclaredIdentifier> declarations = getDeclarations(statement);
        for (OdinDeclaredIdentifier identifier : declarations) {
            boolean isMatch = matcher.test(identifier);
            if (isMatch) {
                odinDeclaredIdentifiers.add(identifier);
            }
        }
        return odinDeclaredIdentifiers;
    }

    @NotNull
    private static List<OdinDeclaredIdentifier> getDeclarations(PsiElement child) {
        List<OdinDeclaredIdentifier> identifierList;
        if (child instanceof OdinVariableDeclarationStatement variableDeclaration) {
            identifierList = variableDeclaration.getIdentifierList().getDeclaredIdentifierList();
        } else if (child instanceof OdinVariableInitializationStatement variableInitialization) {
            identifierList = variableInitialization.getIdentifierList().getDeclaredIdentifierList();
        } else if (child instanceof OdinProcedureDeclarationStatement procedureDeclaration) {
            identifierList = List.of(procedureDeclaration.getDeclaredIdentifier());
        } else if (child instanceof OdinConstantInitializationStatement constantInitializationStatement) {
            identifierList = constantInitializationStatement.getIdentifierList().getDeclaredIdentifierList();
        } else if (child instanceof OdinStructDeclarationStatement structDeclarationStatement) {
            identifierList = List.of(structDeclarationStatement.getDeclaredIdentifier());
        } else if (child instanceof OdinEnumDeclarationStatement enumDeclarationStatement) {
            identifierList = List.of(enumDeclarationStatement.getDeclaredIdentifier());
        } else if (child instanceof OdinUnionDeclarationStatement unionDeclarationStatement) {
            identifierList = List.of(unionDeclarationStatement.getDeclaredIdentifier());
        } else {
            identifierList = Collections.emptyList();
        }
        return identifierList;
    }

    public static <T> T findFirstParentOfType(PsiElement element, boolean strict, Class<T> type) {
        //noinspection unchecked
        return (T) PsiTreeUtil.findFirstParent(element, strict, Conditions.instanceOf(type));
    }

    public static boolean isVariableDeclaration(PsiElement element) {
        return findFirstParentOfType(element, true, OdinVariableDeclarationStatement.class) != null
                || findFirstParentOfType(element, true, OdinVariableInitializationStatement.class) != null;
    }

    public static boolean isProcedureDeclaration(PsiElement element) {
        return element.getParent() instanceof OdinProcedureDeclarationStatement;
    }

    public static boolean isConstantDeclaration(PsiElement element) {
        return findFirstParentOfType(element, true, OdinConstantInitializationStatement.class) != null;
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

    public static OdinTypeType classify(PsiElement element) {
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
        } else {
            return OdinTypeType.UNKNOWN;
        }
    }


    public enum OdinTypeType {
        STRUCT,
        ENUM,
        UNION,
        PROCEDURE,
        VARIABLE,
        CONSTANT,
        UNKNOWN

    }
}
