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
public class TsOdinTypeReference extends TsOdinTypeBase {

    public TsOdinTypeReference(TsOdinTypeKind targetTypeKind) {
        this.targetTypeKind = targetTypeKind;
    }

    @Override
    public TsOdinTypeKind getTypeReferenceKind() {
        return TsOdinTypeKind.UNKNOWN;
    }

    private TsOdinType representedType;
    private final TsOdinTypeKind targetTypeKind;

    private TsOdinTypeReference aliasedTypeReference;
    private OdinExpression typeExpression;

    public TsOdinType referencedType() {
        if (representedType == null) {
            representedType = OdinTypeResolver.resolveTypeReference(context, this);
        }
        return representedType;
    }

    public TsOdinTypeReference baseTypeReference() {
        if (aliasedTypeReference != null) {
            return aliasedTypeReference.baseTypeReference();
        }
        return this;
    }
}
