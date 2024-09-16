package com.lasagnerd.odin.codeInsight.typeSystem;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
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
