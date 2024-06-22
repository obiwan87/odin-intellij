package com.lasagnerd.odin.codeInsight.typeSystem;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * The type that is yielded by an identifier that is a type name.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TsOdinMetaType extends TsOdinType {


    @Override
    public MetaType getMetaType() {
        return MetaType.UNKNOWN;
    }

    public enum MetaType {
        ARRAY,
        BIT_SET,
        BUILTIN,
        ENUM,
        MAP,
        MATRIX,
        MULTI_POINTER,
        PACKAGE,
        POINTER,
        POLYMORPHIC,
        PROCEDURE,
        SLICE,
        STRUCT,
        UNION,
        UNKNOWN,
        VOID, NUMERIC, BOOL, RUNE, STRING, RAWPTR,
    }


    private final MetaType representedMetaType;
    private TsOdinType representedType;

}
