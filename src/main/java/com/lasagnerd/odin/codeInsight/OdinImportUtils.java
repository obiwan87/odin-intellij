package com.lasagnerd.odin.codeInsight;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.lasagnerd.odin.lang.psi.*;
import com.lasagnerd.odin.sdkConfig.OdinSdkConfigPersistentState;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;

public class OdinImportUtils {
    @Data
    public static class OdinPackage {
        String packagePath;
        List<OdinFile> odinFiles = new ArrayList<>();
    }
    public static final Predicate<OdinSymbol> PUBLIC_ELEMENTS_MATCHER = s -> s.getVisibility() == OdinSymbol.OdinVisibility.PUBLIC
            && s.getSymbolType() != OdinSymbol.OdinSymbolType.PACKAGE_REFERENCE;

    @NotNull
    public static String getFileName(@NotNull PsiElement psiElement) {
        VirtualFile virtualFile = psiElement.getContainingFile().getVirtualFile();
        return Objects.requireNonNullElseGet(virtualFile,
                () -> psiElement.getContainingFile().getViewProvider().getVirtualFile()).getName();
    }

    public static String getPackagePath(PsiElement psiElement) {
        OdinFile containingFile = (OdinFile) psiElement.getContainingFile();
        if (containingFile == null)
            return null;
        @NotNull PsiFile virtualFile = containingFile.getOriginalFile();
        PsiDirectory containingDirectory = virtualFile.getContainingDirectory();
        if (containingDirectory != null) {
            return containingDirectory.getVirtualFile().getPath();
        }
        return null;
    }

    public static OdinScope getSymbolsOfImportedPackage(OdinImportDeclarationStatement importStatement) {
        OdinImportInfo importInfo = importStatement.getImportInfo();

        OdinFileScope fileScope = ((OdinFile) importStatement.getContainingFile()).getFileScope();
        PsiFile containingFile = importStatement.getContainingFile();
        VirtualFile virtualFile = containingFile.getVirtualFile();

        if (virtualFile == null) {
            virtualFile = containingFile.getViewProvider().getVirtualFile();
        }

        String path = virtualFile.getCanonicalPath();
        String name = importInfo.packageName();
        Project project = importStatement.getProject();
        return getSymbolsOfImportedPackage(getImportStatementsInfo(fileScope).get(name), path, project);
    }

    /**
     * @param importInfo     The import to be imported
     * @param sourceFilePath Path of the file from where the import should be resolved
     * @param project        Project
     * @return Scope
     */
    public static OdinScope getSymbolsOfImportedPackage(OdinImportInfo importInfo, String sourceFilePath, Project project) {
        List<OdinSymbol> packageDeclarations = new ArrayList<>();
        if (importInfo != null) {
            List<Path> dirs = new ArrayList<>();
            String library = Objects.requireNonNullElse(importInfo.library(), "");
            Path sdkSourceDir = null;
            if (!library.isBlank()) {
                OdinSdkConfigPersistentState sdkConfig = OdinSdkConfigPersistentState.getInstance(project);
                if (sdkConfig.getSdkPath() != null) {
                    sdkSourceDir = Path.of(sdkConfig.getSdkPath(), importInfo.library());
                    dirs.add(sdkSourceDir);
                }
            }
            if (sdkSourceDir != null) {
                dirs.add(sdkSourceDir);
            }
            Path sourcePath = Path.of(sourceFilePath);
            Path currentDir = sourcePath.getParent();

            dirs.add(currentDir);

            for (Path dir : dirs) {
                Path packagePath = dir.resolve(importInfo.path());
                List<OdinFile> importedFiles = getFilesInPackage(project, packagePath);
                for (OdinFile importedFile : importedFiles) {
                    OdinFileScope importedFileScope = importedFile.getFileScope();
                    if (importedFileScope == null) {
                        System.out.println("File scope is null for file %s" + importedFile.getVirtualFile().getPath());
                        continue;
                    }
                    OdinSymbol.OdinVisibility globalFileVisibility = OdinScopeResolver.getGlobalFileVisibility(importedFileScope);
                    if (globalFileVisibility == OdinSymbol.OdinVisibility.PACKAGE_PRIVATE
                            || globalFileVisibility  == OdinSymbol.OdinVisibility.FILE_PRIVATE)
                        continue;

                    Collection<OdinSymbol> fileScopeDeclarations = OdinScopeResolver.getFileScopeDeclarations(importedFileScope,
                                    globalFileVisibility)
                            .getFilteredSymbols(PUBLIC_ELEMENTS_MATCHER);
                    packageDeclarations.addAll(fileScopeDeclarations);
                }

                if (!importedFiles.isEmpty()) {
                    return OdinScope.from(packageDeclarations, packagePath.toString());
                }
            }
        }
        return OdinScope.EMPTY;
    }

    @NotNull
    public static Map<String, OdinImportInfo> getImportStatementsInfo(OdinFileScope fileScope) {
        Map<String, OdinImportInfo> importMap = new HashMap<>();
        List<OdinImportDeclarationStatement> importStatements
                = fileScope.getImportStatements();

        for (OdinImportDeclarationStatement importStatement : importStatements) {
            OdinImportInfo importInfo = importStatement.getImportInfo();
            importMap.put(importInfo.packageName(), importInfo);
        }
        return importMap;
    }

    public static List<OdinFile> getFilesInPackage(Project project, Path importPath) {
        Predicate<VirtualFile> matcher = child -> child.getName().endsWith(".odin");
        return getFilesInPackage(project, importPath, matcher);
    }

    static @NotNull List<OdinFile> getFilesInPackage(Project project, Path importPath, Predicate<VirtualFile> matcher) {
        List<OdinFile> files = new ArrayList<>();
        PsiManager psiManager = PsiManager.getInstance(project);
        VirtualFile packageDirectory = VfsUtil.findFile(importPath, true);
        if (packageDirectory != null) {
            for (VirtualFile child : packageDirectory.getChildren()) {
                if (matcher.test(child)) {
                    PsiFile psiFile = psiManager.findFile(child);
                    if (psiFile instanceof OdinFile odinFile) {
                        files.add(odinFile);
                    }
                }
            }
        }
        return files;
    }

    public static OdinScope getSymbolsOfImportedPackage(String packagePath, OdinImportDeclarationStatement importStatement) {
        OdinImportInfo importInfo = importStatement.getImportInfo();
        OdinFileScope fileScope = ((OdinFile) importStatement.getContainingFile()).getFileScope();
        String path = Path.of(packagePath, getFileName(importStatement)).toString();
        String name = importInfo.packageName();
        Project project = importStatement.getProject();
        return getSymbolsOfImportedPackage(getImportStatementsInfo(fileScope).get(name), path, project);
    }
}
