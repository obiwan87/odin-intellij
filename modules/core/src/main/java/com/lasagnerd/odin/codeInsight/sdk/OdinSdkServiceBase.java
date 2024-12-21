package com.lasagnerd.odin.codeInsight.sdk;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiNamedElement;
import com.lasagnerd.odin.codeInsight.OdinAttributeUtils;
import com.lasagnerd.odin.codeInsight.OdinContext;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.OdinSymbolTable;
import com.lasagnerd.odin.codeInsight.dataflow.OdinSymbolValueStore;
import com.lasagnerd.odin.codeInsight.evaluation.EvEnumValue;
import com.lasagnerd.odin.codeInsight.evaluation.EvOdinValue;
import com.lasagnerd.odin.codeInsight.evaluation.EvOdinValues;
import com.lasagnerd.odin.codeInsight.imports.OdinImport;
import com.lasagnerd.odin.codeInsight.imports.OdinImportUtils;
import com.lasagnerd.odin.codeInsight.symbols.OdinScope;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbol;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolType;
import com.lasagnerd.odin.codeInsight.symbols.OdinVisibility;
import com.lasagnerd.odin.codeInsight.symbols.symbolTable.OdinSymbolTableHelper;
import com.lasagnerd.odin.codeInsight.typeInference.OdinTypeResolver;
import com.lasagnerd.odin.codeInsight.typeSystem.*;
import com.lasagnerd.odin.lang.OdinFileType;
import com.lasagnerd.odin.lang.psi.*;
import com.lasagnerd.odin.projectSettings.OdinSdkUtils;
import org.apache.commons.lang3.ArchUtils;
import org.apache.commons.lang3.arch.Processor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;

import static com.lasagnerd.odin.codeInsight.typeInference.OdinTypeResolver.createTypeReference;

public abstract class OdinSdkServiceBase implements OdinSdkService {

    // Prone to changes
    private static final List<String> SYNTHETIC_CONSTANTS = List.of(
            "ODIN_BUILD_MODE",
            "ODIN_ENDIAN",
            "ODIN_PLATFORM_SUBTARGET",
            "ODIN_SANITIZER_FLAGS",
            "ODIN_NO_RTTI",
            "ODIN_DISABLE_ASSERT",
            "ODIN_OS_STRING",
            "ODIN_ENDIAN_STRING",
            "ODIN_ARCH_STRING",
            "ODIN_ERROR_POS_STYLE",
            "ODIN_DEFAULT_TO_NIL_ALLOCATOR",
            "ODIN_NO_DYNAMIC_LITERALS",
            "ODIN_NO_CRT",
            "ODIN_USE_SEPARATE_MODULES",
            "ODIN_TEST",
            "ODIN_NO_ENTRY_POINT",
            "ODIN_FOREIGN_ERROR_PROCEDURES",
            "ODIN_NO_RTTI",
            "ODIN_BUILD_PROJECT_NAME",
            "ODIN_VALGRIND_SUPPORT",
            "ODIN_OPTIMIZATION_MODE",
            "ODIN_DEFAULT_TO_PANIC_ALLOCATOR",
            "ODIN_NO_BOUNDS_CHECK",
            "ODIN_MINIMUM_OS_VERSION"
    );
    private static final Logger log = Logger.getInstance(OdinSdkServiceBase.class);
    private static final Object LOADING_MUTEX = new Object();
    protected final Project project;
    /**
     * These symbols are implicitly imported
     */
    private List<OdinSymbol> builtInSymbols;

    /**
     * Symbols contained in runtime/core.odin. Contains the definition of SymbolTable, which we need
     */
    private List<OdinSymbol> runtimeCoreSymbols;

    private final Map<String, OdinSymbol> symbolsCache = new HashMap<>();
    private final Map<String, TsOdinType> typesCache = new HashMap<>();
    private Map<OdinImport, List<OdinFile>> sdkImportsCache;
    private Map<OdinSymbol, EvOdinValue> builtInValues;
    private final List<OdinFile> syntheticFiles = new ArrayList<>();


    public OdinSdkServiceBase(Project project) {
        this.project = project;
    }

    protected abstract OdinFile createOdinFile(Project project, Path path);

