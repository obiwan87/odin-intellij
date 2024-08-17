package com.lasagnerd.odin.codeInsight.refactor;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.ide.util.PsiElementListCellRenderer;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.presentation.java.SymbolPresentationUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.introduce.IntroduceHandler;
import com.intellij.refactoring.introduce.PsiIntroduceTarget;
import com.intellij.refactoring.introduce.inplace.AbstractInplaceIntroducer;
import com.intellij.refactoring.introduce.inplace.OccurrencesChooser;
import com.intellij.usageView.UsageInfo;
import com.lasagnerd.odin.codeInsight.typeInference.OdinInferenceEngine;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinPackageReferenceType;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinType;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
                if(scope instanceof OdinRefExpression refExpression) {
                    if(refExpression.getExpression() == null) {
                        TsOdinType tsOdinType = OdinInferenceEngine.doInferType(refExpression);
                        if(tsOdinType instanceof TsOdinPackageReferenceType) {
                            return true;
                        }
                    }
                }
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
        if (start == end - 1) {
            PsiElement element = file.findElementAt(start);
            if (element != null) {
                OdinExpression expression = PsiTreeUtil.getParentOfType(element, OdinExpression.class);
                if (expression != null)
                    return new PsiIntroduceTarget<>(expression);

            }
            return null;
        }
        PsiElement startElement = file.findElementAt(start);
        PsiElement endElement = file.findElementAt(end - 1);

        if (startElement == null || endElement == null)
            return null;
        PsiElement commonParent = PsiTreeUtil.findCommonParent(startElement, endElement);
        if (!(commonParent instanceof OdinExpression parentExpression))
            return null;

        TextRange textRange = parentExpression.getTextRange();
        if (!(textRange.getStartOffset() == start && textRange.getEndOffset() == end)) {
            return null;
        }

        return new PsiIntroduceTarget<>(parentExpression);
    }

    @Override
    protected @Nullable @NlsContexts.DialogMessage String checkSelectedTarget(@NotNull PsiIntroduceTarget<OdinExpression> target, @NotNull PsiFile file, @NotNull Editor editor, @NotNull Project project) {
        return null;
    }

    @Override
    protected @NotNull @NlsContexts.DialogTitle String getRefactoringName() {
        return "Introduce Variable";
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

        return OdinVariableIntroducer.createVariableIntroducer(target, usages, replaceChoice, editor, project);
    }


}
