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
import com.intellij.openapi.application.ex.PathManagerEx;
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
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.impl.ProgressManagerImpl;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.pom.PomModel;
import com.intellij.pom.core.impl.PomModelImpl;
import com.intellij.pom.tree.TreeAspect;
import com.intellij.psi.*;
import com.intellij.psi.impl.*;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistryImpl;
import com.intellij.psi.impl.source.tree.ForeignLeafPsiElement;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageManagerImpl;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.*;
import com.intellij.util.CachedValuesManagerImpl;
import com.intellij.util.KeyedLazyInstance;
import com.intellij.util.SystemProperties;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.messages.MessageBus;
import com.lasagnerd.odin.insights.OdinInsightUtils;
import com.lasagnerd.odin.insights.OdinReferenceResolver;
import com.lasagnerd.odin.insights.OdinScope;
import com.lasagnerd.odin.insights.OdinScopeResolver;
import com.lasagnerd.odin.insights.typeInference.OdinInferenceEngine;
import com.lasagnerd.odin.insights.typeInference.OdinTypeInferenceResult;
import com.lasagnerd.odin.insights.typeSystem.TsOdinArrayType;
import com.lasagnerd.odin.insights.typeSystem.TsOdinStructType;
import com.lasagnerd.odin.insights.typeSystem.TsOdinType;
import com.lasagnerd.odin.insights.typeSystem.TsOdinUnionType;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.MutablePicoContainer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * @noinspection unused, UnstableApiUsage
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
        project.registerService(TreeAspect.class, new TreeAspect());
        project.registerService(CachedValuesManager.class, new CachedValuesManagerImpl(project, new PsiCachedValuesFactory(project)));
        project.registerService(StartupManager.class, new StartupManagerImpl(project, project.getCoroutineScope()));
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
            public String getKey() {
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
            pluginDescriptor = new DefaultPluginDescriptor(PluginId.getId(getClass().getName() + "." + getName()), OdinParsingTest.class.getClassLoader());
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

    protected String getTestDataPath() {
        return PathManagerEx.getTestDataPath();
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

    protected void doTest(boolean checkResult) {
        doTest(checkResult, false);
    }

    protected void doTest(boolean checkResult, @SuppressWarnings("SameParameterValue") boolean ensureNoErrorElements) {
        String name = getTestName();
        try {
            parseFile(name, loadFile(name + "." + myFileExt));
            if (checkResult) {
                checkResult(name, myFile);
                if (ensureNoErrorElements) {
                    ensureNoErrors(myFile);
                }
            } else {
                toParseTreeText(myFile, skipSpaces(), includeRanges());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected PsiFile parseFile(String name, String text) {
        myFile = createPsiFile(name, text);
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

    private static void doSanityChecks(PsiFile root) {
        assertEquals("psi text mismatch", root.getViewProvider().getContents().toString(), root.getText());
        ensureParsed(root);
        ensureCorrectReparse(root);
        checkRangeConsistency(root);
    }

    private static void checkRangeConsistency(PsiFile file) {
        file.accept(new PsiRecursiveElementWalkingVisitor() {
            @Override
            public void visitElement(@NotNull PsiElement element) {
                if (element instanceof ForeignLeafPsiElement) return;

                try {
                    ensureNodeRangeConsistency(element, file);
                } catch (Throwable e) {
                    throw new AssertionError("In " + element + " of " + element.getClass(), e);
                }
                super.visitElement(element);
            }

            private void ensureNodeRangeConsistency(PsiElement parent, PsiFile file) {
                int parentOffset = parent.getTextRange().getStartOffset();
                int childOffset = 0;
                ASTNode child = parent.getNode().getFirstChildNode();
                if (child != null) {
                    while (child != null) {
                        int childLength = checkChildRangeConsistency(file, parentOffset, childOffset, child);
                        childOffset += childLength;
                        child = child.getTreeNext();
                    }
                    assertEquals(childOffset, parent.getTextLength());
                }
            }

            private int checkChildRangeConsistency(PsiFile file, int parentOffset, int childOffset, ASTNode child) {
                assertEquals(child.getStartOffsetInParent(), childOffset);
                assertEquals(child.getStartOffset(), childOffset + parentOffset);
                int childLength = child.getTextLength();
                assertEquals(TextRange.from(childOffset + parentOffset, childLength), child.getTextRange());
                if (!(child.getPsi() instanceof ForeignLeafPsiElement)) {
                    assertEquals(child.getTextRange().substring(file.getText()), child.getText());
                }
                return childLength;
            }
        });
    }

    protected void doTest(String suffix) throws IOException {
        String name = getTestName();
        String text = loadFile(name + "." + myFileExt);
        myFile = createPsiFile(name, text);
        ensureParsed(myFile);
        assertEquals(text, myFile.getText());
        checkResult(name + suffix, myFile);
    }

    protected void doCodeTest(@NotNull String code) throws IOException {
        String name = getTestName();
        myFile = createPsiFile("a", code);
        ensureParsed(myFile);
        assertEquals(code, myFile.getText());
        checkResult(myFilePrefix + name, myFile);
    }

    protected void doReparseTest(String textBefore, String textAfter) {
        var file = createFile("test." + myFileExt, textBefore);
        var fileAfter = createFile("test." + myFileExt, textAfter);

        var rangeStart = StringUtil.commonPrefixLength(textBefore, textAfter);
        var rangeEnd = textBefore.length() - StringUtil.commonSuffixLength(textBefore, textAfter);

        var range = new TextRange(Math.min(rangeStart, rangeEnd), Math.max(rangeStart, rangeEnd));

        var psiToStringDefault = DebugUtil.psiToString(fileAfter, true);
        DebugUtil.performPsiModification("ensureCorrectReparse", () -> {
            new BlockSupportImpl().reparseRange(
                    file,
                    file.getNode(),
                    range,
                    fileAfter.getText(),
                    new EmptyProgressIndicator(),
                    file.getText()
            ).performActualPsiChange(file);
        });
        assertEquals(psiToStringDefault, DebugUtil.psiToString(file, true));
    }

    protected PsiFile createPsiFile(@NotNull String name, @NotNull String text) {
        return createFile(name + "." + myFileExt, text);
    }

    protected PsiFile createFile(@NotNull String name, @NotNull String text) {
        LightVirtualFile virtualFile = new LightVirtualFile(name, myLanguage, text);
        virtualFile.setCharset(StandardCharsets.UTF_8);
        return createFile(virtualFile);
    }

    protected PsiFile createFile(@NotNull LightVirtualFile virtualFile) {
        return myFileFactory.trySetupPsiForFile(virtualFile, myLanguage, true, false);
    }

    protected void checkResult(@NotNull @TestDataFile String targetDataName, @NotNull PsiFile file) throws IOException {
        doCheckResult(myFullDataPath, file, checkAllPsiRoots(), targetDataName, skipSpaces(), includeRanges(), allTreesInSingleFile());
        if (SystemProperties.getBooleanProperty("dumpAstTypeNames", false)) {
            printAstTypeNamesTree(targetDataName, file);
        }
    }


    private void printAstTypeNamesTree(@NotNull @TestDataFile String targetDataName, @NotNull PsiFile file) {
        StringBuffer buffer = new StringBuffer();
        Arrays.stream(file.getNode().getChildren(TokenSet.ANY)).forEach(it -> printAstTypeNamesTree(it, buffer, 0));
        try {
            Files.writeString(Paths.get(myFullDataPath, targetDataName + ".fleet.txt"), buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printAstTypeNamesTree(ASTNode node, StringBuffer buffer, int indent) {
        buffer.append(" ".repeat(indent));
        buffer.append(node.getElementType()).append("\n");
        indent += 2;
        ASTNode childNode = node.getFirstChildNode();

        while (childNode != null) {
            printAstTypeNamesTree(childNode, buffer, indent);
            childNode = childNode.getTreeNext();
        }
    }

    protected boolean allTreesInSingleFile() {
        return false;
    }

    public static void doCheckResult(@NotNull String testDataDir,
                                     @NotNull PsiFile file,
                                     boolean checkAllPsiRoots,
                                     @NotNull String targetDataName,
                                     boolean skipSpaces,
                                     boolean printRanges) {
        doCheckResult(testDataDir, file, checkAllPsiRoots, targetDataName, skipSpaces, printRanges, false);
    }

    public static void doCheckResult(@NotNull String testDataDir,
                                     @NotNull PsiFile file,
                                     boolean checkAllPsiRoots,
                                     @NotNull String targetDataName,
                                     boolean skipSpaces,
                                     boolean printRanges,
                                     boolean allTreesInSingleFile) {
        FileViewProvider provider = file.getViewProvider();
        Set<Language> languages = provider.getLanguages();

        if (!checkAllPsiRoots || languages.size() == 1) {
            doCheckResult(testDataDir, targetDataName + ".txt", toParseTreeText(file, skipSpaces, printRanges).trim());
            return;
        }

        if (allTreesInSingleFile) {
            String expectedName = targetDataName + ".txt";
            StringBuilder sb = new StringBuilder();
            List<Language> languagesList = new ArrayList<>(languages);
            ContainerUtil.sort(languagesList, Comparator.comparing(Language::getID));
            for (Language language : languagesList) {
                sb.append("Subtree: ").append(language.getDisplayName()).append(" (").append(language.getID()).append(")").append("\n")
                        .append(toParseTreeText(provider.getPsi(language), skipSpaces, printRanges).trim())
                        .append("\n").append(StringUtil.repeat("-", 80)).append("\n");
            }
            doCheckResult(testDataDir, expectedName, sb.toString());
        } else {
            for (Language language : languages) {
                PsiFile root = provider.getPsi(language);
                assertNotNull("FileViewProvider " + provider + " didn't return PSI root for language " + language.getID(), root);
                String expectedName = targetDataName + "." + language.getID() + ".txt";
                doCheckResult(testDataDir, expectedName, toParseTreeText(root, skipSpaces, printRanges).trim());
            }
        }
    }

    protected void checkResult(@NotNull String actual) {
        String name = getTestName();
        doCheckResult(myFullDataPath, myFilePrefix + name + ".txt", actual);
    }

    protected void checkResult(@NotNull @TestDataFile String targetDataName, @NotNull String actual) {
        doCheckResult(myFullDataPath, targetDataName, actual);
    }

    public static void doCheckResult(@NotNull String fullPath, @NotNull String targetDataName, @NotNull String actual) {
        String expectedFileName = fullPath + File.separatorChar + targetDataName;
        UsefulTestCase.assertSameLinesWithFile(expectedFileName, actual);
    }

    protected static String toParseTreeText(@NotNull PsiElement file, boolean skipSpaces, boolean printRanges) {
        return DebugUtil.psiToString(file, !skipSpaces, printRanges);
    }

    protected String loadFile(@NotNull @TestDataFile String name) throws IOException {
        return loadFileDefault(myFullDataPath, name);
    }

    public static String loadFileDefault(@NotNull String dir, @NotNull String name) throws IOException {
        return FileUtil.loadFile(new File(dir, name), CharsetToolkit.UTF8, true).trim();
    }

    public static void ensureParsed(@NotNull PsiFile file) {
        file.accept(new PsiElementVisitor() {
            @Override
            public void visitElement(@NotNull PsiElement element) {
                element.acceptChildren(this);
            }
        });
    }

    public static void ensureCorrectReparse(@NotNull final PsiFile file) {
        final String psiToStringDefault = DebugUtil.psiToString(file, true, false);

        DebugUtil.performPsiModification("ensureCorrectReparse", () -> {
            final String fileText = file.getText();
            final DiffLog diffLog = new BlockSupportImpl().reparseRange(
                    file, file.getNode(), TextRange.allOf(fileText), fileText, new EmptyProgressIndicator(), fileText);
            diffLog.performActualPsiChange(file);
        });

        assertEquals(psiToStringDefault, DebugUtil.psiToString(file, true, false));
    }

    public void registerMockInjectedLanguageManager() {
        registerExtensionPoint(project.getExtensionArea(), MultiHostInjector.MULTIHOST_INJECTOR_EP_NAME, MultiHostInjector.class);

        registerExtensionPoint(app.getExtensionArea(), LanguageInjector.EXTENSION_POINT_NAME, LanguageInjector.class);
        project.registerService(DumbService.class, new MockDumbService(project));
//        getApplication().registerService(EditorWindow.class, new EditorWindowTrackerImpl());
        project.registerService(InjectedLanguageManager.class, new InjectedLanguageManagerImpl(project));
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


    protected OdinFile load(String path) throws IOException {
        String fileContent = FileUtil.loadFile(new File(path), CharsetToolkit.UTF8, true).trim();
        return ((OdinFile) parseFile("my_file", fileContent));
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

    public void testSimpleFile() {
        String path = "src/test/testData/simple.odin";
        loadAndCheck(path);
    }

    public void testDemoFile() {
        String path = "src/test/testData/demo.odin";
        loadAndCheck(path);
    }

    public void testTypeInference() throws IOException {
        OdinFile odinFile = load("src/test/testData/type_inference.odin");
        var refExpressions = PsiTreeUtil.findChildrenOfType(odinFile, OdinRefExpression.class);
        Objects.requireNonNull(refExpressions);
        OdinRefExpression odinRefExpression = refExpressions.stream().filter(e -> e.getText().contains("weapon")).findFirst().orElseThrow();

        OdinScope scope = OdinInsightUtils.findScope(odinRefExpression);
        OdinReferenceResolver.resolve(scope, odinRefExpression);

    }

    public void testPolymorphicTypes() throws IOException {
        OdinFile odinFile = load("src/test/testData/type_inference.odin");
        Collection<OdinProcedureDeclarationStatement> procedureDeclarationStatements = PsiTreeUtil.findChildrenOfType(odinFile.getFileScope(), OdinProcedureDeclarationStatement.class);

        {
            OdinTypeInferenceResult odinTypeInferenceResult = inferTypeOfFirstExpressionInProcedure(odinFile, "testTypeInference");

            assertNotNull(odinTypeInferenceResult.getType());
            assertEquals(odinTypeInferenceResult.getType().getName(), "Point");
        }
    }

    public void testPolymorphicTypesWithMultipleParams() throws IOException {
        OdinFile odinFile = load("src/test/testData/type_inference.odin");

        OdinTypeInferenceResult odinTypeInferenceResult = inferTypeOfFirstExpressionInProcedure(odinFile, "testTypeInference2");

        assertNotNull(odinTypeInferenceResult.getType());
        assertEquals("Point", odinTypeInferenceResult.getType().getName());
    }

    public void testPolymorphicTypesWithMultipleAndNestedParams() throws IOException {
        OdinFile odinFile = load("src/test/testData/type_inference.odin");
        OdinTypeInferenceResult odinTypeInferenceResult = inferTypeOfFirstExpressionInProcedure(odinFile, "testTypeInference3");

        assertNotNull(odinTypeInferenceResult.getType());
        assertEquals("Point", odinTypeInferenceResult.getType().getName());
    }

    public void testPolymorphicTypesWithPolymorphicReturnType() throws IOException {
        OdinFile odinFile = load("src/test/testData/type_inference.odin");
        OdinTypeInferenceResult odinTypeInferenceResult = inferTypeOfFirstExpressionInProcedure(odinFile, "testTypeInference4");

        assertNotNull(odinTypeInferenceResult.getType());
        assertInstanceOf(odinTypeInferenceResult.getType(), TsOdinStructType.class);

        TsOdinStructType structType = (TsOdinStructType)odinTypeInferenceResult.getType();
        assertNotEmpty(structType.getFields().values());
        assertTrue(structType.getFields().containsKey("items"));
        assertInstanceOf(structType.getFields().get("items"), TsOdinArrayType.class);
        TsOdinArrayType fieldType = (TsOdinArrayType) structType.getFields().get("items");
        assertInstanceOf(fieldType.getElementType(), TsOdinStructType.class);
        TsOdinStructType elementType = (TsOdinStructType) fieldType.getElementType();
        assertEquals("Point", elementType.getName());
    }

    public void testPolymorphicTypesWithPolymorphicReturn_typeReferenceOnStructField() throws IOException {
        OdinFile odinFile = load("src/test/testData/type_inference.odin");
        OdinTypeInferenceResult odinTypeInferenceResult = inferTypeOfFirstExpressionInProcedure(odinFile, "testTypeInference5");
        System.out.println(odinTypeInferenceResult);
    }

    public void testDeclaredIdentifiersInProcedureBlock() throws IOException {
        OdinFile odinFile = load("src/test/testData/scope_resolution.odin");
        @NotNull OdinProcedureDeclarationStatement procToSearchFrom = findFirstProcedure(odinFile, "proc_to_search_from");
        OdinCallExpression callExpression = PsiTreeUtil.findChildOfType(procToSearchFrom, OdinCallExpression.class);
        assertNotNull(callExpression);
        OdinRefExpression expression = (OdinRefExpression) callExpression.getExpression();

        PsiReference reference = expression.getIdentifier().getReference();
        assertNotNull(reference);
        PsiElement resolvedReference = reference.resolve();
        assertInstanceOf(resolvedReference, OdinDeclaredIdentifier.class);
        OdinDeclaredIdentifier declaredIdentifier = (OdinDeclaredIdentifier) resolvedReference;
        assertEquals("proc_to_find", declaredIdentifier.getIdentifierToken().getText());

    }
    private static @NotNull OdinTypeInferenceResult inferTypeOfFirstExpressionInProcedure(OdinFile odinFile, String procedureName) {
        OdinProcedureDeclarationStatement testTypeInference = findFirstProcedure(odinFile, procedureName);
        OdinExpressionStatement odinExpressionStatement = (OdinExpressionStatement) testTypeInference.getBlockStatements().stream().filter(s -> s instanceof OdinExpressionStatement)
                .findFirst().orElseThrow();

        OdinExpression expression = odinExpressionStatement.getExpression();
        OdinScope scope = OdinInsightUtils.findScope(expression);
        return OdinInferenceEngine.inferType(scope, expression);
    }

    public void testPolymorphicTypesWithMultipleReturnTypes() throws IOException {
        OdinFile odinFile = load("src/test/testData/type_inference.odin");
        var pointVariable = findFirstVariableDeclarationStatement(odinFile, "testTypeInference6", "point");
        assertNotEmpty(pointVariable.getExpressionsList().getExpressionList());
        OdinExpression odinExpression = pointVariable.getExpressionsList().getExpressionList().get(0);
        TsOdinType tsOdinType = OdinInferenceEngine.doInferType(odinExpression);
        assertInstanceOf(tsOdinType, TsOdinStructType.class);
        assertEquals("Point", tsOdinType.getName());
    }

    public void testUnionType() throws IOException {
        OdinFile odinFile = load("src/test/testData/type_inference.odin");
        var shapeVariable = findFirstVariableDeclarationStatement(odinFile, "testTypeInference7", "shape");
        OdinExpression odinExpression = shapeVariable.getExpressionsList().getExpressionList().get(0);
        TsOdinType tsOdinType = OdinInferenceEngine.doInferType(odinExpression);
        assertInstanceOf(tsOdinType, TsOdinUnionType.class);
        assertEquals("Shape", tsOdinType.getName());
    }

    public void testPolyUnionType()throws IOException {
        OdinFile odinFile = load("src/test/testData/type_inference.odin");
        var shapeVariable = findFirstVariableDeclarationStatement(odinFile, "testTypeInference8", "first_shape");
        OdinExpression odinExpression = shapeVariable.getExpressionsList().getExpressionList().get(0);
        TsOdinType tsOdinType = OdinInferenceEngine.doInferType(odinExpression);
        assertInstanceOf(tsOdinType, TsOdinStructType.class);
        assertEquals("Line", tsOdinType.getName());
    }

    private static @NotNull OdinProcedureDeclarationStatement findFirstProcedure(OdinFile odinFile, String procedureName) {
        Collection<OdinProcedureDeclarationStatement> procedureDeclarationStatements = PsiTreeUtil.findChildrenOfType(odinFile.getFileScope(),
                OdinProcedureDeclarationStatement.class);

        return procedureDeclarationStatements.stream()
                .filter(p -> Objects.equals(p.getDeclaredIdentifier().getName(), procedureName))
                .findFirst().orElseThrow();
    }

    private static @NotNull OdinVariableInitializationStatement findFirstVariableDeclarationStatement(OdinFile odinFile, String procedureName, String variableName) {
        OdinProcedureDeclarationStatement procedure = findFirstProcedure(odinFile, procedureName);
        assertNotNull(procedure);
        List<OdinStatement> statements = procedure.getBlockStatements();
        OdinVariableInitializationStatement variable = (OdinVariableInitializationStatement) statements.stream()
                .filter(s -> s instanceof OdinVariableInitializationStatement)
                .filter(v -> ((OdinVariableInitializationStatement) v).getDeclaredIdentifiers().stream().anyMatch(d -> Objects.equals(d.getName(), variableName)))
                .findFirst().orElse(null);
        assertNotNull(variable);

        return variable;
    }

    public void testRefSolver() throws IOException {
        OdinFile odinFile = load("src/test/testData/type_inference.odin");
        Collection<OdinRefExpression> refExpressions = PsiTreeUtil.findChildrenOfType(odinFile, OdinRefExpression.class);

    }


}