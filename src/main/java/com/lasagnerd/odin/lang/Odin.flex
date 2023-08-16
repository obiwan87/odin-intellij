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
ComplexIntegerDecLiteral = {IntegerDecLiteral}i

IntegerHexLiteral = 0x[0-9a-fA-F][0-9a-fA-F_]*
IntegerBinLiteral = 0b[01][01_]*


FloatDecLiteral = [0-9][0-9_]*\.[0-9][0-9_]*([eE][+-]?[0-9][0-9_]*)?
ComplexFloatLiteral = {FloatDecLiteral}i


%state DQ_STRING_STATE
%%

<YYINITIAL> {

        "package"    { return PACKAGE; }
        "import"     { return IMPORT; }
        "proc"       { return PROC; }
        "return"     { return RETURN; }
        "defer"      { return DEFER; }
        "struct"     { return STRUCT; }
        "for"     { return FOR; }
        "in"      { return IN; }
        "do"      { return DO; }
        "if"      { return IF; }
        "else"    { return ELSE; }
        "switch"  { return SWITCH; }
        "case"    { return CASE; }
        "fallthrough" { return FALLTHROUGH; }
        "true"    { return TRUE; }
        "false"   { return FALSE; }
        "when"    { return WHEN; }
        "break"   { return BREAK; }
        "continue" { return CONTINUE; }


        {LineComment} { return LINE_COMMENT; }
        {BlockComment} { return BLOCK_COMMENT; }
        {Identifier} { return IDENTIFIER; }
        {WhiteSpace} { return WHITE_SPACE; }

        \"           { yybegin(DQ_STRING_STATE); string.setLength(0); }

        {IntegerOctLiteral} { return INTEGER_OCT_LITERAL; }
        {IntegerDecLiteral} { return INTEGER_DEC_LITERAL; }
        {IntegerHexLiteral} { return INTEGER_HEX_LITERAL; }
        {IntegerBinLiteral} { return INTEGER_BIN_LITERAL; }
        {FloatDecLiteral} { return FLOAT_DEC_LITERAL; }
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
        "&"         { return AND_BITWISE; }
        "|"         { return OR_BITWISE; }
        "~"         { return XOR_BITWISE; }
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

[^] { return BAD_CHARACTER; }