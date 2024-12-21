package com.lasagnerd.odin.codeInsight.typeSystem;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class TsOdinMapType extends TsOdinTypeBase {
    TsOdinType keyType;
    TsOdinType valueType;

    @Override
    public String getLabel() {
        return "map[" + TsOdinUtils.getLabelOrUndefined(keyType) + "]" + TsOdinUtils.getLabelOrUndefined(valueType);
    }

    @Override
    public TsOdinTypeKind getTypeReferenceKind() {
        return TsOdinTypeKind.MAP;
    }
}
