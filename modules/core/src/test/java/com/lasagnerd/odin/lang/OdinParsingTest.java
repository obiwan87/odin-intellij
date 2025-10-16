// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.lasagnerd.odin.lang;

import com.intellij.concurrency.IdeaForkJoinWorkerThreadFactory;
import com.intellij.ide.plugins.PluginUtil;
import com.intellij.ide.plugins.PluginUtilImpl;
import com.intellij.ide.startup.impl.StartupManagerImpl;
import com.intellij.lang.*;
import com.intellij.lang.impl.PsiBuilderFactoryImpl;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.mock.*;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.extensions.*;
import com.intellij.openapi.extensions.impl.ExtensionPointImpl;
import com.intellij.openapi.extensions.impl.ExtensionsAreaImpl;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.impl.FileDocumentManagerBase;
import com.intellij.openapi.fileEditor.impl.LoadTextUtil;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.options.SchemeManagerFactory;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.impl.ProgressManagerImpl;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.pom.PomModel;
import com.intellij.pom.core.impl.PomModelImpl;
import com.intellij.pom.tree.TreeAspect;
import com.intellij.psi.*;
import com.intellij.psi.impl.PsiCachedValuesFactory;
import com.intellij.psi.impl.PsiFileFactoryImpl;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistryImpl;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageManagerImpl;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.*;
import com.intellij.util.CachedValuesManagerImpl;
import com.intellij.util.KeyedLazyInstance;
import com.intellij.util.SystemProperties;
import com.intellij.util.messages.MessageBus;
import com.lasagnerd.odin.codeInsight.OdinContext;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.OdinSymbolTable;
import com.lasagnerd.odin.codeInsight.dataflow.OdinLattice;
import com.lasagnerd.odin.codeInsight.dataflow.OdinWhenConstraintsSolver;
import com.lasagnerd.odin.codeInsight.evaluation.*;
import com.lasagnerd.odin.codeInsight.imports.OdinImportService;
import com.lasagnerd.odin.codeInsight.sdk.OdinSdkService;
import com.lasagnerd.odin.codeInsight.symbols.OdinReferenceResolver;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbol;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolType;
import com.lasagnerd.odin.codeInsight.symbols.OdinVisibility;
import com.lasagnerd.odin.codeInsight.symbols.symbolTable.OdinSymbolTableHelper;
import com.lasagnerd.odin.codeInsight.typeInference.OdinExpectedTypeEngine;
import com.lasagnerd.odin.codeInsight.typeInference.OdinTypeChecker;
import com.lasagnerd.odin.codeInsight.typeInference.OdinTypeConverter;
import com.lasagnerd.odin.codeInsight.typeSystem.*;
import com.lasagnerd.odin.lang.psi.*;
import com.lasagnerd.odin.lang.psi.impl.OdinAssignmentStatementImpl;
import com.lasagnerd.odin.settings.projectSettings.OdinProjectSettingsService;
import com.lasagnerd.odin.settings.projectSettings.OdinProjectSettingsServiceImpl;
import org.jetbrains.annotations.NotNull;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.MutablePicoContainer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static com.lasagnerd.odin.lang.OdinPsiTestHelpers.*;

/**
 * @noinspection unused, UnstableApiUsage, deprecation
 */
public class OdinParsingTest extends UsefulTestCase {
    private PluginDescriptor pluginDescriptor;

    private MockApplication app;
    protected MockProjectEx project;

    protected String myFilePrefix = "";
    protected String myFileExt;
    protected final String myFullDataPath;
    protected PsiFile myFile;
    private MockPsiManager myPsiManager;
    private PsiFileFactoryImpl myFileFactory;
    protected Language myLanguage;
    private final ParserDefinition[] myDefinitions;
    private final boolean myLowercaseFirstLetter;
    private ExtensionPointImpl<@NotNull KeyedLazyInstance<ParserDefinition>> myLangParserDefinition;

    public OdinParsingTest() {
        myDefinitions = new ParserDefinition[]{new OdinParserDefinition()};
        myFullDataPath = "main.odin";
        myFileExt = "odin";
        myLowercaseFirstLetter = true;
    }

    @NotNull
    protected MockApplication getApplication() {
        return app;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // This makes sure that tasks launched in the shared project are properly cancelled,
        // so they don't leak into the mock app of ParsingTestCase.
        LightPlatformTestCase.closeAndDeleteProject();
        MockApplication app = MockApplication.setUp(getTestRootDisposable());
        this.app = app;
        MutablePicoContainer appContainer = app.getPicoContainer();
        ComponentAdapter component = appContainer.getComponentAdapter(ProgressManager.class.getName());
        if (component == null) {
            appContainer.registerComponentInstance(ProgressManager.class.getName(), new ProgressManagerImpl());
        }
        IdeaForkJoinWorkerThreadFactory.setupForkJoinCommonPool(true);

        project = new MockProjectEx(getTestRootDisposable());
        myPsiManager = new MockPsiManager(project);
        myFileFactory = new PsiFileFactoryImpl(myPsiManager);
        appContainer.registerComponentInstance(MessageBus.class, app.getMessageBus());
        appContainer.registerComponentInstance(SchemeManagerFactory.class, new MockSchemeManagerFactory());
        MockEditorFactory editorFactory = new MockEditorFactory();
        appContainer.registerComponentInstance(EditorFactory.class, editorFactory);
        app.registerService(FileDocumentManager.class, new MockFileDocumentManagerImpl(FileDocumentManagerBase.HARD_REF_TO_DOCUMENT_KEY,
                editorFactory::createDocument));
        app.registerService(PluginUtil.class, new PluginUtilImpl());

        app.registerService(PsiBuilderFactory.class, new PsiBuilderFactoryImpl());
        app.registerService(DefaultASTFactory.class, new DefaultASTFactoryImpl());
        app.registerService(ReferenceProvidersRegistry.class, new ReferenceProvidersRegistryImpl());
        project.registerService(PsiDocumentManager.class, new MockPsiDocumentManager());
        project.registerService(PsiManager.class, myPsiManager);
        project.registerService(PsiFileFactory.class, myFileFactory);
        project.registerService(TreeAspect.class, new TreeAspect());
        project.registerService(SmartPointerManager.class, new MockSmartPointerManager());
        project.registerService(OdinProjectSettingsService.class, new MockProjectSettingsService());

        registerExtensionPoint(project.getExtensionArea(), MultiHostInjector.MULTIHOST_INJECTOR_EP_NAME, MultiHostInjector.class);
        registerExtensionPoint(app.getExtensionArea(), LanguageInjector.EXTENSION_POINT_NAME, LanguageInjector.class);
        project.registerService(DumbService.class, new MockDumbService(project));
        project.registerService(InjectedLanguageManager.class, new InjectedLanguageManagerImpl(project));

        project.registerService(CachedValuesManager.class, new CachedValuesManagerImpl(project, new PsiCachedValuesFactory(project)));
        project.registerService(StartupManager.class, new StartupManagerImpl(project, project.getCoroutineScope()));
        project.registerService(OdinImportService.class, new MockOdinImportService(myFileFactory));
        project.registerService(OdinSdkService.class, new MockSdkService(project, myFileFactory));
        project.registerService(OdinProjectSettingsServiceImpl.class, new OdinProjectSettingsServiceImpl());
        registerExtensionPoint(app.getExtensionArea(), FileTypeFactory.FILE_TYPE_FACTORY_EP, FileTypeFactory.class);
        registerExtensionPoint(app.getExtensionArea(), MetaLanguage.EP_NAME, MetaLanguage.class);

        myLangParserDefinition = app.getExtensionArea().registerFakeBeanPoint(LanguageParserDefinitions.INSTANCE.getName(), getPluginDescriptor());

        if (myDefinitions.length > 0) {
            configureFromParserDefinition(myDefinitions[0], myFileExt);
            // first definition is registered by configureFromParserDefinition
            for (int i = 1, length = myDefinitions.length; i < length; i++) {
                registerParserDefinition(myDefinitions[i]);
            }
        }

        // That's for reparse routines
        project.registerService(PomModel.class, new PomModelImpl(project));
        Registry.markAsLoaded();
    }

    protected final void registerParserDefinition(@NotNull ParserDefinition definition) {
        Language language = definition.getFileNodeType().getLanguage();
        myLangParserDefinition.registerExtension(new KeyedLazyInstance<>() {
            @Override
            public @NotNull String getKey() {
                return language.getID();
            }

            @NotNull
            @Override
            public ParserDefinition getInstance() {
                return definition;
            }
        });
        LanguageParserDefinitions.INSTANCE.clearCache(language);
        disposeOnTearDown(() -> LanguageParserDefinitions.INSTANCE.clearCache(language));
    }

    public void configureFromParserDefinition(@NotNull ParserDefinition definition, String extension) {
        myLanguage = definition.getFileNodeType().getLanguage();
        myFileExt = extension;
        registerParserDefinition(definition);
        app.registerService(FileTypeManager.class, new MockFileTypeManager(new MockLanguageFileType(myLanguage, myFileExt)));
    }

    protected final <T> void registerExtension(@NotNull ExtensionPointName<T> name, @NotNull T extension) {
        //noinspection unchecked
        registerExtensions(name, (Class<T>) extension.getClass(), Collections.singletonList(extension));
    }

    protected final <T> void registerExtensions(@NotNull ExtensionPointName<T> name, @NotNull Class<T> extensionClass, @NotNull List<? extends T> extensions) {
        ExtensionsAreaImpl area = app.getExtensionArea();
        ExtensionPoint<@NotNull T> point = area.getExtensionPointIfRegistered(name.getName());
        if (point == null) {
            point = registerExtensionPoint(area, name, extensionClass);
        }

        for (T extension : extensions) {
            // no need to specify disposable because ParsingTestCase in any case clean area for each test
            //noinspection deprecation
            point.registerExtension(extension);
        }
    }

    protected final <T> void addExplicitExtension(@NotNull LanguageExtension<T> collector, @NotNull Language language, @NotNull T object) {
        ExtensionsAreaImpl area = app.getExtensionArea();
        PluginDescriptor pluginDescriptor = getPluginDescriptor();
        if (!area.hasExtensionPoint(collector.getName())) {
            area.registerFakeBeanPoint(collector.getName(), pluginDescriptor);
        }
        LanguageExtensionPoint<T> extension = new LanguageExtensionPoint<>(language.getID(), object);
        extension.setPluginDescriptor(pluginDescriptor);
        ExtensionTestUtil.addExtension(area, collector, extension);
    }

    protected final <T> void registerExtensionPoint(@NotNull ExtensionPointName<T> extensionPointName, @NotNull Class<T> aClass) {
        registerExtensionPoint(app.getExtensionArea(), extensionPointName, aClass);
    }

    protected <T> ExtensionPointImpl<T> registerExtensionPoint(@NotNull ExtensionsAreaImpl extensionArea,
                                                               @NotNull BaseExtensionPointName<T> extensionPointName,
                                                               @NotNull Class<T> extensionClass) {
        // todo get rid of it - registerExtensionPoint should be not called several times
        String name = extensionPointName.getName();
        if (extensionArea.hasExtensionPoint(name)) {
            return extensionArea.getExtensionPoint(name);
        } else {
            return extensionArea.registerPoint(name, extensionClass, getPluginDescriptor(), false);
        }
    }

    @NotNull
    // easy debug of not disposed extension
    private PluginDescriptor getPluginDescriptor() {
        PluginDescriptor pluginDescriptor = this.pluginDescriptor;
        if (pluginDescriptor == null) {
            pluginDescriptor = new DefaultPluginDescriptor(PluginId.getId(getClass().getName() + "." + getTestName()), OdinParsingTest.class.getClassLoader());
            this.pluginDescriptor = pluginDescriptor;
        }
        return pluginDescriptor;
    }

    @NotNull
    public MockProjectEx getProject() {
        return project;
    }

    public MockPsiManager getPsiManager() {
        return myPsiManager;
    }

    @Override
    protected void tearDown() throws Exception {
        myFile = null;
        project = null;
        myPsiManager = null;
        myFileFactory = null;
        super.tearDown();
    }

    @NotNull
    public final String getTestName() {
        return getTestName(myLowercaseFirstLetter);
    }

    protected boolean includeRanges() {
        return false;
    }

    protected boolean skipSpaces() {
        return false;
    }

    protected boolean checkAllPsiRoots() {
        return true;
    }

    /* Sanity check against thoughtlessly copy-pasting actual test results as the expected test data. */

    protected PsiFile parseFile(String name, String text) {
        return parseFile(name, name, text);
    }

    protected PsiFile parseFile(String path, String name, String text) {
        myFile = createPsiFile(path, name, text);
        assertEquals("light virtual file text mismatch", text, ((LightVirtualFile) myFile.getVirtualFile()).getContent().toString());
        assertEquals("virtual file text mismatch", text, LoadTextUtil.loadText(myFile.getVirtualFile()));
        assertEquals("doc text mismatch", text, Objects.requireNonNull(myFile.getViewProvider().getDocument()).getText());
        if (checkAllPsiRoots()) {
            for (PsiFile root : myFile.getViewProvider().getAllFiles()) {
                doSanityChecks(root);
            }
        } else {
            doSanityChecks(myFile);
        }
        return myFile;
    }

    protected PsiFile createPsiFile(String path, @NotNull String name, @NotNull String text) {
        String name1 = name.endsWith("." + myFileExt) ? name : name + "." + myFileExt;
        LightVirtualFile virtualFile = new LightVirtualFileWithPath(name1, path, myLanguage.getAssociatedFileType(), text);
        virtualFile.setCharset(StandardCharsets.UTF_8);
        return myFileFactory.trySetupPsiForFile(virtualFile, myLanguage, true, false);
    }

    protected void checkResult(@NotNull @TestDataFile String targetDataName, @NotNull PsiFile file) {
        doCheckResult(myFullDataPath, file, checkAllPsiRoots(), targetDataName, skipSpaces(), includeRanges(), allTreesInSingleFile());
        if (SystemProperties.getBooleanProperty("dumpAstTypeNames", false)) {
            printAstTypeNamesTree(targetDataName, file);
        }
    }


