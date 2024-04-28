package com.lasagnerd.odin.insights;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Conditions;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.lang.psi.*;
import com.lasagnerd.odin.lang.typeSystem.TsOdinType;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class OdinInsightUtils {
    public static OdinDeclaration getDeclaration(PsiNamedElement declaredIdentifier) {
        return findFirstParentOfType(declaredIdentifier, false, OdinDeclaration.class);
    }

    @NotNull
    public static String getFileName(@NotNull PsiElement psiElement) {
        VirtualFile virtualFile = psiElement.getContainingFile().getVirtualFile();
        return Objects.requireNonNullElseGet(virtualFile,
                () -> psiElement.getContainingFile().getViewProvider().getVirtualFile()).getName();
    }

    public static String getPackagePath(PsiElement psiElement) {
        OdinFile containingFile = (OdinFile) psiElement.getContainingFile();
        if (containingFile == null)
            return null;
        @NotNull PsiFile virtualFile = containingFile.getOriginalFile();
        return virtualFile.getContainingDirectory().getVirtualFile().getPath();

    }

    public static PsiElement findFirstDeclaration(OdinIdentifier identifier) {
        Scope parentScope = findScope(identifier).with(getPackagePath(identifier));
        OdinRefExpression refExpression = findFirstParentOfType(identifier, true, OdinRefExpression.class);
        Scope scope = Scope.EMPTY;
        if (refExpression != null) {
            if (refExpression.getExpression() != null) {
                scope = OdinReferenceResolver.resolve(parentScope, refExpression.getExpression());
            } else {
                scope = parentScope;
            }
        } else {

            OdinTypeDefinitionExpression typeDefinitionExpression = findFirstParentOfType(identifier, true, OdinTypeDefinitionExpression.class);
            if (typeDefinitionExpression != null) {
                OdinTypeExpression mainTypeExpression = typeDefinitionExpression.getMainTypeExpression();
                if (mainTypeExpression instanceof OdinQualifiedType qualifiedType) {
                    if (qualifiedType.getPackageIdentifier() != null && qualifiedType.getPackageIdentifier()
                            .getIdentifierToken()
                            .getText()
                            .equals(identifier.getIdentifierToken().getText())) {
                        scope = parentScope;
                    } else {
                        TsOdinType type = TypeExpressionResolver.resolveType(parentScope, qualifiedType);
                        scope = type.getParentScope();
                    }
                }
            }
        }

        if (scope == Scope.EMPTY || scope == null) {
            scope = parentScope;
        }

        if (scope != null)
            return scope.findNamedElement(identifier.getIdentifierToken().getText());

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

    static Scope getDeclarationsOfImportedPackage(OdinImportDeclarationStatement importStatement) {
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
        return getDeclarationsOfImportedPackage(getImportStatementsInfo(fileScope).get(name), path, project);
    }

    public static Scope getDeclarationsOfImportedPackage(Scope scope, OdinImportDeclarationStatement importStatement) {
        ImportInfo importInfo = importStatement.getImportInfo();
        OdinFileScope fileScope = ((OdinFile) importStatement.getContainingFile()).getFileScope();
        String path = Path.of(scope.getPackagePath(), OdinInsightUtils.getFileName(importStatement)).toString();
        String name = importInfo.packageName();
        Project project = importStatement.getProject();
        return getDeclarationsOfImportedPackage(getImportStatementsInfo(fileScope).get(name), path, project);
    }

    /**
     * Returns the symbols provided by an expression of type `type` when it is referenced with "." or "->".
     *
     * @param type The type of the expression
     * @return The scope
     */
    public static Scope getScopeProvidedByTypeExpression(TsOdinType type) {
        Scope parentScope = type.getParentScope();
        Scope scope = new Scope();
        OdinDeclaration odinDeclaration = type.getDeclaration();
        if (odinDeclaration instanceof OdinStructDeclarationStatement structDeclarationStatement) {
            List<PsiNamedElement> structFields = getStructFields(structDeclarationStatement);
            for (OdinFieldDeclarationStatement odinFieldDeclarationStatement : getStructFieldsDeclarationStatements(structDeclarationStatement).stream()
                    .filter(f -> f.getUsing() != null)
                    .toList()) {
                if (odinFieldDeclarationStatement.getDeclaredIdentifiers().isEmpty())
                    continue;

                TsOdinType usedType = TypeExpressionResolver.resolveType(parentScope, odinFieldDeclarationStatement.getTypeDefinition().getMainTypeExpression());
                Scope subScope = getScopeProvidedByTypeExpression(usedType);
                scope.addSymbols(subScope);
            }

            scope.addAll(structFields);
            return scope;
        }

        if (odinDeclaration instanceof OdinEnumDeclarationStatement enumDeclarationStatement) {
            return scope.with(getEnumFields(enumDeclarationStatement));
        }

        return Scope.EMPTY;
    }

    @NotNull
    public static List<PsiNamedElement> getEnumFields(OdinEnumDeclarationStatement enumDeclarationStatement) {
        OdinEnumBody enumBody = enumDeclarationStatement.getEnumType()
                .getEnumBlock()
                .getEnumBody();

        if (enumBody == null)
            return Collections.emptyList();

        return enumBody
                .getEnumValueDeclarationList()
                .stream()
                .map(OdinEnumValueDeclaration::getDeclaredIdentifier)
                .collect(Collectors.toList());
    }

    public static List<PsiNamedElement> getStructFields(OdinStructDeclarationStatement structDeclarationStatement) {
        List<OdinFieldDeclarationStatement> fieldDeclarationStatementList = getStructFieldsDeclarationStatements(structDeclarationStatement);

        return fieldDeclarationStatementList.stream()
                .flatMap(x -> x.getDeclaredIdentifiers().stream())
                .collect(Collectors.toList());
    }

    public static @NotNull List<OdinFieldDeclarationStatement> getStructFieldsDeclarationStatements(OdinStructDeclarationStatement structDeclarationStatement) {
        OdinStructBody structBody = structDeclarationStatement
                .getStructType()
                .getStructBlock()
                .getStructBody();

        List<OdinFieldDeclarationStatement> fieldDeclarationStatementList;
        if (structBody == null) {
            fieldDeclarationStatementList = Collections.emptyList();
        } else {
            fieldDeclarationStatementList = structBody.getFieldDeclarationStatementList();
        }
        return fieldDeclarationStatementList;
    }

    public static Scope findScope(PsiElement element) {
        return findScope(element, e -> true);
    }

    public static Scope findScope(PsiElement element, Predicate<PsiElement> matcher) {
        // Find current block,
        //  1. add all statements before that psi element
        //  2. if the block has a declaration area, bring those into scope as well -> here there can be using statements as well
        //  3. if there are any using statements bring them into scope as well
        //  4. if the containing block is in a procedure, stop: Procedures are not closures in Odin

        // Recursively: Now our out-of-scope element is the parent block. Repeat
        // Exception: When no more parent blocks are available, then find the file scope and add ALL declarations to the scope
        // Keep shadowing in mind... a higher scope can not override symbols from lower scopes

        // When we are in a compound literal (e.g. struct instantiation) the fields of the type are also brought into scope

        Scope scope = new Scope();

        // TODO Add fields from struct type

        doFindScope(scope, element, matcher);

        OdinFileScope fileScope = (OdinFileScope) PsiTreeUtil.findFirstParent(element, psi -> psi instanceof OdinFileScope);
        if (fileScope != null) {
            Scope fileScopeDeclarations = getFileScopeDeclarations(fileScope);
            scope.addAll(fileScopeDeclarations.getFiltered(matcher), false);
        }

        return scope;
    }

    public static void doFindScope(Scope scope, PsiElement entrance, Predicate<PsiElement> matcher) {
        OdinBlock containingBlock = (OdinBlock) PsiTreeUtil.findFirstParent(entrance, true, parent -> parent instanceof OdinBlock);
        if (containingBlock != null) {
            OdinStatement containingStatement = findFirstParentOfType(entrance, false, OdinStatement.class);
            OdinStatement lastValidStatement;
            if (PsiTreeUtil.isAncestor(containingBlock, containingStatement, true)) {
                // This means the containing statement is inside the containing block
                lastValidStatement = containingStatement;
            } else {
                lastValidStatement = null;
            }

            boolean afterStatement = false;
            for (OdinStatement statement : containingBlock.getStatements()) {
                if (!afterStatement) {
                    if (statement instanceof OdinDeclaration declaration) {
                        scope.addAll(declaration.getDeclaredIdentifiers(), false);
                    }
                } else {
                    // Here we only add stuff that is not a variable initialization or declaration
                    // Background: Constants and type definitions are available in the entire block, no matter where they
                    // were declared.
                    if (statement instanceof OdinVariableDeclarationStatement)
                        continue;
                    if (statement instanceof OdinVariableInitializationStatement) {
                        continue;
                    }
                    if (statement instanceof OdinDeclaration declaration) {
                        List<? extends PsiNamedElement> declaredIdentifiers = declaration.getDeclaredIdentifiers()
                                .stream().filter(matcher).toList();
                        scope.addAll(declaredIdentifiers, false);
                    }
                }
                if (statement == lastValidStatement) {
                    afterStatement = true;
                }
            }
            // If we are inside a procedure body we also add (return) parameters
            // We don't further look for scope, because a procedure is not a closure in Odin
            if (containingBlock.getParent() instanceof OdinProcedureBody procedureBody) {
                OdinProcedureExpression procedureExpression = findFirstParentOfType(procedureBody, true, OdinProcedureExpression.class);
                OdinProcedureType procedureType = null;
                if (procedureExpression != null) {
                    procedureType = procedureExpression.getProcedureExpressionType().getProcedureType();
                }

                if (procedureType == null) {
                    OdinProcedureDeclarationStatement procedureDeclarationStatement = findFirstParentOfType(procedureBody, true, OdinProcedureDeclarationStatement.class);
                    procedureType = procedureDeclarationStatement.getProcedureType();
                }

                OdinParamEntries paramEntries = procedureType.getParamEntries();
                if (paramEntries != null) {
                    for (OdinParamEntry odinParamEntry : paramEntries.getParamEntryList()) {
                        odinParamEntry
                                .getParameterDeclaration()
                                .getParameterList().stream()
                                .map(OdinParameter::getDeclaredIdentifier)
                                .filter(matcher)
                                .forEach(identifier -> scope.add(identifier, false));
                    }
                }

                OdinReturnParameters returnParameters = procedureType.getReturnParameters();
                if (returnParameters != null) {
                    OdinParamEntries returnParamEntries = returnParameters.getParamEntries();
                    if (returnParamEntries != null) {
                        for (OdinParamEntry odinParamEntry : returnParamEntries.getParamEntryList()) {
                            scope.addAll(odinParamEntry
                                    .getParameterDeclaration()
                                    .getDeclaredIdentifiers().stream()
                                    .filter(matcher)
                                    .toList(), false);
                        }
                    }
                }
                return;
            }

            // Add declarations of block (e.g. parameters, return parameters, if-block declarations, etc)
            if (containingBlock.getParent() instanceof OdinIfStatement ifStatement) {
                OdinStatement statement = ifStatement.getCondition().getStatement();

                scope.addAll(getNamedElements(matcher, statement));
            }

            if (containingBlock.getParent() instanceof OdinWhenStatement whenStatement) {
                OdinStatement statement = whenStatement.getCondition().getStatement();
                scope.addAll(getNamedElements(matcher, statement));
            }

            if (containingBlock.getParent() instanceof OdinForStatement ifStatement) {
                OdinStatement statement = ifStatement.getForHead().getStatement();
                scope.addAll(getNamedElements(matcher, statement));
            }


            doFindScope(scope, containingBlock, matcher);
        }
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

    private static boolean isFieldDeclaration(PsiNamedElement element) {
        return element.getParent() instanceof OdinFieldDeclarationStatement;
    }

    private static boolean isPackageDeclaration(PsiNamedElement element) {
        return element instanceof OdinImportDeclarationStatement
                || element.getParent() instanceof OdinImportDeclarationStatement;
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
        } else if (isParameterDeclaration(element)) {
            return OdinTypeType.PARAMETER;
        } else {
            return OdinTypeType.UNKNOWN;
        }
    }

    public static boolean isParameterDeclaration(PsiElement element) {
        return OdinInsightUtils.findFirstParentOfType(element, true, OdinDeclaration.class) instanceof OdinParameterDeclaration;
    }

    public static OdinProcedureDeclarationStatement getDeclaringProcedure(OdinDeclaredIdentifier element) {
        return element.getParent() instanceof OdinProcedureDeclarationStatement ? (OdinProcedureDeclarationStatement) element.getParent() : null;
    }

    /**
     * @param importInfo     The import to be imported
     * @param sourceFilePath Path of the file from where the import should be resolved
     * @param project        Project
     * @return Scope
     */
    public static Scope getDeclarationsOfImportedPackage(ImportInfo importInfo, String sourceFilePath, Project project) {
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
            Path sourcePath = Path.of(sourceFilePath);
            Path currentDir = sourcePath.getParent();

            dirs.add(currentDir);

            Predicate<PsiElement> publicElementsMatcher = e -> {
                if (e instanceof OdinDeclaredIdentifier declaredIdentifier) {
                    OdinDeclaration declaration = OdinInsightUtils.getDeclaration(declaredIdentifier);
                    return !(declaration instanceof OdinImportDeclarationStatement);
                }
                return !(e instanceof OdinImportDeclarationStatement);
            };


            for (Path dir : dirs) {
                Path packagePath = dir.resolve(importInfo.path());
                var importedFiles = getFilesInPackage(project, packagePath);
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
                    return Scope.from(packageDeclarations, packagePath.toString());
                }
            }
        }
        return Scope.EMPTY;
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


    public static List<OdinFile> getFilesInPackage(Project project, Path importPath) {
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
