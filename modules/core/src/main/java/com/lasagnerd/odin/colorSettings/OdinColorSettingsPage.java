package com.lasagnerd.odin.colorSettings;

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.intellij.openapi.util.NlsContexts;
import com.lasagnerd.odin.OdinIcons;
import com.lasagnerd.odin.lang.OdinSyntaxHighlighter;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Map;

public class OdinColorSettingsPage implements ColorSettingsPage {
    public static final Map<String, TextAttributesKey> ADDITIONAL_TAGS = Map.of(
            "builtin", OdinSyntaxColors.ODIN_BUILTIN_PROC,
            "package", OdinSyntaxColors.ODIN_PACKAGE,
            "constant-pkg-exp", OdinSyntaxColors.ODIN_PKG_EXP_CONSTANT,
            "constant-pkg-private", OdinSyntaxColors.ODIN_PKG_PRIVATE_CONSTANT,
            "constant-file-private", OdinSyntaxColors.ODIN_FILE_PRIVATE_CONSTANT,
            "builtin-type-ref", OdinSyntaxColors.ODIN_BUILTIN_TYPE_REF,
            "proc-call-pkg-exp", OdinSyntaxColors.ODIN_PKG_EXP_PROC_CALL
    );
    @Language("Odin")
    public static final String CODE = """
            package <package>main</package>;
            import "core:fmt"
            import alias "./path/to/package"
            
            <constant-pkg-exp>PI</constant-pkg-exp> :: 3.14159
            <constant-pkg-exp>mat2x2</constant-pkg-exp> :: distinct matrix[2, 2]f32
            
            @private
            <constant-pkg-private>PKG_PRIVATE_CONSTANT</constant-pkg-private> :: PI
            
            @(private="file")
            <constant-file-private>FILE_PRIVATE_CONSTANT</constant-file-private> :: PKG_PRIVATE_CONSTANT
            
            Foo :: struct {
                bar: <builtin-type-ref>int</builtin-type-ref>
            }
            
            Generic_Struct :: struct($T: typeid, $V: int) {
                foo: T,
                val: arr[V]
            }
            
            Union :: union { Foo, f32 }
            
            main :: proc() {
                <package>fmt</package>.<proc-call-pkg-exp>println</proc-call-pkg-exp>("Hi Mom!")
            
                my_array := <builtin>make</builtin>([dynamic]int)
                my_value : <builtin-type-ref>int</builtin-type-ref> = 5
            
                alias.package_exported_func()
            }
            
            control_flow :: proc() {
                if x:=1; x > 0 {
    
                } else if x < 0 {
                    
                } else {
        
                }   
                
                switch x {}      
            }
            
            """;

