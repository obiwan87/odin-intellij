package com.lasagnerd.odin.insights.typeSystem;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class TsOdinUnionType extends TsOdinGenericType {
    List<TsOdinUnionVariant> variants = new ArrayList<>();
    List<TsOdinParameter> parameters = new ArrayList<>();

    @Override
    public TsOdinMetaType.MetaType getMetaType() {
        return TsOdinMetaType.MetaType.UNION;
    }

    @Override
    public String getLabel() {
        return super.getLabel() + TsOdinUtils.parametersStringIfNonEmpty(parameters);
    }
}
