package com.lasagnerd.odin.insights.typeInference;

import com.intellij.psi.PsiNamedElement;
import com.lasagnerd.odin.insights.OdinInsightUtils;
import com.lasagnerd.odin.insights.OdinScope;
import com.lasagnerd.odin.insights.typeSystem.*;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;

public class OdinExpressionInferenceEngine extends OdinVisitor {

    final OdinScope scope;

    TsOdinType type;
    OdinImportDeclarationStatement importDeclarationStatement;

    boolean isImport;

    public OdinExpressionInferenceEngine(OdinScope scope) {
        this.scope = scope;
    }

    public static OdinTypeInferenceResult inferType(OdinScope scope, OdinExpression expression) {
        OdinExpressionInferenceEngine odinExpressionTypeResolver = new OdinExpressionInferenceEngine(scope);
        expression.accept(odinExpressionTypeResolver);
        OdinTypeInferenceResult typeInferenceResult = new OdinTypeInferenceResult();
        typeInferenceResult.setImportDeclarationStatement(odinExpressionTypeResolver.importDeclarationStatement);
        typeInferenceResult.setType(odinExpressionTypeResolver.type);
        typeInferenceResult.setImport(odinExpressionTypeResolver.isImport);
        return typeInferenceResult;
    }

    @Override
    public void visitRefExpression(@NotNull OdinRefExpression refExpression) {
        OdinScope localScope;
        if (refExpression.getExpression() != null) {
            // solve for expression first. This defines the scope
            // extract scope
            OdinExpressionInferenceEngine odinExpressionTypeResolver = new OdinExpressionInferenceEngine(this.scope);
            refExpression.getExpression().accept(odinExpressionTypeResolver);

            localScope = OdinInsightUtils.getScopeProvidedByType(odinExpressionTypeResolver.type);
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
            OdinTypeExpression typeExpression = (OdinTypeExpression) typed.getExpression();
            this.type = OdinTypeResolver.resolveType(this.scope, typeExpression);
        }
    }

    @Override
    public void visitCallExpression(@NotNull OdinCallExpression o) {
        // Get type of expression. If it is callable, retrieve the return type and set that as result
        OdinExpression expression = o.getExpression();
        OdinExpressionInferenceEngine odinExpressionTypeResolver = new OdinExpressionInferenceEngine(this.scope);
        expression.accept(odinExpressionTypeResolver);

        // TODO test this 
        TsOdinType tsOdinType = odinExpressionTypeResolver.type;
        if (tsOdinType instanceof TsOdinMetaType tsOdinMetaType && tsOdinMetaType.getMetaType() == TsOdinMetaType.MetaType.PROCEDURE) {
            TsOdinProcedureType procedureType = (TsOdinProcedureType) OdinTypeResolver.resolveType(scope, tsOdinMetaType.getTypeExpression());
            if (procedureType != null && !procedureType.getReturnTypes().isEmpty()) {
                this.type = procedureType.getReturnTypes().get(0);
            }
        }
    }

    @Override
    public void visitIndexExpression(@NotNull OdinIndexExpression o) {
        // get type of expression. IF it is indexable (array, matrix, bitset, map), retrieve the indexed type and set that as result
        OdinExpression expression = o.getExpression();
        OdinExpressionInferenceEngine odinExpressionTypeResolver = new OdinExpressionInferenceEngine(this.scope);
        expression.accept(odinExpressionTypeResolver);


        TsOdinType tsOdinType = odinExpressionTypeResolver.type;

        if (tsOdinType instanceof TsOdinPointerType pointerType) {
            tsOdinType = pointerType.getDereferencedType();
        }

        if (tsOdinType instanceof TsOdinArrayType arrayType) {
            this.type = arrayType.getElementType();
        }

        if (tsOdinType instanceof TsOdinMapType mapType) {
            this.type = mapType.getValueType();
        }
    }

    @Override
    public void visitDereferenceExpression(@NotNull OdinDereferenceExpression o) {
        // get type of expression. If it is a pointer, retrieve the dereferenced type and set that as result
        OdinExpression expression = o.getExpression();
        OdinExpressionInferenceEngine odinExpressionTypeResolver = new OdinExpressionInferenceEngine(this.scope);
        expression.accept(odinExpressionTypeResolver);
        if (odinExpressionTypeResolver.type instanceof TsOdinPointerType pointerType) {
            this.type = pointerType.getDereferencedType();
        }
    }

    @Override
    public void visitParenthesizedExpression(@NotNull OdinParenthesizedExpression o) {
        OdinExpression expression = o.getExpression();
        OdinExpressionInferenceEngine odinExpressionTypeResolver = new OdinExpressionInferenceEngine(this.scope);
        if (expression != null) {
            expression.accept(odinExpressionTypeResolver);
            this.type = odinExpressionTypeResolver.type;
        }
    }

    @Override
    public void visitProcedureExpression(@NotNull OdinProcedureExpression o) {
        // get type of expression. If it is a procedure, retrieve the return type and set that as result
        var procedureType = o.getProcedureExpressionType().getProcedureType();
        TsOdinMetaType tsOdinMetaType = new TsOdinMetaType(TsOdinMetaType.MetaType.PROCEDURE);
        tsOdinMetaType.setTypeExpression(procedureType);

        this.type = tsOdinMetaType;
    }

    @Override
    public void visitCastExpression(@NotNull OdinCastExpression o) {
        OdinTypeDefinitionExpression typeDefinitionExpression = (OdinTypeDefinitionExpression) o.getTypeDefinitionExpression();
        this.type = OdinTypeResolver.resolveType(scope, typeDefinitionExpression.getMainTypeExpression());
    }

