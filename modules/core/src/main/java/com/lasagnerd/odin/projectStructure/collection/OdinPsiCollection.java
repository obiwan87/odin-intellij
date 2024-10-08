package com.lasagnerd.odin.projectStructure.collection;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.impl.PsiElementBase;
import com.intellij.psi.impl.ResolveScopeManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.IncorrectOperationException;
import com.lasagnerd.odin.lang.OdinLanguage;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class OdinPsiCollection extends PsiElementBase implements PsiDirectoryContainer, NavigatablePsiElement {

    private final String collectionName;
    private final PsiDirectory psiDirectory;

    public OdinPsiCollection(String collectionName, PsiDirectory psiDirectory) {
        this.collectionName = collectionName;
        this.psiDirectory = psiDirectory;
    }

    @Override
    public @NotNull Language getLanguage() {
        return OdinLanguage.INSTANCE;
    }

    @Override
    public @NotNull PsiElement @NotNull [] getChildren() {
        return psiDirectory.getChildren();
    }

    @Override
    public PsiElement getParent() {
        return psiDirectory.getParent();
    }

    @Override
    public TextRange getTextRange() {
        return null;
    }

    @Override
    public int getStartOffsetInParent() {
        return -1;
    }

    @Override
    public int getTextLength() {
        return -1;
    }

    @Override
    public @Nullable PsiElement findElementAt(int offset) {
        return null;
    }

    @Override
    public int getTextOffset() {
        return -1;
    }

    @Override
    public String getText() {
        return collectionName;
    }

    @Override
    public char @NotNull [] textToCharArray() {
        return ArrayUtilRt.EMPTY_CHAR_ARRAY;
    }

    @Override
    public ASTNode getNode() {
        return null;
    }

    @Override
    public @NotNull Project getProject() {
        return getPsiDirectory().getProject();
    }

    @Override
    public PsiDirectory @NotNull [] getDirectories() {
        return psiDirectory.getSubdirectories();
    }

    @Override
    public PsiDirectory @NotNull [] getDirectories(@NotNull GlobalSearchScope scope) {
        return psiDirectory.getSubdirectories();
    }

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        return new OdinPsiCollection(name, psiDirectory);
    }

    @Override
    public String getName() {
        return collectionName;
    }

    @Override
    public @NotNull GlobalSearchScope getResolveScope() {
        return ResolveScopeManager.getElementResolveScope(this.getPsiDirectory());
    }

    @Override
    public boolean isEquivalentTo(PsiElement another) {
        if (another instanceof OdinPsiCollection psiCollection) {
            return PsiEquivalenceUtil.areElementsEquivalent(psiCollection.getPsiDirectory(), getPsiDirectory())
                    && collectionName.equals(psiCollection.collectionName);
        }
        return super.isEquivalentTo(another);
    }

    @Override
    public boolean canNavigate() {
        return true;
    }

    @Override
    public @NotNull PsiElement getNavigationElement() {
        return psiDirectory;
    }

    @Override
    public boolean canNavigateToSource() {
        return true;
    }
}
