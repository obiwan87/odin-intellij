package com.lasagnerd.odin.lang.psi;

import com.intellij.icons.AllIcons;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceService;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.completion.OdinCompletionContributor;
import com.lasagnerd.odin.codeInsight.imports.OdinImport;
import com.lasagnerd.odin.codeInsight.imports.OdinImportUtils;
import com.lasagnerd.odin.codeInsight.symbols.OdinDeclarationSymbolResolver;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbol;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolTable;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolType;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.lasagnerd.odin.lang.psi.OdinTypes.BUILD_FLAG_IDENTIFIER_TOKEN;

public class OdinPsiUtil {

    public static final TokenSet UNARY_OPERATORS = TokenSet.create(
            OdinTypes.PLUS,
            OdinTypes.MINUS,
            OdinTypes.TILDE,
            OdinTypes.AND,
            OdinTypes.NOT,
            OdinTypes.RANGE
    );
    public static final @NotNull TokenSet BINARY_OPERATORS = TokenSet.create(OdinTypes.STAR,
            OdinTypes.DIV,
            OdinTypes.MOD,
            OdinTypes.REMAINDER,
            OdinTypes.PLUS,
            OdinTypes.MINUS,
            OdinTypes.AND,
            OdinTypes.PIPE,
            OdinTypes.TILDE,
            OdinTypes.ANDNOT,
            OdinTypes.LSHIFT,
            OdinTypes.RSHIFT,
            OdinTypes.OR_ELSE,
            OdinTypes.RANGE_INCLUSIVE,
            OdinTypes.RANGE_EXCLUSIVE,
            OdinTypes.IN,
            OdinTypes.NOT_IN,
            OdinTypes.LT,
            OdinTypes.GT,
            OdinTypes.LTE,
            OdinTypes.GTE,
            OdinTypes.EQEQ,
            OdinTypes.NEQ,
            OdinTypes.OROR,
            OdinTypes.ANDAND
    );

    // TokenSet for comparison operators
    public static final @NotNull TokenSet COMPARISON_OPERATORS = TokenSet.create(
            OdinTypes.LT,
            OdinTypes.GT,
            OdinTypes.LTE,
            OdinTypes.GTE,
            OdinTypes.EQEQ,
            OdinTypes.NEQ
    );

    // TokenSet for integers
    public static final @NotNull TokenSet INTEGER_ARITHMETIC_OPERATORS = TokenSet.create(
            OdinTypes.PLUS,
            OdinTypes.MINUS,
            OdinTypes.STAR,
            OdinTypes.DIV,
            OdinTypes.MOD,
            OdinTypes.REMAINDER
    );

    public static final @NotNull TokenSet INTEGER_BITWISE_OPERATORS = TokenSet.create(
            OdinTypes.AND,
            OdinTypes.PIPE,
            OdinTypes.TILDE,
            OdinTypes.ANDNOT,
            OdinTypes.LSHIFT,
            OdinTypes.RSHIFT
    );

    // TokenSet for floats
    public static final @NotNull TokenSet FLOAT_ARITHMETIC_OPERATORS = TokenSet.create(
            OdinTypes.PLUS,
            OdinTypes.MINUS,
            OdinTypes.STAR,
            OdinTypes.DIV
    );

    // TokenSet for enums
    public static final @NotNull TokenSet ENUM_ARITHMETIC_OPERATORS = TokenSet.create(
            OdinTypes.PLUS,
            OdinTypes.MINUS
    );

    public static final @NotNull TokenSet ENUM_BITWISE_OPERATORS = TokenSet.create(
            OdinTypes.AND,
            OdinTypes.PIPE,
            OdinTypes.TILDE,
            OdinTypes.ANDNOT
    );

    // TokenSet for arrays of numeric types
    public static final @NotNull TokenSet ARRAY_ARITHMETIC_OPERATORS = TokenSet.create(
            OdinTypes.PLUS,
            OdinTypes.MINUS,
            OdinTypes.STAR,
            OdinTypes.DIV
    );

    // TokenSet for matrices
    public static final @NotNull TokenSet MATRIX_ARITHMETIC_OPERATORS = TokenSet.create(
            OdinTypes.PLUS,
            OdinTypes.MINUS,
            OdinTypes.STAR,
            OdinTypes.DIV
    );

