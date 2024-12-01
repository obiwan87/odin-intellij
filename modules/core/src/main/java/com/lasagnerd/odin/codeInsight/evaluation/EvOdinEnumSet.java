package com.lasagnerd.odin.codeInsight.evaluation;

import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinEnumType;

import java.util.HashSet;
import java.util.Set;

public class EvOdinEnumSet extends EvOdinValueSet<EvEnumValue, TsOdinEnumType> {
    public EvOdinEnumSet(Set<EvEnumValue> evEnumValues, TsOdinEnumType type) {
        super(EvEnumValue.class, evEnumValues, type);
    }

    @Override
    public EvOdinValueSet<EvEnumValue, TsOdinEnumType> doCombine(EvOdinValueSet<EvEnumValue, TsOdinEnumType> other) {
        Set<EvEnumValue> newValues = new HashSet<>(getValue());
        newValues.addAll(other.getValue());
        return new EvOdinEnumSet(newValues, type);
    }

    @Override
    public EvOdinValueSet<EvEnumValue, TsOdinEnumType> doIntersect(EvOdinValueSet<EvEnumValue, TsOdinEnumType> other) {
        Set<EvEnumValue> newValues = new HashSet<>(getValue());
        newValues.retainAll(other.getValue());
        return new EvOdinEnumSet(newValues, type);
    }

    @Override
    public EvOdinValueSet<EvEnumValue, TsOdinEnumType> complement() {
        EvOdinValue<Set<EvEnumValue>, TsOdinEnumType> enumValues = OdinExpressionEvaluator.getEnumValues(type);
        Set<EvEnumValue> complement = new HashSet<>(enumValues.getValue());
        complement.removeAll(value);
        return new EvOdinEnumSet(complement, type);
    }
}