    @Override
    public void visitOrElseExpression(@NotNull OdinOrElseExpression o) {

    }

    public static TsOdinType resolveTypeOfDeclaration(OdinScope parentScope, OdinDeclaredIdentifier declaredIdentifier, OdinDeclaration odinDeclaration) {
        if (odinDeclaration instanceof OdinVariableDeclarationStatement declarationStatement) {
            var mainType = declarationStatement.getTypeDefinitionExpression().getMainTypeExpression();
            //return getDeclaredIdentifierQualifiedType(parentScope, mainType);
            return OdinTypeResolver.resolveType(parentScope, mainType);
        }

        if (odinDeclaration instanceof OdinVariableInitializationStatement initializationStatement) {
            if (initializationStatement.getTypeDefinitionExpression() != null) {
                OdinTypeExpression mainTypeExpression = initializationStatement.getTypeDefinitionExpression().getMainTypeExpression();
                return OdinTypeResolver.resolveType(parentScope, mainTypeExpression);
            }

            int index = initializationStatement.getIdentifierList().getDeclaredIdentifierList().indexOf(declaredIdentifier);
            OdinExpression odinExpression = initializationStatement.getExpressionsList().getExpressionList().get(index);
            OdinExpressionInferenceEngine odinExpressionTypeResolver = new OdinExpressionInferenceEngine(parentScope);
            odinExpression.accept(odinExpressionTypeResolver);
            return odinExpressionTypeResolver.type;
        }

        if (odinDeclaration instanceof OdinConstantInitializationStatement initializationStatement) {
            if (initializationStatement.getTypeDefinitionExpression() != null) {
                OdinTypeExpression mainType = initializationStatement.getTypeDefinitionExpression().getMainTypeExpression();
                return OdinTypeResolver.resolveType(parentScope, mainType);
            }
            int index = initializationStatement.getIdentifierList().getDeclaredIdentifierList().indexOf(declaredIdentifier);
            OdinExpression odinExpression = initializationStatement.getExpressionsList().getExpressionList().get(index);
            OdinExpressionInferenceEngine odinExpressionTypeResolver = new OdinExpressionInferenceEngine(parentScope);
            odinExpression.accept(odinExpressionTypeResolver);
            return odinExpressionTypeResolver.type;
        }

        if (odinDeclaration instanceof OdinFieldDeclarationStatement fieldDeclarationStatement) {
            OdinExpression mainType;
            if (fieldDeclarationStatement.getTypeDefinitionExpression() instanceof OdinTypeExpression expr) {
                mainType = expr;
            } else {

                OdinTypeDefinitionExpression typeDefinition = fieldDeclarationStatement.getTypeDefinitionExpression();
                mainType = typeDefinition.getMainTypeExpression();
            }

            OdinTypeResolver odinTypeExpressionResolver = new OdinTypeResolver(parentScope);
            mainType.accept(odinTypeExpressionResolver);
            return odinTypeExpressionResolver.type;
        }

        if (odinDeclaration instanceof OdinParameterDecl parameterDeclaration) {
            return OdinTypeResolver.resolveType(parentScope, parameterDeclaration.getTypeDefinition().getMainTypeExpression());
        }

        if (odinDeclaration instanceof OdinParameterInitialization parameterInitialization) {
            OdinTypeDefinitionExpression typeDefinition = parameterInitialization.getTypeDefinition();
            if (typeDefinition != null) {
                return OdinTypeResolver.resolveType(parentScope, typeDefinition.getMainTypeExpression());
            }

            OdinExpression odinExpression = parameterInitialization.getExpression();
            OdinExpressionInferenceEngine odinExpressionTypeResolver = new OdinExpressionInferenceEngine(parentScope);
            odinExpression.accept(odinExpressionTypeResolver);
            return odinExpressionTypeResolver.type;
        }

        if (odinDeclaration instanceof OdinProcedureDeclarationStatement procedure) {
            TsOdinMetaType tsOdinMetaType = new TsOdinMetaType(TsOdinMetaType.MetaType.PROCEDURE);
            tsOdinMetaType.setDeclaration(procedure);
            tsOdinMetaType.setTypeExpression(procedure.getProcedureType());
            return tsOdinMetaType;
        }

        if (odinDeclaration instanceof OdinStructDeclarationStatement structDeclarationStatement) {
            TsOdinMetaType tsOdinMetaType = new TsOdinMetaType(TsOdinMetaType.MetaType.STRUCT);
            tsOdinMetaType.setDeclaration(structDeclarationStatement);
            tsOdinMetaType.setTypeExpression(structDeclarationStatement.getStructType());
            return tsOdinMetaType;
        }

        if (odinDeclaration instanceof OdinEnumDeclarationStatement enumDeclarationStatement) {
            TsOdinMetaType tsOdinMetaType = new TsOdinMetaType(TsOdinMetaType.MetaType.ENUM);
            tsOdinMetaType.setDeclaration(enumDeclarationStatement);
            tsOdinMetaType.setTypeExpression(enumDeclarationStatement.getEnumType());
            return tsOdinMetaType;
        }

        if (odinDeclaration instanceof OdinUnionDeclarationStatement unionDeclarationStatement) {
            TsOdinMetaType tsOdinMetaType = new TsOdinMetaType(TsOdinMetaType.MetaType.UNION);
            tsOdinMetaType.setDeclaration(unionDeclarationStatement);
            tsOdinMetaType.setTypeExpression(unionDeclarationStatement.getUnionType());
            return tsOdinMetaType;
        }


        return TsOdinType.UNKNOWN;
    }
}

