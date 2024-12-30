package com.lasagnerd.odin.debugger;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.ExpressionInfo;
import com.jetbrains.cidr.execution.debugger.CidrEvaluator;
import com.jetbrains.cidr.execution.debugger.CidrStackFrame;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinType;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinUnionType;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinUnionVariant;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class OdinDebuggerEvaluator extends CidrEvaluator {
    public OdinDebuggerEvaluator(@NotNull CidrStackFrame frame) {
        super(frame);
    }

    @Override
    public @Nullable ExpressionInfo getExpressionInfoAtOffset(@NotNull Project project, @NotNull Document document, int offset, boolean sideEffectsAllowed) {
        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
        if (!(psiFile instanceof OdinFile))
            return null;

        TextRange textRange = null;
        String expressionText = "";
        String displayText = "";
        PsiElement psiElementAtOffset = psiFile.findElementAt(offset);
        if (psiElementAtOffset != null) {
            OdinDeclaredIdentifier declaredIdentifier = PsiTreeUtil.getParentOfType(psiElementAtOffset, OdinDeclaredIdentifier.class);
            if (declaredIdentifier != null) {
                textRange = declaredIdentifier.getTextRange();
                expressionText = declaredIdentifier.getText();
                displayText = expressionText;
                return new ExpressionInfo(textRange, expressionText, displayText);
            } else if (psiElementAtOffset.getNode().getElementType() == OdinTypes.IDENTIFIER_TOKEN
                    && psiElementAtOffset.getParent() instanceof OdinIdentifier identifier) {
                if (identifier.getParent() instanceof OdinRefExpression refExpression) {
                    textRange = refExpression.getTextRange();
                    displayText = refExpression.getText();

                }
                OdinRefExpression currentRefExpression = OdinInsightUtils.findTopMostRefExpression(psiElementAtOffset);
                List<String> fragments = new ArrayList<>();
                boolean notSupported = false;

                while (currentRefExpression != null) {
                    if (currentRefExpression.getIdentifier() != null) {
                        fragments.add(currentRefExpression.getIdentifier().getText());
                    }
                    if (currentRefExpression.getType() != null) {
                        if (currentRefExpression.getExpression() != null) {
                            TsOdinType inferredType = currentRefExpression.getExpression().getInferredType();
                            if (inferredType.baseType(true) instanceof TsOdinUnionType unionType) {
                                int v = 1;
                                for (TsOdinUnionVariant variant : unionType.getVariants()) {
                                    if (variant.getType().getName().equals(currentRefExpression.getType().getText())) {
                                        fragments.add("v" + v);
                                        break;
                                    }
                                    v++;
                                }
                            }
                        }
                    }
                    if (currentRefExpression.getExpression() instanceof OdinRefExpression nextRefExpression) {
                        currentRefExpression = nextRefExpression;
                    } else if (currentRefExpression.getExpression() == null) {
                        currentRefExpression = null;
                    } else {
                        notSupported = true;
                        break;
                    }
                }
                if (notSupported) {
                    expressionText = displayText;
                } else {
                    expressionText = String.join(".", fragments.reversed());
                }
            }
        }
        if (textRange != null) {
            return new ExpressionInfo(textRange, expressionText, displayText);
        }
        return null;
    }

    @Override
    public void evaluate(@NotNull String expression, @NotNull XEvaluationCallback callback, @Nullable XSourcePosition expressionPosition) {

        super.evaluate(expression, callback, expressionPosition);
    }
}
