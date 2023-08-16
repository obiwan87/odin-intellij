// This is a generated file. Not intended for manual editing.
package com.lasagnerd.odin.lang;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static com.lasagnerd.odin.lang.psi.OdinTypes.*;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class OdinParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, EXTENDS_SETS_);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    r = parse_root_(t, b);
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b) {
    return parse_root_(t, b, 0);
  }

  static boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return odinFile(b, l + 1);
  }

  public static final TokenSet[] EXTENDS_SETS_ = new TokenSet[] {
    create_token_set_(BINARY_EXPRESSION, CHAIN_EXPRESSION, EXPRESSION, FUNCTION_CALL_EXPRESSION,
      LITERAL_EXPRESSION, PARENTHESIZED_EXPRESSION),
  };

  /* ********************************************************** */
  // expression (COMMA expression)*
  public static boolean argumentList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "argumentList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ARGUMENT_LIST, "<argument list>");
    r = expression(b, l + 1, -1);
    r = r && argumentList_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (COMMA expression)*
  private static boolean argumentList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "argumentList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!argumentList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "argumentList_1", c)) break;
    }
    return true;
  }

  // COMMA expression
  private static boolean argumentList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "argumentList_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // EQ
  //                        | PLUS_EQ
  //                        | MINUS_EQ
  //                        | STAR_EQ
  //                        | DIV_EQ
  //                        | MOD_EQ
  //                        | REMAINDER_EQ
  //                        | AND_EQ
  //                        | OR_EQ
  //                        | XOR_EQ
  //                        | ANDAND_EQ
  //                        | OROR_EQ
  //                        | LSHIFT_EQ
  //                        | RSHIFT_EQ
  public static boolean assignmentOperator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "assignmentOperator")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ASSIGNMENT_OPERATOR, "<assignment operator>");
    r = consumeToken(b, EQ);
    if (!r) r = consumeToken(b, PLUS_EQ);
    if (!r) r = consumeToken(b, MINUS_EQ);
    if (!r) r = consumeToken(b, STAR_EQ);
    if (!r) r = consumeToken(b, DIV_EQ);
    if (!r) r = consumeToken(b, MOD_EQ);
    if (!r) r = consumeToken(b, REMAINDER_EQ);
    if (!r) r = consumeToken(b, AND_EQ);
    if (!r) r = consumeToken(b, OR_EQ);
    if (!r) r = consumeToken(b, XOR_EQ);
    if (!r) r = consumeToken(b, ANDAND_EQ);
    if (!r) r = consumeToken(b, OROR_EQ);
    if (!r) r = consumeToken(b, LSHIFT_EQ);
    if (!r) r = consumeToken(b, RSHIFT_EQ);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // PLUS
  //                    | MINUS
  //                    | STAR
  //                    | DIV
  //                    | MOD
  //                    | REMAINDER
  //                    | AND_BITWISE
  //                    | OR_BITWISE
  //                    | XOR_BITWISE
  //                    | ANDAND
  //                    | OROR
  //                    | LSHIFT
  //                    | RSHIFT
  //                    | EQEQ
  //                    | NEQ
  //                    | LT
  //                    | GT
  //                    | LTE
  //                    | GTE
  //                    | RANGE_INCLUSIVE
  //                    | RANGE_EXCLUSIVE
  public static boolean binaryOperator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "binaryOperator")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BINARY_OPERATOR, "<binary operator>");
    r = consumeToken(b, PLUS);
    if (!r) r = consumeToken(b, MINUS);
    if (!r) r = consumeToken(b, STAR);
    if (!r) r = consumeToken(b, DIV);
    if (!r) r = consumeToken(b, MOD);
    if (!r) r = consumeToken(b, REMAINDER);
    if (!r) r = consumeToken(b, AND_BITWISE);
    if (!r) r = consumeToken(b, OR_BITWISE);
    if (!r) r = consumeToken(b, XOR_BITWISE);
    if (!r) r = consumeToken(b, ANDAND);
    if (!r) r = consumeToken(b, OROR);
    if (!r) r = consumeToken(b, LSHIFT);
    if (!r) r = consumeToken(b, RSHIFT);
    if (!r) r = consumeToken(b, EQEQ);
    if (!r) r = consumeToken(b, NEQ);
    if (!r) r = consumeToken(b, LT);
    if (!r) r = consumeToken(b, GT);
    if (!r) r = consumeToken(b, LTE);
    if (!r) r = consumeToken(b, GTE);
    if (!r) r = consumeToken(b, RANGE_INCLUSIVE);
    if (!r) r = consumeToken(b, RANGE_EXCLUSIVE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // IDENTIFIER COLON IDENTIFIER? COLON expression
  public static boolean constantAssigment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constantAssigment")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, IDENTIFIER, COLON);
    r = r && constantAssigment_2(b, l + 1);
    r = r && consumeToken(b, COLON);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, CONSTANT_ASSIGMENT, r);
    return r;
  }

  // IDENTIFIER?
  private static boolean constantAssigment_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constantAssigment_2")) return false;
    consumeToken(b, IDENTIFIER);
    return true;
  }

  /* ********************************************************** */
  // HASH IDENTIFIER
  public static boolean directive(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive")) return false;
    if (!nextTokenIs(b, HASH)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, HASH, IDENTIFIER);
    exit_section_(b, m, DIRECTIVE, r);
    return r;
  }

  /* ********************************************************** */
  // ELSE ifBlock
  public static boolean elseBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "elseBlock")) return false;
    if (!nextTokenIs(b, ELSE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ELSE);
    r = r && ifBlock(b, l + 1);
    exit_section_(b, m, ELSE_BLOCK, r);
    return r;
  }

  /* ********************************************************** */
  // ELSE IF ifHead ifBlock
  public static boolean elseIfBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "elseIfBlock")) return false;
    if (!nextTokenIs(b, ELSE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, ELSE, IF);
    r = r && ifHead(b, l + 1);
    r = r && ifBlock(b, l + 1);
    exit_section_(b, m, ELSE_IF_BLOCK, r);
    return r;
  }

  /* ********************************************************** */
  // LBRACE (statementAllowedInBlock)* RBRACE
  public static boolean forBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forBlock")) return false;
    if (!nextTokenIs(b, LBRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LBRACE);
    r = r && forBlock_1(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, m, FOR_BLOCK, r);
    return r;
  }

  // (statementAllowedInBlock)*
  private static boolean forBlock_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forBlock_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!forBlock_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "forBlock_1", c)) break;
    }
    return true;
  }

  // (statementAllowedInBlock)
  private static boolean forBlock_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forBlock_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = statementAllowedInBlock(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // forBlock | forSingleStatement
  public static boolean forBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forBody")) return false;
    if (!nextTokenIs(b, "<for body>", DO, LBRACE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FOR_BODY, "<for body>");
    r = forBlock(b, l + 1);
    if (!r) r = forSingleStatement(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // variableAssignment | functionCall_expression
  static boolean forEndStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forEndStatement")) return false;
    boolean r;
    r = variableAssignment(b, l + 1);
    if (!r) r = expression(b, l + 1, 0);
    return r;
  }

  /* ********************************************************** */
  // forIn|forTraditional
  public static boolean forHead(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forHead")) return false;
    if (!nextTokenIs(b, FOR)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = forIn(b, l + 1);
    if (!r) r = forTraditional(b, l + 1);
    exit_section_(b, m, FOR_HEAD, r);
    return r;
  }

  /* ********************************************************** */
  // FOR IDENTIFIER (COMMA IDENTIFIER)* IN expression
  public static boolean forIn(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forIn")) return false;
    if (!nextTokenIs(b, FOR)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, FOR, IDENTIFIER);
    r = r && forIn_2(b, l + 1);
    r = r && consumeToken(b, IN);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, FOR_IN, r);
    return r;
  }

  // (COMMA IDENTIFIER)*
  private static boolean forIn_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forIn_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!forIn_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "forIn_2", c)) break;
    }
    return true;
  }

  // COMMA IDENTIFIER
  private static boolean forIn_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forIn_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, COMMA, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // DO statementAllowedInBlock
  public static boolean forSingleStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forSingleStatement")) return false;
    if (!nextTokenIs(b, DO)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DO);
    r = r && statementAllowedInBlock(b, l + 1);
    exit_section_(b, m, FOR_SINGLE_STATEMENT, r);
    return r;
  }

  /* ********************************************************** */
  // variableAssignmentDeclaration | variableAssignment | functionCall_expression
  static boolean forStartStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forStartStatement")) return false;
    boolean r;
    r = variableAssignmentDeclaration(b, l + 1);
    if (!r) r = variableAssignment(b, l + 1);
    if (!r) r = expression(b, l + 1, 0);
    return r;
  }

  /* ********************************************************** */
  // directive? forHead forBody
  public static boolean forStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forStatement")) return false;
    if (!nextTokenIs(b, "<for statement>", FOR, HASH)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FOR_STATEMENT, "<for statement>");
    r = forStatement_0(b, l + 1);
    r = r && forHead(b, l + 1);
    r = r && forBody(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // directive?
  private static boolean forStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forStatement_0")) return false;
    directive(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // FOR (forStartStatement? SEMICOLON)? expression? (SEMICOLON forEndStatement?)?
  public static boolean forTraditional(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forTraditional")) return false;
    if (!nextTokenIs(b, FOR)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, FOR);
    r = r && forTraditional_1(b, l + 1);
    r = r && forTraditional_2(b, l + 1);
    r = r && forTraditional_3(b, l + 1);
    exit_section_(b, m, FOR_TRADITIONAL, r);
    return r;
  }

  // (forStartStatement? SEMICOLON)?
  private static boolean forTraditional_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forTraditional_1")) return false;
    forTraditional_1_0(b, l + 1);
    return true;
  }

  // forStartStatement? SEMICOLON
  private static boolean forTraditional_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forTraditional_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = forTraditional_1_0_0(b, l + 1);
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, m, null, r);
    return r;
  }

  // forStartStatement?
  private static boolean forTraditional_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forTraditional_1_0_0")) return false;
    forStartStatement(b, l + 1);
    return true;
  }

  // expression?
  private static boolean forTraditional_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forTraditional_2")) return false;
    expression(b, l + 1, -1);
    return true;
  }

  // (SEMICOLON forEndStatement?)?
  private static boolean forTraditional_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forTraditional_3")) return false;
    forTraditional_3_0(b, l + 1);
    return true;
  }

  // SEMICOLON forEndStatement?
  private static boolean forTraditional_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forTraditional_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SEMICOLON);
    r = r && forTraditional_3_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // forEndStatement?
  private static boolean forTraditional_3_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forTraditional_3_0_1")) return false;
    forEndStatement(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // importDeclaration | structDeclaration | statement
  public static boolean globalStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "globalStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, GLOBAL_STATEMENT, "<global statement>");
    r = importDeclaration(b, l + 1);
    if (!r) r = structDeclaration(b, l + 1);
    if (!r) r = statement(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // LBRACE (statementAllowedInBlock)* RBRACE
  public static boolean ifBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifBlock")) return false;
    if (!nextTokenIs(b, LBRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LBRACE);
    r = r && ifBlock_1(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, m, IF_BLOCK, r);
    return r;
  }

  // (statementAllowedInBlock)*
  private static boolean ifBlock_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifBlock_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!ifBlock_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ifBlock_1", c)) break;
    }
    return true;
  }

  // (statementAllowedInBlock)
  private static boolean ifBlock_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifBlock_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = statementAllowedInBlock(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (variableAssignmentDeclaration (COMMA variableAssignmentDeclaration)* SEMICOLON)? expression
  public static boolean ifHead(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifHead")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, IF_HEAD, "<if head>");
    r = ifHead_0(b, l + 1);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (variableAssignmentDeclaration (COMMA variableAssignmentDeclaration)* SEMICOLON)?
  private static boolean ifHead_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifHead_0")) return false;
    ifHead_0_0(b, l + 1);
    return true;
  }

  // variableAssignmentDeclaration (COMMA variableAssignmentDeclaration)* SEMICOLON
  private static boolean ifHead_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifHead_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = variableAssignmentDeclaration(b, l + 1);
    r = r && ifHead_0_0_1(b, l + 1);
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA variableAssignmentDeclaration)*
  private static boolean ifHead_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifHead_0_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!ifHead_0_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ifHead_0_0_1", c)) break;
    }
    return true;
  }

  // COMMA variableAssignmentDeclaration
  private static boolean ifHead_0_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifHead_0_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && variableAssignmentDeclaration(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // IF  ifHead ifBlock (elseIfBlock)* (elseBlock)?
  public static boolean ifStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifStatement")) return false;
    if (!nextTokenIs(b, IF)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IF);
    r = r && ifHead(b, l + 1);
    r = r && ifBlock(b, l + 1);
    r = r && ifStatement_3(b, l + 1);
    r = r && ifStatement_4(b, l + 1);
    exit_section_(b, m, IF_STATEMENT, r);
    return r;
  }

  // (elseIfBlock)*
  private static boolean ifStatement_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifStatement_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!ifStatement_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ifStatement_3", c)) break;
    }
    return true;
  }

  // (elseIfBlock)
  private static boolean ifStatement_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifStatement_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = elseIfBlock(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (elseBlock)?
  private static boolean ifStatement_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifStatement_4")) return false;
    ifStatement_4_0(b, l + 1);
    return true;
  }

  // (elseBlock)
  private static boolean ifStatement_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifStatement_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = elseBlock(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // IMPORT IDENTIFIER? STRING_LITERAL
  public static boolean importDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "importDeclaration")) return false;
    if (!nextTokenIs(b, IMPORT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IMPORT);
    r = r && importDeclaration_1(b, l + 1);
    r = r && consumeToken(b, STRING_LITERAL);
    exit_section_(b, m, IMPORT_DECLARATION, r);
    return r;
  }

  // IDENTIFIER?
  private static boolean importDeclaration_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "importDeclaration_1")) return false;
    consumeToken(b, IDENTIFIER);
    return true;
  }

  /* ********************************************************** */
  // packageDeclaration globalStatement*
  static boolean odinFile(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "odinFile")) return false;
    if (!nextTokenIs(b, PACKAGE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = packageDeclaration(b, l + 1);
    r = r && odinFile_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // globalStatement*
  private static boolean odinFile_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "odinFile_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!globalStatement(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "odinFile_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // PACKAGE IDENTIFIER
  public static boolean packageDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "packageDeclaration")) return false;
    if (!nextTokenIs(b, PACKAGE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, PACKAGE, IDENTIFIER);
    exit_section_(b, m, PACKAGE_DECLARATION, r);
    return r;
  }

  /* ********************************************************** */
  // IDENTIFIER (COLON IDENTIFIER)?
  public static boolean parameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    r = r && parameter_1(b, l + 1);
    exit_section_(b, m, PARAMETER, r);
    return r;
  }

  // (COLON IDENTIFIER)?
  private static boolean parameter_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_1")) return false;
    parameter_1_0(b, l + 1);
    return true;
  }

  // COLON IDENTIFIER
  private static boolean parameter_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, COLON, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // parameter (COMMA parameter)*
  public static boolean parameterList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterList")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = parameter(b, l + 1);
    r = r && parameterList_1(b, l + 1);
    exit_section_(b, m, PARAMETER_LIST, r);
    return r;
  }

  // (COMMA parameter)*
  private static boolean parameterList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!parameterList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "parameterList_1", c)) break;
    }
    return true;
  }

  // COMMA parameter
  private static boolean parameterList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterList_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && parameter(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // IDENTIFIER COLON COLON PROC LPAREN parameterList* RPAREN (ARROW returnType)?
  //                         LBRACE (statement|variableAssignment|functionCall_expression|returnStatement)* RBRACE
  public static boolean procedureDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureDeclaration")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, IDENTIFIER, COLON, COLON, PROC, LPAREN);
    r = r && procedureDeclaration_5(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    r = r && procedureDeclaration_7(b, l + 1);
    r = r && consumeToken(b, LBRACE);
    r = r && procedureDeclaration_9(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, m, PROCEDURE_DECLARATION, r);
    return r;
  }

  // parameterList*
  private static boolean procedureDeclaration_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureDeclaration_5")) return false;
    while (true) {
      int c = current_position_(b);
      if (!parameterList(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "procedureDeclaration_5", c)) break;
    }
    return true;
  }

  // (ARROW returnType)?
  private static boolean procedureDeclaration_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureDeclaration_7")) return false;
    procedureDeclaration_7_0(b, l + 1);
    return true;
  }

  // ARROW returnType
  private static boolean procedureDeclaration_7_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureDeclaration_7_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ARROW);
    r = r && returnType(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (statement|variableAssignment|functionCall_expression|returnStatement)*
  private static boolean procedureDeclaration_9(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureDeclaration_9")) return false;
    while (true) {
      int c = current_position_(b);
      if (!procedureDeclaration_9_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "procedureDeclaration_9", c)) break;
    }
    return true;
  }

  // statement|variableAssignment|functionCall_expression|returnStatement
  private static boolean procedureDeclaration_9_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureDeclaration_9_0")) return false;
    boolean r;
    r = statement(b, l + 1);
    if (!r) r = variableAssignment(b, l + 1);
    if (!r) r = expression(b, l + 1, 0);
    if (!r) r = returnStatement(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // RETURN argumentList?
  public static boolean returnStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnStatement")) return false;
    if (!nextTokenIs(b, RETURN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, RETURN);
    r = r && returnStatement_1(b, l + 1);
    exit_section_(b, m, RETURN_STATEMENT, r);
    return r;
  }

  // argumentList?
  private static boolean returnStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnStatement_1")) return false;
    argumentList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // IDENTIFIER
  //             | LPAREN parameterList* RPAREN
  public static boolean returnType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnType")) return false;
    if (!nextTokenIs(b, "<return type>", IDENTIFIER, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RETURN_TYPE, "<return type>");
    r = consumeToken(b, IDENTIFIER);
    if (!r) r = returnType_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // LPAREN parameterList* RPAREN
  private static boolean returnType_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnType_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && returnType_1_1(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // parameterList*
  private static boolean returnType_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnType_1_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!parameterList(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "returnType_1_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // variableAssignmentDeclaration
  // | variableDeclaration
  // | constantAssigment
  // | procedureDeclaration
  // | forStatement
  // | ifStatement
  // | switchStatement
  public static boolean statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, STATEMENT, "<statement>");
    r = variableAssignmentDeclaration(b, l + 1);
    if (!r) r = variableDeclaration(b, l + 1);
    if (!r) r = constantAssigment(b, l + 1);
    if (!r) r = procedureDeclaration(b, l + 1);
    if (!r) r = forStatement(b, l + 1);
    if (!r) r = ifStatement(b, l + 1);
    if (!r) r = switchStatement(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // statement|variableAssignment|functionCall_expression
  static boolean statementAllowedInBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statementAllowedInBlock")) return false;
    boolean r;
    r = statement(b, l + 1);
    if (!r) r = variableAssignment(b, l + 1);
    if (!r) r = expression(b, l + 1, 0);
    return r;
  }

  /* ********************************************************** */
  // IDENTIFIER COLON COLON STRUCT LBRACE (variableDeclaration COMMA)* RBRACE
  public static boolean structDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structDeclaration")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, IDENTIFIER, COLON, COLON, STRUCT, LBRACE);
    r = r && structDeclaration_5(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, m, STRUCT_DECLARATION, r);
    return r;
  }

  // (variableDeclaration COMMA)*
  private static boolean structDeclaration_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structDeclaration_5")) return false;
    while (true) {
      int c = current_position_(b);
      if (!structDeclaration_5_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "structDeclaration_5", c)) break;
    }
    return true;
  }

  // variableDeclaration COMMA
  private static boolean structDeclaration_5_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structDeclaration_5_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = variableDeclaration(b, l + 1);
    r = r && consumeToken(b, COMMA);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // CASE (expression(COMMA expression)*)? COLON (statementAllowedInBlock|FALLTHROUGH)*
  public static boolean switchCaseBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchCaseBlock")) return false;
    if (!nextTokenIs(b, CASE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, CASE);
    r = r && switchCaseBlock_1(b, l + 1);
    r = r && consumeToken(b, COLON);
    r = r && switchCaseBlock_3(b, l + 1);
    exit_section_(b, m, SWITCH_CASE_BLOCK, r);
    return r;
  }

  // (expression(COMMA expression)*)?
  private static boolean switchCaseBlock_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchCaseBlock_1")) return false;
    switchCaseBlock_1_0(b, l + 1);
    return true;
  }

  // expression(COMMA expression)*
  private static boolean switchCaseBlock_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchCaseBlock_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = expression(b, l + 1, -1);
    r = r && switchCaseBlock_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA expression)*
  private static boolean switchCaseBlock_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchCaseBlock_1_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!switchCaseBlock_1_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "switchCaseBlock_1_0_1", c)) break;
    }
    return true;
  }

  // COMMA expression
  private static boolean switchCaseBlock_1_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchCaseBlock_1_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (statementAllowedInBlock|FALLTHROUGH)*
  private static boolean switchCaseBlock_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchCaseBlock_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!switchCaseBlock_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "switchCaseBlock_3", c)) break;
    }
    return true;
  }

  // statementAllowedInBlock|FALLTHROUGH
  private static boolean switchCaseBlock_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchCaseBlock_3_0")) return false;
    boolean r;
    r = statementAllowedInBlock(b, l + 1);
    if (!r) r = consumeToken(b, FALLTHROUGH);
    return r;
  }

  /* ********************************************************** */
  // SWITCH ifHead? LBRACE (switchCaseBlock)* RBRACE
  public static boolean switchStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchStatement")) return false;
    if (!nextTokenIs(b, SWITCH)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SWITCH);
    r = r && switchStatement_1(b, l + 1);
    r = r && consumeToken(b, LBRACE);
    r = r && switchStatement_3(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, m, SWITCH_STATEMENT, r);
    return r;
  }

  // ifHead?
  private static boolean switchStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchStatement_1")) return false;
    ifHead(b, l + 1);
    return true;
  }

  // (switchCaseBlock)*
  private static boolean switchStatement_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchStatement_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!switchStatement_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "switchStatement_3", c)) break;
    }
    return true;
  }

  // (switchCaseBlock)
  private static boolean switchStatement_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchStatement_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = switchCaseBlock(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // IDENTIFIER (COMMA IDENTIFIER)*
  public static boolean typelessParameterList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typelessParameterList")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    r = r && typelessParameterList_1(b, l + 1);
    exit_section_(b, m, TYPELESS_PARAMETER_LIST, r);
    return r;
  }

  // (COMMA IDENTIFIER)*
  private static boolean typelessParameterList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typelessParameterList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!typelessParameterList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "typelessParameterList_1", c)) break;
    }
    return true;
  }

  // COMMA IDENTIFIER
  private static boolean typelessParameterList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typelessParameterList_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, COMMA, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // IDENTIFIER assignmentOperator expression
  public static boolean variableAssignment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableAssignment")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    r = r && assignmentOperator(b, l + 1);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, VARIABLE_ASSIGNMENT, r);
    return r;
  }

  /* ********************************************************** */
  // (typelessParameterList COLON EQ | IDENTIFIER COLON IDENTIFIER EQ) expression
  public static boolean variableAssignmentDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableAssignmentDeclaration")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = variableAssignmentDeclaration_0(b, l + 1);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, VARIABLE_ASSIGNMENT_DECLARATION, r);
    return r;
  }

  // typelessParameterList COLON EQ | IDENTIFIER COLON IDENTIFIER EQ
  private static boolean variableAssignmentDeclaration_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableAssignmentDeclaration_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = variableAssignmentDeclaration_0_0(b, l + 1);
    if (!r) r = parseTokens(b, 0, IDENTIFIER, COLON, IDENTIFIER, EQ);
    exit_section_(b, m, null, r);
    return r;
  }

  // typelessParameterList COLON EQ
  private static boolean variableAssignmentDeclaration_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableAssignmentDeclaration_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = typelessParameterList(b, l + 1);
    r = r && consumeTokens(b, 0, COLON, EQ);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // IDENTIFIER COLON IDENTIFIER
  public static boolean variableDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableDeclaration")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, IDENTIFIER, COLON, IDENTIFIER);
    exit_section_(b, m, VARIABLE_DECLARATION, r);
    return r;
  }

  /* ********************************************************** */
  // Expression root: expression
  // Operator priority table:
  // 0: PREFIX(parenthesized_expression)
  // 1: POSTFIX(functionCall_expression)
  // 2: ATOM(chain_expression)
  // 3: ATOM(literal_expression)
  // 4: BINARY(binary_expression)
  public static boolean expression(PsiBuilder b, int l, int g) {
    if (!recursion_guard_(b, l, "expression")) return false;
    addVariant(b, "<expression>");
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, "<expression>");
    r = parenthesized_expression(b, l + 1);
    if (!r) r = chain_expression(b, l + 1);
    if (!r) r = literal_expression(b, l + 1);
    p = r;
    r = r && expression_0(b, l + 1, g);
    exit_section_(b, l, m, null, r, p, null);
    return r || p;
  }

  public static boolean expression_0(PsiBuilder b, int l, int g) {
    if (!recursion_guard_(b, l, "expression_0")) return false;
    boolean r = true;
    while (true) {
      Marker m = enter_section_(b, l, _LEFT_, null);
      if (g < 1 && functionCall_expression_0(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, FUNCTION_CALL_EXPRESSION, r, true, null);
      }
      else if (g < 4 && binaryOperator(b, l + 1)) {
        r = expression(b, l, 4);
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else {
        exit_section_(b, l, m, null, false, false, null);
        break;
      }
    }
    return r;
  }

  public static boolean parenthesized_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parenthesized_expression")) return false;
    if (!nextTokenIsSmart(b, LPAREN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeTokenSmart(b, LPAREN);
    p = r;
    r = p && expression(b, l, 0);
    r = p && report_error_(b, consumeToken(b, RPAREN)) && r;
    exit_section_(b, l, m, PARENTHESIZED_EXPRESSION, r, p, null);
    return r || p;
  }

  // LPAREN argumentList* RPAREN
  private static boolean functionCall_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionCall_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, LPAREN);
    r = r && functionCall_expression_0_1(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // argumentList*
  private static boolean functionCall_expression_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionCall_expression_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!argumentList(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "functionCall_expression_0_1", c)) break;
    }
    return true;
  }

  // IDENTIFIER (DOT IDENTIFIER)*
  public static boolean chain_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "chain_expression")) return false;
    if (!nextTokenIsSmart(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, IDENTIFIER);
    r = r && chain_expression_1(b, l + 1);
    exit_section_(b, m, CHAIN_EXPRESSION, r);
    return r;
  }

  // (DOT IDENTIFIER)*
  private static boolean chain_expression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "chain_expression_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!chain_expression_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "chain_expression_1", c)) break;
    }
    return true;
  }

  // DOT IDENTIFIER
  private static boolean chain_expression_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "chain_expression_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokensSmart(b, 0, DOT, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  // STRING_LITERAL
  //          | INTEGER_DEC_LITERAL
  //          | INTEGER_HEX_LITERAL
  //          | INTEGER_OCT_LITERAL
  public static boolean literal_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "literal_expression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LITERAL_EXPRESSION, "<literal expression>");
    r = consumeTokenSmart(b, STRING_LITERAL);
    if (!r) r = consumeTokenSmart(b, INTEGER_DEC_LITERAL);
    if (!r) r = consumeTokenSmart(b, INTEGER_HEX_LITERAL);
    if (!r) r = consumeTokenSmart(b, INTEGER_OCT_LITERAL);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

}
