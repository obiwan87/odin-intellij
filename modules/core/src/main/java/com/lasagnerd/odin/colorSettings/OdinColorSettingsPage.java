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
import java.util.HashMap;
import java.util.Map;

import static com.lasagnerd.odin.colorSettings.OdinSyntaxTextAttributes.*;

public class OdinColorSettingsPage implements ColorSettingsPage {
    public static final Map<String, TextAttributesKey> ADDITIONAL_TAGS = new HashMap<>();

    @Language("Odin")
    public static final String CODE = """
            package testData
            
            import "core:fmt"
            
            // procedures
            @<ODIN_ATTRIBUTE_REF>private</ODIN_ATTRIBUTE_REF>
            <ODIN_PKG_PRIVATE_PROC>package_private_proc</ODIN_PKG_PRIVATE_PROC> :: proc() {
            }
            
            
            @(<ODIN_ATTRIBUTE_REF>private</ODIN_ATTRIBUTE_REF>="file")
            <ODIN_FILE_PRIVATE_PROC>file_private_proc</ODIN_FILE_PRIVATE_PROC> :: proc() {
            }
            
            <ODIN_PKG_EXP_PROC>package_exported_proc</ODIN_PKG_EXP_PROC> :: proc() {
            }
            
            // variables
            @(<ODIN_ATTRIBUTE_REF>private</ODIN_ATTRIBUTE_REF>="file")
            <ODIN_FILE_PRIVATE_VARIABLE>file_private_var</ODIN_FILE_PRIVATE_VARIABLE> := 2
            
            @<ODIN_ATTRIBUTE_REF>private</ODIN_ATTRIBUTE_REF>
            <ODIN_PKG_PRIVATE_VARIABLE>package_private_var</ODIN_PKG_PRIVATE_VARIABLE> := 1
            
            <ODIN_PKG_EXP_VARIABLE>package_exported_var</ODIN_PKG_EXP_VARIABLE> := 3
            
            // variable procedures
            @(<ODIN_ATTRIBUTE_REF>private</ODIN_ATTRIBUTE_REF>="file")
            <ODIN_FILE_PRIVATE_VARIABLE>file_private_var_proc</ODIN_FILE_PRIVATE_VARIABLE> := proc() {
            }
            
            @<ODIN_ATTRIBUTE_REF>private</ODIN_ATTRIBUTE_REF>
            <ODIN_PKG_PRIVATE_VARIABLE>package_private_var_proc</ODIN_PKG_PRIVATE_VARIABLE> := proc() {
            }
            
            <ODIN_PKG_EXP_VARIABLE>package_exported_var_proc</ODIN_PKG_EXP_VARIABLE> := proc() {
            }
            
            // constants
            @(<ODIN_ATTRIBUTE_REF>private</ODIN_ATTRIBUTE_REF>="file")
            FILE_PRIVATE_CONST :: 2
            
            @<ODIN_ATTRIBUTE_REF>private</ODIN_ATTRIBUTE_REF>
            <ODIN_PKG_PRIVATE_CONSTANT>PACKAGE_PRIVATE_CONST</ODIN_PKG_PRIVATE_CONSTANT> :: 1
            
            <ODIN_PKG_EXP_CONSTANT>PACKAGE_EXPORTED_CONST</ODIN_PKG_EXP_CONSTANT> :: 3
            
            // Struct
            @(<ODIN_ATTRIBUTE_REF>private</ODIN_ATTRIBUTE_REF>="file")
            <ODIN_FILE_PRIVATE_STRUCT>File_Private_Struct</ODIN_FILE_PRIVATE_STRUCT> :: struct {
            }
            
            @<ODIN_ATTRIBUTE_REF>private</ODIN_ATTRIBUTE_REF>
            <ODIN_PKG_PRIVATE_STRUCT>Package_Private_Struct</ODIN_PKG_PRIVATE_STRUCT> :: struct {
            }
            
            <ODIN_PKG_EXP_STRUCT>Package_Exported_Struct</ODIN_PKG_EXP_STRUCT> :: struct {
                <ODIN_STRUCT_FIELD>x</ODIN_STRUCT_FIELD>, <ODIN_STRUCT_FIELD>y</ODIN_STRUCT_FIELD>: <ODIN_BUILTIN_TYPE>i32</ODIN_BUILTIN_TYPE>,
                <ODIN_STRUCT_FIELD>p</ODIN_STRUCT_FIELD>: proc()
            }
            
            // Union
            @(<ODIN_ATTRIBUTE_REF>private</ODIN_ATTRIBUTE_REF>="file")
            <ODIN_FILE_PRIVATE_UNION>File_Private_Union</ODIN_FILE_PRIVATE_UNION> :: union {
            }
            
            @(<ODIN_ATTRIBUTE_REF>private</ODIN_ATTRIBUTE_REF>)
            <ODIN_PKG_PRIVATE_UNION>Package_Private_Union</ODIN_PKG_PRIVATE_UNION> :: union {
            }
            
            <ODIN_PKG_EXP_UNION>Package_Exported_Union</ODIN_PKG_EXP_UNION> :: union {
                <ODIN_BUILTIN_TYPE>i32</ODIN_BUILTIN_TYPE>, <ODIN_BUILTIN_TYPE>f32</ODIN_BUILTIN_TYPE>
            }
            
            // Bit Field
            @(<ODIN_ATTRIBUTE_REF>private</ODIN_ATTRIBUTE_REF>="file")
            <ODIN_FILE_PRIVATE_BIT_FIELD>File_Private_Bit_Field</ODIN_FILE_PRIVATE_BIT_FIELD> :: bit_field <ODIN_BUILTIN_TYPE>i32</ODIN_BUILTIN_TYPE> {
            
            }
            
            @<ODIN_ATTRIBUTE_REF>private</ODIN_ATTRIBUTE_REF>
            <ODIN_PKG_PRIVATE_BIT_FIELD>Package_Private_Bit_Field</ODIN_PKG_PRIVATE_BIT_FIELD> :: bit_field <ODIN_BUILTIN_TYPE>i32</ODIN_BUILTIN_TYPE> {
            }
            
            <ODIN_PKG_EXP_BIT_FIELD>Package_Exported_Bit_Field</ODIN_PKG_EXP_BIT_FIELD> :: bit_field <ODIN_BUILTIN_TYPE>i32</ODIN_BUILTIN_TYPE> {
                <ODIN_BIT_FIELD_FIELD>bit_field_field</ODIN_BIT_FIELD_FIELD>: <ODIN_BUILTIN_TYPE>i32</ODIN_BUILTIN_TYPE> | 16,
                <ODIN_BIT_FIELD_FIELD>y</ODIN_BIT_FIELD_FIELD>: <ODIN_BUILTIN_TYPE>i32</ODIN_BUILTIN_TYPE> | 16
            }
            
            // Enum
            @(<ODIN_ATTRIBUTE_REF>private</ODIN_ATTRIBUTE_REF>="file")
            <ODIN_FILE_PRIVATE_ENUM>File_Private_Enum</ODIN_FILE_PRIVATE_ENUM> :: enum {
                <ODIN_ENUM_FIELD>A</ODIN_ENUM_FIELD>
            }
            
            @<ODIN_ATTRIBUTE_REF>private</ODIN_ATTRIBUTE_REF>
            <ODIN_PKG_PRIVATE_ENUM>Package_Private_Enum</ODIN_PKG_PRIVATE_ENUM> :: enum {
                <ODIN_ENUM_FIELD>A</ODIN_ENUM_FIELD>
            }
            
            <ODIN_PKG_EXP_ENUM>Package_Exported_Enum</ODIN_PKG_EXP_ENUM> :: enum {
                <ODIN_ENUM_FIELD>ENUM_FIELD</ODIN_ENUM_FIELD>
            }
            
            // Parameters and parametric polymorphism
            <ODIN_PKG_EXP_PROC>poly_parameters</ODIN_PKG_EXP_PROC> :: proc($<ODIN_POLY_PARAMETER>T</ODIN_POLY_PARAMETER>: typeid, $<ODIN_POLY_PARAMETER>S</ODIN_POLY_PARAMETER>: $<ODIN_POLY_PARAMETER>Arr</ODIN_POLY_PARAMETER>/[dynamic]$<ODIN_POLY_PARAMETER>Elem</ODIN_POLY_PARAMETER>) {
                <ODIN_LOCAL_VARIABLE>x</ODIN_LOCAL_VARIABLE> := <ODIN_POLY_PARAMETER_REF>T</ODIN_POLY_PARAMETER_REF> { }
            }
            
            Struct_With_Poly :: struct($<ODIN_POLY_PARAMETER>T</ODIN_POLY_PARAMETER>: typeid, $<ODIN_POLY_PARAMETER>S</ODIN_POLY_PARAMETER>: int) {
                <ODIN_STRUCT_FIELD>x</ODIN_STRUCT_FIELD>: <ODIN_POLY_PARAMETER_REF>T</ODIN_POLY_PARAMETER_REF>,
                <ODIN_STRUCT_FIELD>y</ODIN_STRUCT_FIELD>: [<ODIN_POLY_PARAMETER_REF>S</ODIN_POLY_PARAMETER_REF>]i32
            }
            
            Union_With_Poly :: union($T: typeid) {
                i32, T
            }
            
            // Tag directives
            <ODIN_PKG_EXP_VARIABLE>CONFIGURED_VALUE</ODIN_PKG_EXP_VARIABLE> : <ODIN_BUILTIN_TYPE>string</ODIN_BUILTIN_TYPE> : <ODIN_DIRECTIVE_REF>#config</ODIN_DIRECTIVE_REF>(CONFIG_ARG, "VALUE")
            
            <ODIN_PKG_EXP_PROC>local_declarations</ODIN_PKG_EXP_PROC> :: proc() {
                <ODIN_LOCAL_ENUM>Local_Enum</ODIN_LOCAL_ENUM> :: enum {
                }
                <ODIN_LOCAL_STRUCT>Local_Struct</ODIN_LOCAL_STRUCT> :: struct {
                }
                <ODIN_LOCAL_UNION>Local_Union</ODIN_LOCAL_UNION> :: union {
                }
                <ODIN_LOCAL_BIT_FIELD>Local_Bit_Field</ODIN_LOCAL_BIT_FIELD> :: bit_field <ODIN_BUILTIN_TYPE>i32</ODIN_BUILTIN_TYPE> {
                }
            
                <ODIN_LOCAL_CONSTANT>LOCAL_CONST</ODIN_LOCAL_CONSTANT> :: 1
            
                <ODIN_LOCAL_VARIABLE>local_var</ODIN_LOCAL_VARIABLE> := 1
            }
            
            <ODIN_PKG_EXP_PROC>references</ODIN_PKG_EXP_PROC> :: proc() {
                // Built-in symbols
                {
                    if(<ODIN_BUILTIN_CONSTANT>ODIN_OS</ODIN_BUILTIN_CONSTANT> == .<ODIN_IMPLICIT_ENUM_FIELD_REF>Windows</ODIN_IMPLICIT_ENUM_FIELD_REF>) {
            
                    }
                    <ODIN_LOCAL_VARIABLE>slice</ODIN_LOCAL_VARIABLE> := <ODIN_BUILTIN_PROC>make</ODIN_BUILTIN_PROC>([]<ODIN_BUILTIN_TYPE>i32</ODIN_BUILTIN_TYPE>, 420)
                    <ODIN_LOCAL_VARIABLE>i</ODIN_LOCAL_VARIABLE> : <ODIN_BUILTIN_TYPE>i32</ODIN_BUILTIN_TYPE> = 1
                }
            
                // Static variables
                {
                    @<ODIN_ATTRIBUTE_REF>static</ODIN_ATTRIBUTE_REF>
                    <ODIN_STATIC_VARIABLE>static_x</ODIN_STATIC_VARIABLE> := 1
                    <ODIN_PACKAGE>fmt</ODIN_PACKAGE>.<ODIN_PKG_EXP_PROC>println</ODIN_PKG_EXP_PROC>(<ODIN_STATIC_VARIABLE_REF>static_x</ODIN_STATIC_VARIABLE_REF>)
                }
            
                // Context
                <ODIN_PACKAGE>fmt</ODIN_PACKAGE>.<ODIN_PKG_EXP_PROC>println</ODIN_PKG_EXP_PROC>(<ODIN_CONTEXT_PARAM_REF>context</ODIN_CONTEXT_PARAM_REF>.<ODIN_STRUCT_FIELD>temp_allocator</ODIN_STRUCT_FIELD>)
            
                // Allocator field
                {
                    <ODIN_LOCAL_VARIABLE>arr</ODIN_LOCAL_VARIABLE> := [dynamic]<ODIN_BUILTIN_TYPE>i32</ODIN_BUILTIN_TYPE> { }
                    <ODIN_LOCAL_VARIABLE>arr</ODIN_LOCAL_VARIABLE>.<ODIN_ALLOCATOR_FIELD_REF>allocator</ODIN_ALLOCATOR_FIELD_REF> = <ODIN_CONTEXT_PARAM_REF>context</ODIN_CONTEXT_PARAM_REF>.<ODIN_STRUCT_FIELD>temp_allocator</ODIN_STRUCT_FIELD>
                }
            
                // Swizzle fields
                {
                    <ODIN_LOCAL_VARIABLE>arr</ODIN_LOCAL_VARIABLE> := [3]<ODIN_BUILTIN_TYPE>i32</ODIN_BUILTIN_TYPE> { }
                    <ODIN_LOCAL_VARIABLE>arr</ODIN_LOCAL_VARIABLE>.<ODIN_SWIZZLE_FIELD_REF>x</ODIN_SWIZZLE_FIELD_REF> = <ODIN_LOCAL_VARIABLE>arr</ODIN_LOCAL_VARIABLE>.<ODIN_SWIZZLE_FIELD_REF>r</ODIN_SWIZZLE_FIELD_REF> + <ODIN_LOCAL_VARIABLE>arr</ODIN_LOCAL_VARIABLE>.<ODIN_SWIZZLE_FIELD_REF>g</ODIN_SWIZZLE_FIELD_REF> + <ODIN_LOCAL_VARIABLE>arr</ODIN_LOCAL_VARIABLE>.<ODIN_SWIZZLE_FIELD_REF>z</ODIN_SWIZZLE_FIELD_REF>
                }
            
                // Implicit enum reference
                {
                    <ODIN_LOCAL_VARIABLE>e</ODIN_LOCAL_VARIABLE> : <ODIN_PKG_EXP_ENUM_REF>Package_Exported_Enum</ODIN_PKG_EXP_ENUM_REF>
                    <ODIN_LOCAL_VARIABLE>e</ODIN_LOCAL_VARIABLE> = .<ODIN_IMPLICIT_ENUM_FIELD_REF>ENUM_FIELD</ODIN_IMPLICIT_ENUM_FIELD_REF>
                }
            
                // Fields brought into scope through "using"
                {
                    {
                        using <ODIN_LOCAL_VARIABLE>s</ODIN_LOCAL_VARIABLE> := <ODIN_PKG_EXP_STRUCT_REF>Package_Exported_Struct</ODIN_PKG_EXP_STRUCT_REF> { }
                        <ODIN_LOCAL_VARIABLE>t</ODIN_LOCAL_VARIABLE> := <ODIN_USING_STRUCT_FIELD_REF>x</ODIN_USING_STRUCT_FIELD_REF> + <ODIN_USING_STRUCT_FIELD_REF>y</ODIN_USING_STRUCT_FIELD_REF>
                    }
                    {
                        using <ODIN_PKG_EXP_ENUM_REF>Package_Exported_Enum</ODIN_PKG_EXP_ENUM_REF>
                        <ODIN_LOCAL_VARIABLE>e</ODIN_LOCAL_VARIABLE> := <ODIN_USING_ENUM_FIELD_REF>ENUM_FIELD</ODIN_USING_ENUM_FIELD_REF>
                    }
                }
            
                // Variable references
                {
                    <ODIN_LOCAL_VARIABLE>file_private_var_ref</ODIN_LOCAL_VARIABLE> := <ODIN_FILE_PRIVATE_VARIABLE_REF>file_private_var</ODIN_FILE_PRIVATE_VARIABLE_REF>
                    <ODIN_LOCAL_VARIABLE>package_private_var_ref</ODIN_LOCAL_VARIABLE> := <ODIN_PKG_PRIVATE_VARIABLE_REF>package_private_var</ODIN_PKG_PRIVATE_VARIABLE_REF>
                    <ODIN_LOCAL_VARIABLE>package_exported_var_ref</ODIN_LOCAL_VARIABLE> := <ODIN_PKG_EXP_VARIABLE_REF>package_exported_var</ODIN_PKG_EXP_VARIABLE_REF>
                }
            
                // Struct references
                {
                    <ODIN_LOCAL_VARIABLE>file_private_struct</ODIN_LOCAL_VARIABLE> := <ODIN_FILE_PRIVATE_STRUCT_REF>File_Private_Struct</ODIN_FILE_PRIVATE_STRUCT_REF> { }
                    <ODIN_LOCAL_VARIABLE>package_private_struct</ODIN_LOCAL_VARIABLE> := <ODIN_PKG_PRIVATE_STRUCT_REF>Package_Private_Struct</ODIN_PKG_PRIVATE_STRUCT_REF> { }
                    <ODIN_LOCAL_VARIABLE>package_exported_struct</ODIN_LOCAL_VARIABLE> := <ODIN_PKG_EXP_STRUCT_REF>Package_Exported_Struct</ODIN_PKG_EXP_STRUCT_REF> { }
            
                    <ODIN_PACKAGE>fmt</ODIN_PACKAGE>.<ODIN_PKG_EXP_PROC>println</ODIN_PKG_EXP_PROC>(<ODIN_LOCAL_VARIABLE>package_exported_struct</ODIN_LOCAL_VARIABLE>.<ODIN_STRUCT_FIELD>x</ODIN_STRUCT_FIELD>)
                }
            
                // Union references
                {
                    <ODIN_LOCAL_VARIABLE>file_private_union</ODIN_LOCAL_VARIABLE> : <ODIN_FILE_PRIVATE_UNION_REF>File_Private_Union</ODIN_FILE_PRIVATE_UNION_REF>
                    <ODIN_LOCAL_VARIABLE>package_private_union</ODIN_LOCAL_VARIABLE> : <ODIN_PKG_PRIVATE_UNION_REF>Package_Private_Union</ODIN_PKG_PRIVATE_UNION_REF>
                    <ODIN_LOCAL_VARIABLE>package_exported_union</ODIN_LOCAL_VARIABLE> : <ODIN_PKG_EXP_UNION_REF>Package_Exported_Union</ODIN_PKG_EXP_UNION_REF>
                }
            
                // BitField references
                {
                    <ODIN_LOCAL_VARIABLE>file_private_bit_field</ODIN_LOCAL_VARIABLE> := <ODIN_FILE_PRIVATE_BIT_FIELD_REF>File_Private_Bit_Field</ODIN_FILE_PRIVATE_BIT_FIELD_REF> { }
                    <ODIN_LOCAL_VARIABLE>package_private_bit_field</ODIN_LOCAL_VARIABLE> := <ODIN_PKG_PRIVATE_BIT_FIELD_REF>Package_Private_Bit_Field</ODIN_PKG_PRIVATE_BIT_FIELD_REF> { }
                    <ODIN_LOCAL_VARIABLE>package_exported_bit_field</ODIN_LOCAL_VARIABLE> := <ODIN_PKG_EXP_BIT_FIELD_REF>Package_Exported_Bit_Field</ODIN_PKG_EXP_BIT_FIELD_REF> { }
            
                    <ODIN_PACKAGE>fmt</ODIN_PACKAGE>.<ODIN_PKG_EXP_PROC>println</ODIN_PKG_EXP_PROC>(<ODIN_LOCAL_VARIABLE>package_exported_bit_field</ODIN_LOCAL_VARIABLE>.<ODIN_BIT_FIELD_FIELD>bit_field_field</ODIN_BIT_FIELD_FIELD>)
                }
            
                // Enum references
                {
                    <ODIN_LOCAL_VARIABLE>file_private_enum</ODIN_LOCAL_VARIABLE> := <ODIN_FILE_PRIVATE_ENUM_REF>File_Private_Enum</ODIN_FILE_PRIVATE_ENUM_REF>.<ODIN_ENUM_FIELD>A</ODIN_ENUM_FIELD>
                    <ODIN_LOCAL_VARIABLE>package_private_enum</ODIN_LOCAL_VARIABLE> := <ODIN_PKG_PRIVATE_ENUM_REF>Package_Private_Enum</ODIN_PKG_PRIVATE_ENUM_REF>.<ODIN_ENUM_FIELD>A</ODIN_ENUM_FIELD>
                    <ODIN_LOCAL_VARIABLE>package_exported_enum</ODIN_LOCAL_VARIABLE> := <ODIN_PKG_EXP_ENUM_REF>Package_Exported_Enum</ODIN_PKG_EXP_ENUM_REF>.<ODIN_ENUM_FIELD>ENUM_FIELD</ODIN_ENUM_FIELD>
                }
            
                // Call expressions
                {
                    <ODIN_LOCAL_VARIABLE>p</ODIN_LOCAL_VARIABLE> := proc() {
                    }
                    <ODIN_LOCAL_VARIABLE>p</ODIN_LOCAL_VARIABLE>()
            
                    <ODIN_FILE_PRIVATE_VAR_CALL>file_private_var_proc</ODIN_FILE_PRIVATE_VAR_CALL>()
                    <ODIN_PKG_EXP_VAR_CALL>package_exported_var_proc</ODIN_PKG_EXP_VAR_CALL>()
                    <ODIN_PKG_PRIVATE_VAR_CALL>package_private_var_proc</ODIN_PKG_PRIVATE_VAR_CALL>()
            
                    <ODIN_LOCAL_PROC>local_proc</ODIN_LOCAL_PROC> :: proc(<ODIN_LOCAL_VARIABLE>parameter_proc</ODIN_LOCAL_VARIABLE>: proc()) {
                        <ODIN_PARAMETER_CALL>parameter_proc</ODIN_PARAMETER_CALL>()
                    }
            
                    <ODIN_LOCAL_VARIABLE>s</ODIN_LOCAL_VARIABLE> :: <ODIN_PKG_EXP_STRUCT_REF>Package_Exported_Struct</ODIN_PKG_EXP_STRUCT_REF>{}
                    <ODIN_LOCAL_VARIABLE>s</ODIN_LOCAL_VARIABLE>.<ODIN_STRUCT_FIELD_CALL>p</ODIN_STRUCT_FIELD_CALL>()
            
                    <ODIN_FILE_PRIVATE_PROC_CALL>file_private_proc</ODIN_FILE_PRIVATE_PROC_CALL>()
                    <ODIN_PKG_PRIVATE_PROC_CALL>package_private_proc</ODIN_PKG_PRIVATE_PROC_CALL>()
                    <ODIN_PKG_EXP_PROC_CALL>package_exported_proc</ODIN_PKG_EXP_PROC_CALL>()
                }
            }
            
            """;

