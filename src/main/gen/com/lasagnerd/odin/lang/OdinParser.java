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
    b = adapt_builder_(t, b, this, null);
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

  /* ********************************************************** */
  // expression (COMMA expression)*
  public static boolean argumentList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "argumentList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ARGUMENT_LIST, "<argument list>");
    r = expression(b, l + 1);
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
    r = r && expression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // IDENTIFIER (DOT IDENTIFIER)*
  public static boolean assignable(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "assignable")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    r = r && assignable_1(b, l + 1);
    exit_section_(b, m, ASSIGNABLE, r);
    return r;
  }

  // (DOT IDENTIFIER)*
  private static boolean assignable_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "assignable_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!assignable_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "assignable_1", c)) break;
    }
    return true;
  }

  // DOT IDENTIFIER
  private static boolean assignable_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "assignable_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, DOT, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // IDENTIFIER DOUBLE_COLON literal
  public static boolean constantDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constantDeclaration")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, IDENTIFIER, DOUBLE_COLON);
    r = r && literal(b, l + 1);
    exit_section_(b, m, CONSTANT_DECLARATION, r);
    return r;
  }

  /* ********************************************************** */
  // functionCall
  //                | assignable
  //                | literal
  public static boolean expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, EXPRESSION, "<expression>");
    r = functionCall(b, l + 1);
    if (!r) r = assignable(b, l + 1);
    if (!r) r = literal(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // assignable LPAREN argumentList* RPAREN
  public static boolean functionCall(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionCall")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = assignable(b, l + 1);
    r = r && consumeToken(b, LPAREN);
    r = r && functionCall_2(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, FUNCTION_CALL, r);
    return r;
  }

  // argumentList*
  private static boolean functionCall_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionCall_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!argumentList(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "functionCall_2", c)) break;
    }
    return true;
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
  // STRING_LITERAL
  //          | INTEGER_DEC_LITERAL
  //          | INTEGER_HEX_LITERAL
  //          | INTEGER_OCT_LITERAL
  public static boolean literal(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "literal")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LITERAL, "<literal>");
    r = consumeToken(b, STRING_LITERAL);
    if (!r) r = consumeToken(b, INTEGER_DEC_LITERAL);
    if (!r) r = consumeToken(b, INTEGER_HEX_LITERAL);
    if (!r) r = consumeToken(b, INTEGER_OCT_LITERAL);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // packageDeclaration statement*
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

  // statement*
  private static boolean odinFile_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "odinFile_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!statement(b, l + 1)) break;
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
  // IDENTIFIER DOUBLE_COLON PROC LPAREN parameterList* RPAREN (ARROW returnType)?
  //                         LBRACE (statement|returnStatement)* RBRACE
  public static boolean procedureDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureDeclaration")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, IDENTIFIER, DOUBLE_COLON, PROC, LPAREN);
    r = r && procedureDeclaration_4(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    r = r && procedureDeclaration_6(b, l + 1);
    r = r && consumeToken(b, LBRACE);
    r = r && procedureDeclaration_8(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, m, PROCEDURE_DECLARATION, r);
    return r;
  }

  // parameterList*
  private static boolean procedureDeclaration_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureDeclaration_4")) return false;
    while (true) {
      int c = current_position_(b);
      if (!parameterList(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "procedureDeclaration_4", c)) break;
    }
    return true;
  }

  // (ARROW returnType)?
  private static boolean procedureDeclaration_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureDeclaration_6")) return false;
    procedureDeclaration_6_0(b, l + 1);
    return true;
  }

  // ARROW returnType
  private static boolean procedureDeclaration_6_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureDeclaration_6_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ARROW);
    r = r && returnType(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (statement|returnStatement)*
  private static boolean procedureDeclaration_8(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureDeclaration_8")) return false;
    while (true) {
      int c = current_position_(b);
      if (!procedureDeclaration_8_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "procedureDeclaration_8", c)) break;
    }
    return true;
  }

  // statement|returnStatement
  private static boolean procedureDeclaration_8_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureDeclaration_8_0")) return false;
    boolean r;
    r = statement(b, l + 1);
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
  // importDeclaration
  // | functionCall
  // | variableAssignmentDeclaration
  // | variableTypedDeclarationAssigment
  // | variableAssignment
  // | variableDeclaration
  // | constantDeclaration
  // | procedureDeclaration
  public static boolean statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statement")) return false;
    if (!nextTokenIs(b, "<statement>", IDENTIFIER, IMPORT)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, STATEMENT, "<statement>");
    r = importDeclaration(b, l + 1);
    if (!r) r = functionCall(b, l + 1);
    if (!r) r = variableAssignmentDeclaration(b, l + 1);
    if (!r) r = variableTypedDeclarationAssigment(b, l + 1);
    if (!r) r = variableAssignment(b, l + 1);
    if (!r) r = variableDeclaration(b, l + 1);
    if (!r) r = constantDeclaration(b, l + 1);
    if (!r) r = procedureDeclaration(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // assignable EQ expression
  public static boolean variableAssignment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableAssignment")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = assignable(b, l + 1);
    r = r && consumeToken(b, EQ);
    r = r && expression(b, l + 1);
    exit_section_(b, m, VARIABLE_ASSIGNMENT, r);
    return r;
  }

  /* ********************************************************** */
  // IDENTIFIER ASSIGN expression
  public static boolean variableAssignmentDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableAssignmentDeclaration")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, IDENTIFIER, ASSIGN);
    r = r && expression(b, l + 1);
    exit_section_(b, m, VARIABLE_ASSIGNMENT_DECLARATION, r);
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
  // IDENTIFIER COLON IDENTIFIER EQ expression
  public static boolean variableTypedDeclarationAssigment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableTypedDeclarationAssigment")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, IDENTIFIER, COLON, IDENTIFIER, EQ);
    r = r && expression(b, l + 1);
    exit_section_(b, m, VARIABLE_TYPED_DECLARATION_ASSIGMENT, r);
    return r;
  }

}
