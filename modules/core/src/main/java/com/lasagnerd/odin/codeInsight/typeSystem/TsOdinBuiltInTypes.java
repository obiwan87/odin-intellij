package com.lasagnerd.odin.codeInsight.typeSystem;

import java.util.*;

public class TsOdinBuiltInTypes {
    public static final TsOdinBuiltInType BOOL = new TsOdinBoolType("bool", 8);
    public static final TsOdinBuiltInType B8 = new TsOdinBoolType("b8", 8);
    public static final TsOdinBuiltInType B16 = new TsOdinBoolType("b16", 16);
    public static final TsOdinBuiltInType B32 = new TsOdinBoolType("b32", 32);
    public static final TsOdinBuiltInType B64 = new TsOdinBoolType("b64", 64);

    // TODO this is an alias, and doesn't need to be defined here. Add to symbols 
    public static final TsOdinBuiltInType BYTE = new TsOdinBuiltInType("byte");

    public static final TsOdinNumericType INT = new TsOdinNumericType("int", 32, false, true, false, false, true, TsOdinNumericType.Endian.UNSPECIFIED);
    public static final TsOdinNumericType I8 = new TsOdinNumericType("i8", 8, false, true, false, false, true, TsOdinNumericType.Endian.UNSPECIFIED);
    public static final TsOdinNumericType I16 = new TsOdinNumericType("i16", 16, false, true, false, false, true, TsOdinNumericType.Endian.UNSPECIFIED);
    public static final TsOdinNumericType I32 = new TsOdinNumericType("i32", 32, false, true, false, false, true, TsOdinNumericType.Endian.UNSPECIFIED);
    public static final TsOdinNumericType I64 = new TsOdinNumericType("i64", 64, false, true, false, false, true, TsOdinNumericType.Endian.UNSPECIFIED);
    public static final TsOdinNumericType I128 = new TsOdinNumericType("i128", 128, false, true, false, false, true, TsOdinNumericType.Endian.UNSPECIFIED);
    public static final TsOdinNumericType UINT = new TsOdinNumericType("uint", 32, false, true, false, false, false, TsOdinNumericType.Endian.UNSPECIFIED);
    public static final TsOdinNumericType U8 = new TsOdinNumericType("u8", 8, false, true, false, false, false, TsOdinNumericType.Endian.UNSPECIFIED);
    public static final TsOdinNumericType U16 = new TsOdinNumericType("u16", 16, false, true, false, false, false, TsOdinNumericType.Endian.UNSPECIFIED);
    public static final TsOdinNumericType U32 = new TsOdinNumericType("u32", 32, false, true, false, false, false, TsOdinNumericType.Endian.UNSPECIFIED);
    public static final TsOdinNumericType U64 = new TsOdinNumericType("u64", 64, false, true, false, false, false, TsOdinNumericType.Endian.UNSPECIFIED);
    public static final TsOdinNumericType U128 = new TsOdinNumericType("u128", 128, false, true, false, false, false, TsOdinNumericType.Endian.UNSPECIFIED);
    public static final TsOdinNumericType UINTPTR = new TsOdinNumericType("uintptr", 64, false, true, false, false, false, TsOdinNumericType.Endian.UNSPECIFIED);
    public static final TsOdinNumericType I16LE = new TsOdinNumericType("i16le", 16, false, true, false, false, true, TsOdinNumericType.Endian.LE);
    public static final TsOdinNumericType I32LE = new TsOdinNumericType("i32le", 32, false, true, false, false, true, TsOdinNumericType.Endian.LE);
    public static final TsOdinNumericType I64LE = new TsOdinNumericType("i64le", 64, false, true, false, false, true, TsOdinNumericType.Endian.LE);
    public static final TsOdinNumericType I128LE = new TsOdinNumericType("i128le", 128, false, true, false, false, true, TsOdinNumericType.Endian.LE);
    public static final TsOdinNumericType U16LE = new TsOdinNumericType("u16le", 16, false, true, false, false, false, TsOdinNumericType.Endian.LE);
    public static final TsOdinNumericType U32LE = new TsOdinNumericType("u32le", 32, false, true, false, false, false, TsOdinNumericType.Endian.LE);
    public static final TsOdinNumericType U64LE = new TsOdinNumericType("u64le", 64, false, true, false, false, false, TsOdinNumericType.Endian.LE);
    public static final TsOdinNumericType U128LE = new TsOdinNumericType("u128le", 128, false, true, false, false, false, TsOdinNumericType.Endian.LE);
    public static final TsOdinNumericType I16BE = new TsOdinNumericType("i16be", 16, false, true, false, false, true, TsOdinNumericType.Endian.BE);
    public static final TsOdinNumericType I32BE = new TsOdinNumericType("i32be", 32, false, true, false, false, true, TsOdinNumericType.Endian.BE);
    public static final TsOdinNumericType I64BE = new TsOdinNumericType("i64be", 64, false, true, false, false, true, TsOdinNumericType.Endian.BE);
    public static final TsOdinNumericType I128BE = new TsOdinNumericType("i128be", 128, false, true, false, false, true, TsOdinNumericType.Endian.BE);
    public static final TsOdinNumericType U16BE = new TsOdinNumericType("u16be", 16, false, true, false, false, false, TsOdinNumericType.Endian.BE);
    public static final TsOdinNumericType U32BE = new TsOdinNumericType("u32be", 32, false, true, false, false, false, TsOdinNumericType.Endian.BE);
    public static final TsOdinNumericType U64BE = new TsOdinNumericType("u64be", 64, false, true, false, false, false, TsOdinNumericType.Endian.BE);
    public static final TsOdinNumericType U128BE = new TsOdinNumericType("u128be", 128, false, true, false, false, false, TsOdinNumericType.Endian.BE);
    public static final TsOdinNumericType F16 = new TsOdinNumericType("f16", 16, true, false, false, false, false, TsOdinNumericType.Endian.UNSPECIFIED);
    public static final TsOdinNumericType F32 = new TsOdinNumericType("f32", 32, true, false, false, false, false, TsOdinNumericType.Endian.UNSPECIFIED);
    public static final TsOdinNumericType F64 = new TsOdinNumericType("f64", 64, true, false, false, false, false, TsOdinNumericType.Endian.UNSPECIFIED);
    public static final TsOdinNumericType F16LE = new TsOdinNumericType("f16le", 16, true, false, false, false, false, TsOdinNumericType.Endian.LE);
    public static final TsOdinNumericType F32LE = new TsOdinNumericType("f32le", 32, true, false, false, false, false, TsOdinNumericType.Endian.LE);
    public static final TsOdinNumericType F64LE = new TsOdinNumericType("f64le", 64, true, false, false, false, false, TsOdinNumericType.Endian.LE);
    public static final TsOdinNumericType F16BE = new TsOdinNumericType("f16be", 16, true, false, false, false, false, TsOdinNumericType.Endian.BE);
    public static final TsOdinNumericType F32BE = new TsOdinNumericType("f32be", 32, true, false, false, false, false, TsOdinNumericType.Endian.BE);
    public static final TsOdinNumericType F64BE = new TsOdinNumericType("f64be", 64, true, false, false, false, false, TsOdinNumericType.Endian.BE);
    public static final TsOdinNumericType COMPLEX32 = new TsOdinNumericType("complex32", 32, true, false, true, false, false, TsOdinNumericType.Endian.UNSPECIFIED);
    public static final TsOdinNumericType COMPLEX64 = new TsOdinNumericType("complex64", 64, true, false, true, false, false, TsOdinNumericType.Endian.UNSPECIFIED);
    public static final TsOdinNumericType COMPLEX128 = new TsOdinNumericType("complex128", 128, true, false, true, false, false, TsOdinNumericType.Endian.UNSPECIFIED);
    public static final TsOdinNumericType QUATERNION64 = new TsOdinNumericType("quaternion64", 64, true, false, false, true, false, TsOdinNumericType.Endian.UNSPECIFIED);
    public static final TsOdinNumericType QUATERNION128 = new TsOdinNumericType("quaternion128", 128, true, false, false, true, false, TsOdinNumericType.Endian.UNSPECIFIED);
    public static final TsOdinNumericType QUATERNION256 = new TsOdinNumericType("quaternion256", 256, true, false, false, true, false, TsOdinNumericType.Endian.UNSPECIFIED);