    AttributesDescriptor[] ATTRIBUTES_DESCRIPTORS = new AttributesDescriptor[]{
            new AttributesDescriptor("Bad character", ODIN_BAD_CHARACTER),

            new AttributesDescriptor("Built-in symbols//Built-in procedure", ODIN_BUILTIN_PROC),
            new AttributesDescriptor("Built-in symbols//Built-in variable", ODIN_BUILTIN_VAR),
            new AttributesDescriptor("Built-in symbols//Built-in type", ODIN_BUILTIN_TYPE),
            new AttributesDescriptor("Built-in symbols//Built-in procedure call", ODIN_BUILTIN_PROC_CALL),
            new AttributesDescriptor("Built-in symbols//Built-in constant", ODIN_BUILTIN_CONSTANT),

            new AttributesDescriptor("Package", ODIN_PACKAGE),

            new AttributesDescriptor("Attributes//Attribute identifier", ODIN_ATTRIBUTE_REF),
            new AttributesDescriptor("Attributes//'@' character", ODIN_AT),

            new AttributesDescriptor("Directive", ODIN_DIRECTIVE_REF),

            // Braces and operators
            new AttributesDescriptor("Braces and operators//Braces", ODIN_BRACES),
            new AttributesDescriptor("Braces and operators//Brackets", ODIN_BRACKETS),
            new AttributesDescriptor("Braces and operators//Colon", ODIN_COLON),
            new AttributesDescriptor("Braces and operators//Comma", ODIN_COMMA),
            new AttributesDescriptor("Braces and operators//Dot", ODIN_DOT),
            new AttributesDescriptor("Braces and operators//Operator", ODIN_OPERATOR),
            new AttributesDescriptor("Braces and operators//Parentheses", ODIN_PARENTHESES),
            new AttributesDescriptor("Braces and operators//Semicolon", ODIN_SEMICOLON),

            // Comments
            new AttributesDescriptor("Comments//Block comment", ODIN_BLOCK_COMMENT),
            new AttributesDescriptor("Comments//Comment reference", ODIN_COMMENT_REFERENCE),
            new AttributesDescriptor("Comments//Line comment", ODIN_LINE_COMMENT),

            // Declarations
            new AttributesDescriptor("Declarations//Label", ODIN_LABEL),
            new AttributesDescriptor("Declarations//Parameter", ODIN_PARAMETER),


            // Keywords
            new AttributesDescriptor("Keyword", ODIN_KEYWORD),

            // Identifiers
            new AttributesDescriptor("Identifier", ODIN_IDENTIFIER),

            // Constants
            new AttributesDescriptor("Declarations//Constants//Local constant", ODIN_LOCAL_CONSTANT),
            new AttributesDescriptor("Declarations//Constants//Package exported constant", ODIN_PKG_EXP_CONSTANT),
            new AttributesDescriptor("Declarations//Constants//File private constant", ODIN_FILE_PRIVATE_CONSTANT),
            new AttributesDescriptor("Declarations//Constants//Package private constant", ODIN_PKG_PRIVATE_CONSTANT),

            // Procedures
            new AttributesDescriptor("Declarations//Procedures//Package exported procedure", ODIN_PKG_EXP_PROC),
            new AttributesDescriptor("Declarations//Procedures//File private procedure", ODIN_FILE_PRIVATE_PROC),
            new AttributesDescriptor("Declarations//Procedures//Package private procedure", ODIN_PKG_PRIVATE_PROC),
            new AttributesDescriptor("Declarations//Procedures//Local procedure", ODIN_LOCAL_PROC),

            // Types
            new AttributesDescriptor("Declarations//Struct//Package exported struct", ODIN_PKG_EXP_STRUCT),
            new AttributesDescriptor("Declarations//Struct//File private struct", ODIN_FILE_PRIVATE_STRUCT),
            new AttributesDescriptor("Declarations//Struct//Package private struct", ODIN_PKG_PRIVATE_STRUCT),
            new AttributesDescriptor("Declarations//Struct//Local struct", ODIN_LOCAL_STRUCT),

            new AttributesDescriptor("Declarations//Union//Package exported union", ODIN_PKG_EXP_UNION),
            new AttributesDescriptor("Declarations//Union//File private union", ODIN_FILE_PRIVATE_UNION),
            new AttributesDescriptor("Declarations//Union//Package private union", ODIN_PKG_PRIVATE_UNION),
            new AttributesDescriptor("Declarations//Union//Local union", ODIN_LOCAL_UNION),

            new AttributesDescriptor("Declarations//Enum//Package exported enum", ODIN_PKG_EXP_ENUM),
            new AttributesDescriptor("Declarations//Enum//File private enum", ODIN_FILE_PRIVATE_ENUM),
            new AttributesDescriptor("Declarations//Enum//Package private enum", ODIN_PKG_PRIVATE_ENUM),
            new AttributesDescriptor("Declarations//Enum//Local enum", ODIN_LOCAL_ENUM),

            new AttributesDescriptor("Declarations//Bit field//Package exported bit field", ODIN_PKG_EXP_BIT_FIELD),
            new AttributesDescriptor("Declarations//Bit field//File private bit field", ODIN_FILE_PRIVATE_BIT_FIELD),
            new AttributesDescriptor("Declarations//Bit field//Package private bit field", ODIN_PKG_PRIVATE_BIT_FIELD),
            new AttributesDescriptor("Declarations//Bit field//Local bit field", ODIN_LOCAL_BIT_FIELD),

//            new AttributesDescriptor("Declarations//Type alias//Package exported type alias", ODIN_PKG_EXP_TYPE_ALIAS),
//            new AttributesDescriptor("Declarations//Type alias//File private type alias", ODIN_FILE_PRIVATE_TYPE_ALIAS),
//            new AttributesDescriptor("Declarations//Type alias//Package private type alias", ODIN_PKG_PRIVATE_TYPE_ALIAS),
//            new AttributesDescriptor("Declarations//Type alias//Local type alias", ODIN_LOCAL_TYPE_ALIAS),

            new AttributesDescriptor("Declarations//Types//Polymorphic parameter", ODIN_POLY_PARAMETER),

            // Variables and Fields
            new AttributesDescriptor("Declarations//Variables//Local variable", ODIN_LOCAL_VARIABLE),
            new AttributesDescriptor("Declarations//Variables//Static variable", ODIN_STATIC_VARIABLE),
            new AttributesDescriptor("Declarations//Variables//Package exported global variable", ODIN_PKG_EXP_VARIABLE),
            new AttributesDescriptor("Declarations//Variables//File private global variable", ODIN_FILE_PRIVATE_VARIABLE),
            new AttributesDescriptor("Declarations//Variables//Package private global variable", ODIN_PKG_PRIVATE_VARIABLE),
            new AttributesDescriptor("Declarations//Variables//Variable reassignment", ODIN_VAR_REASSIGNMENT),
            new AttributesDescriptor("Declarations//Variables//Shadowing variable", ODIN_SHADOWING_VARIABLE),

            new AttributesDescriptor("Declarations//Fields//Struct field", ODIN_STRUCT_FIELD),
            new AttributesDescriptor("Declarations//Fields//Bit field", ODIN_BIT_FIELD_FIELD),
            new AttributesDescriptor("Declarations//Fields//Enum field", ODIN_ENUM_FIELD),


            // Function calls
            new AttributesDescriptor("Procedure calls//Local procedure call", ODIN_LOCAL_PROC_CALL),
            new AttributesDescriptor("Procedure calls//Package exported procedure call", ODIN_PKG_EXP_PROC_CALL),
            new AttributesDescriptor("Procedure calls//Package private procedure call", ODIN_PKG_PRIVATE_PROC_CALL),
            new AttributesDescriptor("Procedure calls//File private procedure call", ODIN_FILE_PRIVATE_PROC_CALL),
            new AttributesDescriptor("Procedure calls//Variable call", ODIN_VAR_CALL),
            new AttributesDescriptor("Procedure calls//Local variable call", ODIN_LOCAL_VAR_CALL),
            new AttributesDescriptor("Procedure calls//Package exported global variable call", ODIN_PKG_EXP_VAR_CALL),
            new AttributesDescriptor("Procedure calls//Package private global variable call", ODIN_PKG_PRIVATE_VAR_CALL),
            new AttributesDescriptor("Procedure calls//File private global variable call", ODIN_FILE_PRIVATE_VAR_CALL),
            new AttributesDescriptor("Procedure calls//Struct field call", ODIN_STRUCT_FIELD_CALL),
            new AttributesDescriptor("Procedure calls//Parameter call", ODIN_PARAMETER_CALL),
//            new AttributesDescriptor("Procedure calls//Fake method call", OdinSyntaxTextAttributes.ODIN_FAKE_METHOD_CALL),

            // References
            new AttributesDescriptor("References//Struct//Package exported struct reference", ODIN_PKG_EXP_STRUCT_REF),
            new AttributesDescriptor("References//Struct//Package private struct reference", ODIN_PKG_PRIVATE_STRUCT_REF),
            new AttributesDescriptor("References//Struct//File private struct reference", ODIN_FILE_PRIVATE_STRUCT_REF),
            new AttributesDescriptor("References//Struct//Local struct reference", ODIN_LOCAL_STRUCT_REF),

            new AttributesDescriptor("References//Union//Package exported union reference", ODIN_PKG_EXP_UNION_REF),
            new AttributesDescriptor("References//Union//Package private union reference", ODIN_PKG_PRIVATE_UNION_REF),
            new AttributesDescriptor("References//Union//File private union reference", ODIN_FILE_PRIVATE_UNION_REF),
            new AttributesDescriptor("References//Union//Local union reference", ODIN_LOCAL_UNION_REF),

            new AttributesDescriptor("References//Bit field//Package exported bit field reference", ODIN_PKG_EXP_BIT_FIELD_REF),
            new AttributesDescriptor("References//Bit field//File private bit field reference", ODIN_FILE_PRIVATE_BIT_FIELD_REF),
            new AttributesDescriptor("References//Bit field//Package private bit field reference", ODIN_PKG_PRIVATE_BIT_FIELD_REF),
            new AttributesDescriptor("References//Bit field//Local bit field reference", ODIN_LOCAL_BIT_FIELD_REF),

            new AttributesDescriptor("References//Enum//Package exported enum reference", ODIN_PKG_EXP_ENUM_REF),
            new AttributesDescriptor("References//Enum//Package private enum reference", ODIN_PKG_PRIVATE_ENUM_REF),
            new AttributesDescriptor("References//Enum//File private enum reference", ODIN_FILE_PRIVATE_ENUM_REF),
            new AttributesDescriptor("References//Enum//Local enum reference", ODIN_LOCAL_ENUM_REF),

            new AttributesDescriptor("References//Fields//Struct field", ODIN_STRUCT_FIELD_REF),
            new AttributesDescriptor("References//Fields//Bit field", ODIN_BIT_FIELD_FIELD_REF),
            new AttributesDescriptor("References//Fields//Enum field", ODIN_ENUM_FIELD_REF),
            new AttributesDescriptor("References//Fields//Allocator field", ODIN_ALLOCATOR_FIELD_REF),
            new AttributesDescriptor("References//Fields//Implicit Enum field", ODIN_IMPLICIT_ENUM_FIELD_REF),
            new AttributesDescriptor("References//Fields//Enum field through using", ODIN_USING_ENUM_FIELD_REF),
            new AttributesDescriptor("References//Fields//Struct field through using", ODIN_USING_STRUCT_FIELD_REF),
            new AttributesDescriptor("References//Fields//Swizzle field", ODIN_SWIZZLE_FIELD_REF),

//            new AttributesDescriptor("References//Type alias//Package exported type alias reference", OdinSyntaxTextAttributes.ODIN_PKG_EXP_TYPE_ALIAS_REF),
//            new AttributesDescriptor("References//Type alias//File private type alias reference", OdinSyntaxTextAttributes.ODIN_FILE_PRIVATE_TYPE_ALIAS_REF),
//            new AttributesDescriptor("References//Type alias//Package private type alias reference", OdinSyntaxTextAttributes.ODIN_PKG_PRIVATE_TYPE_ALIAS_REF),
//            new AttributesDescriptor("References//Type alias//Local type alias reference", OdinSyntaxTextAttributes.ODIN_LOCAL_TYPE_ALIAS_REF),

            new AttributesDescriptor("References//Polymorphic parameter", ODIN_POLY_PARAMETER_REF),
            new AttributesDescriptor("References//Procedure parameter", ODIN_PARAMETER_REF),
            new AttributesDescriptor("References//Context parameter", ODIN_CONTEXT_PARAM_REF),

            // String and Escape
            new AttributesDescriptor("String and Escape//Text", ODIN_TEXT),
            new AttributesDescriptor("String and Escape//Valid escape", ODIN_VALID_ESCAPE),
            new AttributesDescriptor("String and Escape//Invalid escape", ODIN_INVALID_ESCAPE),

            new AttributesDescriptor("Numbers", ODIN_NUMBER)
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
        if (ADDITIONAL_TAGS.isEmpty()) {
            ADDITIONAL_TAGS.put("ODIN_KEYWORD", ODIN_KEYWORD);
            ADDITIONAL_TAGS.put("ODIN_IDENTIFIER", ODIN_IDENTIFIER);
            ADDITIONAL_TAGS.put("ODIN_BAD_CHARACTER", ODIN_BAD_CHARACTER);
            ADDITIONAL_TAGS.put("ODIN_BIT_FIELD", ODIN_BIT_FIELD);
            ADDITIONAL_TAGS.put("ODIN_STRUCT_FIELD", ODIN_STRUCT_FIELD);
            ADDITIONAL_TAGS.put("ODIN_BIT_FIELD_FIELD", ODIN_BIT_FIELD_FIELD);
            ADDITIONAL_TAGS.put("ODIN_ENUM_FIELD", ODIN_ENUM_FIELD);
            ADDITIONAL_TAGS.put("ODIN_PARAMETER", ODIN_PARAMETER);
            ADDITIONAL_TAGS.put("ODIN_PARAMETER_REF", ODIN_PARAMETER_REF);
            ADDITIONAL_TAGS.put("ODIN_CONTEXT_PARAM_REF", ODIN_CONTEXT_PARAM_REF);
            ADDITIONAL_TAGS.put("ODIN_CONSTANT", ODIN_CONSTANT);
            ADDITIONAL_TAGS.put("ODIN_PROC_DECLARATION", ODIN_PROC_DECLARATION);
            ADDITIONAL_TAGS.put("ODIN_VALID_STRING_ESCAPE", ODIN_VALID_STRING_ESCAPE);
            ADDITIONAL_TAGS.put("ODIN_BRACES", ODIN_BRACES);
            ADDITIONAL_TAGS.put("ODIN_BRACKETS", ODIN_BRACKETS);
            ADDITIONAL_TAGS.put("ODIN_PARENTHESES", ODIN_PARENTHESES);
            ADDITIONAL_TAGS.put("ODIN_DOT", ODIN_DOT);
            ADDITIONAL_TAGS.put("ODIN_OPERATOR", ODIN_OPERATOR);
            ADDITIONAL_TAGS.put("ODIN_SEMICOLON", ODIN_SEMICOLON);
            ADDITIONAL_TAGS.put("ODIN_COMMA", ODIN_COMMA);
            ADDITIONAL_TAGS.put("ODIN_COLON", ODIN_COLON);
            ADDITIONAL_TAGS.put("ODIN_AT", ODIN_AT);
            ADDITIONAL_TAGS.put("ODIN_BLOCK_COMMENT", ODIN_BLOCK_COMMENT);
            ADDITIONAL_TAGS.put("ODIN_COMMENT_REFERENCE", ODIN_COMMENT_REFERENCE);
            ADDITIONAL_TAGS.put("ODIN_LINE_COMMENT", ODIN_LINE_COMMENT);
            ADDITIONAL_TAGS.put("ODIN_PACKAGE", ODIN_PACKAGE);
            ADDITIONAL_TAGS.put("ODIN_LABEL", ODIN_LABEL);
            ADDITIONAL_TAGS.put("ODIN_LOCAL_CONSTANT", ODIN_LOCAL_CONSTANT);
            ADDITIONAL_TAGS.put("ODIN_PKG_EXP_CONSTANT", ODIN_PKG_EXP_CONSTANT);
            ADDITIONAL_TAGS.put("ODIN_FILE_PRIVATE_CONSTANT", ODIN_FILE_PRIVATE_CONSTANT);
            ADDITIONAL_TAGS.put("ODIN_PKG_PRIVATE_CONSTANT", ODIN_PKG_PRIVATE_CONSTANT);
            ADDITIONAL_TAGS.put("ODIN_PKG_EXP_PROC", ODIN_PKG_EXP_PROC);
            ADDITIONAL_TAGS.put("ODIN_FILE_PRIVATE_PROC", ODIN_FILE_PRIVATE_PROC);
            ADDITIONAL_TAGS.put("ODIN_PKG_PRIVATE_PROC", ODIN_PKG_PRIVATE_PROC);
            ADDITIONAL_TAGS.put("ODIN_LOCAL_PROC", ODIN_LOCAL_PROC);
            ADDITIONAL_TAGS.put("ODIN_FOREIGN_PROC", ODIN_FOREIGN_PROC);
            ADDITIONAL_TAGS.put("ODIN_PKG_EXP_STRUCT", ODIN_PKG_EXP_STRUCT);
            ADDITIONAL_TAGS.put("ODIN_FILE_PRIVATE_STRUCT", ODIN_FILE_PRIVATE_STRUCT);
            ADDITIONAL_TAGS.put("ODIN_PKG_PRIVATE_STRUCT", ODIN_PKG_PRIVATE_STRUCT);
            ADDITIONAL_TAGS.put("ODIN_LOCAL_STRUCT", ODIN_LOCAL_STRUCT);
            ADDITIONAL_TAGS.put("ODIN_PKG_EXP_UNION", ODIN_PKG_EXP_UNION);
            ADDITIONAL_TAGS.put("ODIN_FILE_PRIVATE_UNION", ODIN_FILE_PRIVATE_UNION);
            ADDITIONAL_TAGS.put("ODIN_PKG_PRIVATE_UNION", ODIN_PKG_PRIVATE_UNION);
            ADDITIONAL_TAGS.put("ODIN_LOCAL_UNION", ODIN_LOCAL_UNION);
            ADDITIONAL_TAGS.put("ODIN_PKG_EXP_ENUM", ODIN_PKG_EXP_ENUM);
            ADDITIONAL_TAGS.put("ODIN_FILE_PRIVATE_ENUM", ODIN_FILE_PRIVATE_ENUM);
            ADDITIONAL_TAGS.put("ODIN_PKG_PRIVATE_ENUM", ODIN_PKG_PRIVATE_ENUM);
            ADDITIONAL_TAGS.put("ODIN_LOCAL_ENUM", ODIN_LOCAL_ENUM);
            ADDITIONAL_TAGS.put("ODIN_PKG_EXP_BIT_FIELD", ODIN_PKG_EXP_BIT_FIELD);
            ADDITIONAL_TAGS.put("ODIN_FILE_PRIVATE_BIT_FIELD", ODIN_FILE_PRIVATE_BIT_FIELD);
            ADDITIONAL_TAGS.put("ODIN_PKG_PRIVATE_BIT_FIELD", ODIN_PKG_PRIVATE_BIT_FIELD);
            ADDITIONAL_TAGS.put("ODIN_LOCAL_BIT_FIELD", ODIN_LOCAL_BIT_FIELD);
//            ADDITIONAL_TAGS.put("ODIN_PKG_EXP_TYPE_ALIAS", ODIN_PKG_EXP_TYPE_ALIAS);
//            ADDITIONAL_TAGS.put("ODIN_FILE_PRIVATE_TYPE_ALIAS", ODIN_FILE_PRIVATE_TYPE_ALIAS);
//            ADDITIONAL_TAGS.put("ODIN_PKG_PRIVATE_TYPE_ALIAS", ODIN_PKG_PRIVATE_TYPE_ALIAS);
//            ADDITIONAL_TAGS.put("ODIN_LOCAL_TYPE_ALIAS", ODIN_LOCAL_TYPE_ALIAS);
            ADDITIONAL_TAGS.put("ODIN_LOCAL_VARIABLE", ODIN_LOCAL_VARIABLE);
            ADDITIONAL_TAGS.put("ODIN_PKG_EXP_VARIABLE", ODIN_PKG_EXP_VARIABLE);
            ADDITIONAL_TAGS.put("ODIN_FILE_PRIVATE_VARIABLE", ODIN_FILE_PRIVATE_VARIABLE);
            ADDITIONAL_TAGS.put("ODIN_PKG_PRIVATE_VARIABLE", ODIN_PKG_PRIVATE_VARIABLE);
            ADDITIONAL_TAGS.put("ODIN_VAR_REASSIGNMENT", ODIN_VAR_REASSIGNMENT);
            ADDITIONAL_TAGS.put("ODIN_SHADOWING_VARIABLE", ODIN_SHADOWING_VARIABLE);
            ADDITIONAL_TAGS.put("ODIN_STATIC_VARIABLE", ODIN_STATIC_VARIABLE);
            ADDITIONAL_TAGS.put("ODIN_STRUCT_FIELD_REF", ODIN_STRUCT_FIELD_REF);
            ADDITIONAL_TAGS.put("ODIN_BIT_FIELD_FIELD_REF", ODIN_BIT_FIELD_FIELD_REF);
            ADDITIONAL_TAGS.put("ODIN_ENUM_FIELD_REF", ODIN_ENUM_FIELD_REF);
            ADDITIONAL_TAGS.put("ODIN_ALLOCATOR_FIELD_REF", ODIN_ALLOCATOR_FIELD_REF);
            ADDITIONAL_TAGS.put("ODIN_IMPLICIT_ENUM_FIELD_REF", ODIN_IMPLICIT_ENUM_FIELD_REF);
            ADDITIONAL_TAGS.put("ODIN_SWIZZLE_FIELD_REF", ODIN_SWIZZLE_FIELD_REF);
            ADDITIONAL_TAGS.put("ODIN_USING_STRUCT_FIELD_REF", ODIN_USING_STRUCT_FIELD_REF);
            ADDITIONAL_TAGS.put("ODIN_USING_ENUM_FIELD_REF", ODIN_USING_ENUM_FIELD_REF);
            ADDITIONAL_TAGS.put("ODIN_STATIC_VARIABLE_REF", ODIN_STATIC_VARIABLE_REF);
            ADDITIONAL_TAGS.put("ODIN_PKG_EXP_PROC_CALL", ODIN_PKG_EXP_PROC_CALL);
            ADDITIONAL_TAGS.put("ODIN_LOCAL_PROC_CALL", ODIN_LOCAL_PROC_CALL);
            ADDITIONAL_TAGS.put("ODIN_FOREIGN_PROC_CALL", ODIN_FOREIGN_PROC_CALL);
            ADDITIONAL_TAGS.put("ODIN_PKG_PRIVATE_PROC_CALL", ODIN_PKG_PRIVATE_PROC_CALL);
            ADDITIONAL_TAGS.put("ODIN_FILE_PRIVATE_PROC_CALL", ODIN_FILE_PRIVATE_PROC_CALL);
            ADDITIONAL_TAGS.put("ODIN_PKG_EXP_STRUCT_REF", ODIN_PKG_EXP_STRUCT_REF);
            ADDITIONAL_TAGS.put("ODIN_PKG_PRIVATE_STRUCT_REF", ODIN_PKG_PRIVATE_STRUCT_REF);
            ADDITIONAL_TAGS.put("ODIN_FILE_PRIVATE_STRUCT_REF", ODIN_FILE_PRIVATE_STRUCT_REF);
            ADDITIONAL_TAGS.put("ODIN_LOCAL_STRUCT_REF", ODIN_LOCAL_STRUCT_REF);
            ADDITIONAL_TAGS.put("ODIN_PKG_EXP_UNION_REF", ODIN_PKG_EXP_UNION_REF);
            ADDITIONAL_TAGS.put("ODIN_PKG_PRIVATE_UNION_REF", ODIN_PKG_PRIVATE_UNION_REF);
            ADDITIONAL_TAGS.put("ODIN_FILE_PRIVATE_UNION_REF", ODIN_FILE_PRIVATE_UNION_REF);
            ADDITIONAL_TAGS.put("ODIN_LOCAL_UNION_REF", ODIN_LOCAL_UNION_REF);
            ADDITIONAL_TAGS.put("ODIN_PKG_EXP_BIT_FIELD_REF", ODIN_PKG_EXP_BIT_FIELD_REF);
            ADDITIONAL_TAGS.put("ODIN_FILE_PRIVATE_BIT_FIELD_REF", ODIN_FILE_PRIVATE_BIT_FIELD_REF);
            ADDITIONAL_TAGS.put("ODIN_PKG_PRIVATE_BIT_FIELD_REF", ODIN_PKG_PRIVATE_BIT_FIELD_REF);
            ADDITIONAL_TAGS.put("ODIN_LOCAL_BIT_FIELD_REF", ODIN_LOCAL_BIT_FIELD_REF);
            ADDITIONAL_TAGS.put("ODIN_PKG_EXP_ENUM_REF", ODIN_PKG_EXP_ENUM_REF);
            ADDITIONAL_TAGS.put("ODIN_FILE_PRIVATE_ENUM_REF", ODIN_FILE_PRIVATE_ENUM_REF);
            ADDITIONAL_TAGS.put("ODIN_PKG_PRIVATE_ENUM_REF", ODIN_PKG_PRIVATE_ENUM_REF);
            ADDITIONAL_TAGS.put("ODIN_LOCAL_ENUM_REF", ODIN_LOCAL_ENUM_REF);
            ADDITIONAL_TAGS.put("ODIN_BUILTIN_PROC", ODIN_BUILTIN_PROC);
            ADDITIONAL_TAGS.put("ODIN_BUILTIN_PROC_CALL", ODIN_BUILTIN_PROC_CALL);
            ADDITIONAL_TAGS.put("ODIN_BUILTIN_VAR", ODIN_BUILTIN_VAR);
            ADDITIONAL_TAGS.put("ODIN_BUILTIN_CONSTANT", ODIN_BUILTIN_CONSTANT);
            ADDITIONAL_TAGS.put("ODIN_BUILTIN_TYPE", ODIN_BUILTIN_TYPE);
//        ADDITIONAL_TAGS.put("ODIN_PKG_EXP_TYPE_ALIAS_REF", ODIN_PKG_EXP_TYPE_ALIAS_REF);
//        ADDITIONAL_TAGS.put("ODIN_FILE_PRIVATE_TYPE_ALIAS_REF", ODIN_FILE_PRIVATE_TYPE_ALIAS_REF);
//        ADDITIONAL_TAGS.put("ODIN_PKG_PRIVATE_TYPE_ALIAS_REF", ODIN_PKG_PRIVATE_TYPE_ALIAS_REF);
//        ADDITIONAL_TAGS.put("ODIN_LOCAL_TYPE_ALIAS_REF", ODIN_LOCAL_TYPE_ALIAS_REF);
            ADDITIONAL_TAGS.put("ODIN_VAR_CALL", ODIN_VAR_CALL);
            ADDITIONAL_TAGS.put("ODIN_LOCAL_VAR_CALL", ODIN_LOCAL_VAR_CALL);
            ADDITIONAL_TAGS.put("ODIN_PKG_EXP_VAR_CALL", ODIN_PKG_EXP_VAR_CALL);
            ADDITIONAL_TAGS.put("ODIN_PKG_PRIVATE_VAR_CALL", ODIN_PKG_PRIVATE_VAR_CALL);
            ADDITIONAL_TAGS.put("ODIN_FILE_PRIVATE_VAR_CALL", ODIN_FILE_PRIVATE_VAR_CALL);
            ADDITIONAL_TAGS.put("ODIN_STRUCT_FIELD_CALL", ODIN_STRUCT_FIELD_CALL);
            ADDITIONAL_TAGS.put("ODIN_PARAMETER_CALL", ODIN_PARAMETER_CALL);
//        ADDITIONAL_TAGS.put("ODIN_FAKE_METHOD_CALL", ODIN_FAKE_METHOD_CALL);
            ADDITIONAL_TAGS.put("ODIN_PKG_EXP_VARIABLE_REF", ODIN_PKG_EXP_VARIABLE_REF);
            ADDITIONAL_TAGS.put("ODIN_FILE_PRIVATE_VARIABLE_REF", ODIN_FILE_PRIVATE_VARIABLE_REF);
            ADDITIONAL_TAGS.put("ODIN_PKG_PRIVATE_VARIABLE_REF", ODIN_PKG_PRIVATE_VARIABLE_REF);
            ADDITIONAL_TAGS.put("ODIN_LOCAL_VARIABLE_REF", ODIN_LOCAL_VARIABLE_REF);
            ADDITIONAL_TAGS.put("ODIN_VAR_REASSIGNMENT_REF", ODIN_VAR_REASSIGNMENT_REF);
            ADDITIONAL_TAGS.put("ODIN_SHADOWING_VARIABLE_REF", ODIN_SHADOWING_VARIABLE_REF);
            ADDITIONAL_TAGS.put("ODIN_ATTRIBUTE_REF", ODIN_ATTRIBUTE_REF);
            ADDITIONAL_TAGS.put("ODIN_DIRECTIVE_REF", ODIN_DIRECTIVE_REF);
            ADDITIONAL_TAGS.put("ODIN_PKG_EXP_CONSTANT_REF", ODIN_PKG_EXP_CONSTANT_REF);
            ADDITIONAL_TAGS.put("ODIN_FILE_PRIVATE_CONSTANT_REF", ODIN_FILE_PRIVATE_CONSTANT_REF);
            ADDITIONAL_TAGS.put("ODIN_PKG_PRIVATE_CONSTANT_REF", ODIN_PKG_PRIVATE_CONSTANT_REF);
            ADDITIONAL_TAGS.put("ODIN_LOCAL_CONSTANT_REF", ODIN_LOCAL_CONSTANT_REF);
            ADDITIONAL_TAGS.put("ODIN_POLY_PARAMETER", ODIN_POLY_PARAMETER);
            ADDITIONAL_TAGS.put("ODIN_POLY_PARAMETER_REF", ODIN_POLY_PARAMETER_REF);
            ADDITIONAL_TAGS.put("ODIN_INVALID_ESCAPE", ODIN_INVALID_ESCAPE);
            ADDITIONAL_TAGS.put("ODIN_TEXT", ODIN_TEXT);
            ADDITIONAL_TAGS.put("ODIN_VALID_ESCAPE", ODIN_VALID_ESCAPE);
            ADDITIONAL_TAGS.put("ODIN_NUMBER", ODIN_NUMBER);

        }
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
