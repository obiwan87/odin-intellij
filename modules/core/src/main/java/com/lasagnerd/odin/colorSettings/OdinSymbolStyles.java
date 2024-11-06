package com.lasagnerd.odin.colorSettings;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.lasagnerd.odin.codeInsight.symbols.OdinScope;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbol;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolType;
import com.lasagnerd.odin.codeInsight.symbols.OdinVisibility;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class OdinSymbolStyles
        extends HashMap<OdinSymbolType, Map<OdinScope, Map<OdinVisibility, Map<OdinIdentifierType, TextAttributesKey>>>>
        implements Map<OdinSymbolType, Map<OdinScope, Map<OdinVisibility, Map<OdinIdentifierType, TextAttributesKey>>>> {

    public TextAttributesKey getReferenceStyle(OdinSymbol symbol) {
        OdinIdentifierType identifierType = OdinIdentifierType.REFERENCE;
        return getStyle(symbol, identifierType);
    }

    private @Nullable TextAttributesKey getStyle(OdinSymbol symbol, OdinIdentifierType identifierType) {
        var map1 = get(symbol.getSymbolType());
        if (map1 != null) {
            var map2 = map1.get(symbol.getScope());
            if (map2 != null) {
                var map3 = map2.get(symbol.getVisibility());
                if (map3 != null) {
                    return map3.get(identifierType);
                }
            }
        }
        return null;
    }

    public TextAttributesKey getDeclarationStyle(OdinSymbol symbol) {
        return getStyle(symbol, OdinIdentifierType.DECLARATION);
    }

    public void addStyle(OdinSymbolType symbolType,
                         OdinScope scope,
                         OdinVisibility visibility,
                         OdinIdentifierType identifierType,
                         TextAttributesKey textAttributesKey) {
        computeIfAbsent(symbolType, k -> new HashMap<>())
                .computeIfAbsent(scope, k -> new HashMap<>())
                .computeIfAbsent(visibility, k -> new HashMap<>())
                .put(identifierType, textAttributesKey);
    }

}
