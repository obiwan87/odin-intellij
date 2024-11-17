package com.lasagnerd.odin.codeInsight.typeSystem;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

// TODO make abstract
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class TsOdinBuiltInType extends TsOdinTypeBase implements Comparable<TsOdinBuiltInType> {


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


