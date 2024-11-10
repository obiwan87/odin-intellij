package com.lasagnerd.odin.colorSettings;

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.lasagnerd.odin.codeInsight.symbols.OdinScope;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolOrigin;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolType;
import com.lasagnerd.odin.codeInsight.symbols.OdinVisibility;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public class OdinSyntaxTextAttributes {
    public static final TextAttributesKey ODIN_KEYWORD = createTextAttributesKey("ODIN_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey ODIN_IDENTIFIER = createTextAttributesKey("ODIN_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER);

    public static final TextAttributesKey ODIN_BAD_CHARACTER = createTextAttributesKey("ODIN_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER);
    public static final TextAttributesKey ODIN_BIT_FIELD = createTextAttributesKey("ODIN_BIT_FIELD", DefaultLanguageHighlighterColors.CLASS_NAME);


    public static final TextAttributesKey ODIN_STRUCT_FIELD = createTextAttributesKey("ODIN_STRUCT_FIELD", DefaultLanguageHighlighterColors.INSTANCE_FIELD);
    public static final TextAttributesKey ODIN_BIT_FIELD_FIELD = createTextAttributesKey("ODIN_BIT_FIELD_FIELD", DefaultLanguageHighlighterColors.INSTANCE_FIELD);
    public static final TextAttributesKey ODIN_ENUM_FIELD = createTextAttributesKey("ODIN_ENUM_FIELD", DefaultLanguageHighlighterColors.CONSTANT);

    public static final TextAttributesKey ODIN_PARAMETER = createTextAttributesKey("ODIN_PARAMETER", DefaultLanguageHighlighterColors.PARAMETER);
    public static final TextAttributesKey ODIN_PARAMETER_REF = createTextAttributesKey("ODIN_PARAMETER_REF", DefaultLanguageHighlighterColors.PARAMETER);
    public static final TextAttributesKey ODIN_CONTEXT_PARAM_REF = createTextAttributesKey("ODIN_CONTEXT_PARAM_REF", DefaultLanguageHighlighterColors.PARAMETER);

    public static final TextAttributesKey ODIN_CONSTANT = createTextAttributesKey("ODIN_CONSTANT", DefaultLanguageHighlighterColors.CONSTANT);
    public static final TextAttributesKey ODIN_PROC_DECLARATION = createTextAttributesKey("ODIN_PROC_DECLARATION", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);
    public static final TextAttributesKey ODIN_VALID_STRING_ESCAPE = createTextAttributesKey("ODIN_VALID_STRING_ESCAPE", DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE);

    // Braces and operators
    public static final TextAttributesKey ODIN_BRACES = createTextAttributesKey("ODIN_BRACES", DefaultLanguageHighlighterColors.BRACES);
    public static final TextAttributesKey ODIN_BRACKETS = createTextAttributesKey("ODIN_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS);
    public static final TextAttributesKey ODIN_PARENTHESES = createTextAttributesKey("ODIN_PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES);
    public static final TextAttributesKey ODIN_DOT = createTextAttributesKey("ODIN_DOT", DefaultLanguageHighlighterColors.DOT);
    public static final TextAttributesKey ODIN_OPERATOR = createTextAttributesKey("ODIN_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey ODIN_SEMICOLON = createTextAttributesKey("ODIN_SEMICOLON", DefaultLanguageHighlighterColors.SEMICOLON);
    public static final TextAttributesKey ODIN_COMMA = createTextAttributesKey("ODIN_COMMA", DefaultLanguageHighlighterColors.COMMA);
    public static final TextAttributesKey ODIN_COLON = createTextAttributesKey("ODIN_COLON", HighlighterColors.TEXT);

    public static final TextAttributesKey ODIN_AT = createTextAttributesKey("ODIN_AT", DefaultLanguageHighlighterColors.METADATA);

    // Comments

    public static final TextAttributesKey ODIN_BLOCK_COMMENT = createTextAttributesKey("ODIN_BLOCK_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT);
    public static final TextAttributesKey ODIN_COMMENT_REFERENCE = createTextAttributesKey("ODIN_COMMENT_REFERENCE", DefaultLanguageHighlighterColors.LINE_COMMENT);
    public static final TextAttributesKey ODIN_LINE_COMMENT = createTextAttributesKey("ODIN_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
    public static final TextAttributesKey ODIN_PACKAGE = createTextAttributesKey("ODIN_PACKAGE", DefaultLanguageHighlighterColors.IDENTIFIER);

    // Declarations
    public static final TextAttributesKey ODIN_LABEL = createTextAttributesKey("ODIN_LABEL", DefaultLanguageHighlighterColors.LABEL);

    // Constants
    public static final TextAttributesKey ODIN_LOCAL_CONSTANT = createTextAttributesKey("ODIN_LOCAL_CONSTANT", DefaultLanguageHighlighterColors.CONSTANT);
    public static final TextAttributesKey ODIN_PKG_EXP_CONSTANT = createTextAttributesKey("ODIN_PKG_EXP_CONSTANT", DefaultLanguageHighlighterColors.CONSTANT);
    public static final TextAttributesKey ODIN_FILE_PRIVATE_CONSTANT = createTextAttributesKey("ODIN_FILE_PRIVATE_CONSTANT", DefaultLanguageHighlighterColors.CONSTANT);
    public static final TextAttributesKey ODIN_PKG_PRIVATE_CONSTANT = createTextAttributesKey("ODIN_PKG_PRIVATE_CONSTANT", DefaultLanguageHighlighterColors.CONSTANT);

    // Procedures

    public static final TextAttributesKey ODIN_PKG_EXP_PROC = createTextAttributesKey("ODIN_PKG_EXP_PROC", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);
    public static final TextAttributesKey ODIN_FILE_PRIVATE_PROC = createTextAttributesKey("ODIN_FILE_PRIVATE_PROC", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);
    public static final TextAttributesKey ODIN_PKG_PRIVATE_PROC = createTextAttributesKey("ODIN_PKG_PRIVATE_PROC", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);
    public static final TextAttributesKey ODIN_LOCAL_PROC = createTextAttributesKey("ODIN_LOCAL_PROC", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);
    public static final TextAttributesKey ODIN_FOREIGN_PROC = createTextAttributesKey("ODIN_FOREIGN_PROC", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);

    // Struct Tags

    // Types
    public static final TextAttributesKey ODIN_PKG_EXP_STRUCT = createTextAttributesKey("ODIN_PKG_EXP_STRUCT", DefaultLanguageHighlighterColors.CLASS_NAME);
    public static final TextAttributesKey ODIN_FILE_PRIVATE_STRUCT = createTextAttributesKey("ODIN_FILE_PRIVATE_STRUCT", DefaultLanguageHighlighterColors.CLASS_NAME);
    public static final TextAttributesKey ODIN_PKG_PRIVATE_STRUCT = createTextAttributesKey("ODIN_PKG_PRIVATE_STRUCT", DefaultLanguageHighlighterColors.CLASS_NAME);
    public static final TextAttributesKey ODIN_LOCAL_STRUCT = createTextAttributesKey("ODIN_LOCAL_STRUCT", DefaultLanguageHighlighterColors.CLASS_NAME);

    // Union
    public static final TextAttributesKey ODIN_PKG_EXP_UNION = createTextAttributesKey("ODIN_PKG_EXP_UNION", DefaultLanguageHighlighterColors.CLASS_NAME);
    public static final TextAttributesKey ODIN_FILE_PRIVATE_UNION = createTextAttributesKey("ODIN_FILE_PRIVATE_UNION", DefaultLanguageHighlighterColors.CLASS_NAME);
    public static final TextAttributesKey ODIN_PKG_PRIVATE_UNION = createTextAttributesKey("ODIN_PKG_PRIVATE_UNION", DefaultLanguageHighlighterColors.CLASS_NAME);
    public static final TextAttributesKey ODIN_LOCAL_UNION = createTextAttributesKey("ODIN_LOCAL_UNION", DefaultLanguageHighlighterColors.CLASS_NAME);

    // Enum
    public static final TextAttributesKey ODIN_PKG_EXP_ENUM = createTextAttributesKey("ODIN_PKG_EXP_ENUM", DefaultLanguageHighlighterColors.CLASS_NAME);
    public static final TextAttributesKey ODIN_FILE_PRIVATE_ENUM = createTextAttributesKey("ODIN_FILE_PRIVATE_ENUM", DefaultLanguageHighlighterColors.CLASS_NAME);
    public static final TextAttributesKey ODIN_PKG_PRIVATE_ENUM = createTextAttributesKey("ODIN_PKG_PRIVATE_ENUM", DefaultLanguageHighlighterColors.CLASS_NAME);
    public static final TextAttributesKey ODIN_LOCAL_ENUM = createTextAttributesKey("ODIN_LOCAL_ENUM", DefaultLanguageHighlighterColors.CLASS_NAME);

    // Bit field
    public static final TextAttributesKey ODIN_PKG_EXP_BIT_FIELD = createTextAttributesKey("ODIN_PKG_EXP_BIT_FIELD", DefaultLanguageHighlighterColors.CLASS_NAME);
    public static final TextAttributesKey ODIN_FILE_PRIVATE_BIT_FIELD = createTextAttributesKey("ODIN_FILE_PRIVATE_BIT_FIELD", DefaultLanguageHighlighterColors.CLASS_NAME);
    public static final TextAttributesKey ODIN_PKG_PRIVATE_BIT_FIELD = createTextAttributesKey("ODIN_PKG_PRIVATE_BIT_FIELD", DefaultLanguageHighlighterColors.CLASS_NAME);
    public static final TextAttributesKey ODIN_LOCAL_BIT_FIELD = createTextAttributesKey("ODIN_LOCAL_BIT_FIELD", DefaultLanguageHighlighterColors.CLASS_NAME);

//    public static final TextAttributesKey ODIN_PKG_EXP_TYPE_ALIAS = createTextAttributesKey("ODIN_PKG_EXP_TYPE_ALIAS", DefaultLanguageHighlighterColors.CLASS_NAME);
//    public static final TextAttributesKey ODIN_FILE_PRIVATE_TYPE_ALIAS = createTextAttributesKey("ODIN_FILE_PRIVATE_TYPE_ALIAS", DefaultLanguageHighlighterColors.CLASS_NAME);
//    public static final TextAttributesKey ODIN_PKG_PRIVATE_TYPE_ALIAS = createTextAttributesKey("ODIN_PKG_PRIVATE_TYPE_ALIAS", DefaultLanguageHighlighterColors.CLASS_NAME);
//    public static final TextAttributesKey ODIN_LOCAL_TYPE_ALIAS = createTextAttributesKey("ODIN_LOCAL_TYPE_ALIAS", DefaultLanguageHighlighterColors.CLASS_NAME);

    // Variables


    public static final TextAttributesKey ODIN_LOCAL_VARIABLE = createTextAttributesKey("ODIN_LOCAL_VARIABLE", DefaultLanguageHighlighterColors.LOCAL_VARIABLE);

    public static final TextAttributesKey ODIN_PKG_EXP_VARIABLE = createTextAttributesKey("ODIN_PKG_EXP_VARIABLE", DefaultLanguageHighlighterColors.GLOBAL_VARIABLE);
    public static final TextAttributesKey ODIN_FILE_PRIVATE_VARIABLE = createTextAttributesKey("ODIN_FILE_PRIVATE_VARIABLE", DefaultLanguageHighlighterColors.GLOBAL_VARIABLE);
    public static final TextAttributesKey ODIN_PKG_PRIVATE_VARIABLE = createTextAttributesKey("ODIN_PKG_PRIVATE_VARIABLE", DefaultLanguageHighlighterColors.GLOBAL_VARIABLE);
    public static final TextAttributesKey ODIN_VAR_REASSIGNMENT = createTextAttributesKey("ODIN_VAR_REASSIGNMENT", DefaultLanguageHighlighterColors.REASSIGNED_LOCAL_VARIABLE);
    public static final TextAttributesKey ODIN_SHADOWING_VARIABLE = createTextAttributesKey("ODIN_SHADOWING_VARIABLE", DefaultLanguageHighlighterColors.LOCAL_VARIABLE);


    public static final TextAttributesKey ODIN_STATIC_VARIABLE = createTextAttributesKey("ODIN_STATIC_VARIABLE", DefaultLanguageHighlighterColors.STATIC_FIELD);

    //////////////////////////////////////////////////
    // REFERENCES
    /////////////////////////////////////////////////

    // Fields
    public static final TextAttributesKey ODIN_STRUCT_FIELD_REF = createTextAttributesKey("ODIN_STRUCT_FIELD_REF", DefaultLanguageHighlighterColors.INSTANCE_FIELD);
    public static final TextAttributesKey ODIN_BIT_FIELD_FIELD_REF = createTextAttributesKey("ODIN_BIT_FIELD_FIELD_REF", DefaultLanguageHighlighterColors.INSTANCE_FIELD);
    public static final TextAttributesKey ODIN_ENUM_FIELD_REF = createTextAttributesKey("ODIN_ENUM_FIELD_REF", DefaultLanguageHighlighterColors.INSTANCE_FIELD);
    public static final TextAttributesKey ODIN_ALLOCATOR_FIELD_REF = createTextAttributesKey("ODIN_ALLOCATOR_FIELD_REF", DefaultLanguageHighlighterColors.INSTANCE_FIELD);
    public static final TextAttributesKey ODIN_IMPLICIT_ENUM_FIELD_REF = createTextAttributesKey("ODIN_IMPLICIT_ENUM_FIELD_REF", DefaultLanguageHighlighterColors.INSTANCE_FIELD);
    public static final TextAttributesKey ODIN_SWIZZLE_FIELD_REF = createTextAttributesKey("ODIN_SWIZZLE_FIELD_REF", DefaultLanguageHighlighterColors.INSTANCE_FIELD);

    // Fields brought into scope with using
    public static final TextAttributesKey ODIN_USING_STRUCT_FIELD_REF = createTextAttributesKey("ODIN_USING_STRUCT_FIELD_REF", DefaultLanguageHighlighterColors.INSTANCE_FIELD);
    public static final TextAttributesKey ODIN_USING_ENUM_FIELD_REF = createTextAttributesKey("ODIN_USING_ENUM_FIELD_REF", DefaultLanguageHighlighterColors.INSTANCE_FIELD);

    public static final TextAttributesKey ODIN_STATIC_VARIABLE_REF = createTextAttributesKey("ODIN_STATIC_VARIABLE_REF", DefaultLanguageHighlighterColors.STATIC_FIELD);

    // Procedure calls
    public static final TextAttributesKey ODIN_PKG_EXP_PROC_CALL = createTextAttributesKey("ODIN_PKG_EXP_PROC_CALL", DefaultLanguageHighlighterColors.FUNCTION_CALL);
    public static final TextAttributesKey ODIN_LOCAL_PROC_CALL = createTextAttributesKey("ODIN_LOCAL_PROC_CALL", ODIN_PKG_EXP_PROC_CALL);
    public static final TextAttributesKey ODIN_FOREIGN_PROC_CALL = createTextAttributesKey("ODIN_FOREIGN_PROC_CALL", ODIN_PKG_EXP_PROC_CALL);
    public static final TextAttributesKey ODIN_PKG_PRIVATE_PROC_CALL = createTextAttributesKey("ODIN_PKG_PRIVATE_PROC_CALL", ODIN_PKG_EXP_PROC_CALL);
    public static final TextAttributesKey ODIN_FILE_PRIVATE_PROC_CALL = createTextAttributesKey("ODIN_FILE_PRIVATE_PROC_CALL", ODIN_PKG_EXP_PROC_CALL);

    // Type references
    public static final TextAttributesKey ODIN_PKG_EXP_STRUCT_REF = createTextAttributesKey("ODIN_PKG_EXP_STRUCT_REF", DefaultLanguageHighlighterColors.CLASS_REFERENCE);
    public static final TextAttributesKey ODIN_PKG_PRIVATE_STRUCT_REF = createTextAttributesKey("ODIN_PKG_PRIVATE_STRUCT_REF", DefaultLanguageHighlighterColors.CLASS_REFERENCE);
    public static final TextAttributesKey ODIN_FILE_PRIVATE_STRUCT_REF = createTextAttributesKey("ODIN_FILE_PRIVATE_STRUCT_REF", DefaultLanguageHighlighterColors.CLASS_REFERENCE);
    public static final TextAttributesKey ODIN_LOCAL_STRUCT_REF = createTextAttributesKey("ODIN_LOCAL_STRUCT_REF", DefaultLanguageHighlighterColors.CLASS_REFERENCE);

    public static final TextAttributesKey ODIN_PKG_EXP_UNION_REF = createTextAttributesKey("ODIN_PKG_EXP_UNION_REF", DefaultLanguageHighlighterColors.CLASS_REFERENCE);
    public static final TextAttributesKey ODIN_PKG_PRIVATE_UNION_REF = createTextAttributesKey("ODIN_PKG_PRIVATE_UNION_REF", DefaultLanguageHighlighterColors.CLASS_REFERENCE);
    public static final TextAttributesKey ODIN_FILE_PRIVATE_UNION_REF = createTextAttributesKey("ODIN_FILE_PRIVATE_UNION_REF", DefaultLanguageHighlighterColors.CLASS_REFERENCE);
    public static final TextAttributesKey ODIN_LOCAL_UNION_REF = createTextAttributesKey("ODIN_LOCAL_UNION_REF", DefaultLanguageHighlighterColors.CLASS_REFERENCE);

    public static final TextAttributesKey ODIN_PKG_EXP_BIT_FIELD_REF = createTextAttributesKey("ODIN_PKG_EXP_BIT_FIELD_REF", DefaultLanguageHighlighterColors.CLASS_REFERENCE);
    public static final TextAttributesKey ODIN_FILE_PRIVATE_BIT_FIELD_REF = createTextAttributesKey("ODIN_FILE_PRIVATE_BIT_FIELD_REF", DefaultLanguageHighlighterColors.CLASS_REFERENCE);
    public static final TextAttributesKey ODIN_PKG_PRIVATE_BIT_FIELD_REF = createTextAttributesKey("ODIN_PKG_PRIVATE_BIT_FIELD_REF", DefaultLanguageHighlighterColors.CLASS_REFERENCE);
    public static final TextAttributesKey ODIN_LOCAL_BIT_FIELD_REF = createTextAttributesKey("ODIN_LOCAL_BIT_FIELD_REF", DefaultLanguageHighlighterColors.CLASS_REFERENCE);

    public static final TextAttributesKey ODIN_PKG_EXP_ENUM_REF = createTextAttributesKey("ODIN_PKG_EXP_ENUM_REF", DefaultLanguageHighlighterColors.CLASS_REFERENCE);
    public static final TextAttributesKey ODIN_FILE_PRIVATE_ENUM_REF = createTextAttributesKey("ODIN_FILE_PRIVATE_ENUM_REF", DefaultLanguageHighlighterColors.CLASS_REFERENCE);
    public static final TextAttributesKey ODIN_PKG_PRIVATE_ENUM_REF = createTextAttributesKey("ODIN_PKG_PRIVATE_ENUM_REF", DefaultLanguageHighlighterColors.CLASS_REFERENCE);
    public static final TextAttributesKey ODIN_LOCAL_ENUM_REF = createTextAttributesKey("ODIN_LOCAL_ENUM_REF", DefaultLanguageHighlighterColors.CLASS_REFERENCE);

    // Built-In
    public static final TextAttributesKey ODIN_BUILTIN_PROC = createTextAttributesKey("ODIN_BUILTIN_PROC", DefaultLanguageHighlighterColors.PREDEFINED_SYMBOL);
    public static final TextAttributesKey ODIN_BUILTIN_PROC_CALL = createTextAttributesKey("ODIN_BUILTIN_PROC_CALL", ODIN_BUILTIN_PROC);
    public static final TextAttributesKey ODIN_BUILTIN_VAR = createTextAttributesKey("ODIN_BUILTIN_VAR", DefaultLanguageHighlighterColors.GLOBAL_VARIABLE);
    public static final TextAttributesKey ODIN_BUILTIN_CONSTANT = createTextAttributesKey("ODIN_BUILTIN_CONSTANT", DefaultLanguageHighlighterColors.CONSTANT);
    public static final TextAttributesKey ODIN_BUILTIN_TYPE = createTextAttributesKey("ODIN_BUILTIN_TYPE", DefaultLanguageHighlighterColors.PREDEFINED_SYMBOL);

    // TODO Maybe implement
//    public static final TextAttributesKey ODIN_PKG_EXP_TYPE_ALIAS_REF = createTextAttributesKey("ODIN_PKG_EXP_TYPE_ALIAS_REF", DefaultLanguageHighlighterColors.CLASS_REFERENCE);
//    public static final TextAttributesKey ODIN_FILE_PRIVATE_TYPE_ALIAS_REF = createTextAttributesKey("ODIN_FILE_PRIVATE_TYPE_ALIAS_REF", DefaultLanguageHighlighterColors.CLASS_REFERENCE);
//    public static final TextAttributesKey ODIN_PKG_PRIVATE_TYPE_ALIAS_REF = createTextAttributesKey("ODIN_PKG_PRIVATE_TYPE_ALIAS_REF", DefaultLanguageHighlighterColors.CLASS_REFERENCE);
//    public static final TextAttributesKey ODIN_LOCAL_TYPE_ALIAS_REF = createTextAttributesKey("ODIN_LOCAL_TYPE_ALIAS_REF", DefaultLanguageHighlighterColors.CLASS_REFERENCE);

    public static final TextAttributesKey ODIN_VAR_CALL = createTextAttributesKey("ODIN_VAR_CALL", DefaultLanguageHighlighterColors.LOCAL_VARIABLE);
    public static final TextAttributesKey ODIN_LOCAL_VAR_CALL = createTextAttributesKey("ODIN_LOCAL_VAR_CALL", DefaultLanguageHighlighterColors.LOCAL_VARIABLE);
    public static final TextAttributesKey ODIN_PKG_EXP_VAR_CALL = createTextAttributesKey("ODIN_PKG_EXP_VAR_CALL", DefaultLanguageHighlighterColors.LOCAL_VARIABLE);
    public static final TextAttributesKey ODIN_PKG_PRIVATE_VAR_CALL = createTextAttributesKey("ODIN_PKG_PRIVATE_VAR_CALL", DefaultLanguageHighlighterColors.LOCAL_VARIABLE);
    public static final TextAttributesKey ODIN_FILE_PRIVATE_VAR_CALL = createTextAttributesKey("ODIN_FILE_PRIVATE_VAR_CALL", DefaultLanguageHighlighterColors.LOCAL_VARIABLE);
    public static final TextAttributesKey ODIN_STRUCT_FIELD_CALL = createTextAttributesKey("ODIN_STRUCT_FIELD_CALL", DefaultLanguageHighlighterColors.INSTANCE_FIELD);
    public static final TextAttributesKey ODIN_PARAMETER_CALL = createTextAttributesKey("ODIN_PARAMETER_CALL", DefaultLanguageHighlighterColors.PARAMETER);

    // TODO
//    public static final TextAttributesKey ODIN_FAKE_METHOD_CALL = createTextAttributesKey("ODIN_FAKE_METHOD_CALL", DefaultLanguageHighlighterColors.FUNCTION_CALL);

    // Variable
    public static final TextAttributesKey ODIN_PKG_EXP_VARIABLE_REF = createTextAttributesKey("ODIN_PKG_EXP_VARIABLE_REF", ODIN_PKG_EXP_VARIABLE);
    public static final TextAttributesKey ODIN_FILE_PRIVATE_VARIABLE_REF = createTextAttributesKey("ODIN_FILE_PRIVATE_VARIABLE_REF", ODIN_FILE_PRIVATE_VARIABLE);
    public static final TextAttributesKey ODIN_PKG_PRIVATE_VARIABLE_REF = createTextAttributesKey("ODIN_PKG_PRIVATE_VARIABLE_REF", ODIN_PKG_PRIVATE_VARIABLE);
    public static final TextAttributesKey ODIN_LOCAL_VARIABLE_REF = createTextAttributesKey("ODIN_LOCAL_VARIABLE_REF", ODIN_LOCAL_VARIABLE);

    // TODO
    public static final TextAttributesKey ODIN_VAR_REASSIGNMENT_REF = createTextAttributesKey("ODIN_VAR_REASSIGNMENT_REF", DefaultLanguageHighlighterColors.REASSIGNED_LOCAL_VARIABLE);
    public static final TextAttributesKey ODIN_SHADOWING_VARIABLE_REF = createTextAttributesKey("ODIN_SHADOWING_VARIABLE_REF", DefaultLanguageHighlighterColors.LOCAL_VARIABLE);

    // Attributes
    public static final TextAttributesKey ODIN_ATTRIBUTE_REF = createTextAttributesKey("ODIN_ATTRIBUTE_REF", DefaultLanguageHighlighterColors.IDENTIFIER);

    // Directives
    public static final TextAttributesKey ODIN_DIRECTIVE_REF = createTextAttributesKey("ODIN_DIRECTIVE_REF", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);

    // Constant
    public static final TextAttributesKey ODIN_PKG_EXP_CONSTANT_REF = createTextAttributesKey("ODIN_PKG_EXP_CONSTANT_REF", ODIN_PKG_EXP_CONSTANT);
    public static final TextAttributesKey ODIN_FILE_PRIVATE_CONSTANT_REF = createTextAttributesKey("ODIN_FILE_PRIVATE_CONSTANT_REF", ODIN_FILE_PRIVATE_CONSTANT);
    public static final TextAttributesKey ODIN_PKG_PRIVATE_CONSTANT_REF = createTextAttributesKey("ODIN_PKG_PRIVATE_CONSTANT_REF", ODIN_PKG_PRIVATE_CONSTANT);
    public static final TextAttributesKey ODIN_LOCAL_CONSTANT_REF = createTextAttributesKey("ODIN_LOCAL_CONSTANT_REF", ODIN_LOCAL_CONSTANT);

    public static final TextAttributesKey ODIN_POLY_PARAMETER = createTextAttributesKey("ODIN_POLY_PARAMETER", DefaultLanguageHighlighterColors.PARAMETER);
    public static final TextAttributesKey ODIN_POLY_PARAMETER_REF = createTextAttributesKey("ODIN_POLY_PARAMETER_REF", DefaultLanguageHighlighterColors.IDENTIFIER);
    // String
    public static final TextAttributesKey ODIN_INVALID_ESCAPE = createTextAttributesKey("ODIN_INVALID_ESCAPE", DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE);
    public static final TextAttributesKey ODIN_TEXT = createTextAttributesKey("ODIN_TEXT", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey ODIN_VALID_ESCAPE = createTextAttributesKey("ODIN_VALID_ESCAPE", DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE);
    public static final TextAttributesKey ODIN_NUMBER = createTextAttributesKey("ODIN_NUMBER", DefaultLanguageHighlighterColors.NUMBER);


    public static final OdinSymbolTextAttributes TEXT_ATTRIBUTES_MAP = new OdinSymbolTextAttributes();


    static {
        // Struct declarations
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.STRUCT, OdinScope.GLOBAL, OdinVisibility.PACKAGE_EXPORTED, OdinIdentifierType.DECLARATION, ODIN_PKG_EXP_STRUCT);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.STRUCT, OdinScope.GLOBAL, OdinVisibility.PACKAGE_PRIVATE, OdinIdentifierType.DECLARATION, ODIN_PKG_PRIVATE_STRUCT);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.STRUCT, OdinScope.GLOBAL, OdinVisibility.FILE_PRIVATE, OdinIdentifierType.DECLARATION, ODIN_FILE_PRIVATE_STRUCT);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.STRUCT, OdinScope.LOCAL, OdinVisibility.NONE, OdinIdentifierType.DECLARATION, ODIN_LOCAL_STRUCT);

        // Union declarations
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.UNION, OdinScope.GLOBAL, OdinVisibility.PACKAGE_EXPORTED, OdinIdentifierType.DECLARATION, ODIN_PKG_EXP_UNION);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.UNION, OdinScope.GLOBAL, OdinVisibility.PACKAGE_PRIVATE, OdinIdentifierType.DECLARATION, ODIN_PKG_PRIVATE_UNION);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.UNION, OdinScope.GLOBAL, OdinVisibility.FILE_PRIVATE, OdinIdentifierType.DECLARATION, ODIN_FILE_PRIVATE_UNION);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.UNION, OdinScope.LOCAL, OdinVisibility.NONE, OdinIdentifierType.DECLARATION, ODIN_LOCAL_UNION);

        // Enum declarations
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.ENUM, OdinScope.GLOBAL, OdinVisibility.PACKAGE_EXPORTED, OdinIdentifierType.DECLARATION, ODIN_PKG_EXP_ENUM);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.ENUM, OdinScope.GLOBAL, OdinVisibility.PACKAGE_PRIVATE, OdinIdentifierType.DECLARATION, ODIN_PKG_PRIVATE_ENUM);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.ENUM, OdinScope.GLOBAL, OdinVisibility.FILE_PRIVATE, OdinIdentifierType.DECLARATION, ODIN_FILE_PRIVATE_ENUM);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.ENUM, OdinScope.LOCAL, OdinVisibility.NONE, OdinIdentifierType.DECLARATION, ODIN_LOCAL_ENUM);

        // Bit Field declarations
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.BIT_FIELD, OdinScope.GLOBAL, OdinVisibility.PACKAGE_EXPORTED, OdinIdentifierType.DECLARATION, ODIN_PKG_EXP_BIT_FIELD);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.BIT_FIELD, OdinScope.GLOBAL, OdinVisibility.PACKAGE_PRIVATE, OdinIdentifierType.DECLARATION, ODIN_PKG_PRIVATE_BIT_FIELD);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.BIT_FIELD, OdinScope.GLOBAL, OdinVisibility.FILE_PRIVATE, OdinIdentifierType.DECLARATION, ODIN_FILE_PRIVATE_BIT_FIELD);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.BIT_FIELD, OdinScope.LOCAL, OdinVisibility.NONE, OdinIdentifierType.DECLARATION, ODIN_LOCAL_BIT_FIELD);

        // Procedures
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.PROCEDURE, OdinScope.GLOBAL, OdinVisibility.PACKAGE_EXPORTED, OdinIdentifierType.DECLARATION, ODIN_PKG_EXP_PROC);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.PROCEDURE, OdinScope.GLOBAL, OdinVisibility.PACKAGE_PRIVATE, OdinIdentifierType.DECLARATION, ODIN_PKG_PRIVATE_PROC);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.PROCEDURE, OdinScope.GLOBAL, OdinVisibility.FILE_PRIVATE, OdinIdentifierType.DECLARATION, ODIN_FILE_PRIVATE_PROC);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.PROCEDURE, OdinScope.LOCAL, OdinVisibility.NONE, OdinIdentifierType.DECLARATION, ODIN_LOCAL_PROC);

        // TODO Check if foreign procedure can have visibility and local scope
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.PROCEDURE, OdinScope.LOCAL, OdinVisibility.NONE, OdinIdentifierType.DECLARATION, OdinSymbolOrigin.FOREIGN, ODIN_FOREIGN_PROC);

        // Variable
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.VARIABLE, OdinScope.GLOBAL, OdinVisibility.PACKAGE_EXPORTED, OdinIdentifierType.DECLARATION, ODIN_PKG_EXP_VARIABLE);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.VARIABLE, OdinScope.GLOBAL, OdinVisibility.PACKAGE_PRIVATE, OdinIdentifierType.DECLARATION, ODIN_PKG_PRIVATE_VARIABLE);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.VARIABLE, OdinScope.GLOBAL, OdinVisibility.FILE_PRIVATE, OdinIdentifierType.DECLARATION, ODIN_FILE_PRIVATE_VARIABLE);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.VARIABLE, OdinScope.LOCAL, OdinVisibility.NONE, OdinIdentifierType.DECLARATION, ODIN_LOCAL_VARIABLE);

        // Constant
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.CONSTANT, OdinScope.GLOBAL, OdinVisibility.PACKAGE_EXPORTED, OdinIdentifierType.DECLARATION, ODIN_PKG_EXP_CONSTANT);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.CONSTANT, OdinScope.GLOBAL, OdinVisibility.PACKAGE_PRIVATE, OdinIdentifierType.DECLARATION, ODIN_PKG_PRIVATE_CONSTANT);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.CONSTANT, OdinScope.GLOBAL, OdinVisibility.FILE_PRIVATE, OdinIdentifierType.DECLARATION, ODIN_FILE_PRIVATE_CONSTANT);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.CONSTANT, OdinScope.LOCAL, OdinVisibility.NONE, OdinIdentifierType.DECLARATION, ODIN_LOCAL_CONSTANT);

        // Type Alias