    private void printAstTypeNamesTree(@NotNull @TestDataFile String targetDataName, @NotNull PsiFile file) {
        StringBuffer buffer = new StringBuffer();
        Arrays.stream(file.getNode().getChildren(TokenSet.ANY)).forEach(it -> OdinPsiTestHelpers.printAstTypeNamesTree(it, buffer, 0));
        try {
            Files.writeString(Paths.get(myFullDataPath, targetDataName + ".fleet.txt"), buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected boolean allTreesInSingleFile() {
        return false;
    }

    protected void checkResult(@NotNull String actual) {
        String name = getTestName();
        doCheckResult(myFullDataPath, myFilePrefix + name + ".txt", actual);
    }

    protected void checkResult(@NotNull @TestDataFile String targetDataName, @NotNull String actual) {
        doCheckResult(myFullDataPath, targetDataName, actual);
    }

    protected String loadFile(@NotNull @TestDataFile String name) throws IOException {
        return loadFileDefault(myFullDataPath, name);
    }


    protected void loadAndCheck(String path) {
        try {
            // FileUtil.loadFile(new File(dir, packageName), CharsetToolkit.UTF8, true).trim();
            OdinFile odinFile = load(path);
            ensureNoErrors(odinFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    Map<String, OdinFile> fileCache = new HashMap<>();

    protected OdinFile load(String path) throws IOException {
        OdinFile odinFile = fileCache.get(path);
        if (odinFile == null) {
            String fileContent = FileUtil.loadFile(new File(path), CharsetToolkit.UTF8, true).trim();

            PsiFile psiFile = parseFile(path, Path.of(path).toFile().getName(), fileContent);

            odinFile = (OdinFile) psiFile;
            fileCache.put(path, odinFile);
        }
        return odinFile;
    }


    protected void ensureNoErrors(PsiFile file) {
        List<String> errors = new ArrayList<>();
        file.accept(new PsiRecursiveElementVisitor() {
            @Override
            public void visitErrorElement(@NotNull PsiErrorElement element) {
                errors.add(element.getTextOffset() + ": " + element.getErrorDescription());
                super.visitErrorElement(element);
            }
        });
        if (!errors.isEmpty()) {
            fail("Found PsiElement errors at offsets:\n" + String.join("\n", errors));
        }
    }

    //-----------------------------------------------------
    // TESTS
    //-----------------------------------------------------

    public void testSimpleFile() {
        String path = "src/test/testData/simple.odin";
        loadAndCheck(path);
    }

    public void testDemoFile() {
        String path = "src/test/testData/demo.odin";
        loadAndCheck(path);
    }

    public void testIntrinsicsFile() {
        String path = "src/test/testData/intrinsics.odin";
        loadAndCheck(path);
    }

    public void testTypeInference() throws IOException {
        OdinFile odinFile = loadTypeInference();
        var refExpressions = PsiTreeUtil.findChildrenOfType(odinFile, OdinRefExpression.class);
        Objects.requireNonNull(refExpressions);
        OdinRefExpression odinRefExpression = refExpressions.stream().filter(e -> e.getText().contains("weapon")).findFirst().orElseThrow();

        OdinSymbolTable context = OdinSymbolTableHelper.buildFullSymbolTable(odinRefExpression, new OdinContext());
        OdinInsightUtils.getReferenceableSymbols(new OdinContext(), odinRefExpression);

    }

    public void testPolymorphicTypes() throws IOException {
        OdinFile odinFile = loadTypeInference();
        Collection<OdinProcedureDefinition> procedureDeclarationStatements = PsiTreeUtil.findChildrenOfType(odinFile.getFileScope(), OdinProcedureDefinition.class);

        {
            TsOdinType type = inferTypeOfFirstExpressionInProcedure(odinFile, "testTypeInference");

            assertNotNull(type);
            assertEquals("Point", type.getName());
        }
    }

    public void testPolymorphicTypesWithMultipleParams() throws IOException {
        OdinFile odinFile = loadTypeInference();

        var type = inferTypeOfFirstExpressionInProcedure(odinFile, "testTypeInference2");

        assertNotNull(type);
        assertEquals("Point", type.getName());
    }

    public void testPolymorphicTypesWithMultipleAndNestedParams() throws IOException {
        OdinFile odinFile = loadTypeInference();
        var type = inferTypeOfFirstExpressionInProcedure(odinFile, "testTypeInference3");

        assertNotNull(type);
        assertEquals("Point", type.getName());
    }

    public void testPolymorphicTypesWithPolymorphicReturnType() throws IOException {
        OdinFile odinFile = loadTypeInference();
        var type = inferTypeOfFirstExpressionInProcedure(odinFile, "testTypeInference4");

        assertNotNull(type);
        assertInstanceOf(type, TsOdinStructType.class);

        TsOdinStructType structType = (TsOdinStructType) type;
        assertNotEmpty(structType.getFields().values());
        assertTrue(structType.getFields().containsKey("items"));
        assertInstanceOf(structType.getFields().get("items"), TsOdinSliceType.class);
        TsOdinSliceType fieldType = (TsOdinSliceType) structType.getFields().get("items");
        assertInstanceOf(fieldType.getElementType(), TsOdinStructType.class);
        TsOdinStructType elementType = (TsOdinStructType) fieldType.getElementType();
        assertEquals("Point", elementType.getName());
    }

    public void testPolymorphicTypesWithPolymorphicReturn_typeReferenceOnStructField() throws IOException {
        OdinFile odinFile = loadTypeInference();
        var type = inferTypeOfFirstExpressionInProcedure(odinFile, "testTypeInference5");
        System.out.println(type);
    }

    public void testDeclaredIdentifiersInProcedureBlock() throws IOException {
        OdinFile odinFile = load("src/test/testData/scope_resolution.odin");
        @NotNull OdinProcedureDefinition procToSearchFrom = findFirstProcedure(odinFile, "proc_to_search_from");
        OdinCallExpression callExpression = PsiTreeUtil.findChildOfType(procToSearchFrom, OdinCallExpression.class);
        assertNotNull(callExpression);
        OdinRefExpression expression = (OdinRefExpression) callExpression.getExpression();

        PsiReference reference = Objects.requireNonNull(expression.getIdentifier()).getReference();
        assertNotNull(reference);
        PsiElement resolvedReference = reference.resolve();
        assertInstanceOf(resolvedReference, OdinDeclaredIdentifier.class);
        OdinDeclaredIdentifier declaredIdentifier = (OdinDeclaredIdentifier) resolvedReference;
        assertEquals("proc_to_find", declaredIdentifier.getIdentifierToken().getText());

    }


    public void testPolymorphicTypesWithMultipleReturnTypes() throws IOException {
        OdinFile odinFile = loadTypeInference();
        var pointVariable = findFirstVariableDeclarationStatement(odinFile, "testTypeInference6", "point");
        assertNotEmpty(Objects.requireNonNull(pointVariable.getRhsExpressions()).getExpressionList());
        OdinExpression odinExpression = pointVariable.getRhsExpressions().getExpressionList().getFirst();
        TsOdinType tsOdinType = odinExpression.getInferredType();
        assertInstanceOf(tsOdinType, TsOdinStructType.class);
        assertEquals("Point", tsOdinType.getName());
    }

    public void testUnionType() throws IOException {
        OdinFile odinFile = loadTypeInference();
        TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testTypeInference7", "shape");
        assertInstanceOf(tsOdinType, TsOdinUnionType.class);
        assertEquals("Shape", tsOdinType.getName());
    }

    public void testPolyUnionType() throws IOException {
        OdinFile odinFile = loadTypeInference();
        String procedureName = "testTypeInference8";
        String variableName = "first_shape";
        TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, procedureName, variableName);
        assertInstanceOf(tsOdinType, TsOdinStructType.class);
        assertEquals("Line", tsOdinType.getName());
    }

    public void testMaybeExpression() throws IOException {
        OdinFile odinFile = loadTypeInference();
        TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testTypeInference9", "k");
        assertInstanceOf(tsOdinType, TsOdinTuple.class);
        List<TsOdinType> types = ((TsOdinTuple) tsOdinType).getTypes();
        assertSize(2, types);
        assertEquals("Point", types.get(0).getName());
        assertEquals("bool", types.get(1).getName());
    }

    public void testOrElseExpression() throws IOException {
        OdinFile odinFile = loadTypeInference();
        TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testTypeInference10", "point");
        assertInstanceOf(tsOdinType, TsOdinStructType.class);
        assertEquals("Point", tsOdinType.getName());
    }

    public void testTypeAssertOneValue() throws IOException {
        OdinFile odinFile = loadTypeInference();
        TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testTypeInference11", "point");
        assertInstanceOf(tsOdinType, TsOdinStructType.class);
        assertEquals("Point", tsOdinType.getName());
    }

    public void testTypeAssertTwoValues() throws IOException {
        OdinFile odinFile = loadTypeInference();
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testTypeInference12", "y");
            assertEquals("bool", tsOdinType.getName());
        }
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testTypeInference12", "x");
            assertEquals("Point", tsOdinType.getName());
        }

    }

    public void testUnaryAndOperator() throws IOException {
        OdinFile odinFile = loadTypeInference();
        TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testTypeInference13", "point_ptr");
        TsOdinPointerType tsOdinPointerType = assertInstanceOf(tsOdinType, TsOdinPointerType.class);
        TsOdinStructType structType = assertInstanceOf(tsOdinPointerType.getDereferencedType(), TsOdinStructType.class);
        assertEquals("Point", structType.getName());

    }

    public void testSliceExpression() throws IOException {
        OdinFile odinFile = loadTypeInference();
        TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testTypeInference14", "point_slice");
        TsOdinSliceType tsOdinArrayType = assertInstanceOf(tsOdinType, TsOdinSliceType.class);
        TsOdinStructType structType = assertInstanceOf(tsOdinArrayType.getElementType(), TsOdinStructType.class);
        assertEquals("Point", structType.getName());

    }

    public void testTernaryConditionals() throws IOException {
        OdinFile odinFile = loadTypeInference();
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testTypeInference15", "point_1");
            TsOdinStructType structType = assertInstanceOf(tsOdinType, TsOdinStructType.class);
            assertEquals("Point", structType.getName());
        }
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testTypeInference15", "point_2");
            TsOdinStructType structType = assertInstanceOf(tsOdinType, TsOdinStructType.class);
            assertEquals("Point", structType.getName());
        }
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testTypeInference15", "point_3");
            TsOdinStructType structType = assertInstanceOf(tsOdinType, TsOdinStructType.class);
            assertEquals("Point", structType.getName());
        }


    }

    public void testLiteralExpressions() throws IOException {
        OdinFile odinFile = loadTypeInference();

        TsOdinType complexNumber1 = inferFirstRightHandExpressionOfVariable(odinFile, "testTypeInference16", "complex_number1");
        assertEquals("untyped complex", complexNumber1.getName());
        TsOdinType complexNumber2 = inferFirstRightHandExpressionOfVariable(odinFile, "testTypeInference16", "complex_number2");
        assertEquals("untyped complex", complexNumber2.getName());
        TsOdinType quaternion1 = inferFirstRightHandExpressionOfVariable(odinFile, "testTypeInference16", "quaternion1");
        assertEquals("untyped quaternion", quaternion1.getName());
        TsOdinType quaternion2 = inferFirstRightHandExpressionOfVariable(odinFile, "testTypeInference16", "quaternion2");
        assertEquals("untyped quaternion", quaternion2.getName());
        TsOdinType quaternion3 = inferFirstRightHandExpressionOfVariable(odinFile, "testTypeInference16", "quaternion3");
        assertEquals("untyped quaternion", quaternion3.getName());
        TsOdinType r = inferFirstRightHandExpressionOfVariable(odinFile, "testTypeInference16", "r");
        assertEquals("untyped rune", r.getName());
        TsOdinType s = inferFirstRightHandExpressionOfVariable(odinFile, "testTypeInference16", "s");
        assertEquals("untyped string", s.getName());
    }

