package com.lasagnerd.odin.codeInsight.evaluation;

import com.lasagnerd.odin.codeInsight.typeInference.OdinTypeChecker;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinBuiltInTypes;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class EvOdinValueSet<VALUE, TYPE extends TsOdinType> extends EvOdinValue<Set<VALUE>, TYPE> {
    private final Class<VALUE> elementType;

    public EvOdinValueSet(Class<VALUE> elementType, Set<VALUE> values, TYPE type) {
        super(values, type);
        this.elementType = elementType;
    }

    public EvOdinValueSet<VALUE, TYPE> combine(EvOdinValueSet<?, ?> other) {
        if (!isCompatible(other)) return TsOdinBuiltInTypes.nullSet();
        return doCombine((EvOdinValueSet<VALUE, TYPE>) other);

    }

    private boolean isCompatible(EvOdinValueSet<?, ?> other) {
        return other.getElementType() == elementType && OdinTypeChecker.checkTypesStrictly(type, other.getType());
    }

    public abstract EvOdinValueSet<VALUE, TYPE> doCombine(EvOdinValueSet<VALUE, TYPE> other);

    public EvOdinValueSet<VALUE, TYPE> intersect(EvOdinValueSet<?, ?> other) {
        if (!isCompatible(other)) return TsOdinBuiltInTypes.nullSet();
        return doIntersect((EvOdinValueSet<VALUE, TYPE>) other);
    }

    public abstract EvOdinValueSet<VALUE, TYPE> doIntersect(EvOdinValueSet<VALUE, TYPE> other);

    public abstract EvOdinValueSet<VALUE, TYPE> complement();
}
