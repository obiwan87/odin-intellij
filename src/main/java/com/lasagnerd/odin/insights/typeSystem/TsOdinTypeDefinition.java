package com.lasagnerd.odin.insights.typeSystem;

import lombok.Data;

@Data
public class TsOdinTypeDefinition {
    String name;
    boolean distinct;
    TsOdinType type;
}
