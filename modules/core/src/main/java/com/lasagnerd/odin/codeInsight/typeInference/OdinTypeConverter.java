package com.lasagnerd.odin.codeInsight.typeInference;

import com.lasagnerd.odin.codeInsight.typeSystem.*;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinMetaType.MetaType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

public class OdinTypeConverter {

    private static final Map<TsOdinUntypedType, Set<TsOdinType>> UNTYPED_CONVERSION_TABLE = new HashMap<>();
    private static final Comparator<TsOdinNumericType> NUMERIC_TYPE_COMPARATOR = Comparator
            .comparing(TsOdinNumericType::isQuaternion)
            .thenComparing(TsOdinNumericType::isComplex)
            .thenComparing(TsOdinNumericType::isFloatingPoint)
            .thenComparing(TsOdinNumericType::getLength);

    static {
        // Untyped Int
        {
            HashSet<TsOdinType> supportedTypes = new HashSet<>();
            UNTYPED_CONVERSION_TABLE.put(TsOdinBuiltInTypes.UNTYPED_INT, supportedTypes);
            supportedTypes.addAll(TsOdinBuiltInTypes.getNumericTypes());
        }

        // Untyped float
        {
            HashSet<TsOdinType> supportedTypes = new HashSet<>();
            UNTYPED_CONVERSION_TABLE.put(TsOdinBuiltInTypes.UNTYPED_FLOAT, supportedTypes);
            supportedTypes.addAll(TsOdinBuiltInTypes.getFloatingPointTypes());
        }

        // Untyped float
        {
            HashSet<TsOdinType> supportedTypes = new HashSet<>();
            UNTYPED_CONVERSION_TABLE.put(TsOdinBuiltInTypes.UNTYPED_STRING, supportedTypes);
            supportedTypes.add(TsOdinBuiltInTypes.STRING);
            supportedTypes.add(TsOdinBuiltInTypes.C_STRING);
        }


    }

    public static @NotNull TsOdinType inferTypeOfBinaryExpression(@NotNull TsOdinType a, @NotNull TsOdinType b) {
        if (a.isUnknown() || b.isUnknown())
            return TsOdinType.UNKNOWN;

        // Easy case: the type kind is the same
        if (a.getMetaType() == b.getMetaType()) {
            if (a instanceof TsOdinBitSetType bitSetA && b instanceof TsOdinBitSetType bitSetB) {
                if (bitSetA.getElementType() == bitSetB.getElementType())
                    return bitSetA;
            }

            if (a instanceof TsOdinArrayType arrayTypeA && b instanceof TsOdinArrayType arrayTypeB) {
                if (arrayTypeA.getElementType() == arrayTypeB.getElementType())
                    return arrayTypeA;
            }

            return convertToTyped(a, b);
        }

        // Give arrays the precedence
        if (a.getMetaType() == MetaType.ARRAY) {
            return convertToArrayType((TsOdinArrayType) a, b);
        }

        if (b.getMetaType() == MetaType.ARRAY) {
            return convertToArrayType((TsOdinArrayType) b, a);
        }


        return TsOdinType.UNKNOWN;
    }

    private static TsOdinType convertToArrayType(TsOdinArrayType arrayType, TsOdinType builtInType) {
        if (arrayType.getElementType() instanceof TsOdinBuiltInType) {
            TsOdinType tsOdinType = convertToTyped(arrayType.getElementType(), builtInType);
            if (!tsOdinType.isUnknown()) {
                return arrayType;
            }
        }
        return TsOdinType.UNKNOWN;
    }

    public static TsOdinType convertToTyped(TsOdinType a, TsOdinType b) {
        if (a.isUntyped() && b.isUntyped()) {
            return convertUntypedTypes((TsOdinUntypedType) a, (TsOdinUntypedType) b);
        }

        if (a == b)
            return a;

        // Here we know that 'b' is untyped and 'a' isn't
        TsOdinUntypedType untypedType;
        TsOdinType typed;

        if (a.isUntyped()) {
            untypedType = (TsOdinUntypedType) a;
            typed = b;
        } else if (b.isUntyped()) {
            untypedType = (TsOdinUntypedType) b;
            typed = a;
        } else {
            return TsOdinType.UNKNOWN;
        }

        if (a.isUntyped() || b.isUntyped())
            if (UNTYPED_CONVERSION_TABLE.getOrDefault(untypedType, Collections.emptySet())
                    .contains(typed)) {
                return typed;
            }

        return TsOdinType.UNKNOWN;
    }

    private static TsOdinType convertUntypedTypes(TsOdinUntypedType untypedA, TsOdinUntypedType untypedB) {
        TsOdinType typedA = convertToTyped(untypedA);
        TsOdinType typedB = convertToTyped(untypedB);

        // In case of string vs rune vs numeric
        if (typedA.getMetaType() != typedB.getMetaType())
            return TsOdinType.UNKNOWN;

        if (typedA.isNumeric()) {
            return max((TsOdinNumericType) typedA, (TsOdinNumericType) typedB);
        }


        return TsOdinType.UNKNOWN;
    }

    private static TsOdinType max(TsOdinNumericType typedA, TsOdinNumericType typedB) {
        return Stream.of(typedA, typedB).max(NUMERIC_TYPE_COMPARATOR).get();
    }

    public static TsOdinType convertToTyped(TsOdinType tsOdinType) {
        if (tsOdinType instanceof TsOdinBuiltInType) {
            if (tsOdinType == TsOdinBuiltInTypes.UNTYPED_STRING) {
                return TsOdinBuiltInTypes.STRING;
            }

            if (tsOdinType == TsOdinBuiltInTypes.UNTYPED_RUNE) {
                return TsOdinBuiltInTypes.RUNE;
            }

            if (tsOdinType == TsOdinBuiltInTypes.UNTYPED_INT) {
                return TsOdinBuiltInTypes.INT;
            }

            if (tsOdinType == TsOdinBuiltInTypes.UNTYPED_COMPLEX) {
                return TsOdinBuiltInTypes.COMPLEX128;
            }

            if (tsOdinType == TsOdinBuiltInTypes.UNTYPED_QUATERNION) {
                return TsOdinBuiltInTypes.QUATERNION256;
            }

            if (tsOdinType == TsOdinBuiltInTypes.UNTYPED_BOOLEAN) {
                return TsOdinBuiltInTypes.BOOL;
            }

            if (tsOdinType == TsOdinBuiltInTypes.UNTYPED_FLOAT) {
                return TsOdinBuiltInTypes.F64;
            }
        }

        return tsOdinType;
    }
}
