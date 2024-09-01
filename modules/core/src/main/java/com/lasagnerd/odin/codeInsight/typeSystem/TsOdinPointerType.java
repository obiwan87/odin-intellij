package com.lasagnerd.odin.codeInsight.typeSystem;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TsOdinPointerType extends TsOdinType {
    private TsOdinType dereferencedType;

    @Override
    public String getLabel() {
        return "^" + (dereferencedType != null ? dereferencedType.getLabel() : "<undefined>");
    }

    @Override
    public TsOdinMetaType.MetaType getMetaType() {
        return TsOdinMetaType.MetaType.POINTER;
    }
}
