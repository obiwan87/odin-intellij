package com.lasagnerd.odin.codeInsight.typeSystem;

import lombok.Getter;

import java.util.Map;
import java.util.TreeMap;

@Getter
public class TsOdinSoaStructType extends TsOdinTypeBase {
    private final TsOdinSoaSliceType soaSliceType;
    private final Map<String, TsOdinType> fields = new TreeMap<>();

    public TsOdinSoaStructType(TsOdinSoaSliceType soaSliceType) {
        this.soaSliceType = soaSliceType;
    }

    @Override
    public TsOdinMetaType.MetaType getMetaType() {
        return TsOdinMetaType.MetaType.SOA_STRUCT;
    }
}
