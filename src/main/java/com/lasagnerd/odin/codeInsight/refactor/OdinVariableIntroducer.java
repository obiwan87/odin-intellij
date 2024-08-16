package com.lasagnerd.odin.codeInsight.refactor;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.introduce.inplace.AbstractInplaceIntroducer;
import com.lasagnerd.odin.lang.OdinFileType;
import com.lasagnerd.odin.lang.psi.OdinDeclaredIdentifier;
import com.lasagnerd.odin.lang.psi.OdinExpression;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

class OdinVariableIntroducer extends AbstractInplaceIntroducer<OdinDeclaredIdentifier, OdinExpression> {

    private final OdinDeclaredIdentifier declaredIdentifier;
    private final OdinDeclaredIdentifier templateIdentifier;
    private final boolean replaceAllOccurrences;
    private final String[] nameSuggestions;

    public OdinVariableIntroducer(@NotNull Project project,
                                  @NotNull Editor editor,
                                  OdinDeclaredIdentifier declaredIdentifier,
                                  OdinExpression[] occurrences,
                                  OdinDeclaredIdentifier templateIdentifier,
                                  boolean replaceAllOccurrences,
                                  String[] nameSuggestions) {
        super(project, editor, null, declaredIdentifier, occurrences, null, OdinFileType.INSTANCE);
        this.declaredIdentifier = declaredIdentifier;
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

}
