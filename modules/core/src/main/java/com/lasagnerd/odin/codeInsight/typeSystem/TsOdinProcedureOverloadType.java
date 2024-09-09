package com.lasagnerd.odin.codeInsight.typeSystem;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class TsOdinProcedureOverloadType extends TsOdinType{
    private List<TsOdinProcedureType> targetProcedures = new ArrayList<>();

    @Override
    public TsOdinMetaType.MetaType getMetaType() {
        return TsOdinMetaType.MetaType.PROCEDURE_OVERLOAD;
    }
}
