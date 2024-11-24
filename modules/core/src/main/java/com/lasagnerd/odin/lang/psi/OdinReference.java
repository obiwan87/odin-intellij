package com.lasagnerd.odin.lang.psi;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.LineColumn;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.imports.OdinImportService;
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

    public OdinReference(@NotNull OdinIdentifier element) {
        super(element);
    }

    public static OdinSymbol findSymbol(@NotNull OdinIdentifier identifier) {
        String packagePath = OdinImportService
                .getInstance(identifier.getProject())
                .getPackagePath(identifier);

        OdinSymbolTable symbolTable = OdinSymbolTableResolver
                .computeSymbolTable(identifier)
                .with(packagePath);

        return findSymbol(identifier, symbolTable);
    }

    public static OdinSymbol findSymbol(@NotNull OdinIdentifier identifier, OdinSymbolTable parentSymbolTable) {
        PsiElement parent = identifier.getParent();
        OdinSymbolTable symbolTable;
        if (parent instanceof OdinRefExpression refExpression) {
            if (refExpression.getExpression() != null) {
                symbolTable = OdinReferenceResolver.resolve(parentSymbolTable, refExpression.getExpression());
            } else {
                symbolTable = parentSymbolTable;
            }
        } else if (parent instanceof OdinImplicitSelectorExpression implicitSelectorExpression) {
            TsOdinType tsOdinType = implicitSelectorExpression.getInferredType(parentSymbolTable);
            if (!tsOdinType.isUnknown()) {
                symbolTable = OdinInsightUtils.getTypeElements(identifier.getProject(), tsOdinType);
            } else {
                symbolTable = parentSymbolTable;
            }
        } else {
            OdinQualifiedType qualifiedType = PsiTreeUtil.getParentOfType(identifier, OdinQualifiedType.class);
            if (qualifiedType != null) {
                if (qualifiedType.getPackageIdentifier() == identifier) {
                    symbolTable = parentSymbolTable;
                } else {
                    symbolTable = OdinReferenceResolver.resolve(parentSymbolTable, qualifiedType);
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
                    return OdinPackageReference.resolvePackagePathDirectory(importDeclarationStatement.getImportPath());
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
            OdinSymbolTable symbolTable = OdinSymbolTableResolver.computeSymbolTable(getElement());
            OdinInsightUtils.OdinCallInfo callInfo = OdinInsightUtils.getCallInfo(symbolTable, namedArgument);
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
            OdinSymbol symbol = findSymbol(getElement());
            if (symbol != null) {
                if (!OdinInsightUtils.isVisible(getElement(), symbol) && symbol.getSymbolType() == OdinSymbolType.PACKAGE_REFERENCE) {
                    return null;
                }
                return symbol;
            }
            return null;
        } catch (StackOverflowError e) {
            logStackOverFlowError(getElement(), LOG);
            return null;
        }
    }

    public static void logStackOverFlowError(@NotNull OdinIdentifier element, Logger log) {
        String text = element.getText();
        int textOffset = element.getTextOffset();
        PsiFile containingFile = element.getContainingFile();
        String fileName = "UNKNOWN";
        if (containingFile != null) {
            VirtualFile virtualFile = containingFile.getVirtualFile();
            if (virtualFile != null) {
                fileName = virtualFile.getCanonicalPath();
            }
            LineColumn lineColumn = StringUtil.offsetToLineColumn(containingFile.getText(), textOffset);
            log.error("Stack overflow caused by element with text '%s' in %s:%d:%d".formatted(text,
                    fileName,
                    lineColumn.line + 1,
                    lineColumn.column + 1));
        } else {
            log.error("Stack overflow caused by element with text '%s'".formatted(text));
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
