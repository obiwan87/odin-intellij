package com.lasagnerd.odin.insights;

import com.intellij.openapi.util.Conditions;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.lang.psi.*;
import groovy.transform.Undefined;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
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

        // File scope declarations
        // procedures ... done
        // variables ... done
        // constants ... done
        // structs ... done
        // enums ... done
        // unions ... done
        // procedure overloads ... done
        // type aliases ... done but recognized as constants
        // TODO
        // foreign import blocks
        // foreign procedure
        // when statements
        //

        // Check file scope
        OdinFileScope odinFileScope = (OdinFileScope) PsiTreeUtil.findFirstParent(lastValidBlock, psi -> psi instanceof OdinFileScope);
        if (odinFileScope != null) {
            List<OdinDeclaredIdentifier> fileScopeDeclarations = getFileScopeDeclarations(odinFileScope);
            for (OdinDeclaredIdentifier fileScopeDeclaration : fileScopeDeclarations) {
                boolean isMatch = matcher.test(fileScopeDeclaration);
                if (isMatch) {
                    return fileScopeDeclaration;
                }
            }
        }
        return null;
    }


    public static List<OdinDeclaredIdentifier> getFileScopeDeclarations(OdinFileScope fileScope) {
        // Find all blocks that are not in a procedure
        List<OdinDeclaredIdentifier> declarations = new ArrayList<>();

        Stack<PsiElement> statementStack = new Stack<>();

        // do bfs
        statementStack.addAll(fileScope.getFileScopeStatementList().getStatementList());
        while(!statementStack.isEmpty()) {
            PsiElement element = statementStack.pop();
            if(element instanceof OdinDeclaration declaration) {
                declarations.addAll(declaration.getDeclaredIdentifiers());
            } else {
                getStatements(element).forEach(statementStack::push);
            }
        }
        return declarations;
    }

    public static List<OdinStatement> getStatements(PsiElement psiElement) {
        if(psiElement instanceof OdinWhenStatement odinWhenStatement) {
            if(odinWhenStatement.getBlock() != null) {
                OdinStatementList statementList = odinWhenStatement.getBlock().getStatementList();
                if(statementList != null) {
                    return statementList.getStatementList();
                }
            }

            if(odinWhenStatement.getDoStatement() != null) {
                return List.of(odinWhenStatement.getDoStatement());
            }
        }

        if(psiElement instanceof OdinForeignBlock foreignBlock) {
            OdinForeignStatementList foreignStatementList = foreignBlock.getForeignStatementList();
            if(foreignStatementList != null) {
                return foreignStatementList.getStatementList();
            }
        }

        return Collections.emptyList();
    }

    @NotNull
    public static List<OdinDeclaredIdentifier> findDeclarations(PsiElement element, Predicate<PsiElement> matcher) {
        List<OdinDeclaredIdentifier> declarations = new ArrayList<>();
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
        OdinFileScope fileScope = (OdinFileScope) PsiTreeUtil.findFirstParent(lastValidBlock, psi -> psi instanceof OdinFileScope);
        List<OdinDeclaredIdentifier> fileScopeDeclarations = getFileScopeDeclarations(fileScope);

        declarations.addAll(fileScopeDeclarations.stream().filter(matcher).toList());

        return declarations;
    }

    public static List<OdinDeclaredIdentifier> getFileScopeDeclarations(OdinFileScope odinFileScope, Predicate<PsiElement> matcher) {
        return getFileScopeDeclarations(odinFileScope).stream().filter(matcher).toList();
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
        } else if (child instanceof OdinProcedureOverloadStatement procedureOverloadStatement) {
            identifierList = List.of(procedureOverloadStatement.getDeclaredIdentifier());
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

    public static boolean isProcedureOverloadDeclaration(PsiElement element) {
        return element.getParent() instanceof OdinProcedureOverloadStatement;
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

    public static OdinTypeType classify(OdinDeclaredIdentifier element) {
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
        } else if(isProcedureOverloadDeclaration(element)) {
            return OdinTypeType.PROCEUDRE_OVERLOAD;
        } else {
            return OdinTypeType.UNKNOWN;
        }
    }

    public static OdinTypeType classify(PsiElement psiElement) {
        return null;
    }

    public static OdinProcedureDeclarationStatement getDeclaringProcedure(OdinDeclaredIdentifier element) {
        return element.getParent() instanceof OdinProcedureDeclarationStatement ? (OdinProcedureDeclarationStatement) element.getParent() : null;
    }

    public enum OdinTypeType {
        STRUCT,
        ENUM,
        UNION,
        PROCEDURE,
        PROCEUDRE_OVERLOAD,
        VARIABLE,
        CONSTANT,
        UNKNOWN

    }
}
