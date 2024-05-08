package com.lasagnerd.odin.insights.typeSystem;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TsOdinBuiltInType extends TsOdinType {
    public static final TsOdinBuiltInType BOOLEAN = new TsOdinBuiltInType("bool");

    public TsOdinBuiltInType() {
    }

    public TsOdinBuiltInType(String name) {
        this.name = name;
    }
}
