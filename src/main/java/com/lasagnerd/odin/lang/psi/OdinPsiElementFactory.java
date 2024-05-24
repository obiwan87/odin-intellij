package com.lasagnerd.odin.lang.psi;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.lasagnerd.odin.lang.OdinFileType;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

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
        OdinStatement odinStatement = file.getFileScope().getStatementList().get(0);
        if(odinStatement instanceof OdinVariableInitializationStatement variableInitializationStatement) {
            OdinExpression odinExpression = variableInitializationStatement.getExpressionsList().getExpressionList().get(0);
            if(odinExpression instanceof OdinRefExpression refExpression) {
                return refExpression.getIdentifier();
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
        OdinStatement odinStatement = file.getFileScope().getStatementList().get(0);
        if(odinStatement instanceof OdinVariableInitializationStatement variableInitializationStatement) {
            return (OdinDeclaredIdentifier) variableInitializationStatement.getDeclaredIdentifiers().get(0);
        }
        throw new RuntimeException("Something went wrong.");
    }

    public OdinImportDeclarationStatement createImport(String relativePath) {
        OdinFile file = createFile(IMPORT.replaceAll("packagePath", relativePath));
        return file.getFileScope().getImportStatements().get(0);
    }
}
