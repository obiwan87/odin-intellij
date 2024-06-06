package com.lasagnerd.odin.codeInsight;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.lasagnerd.odin.codeInsight.imports.OdinImportService;
import com.lasagnerd.odin.lang.OdinFileType;
import com.lasagnerd.odin.lang.psi.*;
import com.lasagnerd.odin.sdkConfig.OdinSdkConfigPersistentState;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;

public class OdinScopeResolver {
    public static OdinScope resolveScope(PsiElement element) {
        OdinScopeResolver odinScopeResolver = new OdinScopeResolver(element);
        return odinScopeResolver.findScope(OdinImportService.getInstance(element.getProject()).getPackagePath(odinScopeResolver.element));
    }

    public static OdinScope resolveScope(PsiElement element, Predicate<OdinSymbol> matcher) {
        OdinScopeResolver odinScopeResolver = new OdinScopeResolver(element, matcher);
        return odinScopeResolver.findScope(OdinImportService.getInstance(element.getProject()).getPackagePath(odinScopeResolver.element));
    }

    private final Predicate<OdinSymbol> matcher;
    private final PsiElement element;

    private OdinScopeResolver(PsiElement element) {
        this.element = element;
        this.matcher = e -> true;
    }

    private OdinScopeResolver(PsiElement element, Predicate<OdinSymbol> matcher) {
        this.matcher = matcher;
        this.element = element;
    }

    public static OdinScope getFileScopeDeclarations(@NotNull OdinFileScope fileScope) {
        return getFileScopeDeclarations(fileScope, getGlobalFileVisibility(fileScope));
    }

    public static OdinScope getFileScopeDeclarations(@NotNull OdinFileScope fileScope, @NotNull OdinSymbol.OdinVisibility globalVisibility) {
        // Find all blocks that are not in a procedure
        List<OdinSymbol> fileScopeSymbols = new ArrayList<>();

        Stack<PsiElement> statementStack = new Stack<>();

        // do bfs
        statementStack.addAll(fileScope.getFileScopeStatementList().getStatementList());
        statementStack.addAll(fileScope.getImportStatementsContainer().getImportDeclarationStatementList());
        while (!statementStack.isEmpty()) {
            PsiElement element = statementStack.pop();
            if (element instanceof OdinDeclaration declaration) {
                List<OdinSymbol> symbols = OdinSymbolResolver.getSymbols(globalVisibility, declaration, OdinScope.EMPTY);
                fileScopeSymbols.addAll(symbols);
            } else {
                getStatements(element).forEach(statementStack::push);
            }
        }
        return OdinScope.from(fileScopeSymbols);
    }

    private static List<OdinStatement> getStatements(PsiElement psiElement) {
        if (psiElement instanceof OdinWhenStatement odinWhenStatement) {
            if (odinWhenStatement.getWhenBlock().getStatementBody().getBlock() != null) {
                OdinStatementList statementList = odinWhenStatement.getWhenBlock().getStatementBody().getBlock().getStatementList();
                if (statementList != null) {
                    return statementList.getStatementList();
                }
            }

            if (odinWhenStatement.getWhenBlock().getStatementBody().getDoStatement() != null) {
                return List.of(odinWhenStatement.getWhenBlock().getStatementBody().getDoStatement());
            }
        }

        if (psiElement instanceof OdinForeignStatement foreignStatement) {
            OdinForeignBlock foreignBlock = foreignStatement.getForeignBlock();
            OdinForeignStatementList foreignStatementList = foreignBlock.getForeignStatementList();
            if (foreignStatementList != null) {
                return foreignStatementList.getStatementList();
            }
        }

        return Collections.emptyList();
    }

    private static List<OdinSymbol> getBuiltInSymbols(Project project) {
        // TODO Cache this stuff
        List<OdinSymbol> builtinSymbols = new ArrayList<>();
        // 0. Import built-in symbols
        Optional<String> sdkPathOptional = OdinSdkConfigPersistentState.getSdkPath(project);

        if (sdkPathOptional.isEmpty())
            return Collections.emptyList();

        String sdkPath = sdkPathOptional.get();
        Path coreBuiltinPath = Path.of(sdkPath, "base", "runtime", "core_builtin.odin");
        Path coreBuiltinSoaPath = Path.of(sdkPath, "base", "runtime", "core_builtin_soa.odin");

        List<Path> builtinPaths = List.of(coreBuiltinPath, coreBuiltinSoaPath);
        for (Path builtinPath : builtinPaths) {
            OdinFile odinFile = createOdinFile(project, builtinPath);
            if (odinFile != null) {
                OdinScope fileScopeDeclarations = odinFile.getFileScope().getScope();
                Collection<OdinSymbol> symbols = fileScopeDeclarations
                        .getSymbolTable().values()
                        .stream()
                        .filter(odinSymbol -> OdinAttributeUtils.containsBuiltin(odinSymbol.getAttributeStatements()))
                        .toList();
                builtinSymbols.addAll(symbols);
            }
        }

        List<String> resources = List.of("odin/builtin.odin", "odin/intrinsics.odin");
        for (String resource : resources) {
            OdinFile odinFile = createOdinFileFromResource(project, resource);
            if (odinFile != null) {
                OdinScope fileScopeDeclarations = odinFile.getFileScope().getScope();
                Collection<OdinSymbol> symbols = fileScopeDeclarations
                        .getSymbolTable().values()
                        .stream()
                        .filter(odinSymbol -> OdinAttributeUtils.containsBuiltin(odinSymbol.getAttributeStatements()))
                        .toList();
                builtinSymbols.addAll(symbols);
            }
        }
        return builtinSymbols;
    }

