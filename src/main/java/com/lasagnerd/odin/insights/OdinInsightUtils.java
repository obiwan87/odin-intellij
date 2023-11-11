package com.lasagnerd.odin.insights;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Conditions;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;

public class OdinInsightUtils {
    @Nullable
    public static PsiElement findFirstDeclaration(OdinIdentifier identifier, Predicate<PsiElement> matcher) {
        PsiElement entrance = identifier;
        PsiElement lastValidBlock = identifier;

        // Check all parent blocks
        while (entrance != null) {
            OdinBlock containingBlock = (OdinBlock) PsiTreeUtil.findFirstParent(entrance,
                    true,
                    parent -> parent instanceof OdinBlock);

            entrance = containingBlock;
            if (containingBlock == null) {
                break;
            }

            lastValidBlock = containingBlock;

            OdinStatementList statementList = containingBlock.getStatementList();
            if (statementList == null) return null;
            for (OdinStatement statement : statementList.getStatementList()) {
                List<OdinDeclaredIdentifier> matchingDeclarations = getMatchingDeclarations(matcher, statement);
                if (!matchingDeclarations.isEmpty()) return matchingDeclarations.get(0);
            }
        }

        // Check file scope
        OdinFileScope odinFileScope = (OdinFileScope) PsiTreeUtil.findFirstParent(lastValidBlock, psi -> psi instanceof OdinFileScope);
        if (odinFileScope != null) {
            List<OdinDeclaredIdentifier> fileScopeDeclarations = getFileScopeDeclarations(odinFileScope);
            for (OdinDeclaredIdentifier fileScopeDeclaration : fileScopeDeclarations) {
                boolean isMatch = matcher.test(fileScopeDeclaration);
                if (isMatch) {
                    return fileScopeDeclaration;
                }
            }
        }

        // TODO this only works with x.y and serves just as a proof of concept
        // Check imported files
        if(identifier != null) {
            QualifiedName qualifiedName = OdinInsightUtils.getQualifiedName(identifier);
            if(qualifiedName == null) {
                return null;
            }

            String[] parts = qualifiedName.name().split("\\.");
            if(parts.length > 1) {
                String importName = parts[0];
                VirtualFile virtualFile = identifier.getContainingFile().getViewProvider().getVirtualFile();
                List<OdinDeclaredIdentifier> declarations = findDeclarationsInImports(virtualFile.getPath(),
                        odinFileScope,
                        importName,
                        identifier.getProject());

                String name = parts[1];
                for (OdinDeclaredIdentifier declaration : declarations) {
                    String text = declaration.getText();
                    if(text.equals(name)) {
                        return declaration;
                    }
                }
            }
        }

        return null;
    }

    public static List<OdinDeclaredIdentifier> getFileScopeDeclarations(OdinFileScope fileScope) {
        // Find all blocks that are not in a procedure
        List<OdinDeclaredIdentifier> declarations = new ArrayList<>();

        Stack<PsiElement> statementStack = new Stack<>();

        // do bfs
        statementStack.addAll(fileScope.getFileScopeStatementList().getStatementList());
        while (!statementStack.isEmpty()) {
            PsiElement element = statementStack.pop();
            if (element instanceof OdinDeclaration declaration) {
                declarations.addAll(declaration.getDeclaredIdentifiers());
            } else {
                getStatements(element).forEach(statementStack::push);
            }
        }
        return declarations;
    }

    private static List<OdinStatement> getStatements(PsiElement psiElement) {
        if (psiElement instanceof OdinWhenStatement odinWhenStatement) {
            if (odinWhenStatement.getBlock() != null) {
                OdinStatementList statementList = odinWhenStatement.getBlock().getStatementList();
                if (statementList != null) {
                    return statementList.getStatementList();
                }
            }

            if (odinWhenStatement.getDoStatement() != null) {
                return List.of(odinWhenStatement.getDoStatement());
            }
        }

        if (psiElement instanceof OdinForeignBlock foreignBlock) {
            OdinForeignStatementList foreignStatementList = foreignBlock.getForeignStatementList();
            if (foreignStatementList != null) {
                return foreignStatementList.getStatementList();
            }
        }

        return Collections.emptyList();
    }


    public record QualifiedName(@NotNull String name, @NotNull OdinRefExpression rootRefExpression) {

    }
    public static QualifiedName getQualifiedName(OdinIdentifier identifier) {
        // Walk up the tree until we find a parent that is not OdinRefExpression
        PsiElement parent = identifier.getParent();
        PsiElement lastRefExpression = null;
        while (parent instanceof OdinRefExpression) {
            lastRefExpression = parent;
            parent = parent.getParent();
        }

        if(lastRefExpression != null) {
            return new QualifiedName(lastRefExpression.getText(), (OdinRefExpression) lastRefExpression);
        }

        return null;
    }

