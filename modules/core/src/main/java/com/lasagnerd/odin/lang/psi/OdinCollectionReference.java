package com.lasagnerd.odin.lang.psi;

import com.intellij.codeInsight.highlighting.HighlightedReference;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReferenceBase;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.imports.OdinImport;
import com.lasagnerd.odin.codeInsight.imports.OdinImportUtils;
import com.lasagnerd.odin.projectStructure.collection.OdinPsiCollection;
import com.lasagnerd.odin.projectStructure.collection.OdinRootTypeResult;
import com.lasagnerd.odin.projectStructure.collection.OdinRootsService;
import com.lasagnerd.odin.riderInterop.OdinRiderInteropService;
import com.lasagnerd.odin.settings.projectSettings.OdinSdkUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class OdinCollectionReference extends PsiReferenceBase<OdinImportPath> implements HighlightedReference {
    private final TextRange rangeInElement;

    public OdinCollectionReference(@NotNull OdinImportPath element) {
        super(element);
        int index = element.getText().indexOf(":");
        this.rangeInElement = new TextRange(1, index);
    }

    @Override
    public @Nullable PsiElement resolve() {
        return resolveCollectionPathDirectory();
    }

    private @Nullable PsiElement resolveCollectionPathDirectory() {
        OdinImport importInfo = OdinImportUtils.getImportInfo(getElement());
        if (importInfo != null && importInfo.collection() != null) {
            Optional<String> validSdkPath = OdinSdkUtils.getValidSdkPath(getElement().getProject());
            if (validSdkPath.isPresent()) {
                Path collectionPath = Path.of(validSdkPath.get(), importInfo.collection());
                VirtualFile collectionDir = VirtualFileManager.getInstance().findFileByNioPath(collectionPath);

                if (collectionDir != null) {
                    PsiDirectory directory = PsiManager.getInstance(getElement().getProject()).findDirectory(collectionDir);
                    if (!OdinRiderInteropService.isRider(myElement.getProject())) {
                        return new OdinPsiCollection(importInfo.collection(), directory);
                    }
                    return directory;
                }
            }
            OdinRootTypeResult odinRootTypeResult = OdinRootsService.getInstance(getElement().getProject())
                    .findCollectionRoot(getElement(), importInfo.collection());
            if (odinRootTypeResult != null && odinRootTypeResult.isCollectionRoot()) {
                VirtualFile collectionDir = odinRootTypeResult.directory();
                if (collectionDir != null) {
                    PsiDirectory directory = PsiManager.getInstance(getElement().getProject()).findDirectory(collectionDir);
                    if (!OdinRiderInteropService.isRider(myElement.getProject())) {
                        return new OdinPsiCollection(odinRootTypeResult.collectionName(), directory);
                    }
                    return directory;
                }
            }
        }
        return null;
    }

    @Override
    public @NotNull TextRange getRangeInElement() {
        return rangeInElement;
    }

    private @NotNull List<LookupElement> getCollectionLookupElements() {
        List<LookupElement> collections = new ArrayList<>();
        Optional<String> validSdkPath = OdinSdkUtils.getValidSdkPath(myElement.getProject());
        if (validSdkPath.isPresent()) {
            List<String> sdkCollections = List.of("core", "base", "vendor");
            for (String sdkCollection : sdkCollections) {
                LookupElementBuilder lookupElement = LookupElementBuilder
                        .create(sdkCollection)
                        .withTypeText("SDK");

                collections.add(lookupElement);
            }
        }
        VirtualFile containingVirtualFile = OdinInsightUtils.getContainingVirtualFile(getElement());
        String basePath = myElement.getProject().getBasePath();
        if (basePath != null) {
            Path projectPath = Path.of(basePath);
            Map<String, Path> collectionPaths = OdinRootsService.getInstance(getElement().getProject())
                    .getCollectionPaths(containingVirtualFile.getPath());
            for (Map.Entry<String, Path> entry : collectionPaths.entrySet()) {
                Path collectinPath = entry.getValue();
                LookupElementBuilder lookupElement = LookupElementBuilder
                        .create(collectinPath, entry.getKey())
                        .withTailText(" " + projectPath.relativize(collectinPath));

                collections.add(lookupElement);
            }
        }
        return collections;
    }
}
