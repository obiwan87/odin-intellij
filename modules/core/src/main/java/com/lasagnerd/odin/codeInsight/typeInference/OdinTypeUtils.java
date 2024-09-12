package com.lasagnerd.odin.codeInsight.typeInference;

import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinArrayType;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinType;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinTypeAlias;

import static com.lasagnerd.odin.codeInsight.typeSystem.TsOdinMetaType.MetaType.ARRAY;

public class OdinTypeUtils {
    static boolean checkTypesStrictly(TsOdinType argumentType, TsOdinType parameterType) {
        argumentType = getBaseType(argumentType);
        parameterType = getBaseType(parameterType);

        if(argumentType == parameterType) {
            return true;
        }

        if (argumentType.getPsiType() != null && parameterType.getPsiType() != null) {
            return argumentType.getPsiType() == parameterType.getPsiType();
        }

        if (argumentType.getPsiTypeExpression() != null && parameterType.getPsiTypeExpression() != null) {
            return argumentType.getPsiType() == parameterType.getPsiType();
        }

        if (argumentType.getMetaType() == ARRAY && parameterType.getMetaType() == ARRAY) {
            return checkTypesStrictly(((TsOdinArrayType) argumentType).getElementType(), ((TsOdinArrayType) parameterType).getElementType());
        }

        return false;
    }

    public static TsOdinType getBaseType(TsOdinType t) {
        if (t instanceof TsOdinTypeAlias alias) {
            if (alias.isDistinct()) {
                return alias;
            }
            return alias.getDistinctBaseType();
        }
        return t;
    }
}
