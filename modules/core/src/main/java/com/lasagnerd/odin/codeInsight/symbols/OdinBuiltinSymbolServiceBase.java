package com.lasagnerd.odin.codeInsight.symbols;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiNamedElement;
import com.lasagnerd.odin.codeInsight.OdinAttributeUtils;
import com.lasagnerd.odin.codeInsight.typeInference.OdinTypeResolver;
import com.lasagnerd.odin.codeInsight.typeSystem.*;
import com.lasagnerd.odin.lang.OdinFileType;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;

public abstract class OdinBuiltinSymbolServiceBase implements OdinBuiltinSymbolService {
    private static final Logger log = Logger.getInstance(OdinBuiltinSymbolServiceBase.class);
    protected final Project project;
    /**
     * These symbols are implicitly imported
     */
    private List<OdinSymbol> builtInSymbols;

    /**
     * Symbols contained in runtime/core.odin. Contains the definition of Context, which we need
     */
    private List<OdinSymbol> runtimeCoreSymbols;

    private final Map<String, OdinSymbol> cachedSymbols = new HashMap<>();
    private final Map<String, TsOdinType> cachedTypes = new HashMap<>();

    public OdinBuiltinSymbolServiceBase(Project project) {
        this.project = project;
    }

    protected abstract OdinFile createOdinFile(Project project, Path path);

