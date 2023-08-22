package com.lasagnerd.odin.lang;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static com.lasagnerd.odin.lang.psi.OdinTypes.*;

%%

%{
 StringBuffer string = new StringBuffer();

  public OdinLexer() {
    this((java.io.Reader)null);
  }
%}

%public
%class OdinLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

LineTerminator=\r|\n|\r\n
WhiteSpace={LineTerminator}{LineTerminator}* | [ \t\f]+{LineTerminator}?
Identifier = [a-zA-Z_][a-zA-Z0-9_]*

LineComment = \/\/[^\r\n]*
BlockComment = \/\*([^*]|\*[^/])*\*\/

// Literals

IntegerOctLiteral = 0o[0-7][0-7_]*

IntegerDecLiteral = [0-9][0-9_]*
ComplexIntegerDecLiteral = {IntegerDecLiteral}[ijk]

IntegerHexLiteral = 0x[0-9a-fA-F][0-9a-fA-F_]*
IntegerBinLiteral = 0b[01][01_]*

FloatLiteral = [0-9][0-9_]*\.[0-9][0-9_]*{ExponentPart}? | {IntegerDecLiteral}{ExponentPart}
ExponentPart = [eE][+-]?[0-9][0-9_]*
ComplexFloatLiteral = {FloatLiteral}[ijk]


%state DQ_STRING_STATE
%state SQ_STRING_STATE
%state RAW_STRING_STATE
%%

<YYINITIAL> {

        "package"     { return PACKAGE; }
        "import"      { return IMPORT; }
        "proc"        { return PROC; }
        "return"      { return RETURN; }
        "defer"       { return DEFER; }
        "struct"      { return STRUCT; }
        "for"         { return FOR; }
        "in"          { return IN; }
        "do"          { return DO; }
        "if"          { return IF; }
        "else"        { return ELSE; }
        "switch"      { return SWITCH; }
        "case"        { return CASE; }
        "fallthrough" { return FALLTHROUGH; }
        "true"        { return TRUE; }
        "false"       { return FALSE; }
        "when"        { return WHEN; }
        "break"       { return BREAK; }
        "continue"    { return CONTINUE; }
        "nil"         { return NIL; }
        "or_else"     { return OR_ELSE; }
        "or_return"   { return OR_RETURN; }
        "foreign"     { return FOREIGN; }
        "cast"        { return CAST; }
        "transmute"   { return TRANSMUTE; }
        "auto_cast"   { return AUTO_CAST; }
        "not_in"      { return NOT_IN; }
        "dynamic"     { return DYNAMIC; }
        "bit_set"     { return BIT_SET; }
        "map"         { return MAP; }
        "using"       { return USING; }
        "enum"        { return ENUM; }
        "union"       { return UNION; }
        "distinct"    { return DISTINCT; }
        "matrix"    { return MATRIX; }
        "where"    { return WHERE; }


        {LineComment} { return LINE_COMMENT; }
        {BlockComment} { return BLOCK_COMMENT; }
        {Identifier} { return IDENTIFIER; }
        {WhiteSpace} { return WHITE_SPACE; }

        \"           { yybegin(DQ_STRING_STATE); string.setLength(0); }
        \'           { yybegin(SQ_STRING_STATE); }
        \`           { yybegin(RAW_STRING_STATE); }

        {IntegerOctLiteral} { return INTEGER_OCT_LITERAL; }
        {IntegerDecLiteral} { return INTEGER_DEC_LITERAL; }
        {IntegerHexLiteral} { return INTEGER_HEX_LITERAL; }
        {IntegerBinLiteral} { return INTEGER_BIN_LITERAL; }
        {FloatLiteral} { return FLOAT_DEC_LITERAL; }

        {ComplexFloatLiteral} { return COMPLEX_FLOAT_LITERAL; }
        {ComplexIntegerDecLiteral} { return COMPLEX_INTEGER_DEC_LITERAL; }

        ":"         { return COLON; }
        "="         { return EQ; }
        \{          { return LBRACE; }
        \}          { return RBRACE; }
        \(          { return LPAREN; }
        \)          { return RPAREN; }
        \.          { return DOT; }
        ","         { return COMMA; }
        "->"        { return ARROW; }
        ";"         { return SEMICOLON; }
        "["         { return LBRACKET; }
        "]"         { return RBRACKET; }
        "#"         { return HASH; }
        "?"         { return QUESTION; }
        "^"         { return CARET; }
        "@"         { return AT; }


        // Operators
        "=="        { return EQEQ; }
        "!="        { return NEQ; }
        "<"         { return LT; }
        "<="        { return LTE; }
        ">"         { return GT; }
        ">="        { return GTE; }
        "&&"        { return ANDAND; }
        "||"        { return OROR; }
        "!"         { return NOT; }
        "+"         { return PLUS; }
        "-"         { return MINUS; }
        "*"         { return STAR; }
        "/"         { return DIV; }
        "%%"        { return REMAINDER; }
        "%"         { return MOD; }
        "&"         { return AND; }
        "&~"        { return ANDNOT; }
        "|"         { return PIPE; }
        "~"         { return TILDE; }
        "<<"        { return LSHIFT; }
        ">>"        { return RSHIFT; }

        // Assignment operators
        "+="        { return PLUS_EQ; }
        "-="        { return MINUS_EQ; }
        "*="        { return STAR_EQ; }
        "/="        { return DIV_EQ; }
        "%="        { return MOD_EQ; }
        "%%="       { return REMAINDER_EQ; }
        "&="        { return AND_EQ; }
        "|="        { return OR_EQ; }
        "~="        { return XOR_EQ; }
        "<<="       { return LSHIFT_EQ; }
        ">>="       { return RSHIFT_EQ; }
        "&&="       { return ANDAND_EQ; }
        "||="       { return OROR_EQ; }

        // Range operators
        ".."        { return RANGE; }
        "..<"       { return RANGE_EXCLUSIVE; }
        "..="       { return RANGE_INCLUSIVE; }

        "---"       { return TRIPLE_DASH; }

        "$"         { return DOLLAR; }
}

    <DQ_STRING_STATE> {
      \"                             { yybegin(YYINITIAL); return DQ_STRING_LITERAL; }
      \\n                            { }
      \\t                            { }
      \\r                            { }
      \\v                            { }
      \\e                            { }
      \\a                            { }
      \\b                            { }
      \\f                            { }
      \\[0-7]{2}                     { }
      \\x[0-9a-fA-F]{2}              { }
      \\u[0-9a-fA-F]{4}              { }
      \\U[0-9a-fA-F]{8}              { }
      \\\"                           { }
      \\                             { }
      [^\n\r\"\\]+                   { }
    }

    // Single quote strings
    <SQ_STRING_STATE> {
        \'                           {yybegin(YYINITIAL); return SQ_STRING_LITERAL; }
        [^\n\r\'\\]+                 { }
        \\\'                         { }
        \\                           { }
    }

    // Raw string delimited by `
    <RAW_STRING_STATE> {
        \`                           {yybegin(YYINITIAL); return RAW_STRING_LITERAL; }
        [^`]+                        { }
    }

[^] { return BAD_CHARACTER; }