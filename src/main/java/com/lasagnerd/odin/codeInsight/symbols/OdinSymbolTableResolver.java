package com.lasagnerd.odin.codeInsight.symbols;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.lasagnerd.odin.codeInsight.*;
import com.lasagnerd.odin.codeInsight.imports.OdinImportService;
import com.lasagnerd.odin.codeInsight.imports.OdinImportUtils;
import com.lasagnerd.odin.codeInsight.typeInference.OdinInferenceEngine;
import com.lasagnerd.odin.codeInsight.typeInference.OdinTypeResolver;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinArrayType;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinStructType;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinType;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;

public class OdinSymbolTableResolver {
    public static OdinSymbolTable computeSymbolTable(PsiElement element) {
        return findVisibleSymbols(element, OdinImportService.getInstance(element.getProject()).getPackagePath(element), s -> true);
    }

    public static OdinSymbolTable computeSymbolTable(PsiElement element, Predicate<OdinSymbol> matcher) {
        return findVisibleSymbols(element, OdinImportService.getInstance(element.getProject()).getPackagePath(element), matcher);
    }

    public static OdinSymbolTable getFileScopeDeclarations(@NotNull OdinFileScope fileScope) {
        return getFileScopeDeclarations(fileScope, getGlobalFileVisibility(fileScope));
    }