    private OdinFile createOdinFileFromResource(Project project, String resourcePath) {
        InputStream resource = OdinSymbolTableHelper.class.getClassLoader().getResourceAsStream(resourcePath);
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
            loadSymbols(List.of(coreOdinPath), symbols);
            runtimeCoreSymbols = symbols;
        }
        return runtimeCoreSymbols;
    }

    public abstract Optional<String> getSdkPath();

    @Override
    public List<OdinSymbol> getBuiltInSymbols() {
        loadBuiltinSymbols();
        return builtInSymbols;
    }

    protected void loadBuiltinSymbols() {
        synchronized (LOADING_MUTEX) {
            if (builtInSymbols == null) {
                builtInSymbols = new ArrayList<>();
                populateBuiltinSymbols(builtInSymbols);
            }
        }
    }

    @Override
    public OdinSymbolTable getBuiltInSymbolTable() {
        loadBuiltinValues();
        OdinSymbolTable symbolTable = new OdinSymbolTable();
        symbolTable.addAll(builtInSymbols);

        return symbolTable;
    }

    @Override
    public boolean isInSyntheticOdinFile(PsiElement element) {
        PsiFile containingFile = element.getContainingFile();
        if (containingFile instanceof OdinFile)
            return syntheticFiles.contains(containingFile);
        return false;
    }

    @Override
    public EvOdinValue getValue(String name) {
        loadBuiltinValues();
        return builtInValues.get(getBuiltinSymbol(name));
    }

    private void loadBuiltinValues() {
        if (builtInValues == null) {
            builtInValues = new HashMap<>();
            // This is not thread safe because it calls populate builtin symbols
            populateBuiltinValues(builtInValues);
        }
    }

    protected void populateBuiltinValues(Map<OdinSymbol, EvOdinValue> valueMap) {
        loadBuiltinSymbols();
        // ODIN_OS
        {
            setOdinOs(valueMap);
            setOdinArch(valueMap);
            valueMap.put(getBuiltinSymbol("ODIN_DEBUG"), EvOdinValues.BUILTIN_IDENTIFIERS.get("false"));
        }
    }

    private void setOdinArch(Map<OdinSymbol, EvOdinValue> valueMap) {
        TsOdinType odinArchType = getType("Odin_Arch_Type");
        Processor processor = ArchUtils.getProcessor();
        Processor.Type type = processor.getType();

        EvEnumValue enumName = switch (type) {
            case AARCH_64 -> new EvEnumValue("arm46", 0);
            case X86 -> new EvEnumValue("amd64", 1);
            case IA_64, PPC, UNKNOWN -> new EvEnumValue("Unknown", 0);
            case RISC_V -> new EvEnumValue("riscv64", 6);
            case null -> new EvEnumValue("Unknown", 0);
        };

        EvOdinValue value = new EvOdinValue(enumName, odinArchType);
        valueMap.put(getBuiltinSymbol("ODIN_ARCH"), value);
        valueMap.put(getBuiltinSymbol("ODIN_ARCH_STRING"), new EvOdinValue(enumName.getName().toLowerCase(), TsOdinBuiltInTypes.STRING));
    }

    private void setOdinOs(Map<OdinSymbol, EvOdinValue> valueMap) {
        TsOdinType odinOsType = getType("Odin_OS_Type");
        /*
        	    Unknown,
                Windows,
                Darwin,
                Linux,
                Essence,
                FreeBSD,
                Haiku,
                OpenBSD,
                WASI,
                JS,
                Freestanding,
         */

        EvEnumValue enumValue;
        if (SystemInfo.isWindows) {
            enumValue = new EvEnumValue("Windows", 1);
        } else if (SystemInfo.isLinux || SystemInfo.isMac) {
            enumValue = new EvEnumValue("Linux", 3);
        } else {
            enumValue = new EvEnumValue("Unknown", 0);
        }
        EvOdinValue value = new EvOdinValue(enumValue, odinOsType);
        valueMap.put(getBuiltinSymbol("ODIN_OS"), value);

        EvOdinValue stringValue = new EvOdinValue(enumValue
                .getName()
                .toLowerCase(), TsOdinBuiltInTypes.STRING);
        valueMap.put(getBuiltinSymbol("ODIN_OS_STRING"), stringValue);
    }

    @Override
    public OdinSymbol getRuntimeCoreSymbol(String symbolName) {
        if (symbolsCache.get(symbolName) == null) {
            List<OdinSymbol> builtInSymbols = getRuntimeCoreSymbols();
            var symbol = builtInSymbols.stream()
                    .filter(s -> s.getName().equals(symbolName))
                    .findFirst()
                    .orElse(null);
            symbolsCache.put(symbolName, symbol);
        }
        return symbolsCache.get(symbolName);
    }

    @Override
    public TsOdinType getType(String typeName) {
        if (typesCache.get(typeName) == null) {
            OdinSymbol symbol = getRuntimeCoreSymbol(typeName);
            if (symbol == null) {
                System.out.printf("No builtin type with name %s%n", typeName);
                return TsOdinBuiltInTypes.UNKNOWN;
            }
            PsiNamedElement declaredIdentifier = symbol.getDeclaredIdentifier();
            if (declaredIdentifier instanceof OdinDeclaredIdentifier odinDeclaredIdentifier) {
                OdinSymbolTable builtinSymbols = OdinSymbolTable.from(getBuiltInSymbols());
                OdinSymbolTable symbolTable = OdinSymbolTableHelper.buildFullSymbolTable(declaredIdentifier, new OdinContext());
                symbolTable.setParentSymbolTable(builtinSymbols);

                TsOdinType tsOdinType = OdinTypeResolver.resolveType(symbolTable.asContext(), odinDeclaredIdentifier);
                if (tsOdinType != null) {
                    typesCache.put(typeName, tsOdinType);
                }
            }
        }
        return typesCache.getOrDefault(typeName, TsOdinBuiltInTypes.UNKNOWN);
    }

    @Override
    public OdinSymbol createImplicitStructSymbol(String symbolName,
                                                 String structTypeName,
                                                 OdinSymbolType symbolType,
                                                 OdinScope symbolScope,
                                                 OdinVisibility symbolVisibility) {
        OdinSymbol odinSymbol = new OdinSymbol();
        odinSymbol.setImplicitlyDeclared(true);
        odinSymbol.setPsiType(findStructType(structTypeName));
        odinSymbol.setName(symbolName);
        odinSymbol.setSymbolType(symbolType);
        odinSymbol.setScope(symbolScope);
        odinSymbol.setVisibility(symbolVisibility);
        // TODO
        odinSymbol.setPackagePath("");

        return odinSymbol;
    }

    @Override
    public void invalidateCache() {
        builtInSymbols = null;
        runtimeCoreSymbols = null;
        sdkImportsCache = null;
        builtInValues = null;
        symbolsCache.clear();
        typesCache.clear();
        syntheticFiles.clear();
    }

    @Override
    public Map<OdinImport, List<OdinFile>> getSdkPackages() {
        if (sdkImportsCache != null)
            return sdkImportsCache;
        sdkImportsCache = new HashMap<>();
        Optional<String> sdkPath = OdinSdkUtils.getValidSdkPath(project);
        if (sdkPath.isPresent()) {
            List<String> collections = List.of("core", "base", "vendor");
            for (String collection : collections) {
                Path rootDirPath = Path.of(sdkPath.get(), collection);
                VirtualFile rootDirPathFile = VirtualFileManager.getInstance().findFileByNioPath(rootDirPath);
                if (rootDirPathFile != null) {
                    Map<OdinImport, List<OdinFile>> odinImportListMap = OdinImportUtils.collectImportablePackages(project, rootDirPathFile, collection, null);
                    sdkImportsCache.putAll(odinImportListMap);
                }
            }
        }

        return sdkImportsCache;
    }

    private OdinStructType findStructType(String typeName) {
        OdinSymbol symbol = getRuntimeCoreSymbol(typeName);
        if (symbol == null)
            return null;

        OdinDeclaration declaration = symbol.getDeclaration();

        return OdinInsightUtils.getDeclaredType(declaration, OdinStructType.class);
    }

    protected void populateBuiltinSymbols(List<OdinSymbol> builtinSymbols) {
        System.out.println("Loading builtin symbols");
        Optional<String> sdkPathOptional = getSdkPath();

        if (sdkPathOptional.isEmpty())
            return;

        // Built-in primitive types (i32, i64, ...)
        String sdkPath = sdkPathOptional.get();
        Collection<TsOdinType> builtInTypes = TsOdinBuiltInTypes.getBuiltInTypes();
        for (TsOdinType builtInType : builtInTypes) {
            typesCache.put(builtInType.getName(), createTypeReference(builtInType, false));
        }

        List<OdinSymbol> syntheticCompilerDeclarations = loadSyntheticCompilerDeclarations();
        Path builtinOdinPath = Path.of(sdkPath, "base", "builtin", "builtin.odin");
        Path coreOdinPath = Path.of(sdkPath, "base", "runtime", "core.odin");

        List<OdinSymbol> builtinOdinSymbols = new ArrayList<>();
        loadSymbols(List.of(builtinOdinPath), builtinOdinSymbols);

        List<OdinSymbol> coreOdinSymbols = new ArrayList<>();
        loadSymbols(List.of(coreOdinPath), coreOdinSymbols);
        builtinSymbols.addAll(builtinOdinSymbols);

        OdinSymbolTable syntheticSymbolTable = OdinSymbolTable.from(syntheticCompilerDeclarations);
        OdinSymbolTable builtinOdinSymbolTable = OdinSymbolTable.from(builtinOdinSymbols);
        OdinSymbolTable coreOdinSymbolTable = OdinSymbolTable.from(coreOdinSymbols);

        // Add byte alias
        {
            OdinSymbol byteSymbol = builtinOdinSymbolTable.getSymbol("byte");
            Objects.requireNonNull(byteSymbol);
            typesCache.put("byte", createTypeReference(
                    TsOdinBuiltInTypes.createByteAlias((OdinDeclaredIdentifier) byteSymbol.getDeclaredIdentifier()), false
            ));
        }

        // Add any with the backing type Raw_Any
        {
            OdinSymbol anyTypeSymbol = coreOdinSymbolTable.getSymbol("Raw_Any");
            Objects.requireNonNull(anyTypeSymbol);
            OdinDeclaredIdentifier declaredIdentifier = (OdinDeclaredIdentifier) anyTypeSymbol.getDeclaredIdentifier();
            TsOdinTypeReference anyStructType = (TsOdinTypeReference) declaredIdentifier.getType(new OdinContext());
            typesCache.put("any", createTypeReference(new TsOdinAnyType((TsOdinStructType) anyStructType.referencedType()), false));
        }

        // Add boolean constants
        addConstant("false", "bool");
        addConstant("true", "bool");

        // Add synthetic types & symbols
        addConstant("ODIN_VENDOR", "string");
        addConstant("ODIN_VERSION", "string");
        addConstant("ODIN_ROOT", "string");
        addConstant("ODIN_DEBUG", "bool");

        // Defined in builtin.odin, but type is loaded from compiler_declarations.odin
        addConstantWithSyntheticType(syntheticSymbolTable, "ODIN_OS", "Odin_OS_Type");
        addConstantWithSyntheticType(syntheticSymbolTable, "ODIN_ARCH", "Odin_Arch_Type");
        addConstantWithSyntheticType(syntheticSymbolTable, "ODIN_BUILD_MODE", "Odin_Build_Mode_Type");

        // Entirely defined in compiler_declarations.odin
        builtinSymbols.add(syntheticSymbolTable.getSymbol("ODIN_BUILD_MODE"));
        builtinSymbols.add(syntheticSymbolTable.getSymbol("ODIN_ENDIAN"));
        builtinSymbols.add(syntheticSymbolTable.getSymbol("ODIN_PLATFORM_SUBTARGET"));
        builtinSymbols.add(syntheticSymbolTable.getSymbol("ODIN_SANITIZER_FLAGS"));
        builtinSymbols.add(syntheticSymbolTable.getSymbol("ODIN_NO_RTTI"));
        builtinSymbols.add(syntheticSymbolTable.getSymbol("ODIN_DISABLE_ASSERT"));
        builtinSymbols.add(syntheticSymbolTable.getSymbol("ODIN_OS_STRING"));
        builtinSymbols.add(syntheticSymbolTable.getSymbol("ODIN_ENDIAN_STRING"));
        builtinSymbols.add(syntheticSymbolTable.getSymbol("ODIN_ARCH_STRING"));
        builtinSymbols.add(syntheticSymbolTable.getSymbol("ODIN_ERROR_POS_STYLE"));
        builtinSymbols.add(syntheticSymbolTable.getSymbol("ODIN_DEFAULT_TO_NIL_ALLOCATOR"));
        builtinSymbols.add(syntheticSymbolTable.getSymbol("ODIN_NO_DYNAMIC_LITERALS"));
        builtinSymbols.add(syntheticSymbolTable.getSymbol("ODIN_NO_CRT"));
        builtinSymbols.add(syntheticSymbolTable.getSymbol("ODIN_USE_SEPARATE_MODULES"));
        builtinSymbols.add(syntheticSymbolTable.getSymbol("ODIN_TEST"));
        builtinSymbols.add(syntheticSymbolTable.getSymbol("ODIN_NO_ENTRY_POINT"));
        builtinSymbols.add(syntheticSymbolTable.getSymbol("ODIN_FOREIGN_ERROR_PROCEDURES"));
        builtinSymbols.add(syntheticSymbolTable.getSymbol("ODIN_NO_RTTI"));
        builtinSymbols.add(syntheticSymbolTable.getSymbol("ODIN_BUILD_PROJECT_NAME"));
        builtinSymbols.add(syntheticSymbolTable.getSymbol("ODIN_VALGRIND_SUPPORT"));
        builtinSymbols.add(syntheticSymbolTable.getSymbol("ODIN_OPTIMIZATION_MODE"));
        builtinSymbols.add(syntheticSymbolTable.getSymbol("ODIN_DEFAULT_TO_PANIC_ALLOCATOR"));
        builtinSymbols.add(syntheticSymbolTable.getSymbol("ODIN_NO_BOUNDS_CHECK"));
        builtinSymbols.add(syntheticSymbolTable.getSymbol("ODIN_MINIMUM_OS_VERSION"));

        // Load symbols marked with @builtin
        Path coreBuiltinPath = Path.of(sdkPath, "base", "runtime", "core_builtin.odin");
        Path coreBuiltinSoaPath = Path.of(sdkPath, "base", "runtime", "core_builtin_soa.odin");

        List<Path> builtinPaths = List.of(coreBuiltinPath, coreBuiltinSoaPath);
        loadSymbols(builtinPaths,
                builtinSymbols,
                odinSymbol -> OdinAttributeUtils.containsAttribute(odinSymbol.getAttributes(), "builtin"));
    }

    @Override
    public OdinSymbolValueStore getSymbolValueStore() {
        loadBuiltinValues();

        OdinSymbolValueStore symbolValueStore = new OdinSymbolValueStore();
        symbolValueStore.putAll(builtInValues);
        return symbolValueStore;
    }

    private @NotNull List<OdinSymbol> loadSyntheticCompilerDeclarations() {
        List<OdinSymbol> syntheticCompilerDeclarations = new ArrayList<>();
        List<String> resources = List.of("odin/compiler_declarations.odin");
        for (String resource : resources) {
            OdinFile odinFile = createOdinFileFromResource(project, resource);
            syntheticFiles.add(odinFile);
            if (odinFile != null) {
                OdinSymbolTable fileScopeDeclarations = odinFile.getFileScope().getFullSymbolTable();
                fileScopeDeclarations
                        .getSymbolTable()
                        .values()
                        .stream()
                        .flatMap(List::stream)
                        .filter(odinSymbol1 -> OdinAttributeUtils.containsAttribute(odinSymbol1.getAttributes(), "builtin")
                                || odinSymbol1.getSymbolType() == OdinSymbolType.PACKAGE_REFERENCE)
                        .forEach(symbol -> {
                            symbol.setBuiltin(true);
                            syntheticCompilerDeclarations.add(symbol);
                        });
            }
        }
        return syntheticCompilerDeclarations;
    }

    private void addConstantWithSyntheticType(OdinSymbolTable syntheticSymbolTable, String constantIdentifier, String constantTypeIdentifier) {
        OdinSymbol symbol = syntheticSymbolTable.getSymbol(constantTypeIdentifier);
        Objects.requireNonNull(symbol);
        OdinDeclaredIdentifier declaredIdentifier = (OdinDeclaredIdentifier) symbol.getDeclaredIdentifier();
        TsOdinTypeReference type = (TsOdinTypeReference) declaredIdentifier.getType(new OdinContext());
        typesCache.put(constantTypeIdentifier, type);
        typesCache.put(constantIdentifier, type.referencedType());
    }

    private void addConstant(String constantIdentifier, String constantTypeIdentifier) {
        TsOdinTypeReference typeReference = (TsOdinTypeReference) typesCache.get(constantTypeIdentifier);
        TsOdinType type = typeReference.referencedType();
        typesCache.put(constantIdentifier, type);
    }

    private void loadSymbols(List<Path> builtinPaths, List<OdinSymbol> builtinSymbols) {
        loadSymbols(builtinPaths, builtinSymbols, s -> true);
    }

    private void loadSymbols(List<Path> builtinPaths,
                             List<OdinSymbol> builtinSymbols,
                             Predicate<OdinSymbol> odinSymbolPredicate) {
        for (Path builtinPath : builtinPaths) {
            OdinFile odinFile = createOdinFile(project, builtinPath);
            if (odinFile != null) {
                OdinFileScope fileScope = odinFile.getFileScope();
                if (fileScope == null) {
                    log.error("File scope is null for file %s".formatted(odinFile.getVirtualFile().getPath()));
                } else {
                    OdinSymbolTable fileScopeDeclarations = fileScope.getFullSymbolTable();
                    fileScopeDeclarations
                            .getSymbolTable()
                            .values()
                            .stream()
                            .flatMap(List::stream)
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
