package com.lasagnerd.odin.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.LiteralTextEscaper;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.lasagnerd.odin.lang.psi.OdinStringLiteral;
import org.jetbrains.annotations.NotNull;

public abstract class OdinStringLiteralMixin extends OdinBasicLiteralImpl implements OdinStringLiteral {
    public OdinStringLiteralMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public boolean isValidHost() {
        return true;
    }

    @Override
    public PsiLanguageInjectionHost updateText(@NotNull String text) {
        return this;
    }

    @Override
    public @NotNull LiteralTextEscaper<? extends PsiLanguageInjectionHost> createLiteralTextEscaper() {
        return new OdinStringLiteralEscaper(this);
    }

    private static class OdinStringLiteralEscaper extends LiteralTextEscaper<OdinStringLiteral> {

        protected OdinStringLiteralEscaper(@NotNull OdinStringLiteral host) {
            super(host);
        }

        @Override
        public boolean decode(@NotNull TextRange rangeInsideHost, @NotNull StringBuilder outChars) {
            String substring = myHost.getText().substring(rangeInsideHost.getStartOffset(), rangeInsideHost.getEndOffset());
            outChars.append(substring);
            return true;
        }

        @Override
        public int getOffsetInHost(int offsetInDecoded, @NotNull TextRange rangeInsideHost) {
            return offsetInDecoded + 1;
        }

        @Override
        public boolean isOneLine() {
            return !myHost.getText().contains("\n");
        }
    }
}
