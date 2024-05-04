package com.lasagnerd.odin.insights.typeSystem;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@EqualsAndHashCode(callSuper = true)
@Data
public class TsOdinStructType extends TsOdinType {
    List<TsOdinPolyParameter> polyParameters = new ArrayList<>();
    Map<String, TsOdinType> fields = new HashMap<>();

    @Nullable
    public TsOdinPolyParameter getPolyParameter(String name) {
        return polyParameters.stream()
                .filter(p -> Objects.equals(p.getValueDeclaredIdentifier().getName(), name))
                .findFirst().orElse(null);
    }
}
