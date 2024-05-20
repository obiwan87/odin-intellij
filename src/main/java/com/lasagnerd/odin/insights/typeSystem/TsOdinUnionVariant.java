package com.lasagnerd.odin.insights.typeSystem;

import com.lasagnerd.odin.lang.psi.OdinType;
import lombok.Data;

@Data
public class TsOdinUnionVariant {
    TsOdinType type;
    OdinType psiType;
}
