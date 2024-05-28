package com.lasagnerd.odin.lang.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.lasagnerd.odin.codeInsight.OdinSymbol;
import com.lasagnerd.odin.codeInsight.OdinSymbolResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class OdinIdentifierOwner extends ASTWrapperPsiElement implements OdinDeclaredIdentifier, PsiNameIdentifierOwner {
    public OdinIdentifierOwner(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        return this;
    }

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        OdinDeclaredIdentifier declaredIdentifier = OdinPsiElementFactory.getInstance(getProject()).createDeclaredIdentifier(name);
        ASTNode currentIdentifierToken = getNode().findChildByType(OdinTypes.IDENTIFIER_TOKEN);
        ASTNode newIdentifierToken = declaredIdentifier.getNode().findChildByType(OdinTypes.IDENTIFIER_TOKEN);
        if (currentIdentifierToken != null && newIdentifierToken != null) {
            getNode().replaceChild(currentIdentifierToken, newIdentifierToken);
        }
        return this;
    }

    @Override
    public String getName() {
        return getIdentifierToken().getText();
    }

    public void accept(@NotNull OdinVisitor visitor) {

    }


    @Override
    public void delete() throws IncorrectOperationException {
        super.delete();
    }

    @Override
    public @NotNull SearchScope getUseScope() {
        OdinSymbol symbol = OdinSymbolResolver.createSymbol(this);
        switch (symbol.getVisibility()) {
            case LOCAL -> {
                OdinFileScope fileScope = PsiTreeUtil.getParentOfType(this, OdinFileScope.class, true);
                if (fileScope != null) {
                    return new LocalSearchScope(fileScope);
                }
            }
            case FILE_PRIVATE -> {
                OdinFileScope fileScope = PsiTreeUtil.getParentOfType(this, OdinFileScope.class, true);
                if (fileScope != null) {
                    return new LocalSearchScope(fileScope.getContainingFile());
                }
            }
        }


        return super.getUseScope();
    }

    private OdinFile @NotNull [] getOdinFiles(List<VirtualFile> odinFilesInPackage) {
        OdinFile[] packageFiles = new OdinFile[odinFilesInPackage.size()];
        for (int i = 0; i < odinFilesInPackage.size(); i++) {
            VirtualFile virtualFile = odinFilesInPackage.get(i);
            PsiFile file = PsiManager.getInstance(getProject()).findFile(virtualFile);
            packageFiles[i] = (OdinFile) file;
        }
        return packageFiles;
    }
}
