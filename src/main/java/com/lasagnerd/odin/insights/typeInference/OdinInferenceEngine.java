package com.lasagnerd.odin.insights.typeInference;

import com.intellij.psi.PsiNamedElement;
import com.lasagnerd.odin.insights.OdinInsightUtils;
import com.lasagnerd.odin.insights.OdinScope;
import com.lasagnerd.odin.insights.typeSystem.*;
import com.lasagnerd.odin.lang.OdinLangSyntaxAnnotator;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class OdinInferenceEngine extends OdinVisitor {

    final OdinScope scope;

    TsOdinType type;
    OdinImportDeclarationStatement importDeclarationStatement;

    boolean isImport;


    private final TsOdinType expectedType;
    private final int lhsValuesCount;


    public OdinInferenceEngine(OdinScope scope) {
        this.scope = scope;
        this.lhsValuesCount = 1;
        this.expectedType = TsOdinType.UNKNOWN;
    }

    public OdinInferenceEngine(OdinScope scope, @NotNull TsOdinType expectedType, int lhsValuesCount) {
        this.scope = scope;
        this.expectedType = expectedType;
        this.lhsValuesCount = lhsValuesCount;
    }

    public static OdinTypeInferenceResult inferType(OdinScope scope, OdinExpression expression) {
        OdinInferenceEngine odinExpressionTypeResolver = new OdinInferenceEngine(scope);
        expression.accept(odinExpressionTypeResolver);
        OdinTypeInferenceResult typeInferenceResult = new OdinTypeInferenceResult();
        typeInferenceResult.setImportDeclarationStatement(odinExpressionTypeResolver.importDeclarationStatement);
        typeInferenceResult.setType(odinExpressionTypeResolver.type);
        typeInferenceResult.setImport(odinExpressionTypeResolver.isImport);
        return typeInferenceResult;
    }

    public static TsOdinType doInferType(OdinScope scope, @NotNull OdinExpression expression) {
        return doInferType(scope, TsOdinType.UNKNOWN, 1, expression);
    }

    public static TsOdinType doInferType(OdinScope scope, TsOdinType expectedType, int lhsValuesCount, @NotNull OdinExpression expression) {
        OdinInferenceEngine odinExpressionTypeResolver = new OdinInferenceEngine(scope, expectedType, lhsValuesCount);
        expression.accept(odinExpressionTypeResolver);

        TsOdinType type = odinExpressionTypeResolver.type;
        if (type == null) {
            return TsOdinType.UNKNOWN;
        }
        return type;
    }

    public static TsOdinType doInferType(OdinScope scope, int lhsValuesCount, @NotNull OdinExpression expression) {
        return doInferType(scope, TsOdinType.UNKNOWN, lhsValuesCount, expression);
    }

    public static TsOdinType doInferType(OdinExpression odinExpression) {
        OdinScope scope = OdinInsightUtils.findScope(odinExpression);
        return doInferType(scope, odinExpression);
    }

    @Override
    public void visitTypeDefinitionExpression(@NotNull OdinTypeDefinitionExpression o) {
        TsOdinBuiltInType tsOdinBuiltInType = new TsOdinBuiltInType();
        tsOdinBuiltInType.setName("typeid");
        this.type = tsOdinBuiltInType;
    }

    @Override
    public void visitRefExpression(@NotNull OdinRefExpression refExpression) {
        OdinScope localScope;
        if (refExpression.getExpression() != null) {
            // solve for expression first. This defines the scope
            // extract scope

            TsOdinType tsOdinType = doInferType(scope, refExpression.getExpression());
            localScope = OdinInsightUtils.getScopeProvidedByType(tsOdinType);

            // The resolved polymorphic types must be taken over from type scope
            this.scope.addTypes(localScope);
        } else {
            localScope = this.scope;
        }

        // using current scope, find identifier declaration and extract type
        String name = refExpression.getIdentifier().getText();
        PsiNamedElement namedElement = localScope.getNamedElement(name);
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
        } else if (namedElement == null) {
            if (OdinLangSyntaxAnnotator.RESERVED_TYPES.contains(name)) {
                TsOdinMetaType tsOdinMetaType = new TsOdinMetaType(TsOdinMetaType.MetaType.BUILTIN);
                tsOdinMetaType.setName(name);
                this.type = tsOdinMetaType;
            }
        }
    }

    @Override
    public void visitCompoundLiteralExpression(@NotNull OdinCompoundLiteralExpression o) {
        if (o.getCompoundLiteral() instanceof OdinCompoundLiteralTyped typed) {
            OdinType typeExpression = typed.getType();
            this.type = OdinTypeResolver.resolveType(this.scope, typeExpression);
        }
    }

    @Override
    public void visitCallExpression(@NotNull OdinCallExpression o) {
        // Get type of expression. If it is callable, retrieve the return type and set that as result
        TsOdinType tsOdinType = doInferType(scope, o.getExpression());
        if (tsOdinType instanceof TsOdinMetaType tsOdinMetaType) {
            if (tsOdinMetaType.getMetaType() == TsOdinMetaType.MetaType.PROCEDURE) {
                TsOdinProcedureType procedureType = (TsOdinProcedureType) OdinTypeResolver.resolveMetaType(scope, tsOdinMetaType);
                if (procedureType != null && !procedureType.getReturnTypes().isEmpty()) {
                    TsOdinProcedureType instantiatedType = OdinTypeInstantiator
                            .instantiateProcedure(scope, o.getArgumentList(), procedureType);
                    if (instantiatedType.getReturnTypes().size() == 1) {
                        this.type = instantiatedType.getReturnTypes().get(0);
                    } else if (instantiatedType.getReturnTypes().size() > 1) {
                        this.type = new TsOdinTuple(instantiatedType.getReturnTypes());
                    } else {
                        this.type = TsOdinType.VOID;
                    }
                }
            }

            if (tsOdinMetaType.getMetaType() == TsOdinMetaType.MetaType.STRUCT) {
                TsOdinStructType structType = (TsOdinStructType) OdinTypeResolver.resolveMetaType(scope, tsOdinMetaType);
                if (structType != null) {
                    this.type = OdinTypeInstantiator.instantiateStruct(scope, o.getArgumentList(), structType);
                }
            }
        }
    }

    @Override
    public void visitIndexExpression(@NotNull OdinIndexExpression o) {
        // get type of expression. IF it is indexable (array, matrix, bitset, map), retrieve the indexed type and set that as result
        OdinExpression expression = o.getExpression();
        TsOdinType tsOdinType = doInferType(scope, expression);

        if (tsOdinType instanceof TsOdinArrayType arrayType) {
            this.type = arrayType.getElementType();
        }

        if (tsOdinType instanceof TsOdinMapType mapType) {
            this.type = mapType.getValueType();
        }
    }

    @Override
    public void visitSliceExpression(@NotNull OdinSliceExpression o) {
        OdinExpression expression = o.getExpression();
        TsOdinType tsOdinType = doInferType(scope, expression);
        if(tsOdinType instanceof TsOdinArrayType) {
            this.type = tsOdinType;
        }
    }

    @Override
    public void visitDereferenceExpression(@NotNull OdinDereferenceExpression o) {
        // get type of expression. If it is a pointer, retrieve the dereferenced type and set that as result
        OdinExpression expression = o.getExpression();
        TsOdinType tsOdinType = doInferType(scope, expression);
        if (tsOdinType instanceof TsOdinPointerType pointerType) {
            this.type = pointerType.getDereferencedType();
        }
    }

    @Override
    public void visitUnaryAndExpression(@NotNull OdinUnaryAndExpression o) {
        OdinExpression expression = o.getExpression();
        if (expression != null) {
            TsOdinType referencedType = doInferType(scope, expression);
            // TODO check if reference type is actually referenceable (E.g. meta type and typeid aren't)
            TsOdinPointerType tsOdinPointerType = new TsOdinPointerType();
            tsOdinPointerType.setDereferencedType(referencedType);
            this.type = tsOdinPointerType;
        }
    }

    @Override
    public void visitParenthesizedExpression(@NotNull OdinParenthesizedExpression o) {
        OdinExpression expression = o.getExpression();
        if (expression != null) {
            this.type = doInferType(scope, expression);
        }
    }

    @Override
    public void visitProcedureExpression(@NotNull OdinProcedureExpression o) {
        // get type of expression. If it is a procedure, retrieve the return type and set that as result
        var procedureType = o.getProcedureTypeContainer().getProcedureType();
        TsOdinMetaType tsOdinMetaType = new TsOdinMetaType(TsOdinMetaType.MetaType.PROCEDURE);
        tsOdinMetaType.setType(procedureType);

        this.type = tsOdinMetaType;
    }

    @Override
    public void visitCastExpression(@NotNull OdinCastExpression o) {
        OdinTypeDefinitionExpression typeDefinitionExpression = (OdinTypeDefinitionExpression) o.getTypeDefinitionExpression();
        this.type = OdinTypeResolver.resolveType(scope, typeDefinitionExpression.getType());
    }

    @Override
    public void visitAutoCastExpression(@NotNull OdinAutoCastExpression o) {
        this.type = this.expectedType;
    }

    @Override
    public void visitTransmuteExpression(@NotNull OdinTransmuteExpression o) {
        OdinTypeDefinitionExpression typeDefinitionExpression = (OdinTypeDefinitionExpression) o.getTypeDefinitionExpression();
        this.type = OdinTypeResolver.resolveType(scope, typeDefinitionExpression.getType());
    }

    @Override
    public void visitOrElseExpression(@NotNull OdinOrElseExpression o) {
        if (!o.getExpressionList().isEmpty()) {
            TsOdinType tsOdinType = doInferType(scope, createOptionalOkTuple(expectedType), 2, o.getExpressionList().get(0));
            if(isOptionalOkTuple(tsOdinType)) {
                this.type = ((TsOdinTuple) tsOdinType).getTypes().get(0);
            }
        }
    }

    @Override
    public void visitTypeAssertionExpression(@NotNull OdinTypeAssertionExpression o) {
        TsOdinType tsOdinType = OdinTypeResolver.resolveType(scope, o.getType());
        if (this.lhsValuesCount == 2) {
            this.type = createOptionalOkTuple(tsOdinType);
        } else {
            this.type = tsOdinType;
        }
    }

    private static @NotNull TsOdinTuple createOptionalOkTuple(TsOdinType tsOdinType) {
        return new TsOdinTuple(List.of(tsOdinType, TsOdinBuiltInType.BOOLEAN));
    }

    private static boolean isOptionalOkTuple(TsOdinType tsOdinType) {
        if (tsOdinType instanceof TsOdinTuple tsOdinTuple) {
            return tsOdinTuple.getTypes().size() == 2
                    && (tsOdinTuple.getTypes().get(1) == TsOdinBuiltInType.BOOLEAN || tsOdinTuple.getTypes().get(1).isNillable());
        }
        return false;
    }

    @Override
    public void visitMaybeExpression(@NotNull OdinMaybeExpression o) {
        TsOdinType expectedUnionType = TsOdinType.UNKNOWN;
        if (isOptionalOkTuple(expectedType)) {

            TsOdinTuple tuple = (TsOdinTuple) expectedType;
            expectedUnionType = tuple.get(0);
        }

        OdinExpression expression = o.getExpression();
        TsOdinType tsOdinType = doInferType(scope, expression);
        if (tsOdinType instanceof TsOdinUnionType tsOdinUnionType) {
            if (tsOdinUnionType.getVariants().size() == 1) {
                this.type = createOptionalOkTuple(tsOdinUnionType.getVariants().get(0).getType());
            } else if(tsOdinUnionType.getVariants().size() > 1 && !expectedUnionType.isUnknown()) {
                // Check if expectedType is in union variants
                this.type = createOptionalOkTuple(expectedUnionType);
            }
        }

    }

    public static TsOdinType resolveTypeOfDeclaration(OdinScope parentScope,
                                                      OdinDeclaredIdentifier declaredIdentifier,
                                                      OdinDeclaration odinDeclaration) {
        if (odinDeclaration instanceof OdinVariableDeclarationStatement declarationStatement) {
            var mainType = declarationStatement.getTypeDefinitionExpression().getType();
            //return getDeclaredIdentifierQualifiedType(parentScope, mainType);
            return OdinTypeResolver.resolveType(parentScope, mainType);
        }

        if (odinDeclaration instanceof OdinVariableInitializationStatement initializationStatement) {
            if (initializationStatement.getTypeDefinitionExpression() != null) {
                OdinType mainTypeExpression = initializationStatement.getTypeDefinitionExpression().getType();
                return OdinTypeResolver.resolveType(parentScope, mainTypeExpression);
            }

            int index = initializationStatement.getIdentifierList().getDeclaredIdentifierList().indexOf(declaredIdentifier);
            List<OdinExpression> expressionList = initializationStatement.getExpressionsList().getExpressionList();
            int lhsValuesCount = initializationStatement.getIdentifierList().getDeclaredIdentifierList().size();

            List<TsOdinType> tsOdinTypes = new ArrayList<>();
            for (OdinExpression odinExpression : expressionList) {
                TsOdinType tsOdinType = doInferType(parentScope, lhsValuesCount, odinExpression);
                if (tsOdinType instanceof TsOdinTuple tuple) {
                    tsOdinTypes.addAll(tuple.getTypes());
                } else {
                    tsOdinTypes.add(tsOdinType);
                }
            }

            if (tsOdinTypes.size() > index) {
                return tsOdinTypes.get(index);
            }
            return TsOdinType.UNKNOWN;
        }

        if (odinDeclaration instanceof OdinConstantInitializationStatement initializationStatement) {
            if (initializationStatement.getTypeDefinitionExpression() != null) {
                OdinType mainType = initializationStatement.getTypeDefinitionExpression().getType();
                return OdinTypeResolver.resolveType(parentScope, mainType);
            }
            int index = initializationStatement.getIdentifierList().getDeclaredIdentifierList().indexOf(declaredIdentifier);
            List<OdinExpression> expressionList = initializationStatement.getExpressionsList().getExpressionList();

            List<TsOdinType> tsOdinTypes = new ArrayList<>();
            for (OdinExpression odinExpression : expressionList) {
                TsOdinType tsOdinType = doInferType(parentScope, odinExpression);
                if (tsOdinType instanceof TsOdinTuple tuple) {
                    tsOdinTypes.addAll(tuple.getTypes());
                } else {
                    tsOdinTypes.add(tsOdinType);
                }
            }

            if (tsOdinTypes.size() > index) {
                return tsOdinTypes.get(index);
            }
            return TsOdinType.UNKNOWN;
        }

        if (odinDeclaration instanceof OdinFieldDeclarationStatement fieldDeclarationStatement) {
            OdinType type;
            if (fieldDeclarationStatement.getTypeDefinitionExpression() instanceof OdinType expr) {
                type = expr;
            } else {

                OdinTypeDefinitionExpression typeDefinition = fieldDeclarationStatement.getTypeDefinitionExpression();
                type = typeDefinition.getType();
            }

            return OdinTypeResolver.resolveType(parentScope, type);
        }

        if (odinDeclaration instanceof OdinParameterDecl parameterDeclaration) {
            return OdinTypeResolver.resolveType(parentScope, parameterDeclaration.getTypeDefinition().getType());
        }

        if (odinDeclaration instanceof OdinParameterInitialization parameterInitialization) {
            OdinTypeDefinitionExpression typeDefinition = parameterInitialization.getTypeDefinition();
            if (typeDefinition != null) {
                return OdinTypeResolver.resolveType(parentScope, typeDefinition.getType());
            }

            OdinExpression odinExpression = parameterInitialization.getExpression();
            return doInferType(parentScope, odinExpression);
        }

        // Meta types
        if (odinDeclaration instanceof OdinProcedureDeclarationStatement procedure) {
            TsOdinMetaType tsOdinMetaType = new TsOdinMetaType(TsOdinMetaType.MetaType.PROCEDURE);
            tsOdinMetaType.setDeclaration(procedure);
            tsOdinMetaType.setType(procedure.getProcedureType());
            tsOdinMetaType.setDeclaredIdentifier(declaredIdentifier);
            tsOdinMetaType.setName(declaredIdentifier.getName());
            return tsOdinMetaType;
        }

        if (odinDeclaration instanceof OdinStructDeclarationStatement structDeclarationStatement) {
            TsOdinMetaType tsOdinMetaType = new TsOdinMetaType(TsOdinMetaType.MetaType.STRUCT);
            tsOdinMetaType.setDeclaration(structDeclarationStatement);
            tsOdinMetaType.setType(structDeclarationStatement.getStructType());
            tsOdinMetaType.setDeclaredIdentifier(declaredIdentifier);
            tsOdinMetaType.setName(declaredIdentifier.getName());
            return tsOdinMetaType;
        }

        if (odinDeclaration instanceof OdinEnumDeclarationStatement enumDeclarationStatement) {
            TsOdinMetaType tsOdinMetaType = new TsOdinMetaType(TsOdinMetaType.MetaType.ENUM);
            tsOdinMetaType.setDeclaration(enumDeclarationStatement);
            tsOdinMetaType.setType(enumDeclarationStatement.getEnumType());
            tsOdinMetaType.setDeclaredIdentifier(declaredIdentifier);
            tsOdinMetaType.setName(declaredIdentifier.getName());
            return tsOdinMetaType;
        }

        if (odinDeclaration instanceof OdinUnionDeclarationStatement unionDeclarationStatement) {
            TsOdinMetaType tsOdinMetaType = new TsOdinMetaType(TsOdinMetaType.MetaType.UNION);
            tsOdinMetaType.setDeclaration(unionDeclarationStatement);
            tsOdinMetaType.setType(unionDeclarationStatement.getUnionType());
            tsOdinMetaType.setDeclaredIdentifier(declaredIdentifier);
            tsOdinMetaType.setName(declaredIdentifier.getName());
            return tsOdinMetaType;
        }

        if (odinDeclaration instanceof OdinPolymorphicType polymorphicType) {
            TsOdinMetaType tsOdinMetaType = new TsOdinMetaType(TsOdinMetaType.MetaType.POLYMORPHIC_PARAMETER);
            tsOdinMetaType.setDeclaration(polymorphicType);
            tsOdinMetaType.setType(polymorphicType);
            tsOdinMetaType.setDeclaredIdentifier(declaredIdentifier);
            tsOdinMetaType.setName(declaredIdentifier.getName());
            return tsOdinMetaType;
        }

        return TsOdinType.UNKNOWN;
    }

    // v.?
    // union(T) -> (T, bool)

    // v.? and target of v=T_k known
    // union(T1, T2, T3, ... , T_k, ..., Tn) -> (T_k, bool)

    // v.(T)
    // if not two values (1, or >2) expected
    // union(T0, ..., T, .., Tn) -> T

    // if two values expected
    // union(T0, ..., T, .., Tn) -> (T, bool)

    // cast(T)x, transmute(T)x
    // type(x) -> T

    // x : T := auto_cast y, and for known T
    // T -> type(y)

    // x or_else y
    // ((T, Union(bool)), T) -> T

    // caller() or_return
    // (T1, ..., Tn) -> (T1, ..., Tn-1)
}