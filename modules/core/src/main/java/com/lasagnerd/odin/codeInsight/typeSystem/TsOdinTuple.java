package com.lasagnerd.odin.codeInsight.typeSystem;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class TsOdinTuple extends TsOdinType {
    private final List<TsOdinType> types;

    public TsOdinTuple(List<TsOdinType> types) {
        this.types = types;
    }

    public TsOdinType get(int index) {
        if(index < types.size()) {
            return types.get(index);
        }
        return TsOdinBuiltInTypes.UNKNOWN;
    }

    @Override
    public TsOdinMetaType.MetaType getMetaType() {
        return TsOdinMetaType.MetaType.UNKNOWN;
    }

    @Override
    public String getLabel() {
        if(types != null) {
            String typeLabels = types.stream().map(TsOdinType::getLabel).collect(Collectors.joining(", "));
            return "(%s)".formatted(typeLabels);
        }

        return "()";
    }
}
