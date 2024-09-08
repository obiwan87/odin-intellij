package com.lasagnerd.odin.codeInsight.typeSystem;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TsOdinTypeAlias extends TsOdinType{
    private TsOdinType aliasedType;
    private boolean distinct;

    @Override
    public TsOdinMetaType.MetaType getMetaType() {
        return TsOdinMetaType.MetaType.ALIAS;
    }

    public TsOdinType getBaseType() {
        if(aliasedType instanceof TsOdinTypeAlias typeAlias) {
            return typeAlias.getBaseType();
        }
        return aliasedType;
    }

    public TsOdinType getDistinctBaseType () {
        if(!isDistinct()) {
            if(aliasedType instanceof TsOdinTypeAlias typeAlias) {
                return typeAlias.getDistinctBaseType();
            }
        }
        return aliasedType;
    }
}
