package com.lasagnerd.odin.codeInsight.typeSystem;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class TsOdinStructType extends TsOdinGenericType implements TsOdinParameterOwner {

    private List<TsOdinParameter> parameters = new ArrayList<>();
    private Map<String, TsOdinType> fields = new HashMap<>();

    @Override
    public TsOdinMetaType.MetaType getMetaType() {
        return TsOdinMetaType.MetaType.STRUCT;
    }

    @Override
    public String getLabel() {
        return super.getLabel() + TsOdinUtils.parametersStringIfNonEmpty(parameters);
    }
}
