package com.lasagnerd.odin.codeInsight.inlayHints;

import com.intellij.codeInsight.hints.declarative.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.codeInsight.OdinContext;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinBuiltInTypes;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinType;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinTypeReference;
import com.lasagnerd.odin.lang.psi.*;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

// Shows an inlay hint for declared identifiers whose type has not been explicitly written out (inferred type).
// e.g. x :[int] = fibonacci(4) (InlayHint in brackets)
public class OdinInferredTypeNameInlayHintsProvider implements InlayHintsProvider {
    @Override
    public @Nullable InlayHintsCollector createCollector(@NonNull PsiFile psiFile, @NonNull Editor editor) {
        return new MyCollector();
    }

    @Override
    public boolean isDumbAware() {
        return true;
    }

    static class MyCollector implements SharedBypassCollector {

        @Override
        public void collectFromElement(@NonNull PsiElement psiElement, @NonNull InlayTreeSink sink) {
            if (!(psiElement instanceof OdinDeclaredIdentifier declaredIdentifier)) return;

            OdinDeclaration declaration = PsiTreeUtil.getParentOfType(psiElement, OdinDeclaration.class);
            if (declaration instanceof OdinInitVariableDeclaration variableDeclaration) {
                if (variableDeclaration.getType() != null) return;

                TsOdinType type = declaredIdentifier.getType(new OdinContext());
                if (type == null || type.isUnknown() || type == TsOdinBuiltInTypes.VOID) return;

                produceInlayHint(sink, type, variableDeclaration.getColonOpening());
            }

            if (declaration instanceof OdinConstantInitDeclaration constantInitDeclaration) {
                TsOdinType declaredIdentifierType = declaredIdentifier.getType(new OdinContext());
                if (!(declaredIdentifierType.baseType(true) instanceof TsOdinTypeReference)) {
                    produceInlayHint(sink, declaredIdentifierType, constantInitDeclaration.getColonOpening());
                }
            }
        }

        private void produceInlayHint(@NonNull InlayTreeSink sink, TsOdinType declaredIdentifierType, OdinColonOpening colonOpening) {
            String label = declaredIdentifierType.getLabel();
            if (label == null || label.isBlank() || label.equals("<undefined>")) return;

            int offset = colonOpening.getColon().getTextRange().getEndOffset();
            InlineInlayPosition pos = new InlineInlayPosition(offset, true, 0);
            sink.addPresentation(pos, null, null, HintFormat.Companion.getDefault(), builder -> {
                builder.text(label, null);
                return null;
            });
        }
    }
}
