package com.lasagnerd.odin.codeInsight.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ProcessingContext;
import com.lasagnerd.odin.codeInsight.imports.OdinImportUtils;
import com.lasagnerd.odin.lang.psi.OdinImportPath;
import com.lasagnerd.odin.projectStructure.OdinRootTypeUtils;
import com.lasagnerd.odin.projectStructure.collection.OdinRootTypeResult;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Stack;

public class OdinImportPathCompletionProvider extends CompletionProvider<CompletionParameters> {
    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
        if (!(parameters.getOriginalPosition() != null && parameters.getOriginalPosition().getParent() instanceof OdinImportPath importPath))
            return;
        PsiFile originalFile = parameters.getOriginalFile();
        String filePath = originalFile.getVirtualFile().getPath();

        Project project = importPath.getProject();
        int offset = parameters.getOffset();
        PsiElement originalPosition = parameters.getOriginalPosition();

        int textOffset = originalPosition.getTextOffset();
        int relativeOffset = offset - textOffset;

        String prefix = "";
        if (relativeOffset > 1) {
            prefix = originalPosition.getText().substring(1, relativeOffset);
        }

        result = result.withPrefixMatcher(prefix);

        Map<String, Path> sdkCollectionPaths = OdinImportUtils.getSdkCollections(project);
        Map<String, Path> collectionPaths = OdinImportUtils.getCollectionPaths(project, filePath);
        PsiDirectory directory = originalFile.getParent();

        addCollectionPaths(project, result, sdkCollectionPaths);
        addCollectionPaths(project, result, collectionPaths);

        if (directory != null) {
            if (prefix.startsWith("./")) {
                addResultsRecursively(result,
                        directory,
                        Path.of(directory.getVirtualFile().getPath()),
                        null,
                        "./");
            }
            addResultsRecursively(result, directory, null);
            if (prefix.startsWith("..")) {
                OdinRootTypeResult sourceRoot = OdinRootTypeUtils.findContainingRoot(project, originalFile.getVirtualFile());
                if (sourceRoot != null) {
                    VirtualFile sourceRootDirectory = sourceRoot.directory();
                    PsiDirectory parentDirectory = directory.getParent();
                    if (parentDirectory != null) {
                        if (sourceRootDirectory != null) {
                            Path sourceRootPath = Path.of(sourceRootDirectory.getPath());
                            addResultsRecursivelyUp(
                                    result,
                                    parentDirectory,
                                    Path.of(directory.getVirtualFile().getPath()),
                                    sourceRootPath
                            );
                        }
                    }
                }
            }
        }

    }

    private void addCollectionPaths(Project project, @NotNull CompletionResultSet result, Map<String, Path> sdkCollectionPaths) {
        for (Map.Entry<String, Path> entry : sdkCollectionPaths.entrySet()) {
            String collection = entry.getKey();
            Path rootPath = entry.getValue();

            PsiDirectory psiDirectory = OdinImportUtils.findPsiDirectory(project, rootPath);
            if (psiDirectory != null) {
                addResultsRecursively(result, psiDirectory, collection);
            }
        }
    }

    private static void addResultsRecursively(@NotNull CompletionResultSet result,
                                              PsiDirectory parentDirectory,
                                              String collection) {
        addResultsRecursively(result, parentDirectory, Path.of(parentDirectory.getVirtualFile().getPath()), collection);
    }

    private static void addResultsRecursively(@NotNull CompletionResultSet result, PsiDirectory parentDirectory, Path root, String collection) {
        addResultsRecursively(result, parentDirectory, root, collection, null);
    }

    private static void addResultsRecursively(@NotNull CompletionResultSet result,
                                              PsiDirectory parentDirectory,
                                              Path root,
                                              String collection,
                                              String prefix) {
        Stack<PsiDirectory> stack = new Stack<>();
        Collections.addAll(stack, parentDirectory.getSubdirectories());

        doAddResultsRecursively(result, stack, root, collection, prefix);
    }


    private static void doAddResultsRecursively(@NotNull CompletionResultSet result,
                                                Stack<PsiDirectory> stack,
                                                Path importRootPath,
                                                String collection,
                                                String prefix) {
        for (PsiDirectory current; !stack.empty(); ) {
            current = stack.pop();

            String path = current.getVirtualFile().getPath();
            Path relativePath = importRootPath.relativize(Path.of(path));
            String lookupString = FileUtil.toSystemIndependentName(relativePath.toString());
            if (collection != null)
                lookupString = collection + ":" + lookupString;

            if (prefix != null)
                lookupString = prefix + lookupString;
            LookupElementBuilder lookupElement = LookupElementBuilder
                    .create(lookupString)
                    .withIcon(AllIcons.Nodes.Package);

            result.addElement(lookupElement);
            Collections.addAll(stack, current.getSubdirectories());
        }
    }

    private static void addResultsRecursivelyUp(CompletionResultSet result,
                                                PsiDirectory startDirectory,
                                                Path root,
                                                Path stop) {
        Path startPath = Path.of(startDirectory.getVirtualFile().getPath());
        if (!startPath.startsWith(stop))
            return;

        Stack<PsiDirectory> stack = new Stack<>();
        stack.add(startDirectory);

        int depth = 0;
        for (PsiDirectory current; !stack.empty(); ) {
            current = stack.pop();
            for (PsiDirectory subdir : current.getSubdirectories()) {
                Path subdirPath = Path.of(subdir.getVirtualFile().getPath());
                System.out.println("- " + subdirPath);
                Path relativePath = root.relativize(subdirPath);
                String lookupString = FileUtil.toSystemIndependentName(relativePath.toString());
                if (lookupString.isBlank()) {
                    continue;
                }

                if (lookupString.equals("..")) {
                    lookupString = "../../" + subdir.getName();
                }
                LookupElementBuilder lookupElement = LookupElementBuilder
                        .create(lookupString)
                        .withIcon(AllIcons.Nodes.Package);

                result.addElement(PrioritizedLookupElement.withPriority(lookupElement, -depth++));
            }
            PsiDirectory parent = current.getParent();
            if (parent != null) {
                Path parentPath = Path.of(parent.getVirtualFile().getPath());
                if (parentPath.startsWith(stop)) {
                    PsiDirectory psiDirectory = OdinImportUtils.findPsiDirectory(current.getProject(), parentPath);
                    if (psiDirectory != null) {
                        stack.add(psiDirectory);
                    }
                }
            }
        }
    }
}
