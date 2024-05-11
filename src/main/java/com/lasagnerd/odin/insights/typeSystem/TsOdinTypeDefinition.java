package com.lasagnerd.odin.insights.typeSystem;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TsOdinTypeDefinition {
    String name;
    boolean distinct;
    TsOdinType type;
    List<TsOdinAttribute> attributes = new ArrayList<>();
}
