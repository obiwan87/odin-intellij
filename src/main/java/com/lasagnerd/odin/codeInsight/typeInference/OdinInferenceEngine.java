package com.lasagnerd.odin.codeInsight.typeInference;

import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.OdinScope;
import com.lasagnerd.odin.codeInsight.OdinScopeResolver;
import com.lasagnerd.odin.codeInsight.OdinSymbol;
import com.lasagnerd.odin.codeInsight.typeSystem.*;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.lasagnerd.odin.codeInsight.typeSystem.TsOdinMetaType.MetaType.*;

public class OdinInferenceEngine extends OdinVisitor {
    // Result fields
    TsOdinType type;
    OdinImportDeclarationStatement importDeclarationStatement;
    boolean isImport;

    // Input fields
    final OdinScope scope;
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

    @NotNull
    public static TsOdinType inferType(OdinScope scope, OdinExpression expression) {
        OdinInferenceEngine odinInferenceEngine = new OdinInferenceEngine(scope);
        expression.accept(odinInferenceEngine);
        if (odinInferenceEngine.isImport) {
            return createPackageReferenceType(scope.getPackagePath(), odinInferenceEngine.importDeclarationStatement);
        } else {
            return odinInferenceEngine.type != null? odinInferenceEngine.type : TsOdinType.UNKNOWN;
        }
    }

    public static TsOdinType doInferType(OdinScope scope, @NotNull OdinExpression expression) {
        return doInferType(scope, TsOdinType.UNKNOWN, 1, expression);
    }

    public static TsOdinType doInferType(OdinScope scope, TsOdinType expectedType, int lhsValuesCount, @NotNull OdinExpression expression) {
        OdinInferenceEngine odinInferenceEngine = new OdinInferenceEngine(scope, expectedType, lhsValuesCount);
        expression.accept(odinInferenceEngine);

        if (odinInferenceEngine.isImport) {
            OdinImportDeclarationStatement importDeclarationStatement = odinInferenceEngine.importDeclarationStatement;
            return createPackageReferenceType(scope.getPackagePath(), importDeclarationStatement);
        }
        TsOdinType type = odinInferenceEngine.type;
        if (type == null) {
            return TsOdinType.UNKNOWN;
        }

        return type;
    }

    private static @NotNull TsOdinPackageReferenceType createPackageReferenceType(String packagePath,
                                                                                  OdinImportDeclarationStatement importDeclarationStatement) {
        TsOdinPackageReferenceType packageType = new TsOdinPackageReferenceType(packagePath);
        packageType.setDeclaration(importDeclarationStatement);
        return packageType;
    }

    public static TsOdinType doInferType(OdinScope scope, int lhsValuesCount, @NotNull OdinExpression expression) {
        return doInferType(scope, TsOdinType.UNKNOWN, lhsValuesCount, expression);
    }

    public static TsOdinType doInferType(OdinExpression odinExpression) {
        OdinScope scope = OdinScopeResolver.resolveScope(odinExpression);
        return doInferType(scope, odinExpression);
    }


    @Override
    public void visitTypeDefinitionExpression(@NotNull OdinTypeDefinitionExpression o) {
        this.type = OdinTypeResolver.findMetaType(scope, o.getType());
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

        if (refExpression.getIdentifier() != null) {
            // using current scope, find identifier declaration and extract type
            String name = refExpression.getIdentifier().getText();
            OdinSymbol symbol = localScope.getSymbol(name);
            if (symbol != null) {
                PsiNamedElement namedElement = symbol.getDeclaredIdentifier();
                if (namedElement instanceof OdinImportDeclarationStatement) {
                    isImport = true;
                    importDeclarationStatement = (OdinImportDeclarationStatement) namedElement;
                } else if (namedElement instanceof OdinDeclaredIdentifier declaredIdentifier) {
                    OdinDeclaration odinDeclaration = PsiTreeUtil.getParentOfType(declaredIdentifier, true, OdinDeclaration.class);

                    if (odinDeclaration instanceof OdinImportDeclarationStatement) {
                        isImport = true;
                        importDeclarationStatement = (OdinImportDeclarationStatement) odinDeclaration;
                    } else {
                        this.type = resolveTypeOfDeclaration(localScope, declaredIdentifier, odinDeclaration);
                    }
                }
            } else {
                if (TsOdinBuiltInType.RESERVED_TYPES.contains(name)) {
                    TsOdinMetaType tsOdinMetaType = new TsOdinMetaType(BUILTIN);
                    tsOdinMetaType.setName(name);
                    this.type = tsOdinMetaType;
                }
            }
        }

        if (refExpression.getType() != null) {
            TsOdinType tsOdinType = OdinTypeResolver.resolveType(scope, refExpression.getType());
            if (this.lhsValuesCount == 2) {
                this.type = createOptionalOkTuple(tsOdinType);
            } else {
                this.type = tsOdinType;
            }
        }
    }

