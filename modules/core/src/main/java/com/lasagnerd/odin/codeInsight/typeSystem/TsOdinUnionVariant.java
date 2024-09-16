package com.lasagnerd.odin.codeInsight.typeSystem;

import com.lasagnerd.odin.lang.psi.OdinType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TsOdinUnionVariant {
    private TsOdinType type;
    private OdinType psiType;
}
