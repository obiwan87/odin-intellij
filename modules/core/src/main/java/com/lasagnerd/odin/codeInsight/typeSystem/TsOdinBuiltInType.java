package com.lasagnerd.odin.codeInsight.typeSystem;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

// TODO make abstract
@EqualsAndHashCode(callSuper = true)
@Data
public class TsOdinBuiltInType extends TsOdinType implements Comparable<TsOdinBuiltInType> {


    TsOdinBuiltInType(String name) {
        this.name = name;
    }


    @Override
    public TsOdinMetaType.MetaType getMetaType() {
        return TsOdinMetaType.MetaType.BUILTIN;
    }

    @Override
    public int compareTo(@NotNull TsOdinBuiltInType o) {
        if (o.isUntyped() && this.isUntyped())
            return 0;

        if (o.isUntyped())
            return 1;

        return -1;
    }
}


