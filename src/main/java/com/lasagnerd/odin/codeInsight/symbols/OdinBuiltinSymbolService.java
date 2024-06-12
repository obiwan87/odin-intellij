package com.lasagnerd.odin.codeInsight.symbols;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.codeInsight.OdinAttributeUtils;
import com.lasagnerd.odin.lang.OdinFileType;
import com.lasagnerd.odin.lang.psi.OdinDeclaration;
import com.lasagnerd.odin.lang.psi.OdinFile;
import com.lasagnerd.odin.lang.psi.OdinStructDeclarationStatement;
import com.lasagnerd.odin.lang.psi.OdinStructType;
import com.lasagnerd.odin.sdkConfig.OdinSdkConfigPersistentState;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;

public class OdinBuiltinSymbolService {
    private final Project project;
    /**
     * These symbols are implicitly imported
     */
    private List<OdinSymbol> builtInSymbols;

    /**
     * Symbols contained in runtime/core.odin. Contains the definition of Context, which we need
     */
    private List<OdinSymbol> runtimeCoreSymbols;

    private OdinSymbol context;

    public OdinBuiltinSymbolService(Project project) {
        this.project = project;
    }

    public static OdinBuiltinSymbolService getInstance(Project project) {
        return project.getService(OdinBuiltinSymbolService.class);
    }

    public static OdinFile createOdinFile(Project project, Path path) {
        VirtualFile virtualFile = VfsUtil.findFile(path, true);
        if (virtualFile != null) {
            return (OdinFile) PsiManager.getInstance(project).findFile(virtualFile);
        }
        return null;
    }

    public static OdinFile createOdinFileFromResource(Project project, String resourcePath) {
        InputStream resource = OdinSymbolTableResolver.class.getClassLoader().getResourceAsStream(resourcePath);
        if (resource == null)
            return null;
        try (resource) {
            String text = new String(resource.readAllBytes(), StandardCharsets.UTF_8);
            return (OdinFile) PsiFileFactory.getInstance(project).createFileFromText("resource.odin", OdinFileType.INSTANCE, text);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<OdinSymbol> getRuntimeCoreSymbols() {
        if(runtimeCoreSymbols == null) {
            Optional<String> sdkPathOptional = OdinSdkConfigPersistentState.getSdkPath(project);

            if (sdkPathOptional.isEmpty()) {
                return Collections.emptyList();
            }
            List<OdinSymbol> symbols = new ArrayList<>();
            String sdkPath = sdkPathOptional.get();
            Path coreOdinPath = Path.of(sdkPath, "base", "runtime", "core.odin");
            doFindBuiltInSymbols(List.of(coreOdinPath), symbols);
            runtimeCoreSymbols = symbols;
        }
        return runtimeCoreSymbols;
    }
    public List<OdinSymbol> getBuiltInSymbols() {
        if(builtInSymbols == null)
            builtInSymbols = doFindBuiltInSymbols();
        return builtInSymbols;
    }

    public OdinSymbol getContextStructSymbol() {
        if(context == null) {
            List<OdinSymbol> builtInSymbols = getRuntimeCoreSymbols();
            context = builtInSymbols.stream().filter(s -> s.getName().equals("Context")).findFirst().orElse(null);
        }
        return context;
    }

    public OdinSymbol createNewContextParameterSymbol() {
        OdinSymbol odinSymbol = new OdinSymbol();
        odinSymbol.setImplicitlyDeclared(true);
        odinSymbol.setPsiType(getContextType());
        odinSymbol.setName("context");
        odinSymbol.setSymbolType(OdinSymbol.OdinSymbolType.PARAMETER);
        odinSymbol.setScope(OdinSymbol.OdinScope.LOCAL);
        odinSymbol.setVisibility(OdinSymbol.OdinVisibility.NONE);
        // TODO
        odinSymbol.setPackagePath("");

        return odinSymbol;
    }

    private OdinStructType getContextType() {
        OdinSymbol contextStructSymbol = getContextStructSymbol();
        if(contextStructSymbol == null)
            return null;

        OdinDeclaration declaration = contextStructSymbol.getDeclaration();
        if(declaration instanceof OdinStructDeclarationStatement structDeclarationStatement) {
                return structDeclarationStatement.getStructType();
        }
        return null;
    }

    public OdinDeclaration getContextStructDeclaration() {
        OdinSymbol contextStructSymbol = getContextStructSymbol();
        if(contextStructSymbol != null) {
            return PsiTreeUtil.getParentOfType(contextStructSymbol.getDeclaredIdentifier(), OdinDeclaration.class, true);
        }
        return null;
    }



    private @NotNull List<OdinSymbol> doFindBuiltInSymbols() {
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
        doFindBuiltInSymbols(builtinPaths, builtinSymbols, odinSymbol -> OdinAttributeUtils.containsBuiltin(odinSymbol.getAttributeStatements()));

        List<String> resources = List.of("odin/builtin.odin", "odin/annotations.odin");
        for (String resource : resources) {
            OdinFile odinFile = createOdinFileFromResource(project, resource);
            if (odinFile != null) {
                OdinSymbolTable fileScopeDeclarations = odinFile.getFileScope().getSymbolTable();
                Collection<OdinSymbol> symbols = fileScopeDeclarations
                        .getSymbolNameMap().values()
                        .stream()
                        .filter(odinSymbol -> OdinAttributeUtils.containsBuiltin(odinSymbol.getAttributeStatements()))
                        .toList();
                builtinSymbols.addAll(symbols);
            }
        }
        return builtinSymbols;
    }

    private void doFindBuiltInSymbols(List<Path> builtinPaths, List<OdinSymbol> builtinSymbols) {
        doFindBuiltInSymbols(builtinPaths, builtinSymbols, s -> true);
    }

    private void doFindBuiltInSymbols(List<Path> builtinPaths, List<OdinSymbol> builtinSymbols, Predicate<OdinSymbol> odinSymbolPredicate) {
        for (Path builtinPath : builtinPaths) {
            OdinFile odinFile = createOdinFile(project, builtinPath);
            if (odinFile != null) {
                OdinSymbolTable fileScopeDeclarations = odinFile.getFileScope().getSymbolTable();
                Collection<OdinSymbol> symbols = fileScopeDeclarations
                        .getSymbolNameMap().values()
                        .stream()
                        .filter(odinSymbolPredicate)
                        .toList();
                builtinSymbols.addAll(symbols);
            }
        }
    }
}