//        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.TYPE_ALIAS, OdinScope.GLOBAL, OdinVisibility.PACKAGE_EXPORTED, OdinIdentifierType.DECLARATION, ODIN_PKG_EXP_TYPE_ALIAS);
//        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.TYPE_ALIAS, OdinScope.GLOBAL, OdinVisibility.PACKAGE_PRIVATE, OdinIdentifierType.DECLARATION, ODIN_PKG_PRIVATE_TYPE_ALIAS);
//        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.TYPE_ALIAS, OdinScope.GLOBAL, OdinVisibility.FILE_PRIVATE, OdinIdentifierType.DECLARATION, ODIN_FILE_PRIVATE_TYPE_ALIAS);
//        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.TYPE_ALIAS, OdinScope.LOCAL, OdinVisibility.NONE, OdinIdentifierType.DECLARATION, ODIN_LOCAL_TYPE_ALIAS);


        // Fields
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.ENUM_FIELD, OdinScope.TYPE, OdinVisibility.NONE, OdinIdentifierType.DECLARATION, ODIN_ENUM_FIELD);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.STRUCT_FIELD, OdinScope.TYPE, OdinVisibility.NONE, OdinIdentifierType.DECLARATION, ODIN_STRUCT_FIELD);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.BIT_FIELD_FIELD, OdinScope.TYPE, OdinVisibility.NONE, OdinIdentifierType.DECLARATION, ODIN_BIT_FIELD_FIELD);

        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.POLYMORPHIC_TYPE, OdinScope.LOCAL, OdinVisibility.NONE, OdinIdentifierType.DECLARATION, ODIN_POLY_PARAMETER);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.PARAMETER, OdinScope.LOCAL, OdinVisibility.NONE, OdinIdentifierType.DECLARATION, ODIN_PARAMETER);

        //////////////////////////////////////////////////
        // REFERENCES
        /////////////////////////////////////////////////

        // Fields
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.STRUCT_FIELD, OdinScope.TYPE, OdinVisibility.NONE, OdinIdentifierType.REFERENCE, ODIN_STRUCT_FIELD_REF);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.BIT_FIELD_FIELD, OdinScope.TYPE, OdinVisibility.NONE, OdinIdentifierType.REFERENCE, ODIN_BIT_FIELD_FIELD_REF);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.ENUM_FIELD, OdinScope.TYPE, OdinVisibility.NONE, OdinIdentifierType.REFERENCE, ODIN_ENUM_FIELD_REF);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.ALLOCATOR_FIELD, OdinScope.TYPE, OdinVisibility.NONE, OdinIdentifierType.REFERENCE, ODIN_ALLOCATOR_FIELD_REF);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.SWIZZLE_FIELD, OdinScope.TYPE, OdinVisibility.NONE, OdinIdentifierType.REFERENCE, ODIN_SWIZZLE_FIELD_REF);

        // Fields brought into scope with using
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.STRUCT_FIELD, OdinScope.TYPE, OdinVisibility.NONE, OdinIdentifierType.REFERENCE, OdinSymbolOrigin.USING, ODIN_USING_STRUCT_FIELD_REF);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.ENUM_FIELD, OdinScope.TYPE, OdinVisibility.NONE, OdinIdentifierType.REFERENCE, OdinSymbolOrigin.USING, ODIN_USING_ENUM_FIELD_REF);

        // Struct
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.STRUCT, OdinScope.GLOBAL, OdinVisibility.PACKAGE_EXPORTED, OdinIdentifierType.REFERENCE, ODIN_PKG_EXP_STRUCT_REF);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.STRUCT, OdinScope.GLOBAL, OdinVisibility.PACKAGE_PRIVATE, OdinIdentifierType.REFERENCE, ODIN_PKG_PRIVATE_STRUCT_REF);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.STRUCT, OdinScope.GLOBAL, OdinVisibility.FILE_PRIVATE, OdinIdentifierType.REFERENCE, ODIN_FILE_PRIVATE_STRUCT_REF);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.STRUCT, OdinScope.LOCAL, OdinVisibility.NONE, OdinIdentifierType.REFERENCE, ODIN_LOCAL_STRUCT_REF);

        // Enum
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.ENUM, OdinScope.GLOBAL, OdinVisibility.PACKAGE_EXPORTED, OdinIdentifierType.REFERENCE, ODIN_PKG_EXP_ENUM_REF);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.ENUM, OdinScope.GLOBAL, OdinVisibility.PACKAGE_PRIVATE, OdinIdentifierType.REFERENCE, ODIN_PKG_PRIVATE_ENUM_REF);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.ENUM, OdinScope.GLOBAL, OdinVisibility.FILE_PRIVATE, OdinIdentifierType.REFERENCE, ODIN_FILE_PRIVATE_ENUM_REF);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.ENUM, OdinScope.LOCAL, OdinVisibility.NONE, OdinIdentifierType.REFERENCE, ODIN_LOCAL_ENUM_REF);

        // Union
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.UNION, OdinScope.GLOBAL, OdinVisibility.PACKAGE_EXPORTED, OdinIdentifierType.REFERENCE, ODIN_PKG_EXP_UNION_REF);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.UNION, OdinScope.GLOBAL, OdinVisibility.PACKAGE_PRIVATE, OdinIdentifierType.REFERENCE, ODIN_PKG_PRIVATE_UNION_REF);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.UNION, OdinScope.GLOBAL, OdinVisibility.FILE_PRIVATE, OdinIdentifierType.REFERENCE, ODIN_FILE_PRIVATE_UNION_REF);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.UNION, OdinScope.LOCAL, OdinVisibility.NONE, OdinIdentifierType.REFERENCE, ODIN_LOCAL_UNION_REF);

        // Bit Field references
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.BIT_FIELD, OdinScope.GLOBAL, OdinVisibility.PACKAGE_EXPORTED, OdinIdentifierType.REFERENCE, ODIN_PKG_EXP_BIT_FIELD_REF);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.BIT_FIELD, OdinScope.GLOBAL, OdinVisibility.PACKAGE_PRIVATE, OdinIdentifierType.REFERENCE, ODIN_PKG_PRIVATE_BIT_FIELD_REF);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.BIT_FIELD, OdinScope.GLOBAL, OdinVisibility.FILE_PRIVATE, OdinIdentifierType.REFERENCE, ODIN_FILE_PRIVATE_BIT_FIELD_REF);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.BIT_FIELD, OdinScope.LOCAL, OdinVisibility.NONE, OdinIdentifierType.REFERENCE, ODIN_LOCAL_BIT_FIELD_REF);

        // Procedures
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.PROCEDURE, OdinScope.GLOBAL, OdinVisibility.PACKAGE_EXPORTED, OdinIdentifierType.REFERENCE, ODIN_PKG_EXP_PROC_CALL);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.PROCEDURE, OdinScope.GLOBAL, OdinVisibility.PACKAGE_PRIVATE, OdinIdentifierType.REFERENCE, ODIN_PKG_PRIVATE_PROC_CALL);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.PROCEDURE, OdinScope.GLOBAL, OdinVisibility.FILE_PRIVATE, OdinIdentifierType.REFERENCE, ODIN_FILE_PRIVATE_PROC_CALL);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.PROCEDURE, OdinScope.LOCAL, OdinVisibility.NONE, OdinIdentifierType.REFERENCE, ODIN_LOCAL_PROC_CALL);

        // TODO Check if foreign procedure can have visibility and local scope
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.PROCEDURE, OdinScope.LOCAL, OdinVisibility.NONE, OdinIdentifierType.REFERENCE, OdinSymbolOrigin.FOREIGN, ODIN_FOREIGN_PROC_CALL);

        // Variable
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.VARIABLE, OdinScope.GLOBAL, OdinVisibility.PACKAGE_EXPORTED, OdinIdentifierType.REFERENCE, ODIN_PKG_EXP_VARIABLE_REF);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.VARIABLE, OdinScope.GLOBAL, OdinVisibility.PACKAGE_PRIVATE, OdinIdentifierType.REFERENCE, ODIN_PKG_PRIVATE_VARIABLE_REF);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.VARIABLE, OdinScope.GLOBAL, OdinVisibility.FILE_PRIVATE, OdinIdentifierType.REFERENCE, ODIN_FILE_PRIVATE_VARIABLE_REF);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.VARIABLE, OdinScope.LOCAL, OdinVisibility.NONE, OdinIdentifierType.REFERENCE, ODIN_LOCAL_VARIABLE_REF);

        // Constant
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.CONSTANT, OdinScope.GLOBAL, OdinVisibility.PACKAGE_EXPORTED, OdinIdentifierType.REFERENCE, ODIN_PKG_EXP_CONSTANT_REF);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.CONSTANT, OdinScope.GLOBAL, OdinVisibility.PACKAGE_PRIVATE, OdinIdentifierType.REFERENCE, ODIN_PKG_PRIVATE_CONSTANT_REF);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.CONSTANT, OdinScope.GLOBAL, OdinVisibility.FILE_PRIVATE, OdinIdentifierType.REFERENCE, ODIN_FILE_PRIVATE_CONSTANT_REF);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.CONSTANT, OdinScope.LOCAL, OdinVisibility.NONE, OdinIdentifierType.REFERENCE, ODIN_LOCAL_CONSTANT_REF);

        // Type Alias