    public void testBitsetsAndEnums() throws IOException {
        OdinFile odinFile = loadTypeInference();
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testBitsetsAndEnums", "d");
            TsOdinEnumType tsOdinEnumType = assertInstanceOf(tsOdinType, TsOdinEnumType.class);
            assertEquals("Direction", tsOdinEnumType.getName());
        }
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testBitsetsAndEnums", "c");
            assertEquals("enum Direction i32", tsOdinType.getLabel());
        }


        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testBitsetsAndEnums", "b");
            assertEquals("bit_set[enum Direction i32; u8]", tsOdinType.baseType().getLabel());
        }

    }


    public void testPrimitiveTypeCasting() throws IOException {
        OdinFile odinFile = loadTypeInference();
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testPrimitiveTypeCasting", "a");
            System.out.println(tsOdinType.getLabel());
            tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testPrimitiveTypeCasting", "bg");
            System.out.println(tsOdinType.getLabel());
            tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testPrimitiveTypeCasting", "rgb");
            System.out.println(tsOdinType.getLabel());
            tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testPrimitiveTypeCasting", "c");
            System.out.println(tsOdinType.getLabel());

        }
    }

    public void testTypeAliases() throws IOException {
        OdinFile odinFile = loadTypeInference();
        TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testTypeInference_withTypeAliases", "point");
        assertInstanceOf(tsOdinType, TsOdinStructType.class);
    }

    public void testTypeAliases_2() throws IOException {
        OdinFile odinFile = loadTypeInference();
        TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testTypeInference_withTypeAliases_2", "point");
        assertInstanceOf(tsOdinType, TsOdinStructType.class);
        assertEquals("Point", tsOdinType.getName());
    }

    public void testForInVars() throws IOException {
        OdinFile odinFile = loadTypeInference();
        {
            assertExpressionIsOfTypeWithName(odinFile,
                    "testForInVars",
                    "test1",
                    TsOdinStructType.class,
                    "Point");

            assertExpressionIsOfTypeWithName(odinFile,
                    "testForInVars",
                    "test2",
                    TsOdinStructType.class,
                    "Line");

            assertExpressionIsOfTypeWithName(odinFile,
                    "testForInVars",
                    "test3",
                    TsOdinBuiltInType.class,
                    "rune");

            assertExpressionIsOfTypeWithName(odinFile,
                    "testForInVars",
                    "test4",
                    TsOdinBuiltInType.class,
                    "int");

            assertExpressionIsOfTypeWithName(odinFile,
                    "testForInVars",
                    "test5",
                    TsOdinStructType.class,
                    "Point");

            assertExpressionIsOfTypeWithName(odinFile,
                    "testForInVars",
                    "test6",
                    TsOdinBuiltInType.class,
                    "int");

            assertExpressionIsOfTypeWithName(odinFile,
                    "testForInVars",
                    "test7",
                    TsOdinStructType.class,
                    "Point");

            assertExpressionIsOfTypeWithName(odinFile,
                    "testForInVars",
                    "test8",
                    TsOdinBuiltInType.class,
                    "int");

        }
    }

    public void testTypeSwitch() throws IOException {
        OdinFile odinFile = loadTypeInference();
        {

            assertExpressionIsOfTypeWithName(odinFile,
                    "testTypeSwitch",
                    "test3",
                    TsOdinBuiltInTypes.I32.getClass(),
                    "i32");

            assertExpressionIsOfTypeWithName(odinFile,
                    "testTypeSwitch",
                    "test1",
                    TsOdinStructType.class,
                    "Point");

            assertExpressionIsOfTypeWithName(odinFile,
                    "testTypeSwitch",
                    "test2",
                    TsOdinStructType.class,
                    "Line");

        }
    }

    public void testTypeInference_arrayBinaryOps() throws IOException {
        OdinFile odinFile = loadTypeInference();
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "binary_operators_on_arrays", "test");
            TsOdinArrayType arrayType = assertInstanceOf(tsOdinType, TsOdinArrayType.class);
            assertEquals(TsOdinBuiltInTypes.F32, arrayType.getElementType());
        }
    }

    public void testParapoly() throws IOException {
        OdinFile odinFile = load("src/test/testData/parapoly.odin");

        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testParapoly_typeIdConstrained", "x");
            TsOdinSliceType tsOdinSliceType = assertInstanceOf(tsOdinType, TsOdinSliceType.class);
            assertEquals("Point", tsOdinSliceType.getElementType().getName());
        }

        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testParapoly_slice", "x");
            assertEquals("i32", tsOdinType.getName());
        }

        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testParapoly_matrix", "x");
            assertEquals("i32", tsOdinType.getName());
        }

        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testParapoly_slice_constrained", "x");
            assertEquals("i32", tsOdinType.getName());
        }

    }

    public void testParapoly_constrained() throws IOException {
        OdinFile odinFile = load("src/test/testData/parapoly.odin");
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testParapoly_slice_constrained", "x");
            assertEquals("i32", tsOdinType.getName());
        }
    }

    public void testParapoly_proc() throws IOException {
        OdinFile odinFile = load("src/test/testData/parapoly.odin");
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testParapoly_proc", "x");
            assertEquals("i32", tsOdinType.getName());
        }
    }

    public void testParapoly_specializedStruct() throws IOException {
        OdinFile odinFile = load("src/test/testData/parapoly.odin");
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testParapoly_specializedStruct", "x");
            TsOdinTypeAlias typeAlias = assertInstanceOf(tsOdinType, TsOdinTypeAlias.class);
            TsOdinStructType structType = assertInstanceOf(typeAlias.getBaseType(), TsOdinStructType.class);
            assertTrue(structType.isSpecialized());
            assertFalse(structType.isGeneric());
            assertNotSame(structType.getGenericType(), TsOdinGenericType.NO_GENERIC_TYPE);
            assertTrue(structType.getGenericType().isGeneric());
            assertFalse(structType.getGenericType().isSpecialized());
        }
    }

    public void testParapoly_recursive() throws IOException {
        OdinFile odinFile = load("src/test/testData/parapoly.odin");
        {
            OdinExpression expression = findFirstExpressionOfVariable(odinFile, "testTypeInference_withRecursivePolyPara", "test");
            TsOdinType tsOdinType = expression.getInferredType();
            TsOdinPointerType pointerType = assertInstanceOf(tsOdinType, TsOdinPointerType.class);
            TsOdinStructType structType = assertInstanceOf(pointerType.getDereferencedType(), TsOdinStructType.class);
            assertEquals("Point", structType.getName());

        }

        {
            OdinExpression expression = findFirstExpressionOfVariable(odinFile, "testTypeInference_withRecursivePolyPara", "test2");
            TsOdinType tsOdinType = expression.getInferredType();
            TsOdinPointerType pointerType = assertInstanceOf(tsOdinType, TsOdinPointerType.class);
            TsOdinStructType structType = assertInstanceOf(pointerType.getDereferencedType(), TsOdinStructType.class);
            assertEquals("Line", structType.getName());

        }
    }

    public void testParapoly_withDoubleInstantiation() throws IOException {
        OdinFile odinFile = load("src/test/testData/parapoly.odin");
        {
            OdinExpression expression = findFirstExpressionOfVariable(odinFile, "testTypeInference_withDoubleInstantiation", "test");
            TsOdinType tsOdinType = expression.getInferredType();
            TsOdinStructType structType = assertInstanceOf(tsOdinType, TsOdinStructType.class);
            assertEquals("Point", structType.getName());


        }
    }

    public void testParapoly_typeAliasAsParam() throws IOException {
        OdinFile odinFile = load("src/test/testData/parapoly.odin");
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testParapoly_typeAliasAsParam", "point");
            TsOdinTypeAlias typeAlias = assertInstanceOf(tsOdinType, TsOdinTypeAlias.class);
            assertEquals("PointAlias", typeAlias.getName());
            TsOdinStructType tsOdinStructType = assertInstanceOf(typeAlias.getBaseType(), TsOdinStructType.class);
            assertEquals("Point", tsOdinStructType.getName());
        }

    }

    public void testParapoly_distinctAlias1() throws IOException {
        OdinFile odinFile = load("src/test/testData/parapoly.odin");

        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testParapoly_distinctAlias", "x");
            TsOdinStructType tsOdinStructType = assertInstanceOf(tsOdinType, TsOdinStructType.class);
            assertEquals("Point", tsOdinStructType.getName());
        }
    }

    public void testParapoly_distinctAlias2() throws IOException {
        OdinFile odinFile = load("src/test/testData/parapoly.odin");
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testParapoly_distinctAlias", "y");
            assertTrue(tsOdinType.isPolymorphic());
        }
    }


    // Visibility tests
    public void testVisibility() throws IOException {
        {
            OdinFile odinFile = load("src/test/testData/mypackage/visibility_annotations.odin");
            OdinProcedureDefinition proc = PsiTreeUtil.findChildOfType(odinFile, OdinProcedureDefinition.class);
            OdinFileScope odinFileScope = odinFile.getFileScope();
            List<OdinSymbol> symbols = new ArrayList<>(OdinSymbolTableHelper.buildFileScopeSymbolTable(odinFileScope, OdinInsightUtils.getGlobalFileVisibility(odinFileScope))
                    .getFilteredSymbols(e -> true));
            symbols.sort(Comparator.comparing(OdinSymbol::getName));
            assertEquals(4, symbols.size());
            assertEquals(OdinVisibility.PACKAGE_EXPORTED, symbols.get(0).getVisibility());
            assertEquals(OdinVisibility.PACKAGE_EXPORTED, symbols.get(1).getVisibility());
            assertEquals(OdinVisibility.PACKAGE_PRIVATE, symbols.get(2).getVisibility());
            assertEquals(OdinVisibility.FILE_PRIVATE, symbols.get(3).getVisibility());
        }

        {
            OdinFile odinFile = load("src/test/testData/mypackage/package_private.odin");
            OdinVisibility globalVisibility = OdinInsightUtils.getGlobalFileVisibility(odinFile.getFileScope());
            assertEquals(OdinVisibility.PACKAGE_PRIVATE, globalVisibility);
        }

        {
            OdinFile odinFile = load("src/test/testData/mypackage/file_private.odin");
            OdinVisibility globalVisibility = OdinInsightUtils.getGlobalFileVisibility(odinFile.getFileScope());
            assertEquals(OdinVisibility.FILE_PRIVATE, globalVisibility);
        }
    }


    // Scope tests

    public void testScoping() throws IOException {
        // Assignment
        OdinFile odinFile = load("src/test/testData/scoping/scoping.odin");
        // File scope
        {
            OdinExpression expression = findFirstExpressionOfVariable(odinFile, "file_scope", "test");
            OdinSymbolTable odinSymbolTable = OdinSymbolTableHelper.doBuildFullSymbolTable(expression);

            assertNotNull(odinSymbolTable.getSymbol("assignment"));
            assertNotNull(odinSymbolTable.getSymbol("partial_scope"));
            assertNotNull(odinSymbolTable.getSymbol("conditional_block"));
            assertNotNull(odinSymbolTable.getSymbol("shadowing"));
            assertNotNull(odinSymbolTable.getSymbol("file_scope"));
            assertNotNull(odinSymbolTable.getSymbol("fmt"));
            assertNotNull(odinSymbolTable.getSymbol("MyStruct"));
            assertNotNull(odinSymbolTable.getSymbol("g_point"));
        }
        {
            OdinInitVariableStatement var = findFirstVariableDeclarationStatement(odinFile, "assignment", "test");
            OdinExpression odinExpression = Objects.requireNonNull(var.getRhsExpressions()).getExpressionList().getFirst();

            com.lasagnerd.odin.codeInsight.OdinSymbolTable odinSymbolTable = OdinSymbolTableHelper.doBuildFullSymbolTable(odinExpression);
            assertNull(odinSymbolTable.getSymbol("test"));
            assertNotNull(odinSymbolTable.getSymbol("x"));
        }

        // Partial scope
        {
            OdinExpression expression = findFirstExpressionOfVariable(odinFile, "partial_scope", "test");
            OdinSymbolTable odinSymbolTable = OdinSymbolTableHelper.doBuildFullSymbolTable(expression);

            assertNotNull(odinSymbolTable.getSymbol("x"));
            assertNull(odinSymbolTable.getSymbol("y"));
        }

        // Conditional block
        {

            // if branch
            {
                OdinExpression expression = findFirstExpressionOfVariable(odinFile, "conditional_block", "test_if");
                OdinSymbolTable odinSymbolTable = OdinSymbolTableHelper.doBuildFullSymbolTable(expression);

                assertNotNull(odinSymbolTable.getSymbol("y"));
                assertNotNull(odinSymbolTable.getSymbol("x"));
            }

            // else-if branch
            {
                OdinExpression expression = findFirstExpressionOfVariable(odinFile, "conditional_block", "test_else_if");
                OdinSymbolTable odinSymbolTable = OdinSymbolTableHelper.doBuildFullSymbolTable(expression);

                assertNotNull(odinSymbolTable.getSymbol("z"));
                assertNotNull(odinSymbolTable.getSymbol("x"));
                assertNull(odinSymbolTable.getSymbol("y"));
            }

            // else branch
            {
                OdinExpression expression = findFirstExpressionOfVariable(odinFile, "conditional_block", "test_else");
                OdinSymbolTable odinSymbolTable = OdinSymbolTableHelper.doBuildFullSymbolTable(expression);

                assertNotNull(odinSymbolTable.getSymbol("z"));
                assertNotNull(odinSymbolTable.getSymbol("x"));
                assertNotNull(odinSymbolTable.getSymbol("w"));
                assertNull(odinSymbolTable.getSymbol("y"));
            }


            // Check visibility in conditional expressions
            {
                OdinProcedureDefinition proc = findFirstProcedure(odinFile, "conditional_block");
                // if
                {
                    OdinIfBlock odinIfBlock = PsiTreeUtil.findChildOfType(proc, OdinIfBlock.class);
                    assertNotNull(odinIfBlock);
                    assertNotNull(odinIfBlock.getCondition());
                    {
                        OdinSymbolTable odinSymbolTable = OdinSymbolTableHelper.doBuildFullSymbolTable(odinIfBlock.getCondition());
                        assertNotNull(odinSymbolTable.getSymbol("x"));
                    }
                    assertNotNull(odinIfBlock.getControlFlowInit());
                    {
                        OdinSymbolTable odinSymbolTable = OdinSymbolTableHelper.doBuildFullSymbolTable(odinIfBlock.getControlFlowInit());
                        assertNull(odinSymbolTable.getSymbol("x"));
                    }
                }


                OdinElseBlock odinElseBlock = PsiTreeUtil.findChildOfType(proc, OdinElseBlock.class);
                assertNotNull(odinElseBlock);
                assertNotNull(odinElseBlock.getIfBlock());
                assertNotNull(odinElseBlock.getIfBlock().getCondition());
                {
                    OdinSymbolTable odinSymbolTable = OdinSymbolTableHelper.doBuildFullSymbolTable(odinElseBlock.getIfBlock().getCondition());
                    assertNotNull(odinSymbolTable.getSymbol("x"));
                    assertNotNull(odinSymbolTable.getSymbol("z"));
                }

                assertNotNull(odinElseBlock.getIfBlock().getControlFlowInit());
                {
                    OdinSymbolTable odinSymbolTable = OdinSymbolTableHelper.doBuildFullSymbolTable(odinElseBlock.getIfBlock().getControlFlowInit());
                    assertNotNull(odinSymbolTable.getSymbol("x"));
                    assertNull(odinSymbolTable.getSymbol("z"));
                }
            }

            // Check visibility in do <something>
            {
                OdinProcedureDefinition procedureDeclarationStatement = findFirstProcedure(odinFile, "conditional_block");
                OdinDoStatement doStatement = PsiTreeUtil.findChildOfType(procedureDeclarationStatement, OdinDoStatement.class);
                assertNotNull(doStatement);

                OdinSymbolTable odinSymbolTable = OdinSymbolTableHelper.doBuildFullSymbolTable(doStatement);
                assertNotNull(odinSymbolTable.getSymbol("x"));
            }
        }

        // Shadowing
        {
            OdinProcedureDefinition proc = findFirstProcedure(odinFile, "shadowing");
            OdinBlock block = proc.getProcedureBody().getBlock();
            assertNotNull(block);

            OdinBlock shadowingBlock = PsiTreeUtil.findChildOfType(block.getStatementList(), OdinBlock.class);
            assertNotNull(shadowingBlock);

            // Check that expression of x that shadows outer x, only sees outer x
            {
                OdinInitVariableStatement shadowingX = PsiTreeUtil.findChildOfType(shadowingBlock, OdinInitVariableStatement.class);
                assertNotNull(shadowingX);
                OdinExpression odinExpression = Objects.requireNonNull(shadowingX.getRhsExpressions()).getExpressionList().getFirst();
                OdinSymbolTable odinSymbolTable = OdinSymbolTableHelper.doBuildFullSymbolTable(odinExpression);
                OdinSymbol symbol = odinSymbolTable.getSymbol("x");
                assertNotNull(symbol);
                PsiNamedElement declaredIdentifier = symbol.getDeclaredIdentifier();
                OdinInitVariableStatement initVariableStatement = PsiTreeUtil.getParentOfType(declaredIdentifier, false, OdinInitVariableStatement.class);
                assertNotNull(initVariableStatement);
                assertEquals("x := 1", initVariableStatement.getText());
            }

            // Check that expression of y, only sees inner x
            {
                OdinInitVariableStatement y = findFirstVariable(shadowingBlock, "y");
                OdinExpression odinExpression = Objects.requireNonNull(y.getRhsExpressions()).getExpressionList().getFirst();
                OdinSymbolTable odinSymbolTable = OdinSymbolTableHelper.doBuildFullSymbolTable(odinExpression);
                OdinSymbol symbol = odinSymbolTable.getSymbol("x");
                assertNotNull(symbol);
                PsiNamedElement declaredIdentifier = symbol.getDeclaredIdentifier();
                OdinInitVariableStatement initVariableStatement = PsiTreeUtil.getParentOfType(declaredIdentifier, false, OdinInitVariableStatement.class);
                assertNotNull(initVariableStatement);
                assertEquals("x := x", initVariableStatement.getText());
            }
        }


    }

    public void testScoping_params() throws IOException {
        // Procedure parameters
        OdinFile odinFile = load("src/test/testData/scoping/scoping.odin");
        {
            OdinExpression expression = findFirstExpressionOfVariable(odinFile, "params", "test");
            OdinSymbolTable visibleSymbols = OdinSymbolTableHelper.doBuildFullSymbolTable(expression);
            assertNotNull(visibleSymbols.getSymbol("x"));
            assertNotNull(visibleSymbols.getSymbol("y"));
            assertNotNull(visibleSymbols.getSymbol("z"));
            assertNotNull(visibleSymbols.getSymbol("u"));
            assertNotNull(visibleSymbols.getSymbol("v"));
            assertNotNull(visibleSymbols.getSymbol("w"));
            assertNotNull(visibleSymbols.getSymbol("my_struct"));
            assertNotNull(visibleSymbols.getSymbol("k"));

            // Return parameters
            assertNotNull(visibleSymbols.getSymbol("r1"));
            assertNotNull(visibleSymbols.getSymbol("r2"));
            assertNotNull(visibleSymbols.getSymbol("r3"));
            assertNotNull(visibleSymbols.getSymbol("r4"));

            // Procedure itself
            assertNotNull(visibleSymbols.getSymbol("params"));
        }

        {
            OdinExpression expression = findFirstExpressionOfVariable(odinFile, "poly_params", "test");
            OdinSymbolTable visibleSymbols = OdinSymbolTableHelper.doBuildFullSymbolTable(expression);
            assertNotNull(visibleSymbols.getSymbol("T"));
            assertNotNull(visibleSymbols.getSymbol("Key"));
            assertNotNull(visibleSymbols.getSymbol("Val"));
        }

        {
            OdinProcedureDefinition procedure = findFirstProcedure(odinFile, "poly_params");
            List<OdinParamEntry> parameters = procedure.getProcedureSignature().getProcedureType().getParamEntryList();

            // proc($T: typeid, t: Table($Key, $Val/Key), k: Key, v: Val)
            {
                OdinParamEntry paramEntry = parameters.get(1); // param "t"
                {
                    OdinSymbolTable visibleSymbols = OdinSymbolTableHelper.doBuildFullSymbolTable(paramEntry);
                    assertNotNull(visibleSymbols.getSymbol("T"));
                    assertNull(visibleSymbols.getSymbol("Key"));
                    assertNull(visibleSymbols.getSymbol("Val"));
                }
                // Constrained type $Val/Key
                {
                    OdinConstrainedType constrainedType = PsiTreeUtil.findChildOfType(paramEntry, OdinConstrainedType.class);
                    OdinSymbolTable visibleSymbols = OdinSymbolTableHelper.doBuildFullSymbolTable(Objects.requireNonNull(constrainedType));
                    assertNotNull(visibleSymbols.getSymbol("T"));
                    assertNotNull(visibleSymbols.getSymbol("Key"));
                    assertNull(visibleSymbols.getSymbol("Value"));
                }
            }

            // proc($T: typeid, t: Table($Key, $Val/Key), k: Key, v: Val)
            {
                OdinParamEntry paramEntry = parameters.getFirst(); // param "$T"
                OdinSymbolTable visibleSymbols = OdinSymbolTableHelper.doBuildFullSymbolTable(paramEntry);
                assertNull(visibleSymbols.getSymbol("T"));
                assertNull(visibleSymbols.getSymbol("Key"));
                assertNull(visibleSymbols.getSymbol("Val"));
            }


            // proc($T: typeid, t: Table($Key, $Val/Key), k: Key, v: Val)
            {
                OdinParamEntry paramEntry = parameters.get(2); // param "k"
                OdinSymbolTable visibleSymbols = OdinSymbolTableHelper.doBuildFullSymbolTable(paramEntry);
                assertNotNull(visibleSymbols.getSymbol("T"));
                assertNotNull(visibleSymbols.getSymbol("Key"));
                assertNotNull(visibleSymbols.getSymbol("Val"));
            }

            // Return params -> (r1: T, r2: Val, r3: Key)
            OdinReturnParameters returnParameters = procedure.getProcedureSignature().getProcedureType().getReturnParameters();
            assertNotNull(returnParameters);
            assertNotNull(returnParameters.getParamEntries());
            List<OdinParamEntry> returnParams = returnParameters.getParamEntries().getParamEntryList();
            {
                for (OdinParamEntry returnParam : returnParams) {
                    OdinSymbolTable visibleSymbols = OdinSymbolTableHelper.doBuildFullSymbolTable(returnParam);
                    assertNotNull(visibleSymbols.getSymbol("T"));
                    assertNotNull(visibleSymbols.getSymbol("Key"));
                    assertNotNull(visibleSymbols.getSymbol("Val"));
                }
            }
        }
    }

    public void testScoping_constants() throws IOException {
        // Constants
        OdinFile odinFile = load("src/test/testData/scoping/scoping.odin");
        {
            OdinExpression expression = findFirstExpressionOfVariable(odinFile, "constants", "test_outer");
            OdinSymbolTable odinSymbolTable = OdinSymbolTableHelper.doBuildFullSymbolTable(expression);
            assertNotNull(odinSymbolTable.getSymbol("K"));
            assertNotNull(odinSymbolTable.getSymbol("p"));
            assertNotNull(odinSymbolTable.getSymbol("S"));
        }

        {
            OdinExpression expression = findFirstExpressionOfVariable(odinFile, "constants", "test_inner");
            OdinSymbolTable odinSymbolTable = OdinSymbolTableHelper.doBuildFullSymbolTable(expression);
            assertNotNull(odinSymbolTable.getSymbol("Kinner"));
            assertNotNull(odinSymbolTable.getSymbol("K"));
            assertNotNull(odinSymbolTable.getSymbol("p"));
            assertNotNull(odinSymbolTable.getSymbol("S"));
        }
    }

    public void testScoping_for() throws IOException {
        OdinFile odinFile = load("src/test/testData/scoping/scoping.odin");
        {
            OdinExpression expression = findFirstExpressionOfVariable(odinFile, "for_block", "test");
            OdinSymbolTable odinSymbolTable = OdinSymbolTableHelper.doBuildFullSymbolTable(expression);
            assertNotNull(odinSymbolTable.getSymbol("i"));
            assertNotNull(odinSymbolTable.getSymbol("j"));
        }

        {
            OdinExpression expression = findFirstExpressionOfVariable(odinFile, "for_in_block", "test");
            OdinSymbolTable odinSymbolTable = OdinSymbolTableHelper.doBuildFullSymbolTable(expression);
            assertNotNull(odinSymbolTable.getSymbol("index"));
            assertNotNull(odinSymbolTable.getSymbol("val"));
        }
    }

    public void testScoping_switch() throws IOException {
        OdinFile odinFile = load("src/test/testData/scoping/scoping.odin");
        {
            OdinExpression expression = findFirstExpressionOfVariable(odinFile, "switch_block", "test_case_1");
            OdinSymbolTable odinSymbolTable = OdinSymbolTableHelper.doBuildFullSymbolTable(expression);
            assertNotNull(odinSymbolTable.getSymbol("x"));
            assertNotNull(odinSymbolTable.getSymbol("y"));
            assertNotNull(odinSymbolTable.getSymbol("s"));
        }

        {
            {
                OdinExpression expression = findFirstExpressionOfVariable(odinFile, "switch_block", "test_case_2");
                OdinSymbolTable odinSymbolTable = OdinSymbolTableHelper.doBuildFullSymbolTable(expression);
                assertNull(odinSymbolTable.getSymbol("x"));
                assertNotNull(odinSymbolTable.getSymbol("y"));
                assertNotNull(odinSymbolTable.getSymbol("s"));
            }

            {
                OdinProcedureDefinition procedure = findFirstProcedure(odinFile, "switch_block");
                OdinSwitchBlock odinSwitchBlock = PsiTreeUtil.findChildOfType(procedure, OdinSwitchBlock.class);
                assertNotNull(odinSwitchBlock);
                {
                    OdinExpression expression = odinSwitchBlock.getExpression();
                    OdinSymbolTable odinSymbolTable = OdinSymbolTableHelper.doBuildFullSymbolTable(Objects.requireNonNull(expression));
                    assertNull(odinSymbolTable.getSymbol("x"));
                    assertNotNull(odinSymbolTable.getSymbol("u"));
                    assertNotNull(odinSymbolTable.getSymbol("s"));
                    assertNull(odinSymbolTable.getSymbol("test_case_1"));
                    assertNull(odinSymbolTable.getSymbol("test_case_2"));
                }

                OdinSwitchCase odinSwitchCase = Objects.requireNonNull(Objects.requireNonNull(odinSwitchBlock.getSwitchBody()).getSwitchCases()).getSwitchCaseList().getFirst();
                {
                    OdinExpression expression = Objects.requireNonNull(odinSwitchCase.getCaseClause())
                            .getExpressionList()
                            .getFirst();
                    OdinSymbolTable odinSymbolTable = OdinSymbolTableHelper.doBuildFullSymbolTable(expression);
                    assertNull(odinSymbolTable.getSymbol("x"));
                    assertNotNull(odinSymbolTable.getSymbol("u"));
                    assertNotNull(odinSymbolTable.getSymbol("s"));
                    assertNull(odinSymbolTable.getSymbol("test_case_1"));
                    assertNull(odinSymbolTable.getSymbol("test_case_2"));
                }
            }
        }

        {
            OdinExpression expression = findFirstExpressionOfVariable(odinFile, "switch_in_block", "test_f32");
            OdinSymbolTable odinSymbolTable = OdinSymbolTableHelper.doBuildFullSymbolTable(expression);
            assertNotNull(odinSymbolTable.getSymbol("x"));
            assertNotNull(odinSymbolTable.getSymbol("t"));
            assertNull(odinSymbolTable.getSymbol("test_i32"));
        }

        {
            OdinExpression expression = findFirstExpressionOfVariable(odinFile, "switch_in_block", "test_i32");
            OdinSymbolTable odinSymbolTable = OdinSymbolTableHelper.doBuildFullSymbolTable(expression);
            assertNotNull(odinSymbolTable.getSymbol("x"));
            assertNotNull(odinSymbolTable.getSymbol("t"));
            assertNull(odinSymbolTable.getSymbol("test_f32"));
        }
    }

    public void testScoping_struct_union() throws IOException {
        OdinFile odinFile = load("src/test/testData/scoping/scoping.odin");
        {
            OdinProcedureDefinition proc = findFirstProcedure(odinFile, "structs_unions");
            OdinStructType struct = PsiTreeUtil.findChildOfType(proc, OdinStructType.class);
            assertNotNull(struct);
            OdinStructBody structBody = Objects.requireNonNull(struct.getStructBlock()).getStructBody();
            OdinSymbolTable odinSymbolTable = OdinSymbolTableHelper.doBuildFullSymbolTable(Objects.requireNonNull(structBody));
            assertNotNull(odinSymbolTable.getSymbol("Key"));
            assertNotNull(odinSymbolTable.getSymbol("Value"));
        }

        OdinProcedureDefinition proc = findFirstProcedure(odinFile, "structs_unions");
        OdinUnionType union = PsiTreeUtil.findChildOfType(proc, OdinUnionType.class);
        assertNotNull(union);
        OdinUnionBody structBody = Objects.requireNonNull(union.getUnionBlock()).getUnionBody();
        OdinSymbolTable odinSymbolTable = OdinSymbolTableHelper.doBuildFullSymbolTable(Objects.requireNonNull(structBody));
        assertNotNull(odinSymbolTable.getSymbol("T1"));
        assertNotNull(odinSymbolTable.getSymbol("T2"));
    }

    public void testScoping_recursiveLocalDefs() throws IOException {
        OdinFile odinFile = load("src/test/testData/scoping/scoping.odin");
        {
            OdinProcedureDefinition proc = findFirstProcedure(odinFile, "recursive_local_defs");
            OdinProcedureDefinition localProc = PsiTreeUtil.findChildOfType(proc, OdinProcedureDefinition.class);
            {
                OdinInitVariableStatement testVar = findFirstVariable(localProc, "test");
                OdinExpression odinExpression = Objects.requireNonNull(testVar.getRhsExpressions()).getExpressionList().getFirst();
                OdinSymbolTable odinSymbolTable = OdinSymbolTableHelper.doBuildFullSymbolTable(odinExpression);
                assertNotNull(odinSymbolTable.getSymbol("p"));
            }
            {
                OdinStructType structVar = PsiTreeUtil.findChildOfType(proc, OdinStructType.class);
                assertNotNull(structVar);
                OdinStructBody structBody = Objects.requireNonNull(structVar.getStructBlock()).getStructBody();
                assertNotNull(structBody);
                List<OdinFieldDeclaration> fieldDeclarationStatementList = structBody.getFieldDeclarationList();
                OdinFieldDeclaration fieldDeclaration = fieldDeclarationStatementList.getFirst();
                OdinType type = fieldDeclaration.getType();
                OdinSymbolTable odinSymbolTable = OdinSymbolTableHelper.doBuildFullSymbolTable(Objects.requireNonNull(type));

                assertNotNull(odinSymbolTable.getSymbol("s"));
            }
        }
    }

    public void testScoping_usingStatement() throws IOException {
        OdinFile odinFile = load("src/test/testData/scoping/scoping.odin");
        {
            OdinExpression expression = findFirstExpressionOfVariable(odinFile, "using_statement", "test");
            OdinSymbolTable odinSymbolTable = OdinSymbolTableHelper.doBuildFullSymbolTable(expression);
            assertNotNull(odinSymbolTable.getSymbol("x"));
            assertNotNull(odinSymbolTable.getSymbol("y"));
        }
        {
            OdinExpression expression = findFirstExpressionOfVariable(odinFile, "using_statement", "test_line");
            OdinSymbolTable odinSymbolTable = OdinSymbolTableHelper.doBuildFullSymbolTable(expression);
            assertNull(odinSymbolTable.getSymbol("x"));
            assertNull(odinSymbolTable.getSymbol("y"));
            assertNotNull(odinSymbolTable.getSymbol("p1"));
            assertNotNull(odinSymbolTable.getSymbol("p2"));
        }

        {
            OdinExpression expression = findFirstExpressionOfVariable(odinFile, "using_statement", "test_triangle");
            OdinSymbolTable odinSymbolTable = OdinSymbolTableHelper.doBuildFullSymbolTable(expression);
            assertNull(odinSymbolTable.getSymbol("x"));
            assertNull(odinSymbolTable.getSymbol("y"));
            assertNotNull(odinSymbolTable.getSymbol("p1"));
            assertNotNull(odinSymbolTable.getSymbol("p2"));
            assertNotNull(odinSymbolTable.getSymbol("p3"));
        }
        {
            OdinExpression expression = findFirstExpressionOfVariable(odinFile, "using_statement", "test_proc");
            OdinSymbolTable odinSymbolTable = OdinSymbolTableHelper.doBuildFullSymbolTable(expression);
            assertNotNull(odinSymbolTable.getSymbol("x"));
            assertNotNull(odinSymbolTable.getSymbol("y"));
        }
        {
            OdinExpression expression = findFirstExpressionOfVariable(odinFile, "using_statement", "test_enum");
            OdinSymbolTable odinSymbolTable = OdinSymbolTableHelper.doBuildFullSymbolTable(expression);
            assertNotNull(odinSymbolTable.getSymbol("R"));
            assertNotNull(odinSymbolTable.getSymbol("G"));
            assertNotNull(odinSymbolTable.getSymbol("B"));
        }
    }

    public void testScoping_nestedProcs() throws IOException {
        OdinFile odinFile = load("src/test/testData/scoping/scoping.odin");
        {
            OdinProcedureDefinition proc = findFirstProcedure(odinFile, "nested_procs");
            OdinProcedureDefinition nestedProc = PsiTreeUtil.findChildOfType(proc, OdinProcedureDefinition.class);
            {
                OdinInitVariableStatement testVar = findFirstVariable(nestedProc, "test");
                OdinExpression odinExpression = Objects.requireNonNull(testVar.getRhsExpressions()).getExpressionList().getFirst();
                OdinSymbolTable odinSymbolTable = OdinSymbolTableHelper.doBuildFullSymbolTable(odinExpression);
                assertNotNull(odinSymbolTable.getSymbol("S"));
                assertNotNull(odinSymbolTable.getSymbol("nested_procs"));
                assertNotNull(odinSymbolTable.getSymbol("r"));
                assertNotNull(odinSymbolTable.getSymbol("p"));
                assertNull(odinSymbolTable.getSymbol("invisible"));
            }
        }
    }

    public void testScoping_labels() throws IOException {
        OdinFile odinFile = load("src/test/testData/scoping/scoping.odin");
        {
            OdinExpression expression = findFirstExpressionOfVariable(odinFile, "labels", "test_1");
            OdinSymbolTable odinSymbolTable = OdinSymbolTableHelper.doBuildFullSymbolTable(expression);
            assertNotNull(odinSymbolTable.getSymbol("label1"));
        }

        {
            OdinExpression expression = findFirstExpressionOfVariable(odinFile, "labels", "test_2");
            OdinSymbolTable odinSymbolTable = OdinSymbolTableHelper.doBuildFullSymbolTable(expression);
            assertNotNull(odinSymbolTable.getSymbol("label1"));
            assertNotNull(odinSymbolTable.getSymbol("label2"));
        }

        {
            OdinExpression expression = findFirstExpressionOfVariable(odinFile, "labels", "test_3");
            OdinSymbolTable odinSymbolTable = OdinSymbolTableHelper.doBuildFullSymbolTable(expression);
            assertNotNull(odinSymbolTable.getSymbol("label1"));
            assertNotNull(odinSymbolTable.getSymbol("label2"));
            assertNotNull(odinSymbolTable.getSymbol("label3"));
        }

        {
            OdinExpression expression = findFirstExpressionOfVariable(odinFile, "labels", "test_4");
            OdinSymbolTable odinSymbolTable = OdinSymbolTableHelper.doBuildFullSymbolTable(expression);
            assertNotNull(odinSymbolTable.getSymbol("label1"));
            assertNotNull(odinSymbolTable.getSymbol("label2"));
            assertNotNull(odinSymbolTable.getSymbol("label3"));
            assertNotNull(odinSymbolTable.getSymbol("label4"));
        }
    }

    public void testScoping_usingPackage() throws IOException {
        OdinFile odinFile = load("src/test/testData/scoping/scoping.odin");
        String packagePath = OdinImportService.getInstance(project).getPackagePath(odinFile);
        {
            OdinExpression expression = findFirstExpressionOfVariable(odinFile, "using_import", "test");
            OdinSymbolTable odinSymbolTable = OdinSymbolTableHelper.doBuildFullSymbolTable(packagePath,
                    expression,
                    com.lasagnerd.odin.codeInsight.symbols.symbolTable.OdinSymbolTableBuilderBase.ALWAYS_FALSE,
                    new OdinContext());
            assertNotNull(odinSymbolTable.getSymbol("a_mypublic_proc"));
            assertNotNull(odinSymbolTable.getSymbol("a_ret"));
        }
    }

    public void testScoping_return_type_defined_proc() throws IOException {
        OdinFile odinFile = load("src/test/testData/scoping/scoping.odin");
        String packagePath = OdinImportService.getInstance(project).getPackagePath(odinFile);
        {
            OdinExpression expression = findFirstExpressionOfVariable(odinFile, "return_type_defined_proc", "test1");
            TsOdinType tsOdinType = expression.getInferredType();
            TsOdinEnumType tsOdinEnumType = assertInstanceOf(tsOdinType, TsOdinEnumType.class);
            assertEquals("Error", tsOdinEnumType.getName());
        }

        {
            OdinExpression expression = findFirstExpressionOfVariable(odinFile, "return_type_defined_proc", "test2");
            TsOdinType tsOdinType = expression.getInferredType();
            TsOdinEnumType tsOdinEnumType = assertInstanceOf(tsOdinType, TsOdinEnumType.class);
            assertEquals("Error", tsOdinEnumType.getName());
        }
    }

    public void testImportPackage() throws IOException {
        {
            OdinFile odinFile = load("src/test/testData/mypackage/packages.odin");

            OdinExpression expression = findFirstExpressionOfVariable(odinFile, "main", "test");
            OdinSymbolTable odinSymbolTable = OdinSymbolTableHelper.buildFullSymbolTable(expression, new OdinContext());
            assertNotNull(odinSymbolTable.getSymbol("a_mypublic_proc"));
            assertNotNull(odinSymbolTable.getSymbol("b_mypackage_private_proc"));
            {
                OdinSymbol symbol = odinSymbolTable.getSymbol("c_myfile_private_proc");
                assertNotNull(symbol);
                assertSame(OdinVisibility.FILE_PRIVATE, symbol.getVisibility());
            }

            assertNotNull(odinSymbolTable.getSymbol("my_private_proc"));
            assertNotNull(odinSymbolTable.getSymbol("my_private_struct"));

            {
                OdinSymbol symbol = odinSymbolTable.getSymbol("my_file_private_global");
                assertNotNull(symbol);
                assertSame(OdinVisibility.FILE_PRIVATE, symbol.getVisibility());
            }
        }

        {
            OdinFile odinFile = load("src/test/testData/otherpackage/other_package.odin");
            OdinExpression expression = findFirstExpressionOfVariable(odinFile, "main", "test");
            TsOdinType tsOdinType = expression.getInferredType();
            TsOdinStructType structType = assertInstanceOf(tsOdinType, TsOdinStructType.class);
            assertEquals("a_ret", structType.getName());
        }
    }

    public void testPackageImport_twoHops() throws IOException {
        OdinFile odinFile = load("src/test/testData/mypackage/two_hops.odin");
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "main", "test");
            assertEquals(TsOdinBuiltInTypes.I32, tsOdinType);
        }

        {
            TsOdinType tsOdinType1 = inferFirstRightHandExpressionOfVariable(odinFile, "main", "second");
            TsOdinType tsOdinType2 = inferFirstRightHandExpressionOfVariable(odinFile, "main", "second");
            assertTrue(OdinTypeChecker.checkTypesStrictly(tsOdinType1.baseType(true), tsOdinType2.baseType(true)));
        }
    }

    public void testReferenceWalk() {
        @org.intellij.lang.annotations.Language("Odin")
        String fileContent = """
                package main;
                
                main :: proc() {
                   a.b.c.d().e.f()
                   a.?.b.c.d().e.f()
                   a().b.?.c[0].d()^.e.f()
                   x := x.b.c.d().e.f() + y.?.b.c.d().e.f() + z().b.?.c[0].d()^.e.f()
                }
                """;


        OdinFile odinFile = (OdinFile) createPsiFile("refs", "refs", fileContent);
        OdinProcedureDefinition procedure = findFirstProcedure(odinFile, "main");
        OdinBlock block = procedure.getProcedureBody().getBlock();
        List<OdinStatement> statements = Objects.requireNonNull(block).getStatements();
        {
            OdinStatement odinStatement = statements.getFirst();
            assertTopMostRefExpressionTextEquals(odinStatement, "a.b.c.d().e.f", "a");
        }
        {
            OdinStatement odinStatement = statements.get(1);
            assertTopMostRefExpressionTextEquals(odinStatement, "a.?.b.c.d().e.f", "a");
        }
        {
            OdinStatement odinStatement = statements.get(2);
            assertTopMostRefExpressionTextEquals(odinStatement, "a().b.?.c[0].d()^.e.f", "a");
        }
        {
            OdinStatement odinStatement = statements.get(3);
            OdinInitVariableStatement var = assertInstanceOf(odinStatement, OdinInitVariableStatement.class);
            OdinExpression odinExpression = Objects.requireNonNull(var.getRhsExpressions()).getExpressionList().getFirst();
            assertTopMostRefExpressionTextEquals(odinExpression, "x.b.c.d().e.f", "x");
            assertTopMostRefExpressionTextEquals(odinExpression, "y.?.b.c.d().e.f", "y");
            assertTopMostRefExpressionTextEquals(odinExpression, "z().b.?.c[0].d()^.e.f", "z");
        }
        {
            OdinStatement odinStatement = statements.getFirst();
            OdinRefExpression topMostRefExpression = getTopMostRefExpression(procedure, "a");

            Collection<OdinIdentifier> odinIdentifiers = PsiTreeUtil.findChildrenOfType(odinStatement, OdinIdentifier.class);
            assertNotEmpty(odinIdentifiers);
            OdinIdentifier identifier = odinIdentifiers.stream().filter(s -> s.getIdentifierToken().getText().equals("a")).findFirst().orElseThrow();
            assertNotNull(identifier);

            List<OdinRefExpression> odinRefExpressions = PsiTreeUtil.collectParents(identifier, OdinRefExpression.class, true, p -> p == topMostRefExpression);
            for (OdinRefExpression odinRefExpression : odinRefExpressions) {
                String text = Objects.requireNonNull(odinRefExpression.getIdentifier()).getText();

                System.out.println(text);
            }
        }
    }

    public void testScoping_compoundLiterals() throws IOException {
        OdinFile odinFile = load("src/test/testData/scoping/scoping.odin");
        {
            OdinProcedureDefinition procedure = findFirstProcedure(odinFile, "literal_blocks");
            OdinAssignmentStatement assignmentStatement = PsiTreeUtil.findChildOfType(procedure, OdinAssignmentStatement.class);
            OdinExpression odinExpression = Objects.requireNonNull(Objects.requireNonNull(assignmentStatement)
                    .getRhsExpressions()).getExpressionList().getFirst();
            TsOdinType tsOdinType = OdinExpectedTypeEngine.inferExpectedType(OdinSymbolTableHelper.buildFullSymbolTable(odinExpression, new OdinContext()).asContext(), odinExpression);
            TsOdinStructType structType = assertInstanceOf(tsOdinType, TsOdinStructType.class);
            assertEquals("MyStruct", structType.getName());
        }
    }

    public void testScoping_usingFields() throws IOException {
        OdinFile odinFile = load("src/test/testData/scoping/scoping.odin");
        {
            OdinExpression expression = findFirstExpressionOfVariable(odinFile, "using_fields", "test");
            TsOdinType tsOdinType = expression.getInferredType();
            TsOdinStructType structType = assertInstanceOf(tsOdinType, TsOdinStructType.class);
            assertEquals("Point", structType.getName());
        }
    }

    public void testScoping_overrideParentSymbols() throws IOException {
        OdinFile odinFile = load("src/test/testData/scoping/scoping.odin");
        {
            OdinExpression expression = findFirstExpressionOfVariable(odinFile, "override_parent_symbols", "test");
            TsOdinType tsOdinType = expression.getInferredType();
            TsOdinStructType structType = assertInstanceOf(tsOdinType, TsOdinStructType.class);
            assertEquals("Point", structType.getName());
        }
    }

    public void testTypeConversion() {
        {
            TsOdinType tsOdinType = OdinTypeConverter.inferTypeOfSymmetricalBinaryExpression(TsOdinBuiltInTypes.UNTYPED_INT, TsOdinBuiltInTypes.I32, null);
            assertEquals(TsOdinBuiltInTypes.I32, tsOdinType);
        }

        {
            TsOdinType tsOdinType = OdinTypeConverter.inferTypeOfSymmetricalBinaryExpression(TsOdinBuiltInTypes.UNTYPED_INT, TsOdinBuiltInTypes.F32, null);
            assertEquals(TsOdinBuiltInTypes.F32, tsOdinType);
        }

        {
            TsOdinType tsOdinType = OdinTypeConverter.inferTypeOfSymmetricalBinaryExpression(TsOdinBuiltInTypes.UNTYPED_STRING, TsOdinBuiltInTypes.STRING, null);
            assertEquals(TsOdinBuiltInTypes.STRING, tsOdinType);
        }

        {
            TsOdinArrayType arrayType = new TsOdinArrayType();
            arrayType.setElementType(TsOdinBuiltInTypes.I32);
            TsOdinType tsOdinType = OdinTypeConverter.inferTypeOfSymmetricalBinaryExpression(arrayType, TsOdinBuiltInTypes.UNTYPED_INT, OdinTypes.PLUS);
            assertEquals(tsOdinType, arrayType);
        }
    }

    public void testCircularReference() throws IOException {
        OdinFile odinFile = load("src/test/testData/r/circular_ref.odin");
        TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "circular_ref", "r");
        TsOdinTypeAlias tsOdinTypeAlias = assertInstanceOf(tsOdinType, TsOdinTypeAlias.class);
        TsOdinNumericType tsOdinNumericType = assertInstanceOf(tsOdinTypeAlias.getBaseType(), TsOdinNumericType.class);
        assertEquals("f32", tsOdinNumericType.getName());
    }

    public void testAliases() throws IOException {
        OdinFile odinFile = load("src/test/testData/typeAliases/type_aliases.odin");
        TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "type_aliases", "x");
        System.out.println(tsOdinType.getClass().getSimpleName());
        System.out.println(tsOdinType.getLabel());
    }

    public void test_typeInference_procedureGroup() throws IOException {
        OdinFile odinFile = loadTypeInference();
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "typeInference_procedureGroup", "x");
            assertInstanceOf(tsOdinType, TsOdinNumericType.class);
            assertEquals(TsOdinBuiltInTypes.I32, tsOdinType);
        }
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "typeInference_procedureGroup", "s");
            assertInstanceOf(tsOdinType, TsOdinNumericType.class);
            assertEquals(TsOdinBuiltInTypes.F64, tsOdinType);
        }
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "typeInference_procedureGroup", "t");
            assertInstanceOf(tsOdinType, TsOdinNumericType.class);
            assertEquals(TsOdinBuiltInTypes.I32, tsOdinType);
        }
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "typeInference_procedureGroup", "u");
            assertInstanceOf(tsOdinType, TsOdinStructType.class);
            assertEquals("Point", tsOdinType.getName());
        }
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "typeInference_procedureGroup", "v");
            assertInstanceOf(tsOdinType, TsOdinNumericType.class);
            assertEquals(TsOdinBuiltInTypes.I32, tsOdinType);
        }
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "typeInference_procedureGroup", "z");
            assertInstanceOf(tsOdinType, TsOdinStructType.class);
            assertEquals("Point", tsOdinType.getName());
        }
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "typeInference_procedureGroup", "w");
            assertInstanceOf(tsOdinType, TsOdinTypeAlias.class);
            assertEquals("PointDistinctAlias", tsOdinType.getName());
        }


        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "typeInference_procedureGroup", "y");
            assertInstanceOf(tsOdinType, TsOdinStringType.class);
            assertEquals(TsOdinBuiltInTypes.STRING, tsOdinType);
        }
    }

    public void test_typeInference_polyProcedureGroup() throws IOException {
        OdinFile odinFile = loadTypeInference();
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "typeInference_polyProcedureGroup", "x");
            assertEquals(TsOdinBuiltInTypes.I32, tsOdinType);
        }
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "typeInference_polyProcedureGroup", "y");
            assertEquals(TsOdinBuiltInTypes.I64, tsOdinType);
        }
    }

    public void test_astNew_procedureGroup() throws IOException {
        OdinFile odinFile = loadTypeInference();
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "test_astNew", "a");
            TsOdinPointerType tsOdinPointerType = assertInstanceOf(tsOdinType, TsOdinPointerType.class);
            TsOdinStructType tsOdinStructType = assertInstanceOf(tsOdinPointerType.getDereferencedType(), TsOdinStructType.class);
            assertEquals("Stmt", tsOdinStructType.getName());
        }
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "test_astNew", "z");
            TsOdinPointerType tsOdinPointerType = assertInstanceOf(tsOdinType, TsOdinPointerType.class);
            TsOdinStructType tsOdinStructType = assertInstanceOf(tsOdinPointerType.getDereferencedType(), TsOdinStructType.class);
            assertEquals("ConcreteNode2", tsOdinStructType.getName());
        }
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "test_astNew", "y");
            TsOdinPointerType tsOdinPointerType = assertInstanceOf(tsOdinType, TsOdinPointerType.class);
            TsOdinStructType tsOdinStructType = assertInstanceOf(tsOdinPointerType.getDereferencedType(), TsOdinStructType.class);
            assertEquals("ConcreteNode", tsOdinStructType.getName());
        }
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "test_astNew", "x");
            TsOdinPointerType tsOdinPointerType = assertInstanceOf(tsOdinType, TsOdinPointerType.class);
            TsOdinStructType tsOdinStructType = assertInstanceOf(tsOdinPointerType.getDereferencedType(), TsOdinStructType.class);
            assertEquals("ConcreteAstNode", tsOdinStructType.getName());
        }
    }

    public void test_typeInference_polyProcedureGroupWithMake() throws IOException {
        OdinFile odinFile = loadTypeInference();
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "test_polyOverloadWithMake", "z");
            TsOdinTuple tsOdinTuple = assertInstanceOf(tsOdinType, TsOdinTuple.class);
            TsOdinTypeAlias tsOdinTypeAlias = assertInstanceOf(tsOdinTuple.get(0), TsOdinTypeAlias.class);
            assertTrue(tsOdinTypeAlias.isDistinct());
            TsOdinSliceType tsOdinSliceType = assertInstanceOf(tsOdinTypeAlias.getBaseType(), TsOdinSliceType.class);
            TsOdinStructType tsOdinStructType = assertInstanceOf(tsOdinSliceType.getElementType(), TsOdinStructType.class);
            assertEquals("Point", tsOdinStructType.getName());
        }

        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "test_polyOverloadWithMake", "y");
            TsOdinTuple tsOdinTuple = assertInstanceOf(tsOdinType, TsOdinTuple.class);
            TsOdinTypeAlias tsOdinTypeAlias = assertInstanceOf(tsOdinTuple.get(0), TsOdinTypeAlias.class);
            assertFalse(tsOdinTypeAlias.isDistinct());
            TsOdinSliceType tsOdinSliceType = assertInstanceOf(tsOdinTypeAlias.getBaseType(), TsOdinSliceType.class);
            TsOdinStructType tsOdinStructType = assertInstanceOf(tsOdinSliceType.getElementType(), TsOdinStructType.class);
            assertEquals("Point", tsOdinStructType.getName());
        }

        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "test_polyOverloadWithMake", "x");
            TsOdinTuple tsOdinTuple = assertInstanceOf(tsOdinType, TsOdinTuple.class);
            TsOdinSliceType tsOdinSliceType = assertInstanceOf(tsOdinTuple.get(0), TsOdinSliceType.class);
            TsOdinStructType tsOdinStructType = assertInstanceOf(tsOdinSliceType.getElementType(), TsOdinStructType.class);
            assertEquals("Point", tsOdinStructType.getName());
        }

    }

    public void test_implicitExpression() throws IOException {
        OdinFile file = loadTypeInference();
        OdinProcedureDefinition proc = findFirstProcedure(file, "testImplicitEnumExpression");

        {
            OdinExpression expression = findFirstExpressionOfVariable(file, "testImplicitEnumExpression", "addition2");
            OdinImplicitSelectorExpression implicitSelectorExpression = PsiTreeUtil.findChildOfType(expression, OdinImplicitSelectorExpression.class);
            assert implicitSelectorExpression != null;
            TsOdinType tsOdinType = implicitSelectorExpression.getInferredType();
            TsOdinEnumType tsOdinEnumType = assertInstanceOf(tsOdinType, TsOdinEnumType.class);
            assertEquals("Direction", tsOdinEnumType.getName());
        }

        {
            OdinAssignmentStatementImpl supersetAssignment = PsiTreeUtil.findChildrenOfType(proc, OdinAssignmentStatementImpl.class).stream()
                    .filter(a -> a.getLhsExpressions().getText().equals("is_superset"))
                    .findFirst()
                    .orElseThrow();

            OdinImplicitSelectorExpression expression = PsiTreeUtil.findChildOfType(supersetAssignment, OdinImplicitSelectorExpression.class);
            assert expression != null;
            TsOdinType tsOdinType = expression.getInferredType();
            TsOdinEnumType tsOdinEnumType = assertInstanceOf(tsOdinType, TsOdinEnumType.class);
            assertEquals("Direction", tsOdinEnumType.getName());
        }

        {
            OdinExpression expression = findFirstExpressionOfVariable(file, "testImplicitEnumExpression", "nested_ternary");
            Collection<OdinImplicitSelectorExpression> implicitSelectorExpressions = PsiTreeUtil.findChildrenOfType(expression, OdinImplicitSelectorExpression.class);
            for (OdinImplicitSelectorExpression implicitSelectorExpression : implicitSelectorExpressions) {
                System.out.println(implicitSelectorExpression.getText());
                TsOdinType tsOdinType = implicitSelectorExpression.getInferredType();
                TsOdinEnumType tsOdinEnumType = assertInstanceOf(tsOdinType, TsOdinEnumType.class);
                assertEquals("Direction", tsOdinEnumType.getName());
            }
        }

        {
            OdinProcedureDefinition southProc = findFirstProcedure(proc, "south_proc");
            OdinImplicitSelectorExpression expression = PsiTreeUtil.findChildOfType(southProc, OdinImplicitSelectorExpression.class);
            assert expression != null;
            TsOdinType tsOdinType = expression.getInferredType();
            TsOdinEnumType tsOdinEnumType = assertInstanceOf(tsOdinType, TsOdinEnumType.class);
            assertEquals("Direction", tsOdinEnumType.getName());
        }


        {
            // Array index
            OdinAssignmentStatement assignmentStatement = PsiTreeUtil.findChildOfType(proc, OdinAssignmentStatement.class);
            OdinImplicitSelectorExpression implicitSelectorExpression = PsiTreeUtil.findChildOfType(assignmentStatement, OdinImplicitSelectorExpression.class);
            assert implicitSelectorExpression != null;
            TsOdinType tsOdinType = implicitSelectorExpression.getInferredType();
            TsOdinEnumType tsOdinEnumType = assertInstanceOf(tsOdinType, TsOdinEnumType.class);
            assertEquals("Direction", tsOdinEnumType.getName());
        }

        {
            OdinExpression expression = findFirstExpressionOfVariable(file, "testImplicitEnumExpression", "ternary");
            OdinImplicitSelectorExpression implicitSelectorExpression = PsiTreeUtil.findChildOfType(expression, OdinImplicitSelectorExpression.class);
            assert implicitSelectorExpression != null;
            TsOdinType tsOdinType = implicitSelectorExpression.getInferredType();
            TsOdinEnumType tsOdinEnumType = assertInstanceOf(tsOdinType, TsOdinEnumType.class);
            assertEquals("Direction", tsOdinEnumType.getName());
        }

        {
            OdinExpression expression = findFirstExpressionOfVariable(file, "testImplicitEnumExpression", "bit_set_operation");
            OdinImplicitSelectorExpression implicitSelectorExpression = PsiTreeUtil.findChildOfType(expression, OdinImplicitSelectorExpression.class);
            assert implicitSelectorExpression != null;
            TsOdinType tsOdinType = implicitSelectorExpression.getInferredType();
            TsOdinEnumType tsOdinEnumType = assertInstanceOf(tsOdinType, TsOdinEnumType.class);
            assertEquals("Direction", tsOdinEnumType.getName());
        }


        {
            OdinExpression expression = findFirstExpressionOfVariable(file, "testImplicitEnumExpression", "addition");
            OdinImplicitSelectorExpression implicitSelectorExpression = PsiTreeUtil.findChildOfType(expression, OdinImplicitSelectorExpression.class);
            assert implicitSelectorExpression != null;
            TsOdinType tsOdinType = implicitSelectorExpression.getInferredType();
            TsOdinEnumType tsOdinEnumType = assertInstanceOf(tsOdinType, TsOdinEnumType.class);
            assertEquals("Direction", tsOdinEnumType.getName());
        }

        {
            OdinExpression expression = findFirstExpressionOfVariable(file, "testImplicitEnumExpression", "field_proc_ret");
            OdinImplicitSelectorExpression implicitSelectorExpression = PsiTreeUtil.findChildOfType(expression, OdinImplicitSelectorExpression.class);
            assert implicitSelectorExpression != null;
            TsOdinType tsOdinType = implicitSelectorExpression.getInferredType();
            TsOdinEnumType tsOdinEnumType = assertInstanceOf(tsOdinType, TsOdinEnumType.class);
            assertEquals("Direction", tsOdinEnumType.getName());
        }

        {
            OdinExpression expression = findFirstExpressionOfVariable(file, "testImplicitEnumExpression", "b");
            OdinImplicitSelectorExpression implicitSelectorExpression = PsiTreeUtil.findChildOfType(expression, OdinImplicitSelectorExpression.class);
            assert implicitSelectorExpression != null;
            TsOdinType tsOdinType = implicitSelectorExpression.getInferredType();
            TsOdinEnumType tsOdinEnumType = assertInstanceOf(tsOdinType, TsOdinEnumType.class);
            assertEquals("Direction", tsOdinEnumType.getName());
        }

        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(file, "testImplicitEnumExpression", "err");
            TsOdinTuple tsOdinTuple = assertInstanceOf(tsOdinType, TsOdinTuple.class);
            TsOdinEnumType tsOdinEnumType = assertInstanceOf(tsOdinTuple.get(1), TsOdinEnumType.class);
            assertEquals("Languages", tsOdinEnumType.getName());
        }

        {
            OdinExpression expression = findFirstExpressionOfVariable(file, "testImplicitEnumExpression", "z");
            OdinImplicitSelectorExpression implicitSelectorExpression = PsiTreeUtil.findChildOfType(expression, OdinImplicitSelectorExpression.class);
            assert implicitSelectorExpression != null;
            TsOdinType tsOdinType = implicitSelectorExpression.getInferredType();
            assertInstanceOf(tsOdinType.baseType(true), TsOdinEnumType.class);
        }

        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(file, "testImplicitEnumExpression", "z");
            TsOdinTypeAlias tsOdinTypeAlias = assertInstanceOf(tsOdinType, TsOdinTypeAlias.class);
            assertTrue(tsOdinTypeAlias.isDistinct());
            assertInstanceOf(tsOdinTypeAlias.baseType(true), TsOdinEnumType.class);
        }
        {
            OdinCallExpression expression = (OdinCallExpression) findFirstExpressionOfVariable(file, "testImplicitEnumExpression", "g");
            List<OdinArgument> arguments = PsiTreeUtil.findChildrenOfType(expression, OdinArgument.class).stream()
                    .toList();

            OdinUnnamedArgument odinArgument = (OdinUnnamedArgument) arguments.get(1);
            OdinExpression odinExpression = odinArgument.getExpression();
            TsOdinType tsOdinType = odinExpression.getInferredType();
            TsOdinEnumType tsOdinEnumType = assertInstanceOf(tsOdinType, TsOdinEnumType.class);
            assertEquals("Direction", tsOdinEnumType.getName());
        }

        {
            OdinCaseClause caseClause = PsiTreeUtil.findChildOfType(proc, OdinCaseClause.class);
            assertNotNull(caseClause);
            OdinExpression expression = caseClause.getExpressionList().getFirst();
            TsOdinType tsOdinType = expression.getInferredType();
            TsOdinEnumType tsOdinEnumType = assertInstanceOf(tsOdinType, TsOdinEnumType.class);
            assertEquals("Direction", tsOdinEnumType.getName());
        }


        {
            OdinImplicitSelectorExpression implicitSelectorExpression = PsiTreeUtil.findChildOfType(proc, OdinImplicitSelectorExpression.class);
            Objects.requireNonNull(implicitSelectorExpression);
            TsOdinType tsOdinType = OdinExpectedTypeEngine.inferExpectedType(
                    OdinSymbolTableHelper.buildFullSymbolTable(implicitSelectorExpression, new OdinContext()).asContext(), implicitSelectorExpression);
            TsOdinEnumType tsOdinEnumType = assertInstanceOf(tsOdinType, TsOdinEnumType.class);
            assertEquals("Direction", tsOdinEnumType.getName());
        }

        {
            OdinExpression expression = findFirstExpressionOfVariable(file, "testImplicitEnumExpression", "s");
            OdinImplicitSelectorExpression implicitSelectorExpression = PsiTreeUtil.findChildOfType(expression, OdinImplicitSelectorExpression.class);
            assertNotNull(implicitSelectorExpression);
            PsiReference reference = implicitSelectorExpression.getIdentifier().getReference();
            assertNotNull(reference);
            PsiElement resolvedReference = reference.resolve();
            assertNotNull(resolvedReference);
        }


    }

    public void test_typeInference_anyType() throws IOException {
        OdinFile odinFile = loadTypeInference();
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testTypeInference_anyType", "y");
            System.out.println(tsOdinType);
        }
    }

    public void testTwoHopsInferenceWithPointer() throws IOException {
        OdinFile odinFile = loadTypeInference();
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testTwoHopsInferenceWithPointer", "y");
            System.out.println(tsOdinType);
        }
    }

    public void testParapolyWithAliases() throws IOException {
        OdinFile odinFile = loadTypeInference();
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testTypeInference_withParaPolyAlias", "first");
            TsOdinStructType tsOdinStructType = assertInstanceOf(tsOdinType, TsOdinStructType.class);
            assertEquals("Point", tsOdinStructType.getName());
        }

        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testTypeInference_withParaPolyAlias", "first2");
            TsOdinStructType tsOdinStructType = assertInstanceOf(tsOdinType, TsOdinStructType.class);
            assertEquals("Point", tsOdinStructType.getName());
        }

        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testTypeInference_withParaPolyAlias", "first_dist");
            TsOdinStructType tsOdinStructType = assertInstanceOf(tsOdinType, TsOdinStructType.class);
            assertEquals("Point", tsOdinStructType.getName());
        }
    }

    public void testTypeChecker_conversionToExpectedType() throws IOException {
        OdinFile file = load("src/test/testData/type_checker.odin");
        {
            OdinProcedureDefinition testTypeConversion = findFirstProcedure(file, "testTypeConversion");
            List<OdinBlockStatement> blocks = getProcedureBlocks(testTypeConversion);

            OdinBlockStatement firstBlock = blocks.getFirst();
            OdinShortVariableDeclarationStatement varType = findFirstVariableDeclaration(firstBlock, "type");
            TsOdinType tsOdinType = inferTypeOfDeclaration(varType.getDeclaration());
            OdinShortVariableDeclarationStatement varExpectedType = findFirstVariableDeclaration(firstBlock, "expected_type");
            TsOdinType tsOdinExpectedType = inferTypeOfDeclaration(varExpectedType.getDeclaration());

            OdinTypeChecker.TypeCheckResult typeCheckResult = OdinTypeChecker.checkTypes(tsOdinType, tsOdinExpectedType, false);
            assertTrue(typeCheckResult.isCompatible());
            assertEquals(1, typeCheckResult.getConversionActionList().size());

        }
    }

    public void testProcedureContext() throws IOException {
        OdinFile file = loadTypeInference();
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(file, "test_procedureContext", "y");
            assertInstanceOf(tsOdinType, TsOdinRawPointerType.class);
        }
    }

    public void testAbsenceOfFieldsInLhsAndRhs() throws IOException {
        OdinFile file = loadTypeInference();
        OdinProcedureDefinition proc = findFirstProcedure(file, "testAbsenceOfFieldsInRhsAndLhs");
        OdinRhs rhs = PsiTreeUtil.findChildOfType(proc, OdinRhs.class);
        assertNotNull(rhs);
        OdinRefExpression odinRefExpression = assertInstanceOf(rhs.getExpression(), OdinRefExpression.class);
        OdinRefExpression odinRefExpression1 = assertInstanceOf(odinRefExpression.getExpression(), OdinRefExpression.class);
        assertNull(odinRefExpression1.getExpression());
        OdinIdentifier identifier = odinRefExpression1.getIdentifier();
        assertNotNull(identifier);
        PsiReference reference = identifier.getReference();
        OdinReference odinReference = assertInstanceOf(reference, OdinReference.class);
        OdinSymbol symbol = odinReference.getSymbol();
        assertNotNull(symbol);
        assertEquals(OdinSymbolType.PARAMETER, symbol.getSymbolType());
    }

    public void testField() throws IOException {
        OdinFile file = loadTypeInference();
        OdinProcedureDefinition proc = findFirstProcedure(file, "test_structField");

        {
            OdinInitVariableStatement varLine = PsiTreeUtil.findChildrenOfType(proc, OdinInitVariableStatement.class)
                    .stream().filter(v -> v.getDeclaration().getDeclaredIdentifiers().getFirst().getText().equals("l"))
                    .findFirst()
                    .orElseThrow();
            {
                OdinExpression odinExpression = PsiTreeUtil.findChildrenOfType(varLine, OdinLhs.class)
                        .stream()
                        .filter(lhs -> lhs.getExpression().getText().equals("alpha")).findFirst()
                        .map(OdinLhs::getExpression)
                        .orElseThrow();

                OdinSymbolTable context = OdinSymbolTableHelper.buildFullSymbolTable(odinExpression, new OdinContext());
                assertNotNull(context.getSymbol("alpha"));
                assertNotNull(context.getSymbol("beta"));
                assertNotNull(context.getSymbol("gamma"));
            }

            {
                OdinExpression odinExpression = PsiTreeUtil.findChildrenOfType(varLine, OdinLhs.class)
                        .stream()
                        .filter(lhs -> lhs.getExpression().getText().equals("x")).findFirst()
                        .map(OdinLhs::getExpression)
                        .orElseThrow();

                OdinSymbolTable context = OdinSymbolTableHelper.buildFullSymbolTable(odinExpression, new OdinContext());
                assertNotNull(context.getSymbol("x"));
                assertNotNull(context.getSymbol("y"));
            }


        }

        {
            OdinLhs lhs = PsiTreeUtil.findChildOfType(proc, OdinLhs.class);
            Objects.requireNonNull(lhs);
            OdinExpression expression = lhs.getExpression();
            OdinSymbolTable context = OdinSymbolTableHelper.buildFullSymbolTable(expression, new OdinContext());
            assertNotNull(context.getSymbol("x"));
        }
    }

    public void testDynamicArrayAllocator() throws IOException {
        OdinFile file = loadTypeInference();
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(file, "test_dynamicArrayAllocatorSymbol", "y");
            TsOdinStructType tsOdinStructType = assertInstanceOf(tsOdinType, TsOdinStructType.class);
            assertEquals("Allocator", tsOdinStructType.getName());
        }
    }

    public void testNamelessStruct() throws IOException {
        OdinFile file = loadTypeInference();
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(file, "testNamelessStruct", "x");
            assertEquals(TsOdinBuiltInTypes.I32, tsOdinType);

        }
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(file, "testNamelessStruct", "y");
            TsOdinStructType tsOdinStructType = assertInstanceOf(tsOdinType, TsOdinStructType.class);
            assertNull(tsOdinStructType.getDeclaration());
            assertNull(tsOdinStructType.getDeclaredIdentifier());
            assertNotNull(tsOdinStructType.getPsiType());
        }
    }

    public void testBitSetOperations() throws IOException {
        OdinFile file = loadTypeInference();
        {
            OdinExpression expression = findFirstExpressionOfVariable(file, "testBitSetOperations", "operation");
            OdinImplicitSelectorExpression implicitSelectorExpression = PsiTreeUtil.findChildOfType(expression, OdinImplicitSelectorExpression.class);
            Objects.requireNonNull(implicitSelectorExpression);

            TsOdinType tsOdinType = implicitSelectorExpression.getInferredType();
            TsOdinEnumType tsOdinEnumType = assertInstanceOf(tsOdinType, TsOdinEnumType.class);
            assertEquals("Direction", tsOdinEnumType.getName());
        }
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(file, "testBitSetOperations", "x");
            TsOdinTypeAlias tsOdinTypeAlias = assertInstanceOf(tsOdinType, TsOdinTypeAlias.class);
            TsOdinBitSetType tsOdinBitSetType = assertInstanceOf(tsOdinTypeAlias.getBaseType(), TsOdinBitSetType.class);
            TsOdinEnumType tsOdinEnumType = assertInstanceOf(tsOdinBitSetType.getElementType(), TsOdinEnumType.class);
            assertEquals("Direction", tsOdinEnumType.getName());
        }
    }

    public void testEnumeratedArrays() throws IOException {
        OdinFile file = loadTypeInference();
        {
            var proc = findFirstProcedure(file, "testEnumeratedArrays");
            {
                Collection<OdinImplicitSelectorExpression> implicitSelectorExpressions = PsiTreeUtil.findChildrenOfType(
                        proc, OdinImplicitSelectorExpression.class
                );
                OdinImplicitSelectorExpression implicitSelectorExpression =
                        implicitSelectorExpressions.stream()
                                .filter(s -> s.getText().equals(".East"))
                                .findFirst()
                                .orElseThrow();
                TsOdinType tsOdinType = implicitSelectorExpression.getInferredType();
                TsOdinEnumType tsOdinEnumType = assertInstanceOf(tsOdinType, TsOdinEnumType.class);
                assertEquals("Direction", tsOdinEnumType.getName());
            }
            {
                OdinImplicitSelectorExpression implicitExpression = PsiTreeUtil.findChildOfType(proc, OdinImplicitSelectorExpression.class);
                {
                    assert implicitExpression != null;
                    TsOdinType tsOdinType = implicitExpression.getInferredType();
                    assertInstanceOf(tsOdinType, TsOdinEnumType.class);
                }
                {
                    OdinSymbolTable odinSymbolTable = OdinSymbolTableHelper.buildFullSymbolTable(Objects.requireNonNull(implicitExpression), new OdinContext());
                    assertNotNull(odinSymbolTable.getSymbol("North"));
                    assertNotNull(odinSymbolTable.getSymbol("South"));
                    assertNotNull(odinSymbolTable.getSymbol("East"));
                    assertNotNull(odinSymbolTable.getSymbol("West"));
                }
            }

        }
    }

    public void testArraysAndSwizzleFields() throws IOException {
        OdinFile file = loadTypeInference();
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(file, "testSwizzleFieldsAndArrays", "d");
            assertEquals(TsOdinBuiltInTypes.F32, tsOdinType);
        }

        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(file, "testSwizzleFieldsAndArrays", "c");
            TsOdinTypeAlias tsOdinTypeAlias = assertInstanceOf(tsOdinType, TsOdinTypeAlias.class);
            TsOdinArrayType tsOdinArrayType = assertInstanceOf(tsOdinTypeAlias.baseType(true), TsOdinArrayType.class);
            assertEquals(TsOdinBuiltInTypes.F32, tsOdinArrayType.getElementType());
        }
    }

    public void testStringBuilder() throws IOException {
        OdinFile file = loadTypeInference();
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(file, "testStringBuilder", "x");
            TsOdinTuple tsOdinTuple = assertInstanceOf(tsOdinType, TsOdinTuple.class);
            TsOdinStructType tsOdinStructType = assertInstanceOf(tsOdinTuple.get(0), TsOdinStructType.class);
            assertEquals("Builder", tsOdinStructType.getName());
        }
    }

    public void testTypeInfoOf() throws IOException {
        OdinFile file = loadTypeInference();
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(file, "testTypeInfoOf", "x");
            TsOdinPointerType tsOdinPointerType = assertInstanceOf(tsOdinType, TsOdinPointerType.class);
            TsOdinStructType tsOdinStructType = assertInstanceOf(tsOdinPointerType.getDereferencedType(), TsOdinStructType.class);
            assertEquals("Type_Info", tsOdinStructType.getName());
        }
    }

    public void testMatrixType() throws IOException {
        OdinFile file = loadTypeInference();
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(file, "testMatrixType", "x");
            TsOdinArrayType tsOdinArrayType = assertInstanceOf(tsOdinType, TsOdinArrayType.class);
            assertEquals(TsOdinBuiltInTypes.I32, tsOdinArrayType.getElementType());
        }
    }

    public void testNestedWhenStatements() throws IOException {
        OdinFile file = loadTypeInference();
        {
            OdinInitVariableStatement var = findFirstVariableDeclarationStatement(file, "testNestedWhenStatements", "z");
            OdinSymbolTable symbolTable = OdinSymbolTableHelper.buildFullSymbolTable(var, new OdinContext());
            assertNotNull(symbolTable.getSymbol("y"));
        }
        {
            OdinInitVariableStatement var = findFirstVariableDeclarationStatement(file, "testNestedWhenStatements", "x");
            OdinSymbolTable symbolTable = OdinSymbolTableHelper.buildFullSymbolTable(var, new OdinContext());
            assertNotNull(symbolTable.getSymbol("CONST"));
        }
    }

    public void testArrayOfStructs() throws IOException {
        OdinFile file = loadTypeInference();
        {
            var proc = findFirstProcedure(file, "testArrayOfStructs");
            OdinLhs lhs = PsiTreeUtil.findChildOfType(proc, OdinLhs.class);
            Objects.requireNonNull(lhs);
            OdinSymbolTable context = OdinSymbolTableHelper.buildFullSymbolTable(lhs, new OdinContext());
            assertNotNull(context.getSymbol("x"));
            assertNotNull(context.getSymbol("y"));
        }
    }

    public void testPointerToCompoundLiteral() throws IOException {
        OdinFile file = loadTypeInference();
        {
            OdinInitVariableStatement firstVariableDeclarationStatement = findFirstVariableDeclarationStatement(file,
                    "testPointerToCompoundLiteral",
                    "proc_call");
            var addressExpression = PsiTreeUtil.findChildOfType(firstVariableDeclarationStatement,
                    OdinAddressExpression.class);

            OdinLhs lhs = PsiTreeUtil.findChildOfType(addressExpression, OdinLhs.class);
            OdinExpression expression = Objects.requireNonNull(lhs).getExpression();
            OdinSymbolTable context = OdinSymbolTableHelper.doBuildFullSymbolTable(expression);
            assertNotNull(context.getSymbol("x"));
            assertNotNull(context.getSymbol("y"));
        }
        {
            OdinInitVariableStatement firstVariableDeclarationStatement = findFirstVariableDeclarationStatement(file,
                    "testPointerToCompoundLiteral",
                    "nested_struct");
            var addressExpression = PsiTreeUtil.findChildOfType(firstVariableDeclarationStatement,
                    OdinAddressExpression.class);

            OdinLhs lhs = PsiTreeUtil.findChildOfType(addressExpression, OdinLhs.class);
            OdinExpression expression = Objects.requireNonNull(lhs).getExpression();
            OdinSymbolTable context = OdinSymbolTableHelper.doBuildFullSymbolTable(expression);
            assertNotNull(context.getSymbol("x"));
            assertNotNull(context.getSymbol("y"));
        }
        {
            OdinInitVariableStatement firstVariableDeclarationStatement = findFirstVariableDeclarationStatement(file, "testPointerToCompoundLiteral", "nested_points");
            var addressExpression = PsiTreeUtil.findChildOfType(firstVariableDeclarationStatement, OdinAddressExpression.class);

            OdinLhs lhs = PsiTreeUtil.findChildOfType(addressExpression, OdinLhs.class);
            OdinExpression expression = Objects.requireNonNull(lhs).getExpression();
            OdinSymbolTable context = OdinSymbolTableHelper.doBuildFullSymbolTable(expression);
            assertNotNull(context.getSymbol("x"));
            assertNotNull(context.getSymbol("y"));
        }
        {
            OdinInitVariableStatement firstVariableDeclarationStatement = findFirstVariableDeclarationStatement(file, "testPointerToCompoundLiteral", "points");
            OdinLhs lhs = PsiTreeUtil.findChildOfType(firstVariableDeclarationStatement, OdinLhs.class);
            OdinExpression expression = Objects.requireNonNull(lhs).getExpression();
            OdinSymbolTable context = OdinSymbolTableHelper.doBuildFullSymbolTable(expression);
            assertNotNull(context.getSymbol("x"));
            assertNotNull(context.getSymbol("y"));
        }
    }

    public void testBitFieldImplicitExpression() throws IOException {
        var file = loadTypeInference();
        {
            OdinInitVariableStatement variable = findFirstVariableDeclarationStatement(file, "testImplicitEnumExpression", "bitfield_from_proc");
            OdinImplicitSelectorExpression expression = PsiTreeUtil.findChildOfType(variable, OdinImplicitSelectorExpression.class);
            assertNotNull(expression);
            TsOdinType inferredType = expression.getInferredType();
            TsOdinEnumType enumType = assertInstanceOf(inferredType, TsOdinEnumType.class);
            assertEquals("Direction", enumType.getName());
        }
        {
            OdinInitVariableStatement variable = findFirstVariableDeclarationStatement(file, "testImplicitEnumExpression", "bitfield");
            OdinImplicitSelectorExpression expression = PsiTreeUtil.findChildOfType(variable, OdinImplicitSelectorExpression.class);
            assertNotNull(expression);
            TsOdinType inferredType = expression.getInferredType();
            TsOdinEnumType enumType = assertInstanceOf(inferredType, TsOdinEnumType.class);
            assertEquals("Direction", enumType.getName());
        }
    }


    public void testPositionalInitialization() throws IOException {
        OdinFile file = loadTypeInference();
        {
            OdinInitVariableStatement var = findFirstVariableDeclarationStatement(file, "testPositionalInitialization", "s");
            OdinImplicitSelectorExpression expression = PsiTreeUtil.findChildOfType(var, OdinImplicitSelectorExpression.class);
            assertNotNull(expression);
            TsOdinType tsOdinType = OdinExpectedTypeEngine.inferExpectedType(
                    OdinSymbolTableHelper.buildFullSymbolTable(expression, new OdinContext()).asContext(), expression);
            TsOdinEnumType tsOdinEnumType = assertInstanceOf(tsOdinType, TsOdinEnumType.class);
            assertEquals("E", tsOdinEnumType.getName());
        }
    }

    public void testRecursiveStruct() throws IOException {
        OdinFile file = loadTypeInference();
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(file, "testRecursiveStruct", "x");
            TsOdinPointerType tsOdinPointerType = assertInstanceOf(tsOdinType, TsOdinPointerType.class);
            TsOdinStructType tsOdinStructType = assertInstanceOf(tsOdinPointerType.getDereferencedType(), TsOdinStructType.class);
            assertEquals("Node", tsOdinStructType.getName());
        }
    }

    public void testPsiFileAtOffset() throws IOException {
        {
//            OdinFile file = load("D:\\dev\\code\\odin-intellij\\modules\\core\\src\\test\\sdk\\core\\os\\os_freebsd.odin");
//            PsiElement element = file.findElementAt(2911);
//            assertNotNull(element);

//            OdinQualifiedType qualifiedType = PsiTreeUtil.getParentOfType(element, OdinQualifiedType.class);
//            OdinSymbol symbol = OdinReferenceResolver.resolve(new OdinContext(), qualifiedType.getTypeIdentifier());
//            assertNotNull(symbol);
//            System.out.println(symbol);

//            OdinRefExpression refExpression = PsiTreeUtil.getParentOfType(element, OdinRefExpression.class);
//            OdinSymbol referencedSymbol = refExpression.getIdentifier().getReferencedSymbol();
//            assertNotNull(referencedSymbol);
//
//            OdinRefExpression refExpression = PsiTreeUtil.getParentOfType(element, OdinRefExpression.class);
//            TsOdinType inferredType = Objects.requireNonNull(refExpression).getInferredType();
//            System.out.println(inferredType);
//            assertFalse(inferredType.isUnknown());

//            EvOdinValue value = OdinExpressionEvaluator.evaluate(refExpression);
//            assertFalse(value.asBool());
//            TsOdinType inferredType = refExpression.getInferredType();
//            TsOdinSliceType tsOdinSliceType = assertInstanceOf(inferredType.baseType(), TsOdinSliceType.class);

//            OdinIdentifier identifier = PsiTreeUtil.getParentOfType(element, OdinIdentifier.class);
//            assertNotNull(identifier);
//            OdinSymbol referencedSymbol = identifier.getReferencedSymbol();
//            assertNotNull(referencedSymbol);

//            file.getFileScope().accept(new OdinRecursiveVisitor() {
//                @Override
//                public void visitIdentifier(@NotNull OdinIdentifier o) {
//                    o.getReferencedSymbol();
//                }
//            });

//            OdinImplicitSelectorExpression implicitSelectorExpression = PsiTreeUtil.getParentOfType(element, OdinImplicitSelectorExpression.class);
//            assertNotNull(implicitSelectorExpression);
//            TsOdinType inferredType = implicitSelectorExpression.getInferredType();
//            assertInstanceOf(inferredType, TsOdinEnumType.class);
////
//            OdinIdentifier identifier = PsiTreeUtil.getParentOfType(element, OdinIdentifier.class);
//            assertNotNull(identifier);
//            PsiReference reference = identifier.getReference();
//            OdinReference odinReference = assertInstanceOf(reference, OdinReference.class);
//            assertNotNull(odinReference);
//            OdinSymbol symbol = odinReference.getSymbol();
//            assertEquals(symbol.getSymbolType(), OdinSymbolType.PROCEDURE);

        }
    }

    public void testOffsetOf() throws IOException {
        OdinFile file = loadTypeInference();
        {
            OdinExpression firstExpressionOfVariable = findFirstExpressionOfVariable(file, "testOffsetOfSymbols", "offset");
            OdinArgument[] arguments = PsiTreeUtil.getChildrenOfType(firstExpressionOfVariable, OdinArgument.class);

            OdinUnnamedArgument argument = (OdinUnnamedArgument) Objects.requireNonNull(arguments)[1];
            assertEquals("x", argument.getText());
            OdinSymbolTable context = OdinSymbolTableHelper.buildFullSymbolTable(argument.getExpression(), new OdinContext());
            assertNotNull(context.getSymbol("x"));
            assertNotNull(context.getSymbol("y"));
        }
    }

    private OdinFile loadTypeInference() throws IOException {
        return load("src/test/testData/type_inference.odin");
    }

    public void testTypeIdSymbols() throws IOException {
        OdinFile file = loadTypeInference();
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(file, "testConstrainedType", "x");
            assertInstanceOf(tsOdinType, TsOdinMapType.class);
            System.out.println(tsOdinType);
        }
    }

    public void testParaPolyFields() throws IOException {
        OdinFile file = loadTypeInference();
        {
            var proc = findFirstProcedure(file, "testFieldsOfParaPoly");

            OdinWhereClause whereClause = PsiTreeUtil.findChildOfType(proc, OdinWhereClause.class);
            OdinRefExpression refExpression = Objects.requireNonNull(PsiTreeUtil.findChildOfType(whereClause, OdinRefExpression.class));

            OdinIdentifier identifier = Objects.requireNonNull(refExpression.getIdentifier());
            OdinSymbol context = identifier.getReference().getSymbol();

            OdinExpression odinExpression = refExpression.getExpression();
            assert odinExpression != null;
            TsOdinType tsOdinType = odinExpression.getInferredType();
            OdinSymbolTable typeElements = OdinInsightUtils.getTypeElements(new OdinContext(), project, tsOdinType);
            assertNotNull(typeElements.getSymbol("Key"));
            assertNotNull(typeElements.getSymbol("Value"));
        }
    }

    public void testConstrainedTypeChecker() throws IOException {
        OdinFile file = load("src/test/sdk/base/runtime/core_builtin_soa.odin");
        {

            PsiElement element = file.findElementAt(1728);
            OdinCallExpression callExpression = PsiTreeUtil.getParentOfType(element, false, OdinCallExpression.class);
            assertNotNull(callExpression);
            TsOdinType tsOdinType = callExpression.getInferredType();
            assertInstanceOf(tsOdinType, TsOdinPointerType.class);
        }
    }

    public void testNestedProcParam() throws IOException {
        OdinFile odinFile = loadTypeInference();
        {
            OdinProcedureDefinition proc = findFirstProcedure(odinFile, "default_swap_proc");
            OdinReturnParameters returnParameters = PsiTreeUtil.findChildOfType(proc, OdinReturnParameters.class);
            OdinProcedureType returnProc = PsiTreeUtil.findChildOfType(returnParameters, OdinProcedureType.class);
            OdinParamEntry paramEntry =
                    Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(returnProc).getParamEntries()))
                            .getParamEntryList()
                            .getFirst();
            OdinParameterDeclaration parameterDeclaration = paramEntry.getParameterDeclaration();
            OdinType typeDefinition = parameterDeclaration.getTypeDefinition();
            if (typeDefinition instanceof OdinArrayType arrayType) {
                OdinType type = arrayType.getType();
                assert type != null;
                OdinSymbolTable context = OdinSymbolTableHelper.buildFullSymbolTable(type, new OdinContext());
                assertNotNull(context.getSymbol("T"));
            }
        }
    }

    public void testSoaSlices() throws IOException {
        OdinFile odinFile = loadTypeInference();
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testSoaSlices", "s");
            TsOdinSoaSliceType soaSlice = assertInstanceOf(tsOdinType, TsOdinSoaSliceType.class);

            assertNotNull(soaSlice.getSlices().get("x"));
            assertNotNull(soaSlice.getSlices().get("y"));
            assertNotNull(soaSlice.getSlices().get("z"));
        }

        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testSoaSlices", "d");
            assertEquals(TsOdinBuiltInTypes.I32, tsOdinType);
        }
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testSoaSlices", "e");
            assertEquals(TsOdinBuiltInTypes.F32, tsOdinType);
        }
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testSoaSlices", "f");
            assertEquals(TsOdinBuiltInTypes.I64, tsOdinType);
        }
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testSoaSlices", "a");
            TsOdinSliceType tsOdinSliceType = assertInstanceOf(tsOdinType, TsOdinSliceType.class);
            assertEquals(TsOdinBuiltInTypes.I32, tsOdinSliceType.getElementType());
        }
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testSoaSlices", "b");
            TsOdinSliceType tsOdinSliceType = assertInstanceOf(tsOdinType, TsOdinSliceType.class);
            assertEquals(TsOdinBuiltInTypes.F32, tsOdinSliceType.getElementType());
        }
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testSoaSlices", "c");
            TsOdinSliceType tsOdinSliceType = assertInstanceOf(tsOdinType, TsOdinSliceType.class);
            assertEquals(TsOdinBuiltInTypes.I64, tsOdinSliceType.getElementType());
        }
    }

    public void testAnyType() throws IOException {
        OdinFile file = loadTypeInference();

        TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(file, "testAnyType", "y");
        assertTrue(tsOdinType.isAnyType());
    }

    public void testUnionConversion() throws IOException {
        OdinFile file = loadTypeInference();

        TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(file, "testUnionConversion", "x");
        assertEquals(TsOdinBuiltInTypes.I32, tsOdinType);
    }

    public void testAnyTypeConversion() throws IOException {
        OdinFile file = loadTypeInference();

        TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(file, "testAnyTypeConversion", "x");
        assertEquals(TsOdinBuiltInTypes.I32, tsOdinType);
    }

    public void testRecoverRules() throws IOException {
        OdinFile file = load("src/test/testData/recover.odin");
    }

    public void testTypeConversionRules() {
        {
            OdinTypeChecker.TypeCheckResult typeCheckResult = OdinTypeChecker.checkTypes(TsOdinBuiltInTypes.F16, TsOdinBuiltInTypes.COMPLEX32);
            assertTrue(typeCheckResult.isCompatible());
        }

        {
            OdinTypeChecker.TypeCheckResult typeCheckResult = OdinTypeChecker.checkTypes(TsOdinBuiltInTypes.F32, TsOdinBuiltInTypes.COMPLEX64);
            assertTrue(typeCheckResult.isCompatible());
        }

        {
            OdinTypeChecker.TypeCheckResult typeCheckResult = OdinTypeChecker.checkTypes(TsOdinBuiltInTypes.F64, TsOdinBuiltInTypes.COMPLEX128);
            assertTrue(typeCheckResult.isCompatible());
        }

        {
            OdinTypeChecker.TypeCheckResult typeCheckResult = OdinTypeChecker.checkTypes(TsOdinBuiltInTypes.F16, TsOdinBuiltInTypes.QUATERNION64);
            assertTrue(typeCheckResult.isCompatible());
        }

        {
            OdinTypeChecker.TypeCheckResult typeCheckResult = OdinTypeChecker.checkTypes(TsOdinBuiltInTypes.F32, TsOdinBuiltInTypes.QUATERNION128);
            assertTrue(typeCheckResult.isCompatible());
        }

        {
            OdinTypeChecker.TypeCheckResult typeCheckResult = OdinTypeChecker.checkTypes(TsOdinBuiltInTypes.F64, TsOdinBuiltInTypes.QUATERNION256);
            assertTrue(typeCheckResult.isCompatible());
        }
    }

    public void testFloatTypeConversion() throws IOException {
        OdinFile file = loadTypeInference();
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(file, "testFloatConversion", "y");
            assertEquals(TsOdinBuiltInTypes.I64, tsOdinType);
        }

        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(file, "testFloatConversion", "x");
            assertEquals(TsOdinBuiltInTypes.F64, tsOdinType);
        }
    }

    public void testSwizzleBuiltinProc() throws IOException {
        OdinFile file = loadTypeInference();
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(file, "testSwizzleBuiltinProc", "x");
            TsOdinArrayType tsOdinArrayType = assertInstanceOf(tsOdinType, TsOdinArrayType.class);
            assertEquals(3, tsOdinArrayType.getSize().intValue());
        }
    }

    // Expression Evaluation

    public void testIntegerValue() throws IOException {
        OdinFile file = loadExpressionEval();
        {
            EvOdinValue evOdinValue = evaluateFirstRightHandExpressionOfConstant(file, "testIntegerValue", "Y");
            Long l = assertInstanceOf(evOdinValue.getValue(), Long.class);
            assertEquals(1, l.longValue());
        }

        {
            EvOdinValue evOdinValue = evaluateFirstRightHandExpressionOfConstant(file, "testIntegerValue", "X");
            Long l = assertInstanceOf(evOdinValue.getValue(), Long.class);
            assertEquals(1, l.longValue());
        }

        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(file, "testIntegerValue", "x");
            TsOdinArrayType tsOdinArrayType = assertInstanceOf(tsOdinType, TsOdinArrayType.class);
            assertEquals(1, tsOdinArrayType.getSize().intValue());
        }
    }

    public void testAddingIntegers() throws IOException {
        OdinFile file = loadExpressionEval();
        {
            EvOdinValue evOdinValue = evaluateFirstRightHandExpressionOfConstant(file, "testAddingIntegers", "Y");
            Long l = assertInstanceOf(evOdinValue.getValue(), Long.class);
            assertEquals(4, l.longValue());
        }
    }

    public void testOdinConstants() throws IOException {
        OdinFile file = loadExpressionEval();
        {
            EvOdinValue evOdinValue = evaluateFirstRightHandExpressionOfConstant(file, "testOdinOs", "IS_BUILD_MODE_DYNAMIC");
            @NotNull Boolean val = assertInstanceOf(evOdinValue.getValue(), Boolean.class);
            assertFalse(val);
        }
        {
            EvOdinValue evOdinValue = evaluateFirstRightHandExpressionOfConstant(file, "testOdinOs", "IS_WINDOWS");
            @NotNull Boolean val = assertInstanceOf(evOdinValue.getValue(), Boolean.class);
            assertTrue(val);
        }

    }

    public void testIndirectImport() throws IOException {
        OdinFile file = loadTestData("evaluation/os_linux.odin");
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(file, "test_indirect_import", "x");
            assertEquals(TsOdinBuiltInTypes.I64, tsOdinType);
        }
    }

    public void testWhenStatementConditions() throws IOException {
        var file = loadExpressionEval();

        OdinProcedureDefinition proc = findFirstProcedure(file, "testWhenStatementConditions");
        Collection<OdinCondition> conditions = PsiTreeUtil.findChildrenOfType(proc, OdinCondition.class);
        for (OdinCondition condition : conditions) {
            try {
                OdinExpression expression = condition.getExpression();
                EvOdinValue value = OdinExpressionEvaluator.evaluate(expression);
                System.out.printf("%s -> %s%n", expression.getText(), value);
            } catch (StackOverflowError t) {
                System.out.println(condition.getExpression().getText() + " caused a stack overflow");
            }
        }
    }

    public void testConditionalImports_easy() throws IOException {
        OdinFile file = loadTestData("evaluation/importee.odin");
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(file, "test_reference", "y");
            assertEquals(TsOdinBuiltInTypes.I32, tsOdinType);
        }
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(file, "test_reference", "x");
            assertEquals(TsOdinBuiltInTypes.F64, tsOdinType);
        }
    }

    public void testConditionalImports_advanced() throws IOException {
        OdinFile file = loadTestData("evaluation/importee.odin");
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(file, "advanced", "win_amd_var");
            assertEquals(TsOdinBuiltInTypes.F64, tsOdinType);
        }
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(file, "advanced", "win_i386_var");
            assertEquals(TsOdinBuiltInTypes.I32, tsOdinType);
        }
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(file, "advanced", "win_arm64_var");
            assertEquals(TsOdinBuiltInTypes.I32, tsOdinType);
        }
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(file, "advanced", "linux_var");
            assertEquals(TsOdinBuiltInTypes.I32, tsOdinType);
        }
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(file, "advanced", "shared_var");
            assertEquals(TsOdinBuiltInTypes.I64, tsOdinType);
        }
    }

    public void testWhenStatementMultipleDefinitions_withAlias() throws IOException {
        OdinFile file = loadExpressionEval();
        {
            TsOdinType firstExpressionOfVariable = inferFirstRightHandExpressionOfVariable(file, "testWhenStatementMultipleDefinitions_withAlias", "linux_val");
            assertEquals(TsOdinBuiltInTypes.I32, firstExpressionOfVariable);
        }
        {
            TsOdinType firstExpressionOfVariable = inferFirstRightHandExpressionOfVariable(file, "testWhenStatementMultipleDefinitions_withAlias", "other_val");
            assertEquals(TsOdinBuiltInTypes.I64, firstExpressionOfVariable);
        }

    }

    public void testWhenStatementMultipleDefinitions() throws IOException {
        OdinFile file = loadExpressionEval();
        {
            TsOdinType firstExpressionOfVariable = inferFirstRightHandExpressionOfVariable(file, "testWhenStatementMultipleDefinitions", "linux_val");
            assertEquals(TsOdinBuiltInTypes.I32, firstExpressionOfVariable);
        }

        {
            TsOdinType firstExpressionOfVariable = inferFirstRightHandExpressionOfVariable(file, "testWhenStatementMultipleDefinitions", "other_val");
            assertEquals(TsOdinBuiltInTypes.F32, firstExpressionOfVariable);
        }

        {
            TsOdinType firstExpressionOfVariable = inferFirstRightHandExpressionOfVariable(file, "testWhenStatementMultipleDefinitions", "darwin_val");
            assertEquals(TsOdinBuiltInTypes.F64, firstExpressionOfVariable);
        }
    }

    public void testWhenStatement() throws IOException {
        OdinFile file = loadExpressionEval();
        {
            EvOdinValue evOdinValue = evaluateFirstRightHandExpressionOfConstant(file, "testWhenStatement", "Y");
            @NotNull Long val = assertInstanceOf(evOdinValue.getValue(), Long.class);
            assertEquals(3, val.longValue());
        }
    }

    public void testUsingVsNonUsing() throws IOException {
        OdinFile file = loadTypeInference();
        {
            OdinProcedureDefinition proc = findFirstProcedure(file, "testUsingVsNonUsing");
            OdinInitVariableStatement s1 = findFirstVariable(proc, "s1");
            {
                OdinExpression expression = s1.getRhsExpressions().getExpressionList().getFirst();
                OdinQualifiedType qualifiedType = PsiTreeUtil.findChildOfType(expression, OdinQualifiedType.class);
                assertNotNull(qualifiedType);
                OdinSymbol referencedSymbol = qualifiedType.getTypeIdentifier().getReferencedSymbol();
                assertFalse(referencedSymbol.isVisibleThroughUsing());
            }
            OdinInitVariableStatement s1Using = findFirstVariable(proc, "s1_using");
            {
                OdinExpression expression = s1Using.getRhsExpressions().getExpressionList().getFirst();
                OdinSimpleRefType qualifiedType = PsiTreeUtil.findChildOfType(expression, OdinSimpleRefType.class);
                assertNotNull(qualifiedType);
                OdinSymbol referencedSymbol = qualifiedType.getIdentifier().getReferencedSymbol();
                assertTrue(referencedSymbol.isVisibleThroughUsing());
            }

        }
    }

    public void testParapolyExpressionEval() throws IOException {
        OdinFile file = loadExpressionEval();
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(file, "testParapolyExpressionEval", "elem3");
            TsOdinArrayType tsOdinArrayType = assertInstanceOf(tsOdinType, TsOdinArrayType.class);
            assertNotNull(tsOdinArrayType.getSize());
            assertEquals(3, tsOdinArrayType.getSize().intValue());
        }

        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(file, "testParapolyExpressionEval", "elem4");
            TsOdinArrayType tsOdinArrayType = assertInstanceOf(tsOdinType, TsOdinArrayType.class);
            assertNotNull(tsOdinArrayType.getSize());
            assertEquals(4, tsOdinArrayType.getSize().intValue());
        }
    }

    public void testEvaluateBuildFlags() throws IOException {
        OdinFile file = loadTestData("evaluation/build_flags.odin");
        {
            // windows i386
            var clauses = file.getFileScope().getBuildFlagClauseList().subList(4, 5);
            OdinBuildFlagEvaluator buildFlagEvaluator = new OdinBuildFlagEvaluator();
            var values = buildFlagEvaluator.evaluate(clauses);
            {
                var odinOs = values.entrySet().stream()
                        .filter(e -> e.getKey().getName().equals("ODIN_OS"))
                        .findFirst().orElseThrow();
                EvOdinValue value = odinOs.getValue();
                EvOdinEnumSet set = assertInstanceOf(value, EvOdinEnumSet.class);
                assertContainsElements(set.getValue(),
                        new EvEnumValue("Windows", 1)
                );
            }

            {
                var odinOs = values.entrySet().stream()
                        .filter(e -> e.getKey().getName().equals("ODIN_ARCH"))
                        .findFirst().orElseThrow();
                EvOdinValue value = odinOs.getValue();
                EvOdinEnumSet set = assertInstanceOf(value, EvOdinEnumSet.class);
                assertContainsElements(set.getValue(),
                        new EvEnumValue("i386", 2)
                );
            }
        }

        {
            // !darwin && !freestanding
            var clauses = file.getFileScope().getBuildFlagClauseList().subList(2, 4);
            OdinBuildFlagEvaluator buildFlagEvaluator = new OdinBuildFlagEvaluator();
            var values = buildFlagEvaluator.evaluate(clauses);
            var odinOs = values.entrySet().stream()
                    .filter(e -> e.getKey().getName().equals("ODIN_OS"))
                    .findFirst().orElseThrow();
            EvOdinValue value = odinOs.getValue();
            EvOdinEnumSet set = assertInstanceOf(value, EvOdinEnumSet.class);
            assertDoesntContain(set.getValue(), new EvEnumValue("Darwin", 2));
            assertDoesntContain(set.getValue(), new EvEnumValue("Freestanding", 12));
            assertContainsElements(set.getValue(),
                    new EvEnumValue("Windows", 1),
                    new EvEnumValue("Linux", 3),
                    new EvEnumValue("Essence", 4),
                    new EvEnumValue("FreeBSD", 5),
                    new EvEnumValue("OpenBSD", 6),
                    new EvEnumValue("NetBSD", 7),
                    new EvEnumValue("Haiku", 8),
                    new EvEnumValue("WASI", 9),
                    new EvEnumValue("JS", 10),
                    new EvEnumValue("Orca", 11)
            );
        }

        {
            // !darwin
            OdinBuildFlagClause buildFlagClause = file.getFileScope().getBuildFlagClauseList().get(2);
            OdinBuildFlagEvaluator buildFlagEvaluator = new OdinBuildFlagEvaluator();
            var values = buildFlagEvaluator.evaluateBuildFlagClause(buildFlagClause);
            var odinOs = values.entrySet().stream()
                    .filter(e -> e.getKey().getName().equals("ODIN_OS"))
                    .findFirst().orElseThrow();
            EvOdinValue value = odinOs.getValue();
            EvOdinEnumSet set = assertInstanceOf(value, EvOdinEnumSet.class);
            assertDoesntContain(set.getValue(), new EvEnumValue("Darwin", 1));
            assertContainsElements(set.getValue(),
                    new EvEnumValue("Windows", 1),
                    new EvEnumValue("Linux", 3),
                    new EvEnumValue("Essence", 4),
                    new EvEnumValue("FreeBSD", 5),
                    new EvEnumValue("OpenBSD", 6),
                    new EvEnumValue("NetBSD", 7),
                    new EvEnumValue("Haiku", 8),
                    new EvEnumValue("WASI", 9),
                    new EvEnumValue("JS", 10),
                    new EvEnumValue("Orca", 11),
                    new EvEnumValue("Freestanding", 12)
            );
        }

        {
            OdinBuildFlagClause buildFlagClause = file.getFileScope().getBuildFlagClauseList().get(1);
            OdinBuildFlagEvaluator buildFlagEvaluator = new OdinBuildFlagEvaluator();
            var values = buildFlagEvaluator.evaluateBuildFlagClause(buildFlagClause);
            var odinOs = values.entrySet().stream()
                    .filter(e -> e.getKey().getName().equals("ODIN_OS"))
                    .findFirst().orElseThrow();
            EvOdinValue value = odinOs.getValue();
            EvOdinEnumSet set = assertInstanceOf(value, EvOdinEnumSet.class);
            assertDoesntContain(set.getValue(), new EvEnumValue("Windows", 1));
            assertContainsElements(set.getValue(),
                    new EvEnumValue("Darwin", 2),
                    new EvEnumValue("Linux", 3),
                    new EvEnumValue("Essence", 4),
                    new EvEnumValue("FreeBSD", 5),
                    new EvEnumValue("OpenBSD", 6),
                    new EvEnumValue("NetBSD", 7),
                    new EvEnumValue("Haiku", 8),
                    new EvEnumValue("WASI", 9),
                    new EvEnumValue("JS", 10),
                    new EvEnumValue("Orca", 11),
                    new EvEnumValue("Freestanding", 12)
            );
        }

        {
            OdinBuildFlagClause buildFlagClause = file.getFileScope().getBuildFlagClauseList().getFirst();
            OdinBuildFlagEvaluator buildFlagEvaluator = new OdinBuildFlagEvaluator();
            var values = buildFlagEvaluator.evaluateBuildFlagClause(buildFlagClause);
            String constantName = "ODIN_OS";
            assertContainsElements(values.keySet().stream()
                    .map(OdinSymbol::getName).toList(), constantName);
            var entry = values.entrySet().stream().findFirst().orElseThrow();
            EvOdinValue value = entry.getValue();
            EvOdinEnumSet set = assertInstanceOf(value, EvOdinEnumSet.class);
            Object firstValue = set.getValue().stream().findFirst().orElseThrow();
            EvEnumValue evEnumValue = assertInstanceOf(firstValue, EvEnumValue.class);
            assertEquals(new EvEnumValue("Windows", 1), evEnumValue);
        }

    }

    public void testDataflowAnalyzer() throws IOException {
        OdinFile file = loadTestData("dataflow/dataflow.odin");

        {
            OdinExpression f = findFirstExpressionOfVariable(file, "types", "s");
            OdinCompoundLiteralExpression compoundLiteralExpression = assertInstanceOf(f, OdinCompoundLiteralExpression.class);
            @NotNull OdinCompoundLiteral compoundLiteral = compoundLiteralExpression.getCompoundLiteral();
            OdinCompoundLiteralTyped compoundLiteralTyped = assertInstanceOf(compoundLiteral, OdinCompoundLiteralTyped.class);
            OdinType type = compoundLiteralTyped.getTypeContainer().getType();
            OdinSimpleRefType simpleRefType = assertInstanceOf(type, OdinSimpleRefType.class);

            OdinSymbol symbol = OdinReferenceResolver.resolve(new OdinContext(), simpleRefType.getIdentifier());

            {
                TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(file, "types", "w");
                assertEquals(TsOdinBuiltInTypes.I32, tsOdinType);
            }

            {
                TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(file, "types", "l");
                assertEquals(TsOdinBuiltInTypes.F64, tsOdinType);
            }
        }

        {
            OdinInitVariableStatement elseWindows = findFirstVariable(file, "is_windows");
            OdinLattice odinLattice = OdinWhenConstraintsSolver.solveLattice(new OdinContext(), elseWindows);
            odinLattice.printValues();
        }
    }

    public void testStructTypeReferenceNotHavingTypeElements() throws IOException {
        OdinFile file = loadTypeInference();
        {
            OdinRefExpression ref = (OdinRefExpression) findFirstExpressionOfVariable(file, "testStructTypeReferenceNotHavingTypeElements", "x");
            OdinSymbol referencedSymbol = Objects.requireNonNull(ref.getIdentifier()).getReferencedSymbol();
            assertNull(referencedSymbol);
        }
    }

    public void testUsingWithAlias() throws IOException {
        OdinFile file = loadTypeInference();
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(file, "testUsingWithAlias", "x");
            assertEquals(TsOdinBuiltInTypes.I32, tsOdinType);
        }
    }

    public void testIteratorWithPolyProc() throws IOException {
        OdinFile odinFile = loadTypeInference();
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testIteratorWithPolyProc", "x");
            TsOdinStructType structType = assertInstanceOf(tsOdinType, TsOdinStructType.class);
            assertEquals("Bullet", structType.getName());
        }

        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testIteratorWithPolyProc", "y");
            assertEquals(TsOdinBuiltInTypes.I32, tsOdinType);
        }
    }

    public void testMatrixTranspose() throws IOException {
        OdinFile odinFile = loadTypeInference();
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testMatrixTranspose", "m_t");
            TsOdinMatrixType matrixType = assertInstanceOf(tsOdinType, TsOdinMatrixType.class);
            assertEquals(3, (int) matrixType.getRows());
            assertEquals(4, (int) matrixType.getColumns());
        }
    }

    public void testPseudoMethods() throws IOException {
        OdinFile odinFile = loadTypeInference();
        {
            OdinProcedureDefinition proc = findFirstProcedure(odinFile, "testPseudoMethods");
            OdinImplicitSelectorExpression expression = PsiTreeUtil.findChildOfType(proc, OdinImplicitSelectorExpression.class);
            assertNotNull(expression);
            TsOdinType type = expression.getInferredType();
            TsOdinEnumType tsOdinEnumType = assertInstanceOf(type, TsOdinEnumType.class);
            assertEquals("Enum", tsOdinEnumType.getName());
        }
    }

    public void testObjc() throws IOException {
        OdinFile odinFile = loadTypeInference();
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testObjc", "lion_milk");
            assertEquals(TsOdinBuiltInTypes.F64, tsOdinType);
        }

        {
            OdinImplicitSelectorExpression implicitSelectorExpression = PsiTreeUtil.findChildOfType(findFirstProcedure(odinFile, "testObjc"), OdinImplicitSelectorExpression.class);
            TsOdinType inferredType = implicitSelectorExpression.getInferredType();
            TsOdinEnumType tsOdinEnumType = assertInstanceOf(inferredType, TsOdinEnumType.class);
            assertEquals("Activity", tsOdinEnumType.getName());
        }

        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testObjc", "dog_initialized");
            TsOdinPointerType tsOdinPointerType = assertInstanceOf(tsOdinType, TsOdinPointerType.class);
            TsOdinObjcClass tsOdinObjcClass = assertInstanceOf(tsOdinPointerType.getDereferencedType(), TsOdinObjcClass.class);
            assertEquals("NSDog", tsOdinObjcClass.getObjcClassName());
            assertEquals("Dog", tsOdinObjcClass.getStructType().getName());
        }


        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testObjc", "lion_roar");
            assertEquals(TsOdinBuiltInTypes.F64, tsOdinType);
        }
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testObjc", "lion_groom");
            assertEquals(TsOdinBuiltInTypes.F64, tsOdinType);
        }
        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testObjc", "lion_cub_roar");
            assertEquals(TsOdinBuiltInTypes.F64, tsOdinType);
        }

        {
            TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testObjc", "cat_name");
            assertEquals(TsOdinBuiltInTypes.STRING, tsOdinType);
        }
    }

    public void testImplicitSelectorsAsMapKeys() throws IOException {
        OdinFile odinFile = loadTypeInference();
        {
            OdinProcedureDefinition proc = findFirstProcedure(odinFile, "testImplicitSelectorsAsMapKeys");
            OdinImplicitSelectorExpression expression = PsiTreeUtil.findChildOfType(proc, OdinImplicitSelectorExpression.class);
            assertNotNull(expression);
            TsOdinType inferredType = expression.getInferredType();
            TsOdinEnumType tsOdinEnumType = assertInstanceOf(inferredType, TsOdinEnumType.class);
            assertEquals("Direction", tsOdinEnumType.getName());
        }
    }

    public void testSoaPointers() throws IOException {
        OdinFile odinFile = loadTypeInference();
        {
            OdinProcedureDefinition proc = findFirstProcedure(odinFile, "testImplicitSelectorsAsMapKeys");
            {
                TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testSoaPointers", "bx");
                assertEquals(TsOdinBuiltInTypes.I32, tsOdinType);
            }

            {
                TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testSoaPointers", "dx");
                assertEquals(TsOdinBuiltInTypes.I32, tsOdinType);
            }

            {
                TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testSoaPointers", "sx");
                assertEquals(TsOdinBuiltInTypes.I32, tsOdinType);
            }

            {
                TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, "testSoaPointers", "ax");
                assertEquals(TsOdinBuiltInTypes.I32, tsOdinType);
            }
        }
    }

    private OdinFile loadExpressionEval() throws IOException {
        String filePath = "expression_eval.odin";
        return loadTestData(filePath);
    }

    private OdinFile loadTestData(String filePath) throws IOException {
        return load("src/test/testData/" + filePath);
    }


}