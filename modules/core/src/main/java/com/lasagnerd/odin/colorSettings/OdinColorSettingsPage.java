package com.lasagnerd.odin.colorSettings;

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
            "builtin", OdinSyntaxTextAttributes.ODIN_BUILTIN_PROC,
            "package", OdinSyntaxTextAttributes.ODIN_PACKAGE,
            "constant-pkg-exp", OdinSyntaxTextAttributes.ODIN_PKG_EXP_CONSTANT,
            "constant-pkg-private", OdinSyntaxTextAttributes.ODIN_PKG_PRIVATE_CONSTANT,
            "constant-file-private", OdinSyntaxTextAttributes.ODIN_FILE_PRIVATE_CONSTANT,
            "builtin-type-ref", OdinSyntaxTextAttributes.ODIN_BUILTIN_TYPE_REF,
            "proc-call-pkg-exp", OdinSyntaxTextAttributes.ODIN_PKG_EXP_PROC_CALL
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
            new AttributesDescriptor("Bad character", OdinSyntaxTextAttributes.ODIN_BAD_CHARACTER),

            new AttributesDescriptor("Built-in symbol", OdinSyntaxTextAttributes.ODIN_BUILTIN_PROC),
            new AttributesDescriptor("Package", OdinSyntaxTextAttributes.ODIN_PACKAGE),

            new AttributesDescriptor("Attributes//Attribute identifier", OdinSyntaxTextAttributes.ODIN_ATTRIBUTE_REF),
            new AttributesDescriptor("Attributes//'@' character", OdinSyntaxTextAttributes.ODIN_AT),

            new AttributesDescriptor("Directive", OdinSyntaxTextAttributes.ODIN_DIRECTIVE_REF),

            // Braces and operators
            new AttributesDescriptor("Braces and operators//Braces", OdinSyntaxTextAttributes.ODIN_BRACES),
            new AttributesDescriptor("Braces and operators//Brackets", OdinSyntaxTextAttributes.ODIN_BRACKETS),
            new AttributesDescriptor("Braces and operators//Colon", OdinSyntaxTextAttributes.ODIN_COLON),
            new AttributesDescriptor("Braces and operators//Comma", OdinSyntaxTextAttributes.ODIN_COMMA),
            new AttributesDescriptor("Braces and operators//Dot", OdinSyntaxTextAttributes.ODIN_DOT),
            new AttributesDescriptor("Braces and operators//Operator", OdinSyntaxTextAttributes.ODIN_OPERATOR),
            new AttributesDescriptor("Braces and operators//Parentheses", OdinSyntaxTextAttributes.ODIN_PARENTHESES),
            new AttributesDescriptor("Braces and operators//Semicolon", OdinSyntaxTextAttributes.ODIN_SEMICOLON),

            // Comments
            new AttributesDescriptor("Comments//Block comment", OdinSyntaxTextAttributes.ODIN_BLOCK_COMMENT),
            new AttributesDescriptor("Comments//Comment reference", OdinSyntaxTextAttributes.ODIN_COMMENT_REFERENCE),
            new AttributesDescriptor("Comments//Line comment", OdinSyntaxTextAttributes.ODIN_LINE_COMMENT),

            // Declarations
            new AttributesDescriptor("Declarations//Label", OdinSyntaxTextAttributes.ODIN_LABEL),
            new AttributesDescriptor("Declarations//Parameter", OdinSyntaxTextAttributes.ODIN_PARAMETER),


            // Keywords
            new AttributesDescriptor("Keyword", OdinSyntaxTextAttributes.ODIN_KEYWORD),

            // Identifiers
            new AttributesDescriptor("Identifier", OdinSyntaxTextAttributes.ODIN_IDENTIFIER),

            // Constants
            new AttributesDescriptor("Declarations//Constants//Local constant", OdinSyntaxTextAttributes.ODIN_LOCAL_CONSTANT),
            new AttributesDescriptor("Declarations//Constants//Package exported constant", OdinSyntaxTextAttributes.ODIN_PKG_EXP_CONSTANT),
            new AttributesDescriptor("Declarations//Constants//File private constant", OdinSyntaxTextAttributes.ODIN_FILE_PRIVATE_CONSTANT),
            new AttributesDescriptor("Declarations//Constants//Package private constant", OdinSyntaxTextAttributes.ODIN_PKG_PRIVATE_CONSTANT),

            // Procedures
            new AttributesDescriptor("Declarations//Procedures//Built-in procedure", OdinSyntaxTextAttributes.ODIN_BUILTIN_PROC),
            new AttributesDescriptor("Declarations//Procedures//Package exported procedure", OdinSyntaxTextAttributes.ODIN_PKG_EXP_PROC),
            new AttributesDescriptor("Declarations//Procedures//File private procedure", OdinSyntaxTextAttributes.ODIN_FILE_PRIVATE_PROC),
            new AttributesDescriptor("Declarations//Procedures//Package private procedure", OdinSyntaxTextAttributes.ODIN_PKG_PRIVATE_PROC),
            new AttributesDescriptor("Declarations//Procedures//Local procedure", OdinSyntaxTextAttributes.ODIN_LOCAL_PROC),

            // Types
            new AttributesDescriptor("Declarations//Struct//Package exported struct", OdinSyntaxTextAttributes.ODIN_PKG_EXP_STRUCT),
            new AttributesDescriptor("Declarations//Struct//File private struct", OdinSyntaxTextAttributes.ODIN_FILE_PRIVATE_STRUCT),
            new AttributesDescriptor("Declarations//Struct//Package private struct", OdinSyntaxTextAttributes.ODIN_PKG_PRIVATE_STRUCT),
            new AttributesDescriptor("Declarations//Struct//Local struct", OdinSyntaxTextAttributes.ODIN_LOCAL_STRUCT),

            new AttributesDescriptor("Declarations//Union//Package exported union", OdinSyntaxTextAttributes.ODIN_PKG_EXP_UNION),
            new AttributesDescriptor("Declarations//Union//File private union", OdinSyntaxTextAttributes.ODIN_FILE_PRIVATE_UNION),
            new AttributesDescriptor("Declarations//Union//Package private union", OdinSyntaxTextAttributes.ODIN_PKG_PRIVATE_UNION),
            new AttributesDescriptor("Declarations//Union//Local union", OdinSyntaxTextAttributes.ODIN_LOCAL_UNION),

            new AttributesDescriptor("Declarations//Enum//Package exported enum", OdinSyntaxTextAttributes.ODIN_PKG_EXP_ENUM),
            new AttributesDescriptor("Declarations//Enum//File private enum", OdinSyntaxTextAttributes.ODIN_FILE_PRIVATE_ENUM),
            new AttributesDescriptor("Declarations//Enum//Package private enum", OdinSyntaxTextAttributes.ODIN_PKG_PRIVATE_ENUM),
            new AttributesDescriptor("Declarations//Enum//Local enum", OdinSyntaxTextAttributes.ODIN_LOCAL_ENUM),

            new AttributesDescriptor("Declarations//Bit field//Package exported bit field", OdinSyntaxTextAttributes.ODIN_PKG_EXP_BIT_FIELD),
            new AttributesDescriptor("Declarations//Bit field//File private bit field", OdinSyntaxTextAttributes.ODIN_FILE_PRIVATE_BIT_FIELD),
            new AttributesDescriptor("Declarations//Bit field//Package private bit field", OdinSyntaxTextAttributes.ODIN_PKG_PRIVATE_BIT_FIELD),
            new AttributesDescriptor("Declarations//Bit field//Local bit field", OdinSyntaxTextAttributes.ODIN_LOCAL_BIT_FIELD),

            new AttributesDescriptor("Declarations//Type alias//Package exported type alias", OdinSyntaxTextAttributes.ODIN_PKG_EXP_TYPE_ALIAS),
            new AttributesDescriptor("Declarations//Type alias//File private type alias", OdinSyntaxTextAttributes.ODIN_FILE_PRIVATE_TYPE_ALIAS),
            new AttributesDescriptor("Declarations//Type alias//Package private type alias", OdinSyntaxTextAttributes.ODIN_PKG_PRIVATE_TYPE_ALIAS),
            new AttributesDescriptor("Declarations//Type alias//Local type alias", OdinSyntaxTextAttributes.ODIN_LOCAL_TYPE_ALIAS),

            new AttributesDescriptor("Declarations//Types//Polymorphic parameter", OdinSyntaxTextAttributes.ODIN_POLY_PARAMETER),

            // Variables and Fields
            new AttributesDescriptor("Declarations//Variables//Built-in variable", OdinSyntaxTextAttributes.ODIN_BUILTIN_VAR),
            new AttributesDescriptor("Declarations//Variables//Local variable", OdinSyntaxTextAttributes.ODIN_LOCAL_VARIABLE),
            new AttributesDescriptor("Declarations//Variables//Static variable", OdinSyntaxTextAttributes.ODIN_STATIC_VARIABLE),
            new AttributesDescriptor("Declarations//Variables//Package exported global variable", OdinSyntaxTextAttributes.ODIN_PKG_EXP_VARIABLE),
            new AttributesDescriptor("Declarations//Variables//File private global variable", OdinSyntaxTextAttributes.ODIN_FILE_PRIVATE_VARIABLE),
            new AttributesDescriptor("Declarations//Variables//Package private global variable", OdinSyntaxTextAttributes.ODIN_PKG_PRIVATE_VARIABLE),
            new AttributesDescriptor("Declarations//Variables//Variable reassignment", OdinSyntaxTextAttributes.ODIN_VAR_REASSIGNMENT),
            new AttributesDescriptor("Declarations//Variables//Shadowing variable", OdinSyntaxTextAttributes.ODIN_SHADOWING_VARIABLE),

            new AttributesDescriptor("Declarations//Fields//Struct field", OdinSyntaxTextAttributes.ODIN_STRUCT_FIELD),
            new AttributesDescriptor("Declarations//Fields//Bit field", OdinSyntaxTextAttributes.ODIN_BIT_FIELD_FIELD),
            new AttributesDescriptor("Declarations//Fields//Enum field", OdinSyntaxTextAttributes.ODIN_ENUM_FIELD),


            // Function calls
            new AttributesDescriptor("Procedure calls//Local procedure call", OdinSyntaxTextAttributes.ODIN_LOCAL_PROC_CALL),
            new AttributesDescriptor("Procedure calls//Package exported procedure call", OdinSyntaxTextAttributes.ODIN_PKG_EXP_PROC_CALL),
            new AttributesDescriptor("Procedure calls//Package private procedure call", OdinSyntaxTextAttributes.ODIN_PKG_PRIVATE_PROC_CALL),
            new AttributesDescriptor("Procedure calls//File private procedure call", OdinSyntaxTextAttributes.ODIN_FILE_PRIVATE_PROC_CALL),
            new AttributesDescriptor("Procedure calls//Built-in procedure call", OdinSyntaxTextAttributes.ODIN_BUILTIN_PROC_CALL),
            new AttributesDescriptor("Procedure calls//Variable call", OdinSyntaxTextAttributes.ODIN_VAR_CALL),
            new AttributesDescriptor("Procedure calls//Local variable call", OdinSyntaxTextAttributes.ODIN_LOCAL_VAR_CALL),
            new AttributesDescriptor("Procedure calls//Package exported global variable call", OdinSyntaxTextAttributes.ODIN_PKG_EXP_VAR_CALL),
            new AttributesDescriptor("Procedure calls//Package private global variable call", OdinSyntaxTextAttributes.ODIN_PKG_PRIVATE_VAR_CALL),
            new AttributesDescriptor("Procedure calls//File private global variable call", OdinSyntaxTextAttributes.ODIN_FILE_PRIVATE_VAR_CALL),
            new AttributesDescriptor("Procedure calls//Struct field call", OdinSyntaxTextAttributes.ODIN_STRUCT_FIELD_CALL),
            new AttributesDescriptor("Procedure calls//Parameter call", OdinSyntaxTextAttributes.ODIN_PARAMETER_CALL),