    AttributesDescriptor[] ATTRIBUTES_DESCRIPTORS = new AttributesDescriptor[]{
            new AttributesDescriptor("Bad character", OdinSyntaxColors.ODIN_BAD_CHARACTER),

            new AttributesDescriptor("Built-in symbol", OdinSyntaxColors.ODIN_BUILTIN_PROC),
            new AttributesDescriptor("Package", OdinSyntaxColors.ODIN_PACKAGE),

            // Braces and operators
            new AttributesDescriptor("Braces and operators//Braces", OdinSyntaxColors.ODIN_BRACES),
            new AttributesDescriptor("Braces and operators//Brackets", OdinSyntaxColors.ODIN_BRACKETS),
            new AttributesDescriptor("Braces and operators//Colon", OdinSyntaxColors.ODIN_COLON),
            new AttributesDescriptor("Braces and operators//Comma", OdinSyntaxColors.ODIN_COMMA),
            new AttributesDescriptor("Braces and operators//Dot", OdinSyntaxColors.ODIN_DOT),
            new AttributesDescriptor("Braces and operators//Operator", OdinSyntaxColors.ODIN_OPERATOR),
            new AttributesDescriptor("Braces and operators//Parentheses", OdinSyntaxColors.ODIN_PARENTHESES),
            new AttributesDescriptor("Braces and operators//Semicolon", OdinSyntaxColors.ODIN_SEMICOLON),

            // Comments
            new AttributesDescriptor("Comments//Block comment", OdinSyntaxColors.ODIN_BLOCK_COMMENT),
            new AttributesDescriptor("Comments//Comment reference", OdinSyntaxColors.ODIN_COMMENT_REFERENCE),
            new AttributesDescriptor("Comments//Line comment", OdinSyntaxColors.ODIN_LINE_COMMENT),

            // Declarations
            new AttributesDescriptor("Declarations//Label", OdinSyntaxColors.ODIN_LABEL),
            new AttributesDescriptor("Declarations//Procedure declaration", OdinSyntaxColors.ODIN_PROC_DECLARATION),
            new AttributesDescriptor("Declarations//Procedure parameter", OdinSyntaxColors.ODIN_PROC_PARAMETER),

            // Keywords
            new AttributesDescriptor("Keyword", OdinSyntaxColors.ODIN_KEYWORD),

            // Identifiers
            new AttributesDescriptor("Identifier", OdinSyntaxColors.ODIN_IDENTIFIER),

            // Constants
            new AttributesDescriptor("Constants//Constant", OdinSyntaxColors.ODIN_CONSTANT),
            new AttributesDescriptor("Constants//Local constant", OdinSyntaxColors.ODIN_LOCAL_CONSTANT),
            new AttributesDescriptor("Constants//Package exported constant", OdinSyntaxColors.ODIN_PKG_EXP_CONSTANT),
            new AttributesDescriptor("Constants//File private constant", OdinSyntaxColors.ODIN_FILE_PRIVATE_CONSTANT),
            new AttributesDescriptor("Constants//Package private constant", OdinSyntaxColors.ODIN_PKG_PRIVATE_CONSTANT),

            // Procedures
            new AttributesDescriptor("Procedures//Built-in procedure", OdinSyntaxColors.ODIN_BUILTIN_PROC),
            new AttributesDescriptor("Procedures//Package exported procedure", OdinSyntaxColors.ODIN_PKG_EXP_PROC),
            new AttributesDescriptor("Procedures//File private procedure", OdinSyntaxColors.ODIN_FILE_PRIVATE_PROC),
            new AttributesDescriptor("Procedures//Package private procedure", OdinSyntaxColors.ODIN_PKG_PRIVATE_PROC),
            new AttributesDescriptor("Procedures//Local procedure", OdinSyntaxColors.ODIN_LOCAL_PROC),

            // Types
            new AttributesDescriptor("Types//Struct", OdinSyntaxColors.ODIN_STRUCT_TYPE),
            new AttributesDescriptor("Types//Union", OdinSyntaxColors.ODIN_UNION_TYPE),
            new AttributesDescriptor("Types//Package exported struct", OdinSyntaxColors.ODIN_PKG_EXP_STRUCT),
            new AttributesDescriptor("Types//File private struct", OdinSyntaxColors.ODIN_FILE_PRIVATE_STRUCT),
            new AttributesDescriptor("Types//Package private struct", OdinSyntaxColors.ODIN_PKG_PRIVATE_STRUCT),
            new AttributesDescriptor("Types//Local struct", OdinSyntaxColors.ODIN_LOCAL_STRUCT),
            new AttributesDescriptor("Types//Package exported union", OdinSyntaxColors.ODIN_PKG_EXP_UNION),
            new AttributesDescriptor("Types//File private union", OdinSyntaxColors.ODIN_FILE_PRIVATE_UNION),
            new AttributesDescriptor("Types//Package private union", OdinSyntaxColors.ODIN_PKG_PRIVATE_UNION),
            new AttributesDescriptor("Types//Local union", OdinSyntaxColors.ODIN_LOCAL_UNION),
            new AttributesDescriptor("Types//Package exported bit field", OdinSyntaxColors.ODIN_PKG_EXP_BIT_FIELD),
            new AttributesDescriptor("Types//File private bit field", OdinSyntaxColors.ODIN_FILE_PRIVATE_BIT_FIELD),
            new AttributesDescriptor("Types//Package private bit field", OdinSyntaxColors.ODIN_PKG_PRIVATE_BIT_FIELD),
            new AttributesDescriptor("Types//Local bit field", OdinSyntaxColors.ODIN_LOCAL_BIT_FIELD),
            new AttributesDescriptor("Types//Package exported type alias", OdinSyntaxColors.ODIN_PKG_EXP_TYPE_ALIAS),
            new AttributesDescriptor("Types//File private type alias", OdinSyntaxColors.ODIN_FILE_PRIVATE_TYPE_ALIAS),
            new AttributesDescriptor("Types//Package private type alias", OdinSyntaxColors.ODIN_PKG_PRIVATE_TYPE_ALIAS),
            new AttributesDescriptor("Types//Local type alias", OdinSyntaxColors.ODIN_LOCAL_TYPE_ALIAS),

            // Variables and Fields
            new AttributesDescriptor("Variables and fields//Built-in variable", OdinSyntaxColors.ODIN_BUILTIN_VAR),
            new AttributesDescriptor("Variables and fields//Global variable", OdinSyntaxColors.ODIN_GLOBAL_VARIABLE),
            new AttributesDescriptor("Variables and fields//Local variable", OdinSyntaxColors.ODIN_LOCAL_VARIABLE),
            new AttributesDescriptor("Variables and fields//Static variable", OdinSyntaxColors.ODIN_STATIC_VARIABLE),
            new AttributesDescriptor("Variables and fields//Field", OdinSyntaxColors.ODIN_FIELD),
            new AttributesDescriptor("Variables and fields//Struct field", OdinSyntaxColors.ODIN_STRUCT_FIELD),
            new AttributesDescriptor("Variables and fields//Bit field", OdinSyntaxColors.ODIN_BIT_FIELD),
            new AttributesDescriptor("Variables and fields//Package exported variable", OdinSyntaxColors.ODIN_PKG_EXP_VARIABLE),
            new AttributesDescriptor("Variables and fields//File private variable", OdinSyntaxColors.ODIN_FILE_PRIVATE_VARIABLE),
            new AttributesDescriptor("Variables and fields//Package private variable", OdinSyntaxColors.ODIN_PKG_PRIVATE_VARIABLE),
            new AttributesDescriptor("Variables and fields//Variable reassignment", OdinSyntaxColors.ODIN_VAR_REASSIGNMENT),
            new AttributesDescriptor("Variables and fields//Shadowing variable", OdinSyntaxColors.ODIN_SHADOWING_VARIABLE),

            // Function calls
            new AttributesDescriptor("Function calls//Procedure call", OdinSyntaxColors.ODIN_PROC_CALL),
            new AttributesDescriptor("Function calls//Local procedure call", OdinSyntaxColors.ODIN_LOCAL_PROC_CALL),
            new AttributesDescriptor("Function calls//Package exported procedure call", OdinSyntaxColors.ODIN_PKG_EXP_PROC_CALL),
            new AttributesDescriptor("Function calls//Package private procedure call", OdinSyntaxColors.ODIN_PKG_PRIVATE_PROC_CALL),
            new AttributesDescriptor("Function calls//File private procedure call", OdinSyntaxColors.ODIN_FILE_PRIVATE_PROC_CALL),
            new AttributesDescriptor("Function calls//Built-in procedure call", OdinSyntaxColors.ODIN_BUILTIN_PROC_CALL),

            // References
            new AttributesDescriptor("References//Struct reference", OdinSyntaxColors.ODIN_STRUCT_REF),
            new AttributesDescriptor("References//Union reference", OdinSyntaxColors.ODIN_UNION_REF),
            new AttributesDescriptor("References//Built-in type reference", OdinSyntaxColors.ODIN_BUILTIN_TYPE_REF),
            new AttributesDescriptor("References//Package exported struct reference", OdinSyntaxColors.ODIN_PKG_EXP_STRUCT_REF),
            new AttributesDescriptor("References//Package private struct reference", OdinSyntaxColors.ODIN_PKG_PRIVATE_STRUCT_REF),
            new AttributesDescriptor("References//File private struct reference", OdinSyntaxColors.ODIN_FILE_PRIVATE_STRUCT_REF),
            new AttributesDescriptor("References//Local struct reference", OdinSyntaxColors.ODIN_LOCAL_STRUCT_REF),
            new AttributesDescriptor("References//Package exported union reference", OdinSyntaxColors.ODIN_PKG_EXP_UNION_REF),
            new AttributesDescriptor("References//Package private union reference", OdinSyntaxColors.ODIN_PKG_PRIVATE_UNION_REF),
            new AttributesDescriptor("References//File private union reference", OdinSyntaxColors.ODIN_FILE_PRIVATE_UNION_REF),
            new AttributesDescriptor("References//Local union reference", OdinSyntaxColors.ODIN_LOCAL_UNION_REF),
            new AttributesDescriptor("References//Package exported bit field reference", OdinSyntaxColors.ODIN_PKG_EXP_BIT_FIELD_REF),
            new AttributesDescriptor("References//File private bit field reference", OdinSyntaxColors.ODIN_FILE_PRIVATE_BIT_FIELD_REF),
            new AttributesDescriptor("References//Package private bit field reference", OdinSyntaxColors.ODIN_PKG_PRIVATE_BIT_FIELD_REF),
            new AttributesDescriptor("References//Local bit field reference", OdinSyntaxColors.ODIN_LOCAL_BIT_FIELD_REF),
            new AttributesDescriptor("References//Package exported type alias reference", OdinSyntaxColors.ODIN_PKG_EXP_TYPE_ALIAS_REF),
            new AttributesDescriptor("References//File private type alias reference", OdinSyntaxColors.ODIN_FILE_PRIVATE_TYPE_ALIAS_REF),
            new AttributesDescriptor("References//Package private type alias reference", OdinSyntaxColors.ODIN_PKG_PRIVATE_TYPE_ALIAS_REF),
            new AttributesDescriptor("References//Local type alias reference", OdinSyntaxColors.ODIN_LOCAL_TYPE_ALIAS_REF),
            new AttributesDescriptor("References//Variable call", OdinSyntaxColors.ODIN_VAR_CALL),
            new AttributesDescriptor("References//Local variable call", OdinSyntaxColors.ODIN_LOCAL_VAR_CALL),
            new AttributesDescriptor("References//Package exported variable call", OdinSyntaxColors.ODIN_PKG_EXP_VAR_CALL),
            new AttributesDescriptor("References//Package private variable call", OdinSyntaxColors.ODIN_PKG_PRIVATE_VAR_CALL),
            new AttributesDescriptor("References//File private variable call", OdinSyntaxColors.ODIN_FILE_PRIVATE_VAR_CALL),
            new AttributesDescriptor("References//Struct field call", OdinSyntaxColors.ODIN_STRUCT_FIELD_CALL),
            new AttributesDescriptor("References//Fake method call", OdinSyntaxColors.ODIN_FAKE_METHOD_CALL),

            // String and Escape
            new AttributesDescriptor("String and Escape//Text", OdinSyntaxColors.ODIN_TEXT),
            new AttributesDescriptor("String and Escape//Valid escape", OdinSyntaxColors.ODIN_VALID_ESCAPE),
            new AttributesDescriptor("String and Escape//Invalid escape", OdinSyntaxColors.ODIN_INVALID_ESCAPE)
    };


    @Override
    public @Nullable Icon getIcon() {
        return OdinIcons.OdinRunConfiguration;
    }

    @Override
    public @NotNull SyntaxHighlighter getHighlighter() {
        return new OdinSyntaxHighlighter();
    }

    @Override
    public @NonNls @NotNull String getDemoText() {
        return CODE;
    }

    @Override
    public @Nullable Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return ADDITIONAL_TAGS;
    }

    @Override
    public AttributesDescriptor @NotNull [] getAttributeDescriptors() {
        return ATTRIBUTES_DESCRIPTORS;
    }

    @Override
    public ColorDescriptor @NotNull [] getColorDescriptors() {
        return new ColorDescriptor[0];
    }

    @Override
    public @NotNull @NlsContexts.ConfigurableName String getDisplayName() {
        return "Odin";
    }
}
