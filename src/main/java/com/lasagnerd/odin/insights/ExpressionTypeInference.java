package com.lasagnerd.odin.insights;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.lasagnerd.odin.lang.psi.*;
import com.lasagnerd.odin.lang.typeSystem.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

class ExpressionTypeInference extends OdinVisitor {

    final Scope scope;

    @Deprecated
    OdinDeclaredIdentifier typeIdentifier;
    TsOdinType type;
    OdinImportDeclarationStatement importDeclarationStatement;

    boolean isImport;

    public ExpressionTypeInference(Scope scope) {
        this.scope = scope;
    }

    @Override
    public void visitRefExpression(@NotNull OdinRefExpression refExpression) {
        Scope localScope;
        if (refExpression.getExpression() != null) {
            // solve for expression first. This defines the scope
            // extract scope
            ExpressionTypeInference expressionTypeInference = new ExpressionTypeInference(this.scope);
            refExpression.getExpression().accept(expressionTypeInference);

            localScope = getCompletionScopeOfType(this.scope, expressionTypeInference.typeIdentifier);

            // TODO
            Scope localScope2 = expressionTypeInference.type.getScope();
            if(localScope2 != null) {
                localScope = localScope2;
            }
            System.out.println(localScope2);
        } else {
            localScope = this.scope;
        }

        // using current scope, find identifier declaration and extract type
        PsiNamedElement namedElement = localScope.findNamedElement(refExpression.getIdentifier().getText());
        if (namedElement instanceof OdinImportDeclarationStatement) {
            isImport = true;
            importDeclarationStatement = (OdinImportDeclarationStatement) namedElement;
        } else if (namedElement instanceof OdinDeclaredIdentifier declaredIdentifier) {
            OdinDeclaration odinDeclaration = OdinInsightUtils.findFirstParentOfType(declaredIdentifier, true, OdinDeclaration.class);

            if (odinDeclaration instanceof OdinImportDeclarationStatement) {
                isImport = true;
                importDeclarationStatement = (OdinImportDeclarationStatement) odinDeclaration;
            } else {
                typeIdentifier = findTypeIdentifier(this.scope, declaredIdentifier, odinDeclaration);
                this.type = resolveTypeOfDeclaration(this.scope, declaredIdentifier, odinDeclaration);
            }
        }
    }

    @Override
    public void visitCompoundLiteralExpression(@NotNull OdinCompoundLiteralExpression o) {
        if (o.getCompoundLiteral() instanceof OdinCompoundLiteralTyped typed) {
            typeIdentifier = getDeclaredIdentifierQualifiedType(this.scope, typed.getTypeExpression());
            this.type = TypeExpressionResolver.resolveType(this.scope, typed.getTypeExpression());
        }
    }

    @Override
    public void visitCallExpression(@NotNull OdinCallExpression o) {
        // Get type of expression. If it is callable, retrieve the return type and set that as result
        OdinExpression expression = o.getExpression();
        ExpressionTypeInference expressionTypeInference = new ExpressionTypeInference(this.scope);
        expression.accept(expressionTypeInference);

        // TODO test this 
        TsOdinType tsOdinType = expressionTypeInference.type;
        if(tsOdinType instanceof TsOdinProcedureType procedureType) {
            if(!procedureType.getReturnTypes().isEmpty()) {
                this.type = procedureType.getReturnTypes().get(0);
            }
        }
    }

    @Override
    public void visitIndexExpression(@NotNull OdinIndexExpression o) {
        // get type of expression. IF it is indexable (array, matrix, bitset, map), retrieve the indexed type and set that as result
        OdinExpression expression = o.getExpression();
        ExpressionTypeInference expressionTypeInference = new ExpressionTypeInference(this.scope);
        expression.accept(expressionTypeInference);
        if(expressionTypeInference.type instanceof TsOdinArrayType arrayType) {
            this.type = arrayType.getElementType();
        }

        if(expressionTypeInference.type instanceof TsOdinMapType mapType) {
            this.type = mapType.getValueType();
        }
    }

