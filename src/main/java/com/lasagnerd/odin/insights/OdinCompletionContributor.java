package com.lasagnerd.odin.insights;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.ExpUiIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.lasagnerd.odin.OdinIcons;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.nio.file.Path;
import java.util.*;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class OdinCompletionContributor extends CompletionContributor {

    public static final PsiElementPattern.@NotNull Capture<PsiElement> REFERENCE = psiElement().withElementType(OdinTypes.IDENTIFIER_TOKEN).afterLeaf(".");

    public static final @NotNull ElementPattern<PsiElement> AT_IDENTIFIER = psiElement().withElementType(OdinTypes.IDENTIFIER_TOKEN).andNot(REFERENCE);

    record ImportInfo(String name, String path, String library) {
    }

    public OdinCompletionContributor() {

        extend(CompletionType.BASIC,
                REFERENCE,
                new CompletionProvider<>() {

                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters,
                                                  @NotNull ProcessingContext context,
                                                  @NotNull CompletionResultSet result) {
                        System.out.println("hello from reference completion");

                        Project project = parameters.getPosition().getProject();
                        PsiElement position = parameters.getPosition().getParent();

                        OdinFile odinFile = (OdinFile) position.getContainingFile();
                        OdinFile originalFile = (OdinFile) parameters.getOriginalFile();
                        OdinFileScope fileScope = odinFile.getFileScope();

                        // Load import map
                        Map<String, ImportInfo> importMap = new HashMap<>();
                        List<OdinImportStatement> importStatements
                                = fileScope.getImportStatementList();

                        for (OdinImportStatement importStatement : importStatements) {
                            String name = importStatement.getAlias() != null
                                    ? importStatement.getAlias().getText()
                                    : null;

                            String path = importStatement.getPath().getText();
                            // Remove quotes
                            path = path.substring(1, path.length() - 1);

                            String[] parts = path.split(":");
                            String library = null;
                            if (parts.length > 1) {
                                library = parts[0];
                                path = parts[1];
                            } else {
                                path = parts[0];
                            }

                            if(name == null) {
                                // Last path segment is the name
                                String[] pathParts = path.split("/");
                                name = pathParts[pathParts.length - 1];
                            }

                            ImportInfo importInfo = new ImportInfo(name, path, library);
                            importMap.put(name, importInfo);
                        }


                        // This constitutes our scope
                        OdinRefExpression reference = (OdinRefExpression) PsiTreeUtil.findSiblingBackward(position, OdinTypes.REF_EXPRESSION, false, null);
                        if (reference != null) {

                            OdinDeclaredIdentifier identifierReference = (OdinDeclaredIdentifier) Objects.requireNonNull(reference.getIdentifier().getReference())
                                    .resolve();

                            if(identifierReference != null) {
                                OdinVariableInitializationStatement initialization = OdinInsightUtils.findFirstParentOfType(identifierReference,
                                        true,
                                        OdinVariableInitializationStatement.class);

                                OdinExpression odinExpression = initialization.getExpressionsList().getExpressionList().get(0);
                                OdinCompoundLiteral compoundLiteral = PsiTreeUtil.findChildOfType(odinExpression, OdinCompoundLiteral.class);

                                findCompletionsForStruct(result, compoundLiteral);
                            }

                            // Check if reference is an import
                            String importName = reference.getIdentifier().getText();
                            ImportInfo importInfo = importMap.get(importName);
                            if (importInfo != null) {
                                Sdk projectSdk = ProjectRootManager.getInstance(project).getProjectSdk();

                                Path sdkSourceDir = Path.of(projectSdk.getHomeDirectory().getPath(), "core");
                                Path currentDir = Path.of(originalFile.getVirtualFile().getPath()).getParent();

                                List<Path> dirs = List.of(currentDir, sdkSourceDir);
                                for (Path dir : dirs) {
                                    var importedFiles = findImportFiles(dir, importInfo, project);
                                    for (OdinFile importedFile : importedFiles) {
                                        OdinFileScope importedFileScope = importedFile.getFileScope();
                                        List<PsiElement> fileScopeDeclarations = OdinInsightUtils.getFileScopeDeclarations(importedFileScope, e -> true);
                                        addLookUpElements(result, fileScopeDeclarations);
                                    }

                                    if(!importedFiles.isEmpty()) {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
        );

        extend(CompletionType.BASIC, AT_IDENTIFIER,
                new CompletionProvider<>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters,
                                                  @NotNull ProcessingContext context,
                                                  @NotNull CompletionResultSet result) {
                        PsiElement position = parameters.getPosition();

                        PsiFile containingFile = position.getContainingFile();

                        PsiElement parent = OdinInsightUtils.findFirstParentOfType(
                                position,
                                true,
                                OdinRefExpression.class);

                        if (parent != null) {
                            // Struct construction
                            OdinCompoundLiteral compoundLiteral
                                    = OdinInsightUtils.findFirstParentOfType(parent, true, OdinCompoundLiteral.class);

                            findCompletionsForStruct(result, compoundLiteral);

                            // Declarations in scope
                            List<PsiElement> declarations = OdinInsightUtils
                                    .findDeclarations(position, e -> true);

                            addLookUpElements(result, declarations);
                        }
                    }
                }
        );

    }

    private static List<OdinFile> findImportFiles(Path directory,
                                                  ImportInfo importInfo,
                                                  Project project) {
        Path importPath = directory.resolve(importInfo.path);
        List<OdinFile> files = new ArrayList<>();
        VirtualFile packageDirectory = VfsUtil.findFile(importPath, true);
        if (packageDirectory != null) {
            for (VirtualFile child : packageDirectory.getChildren()) {
                if (child.getName().endsWith(".odin")) {
                    PsiFile psiFile = PsiManager.getInstance(project).findFile(child);
                    if (psiFile instanceof OdinFile odinFile) {
                        files.add(odinFile);
                    }
                }
            }
        }
        return files;
    }

    private static void addLookUpElements(@NotNull CompletionResultSet result, List<PsiElement> declarations) {
        for (PsiElement declaration : declarations) {
            if (declaration instanceof PsiNameIdentifierOwner declaredIdentifier) {
                Icon icon = switch (OdinInsightUtils.classify(declaredIdentifier)) {
                    case STRUCT -> OdinIcons.Types.Struct;
                    case ENUM -> ExpUiIcons.Nodes.Enum;
                    case UNION -> OdinIcons.Types.Union;
                    case PROCEDURE -> ExpUiIcons.Nodes.Function;
                    case VARIABLE -> ExpUiIcons.Nodes.Variable;
                    case CONSTANT -> ExpUiIcons.Nodes.Constant;
                    case UNKNOWN -> ExpUiIcons.FileTypes.Unknown;
                };

                LookupElementBuilder element = LookupElementBuilder.create(declaredIdentifier).withIcon(icon);
                result.addElement(PrioritizedLookupElement.withPriority(element, 0));
            }
        }
    }

    private static void findCompletionsForStruct(@NotNull CompletionResultSet result, OdinCompoundLiteral compoundLiteral) {
        if (compoundLiteral == null || !(compoundLiteral.getType() instanceof OdinConcreteType concreteType)) {
            return;
        }

        var identifierExpressionList = concreteType.getIdentifierList();
        var identifier = identifierExpressionList.get(0);
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
            String typeOfField = fieldDeclaration.getTypeDefinition().getText();
            for (OdinDeclaredIdentifier declaredIdentifier : fieldDeclaration.getDeclaredIdentifierList()) {
                LookupElementBuilder element = LookupElementBuilder.create((PsiNameIdentifierOwner) declaredIdentifier)
                        .withIcon(ExpUiIcons.Nodes.Property)
                        .withBoldness(true)
                        .withTypeText(typeOfField)
                        .withTailText(" -> " + structName);

                result.addElement(PrioritizedLookupElement.withPriority(element, 100));
            }
        }
    }
}