    // TokenSet for constant strings
    public static final @NotNull TokenSet STRING_ARITHMETIC_OPERATORS = TokenSet.create(
            OdinTypes.PLUS
    );

    // TokenSet for sets
    public static final @NotNull TokenSet SET_OPERATORS = TokenSet.create(
            OdinTypes.PLUS,
            OdinTypes.MINUS,
            OdinTypes.AND,
            OdinTypes.PIPE,
            OdinTypes.TILDE,
            OdinTypes.ANDNOT,
            OdinTypes.IN,
            OdinTypes.NOT_IN
    );


    public static boolean shouldAskParentForReferences(OdinImportPath ignored,
                                                       @NotNull PsiReferenceService.Hints ignored2) {
        return true;
    }

    public static PsiReference getReference(OdinImportPath ignored) {
        return null;
    }

    public static OdinImport getImportInfo(OdinImportPath importPath) {
        return OdinImportUtils.getImportInfo(importPath);
    }

    public static PsiReference[] getReferences(OdinImportPath importPath) {
        return ReferenceProvidersRegistry.getReferencesFromProviders(importPath, PsiReferenceService.Hints.HIGHLIGHTED_REFERENCES);
    }

    public static PsiReference[] getReferences(OdinImportPath importPath, PsiReferenceService.Hints hints) {
        return ReferenceProvidersRegistry.getReferencesFromProviders(importPath, hints);
    }

    public static PsiElement getOperator(OdinBinaryExpression self) {
        ASTNode[] children = self.getNode().getChildren(BINARY_OPERATORS);
        if (children.length > 0)
            return children[0].getPsi();
        return null;
    }

    public static PsiElement getOperator(OdinUnaryExpression unaryExpression) {
        ASTNode[] children = unaryExpression.getNode().getChildren(UNARY_OPERATORS);
        if (children.length > 0)
            return children[0].getPsi();
        return null;
    }

    public static OdinCompoundValueBody getCompoundValueBody(OdinCompoundValue self) {
        return PsiTreeUtil.findChildOfType(self, OdinCompoundValueBody.class);
    }

    public static List<OdinImportDeclarationStatement> getImportStatements(OdinFileScope self) {
        List<OdinImportDeclarationStatement> imports;
        if (self.getImportStatementsContainer() != null) {
            imports = self.getImportStatementsContainer().getImportDeclarationStatementList();
        } else {
            imports = Collections.emptyList();
        }

        List<OdinImportDeclarationStatement> importDeclarationStatementList = new ArrayList<>(imports);
        self.getFileScopeStatementList().getStatementList().stream()
                .filter(s -> s instanceof OdinImportDeclarationStatement)
                .map(OdinImportDeclarationStatement.class::cast)
                .forEach(importDeclarationStatementList::add);

        return importDeclarationStatementList;
    }

