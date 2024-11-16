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
import com.lasagnerd.odin.codeInsight.imports.OdinImport;
import com.lasagnerd.odin.codeInsight.imports.OdinImportUtils;
import com.lasagnerd.odin.codeInsight.typeInference.OdinInferenceEngine;
import com.lasagnerd.odin.codeInsight.typeInference.OdinTypeConverter;
import com.lasagnerd.odin.codeInsight.typeSystem.*;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.*;

public class OdinSpecifyTypeIntention extends PsiElementBaseIntentionAction {
    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {

        OdinVariableInitializationStatement varInit = PsiTreeUtil.getParentOfType(element,
                OdinVariableInitializationStatement.class);

        if (varInit == null)
            return;

        OdinDeclaredIdentifier declaredIdentifier = varInit.getDeclaredIdentifierList().getFirst();
        OdinRhsExpressions rhsExpressions = varInit.getRhsExpressions();
        if (rhsExpressions == null)
            return;

        List<OdinExpression> expressionList = rhsExpressions.getExpressionList();
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
        if (tsOdinType instanceof TsOdinTuple tsOdinTuple) {
            tsOdinType = tsOdinTuple.getTypes().getFirst();
        }

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
                OdinImportUtils.insertImport(project,
                        null,
                        importPath,
                        containingFile.getFileScope());
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

        return PsiUtilCore.getElementType(element) == OdinTypes.IDENTIFIER_TOKEN && varInit
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

    private static class TypePrinter {
        private final OdinFileScope sourceFileScope;
        private final Map<String, OdinImport> pathToImportMap;
        private final List<OdinImport> importsToAdd = new ArrayList<>();
        private final StringBuilder typePresentation = new StringBuilder();


        private TypePrinter(OdinFileScope sourceFileScope) {
            this.sourceFileScope = sourceFileScope;

            pathToImportMap = new HashMap<>();
            for (OdinImportDeclarationStatement importStatement : sourceFileScope.getImportStatements()) {
                OdinImport importInfo = importStatement.getImportInfo();
                Path importPath = OdinImportUtils.getFirstAbsoluteImportPath(importInfo,
                        sourceFileScope.getContainingFile().getVirtualFile().getPath(),
                        sourceFileScope.getProject());
                if (importPath != null) {
                    pathToImportMap.put(importPath.toAbsolutePath().toString(), importInfo);
                }
            }
        }

        public static void printType(TsOdinType tsOdinType) {
            OdinType type = tsOdinType.getPsiType();
            if (type != null) {

            }
        }

//        public void printType(TsOdinType callingType) {
//            if (callingType instanceof TsOdinArrayType tsOdinArrayType) {
//                typePresentation.append("[");
//                typePresentation.append(tsOdinArrayType.getPsiSizeElement().getText());
//                typePresentation.append("]");
//                printType(tsOdinArrayType.getElementType());
//            }
//
//            if (callingType instanceof TsOdinSliceType tsOdinSliceType) {
//                typePresentation.append("[]");
//                printType(tsOdinSliceType.getElementType());
//            }
//
//            if (callingType instanceof TsOdinMapType tsOdinMapType) {
//                typePresentation.append("map[");
//                printType(tsOdinMapType.getKeyType());
//                typePresentation.append("]");
//                printType(tsOdinMapType.getValueType());
//            }
//
//            if (callingType instanceof TsOdinPointerType tsOdinPointerType) {
//                typePresentation.append("^");
//
//                printType(tsOdinPointerType.getDereferencedType());
//            }
//
//            if (callingType instanceof TsOdinMultiPointerType tsOdinMultiPointerType) {
//                typePresentation.append("[^]");
//                printType(tsOdinMultiPointerType.getDereferencedType());
//            }
//
//            if (callingType instanceof TsOdinProcedureType tsOdinProcedureType) {
//                List<TsOdinType> tsOdinTypes = new ArrayList<>();
//                typePresentation.append("proc(");
//                if (!tsOdinProcedureType.getParameters().isEmpty()) {
//                    TsOdinParameter last = tsOdinProcedureType.getParameters().getLast();
//                    for (TsOdinParameter parameter : tsOdinProcedureType.getParameters()) {
//                        typePresentation.append(parameter.getName());
//                        typePresentation.append(": ");
//                        printType(callingType);
//                        if (last != parameter) {
//                            typePresentation.append(", ");
//                        }
//                    }
//                }
//                List<TsOdinParameter> returnParameters = tsOdinProcedureType.getReturnParameters();
//                if (!returnParameters.isEmpty()) {
//                    typePresentation.append(" -> ");
//
//                    if (tsOdinProcedureType.getReturnTypes().size() > 1) {
//                        typePresentation.append("(");
//                    }
//                    TsOdinParameter last = returnParameters.getLast();
//                    for (TsOdinParameter returnParameter : returnParameters) {
//                        printType(returnParameter.getType());
//                        if(last != returnParameter)
//                            typePresentation.append(", ");
//                    }
//
//                    if (tsOdinProcedureType.getReturnTypes().size() > 1) {
//                        typePresentation.append(")");
//                    }
//                }
//            }
//
//            if (callingType instanceof TsOdinStructType structType) {
//                structType.getP
//            }
//
//            if (callingType != null && !callingType.isUnknown()) {
//                typePresentation.append("unknown");
//            }
//
//
//        }

    }

}