    private static OdinFile createOdinFile(Project project, Path path) {
        VirtualFile virtualFile = VfsUtil.findFile(path, true);
        if (virtualFile != null) {
            return (OdinFile) PsiManager.getInstance(project).findFile(virtualFile);
        }
        return null;
    }

    private static OdinFile createOdinFileFromResource(Project project, String resourcePath) {
        InputStream resource = OdinScopeResolver.class.getClassLoader().getResourceAsStream(resourcePath);
        if (resource == null)
            return null;
        try (resource) {
            String text = new String(resource.readAllBytes(), StandardCharsets.UTF_8);
            return (OdinFile) PsiFileFactory.getInstance(project).createFileFromText("resource.odin", OdinFileType.INSTANCE, text);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private OdinScope findScope(String packagePath) {
        // TODO: When looking for a specific declaration, this can be optimized:
        // when building the scope tree, just stop as soon as we find the first matching declaration
        Project project = element.getProject();

        OdinScope scope = new OdinScope();
        scope.setPackagePath(packagePath);

        List<OdinSymbol> builtInSymbols = getBuiltInSymbols(project);

        // 0. Import built-in symbols
        scope.addAll(builtInSymbols);

        // 1. Import symbols from this file
        OdinFileScope fileScope = (OdinFileScope) PsiTreeUtil.findFirstParent(element, psi -> psi instanceof OdinFileScope);

        if (fileScope != null) {
            OdinScope fileScopeDeclarations = fileScope.getScope();
            scope.addAll(fileScopeDeclarations.getFilteredSymbols(matcher), false);
        }

        // 2. Import symbols from other files in the same package
        if (packagePath != null) {
            // Filter out symbols declared with private="file" or do not include anything if comment //+private is in front of package declaration
            List<OdinFile> otherFilesInPackage = getOtherFilesInPackage(project, packagePath, OdinImportUtils.getFileName(element));
            for (OdinFile odinFile : otherFilesInPackage) {
                if (odinFile == null || odinFile.getFileScope() == null) {
                    continue;
                }
                OdinSymbol.OdinVisibility globalFileVisibility = getGlobalFileVisibility(odinFile.getFileScope());
                if (globalFileVisibility == OdinSymbol.OdinVisibility.FILE_PRIVATE) continue;
                Collection<OdinSymbol> fileScopeDeclarations = odinFile.getFileScope().getScope().getSymbolTable()
                        .values()
                        .stream()
                        // TODO check why visibility is null
                        .filter(o -> !o.getVisibility().equals(OdinSymbol.OdinVisibility.FILE_PRIVATE))
                        .toList();


                scope.addAll(fileScopeDeclarations);
            }
        }

        // 3. Import symbols from the scope tree
        OdinScope odinScope = OdinSymbolFinder.doFindVisibleSymbols(element);
        odinScope.putAll(scope);
        odinScope.setPackagePath(packagePath);

        return odinScope;
    }

    public static OdinSymbol.OdinVisibility getGlobalFileVisibility(OdinFileScope fileScope) {
        PsiElement lineComment = PsiTreeUtil.skipSiblingsBackward(fileScope, PsiWhiteSpace.class);
        if (lineComment != null) {
            IElementType elementType = PsiUtilCore.getElementType(lineComment.getNode());
            if (elementType == OdinTypes.LINE_COMMENT) {
                if (lineComment.getText().equals("//+private")) {
                    return OdinSymbol.OdinVisibility.PACKAGE_PRIVATE;
                }

                if (lineComment.getText().equals("//+private file")) {
                    return OdinSymbol.OdinVisibility.FILE_PRIVATE;
                }
            }
        }
        return OdinSymbol.OdinVisibility.PUBLIC;
    }

    /**
     * Gets the files in the indicated package but excludes the file fileName
     *
     * @param project     The current project
     * @param packagePath The packagePath
     * @param fileName    The file to exclude
     * @return Other files in package
     */
    private static @NotNull List<OdinFile> getOtherFilesInPackage(@NotNull Project project, @NotNull String packagePath, String fileName) {
        return OdinImportUtils.getFilesInPackage(project, Path.of(packagePath), virtualFile -> !virtualFile.getName().equals(fileName));
    }
}

