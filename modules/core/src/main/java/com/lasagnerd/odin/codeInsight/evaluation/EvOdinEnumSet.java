package com.lasagnerd.odin.codeInsight.evaluation;

import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinEnumType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class EvOdinEnumSet extends EvOdinValueSet {
    public EvOdinEnumSet(Set<EvEnumValue> evEnumValues, TsOdinEnumType type) {
        super(EvEnumValue.class, evEnumValues, type);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<EvEnumValue> getValue() {
        return (Set<EvEnumValue>) super.getValue();
    }

    @Override
    public boolean isSubset(EvOdinValueSet otherSet) {
        if (!(otherSet instanceof EvOdinEnumSet otherEnumSet)) {
            return false;
        }

        if (!isCompatible(otherEnumSet)) {
            return false;
        }

        if (otherSet.isBottom()) {
            return false;
        }

        return otherEnumSet.getValue().containsAll(this.getValue());
    }

    @Override
    public EvOdinValueSet doCombine(EvOdinValueSet other) {
        if (!(other instanceof EvOdinEnumSet otherEnumSet))
            return EvOdinValues.bottom();
        if (other.isBottom() || isBottom())
            return EvOdinValues.bottom();

        Set<EvEnumValue> newValues = new HashSet<>(getValue());
        newValues.addAll(otherEnumSet.getValue());
        return new EvOdinEnumSet(newValues, (TsOdinEnumType) getType());
    }

    @Override
    public TsOdinEnumType getType() {
        return (TsOdinEnumType) super.getType();
    }

    @Override
    public EvOdinValueSet doIntersect(EvOdinValueSet other) {
        if (!(other instanceof EvOdinEnumSet otherEnumSet))
            return EvOdinValues.bottom();

        Set<EvEnumValue> newValues = new HashSet<>(getValue());
        newValues.retainAll(otherEnumSet.getValue());
        return new EvOdinEnumSet(newValues, getType());
    }

    @Override
    public EvOdinValueSet complement() {
        EvOdinEnumSet enumValues = OdinExpressionEvaluator.getEnumValues(getType());
        Set<EvEnumValue> complement = new HashSet<>(enumValues.getValue());
        complement.removeAll(getValue());
        return new EvOdinEnumSet(complement, getType());
    }

    @Override
    public EvOdinValueSet any() {
        return OdinExpressionEvaluator.getEnumValues(getType());
    }

    @Override
    public EvOdinValueSet doDiff(EvOdinValueSet other) {
        if (!(other instanceof EvOdinEnumSet otherEnumSet))
            return EvOdinValues.bottom();

        Set<EvEnumValue> diff = new HashSet<>(getValue());
        diff.removeAll(otherEnumSet.getValue());
        return new EvOdinEnumSet(diff, this.getType());
    }

    @Override
    public String toString() {
        String vals = getValue().stream().map(EvEnumValue::toString).collect(Collectors.joining(", "));
        return "{%s}".formatted(vals);
    }

    @Override
    public boolean isBottom() {
        return getValue().isEmpty();
    }
}
