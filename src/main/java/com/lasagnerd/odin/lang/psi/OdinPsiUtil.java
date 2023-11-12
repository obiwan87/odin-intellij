package com.lasagnerd.odin.lang.psi;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

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

    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinPackageDeclaration statement) {
        return Collections.singletonList(statement.getDeclaredIdentifier());
    }

    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinUnionDeclarationStatement statement) {
        return Collections.singletonList(statement.getDeclaredIdentifier());
    }

    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinProcedureDeclarationStatement statement) {
        return Collections.singletonList(statement.getDeclaredIdentifier());
    }

    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinVariableInitializationStatement statement) {
        return statement.getIdentifierList().getDeclaredIdentifierList();
    }

    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinBitsetDeclarationStatement statement) {
        return Collections.singletonList(statement.getDeclaredIdentifier());
    }

    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinParameterDeclaration statement) {
        return statement.getDeclaredIdentifiers();
    }

    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinForeignProcedureDeclarationStatement statement) {
        return Collections.singletonList(statement.getDeclaredIdentifier());
    }

    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinConstantInitializationStatement statement) {
        return statement.getIdentifierList().getDeclaredIdentifierList();
    }

    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinVariableDeclarationStatement statement) {
        return statement.getIdentifierList().getDeclaredIdentifierList();
    }

    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinEnumDeclarationStatement statement) {
        return Collections.singletonList(statement.getDeclaredIdentifier());
    }

    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinFieldDeclarationStatement statement) {
        return statement.getDeclaredIdentifierList();
    }

    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinParameterDeclarationStatement statement) {
        return statement.getParameterList().stream().map(OdinParameter::getDeclaredIdentifier).toList();
    }

    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinParameterInitialization statement) {
        return statement.getParameterList().stream().map(OdinParameter::getDeclaredIdentifier).toList();
    }

    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinStructDeclarationStatement statement) {
        return Collections.singletonList(statement.getDeclaredIdentifier());
    }

    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinForeignImportDeclarationStatement statement) {
        if (statement.getAlias() != null)
            return Collections.singletonList(statement.getAlias());
        return Collections.emptyList();
    }

    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinImportDeclarationStatement statement) {
        if (statement.getAlias() != null)
            return Collections.singletonList(statement.getAlias());
        return Collections.emptyList();
    }

    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinEnumValueDeclaration declaration) {
        return Collections.singletonList(declaration.getDeclaredIdentifier());
    }

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

    public static OdinTypeDefinitionExpression getTypeDefinition(OdinParameterDeclaration ignored) {
        throw new RuntimeException("This shouldn't be called! you have a bug somewhere!");
    }

    public static OdinTypeDefinitionExpression getTypeDefinition(OdinParameterInitialization declaration) {
        if(declaration.getExpressionList().size() > 1) {
            return doGetTypeDefinitionExpression(declaration.getExpressionList().get(0));
        }
        return null;
    }

    public static OdinTypeDefinitionExpression getTypeDefinition(OdinUnnamedParameter parameter) {
        return doGetTypeDefinitionExpression(parameter.getExpression());
    }


    @Nullable
    private static OdinTypeDefinitionExpression doGetTypeDefinitionExpression(OdinExpression statement) {
        if(statement instanceof OdinTypeDefinitionExpression typeDefinition) {
            return typeDefinition;
        }
        return null;
    }


    public static PsiElement getType(OdinExpression expression) {
        return null;
    }

}
