package com.lasagnerd.odin.codeInsight.typeSystem;

import com.lasagnerd.odin.lang.psi.OdinArraySize;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class TsOdinArrayType extends TsOdinTypeBase implements TsOdinElementOwner {
    TsOdinType elementType;
    OdinArraySize psiSizeElement;
    Integer size;
    boolean soa;
    boolean simd;

    @Override
    public String getLabel() {
        String text = psiSizeElement != null? psiSizeElement.getText() : (size != null? String.valueOf(size) : "undefined");
        return "[" + text + "]" + (elementType != null ? elementType.getLabel() : "<undefined>");
    }

    @Override
    public TsOdinMetaType.MetaType getMetaType() {
        return TsOdinMetaType.MetaType.ARRAY;
    }
}
