package com.lasagnerd.odin.codeInsight.imports;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.SourceFolder;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.psi.search.UsageSearchContext;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Query;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbol;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolTable;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolTableResolver;
import com.lasagnerd.odin.lang.psi.*;
import com.lasagnerd.odin.projectSettings.OdinSdkLibraryManager;
import com.lasagnerd.odin.projectStructure.module.rootTypes.collection.OdinCollectionRootProperties;
import com.lasagnerd.odin.projectStructure.module.rootTypes.collection.OdinCollectionRootType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;

public class OdinImportUtils {


    public static final Predicate<OdinSymbol> PUBLIC_ELEMENTS_MATCHER = s -> s.getVisibility() == OdinSymbol.OdinVisibility.PUBLIC;

    @Nullable
    public static String getFileName(@NotNull PsiElement psiElement) {
        VirtualFile virtualFile = getContainingVirtualFile(psiElement);
        if (virtualFile != null) {
            return virtualFile.getName();
        }

        return null;
    }

    @Nullable
    public static VirtualFile getContainingVirtualFile(@NotNull PsiElement psiElement) {
        VirtualFile virtualFile = psiElement.getContainingFile().getVirtualFile();
        if (virtualFile == null) {
            virtualFile = psiElement.getContainingFile().getOriginalFile().getVirtualFile();
        }
        return virtualFile;
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
            Path packagePath = getFirstAbsoluteImportPath(importInfo, sourceFilePath, project);
            if (packagePath == null)
                return OdinSymbolTable.EMPTY;

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
        return OdinSymbolTable.EMPTY;
    }

    /**
     * When importing from a collection, there might be competing collection names. This will return
     * the first path or a collection from the SDK if present.
     *
     * @param importInfo     The import to be imported
     * @param sourceFilePath Path of the file from where the import should be resolved
     * @param project        The current project
     * @return The first import path that exists or null if none exist
     */
    public static @Nullable Path getFirstAbsoluteImportPath(OdinImportInfo importInfo,
                                                            String sourceFilePath,
                                                            Project project) {
        List<Path> packagePaths = getAbsoluteImportPaths(importInfo, sourceFilePath, project);

        Path packagePath;
        if (packagePaths.size() > 1) {
            Optional<String> sdkPath = OdinImportService.getInstance(project).getSdkPath();
            if (sdkPath.isPresent()) {
                packagePath = packagePaths.stream()
                        .filter(p -> p.startsWith(sdkPath.get()))
                        .findFirst()
                        .orElseGet(packagePaths::getFirst);
            } else {
                packagePath = packagePaths.getFirst();
            }
        } else if (!packagePaths.isEmpty()) {
            packagePath = packagePaths.getFirst();
        } else {
            packagePath = null;
        }
        return packagePath;
    }

    public static Map<String, Path> getCollectionPaths(Project project, String sourceFilePath) {
        Map<String, Path> collectionPaths = new HashMap<>();
        VirtualFile sourceFile = VirtualFileManager.getInstance().findFileByNioPath(Path.of(sourceFilePath));
        if (sourceFile != null) {
            Module module = ModuleUtilCore.findModuleForFile(sourceFile, project);
            if (module != null) {
                ModuleRootManager model = ModuleRootManager.getInstance(module);
                List<SourceFolder> sourceFolders = Arrays.stream(model.getContentEntries())
                        .flatMap(c -> c.getSourceFolders(OdinCollectionRootType.INSTANCE).stream())
                        .toList();

                for (SourceFolder sourceFolder : sourceFolders) {
                    OdinCollectionRootProperties properties = (OdinCollectionRootProperties) sourceFolder.getJpsElement().getProperties();
                    String collectionName = properties.getCollectionName();
                    VirtualFile collectionDirectory = sourceFolder.getFile();
                    if (collectionDirectory != null) {
                        collectionPaths.put(collectionName, collectionDirectory.toNioPath());
                    }
                }
            }
        }

        return collectionPaths;
    }

    public static @NotNull List<Path> getAbsoluteImportPaths(OdinImportInfo importInfo, String sourceFilePath, Project project) {
        List<Path> dirs = new ArrayList<>();
        String library = Objects.requireNonNullElse(importInfo.collection(), "");
        if (!library.isBlank()) {
            Optional<String> sdkPath = OdinImportService.getInstance(project).getSdkPath();
            if (sdkPath.isPresent()) {
                Path sdkSourceDir = Path.of(sdkPath.get(), importInfo.collection());
                if (sdkSourceDir.toFile().exists()) {
                    dirs.add(sdkSourceDir);
                } else {
                    Map<String, Path> collectionPaths = getCollectionPaths(project, sourceFilePath);
                    Path collectionPath = collectionPaths.get(library);
                    if (collectionPath != null) {
                        dirs.add(collectionPath);
                    }
                }
            }
        }
        Path sourcePath = Path.of(sourceFilePath);
        Path currentDir = sourcePath.getParent();

        dirs.add(currentDir);

        List<Path> packagePaths = new ArrayList<>();
        for (Path dir : dirs) {
            Path packagePath = dir.resolve(importInfo.path()).normalize();
            if (packagePath.toFile().exists()) {
                packagePaths.add(packagePath);
            }
        }
        return packagePaths;
    }

