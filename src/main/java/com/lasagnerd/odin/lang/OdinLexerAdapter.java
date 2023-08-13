package com.lasagnerd.odin.lang;

import com.intellij.lexer.FlexAdapter;

public class OdinLexerAdapter extends FlexAdapter {
    public OdinLexerAdapter() {
        super(new OdinLexer(null));
    }
}
