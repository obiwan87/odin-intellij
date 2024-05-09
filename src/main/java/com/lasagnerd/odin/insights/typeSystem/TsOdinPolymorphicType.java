package com.lasagnerd.odin.insights.typeSystem;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TsOdinPolymorphicType extends TsOdinType {
    @Override
    public String getLabel() {
        return "$" + super.getLabel();
    }
}
