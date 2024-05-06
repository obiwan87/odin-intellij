package com.lasagnerd.odin.lang.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.insights.OdinDeclarationSpec;
import com.lasagnerd.odin.insights.OdinDeclarationSpecifier;
import com.lasagnerd.odin.insights.OdinImportInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        return PsiTreeUtil.getChildrenOfTypeAsList(self, OdinImportDeclarationStatement.class);
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

    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinForeignProcedureDeclarationStatement statement) {
        return Collections.singletonList(statement.getDeclaredIdentifier());
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


    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinParameterDecl statement) {
        return statement.getParameterList().stream().map(OdinParameter::getDeclaredIdentifier).toList();
    }

    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinParameterInitialization statement) {
        return statement.getParameterList().stream().map(OdinParameter::getDeclaredIdentifier).toList();
    }


    // OdinTypedDeclaration

    public static OdinTypeDefinitionExpression getTypeDefinition(OdinConstantInitializationStatement statement) {
        return doGetTypeDefinitionExpression(statement.getTypeDefinitionExpression());
    }

    public static OdinTypeDefinitionExpression getTypeDefinition(OdinVariableDeclarationStatement statement) {
        return doGetTypeDefinitionExpression(statement.getTypeDefinitionExpression());
    }

    public static OdinTypeDefinitionExpression getTypeDefinition(OdinParameterDecl statement) {
        return doGetTypeDefinitionExpression(statement.getTypeDefinitionContainer().getTypeDefinitionExpression());
    }

    public static OdinTypeDefinitionExpression getTypeDefinition(OdinVariableInitializationStatement statement) {
        return doGetTypeDefinitionExpression(statement.getTypeDefinitionExpression());
    }

    public static OdinTypeDefinitionExpression getTypeDefinition(OdinFieldDeclarationStatement statement) {
        return doGetTypeDefinitionExpression(statement.getTypeDefinitionExpression());
    }

    public static OdinTypeDefinitionExpression getTypeDefinition(OdinVariadicParameterDeclaration variadicParameterDeclaration) {
        return doGetTypeDefinitionExpression(variadicParameterDeclaration.getTypeDefinitionContainer().getTypeDefinitionExpression());
    }

    public static OdinTypeDefinitionExpression getTypeDefinition(OdinUnnamedParameter parameter) {
        return doGetTypeDefinitionExpression(parameter.getTypeDefinitionContainer().getTypeDefinitionExpression());
    }

    public static OdinTypeDefinitionExpression getTypeDefinition(OdinParameterDeclaration parameterDeclaration) {
        throw new RuntimeException("This shouldn't be called! you have a bug somewhere: " + parameterDeclaration.getClass().getSimpleName());
    }

    public static OdinTypeDefinitionExpression getTypeDefinition(OdinParameterInitialization declaration) {
        OdinTypeDefinitionContainer typeDefinitionContainer = declaration.getTypeDefinitionContainer();
        if (typeDefinitionContainer != null) {
            return typeDefinitionContainer.getTypeDefinitionExpression();
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
        return importStatement.getPath();
    }


    @Nullable
    private static OdinTypeDefinitionExpression doGetTypeDefinitionExpression(OdinExpression statement) {
        if (statement instanceof OdinTypeDefinitionExpression typeDefinition) {
            return typeDefinition;
        }
        return null;
    }


    public static PsiElement getType(OdinExpression ignore) {
        return null;
    }

    @NotNull
    public static OdinImportInfo getImportInfo(OdinImportDeclarationStatement importStatement) {
        String name = importStatement.getAlias() != null
                ? importStatement.getAlias().getText()
                : null;

        String path = importStatement.getPath().getText();
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

        return new OdinImportInfo(name, path, library);
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
        OdinTypeDefinitionExpression typeDefinitionExpression = null;
        if (arrayType.getExpressionList().size() > 1) {
            OdinExpression odinExpression = arrayType.getExpressionList().get(1);
            if (odinExpression instanceof OdinTypeDefinitionExpression) {
                typeDefinitionExpression = (OdinTypeDefinitionExpression) odinExpression;
            }
        }
        if (!arrayType.getExpressionList().isEmpty()) {
            OdinExpression odinExpression = arrayType.getExpressionList().get(0);
            if (odinExpression instanceof OdinTypeDefinitionExpression) {
                typeDefinitionExpression = (OdinTypeDefinitionExpression) odinExpression;
            }
        }

        if (typeDefinitionExpression != null)
            return typeDefinitionExpression.getType();

        return null;
    }

    public static OdinType getKeyType(OdinMapType mapType) {
        return mapType.getTypeDefinitionExpressionList().get(0).getType();
    }

    public static OdinType getValueType(OdinMapType mapType) {
        return mapType.getTypeDefinitionExpressionList().get(1).getType();
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

    public static List<OdinDeclarationSpec> getDeclarationsSpecs(OdinProcedureDeclarationStatement procedureDeclarationStatement) {
        OdinProcedureType procedureType = procedureDeclarationStatement.getProcedureType();
        return doGetProcedureTypeDeclarationSpecs(procedureType);
    }

    public static List<OdinDeclarationSpec> getDeclarationsSpecs(OdinProcedureExpression procedureExpression) {
        OdinProcedureType procedureType = procedureExpression.getProcedureTypeContainer().getProcedureType();
        return doGetProcedureTypeDeclarationSpecs(procedureType);
    }

    private static @NotNull List<OdinDeclarationSpec> doGetProcedureTypeDeclarationSpecs(OdinProcedureType procedureType) {
        List<OdinDeclarationSpec> declarations = new ArrayList<>();
        {

            for (OdinParamEntry odinParamEntry : procedureType.getParamEntryList()) {
                declarations.addAll(OdinDeclarationSpecifier.getDeclarationSpecs(odinParamEntry.getParameterDeclaration()));
            }
        }

        Collection<OdinPolymorphicType> polymorphicTypes = PsiTreeUtil.findChildrenOfType(procedureType, OdinPolymorphicType.class);
        for (OdinPolymorphicType polymorphicType : polymorphicTypes) {
            OdinDeclarationSpec odinDeclarationSpec = new OdinDeclarationSpec();
            odinDeclarationSpec.setValueDeclaredIdentifier(polymorphicType.getDeclaredIdentifier());
            odinDeclarationSpec.setHasUsing(false);
            declarations.add(odinDeclarationSpec);
        }

        OdinReturnParameters returnParameters = procedureType.getReturnParameters();
        {
            if (returnParameters != null) {
                var paramEntries = procedureType.getParamEntryList();
                for (OdinParamEntry odinParamEntry : paramEntries) {
                    declarations.addAll(OdinDeclarationSpecifier.getDeclarationSpecs(odinParamEntry.getParameterDeclaration()));
                }
            }
        }
        return declarations;
    }

    // Simple Block
    public static List<OdinDeclarationSpec> getDeclarationsSpecs(OdinBlockStatement ignored) {
        return Collections.emptyList();
    }

    // Control flow blocks

    public static List<OdinDeclarationSpec> getDeclarationsSpecs(OdinIfBlock ifBlock) {
        OdinControlFlowInit controlFlowInit = ifBlock.getControlFlowInit();
        if (controlFlowInit != null && controlFlowInit.getStatement() instanceof OdinDeclaration odinDeclaration) {
            return OdinDeclarationSpecifier.getDeclarationSpecs(odinDeclaration);
        }
        return Collections.emptyList();
    }

    public static List<OdinDeclarationSpec> getDeclarationsSpecs(OdinElseIfBlock elseIfBlock) {
        List<OdinDeclarationSpec> specs = new ArrayList<>();
        OdinControlFlowInit controlFlowInit = elseIfBlock.getControlFlowInit();
        if (controlFlowInit != null && controlFlowInit.getStatement() instanceof OdinDeclaration odinDeclaration) {
            specs.addAll(OdinDeclarationSpecifier.getDeclarationSpecs(odinDeclaration));
        }
        addSpecsOfPreviousBlocks(elseIfBlock, specs);

        return specs;
    }

    public static List<OdinDeclarationSpec> getDeclarationsSpecs(OdinElseBlock elseBlock) {
        List<OdinDeclarationSpec> specs = new ArrayList<>();
        addSpecsOfPreviousBlocks(elseBlock, specs);
        return specs;
    }

    private static void addSpecsOfPreviousBlocks(PsiElement conditionalBlock, List<OdinDeclarationSpec> specs) {
        PsiElement prevSibling;
        PsiElement current = conditionalBlock;
        while ((prevSibling = current.getPrevSibling()) != null) {
            if (prevSibling instanceof OdinElseIfBlock elseIfBlocSibling) {
                specs.addAll(elseIfBlocSibling.getDeclarationsSpecs());
                break;
            }

            if (prevSibling instanceof OdinIfBlock ifBlock) {
                specs.addAll(ifBlock.getDeclarationsSpecs());
                break;
            }
            current = prevSibling;
        }
    }

    public static List<OdinDeclarationSpec> getDeclarationsSpecs(OdinForBlock forBlock) {
        OdinControlFlowInit controlFlowInit = forBlock.getControlFlowInit();
        if (controlFlowInit != null && controlFlowInit.getStatement() instanceof OdinDeclaration odinDeclaration) {
            return OdinDeclarationSpecifier.getDeclarationSpecs(odinDeclaration);
        }
        return Collections.emptyList();
    }

    public static List<OdinDeclarationSpec> getDeclarationsSpecs(OdinForInBlock forInStatement) {
        List<OdinDeclarationSpec> specs = new ArrayList<>();
        for (var forInParameter : forInStatement.getForInParameterList()) {
            OdinDeclarationSpec spec = new OdinDeclarationSpec();
            spec.setValueDeclaredIdentifier(forInParameter.getDeclaredIdentifier());
            specs.add(spec);
        }
        return specs;
    }

}
