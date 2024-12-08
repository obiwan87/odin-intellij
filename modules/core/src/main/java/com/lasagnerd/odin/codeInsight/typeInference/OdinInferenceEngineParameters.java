package com.lasagnerd.odin.codeInsight.typeInference;

import com.lasagnerd.odin.codeInsight.OdinContext;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinType;

public record OdinInferenceEngineParameters(
        OdinContext context,
        TsOdinType expectedType,
        int lhsValuesCount,
        boolean explicitMode
) {

    public static OdinInferenceEngineParameters defaultParameters() {
        return new OdinInferenceEngineParameters(null, null, 1, false);
    }

}
