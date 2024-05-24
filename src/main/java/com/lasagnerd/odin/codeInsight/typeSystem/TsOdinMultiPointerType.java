package com.lasagnerd.odin.codeInsight.typeSystem;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TsOdinMultiPointerType extends TsOdinType {
    TsOdinType dereferencedType;

    @Override
    public TsOdinMetaType.MetaType getMetaType() {
        return TsOdinMetaType.MetaType.MULTI_POINTER;
    }
}
