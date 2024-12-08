package com.lasagnerd.odin.codeInsight.symbols;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.lasagnerd.odin.codeInsight.OdinAttributeUtils;
import com.lasagnerd.odin.codeInsight.OdinContext;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.imports.OdinImportService;
import com.lasagnerd.odin.codeInsight.imports.OdinImportUtils;
import com.lasagnerd.odin.codeInsight.typeInference.OdinExpectedTypeEngine;
import com.lasagnerd.odin.codeInsight.typeInference.OdinInferenceEngine;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinMetaType;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinStructType;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinType;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class OdinContextBuilder {
    private static final StopCondition ALWAYS_FALSE = context -> false;

    public static OdinContext buildContext(@NotNull PsiElement element) {
        return buildFullContext(element);
    }

    public static OdinContext getFileScopeContext(@NotNull OdinFileScope fileScope, @NotNull OdinVisibility globalVisibility) {
        OdinContext context = new OdinContext();
        // Find all blocks that are not in a procedure
        List<OdinSymbol> fileScopeSymbols = new ArrayList<>();

        Stack<PsiElement> statementStack = new Stack<>();
        // do bfs
        statementStack.addAll(fileScope.getFileScopeStatementList().getStatementList());
        statementStack.addAll(fileScope.getImportStatements());
        while (!statementStack.isEmpty()) {
            PsiElement element = statementStack.pop();
            if (element instanceof OdinDeclaration declaration) {
                List<OdinSymbol> symbols = OdinDeclarationSymbolResolver.getSymbols(globalVisibility, declaration, OdinContext.EMPTY);
                context.getDeclarationSymbols().computeIfAbsent(declaration, d -> new ArrayList<>()).addAll(symbols);
                fileScopeSymbols.addAll(symbols);
            } else {
                getStatements(element).forEach(statementStack::push);
            }
        }
        context.addAll(fileScopeSymbols);
        context.setScopeBlock(fileScope);

        return context;
    }

    private static List<OdinDeclaration> getFileScopeDeclarations(OdinFileScope fileScope) {
        // Find all blocks that are not in a procedure
        List<OdinDeclaration> fileScopeSymbols = new ArrayList<>();

        Stack<PsiElement> statementStack = new Stack<>();

        // do bfs
        statementStack.addAll(fileScope.getFileScopeStatementList().getStatementList());
        statementStack.addAll(fileScope.getImportStatements());
        while (!statementStack.isEmpty()) {
            PsiElement element = statementStack.pop();
            if (element instanceof OdinDeclaration declaration) {
                fileScopeSymbols.add(declaration);
            } else {
                getStatements(element).forEach(statementStack::push);
            }
        }
        return fileScopeSymbols;
    }

    private static List<OdinStatement> getStatements(@NotNull PsiElement psiElement) {
        if (psiElement instanceof OdinWhenStatement odinWhenStatement) {
            OdinWhenBlock whenBlock = odinWhenStatement.getWhenBlock();
            return getWhenBlockStatements(whenBlock);
        }

        if (psiElement instanceof OdinForeignStatement foreignStatement) {
            OdinForeignBlock foreignBlock = foreignStatement.getForeignBlock();
            OdinForeignStatementList foreignStatementList = foreignBlock.getForeignStatementList();
            if (foreignStatementList != null) {
                return foreignStatementList.getStatementList();
            }
        }

        return Collections.emptyList();
    }

    private static @NotNull List<OdinStatement> getWhenBlockStatements(OdinWhenBlock whenBlock) {
        List<OdinStatement> statements = new ArrayList<>();
        OdinStatementBody statementBody = whenBlock.getStatementBody();

        if (statementBody != null) {
            addStatementsOfStatementBody(statementBody, statements);
        }

        OdinElseWhenBlock elseWhenBlock = whenBlock.getElseWhenBlock();
        if (elseWhenBlock != null) {
            OdinWhenBlock nextWhenBlock = elseWhenBlock.getWhenBlock();
            OdinStatementBody elseStatementBody = elseWhenBlock.getStatementBody();
            if (elseStatementBody != null) {
                addStatementsOfStatementBody(elseStatementBody, statements);
            }
            if (nextWhenBlock != null) {
                statements.addAll(getWhenBlockStatements(nextWhenBlock));
            }
        }
        return statements;
    }

    private static void addStatementsOfStatementBody(OdinStatementBody statementBody, List<OdinStatement> statements) {
        if (statementBody.getBlock() != null) {
            OdinStatementList statementList = statementBody.getBlock().getStatementList();
            if (statementList != null) {
                statements.addAll(statementList.getStatementList());
            }
        }

        if (statementBody.getDoStatement() != null) {
            statements.add(statementBody.getDoStatement());
        }
    }

    private static List<OdinSymbol> getBuiltInSymbols(Project project) {
        OdinSdkService sdkService = OdinSdkService.getInstance(project);
        if (sdkService != null) {

            return sdkService.getBuiltInSymbols().stream()
                    .filter(s -> s.getVisibility() == OdinVisibility.PACKAGE_EXPORTED)
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        return Collections.emptyList();
    }

    /**
     * Builds a full symbol table hierarchy that contains all symbols that visible (referenceable) from element
     *
     * @param element The context element
     * @return The symbol table hierarchy
     */
    private static OdinContext buildFullContext(@NotNull PsiElement element) {
        String packagePath = OdinImportService.getInstance(element.getProject()).getPackagePath(element);

        OdinContext context = getRootContext(element, packagePath);

        // 3. Import symbols from the scope tree
        OdinContext odinContext = doBuildFullContext(packagePath,
                element,
                s -> false,
                context);

        odinContext.setPackagePath(packagePath);

        return odinContext;
    }

    private static @NotNull OdinContext getRootContext(@NotNull PsiElement element, String packagePath) {
        OdinContext context = new OdinContext();
        context.setPackagePath(packagePath);

        List<OdinSymbol> builtInSymbols = getBuiltInSymbols(element.getProject());

        OdinContext builtinContext = OdinContext.from(builtInSymbols);
        builtinContext.setPackagePath("");

        // 0. Import built-in symbols
        if (!OdinSdkService.isInBuiltinOdinFile(element)) {
            context.setRoot(builtinContext);
        }

        // 1. Import symbols from this file
        // Will be done organically by going up tree

        // 2. Import symbols from other files in the same package
        if (packagePath != null) {
            // Filter out symbols declared with private="file" or do not include anything if comment //+private is in front of package declaration
            List<OdinFile> otherFilesInPackage = getOtherFilesInPackage(element.getProject(), packagePath, OdinImportUtils.getFileName(element));
            for (OdinFile odinFile : otherFilesInPackage) {
                if (odinFile == null || odinFile.getFileScope() == null) {
                    continue;
                }
                Collection<OdinSymbol> fileScopeDeclarations = odinFile.getFileScope()
                        .getFullContext()
                        .getSymbolTable()
                        .values()
                        .stream()
                        .filter(s -> s.getSymbolType() != OdinSymbolType.PACKAGE_REFERENCE)
                        .toList();

                context.addAll(fileScopeDeclarations);
            }
        }
        return context;
    }

    public static OdinVisibility getGlobalFileVisibility(@NotNull OdinFileScope fileScope) {
        PsiElement lineComment = PsiTreeUtil.skipSiblingsBackward(fileScope, PsiWhiteSpace.class);
        if (lineComment != null) {
            IElementType elementType = PsiUtilCore.getElementType(lineComment.getNode());
            if (elementType == OdinTypes.LINE_COMMENT) {
                if (lineComment.getText().equals("//+private")) {
                    return OdinVisibility.PACKAGE_PRIVATE;
                }

                if (lineComment.getText().equals("//+private file")) {
                    return OdinVisibility.FILE_PRIVATE;
                }
            }
        }

        OdinBuildFlagClause[] buildFlagClauses = PsiTreeUtil.getChildrenOfType(fileScope, OdinBuildFlagClause.class);
        if (buildFlagClauses == null)
            return OdinVisibility.PACKAGE_EXPORTED;

        for (OdinBuildFlagClause buildFlagClause : buildFlagClauses) {

            String prefix = buildFlagClause.getBuildFlagPrefix().getText();
            if (prefix.equals("#+private")) {
                for (OdinBuildFlagArgument buildFlagArgument : buildFlagClause.getBuildFlagArgumentList()) {
                    if (buildFlagArgument.getBuildFlagList().size() > 1)
                        continue;

                    OdinBuildFlag buildFlag = buildFlagArgument.getBuildFlagList().getFirst();
                    if (!(buildFlag instanceof OdinBuildFlagIdentifier buildFlagIdentifier))
                        continue;
                    if (buildFlagIdentifier.getBuildFlagIdentifierToken()
                            .getText()
                            .trim()
                            .equals("file")) {
                        return OdinVisibility.FILE_PRIVATE;
                    }
                }
                return OdinVisibility.PACKAGE_PRIVATE;
            }
        }

        return OdinVisibility.PACKAGE_EXPORTED;
    }

    /**
     * Gets the files in the indicated package but excludes the file fileName
     *
     * @param project     The current project
     * @param packagePath The packagePath
     * @param fileName    The file to exclude
     * @return Other files in package
     */
    private static @NotNull List<OdinFile> getOtherFilesInPackage(@NotNull Project project, @NotNull String packagePath, String fileName) {
        return OdinImportUtils.getFilesInPackage(project, Path.of(packagePath), virtualFile -> !virtualFile.getName().equals(fileName));
    }

    @TestOnly
    public static OdinContext doBuildFullContext(@NotNull PsiElement position) {
        return doBuildFullContext(position, ALWAYS_FALSE);
    }

    @TestOnly
    public static OdinContext doBuildFullContext(@NotNull PsiElement position, StopCondition stopCondition) {
        return doBuildFullContext(null, position, stopCondition, null);
    }

    public static OdinContext buildIdentifierContext(OdinIdentifier identifier) {
        String packagePath = OdinImportService.getInstance(identifier.getProject()).getPackagePath(identifier);
        OdinStatefulContextBuilder resolver = new OdinStatefulContextBuilder(
                identifier,
                packagePath,
                s -> s.getSymbol(identifier.getText()) != null,
                null
        );

        OdinContext odinContext = resolver.buildMinimalContext(identifier, false);
        return odinContext == null ? OdinContext.EMPTY : odinContext;
    }

    private static class OdinStatefulContextBuilder {
        private final PsiElement originalPosition;
        private final String packagePath;
        private final StopCondition stopCondition;
        private final OdinContext initialContext;
        private final PsiElement context;
        private @Nullable OdinCompoundLiteral parentCompoundLiteral;

        public OdinStatefulContextBuilder(PsiElement originalPosition,
                                          String packagePath,
                                          StopCondition stopCondition,
                                          OdinContext initialContext) {
            this.originalPosition = originalPosition;
            this.packagePath = packagePath;
            this.stopCondition = stopCondition;
            this.initialContext = initialContext;
            this.context = OdinExpectedTypeEngine.findTypeExpectationContext(originalPosition);
        }

        private OdinContext buildFullContext() {
            OdinContext fullContext = buildFullContext(originalPosition);
            return trimToPosition(fullContext, false);
        }

        private OdinContext buildMinimalContext(PsiElement element, boolean constantsOnly) {
            OdinScopeBlock containingScopeBlock = getNextContainingScopeBlock(element);
            if (containingScopeBlock == null) {
                OdinContext rootTable = getRootContext(element, packagePath);
                if (checkStopCondition(rootTable)) {
                    return rootTable;
                }
                return null;
            }

            boolean constantsOnlyNext = isConstantsOnlyNext(constantsOnly, containingScopeBlock);
            boolean forceAddVar = isForceAddVar(containingScopeBlock);


            OdinContext context = new OdinContext(packagePath);

            // Add "offset" symbols first, i.e. the symbols available at the second argument of the builtin
            // procedure offset. These are the members of the type that is passed as first parameter.
            if (containingScopeBlock instanceof OdinArgument argument) {
                addOffsetOfSymbols(argument, context);
                if (checkStopCondition(context)) {
                    return context;
                }
            }

            // Element initializers in compound literals
            if (containingScopeBlock instanceof OdinCompoundLiteral odinCompoundLiteral) {
                addSymbolsOfCompoundLiteral(element, odinCompoundLiteral, context);
                if (checkStopCondition(context)) {
                    return context;
                }
            }

            // Finds the child of the scope block, where of which this element is a child. If we find the parent, this is guaranteed
            // to be != null
            if (containingScopeBlock instanceof OdinProcedureDefinition) {
                addContextParameter(containingScopeBlock.getProject(), context);
                if (checkStopCondition(context)) {
                    return context;
                }
            }

            boolean stopped = computeScopeWithCheck(constantsOnly, containingScopeBlock, context, forceAddVar);
            if (stopped) {
                return context;
            }

            OdinScopeBlock parentScopeBlock = PsiTreeUtil.getParentOfType(containingScopeBlock, false, OdinScopeBlock.class);
            return buildMinimalContext(parentScopeBlock, constantsOnlyNext);
        }

        // Bring field declarations and swizzle into scope
        private static void addSymbolsOfCompoundLiteral(PsiElement element, OdinCompoundLiteral containingScopeBlock, OdinContext context) {
            OdinLhs lhs = PsiTreeUtil.getParentOfType(element, OdinLhs.class, false);
            if (lhs != null) {
                TsOdinType tsOdinType = OdinInferenceEngine.inferTypeOfCompoundLiteral(context, containingScopeBlock);
                List<OdinSymbol> elementSymbols = OdinInsightUtils.getElementSymbols(tsOdinType, tsOdinType.getContext());
                context.addAll(elementSymbols);
            }
        }

        private OdinContext buildFullContext(PsiElement element) {
            // 1. Find the starting point
            //  = a statement whose parent is a scope block
            // 2. Get the parent and get all declarations inside the scope block
            // 3. Add all constant declarations as they are not dependent on the position within the scope block
            // 4. Add all non-constant declarations, depending on whether the position is before or after
            //    the declared symbol

            OdinScopeBlock containingScopeBlock = getNextContainingScopeBlock(element);

            if (containingScopeBlock == null) {
                return Objects.requireNonNullElseGet(initialContext, () -> new OdinContext(packagePath));
            }

            if (containingScopeBlock.getFullContext() != null) {
                // re-using symbol table
                OdinContext context = containingScopeBlock.getFullContext();
                OdinContext parentContext = buildFullContext(containingScopeBlock);
                context.setParentContext(parentContext);
                return context;
            }

            OdinContext parentContext = buildFullContext(containingScopeBlock);

            return doBuildFullContext(containingScopeBlock, parentContext);
        }

        private @NotNull OdinContext doBuildFullContext(OdinScopeBlock containingScopeBlock, OdinContext parentContext) {
            OdinContext context = new OdinContext(packagePath);
            context.setScopeBlock(containingScopeBlock);
            containingScopeBlock.setFullContext(context);

            context.setParentContext(parentContext);

            // Bring field declarations and swizzle into scope
            if (containingScopeBlock instanceof OdinCompoundLiteral compoundLiteral) {
                TsOdinType tsOdinType = OdinInferenceEngine.inferTypeOfCompoundLiteral(context, compoundLiteral);
                List<OdinSymbol> elementSymbols = OdinInsightUtils.getElementSymbols(tsOdinType, tsOdinType.getContext());
                context.addAll(elementSymbols);
            }


            if (containingScopeBlock instanceof OdinArgument argument) {
                addOffsetOfSymbols(argument, context);
            }

            // Finds the child of the scope block, where of which this element is a child. If we find the parent, this is guaranteed
            // to be != null
            if (containingScopeBlock instanceof OdinProcedureDefinition) {
                addContextParameter(containingScopeBlock.getProject(), context);
            }
            List<OdinDeclaration> declarations = getDeclarations(containingScopeBlock);
            for (OdinDeclaration declaration : declarations) {
                List<OdinSymbol> localSymbols = OdinDeclarationSymbolResolver.getSymbols(declaration, context);

                context.getDeclarationSymbols()
                        .computeIfAbsent(declaration, d -> new ArrayList<>())
                        .addAll(localSymbols);

                context.addAll(localSymbols);
            }

            return context;
        }

        private Collection<OdinSymbol> externalSymbols(OdinContext odinContext) {
            // TODO causes concurrent modification exception occasionally
            Set<OdinSymbol> declarationSymbols = odinContext.getDeclarationSymbols().values()
                    .stream()
                    .flatMap(List::stream)
                    .collect(Collectors.toSet());
            HashSet<OdinSymbol> externalSymbols = new HashSet<>(odinContext.getSymbolTable().values());
            externalSymbols.removeAll(declarationSymbols);

            return externalSymbols;
        }

        private OdinContext trimToPosition(OdinContext fullContext, boolean constantsOnly) {
            // 1. Find the starting point
            //  = a statement whose parent is a scope block
            // 2. Get the parent and get all declarations inside the scope block
            // 3. Add all constant declarations as they are not dependent on the position within the scope block
            // 4. Add all non-constant declarations, depending on whether the position is before or after
            //    the declared symbol
            if (fullContext == null)
                return null;
            OdinScopeBlock containingScopeBlock = fullContext.getScopeBlock();

            boolean fileScope = containingScopeBlock instanceof OdinFileScope;
            boolean foreignBlock = containingScopeBlock instanceof OdinForeignBlock;

            if (containingScopeBlock == null)
                return fullContext;

            boolean constantsOnlyNext = isConstantsOnlyNext(constantsOnly, containingScopeBlock);

            if (containingScopeBlock instanceof OdinCompoundLiteral) {
                if (context instanceof OdinLhs) {
                    this.parentCompoundLiteral = this.parentCompoundLiteral == null ? PsiTreeUtil.getParentOfType(context, OdinCompoundLiteral.class)
                            : this.parentCompoundLiteral;
                    if (parentCompoundLiteral != containingScopeBlock) {
                        return trimToPosition(fullContext.getParentContext(), constantsOnlyNext);
                    }
                } else {
                    return trimToPosition(fullContext.getParentContext(), constantsOnlyNext);
                }
            }

            OdinContext context = new OdinContext(packagePath);
            context.setScopeBlock(containingScopeBlock);

            // Since odin does not support closures, all symbols above the current scope, are visible only if they are constants
            OdinContext nextParentContext = fullContext.getParentContext();

            OdinContext trimmedParentContext = trimToPosition(nextParentContext, constantsOnlyNext);

            context.setParentContext(trimmedParentContext);
            context.addAll(externalSymbols(fullContext));

            // Finds the child of the scope block, where of which this element is a child. If we find the parent, this is guaranteed
            // to be != null
            Set<OdinDeclaration> declarations = fullContext.getDeclarationSymbols().keySet();
            // TODO causes concurrent modification exception occasionally
            for (OdinDeclaration declaration : declarations) {
                if (!(declaration instanceof OdinConstantDeclaration)
                        && !isPolymorphicParameter(declaration)
                        && !isStatic(declaration))
                    continue;

                PositionCheckResult positionCheckResult;
                positionCheckResult = checkPosition(declaration);

                if (!positionCheckResult.validPosition)
                    continue;

                List<OdinSymbol> localSymbols = fullContext.getDeclarationSymbols(declaration);
                context.addAll(localSymbols);

                if (checkStopCondition(context))
                    return context;
            }


            if (constantsOnly && !fileScope && !foreignBlock)
                return context;

            for (var declaration : declarations) {
                if (declaration instanceof OdinConstantDeclaration)
                    continue;
                List<OdinSymbol> localSymbols = fullContext.getDeclarationSymbols(declaration);
                for (OdinSymbol symbol : localSymbols) {
                    PositionCheckResult positionCheckResult = checkPosition(declaration);
                    if (!positionCheckResult.validPosition)
                        continue;


                    // Add stuff if we are in file scope (e.g. global variables)
                    boolean shouldAdd = fileScope
                            || foreignBlock
                            || isStrictlyBefore(declaration, positionCheckResult);

                    if (shouldAdd) {
                        context.add(symbol);
                    }

                    if (checkStopCondition(context))
                        return context;
                }
            }

            return context;

        }

        private static boolean isConstantsOnlyNext(boolean constantsOnly, OdinScopeBlock containingScopeBlock) {
            return containingScopeBlock instanceof OdinProcedureDefinition || constantsOnly;
        }

        private static boolean isForceAddVar(OdinScopeBlock containingScopeBlock) {
            return containingScopeBlock instanceof OdinFileScope
                    || containingScopeBlock instanceof OdinForeignBlock;
        }

        private boolean computeScopeWithCheck(boolean constantsOnly,
                                              OdinScopeBlock containingScopeBlock,
                                              OdinContext context,
                                              boolean forceAddVar) {
            if (containingScopeBlock instanceof OdinProcedureDefinition) {
                addContextParameter(containingScopeBlock.getProject(), context);
            }

            // Finds the child of the scope block, where of which this element is a child. If we find the parent, this is guaranteed
            // to be != null
            List<OdinDeclaration> declarations = getDeclarations(containingScopeBlock);
            for (OdinDeclaration declaration : declarations) {
                if (!(declaration instanceof OdinConstantDeclaration) && !isPolymorphicParameter(declaration) && !isStatic(declaration))
                    continue;
                PositionCheckResult positionCheckResult = checkPosition(declaration);
                if (!positionCheckResult.validPosition)
                    continue;

                List<OdinSymbol> localSymbols = OdinDeclarationSymbolResolver.getSymbols(declaration, context);

                context.addAll(localSymbols);

                if (checkStopCondition(context))
                    return true;
            }


            if (constantsOnly && !forceAddVar)
                return false;

            for (var declaration : declarations) {
                if (declaration instanceof OdinConstantDeclaration)
                    continue;
                PositionCheckResult positionCheckResult = checkPosition(declaration);
                if (!positionCheckResult.validPosition)
                    continue;
                boolean shouldAdd = forceAddVar
                        || isStrictlyBefore(declaration, positionCheckResult);

                if (!shouldAdd)
                    continue;

                List<OdinSymbol> localSymbols = OdinDeclarationSymbolResolver.getSymbols(declaration, context);
                for (OdinSymbol symbol : localSymbols) {
                    // Add stuff if we are in file scope (e.g. global variables)

                    context.add(symbol);

                    if (checkStopCondition(context))
                        return true;
                }
            }

            return false;
        }

        private boolean checkStopCondition(OdinContext context) {
            return stopCondition != ALWAYS_FALSE && stopCondition.match(context);
        }

        private PositionCheckResult checkPosition(OdinDeclaration declaration) {
            // the position and the symbol MUST share a common parent
            PsiElement commonParent = PsiTreeUtil.findCommonParent(originalPosition, declaration);
            if (commonParent == null) {
                return new PositionCheckResult(false, null, null);
            }


            // if the position is in the declaration itself, we can assume the identifier has not been really declared yet. skip
            // EXCEPT: If we are in a constant declaration, the declaration itself is in scope, however, it is only legal
            // to use in structs, and procedures. In union and constants using the declaration is not legal.
            boolean usageInsideDeclaration = declaration == commonParent;
            if (usageInsideDeclaration) {
                OdinType type = OdinInsightUtils.getDeclaredType(declaration);
                OdinProcedureDefinition procedureDefinition;
                if (type instanceof OdinProcedureType) {
                    procedureDefinition = PsiTreeUtil.getParentOfType(type, OdinProcedureDefinition.class);
                } else if (type instanceof OdinProcedureLiteralType procedureLiteralType) {
                    procedureDefinition = procedureLiteralType.getProcedureDefinition();
                } else {
                    procedureDefinition = null;
                }

                if (procedureDefinition != null) {
                    OdinProcedureBody declarationBody = procedureDefinition.getProcedureBody();
                    OdinProcedureBody procedureBody = PsiTreeUtil.getParentOfType(originalPosition, OdinProcedureBody.class, false);

                    if (procedureBody != null && PsiTreeUtil.isAncestor(declarationBody, procedureBody, false)) {
                        return new PositionCheckResult(true, commonParent, declaration);
                    }
                }


                if (type instanceof OdinStructType structType) {
                    OdinStructBlock declarationStructBlock = structType.getStructBlock();
                    OdinStructBlock structBlock = PsiTreeUtil.getParentOfType(originalPosition, OdinStructBlock.class);

                    if (structBlock != null && PsiTreeUtil.isAncestor(declarationStructBlock, structBlock, false)) {
                        return new PositionCheckResult(true, commonParent, declaration);
                    }
                }

                return new PositionCheckResult(false, commonParent, declaration);
            }

            // Within param entries, polymorphic parameters and other constant declaration are not visible
            // from earlier parameters
            if (commonParent instanceof OdinParamEntries paramEntries) {
                OdinParamEntry paramEntryPosition = (OdinParamEntry) PsiTreeUtil.findPrevParent(commonParent, originalPosition);
                OdinParamEntry paramEntryDeclaration = (OdinParamEntry) PsiTreeUtil.findPrevParent(commonParent, declaration);

                int indexPosition = paramEntries.getParamEntryList().indexOf(paramEntryPosition);
                int indexDeclaration = paramEntries.getParamEntryList().indexOf(paramEntryDeclaration);
                if (indexPosition < indexDeclaration) {
                    return new PositionCheckResult(false, commonParent, declaration);
                }
            }
            // When the declaration is queried from above of where the declaration is in the tree,
            // by definition, we do not add the symbol
            boolean positionIsAboveDeclaration = PsiTreeUtil.isAncestor(originalPosition, declaration, false);
            if (positionIsAboveDeclaration)
                return new PositionCheckResult(false, commonParent, declaration);

            return new PositionCheckResult(true, commonParent, declaration);
        }

        private static void addContextParameter(@NotNull Project project, OdinContext context) {
            OdinSdkService builtinSymbolService = OdinSdkService.getInstance(project);
            if (builtinSymbolService != null) {
                // TODO check logic of "contextless"
                //OdinStringLiteral callConvention = procedureDefinition.getProcedureType().getStringLiteral();
                //                    String stringLiteralValue = OdinInsightUtils.getStringLiteralValue(callConvention);
                //                    if (stringLiteralValue == null && ) {
                //                        context.add(builtinSymbolService.createNewContextParameterSymbol());
                //                    }
                context.add(OdinSdkService.createContextSymbol(project));
            }
        }

        // In the AST the expression in "switch v in expr" is within the switch scope area, however,
        // semantically the variable "v" does not belong in the scope of the expression. Hence, we skip
        // it
        private static @Nullable OdinScopeBlock getNextContainingScopeBlock(PsiElement element) {
            OdinScopeBlock nextContainingScopeBlock = PsiTreeUtil.getParentOfType(element, true, OdinScopeBlock.class);
            if (nextContainingScopeBlock instanceof OdinSwitchInExpressionScope switchInExpressionScope) {

                nextContainingScopeBlock = PsiTreeUtil.getParentOfType(switchInExpressionScope, OdinScopeBlock.class);
                if (nextContainingScopeBlock != null) {
                    nextContainingScopeBlock = PsiTreeUtil.getParentOfType(nextContainingScopeBlock, OdinScopeBlock.class);
                }
            }
            return nextContainingScopeBlock;
        }

        private static void addOffsetOfSymbols(OdinArgument argument, OdinContext context) {
            OdinCallExpression callExpression = PsiTreeUtil.getParentOfType(argument, OdinCallExpression.class);
            if (callExpression != null && callExpression.getArgumentList().size() == 2) {
                if (argument == callExpression.getArgumentList().get(1)) {
                    OdinSymbol symbol = OdinInsightUtils.findBuiltinSymbolOfCallExpression(context,
                            callExpression,
                            text -> text.equals("offset_of") || text.equals("offset_of_member"));
                    if (symbol != null) {
                        OdinArgument odinArgument = callExpression.getArgumentList().getFirst();
                        OdinExpression typeExpression = getArgumentExpression(odinArgument);
                        if (typeExpression != null) {
                            TsOdinType tsOdinType = typeExpression.getInferredType();
                            if (tsOdinType instanceof TsOdinMetaType metaType) {
                                if (metaType.representedType() instanceof TsOdinStructType structType) {
                                    OdinContext typeElements = OdinInsightUtils.getTypeElements(argument.getProject(), structType);
                                    context.putAll(typeElements);
                                }
                            }
                        }
                    }
                }
            }


        }

        private static @Nullable OdinExpression getArgumentExpression(OdinArgument odinArgument) {
            OdinExpression typeExpression;
            if (odinArgument instanceof OdinUnnamedArgument typeArgument) {
                typeExpression = typeArgument.getExpression();
            } else if (odinArgument instanceof OdinNamedArgument namedTypeArgument) {
                typeExpression = namedTypeArgument.getExpression();
            } else {
                typeExpression = null;
            }
            return typeExpression;
        }

        private boolean isPolymorphicParameter(OdinDeclaration declaration) {
            if (declaration instanceof OdinPolymorphicType)
                return true;
            if (declaration instanceof OdinParameterDeclaration parameterDeclaration) {
                return parameterDeclaration.getDeclaredIdentifiers().stream().anyMatch(i -> i.getDollar() != null);
            }
            return false;
        }

        private boolean isStatic(OdinDeclaration declaration) {
            if (declaration instanceof OdinVariableInitializationStatement variableInitializationStatement) {
                return OdinAttributeUtils.containsAttribute(variableInitializationStatement.getAttributesDefinitionList(), "static");
            }

            if (declaration instanceof OdinVariableDeclarationStatement variableDeclarationStatement) {
                return OdinAttributeUtils.containsAttribute(variableDeclarationStatement.getAttributesDefinitionList(), "static");
            }
            return false;
        }

        private boolean isStrictlyBefore(OdinDeclaration declaration, PositionCheckResult positionCheckResult) {
            PsiElement commonParent = positionCheckResult.commonParent();
            PsiElement containerOfSymbol = declaration != commonParent ? PsiTreeUtil.findPrevParent(commonParent, declaration) : declaration;
            PsiElement containerOfPosition = originalPosition != commonParent ? PsiTreeUtil.findPrevParent(commonParent, originalPosition) : originalPosition;

            // Now check if symbol is strictly a previous sibling of position
            List<@NotNull PsiElement> childrenList = Arrays.stream(commonParent.getChildren()).toList();
            int indexOfSymbol = childrenList.indexOf(containerOfSymbol);
            int indexOfPosition = childrenList.indexOf(containerOfPosition);

            return indexOfPosition > indexOfSymbol;
        }
    }

    public static OdinContext doBuildFullContext(String packagePath,
                                                 @NotNull PsiElement position,
                                                 StopCondition stopCondition,
                                                 OdinContext root) {
        return new OdinStatefulContextBuilder(
                position,
                packagePath,
                stopCondition,
                root).buildFullContext();
    }


    public static @NotNull List<OdinDeclaration> getDeclarations(OdinScopeBlock containingScopeBlock) {
        List<OdinDeclaration> declarations = new ArrayList<>();
        if (containingScopeBlock instanceof OdinStatementList statementList) {
            List<OdinStatement> statements = statementList.getStatementList();
            addDeclarationsFromStatements(statements, declarations);
        }

        if (containingScopeBlock instanceof OdinIfBlock odinIfBlock) {
            addControlFlowInit(odinIfBlock.getControlFlowInit(), declarations);
        }

        if (containingScopeBlock instanceof OdinProcedureDefinition procedureDefinition) {
            OdinProcedureType procedureType = procedureDefinition.getProcedureSignature().getProcedureType();

            addParamEntries(procedureType.getParamEntries(), declarations);
            addPolymorphicDeclarations(procedureType.getParamEntries(), declarations);

            if (procedureType.getReturnParameters() != null) {
                OdinParamEntries returnParamEntries = procedureType.getReturnParameters().getParamEntries();
                addParamEntries(returnParamEntries, declarations);
            }
        }

        // Here we are in a parameter list. The only thing that adds scope in this context are the polymorphic
        // parameters
        if (containingScopeBlock instanceof OdinParamEntries paramEntries) {
            paramEntries.getParamEntryList().forEach(p -> declarations.add(p.getParameterDeclaration()));

            if (paramEntries.getParent() instanceof OdinReturnParameters returnParameters) {
                OdinProcedureType procedureType = PsiTreeUtil.getParentOfType(returnParameters, OdinProcedureType.class);
                if (procedureType != null) {
                    OdinParamEntries inParamEntries = procedureType.getParamEntries();
                    addParamEntries(inParamEntries, declarations);
                    addPolymorphicDeclarations(inParamEntries, declarations);
                }
            } else {
                addPolymorphicDeclarations(paramEntries, declarations);
            }
        }

        if (containingScopeBlock instanceof OdinForBlock forBlock) {
            if (forBlock.getControlFlowInit() != null) {
                addControlFlowInit(forBlock.getControlFlowInit(), declarations);
            }
            if (forBlock.getForInParameterDeclaration() != null) {
                declarations.add(forBlock.getForInParameterDeclaration());
            }
        }

        if (containingScopeBlock instanceof OdinSwitchBlock switchBlock) {
            if (switchBlock.getSwitchInClause() != null) {
                declarations.add(switchBlock.getSwitchInClause().getSwitchTypeVariableDeclaration());
            }
            addControlFlowInit(switchBlock.getControlFlowInit(), declarations);
        }

        if (containingScopeBlock instanceof OdinUnionType unionType) {
            OdinParamEntries paramEntries = unionType.getParamEntries();
            addParamEntries(paramEntries, declarations);
        }

        if (containingScopeBlock instanceof OdinStructType structType) {
            OdinParamEntries paramEntries = structType.getParamEntries();
            addParamEntries(paramEntries, declarations);
        }

        if (containingScopeBlock instanceof OdinIfStatement conditionalStatement) {
            OdinLabelDeclaration labelDeclaration = conditionalStatement.getLabelDeclaration();
            if (labelDeclaration != null) {
                declarations.add(labelDeclaration);
            }
        }

        if (containingScopeBlock instanceof OdinSwitchStatement switchStatement) {
            OdinLabelDeclaration labelDeclaration = switchStatement.getLabelDeclaration();
            if (labelDeclaration != null) {
                declarations.add(labelDeclaration);
            }
        }

        if (containingScopeBlock instanceof OdinForStatement forStatement) {
            OdinLabelDeclaration labelDeclaration = forStatement.getLabelDeclaration();
            if (labelDeclaration != null) {
                declarations.add(labelDeclaration);
            }
        }

        if (containingScopeBlock instanceof OdinFileScope fileScope) {
            declarations.addAll(getFileScopeDeclarations(fileScope));
        }

        if (containingScopeBlock instanceof OdinEnumBody enumBody) {
            declarations.addAll(enumBody.getEnumValueDeclarationList());
        }

        if (containingScopeBlock instanceof OdinForeignBlock foreignBlock) {
            OdinForeignStatementList foreignStatementList = foreignBlock.getForeignStatementList();
            if (foreignStatementList != null) {
                addDeclarationsFromStatements(foreignStatementList.getStatementList(), declarations);
            }
        }

        return declarations;
    }

    private static void addDeclarationsFromStatements(List<OdinStatement> statements, List<OdinDeclaration> declarations) {
        for (OdinStatement odinStatement : statements) {
            if (odinStatement instanceof OdinDeclaration declaration) {
                declarations.add(declaration);
            }
        }
    }

    private static void addPolymorphicDeclarations(OdinParamEntries paramEntries, List<OdinDeclaration> declarations) {
        if (paramEntries != null) {
            Collection<OdinPolymorphicType> polymorphicTypes = PsiTreeUtil.findChildrenOfType(paramEntries, OdinPolymorphicType.class);
            declarations.addAll(polymorphicTypes);
        }
    }

    private static void addParamEntries(OdinParamEntries paramEntries, List<OdinDeclaration> declarations) {
        if (paramEntries != null) {
            for (OdinParamEntry paramEntry : paramEntries.getParamEntryList()) {
                OdinDeclaration declaration = paramEntry.getParameterDeclaration();
                declarations.add(declaration);
            }
        }
    }

    private static void addControlFlowInit(@Nullable OdinControlFlowInit controlFlowInit, List<OdinDeclaration> declarations) {
        if (controlFlowInit != null && controlFlowInit.getStatement() instanceof OdinDeclaration declaration) {
            declarations.add(declaration);
        }
    }


    public static OdinContext buildContext(PsiElement reference, @NonNls @NotNull String originalFilePath) {
        return buildContext(reference).with(originalFilePath);
    }

    @FunctionalInterface
    public interface StopCondition {
        boolean match(OdinContext context);
    }

    record PositionCheckResult(boolean validPosition, PsiElement commonParent, OdinDeclaration declaration) {

    }
}

