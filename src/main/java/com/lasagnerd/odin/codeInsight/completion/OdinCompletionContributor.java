package com.lasagnerd.odin.codeInsight.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.ExpUiIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.lasagnerd.odin.OdinIcons;
import com.lasagnerd.odin.codeInsight.*;
import com.lasagnerd.odin.lang.OdinFileType;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class OdinCompletionContributor extends CompletionContributor {


    public static final PsiElementPattern.@NotNull Capture<PsiElement> REFERENCE = psiElement().withElementType(OdinTypes.IDENTIFIER_TOKEN).afterLeaf(".");

    public static final @NotNull ElementPattern<PsiElement> AT_IDENTIFIER = psiElement()
            .withElementType(OdinTypes.IDENTIFIER_TOKEN)
            .andNot(REFERENCE);

    public OdinCompletionContributor() {

        // REFERENCE completion
        extend(CompletionType.BASIC,
                REFERENCE,
                new CompletionProvider<>() {

                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters,
                                                  @NotNull ProcessingContext context,
                                                  @NotNull CompletionResultSet result) {

                        // Walk up tree until no more ref expressions are found
                        PsiElement position = parameters.getPosition();
                        PsiElement parent = PsiTreeUtil.findFirstParent(position, e -> e instanceof OdinRefExpression);

                        // This constitutes our scope
                        if (parent instanceof OdinRefExpression reference) {
                            OdinScope scope = createScope(parameters, reference);

                            if (reference.getExpression() != null) {
                                // TODO at some point we should return the type of each symbol
                                OdinScope completionScope = OdinReferenceResolver.resolve(scope, reference.getExpression());
                                if (completionScope != null) {
                                    addLookUpElements(result, completionScope.getNamedElements());
                                }
                            }
                        }

                        OdinType parentType = PsiTreeUtil.getParentOfType(position, true, OdinType.class);
                        if (parentType instanceof OdinSimpleRefType) {
                            OdinScope scope = createScope(parameters, parentType);
                            OdinScope completionScope = OdinReferenceResolver.resolve(scope, parentType);
                            if (completionScope != null) {
                                addLookUpElements(result, completionScope.getNamedElements());
                            }
                        }
                    }
                }
        );

        // Basic Completion
        extend(CompletionType.BASIC, AT_IDENTIFIER,
                new CompletionProvider<>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters,
                                                  @NotNull ProcessingContext context,
                                                  @NotNull CompletionResultSet result) {
                        PsiElement position = parameters.getPosition();


                        // TODO This is probably where we will need stub indices / file based indices

                        // 1. Add symbols from all visible stuff in my project
                        OdinFile thisOdinFile = (OdinFile) parameters.getOriginalFile();
                        VirtualFile thisFile = thisOdinFile.getVirtualFile();
                        String thisPackagePath = thisFile.getParent().getPath();
                        Project project = thisOdinFile.getProject();

                        ProjectFileIndex projectFileIndex = ProjectFileIndex.getInstance(project);
                        VirtualFile sourceRoot = projectFileIndex.getSourceRootForFile(thisFile);

                        if (sourceRoot != null) {
                            // Recursively walk through all dirs starting from source root
                            Stack<VirtualFile> work = new Stack<>();
                            Map<String, List<OdinFile>> packages = new HashMap<>();
                            work.add(sourceRoot);

                            do {
                                VirtualFile currentFile = work.pop();
                                if (currentFile.isDirectory()) {
                                    Collections.addAll(work, currentFile.getChildren());
                                } else {
                                    if (currentFile.getFileType() == OdinFileType.INSTANCE) {
                                        String packagePath = currentFile.getParent().getPath();
                                        OdinFile odinFile = (OdinFile) PsiManager.getInstance(project).findFile(currentFile);

                                        if (!packagePath.isBlank() && !packagePath.equals(sourceRoot.getPath())) {
                                            packages.computeIfAbsent(packagePath, k -> new ArrayList<>()).add(odinFile);
                                        }
                                    }
                                }
                            } while (!work.isEmpty());

                            // packages now contains all packages (recursively) under source root
                            // 1. add all the symbols to the lookup elements whilst taking into consideration
                            //  their origin package. Upon accepting a suggestion insert the import statement
                            // if not already present

                            for (Map.Entry<String, List<OdinFile>> entry : packages.entrySet()) {
                                String packagePath = entry.getKey();
                                List<OdinFile> files = entry.getValue();
                                for (OdinFile file : files) {
                                    if (file.getFileScope() == null)
                                        continue;
                                    OdinSymbol.OdinVisibility globalFileVisibility = OdinScopeResolver.getGlobalFileVisibility(file.getFileScope());
                                    OdinScope fileScopeDeclarations = OdinScopeResolver.getFileScopeDeclarations(file.getFileScope(),
                                            globalFileVisibility);

                                    List<PsiNamedElement> visibleSymbols = fileScopeDeclarations
                                            .getSymbols(OdinSymbol.OdinVisibility.PUBLIC)
                                            .stream()
                                            .map(OdinSymbol::getDeclaredIdentifier)
                                            .collect(Collectors.toList());

                                    addLookUpElements(thisOdinFile,
                                            thisPackagePath,
                                            packagePath,
                                            result,
                                            visibleSymbols);
                                }
                            }
                        }

                        // 2. Add symbols from SDK (core, vendor, etc.)

                        // 3. Add symbols from local scope
                        OdinScope localScope = OdinScopeResolver.resolveScope(position, e -> true);
                        addLookUpElements(result, localScope.getNamedElements());
                    }
                }
        );

    }

    private static OdinScope createScope(@NotNull CompletionParameters parameters, PsiElement reference) {
        return OdinScopeResolver.resolveScope(reference, e -> true).with(parameters
                .getOriginalFile()
                .getContainingDirectory()
                .getVirtualFile()
                .getPath());
    }

    private static void addLookUpElements(@NotNull CompletionResultSet result, Collection<PsiNamedElement> namedElements) {
        addLookUpElements(null, "", "", result, namedElements);
    }

    private static void addLookUpElements(OdinFile sourceFile, String sourcePackagePath, String targetPackagePath, @NotNull CompletionResultSet result, Collection<PsiNamedElement> namedElements) {
        String prefix = "";
        if (targetPackagePath != null && !targetPackagePath.isBlank()) {
            // Get last segment of path
            prefix = Path.of(targetPackagePath).getFileName().toString() + ".";
        }

        for (var namedElement : namedElements) {
            if (namedElement instanceof PsiNameIdentifierOwner declaredIdentifier) {
                OdinTypeType typeType = OdinInsightUtils.classify(declaredIdentifier);
                Icon icon = getIcon(typeType);

                final String lookupString = declaredIdentifier.getText();

                switch (typeType) {
                    case PROCEDURE -> {
                        LookupElementBuilder element = LookupElementBuilder
                                .create(lookupString)
                                .withIcon(icon);

                        OdinProcedureDeclarationStatement firstParentOfType = PsiTreeUtil.getParentOfType(declaredIdentifier, true, OdinProcedureDeclarationStatement.class);
                        if (firstParentOfType != null) {
                            element = procedureLookupElement(element, firstParentOfType)
                                    .withInsertHandler(
                                            new CombinedInsertHandler(
                                                    new OdinInsertSymbolHandler(typeType, prefix),
                                                    new OdinInsertImportHandler(sourcePackagePath, targetPackagePath, sourceFile)
                                            )
                                    );
                            result.addElement(PrioritizedLookupElement.withPriority(element, 0));
                        }
                    }
                    case PROCEDURE_OVERLOAD -> {
                        OdinProcedureOverloadDeclarationStatement procedureOverloadStatement = PsiTreeUtil.getParentOfType(declaredIdentifier, true, OdinProcedureOverloadDeclarationStatement.class);
                        if (procedureOverloadStatement == null)
                            break;

                        for (OdinIdentifier odinIdentifier : procedureOverloadStatement.getIdentifierList()) {
                            var resolvedReference = odinIdentifier.getReference();

                            if (resolvedReference != null) {
                                PsiElement resolved = resolvedReference.resolve();
                                if (resolved instanceof OdinDeclaredIdentifier) {
                                    OdinProcedureDeclarationStatement declaringProcedure = OdinInsightUtils.getDeclaringProcedure((OdinDeclaredIdentifier) resolved);
                                    if (declaringProcedure != null) {
                                        LookupElementBuilder element = LookupElementBuilder.create(resolved, lookupString)
                                                .withItemTextItalic(true)
                                                .withIcon(icon)
                                                .withInsertHandler(
                                                        new CombinedInsertHandler(
                                                                new OdinInsertSymbolHandler(typeType, prefix),
                                                                new OdinInsertImportHandler(sourcePackagePath, targetPackagePath, sourceFile)
                                                        )
                                                );
                                        element = procedureLookupElement(element, declaringProcedure);
                                        result.addElement(PrioritizedLookupElement.withPriority(element, 0));
                                    }
                                }
                            }
                        }
                    }
                    case PACKAGE -> {
                        OdinImportDeclarationStatement odinDeclaration = PsiTreeUtil.getParentOfType(declaredIdentifier, false, OdinImportDeclarationStatement.class);
                        if (odinDeclaration != null) {
                            OdinImportInfo info = odinDeclaration.getImportInfo();

                            LookupElementBuilder element = LookupElementBuilder.create(info.packageName())
                                    .withIcon(ExpUiIcons.Nodes.Package)
                                    .withTypeText(info.path());

                            if (info.library() != null) {
                                element = element.withTailText(" -> " + info.library());
                            }

                            result.addElement(PrioritizedLookupElement.withPriority(element, 100));
                        }
                    }
                    default -> {
                        LookupElementBuilder element = LookupElementBuilder.create(lookupString).withIcon(icon)
                                .withInsertHandler(
                                        new CombinedInsertHandler(
                                                new OdinInsertSymbolHandler(typeType, prefix),
                                                new OdinInsertImportHandler(sourcePackagePath, targetPackagePath, sourceFile)
                                        )
                                );
                        result.addElement(PrioritizedLookupElement.withPriority(element, 0));
                    }
                }
            }
        }
    }

    private static Icon getIcon(OdinTypeType typeType) {
        return switch (typeType) {
            case STRUCT -> OdinIcons.Types.Struct;
            case ENUM -> ExpUiIcons.Nodes.Enum;
            case UNION -> OdinIcons.Types.Union;
            case PROCEDURE, PROCEDURE_OVERLOAD -> ExpUiIcons.Nodes.Function;
            case VARIABLE -> ExpUiIcons.Nodes.Variable;
            case CONSTANT -> ExpUiIcons.Nodes.Constant;
            case PACKAGE -> ExpUiIcons.Nodes.Package;
            case FIELD -> ExpUiIcons.Nodes.Property;
            case PARAMETER -> ExpUiIcons.Nodes.Parameter;
            case UNKNOWN -> ExpUiIcons.FileTypes.Unknown;
        };
    }

    @NotNull
    private static LookupElementBuilder procedureLookupElement(LookupElementBuilder element, OdinProcedureDeclarationStatement declaringProcedure) {
        var params = declaringProcedure.getProcedureType().getParamEntryList();
        StringBuilder tailText = new StringBuilder("(");
        String paramList = params.stream().map(PsiElement::getText).collect(Collectors.joining(", "));
        tailText.append(paramList);
        tailText.append(")");
        element = element.withTailText(tailText.toString());

        OdinReturnParameters returnType = declaringProcedure.getProcedureType().getReturnParameters();
        if (returnType != null) {
            element = element.withTypeText(returnType.getText());
        }
        return element;
    }

    // TODO Doesn't work anymore
    private static void findCompletionsForStruct(@NotNull CompletionResultSet result, OdinCompoundLiteral compoundLiteral) {
        // Use type resolver
        if (compoundLiteral == null || !(compoundLiteral.getType() instanceof OdinQualifiedType typeRef)) {
            return;
        }

        var structTypeExpression = (OdinQualifiedType) typeRef.getType();
        var identifier = structTypeExpression.getIdentifier();
        PsiElement reference = Objects.requireNonNull(identifier.getReference()).resolve();

        if (reference == null || !(reference.getParent() instanceof OdinStructDeclarationStatement structDeclarationStatement)) {
            return;
        }

        String structName = structDeclarationStatement.getDeclaredIdentifier().getText();
        OdinStructBody structBody = structDeclarationStatement.getStructType().getStructBlock().getStructBody();
        if (structBody == null) {
            return;
        }

        List<OdinFieldDeclarationStatement> fieldDeclarationStatementList = structBody.getFieldDeclarationStatementList();

        for (OdinFieldDeclarationStatement fieldDeclaration : fieldDeclarationStatementList) {
            String typeOfField = fieldDeclaration.getType().getText();
            for (OdinDeclaredIdentifier declaredIdentifier : fieldDeclaration.getDeclaredIdentifierList()) {
                LookupElementBuilder element = LookupElementBuilder.create(declaredIdentifier)
                        .withIcon(ExpUiIcons.Nodes.Property)
                        .withBoldness(true)
                        .withTypeText(typeOfField)
                        .withTailText(" -> " + structName);

                result.addElement(PrioritizedLookupElement.withPriority(element, 100));
            }
        }
    }

}
