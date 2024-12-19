package com.lasagnerd.odin.codeInsight.dataflow;

import com.intellij.psi.PsiNamedElement;
import com.lasagnerd.odin.codeInsight.evaluation.EvOdinValue;
import com.lasagnerd.odin.codeInsight.evaluation.EvOdinValueSet;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbol;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Getter
public class OdinSymbolValueStore {
    Map<OdinSymbol, EvOdinValue> values = new HashMap<>();

    public EvOdinValue getValue(String name) {
        return values.keySet().stream()
                .filter(s -> Objects.equals(name, s.getName()))
                .findFirst()
                .map(s -> values.get(s))
                .orElse(null);
    }

    public EvOdinValue getValue(PsiNamedElement declaredIdentifier) {
        return values.keySet().stream()
                .filter(s -> Objects.equals(declaredIdentifier, s.getDeclaredIdentifier()))
                .findFirst()
                .map(s -> values.get(s))
                .orElse(null);
    }

    public EvOdinValue getValue(OdinSymbol symbol) {
        return values.get(symbol);
    }

    public void combine(OdinSymbolValueStore other) {
        for (var entry : other.values.entrySet()) {
            EvOdinValue value = values.computeIfAbsent(entry.getKey(), k -> entry.getValue());
            if (value != entry.getValue()) {
                EvOdinValueSet combinedValue = value.asSet().combine(entry.getValue().asSet());
                values.put(entry.getKey(), combinedValue);
            }
        }
    }

    public void intersect(OdinSymbolValueStore otherStore) {
        for (var entry : otherStore.values.entrySet()) {
            EvOdinValue value = values.computeIfAbsent(entry.getKey(), k -> entry.getValue());
            if (value != entry.getValue()) {
                EvOdinValueSet combinedValue = value.asSet().intersect(entry.getValue().asSet());
                values.put(entry.getKey(), combinedValue);
            }
        }
    }

    public OdinSymbolValueStore intersected(OdinSymbolValueStore symbolValueStore) {

        OdinSymbolValueStore copy = copy();
        if (symbolValueStore == null)
            return copy;
        copy.intersect(symbolValueStore);
        return copy;
    }

    public OdinSymbolValueStore copy() {
        OdinSymbolValueStore symbolValueStore = new OdinSymbolValueStore();
        symbolValueStore.getValues().putAll(this.values);
        return symbolValueStore;
    }

    public void printValues() {
        for (Map.Entry<OdinSymbol, EvOdinValue> entry : values.entrySet()) {
            System.out.println(entry.getKey().getName() + ":=" + entry.getValue().toString());
        }
    }

    // An absence of value means ALL values
    // So if
    public boolean isSubset(OdinSymbolValueStore symbolValueStore) {
        if (values.isEmpty()) {
            return symbolValueStore.values.isEmpty();
        }

        if (symbolValueStore.values.isEmpty())
            return true;

        for (var entry : this.values.entrySet()) {
            OdinSymbol symbol = entry.getKey();
            EvOdinValue thisValue = entry.getValue();
            EvOdinValue otherValue = symbolValueStore.values.get(symbol);
            if (otherValue == null)
                continue;

            if (!thisValue.asSet().isSubset(otherValue.asSet())) {
                return false;
            }
        }
        return true;
    }

    /**
     * 'bottom' is a commonly used term in dataflow analysis, the denotes a contradictory/impossible state
     * If any of the symbols assume a 'bottom' value, the whole store is bottom.
     *
     * @return True if the symbol value store is 'bottom'
     */
    public boolean isBottom() {
        for (Map.Entry<OdinSymbol, EvOdinValue> entry : values.entrySet()) {
            if (entry.getValue().asSet().isBottom()) {
                return true;
            }
        }
        return false;
    }
}