    public static final TsOdinBuiltInType RUNE = new TsOdinRuneType();
    public static final TsOdinBuiltInType STRING = new TsOdinStringType("string");
    public static final TsOdinBuiltInType C_STRING = new TsOdinStringType("cstring");

    public static final TsOdinBuiltInType RAW_PTR = new TsOdinRawPointerType();

    public static final TsOdinBuiltInType TYPEID = new TsOdinBuiltInType("typeid");
    public static final TsOdinBuiltInType ANY = new TsOdinBuiltInType("any");

    public static final TsOdinUntypedType UNTYPED_INT = new TsOdinUntypedType("untyped int", TsOdinMetaType.MetaType.NUMERIC);
    public static final TsOdinUntypedType UNTYPED_BOOLEAN = new TsOdinUntypedType("untyped bool", TsOdinMetaType.MetaType.BOOL);
    public static final TsOdinUntypedType UNTYPED_RUNE = new TsOdinUntypedType("untyped rune", TsOdinMetaType.MetaType.RUNE);
    public static final TsOdinUntypedType UNTYPED_STRING = new TsOdinUntypedType("untyped string", TsOdinMetaType.MetaType.STRING);
    public static final TsOdinUntypedType UNTYPED_FLOAT = new TsOdinUntypedType("untyped float", TsOdinMetaType.MetaType.NUMERIC);
    public static final TsOdinUntypedType UNTYPED_COMPLEX = new TsOdinUntypedType("untyped complex", TsOdinMetaType.MetaType.NUMERIC);
    public static final TsOdinUntypedType UNTYPED_QUATERNION = new TsOdinUntypedType("untyped quaternion", TsOdinMetaType.MetaType.NUMERIC);

