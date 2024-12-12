package com.lasagnerd.odin.codeInsight.symbols;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.lasagnerd.odin.codeInsight.OdinContext;
import com.lasagnerd.odin.codeInsight.OdinSymbolTable;
import com.lasagnerd.odin.codeInsight.imports.OdinImportService;
import com.lasagnerd.odin.codeInsight.imports.OdinImportUtils;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class OdinSymbolTableHelper {

    public static OdinSymbolTable buildFullSymbolTable(@NotNull PsiElement element) {
        return buildFullSymbolTable(OdinContext.EMPTY, element);
    }

    public static OdinSymbolTable buildFileScopeSymbolTable(@NotNull OdinFileScope fileScope, @NotNull OdinVisibility globalVisibility) {
        OdinSymbolTable context = new OdinSymbolTable();
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

    public static List<OdinSymbol> getBuiltInSymbols(Project project) {
        OdinSdkService sdkService = OdinSdkService.getInstance(project);
        if (sdkService != null) {

            return sdkService.getBuiltInSymbols().stream()
                    .filter(s -> s.getVisibility() == OdinVisibility.PACKAGE_EXPORTED)
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        return Collections.emptyList();
    }

    public static @NotNull OdinSymbolTable getRootContext(@NotNull PsiElement element, String packagePath) {
        OdinSymbolTable context = new OdinSymbolTable();
        context.setPackagePath(packagePath);

        List<OdinSymbol> builtInSymbols = getBuiltInSymbols(element.getProject());

        OdinSymbolTable builtinContext = OdinSymbolTable.from(builtInSymbols);
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
                        .getFullSymbolTable()
                        .getSymbolTable()
                        .values()
                        .stream()
                        .flatMap(List::stream)
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
    public static OdinSymbolTable doBuildFullSymbolTable(@NotNull PsiElement position) {
        return doBuildFullSymbolTable(position, OdinSymbolTableBuilderBase.ALWAYS_FALSE);
    }

    @TestOnly
    public static OdinSymbolTable doBuildFullSymbolTable(@NotNull PsiElement position, OdinSymbolTableBuilderListener listener) {
        return doBuildFullSymbolTable(null, position, listener, null);
    }

    public static OdinSymbolTable buildMinimalSymbolTable(OdinContext context, PsiElement identifier) {
        OdinSymbolTableBuilderBase resolver = new OdinMinimalSymbolTableBuilder(
                identifier,
                OdinImportService.packagePath(identifier),
                new OdinSymbolTableBuilderListener() {
                    @Override
                    public boolean onCheckpointCalled(OdinSymbolTable symbolTable) {
                        return symbolTable.getSymbol(identifier.getText()) != null;
                    }
                },
                context
        );

        OdinSymbolTable symbolTable = resolver.build();
        return symbolTable == null ? OdinSymbolTable.EMPTY : symbolTable;
    }

    public static OdinSymbolTable buildFullSymbolTable(OdinContext context, PsiElement element) {
        String packagePath = OdinImportService.getInstance(element.getProject()).getPackagePath(element);

        // 3. Import symbols from the scope tree
        OdinSymbolTable symbolTable = doBuildFullSymbolTable(packagePath,
                element,
                OdinSymbolTableBuilderBase.ALWAYS_FALSE,
                context);

        symbolTable.setPackagePath(packagePath);

        return symbolTable;
    }

    public static OdinSymbolTable doBuildFullSymbolTable(String packagePath,
                                                         @NotNull PsiElement position,
                                                         OdinSymbolTableBuilderListener listener,
                                                         OdinContext root) {
        return new OdinFullSymbolTableBuilder(
                position,
                packagePath,
                listener,
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

    public static OdinSymbolTable buildFullSymbolTable(PsiElement reference, @NonNls @NotNull String originalFilePath) {
        return buildFullSymbolTable(reference).with(originalFilePath);
    }

    @FunctionalInterface
    public interface StopCondition {
        boolean match(OdinSymbolTable context);
    }

}

