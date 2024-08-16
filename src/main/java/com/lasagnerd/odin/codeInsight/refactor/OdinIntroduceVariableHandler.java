package com.lasagnerd.odin.codeInsight.refactor;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.ide.util.PsiElementListCellRenderer;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.impl.source.codeStyle.CodeEditUtil;
import com.intellij.psi.presentation.java.SymbolPresentationUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.introduce.IntroduceHandler;
import com.intellij.refactoring.introduce.PsiIntroduceTarget;
import com.intellij.refactoring.introduce.inplace.AbstractInplaceIntroducer;
import com.intellij.refactoring.introduce.inplace.OccurrencesChooser;
import com.intellij.usageView.UsageInfo;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class OdinIntroduceVariableHandler extends IntroduceHandler<PsiIntroduceTarget<OdinExpression>, PsiElement> {
    private static final Logger LOG = Logger.getInstance(OdinIntroduceVariableHandler.class);
    PsiElement element;

    public OdinIntroduceVariableHandler(PsiElement element) {
        this.element = element;
    }

    @Override
    protected @NotNull List<UsageInfo> collectUsages(@NotNull PsiIntroduceTarget<OdinExpression> target, @NotNull PsiElement PsiElement) {
        if (target.getPlace() == null)
            return Collections.emptyList();

        OdinExpression odinExpression = target.getPlace();
        OdinProcedureBody context = PsiTreeUtil.getParentOfType(odinExpression, true, OdinProcedureBody.class);
        if (context == null)
            return Collections.emptyList();

        List<OdinExpression> occurrences = new ArrayList<>();
        occurrences.add(odinExpression);
        context.accept(new OdinRecursiveVisitor() {
                           @Override
                           public void visitExpression(@NotNull OdinExpression currentExpr) {

                               if (currentExpr == odinExpression) {
                                   return;
                               }
                               if (PsiEquivalenceUtil.areElementsEquivalent(currentExpr, odinExpression)) {
                                   occurrences.add(currentExpr);
                                   return;
                               }
                               super.visitExpression(currentExpr);
                           }
                       }
        );

        return occurrences.stream().map(UsageInfo::new).toList();
    }


    @Override
    protected @Nullable @NlsContexts.DialogMessage String checkUsages(@NotNull List<UsageInfo> usages) {
        return null;
    }

    @Override
    protected @NotNull List<PsiElement> collectTargetScopes(@NotNull PsiIntroduceTarget<OdinExpression> target,
                                                            @NotNull Editor editor,
                                                            @NotNull PsiFile file,
                                                            @NotNull Project project) {
        OdinExpression odinExpression = target.getPlace();
        if (odinExpression == null)
            return List.of(file);
        OdinProcedureBody procedureBody = PsiTreeUtil.getParentOfType(odinExpression, OdinProcedureBody.class);
        if (procedureBody == null)
            return List.of(file);

        return List.of(procedureBody);
    }

    @Override
    protected @NotNull Pair<List<PsiIntroduceTarget<OdinExpression>>, Integer> collectTargets(@NotNull PsiFile file,
                                                                                              @NotNull Editor editor,
                                                                                              @NotNull Project project) {
        if (element == null)
            return Pair.create(Collections.emptyList(), 0);
        OdinExpression odinExpression = PsiTreeUtil.getParentOfType(element, OdinExpression.class);
        if (odinExpression != null) {
            List<PsiIntroduceTarget<OdinExpression>> targets = new ArrayList<>();
            PsiTreeUtil.treeWalkUp(odinExpression, null, (scope, prevParent) -> {
                if (scope instanceof OdinExpression curr) {
                    targets.add(new PsiIntroduceTarget<>(curr));
                    return true;
                }
                return false;
            });

            return Pair.create(targets, 0);
        }

        OdinStatementList statementList = PsiTreeUtil.getParentOfType(element, OdinStatementList.class);
        if (statementList == null)
            return Pair.create(Collections.emptyList(), 0);

        PsiElement prevParent = PsiTreeUtil.findPrevParent(statementList, element);

        OdinExpressionStatement expressionStatement = PsiTreeUtil.getPrevSiblingOfType(prevParent, OdinExpressionStatement.class);
        if (expressionStatement != null) {
            OdinExpression expression = expressionStatement.getExpression();
            if (expression != null) {
                return Pair.create(List.of(new PsiIntroduceTarget<>(expression)), 0);
            }
        }
        return Pair.create(Collections.emptyList(), 0);
    }

    @Override
    protected @Nullable PsiIntroduceTarget<OdinExpression> findSelectionTarget(int start,
                                                                               int end,
                                                                               @NotNull PsiFile file,
                                                                               @NotNull Editor editor,
                                                                               @NotNull Project project) {
        if (file instanceof OdinExpression OdinExpression)
            return new PsiIntroduceTarget<>(OdinExpression);
        return null;
    }

    @Override
    protected @Nullable @NlsContexts.DialogMessage String checkSelectedTarget(@NotNull PsiIntroduceTarget<OdinExpression> target, @NotNull PsiFile file, @NotNull Editor editor, @NotNull Project project) {
        return null;
    }

    @Override
    protected @NotNull @NlsContexts.DialogTitle String getRefactoringName() {
        return "";
    }

    @Override
    protected @Nullable String getHelpID() {
        return "";
    }

    @Override
    protected @NotNull @NlsContexts.PopupTitle String getChooseScopeTitle() {
        return "";
    }

    @Override
    protected @NotNull PsiElementListCellRenderer<PsiElement> getScopeRenderer() {
        return new PsiElementListCellRenderer<>() {
            @Override
            public String getElementText(PsiElement element) {
                return SymbolPresentationUtil.getSymbolPresentableText(element);
            }

            @Override
            protected String getContainerText(PsiElement element, String name) {
                return SymbolPresentationUtil.getSymbolContainerText(element);
            }
        };
    }

    @Override
    protected @NotNull AbstractInplaceIntroducer<?, ?> getIntroducer(@NotNull PsiIntroduceTarget<OdinExpression> target,
                                                                     @NotNull PsiElement scopeBlock,
                                                                     @NotNull List<UsageInfo> usages,
                                                                     OccurrencesChooser.@NotNull ReplaceChoice replaceChoice,
                                                                     @NotNull PsiFile file,
                                                                     @NotNull Editor editor,
                                                                     @NotNull Project project) {

        // The chosen expression
        OdinExpression targetExpression = target.getPlace();
        Objects.requireNonNull(targetExpression);

        // If there is more than one occurrence find the one that is top-most
        var sortedUsages = usages.stream().sorted(Comparator.comparing(UsageInfo::getNavigationOffset)).toList();
        UsageInfo firstUsage = sortedUsages.getFirst();
        Objects.requireNonNull(firstUsage.getElement());

        // Perform replace of the expression with variable initialization
        OdinVariableInitializationStatement varInitStatement;
        OdinExpression[] occurrences;
        int n;


        List<String> nameSuggestions = getNameSuggestions(targetExpression);

        if (firstUsage.getElement().getParent() instanceof OdinExpressionStatement) {
            // This means we can replace the first occurrence with the variable initialization
            varInitStatement = performReplace(project, (OdinExpression) firstUsage.getElement(), nameSuggestions.getFirst());
            n = 1;
        } else {
            // When the expression is not directly under an expression statement, we have to create
            // new local variable before the first usage
            varInitStatement = performReplaceWithNewStatement(project, firstUsage, targetExpression, nameSuggestions.getFirst());
            n = 0;
        }

        OdinDeclaredIdentifier declaredIdentifier = Objects.requireNonNull(varInitStatement)
                .getDeclaredIdentifiers().getFirst();


        // Occurrences are only needed when expression needs to be replaced with newly introduced variable
        occurrences = sortedUsages.stream()
                .skip(n)
                .map(UsageInfo::getElement)
                .map(e -> (OdinExpression) e)
                .toList()
                .toArray(new OdinExpression[0]);

        // AFAIK it is used for the template
        OdinDeclaredIdentifier templateIdentifier = OdinPsiElementFactory.getInstance(project)
                .createDeclaredIdentifier(nameSuggestions.getFirst());

        return new OdinVariableIntroducer(project,
                editor,
                declaredIdentifier,
                occurrences,
                templateIdentifier,
                replaceChoice.isAll(),
                nameSuggestions.toArray(new String[0]));
    }

    private List<String> getNameSuggestions(OdinExpression targetExpression) {
        List<String> names = new ArrayList<>();
        if (targetExpression instanceof OdinRefExpression refExpression) {

            OdinIdentifier identifier = refExpression.getIdentifier();
            if (identifier != null)
                names.add(identifier.getText());
        }

        if (names.isEmpty()) {
            names.add("value");
        }

        return names;
    }

    private static @Nullable OdinVariableInitializationStatement performReplaceWithNewStatement(@NotNull Project project, UsageInfo topMostUsage, OdinExpression targetExpression, String name) {
        OdinVariableInitializationStatement varInitStatement;
        AtomicReference<SmartPsiElementPointer<OdinVariableInitializationStatement>> newElementRef = new AtomicReference<>();

        WriteCommandAction.runWriteCommandAction(project, () -> {
            OdinStatement odinStatement = PsiTreeUtil.getParentOfType(topMostUsage.getElement(), OdinStatement.class);
            if (odinStatement == null)
                return;

            OdinStatementList statementList = PsiTreeUtil.getParentOfType(odinStatement, OdinStatementList.class);
            if (statementList == null)
                return;

            OdinVariableInitializationStatement varInit = OdinPsiElementFactory.getInstance(project)
                    .createVariableInitializationStatement(name, "1");
            OdinEos eos = OdinPsiElementFactory.getInstance(project).createEos();

            varInit.getExpressionsList()
                    .getExpressionList()
                    .getFirst()
                    .replace(targetExpression);

            PsiElement psiElement = statementList.addBefore(varInit, odinStatement);
            statementList.addAfter(eos, psiElement);

            newElementRef.set(SmartPointerManager.createPointer((OdinVariableInitializationStatement) psiElement));
        });
        varInitStatement = newElementRef.get().getElement();
        return varInitStatement;
    }

    private static @Nullable OdinVariableInitializationStatement performReplace(@NotNull Project project, OdinExpression odinExpression, String name) {
        AtomicReference<SmartPsiElementPointer<OdinVariableInitializationStatement>> newElementRef = new AtomicReference<>();

        WriteCommandAction.runWriteCommandAction(project, () -> {
            OdinVariableInitializationStatement refactoringVar = OdinPsiElementFactory
                    .getInstance(project)
                    .createVariableInitializationStatement("new_var", "1");

            // Your PSI modification logic here
            refactoringVar.getExpressionsList()
                    .getExpressionList().getFirst()
                    .replace(odinExpression);

            OdinVariableInitializationStatement newElement = (OdinVariableInitializationStatement) odinExpression.replace(refactoringVar);
            newElementRef.set(SmartPointerManager.createPointer(newElement));

            CodeEditUtil.setNodeGeneratedRecursively(newElement.getNode(), true);
        });
        return newElementRef.get().getElement();
    }

}
