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
IntegerHexLiteral = 0x[0-9a-fA-F][0-9a-fA-F_]*

%state STRING_STATE
%%



<YYINITIAL> {

      "package"    { return PACKAGE; }
      "import"     { return IMPORT; }
      "proc"       { return PROC; }
      "return"     { return RETURN; }
      "defer"      { return DEFER; }

      {LineComment} { return LINE_COMMENT; }
      {BlockComment} { return BLOCK_COMMENT; }
      {Identifier} { return IDENTIFIER; }
      {WhiteSpace} { return WHITE_SPACE; }

      \"           { yybegin(STRING_STATE); string.setLength(0); }

      {IntegerOctLiteral} { return INTEGER_OCT_LITERAL; }
      {IntegerDecLiteral} { return INTEGER_DEC_LITERAL; }
      {IntegerHexLiteral} { return INTEGER_HEX_LITERAL; }

      "="          { return EQ; }
      ":="         { return ASSIGN; }
      "::"         { return DOUBLE_COLON; }
      ":"          { return COLON; }
      \{          { return LBRACE; }
      \}          { return RBRACE; }
      \(          { return LPAREN; }
      \)          { return RPAREN; }
      \.          { return DOT; }
      ","         { return COMMA; }
      "->"        { return ARROW; }
}

    <STRING_STATE> {
      \"                             { yybegin(YYINITIAL); return STRING_LITERAL; }
      [^\n\r\"\\]+                   {  }
      \\t                            {  }
      \\n                            {  }
      \\r                            {  }
      \\v                            {  }
      \\e                            {  }
      \\a                            {  }
      \\b                            {  }
      \\f                            {  }
      \\[0-7]{2}                     {  }
      \\x[0-9a-fA-F]{2}              {  }
      \\u[0-9a-fA-F]{4}              {  }
      \\U[0-9a-fA-F]{8}              {  }
      \\\"                           {  }
      \\                             {  }
    }

[^] { return BAD_CHARACTER; }