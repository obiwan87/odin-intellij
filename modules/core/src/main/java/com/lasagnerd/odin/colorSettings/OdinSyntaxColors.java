package com.lasagnerd.odin.colorSettings;

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.lasagnerd.odin.codeInsight.symbols.OdinScope;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolType;
import com.lasagnerd.odin.codeInsight.symbols.OdinVisibility;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public class OdinSyntaxColors {
    public static final TextAttributesKey ODIN_PROCEDURE_TYPE = createTextAttributesKey("ODIN_PROCEDURE_TYPE", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);
    public static final TextAttributesKey ODIN_KEYWORD = createTextAttributesKey("ODIN_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey ODIN_IDENTIFIER = createTextAttributesKey("ODIN_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER);

    public static final TextAttributesKey ODIN_STRUCT_TYPE = createTextAttributesKey("ODIN_STRUCT_TYPE", DefaultLanguageHighlighterColors.CLASS_NAME);
    public static final TextAttributesKey ODIN_STRUCT_REF = createTextAttributesKey("ODIN_STRUCT_REF", DefaultLanguageHighlighterColors.CLASS_REFERENCE);
    public static final TextAttributesKey ODIN_UNION_TYPE = createTextAttributesKey("ODIN_UNION_TYPE", DefaultLanguageHighlighterColors.CLASS_NAME);
    public static final TextAttributesKey ODIN_UNION_REF = createTextAttributesKey("ODIN_UNION_REF", DefaultLanguageHighlighterColors.CLASS_REFERENCE);
    public static final TextAttributesKey ODIN_BAD_CHARACTER = createTextAttributesKey("ODIN_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER);
    public static final TextAttributesKey ODIN_BIT_FIELD = createTextAttributesKey("ODIN_BIT_FIELD", DefaultLanguageHighlighterColors.CLASS_NAME);

    public static final TextAttributesKey ODIN_STRUCT_FIELD = createTextAttributesKey("ODIN_STRUCT_FIELD", DefaultLanguageHighlighterColors.INSTANCE_FIELD);
    public static final TextAttributesKey ODIN_BIT_FIELD_FIELD = createTextAttributesKey("ODIN_BIT_FIELD_FIELD", DefaultLanguageHighlighterColors.INSTANCE_FIELD);
    public static final TextAttributesKey ODIN_ENUM_FIELD = createTextAttributesKey("ODIN_ENUM_FIELD", DefaultLanguageHighlighterColors.CONSTANT);

    public static final TextAttributesKey ODIN_PARAMETER = createTextAttributesKey("ODIN_PARAMETER", DefaultLanguageHighlighterColors.PARAMETER);
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

    // Comments
    public static final TextAttributesKey ODIN_BLOCK_COMMENT = createTextAttributesKey("ODIN_BLOCK_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT);
    public static final TextAttributesKey ODIN_COMMENT_REFERENCE = createTextAttributesKey("ODIN_COMMENT_REFERENCE", DefaultLanguageHighlighterColors.LINE_COMMENT);
    public static final TextAttributesKey ODIN_LINE_COMMENT = createTextAttributesKey("ODIN_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
    public static final TextAttributesKey ODIN_PACKAGE = createTextAttributesKey("ODIN_PACKAGE", DefaultLanguageHighlighterColors.IDENTIFIER);

    // Declarations
    public static final TextAttributesKey ODIN_PROC_PARAMETER = createTextAttributesKey("ODIN_PROC_PARAMETER", DefaultLanguageHighlighterColors.PARAMETER);
    public static final TextAttributesKey ODIN_LABEL = createTextAttributesKey("ODIN_LABEL", DefaultLanguageHighlighterColors.LABEL);

    // Constants
    public static final TextAttributesKey ODIN_LOCAL_CONSTANT = createTextAttributesKey("ODIN_LOCAL_CONSTANT", DefaultLanguageHighlighterColors.CONSTANT);
    public static final TextAttributesKey ODIN_PKG_EXP_CONSTANT = createTextAttributesKey("ODIN_PKG_EXP_CONSTANT", DefaultLanguageHighlighterColors.CONSTANT);
    public static final TextAttributesKey ODIN_FILE_PRIVATE_CONSTANT = createTextAttributesKey("ODIN_FILE_PRIVATE_CONSTANT", DefaultLanguageHighlighterColors.CONSTANT);
    public static final TextAttributesKey ODIN_PKG_PRIVATE_CONSTANT = createTextAttributesKey("ODIN_PKG_PRIVATE_CONSTANT", DefaultLanguageHighlighterColors.CONSTANT);

    // Procedures
    public static final TextAttributesKey ODIN_BUILTIN_PROC = createTextAttributesKey("ODIN_BUILTIN_PROC", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);
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

    public static final TextAttributesKey ODIN_PKG_EXP_UNION = createTextAttributesKey("ODIN_PKG_EXP_UNION", DefaultLanguageHighlighterColors.CLASS_NAME);
    public static final TextAttributesKey ODIN_FILE_PRIVATE_UNION = createTextAttributesKey("ODIN_FILE_PRIVATE_UNION", DefaultLanguageHighlighterColors.CLASS_NAME);
    public static final TextAttributesKey ODIN_PKG_PRIVATE_UNION = createTextAttributesKey("ODIN_PKG_PRIVATE_UNION", DefaultLanguageHighlighterColors.CLASS_NAME);
    public static final TextAttributesKey ODIN_LOCAL_UNION = createTextAttributesKey("ODIN_LOCAL_UNION", DefaultLanguageHighlighterColors.CLASS_NAME);

    public static final TextAttributesKey ODIN_PKG_EXP_BIT_FIELD = createTextAttributesKey("ODIN_PKG_EXP_BIT_FIELD", DefaultLanguageHighlighterColors.CLASS_NAME);
    public static final TextAttributesKey ODIN_FILE_PRIVATE_BIT_FIELD = createTextAttributesKey("ODIN_FILE_PRIVATE_BIT_FIELD", DefaultLanguageHighlighterColors.CLASS_NAME);
    public static final TextAttributesKey ODIN_PKG_PRIVATE_BIT_FIELD = createTextAttributesKey("ODIN_PKG_PRIVATE_BIT_FIELD", DefaultLanguageHighlighterColors.CLASS_NAME);
    public static final TextAttributesKey ODIN_LOCAL_BIT_FIELD = createTextAttributesKey("ODIN_LOCAL_BIT_FIELD", DefaultLanguageHighlighterColors.CLASS_NAME);

    public static final TextAttributesKey ODIN_PKG_EXP_TYPE_ALIAS = createTextAttributesKey("ODIN_PKG_EXP_TYPE_ALIAS", DefaultLanguageHighlighterColors.CLASS_NAME);
    public static final TextAttributesKey ODIN_FILE_PRIVATE_TYPE_ALIAS = createTextAttributesKey("ODIN_FILE_PRIVATE_TYPE_ALIAS", DefaultLanguageHighlighterColors.CLASS_NAME);
    public static final TextAttributesKey ODIN_PKG_PRIVATE_TYPE_ALIAS = createTextAttributesKey("ODIN_PKG_PRIVATE_TYPE_ALIAS", DefaultLanguageHighlighterColors.CLASS_NAME);
    public static final TextAttributesKey ODIN_LOCAL_TYPE_ALIAS = createTextAttributesKey("ODIN_LOCAL_TYPE_ALIAS", DefaultLanguageHighlighterColors.CLASS_NAME);

    // Variables and Fields
    public static final TextAttributesKey ODIN_BUILTIN_VAR = createTextAttributesKey("ODIN_BUILTIN_VAR", DefaultLanguageHighlighterColors.GLOBAL_VARIABLE);
    public static final TextAttributesKey ODIN_LOCAL_VARIABLE = createTextAttributesKey("ODIN_LOCAL_VARIABLE", DefaultLanguageHighlighterColors.LOCAL_VARIABLE);

    public static final TextAttributesKey ODIN_GLOBAL_VARIABLE = createTextAttributesKey("ODIN_GLOBAL_VARIABLE", DefaultLanguageHighlighterColors.GLOBAL_VARIABLE);
    public static final TextAttributesKey ODIN_PKG_EXP_VARIABLE = createTextAttributesKey("ODIN_PKG_EXP_VARIABLE", DefaultLanguageHighlighterColors.GLOBAL_VARIABLE);
    public static final TextAttributesKey ODIN_FILE_PRIVATE_VARIABLE = createTextAttributesKey("ODIN_FILE_PRIVATE_VARIABLE", DefaultLanguageHighlighterColors.GLOBAL_VARIABLE);
    public static final TextAttributesKey ODIN_PKG_PRIVATE_VARIABLE = createTextAttributesKey("ODIN_PKG_PRIVATE_VARIABLE", DefaultLanguageHighlighterColors.GLOBAL_VARIABLE);
    public static final TextAttributesKey ODIN_VAR_REASSIGNMENT = createTextAttributesKey("ODIN_VAR_REASSIGNMENT", DefaultLanguageHighlighterColors.REASSIGNED_LOCAL_VARIABLE);
    public static final TextAttributesKey ODIN_SHADOWING_VARIABLE = createTextAttributesKey("ODIN_SHADOWING_VARIABLE", DefaultLanguageHighlighterColors.LOCAL_VARIABLE);
    public static final TextAttributesKey ODIN_FIELD = createTextAttributesKey("ODIN_FIELD", DefaultLanguageHighlighterColors.INSTANCE_FIELD);

    public static final TextAttributesKey ODIN_USING_STRUCT_FIELD = createTextAttributesKey("ODIN_USING_STRUCT_FIELD", DefaultLanguageHighlighterColors.INSTANCE_FIELD);
    public static final TextAttributesKey ODIN_USING_ENUM_FIELD = createTextAttributesKey("ODIN_USING_ENUM_FIELD", DefaultLanguageHighlighterColors.INSTANCE_FIELD);
    public static final TextAttributesKey ODIN_USING_BIT_FIELD_FIELD = createTextAttributesKey("ODIN_USING_BIT_FIELD_FIELD", DefaultLanguageHighlighterColors.INSTANCE_FIELD);

    public static final TextAttributesKey ODIN_STATIC_VARIABLE = createTextAttributesKey("ODIN_STATIC_VARIABLE", DefaultLanguageHighlighterColors.STATIC_FIELD);

    // References

    // Function calls
    public static final TextAttributesKey ODIN_PROC_CALL = createTextAttributesKey("ODIN_PROC_CALL", DefaultLanguageHighlighterColors.FUNCTION_CALL);
    public static final TextAttributesKey ODIN_LOCAL_PROC_CALL = createTextAttributesKey("ODIN_LOCAL_PROC_CALL", DefaultLanguageHighlighterColors.FUNCTION_CALL);
    public static final TextAttributesKey ODIN_FOREIGN_PROC_CALL = createTextAttributesKey("ODIN_FOREIGN_PROC_CALL", DefaultLanguageHighlighterColors.FUNCTION_CALL);
    public static final TextAttributesKey ODIN_PKG_EXP_PROC_CALL = createTextAttributesKey("ODIN_PKG_EXP_PROC_CALL", DefaultLanguageHighlighterColors.FUNCTION_CALL);
    public static final TextAttributesKey ODIN_PKG_PRIVATE_PROC_CALL = createTextAttributesKey("ODIN_PKG_PRIVATE_PROC_CALL", DefaultLanguageHighlighterColors.FUNCTION_CALL);
    public static final TextAttributesKey ODIN_FILE_PRIVATE_PROC_CALL = createTextAttributesKey("ODIN_FILE_PRIVATE_PROC_CALL", DefaultLanguageHighlighterColors.FUNCTION_CALL);

    public static final TextAttributesKey ODIN_BUILTIN_PROC_CALL = createTextAttributesKey("ODIN_BUILTIN_PROC_CALL", DefaultLanguageHighlighterColors.FUNCTION_CALL);

    // Type references
    public static final TextAttributesKey ODIN_BUILTIN_TYPE_REF = createTextAttributesKey("ODIN_BUILTIN_TYPE_REF", DefaultLanguageHighlighterColors.CLASS_REFERENCE);

    public static final TextAttributesKey ODIN_PKG_EXP_STRUCT_REF = createTextAttributesKey("ODIN_PKG_EXP_STRUCT_REF", DefaultLanguageHighlighterColors.CLASS_REFERENCE);
    public static final TextAttributesKey ODIN_PKG_PRIVATE_STRUCT_REF = createTextAttributesKey("ODIN_PKG_PRIVATE_STRUCT_REF", DefaultLanguageHighlighterColors.CLASS_REFERENCE);
    public static final TextAttributesKey ODIN_FILE_PRIVATE_STRUCT_REF = createTextAttributesKey("ODIN_FILE_PRIVATE_STRUCT_REF", DefaultLanguageHighlighterColors.CLASS_REFERENCE);
    public static final TextAttributesKey ODIN_LOCAL_STRUCT_REF = createTextAttributesKey("ODIN_LOCAL_STRUCT_REF", DefaultLanguageHighlighterColors.CLASS_REFERENCE);

    public static final TextAttributesKey ODIN_PKG_EXP_UNION_REF = createTextAttributesKey("ODIN_PKG_EXP_UNION_REF", DefaultLanguageHighlighterColors.CLASS_REFERENCE);
    public static final TextAttributesKey ODIN_PKG_PRIVATE_UNION_REF = createTextAttributesKey("ODIN_PKG_PRIVATE_UNION_REF", DefaultLanguageHighlighterColors.CLASS_REFERENCE);
    public static final TextAttributesKey ODIN_FILE_PRIVATE_UNION_REF = createTextAttributesKey("ODIN_FILE_PRIVATE_UNION_REF", DefaultLanguageHighlighterColors.CLASS_REFERENCE);
    public static final TextAttributesKey ODIN_LOCAL_UNION_REF = createTextAttributesKey("ODIN_LOCAL_UNION_REF", DefaultLanguageHighlighterColors.CLASS_REFERENCE);

    public static final TextAttributesKey ODIN_PKG_EXP_BIT_FIELD_REF = createTextAttributesKey("ODIN_PKG_EXP_BIT_FIELD_REF", DefaultLanguageHighlighterColors.INSTANCE_FIELD);
    public static final TextAttributesKey ODIN_FILE_PRIVATE_BIT_FIELD_REF = createTextAttributesKey("ODIN_FILE_PRIVATE_BIT_FIELD_REF", DefaultLanguageHighlighterColors.INSTANCE_FIELD);
    public static final TextAttributesKey ODIN_PKG_PRIVATE_BIT_FIELD_REF = createTextAttributesKey("ODIN_PKG_PRIVATE_BIT_FIELD_REF", DefaultLanguageHighlighterColors.INSTANCE_FIELD);
    public static final TextAttributesKey ODIN_LOCAL_BIT_FIELD_REF = createTextAttributesKey("ODIN_LOCAL_BIT_FIELD_REF", DefaultLanguageHighlighterColors.INSTANCE_FIELD);

    public static final TextAttributesKey ODIN_PKG_EXP_TYPE_ALIAS_REF = createTextAttributesKey("ODIN_PKG_EXP_TYPE_ALIAS_REF", DefaultLanguageHighlighterColors.CLASS_REFERENCE);
    public static final TextAttributesKey ODIN_FILE_PRIVATE_TYPE_ALIAS_REF = createTextAttributesKey("ODIN_FILE_PRIVATE_TYPE_ALIAS_REF", DefaultLanguageHighlighterColors.CLASS_REFERENCE);
    public static final TextAttributesKey ODIN_PKG_PRIVATE_TYPE_ALIAS_REF = createTextAttributesKey("ODIN_PKG_PRIVATE_TYPE_ALIAS_REF", DefaultLanguageHighlighterColors.CLASS_REFERENCE);
    public static final TextAttributesKey ODIN_LOCAL_TYPE_ALIAS_REF = createTextAttributesKey("ODIN_LOCAL_TYPE_ALIAS_REF", DefaultLanguageHighlighterColors.CLASS_REFERENCE);

    public static final TextAttributesKey ODIN_VAR_CALL = createTextAttributesKey("ODIN_VAR_CALL", DefaultLanguageHighlighterColors.IDENTIFIER);
    public static final TextAttributesKey ODIN_LOCAL_VAR_CALL = createTextAttributesKey("ODIN_LOCAL_VAR_CALL", DefaultLanguageHighlighterColors.LOCAL_VARIABLE);
    public static final TextAttributesKey ODIN_PKG_EXP_VAR_CALL = createTextAttributesKey("ODIN_PKG_EXP_VAR_CALL", DefaultLanguageHighlighterColors.IDENTIFIER);
    public static final TextAttributesKey ODIN_PKG_PRIVATE_VAR_CALL = createTextAttributesKey("ODIN_PKG_PRIVATE_VAR_CALL", DefaultLanguageHighlighterColors.IDENTIFIER);
    public static final TextAttributesKey ODIN_FILE_PRIVATE_VAR_CALL = createTextAttributesKey("ODIN_FILE_PRIVATE_VAR_CALL", DefaultLanguageHighlighterColors.IDENTIFIER);
    public static final TextAttributesKey ODIN_STRUCT_FIELD_CALL = createTextAttributesKey("ODIN_STRUCT_FIELD_CALL", DefaultLanguageHighlighterColors.INSTANCE_FIELD);
    public static final TextAttributesKey ODIN_FAKE_METHOD_CALL = createTextAttributesKey("ODIN_FAKE_METHOD_CALL", DefaultLanguageHighlighterColors.FUNCTION_CALL);

    public static final TextAttributesKey ODIN_POLY_PARAMETER = createTextAttributesKey("ODIN_POLY_PARAMETER", DefaultLanguageHighlighterColors.PARAMETER);
    public static final TextAttributesKey ODIN_POLY_PARAMETER_REF = createTextAttributesKey("ODIN_POLY_PARAMETER_REF", DefaultLanguageHighlighterColors.IDENTIFIER);
    // String
    public static final TextAttributesKey ODIN_INVALID_ESCAPE = createTextAttributesKey("ODIN_INVALID_ESCAPE", DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE);
    public static final TextAttributesKey ODIN_TEXT = createTextAttributesKey("ODIN_TEXT", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey ODIN_VALID_ESCAPE = createTextAttributesKey("ODIN_VALID_ESCAPE", DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE);
    public static final TextAttributesKey ODIN_NUMBER = createTextAttributesKey("ODIN_NUMBER", DefaultLanguageHighlighterColors.NUMBER);

    public static final OdinSymbolStyles STYLES = new OdinSymbolStyles();
    static {
        // Struct declarations
        STYLES.addStyle(OdinSymbolType.STRUCT, OdinScope.GLOBAL, OdinVisibility.PACKAGE_EXPORTED, OdinIdentifierType.DECLARATION, ODIN_PKG_EXP_STRUCT);
        STYLES.addStyle(OdinSymbolType.STRUCT, OdinScope.GLOBAL, OdinVisibility.PACKAGE_PRIVATE, OdinIdentifierType.DECLARATION, ODIN_PKG_PRIVATE_STRUCT);
        STYLES.addStyle(OdinSymbolType.STRUCT, OdinScope.GLOBAL, OdinVisibility.FILE_PRIVATE, OdinIdentifierType.DECLARATION, ODIN_FILE_PRIVATE_STRUCT);
        STYLES.addStyle(OdinSymbolType.STRUCT, OdinScope.LOCAL, OdinVisibility.NONE, OdinIdentifierType.DECLARATION, ODIN_LOCAL_STRUCT);

        // Union declarations
        STYLES.addStyle(OdinSymbolType.UNION, OdinScope.GLOBAL, OdinVisibility.PACKAGE_EXPORTED, OdinIdentifierType.DECLARATION, ODIN_PKG_EXP_UNION);
        STYLES.addStyle(OdinSymbolType.UNION, OdinScope.GLOBAL, OdinVisibility.PACKAGE_PRIVATE, OdinIdentifierType.DECLARATION, ODIN_PKG_PRIVATE_UNION);
        STYLES.addStyle(OdinSymbolType.UNION, OdinScope.GLOBAL, OdinVisibility.FILE_PRIVATE, OdinIdentifierType.DECLARATION, ODIN_FILE_PRIVATE_UNION);
        STYLES.addStyle(OdinSymbolType.UNION, OdinScope.LOCAL, OdinVisibility.NONE, OdinIdentifierType.DECLARATION, ODIN_LOCAL_UNION);
        
        // Bit Field declarations
        STYLES.addStyle(OdinSymbolType.BIT_FIELD, OdinScope.GLOBAL, OdinVisibility.PACKAGE_EXPORTED, OdinIdentifierType.DECLARATION, ODIN_PKG_EXP_BIT_FIELD);
        STYLES.addStyle(OdinSymbolType.BIT_FIELD, OdinScope.GLOBAL, OdinVisibility.PACKAGE_PRIVATE, OdinIdentifierType.DECLARATION, ODIN_PKG_PRIVATE_BIT_FIELD);
        STYLES.addStyle(OdinSymbolType.BIT_FIELD, OdinScope.GLOBAL, OdinVisibility.FILE_PRIVATE, OdinIdentifierType.DECLARATION, ODIN_FILE_PRIVATE_BIT_FIELD);
        STYLES.addStyle(OdinSymbolType.BIT_FIELD, OdinScope.LOCAL, OdinVisibility.NONE, OdinIdentifierType.DECLARATION, ODIN_LOCAL_BIT_FIELD);
        
        // Procedures
        STYLES.addStyle(OdinSymbolType.PROCEDURE, OdinScope.GLOBAL, OdinVisibility.PACKAGE_EXPORTED, OdinIdentifierType.DECLARATION, ODIN_PKG_EXP_PROC);
        STYLES.addStyle(OdinSymbolType.PROCEDURE, OdinScope.GLOBAL, OdinVisibility.PACKAGE_PRIVATE, OdinIdentifierType.DECLARATION, ODIN_PKG_PRIVATE_PROC);
        STYLES.addStyle(OdinSymbolType.PROCEDURE, OdinScope.GLOBAL, OdinVisibility.FILE_PRIVATE, OdinIdentifierType.DECLARATION, ODIN_FILE_PRIVATE_PROC);
        STYLES.addStyle(OdinSymbolType.PROCEDURE, OdinScope.LOCAL, OdinVisibility.NONE, OdinIdentifierType.DECLARATION, ODIN_LOCAL_PROC);

    }
}

