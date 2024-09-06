package com.lasagnerd.odin.codeInsight.symbols;

public enum OdinSymbolType {
    UNKNOWN,
    BUILTIN_TYPE,
    PARAMETER,
    FIELD,
    PROCEDURE,
    PROCEDURE_OVERLOAD,
    STRUCT,
    UNION,
    ENUM_FIELD,
    ENUM,
    CONSTANT,
    VARIABLE,
    PACKAGE_REFERENCE,
    POLYMORPHIC_TYPE,
    LABEL,
    FOREIGN_IMPORT,
    SWIZZLE_FIELD,
    BIT_SET
}
