package com.lasagnerd.odin.codeInsight.symbols;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.lasagnerd.odin.codeInsight.OdinAttributeUtils;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.imports.OdinImportService;
import com.lasagnerd.odin.codeInsight.imports.OdinImportUtils;
import com.lasagnerd.odin.codeInsight.typeInference.OdinInferenceEngine;
import com.lasagnerd.odin.codeInsight.typeSystem.*;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;

public class OdinSymbolTableResolver {
    public static OdinSymbolTable computeSymbolTable(@NotNull PsiElement element) {
        return findVisibleSymbols(element, OdinImportService.getInstance(element.getProject())
                .getPackagePath(element), s -> true);
    }

    public static OdinSymbolTable computeSymbolTable(@NotNull PsiElement element, Predicate<OdinSymbol> matcher) {
        return findVisibleSymbols(element, OdinImportService.getInstance(element.getProject())
                .getPackagePath(element), matcher);
    }

    public static OdinSymbolTable getFileScopeSymbols(@NotNull OdinFileScope fileScope, @NotNull OdinSymbol.OdinVisibility globalVisibility) {
        // Find all blocks that are not in a procedure
        List<OdinSymbol> fileScopeSymbols = new ArrayList<>();

        Stack<PsiElement> statementStack = new Stack<>();

        // do bfs
        statementStack.addAll(fileScope.getFileScopeStatementList().getStatementList());
        statementStack.addAll(fileScope.getImportStatements());
        while (!statementStack.isEmpty()) {
            PsiElement element = statementStack.pop();
            if (element instanceof OdinDeclaration declaration) {
                List<OdinSymbol> symbols = OdinDeclarationSymbolResolver.getSymbols(globalVisibility, declaration, OdinSymbolTable.EMPTY);
                fileScopeSymbols.addAll(symbols);
            } else {
                getStatements(element).forEach(statementStack::push);
            }
        }
        return OdinSymbolTable.from(fileScopeSymbols);
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

        addStatementsOfStatementBody(statementBody, statements);

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
        OdinBuiltinSymbolService builtinSymbolService = OdinBuiltinSymbolService.getInstance(project);
        if (builtinSymbolService != null)
            return builtinSymbolService.getBuiltInSymbols();
        return Collections.emptyList();
    }

    private static OdinSymbolTable findVisibleSymbols(@NotNull PsiElement element, String packagePath, Predicate<OdinSymbol> matcher) {
        // when building the scope tree, just stop as soon as we find the first matching declaration
        Project project = element.getProject();

        OdinSymbolTable symbolTable = new OdinSymbolTable();
        symbolTable.setPackagePath(packagePath);

        List<OdinSymbol> builtInSymbols = getBuiltInSymbols(project);

        OdinSymbolTable builtinSymbolTable = OdinSymbolTable.from(builtInSymbols);
        // 0. Import built-in symbols
        symbolTable.setRoot(builtinSymbolTable);

        // 1. Import symbols from this file
        // Will be done organically by going up tree

        // 2. Import symbols from other files in the same package
        if (packagePath != null) {
            // TODO actually include private symbols and rather don't suggest them in completion contributor. This way, we can show a different
            //  kind of error when accessing private symbols as opposed to undefined symbols.
            // Filter out symbols declared with private="file" or do not include anything if comment //+private is in front of package declaration
            List<OdinFile> otherFilesInPackage = getOtherFilesInPackage(project, packagePath, OdinImportUtils.getFileName(element));
            for (OdinFile odinFile : otherFilesInPackage) {
                if (odinFile == null || odinFile.getFileScope() == null) {
                    continue;
                }
                OdinSymbol.OdinVisibility globalFileVisibility = getGlobalFileVisibility(odinFile.getFileScope());
                if (globalFileVisibility == OdinSymbol.OdinVisibility.FILE_PRIVATE) continue;
                Collection<OdinSymbol> fileScopeDeclarations = odinFile.getFileScope().getSymbolTable().getSymbolNameMap()
                        .values()
                        .stream()
                        .filter(o -> !o.getVisibility().equals(OdinSymbol.OdinVisibility.FILE_PRIVATE))
                        .toList();


                symbolTable.addAll(fileScopeDeclarations);
            }
        }


        // 3. Import symbols from the scope tree
        OdinSymbolTable odinSymbolTable = doFindVisibleSymbols(packagePath, element, s -> false, false, symbolTable);
        odinSymbolTable.setPackagePath(packagePath);
//        odinSymbolTable.setRoot(symbolTable);

        return odinSymbolTable;
    }

