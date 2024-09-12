package com.lasagnerd.odin.codeInsight.typeInference;

import com.lasagnerd.odin.codeInsight.typeSystem.*;
import com.lasagnerd.odin.lang.psi.OdinFieldDeclarationStatement;
import com.lasagnerd.odin.lang.psi.OdinStructType;
import com.lasagnerd.odin.lang.psi.OdinType;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class OdinTypeChecker {

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
        private List<ConversionAction> conversionActionList = new ArrayList<>();
    }


    TypeCheckResult typeCheckResult = new TypeCheckResult();

    public static TypeCheckResult checkTypes(TsOdinType type, TsOdinType expectedType) {
        OdinTypeChecker odinTypeChecker = new OdinTypeChecker();
        odinTypeChecker.doCheckTypes(type, expectedType);
        return odinTypeChecker.typeCheckResult;
    }

    public void doCheckTypes(TsOdinType type, TsOdinType expectedType) {
        if (OdinTypeUtils.checkTypesStrictly(type, expectedType)) {
            typeCheckResult.setCompatible(true);
            return;
        }

        type = convertToTyped(type, expectedType);

        if (expectedType instanceof TsOdinArrayType tsOdinArrayType) {
            type = convertToTyped(type, tsOdinArrayType.getElementType());
            if (OdinTypeUtils.checkTypesStrictly(type, tsOdinArrayType.getElementType())) {
                addActionAndSetCompatible(ConversionAction.TO_ARRAY);
                return;
            }
        }

        if (expectedType instanceof TsOdinMatrixType tsOdinMatrixType) {
            type = convertToTyped(type, tsOdinMatrixType.getElementType());
            if (OdinTypeUtils.checkTypesStrictly(type, tsOdinMatrixType.getElementType())) {
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
                if (OdinTypeUtils.checkTypesStrictly(tsOdinPointerType.getDereferencedType(), tsOdinMultiPointerType.getDereferencedType())) {
                    addActionAndSetCompatible(ConversionAction.MP_TO_POINTER);
                }
            }
        }

        if (type instanceof TsOdinMultiPointerType tsOdinMultiPointerType) {
            if (expectedType instanceof TsOdinPointerType expectedPointerType) {
                if (OdinTypeUtils.checkTypesStrictly(tsOdinMultiPointerType.getDereferencedType(), expectedPointerType.getDereferencedType())) {
                    addActionAndSetCompatible(ConversionAction.POINTER_TO_MP);
                    return;
                }
            }

            if (expectedType instanceof TsOdinRawPointerType) {
                addActionAndSetCompatible(ConversionAction.TO_RAW_POINTER);
                return;
            }

        }

        if (expectedType instanceof TsOdinStructType &&
                type instanceof TsOdinStructType structType) {
            OdinStructType psiStructType = (OdinStructType) structType.getPsiType();
            // Check if there is a field "using" the expected struct
            for (OdinFieldDeclarationStatement odinFieldDeclarationStatement : psiStructType.getStructBlock().getStructBody().getFieldDeclarationStatementList()) {
                if (odinFieldDeclarationStatement.getUsing() != null) {
                    TsOdinType usingStructType = OdinTypeResolver
                            .resolveType(structType.getSymbolTable(), odinFieldDeclarationStatement.getType());

                    if (OdinTypeUtils.checkTypesStrictly(usingStructType, expectedType)) {
                        addActionAndSetCompatible(ConversionAction.USING_SUBTYPE);
                        return;
                    }
                }
            }
        }
    }

    private @NotNull TsOdinType convertToTyped(TsOdinType type, TsOdinType expectedType) {
        if (type.isUntyped()) {
            TsOdinType tsOdinType = OdinTypeConverter.convertToTyped(type, expectedType);
            if (tsOdinType.isUnknown()) {
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