    private OdinFile createOdinFileFromResource(Project project, String resourcePath) {
        InputStream resource = OdinSymbolTableResolver.class.getClassLoader().getResourceAsStream(resourcePath);
        if (resource == null)
            return null;
        try (resource) {
            String text = new String(resource.readAllBytes(), StandardCharsets.UTF_8);
            return (OdinFile) getPsiFileFactory(project).createFileFromText("resource.odin", OdinFileType.INSTANCE, text);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract PsiFileFactory getPsiFileFactory(Project project);

    @Override
    public List<OdinSymbol> getRuntimeCoreSymbols() {
        if (runtimeCoreSymbols == null || runtimeCoreSymbols.isEmpty()) {
            Optional<String> sdkPathOptional = getSdkPath();


            if (sdkPathOptional.isEmpty()) {
                return Collections.emptyList();
            }
            String sdkPath = sdkPathOptional.get();
            List<OdinSymbol> symbols = new ArrayList<>();
            Path coreOdinPath = Path.of(sdkPath, "base", "runtime", "core.odin");
            doFindBuiltInSymbols(List.of(coreOdinPath), symbols);
            runtimeCoreSymbols = symbols;
        }
        return runtimeCoreSymbols;
    }

    public abstract Optional<String> getSdkPath();

    @Override
    public List<OdinSymbol> getBuiltInSymbols() {
        if (builtInSymbols == null)
            builtInSymbols = doFindBuiltInSymbols();
        return builtInSymbols;
    }

    @Override
    public OdinSymbol getSymbol(String symbolName) {
        if (cachedSymbols.get(symbolName) == null) {
            List<OdinSymbol> builtInSymbols = getRuntimeCoreSymbols();
            var symbol = builtInSymbols.stream().filter(s -> s.getName().equals(symbolName)).findFirst().orElse(null);
            cachedSymbols.put(symbolName, symbol);
        }
        return cachedSymbols.get(symbolName);
    }

    @Override
    public TsOdinType getType(String typeName) {
        if (cachedTypes.get(typeName) == null) {
            OdinSymbol symbol = getSymbol(typeName);

            PsiNamedElement declaredIdentifier = symbol.getDeclaredIdentifier();
            if (declaredIdentifier instanceof OdinDeclaredIdentifier odinDeclaredIdentifier) {
                OdinSymbolTable builtinSymbols = OdinSymbolTable.from(getBuiltInSymbols());
                OdinSymbolTable symbolTable = OdinSymbolTableResolver.computeSymbolTable(declaredIdentifier);
                symbolTable.setParentSymbolTable(builtinSymbols);

                TsOdinType tsOdinType = OdinTypeResolver.resolveType(symbolTable, odinDeclaredIdentifier);
                if (tsOdinType != null) {
                    cachedTypes.put(typeName, tsOdinType);
                }
            }
        }
        return cachedTypes.getOrDefault(typeName, TsOdinBuiltInTypes.UNKNOWN);
    }

    @Override
    public OdinSymbol createImplicitStructSymbol(String symbolName, String structTypeName) {
        OdinSymbol odinSymbol = new OdinSymbol();
        odinSymbol.setImplicitlyDeclared(true);
        odinSymbol.setPsiType(findStructType(structTypeName));
        odinSymbol.setName(symbolName);
        odinSymbol.setSymbolType(OdinSymbolType.PARAMETER);
        odinSymbol.setScope(OdinSymbol.OdinScope.LOCAL);
        odinSymbol.setVisibility(OdinSymbol.OdinVisibility.NONE);
        // TODO
        odinSymbol.setPackagePath("");

        return odinSymbol;
    }

    private OdinStructType findStructType(String typeName) {
        OdinSymbol symbol = getSymbol(typeName);
        if (symbol == null)
            return null;

        OdinDeclaration declaration = symbol.getDeclaration();
        if (declaration instanceof OdinStructDeclarationStatement structDeclarationStatement) {
            return structDeclarationStatement.getStructType();
        }
        return null;
    }

    private @NotNull List<OdinSymbol> doFindBuiltInSymbols() {
        // TODO Cache this stuff
        List<OdinSymbol> builtinSymbols = new ArrayList<>();
        Collection<TsOdinType> builtInTypes = TsOdinBuiltInTypes.getBuiltInTypes();
        for (TsOdinType builtInType : builtInTypes) {
            OdinSymbol odinSymbol = new OdinSymbol();
            odinSymbol.setName(builtInType.getName());
            odinSymbol.setScope(OdinSymbol.OdinScope.TYPE);
            odinSymbol.setSymbolType(OdinSymbolType.BUILTIN_TYPE);
            odinSymbol.setBuiltin(true);
            odinSymbol.setVisibility(OdinSymbol.OdinVisibility.PUBLIC);
            odinSymbol.setImplicitlyDeclared(true);
            builtinSymbols.add(odinSymbol);
        }
        // 0. Import built-in symbols
        Optional<String> sdkPathOptional = getSdkPath();

        if (sdkPathOptional.isEmpty())
            return Collections.emptyList();

        String sdkPath = sdkPathOptional.get();

        Path coreBuiltinPath = Path.of(sdkPath, "base", "runtime", "core_builtin.odin");
        Path coreBuiltinSoaPath = Path.of(sdkPath, "base", "runtime", "core_builtin_soa.odin");

        List<Path> builtinPaths = List.of(coreBuiltinPath, coreBuiltinSoaPath);
        doFindBuiltInSymbols(builtinPaths,
                builtinSymbols,
                odinSymbol -> OdinAttributeUtils.containsAttribute(odinSymbol.getAttributes(), "builtin"));

        List<String> resources = List.of("odin/builtin.odin", "odin/annotations.odin");
        for (String resource : resources) {
            OdinFile odinFile = createOdinFileFromResource(project, resource);
            if (odinFile != null) {
                OdinSymbolTable fileScopeDeclarations = odinFile.getFileScope().getSymbolTable();
                fileScopeDeclarations
                        .getSymbolNameMap().values()
                        .stream()
                        .filter(odinSymbol -> OdinAttributeUtils.containsAttribute(odinSymbol.getAttributes(), "builtin")
                                || odinSymbol.getSymbolType() == OdinSymbolType.PACKAGE_REFERENCE)
                        .forEach(symbol -> {
                            symbol.setBuiltin(true);
                            builtinSymbols.add(symbol);
                        });
            }
        }

//        createBuiltinProcedures(builtinSymbols);
        return builtinSymbols;
    }

    private void createBuiltinProcedures(List<OdinSymbol> builtinSymbols) {
        OdinSymbolTable symbolTable = OdinSymbolTable.from(builtinSymbols);
        OdinSymbol typeInfoSymbol = symbolTable.getSymbol("TypeInfo");
        if (typeInfoSymbol != null) {

            TsOdinType typeInfoStruct = OdinTypeResolver.resolveType(
                    symbolTable,
                    typeInfoSymbol
            );
            TsOdinPointerType tsOdinPointerType = new TsOdinPointerType();
            tsOdinPointerType.setDereferencedType(typeInfoStruct);
            tsOdinPointerType.setSymbolTable(symbolTable);


            OdinSymbol typeInfoOf = symbolTable.getSymbol("type_info_of");
            if (typeInfoOf != null) {
                TsOdinProcedureType typeInfoOfProcedure = (TsOdinProcedureType) OdinTypeResolver.resolveType(symbolTable, typeInfoOf);
                TsOdinParameter tsOdinParameter = typeInfoOfProcedure.getReturnParameters().getFirst();
                tsOdinParameter.setType(tsOdinPointerType);
                tsOdinParameter.setPsiType(null);

                typeInfoOfProcedure.getReturnTypes().set(0, tsOdinPointerType);
            }
        }
    }

    private void doFindBuiltInSymbols(List<Path> builtinPaths, List<OdinSymbol> builtinSymbols) {
        doFindBuiltInSymbols(builtinPaths, builtinSymbols, s -> true);
    }

    private void doFindBuiltInSymbols(List<Path> builtinPaths, List<OdinSymbol> builtinSymbols, Predicate<OdinSymbol> odinSymbolPredicate) {
        for (Path builtinPath : builtinPaths) {
            OdinFile odinFile = createOdinFile(project, builtinPath);
            if (odinFile != null) {
                OdinFileScope fileScope = odinFile.getFileScope();
                if (fileScope == null) {
                    log.error("File scope is null for file %s".formatted(odinFile.getVirtualFile().getPath()));
                } else {
                    OdinSymbolTable fileScopeDeclarations = fileScope.getSymbolTable();
                    fileScopeDeclarations
                            .getSymbolNameMap().values()
                            .stream()
                            .filter(odinSymbolPredicate)
                            .forEach(symbol -> {
                                symbol.setBuiltin(true);
                                builtinSymbols.add(symbol);
                            });
                }
            }
        }
    }
}
