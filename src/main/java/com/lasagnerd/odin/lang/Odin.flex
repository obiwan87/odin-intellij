package com.lasagnerd.odin.lang;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.lasagnerd.odin.lang.psi.OdinTokenType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static com.lasagnerd.odin.lang.psi.OdinTypes.*;

%%

%{
  int commentNestingDepth = 0;
  int previousState = YYINITIAL;
  boolean newLineSeen = false;

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

NewLine=(\r|\n|\r\n)+
WhiteSpace=[ \t\f]+
Identifier = ([:letter:]|_)([:letter:]|[0-9_])*

LineComment = \/\/[^\r\n]*
BlockCommentContent = ([^*\/\r\n]|\/[^*\r\n]|\*[^\/\r\n])
// Literals

IntegerOctLiteral = 0o[0-7][0-7_]*

IntegerDecLiteral = [0-9][0-9_]*
ComplexIntegerDecLiteral = {IntegerDecLiteral}[ijk]

IntegerHexLiteral = 0[xh][0-9a-fA-F_][0-9a-fA-F_]*
IntegerBinLiteral = 0b[01_][01_]*
ZeroFloatLiteral = \.[0-9][0-9_]*{ExponentPart}?[ijk]?
ExponentPart = [eE][+-]?[0-9][0-9_]*


%state DQ_STRING_STATE
%state SQ_STRING_STATE
%state NLSEMI_STATE
%state NEXT_LINE
%state BLOCK_COMMENT_STATE

%state FLOAT_LITERAL_STATE
%%

<YYINITIAL> {

        "package"     { return PACKAGE; }
        "import"      { return IMPORT; }
        "proc"        { return PROC; }
        "return"      { yybegin(NLSEMI_STATE); return RETURN; }
        "defer"       { return DEFER; }
        "struct"      { return STRUCT; }
        "for"         { return FOR; }
        "in"          { return IN; }
        "do"          { return DO; }
        "if"          { return IF; }
        "else"        { return ELSE; }
        "switch"      { return SWITCH; }
        "case"        { return CASE; }
        "fallthrough" { yybegin(NLSEMI_STATE); return FALLTHROUGH; }
        "true"        { yybegin(NLSEMI_STATE); return TRUE; }
        "false"       { yybegin(NLSEMI_STATE); return FALSE; }
        "when"        { return WHEN; }
        "break"       { yybegin(NLSEMI_STATE); return BREAK; }
        "continue"    { yybegin(NLSEMI_STATE); return CONTINUE; }
        "nil"         { yybegin(NLSEMI_STATE); return NIL; }
        "or_else"     { return OR_ELSE; }
        "or_return"   { yybegin(NLSEMI_STATE); return OR_RETURN; }
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


        {LineComment}  { return LINE_COMMENT; }
        //        {BlockComment} { return BLOCK_COMMENT;}

        {Identifier} { yybegin(NLSEMI_STATE); return IDENTIFIER; }
        {WhiteSpace} { return WHITE_SPACE; }
        {NewLine}   { return NEW_LINE; }


        \" {yybegin(DQ_STRING_STATE); return DQ_STRING_START; }
        \'             { yybegin(SQ_STRING_STATE); return SQ_STRING_START;  }
        \`[^`]*\`                                   { yybegin(NLSEMI_STATE); return RAW_STRING_LITERAL; }

        {IntegerDecLiteral} { yybegin(NLSEMI_STATE); return INTEGER_DEC_LITERAL; }
        {IntegerDecLiteral} "." / [^.] {yybegin(NLSEMI_STATE); return FLOAT_DEC_LITERAL; }
        {IntegerDecLiteral}? "." {IntegerDecLiteral} {ExponentPart}? [ijk]? {yybegin(NLSEMI_STATE); return FLOAT_DEC_LITERAL; }
        {IntegerDecLiteral}{ExponentPart}[ijk]? {yybegin(NLSEMI_STATE); return FLOAT_DEC_LITERAL; }

        {IntegerOctLiteral} { yybegin(NLSEMI_STATE); return INTEGER_OCT_LITERAL; }
        {IntegerHexLiteral} { yybegin(NLSEMI_STATE); return INTEGER_HEX_LITERAL; }
        {IntegerBinLiteral} { yybegin(NLSEMI_STATE); return INTEGER_BIN_LITERAL; }

        {ZeroFloatLiteral}  { yybegin(NLSEMI_STATE); return FLOAT_DEC_LITERAL; }
        {ComplexIntegerDecLiteral} { yybegin(NLSEMI_STATE); return COMPLEX_INTEGER_DEC_LITERAL; }

        ":"         { return COLON; }
        "="         { return EQ; }
        \{          { return LBRACE; }
        \}          { yybegin(NLSEMI_STATE); return RBRACE; }
        \(          { return LPAREN; }
        \)          { yybegin(NLSEMI_STATE); return RPAREN; }
        \.          { return DOT; }
        ","         { return COMMA; }
        "->"        { return ARROW; }
        ";"         { return SEMICOLON; }
        "["         { return LBRACKET; }
        "]"         { yybegin(NLSEMI_STATE); return RBRACKET; }
        "#"         { return HASH; }
        "?"         { return QUESTION; }
        "^"         { yybegin(NLSEMI_STATE); return CARET; }
        "@"         { return AT; }

        "/*"        { yybegin(BLOCK_COMMENT_STATE); newLineSeen=false; commentNestingDepth = 1; previousState=YYINITIAL; return BLOCK_COMMENT_START; }

        // Operators
        "=="        { return EQEQ; }
        "!="        { return NEQ; }
        "<"         { return LT; }
        "<="        { return LTE; }
        ">"         { return GT; }
        ">="        { return GTE; }
        "&&"        { return ANDAND; }
        "||"        { return OROR; }
        "!"         { yybegin(NLSEMI_STATE); return NOT; }
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
        "&~="       { return BITWISE_AND_NOT_EQ; }

        // Range operators
        ".."        { return RANGE; }
        "..<"       { return RANGE_EXCLUSIVE; }
        "..="       { return RANGE_INCLUSIVE; }

        "---"       { yybegin(NLSEMI_STATE); return TRIPLE_DASH; }

        "$"         { return DOLLAR; }
        ".?"        { yybegin(NLSEMI_STATE); return DOT_QUESTION; }

        \\          { yybegin(NEXT_LINE);  }
}

    <NLSEMI_STATE> {
        [ \t]+                               { return WHITE_SPACE; }
        "//" [^\r\n]*                        { return LINE_COMMENT; }
        "/*"                                 { yybegin(BLOCK_COMMENT_STATE); newLineSeen=false; commentNestingDepth=1; previousState=NLSEMI_STATE; return BLOCK_COMMENT_START; }
        [\r\n]+ | ';'                        { yybegin(YYINITIAL); return EOS_TOKEN; }
        [^]                                  { yypushback(yylength()); yybegin(YYINITIAL); }
    }

    <NEXT_LINE> {
        [ \t]+                               { return WHITE_SPACE; }
        [\r\n]                               { yybegin(YYINITIAL); return NEW_LINE; }
        [^]                                  { return BAD_CHARACTER; }
    }

    <DQ_STRING_STATE>
    {
        \"  {yybegin(NLSEMI_STATE); return DQ_STRING_END; }
        "\\\"" { return DQ_STRING_LITERAL; }
        [^\"\r\n\\]+ { return DQ_STRING_LITERAL; }
        \\n                            { return DQ_STRING_LITERAL; }
        \\t                            { return DQ_STRING_LITERAL; }
        \\r                            { return DQ_STRING_LITERAL; }
        \\v                            { return DQ_STRING_LITERAL; }
        \\e                            { return DQ_STRING_LITERAL; }
        \\a                            { return DQ_STRING_LITERAL; }
        \\b                            { return DQ_STRING_LITERAL; }
        \\f                            { return DQ_STRING_LITERAL; }
        \\[0-7]{2}                     { return DQ_STRING_LITERAL; }
        \\x[0-9a-fA-F]{2}              { return DQ_STRING_LITERAL; }
        \\u[0-9a-fA-F]{4}              { return DQ_STRING_LITERAL; }
        \\U[0-9a-fA-F]{8}              { return DQ_STRING_LITERAL; }
        \\\"                           { return DQ_STRING_LITERAL; }
        \\                             { return DQ_STRING_LITERAL; }
        [\r\n]     { yybegin(YYINITIAL); return NEW_LINE;}
    }

        <SQ_STRING_STATE>
        {
            \'  {yybegin(NLSEMI_STATE); return SQ_STRING_END; }

            [^\"\r\n\\]+                   { return SQ_STRING_LITERAL; }
            \\n                            { return SQ_STRING_LITERAL; }
            \\t                            { return SQ_STRING_LITERAL; }
            \\r                            { return SQ_STRING_LITERAL; }
            \\v                            { return SQ_STRING_LITERAL; }
            \\e                            { return SQ_STRING_LITERAL; }
            \\a                            { return SQ_STRING_LITERAL; }
            \\b                            { return SQ_STRING_LITERAL; }
            \\f                            { return SQ_STRING_LITERAL; }
            \\[0-7]{2}                     { return SQ_STRING_LITERAL; }
            \\x[0-9a-fA-F]{2}              { return SQ_STRING_LITERAL; }
            \\u[0-9a-fA-F]{4}              { return SQ_STRING_LITERAL; }
            \\U[0-9a-fA-F]{8}              { return SQ_STRING_LITERAL; }
            \\\'                           { return SQ_STRING_LITERAL; }
            \\                             { return SQ_STRING_LITERAL; }
            [\r\n]     { yybegin(YYINITIAL); return NEW_LINE;}
        }

    <BLOCK_COMMENT_STATE> {
        "/*" { commentNestingDepth++; return BLOCK_COMMENT; }
        "*/" {
          commentNestingDepth--;
          if (commentNestingDepth <= 0) {
              if(!newLineSeen) {
                  yybegin(previousState);
              } else {
                  yybegin(YYINITIAL);
              }

              return BLOCK_COMMENT_END;
          } else {
              return BLOCK_COMMENT;
          }
        }

        {BlockCommentContent}+                { return BLOCK_COMMENT; }
        [\r\n]+                               { newLineSeen = true; return NEW_LINE; }
        [^]                                   { return BLOCK_COMMENT; }
    }

[^] { return BAD_CHARACTER; }