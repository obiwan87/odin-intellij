package com.lasagnerd.odin.lang.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;

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

    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinParamDeclaration statement) {
        return Collections.singletonList(statement.getDeclaredIdentifier());
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
        return statement.getParamDeclarationList().stream().map(OdinParamDeclaration::getDeclaredIdentifier).toList();
    }

    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinParameterInitialization statement) {
        return statement.getParamDeclarationList().stream().map(OdinParamDeclaration::getDeclaredIdentifier).toList();
    }

    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinStructDeclarationStatement statement) {
        return Collections.singletonList(statement.getDeclaredIdentifier());
    }


}
