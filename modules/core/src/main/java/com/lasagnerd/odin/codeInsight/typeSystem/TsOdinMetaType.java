package com.lasagnerd.odin.codeInsight.typeSystem;

import com.lasagnerd.odin.lang.psi.OdinExpression;
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
        ALIAS,
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
        PROCEDURE_OVERLOAD,
        DISTINCT_TYPE,
        DYNAMIC_ARRAY,
    }

    private TsOdinType representedType;
    private final MetaType representedMetaType;

    private TsOdinMetaType aliasedMetaType;
    private OdinExpression typeExpression;

}
