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
import com.lasagnerd.odin.settings.projectSettings.OdinProjectSettingsService;
import one.util.streamex.MoreCollectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OdinReferenceResolver {

    public static @Nullable OdinSymbol resolve(@NotNull OdinContext context, @NotNull OdinIdentifier element) {

        try {
            boolean useKnowledge = OdinProjectSettingsService.getInstance(element.getProject())
                    .isConditionalSymbolResolutionEnabled() && context.isUseKnowledge();
            if (useKnowledge) {
                return resolveWithKnowledge(context, element);
            }
            return resolveWithoutKnowledge(context, element);
        } catch (StackOverflowError e) {
            OdinInsightUtils.logStackOverFlowError(element, OdinReference.LOG);
            return null;
        }
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


    private static @Nullable OdinSymbol resolveWithoutKnowledge(OdinContext context, @NotNull OdinIdentifier identifier) {
        OdinSymbolTableBuilder symbolTableBuilder = new OdinMinimalSymbolTableBuilder(identifier,
                OdinImportService.packagePath(identifier),
                createCheckpointListener(identifier),
                context
        );

        String name = identifier.getIdentifierToken().getText();
        OdinSymbolTable identifierSymbols = getIdentifierSymbolTable(new OdinContext(), identifier, symbolTableBuilder);
        List<OdinSymbol> symbols = identifierSymbols.getSymbols(name);
        boolean anyInvalidIdentifiers = symbols.stream()
                .filter(symbol -> symbol.getDeclaredIdentifier() != null)
                .anyMatch(symbol -> !symbol.getDeclaredIdentifier().isValid());

        if (anyInvalidIdentifiers)
            return null;

        symbols = symbols.stream()
                .filter(s -> OdinInsightUtils.isVisible(identifier, s) || s.getSymbolType() != OdinSymbolType.PACKAGE_REFERENCE)
                .collect(MoreCollectors.distinctBy(OdinSymbol::getDeclaredIdentifier));

        if (!symbols.isEmpty()) {
            return symbols.getLast();
        }

        return null;
    }

    private static @Nullable OdinSymbol resolveWithKnowledge(@NotNull OdinContext context,
                                                             @NotNull OdinIdentifier identifier) {

        OdinSymbolTableBuilder symbolTableBuilder = new OdinMinimalSymbolTableBuilder(identifier,
                OdinImportService.packagePath(identifier),
                createEndOfBlockListener(identifier),
                context
        );
        String name = identifier.getIdentifierToken().getText();

        OdinLattice implicitSourceKnowledge = computeImplicitKnowledge(identifier);
        OdinLattice explicitSourceKnowledge = computeExplicitKnowledge(context, identifier);
        boolean sourceInducesExplicitKnowledge = !explicitSourceKnowledge.getValues().isEmpty();

        OdinLattice sourceLattice;
        if (sourceInducesExplicitKnowledge) {
            sourceLattice = explicitSourceKnowledge;
        } else {
            sourceLattice = implicitSourceKnowledge;
        }

        OdinSymbolTable identifierSymbols = getIdentifierSymbolTable(sourceLattice.toContext(), identifier, symbolTableBuilder);

        List<OdinSymbol> symbols = identifierSymbols.getSymbols(name);

        symbols = symbols.stream()
                .filter(s -> OdinInsightUtils.isVisible(identifier, s) || s.getSymbolType() != OdinSymbolType.PACKAGE_REFERENCE)
                .collect(MoreCollectors.distinctBy(OdinSymbol::getDeclaredIdentifier));

        if (!symbols.isEmpty()) {
            // Mutate context, such that
            // TODO This should be returned as a copy. In OdinInferenceEngine we return a more specific
            //  context in the type itself. Should we return it in the symbol?
            context.getSymbolValueStore().intersect(sourceLattice.getSymbolValueStore());


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

            if (validSymbols.size() == 1) {
                return validSymbols.getFirst();
            }

            if (!validSymbols.isEmpty()) {
                return validSymbols.getLast();
            }

            if (!symbols.isEmpty()) {
                return symbols.getLast();
            }
            return null;
        }

        return null;
    }

    public static @NotNull OdinLattice computeExplicitKnowledge(@NotNull OdinContext context, @NotNull PsiElement element) {
        if (OdinSdkService.getInstance(element.getProject()).isInSyntheticOdinFile(element)
                || OdinSdkService.isInBuiltinOdinFile(element)) {
            return OdinLattice.fromContext(context);
        }
        OdinLattice lattice;
        lattice = OdinWhenConstraintsSolver.solveLattice(context, element);
        if (element.getContainingFile() instanceof OdinFile odinFile) {
            OdinSymbolValueStore valuesStore = odinFile
                    .getFileScope().getBuildFlagsValuesStore();
            if (valuesStore != null) {
                lattice.getSymbolValueStore().intersect(valuesStore);
            }
        }
        return lattice;
    }

    public static @NotNull OdinLattice computeImplicitKnowledge(@NotNull PsiElement element) {
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


