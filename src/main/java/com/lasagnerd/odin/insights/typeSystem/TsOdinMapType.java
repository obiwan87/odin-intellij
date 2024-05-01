package com.lasagnerd.odin.insights.typeSystem;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TsOdinMapType extends TsOdinType {
    TsOdinType keyType;
    TsOdinType valueType;
}
