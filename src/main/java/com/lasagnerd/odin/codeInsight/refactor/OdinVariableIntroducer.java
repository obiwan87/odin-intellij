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
import com.lasagnerd.odin.codeInsight.typeInference.OdinInferenceEngine;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinType;
import com.lasagnerd.odin.lang.OdinFileType;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class OdinVariableIntroducer extends AbstractInplaceIntroducer<OdinDeclaredIdentifier, OdinExpression> {

    public static final String TYPE_VARIABLE = "TYPE_VARIABLE";

    public static @NotNull OdinVariableIntroducer createVariableIntroducer(@NotNull PsiIntroduceTarget<OdinExpression> target,
                                                                           @NotNull List<UsageInfo> usages,
                                                                           OccurrencesChooser.@NotNull ReplaceChoice replaceChoice,
                                                                           @NotNull Editor editor,
                                                                           @NotNull Project project) {

        String originalText = editor.getDocument().getText();

        // The chosen expression
        OdinExpression targetExpression = target.getPlace();
        Objects.requireNonNull(targetExpression);

        // If there is more than one occurrence find the one that is top-most
        var sortedUsages = usages.stream().sorted(Comparator.comparing(UsageInfo::getNavigationOffset)).toList();
        UsageInfo firstUsage = sortedUsages.getFirst();
        Objects.requireNonNull(firstUsage.getElement());

        // Perform replace of the expression with variable initialization
        SmartPsiElementPointer<OdinVariableInitializationStatement> varInitStatement;
        OdinExpression[] occurrences;

        List<String> nameSuggestions = OdinNameSuggester.getNameSuggestions(targetExpression);
        if (firstUsage.getElement().getParent() instanceof OdinExpressionStatement) {
            // This means we can replace the first occurrence with the variable initialization
            varInitStatement = performReplace(project, (OdinExpression) firstUsage.getElement(), nameSuggestions.getFirst());
            if (replaceChoice.isAll()) {
                occurrences = skipNOccurrences(sortedUsages, 1);
            } else {
                occurrences = new OdinExpression[0];
            }
        } else {
            // When the expression is not directly under an expression statement, we have to create
            // new local variable before the first usage
            varInitStatement = performReplaceWithNewStatement(project, firstUsage, targetExpression, nameSuggestions.getFirst());
            if (!replaceChoice.isAll()) {
                occurrences = new OdinExpression[]{targetExpression};
            } else {
                occurrences = skipNOccurrences(sortedUsages, 0);
            }
        }

        OdinDeclaredIdentifier declaredIdentifier = Objects.requireNonNull(varInitStatement.getElement())
                .getDeclaredIdentifiers().getFirst();

        // AFAIK it is used for the template
        OdinDeclaredIdentifier templateIdentifier = OdinPsiElementFactory.getInstance(project)
                .createDeclaredIdentifier(nameSuggestions.getFirst());


        return new OdinVariableIntroducer(project,
                editor,
                originalText,
                varInitStatement,
                declaredIdentifier,
                target,
                occurrences,
                templateIdentifier,
                true,
                nameSuggestions.toArray(new String[0]));
    }

    private static OdinExpression @NotNull [] skipNOccurrences(List<UsageInfo> sortedUsages, int n) {
        OdinExpression[] occurrences;
        // Occurrences are only needed when expression needs to be replaced with newly introduced variable
        occurrences = sortedUsages.stream()
                .skip(n)
                .map(UsageInfo::getElement)
                .map(e -> (OdinExpression) e)
                .toList()
                .toArray(new OdinExpression[0]);
        return occurrences;
    }

    private static SmartPsiElementPointer<OdinVariableInitializationStatement> performReplaceWithNewStatement(@NotNull Project project, UsageInfo topMostUsage, OdinExpression targetExpression, String name) {
        AtomicReference<SmartPsiElementPointer<OdinVariableInitializationStatement>> newElementRef = new AtomicReference<>();

        WriteCommandAction.runWriteCommandAction(project, () -> {
            OdinStatement odinStatement = PsiTreeUtil.getParentOfType(topMostUsage.getElement(), OdinStatement.class);
            OdinStatementList statementList = PsiTreeUtil.getParentOfType(odinStatement, OdinStatementList.class);

            Objects.requireNonNull(odinStatement);
            Objects.requireNonNull(statementList);

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

        return newElementRef.get();
    }

    private static SmartPsiElementPointer<OdinVariableInitializationStatement> performReplace(@NotNull Project project,
                                                                                              OdinExpression odinExpression,
                                                                                              String name) {
        AtomicReference<SmartPsiElementPointer<OdinVariableInitializationStatement>> newElementRef = new AtomicReference<>();

        WriteCommandAction.runWriteCommandAction(project, () -> {
            OdinVariableInitializationStatement refactoringVar = OdinPsiElementFactory
                    .getInstance(project)
                    .createVariableInitializationStatement(name, "1");

            // Your PSI modification logic here
            refactoringVar.getExpressionsList()
                    .getExpressionList().getFirst()
                    .replace(odinExpression);

            OdinVariableInitializationStatement newElement = (OdinVariableInitializationStatement) odinExpression.replace(refactoringVar);
            newElementRef.set(SmartPointerManager.createPointer(newElement));

            CodeEditUtil.setNodeGeneratedRecursively(newElement.getNode(), true);
        });
        return newElementRef.get();
    }


    private final String originalText;
    private final SmartPsiElementPointer<OdinVariableInitializationStatement> variableInitializationStatement;
    private final OdinDeclaredIdentifier declaredIdentifier;
    private final @NotNull PsiIntroduceTarget<OdinExpression> target;
    private final OdinDeclaredIdentifier templateIdentifier;
    private final boolean replaceAllOccurrences;
    private final String[] nameSuggestions;

    public OdinVariableIntroducer(@NotNull Project project,
                                  @NotNull Editor editor,
                                  String originalText,
                                  SmartPsiElementPointer<OdinVariableInitializationStatement> variableInitializationStatement,
                                  OdinDeclaredIdentifier declaredIdentifier,
                                  @NotNull PsiIntroduceTarget<OdinExpression> target,
                                  OdinExpression[] occurrences,
                                  OdinDeclaredIdentifier templateIdentifier,
                                  boolean replaceAllOccurrences,
                                  String[] nameSuggestions) {
        super(project,
                editor,
                target.getPlace(),
                declaredIdentifier,
                occurrences,
                "Introduce Variable",
                OdinFileType.INSTANCE);

        this.originalText = originalText;
        this.variableInitializationStatement = variableInitializationStatement;
        this.declaredIdentifier = declaredIdentifier;
        this.target = target;
        this.templateIdentifier = templateIdentifier;
        this.replaceAllOccurrences = replaceAllOccurrences;
        this.nameSuggestions = nameSuggestions;
    }


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
        return nameSuggestions;
    }

    @Override
    protected void performIntroduce() {

    }

    @Override
    public boolean isReplaceAllOccurrences() {
        return replaceAllOccurrences;
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

    @Override
    protected String getRefactoringId() {
        return "Introduce variable";
    }

    @Override
    protected void addAdditionalVariables(TemplateBuilderImpl builder) {
        OdinVariableInitializationStatement varInit = variableInitializationStatement.getElement();
        if (varInit != null) {
            int startOffset = varInit.getColonOpening().getTextRange().getStartOffset();
            TextRange rangeWithinElement = TextRange.create(startOffset + 1, startOffset + 1);

            builder.replaceElement(varInit.getContainingFile(), rangeWithinElement, "");
        }
    }

    @SuppressWarnings("unused")
    private void createTypeVariableWithSuggestions(TemplateBuilderImpl builder, OdinVariableInitializationStatement varInit, TextRange rangeWithinElement) {
        OdinExpression targetExpression = this.target.getPlace();
        if (targetExpression != null) {
            TsOdinType tsOdinType = OdinInferenceEngine.doInferType(targetExpression);

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
        // restore document
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
