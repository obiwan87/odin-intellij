package com.lasagnerd.odin.lang;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import com.lasagnerd.odin.lang.psi.OdinElementType;
import com.lasagnerd.odin.lang.psi.OdinTypes;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public class OdinSyntaxHighlighter extends SyntaxHighlighterBase {

    public static final TextAttributesKey BAD_CHARACTER =
            createTextAttributesKey("ODIN_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER);

    public static final TextAttributesKey KEYWORD =
            createTextAttributesKey("ODIN_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);

    public static final TextAttributesKey IDENTIFIER =
            createTextAttributesKey("ODIN_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER);
    private static final TextAttributesKey[] KEYWORD_KEYS = new TextAttributesKey[]{KEYWORD};

    private static final TextAttributesKey[] EMPTY_KEYS = new TextAttributesKey[0];

    @Override
    public @NotNull Lexer getHighlightingLexer() {
        return new OdinLexerAdapter();
    }

    private static final List<IElementType> keywords = List.of(
            OdinTypes.IF,
            OdinTypes.ELSE,
            OdinTypes.DO,
            OdinTypes.STRUCT,
            OdinTypes.IN,
            OdinTypes.FOR,
            OdinTypes.RETURN,
            OdinTypes.PROC,
            OdinTypes.PACKAGE,
            OdinTypes.IMPORT);

//    private static final List<IElementType> escapedSequences = List.of(
//            OdinTypes.ESCAPE_E,
//            OdinTypes.ESCAPE_B,
//            OdinTypes.ESCAPE_A,
//            OdinTypes.ESCAPE_F,
//            OdinTypes.ESCAPE_N,
//            OdinTypes.ESCAPE_R,
//            OdinTypes.ESCAPE_T,
//            OdinTypes.ESCAPE_V,
//            OdinTypes.ESCAPE_BACKSLASH,
//            OdinTypes.ESCAPE_DOUBLE_QUOTE,
//            OdinTypes.ESCAPE_HEX2,
//            OdinTypes.ESCAPE_HEX4,
//            OdinTypes.ESCAPE_HEX8,
//            OdinTypes.ESCAPE_OCT
//            );

    private static final List<IElementType> numericLiteral = List.of(OdinTypes.INTEGER_DEC_LITERAL, OdinTypes.INTEGER_HEX_LITERAL, OdinTypes.INTEGER_OCT_LITERAL);

    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        if (keywords.contains(tokenType)) {
            return KEYWORD_KEYS;
        }

        if (tokenType.equals(OdinTypes.STRING_LITERAL)) {
            return new TextAttributesKey[]{DefaultLanguageHighlighterColors.STRING};
        }

        if (tokenType.equals(OdinTypes.IDENTIFIER)) {
            return new TextAttributesKey[]{IDENTIFIER};
        }

        if (tokenType.equals(OdinTypes.LINE_COMMENT)) {
            return new TextAttributesKey[]{DefaultLanguageHighlighterColors.LINE_COMMENT};
        }

        if (tokenType.equals(OdinTypes.BLOCK_COMMENT)) {
            return new TextAttributesKey[]{DefaultLanguageHighlighterColors.BLOCK_COMMENT};
        }

        if (numericLiteral.contains(tokenType)) {
            return new TextAttributesKey[]{DefaultLanguageHighlighterColors.NUMBER};
        }

//        if(escapedSequences.contains(tokenType)) {
//            return new TextAttributesKey[]{DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE};
//        }

        if(tokenType.equals(OdinTypes.COMMA)) {
            return new TextAttributesKey[]{DefaultLanguageHighlighterColors.COMMA};
        }

        if(tokenType.equals(OdinTypes.SEMICOLON)) {
            return new TextAttributesKey[]{DefaultLanguageHighlighterColors.SEMICOLON};
        }

        if(tokenType.equals(OdinTypes.DOT)) {
            return new TextAttributesKey[]{DefaultLanguageHighlighterColors.DOT};
        }

        if(tokenType.equals(OdinTypes.LPAREN) || tokenType.equals(OdinTypes.RPAREN)) {
            return new TextAttributesKey[]{DefaultLanguageHighlighterColors.PARENTHESES};
        }

        if(tokenType.equals(OdinTypes.LBRACE) || tokenType.equals(OdinTypes.RBRACE)) {
            return new TextAttributesKey[]{DefaultLanguageHighlighterColors.BRACES};
        }

        return EMPTY_KEYS;
    }
}
