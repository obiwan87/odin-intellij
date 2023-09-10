package com.lasagnerd.odin.lang;

import com.intellij.lexer.FlexLexer;
import com.intellij.lexer.LexerBase;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.lasagnerd.odin.lang.psi.OdinTypes;
import org.jetbrains.annotations.NotNull;

public class OdinLexerAdapter extends LexerBase {

    private static final Logger LOG = Logger.getInstance(OdinLexerAdapter.class);

    private final FlexLexer flexLexer;

    private IElementType tokenType;
    private CharSequence text;

    private int tokenStart;
    private int tokenEnd;

    private int bufferEnd;
    private int state;

    private boolean failed;

    public OdinLexerAdapter() {
        super();
        this.flexLexer = new OdinLexer();
    }

    @Override
    public void start(@NotNull final CharSequence buffer, int startOffset, int endOffset, final int initialState) {
        text = buffer;
        tokenStart = tokenEnd = startOffset;
        bufferEnd = endOffset;
        flexLexer.reset(text, startOffset, endOffset, initialState);
        tokenType = null;
    }

    @Override
    public int getState() {
        locateToken();
        return state;
    }

    @Override
    public IElementType getTokenType() {
        locateToken();
        return tokenType;
    }

    @Override
    public int getTokenStart() {
        locateToken();
        return tokenStart;
    }

    @Override
    public int getTokenEnd() {
        locateToken();
        return tokenEnd;
    }

    @Override
    public void advance() {
        locateToken();
        tokenType = null;
    }

    @NotNull
    @Override
    public CharSequence getBufferSequence() {
        return text;
    }

    @Override
    public int getBufferEnd() {
        return bufferEnd;
    }

    protected void locateToken() {
        if (tokenType != null) return;

        tokenStart = tokenEnd;
        if (failed) return;

        try {
            state = flexLexer.yystate();
            tokenType = flexLexer.advance();
            if(tokenType == OdinTypes.BLOCK_COMMENT_START) {
                boolean multiLine = false;
                do {
                    tokenType = flexLexer.advance();
                    if(tokenType == OdinTypes.NEW_LINE) {
                        multiLine = true;
                    }
                } while(tokenType != OdinTypes.BLOCK_COMMENT_END && tokenType != null);
                tokenType = multiLine? OdinTypes.MULTILINE_BLOCK_COMMENT : OdinTypes.BLOCK_COMMENT;
            }

            tokenEnd = flexLexer.getTokenEnd();
        }
        catch (ProcessCanceledException e) {
            throw e;
        }
        catch (Throwable e) {
            failed = true;
            tokenType = TokenType.BAD_CHARACTER;
            tokenEnd = bufferEnd;
            LOG.warn(flexLexer.getClass().getName(), e);
        }
    }

    @Override
    public String toString() {
        return "FlexAdapter for " + flexLexer.getClass().getName();
    }
}
