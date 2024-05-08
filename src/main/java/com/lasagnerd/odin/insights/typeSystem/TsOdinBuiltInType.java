package com.lasagnerd.odin.insights.typeSystem;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public class TsOdinBuiltInType extends TsOdinType {
    public static final TsOdinBuiltInType BOOLEAN = new TsOdinBuiltInType("bool");
    public static final TsOdinBuiltInType QUATERNION256 = new TsOdinBuiltInType("quaternion256");
    public static final TsOdinBuiltInType COMPLEX128 = new TsOdinBuiltInType("complex128");
    public static final TsOdinBuiltInType INT = new TsOdinBuiltInType("int");
    public static final TsOdinBuiltInType F64 = new TsOdinBuiltInType("f64");
    public static final TsOdinBuiltInType STRING = new TsOdinBuiltInType("string");
    public static final TsOdinBuiltInType TYPE_ID = new TsOdinBuiltInType("typeid");
    public static final TsOdinBuiltInType RUNE = new TsOdinBuiltInType("rune");

    private static final Map<String, TsOdinBuiltInType> builtInTypeMap = new HashMap<>();
    static {
        builtInTypeMap.put(BOOLEAN.getName(), BOOLEAN);
        builtInTypeMap.put(QUATERNION256.getName(), QUATERNION256);
        builtInTypeMap.put(COMPLEX128.getName(), COMPLEX128);
        builtInTypeMap.put(INT.getName(), INT);
        builtInTypeMap.put(F64.getName(), F64);
        builtInTypeMap.put(STRING.getName(), STRING);
        builtInTypeMap.put(TYPE_ID.getName(), TYPE_ID);
        builtInTypeMap.put(RUNE.getName(), RUNE);
    }

    private TsOdinBuiltInType() {
    }

    private TsOdinBuiltInType(String name) {
        this.name = name;
    }

    public static TsOdinBuiltInType getBuiltInType(String identifierText) {
        return builtInTypeMap.get(identifierText);
    }
}
