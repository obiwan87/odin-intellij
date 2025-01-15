package com.lasagnerd.odin.codeInsight.sdk;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.OdinSymbolTable;
import com.lasagnerd.odin.codeInsight.dataflow.OdinSymbolValueStore;
import com.lasagnerd.odin.codeInsight.evaluation.EvOdinValue;
import com.lasagnerd.odin.codeInsight.imports.OdinImport;
import com.lasagnerd.odin.codeInsight.symbols.OdinScope;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbol;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolType;
import com.lasagnerd.odin.codeInsight.symbols.OdinVisibility;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinType;
import com.lasagnerd.odin.lang.psi.OdinFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public interface OdinSdkService {

    static OdinSdkService getInstance(Project project) {
        return project.getService(OdinSdkService.class);
    }

    static OdinSymbol createAllocatorSymbol(Project project) {
        return getInstance(project).createImplicitStructSymbol("allocator", "Allocator",
                OdinSymbolType.ALLOCATOR_FIELD, OdinScope.TYPE, OdinVisibility.NONE);
    }

    static OdinSymbol createContextSymbol(Project project) {
        return getInstance(project).createImplicitStructSymbol("context", "Context", OdinSymbolType.PARAMETER, OdinScope.LOCAL, OdinVisibility.NONE);
    }

    static boolean isInDocumentationPurposeFile(PsiElement element) {
        OdinSdkService instance = OdinSdkService.getInstance(element.getProject());
        if (instance == null)
            return false;

        @NotNull VirtualFile containingFile = OdinInsightUtils.getContainingVirtualFile(element);
        if (!Objects.equals(instance.getBuiltinVirtualFile(), containingFile)) {
            VirtualFile intrinsicsFile = instance.getIntrinsicsFile();
            return Objects.equals(intrinsicsFile, containingFile);
        }
        return true;
    }

    static boolean isInBuiltinOdinFile(PsiElement element) {
        OdinSdkService instance = OdinSdkService.getInstance(element.getProject());
        if (instance == null)
            return false;

        @NotNull VirtualFile containingFile = OdinInsightUtils.getContainingVirtualFile(element);

        VirtualFile builtinVirtualFile = instance.getBuiltinVirtualFile();
        return Objects.equals(builtinVirtualFile, containingFile);
    }

    List<OdinSymbol> getRuntimeCoreSymbols();

    List<OdinSymbol> getBuiltInSymbols();

    default OdinSymbolTable getRuntimeSymbolsTable() {
        return OdinSymbolTable.from(getRuntimeCoreSymbols());
    }

    default OdinSymbol getBuiltinSymbol(String name) {
        return getBuiltInSymbols().stream()
                .filter(s -> Objects.equals(name, s.getName())).findFirst()
                .orElse(null);
    }

    OdinSymbolTable getBuiltInSymbolTable();

    boolean isInSyntheticOdinFile(PsiElement element);

    EvOdinValue getValue(String name);

    OdinSymbol getRuntimeCoreSymbol(String symbolName);

    TsOdinType getType(String typeName);

    OdinSymbol createImplicitStructSymbol(String symbolName, String structTypeName, OdinSymbolType symbolType, OdinScope symbolScope, OdinVisibility symbolVisibility);

    Map<OdinImport, List<OdinFile>> getSdkPackages();

    void invalidateCache();

    void refreshCache();

    VirtualFile getBuiltinVirtualFile();

    VirtualFile getIntrinsicsFile();

    OdinSymbolValueStore getSymbolValueStore();
}
