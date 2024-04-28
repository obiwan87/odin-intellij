package com.lasagnerd.odin.insights;

import com.intellij.psi.PsiNamedElement;
import com.lasagnerd.odin.lang.psi.*;
import com.lasagnerd.odin.lang.typeSystem.*;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class ExpressionTypeInference extends OdinVisitor {

    final Scope scope;

    TsOdinType type;
    OdinImportDeclarationStatement importDeclarationStatement;

    boolean isImport;

    public ExpressionTypeInference(Scope scope) {
        this.scope = scope;
    }

    static TypeInferenceResult inferType(Scope scope, OdinExpression expression) {
        ExpressionTypeInference expressionTypeInference = new ExpressionTypeInference(scope);
        expression.accept(expressionTypeInference);
        TypeInferenceResult typeInferenceResult = new TypeInferenceResult();
        typeInferenceResult.setImportDeclarationStatement(expressionTypeInference.importDeclarationStatement);
        typeInferenceResult.setType(expressionTypeInference.type);
        typeInferenceResult.setImport(expressionTypeInference.isImport);
        return typeInferenceResult;
    }

    @Override
    public void visitRefExpression(@NotNull OdinRefExpression refExpression) {
        Scope localScope;
        if (refExpression.getExpression() != null) {
            // solve for expression first. This defines the scope
            // extract scope
            ExpressionTypeInference expressionTypeInference = new ExpressionTypeInference(this.scope);
            refExpression.getExpression().accept(expressionTypeInference);

            localScope = OdinInsightUtils.getScopeProvidedByTypeExpression(expressionTypeInference.type);
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
                this.type = resolveTypeOfDeclaration(this.scope, declaredIdentifier, odinDeclaration);
            }
        }
    }

    @Override
    public void visitCompoundLiteralExpression(@NotNull OdinCompoundLiteralExpression o) {
        if (o.getCompoundLiteral() instanceof OdinCompoundLiteralTyped typed) {
            OdinTypeExpression typeExpression = typed.getTypeExpression();
            if(typeExpression != null) {
                this.type = TypeExpressionResolver.resolveType(this.scope, typeExpression);
            }
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


        TsOdinType tsOdinType = expressionTypeInference.type;

        if(tsOdinType instanceof TsOdinPointerType pointerType) {
            tsOdinType = pointerType.getDereferencedType();
        }

        if(tsOdinType instanceof TsOdinArrayType arrayType) {
            this.type = arrayType.getElementType();
        }

        if(tsOdinType instanceof TsOdinMapType mapType) {
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

    @Override
    public void visitCastExpression(@NotNull OdinCastExpression o) {
        OdinTypeDefinitionExpression typeDefinitionExpression = (OdinTypeDefinitionExpression) o.getTypeDefinitionExpression();
        this.type = TypeExpressionResolver.resolveType(scope, typeDefinitionExpression.getMainTypeExpression());
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

            } else if (returnExpression instanceof OdinQualifiedType qualifiedType) {
                type = qualifiedType;
            }
        }
        return type;
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
                OdinTypeExpression mainType = initializationStatement.getTypeDefinition().getMainTypeExpression();
                return TypeExpressionResolver.resolveType(parentScope, mainType);
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


}

@Data
class TypeInferenceResult {
    boolean isImport;
    OdinImportDeclarationStatement importDeclarationStatement;
    TsOdinType type;
}