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
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;

public class OdinInsightUtils {
    public static OdinDeclaration getDeclaration(PsiNamedElement declaredIdentifier) {
        return findFirstParentOfType(declaredIdentifier, false, OdinDeclaration.class);
    }

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
                var matchingDeclarations = getNamedElements(matcher, statement);
                if (!matchingDeclarations.isEmpty()) return matchingDeclarations.get(0);
            }
        }

        // Check file scope
        OdinFileScope odinFileScope = (OdinFileScope) PsiTreeUtil.findFirstParent(lastValidBlock, psi -> psi instanceof OdinFileScope);
        if (odinFileScope != null) {
            var fileScopeDeclarations = getFileScopeDeclarations(odinFileScope);
            for (var fileScopeDeclaration : fileScopeDeclarations.getNamedElements()) {
                boolean isMatch = matcher.test(fileScopeDeclaration);
                if (isMatch) {
                    return fileScopeDeclaration;
                }
            }
        }

        // TODO this only works with x.y and serves just as a proof of concept
        // Check imported files
        if (identifier != null) {
            QualifiedName qualifiedName = OdinInsightUtils.getQualifiedName(identifier);
            if (qualifiedName == null) {
                return null;
            }

            String[] parts = qualifiedName.name().split("\\.");
            if (parts.length > 1) {
                String importName = parts[0];
                VirtualFile virtualFile = identifier.getContainingFile().getViewProvider().getVirtualFile();
                var declarations = findDeclarationsInImports(virtualFile.getPath(),
                        odinFileScope,
                        importName,
                        identifier.getProject());

                String name = parts[1];
                for (var declaration : declarations) {
                    String text = declaration.getText();
                    if (text.equals(name)) {
                        return declaration;
                    }
                }
            }
        }

        return null;
    }

    public static Scope getFileScopeDeclarations(OdinFileScope fileScope) {
        // Find all blocks that are not in a procedure
        List<PsiNamedElement> declarations = new ArrayList<>();

        Stack<PsiElement> statementStack = new Stack<>();

        // do bfs
        statementStack.addAll(fileScope.getStatementList());
        while (!statementStack.isEmpty()) {
            PsiElement element = statementStack.pop();
            if (element instanceof OdinImportDeclarationStatement importDeclarationStatement) {
                var alias = importDeclarationStatement.getAlias();
                declarations.add(Objects.requireNonNullElse(alias, importDeclarationStatement));
            } else if (element instanceof OdinDeclaration declaration) {
                declarations.addAll(declaration.getDeclaredIdentifiers());
            } else {
                getStatements(element).forEach(statementStack::push);
            }
        }
        return Scope.from(declarations);
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

    static List<PsiNamedElement> getDeclarationsOfImportedPackage(OdinImportDeclarationStatement importStatement) {
        ImportInfo importInfo = importStatement.getImportInfo();

        OdinFileScope fileScope = ((OdinFile) importStatement.getContainingFile()).getFileScope();
        PsiFile containingFile = importStatement.getContainingFile();
        VirtualFile virtualFile = containingFile.getVirtualFile();

        if (virtualFile == null) {
            virtualFile = containingFile.getViewProvider().getVirtualFile();
        }

        String path = virtualFile.getCanonicalPath();
        String name = importInfo.packageName();
        Project project = importStatement.getProject();
        return findDeclarationsInImports(path, fileScope, name, project);
    }

    static List<PsiNamedElement> getDeclarationsOfImportedPackage(Scope scope, OdinImportDeclarationStatement importStatement) {
        ImportInfo importInfo = importStatement.getImportInfo();
        OdinFileScope fileScope = ((OdinFile) importStatement.getContainingFile()).getFileScope();
        String path = scope.getPackagePath();
        String name = importInfo.packageName();
        Project project = importStatement.getProject();
        return findDeclarationsInImports(path, fileScope, name, project);
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

        if (lastRefExpression != null) {
            return new QualifiedName(lastRefExpression.getText(), (OdinRefExpression) lastRefExpression);
        }

        return null;
    }

    public static Scope findScope(PsiElement element) {
        return findScope(element, e -> true);
    }

    @NotNull
    public static Scope findScope(PsiElement element, Predicate<PsiElement> matcher) {
        List<PsiNamedElement> declarations = new ArrayList<>();
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
                var matchingDeclarations = getNamedElements(matcher, statement);
                declarations.addAll(matchingDeclarations);
            }
        }

        // Check file scope
        OdinFileScope fileScope = (OdinFileScope) PsiTreeUtil.findFirstParent(lastValidBlock, psi -> psi instanceof OdinFileScope);
        if (fileScope != null) {
            Scope fileScopeDeclarations = getFileScopeDeclarations(fileScope);
            declarations.addAll(fileScopeDeclarations.getFiltered(matcher));
        }

        return Scope.from(declarations);
    }

    private static Collection<PsiNamedElement> getFileScopeDeclarations(OdinFileScope odinFileScope, Predicate<PsiElement> matcher) {
        return getFileScopeDeclarations(odinFileScope).getFiltered(matcher);
    }

    private static List<? extends PsiNamedElement> getNamedElements(Predicate<PsiElement> matcher, OdinStatement statement) {
        List<? extends PsiNamedElement> odinDeclaredIdentifiers = new ArrayList<>();
        if (statement instanceof OdinDeclaration declaration) {
            return declaration.getDeclaredIdentifiers().stream().filter(matcher).toList();
        }
        return odinDeclaredIdentifiers;
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

    public static OdinTypeType classify(PsiNamedElement element) {
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
            return OdinTypeType.PROCEDURE_OVERLOAD;
        } else if (isPackageDeclaration(element)) {
            return OdinTypeType.PACKAGE;
        } else if (isFieldDeclaration(element)) {
            return OdinTypeType.FIELD;
        } else {
            return OdinTypeType.UNKNOWN;
        }
    }

    private static boolean isFieldDeclaration(PsiNamedElement element) {
        return element.getParent() instanceof OdinFieldDeclarationStatement;
    }

    private static boolean isPackageDeclaration(PsiNamedElement element) {
        return element instanceof OdinImportDeclarationStatement
                || element.getParent() instanceof OdinImportDeclarationStatement;
    }

    public static OdinProcedureDeclarationStatement getDeclaringProcedure(OdinDeclaredIdentifier element) {
        return element.getParent() instanceof OdinProcedureDeclarationStatement ? (OdinProcedureDeclarationStatement) element.getParent() : null;
    }

    /**
     * @return Returns the declarations from an import with specified name.
     * TODO This should return the target file as well
     */
    public static List<PsiNamedElement> findDeclarationsInImports(String path,
                                                                  OdinFileScope fileScope,
                                                                  String importName,
                                                                  Project project) {
        Map<String, ImportInfo> importMap = getImportStatementsInfo(fileScope);
        ImportInfo importInfo = importMap.get(importName);
        List<PsiNamedElement> packageDeclarations = new ArrayList<>();
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

            Predicate<PsiElement> publicElementsMatcher = e -> {
                if (e instanceof OdinDeclaredIdentifier declaredIdentifier) {
                    OdinDeclaration declaration = OdinInsightUtils.getDeclaration(declaredIdentifier);
                    return !(declaration instanceof OdinImportDeclarationStatement);
                }
                return !(e instanceof OdinImportDeclarationStatement);
            };

            for (Path dir : dirs) {
                var importedFiles = getFilesInPackage(dir, importInfo, project);
                for (OdinFile importedFile : importedFiles) {
                    OdinFileScope importedFileScope = importedFile.getFileScope();
                    if (importedFileScope == null) {
                        System.out.println("File scope is null for file %s" + importedFile.getVirtualFile().getPath());
                        continue;
                    }

                    Collection<PsiNamedElement> fileScopeDeclarations = getFileScopeDeclarations(importedFileScope, publicElementsMatcher);

                    packageDeclarations.addAll(fileScopeDeclarations);
                }

                if (!importedFiles.isEmpty()) {
                    break;
                }
            }
        }
        return packageDeclarations;
    }

    @NotNull
    public static Map<String, ImportInfo> getImportStatementsInfo(OdinFileScope fileScope) {
        Map<String, ImportInfo> importMap = new HashMap<>();
        List<OdinImportDeclarationStatement> importStatements
                = fileScope.getImportStatements();

        for (OdinImportDeclarationStatement importStatement : importStatements) {
            ImportInfo importInfo = importStatement.getImportInfo();
            importMap.put(importInfo.packageName(), importInfo);
        }
        return importMap;
    }


    public static List<OdinFile> getFilesInPackage(Path directory,
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

}