//        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.TYPE_ALIAS, OdinScope.GLOBAL, OdinVisibility.PACKAGE_EXPORTED, OdinIdentifierType.REFERENCE, ODIN_PKG_EXP_TYPE_ALIAS);
//        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.TYPE_ALIAS, OdinScope.GLOBAL, OdinVisibility.PACKAGE_PRIVATE, OdinIdentifierType.REFERENCE, ODIN_PKG_PRIVATE_TYPE_ALIAS);
//        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.TYPE_ALIAS, OdinScope.GLOBAL, OdinVisibility.FILE_PRIVATE, OdinIdentifierType.REFERENCE, ODIN_FILE_PRIVATE_TYPE_ALIAS);
//        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.TYPE_ALIAS, OdinScope.LOCAL, OdinVisibility.NONE, OdinIdentifierType.REFERENCE, ODIN_LOCAL_TYPE_ALIAS);


        // Variable
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.VARIABLE, OdinScope.LOCAL, OdinVisibility.NONE, OdinIdentifierType.CALL, ODIN_LOCAL_VAR_CALL);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.VARIABLE, OdinScope.GLOBAL, OdinVisibility.PACKAGE_EXPORTED, OdinIdentifierType.CALL, ODIN_PKG_EXP_VAR_CALL);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.VARIABLE, OdinScope.GLOBAL, OdinVisibility.PACKAGE_PRIVATE, OdinIdentifierType.CALL, ODIN_PKG_PRIVATE_VAR_CALL);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.VARIABLE, OdinScope.GLOBAL, OdinVisibility.FILE_PRIVATE, OdinIdentifierType.CALL, ODIN_FILE_PRIVATE_VAR_CALL);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.STRUCT_FIELD, OdinScope.TYPE, OdinVisibility.NONE, OdinIdentifierType.CALL, ODIN_STRUCT_FIELD_CALL);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.PARAMETER, OdinScope.LOCAL, OdinVisibility.NONE, OdinIdentifierType.CALL, ODIN_PARAMETER_CALL);

        // Constant
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.PROCEDURE, OdinScope.LOCAL, OdinVisibility.NONE, OdinIdentifierType.CALL, ODIN_LOCAL_PROC_CALL);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.PROCEDURE, OdinScope.GLOBAL, OdinVisibility.PACKAGE_EXPORTED, OdinIdentifierType.CALL, ODIN_PKG_EXP_PROC_CALL);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.PROCEDURE, OdinScope.GLOBAL, OdinVisibility.PACKAGE_PRIVATE, OdinIdentifierType.CALL, ODIN_PKG_PRIVATE_PROC_CALL);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.PROCEDURE, OdinScope.GLOBAL, OdinVisibility.FILE_PRIVATE, OdinIdentifierType.CALL, ODIN_FILE_PRIVATE_PROC_CALL);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.PROCEDURE, OdinScope.TYPE, OdinVisibility.NONE, OdinIdentifierType.CALL, ODIN_STRUCT_FIELD_CALL);

//      TODO
//        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.VARIABLE, OdinScope.TYPE, OdinVisibility.NONE, OdinIdentifierType.CALL, ODIN_FAKE_METHOD_CALL);
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.PARAMETER, OdinScope.LOCAL, OdinVisibility.NONE, OdinIdentifierType.REFERENCE, ODIN_PARAMETER_REF);

        // Poly
        TEXT_ATTRIBUTES_MAP.addTextAttribute(OdinSymbolType.POLYMORPHIC_TYPE, OdinScope.LOCAL, OdinVisibility.NONE, OdinIdentifierType.REFERENCE, ODIN_POLY_PARAMETER_REF);
    }
}

