package com.lasagnerd.odin.codeInsight.typeInference;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.lasagnerd.odin.codeInsight.OdinContext;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.OdinSymbolTable;
import com.lasagnerd.odin.codeInsight.imports.OdinImportService;
import com.lasagnerd.odin.codeInsight.sdk.OdinSdkService;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbol;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolType;
import com.lasagnerd.odin.codeInsight.typeSystem.*;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.lasagnerd.odin.codeInsight.typeSystem.TsOdinMetaType.MetaType.*;

public class OdinInferenceEngine extends OdinVisitor {
    // Result fields
    TsOdinType type;

    // Input fields
    final OdinContext context;
    private final TsOdinType expectedType;
    private final int lhsValuesCount;
    private final boolean explicitMode;

    private OdinInferenceEngine(OdinContext context,
                                TsOdinType expectedType,
                                int lhsValuesCount,
                                boolean explicitMode) {
        this.context = context;
        this.expectedType = expectedType;
        this.lhsValuesCount = lhsValuesCount;
        this.explicitMode = explicitMode;
    }


    private OdinInferenceEngine(OdinContext context, boolean explicitMode) {
        this.context = context;
        this.explicitMode = explicitMode;
        this.expectedType = null;
        this.lhsValuesCount = 1;
    }

    @NotNull
    public static TsOdinType inferTypeInExplicitMode(OdinContext context, OdinExpression expression) {
        OdinInferenceEngine odinInferenceEngine = new OdinInferenceEngine(context, true);
        expression.accept(odinInferenceEngine);

        return odinInferenceEngine.type != null ? odinInferenceEngine.type : TsOdinBuiltInTypes.UNKNOWN;
    }

    public static TsOdinType inferType(OdinInferenceEngineParameters parameters, OdinExpression expression) {
        return inferType(parameters.context(),
                parameters.expectedType(),
                parameters.lhsValuesCount(),
                parameters.explicitMode(),
                expression,
                true);
    }

    public static PsiElement findNextBinaryExpression(PsiElement element, boolean strict) {
        return OdinInsightUtils.findParentOfType(
                element,
                strict,
                new Class<?>[]{
                        OdinBinaryExpression.class
                },
                new Class<?>[]{
                        OdinLhsExpressions.class,
                        OdinIndex.class,
                        OdinLhs.class,
                        OdinReturnStatement.class,
                        OdinRhs.class,
                        OdinLhs.class,
                        OdinArgument.class,
                        OdinRhsExpressions.class,
                        OdinCaseClause.class,
                        OdinVariableInitializationStatement.class,
                        OdinConstantInitializationStatement.class,
                        OdinParameterInitialization.class,
                        OdinCaseClause.class,
                }
        );
    }


    public static TsOdinType inferType(OdinContext context,
                                       TsOdinType expectedType,
                                       int lhsValuesCount,
                                       boolean explicitMode,
                                       @NotNull OdinExpression expression,
                                       boolean calledFromCache) {
        if (!calledFromCache) {
            throw new IllegalCallerException("Not called from cache");
        }

        context = OdinTypeResolver.initializeContext(context, expression);

        OdinInferenceEngine odinInferenceEngine = new OdinInferenceEngine(context, expectedType, lhsValuesCount, explicitMode);
        expression.accept(odinInferenceEngine);
        TsOdinType type = odinInferenceEngine.type;
        if (type == null) {
            return TsOdinBuiltInTypes.UNKNOWN;
        }

        return type;
    }

    private static @NotNull TsOdinPackageReferenceType createPackageReferenceType(String packagePath,
                                                                                  OdinImportDeclarationStatement importDeclarationStatement) {
        TsOdinPackageReferenceType packageType = new TsOdinPackageReferenceType(packagePath);
        packageType.setDeclaration(importDeclarationStatement);
        return packageType;
    }

    public static TsOdinType inferTypeOfCompoundLiteral(OdinContext context, OdinCompoundLiteral compoundLiteral) {
        return inferTypeOfCompoundLiteral((OdinCompoundLiteralExpression) compoundLiteral.getParent(),
                context,
                compoundLiteral,
                false);
    }

    public static TsOdinType inferTypeOfCompoundLiteral(
            OdinCompoundLiteralExpression compoundLiteralExpression,
            OdinContext context,
            OdinCompoundLiteral compoundLiteral,
            boolean explicitMode) {
        if (explicitMode && compoundLiteral instanceof OdinCompoundLiteralUntyped)
            return TsOdinBuiltInTypes.UNDECIDED;

        PsiElement nextBinaryExpression = findNextBinaryExpression(compoundLiteralExpression, true);

        TsOdinType tsOdinType;

        if (compoundLiteral instanceof OdinCompoundLiteralTyped compoundLiteralTyped) {
            tsOdinType = compoundLiteralTyped.getTypeContainer().getType().getResolvedType(context);
        } else if (compoundLiteral instanceof OdinCompoundLiteralUntyped) {
            if (nextBinaryExpression instanceof OdinBinaryExpression binaryExpression) {
                // TODO why left?
                OdinExpression otherExpression = getOtherExpression(compoundLiteralExpression, binaryExpression);
                tsOdinType = inferTypeOfUntypedCompoundLiteralBinaryOperand(compoundLiteralExpression,
                        binaryExpression,
                        otherExpression,
                        context);
            } else {
                tsOdinType = OdinExpectedTypeEngine.inferExpectedType(context, compoundLiteralExpression);
            }
        } else {
            tsOdinType = TsOdinBuiltInTypes.UNKNOWN;
        }
        return tsOdinType;
    }

    static boolean isExplicitPolymorphicParameter(TsOdinType tsOdinType, PsiNamedElement identifier) {
        if (identifier instanceof OdinDeclaredIdentifier declaredIdentifier && declaredIdentifier.getDollar() != null) {
            if (tsOdinType.isTypeId())
                return true;

            if (tsOdinType instanceof TsOdinConstrainedType constrainedType) {
                return constrainedType.getMainType().isTypeId();
            }
        }
        return false;
    }


    TsOdinType doInferType(OdinExpression expression) {
        OdinInferenceEngineParameters inferenceEngineParameters = new OdinInferenceEngineParameters(
                new OdinContext(), null, 1, explicitMode
        );
        return expression.getInferredType(inferenceEngineParameters);
    }

    @Override
    public void visitUnaryExpression(@NotNull OdinUnaryExpression o) {
        PsiElement operator = OdinPsiUtil.getOperator(o);
        if (operator != null) {
            this.type = doInferType(o.getExpression());
        }
    }

    @Override
    public void visitBinaryExpression(@NotNull OdinBinaryExpression o) {
        TsOdinType leftType = inferTypeInExplicitMode(this.context, o.getLeft());
        TsOdinType rightType;
        if (o.getRight() instanceof OdinCompoundLiteralExpression literalExpression
                && literalExpression.getCompoundLiteral() instanceof OdinCompoundLiteralUntyped) {
            rightType = leftType;
        } else {
            rightType = inferTypeInExplicitMode(this.context, Objects.requireNonNull(o.getRight()));
        }

        if (!explicitMode) {
            if (leftType.isUndecided() && rightType.isUndecided()) {
                this.type = OdinExpectedTypeEngine.inferExpectedType(context, o);
                return;
            }

            if (leftType.isUndecided()) {
                this.type = TsOdinBuiltInTypes.UNKNOWN;
                return;
            }

            if (rightType.isUndecided()) {
                this.type = leftType;
                return;
            }
        }


        PsiElement operator = o.getOperator();
        if (operator != null) {
            this.type = OdinTypeConverter.inferTypeOfSymmetricalBinaryExpression(leftType, rightType);
        }
    }

