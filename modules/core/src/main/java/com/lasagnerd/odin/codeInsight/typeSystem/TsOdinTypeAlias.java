package com.lasagnerd.odin.codeInsight.typeSystem;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class TsOdinTypeAlias extends TsOdinTypeBase {
    private TsOdinType aliasedType;

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
            return aliasedType;
        }
        return this;
    }

    @Override
    public String getLabel() {
        if (aliasedType != null) {
            return (isDistinct() ? "distinct " : "") + aliasedType.getLabel();
        }
        return "";
    }
}
