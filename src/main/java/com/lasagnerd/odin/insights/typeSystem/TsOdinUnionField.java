package com.lasagnerd.odin.insights.typeSystem;

import com.lasagnerd.odin.lang.psi.OdinTypeDefinitionExpression;
import lombok.Data;

@Data
public class TsOdinUnionField {
    TsOdinType type;
    OdinTypeDefinitionExpression typeDefinitionExpression;
}
