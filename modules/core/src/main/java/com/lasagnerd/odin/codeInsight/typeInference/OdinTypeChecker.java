package com.lasagnerd.odin.codeInsight.typeInference;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.codeInsight.typeSystem.*;
import com.lasagnerd.odin.lang.psi.*;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class OdinTypeChecker {

    public static boolean checkTypesStrictly(TsOdinType argumentType, TsOdinType parameterType) {
        final TsOdinType argumentBaseType = argumentType.baseType();
        final TsOdinType parameterBaseType = parameterType.baseType();

        if (argumentBaseType == parameterBaseType) {
            return true;
        }

        if (parameterBaseType instanceof TsOdinPolymorphicType) {
            return true;
        }

        if (argumentBaseType.getPsiType() != null && parameterBaseType.getPsiType() != null) {
            if (argumentBaseType.getPsiType() == parameterBaseType.getPsiType())
                return true;
        }

        if (argumentBaseType.getPsiTypeExpression() != null && parameterBaseType.getPsiTypeExpression() != null) {
            if (argumentBaseType.getPsiType() == parameterBaseType.getPsiType())
                return true;
        }

        if (parameterBaseType.isTypeId() && argumentType instanceof TsOdinMetaType) {
            return true;
        }

        if (parameterType instanceof TsOdinConstrainedType constrainedType && argumentType instanceof TsOdinMetaType metaType) {
            if (constrainedType.getMainType().isTypeId()) {
                return checkConstrainedType(constrainedType, metaType.representedType());
            }
        }

        if (argumentBaseType instanceof TsOdinArrayType argArrayType
                && parameterBaseType instanceof TsOdinArrayType parArrayType) {
            if (argArrayType.isSoa() == parArrayType.isSoa() && argArrayType.isSimd() == parArrayType.isSimd()) {
                OdinArraySize argSizeElement = argArrayType.getPsiSizeElement();
                OdinArraySize parSizeElement = parArrayType.getPsiSizeElement();
                if (parSizeElement != null
                        && argSizeElement != null
                        && parSizeElement.getExpression() instanceof OdinLiteralExpression
                        && argSizeElement.getExpression() instanceof OdinLiteralExpression) {

                    if (PsiEquivalenceUtil.areElementsEquivalent(argArrayType.getPsiSizeElement(), parArrayType.getPsiSizeElement())) {
                        return checkTypesStrictly(argArrayType.getElementType(), parArrayType.getElementType());
                    }
                    return false;
                }
                return checkTypesStrictly(argArrayType.getElementType(), parArrayType.getElementType());
            }
            return true;
        }

        if (argumentBaseType instanceof TsOdinSliceType argSliceType
                && parameterBaseType instanceof TsOdinSliceType parSliceType) {
            if (argSliceType.isSoa() == parSliceType.isSoa()) {
                return checkTypesStrictly(argSliceType.getElementType(), parSliceType.getElementType());
            }
            return false;
        }

        if (argumentBaseType instanceof TsOdinMatrixType argMatrixType
                && parameterBaseType instanceof TsOdinMatrixType parMatrixType) {
            return checkTypesStrictly(argMatrixType.getElementType(), parMatrixType.getElementType());
        }

        if (argumentBaseType instanceof TsOdinDynamicArray argDynArray
                && parameterBaseType instanceof TsOdinDynamicArray parDynArray) {
            if (argDynArray.isSoa() == parDynArray.isSoa()) {
                return checkTypesStrictly(argDynArray.getElementType(), parDynArray.getElementType());
            }
            return false;
        }

        return false;
    }

    /**
     * Checks if a type is compatible with a constrained type
     *
     * @param constrainedType the constrained type
     * @param typeToCheck     the type to check
     * @return true if compatible, false otherwise
     */
    public static boolean checkConstrainedType(TsOdinConstrainedType constrainedType, TsOdinType typeToCheck) {
        Map<String, TsOdinType> resolvedTypes = OdinTypeSpecializer.substituteTypes(typeToCheck.baseType(),
                constrainedType.getSpecializedType().baseType());

        Collection<OdinPolymorphicType> polymorphicTypes = PsiTreeUtil.findChildrenOfType(constrainedType.getPsiType(), OdinPolymorphicType.class);
        boolean solved = true;
        for (OdinPolymorphicType polymorphicType : polymorphicTypes) {
            String name = polymorphicType.getDeclaredIdentifier().getName();
            if (!resolvedTypes.containsKey(name)) {
                solved = false;
                break;
            }
        }
        return solved;
    }

    public enum ConversionAction {
        PRIMITIVE_TYPE,
        TO_ARRAY,
        TO_MATRIX,
        TO_RAW_POINTER,
        MP_TO_POINTER,
        POINTER_TO_MP,
        TO_ANY,
        USING_SUBTYPE,
        TO_UNION_VARIANT,
        FLOAT_TO_COMPLEX,
        FLOAT_TO_QUATERNION,
        COMPLEX_TO_QUATERNION,
    }

    @Data
    public static class TypeCheckResult {
        private boolean compatible;
        private List<ConversionAction> conversionActionList = new ArrayList<>();
    }


    TypeCheckResult typeCheckResult = new TypeCheckResult();

    public static TypeCheckResult checkTypes(TsOdinType type, TsOdinType expectedType) {
        return checkTypes(type, expectedType, false);
    }

    public static TypeCheckResult checkTypes(TsOdinType type, TsOdinType expectedType, boolean expectAnyInt) {
        OdinTypeChecker odinTypeChecker = new OdinTypeChecker();
        odinTypeChecker.doCheckTypes(type, expectedType, expectAnyInt);
        return odinTypeChecker.typeCheckResult;
    }

    public void doCheckTypes(TsOdinType type, TsOdinType expectedType) {
        doCheckTypes(type, expectedType, false);
    }

    public void doCheckTypes(TsOdinType type, TsOdinType expectedType, boolean expectAnyInt) {

        type = convertToTyped(type, expectedType);

        if (TsOdinBuiltInTypes.getIntegerTypes().contains(type) && TsOdinBuiltInTypes.getIntegerTypes().contains(expectedType) && expectAnyInt) {
            typeCheckResult.setCompatible(true);
            return;
        }

        if (type instanceof TsOdinConstrainedType constrainedType && expectedType instanceof TsOdinConstrainedType parConstrainedType) {
            doCheckTypes(constrainedType.getSpecializedType(), parConstrainedType.getSpecializedType());
            return;
        }

        if (type instanceof TsOdinPointerType pointerType && expectedType instanceof TsOdinPointerType expectedPointerType) {
            doCheckTypes(pointerType.getDereferencedType(), expectedPointerType.getDereferencedType());
            return;
        }

        if (checkTypesStrictly(type, expectedType)) {
            typeCheckResult.setCompatible(true);
            return;
        }

        if (expectedType instanceof TsOdinPolymorphicType) {
            typeCheckResult.setCompatible(true);
            return;
        }

        TsOdinType indistinctBaseType = type.baseType(true);
        TsOdinType indistinctExpectedBaseType = expectedType.baseType(true);
        if (indistinctBaseType instanceof TsOdinMatrixType &&
                indistinctExpectedBaseType instanceof TsOdinMatrixType) {
            if (checkTypesStrictly(indistinctBaseType, indistinctExpectedBaseType)) {
                typeCheckResult.setCompatible(true);
                return;
            }
        }

        if (indistinctBaseType instanceof TsOdinProcedureType &&
                indistinctExpectedBaseType instanceof TsOdinProcedureType) {
            if (checkTypesStrictly(indistinctBaseType, indistinctExpectedBaseType)) {
                typeCheckResult.setCompatible(true);
                return;
            }
        }

        if (expectedType.isAnyType()) {
            if (type instanceof TsOdinGenericType genericType) {
                if (genericType.isSpecialized()) {
                    addActionAndSetCompatible(ConversionAction.TO_ANY);
                }
            } else if (!type.isPolymorphic()) {
                addActionAndSetCompatible(ConversionAction.TO_ANY);
            }
            return;
        }

        if (type instanceof TsOdinPolymorphicType) {
            typeCheckResult.setCompatible(true);
            return;
        }

        if (expectedType instanceof TsOdinConstrainedType constrainedType) {
            if (type instanceof TsOdinMetaType metaType) {
                TsOdinType representedType = metaType.getRepresentedType();
                if (representedType == null) {
                    representedType = OdinTypeResolver.resolveMetaType(metaType.getSymbolTable(), metaType);
                }

                doCheckTypes(representedType.baseType(true), constrainedType.getSpecializedType());
            }
            return;
        }

        if (TsOdinBuiltInTypes.getFloatingPointTypes().contains(type)) {
            if (type instanceof TsOdinNumericType numericType) {
                if (TsOdinBuiltInTypes.getComplexTypes().contains(expectedType)) {
                    TsOdinNumericType complexType = (TsOdinNumericType) expectedType;
                    if (numericType.getLength() * 2 == complexType.getLength()) {
                        typeCheckResult.getConversionActionList().add(ConversionAction.FLOAT_TO_COMPLEX);
                        typeCheckResult.setCompatible(true);
                        return;
                    }
                }

                if (TsOdinBuiltInTypes.getQuaternionTypes().contains(expectedType)) {
                    TsOdinNumericType quaternionType = (TsOdinNumericType) expectedType;
                    if (numericType.getLength() * 4 == quaternionType.getLength()) {
                        typeCheckResult.getConversionActionList().add(ConversionAction.FLOAT_TO_COMPLEX);
                        typeCheckResult.setCompatible(true);
                        return;
                    }
                }
            }
        }

        if (TsOdinBuiltInTypes.getComplexTypes().contains(type)) {
            if (TsOdinBuiltInTypes.getQuaternionTypes().contains(expectedType)) {
                if (type instanceof TsOdinNumericType complexType) {
                    TsOdinNumericType quaternionType = (TsOdinNumericType) expectedType;
                    if (complexType.getLength() * 2 == quaternionType.getLength()) {
                        typeCheckResult.getConversionActionList().add(ConversionAction.COMPLEX_TO_QUATERNION);
                        typeCheckResult.setCompatible(true);
                        return;
                    }
                }
            }
        }

        if (expectedType instanceof TsOdinArrayType tsOdinArrayType) {
            type = convertToTyped(type, tsOdinArrayType.getElementType());
            if (checkTypesStrictly(type, tsOdinArrayType.getElementType())) {
                addActionAndSetCompatible(ConversionAction.TO_ARRAY);
                return;
            }
        }

        if (expectedType instanceof TsOdinMatrixType tsOdinMatrixType) {
            type = convertToTyped(type, tsOdinMatrixType.getElementType());
            if (checkTypesStrictly(type, tsOdinMatrixType.getElementType())) {
                addActionAndSetCompatible(ConversionAction.TO_MATRIX);
                return;
            }
        }

        if (type instanceof TsOdinPointerType tsOdinPointerType) {
            if (expectedType instanceof TsOdinRawPointerType) {
                addActionAndSetCompatible(ConversionAction.TO_RAW_POINTER);
                return;
            }

            if (expectedType instanceof TsOdinMultiPointerType tsOdinMultiPointerType) {
                if (checkTypesStrictly(tsOdinPointerType.getDereferencedType(), tsOdinMultiPointerType.getDereferencedType())) {
                    addActionAndSetCompatible(ConversionAction.MP_TO_POINTER);
                }
            }
        }

        if (type instanceof TsOdinMultiPointerType tsOdinMultiPointerType) {
            if (expectedType instanceof TsOdinPointerType expectedPointerType) {
                if (checkTypesStrictly(tsOdinMultiPointerType.getDereferencedType(), expectedPointerType.getDereferencedType())) {
                    addActionAndSetCompatible(ConversionAction.POINTER_TO_MP);
                    return;
                }
            }

            if (expectedType instanceof TsOdinRawPointerType) {
                addActionAndSetCompatible(ConversionAction.TO_RAW_POINTER);
                return;
            }

        }

        if (type == TsOdinBuiltInTypes.NIL && expectedType.isNillable()) {
            typeCheckResult.setCompatible(true);
            return;
        }

        if (expectedType instanceof TsOdinUnionType unionType) {
            for (TsOdinUnionVariant variant : unionType.getVariants()) {
                TypeCheckResult unionResult = OdinTypeChecker.checkTypes(type, variant.getType());
                if (unionResult.isCompatible()) {
                    typeCheckResult.getConversionActionList().addAll(unionResult.conversionActionList);
                    typeCheckResult.getConversionActionList().add(ConversionAction.TO_UNION_VARIANT);
                    typeCheckResult.setCompatible(true);
                    break;
                }
            }
        }

        if (expectedType instanceof TsOdinStructType &&
                type instanceof TsOdinStructType structType) {
            OdinStructType psiStructType = (OdinStructType) structType.getPsiType();
            // Check if there is a field "using" the expected struct
            OdinStructBlock structBlock = psiStructType.getStructBlock();
            if (structBlock != null) {
                OdinStructBody structBody = structBlock.getStructBody();
                if (structBody != null) {
                    for (OdinFieldDeclarationStatement odinFieldDeclarationStatement : structBody.getFieldDeclarationStatementList()) {
                        if (odinFieldDeclarationStatement.getUsing() != null) {
                            OdinType declarationType = odinFieldDeclarationStatement.getType();
                            if (declarationType != null) {
                                TsOdinType usedType = OdinTypeResolver.resolveType(
                                        structType.getSymbolTable(),
                                        declarationType
                                );

                                if (usedType instanceof TsOdinPointerType pointerType) {
                                    usedType = pointerType.getDereferencedType();
                                }

                                if (checkTypesStrictly(usedType, expectedType)) {
                                    addActionAndSetCompatible(ConversionAction.USING_SUBTYPE);
                                    return;
                                } else if (usedType instanceof TsOdinStructType) {
                                    TypeCheckResult typeCheckResult = checkTypes(usedType, expectedType, false);
                                    if (typeCheckResult.isCompatible()) {
                                        this.typeCheckResult.getConversionActionList().add(ConversionAction.USING_SUBTYPE);
                                        this.typeCheckResult.getConversionActionList().addAll(typeCheckResult.getConversionActionList());
                                        this.typeCheckResult.setCompatible(true);
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private @NotNull TsOdinType convertToTyped(TsOdinType type, TsOdinType expectedType) {
        if (type.isUntyped()) {
            TsOdinType tsOdinType = OdinTypeConverter.convertToTyped(type, expectedType);
            if (!tsOdinType.isUnknown()) {
                typeCheckResult.conversionActionList.add(ConversionAction.PRIMITIVE_TYPE);
                type = tsOdinType;
            }
        }
        return type;
    }

    private void addActionAndSetCompatible(ConversionAction conversionAction) {
        typeCheckResult.conversionActionList.add(conversionAction);
        typeCheckResult.setCompatible(true);
    }
}
