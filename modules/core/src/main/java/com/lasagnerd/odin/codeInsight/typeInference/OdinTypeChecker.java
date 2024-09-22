package com.lasagnerd.odin.codeInsight.typeInference;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.lasagnerd.odin.codeInsight.typeSystem.*;
import com.lasagnerd.odin.lang.psi.*;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class OdinTypeChecker {

    static boolean checkTypesStrictly(TsOdinType argumentType, TsOdinType parameterType) {
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

        if (argumentBaseType instanceof TsOdinArrayType argArrayType
                && parameterBaseType instanceof TsOdinArrayType parArrayType) {
            if (argArrayType.isSoa() == parArrayType.isSoa() && argArrayType.isSimd() == parArrayType.isSimd()) {
               OdinArraySize argSizeElement = argArrayType.getPsiSizeElement();
                OdinArraySize parSizeElement = parArrayType.getPsiSizeElement();
                if(parSizeElement != null
                        && argSizeElement != null
                        && parSizeElement.getExpression() instanceof OdinLiteralExpression
                        && argSizeElement.getExpression() instanceof OdinLiteralExpression) {

                    if(PsiEquivalenceUtil.areElementsEquivalent(argArrayType.getPsiSizeElement(), parArrayType.getPsiSizeElement())) {
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

    public enum ConversionAction {
        PRIMITIVE_TYPE,
        TO_ARRAY,
        TO_MATRIX,
        TO_RAW_POINTER,
        MP_TO_POINTER,
        POINTER_TO_MP,
        TO_ANY,
        USING_SUBTYPE,
    }

    @Data
    public static class TypeCheckResult {
        private boolean compatible;
        private boolean polymorphic;
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
        if (type instanceof TsOdinPointerType pointerType && expectedType instanceof TsOdinPointerType expectedPointerType) {
            doCheckTypes(pointerType.getDereferencedType(), expectedPointerType.getDereferencedType());
            return;
        }

        if (checkTypesStrictly(type, expectedType)) {
            typeCheckResult.setCompatible(true);
            return;
        }

        if (expectedType instanceof TsOdinPolymorphicType) {
            typeCheckResult.setPolymorphic(true);
            typeCheckResult.setCompatible(true);
            return;
        }

        if(type instanceof TsOdinPolymorphicType) {
            typeCheckResult.setPolymorphic(true);
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

        if (expectedType instanceof TsOdinStructType &&
                type instanceof TsOdinStructType structType) {
            OdinStructType psiStructType = (OdinStructType) structType.getPsiType();
            // Check if there is a field "using" the expected struct
            OdinStructBody structBody = psiStructType.getStructBlock().getStructBody();
            if (structBody != null) {
                for (OdinFieldDeclarationStatement odinFieldDeclarationStatement : structBody.getFieldDeclarationStatementList()) {
                    if (odinFieldDeclarationStatement.getUsing() != null) {
                        TsOdinType usedType = OdinTypeResolver
                                .resolveType(structType.getSymbolTable(),
                                        odinFieldDeclarationStatement.getType());

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
