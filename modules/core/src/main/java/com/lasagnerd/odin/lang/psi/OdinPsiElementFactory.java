package com.lasagnerd.odin.lang.psi;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.lang.OdinFileType;
import groovy.json.StringEscapeUtils;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class OdinPsiElementFactory {

    @Language("Odin")
    private static final String VARIABLE_DECLARATION = """
            package dummy
            
            declaredIdentifier := identifier
            """;

    @Language("Odin")
    private static final String IMPORT = """
            package dummy;
            
            import "packagePath"
            """;

    private static final String IMPORT_WITH_ALIAS = """
            package dummy;
            
            import %s "%s"
            """;

    private final Project project;

    OdinPsiElementFactory(Project project) {
        this.project = project;
    }

    public static OdinPsiElementFactory getInstance(Project project) {
        return project.getService(OdinPsiElementFactory.class);
    }

    @NotNull
    public OdinIdentifier createIdentifier(String name) {
        String dummyCode = VARIABLE_DECLARATION.replaceAll("identifier", name);
        OdinFile file = createFile(dummyCode);
        OdinStatement odinStatement = file.getFileScope().getFileScopeStatementList().getStatementList().getFirst();
        if (odinStatement instanceof OdinInitVariableStatement initVariableStatement) {
            OdinExpression odinExpression = initVariableStatement.getRhsExpressions().getExpressionList().getFirst();
            if (odinExpression instanceof OdinRefExpression refExpression) {
                return Objects.requireNonNull(refExpression.getIdentifier());
            }
        }
        throw new RuntimeException("Something went wrong.");
    }

    private OdinFile createFile(String text) {
        PsiFile fileFromText = PsiFileFactory.getInstance(project).createFileFromText("dummy.odin", OdinFileType.INSTANCE, text);
        if (fileFromText instanceof OdinFile odinFile) {
            return odinFile;
        } else {
            throw new RuntimeException("Wtf, this is not the right file type. Expected an Odin file but got: " + fileFromText);
        }
    }

    public OdinDeclaredIdentifier createDeclaredIdentifier(String name) {
        OdinFile file = createFile(VARIABLE_DECLARATION.replaceAll("declaredIdentifier", name));
        OdinStatement odinStatement = file.getFileScope().getFileScopeStatementList().getStatementList().getFirst();
        if (odinStatement instanceof OdinInitVariableStatement initVariableStatement) {
            return initVariableStatement
                    .getInitVariableDeclaration()
                    .getDeclaredIdentifiers()
                    .getFirst();
        }
        throw new RuntimeException("Something went wrong.");
    }

    public OdinInitVariableStatement createInitVariableStatement(String name, String value) {

        String var = """
                package dummy
                %s := %s
                """.formatted(name, value);
        OdinFile file = createFile(var);
        OdinStatement odinStatement = file.getFileScope().getFileScopeStatementList().getStatementList().getFirst();
        if (odinStatement instanceof OdinInitVariableStatement initVariableStatement) {
            return initVariableStatement;
        }
        throw new RuntimeException("Something went wrong.");
    }

    public OdinInitVariableStatement createInitVariableStatement(String name, String type, String value) {

        String var = """
                package dummy
                %s : %s = %s
                """.formatted(name, type, value);
        OdinFile file = createFile(var);
        OdinStatement odinStatement = file.getFileScope().getFileScopeStatementList().getStatementList().getFirst();
        if (odinStatement instanceof OdinInitVariableStatement initVariableStatement) {
            return initVariableStatement;
        }
        throw new RuntimeException("Something went wrong.");
    }

    public OdinInitVariableStatement createInitVariableStatement(String name, OdinExpression valueExpression) {
        OdinInitVariableStatement statement = createInitVariableStatement(name, "v");
        statement.getRhsExpressions().getExpressionList().getFirst().replace(valueExpression);
        return statement;
    }

    public OdinInitVariableStatement createInitVariableStatement(String name, String type, OdinExpression valueExpression) {
        OdinInitVariableStatement statement = createInitVariableStatement(name, type, "v");
        statement.getRhsExpressions().getExpressionList().getFirst().replace(valueExpression);
        return statement;
    }

    public OdinEos createEos() {

        String var = """
                package dummy
                var1 := 1
                var2 := 2
                """;
        OdinFile file = createFile(var);
        OdinEos odinEos = PsiTreeUtil.findChildOfType(file.getFileScope().getFileScopeStatementList(), OdinEos.class);
        Objects.requireNonNull(odinEos);
        return odinEos;
    }

    public OdinImportStatement createImport(String alias, String packagePath) {
        OdinFile file;
        if (alias == null) {
            file = createFile(IMPORT.replaceAll("packagePath", packagePath));
        } else {
            file = createFile(IMPORT_WITH_ALIAS.formatted(alias, packagePath));
        }
        return file.getFileScope().getImportStatements().getFirst();
    }

    @NotNull
    public OdinImportStatementsContainer createImportStatementsContainer() {
        return createImportStatementsContainer(Collections.emptyList());
    }

    @NotNull
    public OdinImportStatementsContainer createImportStatementsContainer(List<OdinImportStatement> imports) {
        String importStatements = imports.stream()
                .map(OdinImportStatement::getText)
                .reduce("", (a, b) -> a + "\n" + b);
        String dummyCode = """
                package dummy;
                
                %s
                """.formatted(importStatements);
        OdinFile file = createFile(dummyCode);
        OdinImportStatementsContainer importStatementsContainer = file.getFileScope().getImportStatementsContainer();
        Objects.requireNonNull(importStatementsContainer);
        return importStatementsContainer;
    }

    public OdinImportPath createImportPath(String importPath) {
        String dummyCode = """
                package dummy
                
                import %s
                """;

        OdinFile file = createFile(dummyCode.formatted(importPath));
        return file.getFileScope().getImportStatements().getFirst().getImportDeclaration().getImportPath();
    }

    public OdinStringLiteral createStringLiteral(String content) {
        String s = StringEscapeUtils.escapeJava(content);
        String text = """
                package dummy
                
                s := "%s"
                """.formatted(s);

        OdinFile file = createFile(text);
        return Objects.requireNonNull(PsiTreeUtil.findChildOfType(file, OdinStringLiteral.class));
    }
}
