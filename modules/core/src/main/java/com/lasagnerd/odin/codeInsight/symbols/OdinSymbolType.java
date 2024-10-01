package com.lasagnerd.odin.codeInsight.symbols;


import lombok.Getter;

@Getter
public enum OdinSymbolType {
    UNKNOWN(false),
    BUILTIN_TYPE(true),
    PARAMETER(false),
    FIELD(false),
    PROCEDURE(true),
    PROCEDURE_OVERLOAD(true),
    STRUCT(true),
    UNION(true),
    ENUM_FIELD(false),
    ENUM(true),
    CONSTANT(false),
    VARIABLE(false),
    PACKAGE_REFERENCE(true),
    POLYMORPHIC_TYPE(true),
    LABEL(false),
    FOREIGN_IMPORT(false),
    SWIZZLE_FIELD(false),
    SOA_FIELD(false),
    BIT_FIELD(true),
    BIT_SET(true);

    private final boolean type;

    OdinSymbolType(boolean type) {
        this.type = type;
    }
}
