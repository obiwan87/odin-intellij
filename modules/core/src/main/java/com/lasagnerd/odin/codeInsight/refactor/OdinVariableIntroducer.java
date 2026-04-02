package com.lasagnerd.odin.codeInsight.refactor;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.template.*;
import com.intellij.codeInsight.template.impl.TemplateManagerImpl;
import com.intellij.codeInsight.template.impl.TemplateState;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.impl.source.codeStyle.CodeEditUtil;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageEditorUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.introduce.PsiIntroduceTarget;
import com.intellij.refactoring.introduce.inplace.AbstractInplaceIntroducer;
import com.intellij.refactoring.introduce.inplace.OccurrencesChooser;
import com.intellij.usageView.UsageInfo;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinType;
import com.lasagnerd.odin.lang.OdinFileType;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class OdinVariableIntroducer extends AbstractInplaceIntroducer<OdinDeclaredIdentifier, OdinExpression> {

    public static final String TYPE_VARIABLE = "TYPE_VARIABLE";

    private final SmartPsiElementPointer<OdinDeclaredIdentifier> declaredIdentifier;

    private static @NotNull List<OdinExpression> toExpressions(@NotNull List<UsageInfo> sortedUsages) {
        List<OdinExpression> result = new ArrayList<>(sortedUsages.size());
        for (UsageInfo usage : sortedUsages) {
            PsiElement el = usage.getElement();
            if (el instanceof OdinExpression expr) {
                result.add(expr);
            }
        }
        return result;
    }

    private static OdinExpression @NotNull [] skipNOccurrences(@NotNull List<OdinExpression> occurrences, int n) {
        return occurrences.stream()
                .skip(n)
                .toArray(OdinExpression[]::new);
    }

    private static SmartPsiElementPointer<OdinInitVariableStatement> performReplaceWithNewStatement(@NotNull Project project,
                                                                                                    @NotNull OdinExpression firstOccurrenceExpression,
                                                                                                    @NotNull OdinExpression targetExpression,
                                                                                                    @NotNull String name) {
        AtomicReference<SmartPsiElementPointer<OdinInitVariableStatement>> newElementRef = new AtomicReference<>();

        WriteCommandAction.runWriteCommandAction(project, () -> {
            OdinStatement odinStatement = PsiTreeUtil.getParentOfType(firstOccurrenceExpression, OdinStatement.class);
            OdinStatementList statementList = PsiTreeUtil.getParentOfType(odinStatement, OdinStatementList.class);

            Objects.requireNonNull(odinStatement);
            Objects.requireNonNull(statementList);

            OdinInitVariableStatement varInit = OdinPsiElementFactory.getInstance(project)
                    .createInitVariableStatement(name, "1");
            OdinEos eos = OdinPsiElementFactory.getInstance(project).createEos();

            var expressionsList = varInit.getRhsExpressions();
            Objects.requireNonNull(expressionsList);
            expressionsList
                    .getExpressionList()
                    .getFirst()
                    .replace(targetExpression);

            PsiElement psiElement = statementList.addBefore(varInit, odinStatement);
            statementList.addAfter(eos, psiElement);

            newElementRef.set(SmartPointerManager.createPointer((OdinInitVariableStatement) psiElement));
        });

        return newElementRef.get();
    }

    private static SmartPsiElementPointer<OdinInitVariableStatement> performReplace(@NotNull Project project,
                                                                                    OdinExpression odinExpression,
                                                                                    String name) {
        AtomicReference<SmartPsiElementPointer<OdinInitVariableStatement>> newElementRef = new AtomicReference<>();

        WriteCommandAction.runWriteCommandAction(project, () -> {
            OdinInitVariableStatement refactoringVar = OdinPsiElementFactory
                    .getInstance(project)
                    .createInitVariableStatement(name, "1");

            var expressionList = refactoringVar.getRhsExpressions();
            Objects.requireNonNull(expressionList);
            expressionList.getExpressionList().getFirst()
                    .replace(odinExpression);

            OdinInitVariableStatement newElement = (OdinInitVariableStatement) odinExpression.replace(refactoringVar);
            newElementRef.set(SmartPointerManager.createPointer(newElement));

            CodeEditUtil.setNodeGeneratedRecursively(newElement.getNode(), true);
        });
        return newElementRef.get();
    }

    private final String originalText;
    private final SmartPsiElementPointer<OdinInitVariableStatement> initVariableStatement;
    private final @NotNull PsiIntroduceTarget<OdinExpression> target;
    private final String[] nameSuggestions;
    private boolean replaceAllOccurrences;

    // Existing interface/signature MUST remain unchanged.
    public static @NotNull OdinVariableIntroducer createVariableIntroducer(@NotNull PsiIntroduceTarget<OdinExpression> target,
                                                                           @NotNull List<UsageInfo> usages,
                                                                           OccurrencesChooser.@NotNull ReplaceChoice replaceChoice,
                                                                           @NotNull Editor editor,
                                                                           @NotNull Project project) {
        String originalText = editor.getDocument().getText();

        OdinExpression targetExpression = target.getPlace();
        Objects.requireNonNull(targetExpression);

        var sortedUsages = usages.stream().sorted(Comparator.comparing(UsageInfo::getNavigationOffset)).toList();
        List<OdinExpression> sortedExpressions = toExpressions(sortedUsages);

        // If usages were provided, we expect at least one expression occurrence.
        OdinExpression firstOccurrenceExpression = sortedExpressions.isEmpty()
                ? targetExpression
                : sortedExpressions.getFirst();

        SmartPsiElementPointer<OdinInitVariableStatement> varInitStatement;
        OdinExpression[] occurrences;

        List<String> nameSuggestions = OdinNameSuggester.getNameSuggestions(targetExpression);

        if (firstOccurrenceExpression.getParent() instanceof OdinExpressionStatement) {
            varInitStatement = performReplace(project, firstOccurrenceExpression, nameSuggestions.getFirst());
            if (replaceChoice.isAll()) {
                occurrences = skipNOccurrences(sortedExpressions, 1);
            } else {
                occurrences = new OdinExpression[0];
            }
        } else {
            varInitStatement = performReplaceWithNewStatement(project, firstOccurrenceExpression, targetExpression, nameSuggestions.getFirst());
            if (!replaceChoice.isAll()) {
                occurrences = new OdinExpression[]{targetExpression};
            } else {
                occurrences = skipNOccurrences(sortedExpressions, 0);
            }
        }

        OdinDeclaredIdentifier declaredIdentifier = Objects.requireNonNull(varInitStatement.getElement())
                .getDeclaration()
                .getDeclaredIdentifiers().getFirst();

        SmartPsiElementPointer<OdinDeclaredIdentifier> pointer = SmartPointerManager.createPointer(declaredIdentifier);
        return new OdinVariableIntroducer(project,
                editor,
                originalText,
                varInitStatement,
                pointer,
                target,
                occurrences,
                true,
                nameSuggestions.toArray(new String[0]));
    }

    // Variant without usages (the target itself is the occurrence). Does not break the existing method signature.
    @TestOnly
    public static @NotNull OdinVariableIntroducer createVariableIntroducer(@NotNull PsiIntroduceTarget<OdinExpression> target,
                                                                           OccurrencesChooser.@NotNull ReplaceChoice replaceChoice,
                                                                           @NotNull Editor editor,
                                                                           @NotNull Project project) {
        String originalText = editor.getDocument().getText();

        OdinExpression targetExpression = target.getPlace();
        Objects.requireNonNull(targetExpression);

        List<String> nameSuggestions = OdinNameSuggester.getNameSuggestions(targetExpression);

        SmartPsiElementPointer<OdinInitVariableStatement> varInitStatement;
        OdinExpression[] occurrences;

        if (targetExpression.getParent() instanceof OdinExpressionStatement) {
            varInitStatement = performReplace(project, targetExpression, nameSuggestions.getFirst());
            occurrences = new OdinExpression[0];
        } else {
            varInitStatement = performReplaceWithNewStatement(project, targetExpression, targetExpression, nameSuggestions.getFirst());
            occurrences = new OdinExpression[]{targetExpression};
        }

        OdinDeclaredIdentifier declaredIdentifier = Objects.requireNonNull(varInitStatement.getElement())
                .getDeclaration()
                .getDeclaredIdentifiers().getFirst();

        SmartPsiElementPointer<OdinDeclaredIdentifier> pointer = SmartPointerManager.createPointer(declaredIdentifier);
        return new OdinVariableIntroducer(project,
                editor,
                originalText,
                varInitStatement,
                pointer,
                target,
                occurrences,
                true,
                nameSuggestions.toArray(new String[0]));
    }

    public OdinVariableIntroducer(@NotNull Project project,
                                  @NotNull Editor editor,
                                  String originalText,
                                  SmartPsiElementPointer<OdinInitVariableStatement> initVariableStatement,
                                  SmartPsiElementPointer<OdinDeclaredIdentifier> declaredIdentifier,
                                  @NotNull PsiIntroduceTarget<OdinExpression> target,
                                  OdinExpression[] occurrences,
                                  boolean replaceAllOccurrences,
                                  String[] nameSuggestions) {
        super(project,
                editor,
                null,
                declaredIdentifier.getElement(),
                occurrences,
                "Introduce Variable",
                OdinFileType.INSTANCE);

        this.originalText = originalText;
        this.initVariableStatement = initVariableStatement;
        this.declaredIdentifier = declaredIdentifier;
        this.target = target;
        this.replaceAllOccurrences = replaceAllOccurrences;
        this.nameSuggestions = nameSuggestions;
    }

    @Override
    protected @Nullable @NonNls String getActionName() {
        return null;
    }

    @Override
    protected @Nullable OdinDeclaredIdentifier createFieldToStartTemplateOn(boolean replaceAll, String @NotNull [] names) {
        return declaredIdentifier.getElement();
    }

    @Override
    protected String @NotNull [] suggestNames(boolean replaceAll, @Nullable OdinDeclaredIdentifier variable) {
        return nameSuggestions;
    }

    @Override
    protected void performIntroduce() {
        System.out.println("performIntroduce() called");
    }

    @Override
    public boolean isReplaceAllOccurrences() {
        return replaceAllOccurrences;
    }

    @Override
    public void setReplaceAllOccurrences(boolean allOccurrences) {
        this.replaceAllOccurrences = allOccurrences;
    }

    @Override
    protected @Nullable JComponent getComponent() {
        System.out.println("getComponent() called");
        return null;
    }

    @Override
    protected void saveSettings(@NotNull OdinDeclaredIdentifier variable) {
        System.out.println("saveSettings() called");
    }

    @Override
    protected @Nullable OdinDeclaredIdentifier getVariable() {
        return declaredIdentifier.getElement();
    }

    @Override
    public OdinExpression restoreExpression(@NotNull PsiFile containingFile,
                                            @NotNull OdinDeclaredIdentifier variable,
                                            @NotNull RangeMarker marker,
                                            @Nullable String exprText) {
        System.out.println("restoreExpression() called");
        return null;
    }

    @Override
    protected String getRefactoringId() {
        return "Introduce variable";
    }

    @Override
    protected void addAdditionalVariables(TemplateBuilderImpl builder) {
        OdinInitVariableStatement varInit = initVariableStatement.getElement();
        if (varInit != null) {
            int startOffset = varInit.getInitVariableDeclaration().getColonOpening().getTextRange().getStartOffset();
            TextRange rangeWithinElement = TextRange.create(startOffset + 1, startOffset + 1);

            builder.replaceElement(varInit.getContainingFile(), rangeWithinElement, "");
        }
    }

    @SuppressWarnings("unused")
    private void createTypeVariableWithSuggestions(TemplateBuilderImpl builder, OdinInitVariableStatement varInit, TextRange rangeWithinElement) {
        OdinExpression targetExpression = this.target.getPlace();
        if (targetExpression != null) {
            TsOdinType tsOdinType = targetExpression.getInferredType();

            if (!tsOdinType.isUnknown()) {
                LinkedHashSet<String> names = new LinkedHashSet<>();
                names.add(tsOdinType.getName());

                builder.replaceElement(varInit.getContainingFile(),
                        rangeWithinElement,
                        TYPE_VARIABLE,
                        createLookupExpression(names),
                        true);
            } else {
                builder.replaceElement(varInit.getContainingFile(), rangeWithinElement, "");
            }
        } else {
            builder.replaceElement(varInit.getContainingFile(), rangeWithinElement, "");
        }
    }

    private static @NotNull OdinLookupExpression createLookupExpression(LinkedHashSet<String> names) {
        return new OdinLookupExpression("",
                names,
                false,
                "",
                TYPE_VARIABLE);
    }

    @Override
    protected void performCleanup() {
        System.out.println("performCleanup() called");
        WriteCommandAction.writeCommandAction(myProject).run(() -> myEditor.getDocument().setText(originalText));
    }

    public static class OdinLookupExpression extends Expression {
        protected final String myName;
        protected final LookupElement[] myLookupItems;
        private final String myAdvertisementText;

        private final String myVariableName;

        public OdinLookupExpression(String name,
                                    @NotNull LinkedHashSet<String> names,
                                    boolean shouldSelectAll,
                                    String advertisement,
                                    String variableName) {
            myName = name;
            myAdvertisementText = advertisement;
            myVariableName = variableName;
            myLookupItems = initLookupItems(names, shouldSelectAll);
        }

        private LookupElement[] initLookupItems(LinkedHashSet<String> names,
                                                final boolean shouldSelectAll) {
            final LookupElement[] lookupElements = new LookupElement[names.size()];
            final Iterator<String> iterator = names.iterator();
            for (int i = 0; i < lookupElements.length; i++) {
                final String suggestion = iterator.next();
                lookupElements[i] = LookupElementBuilder.create(suggestion).withInsertHandler((context, item) -> {
                    if (shouldSelectAll) return;
                    final Editor topLevelEditor = InjectedLanguageEditorUtil.getTopLevelEditor(context.getEditor());
                    final TemplateState templateState = TemplateManagerImpl.getTemplateState(topLevelEditor);
                    if (templateState != null) {
                        final TextRange range = templateState.getCurrentVariableRange();
                        if (range != null) {
                            topLevelEditor.getDocument().replaceString(range.getStartOffset(), range.getEndOffset(), suggestion);
                        }
                    }
                });
            }
            return lookupElements;
        }

        @Override
        public LookupElement[] calculateLookupItems(ExpressionContext context) {
            return myLookupItems;
        }

        @Override
        public Result calculateResult(ExpressionContext context) {
            final TextResult insertedValue = context.getVariableValue(myVariableName);
            if (insertedValue != null) {
                if (!insertedValue.getText().isEmpty()) return insertedValue;
            }
            return new TextResult(myName);
        }

        @Override
        public boolean requiresCommittedPSI() {
            return false;
        }

        @Override
        public String getAdvertisingText() {
            return myAdvertisementText;
        }
    }
}
