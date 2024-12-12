package com.lasagnerd.odin.codeInsight.symbols;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.codeInsight.OdinContext;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.OdinSymbolTable;
import com.lasagnerd.odin.codeInsight.dataflow.OdinLattice;
import com.lasagnerd.odin.codeInsight.dataflow.OdinWhenConstraintsSolver;
import com.lasagnerd.odin.codeInsight.imports.OdinImportService;
import com.lasagnerd.odin.codeInsight.symbols.symbolTable.OdinMinimalSymbolTableBuilder;
import com.lasagnerd.odin.codeInsight.symbols.symbolTable.OdinSymbolTableBuilder;
import com.lasagnerd.odin.codeInsight.symbols.symbolTable.OdinSymbolTableBuilderListener;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinParameter;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinParameterOwner;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinType;
import com.lasagnerd.odin.lang.psi.*;
import one.util.streamex.MoreCollectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class OdinReferenceResolver {

    // TODO Reference resolver also, like type resolver and inference engine, needs context
    //  Example 1:
    //  In a when-block where the value of ODIN_OS/ODIN_ARCH is statically computable
    //  we need that information to import the correct files.
    //  Example 2:
    //  Same as above, but with build flags (?)

    // see https://odin-lang.org/docs/overview/#file-suffixes
    public static @Nullable OdinSymbol resolve(@NotNull OdinContext context, @NotNull OdinIdentifier element) {
        return resolve(context, element,
                new OdinMinimalSymbolTableBuilder(element,
                        OdinImportService.packagePath(element),
                        createCheckpointListener(element),
                        context
                )
                , false);
    }

    private static @NotNull OdinSymbolTableBuilderListener createEndOfBlockListener(@NotNull OdinIdentifier element) {
        return new OdinSymbolTableBuilderListener() {
            @Override
            public boolean onBlockConsumed(OdinSymbolTable symbolTable, OdinScopeBlock scopeBlock) {
                return symbolTable.getSymbol(element.getText()) != null;
            }
        };
    }

    private static @NotNull OdinSymbolTableBuilderListener createCheckpointListener(@NotNull OdinIdentifier element) {
        return new OdinSymbolTableBuilderListener() {
            @Override
            public boolean onCheckpointCalled(OdinSymbolTable symbolTable) {
                return symbolTable.getSymbol(element.getText()) != null;
            }
        };
    }

    // see https://odin-lang.org/docs/overview/#file-suffixes
    public static @Nullable OdinSymbol resolve(@NotNull OdinContext context,
                                               @NotNull OdinIdentifier element,
                                               OdinSymbolTableBuilder contextProvider,
                                               boolean applyKnowledge) {
        try {
            if (element.getParent() instanceof OdinImplicitSelectorExpression implicitSelectorExpression) {
                TsOdinType tsOdinType = implicitSelectorExpression.getInferredType(context);
                OdinSymbolTable typeElements = OdinInsightUtils.getTypeElements(element.getProject(), tsOdinType);
                return typeElements.getSymbol(element.getText());
            } else if (element.getParent() instanceof OdinNamedArgument namedArgument) {
                OdinInsightUtils.OdinCallInfo callInfo = OdinInsightUtils.getCallInfo(context, namedArgument);
                if (callInfo.callingType() instanceof TsOdinParameterOwner parameterOwner) {
                    List<TsOdinParameter> parameters = parameterOwner.getParameters();
                    TsOdinParameter tsOdinParameter = parameters.stream()
                            .filter(p -> p.getName().equals(namedArgument.getIdentifier().getText()))
                            .findFirst().orElse(null);

                    if (tsOdinParameter != null) {
                        return tsOdinParameter.toSymbol();
                    }
                }
            } else {
                OdinContext symbolContext = getIdentifierContext(context, element, contextProvider);
                String name = element.getIdentifierToken().getText();

                List<OdinSymbol> symbols = symbolContext.getSymbols(name);

                symbols = symbols.stream()
                        .filter(s -> OdinInsightUtils.isVisible(element, s)
                                || s.getSymbolType() != OdinSymbolType.PACKAGE_REFERENCE)
                        .toList();
                symbols = symbols.stream()
                        .collect(MoreCollectors.distinctBy(OdinSymbol::getDeclaredIdentifier));
                if (!symbols.isEmpty()) {
                    if (applyKnowledge) {
                        OdinLattice sourceLattice = OdinWhenConstraintsSolver.solveLattice(symbolContext, element);
                        symbols = symbols.stream().filter(
                                s -> {
                                    OdinLattice targetLattice = OdinWhenConstraintsSolver
                                            .solveLattice(context, s.getDeclaredIdentifier());
                                    return sourceLattice.isSubset(targetLattice);
                                }
                        ).toList();

                    }
                    if (symbols.size() == 1) {
                        return symbols.getFirst();
                    }
                    return null;
                }

                return null;
            }
            return null;
        } catch (StackOverflowError e) {
            OdinInsightUtils.logStackOverFlowError(element, OdinReference.LOG);
            return null;
        }
    }

    // Computes the context under which the identifier is expected to be defined
    static OdinContext getIdentifierContext(OdinContext context, @NotNull OdinIdentifier element, OdinSymbolTableBuilder symbolTableProvider) {
        @NotNull OdinIdentifier identifier = element;
        PsiElement parent = identifier.getParent();
        if (parent instanceof OdinRefExpression refExpression) {
            if (refExpression.getExpression() != null) {
                return OdinInsightUtils.getReferenceableSymbols(refExpression.getExpression()).asContext();
            } else {
                return symbolTableProvider.build().asContext();
            }
        } else {
            OdinQualifiedType qualifiedType = PsiTreeUtil.getParentOfType(identifier, OdinQualifiedType.class);
            if (qualifiedType != null) {
                if (qualifiedType.getPackageIdentifier() == identifier) {
                    return symbolTableProvider.build().asContext();
                } else {
                    return OdinInsightUtils.getReferenceableSymbols(context, qualifiedType).asContext();
                }
            } else if (parent instanceof OdinSimpleRefType) {
                return symbolTableProvider.build().asContext();
            } else {
                return OdinContext.EMPTY;
            }
        }
    }

}


