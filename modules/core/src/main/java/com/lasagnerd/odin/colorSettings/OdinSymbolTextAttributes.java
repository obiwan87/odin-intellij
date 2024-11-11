package com.lasagnerd.odin.colorSettings;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.lasagnerd.odin.codeInsight.symbols.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class OdinSymbolTextAttributes
        extends HashMap<OdinSymbolType, Map<OdinScope, Map<OdinVisibility, Map<OdinSymbolOrigin, Map<OdinIdentifierType, TextAttributesKey>>>>>
        implements Map<OdinSymbolType, Map<OdinScope, Map<OdinVisibility, Map<OdinSymbolOrigin, Map<OdinIdentifierType, TextAttributesKey>>>>> {

    public TextAttributesKey getReferenceTextAttribute(@NotNull OdinSymbol symbol) {
        OdinIdentifierType identifierType = OdinIdentifierType.REFERENCE;
        return getStyle(symbol, identifierType);
    }

    private @Nullable TextAttributesKey getStyle(@NotNull OdinSymbol symbol, @NotNull OdinIdentifierType identifierType) {
        var map1 = get(symbol.getSymbolType());
        if (map1 != null) {
            var map2 = map1.get(symbol.getScope());
            if (map2 != null) {
                var map3 = map2.get(symbol.getVisibility());
                if (map3 != null) {
                    var map4 = map3.get(symbol.getSymbolOrigin());
                    if (map4 != null) {
                        return map4.get(identifierType);
                    }
                }
            }
        }
        return null;
    }

    public TextAttributesKey getDeclarationStyle(@NotNull OdinSymbol symbol) {
        return getStyle(symbol, OdinIdentifierType.DECLARATION);
    }

    public TextAttributesKey getCallTextAttribute(@NotNull OdinSymbol symbol) {
        return getStyle(symbol, OdinIdentifierType.CALL);
    }

    public void addTextAttribute(OdinSymbolType symbolType,
                                 OdinScope scope,
                                 OdinVisibility visibility,
                                 OdinIdentifierType identifierType,
                                 TextAttributesKey textAttributesKey) {
        addTextAttribute(symbolType, scope, visibility, identifierType, OdinSymbolOrigin.NONE, textAttributesKey);
    }

    public void addTextAttribute(OdinSymbolType symbolType,
                                 OdinScope scope,
                                 OdinVisibility visibility,
                                 OdinIdentifierType identifierType,
                                 OdinSymbolOrigin symbolOrigin,
                                 TextAttributesKey textAttributesKey) {
        computeIfAbsent(symbolType, k -> new HashMap<>())
                .computeIfAbsent(scope, k -> new HashMap<>())
                .computeIfAbsent(visibility, k -> new HashMap<>())
                .computeIfAbsent(symbolOrigin, k -> new HashMap<>())
                .put(identifierType, textAttributesKey);
    }

}
