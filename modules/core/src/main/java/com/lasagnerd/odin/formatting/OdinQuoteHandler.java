package com.lasagnerd.odin.formatting;

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler;
import com.lasagnerd.odin.lang.psi.OdinTypes;

public class OdinQuoteHandler extends SimpleTokenSetQuoteHandler {
    public OdinQuoteHandler() {
        super(OdinTypes.SQ_STRING_LITERAL, OdinTypes.DQ_STRING_LITERAL);
    }
}