    public static final List<String> RESERVED_TYPES = List.of(
            "bool",
            "b8",
            "b16",
            "b32",
            "b64",
            "byte",
            "int",
            "i8",
            "i16",
            "i32",
            "i64",
            "i128",
            "uint",
            "u8",
            "u16",
            "u32",
            "u64",
            "u128",
            "uintptr",
            "i16le",
            "i32le",
            "i64le",
            "i128le",
            "u16le",
            "u32le",
            "u64le",
            "u128le",
            "i16be",
            "i32be",
            "i64be",
            "i128be",
            "u16be",
            "u32be",
            "u64be",
            "u128be",
            "f16",
            "f32",
            "f64",
            "f16le",
            "f32le",
            "f64le",
            "f16be",
            "f32be",
            "f64be",
            "complex32",
            "complex64",
            "complex128",
            "quaternion64",
            "quaternion128",
            "quaternion256",
            "rune",
            "string",
            "cstring",
            "rawptr",
            "typeid",
            "any"
    );
    private static final Map<String, TsOdinBuiltInType> builtInTypeMap = new HashMap<>();
    private static List<TsOdinType> integerTypes;
    private static List<TsOdinType> numericTypes;
    private static List<TsOdinType> floatingPointTypes;

    static {
        // Boolean types
        builtInTypeMap.put(BOOL.getName(), BOOL);
        builtInTypeMap.put(B8.getName(), B8);
        builtInTypeMap.put(B16.getName(), B16);
        builtInTypeMap.put(B32.getName(), B32);
        builtInTypeMap.put(B64.getName(), B64);

        // Numeric types
        builtInTypeMap.put(BYTE.getName(), U8);
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

        // Rune
        builtInTypeMap.put(RUNE.getName(), RUNE);

        // String types
        builtInTypeMap.put(STRING.getName(), STRING);
        builtInTypeMap.put(C_STRING.getName(), C_STRING);

        // Raw pointer
        builtInTypeMap.put(RAW_PTR.getName(), RAW_PTR);

        // Type ID
        builtInTypeMap.put(TYPEID.getName(), TYPEID);

        //Any
        builtInTypeMap.put(ANY.getName(), ANY);
    }

    public static TsOdinBuiltInType getBuiltInType(String identifierText) {
        return builtInTypeMap.get(identifierText);
    }

    public static Collection<TsOdinType> getIntegerTypes() {
        if (integerTypes == null) {
            integerTypes = builtInTypeMap.values().stream()
                    .filter(t -> t instanceof TsOdinNumericType)
                    .map(TsOdinNumericType.class::cast)
                    .filter(TsOdinNumericType::isInteger)
                    .map(t -> (TsOdinType) t)
                    .toList();
        }

        return integerTypes;
    }

    public static Collection<TsOdinType> getNumericTypes() {
        if (numericTypes == null) {
            numericTypes = builtInTypeMap.values().stream()
                    .filter(t -> t instanceof TsOdinNumericType)
                    .map(t -> (TsOdinType) t)
                    .toList();
        }

        return numericTypes;
    }

    public static Collection<TsOdinType> getFloatingPointTypes() {
        if (floatingPointTypes == null) {
            floatingPointTypes = builtInTypeMap.values().stream()
                    .filter(t -> t instanceof TsOdinNumericType)
                    .map(t -> (TsOdinType) t)
                    .toList();
        }

        return floatingPointTypes;
    }

    public static Collection<TsOdinType> getBuiltInTypes() {
        return new ArrayList<>(builtInTypeMap.values());
    }
}