//            new AttributesDescriptor("Procedure calls//Fake method call", OdinSyntaxTextAttributes.ODIN_FAKE_METHOD_CALL),

            // References
            new AttributesDescriptor("References//Built-in type reference", OdinSyntaxTextAttributes.ODIN_BUILTIN_TYPE_REF),

            new AttributesDescriptor("References//Struct//Package exported struct reference", OdinSyntaxTextAttributes.ODIN_PKG_EXP_STRUCT_REF),
            new AttributesDescriptor("References//Struct//Package private struct reference", OdinSyntaxTextAttributes.ODIN_PKG_PRIVATE_STRUCT_REF),
            new AttributesDescriptor("References//Struct//File private struct reference", OdinSyntaxTextAttributes.ODIN_FILE_PRIVATE_STRUCT_REF),
            new AttributesDescriptor("References//Struct//Local struct reference", OdinSyntaxTextAttributes.ODIN_LOCAL_STRUCT_REF),

            new AttributesDescriptor("References//Union//Package exported union reference", OdinSyntaxTextAttributes.ODIN_PKG_EXP_UNION_REF),
            new AttributesDescriptor("References//Union//Package private union reference", OdinSyntaxTextAttributes.ODIN_PKG_PRIVATE_UNION_REF),
            new AttributesDescriptor("References//Union//File private union reference", OdinSyntaxTextAttributes.ODIN_FILE_PRIVATE_UNION_REF),
            new AttributesDescriptor("References//Union//Local union reference", OdinSyntaxTextAttributes.ODIN_LOCAL_UNION_REF),

            new AttributesDescriptor("References//Bit field//Package exported bit field reference", OdinSyntaxTextAttributes.ODIN_PKG_EXP_BIT_FIELD_REF),
            new AttributesDescriptor("References//Bit field//File private bit field reference", OdinSyntaxTextAttributes.ODIN_FILE_PRIVATE_BIT_FIELD_REF),
            new AttributesDescriptor("References//Bit field//Package private bit field reference", OdinSyntaxTextAttributes.ODIN_PKG_PRIVATE_BIT_FIELD_REF),
            new AttributesDescriptor("References//Bit field//Local bit field reference", OdinSyntaxTextAttributes.ODIN_LOCAL_BIT_FIELD_REF),

            new AttributesDescriptor("References//Enum//Package exported enum reference", OdinSyntaxTextAttributes.ODIN_PKG_EXP_ENUM_REF),
            new AttributesDescriptor("References//Enum//Package private enum reference", OdinSyntaxTextAttributes.ODIN_PKG_PRIVATE_ENUM_REF),
            new AttributesDescriptor("References//Enum//File private enum reference", OdinSyntaxTextAttributes.ODIN_FILE_PRIVATE_ENUM_REF),
            new AttributesDescriptor("References//Enum//Local enum reference", OdinSyntaxTextAttributes.ODIN_LOCAL_ENUM_REF),

            new AttributesDescriptor("References//Fields//Struct field", OdinSyntaxTextAttributes.ODIN_STRUCT_FIELD_REF),
            new AttributesDescriptor("References//Fields//Bit field", OdinSyntaxTextAttributes.ODIN_BIT_FIELD_FIELD_REF),
            new AttributesDescriptor("References//Fields//Enum field", OdinSyntaxTextAttributes.ODIN_ENUM_FIELD_REF),
            new AttributesDescriptor("References//Fields//Allocator field", OdinSyntaxTextAttributes.ODIN_ALLOCATOR_FIELD_REF),
            new AttributesDescriptor("References//Fields//Implicit Enum field", OdinSyntaxTextAttributes.ODIN_IMPLICIT_ENUM_FIELD_REF),
            new AttributesDescriptor("References//Fields//Enum field through using", OdinSyntaxTextAttributes.ODIN_USING_ENUM_FIELD_REF),
            new AttributesDescriptor("References//Fields//Struct field through using", OdinSyntaxTextAttributes.ODIN_USING_STRUCT_FIELD_REF),
            new AttributesDescriptor("References//Fields//Swizzle field", OdinSyntaxTextAttributes.ODIN_SWIZZLE_FIELD_REF),

