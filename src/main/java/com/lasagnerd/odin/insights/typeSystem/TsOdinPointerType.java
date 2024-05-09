package com.lasagnerd.odin.insights.typeSystem;

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
}
