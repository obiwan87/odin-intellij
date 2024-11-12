package com.lasagnerd.odin.codeInsight.typeSystem;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class TsOdinProcedureType extends TsOdinType {
    List<TsOdinType> returnTypes = new ArrayList<>();
    List<TsOdinParameter> parameters = new ArrayList<>();
    List<TsOdinParameter> returnParameters = new ArrayList<>();

    @Override
    public String getLabel() {
        String label = "proc ";
        label += getName() != null? getName() : "";
        label += "("+ TsOdinUtils.getParametersString(parameters) + ")";
        String returTypesString = returnTypes.stream().map(TsOdinType::getLabel).collect(Collectors.joining(", "));
        if (!returnTypes.isEmpty()) {
            label += " -> ";
        }

        if (returnTypes.size() > 1) {
            label += "(" + returTypesString + ")";
        }

        if(returnTypes.size() == 1) {
            label += returTypesString;
        }
        return label;
    }

    @Override
    public TsOdinMetaType.MetaType getMetaType() {
        return TsOdinMetaType.MetaType.PROCEDURE;
    }
}
