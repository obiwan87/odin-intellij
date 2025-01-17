package com.lasagnerd.odin.codeInsight.typeInference;

import com.intellij.psi.tree.IElementType;
import com.lasagnerd.odin.codeInsight.typeSystem.*;
import com.lasagnerd.odin.lang.psi.OdinPsiUtil;
import com.lasagnerd.odin.lang.psi.OdinTypes;
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

    public static @NotNull TsOdinType inferTypeOfSymmetricalBinaryExpression(@NotNull TsOdinType a,
                                                                             @NotNull TsOdinType b,
                                                                             IElementType operator) {
        if (a.isUnknown() || b.isUnknown())
            return TsOdinBuiltInTypes.UNKNOWN;


        a = a.baseType();
        b = b.baseType();

        // The type kind is the same
        if (a.getTypeReferenceKind() == b.getTypeReferenceKind()) {
            // typed types which match exactly
            if (!a.isUntyped() && !b.isUntyped()
                    && OdinTypeChecker.checkTypesStrictly(a, b)) {
                TsOdinType baseTypeA = a.baseType(true);
                TsOdinType baseTypeB = b.baseType(true);

                if (baseTypeA instanceof TsOdinBitSetType && baseTypeB instanceof TsOdinBitSetType) {
                    if (OdinPsiUtil.ENUM_BITWISE_OPERATORS.contains(operator)) {
                        return a;
                    }
                    return TsOdinBuiltInTypes.UNKNOWN;
                } else if (baseTypeA instanceof TsOdinEnumType && baseTypeB instanceof TsOdinEnumType) {
                    if (OdinPsiUtil.ENUM_BITWISE_OPERATORS.contains(operator) || OdinPsiUtil.ENUM_ARITHMETIC_OPERATORS.contains(operator)) {
                        return a;
                    }
                    return TsOdinBuiltInTypes.UNKNOWN;
                } else if (baseTypeA instanceof TsOdinArrayType arrayTypeA && baseTypeB instanceof TsOdinArrayType arrayTypeB) {
                    if (OdinPsiUtil.ARRAY_ARITHMETIC_OPERATORS.contains(operator)) {
                        // For well-defined arrays only return value if their size matches
                        if (arrayTypeA.getSize() != null && arrayTypeB.getSize() != null) {
                            if (arrayTypeA.getSize().equals(arrayTypeB.getSize())) {
                                return a;
                            }

                        }
                        // Otherwise, we are missing size information. Return just the left operand
                        else {
                            return a;
                        }
                    }
                    return TsOdinBuiltInTypes.UNKNOWN;
                } else if (baseTypeA instanceof TsOdinMatrixType matrixTypeA && baseTypeB instanceof TsOdinMatrixType matrixTypeB) {
                    if (OdinPsiUtil.MATRIX_ARITHMETIC_OPERATORS.contains(operator)) {
                        if (matrixTypeA.sizeKnown() && matrixTypeB.sizeKnown()) {
                            if (matrixTypeA.getRows().equals(matrixTypeB.getRows()) && matrixTypeA.getColumns().equals(matrixTypeB.getColumns())) {
                                return matrixTypeA;
                            }
                        } else {
                            return matrixTypeA;
                        }
                    }
                    return TsOdinBuiltInTypes.UNKNOWN;
                } else {
                    if (baseTypeA.getTypeReferenceKind() == TsOdinTypeKind.STRING) {
                        if (OdinPsiUtil.STRING_ARITHMETIC_OPERATORS.contains(operator)) {
                            return a;
                        }
                    } else if (baseTypeA instanceof TsOdinNumericType numericType) {
                        if (numericType.isInteger()) {
                            if (OdinPsiUtil.INTEGER_ARITHMETIC_OPERATORS.contains(operator)
                                    || OdinPsiUtil.INTEGER_BITWISE_OPERATORS.contains(operator)) {
                                return a;
                            }
                        } else if (numericType.isFloatingPoint() && numericType.isScalar()) {
                            if (OdinPsiUtil.FLOAT_ARITHMETIC_OPERATORS.contains(operator)) {
                                return a;
                            }
                        } else if (!numericType.isScalar()) {
                            if (OdinPsiUtil.ARRAY_ARITHMETIC_OPERATORS.contains(operator)) {
                                return a;
                            }
                        }
                    }

                    return a;
                }
            }
            // Here we know that the referenced type is the same but one of the types must be untyped

            // Find common typed type and check if that is compatible with the passed operator
            TsOdinType typedType = convertToTyped(a, b);
            if (!typedType.isUnknown()) {
                return inferTypeOfSymmetricalBinaryExpression(typedType, typedType, operator);
            }
            return typedType;
        }

        // Give array-l the precedence
        TsOdinType arrayType = tryConvertToArray(a, b, operator);
        if (arrayType != null) return arrayType;

        TsOdinType matrixType = tryConvertToMatrix(a, b, operator);
        if (matrixType != null) return matrixType;


        return TsOdinBuiltInTypes.UNKNOWN;
    }

    private static @Nullable TsOdinType tryConvertToMatrix(@NotNull TsOdinType a,
                                                           @NotNull TsOdinType b,
                                                           IElementType operator) {
        if (!OdinPsiUtil.MATRIX_ARITHMETIC_OPERATORS.contains(operator))
            return null;

        TsOdinType baseTypeA = a.baseType(true);
        if (baseTypeA.getTypeReferenceKind() == TsOdinTypeKind.MATRIX) {
            TsOdinMatrixType matrixType = (TsOdinMatrixType) baseTypeA;

            // Matrix * Vector
            if (operator == OdinTypes.STAR && b.baseType() instanceof TsOdinArrayType arrayType) {
                if (matrixType.isSquare()) {
                    TsOdinType tsOdinType = convertToMatchingArrayType(arrayType, matrixType.getColumns());
                    if (!tsOdinType.isUnknown()) {
                        return tsOdinType;
                    }
                } else {
                    if (matrixType.getColumns() != null && matrixType.getColumns().equals(arrayType.getSize())) {
                        TsOdinMatrixType resultingMatrixType = new TsOdinMatrixType();
                        resultingMatrixType.setRows(matrixType.getColumns());
                        resultingMatrixType.setColumns(1);
                        resultingMatrixType.setElementType(matrixType.getElementType());
                        return resultingMatrixType;
                    }
                }
            }
            // M op Scalar -> When one of the operands is an element type, the result type is always the matrix type
            TsOdinType tsOdinType = convertToElementType(matrixType, b);
            if (!tsOdinType.isUnknown()) {
                return a;
            }
        }

        TsOdinType baseTypeB = b.baseType(true);
        if (baseTypeB.getTypeReferenceKind() == TsOdinTypeKind.MATRIX) {
            TsOdinMatrixType matrixType = (TsOdinMatrixType) baseTypeB;

            // Matrix * Vector
            if (operator == OdinTypes.STAR && a.baseType() instanceof TsOdinArrayType arrayType) {
                if (matrixType.isSquare()) {
                    TsOdinType tsOdinType = convertToMatchingArrayType(arrayType, matrixType.getRows());
                    if (!tsOdinType.isUnknown()) {
                        return tsOdinType;
                    }
                } else {
                    if (matrixType.getRows() != null && matrixType.getRows().equals(arrayType.getSize())) {
                        TsOdinMatrixType resultingMatrixType = new TsOdinMatrixType();
                        resultingMatrixType.setColumns(matrixType.getRows());
                        resultingMatrixType.setRows(1);
                        resultingMatrixType.setElementType(matrixType.getElementType());
                        return resultingMatrixType;
                    }
                }
            }

            // Scalar op Matrix -> When one of the operands is an element type, the result type is always the matrix type
            TsOdinType tsOdinType = convertToElementType(matrixType, a);
            if (!tsOdinType.isUnknown()) {
                return b;
            }
        }
        return null;
    }

    private static TsOdinType convertToMatchingArrayType(@NotNull TsOdinArrayType arrayType, Integer matrixSize) {
        if (arrayType.getSize() != null && arrayType.getSize().equals(matrixSize)) {
            TsOdinArrayType tsOdinArrayType = new TsOdinArrayType();
            tsOdinArrayType.setElementType(arrayType.getElementType());
            tsOdinArrayType.setSize(matrixSize);
            return tsOdinArrayType;
        }
        return TsOdinBuiltInTypes.UNKNOWN;
    }

    private static @Nullable TsOdinType tryConvertToArray(@NotNull TsOdinType a, @NotNull TsOdinType b, IElementType operator) {
        if (!OdinPsiUtil.ARRAY_ARITHMETIC_OPERATORS.contains(operator))
            return null;

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