    @Override
    public void visitCompoundLiteralExpression(@NotNull OdinCompoundLiteralExpression o) {
        if (o.getCompoundLiteral() instanceof OdinCompoundLiteralTyped typed) {
            OdinType psiType = typed.getType();
            this.type = OdinTypeResolver.resolveType(this.scope, psiType);
        }
    }

    @Override
    public void visitCallExpression(@NotNull OdinCallExpression o) {
        // Get type of expression. If it is callable, retrieve the return type and set that as result
        TsOdinType tsOdinType = doInferType(scope, o.getExpression());
        if (tsOdinType instanceof TsOdinMetaType tsOdinMetaType) {
            if (tsOdinMetaType.getRepresentedMetaType() == PROCEDURE) {
                TsOdinProcedureType procedureType = (TsOdinProcedureType) OdinTypeResolver.resolveMetaType(tsOdinType.getScope(), tsOdinMetaType);
                if (!procedureType.getReturnTypes().isEmpty()) {
                    TsOdinProcedureType specializedType = OdinTypeSpecializer
                            .specializeProcedure(scope, o.getArgumentList(), procedureType);
                    if (specializedType.getReturnTypes().size() == 1) {
                        this.type = specializedType.getReturnTypes().get(0);
                    } else if (specializedType.getReturnTypes().size() > 1) {
                        this.type = new TsOdinTuple(specializedType.getReturnTypes());
                    } else {
                        this.type = TsOdinType.VOID;
                    }
                }
            }

            if (tsOdinMetaType.getRepresentedMetaType() == STRUCT) {
                TsOdinStructType structType = (TsOdinStructType) OdinTypeResolver.resolveMetaType(scope, tsOdinMetaType);
                TsOdinStructType specializedStructType = OdinTypeSpecializer.specializeStruct(scope, o.getArgumentList(), structType);
                TsOdinMetaType resultType = new TsOdinMetaType(STRUCT);
                resultType.setRepresentedType(specializedStructType);
                this.type = resultType;
            }

            if (tsOdinMetaType.getRepresentedMetaType() == UNION) {
                TsOdinUnionType unionType = (TsOdinUnionType) OdinTypeResolver.resolveMetaType(scope, tsOdinMetaType);
                TsOdinType specializedUnion = OdinTypeSpecializer.specializeUnion(scope, o.getArgumentList(), unionType);
                TsOdinMetaType resultType = new TsOdinMetaType(UNION);
                resultType.setRepresentedType(specializedUnion);
                this.type = resultType;
            }

            // TODO add casting expression T(expr)
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

        if(tsOdinType instanceof TsOdinSliceType sliceType) {
            this.type = sliceType.getElementType();
        }

        if (tsOdinType instanceof TsOdinMapType mapType) {
            this.type = mapType.getValueType();
        }

        if (tsOdinType instanceof TsOdinMatrixType matrixType) {
            this.type = matrixType.getElementType();
        }

        if (tsOdinType instanceof TsOdinMultiPointerType multiPointerType) {
            this.type = multiPointerType.getDereferencedType();
        }
    }

    @Override
    public void visitSliceExpression(@NotNull OdinSliceExpression o) {
        OdinExpression expression = o.getExpression();
        TsOdinType tsOdinType = doInferType(scope, expression);
        if (tsOdinType instanceof TsOdinSliceType) {
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
    public void visitAddressExpression(@NotNull OdinAddressExpression o) {
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
        TsOdinMetaType tsOdinMetaType = new TsOdinMetaType(PROCEDURE);
        tsOdinMetaType.setType(procedureType);

        this.type = tsOdinMetaType;
    }

    @Override
    public void visitCastExpression(@NotNull OdinCastExpression o) {
        this.type = OdinTypeResolver.resolveType(scope, o.getType());
    }

    @Override
    public void visitAutoCastExpression(@NotNull OdinAutoCastExpression o) {
        this.type = this.expectedType;
    }

    @Override
    public void visitTransmuteExpression(@NotNull OdinTransmuteExpression o) {

        this.type = OdinTypeResolver.resolveType(scope, o.getType());
    }

    @Override
    public void visitOrElseExpression(@NotNull OdinOrElseExpression o) {
        if (!o.getExpressionList().isEmpty()) {
            TsOdinType tsOdinType = doInferType(scope, createOptionalOkTuple(expectedType), 2, o.getExpressionList().get(0));
            if (isOptionalOkTuple(tsOdinType)) {
                this.type = ((TsOdinTuple) tsOdinType).getTypes().get(0);
            }
        }
    }

    @Override
    public void visitElvisExpression(@NotNull OdinElvisExpression o) {
        if (o.getExpressionList().size() == 3) {
            OdinExpression trueBranchExpression = o.getExpressionList().get(1);
            OdinExpression falseBranchExpression = o.getExpressionList().get(2);

            this.type = evaluateConditionalBranchesType(trueBranchExpression, falseBranchExpression);
        }
    }

    @Override
    public void visitTernaryIfExpression(@NotNull OdinTernaryIfExpression o) {
        if (o.getExpressionList().size() == 3) {
            OdinExpression trueBranchExpression = o.getExpressionList().get(0);
            OdinExpression falseBranchExpression = o.getExpressionList().get(2);

            this.type = evaluateConditionalBranchesType(trueBranchExpression, falseBranchExpression);
        }
    }

    @Override
    public void visitTernaryWhenExpression(@NotNull OdinTernaryWhenExpression o) {
        if (o.getExpressionList().size() == 3) {
            OdinExpression trueBranchExpression = o.getExpressionList().get(0);
            OdinExpression falseBranchExpression = o.getExpressionList().get(2);

            this.type = evaluateConditionalBranchesType(trueBranchExpression, falseBranchExpression);
        }
    }

    private @Nullable TsOdinType evaluateConditionalBranchesType(OdinExpression trueBranchExpression, OdinExpression falseBranchExpression) {
        TsOdinType tsOdinTrueType = doInferType(scope, trueBranchExpression);
        TsOdinType tsOdinFalseType = doInferType(scope, falseBranchExpression);

        if (TsOdinUtils.areEqual(tsOdinTrueType, tsOdinFalseType)) {
            return tsOdinTrueType;
        }
        return TsOdinType.UNKNOWN;
    }

    @Override
    public void visitLiteralExpression(@NotNull OdinLiteralExpression o) {
        o.getBasicLiteral().accept(this);
    }

    @Override
    public void visitNumericLiteral(@NotNull OdinNumericLiteral o) {
        if (o.getFloatDecLiteral() != null) {
            this.type = TsOdinBuiltInType.F64.asUntyped();
        } else if (o.getIntegerBinLiteral() != null
                || o.getIntegerHexLiteral() != null
                || o.getIntegerOctLiteral() != null
                || o.getIntegerDecLiteral() != null) {
            this.type = TsOdinBuiltInType.INT.asUntyped();
        } else if (o.getComplexFloatLiteral() != null || o.getComplexIntegerDecLiteral() != null) {
            this.type = TsOdinBuiltInType.COMPLEX128.asUntyped();
        } else if (o.getQuatFloatLiteral() != null || o.getQuatIntegerDecLiteral() != null) {
            this.type = TsOdinBuiltInType.QUATERNION256.asUntyped();
        }
        super.visitNumericLiteral(o);
    }

    @Override
    public void visitStringLiteral(@NotNull OdinStringLiteral o) {
        if (o.getSqStringLiteral() != null) {
            this.type = TsOdinBuiltInType.RUNE.asUntyped();
        }

        if (o.getDqStringLiteral() != null || o.getRawStringLiteral() != null) {
            this.type = TsOdinBuiltInType.STRING.asUntyped();
        }
    }

    @Override
    public void visitRangeInclusiveExpression(@NotNull OdinRangeInclusiveExpression o) {
        OdinExpression odinExpression = o.getExpressionList().get(0);
        this.type = doInferType(scope, odinExpression);
    }

    @Override
    public void visitRangeExclusiveExpression(@NotNull OdinRangeExclusiveExpression o) {
        OdinExpression odinExpression = o.getExpressionList().get(0);
        this.type = doInferType(scope, odinExpression);
    }

    @Override
    public void visitAndExpression(@NotNull OdinAndExpression o) {
        this.type = TsOdinBuiltInType.BOOL;
    }

    @Override
    public void visitOrExpression(@NotNull OdinOrExpression o) {
        this.type = TsOdinBuiltInType.BOOL;
    }

    @Override
    public void visitGteExpression(@NotNull OdinGteExpression o) {
        this.type = TsOdinBuiltInType.BOOL;
    }

    @Override
    public void visitGtExpression(@NotNull OdinGtExpression o) {
        this.type = TsOdinBuiltInType.BOOL;
    }

    @Override
    public void visitLtExpression(@NotNull OdinLtExpression o) {
        this.type = TsOdinBuiltInType.BOOL;
    }

    @Override
    public void visitLteExpression(@NotNull OdinLteExpression o) {
        this.type = TsOdinBuiltInType.BOOL;
    }

    @Override
    public void visitEqeqExpression(@NotNull OdinEqeqExpression o) {
        this.type = TsOdinBuiltInType.BOOL;
    }

    @Override
    public void visitNeqExpression(@NotNull OdinNeqExpression o) {
        this.type = TsOdinBuiltInType.BOOL;
    }

    @Override
    public void visitInExpression(@NotNull OdinInExpression o) {
        this.type = TsOdinBuiltInType.BOOL;
    }

    @Override
    public void visitNotInExpression(@NotNull OdinNotInExpression o) {
        this.type = TsOdinBuiltInType.BOOL;
    }
/*
Require knowledge about type conversion:
mulExpression
divExpression
modExpression
remainderExpression
addExpression
subExpression
 */

    private static @NotNull TsOdinTuple createOptionalOkTuple(TsOdinType tsOdinType) {
        return new TsOdinTuple(List.of(tsOdinType, TsOdinBuiltInType.BOOL));
    }

    private static boolean isOptionalOkTuple(TsOdinType tsOdinType) {
        if (tsOdinType instanceof TsOdinTuple tsOdinTuple) {
            return tsOdinTuple.getTypes().size() == 2
                    && (tsOdinTuple.getTypes().get(1) == TsOdinBuiltInType.BOOL || tsOdinTuple.getTypes().get(1).isNillable());
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
            } else if (tsOdinUnionType.getVariants().size() > 1 && !expectedUnionType.isUnknown()) {
                // Check if expectedType is in union variants
                this.type = createOptionalOkTuple(expectedUnionType);
            }
        }

    }

    public static TsOdinType resolveTypeOfDeclaration(OdinScope parentScope,
                                                      OdinDeclaredIdentifier declaredIdentifier,
                                                      OdinDeclaration odinDeclaration) {
        if (odinDeclaration instanceof OdinVariableDeclarationStatement declarationStatement) {
            var mainType = declarationStatement.getType();
            //return getDeclaredIdentifierQualifiedType(parentScope, mainType);
            return OdinTypeResolver.resolveType(parentScope, mainType);
        }

        if (odinDeclaration instanceof OdinVariableInitializationStatement initializationStatement) {
            if (initializationStatement.getType() != null) {
                return OdinTypeResolver.resolveType(parentScope, initializationStatement.getType());
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
            if (initializationStatement.getType() != null) {
                OdinType mainType = initializationStatement.getType();
                return OdinTypeResolver.resolveType(parentScope, mainType);
            }
            int index = initializationStatement.getIdentifierList()
                    .getDeclaredIdentifierList()
                    .indexOf(declaredIdentifier);

            List<OdinExpression> expressionList = initializationStatement
                    .getExpressionsList()
                    .getExpressionList();

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
            return OdinTypeResolver.resolveType(parentScope, fieldDeclarationStatement.getType());
        }

        if (odinDeclaration instanceof OdinParameterDeclarator parameterDeclaration) {
            return OdinTypeResolver.resolveType(parentScope, parameterDeclaration.getTypeDefinitionContainer().getType());
        }

        if (odinDeclaration instanceof OdinParameterInitialization parameterInitialization) {
            OdinType type = parameterInitialization.getTypeDefinition();
            if (type != null) {
                return OdinTypeResolver.resolveType(parentScope, type);
            }

            OdinExpression odinExpression = parameterInitialization.getExpression();
            return doInferType(parentScope, odinExpression);
        }

        // Meta types
        if (odinDeclaration instanceof OdinProcedureDeclarationStatement procedure) {
            TsOdinMetaType tsOdinMetaType = new TsOdinMetaType(PROCEDURE);
            tsOdinMetaType.setScope(parentScope);
            tsOdinMetaType.setDeclaration(procedure);
            tsOdinMetaType.setType(procedure.getProcedureDefinition().getProcedureType());
            tsOdinMetaType.setDeclaredIdentifier(declaredIdentifier);
            tsOdinMetaType.setName(declaredIdentifier.getName());
            return tsOdinMetaType;
        }

        if (odinDeclaration instanceof OdinStructDeclarationStatement structDeclarationStatement) {
            TsOdinMetaType tsOdinMetaType = new TsOdinMetaType(STRUCT);
            tsOdinMetaType.setScope(parentScope);
            tsOdinMetaType.setDeclaration(structDeclarationStatement);
            tsOdinMetaType.setType(structDeclarationStatement.getStructType());
            tsOdinMetaType.setDeclaredIdentifier(declaredIdentifier);
            tsOdinMetaType.setName(declaredIdentifier.getName());
            return tsOdinMetaType;
        }

        if (odinDeclaration instanceof OdinEnumDeclarationStatement enumDeclarationStatement) {
            TsOdinMetaType tsOdinMetaType = new TsOdinMetaType(ENUM);
            tsOdinMetaType.setScope(parentScope);
            tsOdinMetaType.setDeclaration(enumDeclarationStatement);
            tsOdinMetaType.setType(enumDeclarationStatement.getEnumType());
            tsOdinMetaType.setDeclaredIdentifier(declaredIdentifier);
            tsOdinMetaType.setName(declaredIdentifier.getName());
            return tsOdinMetaType;
        }

        if (odinDeclaration instanceof OdinUnionDeclarationStatement unionDeclarationStatement) {
            TsOdinMetaType tsOdinMetaType = new TsOdinMetaType(UNION);
            tsOdinMetaType.setScope(parentScope);
            tsOdinMetaType.setDeclaration(unionDeclarationStatement);
            tsOdinMetaType.setType(unionDeclarationStatement.getUnionType());
            tsOdinMetaType.setDeclaredIdentifier(declaredIdentifier);
            tsOdinMetaType.setName(declaredIdentifier.getName());
            return tsOdinMetaType;
        }

        if (odinDeclaration instanceof OdinPolymorphicType polymorphicType) {
            TsOdinMetaType tsOdinMetaType = new TsOdinMetaType(POLYMORPHIC);
            tsOdinMetaType.setScope(parentScope);
            tsOdinMetaType.setDeclaration(polymorphicType);
            tsOdinMetaType.setType(polymorphicType);
            tsOdinMetaType.setDeclaredIdentifier(declaredIdentifier);
            tsOdinMetaType.setName(declaredIdentifier.getName());
            return tsOdinMetaType;
        }

        if (odinDeclaration instanceof OdinEnumValueDeclaration odinEnumValueDeclaration) {
            OdinEnumType enumType = PsiTreeUtil.getParentOfType(odinEnumValueDeclaration, true, OdinEnumType.class);
            OdinEnumDeclarationStatement enumDeclarationStatement = PsiTreeUtil.getParentOfType(enumType, true, OdinEnumDeclarationStatement.class);
            OdinDeclaredIdentifier enumDeclaredIdentifier = null;
            if (enumDeclarationStatement != null) {
                enumDeclaredIdentifier = enumDeclarationStatement.getDeclaredIdentifier();
            }
            if (enumType != null) {
                return OdinTypeResolver.resolveType(parentScope, enumDeclaredIdentifier, enumDeclarationStatement, enumType);
            }
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