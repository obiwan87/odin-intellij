package com.lasagnerd.odin.codeInsight.typeSystem;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class TsOdinProcedureGroup extends TsOdinTypeBase {
    private List<TsOdinProcedureType> procedures = new ArrayList<>();

    @Override
    public TsOdinTypeKind getTypeReferenceKind() {
        return TsOdinTypeKind.PROCEDURE_GROUP;
    }
}
