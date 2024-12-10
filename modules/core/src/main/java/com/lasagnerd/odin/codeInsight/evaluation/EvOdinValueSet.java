package com.lasagnerd.odin.codeInsight.evaluation;

import com.lasagnerd.odin.codeInsight.typeInference.OdinTypeChecker;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class EvOdinValueSet extends EvOdinValue {
    private final Class<?> elementType;

    public EvOdinValueSet(Class<?> elementType, Set<?> values, TsOdinType type) {
        super(values, type);
        this.elementType = elementType;
    }

    public EvOdinValueSet combine(EvOdinValueSet other) {
        if (!isCompatible(other)) return EvOdinValues.bottom();
        return doCombine(other);

    }

    protected boolean isCompatible(EvOdinValueSet other) {
        return other.getElementType() == elementType && OdinTypeChecker.checkTypesStrictly(type, other.getType());
    }

    public abstract EvOdinValueSet doCombine(EvOdinValueSet other);

    public EvOdinValueSet intersect(EvOdinValueSet other) {
        if (!isCompatible(other)) return EvOdinValues.bottom();
        return doIntersect(other);
    }

    public abstract EvOdinValueSet doIntersect(EvOdinValueSet other);

    public abstract EvOdinValueSet complement();

    public abstract EvOdinValueSet any();

    public EvOdinValueSet diff(EvOdinValueSet other) {
        if (!isCompatible(other)) return EvOdinValues.bottom();
        return doDiff(other);
    }

    public abstract EvOdinValueSet doDiff(EvOdinValueSet other);

    @Override
    public EvOdinValueSet asSet() {
        return this;
    }

    @Override
    public Set<?> getValue() {
        return (Set<?>) super.getValue();
    }

    public abstract boolean isSubset(EvOdinValueSet set);

}
