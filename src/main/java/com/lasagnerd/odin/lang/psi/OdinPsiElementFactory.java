package com.lasagnerd.odin.lang.psi;

import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.lasagnerd.odin.lang.OdinFileType;
import org.apache.commons.lang3.NotImplementedException;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

public class OdinPsiElementFactory {

    @Language("Odin")
    private static final String DUMMY_FILE_TEMPLATE = """
    package dummy
    
    declaredIdentifier := identifier
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
        String dummyCode = DUMMY_FILE_TEMPLATE.replaceAll("identifier", name);
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
        OdinFile file = createFile(DUMMY_FILE_TEMPLATE.replaceAll("declaredIdentifier", name));
        OdinStatement odinStatement = file.getFileScope().getStatementList().get(0);
        if(odinStatement instanceof OdinVariableInitializationStatement variableInitializationStatement) {
            return (OdinDeclaredIdentifier) variableInitializationStatement.getDeclaredIdentifiers().get(0);
        }
        throw new RuntimeException("Something went wrong.");
    }
}
