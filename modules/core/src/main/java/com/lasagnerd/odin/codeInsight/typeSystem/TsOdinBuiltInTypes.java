package com.lasagnerd.odin.codeInsight.typeSystem;

import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.codeInsight.OdinContext;
import com.lasagnerd.odin.lang.psi.OdinDeclaration;
import com.lasagnerd.odin.lang.psi.OdinDeclaredIdentifier;

import java.util.*;

public class TsOdinBuiltInTypes {
    public static final TsOdinBuiltInType BOOL = new TsOdinBoolType("bool", 8);
    public static final TsOdinBuiltInType B8 = new TsOdinBoolType("b8", 8);
    public static final TsOdinBuiltInType B16 = new TsOdinBoolType("b16", 16);
    public static final TsOdinBuiltInType B32 = new TsOdinBoolType("b32", 32);
    public static final TsOdinBuiltInType B64 = new TsOdinBoolType("b64", 64);

    // TODO this is an alias, and doesn't need to be defined here. Add to symbols 
//    public static final TsOdinBuiltInType BYTE = new TsOdinBuiltInType("byte");

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
    public static final TsOdinBuiltInType NIL = new TsOdinBuiltInType("nil");

    public static final TsOdinUntypedType UNTYPED_INT = new TsOdinUntypedType("untyped int", TsOdinMetaType.MetaType.NUMERIC);
    public static final TsOdinUntypedType UNTYPED_BOOLEAN = new TsOdinUntypedType("untyped bool", TsOdinMetaType.MetaType.BOOL);
    public static final TsOdinUntypedType UNTYPED_RUNE = new TsOdinUntypedType("untyped rune", TsOdinMetaType.MetaType.RUNE);
    public static final TsOdinUntypedType UNTYPED_STRING = new TsOdinUntypedType("untyped string", TsOdinMetaType.MetaType.STRING);
    public static final TsOdinUntypedType UNTYPED_FLOAT = new TsOdinUntypedType("untyped float", TsOdinMetaType.MetaType.NUMERIC);
    public static final TsOdinUntypedType UNTYPED_COMPLEX = new TsOdinUntypedType("untyped complex", TsOdinMetaType.MetaType.NUMERIC);
    public static final TsOdinUntypedType UNTYPED_QUATERNION = new TsOdinUntypedType("untyped quaternion", TsOdinMetaType.MetaType.NUMERIC);

