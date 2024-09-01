package com.lasagnerd.odin.codeInsight.typeSystem;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class TsOdinTuple extends TsOdinType {
    private final List<TsOdinType> types;
    public TsOdinType get(int index) {
        if(index < types.size()) {
            return types.get(index);
        }
        return TsOdinType.UNKNOWN;
    }

    @Override
    public TsOdinMetaType.MetaType getMetaType() {
        return TsOdinMetaType.MetaType.UNKNOWN;
    }
}
