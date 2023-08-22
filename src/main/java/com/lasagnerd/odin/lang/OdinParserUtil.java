package com.lasagnerd.odin.lang;

import com.intellij.lang.PsiBuilder;
import com.intellij.psi.TokenType;
import com.lasagnerd.odin.lang.psi.OdinTypes;

import java.util.Objects;

import static com.intellij.psi.TokenType.WHITE_SPACE;

public class OdinParserUtil {
    public static boolean parseEndOfStatement(PsiBuilder builder, int level) {

        // Look ahead for the end of the statement. The end of a statement can be
        // a semicolon or a newline or a closing brace or the end of the file.

        // ignore whitespace
        while (builder.getTokenType() == WHITE_SPACE) {
            if(Objects.equals(builder.getTokenText(), "\n"))
                return true;
            builder.advanceLexer();
        }

        var nextToken = builder.getTokenType();
        if (nextToken == OdinTypes.SEMICOLON) {
            builder.advanceLexer();
            return true;
        }

        if (nextToken == TokenType.NEW_LINE_INDENT) {
            builder.advanceLexer();
            return true;
        }

        nextToken = builder.lookAhead(1);
        if (nextToken == OdinTypes.RBRACE) {
            builder.advanceLexer();
            return true;
        }

        return false;
    }
}