    public static OdinSymbol.OdinVisibility getGlobalFileVisibility(@NotNull OdinFileScope fileScope) {
        PsiElement lineComment = PsiTreeUtil.skipSiblingsBackward(fileScope, PsiWhiteSpace.class);
        if (lineComment != null) {
            IElementType elementType = PsiUtilCore.getElementType(lineComment.getNode());
            if (elementType == OdinTypes.LINE_COMMENT) {
                if (lineComment.getText().equals("//+private")) {
                    return OdinSymbol.OdinVisibility.PACKAGE_PRIVATE;
                }

                if (lineComment.getText().equals("//+private file")) {
                    return OdinSymbol.OdinVisibility.FILE_PRIVATE;
                }
            }
        }

        return OdinSymbol.OdinVisibility.PUBLIC;
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

    public static OdinSymbol findSymbol(@NotNull OdinIdentifier identifier) {
        return findSymbol(identifier, OdinSymbolTableResolver.computeSymbolTable(identifier)
                .with(OdinImportService.getInstance(identifier.getProject())
                        .getPackagePath(identifier)));
    }

    public static OdinSymbol findSymbol(@NotNull OdinIdentifier identifier, OdinSymbolTable parentScope) {
        PsiElement parent = identifier.getParent();
        OdinSymbolTable symbolTable;
        if (parent instanceof OdinRefExpression refExpression) {
            if (refExpression.getExpression() != null) {
                symbolTable = OdinReferenceResolver.resolve(parentScope, refExpression.getExpression());
            } else {
                symbolTable = parentScope;
            }
        } else {
            OdinQualifiedType qualifiedType = PsiTreeUtil.getParentOfType(identifier, OdinQualifiedType.class);
            if (qualifiedType != null) {
                if (qualifiedType.getPackageIdentifier() == identifier) {
                    symbolTable = parentScope;
                } else {
                    symbolTable = OdinReferenceResolver.resolve(parentScope, qualifiedType);
                }
            } else {
                symbolTable = parentScope;
            }
        }

        if (symbolTable == OdinSymbolTable.EMPTY || symbolTable == null) {
            symbolTable = parentScope;
        }

        if (symbolTable != null) {
            return symbolTable.getSymbol(identifier.getIdentifierToken().getText());
        }

        return null;
    }

    @TestOnly
    public static OdinSymbolTable doFindVisibleSymbols(@NotNull PsiElement position) {
        return doFindVisibleSymbols(position, symbolTable -> false);
    }

    @TestOnly
    public static OdinSymbolTable doFindVisibleSymbols(@NotNull PsiElement position, StopCondition stopCondition) {
        return doFindVisibleSymbols(null, position, stopCondition, false, null);
    }

    private static class OdinStatefulSymbolTableResolver {
        private final PsiElement originalPosition;
        private final String packagePath;
        private final StopCondition stopCondition;
        private final OdinSymbolTable initialSymbolTable;

        public OdinStatefulSymbolTableResolver(PsiElement originalPosition, String packagePath, StopCondition stopCondition, OdinSymbolTable initialSymbolTable) {
            this.originalPosition = originalPosition;
            this.packagePath = packagePath;
            this.stopCondition = stopCondition;
            this.initialSymbolTable = initialSymbolTable;
        }

        private OdinSymbolTable findSymbols() {
            return findSymbols(originalPosition, false);
        }

        private OdinSymbolTable findSymbols(PsiElement position, boolean constantsOnly) {
            // 1. Find the starting point
            //  = a statement whose parent is a scope block
            // 2. Get the parent and get all declarations inside the scope block
            // 3. Add all constant declarations as they are not dependent on the position within the scope block
            // 4. Add all non-constant declarations, depending on whether the position is before or after
            //    the declared symbol

            OdinScopeArea containingScopeBlock = PsiTreeUtil.getParentOfType(position, OdinScopeArea.class);

            boolean fileScope = containingScopeBlock instanceof OdinFileScope;
            boolean foreignBlock = containingScopeBlock instanceof OdinForeignBlock;

            if (containingScopeBlock == null) {
                return Objects.requireNonNullElseGet(initialSymbolTable, () -> new OdinSymbolTable(packagePath));
            }


            OdinSymbolTable symbolTable = new OdinSymbolTable(packagePath);
            // Since odin does not support closures, all symbols above the current scope, are visible only if they are constants
            boolean isContainingBlockProcedure = containingScopeBlock instanceof OdinProcedureDefinition;
            boolean constantsOnlyNext = isContainingBlockProcedure || constantsOnly;
            OdinSymbolTable parentSymbolTable = findSymbols(containingScopeBlock, constantsOnlyNext);
            symbolTable.setParentSymbolTable(parentSymbolTable);


            // Bring field declarations and swizzle into scope
            OdinLhs lhs = PsiTreeUtil.getParentOfType(position, OdinLhs.class, false);
            if (lhs != null && containingScopeBlock instanceof OdinCompoundLiteral compoundLiteral) {
                TsOdinType tsOdinType = OdinInferenceEngine.inferTypeOfCompoundLiteral(symbolTable, compoundLiteral);
                List<OdinSymbol> elementSymbols = OdinInsightUtils.getElementSymbols(tsOdinType, tsOdinType.getSymbolTable());
                symbolTable.addAll(elementSymbols);
            }

            if (containingScopeBlock instanceof OdinProcedureDefinition procedureDefinition) {
                OdinBuiltinSymbolService builtinSymbolService = OdinBuiltinSymbolService.getInstance(procedureDefinition.getProject());
                if (builtinSymbolService != null) {
                    // TODO check logic of "contextless"
                    //OdinStringLiteral callConvention = procedureDefinition.getProcedureType().getStringLiteral();
                    //                    String stringLiteralValue = OdinInsightUtils.getStringLiteralValue(callConvention);
                    //                    if (stringLiteralValue == null && ) {
                    //                        symbolTable.add(builtinSymbolService.createNewContextParameterSymbol());
                    //                    }
                    symbolTable.add(builtinSymbolService.createImplicitStructSymbol("context", "Context"));
                }
            }

            if (containingScopeBlock instanceof OdinArgument argument) {
                addOffsetOfSymbols(argument, symbolTable);
            }

            // Finds the child of the scope block, where of which this element is a child. If we find the parent, this is guaranteed
            // to be != null
            List<OdinDeclaration> declarations = getDeclarations(containingScopeBlock);
            for (OdinDeclaration declaration : declarations) {
                if (!(declaration instanceof OdinConstantDeclaration) && !isPolymorphicParameter(declaration) && !isStatic(declaration))
                    continue;
                PositionCheckResult positionCheckResult = checkPosition(originalPosition, declaration);
                if (!positionCheckResult.validPosition)
                    continue;

                List<OdinSymbol> localSymbols = OdinDeclarationSymbolResolver.getLocalSymbols(declaration, symbolTable);
                symbolTable.addAll(localSymbols);

                if (stopCondition.match(symbolTable))
                    return symbolTable;
            }


            if (constantsOnly && !fileScope &&!foreignBlock)
                return symbolTable;

            for (var declaration : declarations) {
                if (declaration instanceof OdinConstantDeclaration)
                    continue;
                List<OdinSymbol> localSymbols = OdinDeclarationSymbolResolver.getLocalSymbols(declaration, symbolTable);
                for (OdinSymbol symbol : localSymbols) {
                    PositionCheckResult positionCheckResult = checkPosition(originalPosition, declaration);
                    if (!positionCheckResult.validPosition)
                        continue;


                    // Add stuff if we are in file scope (e.g. global variables)
                    boolean shouldAdd = fileScope
                            || foreignBlock
                            || isStrictlyBefore(declaration, positionCheckResult);

                    if (shouldAdd) {
                        symbolTable.add(symbol);
                    }

                    if (stopCondition.match(symbolTable))
                        return symbolTable;
                }
            }

            return symbolTable;
        }

        private void addOffsetOfSymbols(OdinArgument argument, OdinSymbolTable symbolTable) {
            OdinCallExpression callExpression = PsiTreeUtil.getParentOfType(argument, OdinCallExpression.class);
            if (callExpression != null && callExpression.getArgumentList().size() == 2) {
                if (argument == callExpression.getArgumentList().get(1)) {
                    OdinSymbol symbol = OdinInsightUtils.findBuiltinSymbolOfCallExpression(symbolTable,
                            callExpression,
                            text -> text.equals("offset_of") || text.equals("offset_of_member"));
                    if (symbol != null) {
                        OdinArgument odinArgument = callExpression.getArgumentList().getFirst();
                        OdinExpression typeExpression = getArgumentExpression(odinArgument);
                        if (typeExpression != null) {
                            TsOdinType tsOdinType = OdinInferenceEngine.inferType(symbolTable, typeExpression);
                            if (tsOdinType instanceof TsOdinMetaType metaType) {
                                if (metaType.representedType() instanceof TsOdinStructType structType) {
                                    OdinSymbolTable typeElements = OdinInsightUtils.getTypeElements(argument.getProject(), structType);
                                    symbolTable.putAll(typeElements);
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
                return OdinAttributeUtils.containsAttribute(variableInitializationStatement.getAttributeList(), "static");
            }

            if (declaration instanceof OdinVariableDeclarationStatement variableDeclarationStatement) {
                return OdinAttributeUtils.containsAttribute(variableDeclarationStatement.getAttributeList(), "static");
            }
            return false;
        }

        private boolean isStrictlyBefore(OdinDeclaration declaration, PositionCheckResult positionCheckResult) {
            PsiElement commonParent = positionCheckResult.commonParent();
            PsiElement containerOfSymbol = declaration != commonParent ? PsiTreeUtil.findPrevParent(commonParent, declaration) : declaration;
            PsiElement containerOfPosition = PsiTreeUtil.findPrevParent(commonParent, originalPosition);

            // Now check if symbol is strictly a previous sibling of position
            List<@NotNull PsiElement> childrenList = Arrays.stream(commonParent.getChildren()).toList();
            int indexOfSymbol = childrenList.indexOf(containerOfSymbol);
            int indexOfPosition = childrenList.indexOf(containerOfPosition);

            return indexOfPosition > indexOfSymbol;
        }
    }

    public static OdinSymbolTable doFindVisibleSymbols(String packagePath,
                                                       @NotNull PsiElement position,
                                                       StopCondition stopCondition,
                                                       boolean constantsOnly,
                                                       OdinSymbolTable root) {
        return new OdinStatefulSymbolTableResolver(
                position,
                packagePath,
                stopCondition,
                root).findSymbols();
    }


    private static @NotNull List<OdinDeclaration> getDeclarations(PsiElement containingScopeBlock) {
        List<OdinDeclaration> declarations = new ArrayList<>();
        if (containingScopeBlock instanceof OdinStatementList statementList) {
            List<OdinStatement> statements = statementList.getStatementList();
            addDeclarationsFromStatements(statements, declarations);
        }

        if (containingScopeBlock instanceof OdinIfBlock odinIfBlock) {
            addControlFlowInit(odinIfBlock.getControlFlowInit(), declarations);
        }

        if (containingScopeBlock instanceof OdinProcedureDefinition procedureDefinition) {
            OdinProcedureType procedureType = procedureDefinition.getProcedureType();

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
            if(switchBlock.getSwitchInClause() != null) {
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

        if (containingScopeBlock instanceof OdinConditionalStatement conditionalStatement) {
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

    private static PositionCheckResult checkPosition(PsiElement position, OdinDeclaration declaration) {
        // the position and the symbol MUST share a common parent
        PsiElement commonParent = PsiTreeUtil.findCommonParent(position, declaration);
        if (commonParent == null) {
            return new PositionCheckResult(false, null, null);
        }

        // if the position is in the declaration itself, we can assume the identifier has not been really declared yet. skip
        // EXCEPT: If we are in a constant declaration, the declaration itself is in scope, however, it is only legal
        // to use in structs, and procedures. In union and constants using the declaration is not legal.
        boolean usageInsideDeclaration = declaration == commonParent;
        if (usageInsideDeclaration) {
            if (declaration instanceof OdinProcedureDeclarationStatement procedureDeclarationStatement) {
                OdinProcedureBody declarationBody = procedureDeclarationStatement.getProcedureDefinition().getProcedureBody();
                OdinProcedureBody procedureBody = PsiTreeUtil.getParentOfType(position, OdinProcedureBody.class, false);

                if (procedureBody != null && PsiTreeUtil.isAncestor(declarationBody, procedureBody, false)) {
                    return new PositionCheckResult(true, commonParent, declaration);
                }
            }

            if (declaration instanceof OdinStructDeclarationStatement structDeclarationStatement) {
                OdinStructBlock declarationStructBlock = structDeclarationStatement.getStructType().getStructBlock();
                OdinStructBlock structBlock = PsiTreeUtil.getParentOfType(position, OdinStructBlock.class);

                if (structBlock != null && PsiTreeUtil.isAncestor(declarationStructBlock, structBlock, false)) {
                    return new PositionCheckResult(true, commonParent, declaration);
                }
            }

            return new PositionCheckResult(false, commonParent, declaration);
        }

        // Within param entries, polymorphic parameters and other constant declaration are not visible
        // from earlier parameters
        if (commonParent instanceof OdinParamEntries paramEntries) {
            OdinParamEntry paramEntryPosition = (OdinParamEntry) PsiTreeUtil.findPrevParent(commonParent, position);
            OdinParamEntry paramEntryDeclaration = (OdinParamEntry) PsiTreeUtil.findPrevParent(commonParent, declaration);

            int indexPosition = paramEntries.getParamEntryList().indexOf(paramEntryPosition);
            int indexDeclaration = paramEntries.getParamEntryList().indexOf(paramEntryDeclaration);
            if (indexPosition < indexDeclaration) {
                return new PositionCheckResult(false, commonParent, declaration);
            }
        }
        // When the declaration is queried from above of where the declaration is in the tree,
        // by definition, we do not add the symbol
        boolean positionIsAboveDeclaration = PsiTreeUtil.isAncestor(position, declaration, false);
        if (positionIsAboveDeclaration)
            return new PositionCheckResult(false, commonParent, declaration);

        return new PositionCheckResult(true, commonParent, declaration);
    }

    public static OdinSymbolTable computeSymbolTable(PsiElement reference, @NonNls @NotNull String originalFilePath) {
        return computeSymbolTable(reference, e -> true).with(originalFilePath);
    }

    @FunctionalInterface
    public interface StopCondition {
        boolean match(OdinSymbolTable symbolTable);
    }

    record PositionCheckResult(boolean validPosition, PsiElement commonParent, OdinDeclaration declaration) {

    }
}

