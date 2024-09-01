package com.lasagnerd.odin.codeInsight.typeSystem;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TsOdinPolymorphicType extends TsOdinType {
    @Override
    public String getLabel() {
        return "$" + super.getLabel();
    }

    @Override
    public TsOdinMetaType.MetaType getMetaType() {
        return TsOdinMetaType.MetaType.POLYMORPHIC;
    }
}
