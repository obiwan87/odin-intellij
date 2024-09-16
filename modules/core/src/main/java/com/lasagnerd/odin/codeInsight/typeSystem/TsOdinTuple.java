package com.lasagnerd.odin.codeInsight.typeSystem;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

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
}
