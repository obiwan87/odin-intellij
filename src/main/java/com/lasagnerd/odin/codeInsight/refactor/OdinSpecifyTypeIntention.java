package com.lasagnerd.odin.codeInsight.refactor;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.intellij.util.IncorrectOperationException;
import com.lasagnerd.odin.codeInsight.imports.OdinImportUtils;
import com.lasagnerd.odin.codeInsight.typeInference.OdinInferenceEngine;
import com.lasagnerd.odin.codeInsight.typeInference.OdinTypeConverter;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinBuiltInType;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinType;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class OdinSpecifyTypeIntention extends PsiElementBaseIntentionAction {
    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {

        OdinVariableInitializationStatement varInit = PsiTreeUtil.getParentOfType(element,
                OdinVariableInitializationStatement.class);

        if (varInit == null)
            return;

        OdinDeclaredIdentifier declaredIdentifier = varInit.getIdentifierList().getDeclaredIdentifierList().getFirst();
        List<OdinExpression> expressionList = varInit.getExpressionsList().getExpressionList();
        if (expressionList.size() > 1)
            return;
        OdinExpression expression = expressionList.getFirst();

        TsOdinType tsOdinType = OdinInferenceEngine.doInferType(expression);
        if (tsOdinType.isUnknown()) {
            showErrorHint(project, editor);
            return;
        }


        String importPath;
        String packageName;

        OdinFile containingFile;
        if (tsOdinType instanceof TsOdinBuiltInType) {
            containingFile = null;
            importPath = null;
            packageName = "";
            tsOdinType = OdinTypeConverter.convertToTyped(tsOdinType);
        } else {
            if (tsOdinType.getDeclaration() == null) {
                showErrorHint(project, editor);
                return;
            }
            containingFile = (OdinFile) tsOdinType.getDeclaration().getContainingFile();
            if (containingFile != null) {
                VirtualFile sourceElementFile = expression.getContainingFile().getVirtualFile();
                VirtualFile targetElementFile = tsOdinType.getDeclaration().getContainingFile().getVirtualFile();
                if (sourceElementFile != null && targetElementFile != null) {
                    importPath = OdinImportUtils.computeImportPath(expression.getProject(),
                            sourceElementFile,
                            targetElementFile);
                    packageName = getPackageName(importPath);
                } else {
                    return;
                }
            } else {
                importPath = null;
                packageName = "";
            }
        }

        TsOdinType finalTsOdinType = tsOdinType;
        WriteCommandAction.runWriteCommandAction(project, () ->
        {
            // Perform the modification
            if (importPath != null) {
                OdinImportUtils.insertImport(project, importPath, containingFile.getFileScope());
            }
            String type;
            if (!packageName.isBlank()) {
                type = packageName + "." + finalTsOdinType.getName();
            } else {
                type = finalTsOdinType.getName();
            }
            OdinVariableInitializationStatement typedVarInit = OdinPsiElementFactory.getInstance(project)
                    .createVariableInitializationStatement(
                            declaredIdentifier.getName(),
                            type,
                            expression
                    );

            varInit.replace(typedVarInit);
        });


    }

    private static void showErrorHint(@NotNull Project project, Editor editor) {
        CommonRefactoringUtil.showErrorHint(project, editor, "Cannot infer type of expression", "Cannot Perform Refactoring", null);
    }

    private static @NotNull String getPackageName(String importPath) {
        String packageName;
        if (importPath != null) {
            if (!importPath.isBlank()) {
                packageName = OdinImportUtils.getPackageNameFromImportPath(importPath);
                if (packageName == null || packageName.isBlank()) {
                    packageName = "";
                }
            } else {
                packageName = "";
            }
        } else {
            packageName = "";
        }
        return packageName;
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        OdinVariableInitializationStatement varInit = PsiTreeUtil.getParentOfType(element, OdinVariableInitializationStatement.class);
        if (varInit == null)
            return false;

        if (PsiUtilCore.getElementType(element) == OdinTypes.COLON) {
            PsiElement nextElement = element.getContainingFile().findElementAt(element.getTextOffset() - 1);
            if (nextElement != null) {
                return isAvailable(project, editor, nextElement);
            }
        }

        if (element instanceof PsiWhiteSpace) {
            PsiElement nextElement = element.getContainingFile().findElementAt(element.getTextOffset() - 1);
            if (nextElement != null) {
                return isAvailable(project, editor, nextElement);
            }
        }

        return PsiUtilCore.getElementType(element) == OdinTypes.IDENTIFIER_TOKEN && varInit.getIdentifierList()
                .getDeclaredIdentifierList()
                .getFirst().getIdentifierToken() == element;
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "Specify type explicitly";
    }

    @Override
    public @NotNull @IntentionName String getText() {
        return getFamilyName();
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }


}