    @Override
    public void visitDereferenceExpression(@NotNull OdinDereferenceExpression o) {
        // get type of expression. If it is a pointer, retrieve the dereferenced type and set that as result
        OdinExpression expression = o.getExpression();
        ExpressionTypeInference expressionTypeInference = new ExpressionTypeInference(this.scope);
        expression.accept(expressionTypeInference);
        if(expressionTypeInference.type instanceof TsOdinPointerType pointerType) {
            this.type = pointerType.getDereferencedType();
        }
    }

    @Override
    public void visitParenthesizedExpression(@NotNull OdinParenthesizedExpression o) {
        OdinExpression expression = o.getExpression();
        ExpressionTypeInference expressionTypeInference = new ExpressionTypeInference(this.scope);
        if(expression != null) {
            expression.accept(expressionTypeInference);
            this.type = expressionTypeInference.type;
        }
    }

    @Override
    public void visitProcedureExpression(@NotNull OdinProcedureExpression o) {
        // get type of expression. If it is a procedure, retrieve the return type and set that as result
        var procedureType = o.getProcedureExpressionType().getProcedureType();
        this.type = TypeExpressionResolver.resolveType(this.scope, procedureType);
    }


    @Nullable
    private static OdinExpression getReturnTypeExpression(OdinProcedureDeclarationStatement procedure) {
        OdinExpression type = null;
        OdinReturnParameters returnParameters = procedure.getProcedureType().getReturnParameters();
        if (returnParameters != null) {


            OdinExpression returnExpression = returnParameters.getTypeDefinitionExpression();
            OdinParamEntries paramEntries = returnParameters.getParamEntries();
            if (paramEntries != null) {
                if (paramEntries.getParamEntryList().size() == 1) {
                    OdinTypeDefinitionExpression typeDefinition1 = paramEntries.getParamEntryList().get(0).getParameterDeclaration().getTypeDefinition();
                    type = typeDefinition1.getMainTypeExpression();
                }

            } else if (returnExpression instanceof OdinQualifiedType typRef) {
                type = typRef;
            }
        }
        return type;
    }

    @NotNull
    public static List<PsiNamedElement> getEnumFields(OdinEnumDeclarationStatement enumDeclarationStatement) {
        OdinEnumBody enumBody = enumDeclarationStatement.getEnumType()
                .getEnumBlock()
                .getEnumBody();

        if (enumBody == null)
            return Collections.emptyList();

        return enumBody
                .getEnumValueDeclarationList()
                .stream()
                .map(OdinEnumValueDeclaration::getDeclaredIdentifier)
                .collect(Collectors.toList());
    }

    public static List<PsiNamedElement> getStructFields(OdinStructDeclarationStatement structDeclarationStatement) {
        OdinStructBody structBody = structDeclarationStatement
                .getStructType()
                .getStructBlock()
                .getStructBody();

        if (structBody == null) {
            return Collections.emptyList();
        }

        return structBody.getFieldDeclarationStatementList().stream()
                .flatMap(x -> x.getDeclaredIdentifiers().stream())
                .collect(Collectors.toList());
    }

