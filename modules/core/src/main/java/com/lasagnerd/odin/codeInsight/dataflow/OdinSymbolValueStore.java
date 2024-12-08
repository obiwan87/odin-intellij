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

    // TODO implement
    public OdinSymbolValueStore copy() {
        OdinSymbolValueStore store = new OdinSymbolValueStore();
        store.getValues().putAll(values);
        return store;
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

    public void printValues() {
        for (Map.Entry<OdinSymbol, EvOdinValue> entry : values.entrySet()) {
            System.out.println(entry.getKey().getName() + ":=" + entry.getValue().toString());
        }
    }
}
