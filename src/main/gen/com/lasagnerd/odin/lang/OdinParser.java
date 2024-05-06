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
    create_token_set_(COMPOUND_LITERAL_TYPED, COMPOUND_LITERAL_UNTYPED),
    create_token_set_(PARAMETER_DECL, PARAMETER_INITIALIZATION, UNNAMED_PARAMETER),
    create_token_set_(ARGUMENT, NAMED_ARGUMENT, UNNAMED_ARGUMENT),
    create_token_set_(ARRAY_TYPE, BIT_SET_TYPE, CALL_TYPE, CONSTRAINED_TYPE,
      ENUM_TYPE, MAP_TYPE, MATRIX_TYPE, MULTI_POINTER_TYPE,
      PAR_EXPRESSION_TYPE, POINTER_TYPE, POLYMORPHIC_TYPE, PROCEDURE_TYPE,
      QUALIFIED_TYPE, SIMPLE_REF_TYPE, STRUCT_TYPE, TYPE,
      UNION_TYPE, VARIADIC_TYPE),
    create_token_set_(ASSIGNMENT_STATEMENT, ATTRIBUTE_STATEMENT, BITSET_DECLARATION_STATEMENT, BLOCK_STATEMENT,
      BREAK_STATEMENT, CONDITIONAL_STATEMENT, CONSTANT_INITIALIZATION_STATEMENT, CONTINUE_STATEMENT,
      DEFER_STATEMENT, DIRECTIVE_STATEMENT, DO_STATEMENT, ENUM_DECLARATION_STATEMENT,
      EXPRESSION_STATEMENT, FALLTHROUGH_STATEMENT, FIELD_DECLARATION_STATEMENT, FILE_SCOPE_STATEMENT,
      FOREIGN_BLOCK_STATEMENT, FOREIGN_IMPORT_DECLARATION_STATEMENT, FOREIGN_STATEMENT, FOR_IN_STATEMENT,
      FOR_STATEMENT, IMPORT_DECLARATION_STATEMENT, PROCEDURE_DECLARATION_STATEMENT, PROCEDURE_OVERLOAD_DECLARATION_STATEMENT,
      RETURN_STATEMENT, STATEMENT, STRUCT_DECLARATION_STATEMENT, SWITCH_STATEMENT,
      UNION_DECLARATION_STATEMENT, USING_STATEMENT, VARIABLE_DECLARATION_STATEMENT, VARIABLE_INITIALIZATION_STATEMENT,
      WHEN_STATEMENT),
    create_token_set_(AUTO_CAST_EXPRESSION, BINARY_EXPRESSION, CALL_EXPRESSION, CAST_EXPRESSION,
      COMPOUND_LITERAL_EXPRESSION, DEREFERENCE_EXPRESSION, DIRECTIVE_EXPRESSION, ELVIS_EXPRESSION,
      EXPRESSION, IMPLICIT_SELECTOR_EXPRESSION, INDEX_EXPRESSION, LITERAL_EXPRESSION,
      MAYBE_EXPRESSION, OR_BREAK_EXPRESSION, OR_CONTINUE_EXPRESSION, OR_ELSE_EXPRESSION,
      OR_RETURN_EXPRESSION, PARENTHESIZED_EXPRESSION, PROCEDURE_EXPRESSION, REF_EXPRESSION,
      SLICE_EXPRESSION, TERNARY_IF_EXPRESSION, TERNARY_WHEN_EXPRESSION, TRANSMUTE_EXPRESSION,
      TYPE_ASSERTION_EXPRESSION, TYPE_DEFINITION_EXPRESSION, UNARY_AND_EXPRESSION, UNARY_MINUS_EXPRESSION,
      UNARY_NOT_EXPRESSION, UNARY_PLUS_EXPRESSION, UNARY_RANGE_EXPRESSION, UNARY_TILDE_EXPRESSION,
      UNINITIALIZED_EXPRESSION),
  };

  /* ********************************************************** */
  // namedArgument
  //                                          | unnamedArgument
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
  static boolean argumentList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "argumentList")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = argument(b, l + 1);
    r = r && argumentList_1(b, l + 1);
    r = r && argumentList_2(b, l + 1);
    exit_section_(b, m, null, r);
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
  // LPAREN <<enterMode "PAR">> argumentList? <<exitMode "PAR">> RPAREN
  static boolean arguments(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arguments")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && enterMode(b, l + 1, "PAR");
    r = r && arguments_2(b, l + 1);
    r = r && exitMode(b, l + 1, "PAR");
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // argumentList?
  private static boolean arguments_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arguments_2")) return false;
    argumentList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // EQ
  //                                          | PLUS_EQ
  //                                          | MINUS_EQ
  //                                          | STAR_EQ
  //                                          | DIV_EQ
  //                                          | MOD_EQ
  //                                          | REMAINDER_EQ
  //                                          | AND_EQ
  //                                          | OR_EQ
  //                                          | XOR_EQ
  //                                          | ANDAND_EQ
  //                                          | OROR_EQ
  //                                          | LSHIFT_EQ
  //                                          | RSHIFT_EQ
  //                                          | BITWISE_AND_NOT_EQ
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
  // [tagHead] expressionsList assignmentOperator expressionsList
  public static boolean assignmentStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "assignmentStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ASSIGNMENT_STATEMENT, "<assignment statement>");
    r = assignmentStatement_0(b, l + 1);
    r = r && expressionsList(b, l + 1);
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
  // AT IDENTIFIER_TOKEN
  //                                          | AT arguments
  public static boolean attributeStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "attributeStatement")) return false;
    if (!nextTokenIs(b, AT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = parseTokens(b, 0, AT, IDENTIFIER_TOKEN);
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
  // nilLiteral
  //                                          | stringLiteral
  //                                          | numericLiteral
  //                                          | booleanLiteral
  public static boolean basicLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "basicLiteral")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BASIC_LITERAL, "<basic literal>");
    r = nilLiteral(b, l + 1);
    if (!r) r = stringLiteral(b, l + 1);
    if (!r) r = numericLiteral(b, l + 1);
    if (!r) r = booleanLiteral(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // declaredIdentifier doubleColonOperator DISTINCT? bitSetType
  public static boolean bitsetDeclarationStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bitsetDeclarationStatement")) return false;
    if (!nextTokenIs(b, "<bitset declaration statement>", DOLLAR, IDENTIFIER_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BITSET_DECLARATION_STATEMENT, "<bitset declaration statement>");
    r = declaredIdentifier(b, l + 1);
    r = r && doubleColonOperator(b, l + 1);
    r = r && bitsetDeclarationStatement_2(b, l + 1);
    r = r && bitSetType(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // DISTINCT?
  private static boolean bitsetDeclarationStatement_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bitsetDeclarationStatement_2")) return false;
    consumeToken(b, DISTINCT);
    return true;
  }

  /* ********************************************************** */
  // [eos] directive* [eos] blockStart statementList? blockEnd
  public static boolean block(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "block")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BLOCK, "<block>");
    r = block_0(b, l + 1);
    r = r && block_1(b, l + 1);
    r = r && block_2(b, l + 1);
    r = r && blockStart(b, l + 1);
    r = r && block_4(b, l + 1);
    r = r && blockEnd(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // [eos]
  private static boolean block_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "block_0")) return false;
    eos(b, l + 1);
    return true;
  }

  // directive*
  private static boolean block_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "block_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!directive(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "block_1", c)) break;
    }
    return true;
  }

  // [eos]
  private static boolean block_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "block_2")) return false;
    eos(b, l + 1);
    return true;
  }

  // statementList?
  private static boolean block_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "block_4")) return false;
    statementList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // RBRACE
  public static boolean blockEnd(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "blockEnd")) return false;
    if (!nextTokenIs(b, RBRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, RBRACE);
    exit_section_(b, m, BLOCK_END, r);
    return r;
  }

  /* ********************************************************** */
  // LBRACE
  public static boolean blockStart(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "blockStart")) return false;
    if (!nextTokenIs(b, LBRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LBRACE);
    exit_section_(b, m, BLOCK_START, r);
    return r;
  }

  /* ********************************************************** */
  // [(directive eos?)|label] block
  public static boolean blockStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "blockStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BLOCK_STATEMENT, "<block statement>");
    r = blockStatement_0(b, l + 1);
    r = r && block(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // [(directive eos?)|label]
  private static boolean blockStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "blockStatement_0")) return false;
    blockStatement_0_0(b, l + 1);
    return true;
  }

  // (directive eos?)|label
  private static boolean blockStatement_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "blockStatement_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = blockStatement_0_0_0(b, l + 1);
    if (!r) r = label(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // directive eos?
  private static boolean blockStatement_0_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "blockStatement_0_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = directive(b, l + 1);
    r = r && blockStatement_0_0_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // eos?
  private static boolean blockStatement_0_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "blockStatement_0_0_0_1")) return false;
    eos(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // TRUE
  //                                          | FALSE
  public static boolean booleanLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "booleanLiteral")) return false;
    if (!nextTokenIs(b, "<boolean literal>", FALSE, TRUE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BOOLEAN_LITERAL, "<boolean literal>");
    r = consumeToken(b, TRUE);
    if (!r) r = consumeToken(b, FALSE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // BREAK IDENTIFIER_TOKEN?
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

  // IDENTIFIER_TOKEN?
  private static boolean breakStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "breakStatement_1")) return false;
    consumeToken(b, IDENTIFIER_TOKEN);
    return true;
  }

  /* ********************************************************** */
  // COLON statementList?
  public static boolean caseBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "caseBlock")) return false;
    if (!nextTokenIs(b, COLON)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COLON);
    r = r && caseBlock_1(b, l + 1);
    exit_section_(b, m, CASE_BLOCK, r);
    return r;
  }

  // statementList?
  private static boolean caseBlock_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "caseBlock_1")) return false;
    statementList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // COLON
  public static boolean colonClosing(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "colonClosing")) return false;
    if (!nextTokenIs(b, COLON)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COLON);
    exit_section_(b, m, COLON_CLOSING, r);
    return r;
  }

  /* ********************************************************** */
  // COLON
  public static boolean colonOpening(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "colonOpening")) return false;
    if (!nextTokenIs(b, COLON)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COLON);
    exit_section_(b, m, COLON_OPENING, r);
    return r;
  }

  /* ********************************************************** */
  // compoundType compoundValueTyped
  public static boolean compoundLiteralTyped(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compoundLiteralTyped")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, COMPOUND_LITERAL_TYPED, "<compound literal typed>");
    r = compoundType(b, l + 1);
    r = r && compoundValueTyped(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // compoundValueUntyped
  public static boolean compoundLiteralUntyped(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compoundLiteralUntyped")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, COMPOUND_LITERAL_UNTYPED, "<compound literal untyped>");
    r = compoundValueUntyped(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // arrayType | matrixType | bitSetType  | mapType | structType | qualifiedType | callType | simpleRefType | parExpressionType
  static boolean compoundType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compoundType")) return false;
    boolean r;
    r = arrayType(b, l + 1);
    if (!r) r = matrixType(b, l + 1);
    if (!r) r = bitSetType(b, l + 1);
    if (!r) r = mapType(b, l + 1);
    if (!r) r = structType(b, l + 1);
    if (!r) r = qualifiedType(b, l + 1);
    if (!r) r = callType(b, l + 1);
    if (!r) r = simpleRefType(b, l + 1);
    if (!r) r = parExpressionType(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // [expression EQ] expression (COMMA [expression EQ] expression)* [EOS_TOKEN|COMMA]
  public static boolean compoundValueBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compoundValueBody")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, COMPOUND_VALUE_BODY, "<compound value body>");
    r = compoundValueBody_0(b, l + 1);
    r = r && expression(b, l + 1, -1);
    r = r && compoundValueBody_2(b, l + 1);
    r = r && compoundValueBody_3(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // [expression EQ]
  private static boolean compoundValueBody_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compoundValueBody_0")) return false;
    compoundValueBody_0_0(b, l + 1);
    return true;
  }

  // expression EQ
  private static boolean compoundValueBody_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compoundValueBody_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = expression(b, l + 1, -1);
    r = r && consumeToken(b, EQ);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA [expression EQ] expression)*
  private static boolean compoundValueBody_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compoundValueBody_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!compoundValueBody_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "compoundValueBody_2", c)) break;
    }
    return true;
  }

  // COMMA [expression EQ] expression
  private static boolean compoundValueBody_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compoundValueBody_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && compoundValueBody_2_0_1(b, l + 1);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [expression EQ]
  private static boolean compoundValueBody_2_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compoundValueBody_2_0_1")) return false;
    compoundValueBody_2_0_1_0(b, l + 1);
    return true;
  }

  // expression EQ
  private static boolean compoundValueBody_2_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compoundValueBody_2_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = expression(b, l + 1, -1);
    r = r && consumeToken(b, EQ);
    exit_section_(b, m, null, r);
    return r;
  }

  // [EOS_TOKEN|COMMA]
  private static boolean compoundValueBody_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compoundValueBody_3")) return false;
    compoundValueBody_3_0(b, l + 1);
    return true;
  }

  // EOS_TOKEN|COMMA
  private static boolean compoundValueBody_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compoundValueBody_3_0")) return false;
    boolean r;
    r = consumeToken(b, EOS_TOKEN);
    if (!r) r = consumeToken(b, COMMA);
    return r;
  }

  /* ********************************************************** */
  // RBRACE
  public static boolean compoundValueEnd(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compoundValueEnd")) return false;
    if (!nextTokenIs(b, RBRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, RBRACE);
    exit_section_(b, m, COMPOUND_VALUE_END, r);
    return r;
  }

  /* ********************************************************** */
  // LBRACE
  public static boolean compoundValueStart(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compoundValueStart")) return false;
    if (!nextTokenIs(b, LBRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LBRACE);
    exit_section_(b, m, COMPOUND_VALUE_START, r);
    return r;
  }

  /* ********************************************************** */
  // ((<<isModeOn "PAR">> | <<isModeOff "NO_BLOCK">>) compoundValueStart [compoundValueBody] compoundValueEnd)
  //                                          | (compoundValueStart [compoundValueBody] compoundValueEnd <<beforeComma>>)
  public static boolean compoundValueTyped(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compoundValueTyped")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, COMPOUND_VALUE, "<compound value typed>");
    r = compoundValueTyped_0(b, l + 1);
    if (!r) r = compoundValueTyped_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (<<isModeOn "PAR">> | <<isModeOff "NO_BLOCK">>) compoundValueStart [compoundValueBody] compoundValueEnd
  private static boolean compoundValueTyped_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compoundValueTyped_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = compoundValueTyped_0_0(b, l + 1);
    r = r && compoundValueStart(b, l + 1);
    r = r && compoundValueTyped_0_2(b, l + 1);
    r = r && compoundValueEnd(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // <<isModeOn "PAR">> | <<isModeOff "NO_BLOCK">>
  private static boolean compoundValueTyped_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compoundValueTyped_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = isModeOn(b, l + 1, "PAR");
    if (!r) r = isModeOff(b, l + 1, "NO_BLOCK");
    exit_section_(b, m, null, r);
    return r;
  }

  // [compoundValueBody]
  private static boolean compoundValueTyped_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compoundValueTyped_0_2")) return false;
    compoundValueBody(b, l + 1);
    return true;
  }

  // compoundValueStart [compoundValueBody] compoundValueEnd <<beforeComma>>
  private static boolean compoundValueTyped_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compoundValueTyped_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = compoundValueStart(b, l + 1);
    r = r && compoundValueTyped_1_1(b, l + 1);
    r = r && compoundValueEnd(b, l + 1);
    r = r && beforeComma(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [compoundValueBody]
  private static boolean compoundValueTyped_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compoundValueTyped_1_1")) return false;
    compoundValueBody(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // ((<<isModeOn "OPERAND">> | <<isModeOn "PAR">> | <<isModeOff "NO_BLOCK">>) compoundValueStart [compoundValueBody] compoundValueEnd)
  //                                          | (compoundValueStart [compoundValueBody] compoundValueEnd <<beforeOperator>>)
  public static boolean compoundValueUntyped(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compoundValueUntyped")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, COMPOUND_VALUE, "<compound value untyped>");
    r = compoundValueUntyped_0(b, l + 1);
    if (!r) r = compoundValueUntyped_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (<<isModeOn "OPERAND">> | <<isModeOn "PAR">> | <<isModeOff "NO_BLOCK">>) compoundValueStart [compoundValueBody] compoundValueEnd
  private static boolean compoundValueUntyped_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compoundValueUntyped_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = compoundValueUntyped_0_0(b, l + 1);
    r = r && compoundValueStart(b, l + 1);
    r = r && compoundValueUntyped_0_2(b, l + 1);
    r = r && compoundValueEnd(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // <<isModeOn "OPERAND">> | <<isModeOn "PAR">> | <<isModeOff "NO_BLOCK">>
  private static boolean compoundValueUntyped_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compoundValueUntyped_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = isModeOn(b, l + 1, "OPERAND");
    if (!r) r = isModeOn(b, l + 1, "PAR");
    if (!r) r = isModeOff(b, l + 1, "NO_BLOCK");
    exit_section_(b, m, null, r);
    return r;
  }

  // [compoundValueBody]
  private static boolean compoundValueUntyped_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compoundValueUntyped_0_2")) return false;
    compoundValueBody(b, l + 1);
    return true;
  }

  // compoundValueStart [compoundValueBody] compoundValueEnd <<beforeOperator>>
  private static boolean compoundValueUntyped_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compoundValueUntyped_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = compoundValueStart(b, l + 1);
    r = r && compoundValueUntyped_1_1(b, l + 1);
    r = r && compoundValueEnd(b, l + 1);
    r = r && beforeOperator(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [compoundValueBody]
  private static boolean compoundValueUntyped_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compoundValueUntyped_1_1")) return false;
    compoundValueBody(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // expression
  public static boolean condition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "condition")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CONDITION, "<condition>");
    r = expression(b, l + 1, -1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // <<enterNoBlockMode>> [[controlFlowInit] SEMICOLON] condition <<exitNoBlockMode>>
  static boolean conditionalHead(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "conditionalHead")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = enterNoBlockMode(b, l + 1);
    r = r && conditionalHead_1(b, l + 1);
    r = r && condition(b, l + 1);
    r = r && exitNoBlockMode(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [[controlFlowInit] SEMICOLON]
  private static boolean conditionalHead_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "conditionalHead_1")) return false;
    conditionalHead_1_0(b, l + 1);
    return true;
  }

  // [controlFlowInit] SEMICOLON
  private static boolean conditionalHead_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "conditionalHead_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = conditionalHead_1_0_0(b, l + 1);
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, m, null, r);
    return r;
  }

  // [controlFlowInit]
  private static boolean conditionalHead_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "conditionalHead_1_0_0")) return false;
    controlFlowInit(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // [label] [tagHead] ifBlock (sos elseIfBlock)* [sos elseBlock]
  public static boolean conditionalStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "conditionalStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CONDITIONAL_STATEMENT, "<conditional statement>");
    r = conditionalStatement_0(b, l + 1);
    r = r && conditionalStatement_1(b, l + 1);
    r = r && ifBlock(b, l + 1);
    r = r && conditionalStatement_3(b, l + 1);
    r = r && conditionalStatement_4(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // [label]
  private static boolean conditionalStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "conditionalStatement_0")) return false;
    label(b, l + 1);
    return true;
  }

  // [tagHead]
  private static boolean conditionalStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "conditionalStatement_1")) return false;
    tagHead(b, l + 1);
    return true;
  }

  // (sos elseIfBlock)*
  private static boolean conditionalStatement_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "conditionalStatement_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!conditionalStatement_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "conditionalStatement_3", c)) break;
    }
    return true;
  }

  // sos elseIfBlock
  private static boolean conditionalStatement_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "conditionalStatement_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = sos(b, l + 1);
    r = r && elseIfBlock(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [sos elseBlock]
  private static boolean conditionalStatement_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "conditionalStatement_4")) return false;
    conditionalStatement_4_0(b, l + 1);
    return true;
  }

  // sos elseBlock
  private static boolean conditionalStatement_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "conditionalStatement_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = sos(b, l + 1);
    r = r && elseBlock(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // symbolDefinitionHead colonClosing expressionsList
  public static boolean constantInitializationStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constantInitializationStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, CONSTANT_INITIALIZATION_STATEMENT, "<constant initialization statement>");
    r = symbolDefinitionHead(b, l + 1);
    r = r && colonClosing(b, l + 1);
    r = r && expressionsList(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // CONTINUE IDENTIFIER_TOKEN?
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

  // IDENTIFIER_TOKEN?
  private static boolean continueStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "continueStatement_1")) return false;
    consumeToken(b, IDENTIFIER_TOKEN);
    return true;
  }

  /* ********************************************************** */
  // assignmentStatement
  //                                          | variableInitializationStatement
  //                                          | variableDeclarationStatement
  //                                          | callExpression
  public static boolean controlFlowInit(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "controlFlowInit")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CONTROL_FLOW_INIT, "<control flow init>");
    r = assignmentStatement(b, l + 1);
    if (!r) r = variableInitializationStatement(b, l + 1);
    if (!r) r = variableDeclarationStatement(b, l + 1);
    if (!r) r = expression(b, l + 1, 12);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // [DOLLAR] IDENTIFIER_TOKEN
  public static boolean declaredIdentifier(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "declaredIdentifier")) return false;
    if (!nextTokenIs(b, "<declared identifier>", DOLLAR, IDENTIFIER_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DECLARED_IDENTIFIER, "<declared identifier>");
    r = declaredIdentifier_0(b, l + 1);
    r = r && consumeToken(b, IDENTIFIER_TOKEN);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // [DOLLAR]
  private static boolean declaredIdentifier_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "declaredIdentifier_0")) return false;
    consumeToken(b, DOLLAR);
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
  //                                          | expression
  //                                          | conditionalStatement
  //                                          | forStatement
  //                                          | switchStatement
  //                                          | block
  static boolean deferrableStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "deferrableStatement")) return false;
    boolean r;
    r = assignmentStatement(b, l + 1);
    if (!r) r = expression(b, l + 1, -1);
    if (!r) r = conditionalStatement(b, l + 1);
    if (!r) r = forStatement(b, l + 1);
    if (!r) r = switchStatement(b, l + 1);
    if (!r) r = block(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // tagHead [basicLiteral | (LPAREN expressionsList? RPAREN)]
  public static boolean directive(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive")) return false;
    if (!nextTokenIs(b, HASH)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = tagHead(b, l + 1);
    r = r && directive_1(b, l + 1);
    exit_section_(b, m, DIRECTIVE, r);
    return r;
  }

  // [basicLiteral | (LPAREN expressionsList? RPAREN)]
  private static boolean directive_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_1")) return false;
    directive_1_0(b, l + 1);
    return true;
  }

  // basicLiteral | (LPAREN expressionsList? RPAREN)
  private static boolean directive_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = basicLiteral(b, l + 1);
    if (!r) r = directive_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // LPAREN expressionsList? RPAREN
  private static boolean directive_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_1_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && directive_1_0_1_1(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // expressionsList?
  private static boolean directive_1_0_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_1_0_1_1")) return false;
    expressionsList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // directive
  public static boolean directiveStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directiveStatement")) return false;
    if (!nextTokenIs(b, HASH)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = directive(b, l + 1);
    exit_section_(b, m, DIRECTIVE_STATEMENT, r);
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
  // COLON COLON
  public static boolean doubleColonOperator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "doubleColonOperator")) return false;
    if (!nextTokenIs(b, COLON)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, COLON, COLON);
    exit_section_(b, m, DOUBLE_COLON_OPERATOR, r);
    return r;
  }

  /* ********************************************************** */
  // ELSE_TOKEN
  public static boolean else_$(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "else_$")) return false;
    if (!nextTokenIs(b, ELSE_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ELSE_TOKEN);
    exit_section_(b, m, ELSE, r);
    return r;
  }

  /* ********************************************************** */
  // else statementBody
  public static boolean elseBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "elseBlock")) return false;
    if (!nextTokenIs(b, ELSE_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = else_$(b, l + 1);
    r = r && statementBody(b, l + 1);
    exit_section_(b, m, ELSE_BLOCK, r);
    return r;
  }

  /* ********************************************************** */
  // else if
  public static boolean elseIf(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "elseIf")) return false;
    if (!nextTokenIs(b, ELSE_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = else_$(b, l + 1);
    r = r && if_$(b, l + 1);
    exit_section_(b, m, ELSE_IF, r);
    return r;
  }

  /* ********************************************************** */
  // elseIf conditionalHead statementBody
  public static boolean elseIfBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "elseIfBlock")) return false;
    if (!nextTokenIs(b, ELSE_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = elseIf(b, l + 1);
    r = r && conditionalHead(b, l + 1);
    r = r && statementBody(b, l + 1);
    exit_section_(b, m, ELSE_IF_BLOCK, r);
    return r;
  }

  /* ********************************************************** */
  // else WHEN <<enterNoBlockMode>>  condition <<exitNoBlockMode>> statementBody
  public static boolean elseWhenBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "elseWhenBlock")) return false;
    if (!nextTokenIs(b, ELSE_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = else_$(b, l + 1);
    r = r && consumeToken(b, WHEN);
    r = r && enterNoBlockMode(b, l + 1);
    r = r && condition(b, l + 1);
    r = r && exitNoBlockMode(b, l + 1);
    r = r && statementBody(b, l + 1);
    exit_section_(b, m, ELSE_WHEN_BLOCK, r);
    return r;
  }

  /* ********************************************************** */
  // blockStart [enumBody] blockEnd
  public static boolean enumBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumBlock")) return false;
    if (!nextTokenIs(b, LBRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = blockStart(b, l + 1);
    r = r && enumBlock_1(b, l + 1);
    r = r && blockEnd(b, l + 1);
    exit_section_(b, m, ENUM_BLOCK, r);
    return r;
  }

  // [enumBody]
  private static boolean enumBlock_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumBlock_1")) return false;
    enumBody(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // enumValueDeclaration (COMMA enumValueDeclaration)* [EOS_TOKEN|COMMA]
  public static boolean enumBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumBody")) return false;
    if (!nextTokenIs(b, "<enum body>", DOLLAR, IDENTIFIER_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ENUM_BODY, "<enum body>");
    r = enumValueDeclaration(b, l + 1);
    r = r && enumBody_1(b, l + 1);
    r = r && enumBody_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (COMMA enumValueDeclaration)*
  private static boolean enumBody_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumBody_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!enumBody_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "enumBody_1", c)) break;
    }
    return true;
  }

  // COMMA enumValueDeclaration
  private static boolean enumBody_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumBody_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && enumValueDeclaration(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [EOS_TOKEN|COMMA]
  private static boolean enumBody_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumBody_2")) return false;
    enumBody_2_0(b, l + 1);
    return true;
  }

  // EOS_TOKEN|COMMA
  private static boolean enumBody_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumBody_2_0")) return false;
    boolean r;
    r = consumeToken(b, EOS_TOKEN);
    if (!r) r = consumeToken(b, COMMA);
    return r;
  }

  /* ********************************************************** */
  // (attributeStatement eos?)* USING? declaredIdentifier doubleColonOperator DISTINCT? enumType
  public static boolean enumDeclarationStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumDeclarationStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, ENUM_DECLARATION_STATEMENT, "<enum declaration statement>");
    r = enumDeclarationStatement_0(b, l + 1);
    r = r && enumDeclarationStatement_1(b, l + 1);
    r = r && declaredIdentifier(b, l + 1);
    r = r && doubleColonOperator(b, l + 1);
    r = r && enumDeclarationStatement_4(b, l + 1);
    r = r && enumType(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (attributeStatement eos?)*
  private static boolean enumDeclarationStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumDeclarationStatement_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!enumDeclarationStatement_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "enumDeclarationStatement_0", c)) break;
    }
    return true;
  }

  // attributeStatement eos?
  private static boolean enumDeclarationStatement_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumDeclarationStatement_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = attributeStatement(b, l + 1);
    r = r && enumDeclarationStatement_0_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // eos?
  private static boolean enumDeclarationStatement_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumDeclarationStatement_0_0_1")) return false;
    eos(b, l + 1);
    return true;
  }

  // USING?
  private static boolean enumDeclarationStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumDeclarationStatement_1")) return false;
    consumeToken(b, USING);
    return true;
  }

  // DISTINCT?
  private static boolean enumDeclarationStatement_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumDeclarationStatement_4")) return false;
    consumeToken(b, DISTINCT);
    return true;
  }

  /* ********************************************************** */
  // declaredIdentifier [EQ expression]
  public static boolean enumValueDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumValueDeclaration")) return false;
    if (!nextTokenIs(b, "<enum value declaration>", DOLLAR, IDENTIFIER_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ENUM_VALUE_DECLARATION, "<enum value declaration>");
    r = declaredIdentifier(b, l + 1);
    r = r && enumValueDeclaration_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // [EQ expression]
  private static boolean enumValueDeclaration_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumValueDeclaration_1")) return false;
    enumValueDeclaration_1_0(b, l + 1);
    return true;
  }

  // EQ expression
  private static boolean enumValueDeclaration_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumValueDeclaration_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, EQ);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // SEMICOLON | <<eof>> | EOS_TOKEN | <<multilineBlockComment>> | <<atClosingBrace>> | <<afterClosingBrace>>
  public static boolean eos(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "eos")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, EOS, "<eos>");
    r = consumeToken(b, SEMICOLON);
    if (!r) r = eof(b, l + 1);
    if (!r) r = consumeToken(b, EOS_TOKEN);
    if (!r) r = multilineBlockComment(b, l + 1);
    if (!r) r = atClosingBrace(b, l + 1);
    if (!r) r = afterClosingBrace(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // orReturnExpression
  //                                           | orBreakExpression
  //                                           | orContinueExpression
  //                                           | callExpression
  //                                           | qualificationExpression
  //                                           | primaryGroup
  public static boolean expressionStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expressionStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, EXPRESSION_STATEMENT, "<expression statement>");
    r = expression(b, l + 1, -1);
    if (!r) r = expression(b, l + 1, -1);
    if (!r) r = expression(b, l + 1, -1);
    if (!r) r = expression(b, l + 1, 12);
    if (!r) r = expression(b, l + 1, 13);
    if (!r) r = expression(b, l + 1, 22);
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
  // USING? tagHead? declaredIdentifier (COMMA declaredIdentifier)* COLON typeDefinitionExpression [RAW_STRING_LITERAL]
  public static boolean fieldDeclarationStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldDeclarationStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FIELD_DECLARATION_STATEMENT, "<field declaration statement>");
    r = fieldDeclarationStatement_0(b, l + 1);
    r = r && fieldDeclarationStatement_1(b, l + 1);
    r = r && declaredIdentifier(b, l + 1);
    r = r && fieldDeclarationStatement_3(b, l + 1);
    r = r && consumeToken(b, COLON);
    r = r && typeDefinitionExpression(b, l + 1);
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

  // (COMMA declaredIdentifier)*
  private static boolean fieldDeclarationStatement_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldDeclarationStatement_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!fieldDeclarationStatement_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "fieldDeclarationStatement_3", c)) break;
    }
    return true;
  }

  // COMMA declaredIdentifier
  private static boolean fieldDeclarationStatement_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldDeclarationStatement_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && declaredIdentifier(b, l + 1);
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
  // packageDeclaration eos importStatements fileScopeStatementList <<eof>>
  public static boolean fileScope(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fileScope")) return false;
    if (!nextTokenIs(b, PACKAGE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = packageDeclaration(b, l + 1);
    r = r && eos(b, l + 1);
    r = r && importStatements(b, l + 1);
    r = r && fileScopeStatementList(b, l + 1);
    r = r && eof(b, l + 1);
    exit_section_(b, m, FILE_SCOPE, r);
    return r;
  }

  /* ********************************************************** */
  // foreignImportDeclarationStatement
  //                                          | importDeclarationStatement
  //                                          | enumDeclarationStatement
  //                                          | unionDeclarationStatement
  //                                          | structDeclarationStatement
  //                                          | procedureDeclarationStatement
  //                                          | constantInitializationStatement
  //                                          | bitsetDeclarationStatement
  //                                          | variableInitializationStatement
  //                                          | variableDeclarationStatement
  //                                          | procedureOverloadDeclarationStatement
  //                                          | foreignStatement
  //                                          | whenStatement
  //                                          | directiveStatement
  public static boolean fileScopeStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fileScopeStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, FILE_SCOPE_STATEMENT, "<file scope statement>");
    r = foreignImportDeclarationStatement(b, l + 1);
    if (!r) r = importDeclarationStatement(b, l + 1);
    if (!r) r = enumDeclarationStatement(b, l + 1);
    if (!r) r = unionDeclarationStatement(b, l + 1);
    if (!r) r = structDeclarationStatement(b, l + 1);
    if (!r) r = procedureDeclarationStatement(b, l + 1);
    if (!r) r = constantInitializationStatement(b, l + 1);
    if (!r) r = bitsetDeclarationStatement(b, l + 1);
    if (!r) r = variableInitializationStatement(b, l + 1);
    if (!r) r = variableDeclarationStatement(b, l + 1);
    if (!r) r = procedureOverloadDeclarationStatement(b, l + 1);
    if (!r) r = foreignStatement(b, l + 1);
    if (!r) r = whenStatement(b, l + 1);
    if (!r) r = directiveStatement(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // (fileScopeStatement eos)*
  static boolean fileScopeStatementList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fileScopeStatementList")) return false;
    while (true) {
      int c = current_position_(b);
      if (!fileScopeStatementList_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "fileScopeStatementList", c)) break;
    }
    return true;
  }

  // fileScopeStatement eos
  private static boolean fileScopeStatementList_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fileScopeStatementList_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = fileScopeStatement(b, l + 1);
    r = r && eos(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // FOR forHead statementBody
  public static boolean forBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forBlock")) return false;
    if (!nextTokenIs(b, FOR)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, FOR);
    r = r && forHead(b, l + 1);
    r = r && statementBody(b, l + 1);
    exit_section_(b, m, FOR_BLOCK, r);
    return r;
  }

  /* ********************************************************** */
  // <<enterNoBlockMode>> [[[controlFlowInit] SEMICOLON] condition? [SEMICOLON [forUpdate]]] <<exitNoBlockMode>>
  static boolean forHead(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forHead")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = enterNoBlockMode(b, l + 1);
    r = r && forHead_1(b, l + 1);
    r = r && exitNoBlockMode(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [[[controlFlowInit] SEMICOLON] condition? [SEMICOLON [forUpdate]]]
  private static boolean forHead_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forHead_1")) return false;
    forHead_1_0(b, l + 1);
    return true;
  }

  // [[controlFlowInit] SEMICOLON] condition? [SEMICOLON [forUpdate]]
  private static boolean forHead_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forHead_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = forHead_1_0_0(b, l + 1);
    r = r && forHead_1_0_1(b, l + 1);
    r = r && forHead_1_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [[controlFlowInit] SEMICOLON]
  private static boolean forHead_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forHead_1_0_0")) return false;
    forHead_1_0_0_0(b, l + 1);
    return true;
  }

  // [controlFlowInit] SEMICOLON
  private static boolean forHead_1_0_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forHead_1_0_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = forHead_1_0_0_0_0(b, l + 1);
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, m, null, r);
    return r;
  }

  // [controlFlowInit]
  private static boolean forHead_1_0_0_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forHead_1_0_0_0_0")) return false;
    controlFlowInit(b, l + 1);
    return true;
  }

  // condition?
  private static boolean forHead_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forHead_1_0_1")) return false;
    condition(b, l + 1);
    return true;
  }

  // [SEMICOLON [forUpdate]]
  private static boolean forHead_1_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forHead_1_0_2")) return false;
    forHead_1_0_2_0(b, l + 1);
    return true;
  }

  // SEMICOLON [forUpdate]
  private static boolean forHead_1_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forHead_1_0_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SEMICOLON);
    r = r && forHead_1_0_2_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [forUpdate]
  private static boolean forHead_1_0_2_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forHead_1_0_2_0_1")) return false;
    forUpdate(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // FOR <<enterNoBlockMode>> [forInParameter (COMMA forInParameter)*] IN expression <<exitNoBlockMode>> statementBody
  public static boolean forInBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forInBlock")) return false;
    if (!nextTokenIs(b, FOR)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, FOR);
    r = r && enterNoBlockMode(b, l + 1);
    r = r && forInBlock_2(b, l + 1);
    r = r && consumeToken(b, IN);
    r = r && expression(b, l + 1, -1);
    r = r && exitNoBlockMode(b, l + 1);
    r = r && statementBody(b, l + 1);
    exit_section_(b, m, FOR_IN_BLOCK, r);
    return r;
  }

  // [forInParameter (COMMA forInParameter)*]
  private static boolean forInBlock_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forInBlock_2")) return false;
    forInBlock_2_0(b, l + 1);
    return true;
  }

  // forInParameter (COMMA forInParameter)*
  private static boolean forInBlock_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forInBlock_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = forInParameter(b, l + 1);
    r = r && forInBlock_2_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA forInParameter)*
  private static boolean forInBlock_2_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forInBlock_2_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!forInBlock_2_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "forInBlock_2_0_1", c)) break;
    }
    return true;
  }

  // COMMA forInParameter
  private static boolean forInBlock_2_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forInBlock_2_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && forInParameter(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // AND? declaredIdentifier
  public static boolean forInParameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forInParameter")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FOR_IN_PARAMETER, "<for in parameter>");
    r = forInParameter_0(b, l + 1);
    r = r && declaredIdentifier(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // AND?
  private static boolean forInParameter_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forInParameter_0")) return false;
    consumeToken(b, AND);
    return true;
  }

  /* ********************************************************** */
  // label? directive? forInBlock
  public static boolean forInStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forInStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FOR_IN_STATEMENT, "<for in statement>");
    r = forInStatement_0(b, l + 1);
    r = r && forInStatement_1(b, l + 1);
    r = r && forInBlock(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // label?
  private static boolean forInStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forInStatement_0")) return false;
    label(b, l + 1);
    return true;
  }

  // directive?
  private static boolean forInStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forInStatement_1")) return false;
    directive(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // label? directive? forBlock
  public static boolean forStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FOR_STATEMENT, "<for statement>");
    r = forStatement_0(b, l + 1);
    r = r && forStatement_1(b, l + 1);
    r = r && forBlock(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // label?
  private static boolean forStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forStatement_0")) return false;
    label(b, l + 1);
    return true;
  }

  // directive?
  private static boolean forStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forStatement_1")) return false;
    directive(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // assignmentStatement | callExpression
  public static boolean forUpdate(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forUpdate")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FOR_UPDATE, "<for update>");
    r = assignmentStatement(b, l + 1);
    if (!r) r = expression(b, l + 1, 12);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // blockStart foreignStatementList? blockEnd
  public static boolean foreignBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignBlock")) return false;
    if (!nextTokenIs(b, LBRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = blockStart(b, l + 1);
    r = r && foreignBlock_1(b, l + 1);
    r = r && blockEnd(b, l + 1);
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
  //                                          |procedureDeclarationStatement
  //                                          |whenStatement
  public static boolean foreignBlockStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignBlockStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, FOREIGN_BLOCK_STATEMENT, "<foreign block statement>");
    r = variableDeclarationStatement(b, l + 1);
    if (!r) r = procedureDeclarationStatement(b, l + 1);
    if (!r) r = whenStatement(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // (attributeStatement EOS_TOKEN?)* FOREIGN IMPORT
  //         ( (declaredIdentifier? DQ_STRING_LITERAL) | (declaredIdentifier? blockStart DQ_STRING_LITERAL (COMMA DQ_STRING_LITERAL)* COMMA? blockEnd))
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

  // (attributeStatement EOS_TOKEN?)*
  private static boolean foreignImportDeclarationStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignImportDeclarationStatement_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!foreignImportDeclarationStatement_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "foreignImportDeclarationStatement_0", c)) break;
    }
    return true;
  }

  // attributeStatement EOS_TOKEN?
  private static boolean foreignImportDeclarationStatement_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignImportDeclarationStatement_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = attributeStatement(b, l + 1);
    r = r && foreignImportDeclarationStatement_0_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // EOS_TOKEN?
  private static boolean foreignImportDeclarationStatement_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignImportDeclarationStatement_0_0_1")) return false;
    consumeToken(b, EOS_TOKEN);
    return true;
  }

  // (declaredIdentifier? DQ_STRING_LITERAL) | (declaredIdentifier? blockStart DQ_STRING_LITERAL (COMMA DQ_STRING_LITERAL)* COMMA? blockEnd)
  private static boolean foreignImportDeclarationStatement_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignImportDeclarationStatement_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = foreignImportDeclarationStatement_3_0(b, l + 1);
    if (!r) r = foreignImportDeclarationStatement_3_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // declaredIdentifier? DQ_STRING_LITERAL
  private static boolean foreignImportDeclarationStatement_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignImportDeclarationStatement_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = foreignImportDeclarationStatement_3_0_0(b, l + 1);
    r = r && consumeToken(b, DQ_STRING_LITERAL);
    exit_section_(b, m, null, r);
    return r;
  }

  // declaredIdentifier?
  private static boolean foreignImportDeclarationStatement_3_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignImportDeclarationStatement_3_0_0")) return false;
    declaredIdentifier(b, l + 1);
    return true;
  }

  // declaredIdentifier? blockStart DQ_STRING_LITERAL (COMMA DQ_STRING_LITERAL)* COMMA? blockEnd
  private static boolean foreignImportDeclarationStatement_3_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignImportDeclarationStatement_3_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = foreignImportDeclarationStatement_3_1_0(b, l + 1);
    r = r && blockStart(b, l + 1);
    r = r && consumeToken(b, DQ_STRING_LITERAL);
    r = r && foreignImportDeclarationStatement_3_1_3(b, l + 1);
    r = r && foreignImportDeclarationStatement_3_1_4(b, l + 1);
    r = r && blockEnd(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // declaredIdentifier?
  private static boolean foreignImportDeclarationStatement_3_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignImportDeclarationStatement_3_1_0")) return false;
    declaredIdentifier(b, l + 1);
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
  // (attributeStatement EOS_TOKEN?)* FOREIGN IDENTIFIER_TOKEN? foreignBlock
  public static boolean foreignStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignStatement")) return false;
    if (!nextTokenIs(b, "<foreign statement>", AT, FOREIGN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, FOREIGN_STATEMENT, "<foreign statement>");
    r = foreignStatement_0(b, l + 1);
    r = r && consumeToken(b, FOREIGN);
    r = r && foreignStatement_2(b, l + 1);
    r = r && foreignBlock(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (attributeStatement EOS_TOKEN?)*
  private static boolean foreignStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignStatement_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!foreignStatement_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "foreignStatement_0", c)) break;
    }
    return true;
  }

  // attributeStatement EOS_TOKEN?
  private static boolean foreignStatement_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignStatement_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = attributeStatement(b, l + 1);
    r = r && foreignStatement_0_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // EOS_TOKEN?
  private static boolean foreignStatement_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignStatement_0_0_1")) return false;
    consumeToken(b, EOS_TOKEN);
    return true;
  }

  // IDENTIFIER_TOKEN?
  private static boolean foreignStatement_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignStatement_2")) return false;
    consumeToken(b, IDENTIFIER_TOKEN);
    return true;
  }

  /* ********************************************************** */
  // (sos? foreignBlockStatement eos)+
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

  // sos? foreignBlockStatement eos
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

  // sos?
  private static boolean foreignStatementList_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignStatementList_0_0")) return false;
    sos(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // IDENTIFIER_TOKEN
  public static boolean identifier(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "identifier")) return false;
    if (!nextTokenIs(b, IDENTIFIER_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER_TOKEN);
    exit_section_(b, m, IDENTIFIER, r);
    return r;
  }

  /* ********************************************************** */
  // declaredIdentifier (COMMA declaredIdentifier)*
  public static boolean identifierList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "identifierList")) return false;
    if (!nextTokenIs(b, "<identifier list>", DOLLAR, IDENTIFIER_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, IDENTIFIER_LIST, "<identifier list>");
    r = declaredIdentifier(b, l + 1);
    r = r && identifierList_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (COMMA declaredIdentifier)*
  private static boolean identifierList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "identifierList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!identifierList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "identifierList_1", c)) break;
    }
    return true;
  }

  // COMMA declaredIdentifier
  private static boolean identifierList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "identifierList_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && declaredIdentifier(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // IF_TOKEN
  public static boolean if_$(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "if_$")) return false;
    if (!nextTokenIs(b, IF_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IF_TOKEN);
    exit_section_(b, m, IF, r);
    return r;
  }

  /* ********************************************************** */
  // if conditionalHead statementBody
  public static boolean ifBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifBlock")) return false;
    if (!nextTokenIs(b, IF_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = if_$(b, l + 1);
    r = r && conditionalHead(b, l + 1);
    r = r && statementBody(b, l + 1);
    exit_section_(b, m, IF_BLOCK, r);
    return r;
  }

  /* ********************************************************** */
  // IMPORT declaredIdentifier? DQ_STRING_LITERAL
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

  // declaredIdentifier?
  private static boolean importDeclarationStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "importDeclarationStatement_1")) return false;
    declaredIdentifier(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // (importDeclarationStatement eos)*
  static boolean importStatements(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "importStatements")) return false;
    while (true) {
      int c = current_position_(b);
      if (!importStatements_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "importStatements", c)) break;
    }
    return true;
  }

  // importDeclarationStatement eos
  private static boolean importStatements_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "importStatements_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = importDeclarationStatement(b, l + 1);
    r = r && eos(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // LBRACKET expression [COMMA expression] RBRACKET
  public static boolean index(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "index")) return false;
    if (!nextTokenIs(b, LBRACKET)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LBRACKET);
    r = r && expression(b, l + 1, -1);
    r = r && index_2(b, l + 1);
    r = r && consumeToken(b, RBRACKET);
    exit_section_(b, m, INDEX, r);
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
  // IDENTIFIER_TOKEN COLON
  public static boolean label(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "label")) return false;
    if (!nextTokenIs(b, IDENTIFIER_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, IDENTIFIER_TOKEN, COLON);
    exit_section_(b, m, LABEL, r);
    return r;
  }

  /* ********************************************************** */
  // IDENTIFIER_TOKEN EQ expression
  public static boolean namedArgument(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namedArgument")) return false;
    if (!nextTokenIs(b, IDENTIFIER_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, IDENTIFIER_TOKEN, EQ);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, NAMED_ARGUMENT, r);
    return r;
  }

  /* ********************************************************** */
  // NIL
  public static boolean nilLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "nilLiteral")) return false;
    if (!nextTokenIs(b, NIL)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NIL);
    exit_section_(b, m, NIL_LITERAL, r);
    return r;
  }

  /* ********************************************************** */
  // INTEGER_DEC_LITERAL
  //                                          | INTEGER_HEX_LITERAL
  //                                          | INTEGER_OCT_LITERAL
  //                                          | INTEGER_BIN_LITERAL
  //                                          | FLOAT_DEC_LITERAL
  //                                          | COMPLEX_INTEGER_DEC_LITERAL
  //                                          | COMPLEX_FLOAT_LITERAL
  public static boolean numericLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "numericLiteral")) return false;
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
  // fileScope
  static boolean odinFile(PsiBuilder b, int l) {
    return fileScope(b, l + 1);
  }

  /* ********************************************************** */
  // PACKAGE declaredIdentifier
  public static boolean packageDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "packageDeclaration")) return false;
    if (!nextTokenIs(b, PACKAGE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PACKAGE);
    r = r && declaredIdentifier(b, l + 1);
    exit_section_(b, m, PACKAGE_DECLARATION, r);
    return r;
  }

  /* ********************************************************** */
  // paramEntry (COMMA paramEntry)* COMMA?
  static boolean paramEntries(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "paramEntries")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = paramEntry(b, l + 1);
    r = r && paramEntries_1(b, l + 1);
    r = r && paramEntries_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA paramEntry)*
  private static boolean paramEntries_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "paramEntries_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!paramEntries_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "paramEntries_1", c)) break;
    }
    return true;
  }

  // COMMA paramEntry
  private static boolean paramEntries_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "paramEntries_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && paramEntry(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // COMMA?
  private static boolean paramEntries_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "paramEntries_2")) return false;
    consumeToken(b, COMMA);
    return true;
  }

  /* ********************************************************** */
  // directive? (
  //                                              parameterInitialization
  //                                              | parameterDecl
  //                                              | unnamedParameter
  //                                          )
  public static boolean paramEntry(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "paramEntry")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PARAM_ENTRY, "<param entry>");
    r = paramEntry_0(b, l + 1);
    r = r && paramEntry_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // directive?
  private static boolean paramEntry_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "paramEntry_0")) return false;
    directive(b, l + 1);
    return true;
  }

  // parameterInitialization
  //                                              | parameterDecl
  //                                              | unnamedParameter
  private static boolean paramEntry_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "paramEntry_1")) return false;
    boolean r;
    r = parameterInitialization(b, l + 1);
    if (!r) r = parameterDecl(b, l + 1);
    if (!r) r = unnamedParameter(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // [USING] directive? declaredIdentifier
  public static boolean parameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PARAMETER, "<parameter>");
    r = parameter_0(b, l + 1);
    r = r && parameter_1(b, l + 1);
    r = r && declaredIdentifier(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // [USING]
  private static boolean parameter_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_0")) return false;
    consumeToken(b, USING);
    return true;
  }

  // directive?
  private static boolean parameter_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_1")) return false;
    directive(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // parameter (COMMA parameter)* COLON typeDefinitionContainer
  public static boolean parameterDecl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterDecl")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PARAMETER_DECL, "<parameter decl>");
    r = parameter(b, l + 1);
    r = r && parameterDecl_1(b, l + 1);
    r = r && consumeToken(b, COLON);
    r = r && typeDefinitionContainer(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (COMMA parameter)*
  private static boolean parameterDecl_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterDecl_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!parameterDecl_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "parameterDecl_1", c)) break;
    }
    return true;
  }

  // COMMA parameter
  private static boolean parameterDecl_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterDecl_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && parameter(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // parameter COLON [typeDefinitionContainer] EQ expression
  public static boolean parameterInitialization(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterInitialization")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PARAMETER_INITIALIZATION, "<parameter initialization>");
    r = parameter(b, l + 1);
    r = r && consumeToken(b, COLON);
    r = r && parameterInitialization_2(b, l + 1);
    r = r && consumeToken(b, EQ);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // [typeDefinitionContainer]
  private static boolean parameterInitialization_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterInitialization_2")) return false;
    typeDefinitionContainer(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // paramEntries
  static boolean polymorphicParameterList(PsiBuilder b, int l) {
    return paramEntries(b, l + 1);
  }

  /* ********************************************************** */
  // tripleDashBlock
  //                                           | block
  public static boolean procedureBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureBody")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PROCEDURE_BODY, "<procedure body>");
    r = tripleDashBlock(b, l + 1);
    if (!r) r = block(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // (attributeStatement eos?)* declaredIdentifier doubleColonOperator procedureType procedureBody
  public static boolean procedureDeclarationStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureDeclarationStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, PROCEDURE_DECLARATION_STATEMENT, "<procedure declaration statement>");
    r = procedureDeclarationStatement_0(b, l + 1);
    r = r && declaredIdentifier(b, l + 1);
    r = r && doubleColonOperator(b, l + 1);
    r = r && procedureType(b, l + 1);
    r = r && procedureBody(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (attributeStatement eos?)*
  private static boolean procedureDeclarationStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureDeclarationStatement_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!procedureDeclarationStatement_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "procedureDeclarationStatement_0", c)) break;
    }
    return true;
  }

  // attributeStatement eos?
  private static boolean procedureDeclarationStatement_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureDeclarationStatement_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = attributeStatement(b, l + 1);
    r = r && procedureDeclarationStatement_0_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // eos?
  private static boolean procedureDeclarationStatement_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureDeclarationStatement_0_0_1")) return false;
    eos(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // (attributeStatement eos?)* declaredIdentifier doubleColonOperator PROC LBRACE identifier (COMMA identifier)* COMMA? RBRACE
  public static boolean procedureOverloadDeclarationStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureOverloadDeclarationStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, PROCEDURE_OVERLOAD_DECLARATION_STATEMENT, "<procedure overload declaration statement>");
    r = procedureOverloadDeclarationStatement_0(b, l + 1);
    r = r && declaredIdentifier(b, l + 1);
    r = r && doubleColonOperator(b, l + 1);
    r = r && consumeTokens(b, 0, PROC, LBRACE);
    r = r && identifier(b, l + 1);
    r = r && procedureOverloadDeclarationStatement_6(b, l + 1);
    r = r && procedureOverloadDeclarationStatement_7(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (attributeStatement eos?)*
  private static boolean procedureOverloadDeclarationStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureOverloadDeclarationStatement_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!procedureOverloadDeclarationStatement_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "procedureOverloadDeclarationStatement_0", c)) break;
    }
    return true;
  }

  // attributeStatement eos?
  private static boolean procedureOverloadDeclarationStatement_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureOverloadDeclarationStatement_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = attributeStatement(b, l + 1);
    r = r && procedureOverloadDeclarationStatement_0_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // eos?
  private static boolean procedureOverloadDeclarationStatement_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureOverloadDeclarationStatement_0_0_1")) return false;
    eos(b, l + 1);
    return true;
  }

  // (COMMA identifier)*
  private static boolean procedureOverloadDeclarationStatement_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureOverloadDeclarationStatement_6")) return false;
    while (true) {
      int c = current_position_(b);
      if (!procedureOverloadDeclarationStatement_6_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "procedureOverloadDeclarationStatement_6", c)) break;
    }
    return true;
  }

  // COMMA identifier
  private static boolean procedureOverloadDeclarationStatement_6_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureOverloadDeclarationStatement_6_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && identifier(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // COMMA?
  private static boolean procedureOverloadDeclarationStatement_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureOverloadDeclarationStatement_7")) return false;
    consumeToken(b, COMMA);
    return true;
  }

  /* ********************************************************** */
  // procedureType
  public static boolean procedureTypeContainer(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureTypeContainer")) return false;
    if (!nextTokenIs(b, "<procedure type container>", HASH, PROC)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PROCEDURE_TYPE_CONTAINER, "<procedure type container>");
    r = procedureType(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
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
  // NOT | LPAREN paramEntries RPAREN | typeDefinitionExpression
  public static boolean returnParameters(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnParameters")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RETURN_PARAMETERS, "<return parameters>");
    r = consumeToken(b, NOT);
    if (!r) r = returnParameters_1(b, l + 1);
    if (!r) r = typeDefinitionExpression(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // LPAREN paramEntries RPAREN
  private static boolean returnParameters_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnParameters_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && paramEntries(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // [directive] RETURN returnArgumentList?
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

  // [directive]
  private static boolean returnStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnStatement_0")) return false;
    directive(b, l + 1);
    return true;
  }

  // returnArgumentList?
  private static boolean returnStatement_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnStatement_2")) return false;
    returnArgumentList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // LBRACKET expression? COLON expression? RBRACKET
  public static boolean slice(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "slice")) return false;
    if (!nextTokenIs(b, LBRACKET)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LBRACKET);
    r = r && slice_1(b, l + 1);
    r = r && consumeToken(b, COLON);
    r = r && slice_3(b, l + 1);
    r = r && consumeToken(b, RBRACKET);
    exit_section_(b, m, SLICE, r);
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
  // SEMICOLON | NEW_LINE | EOS_TOKEN | <<afterClosingBrace>> | <<multilineBlockComment>>
  public static boolean sos(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "sos")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SOS, "<sos>");
    r = consumeToken(b, SEMICOLON);
    if (!r) r = consumeToken(b, NEW_LINE);
    if (!r) r = consumeToken(b, EOS_TOKEN);
    if (!r) r = afterClosingBrace(b, l + 1);
    if (!r) r = multilineBlockComment(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // assignmentStatement
  //                                          | procedureDeclarationStatement
  //                                          | variableInitializationStatement
  //                                          | structDeclarationStatement
  //                                          | enumDeclarationStatement
  //                                          | unionDeclarationStatement
  //                                          | procedureOverloadDeclarationStatement
  //                                          | constantInitializationStatement
  //                                          | variableDeclarationStatement
  //                                          | bitsetDeclarationStatement
  //                                          | blockStatement
  //                                          | forInStatement
  //                                          | forStatement
  //                                          | conditionalStatement
  //                                          | whenStatement
  //                                          | switchStatement
  //                                          | deferStatement
  //                                          | returnStatement
  //                                          | breakStatement
  //                                          | continueStatement
  //                                          | usingStatement
  //                                          | expressionStatement
  //                                          | fallthroughStatement
  //                                          | foreignImportDeclarationStatement
  //                                          | foreignStatement
  //                                          | importDeclarationStatement
  public static boolean statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, STATEMENT, "<statement>");
    r = assignmentStatement(b, l + 1);
    if (!r) r = procedureDeclarationStatement(b, l + 1);
    if (!r) r = variableInitializationStatement(b, l + 1);
    if (!r) r = structDeclarationStatement(b, l + 1);
    if (!r) r = enumDeclarationStatement(b, l + 1);
    if (!r) r = unionDeclarationStatement(b, l + 1);
    if (!r) r = procedureOverloadDeclarationStatement(b, l + 1);
    if (!r) r = constantInitializationStatement(b, l + 1);
    if (!r) r = variableDeclarationStatement(b, l + 1);
    if (!r) r = bitsetDeclarationStatement(b, l + 1);
    if (!r) r = blockStatement(b, l + 1);
    if (!r) r = forInStatement(b, l + 1);
    if (!r) r = forStatement(b, l + 1);
    if (!r) r = conditionalStatement(b, l + 1);
    if (!r) r = whenStatement(b, l + 1);
    if (!r) r = switchStatement(b, l + 1);
    if (!r) r = deferStatement(b, l + 1);
    if (!r) r = returnStatement(b, l + 1);
    if (!r) r = breakStatement(b, l + 1);
    if (!r) r = continueStatement(b, l + 1);
    if (!r) r = usingStatement(b, l + 1);
    if (!r) r = expressionStatement(b, l + 1);
    if (!r) r = fallthroughStatement(b, l + 1);
    if (!r) r = foreignImportDeclarationStatement(b, l + 1);
    if (!r) r = foreignStatement(b, l + 1);
    if (!r) r = importDeclarationStatement(b, l + 1);
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
  // (sos? statement eos)+
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

  // sos? statement eos
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

  // sos?
  private static boolean statementList_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statementList_0_0")) return false;
    sos(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // DQ_STRING_LITERAL
  //                                          | SQ_STRING_LITERAL
  //                                          | RAW_STRING_LITERAL
  public static boolean stringLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "stringLiteral")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, STRING_LITERAL, "<string literal>");
    r = consumeToken(b, DQ_STRING_LITERAL);
    if (!r) r = consumeToken(b, SQ_STRING_LITERAL);
    if (!r) r = consumeToken(b, RAW_STRING_LITERAL);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // blockStart [structBody] blockEnd
  public static boolean structBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structBlock")) return false;
    if (!nextTokenIs(b, LBRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = blockStart(b, l + 1);
    r = r && structBlock_1(b, l + 1);
    r = r && blockEnd(b, l + 1);
    exit_section_(b, m, STRUCT_BLOCK, r);
    return r;
  }

  // [structBody]
  private static boolean structBlock_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structBlock_1")) return false;
    structBody(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // fieldDeclarationStatement (COMMA fieldDeclarationStatement)*  [COMMA|EOS_TOKEN]
  public static boolean structBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structBody")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, STRUCT_BODY, "<struct body>");
    r = fieldDeclarationStatement(b, l + 1);
    r = r && structBody_1(b, l + 1);
    r = r && structBody_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (COMMA fieldDeclarationStatement)*
  private static boolean structBody_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structBody_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!structBody_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "structBody_1", c)) break;
    }
    return true;
  }

  // COMMA fieldDeclarationStatement
  private static boolean structBody_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structBody_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && fieldDeclarationStatement(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [COMMA|EOS_TOKEN]
  private static boolean structBody_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structBody_2")) return false;
    structBody_2_0(b, l + 1);
    return true;
  }

  // COMMA|EOS_TOKEN
  private static boolean structBody_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structBody_2_0")) return false;
    boolean r;
    r = consumeToken(b, COMMA);
    if (!r) r = consumeToken(b, EOS_TOKEN);
    return r;
  }

  /* ********************************************************** */
  // (attributeStatement eos?)* declaredIdentifier doubleColonOperator DISTINCT? structType
  public static boolean structDeclarationStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structDeclarationStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, STRUCT_DECLARATION_STATEMENT, "<struct declaration statement>");
    r = structDeclarationStatement_0(b, l + 1);
    r = r && declaredIdentifier(b, l + 1);
    r = r && doubleColonOperator(b, l + 1);
    r = r && structDeclarationStatement_3(b, l + 1);
    r = r && structType(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (attributeStatement eos?)*
  private static boolean structDeclarationStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structDeclarationStatement_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!structDeclarationStatement_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "structDeclarationStatement_0", c)) break;
    }
    return true;
  }

  // attributeStatement eos?
  private static boolean structDeclarationStatement_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structDeclarationStatement_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = attributeStatement(b, l + 1);
    r = r && structDeclarationStatement_0_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // eos?
  private static boolean structDeclarationStatement_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structDeclarationStatement_0_0_1")) return false;
    eos(b, l + 1);
    return true;
  }

  // DISTINCT?
  private static boolean structDeclarationStatement_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structDeclarationStatement_3")) return false;
    consumeToken(b, DISTINCT);
    return true;
  }

  /* ********************************************************** */
  // eos? blockStart switchCases blockEnd
  public static boolean switchBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchBody")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SWITCH_BODY, "<switch body>");
    r = switchBody_0(b, l + 1);
    r = r && blockStart(b, l + 1);
    r = r && switchCases(b, l + 1);
    r = r && blockEnd(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // eos?
  private static boolean switchBody_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchBody_0")) return false;
    eos(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // CASE [expression (COMMA expression)*] caseBlock
  public static boolean switchCase(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchCase")) return false;
    if (!nextTokenIs(b, CASE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, CASE);
    r = r && switchCase_1(b, l + 1);
    r = r && caseBlock(b, l + 1);
    exit_section_(b, m, SWITCH_CASE, r);
    return r;
  }

  // [expression (COMMA expression)*]
  private static boolean switchCase_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchCase_1")) return false;
    switchCase_1_0(b, l + 1);
    return true;
  }

  // expression (COMMA expression)*
  private static boolean switchCase_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchCase_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = expression(b, l + 1, -1);
    r = r && switchCase_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA expression)*
  private static boolean switchCase_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchCase_1_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!switchCase_1_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "switchCase_1_0_1", c)) break;
    }
    return true;
  }

  // COMMA expression
  private static boolean switchCase_1_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchCase_1_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // switchCase*
  public static boolean switchCases(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchCases")) return false;
    Marker m = enter_section_(b, l, _NONE_, SWITCH_CASES, "<switch cases>");
    while (true) {
      int c = current_position_(b);
      if (!switchCase(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "switchCases", c)) break;
    }
    exit_section_(b, l, m, true, false, null);
    return true;
  }

  /* ********************************************************** */
  // <<enterNoBlockMode>> [[controlFlowInit SEMICOLON] [expression]] <<exitNoBlockMode>>
  static boolean switchHead(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchHead")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = enterNoBlockMode(b, l + 1);
    r = r && switchHead_1(b, l + 1);
    r = r && exitNoBlockMode(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [[controlFlowInit SEMICOLON] [expression]]
  private static boolean switchHead_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchHead_1")) return false;
    switchHead_1_0(b, l + 1);
    return true;
  }

  // [controlFlowInit SEMICOLON] [expression]
  private static boolean switchHead_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchHead_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = switchHead_1_0_0(b, l + 1);
    r = r && switchHead_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [controlFlowInit SEMICOLON]
  private static boolean switchHead_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchHead_1_0_0")) return false;
    switchHead_1_0_0_0(b, l + 1);
    return true;
  }

  // controlFlowInit SEMICOLON
  private static boolean switchHead_1_0_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchHead_1_0_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = controlFlowInit(b, l + 1);
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, m, null, r);
    return r;
  }

  // [expression]
  private static boolean switchHead_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchHead_1_0_1")) return false;
    expression(b, l + 1, -1);
    return true;
  }

  /* ********************************************************** */
  // [directive] [label]  SWITCH IN? switchHead switchBody
  public static boolean switchStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SWITCH_STATEMENT, "<switch statement>");
    r = switchStatement_0(b, l + 1);
    r = r && switchStatement_1(b, l + 1);
    r = r && consumeToken(b, SWITCH);
    r = r && switchStatement_3(b, l + 1);
    r = r && switchHead(b, l + 1);
    r = r && switchBody(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // [directive]
  private static boolean switchStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchStatement_0")) return false;
    directive(b, l + 1);
    return true;
  }

  // [label]
  private static boolean switchStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchStatement_1")) return false;
    label(b, l + 1);
    return true;
  }

  // IN?
  private static boolean switchStatement_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchStatement_3")) return false;
    consumeToken(b, IN);
    return true;
  }

  /* ********************************************************** */
  // (attributeStatement eos?)* [tagHead] [USING] identifierList colonOpening typeDefinitionExpression?
  static boolean symbolDefinitionHead(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "symbolDefinitionHead")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = symbolDefinitionHead_0(b, l + 1);
    r = r && symbolDefinitionHead_1(b, l + 1);
    r = r && symbolDefinitionHead_2(b, l + 1);
    r = r && identifierList(b, l + 1);
    r = r && colonOpening(b, l + 1);
    r = r && symbolDefinitionHead_5(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (attributeStatement eos?)*
  private static boolean symbolDefinitionHead_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "symbolDefinitionHead_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!symbolDefinitionHead_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "symbolDefinitionHead_0", c)) break;
    }
    return true;
  }

  // attributeStatement eos?
  private static boolean symbolDefinitionHead_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "symbolDefinitionHead_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = attributeStatement(b, l + 1);
    r = r && symbolDefinitionHead_0_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // eos?
  private static boolean symbolDefinitionHead_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "symbolDefinitionHead_0_0_1")) return false;
    eos(b, l + 1);
    return true;
  }

  // [tagHead]
  private static boolean symbolDefinitionHead_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "symbolDefinitionHead_1")) return false;
    tagHead(b, l + 1);
    return true;
  }

  // [USING]
  private static boolean symbolDefinitionHead_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "symbolDefinitionHead_2")) return false;
    consumeToken(b, USING);
    return true;
  }

  // typeDefinitionExpression?
  private static boolean symbolDefinitionHead_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "symbolDefinitionHead_5")) return false;
    typeDefinitionExpression(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // HASH IDENTIFIER_TOKEN
  public static boolean tagHead(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tagHead")) return false;
    if (!nextTokenIs(b, HASH)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, HASH, IDENTIFIER_TOKEN);
    exit_section_(b, m, TAG_HEAD, r);
    return r;
  }

  /* ********************************************************** */
  // [directive] TRIPLE_DASH
  static boolean tripleDashBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tripleDashBlock")) return false;
    if (!nextTokenIs(b, "", HASH, TRIPLE_DASH)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = tripleDashBlock_0(b, l + 1);
    r = r && consumeToken(b, TRIPLE_DASH);
    exit_section_(b, m, null, r);
    return r;
  }

  // [directive]
  private static boolean tripleDashBlock_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tripleDashBlock_0")) return false;
    directive(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // typeDefinitionExpression
  public static boolean typeDefinitionContainer(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeDefinitionContainer")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TYPE_DEFINITION_CONTAINER, "<type definition container>");
    r = typeDefinitionExpression(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // blockStart [unionBody] blockEnd
  public static boolean unionBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionBlock")) return false;
    if (!nextTokenIs(b, LBRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = blockStart(b, l + 1);
    r = r && unionBlock_1(b, l + 1);
    r = r && blockEnd(b, l + 1);
    exit_section_(b, m, UNION_BLOCK, r);
    return r;
  }

  // [unionBody]
  private static boolean unionBlock_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionBlock_1")) return false;
    unionBody(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // typeDefinitionExpression (COMMA typeDefinitionExpression)* [COMMA|EOS_TOKEN]
  public static boolean unionBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionBody")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, UNION_BODY, "<union body>");
    r = typeDefinitionExpression(b, l + 1);
    r = r && unionBody_1(b, l + 1);
    r = r && unionBody_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (COMMA typeDefinitionExpression)*
  private static boolean unionBody_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionBody_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!unionBody_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "unionBody_1", c)) break;
    }
    return true;
  }

  // COMMA typeDefinitionExpression
  private static boolean unionBody_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionBody_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && typeDefinitionExpression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [COMMA|EOS_TOKEN]
  private static boolean unionBody_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionBody_2")) return false;
    unionBody_2_0(b, l + 1);
    return true;
  }

  // COMMA|EOS_TOKEN
  private static boolean unionBody_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionBody_2_0")) return false;
    boolean r;
    r = consumeToken(b, COMMA);
    if (!r) r = consumeToken(b, EOS_TOKEN);
    return r;
  }

  /* ********************************************************** */
  // (attributeStatement eos?)* declaredIdentifier doubleColonOperator DISTINCT? unionType
  public static boolean unionDeclarationStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionDeclarationStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, UNION_DECLARATION_STATEMENT, "<union declaration statement>");
    r = unionDeclarationStatement_0(b, l + 1);
    r = r && declaredIdentifier(b, l + 1);
    r = r && doubleColonOperator(b, l + 1);
    r = r && unionDeclarationStatement_3(b, l + 1);
    r = r && unionType(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (attributeStatement eos?)*
  private static boolean unionDeclarationStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionDeclarationStatement_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!unionDeclarationStatement_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "unionDeclarationStatement_0", c)) break;
    }
    return true;
  }

  // attributeStatement eos?
  private static boolean unionDeclarationStatement_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionDeclarationStatement_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = attributeStatement(b, l + 1);
    r = r && unionDeclarationStatement_0_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // eos?
  private static boolean unionDeclarationStatement_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionDeclarationStatement_0_0_1")) return false;
    eos(b, l + 1);
    return true;
  }

  // DISTINCT?
  private static boolean unionDeclarationStatement_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionDeclarationStatement_3")) return false;
    consumeToken(b, DISTINCT);
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
  // RANGE? typeDefinitionContainer
  public static boolean unnamedParameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unnamedParameter")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, UNNAMED_PARAMETER, "<unnamed parameter>");
    r = unnamedParameter_0(b, l + 1);
    r = r && typeDefinitionContainer(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // RANGE?
  private static boolean unnamedParameter_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unnamedParameter_0")) return false;
    consumeToken(b, RANGE);
    return true;
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
  // (attributeStatement eos?)* [USING] identifierList COLON typeDefinitionExpression
  public static boolean variableDeclarationStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableDeclarationStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, VARIABLE_DECLARATION_STATEMENT, "<variable declaration statement>");
    r = variableDeclarationStatement_0(b, l + 1);
    r = r && variableDeclarationStatement_1(b, l + 1);
    r = r && identifierList(b, l + 1);
    r = r && consumeToken(b, COLON);
    r = r && typeDefinitionExpression(b, l + 1);
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

  // [USING]
  private static boolean variableDeclarationStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableDeclarationStatement_1")) return false;
    consumeToken(b, USING);
    return true;
  }

  /* ********************************************************** */
  // symbolDefinitionHead EQ expressionsList
  public static boolean variableInitializationStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableInitializationStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, VARIABLE_INITIALIZATION_STATEMENT, "<variable initialization statement>");
    r = symbolDefinitionHead(b, l + 1);
    r = r && consumeToken(b, EQ);
    r = r && expressionsList(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // [label] [tagHead] WHEN <<enterNoBlockMode>> condition <<exitNoBlockMode>> statementBody (sos elseWhenBlock)* [sos elseBlock]
  public static boolean whenStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "whenStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, WHEN_STATEMENT, "<when statement>");
    r = whenStatement_0(b, l + 1);
    r = r && whenStatement_1(b, l + 1);
    r = r && consumeToken(b, WHEN);
    r = r && enterNoBlockMode(b, l + 1);
    r = r && condition(b, l + 1);
    r = r && exitNoBlockMode(b, l + 1);
    r = r && statementBody(b, l + 1);
    r = r && whenStatement_7(b, l + 1);
    r = r && whenStatement_8(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // [label]
  private static boolean whenStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "whenStatement_0")) return false;
    label(b, l + 1);
    return true;
  }

  // [tagHead]
  private static boolean whenStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "whenStatement_1")) return false;
    tagHead(b, l + 1);
    return true;
  }

  // (sos elseWhenBlock)*
  private static boolean whenStatement_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "whenStatement_7")) return false;
    while (true) {
      int c = current_position_(b);
      if (!whenStatement_7_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "whenStatement_7", c)) break;
    }
    return true;
  }

  // sos elseWhenBlock
  private static boolean whenStatement_7_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "whenStatement_7_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = sos(b, l + 1);
    r = r && elseWhenBlock(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [sos elseBlock]
  private static boolean whenStatement_8(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "whenStatement_8")) return false;
    whenStatement_8_0(b, l + 1);
    return true;
  }

  // sos elseBlock
  private static boolean whenStatement_8_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "whenStatement_8_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = sos(b, l + 1);
    r = r && elseBlock(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
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
  // 0: POSTFIX(orReturnExpression) POSTFIX(orBreakExpression) POSTFIX(orContinueExpression) POSTFIX(typeAssertionExpression)
  // 1: BINARY(orElseExpression)
  // 2: BINARY(elvisExpression) BINARY(ternaryIfExpression) BINARY(ternaryWhenExpression)
  // 3: BINARY(rangeInclusiveExpression) BINARY(rangeExclusiveExpression)
  // 4: BINARY(orExpression)
  // 5: BINARY(andExpression)
  // 6: BINARY(ltExpression) BINARY(gtExpression) BINARY(lteExpression) BINARY(gteExpression)
  //    BINARY(eqeqExpression) BINARY(neqExpression)
  // 7: BINARY(addExpression) BINARY(subExpression) BINARY(bitwiseOrExpression) BINARY(bitwiseXorExpression)
  //    BINARY(inExpression) BINARY(notInExpression)
  // 8: BINARY(mulExpression) BINARY(divExpression) BINARY(modExpression) BINARY(remainderExpression)
  //    BINARY(bitwiseAndExpression) BINARY(lshiftExpression) BINARY(rshiftExpression) BINARY(bitwiseAndNotExpression)
  // 9: PREFIX(unaryPlusExpression) PREFIX(unaryMinusExpression) PREFIX(unaryTildeExpression) PREFIX(unaryAndExpression)
  //    PREFIX(unaryNotExpression) PREFIX(unaryRangeExpression)
  // 10: ATOM(implicitSelectorExpression)
  // 11: POSTFIX(maybeExpression)
  // 12: ATOM(uninitializedExpression)
  // 13: POSTFIX(callExpression)
  // 14: POSTFIX(qualificationExpression)
  // 15: POSTFIX(indexExpression)
  // 16: POSTFIX(sliceExpression)
  // 17: POSTFIX(dereferenceExpression)
  // 18: ATOM(procedureExpression)
  // 19: ATOM(transmuteExpression)
  // 20: PREFIX(autoCastExpression)
  // 21: ATOM(castExpression)
  // 22: ATOM(compoundLiteralExpression)
  // 23: ATOM(simpleRefExpression) PREFIX(parenthesizedExpression) ATOM(typeDefinitionExpression) ATOM(directiveExpression)
  //    ATOM(literalExpression)
  public static boolean expression(PsiBuilder b, int l, int g) {
    if (!recursion_guard_(b, l, "expression")) return false;
    addVariant(b, "<expression>");
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, "<expression>");
    r = unaryPlusExpression(b, l + 1);
    if (!r) r = unaryMinusExpression(b, l + 1);
    if (!r) r = unaryTildeExpression(b, l + 1);
    if (!r) r = unaryAndExpression(b, l + 1);
    if (!r) r = unaryNotExpression(b, l + 1);
    if (!r) r = unaryRangeExpression(b, l + 1);
    if (!r) r = implicitSelectorExpression(b, l + 1);
    if (!r) r = uninitializedExpression(b, l + 1);
    if (!r) r = procedureExpression(b, l + 1);
    if (!r) r = transmuteExpression(b, l + 1);
    if (!r) r = autoCastExpression(b, l + 1);
    if (!r) r = castExpression(b, l + 1);
    if (!r) r = compoundLiteralExpression(b, l + 1);
    if (!r) r = simpleRefExpression(b, l + 1);
    if (!r) r = parenthesizedExpression(b, l + 1);
    if (!r) r = typeDefinitionExpression(b, l + 1);
    if (!r) r = directiveExpression(b, l + 1);
    if (!r) r = literalExpression(b, l + 1);
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
      if (g < 0 && consumeTokenSmart(b, OR_RETURN)) {
        r = true;
        exit_section_(b, l, m, OR_RETURN_EXPRESSION, r, true, null);
      }
      else if (g < 0 && orBreakExpression_0(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, OR_BREAK_EXPRESSION, r, true, null);
      }
      else if (g < 0 && orContinueExpression_0(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, OR_CONTINUE_EXPRESSION, r, true, null);
      }
      else if (g < 0 && typeAssertionExpression_0(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, TYPE_ASSERTION_EXPRESSION, r, true, null);
      }
      else if (g < 1 && orElseExpression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 1));
        r = exitMode(b, l + 1, "OPERAND") && r;
        exit_section_(b, l, m, OR_ELSE_EXPRESSION, r, true, null);
      }
      else if (g < 2 && elvisExpression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 2));
        r = elvisExpression_1(b, l + 1) && r;
        exit_section_(b, l, m, ELVIS_EXPRESSION, r, true, null);
      }
      else if (g < 2 && ternaryIfExpression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 2));
        r = ternaryIfExpression_1(b, l + 1) && r;
        exit_section_(b, l, m, TERNARY_IF_EXPRESSION, r, true, null);
      }
      else if (g < 2 && ternaryWhenExpression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 2));
        r = ternaryWhenExpression_1(b, l + 1) && r;
        exit_section_(b, l, m, TERNARY_WHEN_EXPRESSION, r, true, null);
      }
      else if (g < 3 && rangeInclusiveExpression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 3));
        r = exitMode(b, l + 1, "OPERAND") && r;
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 3 && rangeExclusiveExpression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 3));
        r = exitMode(b, l + 1, "OPERAND") && r;
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 4 && orExpression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 4));
        r = exitMode(b, l + 1, "OPERAND") && r;
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 5 && andExpression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 5));
        r = exitMode(b, l + 1, "OPERAND") && r;
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 6 && ltExpression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 6));
        r = exitMode(b, l + 1, "OPERAND") && r;
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 6 && gtExpression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 6));
        r = exitMode(b, l + 1, "OPERAND") && r;
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 6 && lteExpression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 6));
        r = exitMode(b, l + 1, "OPERAND") && r;
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 6 && gteExpression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 6));
        r = exitMode(b, l + 1, "OPERAND") && r;
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 6 && eqeqExpression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 6));
        r = exitMode(b, l + 1, "OPERAND") && r;
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 6 && neqExpression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 6));
        r = exitMode(b, l + 1, "OPERAND") && r;
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 7 && addExpression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 7));
        r = exitMode(b, l + 1, "OPERAND") && r;
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 7 && subExpression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 7));
        r = exitMode(b, l + 1, "OPERAND") && r;
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 7 && bitwiseOrExpression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 7));
        r = exitMode(b, l + 1, "OPERAND") && r;
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 7 && bitwiseXorExpression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 7));
        r = exitMode(b, l + 1, "OPERAND") && r;
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 7 && inExpression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 7));
        r = exitMode(b, l + 1, "OPERAND") && r;
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 7 && notInExpression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 7));
        r = exitMode(b, l + 1, "OPERAND") && r;
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 8 && mulExpression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 8));
        r = exitMode(b, l + 1, "OPERAND") && r;
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 8 && divExpression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 8));
        r = exitMode(b, l + 1, "OPERAND") && r;
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 8 && modExpression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 8));
        r = exitMode(b, l + 1, "OPERAND") && r;
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 8 && remainderExpression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 8));
        r = exitMode(b, l + 1, "OPERAND") && r;
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 8 && bitwiseAndExpression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 8));
        r = exitMode(b, l + 1, "OPERAND") && r;
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 8 && lshiftExpression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 8));
        r = exitMode(b, l + 1, "OPERAND") && r;
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 8 && rshiftExpression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 8));
        r = exitMode(b, l + 1, "OPERAND") && r;
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 8 && bitwiseAndNotExpression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 8));
        r = exitMode(b, l + 1, "OPERAND") && r;
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 11 && consumeTokenSmart(b, DOT_QUESTION)) {
        r = true;
        exit_section_(b, l, m, MAYBE_EXPRESSION, r, true, null);
      }
      else if (g < 13 && arguments(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, CALL_EXPRESSION, r, true, null);
      }
      else if (g < 14 && qualificationExpression_0(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, REF_EXPRESSION, r, true, null);
      }
      else if (g < 15 && index(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, INDEX_EXPRESSION, r, true, null);
      }
      else if (g < 16 && slice(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, SLICE_EXPRESSION, r, true, null);
      }
      else if (g < 17 && consumeTokenSmart(b, CARET)) {
        r = true;
        exit_section_(b, l, m, DEREFERENCE_EXPRESSION, r, true, null);
      }
      else {
        exit_section_(b, l, m, null, false, false, null);
        break;
      }
    }
    return r;
  }

  // OR_BREAK [IDENTIFIER_TOKEN]
  private static boolean orBreakExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "orBreakExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, OR_BREAK);
    r = r && orBreakExpression_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [IDENTIFIER_TOKEN]
  private static boolean orBreakExpression_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "orBreakExpression_0_1")) return false;
    consumeTokenSmart(b, IDENTIFIER_TOKEN);
    return true;
  }

  // OR_CONTINUE [IDENTIFIER_TOKEN]
  private static boolean orContinueExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "orContinueExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, OR_CONTINUE);
    r = r && orContinueExpression_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [IDENTIFIER_TOKEN]
  private static boolean orContinueExpression_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "orContinueExpression_0_1")) return false;
    consumeTokenSmart(b, IDENTIFIER_TOKEN);
    return true;
  }

  // DOT LPAREN type RPAREN
  private static boolean typeAssertionExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeAssertionExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokensSmart(b, 0, DOT, LPAREN);
    r = r && type(b, l + 1, -1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // OR_ELSE <<enterMode "OPERAND">>
  private static boolean orElseExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "orElseExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, OR_ELSE);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // QUESTION <<enterMode "OPERAND">>
  private static boolean elvisExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "elvisExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, QUESTION);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // COLON expression <<exitMode "OPERAND">>
  private static boolean elvisExpression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "elvisExpression_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COLON);
    r = r && expression(b, l + 1, -1);
    r = r && exitMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // if <<enterMode "OPERAND">>
  private static boolean ternaryIfExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ternaryIfExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = if_$(b, l + 1);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // else expression <<exitMode "OPERAND">>
  private static boolean ternaryIfExpression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ternaryIfExpression_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = else_$(b, l + 1);
    r = r && expression(b, l + 1, -1);
    r = r && exitMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // WHEN <<enterMode "OPERAND">>
  private static boolean ternaryWhenExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ternaryWhenExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, WHEN);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // else expression <<exitMode "OPERAND">>
  private static boolean ternaryWhenExpression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ternaryWhenExpression_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = else_$(b, l + 1);
    r = r && expression(b, l + 1, -1);
    r = r && exitMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // RANGE_INCLUSIVE <<enterMode "OPERAND">>
  private static boolean rangeInclusiveExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rangeInclusiveExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, RANGE_INCLUSIVE);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // RANGE_EXCLUSIVE <<enterMode "OPERAND">>
  private static boolean rangeExclusiveExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rangeExclusiveExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, RANGE_EXCLUSIVE);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // OROR <<enterMode "OPERAND">>
  private static boolean orExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "orExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, OROR);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // ANDAND <<enterMode "OPERAND">>
  private static boolean andExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "andExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, ANDAND);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // LT <<enterMode "OPERAND">>
  private static boolean ltExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ltExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, LT);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // GT <<enterMode "OPERAND">>
  private static boolean gtExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "gtExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, GT);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // LTE <<enterMode "OPERAND">>
  private static boolean lteExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lteExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, LTE);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // GTE <<enterMode "OPERAND">>
  private static boolean gteExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "gteExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, GTE);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // EQEQ <<enterMode "OPERAND">>
  private static boolean eqeqExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "eqeqExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, EQEQ);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // NEQ <<enterMode "OPERAND">>
  private static boolean neqExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "neqExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, NEQ);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // PLUS <<enterMode "OPERAND">>
  private static boolean addExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "addExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, PLUS);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // MINUS <<enterMode "OPERAND">>
  private static boolean subExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "subExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, MINUS);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // PIPE <<enterMode "OPERAND">>
  private static boolean bitwiseOrExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bitwiseOrExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, PIPE);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // TILDE <<enterMode "OPERAND">>
  private static boolean bitwiseXorExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bitwiseXorExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, TILDE);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // IN <<enterMode "OPERAND">>
  private static boolean inExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, IN);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // NOT_IN <<enterMode "OPERAND">>
  private static boolean notInExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "notInExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, NOT_IN);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // STAR <<enterMode "OPERAND">>
  private static boolean mulExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mulExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, STAR);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // DIV <<enterMode "OPERAND">>
  private static boolean divExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "divExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, DIV);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // MOD <<enterMode "OPERAND">>
  private static boolean modExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "modExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, MOD);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // REMAINDER <<enterMode "OPERAND">>
  private static boolean remainderExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "remainderExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, REMAINDER);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // AND <<enterMode "OPERAND">>
  private static boolean bitwiseAndExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bitwiseAndExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, AND);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // LSHIFT <<enterMode "OPERAND">>
  private static boolean lshiftExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lshiftExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, LSHIFT);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // RSHIFT <<enterMode "OPERAND">>
  private static boolean rshiftExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rshiftExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, RSHIFT);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // ANDNOT <<enterMode "OPERAND">>
  private static boolean bitwiseAndNotExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bitwiseAndNotExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, ANDNOT);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  public static boolean unaryPlusExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unaryPlusExpression")) return false;
    if (!nextTokenIsSmart(b, PLUS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = unaryPlusExpression_0(b, l + 1);
    p = r;
    r = p && expression(b, l, 9);
    r = p && report_error_(b, exitMode(b, l + 1, "OPERAND")) && r;
    exit_section_(b, l, m, UNARY_PLUS_EXPRESSION, r, p, null);
    return r || p;
  }

  // PLUS <<enterMode "OPERAND">>
  private static boolean unaryPlusExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unaryPlusExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, PLUS);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  public static boolean unaryMinusExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unaryMinusExpression")) return false;
    if (!nextTokenIsSmart(b, MINUS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = unaryMinusExpression_0(b, l + 1);
    p = r;
    r = p && expression(b, l, 9);
    r = p && report_error_(b, exitMode(b, l + 1, "OPERAND")) && r;
    exit_section_(b, l, m, UNARY_MINUS_EXPRESSION, r, p, null);
    return r || p;
  }

  // MINUS <<enterMode "OPERAND">>
  private static boolean unaryMinusExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unaryMinusExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, MINUS);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  public static boolean unaryTildeExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unaryTildeExpression")) return false;
    if (!nextTokenIsSmart(b, TILDE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = unaryTildeExpression_0(b, l + 1);
    p = r;
    r = p && expression(b, l, 9);
    r = p && report_error_(b, exitMode(b, l + 1, "OPERAND")) && r;
    exit_section_(b, l, m, UNARY_TILDE_EXPRESSION, r, p, null);
    return r || p;
  }

  // TILDE <<enterMode "OPERAND">>
  private static boolean unaryTildeExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unaryTildeExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, TILDE);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  public static boolean unaryAndExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unaryAndExpression")) return false;
    if (!nextTokenIsSmart(b, AND)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = unaryAndExpression_0(b, l + 1);
    p = r;
    r = p && expression(b, l, 9);
    r = p && report_error_(b, exitMode(b, l + 1, "OPERAND")) && r;
    exit_section_(b, l, m, UNARY_AND_EXPRESSION, r, p, null);
    return r || p;
  }

  // AND <<enterMode "OPERAND">>
  private static boolean unaryAndExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unaryAndExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, AND);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  public static boolean unaryNotExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unaryNotExpression")) return false;
    if (!nextTokenIsSmart(b, NOT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = unaryNotExpression_0(b, l + 1);
    p = r;
    r = p && expression(b, l, 9);
    r = p && report_error_(b, exitMode(b, l + 1, "OPERAND")) && r;
    exit_section_(b, l, m, UNARY_NOT_EXPRESSION, r, p, null);
    return r || p;
  }

  // NOT <<enterMode "OPERAND">>
  private static boolean unaryNotExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unaryNotExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, NOT);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  public static boolean unaryRangeExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unaryRangeExpression")) return false;
    if (!nextTokenIsSmart(b, RANGE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = unaryRangeExpression_0(b, l + 1);
    p = r;
    r = p && expression(b, l, 9);
    r = p && report_error_(b, exitMode(b, l + 1, "OPERAND")) && r;
    exit_section_(b, l, m, UNARY_RANGE_EXPRESSION, r, p, null);
    return r || p;
  }

  // RANGE <<enterMode "OPERAND">>
  private static boolean unaryRangeExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unaryRangeExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, RANGE);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // DOT identifier
  public static boolean implicitSelectorExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "implicitSelectorExpression")) return false;
    if (!nextTokenIsSmart(b, DOT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, DOT);
    r = r && identifier(b, l + 1);
    exit_section_(b, m, IMPLICIT_SELECTOR_EXPRESSION, r);
    return r;
  }

  // TRIPLE_DASH
  public static boolean uninitializedExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "uninitializedExpression")) return false;
    if (!nextTokenIsSmart(b, TRIPLE_DASH)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, TRIPLE_DASH);
    exit_section_(b, m, UNINITIALIZED_EXPRESSION, r);
    return r;
  }

  // (DOT|ARROW) identifier
  private static boolean qualificationExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qualificationExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = qualificationExpression_0_0(b, l + 1);
    r = r && identifier(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // DOT|ARROW
  private static boolean qualificationExpression_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qualificationExpression_0_0")) return false;
    boolean r;
    r = consumeTokenSmart(b, DOT);
    if (!r) r = consumeTokenSmart(b, ARROW);
    return r;
  }

  // procedureTypeContainer procedureBody
  public static boolean procedureExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureExpression")) return false;
    if (!nextTokenIsSmart(b, HASH, PROC)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PROCEDURE_EXPRESSION, "<procedure expression>");
    r = procedureTypeContainer(b, l + 1);
    r = r && procedureBody(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // TRANSMUTE LPAREN expression RPAREN expression
  public static boolean transmuteExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "transmuteExpression")) return false;
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

  public static boolean autoCastExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "autoCastExpression")) return false;
    if (!nextTokenIsSmart(b, AUTO_CAST)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeTokenSmart(b, AUTO_CAST);
    p = r;
    r = p && expression(b, l, 20);
    exit_section_(b, l, m, AUTO_CAST_EXPRESSION, r, p, null);
    return r || p;
  }

  // CAST LPAREN typeDefinitionExpression RPAREN expression
  public static boolean castExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "castExpression")) return false;
    if (!nextTokenIsSmart(b, CAST)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokensSmart(b, 0, CAST, LPAREN);
    r = r && typeDefinitionExpression(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, CAST_EXPRESSION, r);
    return r;
  }

  // compoundLiteralTyped | compoundLiteralUntyped
  public static boolean compoundLiteralExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compoundLiteralExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, COMPOUND_LITERAL_EXPRESSION, "<compound literal expression>");
    r = compoundLiteralTyped(b, l + 1);
    if (!r) r = compoundLiteralUntyped(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // identifier
  public static boolean simpleRefExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpleRefExpression")) return false;
    if (!nextTokenIsSmart(b, IDENTIFIER_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = identifier(b, l + 1);
    exit_section_(b, m, REF_EXPRESSION, r);
    return r;
  }

  public static boolean parenthesizedExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parenthesizedExpression")) return false;
    if (!nextTokenIsSmart(b, LPAREN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = parenthesizedExpression_0(b, l + 1);
    p = r;
    r = p && expression(b, l, -1);
    r = p && report_error_(b, parenthesizedExpression_1(b, l + 1)) && r;
    exit_section_(b, l, m, PARENTHESIZED_EXPRESSION, r, p, null);
    return r || p;
  }

  // LPAREN <<enterMode "PAR">>
  private static boolean parenthesizedExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parenthesizedExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, LPAREN);
    r = r && enterMode(b, l + 1, "PAR");
    exit_section_(b, m, null, r);
    return r;
  }

  // <<exitMode "PAR">> RPAREN
  private static boolean parenthesizedExpression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parenthesizedExpression_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = exitMode(b, l + 1, "PAR");
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // DISTINCT? [directive] type
  public static boolean typeDefinitionExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeDefinitionExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TYPE_DEFINITION_EXPRESSION, "<type definition expression>");
    r = typeDefinitionExpression_0(b, l + 1);
    r = r && typeDefinitionExpression_1(b, l + 1);
    r = r && type(b, l + 1, -1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // DISTINCT?
  private static boolean typeDefinitionExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeDefinitionExpression_0")) return false;
    consumeTokenSmart(b, DISTINCT);
    return true;
  }

  // [directive]
  private static boolean typeDefinitionExpression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeDefinitionExpression_1")) return false;
    directive(b, l + 1);
    return true;
  }

  // directive
  public static boolean directiveExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directiveExpression")) return false;
    if (!nextTokenIsSmart(b, HASH)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = directive(b, l + 1);
    exit_section_(b, m, DIRECTIVE_EXPRESSION, r);
    return r;
  }

  // basicLiteral
  public static boolean literalExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "literalExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LITERAL_EXPRESSION, "<literal expression>");
    r = basicLiteral(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // Expression root: type
  // Operator priority table:
  // 0: ATOM(mapType)
  // 1: ATOM(matrixType)
  // 2: ATOM(bitSetType)
  // 3: PREFIX(multiPointerType)
  // 4: ATOM(arrayType)
  // 5: ATOM(procedureType)
  // 6: ATOM(structType)
  // 7: ATOM(enumType)
  // 8: ATOM(unionType)
  // 9: PREFIX(pointerType)
  // 10: ATOM(polymorphicType)
  // 11: BINARY(constrainedType)
  // 12: ATOM(parExpressionType)
  // 13: ATOM(qualifiedType)
  // 14: ATOM(callType)
  // 15: PREFIX(variadicType)
  // 16: ATOM(simpleRefType)
  public static boolean type(PsiBuilder b, int l, int g) {
    if (!recursion_guard_(b, l, "type")) return false;
    addVariant(b, "<type>");
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, "<type>");
    r = mapType(b, l + 1);
    if (!r) r = matrixType(b, l + 1);
    if (!r) r = bitSetType(b, l + 1);
    if (!r) r = multiPointerType(b, l + 1);
    if (!r) r = arrayType(b, l + 1);
    if (!r) r = procedureType(b, l + 1);
    if (!r) r = structType(b, l + 1);
    if (!r) r = enumType(b, l + 1);
    if (!r) r = unionType(b, l + 1);
    if (!r) r = pointerType(b, l + 1);
    if (!r) r = polymorphicType(b, l + 1);
    if (!r) r = parExpressionType(b, l + 1);
    if (!r) r = qualifiedType(b, l + 1);
    if (!r) r = callType(b, l + 1);
    if (!r) r = variadicType(b, l + 1);
    if (!r) r = simpleRefType(b, l + 1);
    p = r;
    r = r && type_0(b, l + 1, g);
    exit_section_(b, l, m, null, r, p, null);
    return r || p;
  }

  public static boolean type_0(PsiBuilder b, int l, int g) {
    if (!recursion_guard_(b, l, "type_0")) return false;
    boolean r = true;
    while (true) {
      Marker m = enter_section_(b, l, _LEFT_, null);
      if (g < 11 && consumeTokenSmart(b, DIV)) {
        r = type(b, l, 11);
        exit_section_(b, l, m, CONSTRAINED_TYPE, r, true, null);
      }
      else {
        exit_section_(b, l, m, null, false, false, null);
        break;
      }
    }
    return r;
  }

  // MAP LBRACKET typeDefinitionExpression RBRACKET typeDefinitionExpression
  public static boolean mapType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mapType")) return false;
    if (!nextTokenIsSmart(b, MAP)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokensSmart(b, 0, MAP, LBRACKET);
    r = r && typeDefinitionExpression(b, l + 1);
    r = r && consumeToken(b, RBRACKET);
    r = r && typeDefinitionExpression(b, l + 1);
    exit_section_(b, m, MAP_TYPE, r);
    return r;
  }

  // MATRIX LBRACKET expression COMMA expression RBRACKET typeDefinitionExpression
  public static boolean matrixType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "matrixType")) return false;
    if (!nextTokenIsSmart(b, MATRIX)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokensSmart(b, 0, MATRIX, LBRACKET);
    r = r && expression(b, l + 1, -1);
    r = r && consumeToken(b, COMMA);
    r = r && expression(b, l + 1, -1);
    r = r && consumeToken(b, RBRACKET);
    r = r && typeDefinitionExpression(b, l + 1);
    exit_section_(b, m, MATRIX_TYPE, r);
    return r;
  }

  // BIT_SET LBRACKET expression [SEMICOLON typeDefinitionExpression] RBRACKET
  public static boolean bitSetType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bitSetType")) return false;
    if (!nextTokenIsSmart(b, BIT_SET)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokensSmart(b, 0, BIT_SET, LBRACKET);
    r = r && expression(b, l + 1, -1);
    r = r && bitSetType_3(b, l + 1);
    r = r && consumeToken(b, RBRACKET);
    exit_section_(b, m, BIT_SET_TYPE, r);
    return r;
  }

  // [SEMICOLON typeDefinitionExpression]
  private static boolean bitSetType_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bitSetType_3")) return false;
    bitSetType_3_0(b, l + 1);
    return true;
  }

  // SEMICOLON typeDefinitionExpression
  private static boolean bitSetType_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bitSetType_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, SEMICOLON);
    r = r && typeDefinitionExpression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  public static boolean multiPointerType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "multiPointerType")) return false;
    if (!nextTokenIsSmart(b, LBRACKET)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = parseTokensSmart(b, 0, LBRACKET, CARET, RBRACKET);
    p = r;
    r = p && type(b, l, 3);
    exit_section_(b, l, m, MULTI_POINTER_TYPE, r, p, null);
    return r || p;
  }

  // [tagHead] LBRACKET [QUESTION|DYNAMIC|expression] RBRACKET typeDefinitionExpression
  public static boolean arrayType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayType")) return false;
    if (!nextTokenIsSmart(b, HASH, LBRACKET)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ARRAY_TYPE, "<array type>");
    r = arrayType_0(b, l + 1);
    r = r && consumeToken(b, LBRACKET);
    r = r && arrayType_2(b, l + 1);
    r = r && consumeToken(b, RBRACKET);
    r = r && typeDefinitionExpression(b, l + 1);
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
    r = consumeTokenSmart(b, QUESTION);
    if (!r) r = consumeTokenSmart(b, DYNAMIC);
    if (!r) r = expression(b, l + 1, -1);
    return r;
  }

  // directive? PROC stringLiteral? LPAREN [paramEntries] RPAREN [ARROW returnParameters] <<enterNoBlockMode>> [eos? whereClause eos?] <<exitNoBlockMode>>
  public static boolean procedureType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureType")) return false;
    if (!nextTokenIsSmart(b, HASH, PROC)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PROCEDURE_TYPE, "<procedure type>");
    r = procedureType_0(b, l + 1);
    r = r && consumeToken(b, PROC);
    r = r && procedureType_2(b, l + 1);
    r = r && consumeToken(b, LPAREN);
    r = r && procedureType_4(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    r = r && procedureType_6(b, l + 1);
    r = r && enterNoBlockMode(b, l + 1);
    r = r && procedureType_8(b, l + 1);
    r = r && exitNoBlockMode(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // directive?
  private static boolean procedureType_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureType_0")) return false;
    directive(b, l + 1);
    return true;
  }

  // stringLiteral?
  private static boolean procedureType_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureType_2")) return false;
    stringLiteral(b, l + 1);
    return true;
  }

  // [paramEntries]
  private static boolean procedureType_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureType_4")) return false;
    paramEntries(b, l + 1);
    return true;
  }

  // [ARROW returnParameters]
  private static boolean procedureType_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureType_6")) return false;
    procedureType_6_0(b, l + 1);
    return true;
  }

  // ARROW returnParameters
  private static boolean procedureType_6_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureType_6_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, ARROW);
    r = r && returnParameters(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [eos? whereClause eos?]
  private static boolean procedureType_8(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureType_8")) return false;
    procedureType_8_0(b, l + 1);
    return true;
  }

  // eos? whereClause eos?
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

  // eos?
  private static boolean procedureType_8_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureType_8_0_0")) return false;
    eos(b, l + 1);
    return true;
  }

  // eos?
  private static boolean procedureType_8_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureType_8_0_2")) return false;
    eos(b, l + 1);
    return true;
  }

  // STRUCT [LPAREN polymorphicParameterList RPAREN [eos? whereClause eos?]] <<enterNoBlockMode>> directive* <<exitNoBlockMode>> structBlock
  public static boolean structType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structType")) return false;
    if (!nextTokenIsSmart(b, STRUCT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, STRUCT);
    r = r && structType_1(b, l + 1);
    r = r && enterNoBlockMode(b, l + 1);
    r = r && structType_3(b, l + 1);
    r = r && exitNoBlockMode(b, l + 1);
    r = r && structBlock(b, l + 1);
    exit_section_(b, m, STRUCT_TYPE, r);
    return r;
  }

  // [LPAREN polymorphicParameterList RPAREN [eos? whereClause eos?]]
  private static boolean structType_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structType_1")) return false;
    structType_1_0(b, l + 1);
    return true;
  }

  // LPAREN polymorphicParameterList RPAREN [eos? whereClause eos?]
  private static boolean structType_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structType_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, LPAREN);
    r = r && polymorphicParameterList(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    r = r && structType_1_0_3(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [eos? whereClause eos?]
  private static boolean structType_1_0_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structType_1_0_3")) return false;
    structType_1_0_3_0(b, l + 1);
    return true;
  }

  // eos? whereClause eos?
  private static boolean structType_1_0_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structType_1_0_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = structType_1_0_3_0_0(b, l + 1);
    r = r && whereClause(b, l + 1);
    r = r && structType_1_0_3_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // eos?
  private static boolean structType_1_0_3_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structType_1_0_3_0_0")) return false;
    eos(b, l + 1);
    return true;
  }

  // eos?
  private static boolean structType_1_0_3_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structType_1_0_3_0_2")) return false;
    eos(b, l + 1);
    return true;
  }

  // directive*
  private static boolean structType_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structType_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!directive(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "structType_3", c)) break;
    }
    return true;
  }

  // ENUM [IDENTIFIER_TOKEN (DOT IDENTIFIER_TOKEN)*] enumBlock
  public static boolean enumType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumType")) return false;
    if (!nextTokenIsSmart(b, ENUM)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, ENUM);
    r = r && enumType_1(b, l + 1);
    r = r && enumBlock(b, l + 1);
    exit_section_(b, m, ENUM_TYPE, r);
    return r;
  }

  // [IDENTIFIER_TOKEN (DOT IDENTIFIER_TOKEN)*]
  private static boolean enumType_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumType_1")) return false;
    enumType_1_0(b, l + 1);
    return true;
  }

  // IDENTIFIER_TOKEN (DOT IDENTIFIER_TOKEN)*
  private static boolean enumType_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumType_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, IDENTIFIER_TOKEN);
    r = r && enumType_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (DOT IDENTIFIER_TOKEN)*
  private static boolean enumType_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumType_1_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!enumType_1_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "enumType_1_0_1", c)) break;
    }
    return true;
  }

  // DOT IDENTIFIER_TOKEN
  private static boolean enumType_1_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumType_1_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokensSmart(b, 0, DOT, IDENTIFIER_TOKEN);
    exit_section_(b, m, null, r);
    return r;
  }

  // UNION [LPAREN polymorphicParameterList RPAREN [eos? whereClause eos?]] <<enterNoBlockMode>> directive* <<exitNoBlockMode>> unionBlock
  public static boolean unionType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionType")) return false;
    if (!nextTokenIsSmart(b, UNION)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, UNION);
    r = r && unionType_1(b, l + 1);
    r = r && enterNoBlockMode(b, l + 1);
    r = r && unionType_3(b, l + 1);
    r = r && exitNoBlockMode(b, l + 1);
    r = r && unionBlock(b, l + 1);
    exit_section_(b, m, UNION_TYPE, r);
    return r;
  }

  // [LPAREN polymorphicParameterList RPAREN [eos? whereClause eos?]]
  private static boolean unionType_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionType_1")) return false;
    unionType_1_0(b, l + 1);
    return true;
  }

  // LPAREN polymorphicParameterList RPAREN [eos? whereClause eos?]
  private static boolean unionType_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionType_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, LPAREN);
    r = r && polymorphicParameterList(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    r = r && unionType_1_0_3(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [eos? whereClause eos?]
  private static boolean unionType_1_0_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionType_1_0_3")) return false;
    unionType_1_0_3_0(b, l + 1);
    return true;
  }

  // eos? whereClause eos?
  private static boolean unionType_1_0_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionType_1_0_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = unionType_1_0_3_0_0(b, l + 1);
    r = r && whereClause(b, l + 1);
    r = r && unionType_1_0_3_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // eos?
  private static boolean unionType_1_0_3_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionType_1_0_3_0_0")) return false;
    eos(b, l + 1);
    return true;
  }

  // eos?
  private static boolean unionType_1_0_3_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionType_1_0_3_0_2")) return false;
    eos(b, l + 1);
    return true;
  }

  // directive*
  private static boolean unionType_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionType_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!directive(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "unionType_3", c)) break;
    }
    return true;
  }

  public static boolean pointerType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "pointerType")) return false;
    if (!nextTokenIsSmart(b, CARET)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeTokenSmart(b, CARET);
    p = r;
    r = p && type(b, l, 9);
    exit_section_(b, l, m, POINTER_TYPE, r, p, null);
    return r || p;
  }

  // DOLLAR declaredIdentifier
  public static boolean polymorphicType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "polymorphicType")) return false;
    if (!nextTokenIsSmart(b, DOLLAR)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, DOLLAR);
    r = r && declaredIdentifier(b, l + 1);
    exit_section_(b, m, POLYMORPHIC_TYPE, r);
    return r;
  }

  // parenthesizedExpression
  public static boolean parExpressionType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parExpressionType")) return false;
    if (!nextTokenIsSmart(b, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = parenthesizedExpression(b, l + 1);
    exit_section_(b, m, PAR_EXPRESSION_TYPE, r);
    return r;
  }

  // identifier DOT (callType | simpleRefType)
  public static boolean qualifiedType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qualifiedType")) return false;
    if (!nextTokenIsSmart(b, IDENTIFIER_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = identifier(b, l + 1);
    r = r && consumeToken(b, DOT);
    r = r && qualifiedType_2(b, l + 1);
    exit_section_(b, m, QUALIFIED_TYPE, r);
    return r;
  }

  // callType | simpleRefType
  private static boolean qualifiedType_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qualifiedType_2")) return false;
    boolean r;
    r = callType(b, l + 1);
    if (!r) r = simpleRefType(b, l + 1);
    return r;
  }

  // identifier arguments
  public static boolean callType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "callType")) return false;
    if (!nextTokenIsSmart(b, IDENTIFIER_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = identifier(b, l + 1);
    r = r && arguments(b, l + 1);
    exit_section_(b, m, CALL_TYPE, r);
    return r;
  }

  public static boolean variadicType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variadicType")) return false;
    if (!nextTokenIsSmart(b, RANGE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeTokenSmart(b, RANGE);
    p = r;
    r = p && type(b, l, 15);
    exit_section_(b, l, m, VARIADIC_TYPE, r, p, null);
    return r || p;
  }

  // identifier
  public static boolean simpleRefType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpleRefType")) return false;
    if (!nextTokenIsSmart(b, IDENTIFIER_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = identifier(b, l + 1);
    exit_section_(b, m, SIMPLE_REF_TYPE, r);
    return r;
  }

}