    public static final TsOdinBuiltinProc LEN = new TsOdinBuiltinProc("len");
    public static final TsOdinBuiltinProc CAP = new TsOdinBuiltinProc("cap");
    public static final TsOdinBuiltinProc SIZE_OF = new TsOdinBuiltinProc("size_of");
    public static final TsOdinBuiltinProc ALIGN_OF = new TsOdinBuiltinProc("align_of");
    public static final TsOdinBuiltinProc OFFSET_OF_SELECTOR = new TsOdinBuiltinProc("offset_of_selector");
    public static final TsOdinBuiltinProc OFFSET_OF_MEMBER = new TsOdinBuiltinProc("offset_of_member");
    public static final TsOdinBuiltinProc OFFSET_OF = new TsOdinBuiltinProc("offset_of");
    public static final TsOdinBuiltinProc OFFSET_OF_BY_STRING = new TsOdinBuiltinProc("offset_of_by_string");
    public static final TsOdinBuiltinProc TYPE_OF = new TsOdinBuiltinProc("type_of");
    public static final TsOdinBuiltinProc TYPE_INFO_OF = new TsOdinBuiltinProc("type_info_of");
    public static final TsOdinBuiltinProc TYPEID_OF = new TsOdinBuiltinProc("typeid_of");
    public static final TsOdinBuiltinProc SWIZZLE = new TsOdinBuiltinProc("swizzle");
    public static final TsOdinBuiltinProc COMPLEX = new TsOdinBuiltinProc("complex");
    public static final TsOdinBuiltinProc QUATERNION = new TsOdinBuiltinProc("quaternion");
    public static final TsOdinBuiltinProc REAL = new TsOdinBuiltinProc("real");
    public static final TsOdinBuiltinProc IMAG = new TsOdinBuiltinProc("imag");
    public static final TsOdinBuiltinProc JMAG = new TsOdinBuiltinProc("jmag");
    public static final TsOdinBuiltinProc KMAG = new TsOdinBuiltinProc("kmag");
    public static final TsOdinBuiltinProc CONJ = new TsOdinBuiltinProc("conj");
    public static final TsOdinBuiltinProc EXPAND_VALUES = new TsOdinBuiltinProc("expand_values");
    public static final TsOdinBuiltinProc MIN = new TsOdinBuiltinProc("min");
    public static final TsOdinBuiltinProc MAX = new TsOdinBuiltinProc("max");
    public static final TsOdinBuiltinProc ABS = new TsOdinBuiltinProc("abs");
    public static final TsOdinBuiltinProc CLAMP = new TsOdinBuiltinProc("clamp");
    public static final TsOdinBuiltinProc SOA_ZIP = new TsOdinBuiltinProc("soa_zip");
    public static final TsOdinBuiltinProc SOA_UNZIP = new TsOdinBuiltinProc("soa_unzip");
    public static final TsOdinBuiltinProc UNREACHABLE = new TsOdinBuiltinProc("unreachable");

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
            "nil"
    );


    public static final TsOdinType UNKNOWN = new TsOdinUnknownType();
    public static final TsOdinType UNDECIDED = new TsOdinUndecidedType();

    public static final TsOdinType VOID = new TsOdinVoidType();

    private static final Map<String, TsOdinBuiltInType> builtInTypeMap = new HashMap<>();
    private static List<TsOdinType> integerTypes;
    private static List<TsOdinType> numericTypes;
    private static List<TsOdinType> floatingPointTypes;
    private static List<TsOdinType> stringTypes;
    private static List<TsOdinType> boolTypes;
    private static List<TsOdinType> procedures;

    static {
        // Boolean types
        builtInTypeMap.put(BOOL.getName(), BOOL);
        builtInTypeMap.put(B8.getName(), B8);
        builtInTypeMap.put(B16.getName(), B16);
        builtInTypeMap.put(B32.getName(), B32);
        builtInTypeMap.put(B64.getName(), B64);

        // Numeric types
//        builtInTypeMap.put(BYTE.getName(), U8);
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

        // Nil "type"
        builtInTypeMap.put(NIL.getName(), NIL);

        // Procedures
        builtInTypeMap.put(LEN.getName(), LEN);
        builtInTypeMap.put(CAP.getName(), CAP);
        builtInTypeMap.put(SIZE_OF.getName(), SIZE_OF);
        builtInTypeMap.put(ALIGN_OF.getName(), ALIGN_OF);
        builtInTypeMap.put(OFFSET_OF_SELECTOR.getName(), OFFSET_OF_SELECTOR);
        builtInTypeMap.put(OFFSET_OF_MEMBER.getName(), OFFSET_OF_MEMBER);
        builtInTypeMap.put(OFFSET_OF.getName(), OFFSET_OF);
        builtInTypeMap.put(OFFSET_OF_BY_STRING.getName(), OFFSET_OF_BY_STRING);
        builtInTypeMap.put(TYPE_OF.getName(), TYPE_OF);
        builtInTypeMap.put(TYPE_INFO_OF.getName(), TYPE_INFO_OF);
        builtInTypeMap.put(TYPEID_OF.getName(), TYPEID_OF);
        builtInTypeMap.put(SWIZZLE.getName(), SWIZZLE);
        builtInTypeMap.put(COMPLEX.getName(), COMPLEX);
        builtInTypeMap.put(QUATERNION.getName(), QUATERNION);
        builtInTypeMap.put(REAL.getName(), REAL);
        builtInTypeMap.put(IMAG.getName(), IMAG);
        builtInTypeMap.put(JMAG.getName(), JMAG);
        builtInTypeMap.put(KMAG.getName(), KMAG);
        builtInTypeMap.put(CONJ.getName(), CONJ);
        builtInTypeMap.put(EXPAND_VALUES.getName(), EXPAND_VALUES);
        builtInTypeMap.put(MIN.getName(), MIN);
        builtInTypeMap.put(MAX.getName(), MAX);
        builtInTypeMap.put(ABS.getName(), ABS);
        builtInTypeMap.put(CLAMP.getName(), CLAMP);
        builtInTypeMap.put(SOA_ZIP.getName(), SOA_ZIP);
        builtInTypeMap.put(SOA_UNZIP.getName(), SOA_UNZIP);
        builtInTypeMap.put(UNREACHABLE.getName(), UNREACHABLE);
    }

    public static TsOdinTypeAlias createByteAlias(OdinDeclaredIdentifier declaredIdentifier) {
        TsOdinTypeAlias alias = new TsOdinTypeAlias();
        alias.setName("byte");
        alias.setAliasedType(U8);
        alias.setDeclaredIdentifier(declaredIdentifier);
        OdinDeclaration declaration = PsiTreeUtil.getParentOfType(declaredIdentifier, OdinDeclaration.class);
        alias.setDeclaration(declaration);
        alias.setDistinct(false);
        return alias;
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

    public static Collection<TsOdinType> getStringTypes() {
        if (stringTypes == null) {
            stringTypes = List.of(STRING, C_STRING, RUNE);
        }
        return stringTypes;
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

    public static Collection<TsOdinType> getBoolTypes() {
        if (boolTypes == null) {
            boolTypes = builtInTypeMap.values().stream()
                    .filter(t -> t instanceof TsOdinBoolType)
                    .map(t -> (TsOdinType) t)
                    .toList();
        }

        return boolTypes;
    }

    public static Collection<TsOdinType> getProcedures() {
        if (procedures == null) {
            procedures = builtInTypeMap.values().stream()
                    .filter(t -> t instanceof TsOdinNumericType)
                    .map(t -> (TsOdinType) t)
                    .toList();
        }

        return procedures;
    }

    public static Collection<TsOdinType> getFloatingPointTypes() {
        if (floatingPointTypes == null) {
            floatingPointTypes = builtInTypeMap.values().stream()
                    .filter(t -> t instanceof TsOdinNumericType n && n.isFloatingPoint())
                    .map(t -> (TsOdinType) t)
                    .toList();
        }

        return floatingPointTypes;
    }

    public static Collection<TsOdinType> getSimpleFloatingPointTypes() {
        return List.of(F16, F32, F64);
    }

    public static TsOdinType getBackingTypeOfComplexOrQuaternion(TsOdinType type) {
        if (!(type instanceof TsOdinNumericType numericType))
            return UNKNOWN;

        int length = numericType.getLength();
        if (numericType.isComplex()) {
            if (length == 32) {
                return F16;
            } else if (length == 64) {
                return F32;
            } else if (length == 128) {
                return F64;
            }
        } else if (numericType.isQuaternion()) {
            if (length == 64) {
                return F16;
            } else if (length == 128) {
                return F32;
            } else if (length == 256) {
                return F64;
            }
        }
        return TsOdinBuiltInTypes.UNKNOWN;
    }

    public static Collection<TsOdinType> getBuiltInTypes() {
        return new ArrayList<>(builtInTypeMap.values());
    }

    public static Collection<TsOdinType> getComplexTypes() {
        return List.of(COMPLEX32, COMPLEX64, COMPLEX128);
    }

    public static Collection<TsOdinType> getQuaternionTypes() {
        return List.of(QUATERNION64, QUATERNION128, QUATERNION256);
    }

    private static class TsOdinUnknownType extends TsOdinTypeBase {
        {
            this.context = new OdinContext();
        }

        @Override
        public String getLabel() {
            return "UNKNOWN";
        }

        @Override
        public TsOdinMetaType.MetaType getMetaType() {
            return TsOdinMetaType.MetaType.UNKNOWN;
        }

        @Override
        public String getName() {
            return "UNKNOWN";
        }

        @Override
        public boolean isUnknown() {
            return true;
        }
    }

    private static class TsOdinUndecidedType extends TsOdinTypeBase {
        {
            this.context = new OdinContext();
        }

        @Override
        public String getLabel() {
            return "UNDECIDED";
        }

        @Override
        public TsOdinMetaType.MetaType getMetaType() {
            return TsOdinMetaType.MetaType.UNDECIDED;
        }

        @Override
        public String getName() {
            return "UNDECIDED";
        }

        @Override
        public boolean isUndecided() {
            return true;
        }
    }

    private static class TsOdinVoidType extends TsOdinTypeBase {
        {
            this.context = new OdinContext();
        }

        @Override
        public String getName() {
            return "VOID";
        }

        @Override
        public String getLabel() {
            return "VOID";
        }

        @Override
        public TsOdinMetaType.MetaType getMetaType() {
            return TsOdinMetaType.MetaType.VOID;
        }
    }

}
