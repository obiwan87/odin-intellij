package com.lasagnerd.odin.insights.typeSystem;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TsOdinMapType extends TsOdinType {
    TsOdinType keyType;
    TsOdinType valueType;

    @Override
    public String getLabel() {
        return "map[" + getLabelOrUndefined(keyType) + "]" + getLabelOrUndefined(valueType);
    }

    @Override
    public TsOdinMetaType.MetaType getMetaType() {
        return TsOdinMetaType.MetaType.MAP;
    }
}
