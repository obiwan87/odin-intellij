package com.lasagnerd.odin.codeInsight.typeInference;

import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolTable;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinType;

public record OdinInferenceEngineParameters(
        OdinSymbolTable symbolTable,
        TsOdinType expectedType,
        int lhsValuesCount,
        boolean explicitMode
) {

    public static OdinInferenceEngineParameters defaultParameters() {
        return new OdinInferenceEngineParameters(null, null, 1, false);
    }

}
