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
        BIT_FIELD,
        BIT_SET,
        BOOL,
        BUILTIN,
        ENUM,
        MAP,
        MATRIX,
        MULTI_POINTER,
        NUMERIC,
        PACKAGE,
        POINTER,
        POLYMORPHIC,
        PROCEDURE,
        RAWPTR,
        RUNE,
        SLICE,
        STRING,
        STRUCT,
        UNION,
        UNKNOWN,
        VOID,
    }


    private final MetaType representedMetaType;
    private TsOdinType representedType;

}
