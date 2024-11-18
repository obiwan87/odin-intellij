package com.lasagnerd.odin.lang;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.lasagnerd.odin.colorSettings.OdinSyntaxTextAttributes;
import com.lasagnerd.odin.lang.psi.OdinTypes;
import org.jetbrains.annotations.NotNull;

public class OdinSyntaxHighlighter extends SyntaxHighlighterBase {


    public static final TextAttributesKey[] KEYWORD_KEYS = new TextAttributesKey[]{OdinSyntaxTextAttributes.ODIN_KEYWORD};

    public static final TextAttributesKey[] EMPTY_KEYS = new TextAttributesKey[0];
    public static final TextAttributesKey[] STRINGS = {OdinSyntaxTextAttributes.ODIN_TEXT};
    public static final TextAttributesKey[] IDENTIFIERS = {OdinSyntaxTextAttributes.ODIN_IDENTIFIER};
    public static final TextAttributesKey[] BUILD_FLAG_IDENTIFIER = {OdinSyntaxTextAttributes.ODIN_BUILD_FLAG_IDENTIFIER};
    public static final TextAttributesKey[] LINE_COMMENT = {OdinSyntaxTextAttributes.ODIN_LINE_COMMENT};
    public static final TextAttributesKey[] BLOCK_COMMENT = {OdinSyntaxTextAttributes.ODIN_BLOCK_COMMENT};
    public static final TextAttributesKey[] NUMBER = {OdinSyntaxTextAttributes.ODIN_NUMBER};
    public static final TextAttributesKey[] COMMA = {OdinSyntaxTextAttributes.ODIN_COMMA};
    public static final TextAttributesKey[] SEMICOLON = {OdinSyntaxTextAttributes.ODIN_SEMICOLON};
    public static final TextAttributesKey[] DOT = {OdinSyntaxTextAttributes.ODIN_DOT};
    public static final TextAttributesKey[] PARENTHESES = {OdinSyntaxTextAttributes.ODIN_PARENTHESES};
    public static final TextAttributesKey[] BRACES = {OdinSyntaxTextAttributes.ODIN_BRACES};
    public static final TextAttributesKey[] ATTRIBUTE_PREFIX = {OdinSyntaxTextAttributes.ODIN_AT};
    public static final TextAttributesKey[] COLON = {OdinSyntaxTextAttributes.ODIN_COLON};
    public static final TextAttributesKey[] OPERATOR_KEYS = {OdinSyntaxTextAttributes.ODIN_OPERATOR};


    @Override
    public @NotNull Lexer getHighlightingLexer() {
        return new OdinLexerAdapter();
    }

    private static final TokenSet OPERATORS = TokenSet.create(
            OdinTypes.DIV,
            OdinTypes.MOD,
            OdinTypes.REMAINDER,
            OdinTypes.PLUS,
            OdinTypes.MINUS,
            OdinTypes.AND,
            OdinTypes.PIPE,
            OdinTypes.TILDE,
            OdinTypes.ANDNOT,
            OdinTypes.LSHIFT,
            OdinTypes.RSHIFT,
            OdinTypes.OR_ELSE,
            OdinTypes.RANGE_INCLUSIVE,
            OdinTypes.RANGE_EXCLUSIVE,
            OdinTypes.IN,
            OdinTypes.NOT_IN,
            OdinTypes.LT,
            OdinTypes.GT,
            OdinTypes.LTE,
            OdinTypes.GTE,
            OdinTypes.EQEQ,
            OdinTypes.NEQ,
            OdinTypes.OROR,
            OdinTypes.ANDAND,
            OdinTypes.NOT,
            OdinTypes.RANGE
    );
    private static final TokenSet KEYWORDS = TokenSet.create(
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
            OdinTypes.SWITCH,
            OdinTypes.CASE,
            OdinTypes.FALLTHROUGH,
            OdinTypes.IF_TOKEN,
            OdinTypes.ELSE_TOKEN,
            OdinTypes.DO,
            OdinTypes.STRUCT,
            OdinTypes.BIT_FIELD,
            OdinTypes.IN,
            OdinTypes.FOR,
            OdinTypes.RETURN,
            OdinTypes.PROC,
            OdinTypes.PACKAGE,
            OdinTypes.IMPORT,
            OdinTypes.MAP,
            OdinTypes.USING,
            OdinTypes.MATRIX,
            OdinTypes.ENUM,
            OdinTypes.UNION,
            OdinTypes.FOREIGN,
            OdinTypes.WHERE,
            OdinTypes.DISTINCT,
            OdinTypes.OR_ELSE,
            OdinTypes.OR_RETURN,
            OdinTypes.OR_BREAK,
            OdinTypes.OR_CONTINUE,
            OdinTypes.BUILD_FLAG_PREFIX_TOKEN
    );

    private static final TokenSet NUMERIC_LITERALS = TokenSet.create(
            OdinTypes.INTEGER_DEC_LITERAL,
            OdinTypes.INTEGER_HEX_LITERAL,
            OdinTypes.INTEGER_OCT_LITERAL,
            OdinTypes.INTEGER_BIN_LITERAL,
            OdinTypes.FLOAT_DEC_LITERAL,
            OdinTypes.COMPLEX_FLOAT_LITERAL,
            OdinTypes.COMPLEX_INTEGER_DEC_LITERAL,
            OdinTypes.QUAT_FLOAT_LITERAL,
            OdinTypes.QUAT_INTEGER_DEC_LITERAL
    );


    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {

        if (KEYWORDS.contains(tokenType)) {
            return KEYWORD_KEYS;
        }

        if (OPERATORS.contains(tokenType)) {
            return OPERATOR_KEYS;
        }

        if (tokenType.equals(OdinTypes.DQ_STRING_LITERAL) ||
                tokenType.equals(OdinTypes.SQ_STRING_LITERAL) ||
                tokenType.equals(OdinTypes.RAW_STRING_LITERAL)
        ) {
            return STRINGS;
        }

        if (tokenType.equals(OdinTypes.IDENTIFIER_TOKEN)) {
            return IDENTIFIERS;
        }

        if (tokenType.equals(OdinTypes.BUILD_FLAG_IDENTIFIER_TOKEN)) {
            return BUILD_FLAG_IDENTIFIER;
        }

        if (tokenType.equals(OdinTypes.LINE_COMMENT)) {
            return LINE_COMMENT;
        }

        if (tokenType.equals(OdinTypes.BLOCK_COMMENT)) {
            return BLOCK_COMMENT;
        }

        if (tokenType.equals(OdinTypes.MULTILINE_BLOCK_COMMENT)) {
            return BLOCK_COMMENT;
        }

        if (NUMERIC_LITERALS.contains(tokenType)) {
            return NUMBER;
        }

        if (tokenType.equals(OdinTypes.COMMA)) {
            return COMMA;
        }

        if (tokenType.equals(OdinTypes.SEMICOLON)) {
            return SEMICOLON;
        }

        if (tokenType.equals(OdinTypes.COLON)) {
            return COLON;
        }

        if (tokenType.equals(OdinTypes.DOT)) {
            return DOT;
        }

        if (tokenType.equals(OdinTypes.LPAREN) || tokenType.equals(OdinTypes.RPAREN)) {
            return PARENTHESES;
        }

        if (tokenType.equals(OdinTypes.LBRACE) || tokenType.equals(OdinTypes.RBRACE)) {
            return BRACES;
        }

        if (tokenType.equals(OdinTypes.AT)) {
            return ATTRIBUTE_PREFIX;
        }

        return EMPTY_KEYS;
    }
}
