package com.lasagnerd.odin.codeInsight.typeSystem;

import lombok.Getter;

import java.util.Map;
import java.util.TreeMap;

@Getter
public class TsOdinSoaSliceType extends TsOdinTypeBase {
    Map<String, TsOdinType> slices = new TreeMap<>();
    TsOdinSoaStructType soaStructType;

    @Override
    public TsOdinTypeKind getTypeReferenceKind() {
        return TsOdinTypeKind.SOA_SLICE;
    }

    public TsOdinSoaStructType getSoaStructType() {
        if (soaStructType == null) {
            soaStructType = new TsOdinSoaStructType(this);
            slices.forEach(
                    (name, type) -> {
                        if (type instanceof TsOdinSliceType tsOdinSliceType) {
                            soaStructType.getFields().put(name, tsOdinSliceType.elementType);
                        }
                    }
            );
        }

        return soaStructType;
    }
}