    /**
     * Finds the type declaration for a given identifier. Works only for types that are not arrays, maps, slices, etc.
     *
     * @param parentScope        The scope in which the identifier is declared
     * @param declaredIdentifier The identifier for which the type should be found
     * @param odinDeclaration    The declaration in which the identifier is declared
     * @return The type declaration for the given identifier
     */
    @Deprecated
    private static OdinDeclaredIdentifier findTypeIdentifier(Scope parentScope, OdinDeclaredIdentifier declaredIdentifier, OdinDeclaration odinDeclaration) {
        if (odinDeclaration instanceof OdinVariableDeclarationStatement declarationStatement) {
            var mainType = declarationStatement.getTypeDefinition().getMainTypeExpression();
            return getDeclaredIdentifierQualifiedType(parentScope, mainType);
        }

        if (odinDeclaration instanceof OdinVariableInitializationStatement initializationStatement) {
            if (initializationStatement.getTypeDefinition() != null) {
                return getDeclaredIdentifierQualifiedType(parentScope, initializationStatement.getTypeDefinition().getMainTypeExpression());
            }

            int index = initializationStatement.getIdentifierList().getDeclaredIdentifierList().indexOf(declaredIdentifier);
            OdinExpression odinExpression = initializationStatement.getExpressionsList().getExpressionList().get(index);
            ExpressionTypeInference expressionTypeInference = new ExpressionTypeInference(parentScope);
            odinExpression.accept(expressionTypeInference);
            return expressionTypeInference.typeIdentifier;
        }

        if (odinDeclaration instanceof OdinConstantInitializationStatement initializationStatement) {
            if (initializationStatement.getTypeDefinition() != null) {
                OdinExpression mainType = initializationStatement.getTypeDefinition().getMainTypeExpression();
                return getDeclaredIdentifierQualifiedType(parentScope, mainType);
            }
            int index = initializationStatement.getIdentifierList().getDeclaredIdentifierList().indexOf(declaredIdentifier);
            OdinExpression odinExpression = initializationStatement.getExpressionsList().getExpressionList().get(index);
            ExpressionTypeInference expressionTypeInference = new ExpressionTypeInference(parentScope);
            odinExpression.accept(expressionTypeInference);
            return expressionTypeInference.typeIdentifier;
        }

        if (odinDeclaration instanceof OdinFieldDeclarationStatement fieldDeclarationStatement) {
            OdinExpression mainType;
            if (fieldDeclarationStatement.getTypeDefinition() instanceof OdinTypeExpression expr) {
                mainType = expr;
            } else {

                OdinTypeDefinitionExpression typeDefinition = fieldDeclarationStatement.getTypeDefinition();
                mainType = typeDefinition.getMainTypeExpression();
            }

            return getDeclaredIdentifierQualifiedType(parentScope, mainType);
        }

        if (odinDeclaration instanceof OdinParameterDeclaration parameterDeclaration) {

        }

        if (odinDeclaration instanceof OdinParameterInitialization parameterInitialization) {

        }

        if (odinDeclaration instanceof OdinProcedureDeclarationStatement procedure) {
            return procedure.getDeclaredIdentifier();
        }

        return null;
    }

