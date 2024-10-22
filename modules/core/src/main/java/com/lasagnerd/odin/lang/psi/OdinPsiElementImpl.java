package com.lasagnerd.odin.lang.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceService;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiUtilCore;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolTable;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Setter
@Getter
public class OdinPsiElementImpl extends ASTWrapperPsiElement implements OdinPsiElement {

    protected OdinSymbolTable symbolTable;

    public OdinPsiElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public String toString() {
        IElementType elementType = PsiUtilCore.getElementType(this);
        if (elementType != null) {
            return elementType.toString();
        }
        return super.toString();
    }

    @Override
    public PsiReference @NotNull [] getReferences() {
        PsiReference[] references = super.getReferences();
        if (references.length == 0 && this instanceof OdinImportPath) {
            return ReferenceProvidersRegistry.getReferencesFromProviders(this, PsiReferenceService.Hints.HIGHLIGHTED_REFERENCES);
        }
        return references;
    }

    @Override
    public void subtreeChanged() {
        symbolTable = null;
    }
}
