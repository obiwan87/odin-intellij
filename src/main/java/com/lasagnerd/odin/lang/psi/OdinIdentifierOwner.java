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
import com.lasagnerd.odin.codeInsight.symbols.OdinDeclarationSymbolResolver;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolTableResolver;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbol;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public abstract class OdinIdentifierOwner extends ASTWrapperPsiElement implements OdinDeclaredIdentifier, PsiNameIdentifierOwner {
    public OdinIdentifierOwner(@NotNull ASTNode node) {
        super(node);
    }

    public static OdinSymbol createSymbol(OdinDeclaredIdentifier declaredIdentifier) {
        AtomicReference<OdinDeclaration> declaration = new AtomicReference<>();
        PsiElement declaringParent = PsiTreeUtil.findFirstParent(declaredIdentifier, parent -> {
            if(parent instanceof OdinDeclaration odinDeclaration) {
                if(odinDeclaration.getDeclaredIdentifiers().contains(declaredIdentifier)) {
                    declaration.set(odinDeclaration);
                }
            }
            return parent instanceof OdinScopeBlock;
        });

        if(declaringParent instanceof OdinFileScope fileScope) {
            Collection<OdinAttributeStatement> attributeStatements = PsiTreeUtil.findChildrenOfType(declaration.get(), OdinAttributeStatement.class);
            OdinSymbol.OdinVisibility globalFileVisibility = OdinSymbolTableResolver.getGlobalFileVisibility(fileScope);
            return new OdinSymbol(declaredIdentifier, OdinDeclarationSymbolResolver.getVisibility(attributeStatements, globalFileVisibility));
        }

        if(declaringParent instanceof OdinScopeBlock) {
            if(declaration.get() != null) {
                if(declaration.get().getParent() instanceof OdinFileScopeStatementList fileScopeStatementList) {
                    Collection<OdinAttributeStatement> attributeStatements = PsiTreeUtil.findChildrenOfType(declaration.get(), OdinAttributeStatement.class);
                    OdinFileScope fileScopeStatementListParent = (OdinFileScope) fileScopeStatementList.getParent();
                    OdinSymbol.OdinVisibility globalFileVisibility = OdinSymbolTableResolver.getGlobalFileVisibility(fileScopeStatementListParent);
                    return new OdinSymbol(declaredIdentifier, OdinDeclarationSymbolResolver.getVisibility(attributeStatements, globalFileVisibility));
                }
            }
            return new OdinSymbol(declaredIdentifier, OdinSymbol.OdinVisibility.LOCAL);
        }
        return new OdinSymbol(declaredIdentifier);
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
        // TODO
        OdinSymbol symbol = createSymbol(this);
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
