package com.lasagnerd.odin.insights;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Conditions;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.lasagnerd.odin.insights.typeInference.OdinTypeResolver;
import com.lasagnerd.odin.insights.typeSystem.TsOdinPackageType;
import com.lasagnerd.odin.insights.typeSystem.TsOdinPointerType;
import com.lasagnerd.odin.insights.typeSystem.TsOdinType;
import com.lasagnerd.odin.lang.psi.*;
import com.lasagnerd.odin.sdkConfig.OdinSdkConfigPersistentState;
import org.apache.commons.text.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;
import org.mozilla.javascript.ast.StringLiteral;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class OdinInsightUtils {

    public static final Predicate<PsiElement> PUBLIC_ELEMENTS_MATCHER = e -> {
        if (e instanceof OdinDeclaredIdentifier declaredIdentifier) {
            OdinDeclaration declaration = OdinInsightUtils.getDeclaration(declaredIdentifier);
            return !(declaration instanceof OdinImportDeclarationStatement);
        }
        return !(e instanceof OdinImportDeclarationStatement);
    };

    public static final Predicate<PsiElement> PACKAGE_VISIBLE_ELEMENTS_MATCHER = PUBLIC_ELEMENTS_MATCHER;

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
        PsiDirectory containingDirectory = virtualFile.getContainingDirectory();
        if (containingDirectory != null) {
            return containingDirectory.getVirtualFile().getPath();
        }
        return null;
    }

    public static OdinSymbol findSymbol(OdinIdentifier identifier) {
        OdinScope parentScope = OdinScopeResolver.resolveScope(identifier).with(getPackagePath(identifier));
        OdinRefExpression refExpression = findFirstParentOfType(identifier, true, OdinRefExpression.class);
        OdinScope scope;
        if (refExpression != null) {
            if (refExpression.getExpression() != null) {
                scope = OdinReferenceResolver.resolve(parentScope, refExpression.getExpression());
            } else {
                scope = parentScope;
            }
        } else {
            OdinQualifiedType qualifiedType = OdinInsightUtils.findFirstParentOfType(identifier, true, OdinQualifiedType.class);
            if (qualifiedType != null) {
                scope = OdinReferenceResolver.resolve(parentScope, qualifiedType);
            } else {
                scope = parentScope;
            }
        }

        if (scope == OdinScope.EMPTY || scope == null) {
            scope = parentScope;
        }

        if (scope != null) {
            return scope.getSymbol(identifier.getIdentifierToken().getText());
        }

        return null;
    }

    static OdinScope getDeclarationsOfImportedPackage(OdinImportDeclarationStatement importStatement) {
        OdinImportInfo importInfo = importStatement.getImportInfo();

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

    public static boolean isStringLiteralWithValue(OdinExpression odinExpression, String val) {
        return Objects.equals(getStringLiteralValue(odinExpression), val);
    }

    public static String getStringLiteralValue(OdinExpression odinExpression) {
        if (odinExpression instanceof OdinLiteralExpression literalExpression) {
            if (literalExpression.getBasicLiteral() instanceof OdinStringLiteral stringLiteral) {
                if (stringLiteral.getDqStringLiteral() != null || stringLiteral.getSqStringLiteral() != null) {
                    String text = literalExpression.getText();

                    if (text.length() >= 2) {
                        text = text.substring(1, text.length() - 1);
                        return StringEscapeUtils.unescapeJava(text);
                    }
                }
            }
        }
        return null;
    }

    public static OdinScope getDeclarationsOfImportedPackage(OdinScope scope, OdinImportDeclarationStatement importStatement) {
        OdinImportInfo importInfo = importStatement.getImportInfo();
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
    public static OdinScope getScopeProvidedByType(TsOdinType type) {
        if(type instanceof TsOdinPackageType packageType) {
            return OdinInsightUtils
                    .getDeclarationsOfImportedPackage((OdinImportDeclarationStatement) packageType.getDeclaration());
        }
        OdinScope typeScope = type.getScope();
        OdinScope scope = new OdinScope();
        if (type instanceof TsOdinPointerType pointerType) {
            type = pointerType.getDereferencedType();
        }
        OdinDeclaration odinDeclaration = type.getDeclaration();

        if (odinDeclaration instanceof OdinStructDeclarationStatement structDeclarationStatement) {
            List<OdinSymbol> structFields = getStructFields(structDeclarationStatement);
            for (OdinFieldDeclarationStatement odinFieldDeclarationStatement : getStructFieldsDeclarationStatements(structDeclarationStatement).stream()
                    .filter(f -> f.getUsing() != null)
                    .toList()) {
                if (odinFieldDeclarationStatement.getDeclaredIdentifiers().isEmpty())
                    continue;

                TsOdinType usedType = OdinTypeResolver.resolveType(typeScope, odinFieldDeclarationStatement.getTypeDefinitionExpression().getType());
                OdinScope subScope = getScopeProvidedByType(usedType);
                scope.putAll(subScope);
            }

            scope.addAll(structFields);
            scope.addTypes(typeScope);
            return scope;
        }

        if (odinDeclaration instanceof OdinEnumDeclarationStatement enumDeclarationStatement) {
            return scope.with(getEnumFields(enumDeclarationStatement));
        }

        return OdinScope.EMPTY;
    }

    @NotNull
    public static List<OdinSymbol> getEnumFields(OdinEnumDeclarationStatement enumDeclarationStatement) {
        OdinEnumType enumType = enumDeclarationStatement.getEnumType();

        return getEnumFields(enumType);
    }

    private static @NotNull List<OdinSymbol> getEnumFields(OdinEnumType enumType) {
        OdinEnumBody enumBody = enumType
                .getEnumBlock()
                .getEnumBody();

        if (enumBody == null)
            return Collections.emptyList();

        return enumBody
                .getEnumValueDeclarationList()
                .stream()
                .map(OdinEnumValueDeclaration::getDeclaredIdentifier)
                .map(OdinSymbol::new)
                .collect(Collectors.toList());
    }

    public static List<OdinSymbol> getStructFields(OdinStructDeclarationStatement structDeclarationStatement) {
        List<OdinFieldDeclarationStatement> fieldDeclarationStatementList = getStructFieldsDeclarationStatements(structDeclarationStatement);

        return fieldDeclarationStatementList.stream()
                .flatMap(x -> x.getDeclaredIdentifiers().stream())
                .map(OdinSymbol::new)
                .collect(Collectors.toList());
    }

    public static @NotNull List<OdinFieldDeclarationStatement> getStructFieldsDeclarationStatements(OdinStructDeclarationStatement structDeclarationStatement) {
        OdinStructType structType = structDeclarationStatement
                .getStructType();
        return getStructFieldsDeclarationStatements(structType);
    }

    public static @NotNull List<OdinFieldDeclarationStatement> getStructFieldsDeclarationStatements(OdinStructType structType) {
        OdinStructBody structBody = structType
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
        return element.getParent() instanceof OdinProcedureOverloadDeclarationStatement;
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
    public static OdinScope getDeclarationsOfImportedPackage(OdinImportInfo importInfo, String sourceFilePath, Project project) {
        List<OdinSymbol> packageDeclarations = new ArrayList<>();
        if (importInfo != null) {


            List<Path> dirs = new ArrayList<>();
            String library = Objects.requireNonNullElse(importInfo.library(), "");
            Path sdkSourceDir = null;
            if (!library.isBlank()) {
                OdinSdkConfigPersistentState sdkConfig = OdinSdkConfigPersistentState.getInstance(project);
                if (sdkConfig.getSdkPath() != null) {
                    sdkSourceDir = Path.of(sdkConfig.getSdkPath(), importInfo.library());
                    dirs.add(sdkSourceDir);
                }
            }
            if (sdkSourceDir != null) {
                dirs.add(sdkSourceDir);
            }
            Path sourcePath = Path.of(sourceFilePath);
            Path currentDir = sourcePath.getParent();

            dirs.add(currentDir);


            for (Path dir : dirs) {
                Path packagePath = dir.resolve(importInfo.path());
                var importedFiles = getFilesInPackage(project, packagePath);
                for (OdinFile importedFile : importedFiles) {
                    OdinFileScope importedFileScope = importedFile.getFileScope();
                    if (importedFileScope == null) {
                        System.out.println("File scope is null for file %s" + importedFile.getVirtualFile().getPath());
                        continue;
                    }

                    Collection<OdinSymbol> fileScopeDeclarations = OdinScopeResolver.getFileScopeDeclarations(importedFileScope, PUBLIC_ELEMENTS_MATCHER);
                    packageDeclarations.addAll(fileScopeDeclarations);
                }

                if (!importedFiles.isEmpty()) {
                    return OdinScope.from(packageDeclarations, packagePath.toString());
                }
            }
        }
        return OdinScope.EMPTY;
    }

    @NotNull
    public static Map<String, OdinImportInfo> getImportStatementsInfo(OdinFileScope fileScope) {
        Map<String, OdinImportInfo> importMap = new HashMap<>();
        List<OdinImportDeclarationStatement> importStatements
                = fileScope.getImportStatements();

        for (OdinImportDeclarationStatement importStatement : importStatements) {
            OdinImportInfo importInfo = importStatement.getImportInfo();
            importMap.put(importInfo.packageName(), importInfo);
        }
        return importMap;
    }


    public static List<OdinFile> getFilesInPackage(Project project, Path importPath) {
        Predicate<VirtualFile> matcher = child -> child.getName().endsWith(".odin");
        return getFilesInPackage(project, importPath, matcher);
    }

    static @NotNull List<OdinFile> getFilesInPackage(Project project, Path importPath, Predicate<VirtualFile> matcher) {
        List<OdinFile> files = new ArrayList<>();
        VirtualFile packageDirectory = VfsUtil.findFile(importPath, true);
        if (packageDirectory != null) {
            for (VirtualFile child : packageDirectory.getChildren()) {
                if (matcher.test(child)) {
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
