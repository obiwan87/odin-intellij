package com.lasagnerd.odin.codeInsight.imports;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.psi.search.UsageSearchContext;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Query;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolTable;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbol;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolTableResolver;
import com.lasagnerd.odin.lang.psi.OdinFile;
import com.lasagnerd.odin.lang.psi.OdinFileScope;
import com.lasagnerd.odin.lang.psi.OdinIdentifier;
import com.lasagnerd.odin.lang.psi.OdinImportDeclarationStatement;
import com.lasagnerd.odin.sdkConfig.OdinSdkConfigPersistentState;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;

public class OdinImportUtils {
    public static boolean isUnusedImport(OdinImportDeclarationStatement importDeclarationStatement) {
        PsiFile containingFile = importDeclarationStatement.getContainingFile();
        if (importDeclarationStatement.getDeclaredIdentifier() == null) {
            String text = importDeclarationStatement.getImportInfo().packageName();

            if (text.isBlank())
                return false;

            Project project = importDeclarationStatement.getProject();

            return PsiSearchHelper.getInstance(project).processElementsWithWord(
                    (element, offsetInElement) -> {
                        if (element instanceof OdinIdentifier identifier) {
                            PsiReference reference = identifier.getReference();
                            if (reference != null) {
                                PsiElement resolvedReference = reference.resolve();
                                return resolvedReference != importDeclarationStatement;
                            }
                        }
                        return true;
                    },

                    new LocalSearchScope(containingFile),
                    text,
                    UsageSearchContext.IN_CODE,
                    true
            );
        } else {
            Query<PsiReference> search = ReferencesSearch.search(importDeclarationStatement.getDeclaredIdentifier(), new LocalSearchScope(containingFile), true);
            return search.findFirst() == null;
        }
    }

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


    public static OdinSymbolTable getSymbolsOfImportedPackage(OdinImportDeclarationStatement importStatement) {
        OdinImportInfo importInfo = importStatement.getImportInfo();

        OdinFileScope fileScope = ((OdinFile) importStatement.getContainingFile()).getFileScope();
        PsiFile containingFile = importStatement.getContainingFile();
        VirtualFile virtualFile = containingFile.getVirtualFile();

        if (virtualFile == null) {
            virtualFile = containingFile.getViewProvider().getVirtualFile();
        }

        String path = OdinImportService.getInstance(importStatement.getProject()).getCanonicalPath(virtualFile);
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
    public static OdinSymbolTable getSymbolsOfImportedPackage(OdinImportInfo importInfo, String sourceFilePath, Project project) {
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
                Path packagePath = dir.resolve(importInfo.path()).normalize();
                List<OdinFile> importedFiles = getFilesInPackage(project, packagePath);
                for (OdinFile importedFile : importedFiles) {
                    OdinFileScope importedFileScope = importedFile.getFileScope();
                    if (importedFileScope == null) {
                        System.out.printf("File scope is null for file %s%n", importedFile.getVirtualFile().getPath());
                        continue;
                    }
                    OdinSymbol.OdinVisibility globalFileVisibility = OdinSymbolTableResolver.getGlobalFileVisibility(importedFileScope);
                    if (globalFileVisibility == OdinSymbol.OdinVisibility.PACKAGE_PRIVATE
                            || globalFileVisibility == OdinSymbol.OdinVisibility.FILE_PRIVATE)
                        continue;

                    Collection<OdinSymbol> fileScopeDeclarations = importedFileScope
                            .getSymbolTable()
                            .getFilteredSymbols(PUBLIC_ELEMENTS_MATCHER);

                    packageDeclarations.addAll(fileScopeDeclarations);
                }

                if (!importedFiles.isEmpty()) {
                    return OdinSymbolTable.from(packageDeclarations, packagePath.toString());
                }
            }
        }
        return OdinSymbolTable.EMPTY;
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

    public static @NotNull List<OdinFile> getFilesInPackage(Project project, Path importPath, Predicate<VirtualFile> matcher) {
        List<OdinFile> files = new ArrayList<>();
        VirtualFile[] children = OdinImportService.getInstance(project).getFilesInPath(importPath);

        for (VirtualFile child : children) {
            if (matcher.test(child)) {
                PsiFile psiFile = OdinImportService.getInstance(project).createPsiFile(child);
                if (psiFile instanceof OdinFile odinFile) {
                    files.add(odinFile);
                }
            }
        }
        return files;
    }

    public static OdinSymbolTable getSymbolsOfImportedPackage(String packagePath, OdinImportDeclarationStatement importStatement) {
        OdinImportInfo importInfo = importStatement.getImportInfo();
        OdinFileScope fileScope = ((OdinFile) importStatement.getContainingFile()).getFileScope();
        String path = Path.of(packagePath, getFileName(importStatement)).toString();
        String name = importInfo.packageName();
        Project project = importStatement.getProject();
        return getSymbolsOfImportedPackage(getImportStatementsInfo(fileScope).get(name), path, project);
    }
}