    public static @NotNull OdinImportInfo computeRelativeImport(Project project, String sourceFilePath, String targetFilePath) {
        Path sourceDir = Path.of(sourceFilePath);
        Path targetDir = Path.of(targetFilePath).normalize();

        Optional<String> sdkPath = OdinImportService.getInstance(project).getSdkPath();
        if (sdkPath.isPresent()) {
            if (targetDir.startsWith(Path.of(sdkPath.get()))) {
                Path relativePath = Path.of(sdkPath.get()).relativize(targetDir);
                if (relativePath.getNameCount() >= 2) {
                    String library = FileUtil.toSystemIndependentName(relativePath.getName(0).toString());
                    String subPath = FileUtil.toSystemIndependentName(relativePath.subpath(1, targetDir.getNameCount()).toString());
                    String packageName = relativePath.getFileName().toString();
                    String fullImportPath = library + ":" + subPath;
                    return new OdinImportInfo(fullImportPath, packageName, subPath, library);
                }
            }
        }

        String importPath = FileUtil.toSystemIndependentName(sourceDir.relativize(targetDir).toString());
        String packageName = targetDir.getFileName().toString();
        return new OdinImportInfo(importPath, packageName, importPath, null);
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
        // Check if package is null. If yes log debug
        String path = Path.of(packagePath, getFileName(importStatement)).toString();
        String name = importInfo.packageName();
        Project project = importStatement.getProject();
        return getSymbolsOfImportedPackage(getImportStatementsInfo(fileScope).get(name), path, project);
    }


    public static String computeImportPath(@NotNull Project project, VirtualFile sourceElementFile, VirtualFile targetElementFile) {
        String sourcePath = sourceElementFile
                .getParent()
                .getPath();
        String targetPath = targetElementFile.getParent().getPath();
        if (sourcePath.equals(targetPath))
            return "";

        ProjectRootManager projectRootManager = ProjectRootManager.getInstance(project);
        String libraryRootForPath = OdinSdkLibraryManager.findLibraryRootForPath(project, targetElementFile.getPath());
        if (libraryRootForPath == null) {
            boolean inProject = projectRootManager.getFileIndex().isInProject(targetElementFile);
            if (inProject) {
                return FileUtil.toSystemIndependentName(Path.of(sourceElementFile.getParent().getPath())
                        .relativize(Path.of(targetElementFile.getParent().getPath())).toString());
            }
            return null;
        }

        String string = Path.of(libraryRootForPath)
                .relativize(Path.of(targetElementFile.getParent().getPath())).toString();
        String relativePath = FileUtil.toSystemIndependentName(
                string);


        int indexOfFirstSeparator = relativePath.indexOf('/');
        if (indexOfFirstSeparator > 0) {
            return relativePath.substring(0, indexOfFirstSeparator) + ":" + relativePath.substring(indexOfFirstSeparator + 1);
        }
        return null;
    }

    public static String getPackageNameFromImportPath(String importPath) {
        importPath = importPath.replaceAll("^.*?:", "");
        String[] split = importPath.split("/");
        if (split.length > 0) {
            return split[split.length - 1];
        }
        return null;
    }

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

    public static void insertImport(Project project, String importPath, OdinFileScope fileScope) {

        OdinImportDeclarationStatement anImport = OdinPsiElementFactory.getInstance(project)
                .createImport(importPath);

        OdinImportStatementsContainer importStatementsContainer = fileScope.getImportStatementsContainer();
        if (importStatementsContainer == null) {
            OdinImportStatementsContainer templateImportStatementsContainer = OdinPsiElementFactory.getInstance(project)
                    .createImportStatementsContainer(List.of(anImport));
            fileScope.addAfter(templateImportStatementsContainer, fileScope.getEos());
        } else {
            OdinImportDeclarationStatement odinImportDeclarationStatement = fileScope.getImportStatementsContainer().getImportDeclarationStatementList().getLast();
            importStatementsContainer.addAfter(anImport, odinImportDeclarationStatement);
        }
    }
}
