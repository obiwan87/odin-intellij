package com.lasagnerd.odin.codeInsight.typeSystem;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TsOdinUtils {
    public static boolean areEqual(TsOdinType t1, TsOdinType t2) {
        return true;
    }

    public static String parametersStringIfNonEmpty(List<TsOdinParameter> parameters) {
        String label = "";
        String parametersList = getParametersString(parameters);
        if (!parameters.isEmpty()) {
            label += "(" + parametersList + ")";
        }
        return label;
    }

    static @NotNull String getParametersString(List<TsOdinParameter> parameters1) {
        return parameters1.stream()
                .map(TsOdinParameter::getType)
                .filter(Objects::nonNull).map(TsOdinType::getLabel)
                .collect(Collectors.joining(", "));
    }

    static String getLabelOrUndefined(TsOdinType type) {
        if(type == null)
            return "<undefined>";
        return type.getLabel();
    }
}
