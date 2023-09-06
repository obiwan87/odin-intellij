// This is a generated file. Not intended for manual editing.
package com.lasagnerd.odin.lang;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static com.lasagnerd.odin.lang.psi.OdinTypes.*;
import static com.lasagnerd.odin.lang.OdinParserUtil.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;

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
    create_token_set_(ARGUMENT, NAMED_ARGUMENT, UNNAMED_ARGUMENT),
    create_token_set_(CARET_PRIMARY, COMBINED_PRIMARY, OPERAND_PRIMARY, PRIMARY),
    create_token_set_(AUTO_CAST_EXPRESSION, BINARY_EXPRESSION, CALL_EXPRESSION, CAST_EXPRESSION,
      EXPRESSION, IDENTIFIER_EXPRESSION, IMPLICIT_SELECTOR_EXPRESSION, MAYBE_EXPRESSION,
      OR_RETURN_EXPRESSION, PARENTHESIZED_EXPRESSION, PRIMARY_EXPRESSION, PROCEDURE_EXPRESSION,
      SET_EXPRESSION, TAG_STATEMENT_EXPRESSION, TERNARY_COND_EXPRESSION, TERNARY_IF_EXPRESSION,
      TERNARY_WHEN_EXPRESSION, TRANSMUTE_EXPRESSION, TRIPLE_DASH_LITERAL_EXPRESSION, TYPE_DEFINITION_EXPRESSION,
      TYPE_DEFINITION_VALUE_EXPRESSION, UNARY_EXPRESSION, UNINITIALIZED_EXPRESSION),
    create_token_set_(ASSIGNMENT_STATEMENT, ATTRIBUTE_STATEMENT, BITSET_DECLARATION_STATEMENT, BLOCK_STATEMENT,
      BREAK_STATEMENT, CONSTANT_INITIALIZATION_STATEMENT, CONTINUE_STATEMENT, DEFER_STATEMENT,
      DO_STATEMENT, ENUM_DECLARATION_STATEMENT, EXPRESSION_STATEMENT, FALLTHROUGH_STATEMENT,
      FIELD_DECLARATION_STATEMENT, FILE_SCOPE_STATEMENT, FOREIGN_BLOCK_STATEMENT, FOREIGN_IMPORT_DECLARATION_STATEMENT,
      FOREIGN_PROCEDURE_DECLARATION_STATEMENT, FOREIGN_STATEMENT, FOR_STATEMENT, IF_STATEMENT,
      IMPORT_DECLARATION_STATEMENT, PARAMETER_DECLARATION_STATEMENT, PROCEDURE_DECLARATION_STATEMENT, PROCEDURE_OVERLOAD_STATEMENT,
      RETURN_STATEMENT, STATEMENT, STRUCT_DECLARATION_STATEMENT, SWITCH_STATEMENT,
      TYPE_ALIAS_DECLARATION_STATEMENT, UNION_DECLARATION_STATEMENT, USING_STATEMENT, VARIABLE_DECLARATION_STATEMENT,
      VARIABLE_INITIALIZATION_STATEMENT, WHEN_STATEMENT),
  };

  /* ********************************************************** */
  // namedArgument
  //                   | unnamedArgument
  public static boolean argument(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "argument")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, ARGUMENT, "<argument>");
    r = namedArgument(b, l + 1);
    if (!r) r = unnamedArgument(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // argument (COMMA argument)* COMMA?
  public static boolean argumentList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "argumentList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ARGUMENT_LIST, "<argument list>");
    r = argument(b, l + 1);
    r = r && argumentList_1(b, l + 1);
    r = r && argumentList_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (COMMA argument)*
  private static boolean argumentList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "argumentList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!argumentList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "argumentList_1", c)) break;
    }
    return true;
  }

  // COMMA argument
  private static boolean argumentList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "argumentList_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && argument(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // COMMA?
  private static boolean argumentList_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "argumentList_2")) return false;
    consumeToken(b, COMMA);
    return true;
  }

  /* ********************************************************** */
  // LPAREN argumentList? RPAREN
  static boolean arguments(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arguments")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && arguments_1(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // argumentList?
  private static boolean arguments_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arguments_1")) return false;
    argumentList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // expression (COMMA expression)* COMMA?
  static boolean arrayExpressionsList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayExpressionsList")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = expression(b, l + 1, -1);
    r = r && arrayExpressionsList_1(b, l + 1);
    r = r && arrayExpressionsList_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA expression)*
  private static boolean arrayExpressionsList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayExpressionsList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!arrayExpressionsList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "arrayExpressionsList_1", c)) break;
    }
    return true;
  }

  // COMMA expression
  private static boolean arrayExpressionsList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayExpressionsList_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  // COMMA?
  private static boolean arrayExpressionsList_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayExpressionsList_2")) return false;
    consumeToken(b, COMMA);
    return true;
  }

  /* ********************************************************** */
  // ((intLiteral (RANGE_INCLUSIVE|RANGE_EXCLUSIVE) intLiteral)|intLiteral) EQ expression
  static boolean arrayIndexedInitialization(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayIndexedInitialization")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = arrayIndexedInitialization_0(b, l + 1);
    r = r && consumeToken(b, EQ);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (intLiteral (RANGE_INCLUSIVE|RANGE_EXCLUSIVE) intLiteral)|intLiteral
  private static boolean arrayIndexedInitialization_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayIndexedInitialization_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = arrayIndexedInitialization_0_0(b, l + 1);
    if (!r) r = intLiteral(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // intLiteral (RANGE_INCLUSIVE|RANGE_EXCLUSIVE) intLiteral
  private static boolean arrayIndexedInitialization_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayIndexedInitialization_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = intLiteral(b, l + 1);
    r = r && arrayIndexedInitialization_0_0_1(b, l + 1);
    r = r && intLiteral(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // RANGE_INCLUSIVE|RANGE_EXCLUSIVE
  private static boolean arrayIndexedInitialization_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayIndexedInitialization_0_0_1")) return false;
    boolean r;
    r = consumeToken(b, RANGE_INCLUSIVE);
    if (!r) r = consumeToken(b, RANGE_EXCLUSIVE);
    return r;
  }

  /* ********************************************************** */
  // arrayIndexedInitialization (COMMA arrayIndexedInitialization)* COMMA?
  static boolean arrayIndexedList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayIndexedList")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = arrayIndexedInitialization(b, l + 1);
    r = r && arrayIndexedList_1(b, l + 1);
    r = r && arrayIndexedList_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA arrayIndexedInitialization)*
  private static boolean arrayIndexedList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayIndexedList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!arrayIndexedList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "arrayIndexedList_1", c)) break;
    }
    return true;
  }

  // COMMA arrayIndexedInitialization
  private static boolean arrayIndexedList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayIndexedList_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && arrayIndexedInitialization(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // COMMA?
  private static boolean arrayIndexedList_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayIndexedList_2")) return false;
    consumeToken(b, COMMA);
    return true;
  }

  /* ********************************************************** */
  // [tagHead] LBRACKET (expression|QUESTION|DYNAMIC) RBRACKET typeDefinition_expression LBRACE [arrayIndexedList|arrayExpressionsList] RBRACE
  public static boolean arrayLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayLiteral")) return false;
    if (!nextTokenIs(b, "<array literal>", HASH, LBRACKET)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ARRAY_LITERAL, "<array literal>");
    r = arrayLiteral_0(b, l + 1);
    r = r && consumeToken(b, LBRACKET);
    r = r && arrayLiteral_2(b, l + 1);
    r = r && consumeToken(b, RBRACKET);
    r = r && typeDefinition_expression(b, l + 1);
    r = r && consumeToken(b, LBRACE);
    r = r && arrayLiteral_6(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // [tagHead]
  private static boolean arrayLiteral_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayLiteral_0")) return false;
    tagHead(b, l + 1);
    return true;
  }

  // expression|QUESTION|DYNAMIC
  private static boolean arrayLiteral_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayLiteral_2")) return false;
    boolean r;
    r = expression(b, l + 1, -1);
    if (!r) r = consumeToken(b, QUESTION);
    if (!r) r = consumeToken(b, DYNAMIC);
    return r;
  }

  // [arrayIndexedList|arrayExpressionsList]
  private static boolean arrayLiteral_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayLiteral_6")) return false;
    arrayLiteral_6_0(b, l + 1);
    return true;
  }

  // arrayIndexedList|arrayExpressionsList
  private static boolean arrayLiteral_6_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayLiteral_6_0")) return false;
    boolean r;
    r = arrayIndexedList(b, l + 1);
    if (!r) r = arrayExpressionsList(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // [tagHead] LBRACKET [QUESTION|DYNAMIC|expression] RBRACKET typeDefinition_expression
  public static boolean arrayType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayType")) return false;
    if (!nextTokenIs(b, "<array type>", HASH, LBRACKET)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ARRAY_TYPE, "<array type>");
    r = arrayType_0(b, l + 1);
    r = r && consumeToken(b, LBRACKET);
    r = r && arrayType_2(b, l + 1);
    r = r && consumeToken(b, RBRACKET);
    r = r && typeDefinition_expression(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // [tagHead]
  private static boolean arrayType_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayType_0")) return false;
    tagHead(b, l + 1);
    return true;
  }

  // [QUESTION|DYNAMIC|expression]
  private static boolean arrayType_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayType_2")) return false;
    arrayType_2_0(b, l + 1);
    return true;
  }

  // QUESTION|DYNAMIC|expression
  private static boolean arrayType_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayType_2_0")) return false;
    boolean r;
    r = consumeToken(b, QUESTION);
    if (!r) r = consumeToken(b, DYNAMIC);
    if (!r) r = expression(b, l + 1, -1);
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
  //                        | BITWISE_AND_NOT_EQ
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
    if (!r) r = consumeToken(b, BITWISE_AND_NOT_EQ);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // [tagHead] leftHandExpressions assignmentOperator expressionsList
  public static boolean assignmentStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "assignmentStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ASSIGNMENT_STATEMENT, "<assignment statement>");
    r = assignmentStatement_0(b, l + 1);
    r = r && leftHandExpressions(b, l + 1);
    r = r && assignmentOperator(b, l + 1);
    r = r && expressionsList(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // [tagHead]
  private static boolean assignmentStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "assignmentStatement_0")) return false;
    tagHead(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // AT IDENTIFIER
  //                        | AT arguments
  public static boolean attributeStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "attributeStatement")) return false;
    if (!nextTokenIs(b, AT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = parseTokens(b, 0, AT, IDENTIFIER);
    if (!r) r = attributeStatement_1(b, l + 1);
    exit_section_(b, m, ATTRIBUTE_STATEMENT, r);
    return r;
  }

  // AT arguments
  private static boolean attributeStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "attributeStatement_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, AT);
    r = r && arguments(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // nil_literal
  //                   | string_literal
  //                   | numeric_literal
  //                   | boolean_literal
  //                   | tripleDashLiteral_expression
  public static boolean basic_literal(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "basic_literal")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BASIC_LITERAL, "<basic literal>");
    r = nil_literal(b, l + 1);
    if (!r) r = string_literal(b, l + 1);
    if (!r) r = numeric_literal(b, l + 1);
    if (!r) r = boolean_literal(b, l + 1);
    if (!r) r = tripleDashLiteral_expression(b, l + 1);
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
  //                    | PIPE
  //                    | TILDE
  //                    | ANDAND
  //                    | ANDNOT
  //                    | AND
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
  //                    | OR_ELSE
  //                    | IN
  //                    | NOT_IN
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
    if (!r) r = consumeToken(b, PIPE);
    if (!r) r = consumeToken(b, TILDE);
    if (!r) r = consumeToken(b, ANDAND);
    if (!r) r = consumeToken(b, ANDNOT);
    if (!r) r = consumeToken(b, AND);
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
    if (!r) r = consumeToken(b, OR_ELSE);
    if (!r) r = consumeToken(b, IN);
    if (!r) r = consumeToken(b, NOT_IN);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // BIT_SET LBRACKET expression [SEMICOLON primary_expression] RBRACKET
  public static boolean bitSetType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bitSetType")) return false;
    if (!nextTokenIs(b, BIT_SET)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIT_SET, LBRACKET);
    r = r && expression(b, l + 1, -1);
    r = r && bitSetType_3(b, l + 1);
    r = r && consumeToken(b, RBRACKET);
    exit_section_(b, m, BIT_SET_TYPE, r);
    return r;
  }

  // [SEMICOLON primary_expression]
  private static boolean bitSetType_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bitSetType_3")) return false;
    bitSetType_3_0(b, l + 1);
    return true;
  }

  // SEMICOLON primary_expression
  private static boolean bitSetType_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bitSetType_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SEMICOLON);
    r = r && primary_expression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // IDENTIFIER COLON COLON BIT_SET LBRACKET expression [SEMICOLON typeDefinition_expression] RBRACKET
  public static boolean bitsetDeclarationStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bitsetDeclarationStatement")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, IDENTIFIER, COLON, COLON, BIT_SET, LBRACKET);
    r = r && expression(b, l + 1, -1);
    r = r && bitsetDeclarationStatement_6(b, l + 1);
    r = r && consumeToken(b, RBRACKET);
    exit_section_(b, m, BITSET_DECLARATION_STATEMENT, r);
    return r;
  }

  // [SEMICOLON typeDefinition_expression]
  private static boolean bitsetDeclarationStatement_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bitsetDeclarationStatement_6")) return false;
    bitsetDeclarationStatement_6_0(b, l + 1);
    return true;
  }

  // SEMICOLON typeDefinition_expression
  private static boolean bitsetDeclarationStatement_6_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bitsetDeclarationStatement_6_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SEMICOLON);
    r = r && typeDefinition_expression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // tagStatement_expression* LBRACE statementList? RBRACE
  public static boolean block(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "block")) return false;
    if (!nextTokenIs(b, "<block>", HASH, LBRACE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BLOCK, "<block>");
    r = block_0(b, l + 1);
    r = r && consumeToken(b, LBRACE);
    r = r && block_2(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // tagStatement_expression*
  private static boolean block_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "block_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!tagStatement_expression(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "block_0", c)) break;
    }
    return true;
  }

  // statementList?
  private static boolean block_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "block_2")) return false;
    statementList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // [(tagStatement_expression eos*)|label] block
  public static boolean blockStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "blockStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BLOCK_STATEMENT, "<block statement>");
    r = blockStatement_0(b, l + 1);
    r = r && block(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // [(tagStatement_expression eos*)|label]
  private static boolean blockStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "blockStatement_0")) return false;
    blockStatement_0_0(b, l + 1);
    return true;
  }

  // (tagStatement_expression eos*)|label
  private static boolean blockStatement_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "blockStatement_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = blockStatement_0_0_0(b, l + 1);
    if (!r) r = label(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // tagStatement_expression eos*
  private static boolean blockStatement_0_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "blockStatement_0_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = tagStatement_expression(b, l + 1);
    r = r && blockStatement_0_0_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // eos*
  private static boolean blockStatement_0_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "blockStatement_0_0_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!eos(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "blockStatement_0_0_0_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // TRUE
  //                     | FALSE
  public static boolean boolean_literal(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "boolean_literal")) return false;
    if (!nextTokenIs(b, "<boolean literal>", FALSE, TRUE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BOOLEAN_LITERAL, "<boolean literal>");
    r = consumeToken(b, TRUE);
    if (!r) r = consumeToken(b, FALSE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // BREAK IDENTIFIER?
  public static boolean breakStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "breakStatement")) return false;
    if (!nextTokenIs(b, BREAK)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, BREAK);
    r = r && breakStatement_1(b, l + 1);
    exit_section_(b, m, BREAK_STATEMENT, r);
    return r;
  }

  // IDENTIFIER?
  private static boolean breakStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "breakStatement_1")) return false;
    consumeToken(b, IDENTIFIER);
    return true;
  }

  /* ********************************************************** */
  // caller LPAREN argumentList? RPAREN
  public static boolean call_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "call_expression")) return false;
    if (!nextTokenIs(b, "<call expression>", IDENTIFIER, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CALL_EXPRESSION, "<call expression>");
    r = caller(b, l + 1);
    r = r && consumeToken(b, LPAREN);
    r = r && call_expression_2(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // argumentList?
  private static boolean call_expression_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "call_expression_2")) return false;
    argumentList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // parenthesized_expression | identifier_expression
  public static boolean caller(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "caller")) return false;
    if (!nextTokenIs(b, "<caller>", IDENTIFIER, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CALLER, "<caller>");
    r = parenthesized_expression(b, l + 1);
    if (!r) r = identifier_expression(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // expression
  //                   | typeDefinition_expression
  //                   | DOT IDENTIFIER
  public static boolean caseExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "caseExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CASE_EXPRESSION, "<case expression>");
    r = expression(b, l + 1, -1);
    if (!r) r = typeDefinition_expression(b, l + 1);
    if (!r) r = parseTokens(b, 0, DOT, IDENTIFIER);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // mapLiteral
  //                       | matrixLiteral
  //                       | arrayLiteral
  //                       | sliceLiteral
  //                       | structLiteral
  //                       | literalValue
  public static boolean composite_literal(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "composite_literal")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, COMPOSITE_LITERAL, "<composite literal>");
    r = mapLiteral(b, l + 1);
    if (!r) r = matrixLiteral(b, l + 1);
    if (!r) r = arrayLiteral(b, l + 1);
    if (!r) r = sliceLiteral(b, l + 1);
    if (!r) r = structLiteral(b, l + 1);
    if (!r) r = literalValue(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // <<enterMode "BLOCK">> [ifInit SEMICOLON] expression <<exitMode "BLOCK">>
  static boolean condition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "condition")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = enterMode(b, l + 1, "BLOCK");
    r = r && condition_1(b, l + 1);
    r = r && expression(b, l + 1, -1);
    r = r && exitMode(b, l + 1, "BLOCK");
    exit_section_(b, m, null, r);
    return r;
  }

  // [ifInit SEMICOLON]
  private static boolean condition_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "condition_1")) return false;
    condition_1_0(b, l + 1);
    return true;
  }

  // ifInit SEMICOLON
  private static boolean condition_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "condition_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ifInit(b, l + 1);
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (attributeStatement eos*)* [USING] IDENTIFIER COLON typeDefinition_expression? COLON expression
  public static boolean constantInitializationStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constantInitializationStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, CONSTANT_INITIALIZATION_STATEMENT, "<constant initialization statement>");
    r = constantInitializationStatement_0(b, l + 1);
    r = r && constantInitializationStatement_1(b, l + 1);
    r = r && consumeTokens(b, 0, IDENTIFIER, COLON);
    r = r && constantInitializationStatement_4(b, l + 1);
    r = r && consumeToken(b, COLON);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (attributeStatement eos*)*
  private static boolean constantInitializationStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constantInitializationStatement_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!constantInitializationStatement_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "constantInitializationStatement_0", c)) break;
    }
    return true;
  }

  // attributeStatement eos*
  private static boolean constantInitializationStatement_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constantInitializationStatement_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = attributeStatement(b, l + 1);
    r = r && constantInitializationStatement_0_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // eos*
  private static boolean constantInitializationStatement_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constantInitializationStatement_0_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!eos(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "constantInitializationStatement_0_0_1", c)) break;
    }
    return true;
  }

  // [USING]
  private static boolean constantInitializationStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constantInitializationStatement_1")) return false;
    consumeToken(b, USING);
    return true;
  }

  // typeDefinition_expression?
  private static boolean constantInitializationStatement_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constantInitializationStatement_4")) return false;
    typeDefinition_expression(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // CONTINUE IDENTIFIER?
  public static boolean continueStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "continueStatement")) return false;
    if (!nextTokenIs(b, CONTINUE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, CONTINUE);
    r = r && continueStatement_1(b, l + 1);
    exit_section_(b, m, CONTINUE_STATEMENT, r);
    return r;
  }

  // IDENTIFIER?
  private static boolean continueStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "continueStatement_1")) return false;
    consumeToken(b, IDENTIFIER);
    return true;
  }

  /* ********************************************************** */
  // DEFER deferrableStatement
  public static boolean deferStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "deferStatement")) return false;
    if (!nextTokenIs(b, DEFER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DEFER);
    r = r && deferrableStatement(b, l + 1);
    exit_section_(b, m, DEFER_STATEMENT, r);
    return r;
  }

  /* ********************************************************** */
  // assignmentStatement
  //                                 | expression
  //                                 | ifStatement
  //                                 | forStatement
  //                                 | switchStatement
  //                                 | block
  static boolean deferrableStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "deferrableStatement")) return false;
    boolean r;
    r = assignmentStatement(b, l + 1);
    if (!r) r = expression(b, l + 1, -1);
    if (!r) r = ifStatement(b, l + 1);
    if (!r) r = forStatement(b, l + 1);
    if (!r) r = switchStatement(b, l + 1);
    if (!r) r = block(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // DO statement
  public static boolean doStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "doStatement")) return false;
    if (!nextTokenIs(b, DO)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DO);
    r = r && statement(b, l + 1);
    exit_section_(b, m, DO_STATEMENT, r);
    return r;
  }

  /* ********************************************************** */
  // ELSE statementBody
  public static boolean elseBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "elseBlock")) return false;
    if (!nextTokenIs(b, ELSE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ELSE);
    r = r && statementBody(b, l + 1);
    exit_section_(b, m, ELSE_BLOCK, r);
    return r;
  }

  /* ********************************************************** */
  // ELSE IF condition statementBody
  public static boolean elseIfBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "elseIfBlock")) return false;
    if (!nextTokenIs(b, ELSE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, ELSE, IF);
    r = r && condition(b, l + 1);
    r = r && statementBody(b, l + 1);
    exit_section_(b, m, ELSE_IF_BLOCK, r);
    return r;
  }

  /* ********************************************************** */
  // ELSE WHEN condition block
  public static boolean elseWhenBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "elseWhenBlock")) return false;
    if (!nextTokenIs(b, ELSE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, ELSE, WHEN);
    r = r && condition(b, l + 1);
    r = r && block(b, l + 1);
    exit_section_(b, m, ELSE_WHEN_BLOCK, r);
    return r;
  }

  /* ********************************************************** */
  // [IDENTIFIER EQ] expression
  static boolean enumAssignment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumAssignment")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = enumAssignment_0(b, l + 1);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [IDENTIFIER EQ]
  private static boolean enumAssignment_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumAssignment_0")) return false;
    parseTokens(b, 0, IDENTIFIER, EQ);
    return true;
  }

  /* ********************************************************** */
  // (attributeStatement eos*) USING? IDENTIFIER COLON COLON enumSpec
  public static boolean enumDeclarationStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumDeclarationStatement")) return false;
    if (!nextTokenIs(b, AT)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, ENUM_DECLARATION_STATEMENT, null);
    r = enumDeclarationStatement_0(b, l + 1);
    r = r && enumDeclarationStatement_1(b, l + 1);
    r = r && consumeTokens(b, 0, IDENTIFIER, COLON, COLON);
    r = r && enumSpec(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // attributeStatement eos*
  private static boolean enumDeclarationStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumDeclarationStatement_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = attributeStatement(b, l + 1);
    r = r && enumDeclarationStatement_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // eos*
  private static boolean enumDeclarationStatement_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumDeclarationStatement_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!eos(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "enumDeclarationStatement_0_1", c)) break;
    }
    return true;
  }

  // USING?
  private static boolean enumDeclarationStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumDeclarationStatement_1")) return false;
    consumeToken(b, USING);
    return true;
  }

  /* ********************************************************** */
  // ENUM [IDENTIFIER (DOT IDENTIFIER)*] LBRACE [enumAssignment (COMMA enumAssignment)* COMMA?] RBRACE
  public static boolean enumSpec(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumSpec")) return false;
    if (!nextTokenIs(b, ENUM)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ENUM);
    r = r && enumSpec_1(b, l + 1);
    r = r && consumeToken(b, LBRACE);
    r = r && enumSpec_3(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, m, ENUM_SPEC, r);
    return r;
  }

  // [IDENTIFIER (DOT IDENTIFIER)*]
  private static boolean enumSpec_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumSpec_1")) return false;
    enumSpec_1_0(b, l + 1);
    return true;
  }

  // IDENTIFIER (DOT IDENTIFIER)*
  private static boolean enumSpec_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumSpec_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    r = r && enumSpec_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (DOT IDENTIFIER)*
  private static boolean enumSpec_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumSpec_1_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!enumSpec_1_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "enumSpec_1_0_1", c)) break;
    }
    return true;
  }

  // DOT IDENTIFIER
  private static boolean enumSpec_1_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumSpec_1_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, DOT, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  // [enumAssignment (COMMA enumAssignment)* COMMA?]
  private static boolean enumSpec_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumSpec_3")) return false;
    enumSpec_3_0(b, l + 1);
    return true;
  }

  // enumAssignment (COMMA enumAssignment)* COMMA?
  private static boolean enumSpec_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumSpec_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = enumAssignment(b, l + 1);
    r = r && enumSpec_3_0_1(b, l + 1);
    r = r && enumSpec_3_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA enumAssignment)*
  private static boolean enumSpec_3_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumSpec_3_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!enumSpec_3_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "enumSpec_3_0_1", c)) break;
    }
    return true;
  }

  // COMMA enumAssignment
  private static boolean enumSpec_3_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumSpec_3_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && enumAssignment(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // COMMA?
  private static boolean enumSpec_3_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumSpec_3_0_2")) return false;
    consumeToken(b, COMMA);
    return true;
  }

  /* ********************************************************** */
  // SEMICOLON | <<eof>> | EOS_TOKEN | <<closingBracket>>
  public static boolean eos(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "eos")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, EOS, "<eos>");
    r = consumeToken(b, SEMICOLON);
    if (!r) r = eof(b, l + 1);
    if (!r) r = consumeToken(b, EOS_TOKEN);
    if (!r) r = closingBracket(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // expression
  public static boolean expressionStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expressionStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, EXPRESSION_STATEMENT, "<expression statement>");
    r = expression(b, l + 1, -1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // expression (COMMA expression)*
  public static boolean expressionsList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expressionsList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, EXPRESSIONS_LIST, "<expressions list>");
    r = expression(b, l + 1, -1);
    r = r && expressionsList_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (COMMA expression)*
  private static boolean expressionsList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expressionsList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!expressionsList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "expressionsList_1", c)) break;
    }
    return true;
  }

  // COMMA expression
  private static boolean expressionsList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expressionsList_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // FALLTHROUGH
  public static boolean fallthroughStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fallthroughStatement")) return false;
    if (!nextTokenIs(b, FALLTHROUGH)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, FALLTHROUGH);
    exit_section_(b, m, FALLTHROUGH_STATEMENT, r);
    return r;
  }

  /* ********************************************************** */
  // USING? tagHead? IDENTIFIER (COMMA IDENTIFIER)* COLON typeDefinition_expression [RAW_STRING_LITERAL]
  public static boolean fieldDeclarationStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldDeclarationStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FIELD_DECLARATION_STATEMENT, "<field declaration statement>");
    r = fieldDeclarationStatement_0(b, l + 1);
    r = r && fieldDeclarationStatement_1(b, l + 1);
    r = r && consumeToken(b, IDENTIFIER);
    r = r && fieldDeclarationStatement_3(b, l + 1);
    r = r && consumeToken(b, COLON);
    r = r && typeDefinition_expression(b, l + 1);
    r = r && fieldDeclarationStatement_6(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // USING?
  private static boolean fieldDeclarationStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldDeclarationStatement_0")) return false;
    consumeToken(b, USING);
    return true;
  }

  // tagHead?
  private static boolean fieldDeclarationStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldDeclarationStatement_1")) return false;
    tagHead(b, l + 1);
    return true;
  }

  // (COMMA IDENTIFIER)*
  private static boolean fieldDeclarationStatement_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldDeclarationStatement_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!fieldDeclarationStatement_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "fieldDeclarationStatement_3", c)) break;
    }
    return true;
  }

  // COMMA IDENTIFIER
  private static boolean fieldDeclarationStatement_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldDeclarationStatement_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, COMMA, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  // [RAW_STRING_LITERAL]
  private static boolean fieldDeclarationStatement_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldDeclarationStatement_6")) return false;
    consumeToken(b, RAW_STRING_LITERAL);
    return true;
  }

  /* ********************************************************** */
  // foreignImportDeclarationStatement
  //                 | importDeclarationStatement
  //                 | enumDeclarationStatement
  //                 | procedureDeclarationStatement
  //                 | constantInitializationStatement
  //                 | unionDeclarationStatement
  //                 | typeAliasDeclarationStatement
  //                 | structDeclarationStatement
  //                 | bitsetDeclarationStatement
  //                 | variableInitializationStatement
  //                 | variableDeclarationStatement
  //                 | procedureOverloadStatement
  //                 | foreignStatement
  //                 | whenStatement
  //                 | tagStatement_expression
  public static boolean fileScopeStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fileScopeStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, FILE_SCOPE_STATEMENT, "<file scope statement>");
    r = foreignImportDeclarationStatement(b, l + 1);
    if (!r) r = importDeclarationStatement(b, l + 1);
    if (!r) r = enumDeclarationStatement(b, l + 1);
    if (!r) r = procedureDeclarationStatement(b, l + 1);
    if (!r) r = constantInitializationStatement(b, l + 1);
    if (!r) r = unionDeclarationStatement(b, l + 1);
    if (!r) r = typeAliasDeclarationStatement(b, l + 1);
    if (!r) r = structDeclarationStatement(b, l + 1);
    if (!r) r = bitsetDeclarationStatement(b, l + 1);
    if (!r) r = variableInitializationStatement(b, l + 1);
    if (!r) r = variableDeclarationStatement(b, l + 1);
    if (!r) r = procedureOverloadStatement(b, l + 1);
    if (!r) r = foreignStatement(b, l + 1);
    if (!r) r = whenStatement(b, l + 1);
    if (!r) r = tagStatement_expression(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // assignmentStatement | call_expression
  static boolean forEndStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forEndStatement")) return false;
    boolean r;
    r = assignmentStatement(b, l + 1);
    if (!r) r = call_expression(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // AND? primary_expression
  static boolean forExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forExpression")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = forExpression_0(b, l + 1);
    r = r && primary_expression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // AND?
  private static boolean forExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forExpression_0")) return false;
    consumeToken(b, AND);
    return true;
  }

  /* ********************************************************** */
  // FOR <<enterMode "BLOCK">> (forIn|forTraditional) <<exitMode "BLOCK">>
  public static boolean forHead(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forHead")) return false;
    if (!nextTokenIs(b, FOR)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, FOR);
    r = r && enterMode(b, l + 1, "BLOCK");
    r = r && forHead_2(b, l + 1);
    r = r && exitMode(b, l + 1, "BLOCK");
    exit_section_(b, m, FOR_HEAD, r);
    return r;
  }

  // forIn|forTraditional
  private static boolean forHead_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forHead_2")) return false;
    boolean r;
    r = forIn(b, l + 1);
    if (!r) r = forTraditional(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // forExpression (COMMA forExpression)* IN expression
  static boolean forIn(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forIn")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = forExpression(b, l + 1);
    r = r && forIn_1(b, l + 1);
    r = r && consumeToken(b, IN);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA forExpression)*
  private static boolean forIn_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forIn_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!forIn_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "forIn_1", c)) break;
    }
    return true;
  }

  // COMMA forExpression
  private static boolean forIn_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forIn_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && forExpression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // variableInitializationStatement | assignmentStatement | call_expression
  static boolean forStartStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forStartStatement")) return false;
    boolean r;
    r = variableInitializationStatement(b, l + 1);
    if (!r) r = assignmentStatement(b, l + 1);
    if (!r) r = call_expression(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // label? tagStatement_expression? forHead statementBody
  public static boolean forStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FOR_STATEMENT, "<for statement>");
    r = forStatement_0(b, l + 1);
    r = r && forStatement_1(b, l + 1);
    r = r && forHead(b, l + 1);
    r = r && statementBody(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // label?
  private static boolean forStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forStatement_0")) return false;
    label(b, l + 1);
    return true;
  }

  // tagStatement_expression?
  private static boolean forStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forStatement_1")) return false;
    tagStatement_expression(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // (forStartStatement? SEMICOLON)? expression? (SEMICOLON forEndStatement?)?
  static boolean forTraditional(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forTraditional")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = forTraditional_0(b, l + 1);
    r = r && forTraditional_1(b, l + 1);
    r = r && forTraditional_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (forStartStatement? SEMICOLON)?
  private static boolean forTraditional_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forTraditional_0")) return false;
    forTraditional_0_0(b, l + 1);
    return true;
  }

  // forStartStatement? SEMICOLON
  private static boolean forTraditional_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forTraditional_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = forTraditional_0_0_0(b, l + 1);
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, m, null, r);
    return r;
  }

  // forStartStatement?
  private static boolean forTraditional_0_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forTraditional_0_0_0")) return false;
    forStartStatement(b, l + 1);
    return true;
  }

  // expression?
  private static boolean forTraditional_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forTraditional_1")) return false;
    expression(b, l + 1, -1);
    return true;
  }

  // (SEMICOLON forEndStatement?)?
  private static boolean forTraditional_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forTraditional_2")) return false;
    forTraditional_2_0(b, l + 1);
    return true;
  }

  // SEMICOLON forEndStatement?
  private static boolean forTraditional_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forTraditional_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SEMICOLON);
    r = r && forTraditional_2_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // forEndStatement?
  private static boolean forTraditional_2_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forTraditional_2_0_1")) return false;
    forEndStatement(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // LBRACE foreignStatementList? RBRACE
  public static boolean foreignBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignBlock")) return false;
    if (!nextTokenIs(b, LBRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LBRACE);
    r = r && foreignBlock_1(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, m, FOREIGN_BLOCK, r);
    return r;
  }

  // foreignStatementList?
  private static boolean foreignBlock_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignBlock_1")) return false;
    foreignStatementList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // variableDeclarationStatement
  //                          |foreignProcedureDeclarationStatement
  //                          |whenStatement
  public static boolean foreignBlockStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignBlockStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, FOREIGN_BLOCK_STATEMENT, "<foreign block statement>");
    r = variableDeclarationStatement(b, l + 1);
    if (!r) r = foreignProcedureDeclarationStatement(b, l + 1);
    if (!r) r = whenStatement(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // (attributeStatement eos*)* FOREIGN IMPORT ( IDENTIFIER? DQ_STRING_LITERAL | IDENTIFIER? LBRACE DQ_STRING_LITERAL (COMMA DQ_STRING_LITERAL)* COMMA? RBRACE)
  public static boolean foreignImportDeclarationStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignImportDeclarationStatement")) return false;
    if (!nextTokenIs(b, "<foreign import declaration statement>", AT, FOREIGN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, FOREIGN_IMPORT_DECLARATION_STATEMENT, "<foreign import declaration statement>");
    r = foreignImportDeclarationStatement_0(b, l + 1);
    r = r && consumeTokens(b, 0, FOREIGN, IMPORT);
    r = r && foreignImportDeclarationStatement_3(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (attributeStatement eos*)*
  private static boolean foreignImportDeclarationStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignImportDeclarationStatement_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!foreignImportDeclarationStatement_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "foreignImportDeclarationStatement_0", c)) break;
    }
    return true;
  }

  // attributeStatement eos*
  private static boolean foreignImportDeclarationStatement_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignImportDeclarationStatement_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = attributeStatement(b, l + 1);
    r = r && foreignImportDeclarationStatement_0_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // eos*
  private static boolean foreignImportDeclarationStatement_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignImportDeclarationStatement_0_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!eos(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "foreignImportDeclarationStatement_0_0_1", c)) break;
    }
    return true;
  }

  // IDENTIFIER? DQ_STRING_LITERAL | IDENTIFIER? LBRACE DQ_STRING_LITERAL (COMMA DQ_STRING_LITERAL)* COMMA? RBRACE
  private static boolean foreignImportDeclarationStatement_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignImportDeclarationStatement_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = foreignImportDeclarationStatement_3_0(b, l + 1);
    if (!r) r = foreignImportDeclarationStatement_3_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // IDENTIFIER? DQ_STRING_LITERAL
  private static boolean foreignImportDeclarationStatement_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignImportDeclarationStatement_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = foreignImportDeclarationStatement_3_0_0(b, l + 1);
    r = r && consumeToken(b, DQ_STRING_LITERAL);
    exit_section_(b, m, null, r);
    return r;
  }

  // IDENTIFIER?
  private static boolean foreignImportDeclarationStatement_3_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignImportDeclarationStatement_3_0_0")) return false;
    consumeToken(b, IDENTIFIER);
    return true;
  }

  // IDENTIFIER? LBRACE DQ_STRING_LITERAL (COMMA DQ_STRING_LITERAL)* COMMA? RBRACE
  private static boolean foreignImportDeclarationStatement_3_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignImportDeclarationStatement_3_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = foreignImportDeclarationStatement_3_1_0(b, l + 1);
    r = r && consumeTokens(b, 0, LBRACE, DQ_STRING_LITERAL);
    r = r && foreignImportDeclarationStatement_3_1_3(b, l + 1);
    r = r && foreignImportDeclarationStatement_3_1_4(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, m, null, r);
    return r;
  }

  // IDENTIFIER?
  private static boolean foreignImportDeclarationStatement_3_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignImportDeclarationStatement_3_1_0")) return false;
    consumeToken(b, IDENTIFIER);
    return true;
  }

  // (COMMA DQ_STRING_LITERAL)*
  private static boolean foreignImportDeclarationStatement_3_1_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignImportDeclarationStatement_3_1_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!foreignImportDeclarationStatement_3_1_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "foreignImportDeclarationStatement_3_1_3", c)) break;
    }
    return true;
  }

  // COMMA DQ_STRING_LITERAL
  private static boolean foreignImportDeclarationStatement_3_1_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignImportDeclarationStatement_3_1_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, COMMA, DQ_STRING_LITERAL);
    exit_section_(b, m, null, r);
    return r;
  }

  // COMMA?
  private static boolean foreignImportDeclarationStatement_3_1_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignImportDeclarationStatement_3_1_4")) return false;
    consumeToken(b, COMMA);
    return true;
  }

  /* ********************************************************** */
  // (attributeStatement eos*)* IDENTIFIER COLON COLON PROC string_literal? LPAREN parameterList* RPAREN [ARROW returnType] TRIPLE_DASH
  public static boolean foreignProcedureDeclarationStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignProcedureDeclarationStatement")) return false;
    if (!nextTokenIs(b, "<foreign procedure declaration statement>", AT, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, FOREIGN_PROCEDURE_DECLARATION_STATEMENT, "<foreign procedure declaration statement>");
    r = foreignProcedureDeclarationStatement_0(b, l + 1);
    r = r && consumeTokens(b, 0, IDENTIFIER, COLON, COLON, PROC);
    r = r && foreignProcedureDeclarationStatement_5(b, l + 1);
    r = r && consumeToken(b, LPAREN);
    r = r && foreignProcedureDeclarationStatement_7(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    r = r && foreignProcedureDeclarationStatement_9(b, l + 1);
    r = r && consumeToken(b, TRIPLE_DASH);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (attributeStatement eos*)*
  private static boolean foreignProcedureDeclarationStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignProcedureDeclarationStatement_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!foreignProcedureDeclarationStatement_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "foreignProcedureDeclarationStatement_0", c)) break;
    }
    return true;
  }

  // attributeStatement eos*
  private static boolean foreignProcedureDeclarationStatement_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignProcedureDeclarationStatement_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = attributeStatement(b, l + 1);
    r = r && foreignProcedureDeclarationStatement_0_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // eos*
  private static boolean foreignProcedureDeclarationStatement_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignProcedureDeclarationStatement_0_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!eos(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "foreignProcedureDeclarationStatement_0_0_1", c)) break;
    }
    return true;
  }

  // string_literal?
  private static boolean foreignProcedureDeclarationStatement_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignProcedureDeclarationStatement_5")) return false;
    string_literal(b, l + 1);
    return true;
  }

  // parameterList*
  private static boolean foreignProcedureDeclarationStatement_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignProcedureDeclarationStatement_7")) return false;
    while (true) {
      int c = current_position_(b);
      if (!parameterList(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "foreignProcedureDeclarationStatement_7", c)) break;
    }
    return true;
  }

  // [ARROW returnType]
  private static boolean foreignProcedureDeclarationStatement_9(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignProcedureDeclarationStatement_9")) return false;
    foreignProcedureDeclarationStatement_9_0(b, l + 1);
    return true;
  }

  // ARROW returnType
  private static boolean foreignProcedureDeclarationStatement_9_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignProcedureDeclarationStatement_9_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ARROW);
    r = r && returnType(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (attributeStatement eos)* FOREIGN IDENTIFIER? foreignBlock
  public static boolean foreignStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignStatement")) return false;
    if (!nextTokenIs(b, "<foreign statement>", AT, FOREIGN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FOREIGN_STATEMENT, "<foreign statement>");
    r = foreignStatement_0(b, l + 1);
    r = r && consumeToken(b, FOREIGN);
    r = r && foreignStatement_2(b, l + 1);
    r = r && foreignBlock(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (attributeStatement eos)*
  private static boolean foreignStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignStatement_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!foreignStatement_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "foreignStatement_0", c)) break;
    }
    return true;
  }

  // attributeStatement eos
  private static boolean foreignStatement_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignStatement_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = attributeStatement(b, l + 1);
    r = r && eos(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // IDENTIFIER?
  private static boolean foreignStatement_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignStatement_2")) return false;
    consumeToken(b, IDENTIFIER);
    return true;
  }

  /* ********************************************************** */
  // ((SEMICOLON | NEW_LINE | EOS_TOKEN | <<closingBracket>>)? foreignBlockStatement eos)+
  public static boolean foreignStatementList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignStatementList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FOREIGN_STATEMENT_LIST, "<foreign statement list>");
    r = foreignStatementList_0(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!foreignStatementList_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "foreignStatementList", c)) break;
    }
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (SEMICOLON | NEW_LINE | EOS_TOKEN | <<closingBracket>>)? foreignBlockStatement eos
  private static boolean foreignStatementList_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignStatementList_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = foreignStatementList_0_0(b, l + 1);
    r = r && foreignBlockStatement(b, l + 1);
    r = r && eos(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (SEMICOLON | NEW_LINE | EOS_TOKEN | <<closingBracket>>)?
  private static boolean foreignStatementList_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignStatementList_0_0")) return false;
    foreignStatementList_0_0_0(b, l + 1);
    return true;
  }

  // SEMICOLON | NEW_LINE | EOS_TOKEN | <<closingBracket>>
  private static boolean foreignStatementList_0_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignStatementList_0_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SEMICOLON);
    if (!r) r = consumeToken(b, NEW_LINE);
    if (!r) r = consumeToken(b, EOS_TOKEN);
    if (!r) r = closingBracket(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // IDENTIFIER (COMMA IDENTIFIER)*
  public static boolean identifierList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "identifierList")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    r = r && identifierList_1(b, l + 1);
    exit_section_(b, m, IDENTIFIER_LIST, r);
    return r;
  }

  // (COMMA IDENTIFIER)*
  private static boolean identifierList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "identifierList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!identifierList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "identifierList_1", c)) break;
    }
    return true;
  }

  // COMMA IDENTIFIER
  private static boolean identifierList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "identifierList_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, COMMA, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // assignmentStatement|variableInitializationStatement|variableDeclarationStatement
  static boolean ifInit(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifInit")) return false;
    boolean r;
    r = assignmentStatement(b, l + 1);
    if (!r) r = variableInitializationStatement(b, l + 1);
    if (!r) r = variableDeclarationStatement(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // [label] [tagHead] IF condition statementBody (eos* elseIfBlock)* (eos* elseBlock)?
  public static boolean ifStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, IF_STATEMENT, "<if statement>");
    r = ifStatement_0(b, l + 1);
    r = r && ifStatement_1(b, l + 1);
    r = r && consumeToken(b, IF);
    r = r && condition(b, l + 1);
    r = r && statementBody(b, l + 1);
    r = r && ifStatement_5(b, l + 1);
    r = r && ifStatement_6(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // [label]
  private static boolean ifStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifStatement_0")) return false;
    label(b, l + 1);
    return true;
  }

  // [tagHead]
  private static boolean ifStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifStatement_1")) return false;
    tagHead(b, l + 1);
    return true;
  }

  // (eos* elseIfBlock)*
  private static boolean ifStatement_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifStatement_5")) return false;
    while (true) {
      int c = current_position_(b);
      if (!ifStatement_5_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ifStatement_5", c)) break;
    }
    return true;
  }

  // eos* elseIfBlock
  private static boolean ifStatement_5_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifStatement_5_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ifStatement_5_0_0(b, l + 1);
    r = r && elseIfBlock(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // eos*
  private static boolean ifStatement_5_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifStatement_5_0_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!eos(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ifStatement_5_0_0", c)) break;
    }
    return true;
  }

  // (eos* elseBlock)?
  private static boolean ifStatement_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifStatement_6")) return false;
    ifStatement_6_0(b, l + 1);
    return true;
  }

  // eos* elseBlock
  private static boolean ifStatement_6_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifStatement_6_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ifStatement_6_0_0(b, l + 1);
    r = r && elseBlock(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // eos*
  private static boolean ifStatement_6_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifStatement_6_0_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!eos(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ifStatement_6_0_0", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // IMPORT IDENTIFIER? DQ_STRING_LITERAL
  public static boolean importDeclarationStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "importDeclarationStatement")) return false;
    if (!nextTokenIs(b, IMPORT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IMPORT);
    r = r && importDeclarationStatement_1(b, l + 1);
    r = r && consumeToken(b, DQ_STRING_LITERAL);
    exit_section_(b, m, IMPORT_DECLARATION_STATEMENT, r);
    return r;
  }

  // IDENTIFIER?
  private static boolean importDeclarationStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "importDeclarationStatement_1")) return false;
    consumeToken(b, IDENTIFIER);
    return true;
  }

  /* ********************************************************** */
  // LBRACKET expression [COMMA expression] RBRACKET
  static boolean index(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "index")) return false;
    if (!nextTokenIs(b, LBRACKET)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LBRACKET);
    r = r && expression(b, l + 1, -1);
    r = r && index_2(b, l + 1);
    r = r && consumeToken(b, RBRACKET);
    exit_section_(b, m, null, r);
    return r;
  }

  // [COMMA expression]
  private static boolean index_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "index_2")) return false;
    index_2_0(b, l + 1);
    return true;
  }

  // COMMA expression
  private static boolean index_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "index_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // INTEGER_DEC_LITERAL
  //               | INTEGER_HEX_LITERAL
  //               | INTEGER_OCT_LITERAL
  //               | INTEGER_BIN_LITERAL
  //               | SQ_STRING_LITERAL
  //               | DOT IDENTIFIER
  public static boolean intLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "intLiteral")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, INT_LITERAL, "<int literal>");
    r = consumeToken(b, INTEGER_DEC_LITERAL);
    if (!r) r = consumeToken(b, INTEGER_HEX_LITERAL);
    if (!r) r = consumeToken(b, INTEGER_OCT_LITERAL);
    if (!r) r = consumeToken(b, INTEGER_BIN_LITERAL);
    if (!r) r = consumeToken(b, SQ_STRING_LITERAL);
    if (!r) r = parseTokens(b, 0, DOT, IDENTIFIER);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // IDENTIFIER COLON
  public static boolean label(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "label")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, IDENTIFIER, COLON);
    exit_section_(b, m, LABEL, r);
    return r;
  }

  /* ********************************************************** */
  // primary_expression
  public static boolean leftHandExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "leftHandExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LEFT_HAND_EXPRESSION, "<left hand expression>");
    r = primary_expression(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // leftHandExpression (COMMA leftHandExpression)*
  public static boolean leftHandExpressions(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "leftHandExpressions")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LEFT_HAND_EXPRESSIONS, "<left hand expressions>");
    r = leftHandExpression(b, l + 1);
    r = r && leftHandExpressions_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (COMMA leftHandExpression)*
  private static boolean leftHandExpressions_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "leftHandExpressions_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!leftHandExpressions_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "leftHandExpressions_1", c)) break;
    }
    return true;
  }

  // COMMA leftHandExpression
  private static boolean leftHandExpressions_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "leftHandExpressions_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && leftHandExpression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // basic_literal | composite_literal
  public static boolean literal(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "literal")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LITERAL, "<literal>");
    r = basic_literal(b, l + 1);
    if (!r) r = composite_literal(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // (<<isModeOn "PAR">> | <<isModeOff "BLOCK">>)  LBRACE [[expression EQ] expression (COMMA [expression EQ] expression)* COMMA?] RBRACE
  public static boolean literalValue(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "literalValue")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LITERAL_VALUE, "<literal value>");
    r = literalValue_0(b, l + 1);
    r = r && consumeToken(b, LBRACE);
    r = r && literalValue_2(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // <<isModeOn "PAR">> | <<isModeOff "BLOCK">>
  private static boolean literalValue_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "literalValue_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = isModeOn(b, l + 1, "PAR");
    if (!r) r = isModeOff(b, l + 1, "BLOCK");
    exit_section_(b, m, null, r);
    return r;
  }

  // [[expression EQ] expression (COMMA [expression EQ] expression)* COMMA?]
  private static boolean literalValue_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "literalValue_2")) return false;
    literalValue_2_0(b, l + 1);
    return true;
  }

  // [expression EQ] expression (COMMA [expression EQ] expression)* COMMA?
  private static boolean literalValue_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "literalValue_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = literalValue_2_0_0(b, l + 1);
    r = r && expression(b, l + 1, -1);
    r = r && literalValue_2_0_2(b, l + 1);
    r = r && literalValue_2_0_3(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [expression EQ]
  private static boolean literalValue_2_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "literalValue_2_0_0")) return false;
    literalValue_2_0_0_0(b, l + 1);
    return true;
  }

  // expression EQ
  private static boolean literalValue_2_0_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "literalValue_2_0_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = expression(b, l + 1, -1);
    r = r && consumeToken(b, EQ);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA [expression EQ] expression)*
  private static boolean literalValue_2_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "literalValue_2_0_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!literalValue_2_0_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "literalValue_2_0_2", c)) break;
    }
    return true;
  }

  // COMMA [expression EQ] expression
  private static boolean literalValue_2_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "literalValue_2_0_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && literalValue_2_0_2_0_1(b, l + 1);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [expression EQ]
  private static boolean literalValue_2_0_2_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "literalValue_2_0_2_0_1")) return false;
    literalValue_2_0_2_0_1_0(b, l + 1);
    return true;
  }

  // expression EQ
  private static boolean literalValue_2_0_2_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "literalValue_2_0_2_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = expression(b, l + 1, -1);
    r = r && consumeToken(b, EQ);
    exit_section_(b, m, null, r);
    return r;
  }

  // COMMA?
  private static boolean literalValue_2_0_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "literalValue_2_0_3")) return false;
    consumeToken(b, COMMA);
    return true;
  }

  /* ********************************************************** */
  // expression EQ expression
  public static boolean mapInitAssigment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mapInitAssigment")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, MAP_INIT_ASSIGMENT, "<map init assigment>");
    r = expression(b, l + 1, -1);
    r = r && consumeToken(b, EQ);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // mapInitAssigment (COMMA mapInitAssigment)* COMMA?
  public static boolean mapInitAssignments(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mapInitAssignments")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, MAP_INIT_ASSIGNMENTS, "<map init assignments>");
    r = mapInitAssigment(b, l + 1);
    r = r && mapInitAssignments_1(b, l + 1);
    r = r && mapInitAssignments_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (COMMA mapInitAssigment)*
  private static boolean mapInitAssignments_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mapInitAssignments_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!mapInitAssignments_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "mapInitAssignments_1", c)) break;
    }
    return true;
  }

  // COMMA mapInitAssigment
  private static boolean mapInitAssignments_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mapInitAssignments_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && mapInitAssigment(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // COMMA?
  private static boolean mapInitAssignments_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mapInitAssignments_2")) return false;
    consumeToken(b, COMMA);
    return true;
  }

  /* ********************************************************** */
  // mapType LBRACE [mapInitAssignments] RBRACE
  public static boolean mapLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mapLiteral")) return false;
    if (!nextTokenIs(b, MAP)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = mapType(b, l + 1);
    r = r && consumeToken(b, LBRACE);
    r = r && mapLiteral_2(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, m, MAP_LITERAL, r);
    return r;
  }

  // [mapInitAssignments]
  private static boolean mapLiteral_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mapLiteral_2")) return false;
    mapInitAssignments(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // MAP LBRACKET typeDefinition_expression RBRACKET typeDefinition_expression
  public static boolean mapType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mapType")) return false;
    if (!nextTokenIs(b, MAP)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, MAP, LBRACKET);
    r = r && typeDefinition_expression(b, l + 1);
    r = r && consumeToken(b, RBRACKET);
    r = r && typeDefinition_expression(b, l + 1);
    exit_section_(b, m, MAP_TYPE, r);
    return r;
  }

  /* ********************************************************** */
  // MATRIX LBRACKET INTEGER_DEC_LITERAL COMMA INTEGER_DEC_LITERAL RBRACKET LBRACE expression (COMMA expression)* RBRACE
  public static boolean matrixLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "matrixLiteral")) return false;
    if (!nextTokenIs(b, MATRIX)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, MATRIX, LBRACKET, INTEGER_DEC_LITERAL, COMMA, INTEGER_DEC_LITERAL, RBRACKET, LBRACE);
    r = r && expression(b, l + 1, -1);
    r = r && matrixLiteral_8(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, m, MATRIX_LITERAL, r);
    return r;
  }

  // (COMMA expression)*
  private static boolean matrixLiteral_8(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "matrixLiteral_8")) return false;
    while (true) {
      int c = current_position_(b);
      if (!matrixLiteral_8_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "matrixLiteral_8", c)) break;
    }
    return true;
  }

  // COMMA expression
  private static boolean matrixLiteral_8_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "matrixLiteral_8_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // MATRIX LBRACKET expression COMMA expression RBRACKET typeDefinition_expression
  public static boolean matrixType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "matrixType")) return false;
    if (!nextTokenIs(b, MATRIX)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, MATRIX, LBRACKET);
    r = r && expression(b, l + 1, -1);
    r = r && consumeToken(b, COMMA);
    r = r && expression(b, l + 1, -1);
    r = r && consumeToken(b, RBRACKET);
    r = r && typeDefinition_expression(b, l + 1);
    exit_section_(b, m, MATRIX_TYPE, r);
    return r;
  }

  /* ********************************************************** */
  // LBRACKET CARET RBRACKET type
  public static boolean multiPointerType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "multiPointerType")) return false;
    if (!nextTokenIs(b, LBRACKET)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, LBRACKET, CARET, RBRACKET);
    r = r && type(b, l + 1);
    exit_section_(b, m, MULTI_POINTER_TYPE, r);
    return r;
  }

  /* ********************************************************** */
  // IDENTIFIER EQ expression
  public static boolean namedArgument(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namedArgument")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, IDENTIFIER, EQ);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, NAMED_ARGUMENT, r);
    return r;
  }

  /* ********************************************************** */
  // NIL
  public static boolean nil_literal(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "nil_literal")) return false;
    if (!nextTokenIs(b, NIL)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NIL);
    exit_section_(b, m, NIL_LITERAL, r);
    return r;
  }

  /* ********************************************************** */
  // INTEGER_DEC_LITERAL
  //                     | INTEGER_HEX_LITERAL
  //                     | INTEGER_OCT_LITERAL
  //                     | INTEGER_BIN_LITERAL
  //                     | FLOAT_DEC_LITERAL
  //                     | COMPLEX_INTEGER_DEC_LITERAL
  //                     | COMPLEX_FLOAT_LITERAL
  public static boolean numeric_literal(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "numeric_literal")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, NUMERIC_LITERAL, "<numeric literal>");
    r = consumeToken(b, INTEGER_DEC_LITERAL);
    if (!r) r = consumeToken(b, INTEGER_HEX_LITERAL);
    if (!r) r = consumeToken(b, INTEGER_OCT_LITERAL);
    if (!r) r = consumeToken(b, INTEGER_BIN_LITERAL);
    if (!r) r = consumeToken(b, FLOAT_DEC_LITERAL);
    if (!r) r = consumeToken(b, COMPLEX_INTEGER_DEC_LITERAL);
    if (!r) r = consumeToken(b, COMPLEX_FLOAT_LITERAL);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // packageClause eos (importDeclarationStatement eos)* (fileScopeStatement eos)* <<eof>>
  static boolean odinFile(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "odinFile")) return false;
    if (!nextTokenIs(b, PACKAGE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = packageClause(b, l + 1);
    r = r && eos(b, l + 1);
    r = r && odinFile_2(b, l + 1);
    r = r && odinFile_3(b, l + 1);
    r = r && eof(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (importDeclarationStatement eos)*
  private static boolean odinFile_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "odinFile_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!odinFile_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "odinFile_2", c)) break;
    }
    return true;
  }

  // importDeclarationStatement eos
  private static boolean odinFile_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "odinFile_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = importDeclarationStatement(b, l + 1);
    r = r && eos(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (fileScopeStatement eos)*
  private static boolean odinFile_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "odinFile_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!odinFile_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "odinFile_3", c)) break;
    }
    return true;
  }

  // fileScopeStatement eos
  private static boolean odinFile_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "odinFile_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = fileScopeStatement(b, l + 1);
    r = r && eos(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // PACKAGE IDENTIFIER
  public static boolean packageClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "packageClause")) return false;
    if (!nextTokenIs(b, PACKAGE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, PACKAGE, IDENTIFIER);
    exit_section_(b, m, PACKAGE_CLAUSE, r);
    return r;
  }

  /* ********************************************************** */
  // tagStatement_expression? (
  //               variadicParameter
  //               | parameterInitialization
  //               | parameterDeclarationStatement
  //               | parameterType
  //               )
  public static boolean parameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PARAMETER, "<parameter>");
    r = parameter_0(b, l + 1);
    r = r && parameter_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // tagStatement_expression?
  private static boolean parameter_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_0")) return false;
    tagStatement_expression(b, l + 1);
    return true;
  }

  // variadicParameter
  //               | parameterInitialization
  //               | parameterDeclarationStatement
  //               | parameterType
  private static boolean parameter_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_1")) return false;
    boolean r;
    r = variadicParameter(b, l + 1);
    if (!r) r = parameterInitialization(b, l + 1);
    if (!r) r = parameterDeclarationStatement(b, l + 1);
    if (!r) r = parameterType(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // [USING|DOLLAR] tagStatement_expression? IDENTIFIER (COMMA DOLLAR? IDENTIFIER)* COLON parameterType parameterTypeSpecialization?
  public static boolean parameterDeclarationStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterDeclarationStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PARAMETER_DECLARATION_STATEMENT, "<parameter declaration statement>");
    r = parameterDeclarationStatement_0(b, l + 1);
    r = r && parameterDeclarationStatement_1(b, l + 1);
    r = r && consumeToken(b, IDENTIFIER);
    r = r && parameterDeclarationStatement_3(b, l + 1);
    r = r && consumeToken(b, COLON);
    r = r && parameterType(b, l + 1);
    r = r && parameterDeclarationStatement_6(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // [USING|DOLLAR]
  private static boolean parameterDeclarationStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterDeclarationStatement_0")) return false;
    parameterDeclarationStatement_0_0(b, l + 1);
    return true;
  }

  // USING|DOLLAR
  private static boolean parameterDeclarationStatement_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterDeclarationStatement_0_0")) return false;
    boolean r;
    r = consumeToken(b, USING);
    if (!r) r = consumeToken(b, DOLLAR);
    return r;
  }

  // tagStatement_expression?
  private static boolean parameterDeclarationStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterDeclarationStatement_1")) return false;
    tagStatement_expression(b, l + 1);
    return true;
  }

  // (COMMA DOLLAR? IDENTIFIER)*
  private static boolean parameterDeclarationStatement_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterDeclarationStatement_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!parameterDeclarationStatement_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "parameterDeclarationStatement_3", c)) break;
    }
    return true;
  }

  // COMMA DOLLAR? IDENTIFIER
  private static boolean parameterDeclarationStatement_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterDeclarationStatement_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && parameterDeclarationStatement_3_0_1(b, l + 1);
    r = r && consumeToken(b, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  // DOLLAR?
  private static boolean parameterDeclarationStatement_3_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterDeclarationStatement_3_0_1")) return false;
    consumeToken(b, DOLLAR);
    return true;
  }

  // parameterTypeSpecialization?
  private static boolean parameterDeclarationStatement_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterDeclarationStatement_6")) return false;
    parameterTypeSpecialization(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // IDENTIFIER COLON typeDefinition_expression? EQ expression
  static boolean parameterInitialization(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterInitialization")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, IDENTIFIER, COLON);
    r = r && parameterInitialization_2(b, l + 1);
    r = r && consumeToken(b, EQ);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  // typeDefinition_expression?
  private static boolean parameterInitialization_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterInitialization_2")) return false;
    typeDefinition_expression(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // parameter (COMMA parameter)* COMMA?
  public static boolean parameterList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PARAMETER_LIST, "<parameter list>");
    r = parameter(b, l + 1);
    r = r && parameterList_1(b, l + 1);
    r = r && parameterList_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
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

  // COMMA?
  private static boolean parameterList_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterList_2")) return false;
    consumeToken(b, COMMA);
    return true;
  }

  /* ********************************************************** */
  // RANGE? typeDefinition_expression
  static boolean parameterType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterType")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = parameterType_0(b, l + 1);
    r = r && typeDefinition_expression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // RANGE?
  private static boolean parameterType_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterType_0")) return false;
    consumeToken(b, RANGE);
    return true;
  }

  /* ********************************************************** */
  // DIV expression
  public static boolean parameterTypeSpecialization(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterTypeSpecialization")) return false;
    if (!nextTokenIs(b, DIV)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DIV);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, PARAMETER_TYPE_SPECIALIZATION, r);
    return r;
  }

  /* ********************************************************** */
  // LPAREN <<enterMode "PAR">> expression <<exitMode "PAR">> RPAREN
  public static boolean parenthesized_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parenthesized_expression")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && enterMode(b, l + 1, "PAR");
    r = r && expression(b, l + 1, -1);
    r = r && exitMode(b, l + 1, "PAR");
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, PARENTHESIZED_EXPRESSION, r);
    return r;
  }

  /* ********************************************************** */
  // CARET type
  public static boolean pointerType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "pointerType")) return false;
    if (!nextTokenIs(b, CARET)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, CARET);
    r = r && type(b, l + 1);
    exit_section_(b, m, POINTER_TYPE, r);
    return r;
  }

  /* ********************************************************** */
  // block|TRIPLE_DASH
  public static boolean procedureBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureBody")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PROCEDURE_BODY, "<procedure body>");
    r = block(b, l + 1);
    if (!r) r = consumeToken(b, TRIPLE_DASH);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // (attributeStatement eos*)* IDENTIFIER COLON COLON procedureType procedureBody
  public static boolean procedureDeclarationStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureDeclarationStatement")) return false;
    if (!nextTokenIs(b, "<procedure declaration statement>", AT, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, PROCEDURE_DECLARATION_STATEMENT, "<procedure declaration statement>");
    r = procedureDeclarationStatement_0(b, l + 1);
    r = r && consumeTokens(b, 0, IDENTIFIER, COLON, COLON);
    r = r && procedureType(b, l + 1);
    r = r && procedureBody(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (attributeStatement eos*)*
  private static boolean procedureDeclarationStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureDeclarationStatement_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!procedureDeclarationStatement_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "procedureDeclarationStatement_0", c)) break;
    }
    return true;
  }

  // attributeStatement eos*
  private static boolean procedureDeclarationStatement_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureDeclarationStatement_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = attributeStatement(b, l + 1);
    r = r && procedureDeclarationStatement_0_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // eos*
  private static boolean procedureDeclarationStatement_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureDeclarationStatement_0_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!eos(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "procedureDeclarationStatement_0_0_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // (attributeStatement eos*)* IDENTIFIER COLON COLON PROC LBRACE IDENTIFIER (COMMA IDENTIFIER)* COMMA? RBRACE
  public static boolean procedureOverloadStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureOverloadStatement")) return false;
    if (!nextTokenIs(b, "<procedure overload statement>", AT, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, PROCEDURE_OVERLOAD_STATEMENT, "<procedure overload statement>");
    r = procedureOverloadStatement_0(b, l + 1);
    r = r && consumeTokens(b, 0, IDENTIFIER, COLON, COLON, PROC, LBRACE, IDENTIFIER);
    r = r && procedureOverloadStatement_7(b, l + 1);
    r = r && procedureOverloadStatement_8(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (attributeStatement eos*)*
  private static boolean procedureOverloadStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureOverloadStatement_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!procedureOverloadStatement_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "procedureOverloadStatement_0", c)) break;
    }
    return true;
  }

  // attributeStatement eos*
  private static boolean procedureOverloadStatement_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureOverloadStatement_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = attributeStatement(b, l + 1);
    r = r && procedureOverloadStatement_0_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // eos*
  private static boolean procedureOverloadStatement_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureOverloadStatement_0_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!eos(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "procedureOverloadStatement_0_0_1", c)) break;
    }
    return true;
  }

  // (COMMA IDENTIFIER)*
  private static boolean procedureOverloadStatement_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureOverloadStatement_7")) return false;
    while (true) {
      int c = current_position_(b);
      if (!procedureOverloadStatement_7_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "procedureOverloadStatement_7", c)) break;
    }
    return true;
  }

  // COMMA IDENTIFIER
  private static boolean procedureOverloadStatement_7_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureOverloadStatement_7_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, COMMA, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  // COMMA?
  private static boolean procedureOverloadStatement_8(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureOverloadStatement_8")) return false;
    consumeToken(b, COMMA);
    return true;
  }

  /* ********************************************************** */
  // tagStatement_expression? PROC string_literal? LPAREN parameterList* RPAREN [ARROW returnType] <<enterMode "BLOCK">> [eos* whereClause eos*] <<exitMode "BLOCK">>
  public static boolean procedureType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureType")) return false;
    if (!nextTokenIs(b, "<procedure type>", HASH, PROC)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PROCEDURE_TYPE, "<procedure type>");
    r = procedureType_0(b, l + 1);
    r = r && consumeToken(b, PROC);
    r = r && procedureType_2(b, l + 1);
    r = r && consumeToken(b, LPAREN);
    r = r && procedureType_4(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    r = r && procedureType_6(b, l + 1);
    r = r && enterMode(b, l + 1, "BLOCK");
    r = r && procedureType_8(b, l + 1);
    r = r && exitMode(b, l + 1, "BLOCK");
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // tagStatement_expression?
  private static boolean procedureType_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureType_0")) return false;
    tagStatement_expression(b, l + 1);
    return true;
  }

  // string_literal?
  private static boolean procedureType_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureType_2")) return false;
    string_literal(b, l + 1);
    return true;
  }

  // parameterList*
  private static boolean procedureType_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureType_4")) return false;
    while (true) {
      int c = current_position_(b);
      if (!parameterList(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "procedureType_4", c)) break;
    }
    return true;
  }

  // [ARROW returnType]
  private static boolean procedureType_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureType_6")) return false;
    procedureType_6_0(b, l + 1);
    return true;
  }

  // ARROW returnType
  private static boolean procedureType_6_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureType_6_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ARROW);
    r = r && returnType(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [eos* whereClause eos*]
  private static boolean procedureType_8(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureType_8")) return false;
    procedureType_8_0(b, l + 1);
    return true;
  }

  // eos* whereClause eos*
  private static boolean procedureType_8_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureType_8_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = procedureType_8_0_0(b, l + 1);
    r = r && whereClause(b, l + 1);
    r = r && procedureType_8_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // eos*
  private static boolean procedureType_8_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureType_8_0_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!eos(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "procedureType_8_0_0", c)) break;
    }
    return true;
  }

  // eos*
  private static boolean procedureType_8_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureType_8_0_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!eos(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "procedureType_8_0_2", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // expression (COMMA expression)* COMMA?
  static boolean returnArgumentList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnArgumentList")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = expression(b, l + 1, -1);
    r = r && returnArgumentList_1(b, l + 1);
    r = r && returnArgumentList_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA expression)*
  private static boolean returnArgumentList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnArgumentList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!returnArgumentList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "returnArgumentList_1", c)) break;
    }
    return true;
  }

  // COMMA expression
  private static boolean returnArgumentList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnArgumentList_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  // COMMA?
  private static boolean returnArgumentList_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnArgumentList_2")) return false;
    consumeToken(b, COMMA);
    return true;
  }

  /* ********************************************************** */
  // [tagStatement_expression] RETURN returnArgumentList?
  public static boolean returnStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnStatement")) return false;
    if (!nextTokenIs(b, "<return statement>", HASH, RETURN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RETURN_STATEMENT, "<return statement>");
    r = returnStatement_0(b, l + 1);
    r = r && consumeToken(b, RETURN);
    r = r && returnStatement_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // [tagStatement_expression]
  private static boolean returnStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnStatement_0")) return false;
    tagStatement_expression(b, l + 1);
    return true;
  }

  // returnArgumentList?
  private static boolean returnStatement_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnStatement_2")) return false;
    returnArgumentList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // NOT | LPAREN parameterList RPAREN | typeDefinition_expression
  public static boolean returnType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnType")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RETURN_TYPE, "<return type>");
    r = consumeToken(b, NOT);
    if (!r) r = returnType_1(b, l + 1);
    if (!r) r = typeDefinition_expression(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // LPAREN parameterList RPAREN
  private static boolean returnType_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnType_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && parameterList(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // LBRACKET expression? COLON expression? RBRACKET
  static boolean slice(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "slice")) return false;
    if (!nextTokenIs(b, LBRACKET)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LBRACKET);
    r = r && slice_1(b, l + 1);
    r = r && consumeToken(b, COLON);
    r = r && slice_3(b, l + 1);
    r = r && consumeToken(b, RBRACKET);
    exit_section_(b, m, null, r);
    return r;
  }

  // expression?
  private static boolean slice_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "slice_1")) return false;
    expression(b, l + 1, -1);
    return true;
  }

  // expression?
  private static boolean slice_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "slice_3")) return false;
    expression(b, l + 1, -1);
    return true;
  }

  /* ********************************************************** */
  // LBRACKET RBRACKET expression LBRACE [expression (COMMA expression)* COMMA?] RBRACE
  public static boolean sliceLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "sliceLiteral")) return false;
    if (!nextTokenIs(b, LBRACKET)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, LBRACKET, RBRACKET);
    r = r && expression(b, l + 1, -1);
    r = r && consumeToken(b, LBRACE);
    r = r && sliceLiteral_4(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, m, SLICE_LITERAL, r);
    return r;
  }

  // [expression (COMMA expression)* COMMA?]
  private static boolean sliceLiteral_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "sliceLiteral_4")) return false;
    sliceLiteral_4_0(b, l + 1);
    return true;
  }

  // expression (COMMA expression)* COMMA?
  private static boolean sliceLiteral_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "sliceLiteral_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = expression(b, l + 1, -1);
    r = r && sliceLiteral_4_0_1(b, l + 1);
    r = r && sliceLiteral_4_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA expression)*
  private static boolean sliceLiteral_4_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "sliceLiteral_4_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!sliceLiteral_4_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "sliceLiteral_4_0_1", c)) break;
    }
    return true;
  }

  // COMMA expression
  private static boolean sliceLiteral_4_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "sliceLiteral_4_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  // COMMA?
  private static boolean sliceLiteral_4_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "sliceLiteral_4_0_2")) return false;
    consumeToken(b, COMMA);
    return true;
  }

  /* ********************************************************** */
  // assignmentStatement
  //                 | procedureDeclarationStatement
  //                 | variableInitializationStatement
  //                 | variableDeclarationStatement
  //                 | structDeclarationStatement
  //                 | enumDeclarationStatement
  //                 | procedureOverloadStatement
  //                 | constantInitializationStatement
  //                 | bitsetDeclarationStatement
  //                 | blockStatement
  //                 | forStatement
  //                 | ifStatement
  //                 | whenStatement
  //                 | switchStatement
  //                 | deferStatement
  //                 | returnStatement
  //                 | breakStatement
  //                 | continueStatement
  //                 | attributeStatement
  //                 | usingStatement
  //                 | expressionStatement
  //                 | fallthroughStatement
  //                 | foreignStatement
  //                 | foreignImportDeclarationStatement
  public static boolean statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, STATEMENT, "<statement>");
    r = assignmentStatement(b, l + 1);
    if (!r) r = procedureDeclarationStatement(b, l + 1);
    if (!r) r = variableInitializationStatement(b, l + 1);
    if (!r) r = variableDeclarationStatement(b, l + 1);
    if (!r) r = structDeclarationStatement(b, l + 1);
    if (!r) r = enumDeclarationStatement(b, l + 1);
    if (!r) r = procedureOverloadStatement(b, l + 1);
    if (!r) r = constantInitializationStatement(b, l + 1);
    if (!r) r = bitsetDeclarationStatement(b, l + 1);
    if (!r) r = blockStatement(b, l + 1);
    if (!r) r = forStatement(b, l + 1);
    if (!r) r = ifStatement(b, l + 1);
    if (!r) r = whenStatement(b, l + 1);
    if (!r) r = switchStatement(b, l + 1);
    if (!r) r = deferStatement(b, l + 1);
    if (!r) r = returnStatement(b, l + 1);
    if (!r) r = breakStatement(b, l + 1);
    if (!r) r = continueStatement(b, l + 1);
    if (!r) r = attributeStatement(b, l + 1);
    if (!r) r = usingStatement(b, l + 1);
    if (!r) r = expressionStatement(b, l + 1);
    if (!r) r = fallthroughStatement(b, l + 1);
    if (!r) r = foreignStatement(b, l + 1);
    if (!r) r = foreignImportDeclarationStatement(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // block|doStatement
  public static boolean statementBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statementBody")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, STATEMENT_BODY, "<statement body>");
    r = block(b, l + 1);
    if (!r) r = doStatement(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // ((SEMICOLON | NEW_LINE | EOS_TOKEN | <<closingBracket>>)? statement eos)+
  public static boolean statementList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statementList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, STATEMENT_LIST, "<statement list>");
    r = statementList_0(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!statementList_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "statementList", c)) break;
    }
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (SEMICOLON | NEW_LINE | EOS_TOKEN | <<closingBracket>>)? statement eos
  private static boolean statementList_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statementList_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = statementList_0_0(b, l + 1);
    r = r && statement(b, l + 1);
    r = r && eos(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (SEMICOLON | NEW_LINE | EOS_TOKEN | <<closingBracket>>)?
  private static boolean statementList_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statementList_0_0")) return false;
    statementList_0_0_0(b, l + 1);
    return true;
  }

  // SEMICOLON | NEW_LINE | EOS_TOKEN | <<closingBracket>>
  private static boolean statementList_0_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statementList_0_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SEMICOLON);
    if (!r) r = consumeToken(b, NEW_LINE);
    if (!r) r = consumeToken(b, EOS_TOKEN);
    if (!r) r = closingBracket(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // DQ_STRING_LITERAL
  //                     | SQ_STRING_LITERAL
  //                     | RAW_STRING_LITERAL
  public static boolean string_literal(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "string_literal")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, STRING_LITERAL, "<string literal>");
    r = consumeToken(b, DQ_STRING_LITERAL);
    if (!r) r = consumeToken(b, SQ_STRING_LITERAL);
    if (!r) r = consumeToken(b, RAW_STRING_LITERAL);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // attributeStatement? IDENTIFIER COLON COLON structSpec
  public static boolean structDeclarationStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structDeclarationStatement")) return false;
    if (!nextTokenIs(b, "<struct declaration statement>", AT, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, STRUCT_DECLARATION_STATEMENT, "<struct declaration statement>");
    r = structDeclarationStatement_0(b, l + 1);
    r = r && consumeTokens(b, 0, IDENTIFIER, COLON, COLON);
    r = r && structSpec(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // attributeStatement?
  private static boolean structDeclarationStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structDeclarationStatement_0")) return false;
    attributeStatement(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // IDENTIFIER (DOT IDENTIFIER)* literalValue
  public static boolean structLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structLiteral")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    r = r && structLiteral_1(b, l + 1);
    r = r && literalValue(b, l + 1);
    exit_section_(b, m, STRUCT_LITERAL, r);
    return r;
  }

  // (DOT IDENTIFIER)*
  private static boolean structLiteral_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structLiteral_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!structLiteral_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "structLiteral_1", c)) break;
    }
    return true;
  }

  // DOT IDENTIFIER
  private static boolean structLiteral_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structLiteral_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, DOT, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // STRUCT [LPAREN parameterList RPAREN [eos* whereClause eos*]] <<enterMode "BLOCK">>[tagStatement_expression] <<exitMode "BLOCK">> LBRACE [fieldDeclarationStatement (COMMA fieldDeclarationStatement)* COMMA?] RBRACE
  public static boolean structSpec(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structSpec")) return false;
    if (!nextTokenIs(b, STRUCT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, STRUCT);
    r = r && structSpec_1(b, l + 1);
    r = r && enterMode(b, l + 1, "BLOCK");
    r = r && structSpec_3(b, l + 1);
    r = r && exitMode(b, l + 1, "BLOCK");
    r = r && consumeToken(b, LBRACE);
    r = r && structSpec_6(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, m, STRUCT_SPEC, r);
    return r;
  }

  // [LPAREN parameterList RPAREN [eos* whereClause eos*]]
  private static boolean structSpec_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structSpec_1")) return false;
    structSpec_1_0(b, l + 1);
    return true;
  }

  // LPAREN parameterList RPAREN [eos* whereClause eos*]
  private static boolean structSpec_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structSpec_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && parameterList(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    r = r && structSpec_1_0_3(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [eos* whereClause eos*]
  private static boolean structSpec_1_0_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structSpec_1_0_3")) return false;
    structSpec_1_0_3_0(b, l + 1);
    return true;
  }

  // eos* whereClause eos*
  private static boolean structSpec_1_0_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structSpec_1_0_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = structSpec_1_0_3_0_0(b, l + 1);
    r = r && whereClause(b, l + 1);
    r = r && structSpec_1_0_3_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // eos*
  private static boolean structSpec_1_0_3_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structSpec_1_0_3_0_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!eos(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "structSpec_1_0_3_0_0", c)) break;
    }
    return true;
  }

  // eos*
  private static boolean structSpec_1_0_3_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structSpec_1_0_3_0_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!eos(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "structSpec_1_0_3_0_2", c)) break;
    }
    return true;
  }

  // [tagStatement_expression]
  private static boolean structSpec_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structSpec_3")) return false;
    tagStatement_expression(b, l + 1);
    return true;
  }

  // [fieldDeclarationStatement (COMMA fieldDeclarationStatement)* COMMA?]
  private static boolean structSpec_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structSpec_6")) return false;
    structSpec_6_0(b, l + 1);
    return true;
  }

  // fieldDeclarationStatement (COMMA fieldDeclarationStatement)* COMMA?
  private static boolean structSpec_6_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structSpec_6_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = fieldDeclarationStatement(b, l + 1);
    r = r && structSpec_6_0_1(b, l + 1);
    r = r && structSpec_6_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA fieldDeclarationStatement)*
  private static boolean structSpec_6_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structSpec_6_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!structSpec_6_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "structSpec_6_0_1", c)) break;
    }
    return true;
  }

  // COMMA fieldDeclarationStatement
  private static boolean structSpec_6_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structSpec_6_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && fieldDeclarationStatement(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // COMMA?
  private static boolean structSpec_6_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structSpec_6_0_2")) return false;
    consumeToken(b, COMMA);
    return true;
  }

  /* ********************************************************** */
  // CASE (caseExpression(COMMA caseExpression)*)? COLON statementList?
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

  // (caseExpression(COMMA caseExpression)*)?
  private static boolean switchCaseBlock_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchCaseBlock_1")) return false;
    switchCaseBlock_1_0(b, l + 1);
    return true;
  }

  // caseExpression(COMMA caseExpression)*
  private static boolean switchCaseBlock_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchCaseBlock_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = caseExpression(b, l + 1);
    r = r && switchCaseBlock_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA caseExpression)*
  private static boolean switchCaseBlock_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchCaseBlock_1_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!switchCaseBlock_1_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "switchCaseBlock_1_0_1", c)) break;
    }
    return true;
  }

  // COMMA caseExpression
  private static boolean switchCaseBlock_1_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchCaseBlock_1_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && caseExpression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // statementList?
  private static boolean switchCaseBlock_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchCaseBlock_3")) return false;
    statementList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // [ifInit SEMICOLON] expression?
  static boolean switchCondition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchCondition")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = switchCondition_0(b, l + 1);
    r = r && switchCondition_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [ifInit SEMICOLON]
  private static boolean switchCondition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchCondition_0")) return false;
    switchCondition_0_0(b, l + 1);
    return true;
  }

  // ifInit SEMICOLON
  private static boolean switchCondition_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchCondition_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ifInit(b, l + 1);
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, m, null, r);
    return r;
  }

  // expression?
  private static boolean switchCondition_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchCondition_1")) return false;
    expression(b, l + 1, -1);
    return true;
  }

  /* ********************************************************** */
  // [label] [tagStatement_expression] SWITCH IN? <<enterMode "BLOCK">> switchCondition? <<exitMode "BLOCK">> LBRACE (switchCaseBlock)* RBRACE
  public static boolean switchStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SWITCH_STATEMENT, "<switch statement>");
    r = switchStatement_0(b, l + 1);
    r = r && switchStatement_1(b, l + 1);
    r = r && consumeToken(b, SWITCH);
    r = r && switchStatement_3(b, l + 1);
    r = r && enterMode(b, l + 1, "BLOCK");
    r = r && switchStatement_5(b, l + 1);
    r = r && exitMode(b, l + 1, "BLOCK");
    r = r && consumeToken(b, LBRACE);
    r = r && switchStatement_8(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // [label]
  private static boolean switchStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchStatement_0")) return false;
    label(b, l + 1);
    return true;
  }

  // [tagStatement_expression]
  private static boolean switchStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchStatement_1")) return false;
    tagStatement_expression(b, l + 1);
    return true;
  }

  // IN?
  private static boolean switchStatement_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchStatement_3")) return false;
    consumeToken(b, IN);
    return true;
  }

  // switchCondition?
  private static boolean switchStatement_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchStatement_5")) return false;
    switchCondition(b, l + 1);
    return true;
  }

  // (switchCaseBlock)*
  private static boolean switchStatement_8(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchStatement_8")) return false;
    while (true) {
      int c = current_position_(b);
      if (!switchStatement_8_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "switchStatement_8", c)) break;
    }
    return true;
  }

  // (switchCaseBlock)
  private static boolean switchStatement_8_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchStatement_8_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = switchCaseBlock(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // HASH IDENTIFIER
  public static boolean tagHead(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tagHead")) return false;
    if (!nextTokenIs(b, HASH)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, HASH, IDENTIFIER);
    exit_section_(b, m, TAG_HEAD, r);
    return r;
  }

  /* ********************************************************** */
  // TRIPLE_DASH
  public static boolean tripleDashLiteral_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tripleDashLiteral_expression")) return false;
    if (!nextTokenIs(b, TRIPLE_DASH)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, TRIPLE_DASH);
    exit_section_(b, m, TRIPLE_DASH_LITERAL_EXPRESSION, r);
    return r;
  }

  /* ********************************************************** */
  // mapType
  //          | matrixType
  //          | bitSetType
  //          | multiPointerType
  //          | arrayType
  //          | procedureType
  //          | structSpec
  //          | enumSpec
  //          | unionSpec
  //          | pointerType
  //          | (DOLLAR? primary_expression)
  public static boolean type(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TYPE, "<type>");
    r = mapType(b, l + 1);
    if (!r) r = matrixType(b, l + 1);
    if (!r) r = bitSetType(b, l + 1);
    if (!r) r = multiPointerType(b, l + 1);
    if (!r) r = arrayType(b, l + 1);
    if (!r) r = procedureType(b, l + 1);
    if (!r) r = structSpec(b, l + 1);
    if (!r) r = enumSpec(b, l + 1);
    if (!r) r = unionSpec(b, l + 1);
    if (!r) r = pointerType(b, l + 1);
    if (!r) r = type_10(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // DOLLAR? primary_expression
  private static boolean type_10(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_10")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = type_10_0(b, l + 1);
    r = r && primary_expression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // DOLLAR?
  private static boolean type_10_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_10_0")) return false;
    consumeToken(b, DOLLAR);
    return true;
  }

  /* ********************************************************** */
  // IDENTIFIER COLON COLON DISTINCT? typeDefinition_expression
  public static boolean typeAliasDeclarationStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeAliasDeclarationStatement")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, IDENTIFIER, COLON, COLON);
    r = r && typeAliasDeclarationStatement_3(b, l + 1);
    r = r && typeDefinition_expression(b, l + 1);
    exit_section_(b, m, TYPE_ALIAS_DECLARATION_STATEMENT, r);
    return r;
  }

  // DISTINCT?
  private static boolean typeAliasDeclarationStatement_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeAliasDeclarationStatement_3")) return false;
    consumeToken(b, DISTINCT);
    return true;
  }

  /* ********************************************************** */
  // DOT LPAREN typeDefinition_expression RPAREN
  static boolean typeAssertion(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeAssertion")) return false;
    if (!nextTokenIs(b, DOT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, DOT, LPAREN);
    r = r && typeDefinition_expression(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // PLUS      // Arithmetic identity
  //                   | MINUS   // Arithmetic negation
  //                   | NOT     // Boolean not
  //                   | TILDE   // Bitwise not
  //                   | AND     // Address of
  //                   | RANGE
  public static boolean unaryOperator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unaryOperator")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, UNARY_OPERATOR, "<unary operator>");
    r = consumeToken(b, PLUS);
    if (!r) r = consumeToken(b, MINUS);
    if (!r) r = consumeToken(b, NOT);
    if (!r) r = consumeToken(b, TILDE);
    if (!r) r = consumeToken(b, AND);
    if (!r) r = consumeToken(b, RANGE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // attributeStatement? IDENTIFIER COLON COLON unionSpec
  public static boolean unionDeclarationStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionDeclarationStatement")) return false;
    if (!nextTokenIs(b, "<union declaration statement>", AT, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, UNION_DECLARATION_STATEMENT, "<union declaration statement>");
    r = unionDeclarationStatement_0(b, l + 1);
    r = r && consumeTokens(b, 0, IDENTIFIER, COLON, COLON);
    r = r && unionSpec(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // attributeStatement?
  private static boolean unionDeclarationStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionDeclarationStatement_0")) return false;
    attributeStatement(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // UNION [LPAREN parameterList RPAREN [eos* whereClause eos*]] <<enterMode "BLOCK">>[tagStatement_expression] <<exitMode "BLOCK">> LBRACE [typeDefinition_expression (COMMA typeDefinition_expression)* COMMA?] RBRACE
  public static boolean unionSpec(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionSpec")) return false;
    if (!nextTokenIs(b, UNION)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, UNION);
    r = r && unionSpec_1(b, l + 1);
    r = r && enterMode(b, l + 1, "BLOCK");
    r = r && unionSpec_3(b, l + 1);
    r = r && exitMode(b, l + 1, "BLOCK");
    r = r && consumeToken(b, LBRACE);
    r = r && unionSpec_6(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, m, UNION_SPEC, r);
    return r;
  }

  // [LPAREN parameterList RPAREN [eos* whereClause eos*]]
  private static boolean unionSpec_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionSpec_1")) return false;
    unionSpec_1_0(b, l + 1);
    return true;
  }

  // LPAREN parameterList RPAREN [eos* whereClause eos*]
  private static boolean unionSpec_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionSpec_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && parameterList(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    r = r && unionSpec_1_0_3(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [eos* whereClause eos*]
  private static boolean unionSpec_1_0_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionSpec_1_0_3")) return false;
    unionSpec_1_0_3_0(b, l + 1);
    return true;
  }

  // eos* whereClause eos*
  private static boolean unionSpec_1_0_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionSpec_1_0_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = unionSpec_1_0_3_0_0(b, l + 1);
    r = r && whereClause(b, l + 1);
    r = r && unionSpec_1_0_3_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // eos*
  private static boolean unionSpec_1_0_3_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionSpec_1_0_3_0_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!eos(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "unionSpec_1_0_3_0_0", c)) break;
    }
    return true;
  }

  // eos*
  private static boolean unionSpec_1_0_3_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionSpec_1_0_3_0_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!eos(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "unionSpec_1_0_3_0_2", c)) break;
    }
    return true;
  }

  // [tagStatement_expression]
  private static boolean unionSpec_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionSpec_3")) return false;
    tagStatement_expression(b, l + 1);
    return true;
  }

  // [typeDefinition_expression (COMMA typeDefinition_expression)* COMMA?]
  private static boolean unionSpec_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionSpec_6")) return false;
    unionSpec_6_0(b, l + 1);
    return true;
  }

  // typeDefinition_expression (COMMA typeDefinition_expression)* COMMA?
  private static boolean unionSpec_6_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionSpec_6_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = typeDefinition_expression(b, l + 1);
    r = r && unionSpec_6_0_1(b, l + 1);
    r = r && unionSpec_6_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA typeDefinition_expression)*
  private static boolean unionSpec_6_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionSpec_6_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!unionSpec_6_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "unionSpec_6_0_1", c)) break;
    }
    return true;
  }

  // COMMA typeDefinition_expression
  private static boolean unionSpec_6_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionSpec_6_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && typeDefinition_expression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // COMMA?
  private static boolean unionSpec_6_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionSpec_6_0_2")) return false;
    consumeToken(b, COMMA);
    return true;
  }

  /* ********************************************************** */
  // expression
  public static boolean unnamedArgument(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unnamedArgument")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, UNNAMED_ARGUMENT, "<unnamed argument>");
    r = expression(b, l + 1, -1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // USING expression
  public static boolean usingStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "usingStatement")) return false;
    if (!nextTokenIs(b, USING)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, USING);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, USING_STATEMENT, r);
    return r;
  }

  /* ********************************************************** */
  // (attributeStatement eos?)* IDENTIFIER (COMMA IDENTIFIER)* COLON typeDefinition_expression
  public static boolean variableDeclarationStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableDeclarationStatement")) return false;
    if (!nextTokenIs(b, "<variable declaration statement>", AT, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, VARIABLE_DECLARATION_STATEMENT, "<variable declaration statement>");
    r = variableDeclarationStatement_0(b, l + 1);
    r = r && consumeToken(b, IDENTIFIER);
    r = r && variableDeclarationStatement_2(b, l + 1);
    r = r && consumeToken(b, COLON);
    r = r && typeDefinition_expression(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (attributeStatement eos?)*
  private static boolean variableDeclarationStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableDeclarationStatement_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!variableDeclarationStatement_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "variableDeclarationStatement_0", c)) break;
    }
    return true;
  }

  // attributeStatement eos?
  private static boolean variableDeclarationStatement_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableDeclarationStatement_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = attributeStatement(b, l + 1);
    r = r && variableDeclarationStatement_0_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // eos?
  private static boolean variableDeclarationStatement_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableDeclarationStatement_0_0_1")) return false;
    eos(b, l + 1);
    return true;
  }

  // (COMMA IDENTIFIER)*
  private static boolean variableDeclarationStatement_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableDeclarationStatement_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!variableDeclarationStatement_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "variableDeclarationStatement_2", c)) break;
    }
    return true;
  }

  // COMMA IDENTIFIER
  private static boolean variableDeclarationStatement_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableDeclarationStatement_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, COMMA, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (attributeStatement eos*)* [tagHead] identifierList COLON typeDefinition_expression? (EQ|COLON) expressionsList
  public static boolean variableInitializationStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableInitializationStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, VARIABLE_INITIALIZATION_STATEMENT, "<variable initialization statement>");
    r = variableInitializationStatement_0(b, l + 1);
    r = r && variableInitializationStatement_1(b, l + 1);
    r = r && identifierList(b, l + 1);
    r = r && consumeToken(b, COLON);
    r = r && variableInitializationStatement_4(b, l + 1);
    r = r && variableInitializationStatement_5(b, l + 1);
    r = r && expressionsList(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (attributeStatement eos*)*
  private static boolean variableInitializationStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableInitializationStatement_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!variableInitializationStatement_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "variableInitializationStatement_0", c)) break;
    }
    return true;
  }

  // attributeStatement eos*
  private static boolean variableInitializationStatement_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableInitializationStatement_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = attributeStatement(b, l + 1);
    r = r && variableInitializationStatement_0_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // eos*
  private static boolean variableInitializationStatement_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableInitializationStatement_0_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!eos(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "variableInitializationStatement_0_0_1", c)) break;
    }
    return true;
  }

  // [tagHead]
  private static boolean variableInitializationStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableInitializationStatement_1")) return false;
    tagHead(b, l + 1);
    return true;
  }

  // typeDefinition_expression?
  private static boolean variableInitializationStatement_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableInitializationStatement_4")) return false;
    typeDefinition_expression(b, l + 1);
    return true;
  }

  // EQ|COLON
  private static boolean variableInitializationStatement_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableInitializationStatement_5")) return false;
    boolean r;
    r = consumeToken(b, EQ);
    if (!r) r = consumeToken(b, COLON);
    return r;
  }

  /* ********************************************************** */
  // IDENTIFIER COLON RANGE expression
  static boolean variadicParameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variadicParameter")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, IDENTIFIER, COLON, RANGE);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // WHEN condition block (eos* elseWhenBlock)* (eos* elseBlock)?
  public static boolean whenStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "whenStatement")) return false;
    if (!nextTokenIs(b, WHEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, WHEN);
    r = r && condition(b, l + 1);
    r = r && block(b, l + 1);
    r = r && whenStatement_3(b, l + 1);
    r = r && whenStatement_4(b, l + 1);
    exit_section_(b, m, WHEN_STATEMENT, r);
    return r;
  }

  // (eos* elseWhenBlock)*
  private static boolean whenStatement_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "whenStatement_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!whenStatement_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "whenStatement_3", c)) break;
    }
    return true;
  }

  // eos* elseWhenBlock
  private static boolean whenStatement_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "whenStatement_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = whenStatement_3_0_0(b, l + 1);
    r = r && elseWhenBlock(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // eos*
  private static boolean whenStatement_3_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "whenStatement_3_0_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!eos(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "whenStatement_3_0_0", c)) break;
    }
    return true;
  }

  // (eos* elseBlock)?
  private static boolean whenStatement_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "whenStatement_4")) return false;
    whenStatement_4_0(b, l + 1);
    return true;
  }

  // eos* elseBlock
  private static boolean whenStatement_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "whenStatement_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = whenStatement_4_0_0(b, l + 1);
    r = r && elseBlock(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // eos*
  private static boolean whenStatement_4_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "whenStatement_4_0_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!eos(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "whenStatement_4_0_0", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // WHERE expression (COMMA expression)*
  public static boolean whereClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "whereClause")) return false;
    if (!nextTokenIs(b, WHERE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, WHERE);
    r = r && expression(b, l + 1, -1);
    r = r && whereClause_2(b, l + 1);
    exit_section_(b, m, WHERE_CLAUSE, r);
    return r;
  }

  // (COMMA expression)*
  private static boolean whereClause_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "whereClause_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!whereClause_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "whereClause_2", c)) break;
    }
    return true;
  }

  // COMMA expression
  private static boolean whereClause_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "whereClause_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // Expression root: expression
  // Operator priority table:
  // 0: PREFIX(implicitSelector_expression)
  // 1: ATOM(primary_expression)
  // 2: ATOM(identifier_expression)
  // 3: BINARY(ternary_cond_expression) BINARY(ternary_if_expression) BINARY(ternary_when_expression)
  // 4: ATOM(procedure_expression)
  // 5: POSTFIX(maybe_expression)
  // 6: PREFIX(unary_expression)
  // 7: BINARY(binary_expression)
  // 8: POSTFIX(or_return_expression)
  // 9: ATOM(set_expression)
  // 10: ATOM(uninitialized_expression)
  // 11: ATOM(cast_expression)
  // 12: ATOM(transmute_expression)
  // 13: PREFIX(auto_cast_expression)
  // 14: ATOM(typeDefinitionValue_expression)
  // 15: ATOM(typeDefinition_expression)
  // 16: ATOM(tagStatement_expression)
  public static boolean expression(PsiBuilder b, int l, int g) {
    if (!recursion_guard_(b, l, "expression")) return false;
    addVariant(b, "<expression>");
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, "<expression>");
    r = implicitSelector_expression(b, l + 1);
    if (!r) r = primary_expression(b, l + 1);
    if (!r) r = identifier_expression(b, l + 1);
    if (!r) r = procedure_expression(b, l + 1);
    if (!r) r = unary_expression(b, l + 1);
    if (!r) r = set_expression(b, l + 1);
    if (!r) r = uninitialized_expression(b, l + 1);
    if (!r) r = cast_expression(b, l + 1);
    if (!r) r = transmute_expression(b, l + 1);
    if (!r) r = auto_cast_expression(b, l + 1);
    if (!r) r = typeDefinitionValue_expression(b, l + 1);
    if (!r) r = typeDefinition_expression(b, l + 1);
    if (!r) r = tagStatement_expression(b, l + 1);
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
      if (g < 3 && consumeTokenSmart(b, QUESTION)) {
        r = report_error_(b, expression(b, l, 3));
        r = ternary_cond_expression_1(b, l + 1) && r;
        exit_section_(b, l, m, TERNARY_COND_EXPRESSION, r, true, null);
      }
      else if (g < 3 && consumeTokenSmart(b, IF)) {
        r = report_error_(b, expression(b, l, 3));
        r = ternary_if_expression_1(b, l + 1) && r;
        exit_section_(b, l, m, TERNARY_IF_EXPRESSION, r, true, null);
      }
      else if (g < 3 && consumeTokenSmart(b, WHEN)) {
        r = report_error_(b, expression(b, l, 3));
        r = ternary_when_expression_1(b, l + 1) && r;
        exit_section_(b, l, m, TERNARY_WHEN_EXPRESSION, r, true, null);
      }
      else if (g < 5 && consumeTokenSmart(b, DOT_QUESTION)) {
        r = true;
        exit_section_(b, l, m, MAYBE_EXPRESSION, r, true, null);
      }
      else if (g < 7 && binaryOperator(b, l + 1)) {
        r = expression(b, l, 7);
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 8 && consumeTokenSmart(b, OR_RETURN)) {
        r = true;
        exit_section_(b, l, m, OR_RETURN_EXPRESSION, r, true, null);
      }
      else {
        exit_section_(b, l, m, null, false, false, null);
        break;
      }
    }
    return r;
  }

  public static boolean implicitSelector_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "implicitSelector_expression")) return false;
    if (!nextTokenIsSmart(b, DOT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeTokenSmart(b, DOT);
    p = r;
    r = p && expression(b, l, 0);
    exit_section_(b, l, m, IMPLICIT_SELECTOR_EXPRESSION, r, p, null);
    return r || p;
  }

  // [tagHead] primary
  public static boolean primary_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "primary_expression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PRIMARY_EXPRESSION, "<primary expression>");
    r = primary_expression_0(b, l + 1);
    r = r && primary(b, l + 1, -1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // [tagHead]
  private static boolean primary_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "primary_expression_0")) return false;
    tagHead(b, l + 1);
    return true;
  }

  // IDENTIFIER
  public static boolean identifier_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "identifier_expression")) return false;
    if (!nextTokenIsSmart(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, IDENTIFIER);
    exit_section_(b, m, IDENTIFIER_EXPRESSION, r);
    return r;
  }

  // COLON expression
  private static boolean ternary_cond_expression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ternary_cond_expression_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COLON);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ELSE expression
  private static boolean ternary_if_expression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ternary_if_expression_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ELSE);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ELSE expression
  private static boolean ternary_when_expression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ternary_when_expression_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ELSE);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  // procedureType procedureBody
  public static boolean procedure_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedure_expression")) return false;
    if (!nextTokenIsSmart(b, HASH, PROC)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PROCEDURE_EXPRESSION, "<procedure expression>");
    r = procedureType(b, l + 1);
    r = r && procedureBody(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  public static boolean unary_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unary_expression")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = unaryOperator(b, l + 1);
    p = r;
    r = p && expression(b, l, 0);
    exit_section_(b, l, m, UNARY_EXPRESSION, r, p, null);
    return r || p;
  }

  // LBRACE [expression (COMMA expression)* COMMA?] RBRACE
  public static boolean set_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "set_expression")) return false;
    if (!nextTokenIsSmart(b, LBRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, LBRACE);
    r = r && set_expression_1(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, m, SET_EXPRESSION, r);
    return r;
  }

  // [expression (COMMA expression)* COMMA?]
  private static boolean set_expression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "set_expression_1")) return false;
    set_expression_1_0(b, l + 1);
    return true;
  }

  // expression (COMMA expression)* COMMA?
  private static boolean set_expression_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "set_expression_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = expression(b, l + 1, -1);
    r = r && set_expression_1_0_1(b, l + 1);
    r = r && set_expression_1_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA expression)*
  private static boolean set_expression_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "set_expression_1_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!set_expression_1_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "set_expression_1_0_1", c)) break;
    }
    return true;
  }

  // COMMA expression
  private static boolean set_expression_1_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "set_expression_1_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, COMMA);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  // COMMA?
  private static boolean set_expression_1_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "set_expression_1_0_2")) return false;
    consumeTokenSmart(b, COMMA);
    return true;
  }

  // TRIPLE_DASH
  public static boolean uninitialized_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "uninitialized_expression")) return false;
    if (!nextTokenIsSmart(b, TRIPLE_DASH)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, TRIPLE_DASH);
    exit_section_(b, m, UNINITIALIZED_EXPRESSION, r);
    return r;
  }

  // CAST LPAREN typeDefinition_expression RPAREN expression
  public static boolean cast_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "cast_expression")) return false;
    if (!nextTokenIsSmart(b, CAST)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokensSmart(b, 0, CAST, LPAREN);
    r = r && typeDefinition_expression(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, CAST_EXPRESSION, r);
    return r;
  }

  // TRANSMUTE LPAREN expression RPAREN expression
  public static boolean transmute_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "transmute_expression")) return false;
    if (!nextTokenIsSmart(b, TRANSMUTE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokensSmart(b, 0, TRANSMUTE, LPAREN);
    r = r && expression(b, l + 1, -1);
    r = r && consumeToken(b, RPAREN);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, TRANSMUTE_EXPRESSION, r);
    return r;
  }

  public static boolean auto_cast_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "auto_cast_expression")) return false;
    if (!nextTokenIsSmart(b, AUTO_CAST)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeTokenSmart(b, AUTO_CAST);
    p = r;
    r = p && expression(b, l, 13);
    exit_section_(b, l, m, AUTO_CAST_EXPRESSION, r, p, null);
    return r || p;
  }

  // type literalValue
  public static boolean typeDefinitionValue_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeDefinitionValue_expression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TYPE_DEFINITION_VALUE_EXPRESSION, "<type definition value expression>");
    r = type(b, l + 1);
    r = r && literalValue(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // DISTINCT? [tagStatement_expression] type
  public static boolean typeDefinition_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeDefinition_expression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TYPE_DEFINITION_EXPRESSION, "<type definition expression>");
    r = typeDefinition_expression_0(b, l + 1);
    r = r && typeDefinition_expression_1(b, l + 1);
    r = r && type(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // DISTINCT?
  private static boolean typeDefinition_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeDefinition_expression_0")) return false;
    consumeTokenSmart(b, DISTINCT);
    return true;
  }

  // [tagStatement_expression]
  private static boolean typeDefinition_expression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeDefinition_expression_1")) return false;
    tagStatement_expression(b, l + 1);
    return true;
  }

  // tagHead [literal | (LPAREN expressionsList? RPAREN)]
  public static boolean tagStatement_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tagStatement_expression")) return false;
    if (!nextTokenIsSmart(b, HASH)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = tagHead(b, l + 1);
    r = r && tagStatement_expression_1(b, l + 1);
    exit_section_(b, m, TAG_STATEMENT_EXPRESSION, r);
    return r;
  }

  // [literal | (LPAREN expressionsList? RPAREN)]
  private static boolean tagStatement_expression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tagStatement_expression_1")) return false;
    tagStatement_expression_1_0(b, l + 1);
    return true;
  }

  // literal | (LPAREN expressionsList? RPAREN)
  private static boolean tagStatement_expression_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tagStatement_expression_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = literal(b, l + 1);
    if (!r) r = tagStatement_expression_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // LPAREN expressionsList? RPAREN
  private static boolean tagStatement_expression_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tagStatement_expression_1_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, LPAREN);
    r = r && tagStatement_expression_1_0_1_1(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // expressionsList?
  private static boolean tagStatement_expression_1_0_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tagStatement_expression_1_0_1_1")) return false;
    expressionsList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // Expression root: primary
  // Operator priority table:
  // 0: ATOM(operand_primary)
  // 1: POSTFIX(caret_primary)
  // 2: POSTFIX(combined_primary)
  public static boolean primary(PsiBuilder b, int l, int g) {
    if (!recursion_guard_(b, l, "primary")) return false;
    addVariant(b, "<primary>");
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, "<primary>");
    r = operand_primary(b, l + 1);
    p = r;
    r = r && primary_0(b, l + 1, g);
    exit_section_(b, l, m, null, r, p, null);
    return r || p;
  }

  public static boolean primary_0(PsiBuilder b, int l, int g) {
    if (!recursion_guard_(b, l, "primary_0")) return false;
    boolean r = true;
    while (true) {
      Marker m = enter_section_(b, l, _LEFT_, null);
      if (g < 1 && consumeTokenSmart(b, CARET)) {
        r = true;
        exit_section_(b, l, m, CARET_PRIMARY, r, true, null);
      }
      else if (g < 2 && combined_primary_0(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, COMBINED_PRIMARY, r, true, null);
      }
      else {
        exit_section_(b, l, m, null, false, false, null);
        break;
      }
    }
    return r;
  }

  // literal | identifier_expression | parenthesized_expression
  public static boolean operand_primary(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "operand_primary")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, OPERAND_PRIMARY, "<operand primary>");
    r = literal(b, l + 1);
    if (!r) r = identifier_expression(b, l + 1);
    if (!r) r = parenthesized_expression(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // DOT IDENTIFIER
  //                               | ARROW IDENTIFIER
  //                               | index
  //                               | typeAssertion
  //                               | slice
  //                               | arguments
  //                               | literalValue
  private static boolean combined_primary_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "combined_primary_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = parseTokensSmart(b, 0, DOT, IDENTIFIER);
    if (!r) r = parseTokensSmart(b, 0, ARROW, IDENTIFIER);
    if (!r) r = index(b, l + 1);
    if (!r) r = typeAssertion(b, l + 1);
    if (!r) r = slice(b, l + 1);
    if (!r) r = arguments(b, l + 1);
    if (!r) r = literalValue(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

}
