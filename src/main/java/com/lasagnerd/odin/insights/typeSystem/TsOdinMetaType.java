package com.lasagnerd.odin.insights.typeSystem;

import com.lasagnerd.odin.lang.psi.OdinType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * The type that is yielded by an identifier that is a type name.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TsOdinMetaType extends TsOdinType {
    public enum MetaType {
        PROCEDURE,
        STRUCT,
        UNION,
        ENUM,
        POLYMORPHIC_PARAMETER,
        BUILTIN
    }

    private final MetaType metaType;

}
