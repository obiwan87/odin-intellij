package com.lasagnerd.odin.codeInsight.typeSystem;

import com.lasagnerd.odin.codeInsight.typeInference.OdinTypeResolver;
import com.lasagnerd.odin.lang.psi.OdinExpression;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * The type that is yielded by an identifier that is a type name.
 */
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class TsOdinMetaType extends TsOdinTypeBase {

    public TsOdinMetaType(MetaType representedMetaType) {
        this.representedMetaType = representedMetaType;
    }

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
        UNDECIDED,
        VOID,
        PROCEDURE_GROUP,
        DISTINCT_TYPE,
        DYNAMIC_ARRAY,
        SOA_SLICE,
        SOA_STRUCT,
    }

    private TsOdinType representedType;
    private final MetaType representedMetaType;

    private TsOdinMetaType aliasedMetaType;
    private OdinExpression typeExpression;

    public TsOdinType representedType() {
        if(representedType == null) {
            representedType = OdinTypeResolver.resolveMetaType(symbolTable, this);
        }
        return representedType;
    }

    public TsOdinMetaType baseMetaType() {
        if(aliasedMetaType != null) {
            return aliasedMetaType.baseMetaType();
        }
        return this;
    }
}
