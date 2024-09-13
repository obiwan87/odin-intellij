package com.lasagnerd.odin.codeInsight.symbols;

import com.intellij.openapi.project.Project;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinType;

import java.util.List;

public interface OdinBuiltinSymbolService {
    static OdinBuiltinSymbolService getInstance(Project project) {
        return project.getService(OdinBuiltinSymbolService.class);
    }

    List<OdinSymbol> getRuntimeCoreSymbols();

    List<OdinSymbol> getBuiltInSymbols();

    OdinSymbol getSymbol(String symbolName);

    TsOdinType getType(String typeName);

    OdinSymbol createImplicitStructSymbol(String symbolName, String structTypeName);
}