    // OdinDeclaration

    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinPackageDeclaration statement) {
        return Collections.singletonList(statement.getDeclaredIdentifier());
    }

    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinVariableInitializationStatement statement) {
        return statement.getDeclaredIdentifierList();
    }

    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinPolymorphicType polymorphicType) {
        return Collections.singletonList(polymorphicType.getDeclaredIdentifier());
    }

    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinParameterDeclaration statement) {
        return statement.getParameterList().stream().map(OdinParameter::getDeclaredIdentifier).toList();
    }

    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinConstantInitializationStatement statement) {
        return statement.getDeclaredIdentifierList();
    }

    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinVariableDeclarationStatement statement) {
        return statement.getDeclaredIdentifierList();
    }

    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinFieldDeclarationStatement statement) {
        return statement.getDeclaredIdentifierList();
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

    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinLabelDeclaration labelDeclaration) {
        return Collections.singletonList(labelDeclaration.getDeclaredIdentifier());
    }

    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinBitFieldFieldDeclaration bitFieldFieldDeclaration) {
        return Collections.singletonList(bitFieldFieldDeclaration.getDeclaredIdentifier());
    }

    // OdinTypedDeclaration

    public static OdinType getTypeDefinition(OdinConstantInitializationStatement statement) {
        return statement.getType();
    }

    public static OdinType getTypeDefinition(OdinVariableDeclarationStatement statement) {
        return statement.getType();
    }

    public static OdinType getTypeDefinition(OdinParameterDeclarator statement) {
        OdinTypeDefinitionContainer typeDefinitionContainer = statement.getTypeDefinitionContainer();
        if (typeDefinitionContainer != null) {
            return typeDefinitionContainer.getType();
        }

        return null;
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

    public static PsiElement setName(OdinImportDeclarationStatement importStatement, @NotNull String ignored) {
        return importStatement;
    }

    public static PsiElement getNameIdentifier(OdinImportDeclarationStatement importStatement) {
        if (importStatement.getAlias() != null)
            return importStatement.getAlias();
        return null;
    }


    public static PsiElement getType(OdinExpression ignore) {
        return null;
    }

    @NotNull
    public static OdinImport getImportInfo(OdinImportDeclarationStatement importStatement) {
        String name = importStatement.getAlias() != null
                ? importStatement.getAlias().getText()
                : null;

        String fullPathWithQuotes = importStatement.getPath().getText();
        String fullPath = fullPathWithQuotes.substring(1, fullPathWithQuotes.length() - 1);
        String path = fullPath;

        // Remove quotes
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

        return new OdinImport(fullPath, name, path, library, null);
    }

    public static OdinIdentifier getPackageIdentifier(OdinQualifiedType qualifiedType) {
        return qualifiedType.getIdentifier();
    }

    public static OdinIdentifier getTypeIdentifier(OdinQualifiedType qualifiedType) {
        OdinType typeExpression = qualifiedType.getSimpleRefType();
        if (typeExpression instanceof OdinSimpleRefType refType) {
            return refType.getIdentifier();
        }

        return null;
    }

    public static OdinType getTypeDefinition(OdinArrayType arrayType) {
        return arrayType.getType();
    }

    public static OdinType getKeyType(OdinMapType mapType) {
        return mapType.getTypeList().getFirst();
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

    // Procedure

    public static List<OdinSymbol> getSymbols(OdinProcedureExpression procedureExpression) {
        OdinProcedureType procedureType = procedureExpression
                .getProcedureLiteralType()
                .getProcedureDefinition()
                .getProcedureSignature()
                .getProcedureType();
        return doGetProcedureTypeSymbols(procedureType);
    }

    private static @NotNull List<OdinSymbol> doGetProcedureTypeSymbols(OdinProcedureType procedureType) {
        List<OdinSymbol> declarations = new ArrayList<>();
        {

            for (OdinParamEntry odinParamEntry : procedureType.getParamEntryList()) {
                declarations.addAll(OdinDeclarationSymbolResolver.getSymbols(odinParamEntry.getParameterDeclaration()));
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
                var paramEntries = procedureType.getReturnParameters().getParamEntryList();
                for (OdinParamEntry odinParamEntry : paramEntries) {
                    declarations.addAll(OdinDeclarationSymbolResolver.getSymbols(odinParamEntry.getParameterDeclaration()));
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

    public static List<OdinSymbol> getSymbols(OdinForBlock forInStatement) {
        List<OdinSymbol> symbols = new ArrayList<>();
        if (forInStatement.getForInParameterDeclaration() != null) {
            for (var forInParameter : forInStatement.getForInParameterDeclaration().getForInParameterDeclaratorList()) {
                OdinSymbol spec = new OdinSymbol(forInParameter.getDeclaredIdentifier());
                symbols.add(spec);
            }
        }
        return symbols;
    }

    public static List<OdinParamEntry> getParamEntryList(OdinProcedureType procedureType) {
        if (procedureType.getParamEntries() != null)
            return procedureType.getParamEntries().getParamEntryList();
        return Collections.emptyList();
    }

    public static List<OdinParamEntry> getParamEntryList(OdinStructType structType) {
        if (structType.getParamEntries() != null)
            return structType.getParamEntries().getParamEntryList();
        return Collections.emptyList();
    }

    public static List<OdinParamEntry> getParamEntryList(OdinUnionType unionType) {
        if (unionType.getParamEntries() != null)
            return unionType.getParamEntries().getParamEntryList();
        return Collections.emptyList();
    }

    public static List<OdinParamEntry> getParamEntryList(OdinReturnParameters returnParameters) {
        if (returnParameters.getParamEntries() != null)
            return returnParameters.getParamEntries().getParamEntryList();
        return Collections.emptyList();
    }

    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinUsingStatement usingStatement) {
        List<OdinDeclaredIdentifier> declaredIdentifiers = new ArrayList<>();
        for (OdinExpression expression : usingStatement.getExpressionList()) {
            TsOdinType tsOdinType = expression.getInferredType();

            OdinSymbolTable typeSymbols = OdinInsightUtils.getTypeElements(usingStatement.getProject(), tsOdinType);
            typeSymbols.getNamedElements().stream().filter(s -> s instanceof OdinDeclaredIdentifier)
                    .map(OdinDeclaredIdentifier.class::cast)
                    .forEach(declaredIdentifiers::add);
        }

        return declaredIdentifiers;
    }

    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinForInParameterDeclaration forInParameterDeclaration) {
        return forInParameterDeclaration.getForInParameterDeclaratorList().stream().map(OdinForInParameterDeclarator::getDeclaredIdentifier).toList();
    }

    public static List<OdinDeclaredIdentifier> getDeclaredIdentifiers(OdinSwitchTypeVariableDeclaration typeVariableDeclaration) {
        return Collections.singletonList(typeVariableDeclaration.getDeclaredIdentifier());
    }

    public static ItemPresentation getPresentation(OdinDeclaredIdentifier declaredIdentifier) {
        OdinSymbolType symbolType = OdinInsightUtils.classify(declaredIdentifier);
        Icon icon = OdinCompletionContributor.getIcon(symbolType);
        if (icon == null)
            icon = AllIcons.Nodes.Property;
        return new PresentationData(declaredIdentifier.getName(), "", icon, null);
    }

    public static @Nullable OdinIdentifier getIdentifier(OdinProcedureRef procedureRef) {
        OdinType type = procedureRef.getType();
        OdinIdentifier odinIdentifier;
        if (type instanceof OdinSimpleRefType refType) {
            odinIdentifier = refType.getIdentifier();
        } else if (type instanceof OdinQualifiedType qualifiedType) {
            odinIdentifier = qualifiedType.getIdentifier();
        } else {
            odinIdentifier = null;
        }
        return odinIdentifier;
    }

    public static List<OdinExpression> getExpressionList(OdinConstantInitializationStatement constantInitializationStatement) {
        return PsiTreeUtil.getChildrenOfTypeAsList(constantInitializationStatement, OdinExpression.class);
    }

    public static OdinRhsExpressions getRhsExpressions(OdinVariableInitializationStatement variableInitializationStatement) {
        return PsiTreeUtil.getChildOfType(variableInitializationStatement, OdinRhsExpressions.class);
    }

    public static OdinExpression getExpression(OdinSwitchInClause switchInClause) {
        return switchInClause.getSwitchInExpressionScope().getExpression();
    }

    public static List<OdinProcedureRef> getProcedureRefList(OdinProcedureGroupType procedureGroupType) {
        if (procedureGroupType.getProcedureGroupBlock() != null) {
            return procedureGroupType.getProcedureGroupBlock().getProcedureGroupBody().getProcedureRefList();
        }
        return Collections.emptyList();
    }

    public static PsiElement getBuildFlagIdentifierToken(OdinBuildFlagIdentifier buildFlagIdentifier) {
        ASTNode node = buildFlagIdentifier.getNode();
        ASTNode[] children = node.getChildren(TokenSet.create(BUILD_FLAG_IDENTIFIER_TOKEN));
        if (children.length > 0) {
            return children[0].getPsi();
        }
        return null;
    }

    public static String getName(OdinForeignImportDeclarationStatement foreignImportDeclarationStatement) {
        if (foreignImportDeclarationStatement.getAlias() != null)
            return foreignImportDeclarationStatement.getAlias().getName();
        if (foreignImportDeclarationStatement.getForeignImportPathList().size() == 1) {
            OdinForeignImportPath importPath = foreignImportDeclarationStatement.getForeignImportPathList().getFirst();
            return StringUtil.unquoteString(importPath.getText());
        }
        return null;
    }

    public static PsiElement setName(OdinForeignImportDeclarationStatement foreignImportDeclarationStatement, String name) {
        return foreignImportDeclarationStatement;
    }

    public static PsiElement getNameIdentifier(OdinForeignImportDeclarationStatement foreignImportDeclarationStatement) {
        if (foreignImportDeclarationStatement.getAlias() != null) {
            return foreignImportDeclarationStatement.getAlias();
        }
        if (foreignImportDeclarationStatement.getForeignImportPathList().size() == 1) {
            return foreignImportDeclarationStatement.getForeignImportPathList().getFirst();
        }
        return null;
    }
}
