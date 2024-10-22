package com.lasagnerd.odin.codeInsight.symbols;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.lasagnerd.odin.codeInsight.evaluation.EvOdinValue;
import com.lasagnerd.odin.codeInsight.imports.OdinImport;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinType;
import com.lasagnerd.odin.lang.psi.OdinFile;

import java.util.List;
import java.util.Map;

public interface OdinSdkService {
    static OdinSdkService getInstance(Project project) {
        return project.getService(OdinSdkService.class);
    }

    static OdinSymbol createAllocatorSymbol(Project project) {
        return getInstance(project).createImplicitStructSymbol("allocator", "Allocator");
    }

    List<OdinSymbol> getRuntimeCoreSymbols();

    List<OdinSymbol> getBuiltInSymbols();

    OdinSymbolTable getBuiltInSymbolTable();

    boolean isInSyntheticOdinFile(PsiElement element);

    EvOdinValue getValue(String name);

    OdinSymbol getSymbol(String symbolName);

    TsOdinType getType(String typeName);

    OdinSymbol createImplicitStructSymbol(String symbolName, String structTypeName);

    Map<OdinImport, List<OdinFile>> getSdkPackages();

    void invalidateCache();
}
