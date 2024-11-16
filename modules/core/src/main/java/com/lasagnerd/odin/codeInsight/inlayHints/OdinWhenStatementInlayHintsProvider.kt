package com.lasagnerd.odin.codeInsight.inlayHints

import com.intellij.codeInsight.hints.declarative.*
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.lasagnerd.odin.codeInsight.evaluation.OdinExpressionEvaluator
import com.lasagnerd.odin.lang.psi.OdinCondition
import com.lasagnerd.odin.lang.psi.OdinWhenBlock

class OdinWhenStatementInlayHintsProvider : InlayHintsProvider {
    override fun createCollector(file: PsiFile, editor: Editor): InlayHintsCollector? {
        return MyCollector()
    }

    private class MyCollector : SharedBypassCollector {
        override fun collectFromElement(element: PsiElement, sink: InlayTreeSink) {
            if (element is OdinCondition) {
                if (element.getParent() is OdinWhenBlock) {
                    val value = OdinExpressionEvaluator.evaluate(element.expression)
                    val valString = value.asBool()?.toString() ?: "?"

                    val pos = InlineInlayPosition(element.getTextRange().endOffset, true, 1)
                    sink.addPresentation(
                        pos, null, null, HintFormat.default
                    ) {
                        text("= $valString")
                    }
                }
            }
        }
    }

    override fun isDumbAware(): Boolean {
        return true
    }
}
