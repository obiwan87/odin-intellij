package com.lasagnerd.odin.insights.typeSystem;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public class TsOdinBuiltInType extends TsOdinType {
    public static final TsOdinBuiltInType BOOL = new TsOdinBuiltInType("bool");
    public static final TsOdinBuiltInType B8 = new TsOdinBuiltInType("b8");
    public static final TsOdinBuiltInType B16 = new TsOdinBuiltInType("b16");
    public static final TsOdinBuiltInType B32 = new TsOdinBuiltInType("b32");
    public static final TsOdinBuiltInType B64 = new TsOdinBuiltInType("b64");
    public static final TsOdinBuiltInType BYTE = new TsOdinBuiltInType("byte");
    public static final TsOdinBuiltInType INT = new TsOdinBuiltInType("int");
    public static final TsOdinBuiltInType I8 = new TsOdinBuiltInType("i8");
    public static final TsOdinBuiltInType I16 = new TsOdinBuiltInType("i16");
    public static final TsOdinBuiltInType I32 = new TsOdinBuiltInType("i32");
    public static final TsOdinBuiltInType I64 = new TsOdinBuiltInType("i64");
    public static final TsOdinBuiltInType I128 = new TsOdinBuiltInType("i128");
    public static final TsOdinBuiltInType UINT = new TsOdinBuiltInType("uint");
    public static final TsOdinBuiltInType U8 = new TsOdinBuiltInType("u8");
    public static final TsOdinBuiltInType U16 = new TsOdinBuiltInType("u16");
    public static final TsOdinBuiltInType U32 = new TsOdinBuiltInType("u32");
    public static final TsOdinBuiltInType U64 = new TsOdinBuiltInType("u64");
    public static final TsOdinBuiltInType U128 = new TsOdinBuiltInType("u128");
    public static final TsOdinBuiltInType UINTPTR = new TsOdinBuiltInType("uintptr");
    public static final TsOdinBuiltInType I16LE = new TsOdinBuiltInType("i16le");
    public static final TsOdinBuiltInType I32LE = new TsOdinBuiltInType("i32le");
    public static final TsOdinBuiltInType I64LE = new TsOdinBuiltInType("i64le");
    public static final TsOdinBuiltInType I128LE = new TsOdinBuiltInType("i128le");
    public static final TsOdinBuiltInType U16LE = new TsOdinBuiltInType("u16le");
    public static final TsOdinBuiltInType U32LE = new TsOdinBuiltInType("u32le");
    public static final TsOdinBuiltInType U64LE = new TsOdinBuiltInType("u64le");
    public static final TsOdinBuiltInType U128LE = new TsOdinBuiltInType("u128le");
    public static final TsOdinBuiltInType I16BE = new TsOdinBuiltInType("i16be");
    public static final TsOdinBuiltInType I32BE = new TsOdinBuiltInType("i32be");
    public static final TsOdinBuiltInType I64BE = new TsOdinBuiltInType("i64be");
    public static final TsOdinBuiltInType I128BE = new TsOdinBuiltInType("i128be");
    public static final TsOdinBuiltInType U16BE = new TsOdinBuiltInType("u16be");
    public static final TsOdinBuiltInType U32BE = new TsOdinBuiltInType("u32be");
    public static final TsOdinBuiltInType U64BE = new TsOdinBuiltInType("u64be");
    public static final TsOdinBuiltInType U128BE = new TsOdinBuiltInType("u128be");
    public static final TsOdinBuiltInType F16 = new TsOdinBuiltInType("f16");
    public static final TsOdinBuiltInType F32 = new TsOdinBuiltInType("f32");
    public static final TsOdinBuiltInType F64 = new TsOdinBuiltInType("f64");
    public static final TsOdinBuiltInType F16LE = new TsOdinBuiltInType("f16le");
    public static final TsOdinBuiltInType F32LE = new TsOdinBuiltInType("f32le");
    public static final TsOdinBuiltInType F64LE = new TsOdinBuiltInType("f64le");
    public static final TsOdinBuiltInType F16BE = new TsOdinBuiltInType("f16be");
    public static final TsOdinBuiltInType F32BE = new TsOdinBuiltInType("f32be");
    public static final TsOdinBuiltInType F64BE = new TsOdinBuiltInType("f64be");
    public static final TsOdinBuiltInType COMPLEX32 = new TsOdinBuiltInType("complex32");
    public static final TsOdinBuiltInType COMPLEX64 = new TsOdinBuiltInType("complex64");
    public static final TsOdinBuiltInType COMPLEX128 = new TsOdinBuiltInType("complex128");
    public static final TsOdinBuiltInType QUATERNION64 = new TsOdinBuiltInType("quaternion64");
    public static final TsOdinBuiltInType QUATERNION128 = new TsOdinBuiltInType("quaternion128");
    public static final TsOdinBuiltInType QUATERNION256 = new TsOdinBuiltInType("quaternion256");
    public static final TsOdinBuiltInType RUNE = new TsOdinBuiltInType("rune");
    public static final TsOdinBuiltInType STRING = new TsOdinBuiltInType("string");
    public static final TsOdinBuiltInType CSTRING = new TsOdinBuiltInType("cstring");
    public static final TsOdinBuiltInType RAWPTR = new TsOdinBuiltInType("rawptr");
    public static final TsOdinBuiltInType TYPEID = new TsOdinBuiltInType("typeid");
    public static final TsOdinBuiltInType ANY = new TsOdinBuiltInType("any");


    private static final Map<String, TsOdinBuiltInType> builtInTypeMap = new HashMap<>();

    static {
        builtInTypeMap.put(BOOL.getName(), BOOL);
        builtInTypeMap.put(B8.getName(), B8);
        builtInTypeMap.put(B16.getName(), B16);
        builtInTypeMap.put(B32.getName(), B32);
        builtInTypeMap.put(B64.getName(), B64);
        builtInTypeMap.put(BYTE.getName(), BYTE);
        builtInTypeMap.put(INT.getName(), INT);
        builtInTypeMap.put(I8.getName(), I8);
        builtInTypeMap.put(I16.getName(), I16);
        builtInTypeMap.put(I32.getName(), I32);
        builtInTypeMap.put(I64.getName(), I64);
        builtInTypeMap.put(I128.getName(), I128);
        builtInTypeMap.put(UINT.getName(), UINT);
        builtInTypeMap.put(U8.getName(), U8);
        builtInTypeMap.put(U16.getName(), U16);
        builtInTypeMap.put(U32.getName(), U32);
        builtInTypeMap.put(U64.getName(), U64);
        builtInTypeMap.put(U128.getName(), U128);
        builtInTypeMap.put(UINTPTR.getName(), UINTPTR);
        builtInTypeMap.put(I16LE.getName(), I16LE);
        builtInTypeMap.put(I32LE.getName(), I32LE);
        builtInTypeMap.put(I64LE.getName(), I64LE);
        builtInTypeMap.put(I128LE.getName(), I128LE);
        builtInTypeMap.put(U16LE.getName(), U16LE);
        builtInTypeMap.put(U32LE.getName(), U32LE);
        builtInTypeMap.put(U64LE.getName(), U64LE);
        builtInTypeMap.put(U128LE.getName(), U128LE);
        builtInTypeMap.put(I16BE.getName(), I16BE);
        builtInTypeMap.put(I32BE.getName(), I32BE);
        builtInTypeMap.put(I64BE.getName(), I64BE);
        builtInTypeMap.put(I128BE.getName(), I128BE);
        builtInTypeMap.put(U16BE.getName(), U16BE);
        builtInTypeMap.put(U32BE.getName(), U32BE);
        builtInTypeMap.put(U64BE.getName(), U64BE);
        builtInTypeMap.put(U128BE.getName(), U128BE);
        builtInTypeMap.put(F16.getName(), F16);
        builtInTypeMap.put(F32.getName(), F32);
        builtInTypeMap.put(F64.getName(), F64);
        builtInTypeMap.put(F16LE.getName(), F16LE);
        builtInTypeMap.put(F32LE.getName(), F32LE);
        builtInTypeMap.put(F64LE.getName(), F64LE);
        builtInTypeMap.put(F16BE.getName(), F16BE);
        builtInTypeMap.put(F32BE.getName(), F32BE);
        builtInTypeMap.put(F64BE.getName(), F64BE);
        builtInTypeMap.put(COMPLEX32.getName(), COMPLEX32);
        builtInTypeMap.put(COMPLEX64.getName(), COMPLEX64);
        builtInTypeMap.put(COMPLEX128.getName(), COMPLEX128);
        builtInTypeMap.put(QUATERNION64.getName(), QUATERNION64);
        builtInTypeMap.put(QUATERNION128.getName(), QUATERNION128);
        builtInTypeMap.put(QUATERNION256.getName(), QUATERNION256);
        builtInTypeMap.put(RUNE.getName(), RUNE);
        builtInTypeMap.put(STRING.getName(), STRING);
        builtInTypeMap.put(CSTRING.getName(), CSTRING);
        builtInTypeMap.put(RAWPTR.getName(), RAWPTR);
        builtInTypeMap.put(TYPEID.getName(), TYPEID);
        builtInTypeMap.put(ANY.getName(), ANY);
    }

    private TsOdinBuiltInType(String name) {
        this.name = name;
    }

    public static TsOdinBuiltInType getBuiltInType(String identifierText) {
        return builtInTypeMap.get(identifierText);
    }
}