    private TsOdinType resolveTypeOfDeclaration(Scope parentScope, OdinDeclaredIdentifier declaredIdentifier, OdinDeclaration odinDeclaration) {
        if (odinDeclaration instanceof OdinVariableDeclarationStatement declarationStatement) {
            var mainType = declarationStatement.getTypeDefinition().getMainTypeExpression();
            //return getDeclaredIdentifierQualifiedType(parentScope, mainType);
            return TypeExpressionResolver.resolveType(parentScope, mainType);
        }

        if (odinDeclaration instanceof OdinVariableInitializationStatement initializationStatement) {
            if (initializationStatement.getTypeDefinition() != null) {
                OdinTypeExpression mainTypeExpression = initializationStatement.getTypeDefinition().getMainTypeExpression();
                return TypeExpressionResolver.resolveType(parentScope, mainTypeExpression);
            }

            int index = initializationStatement.getIdentifierList().getDeclaredIdentifierList().indexOf(declaredIdentifier);
            OdinExpression odinExpression = initializationStatement.getExpressionsList().getExpressionList().get(index);
            ExpressionTypeInference expressionTypeInference = new ExpressionTypeInference(parentScope);
            odinExpression.accept(expressionTypeInference);
            return expressionTypeInference.type;
        }

        if (odinDeclaration instanceof OdinConstantInitializationStatement initializationStatement) {
            if (initializationStatement.getTypeDefinition() != null) {
                OdinExpression mainType = initializationStatement.getTypeDefinition().getMainTypeExpression();
                //return getDeclaredIdentifierQualifiedType(parentScope, mainType);
            }
            int index = initializationStatement.getIdentifierList().getDeclaredIdentifierList().indexOf(declaredIdentifier);
            OdinExpression odinExpression = initializationStatement.getExpressionsList().getExpressionList().get(index);
            ExpressionTypeInference expressionTypeInference = new ExpressionTypeInference(parentScope);
            odinExpression.accept(expressionTypeInference);
            return expressionTypeInference.type;
        }

        if (odinDeclaration instanceof OdinFieldDeclarationStatement fieldDeclarationStatement) {
            OdinExpression mainType;
            if (fieldDeclarationStatement.getTypeDefinition() instanceof OdinTypeExpression expr) {
                mainType = expr;
            } else {

                OdinTypeDefinitionExpression typeDefinition = fieldDeclarationStatement.getTypeDefinition();
                mainType = typeDefinition.getMainTypeExpression();
            }

            TypeExpressionResolver typeExpressionResolver = new TypeExpressionResolver(parentScope);
            mainType.accept(typeExpressionResolver);
            return typeExpressionResolver.type;
        }

        if (odinDeclaration instanceof OdinParameterDeclarationStatement parameterDeclaration) {
            return TypeExpressionResolver.resolveType(scope, parameterDeclaration.getTypeDefinition().getMainTypeExpression());
        }

        if (odinDeclaration instanceof OdinParameterInitialization parameterInitialization) {
            OdinTypeDefinitionExpression typeDefinition = parameterInitialization.getTypeDefinition();
            if(typeDefinition != null) {
                return TypeExpressionResolver.resolveType(parentScope, typeDefinition.getMainTypeExpression());
            }

            OdinExpression odinExpression = parameterInitialization.getExpressionList().get(0);
            ExpressionTypeInference expressionTypeInference = new ExpressionTypeInference(parentScope);
            odinExpression.accept(expressionTypeInference);
            return expressionTypeInference.type;
        }

        if (odinDeclaration instanceof OdinProcedureDeclarationStatement procedure) {
            return TypeExpressionResolver.resolveType(parentScope, procedure.getProcedureType());
        }

        return TsOdinType.UNKNOWN;
    }

    private static OdinDeclaredIdentifier getDeclaredIdentifierQualifiedType(Scope parentScope, @Nullable OdinExpression typeExpression) {
        if (typeExpression instanceof OdinQualifiedType odinQualifiedType) {
            if (odinQualifiedType.getPackageIdentifier() != null) {
                Scope packageScope = parentScope.getScopeOfImport(odinQualifiedType.getPackageIdentifier().getIdentifierToken().getText());
                return (OdinDeclaredIdentifier) packageScope.findNamedElement(odinQualifiedType.getTypeIdentifier().getIdentifierToken().getText());
            } else {
                return (OdinDeclaredIdentifier) parentScope.findNamedElement(odinQualifiedType.getTypeIdentifier()
                        .getIdentifierToken()
                        .getText());
            }
        }
        return null;
    }

    static Scope getCompletionScopeOfType(Scope scope, PsiElement identifier) {
        if (!(identifier instanceof OdinDeclaredIdentifier))
            return null;

        OdinDeclaration odinDeclaration = OdinInsightUtils.findFirstParentOfType(identifier,
                false,
                OdinDeclaration.class);

        if (odinDeclaration instanceof OdinStructDeclarationStatement structDeclarationStatement) {
            return scope.with(getStructFields(structDeclarationStatement));
        }

        if (odinDeclaration instanceof OdinEnumDeclarationStatement enumDeclarationStatement) {
            return scope.with(getEnumFields(enumDeclarationStatement));
        }

        if (odinDeclaration instanceof OdinImportDeclarationStatement importDeclarationStatement) {
            return OdinInsightUtils.getDeclarationsOfImportedPackage(scope, importDeclarationStatement);
        }

        return Scope.EMPTY;
    }
}
