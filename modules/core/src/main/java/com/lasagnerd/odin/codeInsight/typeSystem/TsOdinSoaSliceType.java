package com.lasagnerd.odin.codeInsight.typeSystem;

import lombok.Getter;

import java.util.Map;
import java.util.TreeMap;

@Getter
public class TsOdinSoaSliceType extends TsOdinType {
    Map<String, TsOdinType> slices = new TreeMap<>();
    TsOdinSoaStructType soaStructType;

    @Override
    public TsOdinMetaType.MetaType getMetaType() {
        return TsOdinMetaType.MetaType.SOA_SLICE;
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