package com.lasagnerd.odin.codeInsight.typeInference;

import com.lasagnerd.odin.codeInsight.typeSystem.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
            supportedTypes.addAll(TsOdinBuiltInTypes.getNumericTypes());
        }

        // Untyped float
        {
            HashSet<TsOdinType> supportedTypes = new HashSet<>();
            UNTYPED_CONVERSION_TABLE.put(TsOdinBuiltInTypes.UNTYPED_STRING, supportedTypes);
            supportedTypes.add(TsOdinBuiltInTypes.STRING);
            supportedTypes.add(TsOdinBuiltInTypes.C_STRING);
        }


    }

    public static @NotNull TsOdinType inferTypeOfSymmetricalBinaryExpression(@NotNull TsOdinType a, @NotNull TsOdinType b) {
        if (a.isUnknown() || b.isUnknown())
            return TsOdinBuiltInTypes.UNKNOWN;


        a = a.baseType();
        b = b.baseType();

        // Easy case: the type kind is the same
        if (a.getTypeReferenceKind() == b.getTypeReferenceKind()) {
            if (OdinTypeChecker.checkTypesStrictly(a, b)) {
                TsOdinType baseTypeA = a.baseType(true);
                TsOdinType baseTypeB = b.baseType(true);
                if (baseTypeA instanceof TsOdinBitSetType && baseTypeB instanceof TsOdinBitSetType) {
                    return a;
                }

                if (baseTypeA instanceof TsOdinArrayType && baseTypeB instanceof TsOdinArrayType) {
                    return a;
                }
                if (!a.isUntyped() && !b.isUntyped())
                    return a;
            }
            return convertToTyped(a, b);
        }

        // Give array-l the precedence
        TsOdinType arrayType = tryConvertToArray(a, b);
        if (arrayType != null) return arrayType;

        TsOdinType matrixType = tryConvertToMatrix(a, b);
        if (matrixType != null) return matrixType;


        return TsOdinBuiltInTypes.UNKNOWN;
    }

    private static @Nullable TsOdinType tryConvertToMatrix(@NotNull TsOdinType a, @NotNull TsOdinType b) {
        TsOdinType tsOdinType = doTryConvertToMatrixType(a, b);
        if (tsOdinType != null) return tsOdinType;

        return doTryConvertToMatrixType(b, a);
    }

    private static @Nullable TsOdinType doTryConvertToMatrixType(@NotNull TsOdinType a, @NotNull TsOdinType b) {
        TsOdinType baseTypeA = a.baseType(true);
        if (baseTypeA.getTypeReferenceKind() == TsOdinTypeKind.MATRIX) {
            if (b.baseType() instanceof TsOdinArrayType arrayType) {
                TsOdinType tsOdinType = convertToMatchingArrayType((TsOdinMatrixType) baseTypeA, arrayType);
                if (!tsOdinType.isUnknown()) {
                    return tsOdinType;
                }
            }
            TsOdinType tsOdinType = convertToElementType((TsOdinMatrixType) baseTypeA, b);
            if (!tsOdinType.isUnknown()) {
                return a;
            }
        }
        return null;
    }

    private static TsOdinType convertToMatchingArrayType(TsOdinMatrixType matrixType, @NotNull TsOdinArrayType arrayType) {
        if (arrayType.getSize() != null
                && matrixType.getRows() != null && matrixType.getColumns() != null) {
            TsOdinArrayType tsOdinArrayType = new TsOdinArrayType();
            tsOdinArrayType.setElementType(arrayType.getElementType());
            tsOdinArrayType.setSize(matrixType.getRows());
            return tsOdinArrayType;
        }
        return TsOdinBuiltInTypes.UNKNOWN;
    }

    private static @Nullable TsOdinType tryConvertToArray(@NotNull TsOdinType a, @NotNull TsOdinType b) {
        TsOdinType baseTypeA = a.baseType(true);
        if (baseTypeA.getTypeReferenceKind() == TsOdinTypeKind.ARRAY) {
            TsOdinType tsOdinType = convertToElementType((TsOdinArrayType) baseTypeA, b);
            if (!tsOdinType.isUnknown()) {
                return a;
            }
        }

        TsOdinType baseTypeB = b.baseType(true);
        if (baseTypeB.getTypeReferenceKind() == TsOdinTypeKind.ARRAY) {
            TsOdinType tsOdinType = convertToElementType((TsOdinArrayType) baseTypeB, a);
            if (!tsOdinType.isUnknown()) {
                return b;
            }
        }
        return null;
    }

    private static TsOdinType convertToElementType(TsOdinElementOwner arrayType, TsOdinType builtInType) {
        if (arrayType.getElementType().baseType() instanceof TsOdinBuiltInType) {
            TsOdinType tsOdinType = convertToTyped(arrayType.getElementType().baseType(), builtInType);
            if (!tsOdinType.isUnknown()) {
                return arrayType;
            }
        }
        return TsOdinBuiltInTypes.UNKNOWN;
    }

    /**
     * Computes the resulting type of converting a -> b or b -> a.
     *
     * @param a First type
     * @param b Second type
     * @return The compatible type if successful, or UNKNOWN_TYPE if conversion fails.
     */
    public static TsOdinType convertToTyped(TsOdinType a, TsOdinType b) {
        if (a.isUntyped() && b.isUntyped()) {
            return convertUntypedTypes((TsOdinUntypedType) a, (TsOdinUntypedType) b);
        }

        if (a == b)
            return a;

        // Here we know that at least one is not untyped. Figure out which
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
        if (typedA.getTypeReferenceKind() != typedB.getTypeReferenceKind())
            return TsOdinBuiltInTypes.UNKNOWN;

        if (typedA.isNumeric()) {
            return max((TsOdinNumericType) typedA, (TsOdinNumericType) typedB);
        }


        return TsOdinBuiltInTypes.UNKNOWN;
    }

    private static TsOdinType max(TsOdinNumericType typedA, TsOdinNumericType typedB) {
        return Stream.of(typedA, typedB).max(NUMERIC_TYPE_COMPARATOR).get();
    }

    public static @NotNull TsOdinType convertToTyped(@NotNull TsOdinType tsOdinType) {
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