//            new AttributesDescriptor("References//Type alias//Package exported type alias reference", OdinSyntaxTextAttributes.ODIN_PKG_EXP_TYPE_ALIAS_REF),
//            new AttributesDescriptor("References//Type alias//File private type alias reference", OdinSyntaxTextAttributes.ODIN_FILE_PRIVATE_TYPE_ALIAS_REF),
//            new AttributesDescriptor("References//Type alias//Package private type alias reference", OdinSyntaxTextAttributes.ODIN_PKG_PRIVATE_TYPE_ALIAS_REF),
//            new AttributesDescriptor("References//Type alias//Local type alias reference", OdinSyntaxTextAttributes.ODIN_LOCAL_TYPE_ALIAS_REF),

            new AttributesDescriptor("References//Polymorphic parameter", OdinSyntaxTextAttributes.ODIN_POLY_PARAMETER_REF),
            new AttributesDescriptor("References//Procedure parameter", OdinSyntaxTextAttributes.ODIN_PARAMETER_REF),
            new AttributesDescriptor("References//Context parameter", OdinSyntaxTextAttributes.ODIN_CONTEXT_PARAM_REF),

            // String and Escape
            new AttributesDescriptor("String and Escape//Text", OdinSyntaxTextAttributes.ODIN_TEXT),
            new AttributesDescriptor("String and Escape//Valid escape", OdinSyntaxTextAttributes.ODIN_VALID_ESCAPE),
            new AttributesDescriptor("String and Escape//Invalid escape", OdinSyntaxTextAttributes.ODIN_INVALID_ESCAPE),

            new AttributesDescriptor("Numbers", OdinSyntaxTextAttributes.ODIN_NUMBER)
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
