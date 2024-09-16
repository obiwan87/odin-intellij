package com.lasagnerd.odin.codeInsight.typeSystem;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class TsOdinProcedureOverloadType extends TsOdinType{
    private List<TsOdinProcedureType> targetProcedures = new ArrayList<>();

    @Override
    public TsOdinMetaType.MetaType getMetaType() {
        return TsOdinMetaType.MetaType.PROCEDURE_OVERLOAD;
    }
}
