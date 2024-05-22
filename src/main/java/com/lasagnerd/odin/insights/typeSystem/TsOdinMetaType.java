package com.lasagnerd.odin.insights.typeSystem;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Delegate;

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
        PROCEDURE,
        PACKAGE,
        STRUCT,
        UNION,
        ENUM,
        BIT_SET,
        MATRIX,
        ARRAY,
        SLICE,
        MAP,
        POINTER,
        MULTI_POINTER,
        POLYMORPHIC,
        BUILTIN,
        VOID,
        UNKNOWN
    }


    private final MetaType representedMetaType;
    private TsOdinType representedType;

}
