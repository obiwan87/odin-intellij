package com.lasagnerd.odin.lang.typeSystem;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TsOdinMapType extends TsOdinType {
    TsOdinType keyType;
    TsOdinType valueType;
}
