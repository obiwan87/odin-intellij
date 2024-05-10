package com.lasagnerd.odin.insights.typeSystem;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TsOdinEnumType extends TsOdinType {
    TsOdinBuiltInType backingType;

    @Override
    public String getLabel() {
        return "enum " + getName() + (backingType != null ? " " + backingType.getLabel() : "");
    }

    @Override
    public TsOdinMetaType.MetaType getMetaType() {
        return TsOdinMetaType.MetaType.ENUM;
    }
}
