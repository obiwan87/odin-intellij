package com.lasagnerd.odin.lang.psi;

import com.intellij.mock.MockProject;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.util.IncorrectOperationException;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbol;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class OdinReference extends PsiReferenceBase<OdinIdentifier> {
    public static Logger LOG = Logger.getInstance(OdinReference.class);

    private final OdinSymbol symbol;
    private final PsiElement resolvedReference;

    public OdinReference(OdinIdentifier element, OdinSymbol symbol) {
        super(element);
        this.symbol = symbol;
        if (this.symbol != null) {
            this.resolvedReference = getDeclaredIdentifier(this.symbol);
        } else {
            this.resolvedReference = null;
        }

    }

    @Override
    public @NotNull TextRange getAbsoluteRange() {
        return getRangeInElement().shiftRight(getElement().getIdentifierToken().getTextRange().getStartOffset());
    }

    @Override
    public @Nullable PsiElement resolve() {
        return resolvedReference;
    }

    private static PsiElement getDeclaredIdentifier(OdinSymbol symbol) {
        if (symbol != null) {
            if (symbol.getSymbolType() == OdinSymbolType.PACKAGE_REFERENCE) {
                PsiNamedElement declaredIdentifier = symbol.getDeclaredIdentifier();
                if (declaredIdentifier instanceof OdinImportDeclaration importDeclaration) {
                    // TODO here we only resolve to import declaration, however, when wants to jump to declaration
                    //  we want to open the path. How do do that? HintedReferenceHost?
                    if (declaredIdentifier.getProject() instanceof MockProject) {
                        return importDeclaration;
                    }
                    return OdinPackageReference.resolvePackagePathDirectory(importDeclaration.getImportPath());
                } else {
                    return declaredIdentifier;
                }
            } else {
                return symbol.getDeclaredIdentifier();
            }
        }
        return null;
    }

    @Override
    public Object @NotNull [] getVariants() {
        return new Object[0];
    }

    @Override
    public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
        if (element instanceof PsiDirectory) {
            return element;
        }
        return super.bindToElement(element);
    }
}
