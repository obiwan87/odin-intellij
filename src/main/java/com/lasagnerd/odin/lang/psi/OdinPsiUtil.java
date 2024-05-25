package com.lasagnerd.odin.lang.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.codeInsight.OdinImportInfo;
import com.lasagnerd.odin.codeInsight.OdinSymbol;
import com.lasagnerd.odin.codeInsight.OdinSymbolResolver;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class OdinPsiUtil {
    public static PsiReference getReference(OdinIdentifier self) {
        return new OdinReference(self);
    }

    public static PsiElement getOperator(OdinBinaryExpression self) {
        return self.getChildren().length > 1 ? self.getChildren()[1] : null;
    }

    public static OdinCompoundValueBody getCompoundValueBody(OdinCompoundValue self) {
        return PsiTreeUtil.findChildOfType(self, OdinCompoundValueBody.class);
    }

    public static List<OdinImportDeclarationStatement> getImportStatements(OdinFileScope self) {
        return self.getImportStatementsContainer().getImportDeclarationStatementList();
    }

    // OdinDeclaration

    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinPackageDeclaration statement) {
        return Collections.singletonList(statement.getDeclaredIdentifier());
    }

    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinUnionDeclarationStatement statement) {
        return Collections.singletonList(statement.getDeclaredIdentifier());
    }

    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinProcedureDeclarationStatement statement) {
        return Collections.singletonList(statement.getDeclaredIdentifier());
    }

    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinVariableInitializationStatement statement) {
        return statement.getIdentifierList().getDeclaredIdentifierList();
    }

    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinBitsetDeclarationStatement statement) {
        return Collections.singletonList(statement.getDeclaredIdentifier());
    }

    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinPolymorphicType polymorphicType) {
        return Collections.singletonList(polymorphicType.getDeclaredIdentifier());
    }

    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinParameterDeclaration statement) {
        return statement.getParameterList().stream().map(OdinParameter::getDeclaredIdentifier).toList();
    }

    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinConstantInitializationStatement statement) {
        return statement.getIdentifierList().getDeclaredIdentifierList();
    }

    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinVariableDeclarationStatement statement) {
        return statement.getIdentifierList().getDeclaredIdentifierList();
    }

    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinEnumDeclarationStatement statement) {
        return Collections.singletonList(statement.getDeclaredIdentifier());
    }

    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinFieldDeclarationStatement statement) {
        return statement.getDeclaredIdentifierList();
    }


    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinStructDeclarationStatement statement) {
        return Collections.singletonList(statement.getDeclaredIdentifier());
    }

    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinForeignImportDeclarationStatement statement) {
        if (statement.getAlias() != null)
            return Collections.singletonList(statement.getAlias());
        return Collections.emptyList();
    }

    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinImportDeclarationStatement statement) {
        if (statement.getAlias() != null)
            return Collections.singletonList(statement.getAlias());
        return Collections.emptyList();
    }

    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinEnumValueDeclaration declaration) {
        return Collections.singletonList(declaration.getDeclaredIdentifier());
    }


    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinParameterDeclarator statement) {
        return statement.getParameterList().stream().map(OdinParameter::getDeclaredIdentifier).toList();
    }

    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinParameterInitialization statement) {
        return statement.getParameterList().stream().map(OdinParameter::getDeclaredIdentifier).toList();
    }

    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinProcedureOverloadDeclarationStatement statement) {
        return Collections.singletonList(statement.getDeclaredIdentifier());
    }

    // OdinTypedDeclaration

    public static OdinType getTypeDefinition(OdinConstantInitializationStatement statement) {
        return statement.getType();
    }

    public static OdinType getTypeDefinition(OdinVariableDeclarationStatement statement) {
        return statement.getType();
    }

    public static OdinType getTypeDefinition(OdinParameterDeclarator statement) {
        return statement.getTypeDefinitionContainer().getType();
    }

    public static OdinType getTypeDefinition(OdinVariableInitializationStatement statement) {
        return statement.getType();
    }

    public static OdinType getTypeDefinition(OdinFieldDeclarationStatement statement) {
        return statement.getType();
    }

    public static OdinType getTypeDefinition(OdinUnnamedParameter parameter) {
        return parameter.getTypeDefinitionContainer().getType();
    }

    public static OdinType getTypeDefinition(OdinParameterDeclaration parameterDeclaration) {
        throw new RuntimeException("This shouldn't be called! you have a bug somewhere: " + parameterDeclaration.getClass().getSimpleName());
    }

    public static OdinType getTypeDefinition(OdinParameterInitialization declaration) {
        OdinTypeDefinitionContainer typeDefinitionContainer = declaration.getTypeDefinitionContainer();
        if (typeDefinitionContainer != null) {
            return typeDefinitionContainer.getType();
        }
        return null;
    }

    // OdinImportDeclaration

    public static String getName(OdinImportDeclarationStatement importStatement) {
        return importStatement.getImportInfo().packageName();
    }

    public static PsiElement setName(OdinImportDeclarationStatement importStatement, @NotNull String name) {
        return importStatement;
    }

    public static PsiElement getNameIdentifier(OdinImportDeclarationStatement importStatement) {
        if (importStatement.getAlias() != null)
            return importStatement.getAlias();
        return importStatement;
    }


    public static PsiElement getType(OdinExpression ignore) {
        return null;
    }

    @NotNull
    public static OdinImportInfo getImportInfo(OdinImportDeclarationStatement importStatement) {
        String name = importStatement.getAlias() != null
                ? importStatement.getAlias().getText()
                : null;

        String fullPath = importStatement.getPath().getText();
        String path = fullPath;
        // Remove quotes
        path = path.substring(1, path.length() - 1);

        String[] parts = path.split(":");
        String library = null;
        if (parts.length > 1) {
            library = parts[0];
            path = parts[1];
        } else {
            path = parts[0];
        }

        if (name == null) {
            // Last path segment is the packageName
            String[] pathParts = path.split("/");
            name = pathParts[pathParts.length - 1];
        }

        return new OdinImportInfo(fullPath, name, path, library);
    }

    public static OdinIdentifier getPackageIdentifier(OdinQualifiedType qualifiedType) {
        return qualifiedType.getIdentifier();
    }

    public static OdinIdentifier getTypeIdentifier(OdinQualifiedType qualifiedType) {
        OdinType typeExpression = qualifiedType.getType();
        if (typeExpression instanceof OdinSimpleRefType refType) {
            return refType.getIdentifier();
        }

        if (typeExpression instanceof OdinCallType callType) {
            return callType.getIdentifier();
        }

        return null;
    }

    public static OdinType getTypeDefinition(OdinArrayType arrayType) {
        return arrayType.getType();
    }

    public static OdinType getKeyType(OdinMapType mapType) {
        return mapType.getTypeList().get(0);
    }

    public static OdinType getValueType(OdinMapType mapType) {
        return mapType.getTypeList().get(1);
    }

    public static List<OdinStatement> getStatements(OdinBlock odinBlock) {
        if (odinBlock.getStatementList() != null) {
            return odinBlock.getStatementList().getStatementList();
        }
        return Collections.emptyList();
    }

    // Scope Blocks

    public static List<OdinStatement> getBlockStatements(OdinProcedureDeclarationStatement procedureDeclarationStatement) {
        OdinBlock block = procedureDeclarationStatement.getProcedureBody().getBlock();
        if (block != null) {
            return block.getStatements();
        }
        return Collections.emptyList();
    }

    public static List<OdinStatement> getBlockStatements(OdinProcedureExpression procedureExpression) {
        OdinBlock block = procedureExpression.getProcedureBody().getBlock();
        if (block != null) {
            return block.getStatements();
        }
        return Collections.emptyList();
    }

    public static List<OdinStatement> doGetBlockStatements(OdinStatementBody statementBody) {
        if (statementBody.getBlock() != null) {
            return statementBody.getBlock().getStatements();
        }

        if (statementBody.getDoStatement() != null) {
            return Collections.singletonList(statementBody.getDoStatement().getStatement());
        }
        return Collections.emptyList();
    }

    public static List<OdinStatement> getBlockStatements(OdinIfBlock ifBlock) {
        return doGetBlockStatements(ifBlock.getStatementBody());
    }

    public static List<OdinStatement> getBlockStatements(OdinElseIfBlock elseIfBlock) {
        return doGetBlockStatements(elseIfBlock.getStatementBody());
    }

    public static List<OdinStatement> getBlockStatements(OdinElseBlock elseBlock) {
        return doGetBlockStatements(elseBlock.getStatementBody());
    }

    public static List<OdinStatement> getBlockStatements(OdinBlockStatement blockStatement) {
        return blockStatement.getBlock().getStatements();
    }

    public static List<OdinStatement> getBlockStatements(OdinForBlock forBlock) {
        return doGetBlockStatements(forBlock.getStatementBody());
    }

    public static List<OdinStatement> getBlockStatements(OdinForInBlock forInBlock) {
        return doGetBlockStatements(forInBlock.getStatementBody());
    }

    // Declaration specs


    // Procedures

    public static List<OdinSymbol> getSymbols(OdinProcedureDeclarationStatement procedureDeclarationStatement) {
        OdinProcedureType procedureType = procedureDeclarationStatement.getProcedureType();
        return doGetProcedureTypeDeclarationSpecs(procedureType);
    }

    public static List<OdinSymbol> getSymbols(OdinProcedureExpression procedureExpression) {
        OdinProcedureType procedureType = procedureExpression.getProcedureTypeContainer().getProcedureType();
        return doGetProcedureTypeDeclarationSpecs(procedureType);
    }

    private static @NotNull List<OdinSymbol> doGetProcedureTypeDeclarationSpecs(OdinProcedureType procedureType) {
        List<OdinSymbol> declarations = new ArrayList<>();
        {

            for (OdinParamEntry odinParamEntry : procedureType.getParamEntryList()) {
                declarations.addAll(OdinSymbolResolver.getSymbols(odinParamEntry.getParameterDeclaration()));
            }
        }

        Collection<OdinPolymorphicType> polymorphicTypes = PsiTreeUtil.findChildrenOfType(procedureType, OdinPolymorphicType.class);
        for (OdinPolymorphicType polymorphicType : polymorphicTypes) {
            OdinSymbol odinSymbol = new OdinSymbol(polymorphicType.getDeclaredIdentifier());
            odinSymbol.setHasUsing(false);
            declarations.add(odinSymbol);
        }

        OdinReturnParameters returnParameters = procedureType.getReturnParameters();
        {
            if (returnParameters != null) {
                var paramEntries = procedureType.getParamEntryList();
                for (OdinParamEntry odinParamEntry : paramEntries) {
                    declarations.addAll(OdinSymbolResolver.getSymbols(odinParamEntry.getParameterDeclaration()));
                }
            }
        }
        return declarations;
    }

    // Simple Block
    public static List<OdinSymbol> getSymbols(OdinBlockStatement ignored) {
        return Collections.emptyList();
    }

    // Control flow blocks

    public static List<OdinSymbol> getSymbols(OdinIfBlock ifBlock) {
        OdinControlFlowInit controlFlowInit = ifBlock.getControlFlowInit();
        if (controlFlowInit != null && controlFlowInit.getStatement() instanceof OdinDeclaration odinDeclaration) {
            return OdinSymbolResolver.getSymbols(odinDeclaration);
        }
        return Collections.emptyList();
    }

    public static List<OdinSymbol> getSymbols(OdinElseIfBlock elseIfBlock) {
        List<OdinSymbol> specs = new ArrayList<>();
        OdinControlFlowInit controlFlowInit = elseIfBlock.getControlFlowInit();
        if (controlFlowInit != null && controlFlowInit.getStatement() instanceof OdinDeclaration odinDeclaration) {
            specs.addAll(OdinSymbolResolver.getSymbols(odinDeclaration));
        }
        addSpecsOfPreviousBlocks(elseIfBlock, specs);

        return specs;
    }

    public static List<OdinSymbol> getSymbols(OdinElseBlock elseBlock) {
        List<OdinSymbol> specs = new ArrayList<>();
        addSpecsOfPreviousBlocks(elseBlock, specs);
        return specs;
    }

    private static void addSpecsOfPreviousBlocks(PsiElement conditionalBlock, List<OdinSymbol> specs) {
        PsiElement prevSibling;
        PsiElement current = conditionalBlock;
        while ((prevSibling = current.getPrevSibling()) != null) {
            if (prevSibling instanceof OdinElseIfBlock elseIfBlocSibling) {
                specs.addAll(elseIfBlocSibling.getSymbols());
                break;
            }

            if (prevSibling instanceof OdinIfBlock ifBlock) {
                specs.addAll(ifBlock.getSymbols());
                break;
            }
            current = prevSibling;
        }
    }

    public static List<OdinSymbol> getSymbols(OdinForBlock forBlock) {
        OdinControlFlowInit controlFlowInit = forBlock.getControlFlowInit();
        if (controlFlowInit != null && controlFlowInit.getStatement() instanceof OdinDeclaration odinDeclaration) {
            return OdinSymbolResolver.getSymbols(odinDeclaration);
        }
        return Collections.emptyList();
    }

    public static List<OdinSymbol> getSymbols(OdinForInBlock forInStatement) {
        List<OdinSymbol> symbols = new ArrayList<>();
        for (var forInParameter : forInStatement.getForInParameterList()) {
            OdinSymbol spec = new OdinSymbol(forInParameter.getDeclaredIdentifier());
            symbols.add(spec);
        }
        return symbols;
    }

}
