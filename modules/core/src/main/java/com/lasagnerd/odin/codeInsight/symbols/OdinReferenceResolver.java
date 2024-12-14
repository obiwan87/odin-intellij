package com.lasagnerd.odin.codeInsight.symbols;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.codeInsight.OdinContext;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.OdinSymbolTable;
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

import java.util.Collections;
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
        // Here we need to check if we are in a when block or if there is knowledge from build flags
        // In that case we use the slower, but more precise, end of block listener,
        // which ensures that all symbols in one block are gathered. These can then be pruned
        // using the knowledge at hand.


        return resolve(context, element,
                new OdinMinimalSymbolTableBuilder(element,
                        OdinImportService.packagePath(element),
                        createEndOfBlockListener(element),
                        context
                )
        );
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
                                               @NotNull OdinIdentifier identifier,
                                               OdinSymbolTableBuilder contextProvider) {
        System.out.println("Resolving reference to " + identifier.getText() + ":" + identifier.getLocation());

        try {
            var identifierSymbols = getIdentifierSymbolTable(context, identifier, contextProvider);
            String name = identifier.getIdentifierToken().getText();

            List<OdinSymbol> symbols = identifierSymbols.getSymbols(name);

            symbols = symbols.stream()
                    .filter(s -> OdinInsightUtils.isVisible(identifier, s)
                            || s.getSymbolType() != OdinSymbolType.PACKAGE_REFERENCE)
                    .toList();
            symbols = symbols.stream()
                    .collect(MoreCollectors.distinctBy(OdinSymbol::getDeclaredIdentifier));
            if (!symbols.isEmpty()) {
//                    if (applyKnowledge) {
//                        System.out.println("Solving lattice for " + identifier.getText() + ":" + identifier.getLocation());
//                        OdinLattice sourceLattice = OdinWhenConstraintsSolver.solveLattice(context, identifier);
//                        symbols = symbols.stream()
//                                .filter(
//                                        s -> {
//                                            OdinLattice targetLattice = OdinWhenConstraintsSolver
//                                                    .solveLattice(context, (OdinPsiElement) s.getDeclaredIdentifier());
//                                            return sourceLattice.isSubset(targetLattice);
//                                        }
//                                ).toList();
//
//                    }
                if (symbols.size() == 1) {
                    return symbols.getFirst();
                }
                return symbols.getLast();
            }

            return null;
        } catch (StackOverflowError e) {
            OdinInsightUtils.logStackOverFlowError(identifier, OdinReference.LOG);
            return null;
        }
    }

    // Computes the symbol table under which the identifier is expected to be defined
    static OdinSymbolTable getIdentifierSymbolTable(OdinContext context, @NotNull OdinIdentifier element, OdinSymbolTableBuilder symbolTableProvider) {
        PsiElement parent = element.getParent();
        if (element.getParent() instanceof OdinImplicitSelectorExpression implicitSelectorExpression) {
            TsOdinType tsOdinType = implicitSelectorExpression.getInferredType(context);
            return OdinInsightUtils.getTypeElements(element.getProject(), tsOdinType);
        } else if (element.getParent() instanceof OdinNamedArgument namedArgument) {
            OdinInsightUtils.OdinCallInfo callInfo = OdinInsightUtils.getCallInfo(context, namedArgument);
            if (callInfo.callingType() instanceof TsOdinParameterOwner parameterOwner) {
                List<TsOdinParameter> parameters = parameterOwner.getParameters();
                TsOdinParameter tsOdinParameter = parameters.stream()
                        .filter(p -> p.getName().equals(namedArgument.getIdentifier().getText()))
                        .findFirst().orElse(null);

                if (tsOdinParameter != null) {
                    return OdinSymbolTable.from(Collections.singleton(tsOdinParameter.toSymbol()));
                }
            }
        }

        if (parent instanceof OdinRefExpression refExpression) {
            if (refExpression.getExpression() != null) {
                return OdinInsightUtils.getReferenceableSymbols(context, refExpression.getExpression());
            } else {
                return symbolTableProvider.build();
            }
        } else {
            OdinQualifiedType qualifiedType = PsiTreeUtil.getParentOfType(element, OdinQualifiedType.class);
            if (qualifiedType != null) {
                if (qualifiedType.getPackageIdentifier() == element) {
                    return symbolTableProvider.build();
                } else {
                    return OdinInsightUtils.getReferenceableSymbols(context, qualifiedType);
                }
            } else if (parent instanceof OdinSimpleRefType) {
                return symbolTableProvider.build();
            } else {
                return OdinSymbolTable.EMPTY;
            }
        }
    }
}


