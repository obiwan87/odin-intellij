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
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinPointerType;
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

                    OdinRefExpression currentRefExpression = refExpression;
                    List<ExpressionFragment> fragments = new ArrayList<>();
                    boolean notSupported = false;
                    while (currentRefExpression != null) {
                        TsOdinType currentType = currentRefExpression.getInferredType();
                        if (currentRefExpression.getIdentifier() != null) {
                            fragments.add(new ExpressionFragment(currentRefExpression.getIdentifier().getText(), currentType));
                        }
                        if (currentRefExpression.getType() != null) {
                            if (currentRefExpression.getExpression() != null) {
                                TsOdinType parentType = currentRefExpression.getExpression().getInferredType();
                                if (parentType.baseType(true) instanceof TsOdinUnionType unionType) {
                                    int v = 1;
                                    for (TsOdinUnionVariant variant : unionType.getVariants()) {
                                        if (variant.getType().getName().equals(currentRefExpression.getType().getText())) {
                                            String expression = "v" + v;
                                            fragments.add(new ExpressionFragment(expression, variant.getType()));
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
                        StringBuilder expressionSb = new StringBuilder();
                        List<ExpressionFragment> reversed = fragments.reversed();
                        for (int i = 0; i < reversed.size(); i++) {
                            ExpressionFragment expressionFragment = reversed.get(i);
                            expressionSb.append(expressionFragment.expression());
                            if (i < reversed.size() - 1) {
                                if (expressionFragment.isPointer()) {
                                    expressionSb.append("->");
                                } else {
                                    expressionSb.append(".");
                                }
                            }
                        }

                        expressionText = expressionSb.toString();
                    }
                }
            }
        }
        if (textRange != null) {
            return new ExpressionInfo(textRange, expressionText, displayText);
        }
        return null;
    }

    record ExpressionFragment(String expression, TsOdinType baseType) {
        public boolean isPointer() {
            return baseType.baseType(true) instanceof TsOdinPointerType;
        }
    }

    @Override
    public void evaluate(@NotNull String expression, @NotNull XEvaluationCallback callback, @Nullable XSourcePosition expressionPosition) {

        super.evaluate(expression, callback, expressionPosition);
    }
}