    @NotNull
    public static List<OdinDeclaredIdentifier> findDeclarations(PsiElement element, Predicate<PsiElement> matcher) {
        List<OdinDeclaredIdentifier> declarations = new ArrayList<>();
        PsiElement entrance = element;
        PsiElement lastValidBlock = element;

        // Check all parent blocks
        while (entrance != null) {
            OdinBlock containingBlock = (OdinBlock) PsiTreeUtil.findFirstParent(entrance, true, parent -> parent instanceof OdinBlock);
            entrance = containingBlock;
            if (containingBlock == null) {
                break;
            }

            lastValidBlock = containingBlock;

            OdinStatementList statementList = containingBlock.getStatementList();
            if (statementList == null) {
                continue;
            }

            for (OdinStatement statement : statementList.getStatementList()) {
                List<OdinDeclaredIdentifier> matchingDeclarations = getMatchingDeclarations(matcher, statement);
                declarations.addAll(matchingDeclarations);
            }
        }

        // Check file scope
        OdinFileScope fileScope = (OdinFileScope) PsiTreeUtil.findFirstParent(lastValidBlock, psi -> psi instanceof OdinFileScope);
        if(fileScope != null) {
            List<OdinDeclaredIdentifier> fileScopeDeclarations = getFileScopeDeclarations(fileScope);
            declarations.addAll(fileScopeDeclarations.stream().filter(matcher).toList());
        }
        // TODO this only works with x.y and serves just as a proof of concept
        // Check imported files
        if(element instanceof OdinIdentifier identifier) {
            QualifiedName qualifiedName = OdinInsightUtils.getQualifiedName(identifier);
            if(qualifiedName == null) {
                return Collections.emptyList();
            }
            VirtualFile virtualFile = identifier.getContainingFile().getViewProvider().getVirtualFile();
            String[] parts = qualifiedName.name().split("\\.");
            Project project = identifier.getProject();

            List<OdinDeclaredIdentifier> importedDeclarations;
            if(parts.length > 1) {
                String importName = parts[0];
                importedDeclarations = findDeclarationsInImports(virtualFile.getPath(),
                        fileScope,
                        importName,
                        project);
            } else {
                importedDeclarations = Collections.emptyList();
            }

            declarations.addAll(importedDeclarations);
        }

        return declarations;
    }

    public static List<OdinDeclaredIdentifier> getFileScopeDeclarations(OdinFileScope odinFileScope, Predicate<PsiElement> matcher) {
        return getFileScopeDeclarations(odinFileScope).stream().filter(matcher).toList();
    }

    private static List<OdinDeclaredIdentifier> getMatchingDeclarations(Predicate<PsiElement> matcher, OdinStatement statement) {
        List<OdinDeclaredIdentifier> odinDeclaredIdentifiers = new ArrayList<>();
        List<OdinDeclaredIdentifier> declarations = getDeclarations(statement);
        for (OdinDeclaredIdentifier identifier : declarations) {
            boolean isMatch = matcher.test(identifier);
            if (isMatch) {
                odinDeclaredIdentifiers.add(identifier);
            }
        }
        return odinDeclaredIdentifiers;
    }

    @NotNull
    private static List<OdinDeclaredIdentifier> getDeclarations(PsiElement child) {
        List<OdinDeclaredIdentifier> identifierList;
        if (child instanceof OdinVariableDeclarationStatement variableDeclaration) {
            identifierList = variableDeclaration.getIdentifierList().getDeclaredIdentifierList();
        } else if (child instanceof OdinVariableInitializationStatement variableInitialization) {
            identifierList = variableInitialization.getIdentifierList().getDeclaredIdentifierList();
        } else if (child instanceof OdinProcedureDeclarationStatement procedureDeclaration) {
            identifierList = List.of(procedureDeclaration.getDeclaredIdentifier());
        } else if (child instanceof OdinConstantInitializationStatement constantInitializationStatement) {
            identifierList = constantInitializationStatement.getIdentifierList().getDeclaredIdentifierList();
        } else if (child instanceof OdinStructDeclarationStatement structDeclarationStatement) {
            identifierList = List.of(structDeclarationStatement.getDeclaredIdentifier());
        } else if (child instanceof OdinEnumDeclarationStatement enumDeclarationStatement) {
            identifierList = List.of(enumDeclarationStatement.getDeclaredIdentifier());
        } else if (child instanceof OdinUnionDeclarationStatement unionDeclarationStatement) {
            identifierList = List.of(unionDeclarationStatement.getDeclaredIdentifier());
        } else if (child instanceof OdinProcedureOverloadStatement procedureOverloadStatement) {
            identifierList = List.of(procedureOverloadStatement.getDeclaredIdentifier());
        } else {
            identifierList = Collections.emptyList();
        }
        return identifierList;
    }

    public static <T> T findFirstParentOfType(PsiElement element, boolean strict, Class<T> type) {
        //noinspection unchecked
        return (T) PsiTreeUtil.findFirstParent(element, strict, Conditions.instanceOf(type));
    }

