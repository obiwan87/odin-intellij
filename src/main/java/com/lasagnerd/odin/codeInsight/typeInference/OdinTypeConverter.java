package com.lasagnerd.odin.codeInsight.typeInference;

import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinArrayType;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinBuiltInType;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinBuiltInTypes;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinMetaType.MetaType;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class OdinTypeConverter {

    private static Map<TsOdinBuiltInType, Set<TsOdinBuiltInType>> CONVERSION_TABLE = new HashMap<>();

    static {
        CONVERSION_TABLE.put(
                TsOdinBuiltInTypes.UNTYPED_INT,
                Set.of(

                )
        );
    }

    public static @NotNull TsOdinType convertTypeOfBinaryExpression(@NotNull TsOdinType a, @NotNull TsOdinType b) {
        if (a.isUnknown() || b.isUnknown())
            return TsOdinType.UNKNOWN;

        Arrays.sort(new TsOdinType[]{a, b});
        if (a.getMetaType() == MetaType.ARRAY && b.getMetaType() == MetaType.BUILTIN) {
            return doCovertBuiltinToArrayType((TsOdinArrayType) a, (TsOdinBuiltInType) b);
        }
        return TsOdinType.UNKNOWN;
    }

    private static TsOdinType doCovertBuiltinToArrayType(TsOdinArrayType arrayType, TsOdinBuiltInType builtInType) {
        if (arrayType.getElementType().getMetaType() == MetaType.BUILTIN) {
            return doConvertBuiltinTypes((TsOdinBuiltInType) arrayType.getElementType(), builtInType);
        }
        return TsOdinType.UNKNOWN;
    }

    private static TsOdinType doConvertBuiltinTypes(TsOdinBuiltInType a, TsOdinBuiltInType b) {
        // Untyped types are "less than" typed types
        if (a.compareTo(b) < 0) {
            return doConvertBuiltinTypes(b, a);
        }

        if(!a.isUntyped() && !b.isUntyped())
            return TsOdinType.UNKNOWN;

        if(a.isUntyped())
            return convertUntypedTypes(a, b);

        // Here we know that b is untyped and a isn't
        return TsOdinType.UNKNOWN;
    }

    private static TsOdinType convertUntypedTypes(TsOdinBuiltInType untypedA, TsOdinBuiltInType untypedB) {
        return null;
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
        }

        return tsOdinType;
    }
}
