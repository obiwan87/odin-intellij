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
import com.intellij.psi.*;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.psi.search.UsageSearchContext;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Query;
import com.lasagnerd.odin.codeInsight.refactor.OdinNameSuggester;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbol;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolTable;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolTableResolver;
import com.lasagnerd.odin.lang.OdinFileType;
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
        OdinImport importInfo = importStatement.getImportInfo();

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
    public static OdinSymbolTable getSymbolsOfImportedPackage(OdinImport importInfo, String sourceFilePath, Project project) {
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
    public static @Nullable Path getFirstAbsoluteImportPath(OdinImport importInfo,
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

    /**
     * Collects the importable packages of a directory from the perspective of a requesting package path.
     *
     * @param project               Current project
     * @param rootDir               The dir for which to collect importable packages
     * @param collection            Whether this dir is a collection
     * @param requestingPackagePath The path from which to collect the importable packages
     * @return A map of imports to files
     */
    public static @NotNull Map<OdinImport, List<OdinFile>> collectImportablePackages(Project project,
                                                                                     VirtualFile rootDir,
                                                                                     @Nullable String collection,
                                                                                     String requestingPackagePath) {
        String rootDirPath = rootDir.getPath();
        Stack<VirtualFile> work = new Stack<>();
        Map<OdinImport, List<OdinFile>> packages = new HashMap<>();
        work.add(rootDir);

        do {
            VirtualFile currentFile = work.pop();
            if (currentFile.isDirectory()) {
                Collections.addAll(work, currentFile.getChildren());
            } else {
                if (currentFile.getFileType() == OdinFileType.INSTANCE) {
                    VirtualFile parent = currentFile.getParent();
                    String packagePath = parent.getPath();
                    Path packageNioPath = Path.of(packagePath);
                    OdinFile odinFile = (OdinFile) PsiManager.getInstance(project).findFile(currentFile);
                    if (odinFile == null) continue;
                    if (odinFile.getVirtualFile() == null) continue;

                    String filePath = odinFile.getVirtualFile().getPath();
                    if (!packagePath.isBlank()
                            && !packagePath.equals(rootDirPath)
                            && !packagePath.equals(requestingPackagePath)
                    ) {
                        String packageName = parent.getName();
                        boolean aliasFound = false;
                        String alias = null;
                        if (!OdinNameSuggester.isValidIdentifier(packageName)) {
                            alias = packageName;
                            for (int j = packageNioPath.getNameCount() - 1; j > 0; j--) {
                                String name = packageNioPath.getName(j).toString();
                                if (Character.isJavaIdentifierStart(name.charAt(0))) {
                                    alias = OdinNameSuggester.normalizeName(name + "_" + alias);
                                    aliasFound = true;
                                    break;
                                }
                            }

                            if (!aliasFound) {
                                alias = OdinNameSuggester.normalizeName("_" + packageName);
                            }
                        }

                        String relativeImportPath;
                        Path fileNioPath = Path.of(filePath);

                        if (requestingPackagePath != null) {
                            relativeImportPath = FileUtil.toSystemIndependentName(Path.of(requestingPackagePath)
                                    .relativize(fileNioPath)
                                    .toString());
                        } else {
                            relativeImportPath = FileUtil.toSystemIndependentName(
                                    Path.of(rootDirPath)
                                            .relativize(fileNioPath.getParent())
                                            .toString()
                            );
                        }
                        String fullImportPath = collection == null ? relativeImportPath : collection + ":" + relativeImportPath;
                        packages.computeIfAbsent(new OdinImport(fullImportPath,
                                packageName,
                                relativeImportPath,
                                collection, alias), k -> new ArrayList<>()).add(odinFile);
                    }
                }
            }
        } while (!work.isEmpty());
        return packages;
    }

    public String getCollectionName(Project project, String path) {
        Path nioPath = Path.of(path);
        Map<String, Path> collectionPaths = getCollectionPaths(project, path);
        for (Map.Entry<String, Path> entry : collectionPaths.entrySet()) {
            if (entry.getValue().equals(nioPath)) {
                return entry.getKey();
            }
        }

        return null;
    }

    public static @NotNull List<Path> getAbsoluteImportPaths(OdinImport importInfo, String sourceFilePath, Project project) {
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

    public static @NotNull OdinImport computeRelativeImport(Project project, String sourceFilePath, String targetFilePath) {
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
                    return new OdinImport(fullImportPath, packageName, subPath, library, null);
                }
            }
        }

        String importPath = FileUtil.toSystemIndependentName(sourceDir.relativize(targetDir).toString());
        String packageName = targetDir.getFileName().toString();
        return new OdinImport(importPath, packageName, importPath, null, null);
    }

    @NotNull
    public static Map<String, OdinImport> getImportStatementsInfo(OdinFileScope fileScope) {
        Map<String, OdinImport> importMap = new HashMap<>();
        List<OdinImportDeclarationStatement> importStatements
                = fileScope.getImportStatements();

        for (OdinImportDeclarationStatement importStatement : importStatements) {
            OdinImport importInfo = importStatement.getImportInfo();
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
        OdinImport importInfo = importStatement.getImportInfo();
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

            PsiDirectory psiDirectory = OdinPackageReference.resolvePackagePathDirectory(importDeclarationStatement.getImportPath());
            if (text.isBlank())
                return false;

            Project project = importDeclarationStatement.getProject();

            return PsiSearchHelper.getInstance(project).processElementsWithWord(
                    (element, offsetInElement) -> {
                        if (element instanceof OdinIdentifier identifier) {
                            PsiReference reference = identifier.getReference();
                            if (reference != null) {
                                PsiElement resolvedReference = reference.resolve();
                                if (resolvedReference != null) {
                                    return !resolvedReference.isEquivalentTo(psiDirectory);
                                }
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

    public static void insertImport(Project project, String alias, String importPath, OdinFileScope fileScope) {
        OdinImportDeclarationStatement anImport = OdinPsiElementFactory.getInstance(project)
                .createImport(alias, importPath);

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

    public static @Nullable OdinImport getImportInfo(@NotNull OdinImportPath element) {
        OdinImportDeclarationStatement importDeclarationStatement = PsiTreeUtil.getParentOfType(element, OdinImportDeclarationStatement.class);
        if (importDeclarationStatement == null)
            return null;
        return importDeclarationStatement.getImportInfo();
    }
}
