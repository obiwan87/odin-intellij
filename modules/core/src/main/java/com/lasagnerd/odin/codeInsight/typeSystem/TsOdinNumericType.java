package com.lasagnerd.odin.codeInsight.typeSystem;

import lombok.Getter;

@Getter
public class TsOdinNumericType extends TsOdinBuiltInType {
    private final int length;
    private final boolean floatingPoint;
    private final boolean integer;
    private final boolean complex;
    private final boolean quaternion;
    private final boolean signed;
    private final Endian endian;

    public boolean isScalar() {
        return !complex && !quaternion;
    }

    public enum Endian {
        UNSPECIFIED,
        LE,
        BE,
    }

    public TsOdinNumericType(String name, int length, boolean floatingPoint, boolean integer, boolean complex, boolean quaternion, boolean signed, Endian endian) {
        super(name);
        this.length = length;
        this.floatingPoint = floatingPoint;
        this.integer = integer;
        this.complex = complex;
        this.quaternion = quaternion;
        this.signed = signed;
        this.endian = endian;
    }

    @Override
    public TsOdinTypeKind getTypeReferenceKind() {
        return TsOdinTypeKind.NUMERIC;
    }
}
