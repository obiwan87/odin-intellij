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
    create_token_set_(ARGUMENT, NAMED_ARGUMENT, UNNAMED_ARGUMENT),
    create_token_set_(PARAMETER_DECLARATION_STATEMENT, PARAMETER_INITIALIZATION, UNNAMED_PARAMETER, VARIADIC_PARAMETER_DECLARATION),
    create_token_set_(ASSIGNMENT_STATEMENT, ATTRIBUTE_STATEMENT, BITSET_DECLARATION_STATEMENT, BLOCK_STATEMENT,
      BREAK_STATEMENT, CONSTANT_INITIALIZATION_STATEMENT, CONTINUE_STATEMENT, DEFER_STATEMENT,
      DO_STATEMENT, ENUM_DECLARATION_STATEMENT, EXPRESSION_STATEMENT, FALLTHROUGH_STATEMENT,
      FIELD_DECLARATION_STATEMENT, FILE_SCOPE_STATEMENT, FOREIGN_BLOCK_STATEMENT, FOREIGN_IMPORT_DECLARATION_STATEMENT,
      FOREIGN_PROCEDURE_DECLARATION_STATEMENT, FOREIGN_STATEMENT, FOR_STATEMENT, IF_STATEMENT,
      IMPORT_DECLARATION_STATEMENT, PROCEDURE_DECLARATION_STATEMENT, PROCEDURE_OVERLOAD_STATEMENT, RETURN_STATEMENT,
      STATEMENT, STRUCT_DECLARATION_STATEMENT, SWITCH_STATEMENT, TAG_STATEMENT,
      UNION_DECLARATION_STATEMENT, USING_STATEMENT, VARIABLE_DECLARATION_STATEMENT, VARIABLE_INITIALIZATION_STATEMENT,
      WHEN_STATEMENT),
    create_token_set_(ARRAY_TYPE, AUTO_CAST_EXPRESSION, BINARY_EXPRESSION, BIT_SET_TYPE,
      CALL_EXPRESSION, CAST_EXPRESSION, COMPOUND_LITERAL_EXPRESSION, CONSTRAINED_TYPE,
      DEREFERENCE_EXPRESSION, ELVIS_EXPRESSION, ENUM_TYPE, EXPRESSION,
      GENERIC_TYPE, INDEX_EXPRESSION, LITERAL_EXPRESSION, MAP_TYPE,
      MATRIX_TYPE, MAYBE_EXPRESSION, MULTI_POINTER_TYPE, OR_BREAK_EXPRESSION,
      OR_CONTINUE_EXPRESSION, OR_RETURN_EXPRESSION, PARENTHESIZED_EXPRESSION, PAR_EXPRESSION_TYPE,
      POINTER_TYPE, PROCEDURE_EXPRESSION, PROCEDURE_TYPE, REF_EXPRESSION,
      SLICE_EXPRESSION, STRUCT_TYPE, TAG_STATEMENT_EXPRESSION, TERNARY_IF_EXPRESSION,
      TERNARY_WHEN_EXPRESSION, TRANSMUTE_EXPRESSION, TRIPLE_DASH_LITERAL_EXPRESSION, TYPE_ASSERTION_EXPRESSION,
      TYPE_DEFINITION_EXPRESSION, TYPE_EXPRESSION, TYPE_REF, UNARY_AND_EXPRESSION,
      UNARY_DOT_EXPRESSION, UNARY_MINUS_EXPRESSION, UNARY_NOT_EXPRESSION, UNARY_PLUS_EXPRESSION,
      UNARY_RANGE_EXPRESSION, UNARY_TILDE_EXPRESSION, UNINITIALIZED_EXPRESSION, UNION_TYPE),
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
  // nil_literal
  //                                          | string_literal
  //                                          | numeric_literal
  //                                          | boolean_literal
  //                                          | tripleDashLiteral_expression
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
  // [eos] tagStatement* blockStart statementList? blockEnd
  public static boolean block(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "block")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BLOCK, "<block>");
    r = block_0(b, l + 1);
    r = r && block_1(b, l + 1);
    r = r && blockStart(b, l + 1);
    r = r && block_3(b, l + 1);
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

  // tagStatement*
  private static boolean block_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "block_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!tagStatement(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "block_1", c)) break;
    }
    return true;
  }

  // statementList?
  private static boolean block_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "block_3")) return false;
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
  // [(tagStatement eos*)|label] block
  public static boolean blockStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "blockStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, BLOCK_STATEMENT, "<block statement>");
    r = blockStatement_0(b, l + 1);
    r = r && block(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // [(tagStatement eos*)|label]
  private static boolean blockStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "blockStatement_0")) return false;
    blockStatement_0_0(b, l + 1);
    return true;
  }

  // (tagStatement eos*)|label
  private static boolean blockStatement_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "blockStatement_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = blockStatement_0_0_0(b, l + 1);
    if (!r) r = label(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // tagStatement eos*
  private static boolean blockStatement_0_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "blockStatement_0_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = tagStatement(b, l + 1);
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
  //                                          | FALSE
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
  // arrayType | matrixType | bitSetType  | mapType | structType | typeRef | parExpressionType
  static boolean compoundType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compoundType")) return false;
    boolean r;
    r = arrayType(b, l + 1);
    if (!r) r = matrixType(b, l + 1);
    if (!r) r = bitSetType(b, l + 1);
    if (!r) r = mapType(b, l + 1);
    if (!r) r = structType(b, l + 1);
    if (!r) r = typeRef(b, l + 1);
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
  // compoundType compound_value_typed
  public static boolean compound_literal_typed(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compound_literal_typed")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, COMPOUND_LITERAL_TYPED, "<compound literal typed>");
    r = compoundType(b, l + 1);
    r = r && compound_value_typed(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // compound_value_untyped
  public static boolean compound_literal_untyped(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compound_literal_untyped")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, COMPOUND_LITERAL_UNTYPED, "<compound literal untyped>");
    r = compound_value_untyped(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // ((<<isModeOn "PAR">> | <<isModeOff "BLOCK">>) compoundValueStart [compoundValueBody] compoundValueEnd)
  //                                          | (compoundValueStart [compoundValueBody] compoundValueEnd <<beforeComma>>)
  public static boolean compound_value_typed(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compound_value_typed")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, COMPOUND_VALUE, "<compound value typed>");
    r = compound_value_typed_0(b, l + 1);
    if (!r) r = compound_value_typed_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (<<isModeOn "PAR">> | <<isModeOff "BLOCK">>) compoundValueStart [compoundValueBody] compoundValueEnd
  private static boolean compound_value_typed_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compound_value_typed_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = compound_value_typed_0_0(b, l + 1);
    r = r && compoundValueStart(b, l + 1);
    r = r && compound_value_typed_0_2(b, l + 1);
    r = r && compoundValueEnd(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // <<isModeOn "PAR">> | <<isModeOff "BLOCK">>
  private static boolean compound_value_typed_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compound_value_typed_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = isModeOn(b, l + 1, "PAR");
    if (!r) r = isModeOff(b, l + 1, "BLOCK");
    exit_section_(b, m, null, r);
    return r;
  }

  // [compoundValueBody]
  private static boolean compound_value_typed_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compound_value_typed_0_2")) return false;
    compoundValueBody(b, l + 1);
    return true;
  }

  // compoundValueStart [compoundValueBody] compoundValueEnd <<beforeComma>>
  private static boolean compound_value_typed_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compound_value_typed_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = compoundValueStart(b, l + 1);
    r = r && compound_value_typed_1_1(b, l + 1);
    r = r && compoundValueEnd(b, l + 1);
    r = r && beforeComma(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [compoundValueBody]
  private static boolean compound_value_typed_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compound_value_typed_1_1")) return false;
    compoundValueBody(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // ((<<isModeOn "OPERAND">> | <<isModeOn "PAR">> | <<isModeOff "BLOCK">>) compoundValueStart [compoundValueBody] compoundValueEnd)
  //                                          | (compoundValueStart [compoundValueBody] compoundValueEnd <<beforeOperator>>)
  public static boolean compound_value_untyped(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compound_value_untyped")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, COMPOUND_VALUE, "<compound value untyped>");
    r = compound_value_untyped_0(b, l + 1);
    if (!r) r = compound_value_untyped_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (<<isModeOn "OPERAND">> | <<isModeOn "PAR">> | <<isModeOff "BLOCK">>) compoundValueStart [compoundValueBody] compoundValueEnd
  private static boolean compound_value_untyped_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compound_value_untyped_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = compound_value_untyped_0_0(b, l + 1);
    r = r && compoundValueStart(b, l + 1);
    r = r && compound_value_untyped_0_2(b, l + 1);
    r = r && compoundValueEnd(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // <<isModeOn "OPERAND">> | <<isModeOn "PAR">> | <<isModeOff "BLOCK">>
  private static boolean compound_value_untyped_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compound_value_untyped_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = isModeOn(b, l + 1, "OPERAND");
    if (!r) r = isModeOn(b, l + 1, "PAR");
    if (!r) r = isModeOff(b, l + 1, "BLOCK");
    exit_section_(b, m, null, r);
    return r;
  }

  // [compoundValueBody]
  private static boolean compound_value_untyped_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compound_value_untyped_0_2")) return false;
    compoundValueBody(b, l + 1);
    return true;
  }

  // compoundValueStart [compoundValueBody] compoundValueEnd <<beforeOperator>>
  private static boolean compound_value_untyped_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compound_value_untyped_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = compoundValueStart(b, l + 1);
    r = r && compound_value_untyped_1_1(b, l + 1);
    r = r && compoundValueEnd(b, l + 1);
    r = r && beforeOperator(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [compoundValueBody]
  private static boolean compound_value_untyped_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compound_value_untyped_1_1")) return false;
    compoundValueBody(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // <<enterMode "BLOCK">> [ifInit SEMICOLON] expression <<exitMode "BLOCK">>
  public static boolean condition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "condition")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CONDITION, "<condition>");
    r = enterMode(b, l + 1, "BLOCK");
    r = r && condition_1(b, l + 1);
    r = r && expression(b, l + 1, -1);
    r = r && exitMode(b, l + 1, "BLOCK");
    exit_section_(b, l, m, r, false, null);
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
  //                                          | ifStatement
  //                                          | forStatement
  //                                          | switchStatement
  //                                          | block
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
  // elseIf condition statementBody
  public static boolean elseIfBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "elseIfBlock")) return false;
    if (!nextTokenIs(b, ELSE_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = elseIf(b, l + 1);
    r = r && condition(b, l + 1);
    r = r && statementBody(b, l + 1);
    exit_section_(b, m, ELSE_IF_BLOCK, r);
    return r;
  }

  /* ********************************************************** */
  // else WHEN condition statementBody
  public static boolean elseWhenBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "elseWhenBlock")) return false;
    if (!nextTokenIs(b, ELSE_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = else_$(b, l + 1);
    r = r && consumeToken(b, WHEN);
    r = r && condition(b, l + 1);
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
  // (attributeStatement eos*)* USING? declaredIdentifier doubleColonOperator DISTINCT? enumType
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

  // (attributeStatement eos*)*
  private static boolean enumDeclarationStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumDeclarationStatement_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!enumDeclarationStatement_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "enumDeclarationStatement_0", c)) break;
    }
    return true;
  }

  // attributeStatement eos*
  private static boolean enumDeclarationStatement_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumDeclarationStatement_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = attributeStatement(b, l + 1);
    r = r && enumDeclarationStatement_0_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // eos*
  private static boolean enumDeclarationStatement_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumDeclarationStatement_0_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!eos(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "enumDeclarationStatement_0_0_1", c)) break;
    }
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
  // USING? tagHead? declaredIdentifier (COMMA declaredIdentifier)* COLON typeDefinition_expression [RAW_STRING_LITERAL]
  public static boolean fieldDeclarationStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldDeclarationStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FIELD_DECLARATION_STATEMENT, "<field declaration statement>");
    r = fieldDeclarationStatement_0(b, l + 1);
    r = r && fieldDeclarationStatement_1(b, l + 1);
    r = r && declaredIdentifier(b, l + 1);
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
  //                                          | procedureOverloadStatement
  //                                          | foreignStatement
  //                                          | whenStatement
  //                                          | tagStatement_expression
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
    if (!r) r = procedureOverloadStatement(b, l + 1);
    if (!r) r = foreignStatement(b, l + 1);
    if (!r) r = whenStatement(b, l + 1);
    if (!r) r = tagStatement_expression(b, l + 1);
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
  // assignmentStatement | call_expression
  static boolean forEndStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forEndStatement")) return false;
    boolean r;
    r = assignmentStatement(b, l + 1);
    if (!r) r = expression(b, l + 1, 11);
    return r;
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
  // [forInExpression (COMMA forInExpression)*] IN expression
  static boolean forIn(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forIn")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = forIn_0(b, l + 1);
    r = r && consumeToken(b, IN);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [forInExpression (COMMA forInExpression)*]
  private static boolean forIn_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forIn_0")) return false;
    forIn_0_0(b, l + 1);
    return true;
  }

  // forInExpression (COMMA forInExpression)*
  private static boolean forIn_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forIn_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = forInExpression(b, l + 1);
    r = r && forIn_0_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA forInExpression)*
  private static boolean forIn_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forIn_0_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!forIn_0_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "forIn_0_0_1", c)) break;
    }
    return true;
  }

  // COMMA forInExpression
  private static boolean forIn_0_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forIn_0_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && forInExpression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // AND? identifier
  static boolean forInExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forInExpression")) return false;
    if (!nextTokenIs(b, "", AND, IDENTIFIER_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = forInExpression_0(b, l + 1);
    r = r && identifier(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // AND?
  private static boolean forInExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forInExpression_0")) return false;
    consumeToken(b, AND);
    return true;
  }

  /* ********************************************************** */
  // variableInitializationStatement | assignmentStatement | call_expression
  static boolean forStartStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forStartStatement")) return false;
    boolean r;
    r = variableInitializationStatement(b, l + 1);
    if (!r) r = assignmentStatement(b, l + 1);
    if (!r) r = expression(b, l + 1, 11);
    return r;
  }

  /* ********************************************************** */
  // label? tagStatement? forHead statementBody
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

  // tagStatement?
  private static boolean forStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forStatement_1")) return false;
    tagStatement(b, l + 1);
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
  //                                          |foreignProcedureDeclarationStatement
  //                                          |whenStatement
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
  // (attributeStatement eos?)* declaredIdentifier doubleColonOperator PROC string_literal? LPAREN [paramEntries] RPAREN [ARROW returnArguments] TRIPLE_DASH
  public static boolean foreignProcedureDeclarationStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignProcedureDeclarationStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, FOREIGN_PROCEDURE_DECLARATION_STATEMENT, "<foreign procedure declaration statement>");
    r = foreignProcedureDeclarationStatement_0(b, l + 1);
    r = r && declaredIdentifier(b, l + 1);
    r = r && doubleColonOperator(b, l + 1);
    r = r && consumeToken(b, PROC);
    r = r && foreignProcedureDeclarationStatement_4(b, l + 1);
    r = r && consumeToken(b, LPAREN);
    r = r && foreignProcedureDeclarationStatement_6(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    r = r && foreignProcedureDeclarationStatement_8(b, l + 1);
    r = r && consumeToken(b, TRIPLE_DASH);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (attributeStatement eos?)*
  private static boolean foreignProcedureDeclarationStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignProcedureDeclarationStatement_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!foreignProcedureDeclarationStatement_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "foreignProcedureDeclarationStatement_0", c)) break;
    }
    return true;
  }

  // attributeStatement eos?
  private static boolean foreignProcedureDeclarationStatement_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignProcedureDeclarationStatement_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = attributeStatement(b, l + 1);
    r = r && foreignProcedureDeclarationStatement_0_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // eos?
  private static boolean foreignProcedureDeclarationStatement_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignProcedureDeclarationStatement_0_0_1")) return false;
    eos(b, l + 1);
    return true;
  }

  // string_literal?
  private static boolean foreignProcedureDeclarationStatement_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignProcedureDeclarationStatement_4")) return false;
    string_literal(b, l + 1);
    return true;
  }

  // [paramEntries]
  private static boolean foreignProcedureDeclarationStatement_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignProcedureDeclarationStatement_6")) return false;
    paramEntries(b, l + 1);
    return true;
  }

  // [ARROW returnArguments]
  private static boolean foreignProcedureDeclarationStatement_8(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignProcedureDeclarationStatement_8")) return false;
    foreignProcedureDeclarationStatement_8_0(b, l + 1);
    return true;
  }

  // ARROW returnArguments
  private static boolean foreignProcedureDeclarationStatement_8_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "foreignProcedureDeclarationStatement_8_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ARROW);
    r = r && returnArguments(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
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
  // [label] [tagHead] if condition statementBody (sos elseIfBlock)* [sos elseBlock]
  public static boolean ifStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, IF_STATEMENT, "<if statement>");
    r = ifStatement_0(b, l + 1);
    r = r && ifStatement_1(b, l + 1);
    r = r && if_$(b, l + 1);
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

  // (sos elseIfBlock)*
  private static boolean ifStatement_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifStatement_5")) return false;
    while (true) {
      int c = current_position_(b);
      if (!ifStatement_5_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ifStatement_5", c)) break;
    }
    return true;
  }

  // sos elseIfBlock
  private static boolean ifStatement_5_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifStatement_5_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = sos(b, l + 1);
    r = r && elseIfBlock(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [sos elseBlock]
  private static boolean ifStatement_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifStatement_6")) return false;
    ifStatement_6_0(b, l + 1);
    return true;
  }

  // sos elseBlock
  private static boolean ifStatement_6_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifStatement_6_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = sos(b, l + 1);
    r = r && elseBlock(b, l + 1);
    exit_section_(b, m, null, r);
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
  // type_expression | parenthesized_expression
  static boolean mainType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mainType")) return false;
    boolean r;
    r = type_expression(b, l + 1, -1);
    if (!r) r = parenthesized_expression(b, l + 1);
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
  //                                          | INTEGER_HEX_LITERAL
  //                                          | INTEGER_OCT_LITERAL
  //                                          | INTEGER_BIN_LITERAL
  //                                          | FLOAT_DEC_LITERAL
  //                                          | COMPLEX_INTEGER_DEC_LITERAL
  //                                          | COMPLEX_FLOAT_LITERAL
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
  // parenthesized_expression
  public static boolean parExpressionType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parExpressionType")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, PAR_EXPRESSION_TYPE, null);
    r = parenthesized_expression(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // paramEntry (COMMA paramEntry)* COMMA?
  public static boolean paramEntries(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "paramEntries")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PARAM_ENTRIES, "<param entries>");
    r = paramEntry(b, l + 1);
    r = r && paramEntries_1(b, l + 1);
    r = r && paramEntries_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
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
  // tagStatement? (
  //                                          variadicParameterDeclaration
  //                                          | parameterInitialization
  //                                          | parameterDeclarationStatement
  //                                          | unnamedParameter
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

  // tagStatement?
  private static boolean paramEntry_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "paramEntry_0")) return false;
    tagStatement(b, l + 1);
    return true;
  }

  // variadicParameterDeclaration
  //                                          | parameterInitialization
  //                                          | parameterDeclarationStatement
  //                                          | unnamedParameter
  private static boolean paramEntry_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "paramEntry_1")) return false;
    boolean r;
    r = variadicParameterDeclaration(b, l + 1);
    if (!r) r = parameterInitialization(b, l + 1);
    if (!r) r = parameterDeclarationStatement(b, l + 1);
    if (!r) r = unnamedParameter(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // [USING] tagStatement? declaredIdentifier
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

  // tagStatement?
  private static boolean parameter_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_1")) return false;
    tagStatement(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // parameter (COMMA parameter)* COLON typeDefinition_expression
  public static boolean parameterDeclarationStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterDeclarationStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PARAMETER_DECLARATION_STATEMENT, "<parameter declaration statement>");
    r = parameter(b, l + 1);
    r = r && parameterDeclarationStatement_1(b, l + 1);
    r = r && consumeToken(b, COLON);
    r = r && typeDefinition_expression(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (COMMA parameter)*
  private static boolean parameterDeclarationStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterDeclarationStatement_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!parameterDeclarationStatement_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "parameterDeclarationStatement_1", c)) break;
    }
    return true;
  }

  // COMMA parameter
  private static boolean parameterDeclarationStatement_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterDeclarationStatement_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && parameter(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // parameter COLON [typeDefinition_expression] EQ expression
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

  // [typeDefinition_expression]
  private static boolean parameterInitialization_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterInitialization_2")) return false;
    typeDefinition_expression(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // tagStatement? (
  //                                          variadicParameterDeclaration
  //                                          | parameterInitialization
  //                                          | parameterDeclarationStatement
  //                                          )
  public static boolean polymorphicParameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "polymorphicParameter")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, POLYMORPHIC_PARAMETER, "<polymorphic parameter>");
    r = polymorphicParameter_0(b, l + 1);
    r = r && polymorphicParameter_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // tagStatement?
  private static boolean polymorphicParameter_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "polymorphicParameter_0")) return false;
    tagStatement(b, l + 1);
    return true;
  }

  // variadicParameterDeclaration
  //                                          | parameterInitialization
  //                                          | parameterDeclarationStatement
  private static boolean polymorphicParameter_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "polymorphicParameter_1")) return false;
    boolean r;
    r = variadicParameterDeclaration(b, l + 1);
    if (!r) r = parameterInitialization(b, l + 1);
    if (!r) r = parameterDeclarationStatement(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // polymorphicParameter (COMMA polymorphicParameter)* COMMA?
  public static boolean polymorphicParameterList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "polymorphicParameterList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, POLYMORPHIC_PARAMETER_LIST, "<polymorphic parameter list>");
    r = polymorphicParameter(b, l + 1);
    r = r && polymorphicParameterList_1(b, l + 1);
    r = r && polymorphicParameterList_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (COMMA polymorphicParameter)*
  private static boolean polymorphicParameterList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "polymorphicParameterList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!polymorphicParameterList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "polymorphicParameterList_1", c)) break;
    }
    return true;
  }

  // COMMA polymorphicParameter
  private static boolean polymorphicParameterList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "polymorphicParameterList_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && polymorphicParameter(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // COMMA?
  private static boolean polymorphicParameterList_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "polymorphicParameterList_2")) return false;
    consumeToken(b, COMMA);
    return true;
  }

  /* ********************************************************** */
  // block| [tagStatement] TRIPLE_DASH
  public static boolean procedureBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureBody")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PROCEDURE_BODY, "<procedure body>");
    r = block(b, l + 1);
    if (!r) r = procedureBody_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // [tagStatement] TRIPLE_DASH
  private static boolean procedureBody_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureBody_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = procedureBody_1_0(b, l + 1);
    r = r && consumeToken(b, TRIPLE_DASH);
    exit_section_(b, m, null, r);
    return r;
  }

  // [tagStatement]
  private static boolean procedureBody_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureBody_1_0")) return false;
    tagStatement(b, l + 1);
    return true;
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
  public static boolean procedureOverloadStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureOverloadStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, PROCEDURE_OVERLOAD_STATEMENT, "<procedure overload statement>");
    r = procedureOverloadStatement_0(b, l + 1);
    r = r && declaredIdentifier(b, l + 1);
    r = r && doubleColonOperator(b, l + 1);
    r = r && consumeTokens(b, 0, PROC, LBRACE);
    r = r && identifier(b, l + 1);
    r = r && procedureOverloadStatement_6(b, l + 1);
    r = r && procedureOverloadStatement_7(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (attributeStatement eos?)*
  private static boolean procedureOverloadStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureOverloadStatement_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!procedureOverloadStatement_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "procedureOverloadStatement_0", c)) break;
    }
    return true;
  }

  // attributeStatement eos?
  private static boolean procedureOverloadStatement_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureOverloadStatement_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = attributeStatement(b, l + 1);
    r = r && procedureOverloadStatement_0_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // eos?
  private static boolean procedureOverloadStatement_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureOverloadStatement_0_0_1")) return false;
    eos(b, l + 1);
    return true;
  }

  // (COMMA identifier)*
  private static boolean procedureOverloadStatement_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureOverloadStatement_6")) return false;
    while (true) {
      int c = current_position_(b);
      if (!procedureOverloadStatement_6_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "procedureOverloadStatement_6", c)) break;
    }
    return true;
  }

  // COMMA identifier
  private static boolean procedureOverloadStatement_6_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureOverloadStatement_6_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && identifier(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // COMMA?
  private static boolean procedureOverloadStatement_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureOverloadStatement_7")) return false;
    consumeToken(b, COMMA);
    return true;
  }

  /* ********************************************************** */
  // identifier [DOT identifier]
  static boolean qualifiedNameTypeIdentifier(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qualifiedNameTypeIdentifier")) return false;
    if (!nextTokenIs(b, IDENTIFIER_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = identifier(b, l + 1);
    r = r && qualifiedNameTypeIdentifier_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [DOT identifier]
  private static boolean qualifiedNameTypeIdentifier_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qualifiedNameTypeIdentifier_1")) return false;
    qualifiedNameTypeIdentifier_1_0(b, l + 1);
    return true;
  }

  // DOT identifier
  private static boolean qualifiedNameTypeIdentifier_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qualifiedNameTypeIdentifier_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DOT);
    r = r && identifier(b, l + 1);
    exit_section_(b, m, null, r);
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
  // NOT | LPAREN paramEntries RPAREN | typeDefinition_expression
  public static boolean returnArguments(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnArguments")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RETURN_ARGUMENTS, "<return arguments>");
    r = consumeToken(b, NOT);
    if (!r) r = returnArguments_1(b, l + 1);
    if (!r) r = typeDefinition_expression(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // LPAREN paramEntries RPAREN
  private static boolean returnArguments_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnArguments_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && paramEntries(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // [tagStatement] RETURN returnArgumentList?
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

  // [tagStatement]
  private static boolean returnStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnStatement_0")) return false;
    tagStatement(b, l + 1);
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
  //                                          | procedureOverloadStatement
  //                                          | constantInitializationStatement
  //                                          | variableDeclarationStatement
  //                                          | bitsetDeclarationStatement
  //                                          | blockStatement
  //                                          | forStatement
  //                                          | ifStatement
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
    if (!r) r = procedureOverloadStatement(b, l + 1);
    if (!r) r = constantInitializationStatement(b, l + 1);
    if (!r) r = variableDeclarationStatement(b, l + 1);
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
  static boolean statementBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statementBody")) return false;
    boolean r;
    r = block(b, l + 1);
    if (!r) r = doStatement(b, l + 1);
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
  // (attributeStatement eos*)* declaredIdentifier doubleColonOperator DISTINCT? structType
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

  // (attributeStatement eos*)*
  private static boolean structDeclarationStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structDeclarationStatement_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!structDeclarationStatement_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "structDeclarationStatement_0", c)) break;
    }
    return true;
  }

  // attributeStatement eos*
  private static boolean structDeclarationStatement_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structDeclarationStatement_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = attributeStatement(b, l + 1);
    r = r && structDeclarationStatement_0_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // eos*
  private static boolean structDeclarationStatement_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structDeclarationStatement_0_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!eos(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "structDeclarationStatement_0_0_1", c)) break;
    }
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
  // CASE (expression (COMMA expression)*)? caseBlock
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

  // (expression (COMMA expression)*)?
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
  // SWITCH IN? <<enterMode "BLOCK">> switchCondition? <<exitMode "BLOCK">>
  public static boolean switchHead(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchHead")) return false;
    if (!nextTokenIs(b, SWITCH)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SWITCH);
    r = r && switchHead_1(b, l + 1);
    r = r && enterMode(b, l + 1, "BLOCK");
    r = r && switchHead_3(b, l + 1);
    r = r && exitMode(b, l + 1, "BLOCK");
    exit_section_(b, m, SWITCH_HEAD, r);
    return r;
  }

  // IN?
  private static boolean switchHead_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchHead_1")) return false;
    consumeToken(b, IN);
    return true;
  }

  // switchCondition?
  private static boolean switchHead_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchHead_3")) return false;
    switchCondition(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // [label] [tagStatement_expression] switchHead switchBody
  public static boolean switchStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SWITCH_STATEMENT, "<switch statement>");
    r = switchStatement_0(b, l + 1);
    r = r && switchStatement_1(b, l + 1);
    r = r && switchHead(b, l + 1);
    r = r && switchBody(b, l + 1);
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

  /* ********************************************************** */
  // (attributeStatement eos*)* [tagHead] identifierList colonOpening typeDefinition_expression?
  static boolean symbolDefinitionHead(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "symbolDefinitionHead")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = symbolDefinitionHead_0(b, l + 1);
    r = r && symbolDefinitionHead_1(b, l + 1);
    r = r && identifierList(b, l + 1);
    r = r && colonOpening(b, l + 1);
    r = r && symbolDefinitionHead_4(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (attributeStatement eos*)*
  private static boolean symbolDefinitionHead_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "symbolDefinitionHead_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!symbolDefinitionHead_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "symbolDefinitionHead_0", c)) break;
    }
    return true;
  }

  // attributeStatement eos*
  private static boolean symbolDefinitionHead_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "symbolDefinitionHead_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = attributeStatement(b, l + 1);
    r = r && symbolDefinitionHead_0_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // eos*
  private static boolean symbolDefinitionHead_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "symbolDefinitionHead_0_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!eos(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "symbolDefinitionHead_0_0_1", c)) break;
    }
    return true;
  }

  // [tagHead]
  private static boolean symbolDefinitionHead_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "symbolDefinitionHead_1")) return false;
    tagHead(b, l + 1);
    return true;
  }

  // typeDefinition_expression?
  private static boolean symbolDefinitionHead_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "symbolDefinitionHead_4")) return false;
    typeDefinition_expression(b, l + 1);
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
  // tagHead [basic_literal | (LPAREN expressionsList? RPAREN)]
  public static boolean tagStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tagStatement")) return false;
    if (!nextTokenIs(b, HASH)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = tagHead(b, l + 1);
    r = r && tagStatement_1(b, l + 1);
    exit_section_(b, m, TAG_STATEMENT, r);
    return r;
  }

  // [basic_literal | (LPAREN expressionsList? RPAREN)]
  private static boolean tagStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tagStatement_1")) return false;
    tagStatement_1_0(b, l + 1);
    return true;
  }

  // basic_literal | (LPAREN expressionsList? RPAREN)
  private static boolean tagStatement_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tagStatement_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = basic_literal(b, l + 1);
    if (!r) r = tagStatement_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // LPAREN expressionsList? RPAREN
  private static boolean tagStatement_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tagStatement_1_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && tagStatement_1_0_1_1(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // expressionsList?
  private static boolean tagStatement_1_0_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tagStatement_1_0_1_1")) return false;
    expressionsList(b, l + 1);
    return true;
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
  // typeDefinition_expression
  static boolean typeDefinition(PsiBuilder b, int l) {
    return typeDefinition_expression(b, l + 1);
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
  // typeDefinition_expression (COMMA typeDefinition_expression)* [COMMA|EOS_TOKEN]
  public static boolean unionBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionBody")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, UNION_BODY, "<union body>");
    r = typeDefinition_expression(b, l + 1);
    r = r && unionBody_1(b, l + 1);
    r = r && unionBody_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (COMMA typeDefinition_expression)*
  private static boolean unionBody_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionBody_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!unionBody_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "unionBody_1", c)) break;
    }
    return true;
  }

  // COMMA typeDefinition_expression
  private static boolean unionBody_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionBody_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && typeDefinition_expression(b, l + 1);
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
  // (attributeStatement eos*)* declaredIdentifier doubleColonOperator DISTINCT? unionType
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

  // (attributeStatement eos*)*
  private static boolean unionDeclarationStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionDeclarationStatement_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!unionDeclarationStatement_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "unionDeclarationStatement_0", c)) break;
    }
    return true;
  }

  // attributeStatement eos*
  private static boolean unionDeclarationStatement_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionDeclarationStatement_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = attributeStatement(b, l + 1);
    r = r && unionDeclarationStatement_0_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // eos*
  private static boolean unionDeclarationStatement_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionDeclarationStatement_0_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!eos(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "unionDeclarationStatement_0_0_1", c)) break;
    }
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
  // RANGE? typeDefinition_expression
  public static boolean unnamedParameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unnamedParameter")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, UNNAMED_PARAMETER, "<unnamed parameter>");
    r = unnamedParameter_0(b, l + 1);
    r = r && typeDefinition_expression(b, l + 1);
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
  // (attributeStatement eos?)* identifierList COLON typeDefinition_expression
  public static boolean variableDeclarationStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableDeclarationStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, VARIABLE_DECLARATION_STATEMENT, "<variable declaration statement>");
    r = variableDeclarationStatement_0(b, l + 1);
    r = r && identifierList(b, l + 1);
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
  // parameter COLON RANGE typeDefinition_expression
  public static boolean variadicParameterDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variadicParameterDeclaration")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, VARIADIC_PARAMETER_DECLARATION, "<variadic parameter declaration>");
    r = parameter(b, l + 1);
    r = r && consumeTokens(b, 0, COLON, RANGE);
    r = r && typeDefinition_expression(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // WHEN condition statementBody (sos elseWhenBlock)* (sos elseBlock)?
  public static boolean whenStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "whenStatement")) return false;
    if (!nextTokenIs(b, WHEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, WHEN);
    r = r && condition(b, l + 1);
    r = r && statementBody(b, l + 1);
    r = r && whenStatement_3(b, l + 1);
    r = r && whenStatement_4(b, l + 1);
    exit_section_(b, m, WHEN_STATEMENT, r);
    return r;
  }

  // (sos elseWhenBlock)*
  private static boolean whenStatement_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "whenStatement_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!whenStatement_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "whenStatement_3", c)) break;
    }
    return true;
  }

  // sos elseWhenBlock
  private static boolean whenStatement_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "whenStatement_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = sos(b, l + 1);
    r = r && elseWhenBlock(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (sos elseBlock)?
  private static boolean whenStatement_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "whenStatement_4")) return false;
    whenStatement_4_0(b, l + 1);
    return true;
  }

  // sos elseBlock
  private static boolean whenStatement_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "whenStatement_4_0")) return false;
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
  // 0: POSTFIX(or_return_expression) POSTFIX(or_break_expression) POSTFIX(or_continue_expression)
  // 1: BINARY(or_else_expression)
  // 2: BINARY(elvis_expression) BINARY(ternary_if_expression) BINARY(ternary_when_expression)
  // 3: BINARY(range_inclusive_expression) BINARY(range_exclusive_expression)
  // 4: BINARY(or_expression)
  // 5: BINARY(and_expression)
  // 6: BINARY(lt_expression) BINARY(gt_expression) BINARY(lte_expression) BINARY(gte_expression)
  //    BINARY(eqeq_expression) BINARY(neq_expression)
  // 7: BINARY(add_expression) BINARY(sub_expression) BINARY(bitwise_or_expression) BINARY(bitwise_xor_expression)
  //    BINARY(in_expression) BINARY(not_in_expression)
  // 8: BINARY(mul_expression) BINARY(div_expression) BINARY(mod_expression) BINARY(remainder_expression)
  //    BINARY(bitwise_and_expression) BINARY(lshift_expression) BINARY(rshift_expression) BINARY(bitwise_and_not_expression)
  // 9: PREFIX(unary_plus_expression) PREFIX(unary_minus_expression) PREFIX(unary_tilde_expression) PREFIX(unary_and_expression)
  //    PREFIX(unary_not_expression) PREFIX(unary_range_expression) PREFIX(unary_dot_expression)
  // 10: POSTFIX(maybe_expression)
  // 11: ATOM(uninitialized_expression)
  // 12: POSTFIX(call_expression)
  // 13: POSTFIX(qualification_expression)
  // 14: POSTFIX(index_expression)
  // 15: POSTFIX(slice_expression)
  // 16: POSTFIX(dereference_expression)
  // 17: POSTFIX(procedure_expression)
  // 18: ATOM(transmute_expression)
  // 19: PREFIX(auto_cast_expression)
  // 20: ATOM(cast_expression)
  // 21: POSTFIX(typeAssertion_expression)
  // 22: ATOM(compound_literal_expression)
  // 23: ATOM(simple_ref_expression) ATOM(typeDefinition_expression) ATOM(tagStatement_expression) ATOM(literal_expression)
  //    PREFIX(parenthesized_expression)
  public static boolean expression(PsiBuilder b, int l, int g) {
    if (!recursion_guard_(b, l, "expression")) return false;
    addVariant(b, "<expression>");
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, "<expression>");
    r = unary_plus_expression(b, l + 1);
    if (!r) r = unary_minus_expression(b, l + 1);
    if (!r) r = unary_tilde_expression(b, l + 1);
    if (!r) r = unary_and_expression(b, l + 1);
    if (!r) r = unary_not_expression(b, l + 1);
    if (!r) r = unary_range_expression(b, l + 1);
    if (!r) r = unary_dot_expression(b, l + 1);
    if (!r) r = uninitialized_expression(b, l + 1);
    if (!r) r = transmute_expression(b, l + 1);
    if (!r) r = auto_cast_expression(b, l + 1);
    if (!r) r = cast_expression(b, l + 1);
    if (!r) r = compound_literal_expression(b, l + 1);
    if (!r) r = simple_ref_expression(b, l + 1);
    if (!r) r = typeDefinition_expression(b, l + 1);
    if (!r) r = tagStatement_expression(b, l + 1);
    if (!r) r = literal_expression(b, l + 1);
    if (!r) r = parenthesized_expression(b, l + 1);
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
      else if (g < 0 && or_break_expression_0(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, OR_BREAK_EXPRESSION, r, true, null);
      }
      else if (g < 0 && or_continue_expression_0(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, OR_CONTINUE_EXPRESSION, r, true, null);
      }
      else if (g < 1 && or_else_expression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 1));
        r = exitMode(b, l + 1, "OPERAND") && r;
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 2 && elvis_expression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 2));
        r = elvis_expression_1(b, l + 1) && r;
        exit_section_(b, l, m, ELVIS_EXPRESSION, r, true, null);
      }
      else if (g < 2 && ternary_if_expression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 2));
        r = ternary_if_expression_1(b, l + 1) && r;
        exit_section_(b, l, m, TERNARY_IF_EXPRESSION, r, true, null);
      }
      else if (g < 2 && ternary_when_expression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 2));
        r = ternary_when_expression_1(b, l + 1) && r;
        exit_section_(b, l, m, TERNARY_WHEN_EXPRESSION, r, true, null);
      }
      else if (g < 3 && range_inclusive_expression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 3));
        r = exitMode(b, l + 1, "OPERAND") && r;
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 3 && range_exclusive_expression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 3));
        r = exitMode(b, l + 1, "OPERAND") && r;
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 4 && or_expression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 4));
        r = exitMode(b, l + 1, "OPERAND") && r;
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 5 && and_expression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 5));
        r = exitMode(b, l + 1, "OPERAND") && r;
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 6 && lt_expression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 6));
        r = exitMode(b, l + 1, "OPERAND") && r;
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 6 && gt_expression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 6));
        r = exitMode(b, l + 1, "OPERAND") && r;
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 6 && lte_expression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 6));
        r = exitMode(b, l + 1, "OPERAND") && r;
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 6 && gte_expression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 6));
        r = exitMode(b, l + 1, "OPERAND") && r;
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 6 && eqeq_expression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 6));
        r = exitMode(b, l + 1, "OPERAND") && r;
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 6 && neq_expression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 6));
        r = exitMode(b, l + 1, "OPERAND") && r;
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 7 && add_expression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 7));
        r = exitMode(b, l + 1, "OPERAND") && r;
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 7 && sub_expression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 7));
        r = exitMode(b, l + 1, "OPERAND") && r;
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 7 && bitwise_or_expression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 7));
        r = exitMode(b, l + 1, "OPERAND") && r;
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 7 && bitwise_xor_expression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 7));
        r = exitMode(b, l + 1, "OPERAND") && r;
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 7 && in_expression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 7));
        r = exitMode(b, l + 1, "OPERAND") && r;
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 7 && not_in_expression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 7));
        r = exitMode(b, l + 1, "OPERAND") && r;
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 8 && mul_expression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 8));
        r = exitMode(b, l + 1, "OPERAND") && r;
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 8 && div_expression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 8));
        r = exitMode(b, l + 1, "OPERAND") && r;
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 8 && mod_expression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 8));
        r = exitMode(b, l + 1, "OPERAND") && r;
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 8 && remainder_expression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 8));
        r = exitMode(b, l + 1, "OPERAND") && r;
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 8 && bitwise_and_expression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 8));
        r = exitMode(b, l + 1, "OPERAND") && r;
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 8 && lshift_expression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 8));
        r = exitMode(b, l + 1, "OPERAND") && r;
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 8 && rshift_expression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 8));
        r = exitMode(b, l + 1, "OPERAND") && r;
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 8 && bitwise_and_not_expression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 8));
        r = exitMode(b, l + 1, "OPERAND") && r;
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 10 && consumeTokenSmart(b, DOT_QUESTION)) {
        r = true;
        exit_section_(b, l, m, MAYBE_EXPRESSION, r, true, null);
      }
      else if (g < 12 && arguments(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, CALL_EXPRESSION, r, true, null);
      }
      else if (g < 13 && qualification_expression_0(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, REF_EXPRESSION, r, true, null);
      }
      else if (g < 14 && index(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, INDEX_EXPRESSION, r, true, null);
      }
      else if (g < 15 && slice(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, SLICE_EXPRESSION, r, true, null);
      }
      else if (g < 16 && consumeTokenSmart(b, CARET)) {
        r = true;
        exit_section_(b, l, m, DEREFERENCE_EXPRESSION, r, true, null);
      }
      else if (g < 17 && leftMarkerIs(b, PROCEDURE_TYPE) && procedureBody(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, PROCEDURE_EXPRESSION, r, true, null);
      }
      else if (g < 21 && typeAssertion_expression_0(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, TYPE_ASSERTION_EXPRESSION, r, true, null);
      }
      else {
        exit_section_(b, l, m, null, false, false, null);
        break;
      }
    }
    return r;
  }

  // OR_BREAK [IDENTIFIER_TOKEN]
  private static boolean or_break_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "or_break_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, OR_BREAK);
    r = r && or_break_expression_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [IDENTIFIER_TOKEN]
  private static boolean or_break_expression_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "or_break_expression_0_1")) return false;
    consumeTokenSmart(b, IDENTIFIER_TOKEN);
    return true;
  }

  // OR_CONTINUE [IDENTIFIER_TOKEN]
  private static boolean or_continue_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "or_continue_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, OR_CONTINUE);
    r = r && or_continue_expression_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [IDENTIFIER_TOKEN]
  private static boolean or_continue_expression_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "or_continue_expression_0_1")) return false;
    consumeTokenSmart(b, IDENTIFIER_TOKEN);
    return true;
  }

  // OR_ELSE <<enterMode "OPERAND">>
  private static boolean or_else_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "or_else_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, OR_ELSE);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // QUESTION <<enterMode "OPERAND">>
  private static boolean elvis_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "elvis_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, QUESTION);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // COLON expression <<exitMode "OPERAND">>
  private static boolean elvis_expression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "elvis_expression_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COLON);
    r = r && expression(b, l + 1, -1);
    r = r && exitMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // if <<enterMode "OPERAND">>
  private static boolean ternary_if_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ternary_if_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = if_$(b, l + 1);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // else expression <<exitMode "OPERAND">>
  private static boolean ternary_if_expression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ternary_if_expression_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = else_$(b, l + 1);
    r = r && expression(b, l + 1, -1);
    r = r && exitMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // WHEN <<enterMode "OPERAND">>
  private static boolean ternary_when_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ternary_when_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, WHEN);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // else expression <<exitMode "OPERAND">>
  private static boolean ternary_when_expression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ternary_when_expression_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = else_$(b, l + 1);
    r = r && expression(b, l + 1, -1);
    r = r && exitMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // RANGE_INCLUSIVE <<enterMode "OPERAND">>
  private static boolean range_inclusive_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "range_inclusive_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, RANGE_INCLUSIVE);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // RANGE_EXCLUSIVE <<enterMode "OPERAND">>
  private static boolean range_exclusive_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "range_exclusive_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, RANGE_EXCLUSIVE);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // OROR <<enterMode "OPERAND">>
  private static boolean or_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "or_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, OROR);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // ANDAND <<enterMode "OPERAND">>
  private static boolean and_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "and_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, ANDAND);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // LT <<enterMode "OPERAND">>
  private static boolean lt_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lt_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, LT);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // GT <<enterMode "OPERAND">>
  private static boolean gt_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "gt_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, GT);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // LTE <<enterMode "OPERAND">>
  private static boolean lte_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lte_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, LTE);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // GTE <<enterMode "OPERAND">>
  private static boolean gte_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "gte_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, GTE);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // EQEQ <<enterMode "OPERAND">>
  private static boolean eqeq_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "eqeq_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, EQEQ);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // NEQ <<enterMode "OPERAND">>
  private static boolean neq_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "neq_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, NEQ);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // PLUS <<enterMode "OPERAND">>
  private static boolean add_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "add_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, PLUS);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // MINUS <<enterMode "OPERAND">>
  private static boolean sub_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "sub_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, MINUS);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // PIPE <<enterMode "OPERAND">>
  private static boolean bitwise_or_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bitwise_or_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, PIPE);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // TILDE <<enterMode "OPERAND">>
  private static boolean bitwise_xor_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bitwise_xor_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, TILDE);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // IN <<enterMode "OPERAND">>
  private static boolean in_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "in_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, IN);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // NOT_IN <<enterMode "OPERAND">>
  private static boolean not_in_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "not_in_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, NOT_IN);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // STAR <<enterMode "OPERAND">>
  private static boolean mul_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mul_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, STAR);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // DIV <<enterMode "OPERAND">>
  private static boolean div_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "div_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, DIV);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // MOD <<enterMode "OPERAND">>
  private static boolean mod_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mod_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, MOD);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // REMAINDER <<enterMode "OPERAND">>
  private static boolean remainder_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "remainder_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, REMAINDER);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // AND <<enterMode "OPERAND">>
  private static boolean bitwise_and_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bitwise_and_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, AND);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // LSHIFT <<enterMode "OPERAND">>
  private static boolean lshift_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lshift_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, LSHIFT);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // RSHIFT <<enterMode "OPERAND">>
  private static boolean rshift_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rshift_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, RSHIFT);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  // ANDNOT <<enterMode "OPERAND">>
  private static boolean bitwise_and_not_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bitwise_and_not_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, ANDNOT);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  public static boolean unary_plus_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unary_plus_expression")) return false;
    if (!nextTokenIsSmart(b, PLUS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = unary_plus_expression_0(b, l + 1);
    p = r;
    r = p && expression(b, l, 9);
    r = p && report_error_(b, exitMode(b, l + 1, "OPERAND")) && r;
    exit_section_(b, l, m, UNARY_PLUS_EXPRESSION, r, p, null);
    return r || p;
  }

  // PLUS <<enterMode "OPERAND">>
  private static boolean unary_plus_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unary_plus_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, PLUS);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  public static boolean unary_minus_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unary_minus_expression")) return false;
    if (!nextTokenIsSmart(b, MINUS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = unary_minus_expression_0(b, l + 1);
    p = r;
    r = p && expression(b, l, 9);
    r = p && report_error_(b, exitMode(b, l + 1, "OPERAND")) && r;
    exit_section_(b, l, m, UNARY_MINUS_EXPRESSION, r, p, null);
    return r || p;
  }

  // MINUS <<enterMode "OPERAND">>
  private static boolean unary_minus_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unary_minus_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, MINUS);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  public static boolean unary_tilde_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unary_tilde_expression")) return false;
    if (!nextTokenIsSmart(b, TILDE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = unary_tilde_expression_0(b, l + 1);
    p = r;
    r = p && expression(b, l, 9);
    r = p && report_error_(b, exitMode(b, l + 1, "OPERAND")) && r;
    exit_section_(b, l, m, UNARY_TILDE_EXPRESSION, r, p, null);
    return r || p;
  }

  // TILDE <<enterMode "OPERAND">>
  private static boolean unary_tilde_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unary_tilde_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, TILDE);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  public static boolean unary_and_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unary_and_expression")) return false;
    if (!nextTokenIsSmart(b, AND)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = unary_and_expression_0(b, l + 1);
    p = r;
    r = p && expression(b, l, 9);
    r = p && report_error_(b, exitMode(b, l + 1, "OPERAND")) && r;
    exit_section_(b, l, m, UNARY_AND_EXPRESSION, r, p, null);
    return r || p;
  }

  // AND <<enterMode "OPERAND">>
  private static boolean unary_and_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unary_and_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, AND);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  public static boolean unary_not_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unary_not_expression")) return false;
    if (!nextTokenIsSmart(b, NOT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = unary_not_expression_0(b, l + 1);
    p = r;
    r = p && expression(b, l, 9);
    r = p && report_error_(b, exitMode(b, l + 1, "OPERAND")) && r;
    exit_section_(b, l, m, UNARY_NOT_EXPRESSION, r, p, null);
    return r || p;
  }

  // NOT <<enterMode "OPERAND">>
  private static boolean unary_not_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unary_not_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, NOT);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  public static boolean unary_range_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unary_range_expression")) return false;
    if (!nextTokenIsSmart(b, RANGE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = unary_range_expression_0(b, l + 1);
    p = r;
    r = p && expression(b, l, 9);
    r = p && report_error_(b, exitMode(b, l + 1, "OPERAND")) && r;
    exit_section_(b, l, m, UNARY_RANGE_EXPRESSION, r, p, null);
    return r || p;
  }

  // RANGE <<enterMode "OPERAND">>
  private static boolean unary_range_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unary_range_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, RANGE);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
  }

  public static boolean unary_dot_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unary_dot_expression")) return false;
    if (!nextTokenIsSmart(b, DOT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = unary_dot_expression_0(b, l + 1);
    p = r;
    r = p && expression(b, l, 9);
    r = p && report_error_(b, exitMode(b, l + 1, "OPERAND")) && r;
    exit_section_(b, l, m, UNARY_DOT_EXPRESSION, r, p, null);
    return r || p;
  }

  // DOT <<enterMode "OPERAND">>
  private static boolean unary_dot_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unary_dot_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, DOT);
    r = r && enterMode(b, l + 1, "OPERAND");
    exit_section_(b, m, null, r);
    return r;
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

  // (DOT|ARROW) identifier
  private static boolean qualification_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qualification_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = qualification_expression_0_0(b, l + 1);
    r = r && identifier(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // DOT|ARROW
  private static boolean qualification_expression_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qualification_expression_0_0")) return false;
    boolean r;
    r = consumeTokenSmart(b, DOT);
    if (!r) r = consumeTokenSmart(b, ARROW);
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
    r = p && expression(b, l, 19);
    exit_section_(b, l, m, AUTO_CAST_EXPRESSION, r, p, null);
    return r || p;
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

  // DOT LPAREN typeDefinition RPAREN
  private static boolean typeAssertion_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeAssertion_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokensSmart(b, 0, DOT, LPAREN);
    r = r && typeDefinition(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // compound_literal_typed | compound_literal_untyped
  public static boolean compound_literal_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compound_literal_expression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, COMPOUND_LITERAL_EXPRESSION, "<compound literal expression>");
    r = compound_literal_typed(b, l + 1);
    if (!r) r = compound_literal_untyped(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // identifier
  public static boolean simple_ref_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simple_ref_expression")) return false;
    if (!nextTokenIsSmart(b, IDENTIFIER_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = identifier(b, l + 1);
    exit_section_(b, m, REF_EXPRESSION, r);
    return r;
  }

  // DISTINCT? [tagStatement] mainType [DIV type_expression]
  public static boolean typeDefinition_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeDefinition_expression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, TYPE_DEFINITION_EXPRESSION, "<type definition expression>");
    r = typeDefinition_expression_0(b, l + 1);
    r = r && typeDefinition_expression_1(b, l + 1);
    r = r && mainType(b, l + 1);
    r = r && typeDefinition_expression_3(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // DISTINCT?
  private static boolean typeDefinition_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeDefinition_expression_0")) return false;
    consumeTokenSmart(b, DISTINCT);
    return true;
  }

  // [tagStatement]
  private static boolean typeDefinition_expression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeDefinition_expression_1")) return false;
    tagStatement(b, l + 1);
    return true;
  }

  // [DIV type_expression]
  private static boolean typeDefinition_expression_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeDefinition_expression_3")) return false;
    typeDefinition_expression_3_0(b, l + 1);
    return true;
  }

  // DIV type_expression
  private static boolean typeDefinition_expression_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeDefinition_expression_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, DIV);
    r = r && type_expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  // tagHead [basic_literal | (LPAREN expressionsList? RPAREN)]
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

  // [basic_literal | (LPAREN expressionsList? RPAREN)]
  private static boolean tagStatement_expression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tagStatement_expression_1")) return false;
    tagStatement_expression_1_0(b, l + 1);
    return true;
  }

  // basic_literal | (LPAREN expressionsList? RPAREN)
  private static boolean tagStatement_expression_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tagStatement_expression_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = basic_literal(b, l + 1);
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

  // basic_literal
  public static boolean literal_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "literal_expression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LITERAL_EXPRESSION, "<literal expression>");
    r = basic_literal(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  public static boolean parenthesized_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parenthesized_expression")) return false;
    if (!nextTokenIsSmart(b, LPAREN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = parenthesized_expression_0(b, l + 1);
    p = r;
    r = p && expression(b, l, -1);
    r = p && report_error_(b, parenthesized_expression_1(b, l + 1)) && r;
    exit_section_(b, l, m, PARENTHESIZED_EXPRESSION, r, p, null);
    return r || p;
  }

  // LPAREN <<enterMode "PAR">>
  private static boolean parenthesized_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parenthesized_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, LPAREN);
    r = r && enterMode(b, l + 1, "PAR");
    exit_section_(b, m, null, r);
    return r;
  }

  // <<exitMode "PAR">> RPAREN
  private static boolean parenthesized_expression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parenthesized_expression_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = exitMode(b, l + 1, "PAR");
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // Expression root: type_expression
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
  // 10: ATOM(typeRef)
  // 11: ATOM(genericType)
  // 12: BINARY(constrainedType)
  public static boolean type_expression(PsiBuilder b, int l, int g) {
    if (!recursion_guard_(b, l, "type_expression")) return false;
    addVariant(b, "<type expression>");
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, "<type expression>");
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
    if (!r) r = typeRef(b, l + 1);
    if (!r) r = genericType(b, l + 1);
    p = r;
    r = r && type_expression_0(b, l + 1, g);
    exit_section_(b, l, m, null, r, p, null);
    return r || p;
  }

  public static boolean type_expression_0(PsiBuilder b, int l, int g) {
    if (!recursion_guard_(b, l, "type_expression_0")) return false;
    boolean r = true;
    while (true) {
      Marker m = enter_section_(b, l, _LEFT_, null);
      if (g < 12 && consumeTokenSmart(b, DIV)) {
        r = type_expression(b, l, 12);
        exit_section_(b, l, m, CONSTRAINED_TYPE, r, true, null);
      }
      else {
        exit_section_(b, l, m, null, false, false, null);
        break;
      }
    }
    return r;
  }

  // MAP LBRACKET typeDefinition_expression RBRACKET typeDefinition_expression
  public static boolean mapType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mapType")) return false;
    if (!nextTokenIsSmart(b, MAP)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokensSmart(b, 0, MAP, LBRACKET);
    r = r && typeDefinition_expression(b, l + 1);
    r = r && consumeToken(b, RBRACKET);
    r = r && typeDefinition_expression(b, l + 1);
    exit_section_(b, m, MAP_TYPE, r);
    return r;
  }

  // MATRIX LBRACKET expression COMMA expression RBRACKET typeDefinition_expression
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
    r = r && typeDefinition_expression(b, l + 1);
    exit_section_(b, m, MATRIX_TYPE, r);
    return r;
  }

  // BIT_SET LBRACKET expression [SEMICOLON typeDefinition_expression] RBRACKET
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

  // [SEMICOLON typeDefinition_expression]
  private static boolean bitSetType_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bitSetType_3")) return false;
    bitSetType_3_0(b, l + 1);
    return true;
  }

  // SEMICOLON typeDefinition_expression
  private static boolean bitSetType_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bitSetType_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, SEMICOLON);
    r = r && typeDefinition_expression(b, l + 1);
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
    r = p && type_expression(b, l, 3);
    exit_section_(b, l, m, MULTI_POINTER_TYPE, r, p, null);
    return r || p;
  }

  // [tagHead] LBRACKET [QUESTION|DYNAMIC|expression] RBRACKET typeDefinition_expression
  public static boolean arrayType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayType")) return false;
    if (!nextTokenIsSmart(b, HASH, LBRACKET)) return false;
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
    r = consumeTokenSmart(b, QUESTION);
    if (!r) r = consumeTokenSmart(b, DYNAMIC);
    if (!r) r = expression(b, l + 1, -1);
    return r;
  }

  // tagStatement? PROC string_literal? LPAREN [paramEntries] RPAREN [ARROW returnArguments] <<enterMode "BLOCK">> [eos* whereClause eos*] <<exitMode "BLOCK">>
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
    r = r && enterMode(b, l + 1, "BLOCK");
    r = r && procedureType_8(b, l + 1);
    r = r && exitMode(b, l + 1, "BLOCK");
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // tagStatement?
  private static boolean procedureType_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureType_0")) return false;
    tagStatement(b, l + 1);
    return true;
  }

  // string_literal?
  private static boolean procedureType_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureType_2")) return false;
    string_literal(b, l + 1);
    return true;
  }

  // [paramEntries]
  private static boolean procedureType_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureType_4")) return false;
    paramEntries(b, l + 1);
    return true;
  }

  // [ARROW returnArguments]
  private static boolean procedureType_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureType_6")) return false;
    procedureType_6_0(b, l + 1);
    return true;
  }

  // ARROW returnArguments
  private static boolean procedureType_6_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureType_6_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, ARROW);
    r = r && returnArguments(b, l + 1);
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

  // STRUCT [LPAREN polymorphicParameterList RPAREN [eos* whereClause eos*]] <<enterMode "BLOCK">> tagStatement* <<exitMode "BLOCK">> structBlock
  public static boolean structType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structType")) return false;
    if (!nextTokenIsSmart(b, STRUCT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, STRUCT);
    r = r && structType_1(b, l + 1);
    r = r && enterMode(b, l + 1, "BLOCK");
    r = r && structType_3(b, l + 1);
    r = r && exitMode(b, l + 1, "BLOCK");
    r = r && structBlock(b, l + 1);
    exit_section_(b, m, STRUCT_TYPE, r);
    return r;
  }

  // [LPAREN polymorphicParameterList RPAREN [eos* whereClause eos*]]
  private static boolean structType_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structType_1")) return false;
    structType_1_0(b, l + 1);
    return true;
  }

  // LPAREN polymorphicParameterList RPAREN [eos* whereClause eos*]
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

  // [eos* whereClause eos*]
  private static boolean structType_1_0_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structType_1_0_3")) return false;
    structType_1_0_3_0(b, l + 1);
    return true;
  }

  // eos* whereClause eos*
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

  // eos*
  private static boolean structType_1_0_3_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structType_1_0_3_0_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!eos(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "structType_1_0_3_0_0", c)) break;
    }
    return true;
  }

  // eos*
  private static boolean structType_1_0_3_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structType_1_0_3_0_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!eos(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "structType_1_0_3_0_2", c)) break;
    }
    return true;
  }

  // tagStatement*
  private static boolean structType_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structType_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!tagStatement(b, l + 1)) break;
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

  // UNION [LPAREN polymorphicParameterList RPAREN [eos* whereClause eos*]] <<enterMode "BLOCK">> tagStatement* <<exitMode "BLOCK">> unionBlock
  public static boolean unionType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionType")) return false;
    if (!nextTokenIsSmart(b, UNION)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, UNION);
    r = r && unionType_1(b, l + 1);
    r = r && enterMode(b, l + 1, "BLOCK");
    r = r && unionType_3(b, l + 1);
    r = r && exitMode(b, l + 1, "BLOCK");
    r = r && unionBlock(b, l + 1);
    exit_section_(b, m, UNION_TYPE, r);
    return r;
  }

  // [LPAREN polymorphicParameterList RPAREN [eos* whereClause eos*]]
  private static boolean unionType_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionType_1")) return false;
    unionType_1_0(b, l + 1);
    return true;
  }

  // LPAREN polymorphicParameterList RPAREN [eos* whereClause eos*]
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

  // [eos* whereClause eos*]
  private static boolean unionType_1_0_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionType_1_0_3")) return false;
    unionType_1_0_3_0(b, l + 1);
    return true;
  }

  // eos* whereClause eos*
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

  // eos*
  private static boolean unionType_1_0_3_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionType_1_0_3_0_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!eos(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "unionType_1_0_3_0_0", c)) break;
    }
    return true;
  }

  // eos*
  private static boolean unionType_1_0_3_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionType_1_0_3_0_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!eos(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "unionType_1_0_3_0_2", c)) break;
    }
    return true;
  }

  // tagStatement*
  private static boolean unionType_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionType_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!tagStatement(b, l + 1)) break;
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
    r = p && type_expression(b, l, 9);
    exit_section_(b, l, m, POINTER_TYPE, r, p, null);
    return r || p;
  }

  // qualifiedNameTypeIdentifier [LPAREN expression (COMMA expression)* RPAREN]
  public static boolean typeRef(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeRef")) return false;
    if (!nextTokenIsSmart(b, IDENTIFIER_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = qualifiedNameTypeIdentifier(b, l + 1);
    r = r && typeRef_1(b, l + 1);
    exit_section_(b, m, TYPE_REF, r);
    return r;
  }

  // [LPAREN expression (COMMA expression)* RPAREN]
  private static boolean typeRef_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeRef_1")) return false;
    typeRef_1_0(b, l + 1);
    return true;
  }

  // LPAREN expression (COMMA expression)* RPAREN
  private static boolean typeRef_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeRef_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, LPAREN);
    r = r && expression(b, l + 1, -1);
    r = r && typeRef_1_0_2(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA expression)*
  private static boolean typeRef_1_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeRef_1_0_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!typeRef_1_0_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "typeRef_1_0_2", c)) break;
    }
    return true;
  }

  // COMMA expression
  private static boolean typeRef_1_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeRef_1_0_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, COMMA);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  // DOLLAR identifier
  public static boolean genericType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "genericType")) return false;
    if (!nextTokenIsSmart(b, DOLLAR)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, DOLLAR);
    r = r && identifier(b, l + 1);
    exit_section_(b, m, GENERIC_TYPE, r);
    return r;
  }

}
