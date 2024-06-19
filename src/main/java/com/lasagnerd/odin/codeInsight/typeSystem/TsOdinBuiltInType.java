package com.lasagnerd.odin.codeInsight.typeSystem;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
public class TsOdinBuiltInType extends TsOdinType implements Comparable<TsOdinBuiltInType> {

    public boolean isUntyped() {
        if (this == TsOdinBuiltInTypes.UNTYPED_INT)
            return true;

        if (this == TsOdinBuiltInTypes.UNTYPED_BOOLEAN)
            return true;

        if (this == TsOdinBuiltInTypes.UNTYPED_FLOAT)
            return true;

        if (this == TsOdinBuiltInTypes.UNTYPED_COMPLEX)
            return true;

        if (this == TsOdinBuiltInTypes.UNTYPED_STRING)
            return true;

        if (this == TsOdinBuiltInTypes.UNTYPED_RUNE)
            return true;

        return this == TsOdinBuiltInTypes.UNTYPED_QUATERNION;
    }


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


