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
            return TsOdinBuiltInTypes.UNKNOWN;

        a = a.baseType();
        b = b.baseType();

        // Easy case: the type kind is the same
        if (a.getMetaType() == b.getMetaType()) {
            if (OdinTypeChecker.checkTypesStrictly(a, b)) {
                TsOdinType baseTypeA = a.baseType(true);
                TsOdinType baseTypeB = b.baseType(true);
                if (baseTypeA instanceof TsOdinBitSetType && baseTypeB instanceof TsOdinBitSetType) {
                    return a;
                }

                if (baseTypeA instanceof TsOdinArrayType && baseTypeB instanceof TsOdinArrayType) {
                    return a;
                }
            }
            return convertToTyped(a, b);
        }

        // Give arrays the precedence
        TsOdinType baseTypeA = a.baseType(true);
        if (baseTypeA.getMetaType() == MetaType.ARRAY) {
            TsOdinType tsOdinType = convertToArrayType((TsOdinArrayType) baseTypeA, b);
            if(!tsOdinType.isUnknown()) {
                return a;
            }
        }

        TsOdinType baseTypeB = b.baseType(true);
        if (baseTypeB.getMetaType() == MetaType.ARRAY) {
            TsOdinType tsOdinType = convertToArrayType((TsOdinArrayType) baseTypeB, a);
            if(!tsOdinType.isUnknown()) {
                return b;
            }
        }


        return TsOdinBuiltInTypes.UNKNOWN;
    }

    private static TsOdinType convertToArrayType(TsOdinArrayType arrayType, TsOdinType builtInType) {
        if (arrayType.getElementType() instanceof TsOdinBuiltInType) {
            TsOdinType tsOdinType = convertToTyped(arrayType.getElementType(), builtInType);
            if (!tsOdinType.isUnknown()) {
                return arrayType;
            }
        }
        return TsOdinBuiltInTypes.UNKNOWN;
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
            return TsOdinBuiltInTypes.UNKNOWN;
        }

        if (a.isUntyped() || b.isUntyped())
            if (UNTYPED_CONVERSION_TABLE.getOrDefault(untypedType, Collections.emptySet())
                    .contains(typed)) {
                return typed;
            }

        return TsOdinBuiltInTypes.UNKNOWN;
    }

    private static TsOdinType convertUntypedTypes(TsOdinUntypedType untypedA, TsOdinUntypedType untypedB) {
        TsOdinType typedA = convertToTyped(untypedA);
        TsOdinType typedB = convertToTyped(untypedB);

        // In case of string vs rune vs numeric
        if (typedA.getMetaType() != typedB.getMetaType())
            return TsOdinBuiltInTypes.UNKNOWN;

        if (typedA.isNumeric()) {
            return max((TsOdinNumericType) typedA, (TsOdinNumericType) typedB);
        }


        return TsOdinBuiltInTypes.UNKNOWN;
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
