package com.lasagnerd.odin.lang.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceService;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiUtilCore;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.OdinSymbolTable;
import com.lasagnerd.odin.codeInsight.imports.OdinImportUtils;
import com.lasagnerd.odin.lang.psi.*;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Setter
@Getter
public class OdinPsiElementImpl extends ASTWrapperPsiElement implements OdinPsiElement {
    protected OdinSymbolTable fullSymbolTable;

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
        fullSymbolTable = null;
    }

    @Override
    public OdinExpression parenthesesUnwrap() {
        if (this instanceof OdinParenthesizedExpression par) {
            OdinExpression expression = par.getExpression();
            if (expression != null) {
                return expression.parenthesesUnwrap();
            }
        }

        if (this instanceof OdinExpression odinExpression) {
            return odinExpression;
        }
        return null;
    }

    @Override
    public String getLocation() {
        VirtualFile containingVirtualFile = OdinImportUtils.getContainingVirtualFile(this);
        String lineColumn = OdinInsightUtils.getLineColumn(this);

        return "%s:%s".formatted(containingVirtualFile.getPath(), lineColumn);
    }

    @Override
    public OdinFile getContainingOdinFile() {
        PsiFile containingFile = getContainingFile();
        if (containingFile instanceof OdinFile)
            return (OdinFile) containingFile;
        return null;
    }
}
