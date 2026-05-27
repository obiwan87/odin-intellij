package com.lasagnerd.odin.codeInsight.inlayHints;

import com.intellij.codeInsight.hints.declarative.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.lasagnerd.odin.codeInsight.evaluation.OdinExpressionEvaluator;
import com.lasagnerd.odin.lang.psi.OdinCondition;
import com.lasagnerd.odin.lang.psi.OdinWhenBlock;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class OdinWhenStatementInlayHintsProvider implements InlayHintsProvider {
    @Override
    public @Nullable InlayHintsCollector createCollector(@NonNull PsiFile file, @NonNull Editor editor) {
        return new MyCollector();
    }

    @Override
    public boolean isDumbAware() {
        return true;
    }

    private static class MyCollector implements SharedBypassCollector {
        @Override
        public void collectFromElement(@NonNull PsiElement element, @NonNull InlayTreeSink sink) {
            if (!(element instanceof OdinCondition condition)) return;
            if (!(condition.getParent() instanceof OdinWhenBlock)) return;

            var value = OdinExpressionEvaluator.evaluate(condition.getExpression());
            Boolean boolValue = value.asBool();
            String valString = boolValue != null ? boolValue.toString() : "?";

            InlineInlayPosition pos = new InlineInlayPosition(element.getTextRange().getEndOffset(), true, 1);
            sink.addPresentation(pos, null, null, HintFormat.Companion.getDefault(), builder -> {
                builder.text("= " + valString, null);
                return null;
            });
        }
    }
}
