package com.lasagnerd.odin.codeInsight.inlayHints

import com.intellij.codeInsight.hints.declarative.*
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.lasagnerd.odin.codeInsight.evaluation.OdinExpressionEvaluator
import com.lasagnerd.odin.lang.psi.OdinCondition
import com.lasagnerd.odin.lang.psi.OdinWhenBlock

class OdinWhenStatementInlayHintsProvider : InlayHintsProvider {
    override fun createCollector(psiFile: PsiFile, editor: Editor): InlayHintsCollector? {
        return MyCollector()
    }

    private class MyCollector : SharedBypassCollector {
        override fun collectFromElement(psiElement: PsiElement, inlayTreeSink: InlayTreeSink) {
            if (psiElement is OdinCondition) {
                if (psiElement.getParent() is OdinWhenBlock) {
                    val value = OdinExpressionEvaluator.evaluate(psiElement.expression)
                    val valString = value.asBool()?.toString() ?: "?"

                    val pos = InlineInlayPosition(psiElement.getTextRange().endOffset, true, 1)
                    inlayTreeSink.addPresentation(
                        pos, null, null, HintFormat.default
                    ) {
                        text("= $valString")
                    }
                }
            }
        }
    }
}
