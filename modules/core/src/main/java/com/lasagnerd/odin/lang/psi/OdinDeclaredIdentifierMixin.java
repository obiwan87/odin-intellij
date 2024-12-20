package com.lasagnerd.odin.lang.psi;

import com.intellij.icons.AllIcons;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.ParameterizedCachedValue;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.lasagnerd.odin.codeInsight.OdinContext;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.completion.OdinCompletionContributor;
import com.lasagnerd.odin.codeInsight.sdk.OdinSdkService;
import com.lasagnerd.odin.codeInsight.symbols.OdinDeclarationSymbolResolver;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbol;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolType;
import com.lasagnerd.odin.codeInsight.typeInference.OdinInferenceEngine;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinType;
import com.lasagnerd.odin.lang.psi.impl.OdinPsiElementImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public abstract class OdinDeclaredIdentifierMixin extends OdinPsiElementImpl implements OdinDeclaredIdentifier {
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

    private ParameterizedCachedValue<TsOdinType, OdinContext> cachedValue;

    public TsOdinType getType(OdinContext context) {
        if (!context.isUseCache()) {
            return resolveType(context);
        }
        if (cachedValue == null) {
            cachedValue = createCachedValue();
        }
        return cachedValue.getValue(context);
    }

    private ParameterizedCachedValue<TsOdinType, OdinContext> createCachedValue() {
        return CachedValuesManager.getManager(getProject()).createParameterizedCachedValue(
                this::computeType,
                false
        );
    }

    private CachedValueProvider.Result<TsOdinType> computeType(OdinContext context) {
        List<Object> dependencies = new ArrayList<>();
        dependencies.add(this);
        TsOdinType result = resolveType(context);
        return CachedValueProvider.Result.create(result, dependencies);
    }

    private @NotNull TsOdinType resolveType(OdinContext context) {
        TsOdinType result;
        TsOdinType tsOdinType = tryGetBuiltinType(this);
        if (tsOdinType == null || tsOdinType.isUnknown()) {
            result = OdinInferenceEngine.resolveTypeOfDeclaredIdentifier(context, this);
        } else {
            result = tsOdinType;
        }
        return result;
    }

    public static @Nullable TsOdinType tryGetBuiltinType(OdinDeclaredIdentifier declaredIdentifier) {
        Project project = declaredIdentifier.getProject();
        OdinSdkService instance = OdinSdkService.getInstance(project);
        boolean builtinDeclaration = OdinSdkService.isInBuiltinOdinFile(declaredIdentifier);
        if (builtinDeclaration) {
            return instance.getType(declaredIdentifier.getName());
        }
        return null;
    }

}