    public static boolean isVariableDeclaration(PsiElement element) {
        return findFirstParentOfType(element, true, OdinVariableDeclarationStatement.class) != null
                || findFirstParentOfType(element, true, OdinVariableInitializationStatement.class) != null;
    }

    public static boolean isProcedureDeclaration(PsiElement element) {
        return element.getParent() instanceof OdinProcedureDeclarationStatement;
    }

    public static boolean isProcedureOverloadDeclaration(PsiElement element) {
        return element.getParent() instanceof OdinProcedureOverloadStatement;
    }

    public static boolean isConstantDeclaration(PsiElement element) {
        return findFirstParentOfType(element, true, OdinConstantInitializationStatement.class) != null;
    }

    public static boolean isStructDeclaration(PsiElement element) {
        return element.getParent() instanceof OdinStructDeclarationStatement;
    }

    public static boolean isEnumDeclaration(PsiElement element) {
        return element.getParent() instanceof OdinEnumDeclarationStatement;
    }

    public static boolean isUnionDeclaration(PsiElement element) {
        return element.getParent() instanceof OdinUnionDeclarationStatement;
    }

    public static OdinTypeType classify(OdinDeclaredIdentifier element) {
        if (isStructDeclaration(element)) {
            return OdinTypeType.STRUCT;
        } else if (isEnumDeclaration(element)) {
            return OdinTypeType.ENUM;
        } else if (isUnionDeclaration(element)) {
            return OdinTypeType.UNION;
        } else if (isProcedureDeclaration(element)) {
            return OdinTypeType.PROCEDURE;
        } else if (isVariableDeclaration(element)) {
            return OdinTypeType.VARIABLE;
        } else if (isConstantDeclaration(element)) {
            return OdinTypeType.CONSTANT;
        } else if (isProcedureOverloadDeclaration(element)) {
            return OdinTypeType.PROCEUDRE_OVERLOAD;
        } else {
            return OdinTypeType.UNKNOWN;
        }
    }

    public static OdinTypeType classify(PsiElement psiElement) {
        return null;
    }

    public static OdinProcedureDeclarationStatement getDeclaringProcedure(OdinDeclaredIdentifier element) {
        return element.getParent() instanceof OdinProcedureDeclarationStatement ? (OdinProcedureDeclarationStatement) element.getParent() : null;
    }

    public static List<OdinDeclaredIdentifier> findDeclarationsInImports(String path,
                                                                         OdinFileScope fileScope,
                                                                         String importName,
                                                                         Project project) {
        Map<String, ImportInfo> importMap = collectImportStatements(fileScope);
        ImportInfo importInfo = importMap.get(importName);
        List<OdinDeclaredIdentifier> fileScopeDeclarations = new ArrayList<>();
        if (importInfo != null) {
            Sdk projectSdk = ProjectRootManager.getInstance(project).getProjectSdk();

            List<Path> dirs = new ArrayList<>();
            if (projectSdk != null) {
                String library = Objects.requireNonNullElse(importInfo.library(), "");
                if (!library.isBlank()) {
                    Path sdkSourceDir = Path.of(Objects.requireNonNull(projectSdk.getHomeDirectory()).getPath(), library);
                    dirs.add(sdkSourceDir);
                }
            }
            Path currentDir = Path.of(path).getParent();
            dirs.add(currentDir);

            Predicate<PsiElement> psiElementPredicate = e -> true;
            for (Path dir : dirs) {
                var importedFiles = findImportFiles(dir, importInfo, project);
                for (OdinFile importedFile : importedFiles) {
                    OdinFileScope importedFileScope = importedFile.getFileScope();
                    if (importedFileScope == null) {
                        System.out.println("File scope is null for file %s" + importedFile.getVirtualFile().getPath());
                        continue;
                    }

                    fileScopeDeclarations.addAll(getFileScopeDeclarations(importedFileScope, psiElementPredicate));
                }

                if (!importedFiles.isEmpty()) {
                    break;
                }
            }
        }
        return fileScopeDeclarations;
    }

    @NotNull
    public static Map<String, ImportInfo> collectImportStatements(OdinFileScope fileScope) {
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

            if (name == null) {
                // Last path segment is the name
                String[] pathParts = path.split("/");
                name = pathParts[pathParts.length - 1];
            }

            ImportInfo importInfo = new ImportInfo(name, path, library);
            importMap.put(name, importInfo);
        }
        return importMap;
    }

    public static List<OdinFile> findImportFiles(Path directory,
                                                 ImportInfo importInfo,
                                                 Project project) {

        Path importPath = directory.resolve(importInfo.path());
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

    public enum OdinTypeType {
        STRUCT,
        ENUM,
        UNION,
        PROCEDURE,
        PROCEUDRE_OVERLOAD,
        VARIABLE,
        CONSTANT,
        UNKNOWN

    }
}
