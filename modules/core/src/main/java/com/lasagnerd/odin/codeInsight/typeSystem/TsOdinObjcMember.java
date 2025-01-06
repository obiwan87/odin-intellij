package com.lasagnerd.odin.codeInsight.typeSystem;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TsOdinObjcMember extends TsOdinTypeBase implements TsOdinParameterOwner {
    TsOdinObjcClass objcClass;
    TsOdinProcedureType procedureType;
    String objcMemberName;

    @Override
    public List<TsOdinParameter> getParameters() {
        return procedureType.getParameters();
    }

    @Override
    public TsOdinTypeKind getTypeReferenceKind() {
        return TsOdinTypeKind.OBJC_MEMBER;
    }
}
