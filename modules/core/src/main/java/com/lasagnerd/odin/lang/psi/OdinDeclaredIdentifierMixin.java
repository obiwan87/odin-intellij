package com.lasagnerd.odin.lang.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.icons.AllIcons;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.completion.OdinCompletionContributor;
import com.lasagnerd.odin.codeInsight.imports.OdinImportUtils;
import com.lasagnerd.odin.codeInsight.symbols.OdinDeclarationSymbolResolver;
import com.lasagnerd.odin.codeInsight.symbols.OdinSdkService;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbol;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolType;
import com.lasagnerd.odin.codeInsight.typeInference.OdinInferenceEngine;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class OdinDeclaredIdentifierMixin extends ASTWrapperPsiElement implements OdinDeclaredIdentifier {
    public OdinDeclaredIdentifierMixin(@NotNull ASTNode node) {
        super(node);
    }

    public OdinSymbol createSymbol() {
        OdinDeclaration declaration = PsiTreeUtil.getParentOfType(this, OdinDeclaration.class);

        if (declaration != null) {
            List<OdinSymbol> symbols = OdinDeclarationSymbolResolver.getSymbols(declaration);
            return symbols.stream()
                    .filter(s -> s.getName().equals(this.getName()))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    public ItemPresentation getPresentation() {
        OdinSymbolType symbolType = OdinInsightUtils.classify(this);
        Icon icon = OdinCompletionContributor.getIcon(symbolType);
        if (icon == null)
            icon = AllIcons.Nodes.Property;
        return new PresentationData(this.getName(), "", icon, null);
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
        OdinSymbol symbol = createSymbol();
        switch (symbol.getVisibility()) {
            case NONE -> {
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

    private CachedValue<TsOdinType> cachedValue;

    public TsOdinType getType() {
        if (cachedValue == null) {
            cachedValue = createCachedValue();
        }
        return cachedValue.getValue();
    }

    private CachedValue<TsOdinType> createCachedValue() {
        return CachedValuesManager.getManager(getProject()).createCachedValue(
                this::computeType
        );
    }

    private CachedValueProvider.Result<TsOdinType> computeType() {
        List<Object> dependencies = new ArrayList<>();
        dependencies.add(this);
        TsOdinType tsOdinType = tryGetBuiltinType(this);
        if (tsOdinType == null) {
            return CachedValueProvider.Result.create(OdinInferenceEngine.resolveTypeOfDeclaredIdentifier(this), dependencies);
        }
        return CachedValueProvider.Result.create(tsOdinType, dependencies);
    }

    public static @Nullable TsOdinType tryGetBuiltinType(OdinDeclaredIdentifier declaredIdentifier) {
        Project project = declaredIdentifier.getProject();
        OdinSdkService instance = OdinSdkService.getInstance(project);
        if (instance == null)
            return null;

        VirtualFile builtinVirtualFile = instance.getBuiltinVirtualFile();
        @NotNull VirtualFile containingFile = OdinImportUtils.getContainingVirtualFile(declaredIdentifier);

        boolean builtinDeclaration = Objects.equals(builtinVirtualFile, containingFile);
        if (builtinDeclaration) {
            return instance.getType(declaredIdentifier.getName());
        }
        return null;
    }
}
