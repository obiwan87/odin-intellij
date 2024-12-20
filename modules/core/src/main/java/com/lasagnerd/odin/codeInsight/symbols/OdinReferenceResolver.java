package com.lasagnerd.odin.codeInsight.symbols;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.codeInsight.OdinContext;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.OdinSymbolTable;
import com.lasagnerd.odin.codeInsight.dataflow.OdinLattice;
import com.lasagnerd.odin.codeInsight.dataflow.OdinSymbolValueStore;
import com.lasagnerd.odin.codeInsight.dataflow.OdinWhenConstraintsSolver;
import com.lasagnerd.odin.codeInsight.imports.OdinImportService;
import com.lasagnerd.odin.codeInsight.sdk.OdinSdkService;
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

import java.util.ArrayList;
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


    public static @Nullable OdinSymbol resolve(@NotNull OdinContext context,
                                               @NotNull OdinIdentifier identifier,
                                               OdinSymbolTableBuilder symbolTableBuilder) {
//        System.out.println("Resolving reference to " + identifier.getText() + ":" + identifier.getLocation());

        try {
            return resolveWithKnowledge(context, identifier, symbolTableBuilder);
//            return getSymbolWithKnowledgeApplication(context, identifier, contextProvider);
        } catch (StackOverflowError e) {
            OdinInsightUtils.logStackOverFlowError(identifier, OdinReference.LOG);
            return null;
        }
    }

    private static @Nullable OdinSymbol resolveWithoutKnowledge(@NotNull OdinIdentifier identifier, OdinSymbolTableBuilder symbolTableBuilder) {
        String name = identifier.getIdentifierToken().getText();
        OdinSymbolTable identifierSymbols = getIdentifierSymbolTable(new OdinContext(), identifier, symbolTableBuilder);
        List<OdinSymbol> symbols = identifierSymbols.getSymbols(name);
        symbols = symbols.stream()
                .filter(s -> OdinInsightUtils.isVisible(identifier, s) || s.getSymbolType() != OdinSymbolType.PACKAGE_REFERENCE)
                .collect(MoreCollectors.distinctBy(OdinSymbol::getDeclaredIdentifier));

        if (!symbols.isEmpty()) {
            return symbols.getLast();
        }

        return null;
    }

    private static @Nullable OdinSymbol resolveWithKnowledge(@NotNull OdinContext context,
                                                             @NotNull OdinIdentifier identifier,
                                                             OdinSymbolTableBuilder contextProvider) {
        // We have two different types of situations
        // Either the element is under a when statement, or in a file with defined build flag clauses/a certain
        // file suffix (explicit context)
        // Or, the knowledge is induced by the values provided by the current target platform and build profile
        // e.g. ODIN_OS = .Windows, ODIN_DEBUG = false (implicit context)

        // TODO 1. We need to consider the incoming knowledge coming from context
        //  2. We need a way to distinguish whether the incoming knowledge is implicit or explicit. Have a flag? Different symbolic class?
        String name = identifier.getIdentifierToken().getText();


        OdinLattice implicitSourceKnowledge = getImplicitKnowledge(identifier);
        OdinLattice explicitSourceKnowledge = computeExplicitKnowledge(context, identifier);
        boolean sourceInducesExplicitKnowledge = !explicitSourceKnowledge.getValues().isEmpty();

        OdinLattice sourceLattice;
        if (sourceInducesExplicitKnowledge) {
            sourceLattice = explicitSourceKnowledge;
        } else {
            sourceLattice = implicitSourceKnowledge;
        }

        OdinSymbolTable identifierSymbols = getIdentifierSymbolTable(sourceLattice.toContext(), identifier, contextProvider);

        List<OdinSymbol> symbols = identifierSymbols.getSymbols(name);

        symbols = symbols.stream()
                .filter(s -> OdinInsightUtils.isVisible(identifier, s) || s.getSymbolType() != OdinSymbolType.PACKAGE_REFERENCE)
                .collect(MoreCollectors.distinctBy(OdinSymbol::getDeclaredIdentifier));

        if (!symbols.isEmpty()) {
            // Mutate context, such that
            // TODO is this even correct?
            context.getSymbolValueStore().intersect(sourceLattice.getSymbolValueStore());
            // System.out.println("Solving lattice for " + identifier.getText() + ":" + identifier.getLocation());

            List<OdinSymbol> validSymbols = new ArrayList<>();
            for (OdinSymbol symbol : symbols) {

                if (symbol.getDeclaredIdentifier() == null || symbol.isImplicitlyDeclared()) {
                    validSymbols.add(symbol);
                    continue;
                }

                OdinPsiElement declaredIdentifier = (OdinPsiElement) symbol.getDeclaredIdentifier();
                OdinLattice explicitTargetKnowledge = computeExplicitKnowledge(context, declaredIdentifier);
                boolean targetInducesExplicitKnowledge = !explicitTargetKnowledge.getValues().isEmpty();

                if (!targetInducesExplicitKnowledge && !sourceInducesExplicitKnowledge) {
                    validSymbols.add(symbol);
                    continue;
                }

                boolean include;
                if (sourceInducesExplicitKnowledge) {
                    include = !targetInducesExplicitKnowledge || explicitTargetKnowledge.isSubset(explicitSourceKnowledge);
                } else {
                    include = explicitTargetKnowledge.isSubset(implicitSourceKnowledge);
                }
                if (include) {
                    validSymbols.add(symbol);
                }
            }

            if (validSymbols.size() == 1
                    && validSymbols.getFirst().getDeclaredIdentifier() != null) {
                // Evaluate what the knowledge would be independently of the identifier
                // source. If the target has a compatible knowledge state, we can use the
                // cache, otherwise, this is a non-idempotent operation, that requires not
                // using the cache.
                OdinLattice implicitTargetKnowledge = getImplicitKnowledge(validSymbols.getFirst().getDeclaredIdentifier());

                if (!implicitTargetKnowledge.isSubset(explicitSourceKnowledge)) {
                    context.setUseCache(false);
                }
            }

            if (validSymbols.size() == 1) {
                return validSymbols.getFirst();
            }

            if (!symbols.isEmpty()) {
                return symbols.getLast();
            }
            return null;
        }

        return null;
    }

    public static @NotNull OdinLattice computeExplicitKnowledge(@NotNull OdinContext context, @NotNull OdinPsiElement element) {
        if (OdinSdkService.getInstance(element.getProject()).isInSyntheticOdinFile(element)
                || OdinSdkService.isInBuiltinOdinFile(element)) {
            return OdinLattice.fromContext(context);
        }
        OdinLattice lattice;
        lattice = OdinWhenConstraintsSolver.solveLattice(context, element);
        if (element.getContainingOdinFile() != null) {
            OdinSymbolValueStore valuesStore = element.getContainingOdinFile()
                    .getFileScope().getBuildFlagsValuesStore();
            if (valuesStore != null) {
                lattice.getSymbolValueStore().intersect(valuesStore);
            }
        }
        return lattice;
    }

    private static @NotNull OdinLattice getImplicitKnowledge(@NotNull PsiElement element) {
        if (OdinSdkService.getInstance(element.getProject()).isInSyntheticOdinFile(element)
                || OdinSdkService.isInBuiltinOdinFile(element)) {
            return new OdinLattice();
        }
        OdinLattice sourceLattice;
        OdinSymbolValueStore defaultValue = OdinSdkService
                .getInstance(element.getProject()).getSymbolValueStore();
        sourceLattice = new OdinLattice();

        sourceLattice.getSymbolValueStore().combine(defaultValue);
        return sourceLattice;
    }

    private static boolean inducesExplicitKnowledge(OdinPsiElement psiElement) {

        OdinWhenStatement whenStatement = PsiTreeUtil.getParentOfType(psiElement, OdinWhenStatement.class);
        if (whenStatement != null)
            return true;

        OdinFile odinFile = psiElement.getContainingOdinFile();
        if (odinFile != null) {
            OdinSymbolValueStore valuesStore = odinFile.getFileScope().getBuildFlagsValuesStore();
            return valuesStore != null && !valuesStore.isEmpty();
        }
        return false;
    }

    // Computes the symbol table under which the identifier is expected to be defined
    static OdinSymbolTable getIdentifierSymbolTable(OdinContext context, @NotNull OdinIdentifier element, OdinSymbolTableBuilder symbolTableProvider) {
        PsiElement parent = element.getParent();
        if (element.getParent() instanceof OdinImplicitSelectorExpression implicitSelectorExpression) {
            TsOdinType tsOdinType = implicitSelectorExpression.getInferredType(context);
            return OdinInsightUtils.getTypeElements(context, element.getProject(), tsOdinType);
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


