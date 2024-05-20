package com.lasagnerd.odin.insights.typeSystem;

import com.lasagnerd.odin.lang.psi.OdinDeclaredIdentifier;
import com.lasagnerd.odin.lang.psi.OdinType;
import lombok.Data;

@Data
public class TsOdinParameter {
    String valueName;
    OdinDeclaredIdentifier valueDeclaredIdentifier;

    boolean isExplicitPolymorphicParameter;

    OdinType psiType;
    TsOdinType type;

    int index;
}