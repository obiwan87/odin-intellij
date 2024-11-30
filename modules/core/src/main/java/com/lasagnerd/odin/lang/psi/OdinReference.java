package com.lasagnerd.odin.lang.psi;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.symbols.*;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinParameter;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinParameterOwner;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class OdinReference extends PsiReferenceBase<OdinIdentifier> {
    public static Logger LOG = Logger.getInstance(OdinReference.class);

    @Getter
    private OdinSymbol symbol;
    @Getter
    private PsiElement resolvedReference;

    boolean resolved = false;

    private OdinSymbolTable symbolTable;

    public OdinReference(@NotNull OdinIdentifier element) {
        super(element);
    }

    public OdinSymbol findSymbol() {
        OdinSymbolTable symbolTable = OdinSymbolTableResolver.findSymbolTable(getElement());
        return findSymbol(symbolTable);
    }

    public OdinSymbol findSymbol(OdinSymbolTable parentSymbolTable) {
        @NotNull OdinIdentifier identifier = getElement();
        PsiElement parent = identifier.getParent();
        OdinSymbolTable symbolTable;
        if (parent instanceof OdinRefExpression refExpression) {
            if (refExpression.getExpression() != null) {
                symbolTable = OdinReferenceResolver.resolve(refExpression.getExpression());
            } else {
                symbolTable = parentSymbolTable;
            }
        } else if (parent instanceof OdinImplicitSelectorExpression implicitSelectorExpression) {
            TsOdinType tsOdinType = implicitSelectorExpression.getInferredType();
            if (!tsOdinType.isUnknown()) {
                symbolTable = OdinInsightUtils.getTypeElements(identifier.getProject(), tsOdinType);
            } else {
                // TODO This might lead to resolving to a wrong symbol that has the same name
                symbolTable = parentSymbolTable;
            }
        } else {
            OdinQualifiedType qualifiedType = PsiTreeUtil.getParentOfType(identifier, OdinQualifiedType.class);
            if (qualifiedType != null) {
                if (qualifiedType.getPackageIdentifier() == identifier) {
                    symbolTable = parentSymbolTable;
                } else {
                    symbolTable = OdinReferenceResolver.resolve(qualifiedType);
                }
            } else {
                symbolTable = parentSymbolTable;
            }
        }

        if (symbolTable == OdinSymbolTable.EMPTY || symbolTable == null) {
            symbolTable = parentSymbolTable;
        }

        if (symbolTable != null) {
            return symbolTable.getSymbol(identifier.getIdentifierToken().getText());
        }

        return null;
    }

    @Override
    public @NotNull TextRange getAbsoluteRange() {
        return getRangeInElement().shiftRight(getElement().getIdentifierToken().getTextRange().getStartOffset());
    }

    @Override
    public @Nullable PsiElement resolve() {
        if (!this.resolved) {
            this.symbol = doResolve();
            this.resolvedReference = resolveSymbol(this.symbol);
            this.resolved = true;
        }

        return this.resolvedReference;
    }

    private static PsiElement resolveSymbol(OdinSymbol symbol) {
        if (symbol != null) {
            if (symbol.getSymbolType() == OdinSymbolType.PACKAGE_REFERENCE) {
                PsiNamedElement declaredIdentifier = symbol.getDeclaredIdentifier();
                if (declaredIdentifier instanceof OdinImportDeclarationStatement importDeclarationStatement) {
                    // TODO here we only resolve to import declaration, however, when wants to jump to declaration
                    //  we want to open the path. How do do that? HintedReferenceHost?
//                    return OdinPackageReference.resolvePackagePathDirectory(importDeclarationStatement.getImportPath());
                    return importDeclarationStatement;
                } else {
                    return declaredIdentifier;
                }
            } else {
                return symbol.getDeclaredIdentifier();
            }
        }
        return null;
    }

    private @Nullable OdinSymbol doResolve() {
        if (getElement().getParent() instanceof OdinImplicitSelectorExpression implicitSelectorExpression) {
            TsOdinType tsOdinType = implicitSelectorExpression.getInferredType();
            OdinSymbolTable typeElements = OdinInsightUtils.getTypeElements(getElement().getProject(), tsOdinType);
            return typeElements.getSymbol(getElement().getText());
        }

        if (getElement().getParent() instanceof OdinNamedArgument namedArgument) {
            OdinInsightUtils.OdinCallInfo callInfo = OdinInsightUtils.getCallInfo(namedArgument);
            if (callInfo.callingType() instanceof TsOdinParameterOwner parameterOwner) {
                List<TsOdinParameter> parameters = parameterOwner.getParameters();
                TsOdinParameter tsOdinParameter = parameters.stream()
                        .filter(p -> p.getName().equals(namedArgument.getIdentifier().getText()))
                        .findFirst().orElse(null);

                if (tsOdinParameter != null) {
                    return tsOdinParameter.toSymbol();
                }
            }
        }

        try {
            OdinSymbol symbol = findSymbol();
            if (symbol != null) {
                if (!OdinInsightUtils.isVisible(getElement(), symbol) && symbol.getSymbolType() == OdinSymbolType.PACKAGE_REFERENCE) {
                    return null;
                }
                return symbol;
            }
            return null;
        } catch (StackOverflowError e) {
            OdinInsightUtils.logStackOverFlowError(getElement(), LOG);
            return null;
        }
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
