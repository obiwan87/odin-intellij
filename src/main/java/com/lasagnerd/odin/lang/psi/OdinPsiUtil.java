package com.lasagnerd.odin.lang.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.insights.ImportInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class OdinPsiUtil {
    public static PsiReference getReference(OdinIdentifier self) {
        return new OdinReference(self);
    }

    public static PsiElement getOperator(OdinBinaryExpression self) {
        return self.getChildren().length > 1 ? self.getChildren()[1] : null;
    }

    public static OdinCompoundValueBody getCompoundValueBody(OdinCompoundValue self) {
        return PsiTreeUtil.findChildOfType(self, OdinCompoundValueBody.class);
    }

    public static List<OdinImportDeclarationStatement> getImportStatements(OdinFileScope self) {
        return PsiTreeUtil.getChildrenOfTypeAsList(self, OdinImportDeclarationStatement.class);
    }

    // OdinDeclaration

    public static List<? extends PsiNamedElement> getDeclaredIdentifiers(OdinPackageDeclaration statement) {
        return Collections.singletonList(statement.getDeclaredIdentifier());
    }

    public static List<? extends PsiNamedElement> getDeclaredIdentifiers(OdinUnionDeclarationStatement statement) {
        return Collections.singletonList(statement.getDeclaredIdentifier());
    }

    public static List<? extends PsiNamedElement> getDeclaredIdentifiers(OdinProcedureDeclarationStatement statement) {
        return Collections.singletonList(statement.getDeclaredIdentifier());
    }

    public static List<? extends PsiNamedElement> getDeclaredIdentifiers(OdinVariableInitializationStatement statement) {
        return statement.getIdentifierList().getDeclaredIdentifierList();
    }

    public static List<? extends PsiNamedElement> getDeclaredIdentifiers(OdinBitsetDeclarationStatement statement) {
        return Collections.singletonList(statement.getDeclaredIdentifier());
    }

    public static List<? extends PsiNamedElement> getDeclaredIdentifiers(OdinParameterDeclaration statement) {
        return statement.getDeclaredIdentifiers();
    }

    public static List<? extends PsiNamedElement> getDeclaredIdentifiers(OdinForeignProcedureDeclarationStatement statement) {
        return Collections.singletonList(statement.getDeclaredIdentifier());
    }

    public static List<? extends PsiNamedElement> getDeclaredIdentifiers(OdinConstantInitializationStatement statement) {
        return statement.getIdentifierList().getDeclaredIdentifierList();
    }

    public static List<? extends PsiNamedElement> getDeclaredIdentifiers(OdinVariableDeclarationStatement statement) {
        return statement.getIdentifierList().getDeclaredIdentifierList();
    }

    public static List<? extends PsiNamedElement> getDeclaredIdentifiers(OdinEnumDeclarationStatement statement) {
        return Collections.singletonList(statement.getDeclaredIdentifier());
    }

    public static List<? extends PsiNamedElement> getDeclaredIdentifiers(OdinFieldDeclarationStatement statement) {
        return statement.getDeclaredIdentifierList();
    }



    public static List<? extends PsiNamedElement> getDeclaredIdentifiers(OdinStructDeclarationStatement statement) {
        return Collections.singletonList(statement.getDeclaredIdentifier());
    }

    public static List<? extends PsiNamedElement> getDeclaredIdentifiers(OdinForeignImportDeclarationStatement statement) {
        if (statement.getAlias() != null)
            return Collections.singletonList(statement.getAlias());
        return Collections.emptyList();
    }

    public static List<? extends PsiNamedElement> getDeclaredIdentifiers(OdinImportDeclarationStatement statement) {
        if (statement.getAlias() != null)
            return Collections.singletonList(statement.getAlias());
        return Collections.emptyList();
    }

    public static List<? extends PsiNamedElement> getDeclaredIdentifiers(OdinEnumValueDeclaration declaration) {
        return Collections.singletonList(declaration.getDeclaredIdentifier());
    }


    public static List<? extends PsiNamedElement> getDeclaredIdentifiers(OdinParameterDeclarationStatement statement) {
        return statement.getParameterList().stream().map(OdinParameter::getDeclaredIdentifier).toList();
    }

    public static List<? extends PsiNamedElement> getDeclaredIdentifiers(OdinParameterInitialization statement) {
        return statement.getParameterList().stream().map(OdinParameter::getDeclaredIdentifier).toList();
    }



    // OdinTypedDeclaration

    public static OdinTypeDefinitionExpression getTypeDefinition(OdinConstantInitializationStatement statement) {
        return doGetTypeDefinitionExpression(statement.getExpression());
    }

    public static OdinTypeDefinitionExpression getTypeDefinition(OdinVariableDeclarationStatement statement) {
        return doGetTypeDefinitionExpression(statement.getExpression());
    }

    public static OdinTypeDefinitionExpression getTypeDefinition(OdinParameterDeclarationStatement statement) {
        return doGetTypeDefinitionExpression(statement.getExpression());
    }

    public static OdinTypeDefinitionExpression getTypeDefinition(OdinVariableInitializationStatement statement) {
        return doGetTypeDefinitionExpression(statement.getExpression());
    }

    public static OdinTypeDefinitionExpression getTypeDefinition(OdinFieldDeclarationStatement statement) {
        return doGetTypeDefinitionExpression(statement.getExpression());
    }

    public static OdinTypeDefinitionExpression getTypeDefinition(OdinVariadicParameterDeclaration variadicParameterDeclaration) {
        return doGetTypeDefinitionExpression(variadicParameterDeclaration.getExpression());
    }

    public static OdinTypeDefinitionExpression getTypeDefinition(OdinUnnamedParameter parameter) {
        return doGetTypeDefinitionExpression(parameter.getExpression());
    }

    public static OdinTypeDefinitionExpression getTypeDefinition(OdinParameterDeclaration parameterDeclaration) {
        throw new RuntimeException("This shouldn't be called! you have a bug somewhere: " + parameterDeclaration.getClass().getSimpleName());
    }

    public static OdinTypeDefinitionExpression getTypeDefinition(OdinParameterInitialization declaration) {
        if (declaration.getExpressionList().size() > 1) {
            return doGetTypeDefinitionExpression(declaration.getExpressionList().get(0));
        }
        return null;
    }

    // OdinImportDeclaration

    public static String getName(OdinImportDeclarationStatement importStatement) {
        return importStatement.getImportInfo().packageName();
    }

    public static PsiElement setName(OdinImportDeclarationStatement importStatement, @NotNull String name) {
        return importStatement;
    }

    public static PsiElement getNameIdentifier(OdinImportDeclarationStatement importStatement) {
        if (importStatement.getAlias() != null)
            return importStatement.getAlias();
        return importStatement.getPath();
    }


    @Nullable
    private static OdinTypeDefinitionExpression doGetTypeDefinitionExpression(OdinExpression statement) {
        if (statement instanceof OdinTypeDefinitionExpression typeDefinition) {
            return typeDefinition;
        }
        return null;
    }


    public static PsiElement getType(OdinExpression ignore) {
        return null;
    }

    @NotNull
    public static ImportInfo getImportInfo(OdinImportDeclarationStatement importStatement) {
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
            // Last path segment is the packageName
            String[] pathParts = path.split("/");
            name = pathParts[pathParts.length - 1];
        }

        return new ImportInfo(name, path, library);
    }
}
