package com.lasagnerd.odin.insights.typeInference;

import com.lasagnerd.odin.insights.typeSystem.TsOdinType;
import com.lasagnerd.odin.lang.psi.OdinImportDeclarationStatement;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

@Data
public class OdinTypeInferenceResult {
    boolean isImport;
    OdinImportDeclarationStatement importDeclarationStatement;
    @Nullable
    TsOdinType type;
}
