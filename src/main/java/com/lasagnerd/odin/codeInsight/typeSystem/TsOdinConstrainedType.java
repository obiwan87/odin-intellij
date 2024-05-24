package com.lasagnerd.odin.codeInsight.typeSystem;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TsOdinConstrainedType extends TsOdinType {

    TsOdinType mainType;
    TsOdinType specializedType;

    @Override
    public TsOdinMetaType.MetaType getMetaType() {
        return TsOdinMetaType.MetaType.UNKNOWN;
    }
}