    @Override
    public void visitTypeDefinitionExpression(@NotNull OdinTypeDefinitionExpression o) {
        this.type = OdinTypeResolver.findMetaType(context, o, o.getType());
    }

    @Override
    public void visitDirectiveExpression(@NotNull OdinDirectiveExpression o) {
        PsiElement identifierToken = o.getDirectiveIdentifier().getIdentifierToken();
        if (identifierToken.getText().equals("caller_location")) {
            OdinSdkService builtinSymbolService = OdinSdkService.getInstance(o.getProject());
            if (builtinSymbolService != null) {
                OdinSymbolTable symbolTable = builtinSymbolService.getRuntimeSymbolsTable();
                OdinSymbol symbol = symbolTable.getSymbol("Source_Code_Location");
                if (symbol != null) {
                    OdinDeclaration declaration = symbol.getDeclaration();
                    if (declaration instanceof OdinConstantInitializationStatement structDeclarationStatement) {
                        OdinStructType structType = PsiTreeUtil.findChildOfType(structDeclarationStatement, OdinStructType.class);
                        if (structType != null) {
                            this.type = structType.getResolvedType(this.context);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void visitImplicitSelectorExpression(@NotNull OdinImplicitSelectorExpression o) {
        if (explicitMode) {
            this.type = TsOdinBuiltInTypes.UNDECIDED;
            return;
        }
        this.type = inferTypeOfImplicitSelectorExpression(o, context);
    }

    private TsOdinType inferTypeOfImplicitSelectorExpression(@NotNull OdinImplicitSelectorExpression o, OdinContext context1) {
        String enumValue = o.getIdentifier().getText();

        // TODO This code is problematic, it doesn't account for ternary expressions
        //  parentheses. It will cause problems in all branches. We need to propagate down
        //  the type

        // This code belongs to propagate down
        PsiElement context = findNextBinaryExpression(o, false);
        TsOdinType type;
        OdinExpression expression = null;

        if (context instanceof OdinBinaryExpression binaryExpression) {
            OdinExpression otherExpression = getOtherExpression(o, binaryExpression);
            type = inferTypeOfImplicitSelectorBinaryOperand(o, binaryExpression, otherExpression, enumValue);
            expression = binaryExpression;
        } else {
            TsOdinType expectedType = OdinExpectedTypeEngine.inferExpectedType(context1, o);
            type = findEnumValue(expectedType, enumValue);
        }

        if (!type.isUnknown()) {
            if (expression != null) {
                return OdinExpectedTypeEngine.propagateTypeDown(type, expression, o);
            }
            return type;
        }
        return TsOdinBuiltInTypes.UNKNOWN;
    }

    private static @Nullable OdinExpression getOtherExpression(@NotNull PsiElement element, OdinBinaryExpression binaryExpression) {
        PsiElement prevParent = PsiTreeUtil.findPrevParent(binaryExpression, element);
        if (prevParent instanceof OdinExpression expression) {
            return binaryExpression.getLeft() == expression ? binaryExpression.getRight() : binaryExpression.getLeft();
        }
        return null;
    }

    private TsOdinType inferTypeOfImplicitSelectorBinaryOperand(OdinImplicitSelectorExpression implicitSelectorExpression,
                                                                OdinBinaryExpression binaryExpression,
                                                                OdinExpression operandExpression,
                                                                String enumValue) {
        if (operandExpression != null) {
            TsOdinType tsOdinType = inferTypeInExplicitMode(context, operandExpression);
            if (tsOdinType.isUndecided()) {
                PsiElement nextContext = findNextBinaryExpression(binaryExpression, true);
                if (nextContext instanceof OdinBinaryExpression nextBinaryExpression) {
                    OdinExpression otherExpression = getOtherExpression(implicitSelectorExpression, nextBinaryExpression);
                    return inferTypeOfImplicitSelectorBinaryOperand(implicitSelectorExpression, nextBinaryExpression, otherExpression, enumValue);
                } else {
                    return OdinExpectedTypeEngine.inferExpectedType(context, binaryExpression);
                }
            }

            IElementType operatorType = PsiUtilCore.getElementType(binaryExpression.getOperator());
            if (operatorType == OdinTypes.OR_ELSE) {
                if (isOptionalOkTuple(tsOdinType)) {
                    TsOdinTuple tsOdinTuple = (TsOdinTuple) tsOdinType;
                    return findEnumValue(tsOdinTuple.get(0), enumValue);
                }

                if (tsOdinType instanceof TsOdinEnumType) {
                    return findEnumValue(tsOdinType, enumValue);
                }
            }
            if (OdinPsiUtil.COMPARISON_OPERATORS.contains(operatorType)
                    || OdinPsiUtil.ENUM_ARITHMETIC_OPERATORS.contains(operatorType)
                    || OdinPsiUtil.ENUM_BITWISE_OPERATORS.contains(operatorType)) {
                return findEnumValue(tsOdinType, enumValue);
            }

            if (operatorType == OdinTypes.IN || operatorType == OdinTypes.NOT_IN) {
                if (tsOdinType.baseType(true) instanceof TsOdinBitSetType tsOdinBitSetType) {
                    if (tsOdinBitSetType.getElementType() instanceof TsOdinEnumType tsOdinEnumType) {
                        if (enumContainsValue(tsOdinEnumType, enumValue)) {
                            return tsOdinEnumType;
                        }
                    }
                }
            }


            if (operatorType == OdinTypes.RANGE_INCLUSIVE || operatorType == OdinTypes.RANGE_EXCLUSIVE) {
                TsOdinType caseClauseType = OdinExpectedTypeEngine.inferExpectedType(context, binaryExpression);
                if (caseClauseType.baseType(true) instanceof TsOdinEnumType tsOdinEnumType) {
                    if (enumContainsValue(tsOdinEnumType, enumValue)) {
                        return tsOdinEnumType;
                    }
                }
            }
        }
        return TsOdinBuiltInTypes.UNKNOWN;
    }

    private static TsOdinType inferTypeOfUntypedCompoundLiteralBinaryOperand(OdinCompoundLiteralExpression compoundLiteralExpression,
                                                                             OdinBinaryExpression binaryExpression,
                                                                             OdinExpression operandExpression,
                                                                             OdinContext context) {
        if (operandExpression != null) {
            TsOdinType tsOdinType = inferTypeInExplicitMode(context, operandExpression);
            if (tsOdinType.isUndecided()) {
                PsiElement nextContext = findNextBinaryExpression(binaryExpression, true);
                if (nextContext instanceof OdinBinaryExpression nextBinaryExpression) {
                    OdinExpression otherExpression = getOtherExpression(compoundLiteralExpression, nextBinaryExpression);
                    return inferTypeOfUntypedCompoundLiteralBinaryOperand(compoundLiteralExpression, nextBinaryExpression, otherExpression, context);
                } else {
                    return OdinExpectedTypeEngine.inferExpectedType(context, binaryExpression);
                }
            }

            IElementType operatorType = PsiUtilCore.getElementType(binaryExpression.getOperator());
            if (OdinPsiUtil.SET_OPERATORS.contains(operatorType)
                    || OdinPsiUtil.COMPARISON_OPERATORS.contains(operatorType)) {
                return tsOdinType;
            }

        }
        return TsOdinBuiltInTypes.UNKNOWN;
    }

    private static @NotNull TsOdinType findEnumValue(TsOdinType tsOdinType, String enumValue) {
        TsOdinType implicitSelectorType = null;
        if (tsOdinType.baseType(true) instanceof TsOdinEnumType tsOdinEnumType) {
            if (enumContainsValue(tsOdinEnumType, enumValue)) {
                implicitSelectorType = tsOdinEnumType;
            }
        } else if (tsOdinType.baseType(true) instanceof TsOdinUnionType unionType) {
            for (TsOdinUnionVariant variant : unionType.getVariants()) {
                TsOdinType variantType = variant.getType();
                if (variantType.baseType(true) instanceof TsOdinEnumType tsOdinEnumType) {
                    if (enumContainsValue(tsOdinEnumType, enumValue)) {
                        implicitSelectorType = tsOdinEnumType;
                        break;
                    }
                }
            }
        }

        if (implicitSelectorType == null) {
            implicitSelectorType = TsOdinBuiltInTypes.UNKNOWN;
        }
        return implicitSelectorType;
    }

    public static boolean enumContainsValue(TsOdinEnumType tsOdinEnumType, String enumValue) {
        List<OdinSymbol> typeElements = OdinInsightUtils.getTypeElements(new OdinContext(), tsOdinEnumType);
        return typeElements.stream().anyMatch(s -> s.getName().equals(enumValue));
    }

    @Override
    public void visitRefExpression(@NotNull OdinRefExpression refExpression) {
        TsOdinType tsOdinRefExpressionType = TsOdinBuiltInTypes.UNKNOWN;
        Project project = refExpression.getProject();
        if (refExpression.getExpression() != null) {
            // solve for expression first. This defines the scope
            TsOdinType refExpressionType = doInferType(refExpression.getExpression());
            tsOdinRefExpressionType = OdinInsightUtils.getReferenceableType(refExpressionType);
        }


        OdinIdentifier identifier = refExpression.getIdentifier();
        if (identifier != null) {
            // using current scope, find identifier declaration and extract type

            String name = identifier.getText();
            // If this is an identifier in a ref expression like ref.thisIdentifier
            // getReferencedSymbol() will get the inferred type of 'ref' from the cache
            // because it has been computed above. Then ,in OdinReference, the correct
            // using the type elements of that type, it will correctly retrieve the symbol
            OdinSymbol symbol = identifier.getReferencedSymbol(context);
            if (symbol != null) {
                this.type = getSymbolType(
                        context,
                        project,
                        symbol,
                        tsOdinRefExpressionType,
                        identifier
                );
            } else {
                // TODO Add poly paras as symbols
                TsOdinType polyParameter = context.getPolymorphicType(name);
                if (polyParameter != null) {
                    this.type = createPolymorphicMetaType(polyParameter);
                } else if (TsOdinBuiltInTypes.RESERVED_TYPES.contains(name)) {
                    this.type = createBuiltinMetaType(name);
                }
            }
        }

        // Type conversion
        if (refExpression.getType() != null) {
            TsOdinType tsOdinType = refExpression.getType().getResolvedType(context);
            if (this.lhsValuesCount == 2) {
                this.type = createOptionalOkTuple(tsOdinType);
            } else {
                this.type = tsOdinType;
            }
        }
    }

    private static @NotNull TsOdinMetaType createPolymorphicMetaType(TsOdinType polyParameter) {
        TsOdinMetaType tsOdinMetaType = new TsOdinMetaType(POLYMORPHIC);
        tsOdinMetaType.setDeclaration(polyParameter.getDeclaration());
        tsOdinMetaType.setPsiType(polyParameter.getPsiType());
        tsOdinMetaType.setDeclaredIdentifier(polyParameter.getDeclaredIdentifier());
        tsOdinMetaType.setName(polyParameter.getName());
        tsOdinMetaType.setRepresentedType(polyParameter);
        return tsOdinMetaType;
    }

    public static @NotNull TsOdinType getSymbolType(
            OdinContext context,
            @NotNull Project project,
            OdinSymbol symbol,
            @Nullable TsOdinType tsOdinRefExpressionType,
            PsiElement position) {

        // Check for specialized types
        if (tsOdinRefExpressionType != null && tsOdinRefExpressionType.baseType(true) instanceof TsOdinStructType tsOdinStructType) {
            if (tsOdinStructType.getFields().containsKey(symbol.getName())) {
                return tsOdinStructType.getFields().get(symbol.getName());
            }
        }

        // Implicitly declared symbols, like swizzle fields
        if (symbol.isImplicitlyDeclared()) {
            if (symbol.getSymbolType() == OdinSymbolType.SWIZZLE_FIELD) {
                int swizzleArraySize = symbol.getName().length();
                if (tsOdinRefExpressionType instanceof TsOdinArrayType tsOdinArrayType) {
                    if (swizzleArraySize == 1) {
                        return tsOdinArrayType.getElementType();
                    } else {
                        TsOdinArrayType swizzleArray = new TsOdinArrayType();
                        swizzleArray.setContext(tsOdinArrayType.getContext());
                        swizzleArray.setElementType(tsOdinArrayType.getElementType());
                        swizzleArray.setSize(swizzleArraySize);
                        return swizzleArray;
                    }
                } else if (swizzleArraySize == 1) {
                    if (tsOdinRefExpressionType == TsOdinBuiltInTypes.COMPLEX32) {
                        return TsOdinBuiltInTypes.F16;
                    } else if (tsOdinRefExpressionType == TsOdinBuiltInTypes.COMPLEX64) {
                        return TsOdinBuiltInTypes.F32;
                    } else if (tsOdinRefExpressionType == TsOdinBuiltInTypes.COMPLEX128) {
                        return TsOdinBuiltInTypes.F64;
                    } else if (tsOdinRefExpressionType == TsOdinBuiltInTypes.QUATERNION64) {
                        return TsOdinBuiltInTypes.F16;
                    } else if (tsOdinRefExpressionType == TsOdinBuiltInTypes.QUATERNION128) {
                        return TsOdinBuiltInTypes.F32;
                    } else if (tsOdinRefExpressionType == TsOdinBuiltInTypes.QUATERNION256) {
                        return TsOdinBuiltInTypes.F64;
                    }
                }
                // SOA fields
            } else if (symbol.getSymbolType() == OdinSymbolType.SOA_FIELD) {
                if (tsOdinRefExpressionType instanceof TsOdinSoaStructType soaStructType) {
                    return soaStructType.getFields().get(symbol.getName());
                }
            }
            // Built-in symbols
            else if (symbol.getSymbolType() == OdinSymbolType.BUILTIN_TYPE) {
                return createBuiltinMetaType(symbol.getName());
            } else {
                OdinSdkService builtinSymbolService = OdinSdkService.getInstance(project);
                if (symbol.getPsiType() != null && builtinSymbolService != null) {
                    String typeName = OdinInsightUtils.getTypeName(symbol.getPsiType());
                    return builtinSymbolService.getType(typeName);
                }
            }
        } else {

            TsOdinType tsOdinType = resolveTypeOfNamedElement(symbol.getDeclaredIdentifier(), context);
            OdinDeclaration declaration = symbol.getDeclaration();
            if (declaration instanceof OdinSwitchTypeVariableDeclaration switchTypeVariableDeclaration) {
                OdinSwitchBlock switchInBlock = PsiTreeUtil.getParentOfType(switchTypeVariableDeclaration,
                        OdinSwitchBlock.class,
                        true);
                if (switchInBlock != null) {
                    return getSwitchInReferenceType(tsOdinType, position, switchInBlock);
                }
            }

            if (isExplicitPolymorphicParameter(tsOdinType, symbol.getDeclaredIdentifier())) {
                return createPolymorphicMetaType(
                        createExplicitPolymorphicType(
                                (OdinDeclaredIdentifier) symbol.getDeclaredIdentifier(),
                                symbol.getDeclaration()
                        )
                );
            }
            return tsOdinType;
        }
        return TsOdinBuiltInTypes.UNKNOWN;
    }

    private static @NotNull TsOdinType resolveTypeOfNamedElement(PsiNamedElement namedElement, OdinContext context) {
        OdinImportDeclarationStatement importDeclarationStatement = getImportDeclarationStatement(namedElement);
        if (importDeclarationStatement != null) {
            return createPackageReferenceType(OdinImportService
                    .getInstance(namedElement.getProject())
                    .getPackagePath(importDeclarationStatement), importDeclarationStatement);
        } else if (namedElement instanceof OdinDeclaredIdentifier declaredIdentifier) {
            TsOdinType tsOdinType = doResolveTypeOfDeclaredIdentifier(declaredIdentifier, context);
            OdinDeclaration declaration = PsiTreeUtil.getParentOfType(declaredIdentifier, OdinDeclaration.class);
            if (!(declaration instanceof OdinConstantInitializationStatement)) {
                return OdinTypeConverter.convertToTyped(tsOdinType);
            }
            return tsOdinType;
        }
        return TsOdinBuiltInTypes.UNKNOWN;
    }

    private static OdinImportDeclarationStatement getImportDeclarationStatement(PsiNamedElement namedElement) {
        if (namedElement instanceof OdinImportDeclarationStatement importDeclarationStatement) {
            return importDeclarationStatement;
        }
        if (namedElement instanceof OdinDeclaredIdentifier) {
            OdinDeclaration odinDeclaration = PsiTreeUtil.getParentOfType(namedElement, true, OdinDeclaration.class);
            if (odinDeclaration instanceof OdinImportDeclarationStatement importDeclarationStatement) {
                return importDeclarationStatement;
            }
        }
        return null;
    }

    public static @NotNull TsOdinMetaType createBuiltinMetaType(String name) {
        TsOdinBuiltInType builtInType = TsOdinBuiltInTypes.getBuiltInType(name);
        TsOdinMetaType tsOdinMetaType = new TsOdinMetaType(builtInType.getMetaType());
        tsOdinMetaType.setName(name);
        tsOdinMetaType.setRepresentedType(TsOdinBuiltInTypes.getBuiltInType(name));
        return tsOdinMetaType;
    }

    @Override
    public void visitCompoundLiteralExpression(@NotNull OdinCompoundLiteralExpression o) {
        this.type = inferTypeOfCompoundLiteral(o, context, o.getCompoundLiteral(), explicitMode);
    }

    @Override
    public void visitCallExpression(@NotNull OdinCallExpression o) {
        // Get type of expression. If it is callable, retrieve the return type and set that as result
        if (o instanceof OdinDirectiveExpression directiveExpression) {
            if (directiveExpression.getDirectiveIdentifier().getIdentifierToken().getText().equals("config")) {
                if (o.getArgumentList().size() == 2) {
                    OdinArgument lastArgument = o.getArgumentList().getLast();
                    if (lastArgument instanceof OdinUnnamedArgument unnamedArgument) {
                        OdinExpression expression = unnamedArgument.getExpression();
                        TsOdinType tsOdinType = doInferType(expression);
                        this.type = tsOdinType.typed();
                        return;
                    }
                }
            }
        }
        TsOdinType tsOdinType = doInferType(o.getExpression());

        if (tsOdinType instanceof TsOdinMetaType tsOdinMetaType) {
            // resolve to base type
            TsOdinMetaType tsOdinOriginalMetaType = tsOdinMetaType;
            if (tsOdinMetaType.getRepresentedMetaType() == ALIAS) {
                tsOdinMetaType = tsOdinMetaType.baseMetaType();
            }

            TsOdinMetaType.MetaType representedMetaType = tsOdinMetaType.getRepresentedMetaType();
            // normal procedure call
            if (representedMetaType == PROCEDURE) {
                TsOdinProcedureType procedureType = (TsOdinProcedureType) OdinTypeResolver.resolveMetaType(tsOdinType.getContext(), tsOdinMetaType);
                this.type = inferTypeOfProcedureCall(o, procedureType, context);
            }
            // struct specialization
            else if (representedMetaType == STRUCT) {
                TsOdinStructType structType = (TsOdinStructType) OdinTypeResolver.resolveMetaType(context, tsOdinMetaType);
                TsOdinStructType specializedStructType = OdinTypeSpecializer.specializeStructOrGetCached(context, structType, o.getArgumentList());
                TsOdinMetaType resultType = new TsOdinMetaType(STRUCT);
                resultType.setRepresentedType(specializedStructType);
                this.type = resultType;
            }
            // union specialization
            else if (representedMetaType == UNION) {
                TsOdinUnionType unionType = (TsOdinUnionType) OdinTypeResolver.resolveMetaType(context, tsOdinMetaType);
                TsOdinType specializedUnion = OdinTypeSpecializer.specializeUnionOrGetCached(context, unionType, o.getArgumentList());
                TsOdinMetaType resultType = new TsOdinMetaType(UNION);
                resultType.setRepresentedType(specializedUnion);
                this.type = resultType;
            }
            // procedure group
            else if (representedMetaType == PROCEDURE_GROUP) {
                TsOdinProcedureGroup procedureGroupType = (TsOdinProcedureGroup) OdinTypeResolver.resolveMetaType(tsOdinType.getContext(), tsOdinMetaType);
                this.type = inferTypeOfBestProcedure(o, procedureGroupType);
            }
            // Builtin procedures
            else if (representedMetaType == BUILTIN && tsOdinMetaType.representedType() instanceof TsOdinBuiltinProc proc) {
                this.type = OdinBuiltinProcedures.inferType(this, proc, o);
            }
            // type casting
            else {
                OdinExpression expression = o.getExpression().parenthesesUnwrap();
                if (expression instanceof OdinRefExpression
                        || expression instanceof OdinTypeDefinitionExpression) {
                    this.type = tsOdinOriginalMetaType.representedType();
                }
            }
        }
        // handles cases where the type was set in a field or a variable
        else if (tsOdinType.baseType(true) instanceof TsOdinProcedureType procedureType) {
            this.type = inferTypeOfProcedureCall(o, procedureType, context);
        } else if (tsOdinType.baseType(true) instanceof TsOdinProcedureGroup procedureGroupType) {
            this.type = inferTypeOfBestProcedure(o, procedureGroupType);
        }
    }

    private TsOdinType inferTypeOfBestProcedure(@NotNull OdinCallExpression o, TsOdinProcedureGroup procedureGroupType) {
        ProcedureRankingResult result = OdinProcedureRanker.findBestProcedure(context, procedureGroupType, o.getArgumentList());

        if (result.bestProcedure() != null) {
            return inferTypeOfProcedureCall(o, result.bestProcedure(), context);
        }

        if (!result.compatibleProcedures().isEmpty()) {
            boolean sameReturnTypes = true;
            outer:
            for (int i = 0; i < result.compatibleProcedures().size() - 1; i++) {
                TsOdinProcedureType a = result.compatibleProcedures().get(i).getFirst();
                for (int j = i + 1; j < result.compatibleProcedures().size(); j++) {
                    TsOdinProcedureType b = result.compatibleProcedures().get(i).getFirst();
                    if (a.getReturnTypes().size() == b.getReturnTypes().size()) {
                        for (int k = 0; k < a.getReturnTypes().size(); k++) {
                            TsOdinType returnTypeA = a.getReturnTypes().get(k);
                            TsOdinType returnTypeB = a.getReturnTypes().get(k);
                            if (!OdinTypeChecker.checkTypesStrictly(returnTypeA, returnTypeB) &&
                                    !OdinTypeChecker.checkTypesStrictly(returnTypeB, returnTypeA)
                            ) {
                                sameReturnTypes = false;
                                break outer;
                            }
                        }
                    } else {
                        sameReturnTypes = false;
                        break outer;
                    }
                }
            }

            if (sameReturnTypes) {
                TsOdinProcedureType firstProcedure = result.compatibleProcedures().getFirst().getFirst();
                if (!firstProcedure.getReturnTypes().isEmpty()) {
                    return inferTypeOfProcedureCall(o, result.compatibleProcedures().getFirst().getFirst(), context);
                } else {
                    return TsOdinBuiltInTypes.VOID;
                }
            }
            System.out.println("Could not determine best procedure for " + o.getText() + " at " + OdinInsightUtils.getLineColumn(o));
        } else {
            System.out.println("Could not find any compatible candidate for " + o.getText() + " at " + OdinInsightUtils.getLineColumn(o));
        }
        return TsOdinBuiltInTypes.UNKNOWN;
    }

    public record ProcedureRankingResult(
            List<Pair<TsOdinProcedureType, List<Pair<TsOdinType, OdinTypeChecker.TypeCheckResult>>>> compatibleProcedures,
            TsOdinProcedureType bestProcedure) {
    }

    private TsOdinType inferTypeOfProcedureCall(@NotNull OdinCallExpression o,
                                                TsOdinProcedureType procedureType,
                                                OdinContext context) {
        if (!procedureType.getReturnTypes().isEmpty()) {
            TsOdinProcedureType specializedType = OdinTypeSpecializer
                    .specializeProcedure(context, o.getArgumentList(), procedureType);
            if (specializedType.getReturnTypes().size() == 1) {
                return specializedType.getReturnTypes().getFirst();
            } else if (specializedType.getReturnTypes().size() > 1) {
                return new TsOdinTuple(specializedType.getReturnTypes());
            } else {
                return TsOdinBuiltInTypes.VOID;
            }
        }
        return TsOdinBuiltInTypes.UNKNOWN;
    }

    @Override
    public void visitIndexExpression(@NotNull OdinIndexExpression o) {
        // get type of expression. IF it is indexable (array, matrix, bitset, map), retrieve the indexed type and set that as result
        OdinExpression expression = o.getExpression();
        TsOdinType tsOdinType = doInferType(expression);

        tsOdinType = OdinInsightUtils.getReferenceableType(tsOdinType);

        if (tsOdinType instanceof TsOdinPointerType pointerType) {
            tsOdinType = pointerType.getDereferencedType();
        }

        if (tsOdinType instanceof TsOdinArrayType arrayType) {
            this.type = arrayType.getElementType();
        }

        if (tsOdinType instanceof TsOdinSliceType sliceType) {
            this.type = sliceType.getElementType();
        }

        if (tsOdinType instanceof TsOdinMapType mapType) {
            this.type = mapType.getValueType();
        }

        if (tsOdinType instanceof TsOdinMatrixType matrixType) {
            if (o.getIndex().getExpressionList().size() == 2) {
                this.type = matrixType.getElementType();
            } else {
                TsOdinArrayType tsOdinArrayType = new TsOdinArrayType();
                tsOdinArrayType.setElementType(matrixType.getElementType());
                if (matrixType.getPsiType() instanceof OdinMatrixType psiMatrixType) {
                    tsOdinArrayType.setPsiSizeElement(psiMatrixType.getArraySizeList().getFirst());
                }
                tsOdinArrayType.setContext(matrixType.getContext());
                this.type = tsOdinArrayType;
            }
        }

        if (tsOdinType instanceof TsOdinMultiPointerType multiPointerType) {
            this.type = multiPointerType.getDereferencedType();
        }

        if (tsOdinType instanceof TsOdinDynamicArray dynamicArray) {
            this.type = dynamicArray.getElementType();
        }
    }

    @Override
    public void visitSliceExpression(@NotNull OdinSliceExpression o) {
        OdinExpression expression = o.getExpression();
        TsOdinType tsOdinType = doInferType(expression);
        if (tsOdinType instanceof TsOdinSliceType
                || tsOdinType instanceof TsOdinMultiPointerType
                || tsOdinType instanceof TsOdinArrayType
                || tsOdinType instanceof TsOdinDynamicArray
        ) {
            this.type = tsOdinType;
        }
    }

    @Override
    public void visitDereferenceExpression(@NotNull OdinDereferenceExpression o) {
        // get type of expression. If it is a pointer, retrieve the dereferenced type and set that as result
        OdinExpression expression = o.getExpression();
        TsOdinType tsOdinType = doInferType(expression);
        if (tsOdinType instanceof TsOdinPointerType pointerType) {
            this.type = pointerType.getDereferencedType();
        }
    }

    @Override
    public void visitAddressExpression(@NotNull OdinAddressExpression o) {
        OdinExpression expression = o.getExpression();
        if (expression != null) {
            TsOdinType referencedType = doInferType(expression);
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
            this.type = doInferType(expression);
        }
    }

    @Override
    public void visitProcedureExpression(@NotNull OdinProcedureExpression o) {
        // get type of expression. If it is a procedure, retrieve the return type and set that as result
        var procedureType = o.getProcedureLiteralType();
        TsOdinMetaType tsOdinMetaType = new TsOdinMetaType(PROCEDURE);
        tsOdinMetaType.setPsiType(procedureType);

        this.type = tsOdinMetaType;
    }

    @Override
    public void visitCastExpression(@NotNull OdinCastExpression o) {
        this.type = o.getType().getResolvedType(context);
    }

    @Override
    public void visitAutoCastExpression(@NotNull OdinAutoCastExpression o) {
        this.type = this.expectedType;
    }

    @Override
    public void visitTransmuteExpression(@NotNull OdinTransmuteExpression o) {
        this.type = o.getType().getResolvedType(context);
    }

    @Override
    public void visitOrElseExpression(@NotNull OdinOrElseExpression o) {
        if (!o.getExpressionList().isEmpty()) {
            // TODO respect explicit mode
            OdinExpression expression = o.getExpressionList().getFirst();
            TsOdinType tsOdinType = expression.getInferredType(new OdinInferenceEngineParameters(context, createOptionalOkTuple(expectedType), 2, true));
            if (isOptionalOkTuple(tsOdinType)) {
                this.type = ((TsOdinTuple) tsOdinType).getTypes().getFirst();
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
        TsOdinType tsOdinTrueType = doInferType(trueBranchExpression);
        TsOdinType tsOdinFalseType = doInferType(falseBranchExpression);

        if (TsOdinUtils.areEqual(tsOdinTrueType, tsOdinFalseType)) {
            return tsOdinTrueType;
        }
        return TsOdinBuiltInTypes.UNKNOWN;
    }

    @Override
    public void visitLiteralExpression(@NotNull OdinLiteralExpression o) {
        o.getBasicLiteral().accept(this);
    }

    @Override
    public void visitNumericLiteral(@NotNull OdinNumericLiteral o) {
        if (o.getFloatDecLiteral() != null) {
            this.type = TsOdinBuiltInTypes.UNTYPED_FLOAT;
        } else if (o.getIntegerBinLiteral() != null
                || o.getIntegerHexLiteral() != null
                || o.getIntegerOctLiteral() != null
                || o.getIntegerDecLiteral() != null) {
            this.type = TsOdinBuiltInTypes.UNTYPED_INT;
        } else if (o.getComplexFloatLiteral() != null || o.getComplexIntegerDecLiteral() != null) {
            this.type = TsOdinBuiltInTypes.UNTYPED_COMPLEX;
        } else if (o.getQuatFloatLiteral() != null || o.getQuatIntegerDecLiteral() != null) {
            this.type = TsOdinBuiltInTypes.UNTYPED_QUATERNION;
        }
        super.visitNumericLiteral(o);
    }

    @Override
    public void visitStringLiteral(@NotNull OdinStringLiteral o) {
        if (o.getSqStringLiteral() != null) {
            this.type = TsOdinBuiltInTypes.UNTYPED_RUNE;
        }

        if (o.getDqStringLiteral() != null || o.getRawStringLiteral() != null) {
            this.type = TsOdinBuiltInTypes.UNTYPED_STRING;
        }
    }

    @Override
    public void visitRangeInclusiveExpression(@NotNull OdinRangeInclusiveExpression o) {
        OdinExpression odinExpression = o.getExpressionList().getFirst();
        this.type = doInferType(odinExpression);
    }

    @Override
    public void visitRangeExclusiveExpression(@NotNull OdinRangeExclusiveExpression o) {
        OdinExpression odinExpression = o.getExpressionList().getFirst();
        this.type = doInferType(odinExpression);
    }

    @Override
    public void visitAndExpression(@NotNull OdinAndExpression o) {
        this.type = TsOdinBuiltInTypes.BOOL;
    }

    @Override
    public void visitOrExpression(@NotNull OdinOrExpression o) {
        this.type = TsOdinBuiltInTypes.BOOL;
    }

    @Override
    public void visitGteExpression(@NotNull OdinGteExpression o) {
        this.type = TsOdinBuiltInTypes.BOOL;
    }

    @Override
    public void visitGtExpression(@NotNull OdinGtExpression o) {
        this.type = TsOdinBuiltInTypes.BOOL;
    }

    @Override
    public void visitLtExpression(@NotNull OdinLtExpression o) {
        this.type = TsOdinBuiltInTypes.BOOL;
    }

    @Override
    public void visitLteExpression(@NotNull OdinLteExpression o) {
        this.type = TsOdinBuiltInTypes.BOOL;
    }

    @Override
    public void visitEqeqExpression(@NotNull OdinEqeqExpression o) {
        this.type = TsOdinBuiltInTypes.BOOL;
    }

    @Override
    public void visitNeqExpression(@NotNull OdinNeqExpression o) {
        this.type = TsOdinBuiltInTypes.BOOL;
    }

    @Override
    public void visitInExpression(@NotNull OdinInExpression o) {
        this.type = TsOdinBuiltInTypes.BOOL;
    }

    @Override
    public void visitNotInExpression(@NotNull OdinNotInExpression o) {
        this.type = TsOdinBuiltInTypes.BOOL;
    }

    private static @NotNull TsOdinTuple createOptionalOkTuple(TsOdinType tsOdinType) {
        return new TsOdinTuple(List.of(tsOdinType == null ? TsOdinBuiltInTypes.UNDECIDED : tsOdinType, TsOdinBuiltInTypes.BOOL));
    }

    private static boolean isOptionalOkTuple(TsOdinType tsOdinType) {
        if (tsOdinType instanceof TsOdinTuple tsOdinTuple) {
            return tsOdinTuple.getTypes().size() == 2
                    && (tsOdinTuple.getTypes().get(1) == TsOdinBuiltInTypes.BOOL || tsOdinTuple.getTypes().get(1).isNillable());
        }
        return false;
    }

    @Override
    public void visitMaybeExpression(@NotNull OdinMaybeExpression o) {
        TsOdinType expectedUnionType = TsOdinBuiltInTypes.UNKNOWN;
        if (isOptionalOkTuple(expectedType)) {

            TsOdinTuple tuple = (TsOdinTuple) expectedType;
            expectedUnionType = tuple.get(0);
        }

        OdinExpression expression = o.getExpression();
        TsOdinType tsOdinType = doInferType(expression);
        if (tsOdinType instanceof TsOdinUnionType tsOdinUnionType) {
            if (tsOdinUnionType.getVariants().size() == 1) {
                this.type = createOptionalOkTuple(tsOdinUnionType.getVariants().getFirst().getType());
            } else if (tsOdinUnionType.getVariants().size() > 1 && !expectedUnionType.isUnknown()) {
                // Check if expectedType is in union variants
                this.type = createOptionalOkTuple(expectedUnionType);
            }
        }

    }

    public static @NotNull TsOdinType resolveTypeOfDeclaredIdentifier(OdinContext context, OdinDeclaredIdentifier declaredIdentifier) {
        OdinDeclaration odinDeclaration = PsiTreeUtil.getParentOfType(declaredIdentifier,
                false,
                OdinDeclaration.class);
        // NOTE: We cannot remove the symbol table because we might have to substitute types
        // in the context of specialized structs, procedures and unions
        // TODO How can we still use the cache?
        if (odinDeclaration instanceof OdinVariableDeclarationStatement declarationStatement) {
            var mainType = declarationStatement.getType();
            return mainType.getResolvedType(context);
        }

        if (odinDeclaration instanceof OdinVariableInitializationStatement initializationStatement) {
            if (initializationStatement.getType() != null) {
                return initializationStatement.getType().getResolvedType(context);
            }

            int index = initializationStatement.getDeclaredIdentifierList().indexOf(declaredIdentifier);
            List<OdinExpression> expressionList = Objects
                    .requireNonNull(initializationStatement.getRhsExpressions())
                    .getExpressionList();

            int lhsValuesCount = initializationStatement.getDeclaredIdentifierList().size();

            List<TsOdinType> tsOdinTypes = new ArrayList<>();
            for (OdinExpression odinExpression : expressionList) {
                TsOdinType tsOdinType = odinExpression.getInferredType(new OdinInferenceEngineParameters(context,
                        null,
                        lhsValuesCount,
                        false));
                if (tsOdinType instanceof TsOdinTuple tuple) {
                    tsOdinTypes.addAll(tuple.getTypes());
                } else {
                    tsOdinTypes.add(tsOdinType);
                }
            }

            if (tsOdinTypes.size() > index) {
                return tsOdinTypes.get(index);
            }
            return TsOdinBuiltInTypes.UNKNOWN;
        }

        // If we get a type here, then it's an alias
        if (odinDeclaration instanceof OdinConstantInitializationStatement initializationStatement) {
            if (initializationStatement.getType() != null) {
                OdinType mainType = initializationStatement.getType();
                return mainType.getResolvedType(context);
            }

            TsOdinMetaType metaType = findMetaType(context, declaredIdentifier, initializationStatement);
            if (metaType != null) return metaType;


            int index = initializationStatement
                    .getDeclaredIdentifierList()
                    .indexOf(declaredIdentifier);

            List<OdinExpression> expressionList = initializationStatement
                    .getExpressionList();

            List<TsOdinType> tsOdinTypes = new ArrayList<>();
            for (OdinExpression odinExpression : expressionList) {
                TsOdinType tsOdinType = odinExpression.getInferredType();
                if (tsOdinType instanceof TsOdinTuple tuple) {
                    tsOdinTypes.addAll(tuple.getTypes());
                } else {
                    tsOdinTypes.add(tsOdinType);
                }
            }

            if (tsOdinTypes.size() > index) {
                TsOdinType tsOdinType = tsOdinTypes.get(index);
                if (tsOdinType instanceof TsOdinMetaType aliasedMetaType) {
                    TsOdinMetaType tsOdinMetaType = new TsOdinMetaType(ALIAS);
                    tsOdinMetaType.setDeclaration(odinDeclaration);
                    tsOdinMetaType.setTypeExpression(expressionList.get(index));
                    tsOdinMetaType.setDeclaredIdentifier(declaredIdentifier);
                    tsOdinMetaType.setName(declaredIdentifier.getName());
                    tsOdinMetaType.setAliasedMetaType(aliasedMetaType);

                    return tsOdinMetaType;
                }
                return tsOdinType;
            }
            return TsOdinBuiltInTypes.UNKNOWN;
        }

        if (odinDeclaration instanceof OdinFieldDeclarationStatement fieldDeclarationStatement) {
            if (fieldDeclarationStatement.getType() != null) {
                return fieldDeclarationStatement.getType().getResolvedType(context);
            }
            return TsOdinBuiltInTypes.UNKNOWN;
        }

        if (odinDeclaration instanceof OdinParameterDeclarator parameterDeclaration) {
            OdinTypeDefinitionContainer typeDefinitionContainer = parameterDeclaration
                    .getTypeDefinitionContainer();
            if (typeDefinitionContainer != null) {
                OdinType parameterType = typeDefinitionContainer
                        .getType();
                return parameterType.getResolvedType(context);
            }
        }

        if (odinDeclaration instanceof OdinParameterInitialization parameterInitialization) {
            OdinType type = parameterInitialization.getTypeDefinition();
            if (type != null) {
                return type.getResolvedType(context);
            }

            OdinExpression odinExpression = parameterInitialization.getExpression();
            return odinExpression.getInferredType();
        }

        if (odinDeclaration instanceof OdinPolymorphicType polymorphicType) {
            return OdinTypeResolver.findMetaType(context,
                    declaredIdentifier,
                    odinDeclaration,
                    null,
                    polymorphicType);
        }

        if (odinDeclaration instanceof OdinEnumValueDeclaration odinEnumValueDeclaration) {
            OdinEnumType enumType = PsiTreeUtil.getParentOfType(odinEnumValueDeclaration, true, OdinEnumType.class);
            OdinConstantInitializationStatement enumDeclarationStatement = PsiTreeUtil.getParentOfType(enumType, true, OdinConstantInitializationStatement.class);

            OdinDeclaredIdentifier enumDeclaredIdentifier = null;
            if (enumDeclarationStatement != null) {
                enumDeclaredIdentifier = enumDeclarationStatement.getDeclaredIdentifiers().getFirst();
            }
            if (enumType != null) {
                return OdinTypeResolver.resolveType(context,
                        enumDeclaredIdentifier,
                        enumDeclarationStatement,
                        enumType);
            }
        }

        if (odinDeclaration instanceof OdinForInParameterDeclaration forInParameterDeclaration) {
            OdinForBlock forInBlock = PsiTreeUtil.getParentOfType(forInParameterDeclaration, OdinForBlock.class);

            if (forInBlock != null && forInBlock.getForInParameterDeclaration() != null) {
                List<OdinDeclaredIdentifier> identifiers = forInParameterDeclaration.getForInParameterDeclaratorList().stream().map(OdinForInParameterDeclarator::getDeclaredIdentifier).toList();
                int index = identifiers.indexOf(declaredIdentifier);
                var odinDeclaredIdentifier = forInParameterDeclaration.getForInParameterDeclaratorList().get(index);
                boolean isReference = odinDeclaredIdentifier.getAnd() != null;

                // TODO Range expression should be treated differently. For now, just take the type the expression resolves to
                OdinExpression expression = forInBlock.getForInParameterDeclaration().getExpression();
                TsOdinType refExpressionType = expression.getInferredType();
                TsOdinType tsOdinType = OdinInsightUtils.getReferenceableType(refExpressionType)
                        .baseType(true);

                if (tsOdinType instanceof TsOdinMapType mapType) {
                    if (index == 0) {
                        return createReferenceType(mapType.getKeyType(), isReference);
                    }
                    if (index == 1) {
                        return createReferenceType(mapType.getValueType(), isReference);
                    }
                }

                if (tsOdinType instanceof TsOdinArrayType arrayType) {
                    if (index == 0) {
                        return createReferenceType(arrayType.getElementType(), isReference);
                    }

                    if (index == 1) {
                        return TsOdinBuiltInTypes.INT;
                    }
                }

                if (tsOdinType == TsOdinBuiltInTypes.STRING || tsOdinType == TsOdinBuiltInTypes.UNTYPED_STRING) {
                    if (index == 0) {
                        return TsOdinBuiltInTypes.RUNE;
                    }

                    if (index == 1) {
                        return TsOdinBuiltInTypes.INT;
                    }
                }

                if (tsOdinType instanceof TsOdinSliceType sliceType) {
                    if (index == 0) {
                        return createReferenceType(sliceType.getElementType(), isReference);
                    }

                    if (index == 1) {
                        return TsOdinBuiltInTypes.INT;
                    }
                }

                if (tsOdinType instanceof TsOdinDynamicArray dynamicArray) {
                    if (index == 0) {
                        return createReferenceType(dynamicArray.getElementType(), isReference);
                    }

                    if (index == 1) {
                        return TsOdinBuiltInTypes.INT;
                    }
                }

                if (tsOdinType instanceof TsOdinMultiPointerType multiPointerType) {
                    if (index == 0) {
                        return createReferenceType(multiPointerType.getDereferencedType(), isReference);
                    }

                    if (index == 1) {
                        return TsOdinBuiltInTypes.INT;
                    }
                }

                if (tsOdinType instanceof TsOdinTuple tuple) {
                    return tuple.get(index);
                }

                if (tsOdinType instanceof TsOdinSoaSliceType soaSliceType) {
                    if (index == 0) {
                        return createReferenceType(soaSliceType.getSoaStructType(), isReference);
                    }

                    if (index == 1) {
                        return TsOdinBuiltInTypes.INT;
                    }
                }

                if (tsOdinType instanceof TsOdinBitSetType bitSetType) {
                    if (index == 0) {
                        return createReferenceType(bitSetType.getElementType(), isReference);
                    }
                    if (index == 1) {
                        return TsOdinBuiltInTypes.INT;
                    }
                }

                if (tsOdinType instanceof TsOdinMetaType metaType && metaType.representedType()
                        .baseType(true) instanceof TsOdinEnumType enumType) {
                    if (index == 0) {
                        return enumType;
                    }

                    if (index == 1) {
                        return TsOdinBuiltInTypes.INT;
                    }
                }

                if (expression instanceof OdinRangeExclusiveExpression || expression instanceof OdinRangeInclusiveExpression) {
                    return tsOdinType;
                }
                return TsOdinBuiltInTypes.UNKNOWN;
            }

        }

        if (odinDeclaration instanceof OdinSwitchTypeVariableDeclaration) {
            OdinSwitchBlock switchInBlock = PsiTreeUtil.getParentOfType(odinDeclaration, OdinSwitchBlock.class, true);
            if (switchInBlock != null && switchInBlock.getSwitchInClause() != null) {
                OdinExpression expression = switchInBlock.getSwitchInClause().getExpression();
                return expression.getInferredType();
            }
        }

        return TsOdinBuiltInTypes.UNKNOWN;
    }

    private static @NotNull TsOdinType doResolveTypeOfDeclaredIdentifier(OdinDeclaredIdentifier declaredIdentifier, OdinContext context) {
        return declaredIdentifier.getType(context);
    }

    public static @NotNull TsOdinPolymorphicType createExplicitPolymorphicType(OdinDeclaredIdentifier declaredIdentifier, OdinDeclaration odinDeclaration) {
        TsOdinPolymorphicType tsOdinPolymorphicType = new TsOdinPolymorphicType();
        tsOdinPolymorphicType.setDeclaration(odinDeclaration);
        tsOdinPolymorphicType.setDeclaredIdentifier(declaredIdentifier);
        tsOdinPolymorphicType.setName(declaredIdentifier.getName());
        tsOdinPolymorphicType.setDistinct(true);
        tsOdinPolymorphicType.setExplicit(true);
        return tsOdinPolymorphicType;
    }

    private static TsOdinType getSwitchInReferenceType(
            TsOdinType declarationType,
            @NotNull PsiElement position,
            OdinSwitchBlock switchInBlock) {
        List<OdinSwitchCase> ancestors = new ArrayList<>();
        OdinSwitchBody switchBody = switchInBlock.getSwitchBody();
        if (switchBody != null) {
            OdinSwitchCases switchCases = switchBody.getSwitchCases();
            if (switchCases != null) {
                for (OdinSwitchCase odinSwitchCase : switchCases.getSwitchCaseList()) {
                    // TODO remove from here and compute outside of this method
                    if (PsiTreeUtil.isAncestor(odinSwitchCase, position, true)) {
                        ancestors.add(odinSwitchCase);
                    }
                }
            }
        }
        if (ancestors.size() != 1)
            return TsOdinBuiltInTypes.UNKNOWN;

        OdinSwitchCase switchCase = ancestors.getFirst();
        if (switchCase != null && switchCase.getCaseClause() != null) {
            @NotNull List<OdinExpression> expressionList = switchCase.getCaseClause().getExpressionList();

            if (expressionList.size() == 1) {
                OdinExpression odinExpression = expressionList.getFirst();
                TsOdinType caseType = odinExpression.getInferredType();
                if (caseType instanceof TsOdinMetaType metaType) {
                    return OdinTypeResolver.resolveMetaType(caseType.getContext(), metaType);
                }
            }
        }
        return declarationType;
    }

    public static @Nullable TsOdinMetaType findMetaType(@NotNull OdinContext context,
                                                        OdinDeclaredIdentifier declaredIdentifier,
                                                        OdinConstantInitializationStatement initializationStatement) {
        if (initializationStatement.getExpressionList().isEmpty())
            return null;
        OdinExpression firstExpression = initializationStatement.getExpressionList().getFirst();
        OdinType declaredType = OdinInsightUtils.getDeclaredType(initializationStatement);
        if (
                declaredType instanceof OdinStructType
                        || declaredType instanceof OdinBitFieldType
                        || declaredType instanceof OdinUnionType
                        || declaredType instanceof OdinProcedureGroupType
                        || declaredType instanceof OdinProcedureType
                        || declaredType instanceof OdinProcedureLiteralType
                        || declaredType instanceof OdinEnumType) {
            // check distinct
            return OdinTypeResolver.findMetaType(
                    context,
                    declaredIdentifier,
                    initializationStatement,
                    firstExpression,
                    declaredType
            );
        }
        return null;
    }

    @SuppressWarnings("unused")
    public static TsOdinType createReferenceType(TsOdinType dereferencedType, boolean isReference) {
        return dereferencedType;
    }

    @Override
    public void visitOrBreakExpression(@NotNull OdinOrBreakExpression o) {
        OdinExpression expression = o.getExpression();
        inferTypeOfOrStatements(expression);
    }

    @Override
    public void visitOrContinueExpression(@NotNull OdinOrContinueExpression o) {
        OdinExpression expression = o.getExpression();
        inferTypeOfOrStatements(expression);
    }

    @Override
    public void visitOrReturnExpression(@NotNull OdinOrReturnExpression o) {
        OdinExpression expression = o.getExpression();
        inferTypeOfOrStatements(expression);
    }

    private void inferTypeOfOrStatements(OdinExpression expression) {
        TsOdinType tsOdinType = expression.getInferredType(new OdinInferenceEngineParameters(context,
                TsOdinBuiltInTypes.UNKNOWN,
                2,
                explicitMode));
        if (tsOdinType instanceof TsOdinTuple tsOdinTuple) {
            if (tsOdinTuple.getTypes().size() == 2) {
                this.type = tsOdinTuple.getTypes().getFirst();
            } else if (tsOdinTuple.getTypes().size() > 2) {
                List<TsOdinType> tsOdinTypes = tsOdinTuple.getTypes().subList(0, tsOdinTuple.getTypes().size() - 1);
                this.type = new TsOdinTuple(tsOdinTypes);
            }
        }
    }
}