    public static OdinSymbolTable getFileScopeDeclarations(@NotNull OdinFileScope fileScope, @NotNull OdinSymbol.OdinVisibility globalVisibility) {
        // Find all blocks that are not in a procedure
        List<OdinSymbol> fileScopeSymbols = new ArrayList<>();

        Stack<PsiElement> statementStack = new Stack<>();

        // do bfs
        statementStack.addAll(fileScope.getFileScopeStatementList().getStatementList());
        statementStack.addAll(fileScope.getImportStatementsContainer().getImportDeclarationStatementList());
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

    private static List<OdinStatement> getStatements(PsiElement psiElement) {
        if (psiElement instanceof OdinWhenStatement odinWhenStatement) {
            if (odinWhenStatement.getWhenBlock().getStatementBody().getBlock() != null) {
                OdinStatementList statementList = odinWhenStatement.getWhenBlock().getStatementBody().getBlock().getStatementList();
                if (statementList != null) {
                    return statementList.getStatementList();
                }
            }

            if (odinWhenStatement.getWhenBlock().getStatementBody().getDoStatement() != null) {
                return List.of(odinWhenStatement.getWhenBlock().getStatementBody().getDoStatement());
            }
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

    private static List<OdinSymbol> getBuiltInSymbols(Project project) {
        OdinBuiltinSymbolService builtinSymbolService = OdinBuiltinSymbolService.getInstance(project);
        if (builtinSymbolService != null)
            return builtinSymbolService.getBuiltInSymbols();
        return Collections.emptyList();
    }

    private static OdinSymbolTable findVisibleSymbols(PsiElement element, String packagePath, Predicate<OdinSymbol> matcher) {
        // TODO: The different scopes (builtin, package, etc.) should be organized in their own SymbolTables for consistency and for correct behaviour
        //  e.g. in Odin it's possible to override built-in symbols
        // TODO: When looking for a specific declaration, this can be optimized:
        // when building the scope tree, just stop as soon as we find the first matching declaration
        Project project = element.getProject();

        OdinSymbolTable symbolTable = new OdinSymbolTable();
        symbolTable.setPackagePath(packagePath);

        List<OdinSymbol> builtInSymbols = getBuiltInSymbols(project);

        // 0. Import built-in symbols
        symbolTable.addAll(builtInSymbols);

        // 1. Import symbols from this file
        OdinFileScope fileScope = (OdinFileScope) PsiTreeUtil.findFirstParent(element, psi -> psi instanceof OdinFileScope);

        if (fileScope != null) {
            OdinSymbolTable fileScopeDeclarations = fileScope.getSymbolTable();
            symbolTable.addAll(fileScopeDeclarations.getFilteredSymbols(matcher), false);
        }

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
        OdinSymbolTable odinSymbolTable = doFindVisibleSymbols(packagePath, element, s -> false, false);
        odinSymbolTable.setPackagePath(packagePath);
        odinSymbolTable.setRoot(symbolTable);

        return odinSymbolTable;
    }

    public static OdinSymbol.OdinVisibility getGlobalFileVisibility(OdinFileScope fileScope) {
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

    public static OdinSymbol findSymbol(OdinIdentifier identifier) {
        OdinSymbolTable parentScope = computeSymbolTable(identifier)
                .with(OdinImportService.getInstance(identifier.getProject()).getPackagePath(identifier));
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
    public static OdinSymbolTable doFindVisibleSymbols(PsiElement position) {
        return doFindVisibleSymbols(position, symbolTable -> false);
    }

    @TestOnly
    public static OdinSymbolTable doFindVisibleSymbols(PsiElement position, StopCondition stopCondition) {
        return doFindVisibleSymbols(null, position, stopCondition, false);
    }

    public static OdinSymbolTable doFindVisibleSymbols(String packagePath, PsiElement position, StopCondition stopCondition, boolean constantsOnly) {
        // 1. Find the starting point
        //  = a statement whose parent is a scope block
        // 2. Get the parent and define and get all declarations inside the scope block
        // 3. Add all constant declarations as they are not dependent on the position within the scope block
        // 4. Add all non-constant declarations, depending on whether the position is before or after
        //    the declared symbol

        OdinScopeArea containingScopeBlock = PsiTreeUtil.getParentOfType(position, OdinScopeArea.class);

        if (containingScopeBlock == null)
            return new OdinSymbolTable(packagePath);

        if (containingScopeBlock instanceof OdinFileScope odinFileScope) {
            return getFileScopeDeclarations(odinFileScope);
        }

        OdinSymbolTable symbolTable = new OdinSymbolTable(packagePath);
        // Since odin does not support closures, all symbols above the current scope, are visible only if they are constants
        boolean isContainingBlockProcedure = containingScopeBlock instanceof OdinProcedureDefinition;
        boolean constantsOnlyNext = isContainingBlockProcedure || constantsOnly;
        OdinSymbolTable parentSymbolTable = doFindVisibleSymbols(packagePath, containingScopeBlock, stopCondition, constantsOnlyNext);
        symbolTable.setParentSymbolTable(parentSymbolTable);

        OdinLhs lhs = PsiTreeUtil.getParentOfType(position, OdinLhs.class, false);
        if (lhs != null) {
            if (containingScopeBlock instanceof OdinCompoundLiteralTyped compoundLiteralTyped) {
                TsOdinType tsOdinType = OdinTypeResolver.resolveType(symbolTable, compoundLiteralTyped.getType());
                addElementSymbols(tsOdinType, symbolTable);
            }
            if (containingScopeBlock instanceof OdinCompoundLiteralUntyped untyped) {
                TsOdinType tsOdinType = OdinInferenceEngine.inferExpectedType(symbolTable, (OdinExpression) untyped.getParent());
                addElementSymbols(tsOdinType, symbolTable);
            }
        }

        if (containingScopeBlock instanceof OdinProcedureDefinition procedureDefinition) {
            OdinBuiltinSymbolService builtinSymbolService = OdinBuiltinSymbolService.getInstance(procedureDefinition.getProject());
            if (builtinSymbolService != null) {
                OdinStringLiteral callConvention = procedureDefinition.getProcedureType().getStringLiteral();
                if (callConvention != null) {
                    // TODO it is unclear what "contextless" means in core.odin
//                    String stringLiteralValue = OdinInsightUtils.getStringLiteralValue(callConvention);
//                    if (stringLiteralValue == null && ) {
//                        symbolTable.add(builtinSymbolService.createNewContextParameterSymbol());
//                    }
                    symbolTable.add(builtinSymbolService.createNewContextParameterSymbol());
                } else {
                    symbolTable.add(builtinSymbolService.createNewContextParameterSymbol());
                }
            }
        }

        // Finds the child of the scope block, where of which this element is a child. If we find the parent, this is guaranteed
        // to be != null
        List<OdinDeclaration> declarations = getDeclarations(containingScopeBlock);
        for (OdinDeclaration declaration : declarations) {
            if (!(declaration instanceof OdinConstantDeclaration constantDeclaration))
                continue;
            PositionCheckResult positionCheckResult = checkPosition(position, declaration);
            if (!positionCheckResult.validPosition)
                continue;

            List<OdinSymbol> localSymbols = OdinDeclarationSymbolResolver.getLocalSymbols(constantDeclaration, symbolTable);
            symbolTable.addAll(localSymbols);

            if (stopCondition.match(symbolTable))
                return symbolTable;
        }

        if (constantsOnly)
            return symbolTable;

        for (var declaration : declarations) {
            if (declaration instanceof OdinConstantDeclaration)
                continue;
            List<OdinSymbol> localSymbols = OdinDeclarationSymbolResolver.getLocalSymbols(declaration, symbolTable);
            for (OdinSymbol symbol : localSymbols) {
                PositionCheckResult positionCheckResult = checkPosition(position, declaration);
                if (!positionCheckResult.validPosition)
                    continue;

                PsiElement commonParent = positionCheckResult.commonParent();
                PsiElement containerOfSymbol = declaration != commonParent ? PsiTreeUtil.findPrevParent(commonParent, declaration) : declaration;
                PsiElement containerOfPosition = PsiTreeUtil.findPrevParent(commonParent, position);

                // Now check if symbol is strictly a previous sibling of position
                List<@NotNull PsiElement> childrenList = Arrays.stream(commonParent.getChildren()).toList();
                int indexOfSymbol = childrenList.indexOf(containerOfSymbol);
                int indexOfPosition = childrenList.indexOf(containerOfPosition);

                if (indexOfPosition > indexOfSymbol) {
                    symbolTable.add(symbol);
                }

                if (stopCondition.match(symbolTable))
                    return symbolTable;
            }
        }

        return symbolTable;
    }

    private static void addElementSymbols(TsOdinType tsOdinType, OdinSymbolTable symbolTable) {
        if (tsOdinType instanceof TsOdinStructType tsOdinStructType) {
            // TODO will this work with aliases?
            List<OdinSymbol> typeSymbols = OdinInsightUtils.getTypeSymbols(tsOdinStructType, symbolTable);
            symbolTable.addAll(typeSymbols);
        }

        if (tsOdinType instanceof TsOdinArrayType tsOdinArrayType) {
            List<String> swizzleSymbols = List.of("r", "g", "b", "a", "x", "y", "z", "w");
            for (String swizzleSymbol : swizzleSymbols) {
                OdinSymbol odinSymbol = new OdinSymbol();
                odinSymbol.setName(swizzleSymbol);

                TsOdinType elementType = tsOdinArrayType.getElementType();
                if (elementType != null) {
                    odinSymbol.setPsiType(elementType.getType());
                }
                odinSymbol.setVisibility(OdinSymbol.OdinVisibility.NONE);
                odinSymbol.setImplicitlyDeclared(true);
                odinSymbol.setScope(OdinSymbol.OdinScope.TYPE);
                odinSymbol.setSymbolType(OdinSymbol.OdinSymbolType.FIELD);
                symbolTable.add(odinSymbol);
            }
        }
    }

    private static @NotNull List<OdinDeclaration> getDeclarations(PsiElement containingScopeBlock) {
        List<OdinDeclaration> declarations = new ArrayList<>();
        if (containingScopeBlock instanceof OdinStatementList statementList) {
            for (OdinStatement odinStatement : statementList.getStatementList()) {
                if (odinStatement instanceof OdinDeclaration declaration) {
                    declarations.add(declaration);
                }
            }
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
            addControlFlowInit(forBlock.getControlFlowInit(), declarations);
        }

        if (containingScopeBlock instanceof OdinForInBlock forInBlock) {
            declarations.addAll(forInBlock.getForInParameterDeclarationList());
        }

        if (containingScopeBlock instanceof OdinSwitchBlock switchBlock) {
            addControlFlowInit(switchBlock.getControlFlowInit(), declarations);
        }

        if (containingScopeBlock instanceof OdinSwitchInBlock switchInBlock) {
            declarations.add(switchInBlock.getSwitchTypeVariableDeclaration());
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

        if (containingScopeBlock instanceof OdinForInStatement forInStatement) {
            OdinLabelDeclaration labelDeclaration = forInStatement.getLabelDeclaration();
            if (labelDeclaration != null) {
                declarations.add(labelDeclaration);
            }
        }

        return declarations;
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
        if (usageInsideDeclaration && !(declaration instanceof OdinConstantDeclaration)) {
            return new PositionCheckResult(false, commonParent, declaration);
        }

        // When the declaration is queried from above of where the declaration is in the tree,
        // by definition, we do not add the symbol
        boolean positionIsAboveDeclaration = PsiTreeUtil.isAncestor(position, declaration, false);
        if (positionIsAboveDeclaration)
            return new PositionCheckResult(false, commonParent, declaration);

        return new PositionCheckResult(true, commonParent, declaration);
    }

    @FunctionalInterface
    public interface StopCondition {
        boolean match(OdinSymbolTable symbolTable);
    }

    record PositionCheckResult(boolean validPosition, PsiElement commonParent, OdinDeclaration declaration) {

    }
}

