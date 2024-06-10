package com.lasagnerd.odin.lang;

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.JBColor;
import com.lasagnerd.odin.OdinIcons;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class OdinColorSettingsPage implements ColorSettingsPage {

    AttributesDescriptor[] ATTRIBUTES_DESCRIPTORS = new AttributesDescriptor[]{
            new AttributesDescriptor("Built-in symbol", OdinSyntaxHighlighter.BUILTIN_FUNCTION),
            new AttributesDescriptor("Package", OdinSyntaxHighlighter.PACKAGE)
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

        @Language("Odin")
        String s = """
                package main;
                import "core:fmt"
                                
                <constant>PI</constant> :: 3.14159
                <constant>mat2x2</constant> :: distinct matrix[2, 2]f32
                                
                foo :: struct {
                    bar: <builtin>int</builtin>
                }
                    
                main :: proc() {
                    fmt.println("Hi Mom!")
                    
                    my_array := <builtin>make</builtin>([dynamic]int)
                    my_value : <builtin>int</builtin> = 5
                }
                """;
        return s;
    }

    @Override
    public @Nullable Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return Map.of(
                "builtin", OdinSyntaxHighlighter.BUILTIN_FUNCTION,
                "package", OdinSyntaxHighlighter.PACKAGE,
                "constant", DefaultLanguageHighlighterColors.CONSTANT
        );
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
