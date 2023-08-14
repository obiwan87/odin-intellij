package com.lasagnerd.odin

import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import com.intellij.refactoring.suggested.startOffset
import com.lasagnerd.odin.lang.psi.OdinTypes

class OdinBraceMatcher : PairedBraceMatcher {
    override fun getPairs(): Array<BracePair> {
        return listOf(
            BracePair(OdinTypes.LBRACE, OdinTypes.RBRACE, true),
            BracePair(OdinTypes.LPAREN, OdinTypes.RPAREN, true),
            BracePair(OdinTypes.LBRACKET, OdinTypes.LBRACKET, true)
        ).toTypedArray();
    }

    override fun isPairedBracesAllowedBeforeType(p0: IElementType, p1: IElementType?): Boolean {
        return true
    }

    override fun getCodeConstructStart(p0: PsiFile?, offset: Int): Int {
        val elementAtOffset = p0?.findElementAt(offset)
        return elementAtOffset?.parent?.startOffset ?: offset
    }

}

