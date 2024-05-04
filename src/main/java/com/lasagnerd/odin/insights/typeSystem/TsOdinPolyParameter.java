package com.lasagnerd.odin.insights.typeSystem;

import com.lasagnerd.odin.lang.psi.OdinDeclaredIdentifier;
import lombok.Data;

@Data
public class TsOdinPolyParameter {
    String valueName;
    OdinDeclaredIdentifier valueDeclaredIdentifier;
    boolean isValuePolymorphic;

    TsOdinType type;

    int index;
}
