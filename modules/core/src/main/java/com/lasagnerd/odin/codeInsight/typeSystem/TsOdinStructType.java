package com.lasagnerd.odin.codeInsight.typeSystem;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.*;

@EqualsAndHashCode(callSuper = true)
@Data
public class TsOdinStructType extends TsOdinGenericType {

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
