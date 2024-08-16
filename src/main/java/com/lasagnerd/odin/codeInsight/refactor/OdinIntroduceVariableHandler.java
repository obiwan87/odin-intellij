package com.lasagnerd.odin.codeInsight.refactor;

import com.intellij.ide.util.PsiElementListCellRenderer;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.presentation.java.SymbolPresentationUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.introduce.IntroduceHandler;
import com.intellij.refactoring.introduce.PsiIntroduceTarget;
import com.intellij.refactoring.introduce.inplace.AbstractInplaceIntroducer;
import com.intellij.refactoring.introduce.inplace.OccurrencesChooser;
import com.intellij.usageView.UsageInfo;
import com.lasagnerd.odin.lang.OdinFileType;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class OdinIntroduceVariableHandler extends IntroduceHandler<PsiIntroduceTarget<OdinExpression>, OdinExpression> {
    PsiElement element;

    public OdinIntroduceVariableHandler(PsiElement element) {
        this.element = element;
    }

    @Override
    protected @NotNull List<UsageInfo> collectUsages(@NotNull PsiIntroduceTarget<OdinExpression> target, @NotNull OdinExpression odinExpression) {
        return List.of();
    }

    @Override
    protected @Nullable @NlsContexts.DialogMessage String checkUsages(@NotNull List<UsageInfo> usages) {
        return null;
    }

    @Override
    protected @NotNull List<OdinExpression> collectTargetScopes(@NotNull PsiIntroduceTarget<OdinExpression> target,
                                                                @NotNull Editor editor,
                                                                @NotNull PsiFile file,
                                                                @NotNull Project project) {
        OdinExpression odinExpression = target.getPlace();
        if (odinExpression == null) {
            return List.of();
        }
        List<OdinExpression> expressions = new ArrayList<>();

        PsiTreeUtil.treeWalkUp(odinExpression, null, (scope, prevParent) -> {
            if (scope instanceof OdinExpression curr) {
                expressions.add(curr);
                return true;
            }
            return false;
        });
        return expressions;
    }

    @Override
    protected @NotNull Pair<List<PsiIntroduceTarget<OdinExpression>>, Integer> collectTargets(@NotNull PsiFile file,
                                                                                              @NotNull Editor editor,
                                                                                              @NotNull Project project) {
        if (element == null)
            return Pair.create(Collections.emptyList(), 0);

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
    protected @NotNull PsiElementListCellRenderer<OdinExpression> getScopeRenderer() {
        return new PsiElementListCellRenderer<>() {
            @Override
            public String getElementText(OdinExpression element) {
                return SymbolPresentationUtil.getSymbolPresentableText(element);
            }

            @Override
            protected String getContainerText(OdinExpression element, String name) {
                return SymbolPresentationUtil.getSymbolContainerText(element);
            }
        };
    }

    @Override
    protected @NotNull AbstractInplaceIntroducer<?, ?> getIntroducer(@NotNull PsiIntroduceTarget<OdinExpression> target,
                                                                     @NotNull OdinExpression odinExpression,
                                                                     @NotNull List<UsageInfo> usages,
                                                                     OccurrencesChooser.@NotNull ReplaceChoice replaceChoice,
                                                                     @NotNull PsiFile file,
                                                                     @NotNull Editor editor,
                                                                     @NotNull Project project) {

        var refactoringVar = OdinPsiElementFactory.getInstance(project)
                .createVariableInitializationStatement("new_var", odinExpression.getText());

        AtomicReference<PsiElement> newElementRef = new AtomicReference<>();
        WriteCommandAction.runWriteCommandAction(project, () -> {
            // Your PSI modification logic here
            PsiElement newElement = odinExpression.replace(refactoringVar);
            newElementRef.set(newElement);
        });

        var templateVar = OdinPsiElementFactory.getInstance(project)
                .createVariableInitializationStatement("new_var", odinExpression.getText());
        OdinDeclaredIdentifier templateIdentifier = templateVar.getDeclaredIdentifiers().getFirst();
        OdinVariableInitializationStatement variableInitializationStatement = (OdinVariableInitializationStatement) newElementRef.get();
        OdinDeclaredIdentifier declaredIdentifier = variableInitializationStatement.getDeclaredIdentifiers().getFirst();

        return new AbstractInplaceIntroducer<>(project,
                editor,
                null,
                declaredIdentifier,
                new OdinExpression[0],
                null,
                OdinFileType.INSTANCE
        ) {
            @Override
            protected @Nullable @NonNls String getActionName() {
                return null;
            }

            @Override
            protected @Nullable OdinDeclaredIdentifier createFieldToStartTemplateOn(boolean replaceAll, String @NotNull [] names) {

                return declaredIdentifier;
            }

            @Override
            protected String @NotNull [] suggestNames(boolean replaceAll, @Nullable OdinDeclaredIdentifier variable) {
                return new String[]{"var1", "var2", "var3"};
            }

            @Override
            protected void performIntroduce() {

            }

            @Override
            public boolean isReplaceAllOccurrences() {
                return false;
            }

            @Override
            public void setReplaceAllOccurrences(boolean allOccurrences) {

            }

            @Override
            protected @Nullable JComponent getComponent() {
                return null;
            }

            @Override
            protected void saveSettings(@NotNull OdinDeclaredIdentifier variable) {

            }

            @Override
            protected @Nullable OdinDeclaredIdentifier getVariable() {
                return templateIdentifier;
            }

            @Override
            public OdinExpression restoreExpression(@NotNull PsiFile containingFile,
                                                    @NotNull OdinDeclaredIdentifier variable,
                                                    @NotNull RangeMarker marker,
                                                    @Nullable String exprText) {
                return null;
            }

        };
    }
}
