package com.lasagnerd.odin.lang;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.colors.impl.EditorColorsSchemeImpl;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import com.intellij.ui.Colors;
import com.intellij.ui.JBColor;
import com.lasagnerd.odin.lang.psi.OdinTypes;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public class OdinSyntaxHighlighter extends SyntaxHighlighterBase {
    public static final TextAttributesKey BUILTIN_FUNCTION = createTextAttributesKey("ODIN_BUILTIN_FUNCTION", DefaultLanguageHighlighterColors.KEYWORD);;

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
            OdinTypes.BIT_SET,
            OdinTypes.DYNAMIC,
            OdinTypes.NOT_IN,
            OdinTypes.CAST,
            OdinTypes.AUTO_CAST,
            OdinTypes.TRANSMUTE,
            OdinTypes.BREAK,
            OdinTypes.CONTINUE,
            OdinTypes.WHEN,
            OdinTypes.DEFER,
            OdinTypes.TRUE,
            OdinTypes.FALSE,
            OdinTypes.SWITCH,
            OdinTypes.CASE,
            OdinTypes.FALLTHROUGH,
            OdinTypes.IF_TOKEN,
            OdinTypes.ELSE_TOKEN,
            OdinTypes.DO,
            OdinTypes.STRUCT,
            OdinTypes.IN,
            OdinTypes.FOR,
            OdinTypes.RETURN,
            OdinTypes.PROC,
            OdinTypes.PACKAGE,
            OdinTypes.IMPORT,
            OdinTypes.MAP,
            OdinTypes.USING,
            OdinTypes.NIL,
            OdinTypes.MATRIX,
            OdinTypes.ENUM,
            OdinTypes.UNION,
            OdinTypes.FOREIGN,
            OdinTypes.WHERE,
            OdinTypes.DISTINCT,
            OdinTypes.OR_ELSE,
            OdinTypes.OR_RETURN,
            OdinTypes.OR_BREAK,
            OdinTypes.OR_CONTINUE
    );

    private static final List<IElementType> numericLiteral = List.of(
            OdinTypes.INTEGER_DEC_LITERAL,
            OdinTypes.INTEGER_HEX_LITERAL,
            OdinTypes.INTEGER_OCT_LITERAL,
            OdinTypes.INTEGER_BIN_LITERAL,
            OdinTypes.FLOAT_DEC_LITERAL,
            OdinTypes.COMPLEX_FLOAT_LITERAL,
            OdinTypes.COMPLEX_INTEGER_DEC_LITERAL
    );

    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        if (keywords.contains(tokenType)) {
            return KEYWORD_KEYS;
        }

        if (tokenType.equals(OdinTypes.DQ_STRING_LITERAL) ||
            tokenType.equals(OdinTypes.SQ_STRING_LITERAL) ||
            tokenType.equals(OdinTypes.RAW_STRING_LITERAL)
        ) {
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

        if (tokenType.equals(OdinTypes.MULTILINE_BLOCK_COMMENT)) {
            return new TextAttributesKey[]{DefaultLanguageHighlighterColors.BLOCK_COMMENT};
        }

        if (numericLiteral.contains(tokenType)) {
            return new TextAttributesKey[]{DefaultLanguageHighlighterColors.NUMBER};
        }

        if (tokenType.equals(OdinTypes.COMMA)) {
            return new TextAttributesKey[]{DefaultLanguageHighlighterColors.COMMA};
        }

        if (tokenType.equals(OdinTypes.SEMICOLON)) {
            return new TextAttributesKey[]{DefaultLanguageHighlighterColors.SEMICOLON};
        }

        if (tokenType.equals(OdinTypes.DOT)) {
            return new TextAttributesKey[]{DefaultLanguageHighlighterColors.DOT};
        }

        if (tokenType.equals(OdinTypes.LPAREN) || tokenType.equals(OdinTypes.RPAREN)) {
            return new TextAttributesKey[]{DefaultLanguageHighlighterColors.PARENTHESES};
        }

        if (tokenType.equals(OdinTypes.LBRACE) || tokenType.equals(OdinTypes.RBRACE)) {
            return new TextAttributesKey[]{DefaultLanguageHighlighterColors.BRACES};
        }

        if(tokenType.equals(OdinTypes.AT)) {
            return new TextAttributesKey[]{DefaultLanguageHighlighterColors.METADATA};
        }


        return EMPTY_KEYS;
    }
}
