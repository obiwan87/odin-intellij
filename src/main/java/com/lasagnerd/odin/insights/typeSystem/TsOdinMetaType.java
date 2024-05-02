package com.lasagnerd.odin.insights.typeSystem;

import com.lasagnerd.odin.lang.psi.OdinType;
import com.lasagnerd.odin.lang.psi.OdinTypeExpression;
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
        ENUM
    }

    private final MetaType metaType;
    public OdinType typeExpression;
}
