package com.lasagnerd.odin.codeInsight.typeInference;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.symbols.*;
import com.lasagnerd.odin.codeInsight.typeSystem.*;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.lasagnerd.odin.codeInsight.typeSystem.TsOdinMetaType.MetaType.*;

public class OdinInferenceEngine extends OdinVisitor {
    // Result fields
    TsOdinType type;

    // Input fields
    final OdinSymbolTable symbolTable;
    private final TsOdinType expectedType;
    private final int lhsValuesCount;


    public OdinInferenceEngine(OdinSymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.lhsValuesCount = 1;
        this.expectedType = TsOdinBuiltInTypes.UNKNOWN;
    }

    public OdinInferenceEngine(OdinSymbolTable symbolTable, @NotNull TsOdinType expectedType, int lhsValuesCount) {
        this.symbolTable = symbolTable;
        this.expectedType = expectedType;
        this.lhsValuesCount = lhsValuesCount;
    }

    @NotNull
    public static TsOdinType inferType(OdinSymbolTable symbolTable, OdinExpression expression) {
        OdinInferenceEngine odinInferenceEngine = new OdinInferenceEngine(symbolTable);
        expression.accept(odinInferenceEngine);
        return odinInferenceEngine.type != null ? odinInferenceEngine.type : TsOdinBuiltInTypes.UNKNOWN;
    }

    public static TsOdinType doInferType(OdinSymbolTable symbolTable, @NotNull OdinExpression expression) {
        return doInferType(symbolTable, TsOdinBuiltInTypes.UNKNOWN, 1, expression);
    }

    public static TsOdinType doInferType(OdinSymbolTable symbolTable, TsOdinType expectedType, int lhsValuesCount, @NotNull OdinExpression expression) {
        OdinInferenceEngine odinInferenceEngine = new OdinInferenceEngine(symbolTable, expectedType, lhsValuesCount);
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

    public static TsOdinType doInferType(OdinSymbolTable symbolTable, int lhsValuesCount, @NotNull OdinExpression expression) {
        return doInferType(symbolTable, TsOdinBuiltInTypes.UNKNOWN, lhsValuesCount, expression);
    }

    public static TsOdinType doInferType(OdinExpression odinExpression) {
        OdinSymbolTable symbolTable = OdinSymbolTableResolver.computeSymbolTable(odinExpression);
        return doInferType(symbolTable, odinExpression);
    }

    public static TsOdinType inferTypeOfCompoundLiteral(OdinSymbolTable symbolTable, OdinCompoundLiteral compoundLiteral) {
        TsOdinType tsOdinType;
        if (compoundLiteral instanceof OdinCompoundLiteralTyped compoundLiteralTyped) {
            tsOdinType = OdinTypeResolver.resolveType(symbolTable, compoundLiteralTyped.getTypeContainer().getType());
        } else if (compoundLiteral instanceof OdinCompoundLiteralUntyped && compoundLiteral.getParent() instanceof OdinExpression odinExpression) {
            if (odinExpression.getParent() instanceof OdinBinaryExpression binaryExpression) {
                tsOdinType = inferType(symbolTable, binaryExpression.getLeft());
            } else {
                tsOdinType = inferExpectedType(symbolTable, odinExpression);
            }
        } else {
            tsOdinType = TsOdinBuiltInTypes.UNKNOWN;
        }
        return tsOdinType;
    }

    @Override
    public void visitUnaryExpression(@NotNull OdinUnaryExpression o) {
        PsiElement operator = OdinPsiUtil.getOperator(o);
        if (operator != null) {
            this.type = inferType(symbolTable, o.getExpression());
        }
    }

    @Override
    public void visitBinaryExpression(@NotNull OdinBinaryExpression o) {
        TsOdinType leftType = inferType(this.symbolTable, o.getLeft());
        TsOdinType rightType;
        if (o.getRight() instanceof OdinCompoundLiteralExpression literalExpression
                && literalExpression.getCompoundLiteral() instanceof OdinCompoundLiteralUntyped) {
            rightType = leftType;
        } else {
            rightType = inferType(this.symbolTable, Objects.requireNonNull(o.getRight()));
        }

        PsiElement operator = o.getOperator();
        if (operator != null) {
            IElementType operatorType = PsiUtilCore.getElementType(operator);
            this.type = OdinTypeConverter.inferTypeOfSymmetricalBinaryExpression(leftType, rightType);
        }
    }

    @Override
    public void visitTypeDefinitionExpression(@NotNull OdinTypeDefinitionExpression o) {
        this.type = OdinTypeResolver.findMetaType(symbolTable, o, o.getType());
    }

    @Override
    public void visitDirectiveExpression(@NotNull OdinDirectiveExpression o) {
        PsiElement identifierToken = o.getDirectiveIdentifier().getIdentifierToken();
        if (identifierToken.getText().equals("caller_location")) {
            OdinSdkService builtinSymbolService = OdinSdkService.getInstance(o.getProject());
            if (builtinSymbolService != null) {
                List<OdinSymbol> runtimeCoreSymbols = builtinSymbolService.getRuntimeCoreSymbols();
                OdinSymbolTable odinSymbolTable = OdinSymbolTable.from(runtimeCoreSymbols);
                OdinSymbol symbol = odinSymbolTable.getSymbol("Source_Code_Location");
                if (symbol != null) {
                    OdinDeclaration declaration = symbol.getDeclaration();
                    if (declaration instanceof OdinConstantInitializationStatement structDeclarationStatement) {
                        OdinStructType structType = PsiTreeUtil.findChildOfType(structDeclarationStatement, OdinStructType.class);
                        if (structType != null) {
                            this.type = OdinTypeResolver.resolveType(odinSymbolTable, structType);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void visitImplicitSelectorExpression(@NotNull OdinImplicitSelectorExpression o) {
        String enumValue = o.getIdentifier().getText();
        PsiElement expressionContainer = PsiTreeUtil.getParentOfType(o, true, OdinLhs.class, OdinRhs.class);

        if (expressionContainer != null) {
            OdinCompoundLiteral compoundLiteral = PsiTreeUtil.getParentOfType(expressionContainer, OdinCompoundLiteral.class);
            TsOdinType tsOdinType = inferTypeOfCompoundLiteral(symbolTable, compoundLiteral).baseType(true);


            // for arrays this is only acceptable if it is the lhs
            if (tsOdinType instanceof TsOdinArrayType arrayType && expressionContainer instanceof OdinLhs) {
                OdinExpression psiTypeExpression = arrayType.getPsiSizeElement().getExpression();
                if (psiTypeExpression != null) {
                    TsOdinType sizeType = OdinInferenceEngine.inferType(symbolTable, psiTypeExpression);
                    if (sizeType instanceof TsOdinMetaType metaType) {
                        this.type = metaType.representedType();
                    }
                }
            } else if (tsOdinType instanceof TsOdinBitSetType bitSetType && expressionContainer instanceof OdinRhs) {
                this.type = bitSetType.getElementType();
            } else if (expressionContainer instanceof OdinRhs) {
                TsOdinType expectedType = inferExpectedType(symbolTable, o);
                this.type = inferImplicitSelectorType(expectedType, enumValue);
            }
        } else {
            // TODO fix this: with case clause expression ".A ..= .B" this won't work
            PsiElement parent = o.getParent();
            PsiElement firstRhsExpression = findFirstRhsContainer(parent);

            if (parent instanceof OdinExpression odinExpression) {
                parent = odinExpression.unwrap();
            }

            if (parent instanceof OdinBinaryExpression binaryExpression) {
                OdinExpression otherExpression = binaryExpression.getLeft() == o ? binaryExpression.getRight() : binaryExpression.getLeft();
                if (otherExpression != null) {
                    IElementType operatorType = PsiUtilCore.getElementType(binaryExpression.getOperator());
                    if (OdinPsiUtil.COMPARISON_OPERATORS.contains(operatorType)) {
                        TsOdinType tsOdinType = inferType(symbolTable, otherExpression);
                        this.type = inferImplicitSelectorType(tsOdinType, enumValue);
                    }

                    if (operatorType == OdinTypes.IN || operatorType == OdinTypes.NOT_IN) {
                        TsOdinType tsOdinType = inferType(symbolTable, otherExpression);
                        if (tsOdinType.baseType(true) instanceof TsOdinBitSetType tsOdinBitSetType) {
                            if (tsOdinBitSetType.getElementType() instanceof TsOdinEnumType tsOdinEnumType) {
                                if (enumContainsValue(tsOdinEnumType, enumValue)) {
                                    this.type = tsOdinEnumType;
                                }
                            }
                        }
                    }

                    if (operatorType == OdinTypes.RANGE_INCLUSIVE || operatorType == OdinTypes.RANGE_EXCLUSIVE) {
                        if (firstRhsExpression instanceof OdinCaseClause) {
                            TsOdinType caseClauseType = inferExpectedType(symbolTable, binaryExpression);
                            if (caseClauseType.baseType(true) instanceof TsOdinEnumType tsOdinEnumType) {
                                if (enumContainsValue(tsOdinEnumType, enumValue)) {
                                    this.type = tsOdinEnumType;
                                }
                            }
                        }
                    }
                }
            } else if (parent instanceof OdinIndex index && index.getParent() instanceof OdinIndexExpression indexExpression) {
                TsOdinType tsOdinType = inferType(symbolTable, indexExpression.getExpression());
                TsOdinType baseType = tsOdinType.baseType(true);
                if (baseType instanceof TsOdinArrayType tsOdinArrayType) {
                    OdinArraySize psiSizeElement = tsOdinArrayType.getPsiSizeElement();
                    if (psiSizeElement != null && psiSizeElement.getExpression() != null) {
                        TsOdinType sizeType = OdinInferenceEngine.inferType(symbolTable, psiSizeElement.getExpression());
                        if (sizeType instanceof TsOdinMetaType metaType && metaType.representedType() instanceof TsOdinEnumType tsOdinEnumType) {
                            if (enumContainsValue(tsOdinEnumType, enumValue)) {
                                this.type = tsOdinEnumType;
                            }
                        }
                    }
                } else if (baseType instanceof TsOdinMapType tsOdinMapType) {
                    if (tsOdinMapType.getKeyType().baseType(true) instanceof TsOdinEnumType tsOdinEnumType) {
                        if (enumContainsValue(tsOdinEnumType, enumValue)) {
                            this.type = tsOdinEnumType;
                        }
                    }
                }
            } else {
                TsOdinType expectedType = inferExpectedType(symbolTable, o);
                this.type = inferImplicitSelectorType(expectedType, enumValue);
            }
        }
    }

    private @NotNull TsOdinType inferImplicitSelectorType(TsOdinType tsOdinType, String enumValue) {
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

    private static boolean enumContainsValue(TsOdinEnumType tsOdinEnumType, String enumValue) {
        List<OdinSymbol> typeElements = OdinInsightUtils.getTypeElements(tsOdinEnumType, OdinSymbolTable.EMPTY);
        return typeElements.stream().anyMatch(s -> s.getName().equals(enumValue));
    }

    @Override
    public void visitRefExpression(@NotNull OdinRefExpression refExpression) {
        OdinSymbolTable localSymbolTable;
        OdinSymbolTable globalSymbolTable;

        TsOdinType tsOdinRefExpressionType = TsOdinBuiltInTypes.UNKNOWN;
        if (refExpression.getExpression() != null) {
            // solve for expression first. This defines the scope
            // extract symbol table<
            tsOdinRefExpressionType = OdinInsightUtils.getReferenceableType(doInferType(symbolTable, refExpression.getExpression()));
            OdinSymbolTable typeSymbols = OdinInsightUtils.getTypeElements(refExpression.getProject(), tsOdinRefExpressionType, true);

            if (tsOdinRefExpressionType instanceof TsOdinPackageReferenceType) {
                localSymbolTable = typeSymbols;
                globalSymbolTable = typeSymbols;
            } else {
                globalSymbolTable = tsOdinRefExpressionType.getSymbolTable();
                globalSymbolTable.getTypeTable().putAll(typeSymbols.getTypeTable());
                localSymbolTable = typeSymbols;
            }
            // The resolved polymorphic types must be taken over from type scope
            this.symbolTable.addTypes(localSymbolTable);
        } else {
            localSymbolTable = this.symbolTable;
            globalSymbolTable = this.symbolTable;
        }

        if (refExpression.getIdentifier() != null) {
            // using current scope, find identifier declaration and extract type
            String name = refExpression.getIdentifier().getText();
            OdinSymbol symbol = localSymbolTable.getSymbol(name);
            if (symbol != null) {
                if (symbol.isImplicitlyDeclared()) {
                    if (symbol.getSymbolType() == OdinSymbolType.SWIZZLE_FIELD) {
                        int swizzleArraySize = symbol.getName().length();
                        if (tsOdinRefExpressionType instanceof TsOdinArrayType tsOdinArrayType) {
                            if (swizzleArraySize == 1) {
                                this.type = tsOdinArrayType.getElementType();
                            } else {
                                TsOdinArrayType swizzleArray = new TsOdinArrayType();
                                swizzleArray.setSymbolTable(tsOdinArrayType.getSymbolTable());
                                swizzleArray.setElementType(tsOdinArrayType.getElementType());
                                swizzleArray.setSize(swizzleArraySize);
                                this.type = swizzleArray;
                            }
                        } else if (swizzleArraySize == 1) {
                            // TODO complex, quaternion
                            if (tsOdinRefExpressionType == TsOdinBuiltInTypes.COMPLEX32) {
                                this.type = TsOdinBuiltInTypes.F16;
                            } else if (tsOdinRefExpressionType == TsOdinBuiltInTypes.COMPLEX64) {
                                this.type = TsOdinBuiltInTypes.F32;
                            } else if (tsOdinRefExpressionType == TsOdinBuiltInTypes.COMPLEX128) {
                                this.type = TsOdinBuiltInTypes.F64;
                            } else if (tsOdinRefExpressionType == TsOdinBuiltInTypes.QUATERNION64) {
                                this.type = TsOdinBuiltInTypes.F16;
                            } else if (tsOdinRefExpressionType == TsOdinBuiltInTypes.QUATERNION128) {
                                this.type = TsOdinBuiltInTypes.F32;
                            } else if (tsOdinRefExpressionType == TsOdinBuiltInTypes.QUATERNION256) {
                                this.type = TsOdinBuiltInTypes.F64;
                            }
                        }

                    } else if (symbol.getSymbolType() == OdinSymbolType.SOA_FIELD) {
                        if (tsOdinRefExpressionType instanceof TsOdinSoaStructType soaStructType) {
                            this.type = soaStructType.getFields().get(symbol.getName());
                        }
                    } else if (symbol.getSymbolType() == OdinSymbolType.BUILTIN_TYPE) {
                        this.type = createBuiltinMetaType(name);
                    } else {
                        Project project = refExpression.getProject();
                        OdinSdkService builtinSymbolService = OdinSdkService.getInstance(project);
                        if (symbol.getPsiType() != null && builtinSymbolService != null) {
                            String typeName = OdinInsightUtils.getTypeName(symbol.getPsiType());
                            this.type = builtinSymbolService.getType(typeName);
                        }
                    }
                } else {
                    OdinSymbolTable symbolTableForTypeResolution;

                    if (symbol.isVisibleThroughUsing()) {
                        symbolTableForTypeResolution = OdinSymbolTableResolver.computeSymbolTable(symbol.getDeclaredIdentifier());
                        symbolTableForTypeResolution.putAll(globalSymbolTable);
                    } else {
                        symbolTableForTypeResolution = globalSymbolTable;
                    }

                    this.type = inferTypeOfDeclaredIdentifier(
                            symbolTableForTypeResolution,
                            symbol.getDeclaredIdentifier(),
                            refExpression.getIdentifier()
                    );
                }
            } else {
                // TODO Add poly paras as symbols
                TsOdinType polyParameter = symbolTable.getType(name);
                if (polyParameter != null) {
                    TsOdinMetaType tsOdinMetaType = new TsOdinMetaType(POLYMORPHIC);
                    tsOdinMetaType.setSymbolTable(symbolTable);
                    tsOdinMetaType.setDeclaration(polyParameter.getDeclaration());
                    tsOdinMetaType.setPsiType(polyParameter.getPsiType());
                    tsOdinMetaType.setDeclaredIdentifier(polyParameter.getDeclaredIdentifier());
                    tsOdinMetaType.setName(name);
                    this.type = tsOdinMetaType;
                } else if (TsOdinBuiltInTypes.RESERVED_TYPES.contains(name)) {
                    this.type = createBuiltinMetaType(name);
                }
            }
        }

        // Type conversion
        if (refExpression.getType() != null) {
            TsOdinType tsOdinType = OdinTypeResolver.resolveType(symbolTable, refExpression.getType());
            if (this.lhsValuesCount == 2) {
                this.type = createOptionalOkTuple(tsOdinType);
            } else {
                this.type = tsOdinType;
            }
        }
    }

    private static @Nullable TsOdinType inferTypeOfDeclaredIdentifier(
            OdinSymbolTable globalSymbolTable,
            PsiNamedElement namedElement,
            @NotNull OdinIdentifier identifier) {
        TsOdinType tsOdinType = TsOdinBuiltInTypes.UNKNOWN;
        OdinImportDeclarationStatement importDeclarationStatement = getImportDeclarationStatement(namedElement);
        if (importDeclarationStatement != null) {
            tsOdinType = createPackageReferenceType(globalSymbolTable.getPackagePath(), importDeclarationStatement);
        } else if (namedElement instanceof OdinDeclaredIdentifier declaredIdentifier) {
            OdinDeclaration odinDeclaration = PsiTreeUtil.getParentOfType(namedElement,
                    false,
                    OdinDeclaration.class);

            tsOdinType = resolveTypeOfDeclaration(identifier,
                    globalSymbolTable,
                    declaredIdentifier,
                    odinDeclaration
            );
            tsOdinType = OdinTypeConverter.convertToTyped(tsOdinType);
        }
        return tsOdinType;
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
        this.type = inferTypeOfCompoundLiteral(symbolTable, o.getCompoundLiteral());
    }

    @Override
    public void visitCallExpression(@NotNull OdinCallExpression o) {
        // Get type of expression. If it is callable, retrieve the return type and set that as result
        TsOdinType tsOdinType = doInferType(symbolTable, o.getExpression());

        if (tsOdinType instanceof TsOdinMetaType tsOdinMetaType) {
            // resolve to base type
            TsOdinMetaType tsOdinOriginalMetaType = tsOdinMetaType;
            if (tsOdinMetaType.getRepresentedMetaType() == ALIAS) {
                tsOdinMetaType = tsOdinMetaType.baseMetaType();
            }

            TsOdinMetaType.MetaType representedMetaType = tsOdinMetaType.getRepresentedMetaType();
            // normal procedure call
            if (representedMetaType == PROCEDURE) {
                TsOdinProcedureType procedureType = (TsOdinProcedureType) OdinTypeResolver.resolveMetaType(tsOdinType.getSymbolTable(), tsOdinMetaType);
                this.type = inferTypeOfProcedureCall(o, procedureType, symbolTable);
            }
            // struct specialization
            else if (representedMetaType == STRUCT) {
                TsOdinStructType structType = (TsOdinStructType) OdinTypeResolver.resolveMetaType(symbolTable, tsOdinMetaType);
                TsOdinStructType specializedStructType = OdinTypeSpecializer.specializeStructOrGetCached(symbolTable, structType, o.getArgumentList());
                TsOdinMetaType resultType = new TsOdinMetaType(STRUCT);
                resultType.setRepresentedType(specializedStructType);
                this.type = resultType;
            }
            // union specialization
            else if (representedMetaType == UNION) {
                TsOdinUnionType unionType = (TsOdinUnionType) OdinTypeResolver.resolveMetaType(symbolTable, tsOdinMetaType);
                TsOdinType specializedUnion = OdinTypeSpecializer.specializeUnionOrGetCached(symbolTable, unionType, o.getArgumentList());
                TsOdinMetaType resultType = new TsOdinMetaType(UNION);
                resultType.setRepresentedType(specializedUnion);
                this.type = resultType;
            }
            // procedure group
            else if (representedMetaType == PROCEDURE_GROUP) {
                TsOdinProcedureGroup procedureGroupType = (TsOdinProcedureGroup) OdinTypeResolver.resolveMetaType(tsOdinType.getSymbolTable(), tsOdinMetaType);
                this.type = inferTypeOfBestProcedure(o, procedureGroupType);
            }
            // type casting
            else {
                OdinExpression expression = o.getExpression().unwrap();
                if (expression instanceof OdinRefExpression
                        || expression instanceof OdinTypeDefinitionExpression) {
                    this.type = tsOdinOriginalMetaType.representedType();
                }
            }
        }
        // handles cases where the type was set in a field or a variable
        else if (tsOdinType.baseType(true) instanceof TsOdinProcedureType procedureType) {
            this.type = inferTypeOfProcedureCall(o, procedureType, symbolTable);
        } else if (tsOdinType.baseType(true) instanceof TsOdinProcedureGroup procedureGroupType) {
            this.type = inferTypeOfBestProcedure(o, procedureGroupType);
        }
    }

    private TsOdinType inferTypeOfBestProcedure(@NotNull OdinCallExpression o, TsOdinProcedureGroup procedureGroupType) {
        ProcedureRankingResult result = findBestProcedure(symbolTable, procedureGroupType, o.getArgumentList());

        if (result.bestProcedure() != null) {
            return inferTypeOfProcedureCall(o, result.bestProcedure(), symbolTable);
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
                    return inferTypeOfProcedureCall(o, result.compatibleProcedures().getFirst().getFirst(), symbolTable);
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

    private static @NotNull ProcedureRankingResult findBestProcedure(OdinSymbolTable symbolTable, TsOdinProcedureGroup procedureGroup, @NotNull List<OdinArgument> argumentList) {
        List<Pair<TsOdinProcedureType, List<Pair<TsOdinType, OdinTypeChecker.TypeCheckResult>>>> compatibleProcedures = new ArrayList<>();
        for (TsOdinProcedureType targetProcedure : procedureGroup.getProcedures()) {
            @Nullable Map<OdinExpression, TsOdinParameter> argumentExpressions = OdinInsightUtils.getArgumentToParameterMap(targetProcedure.getParameters(), argumentList);
            if (argumentExpressions == null) continue;

            boolean allParametersCompatible = true;

            // Gather all compatible procedures along with their original argument
            List<Pair<TsOdinType, OdinTypeChecker.TypeCheckResult>> compatibilityResults = new ArrayList<>();
            for (var entry : argumentExpressions.entrySet()) {
                TsOdinParameter tsOdinParameter = entry.getValue();
                OdinExpression argumentExpression = entry.getKey();

                TsOdinType parameterType = tsOdinParameter.getType().baseType();
                TsOdinType argumentType = TsOdinBuiltInTypes.UNKNOWN;
                TsOdinType parameterBaseType = parameterType.baseType(true);
                if (argumentExpression instanceof OdinImplicitSelectorExpression implicitSelectorExpression) {
                    if (parameterBaseType instanceof TsOdinEnumType enumType) {
                        if (enumContainsValue(enumType, implicitSelectorExpression.getIdentifier().getText())) {
                            argumentType = parameterType;
                        }
                    }
                } else if (argumentExpression instanceof OdinCompoundLiteralExpression compoundLiteralExpression
                        && compoundLiteralExpression.getCompoundLiteral() instanceof OdinCompoundLiteralUntyped) {
                    if (parameterBaseType instanceof TsOdinBitSetType
                            || parameterBaseType instanceof TsOdinStructType
                            || parameterBaseType instanceof TsOdinArrayType
                            || parameterBaseType instanceof TsOdinSliceType
                            || parameterBaseType instanceof TsOdinMatrixType) {
                        argumentType = parameterType;
                    }
                } else {
                    argumentType = inferType(symbolTable, argumentExpression).baseType();
                }

                OdinTypeChecker.TypeCheckResult compatibilityResult = OdinTypeChecker.checkTypes(argumentType,
                        parameterType,
                        tsOdinParameter.isAnyInt());

                if (!compatibilityResult.isCompatible()) {
                    allParametersCompatible = false;
                    break;
                }
                compatibilityResults.add(Pair.create(argumentType, compatibilityResult));
            }
            if (allParametersCompatible) {
                compatibleProcedures.add(Pair.create(targetProcedure, compatibilityResults));
            }
        }

        TsOdinProcedureType bestProcedure = null;
        if (compatibleProcedures.size() == 1) {
            bestProcedure = compatibleProcedures.getFirst().getFirst();

        } else if (!compatibleProcedures.isEmpty()) {
            bestProcedure = breakTie(compatibleProcedures);
        }
        return new ProcedureRankingResult(compatibleProcedures, bestProcedure);
    }

    private record ProcedureRankingResult(
            List<Pair<TsOdinProcedureType, List<Pair<TsOdinType, OdinTypeChecker.TypeCheckResult>>>> compatibleProcedures,
            TsOdinProcedureType bestProcedure) {
    }

    private static @Nullable TsOdinProcedureType breakTie(List<Pair<TsOdinProcedureType, List<Pair<TsOdinType, OdinTypeChecker.TypeCheckResult>>>> compatibleProcedures) {
        // tie-breaker if possible

        List<Pair<TsOdinProcedureType, Integer>> scores = new ArrayList<>();
        for (var compatibleProcedure : compatibleProcedures) {
            int conversionCost = 0;
            TsOdinProcedureType tsOdinProcedureType = compatibleProcedure.getFirst();
            for (var compatibilityResult : compatibleProcedure.getSecond()) {

                var result = compatibilityResult.getSecond();
                conversionCost += result.getConversionActionList().size();
            }
            scores.add(Pair.create(tsOdinProcedureType, conversionCost));
        }

        Integer minConversionCost = scores.stream().mapToInt(p -> p.getSecond()).min().orElseThrow();
        List<Pair<TsOdinProcedureType, Integer>> minConversionCosts = scores.stream().filter(p -> p.getSecond().equals(minConversionCost)).toList();
        TsOdinProcedureType bestProcedure;
        if (minConversionCosts.size() == 1) {
            bestProcedure = minConversionCosts.getFirst().getFirst();
        } else {
            bestProcedure = null;
        }
        return bestProcedure;
    }

    private static TsOdinType inferTypeOfProcedureCall(@NotNull OdinCallExpression o,
                                                       TsOdinProcedureType procedureType,
                                                       OdinSymbolTable symbolTable) {
        OdinSymbol soaZip = OdinInsightUtils.findBuiltinSymbolOfCallExpression(symbolTable, o, text -> text.equals("soa_zip"));
        OdinSymbol soaUnzip = OdinInsightUtils.findBuiltinSymbolOfCallExpression(symbolTable, o, text -> text.equals("soa_unzip"));
        OdinSymbol swizzle = OdinInsightUtils.findBuiltinSymbolOfCallExpression(symbolTable, o, text -> text.equals("swizzle"));
        OdinSymbol typeOf = OdinInsightUtils.findBuiltinSymbolOfCallExpression(symbolTable, o, text -> text.equals("type_of"));
        if (soaZip != null) {
            TsOdinSoaSliceType soaSlice = new TsOdinSoaSliceType();
            for (OdinArgument odinArgument : o.getArgumentList()) {
                if (odinArgument instanceof OdinNamedArgument namedArgument) {
                    TsOdinType sliceType = inferType(symbolTable, namedArgument.getExpression());
                    soaSlice.getSlices().put(namedArgument.getIdentifier().getText(), sliceType);
                }
            }
            return soaSlice;
        } else if (soaUnzip != null) {
            TsOdinTuple tuple = new TsOdinTuple();

            if (o.getArgumentList().size() == 1) {
                if (o.getArgumentList().getFirst() instanceof OdinUnnamedArgument unnamedArgument) {
                    TsOdinType tsOdinType = inferType(symbolTable, unnamedArgument.getExpression());
                    if (tsOdinType instanceof TsOdinSoaSliceType tsOdinSoaSliceType) {
                        tuple.getTypes().addAll(tsOdinSoaSliceType.getSlices().values());
                    }
                }
            }
            return tuple;
        } else if (swizzle != null) {
            if (o.getArgumentList().size() > 1) {
                OdinArgument first = o.getArgumentList().getFirst();
                if (first instanceof OdinUnnamedArgument arrayArgument) {
                    TsOdinType tsOdinType = OdinInferenceEngine.inferType(symbolTable, arrayArgument.getExpression());
                    if (tsOdinType.baseType(true) instanceof TsOdinArrayType tsOdinArrayType) {
                        tsOdinArrayType.setSize(o.getArgumentList().size() - 1);
                        return tsOdinArrayType;
                    }
                }
            }
        } else if (typeOf != null) {
            List<OdinArgument> argumentList = o.getArgumentList();
            if (!argumentList.isEmpty()) {
                OdinArgument first = argumentList.getFirst();
                if (first instanceof OdinUnnamedArgument argument) {
                    TsOdinType tsOdinType = OdinInferenceEngine.doInferType(argument.getExpression());
                    return OdinTypeResolver.createMetaType(tsOdinType, null);
                }
            }
        } else if (!procedureType.getReturnTypes().isEmpty()) {
            TsOdinProcedureType specializedType = OdinTypeSpecializer
                    .specializeProcedure(symbolTable, o.getArgumentList(), procedureType);
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
        TsOdinType tsOdinType = doInferType(symbolTable, expression);

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
                tsOdinArrayType.setSymbolTable(matrixType.getSymbolTable());
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
        TsOdinType tsOdinType = doInferType(symbolTable, expression);
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
        TsOdinType tsOdinType = doInferType(symbolTable, expression);
        if (tsOdinType instanceof TsOdinPointerType pointerType) {
            this.type = pointerType.getDereferencedType();
        }
    }

    @Override
    public void visitAddressExpression(@NotNull OdinAddressExpression o) {
        OdinExpression expression = o.getExpression();
        if (expression != null) {
            TsOdinType referencedType = doInferType(symbolTable, expression);
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
            this.type = doInferType(symbolTable, expression);
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
        this.type = OdinTypeResolver.resolveType(symbolTable, o.getType());
    }

    @Override
    public void visitAutoCastExpression(@NotNull OdinAutoCastExpression o) {
        this.type = this.expectedType;
    }

    @Override
    public void visitTransmuteExpression(@NotNull OdinTransmuteExpression o) {

        this.type = OdinTypeResolver.resolveType(symbolTable, o.getType());
    }

    @Override
    public void visitOrElseExpression(@NotNull OdinOrElseExpression o) {
        if (!o.getExpressionList().isEmpty()) {
            TsOdinType tsOdinType = doInferType(symbolTable, createOptionalOkTuple(expectedType), 2, o.getExpressionList().getFirst());
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
        TsOdinType tsOdinTrueType = doInferType(symbolTable, trueBranchExpression);
        TsOdinType tsOdinFalseType = doInferType(symbolTable, falseBranchExpression);

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
        this.type = doInferType(symbolTable, odinExpression);
    }

    @Override
    public void visitRangeExclusiveExpression(@NotNull OdinRangeExclusiveExpression o) {
        OdinExpression odinExpression = o.getExpressionList().getFirst();
        this.type = doInferType(symbolTable, odinExpression);
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
        return new TsOdinTuple(List.of(tsOdinType, TsOdinBuiltInTypes.BOOL));
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
        TsOdinType tsOdinType = doInferType(symbolTable, expression);
        if (tsOdinType instanceof TsOdinUnionType tsOdinUnionType) {
            if (tsOdinUnionType.getVariants().size() == 1) {
                this.type = createOptionalOkTuple(tsOdinUnionType.getVariants().getFirst().getType());
            } else if (tsOdinUnionType.getVariants().size() > 1 && !expectedUnionType.isUnknown()) {
                // Check if expectedType is in union variants
                this.type = createOptionalOkTuple(expectedUnionType);
            }
        }

    }

    public static TsOdinType resolveTypeOfDeclaration(@Nullable OdinIdentifier identifier,
                                                      OdinSymbolTable parentSymbolTable,
                                                      OdinDeclaredIdentifier declaredIdentifier,
                                                      OdinDeclaration odinDeclaration) {
        if (odinDeclaration instanceof OdinVariableDeclarationStatement declarationStatement) {
            var mainType = declarationStatement.getType();
            return OdinTypeResolver.resolveType(parentSymbolTable, mainType);
        }

        if (odinDeclaration instanceof OdinVariableInitializationStatement initializationStatement) {
            if (initializationStatement.getType() != null) {
                return OdinTypeResolver.resolveType(parentSymbolTable, initializationStatement.getType());
            }

            int index = initializationStatement.getDeclaredIdentifierList().indexOf(declaredIdentifier);
            List<OdinExpression> expressionList = Objects
                    .requireNonNull(initializationStatement.getRhsExpressions())
                    .getExpressionList();

            int lhsValuesCount = initializationStatement.getDeclaredIdentifierList().size();

            List<TsOdinType> tsOdinTypes = new ArrayList<>();
            for (OdinExpression odinExpression : expressionList) {
                // TODO Only recompute if we know that the declared identifier is shadowing another one (maybe save this information
                //  in the symbol?)
                OdinSymbolTable odinSymbolTable = OdinSymbolTableResolver.computeSymbolTable(odinExpression);
                TsOdinType tsOdinType = doInferType(odinSymbolTable, lhsValuesCount, odinExpression);
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
                return OdinTypeResolver.resolveType(parentSymbolTable, mainType);
            }

            TsOdinMetaType metaType = findMetaType(parentSymbolTable, declaredIdentifier, initializationStatement);
            if (metaType != null) return metaType;


            int index = initializationStatement
                    .getDeclaredIdentifierList()
                    .indexOf(declaredIdentifier);

            List<OdinExpression> expressionList = initializationStatement
                    .getExpressionList();

            List<TsOdinType> tsOdinTypes = new ArrayList<>();
            for (OdinExpression odinExpression : expressionList) {
                OdinSymbolTable nextSymbolTable = OdinSymbolTableResolver.computeSymbolTable(odinExpression);
                TsOdinType tsOdinType = doInferType(nextSymbolTable, odinExpression);
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
                    tsOdinMetaType.setSymbolTable(parentSymbolTable);
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
                return OdinTypeResolver.resolveType(parentSymbolTable, fieldDeclarationStatement.getType());
            }
            return TsOdinBuiltInTypes.UNKNOWN;
        }

        if (odinDeclaration instanceof OdinParameterDeclarator parameterDeclaration) {
            OdinType parameterType = parameterDeclaration
                    .getTypeDefinitionContainer()
                    .getType();
            OdinSymbolTable typeSymbolTable = OdinSymbolTableResolver.computeSymbolTable(parameterType);
            return OdinTypeResolver.resolveType(typeSymbolTable, parameterType);
        }

        if (odinDeclaration instanceof OdinParameterInitialization parameterInitialization) {
            OdinType type = parameterInitialization.getTypeDefinition();
            if (type != null) {
                return OdinTypeResolver.resolveType(parentSymbolTable, type);
            }

            OdinExpression odinExpression = parameterInitialization.getExpression();
            return doInferType(parentSymbolTable, odinExpression);
        }

        if (odinDeclaration instanceof OdinPolymorphicType polymorphicType) {
            TsOdinMetaType tsOdinMetaType = new TsOdinMetaType(POLYMORPHIC);
            tsOdinMetaType.setSymbolTable(parentSymbolTable);
            tsOdinMetaType.setDeclaration(polymorphicType);
            tsOdinMetaType.setPsiType(polymorphicType);
            tsOdinMetaType.setDeclaredIdentifier(declaredIdentifier);
            tsOdinMetaType.setName(declaredIdentifier.getName());
            return tsOdinMetaType;
        }

        if (odinDeclaration instanceof OdinEnumValueDeclaration odinEnumValueDeclaration) {
            OdinEnumType enumType = PsiTreeUtil.getParentOfType(odinEnumValueDeclaration, true, OdinEnumType.class);
            OdinConstantInitializationStatement enumDeclarationStatement = PsiTreeUtil.getParentOfType(enumType, true, OdinConstantInitializationStatement.class);

            OdinDeclaredIdentifier enumDeclaredIdentifier = null;
            if (enumDeclarationStatement != null) {
                enumDeclaredIdentifier = enumDeclarationStatement.getDeclaredIdentifiers().getFirst();
            }
            if (enumType != null) {
                return OdinTypeResolver.resolveType(parentSymbolTable,
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
                TsOdinType tsOdinType = OdinInsightUtils.getReferenceableType(inferType(parentSymbolTable, expression))
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

                if (expression instanceof OdinRangeExclusiveExpression || expression instanceof OdinRangeInclusiveExpression) {
                    return tsOdinType;
                }
                return TsOdinBuiltInTypes.UNKNOWN;
            }

        }

        if (odinDeclaration instanceof OdinSwitchTypeVariableDeclaration && identifier != null) {
            OdinSwitchBlock switchInBlock = PsiTreeUtil.getParentOfType(odinDeclaration, OdinSwitchBlock.class, true);
            if (switchInBlock != null && switchInBlock.getSwitchInClause() != null) {
                TsOdinType tsOdinType = inferType(parentSymbolTable, switchInBlock.getSwitchInClause().getExpression());
                List<OdinSwitchCase> ancestors = new ArrayList<>();
                OdinSwitchBody switchBody = switchInBlock.getSwitchBody();
                if (switchBody != null) {
                    OdinSwitchCases switchCases = switchBody.getSwitchCases();
                    if (switchCases != null) {
                        for (OdinSwitchCase odinSwitchCase : switchCases.getSwitchCaseList()) {
                            if (PsiTreeUtil.isAncestor(odinSwitchCase, identifier, true)) {
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
                        TsOdinType caseType = inferType(parentSymbolTable, odinExpression);
                        if (caseType instanceof TsOdinMetaType metaType) {
                            return OdinTypeResolver.resolveMetaType(caseType.getSymbolTable(), metaType);
                        }
                    }
                }
                return tsOdinType;
            }
        }


        return TsOdinBuiltInTypes.UNKNOWN;
    }

    public static @Nullable TsOdinMetaType findMetaType(OdinSymbolTable symbolTable,
                                                        OdinDeclaredIdentifier declaredIdentifier,
                                                        OdinConstantInitializationStatement initializationStatement) {
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
                    symbolTable,
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

    /**
     * Types are expected at in/out nodes such as return statements, case blocks, arguments, assignments of typed variables, etc.
     * This method finds the expected type of the RHS of where the passed PSI element is located in the AST.
     *
     * @param symbolTable The symbol table used for resolving the expected type
     * @param position    The expression for which we want to find out, whether it is expected to have a certain type
     * @return The expected type
     */
    public static TsOdinType inferExpectedType(OdinSymbolTable symbolTable, OdinExpression position) {
        // Find the container of the rhs expression
        PsiElement rhsContainer = findFirstRhsContainer(position);
        if (rhsContainer == null)
            return TsOdinBuiltInTypes.UNKNOWN;

        // Find the top most expression under the rhs container
        PsiElement prevParent = rhsContainer != position ? PsiTreeUtil.findPrevParent(rhsContainer, position) : rhsContainer;
        if (!(prevParent instanceof OdinExpression rhsExpression)) {
            return TsOdinBuiltInTypes.UNKNOWN;
        }

        if (rhsContainer instanceof OdinReturnStatement returnStatement) {
            OdinProcedureDefinition procedureDefinition = PsiTreeUtil.getParentOfType(returnStatement, OdinProcedureDefinition.class);
            if (procedureDefinition != null) {
                int pos = returnStatement.getExpressionList().indexOf(rhsExpression);
                OdinReturnParameters returnParameters = procedureDefinition.getProcedureSignature().getProcedureType().getReturnParameters();
                if (returnParameters != null) {
                    OdinParamEntries paramEntries = returnParameters.getParamEntries();
                    if (paramEntries != null) {
                        if (paramEntries.getParamEntryList().size() > pos) {
                            OdinParamEntry paramEntry = paramEntries.getParamEntryList().get(pos);
                            return OdinTypeResolver.resolveType(symbolTable, paramEntry.getParameterDeclaration().getTypeDefinition());
                        }
                    } else if (pos == 0) {
                        OdinType psiType = returnParameters.getType();
                        if (psiType != null) {
                            return OdinTypeResolver.resolveType(symbolTable, psiType);
                        }
                    }
                }
            }
        }

        if (rhsContainer instanceof OdinRhsExpressions rhsExpressions) {
            int index = rhsExpressions.getExpressionList().indexOf(rhsExpression);
            PsiElement grandParent = rhsExpressions.getParent();

            if (grandParent instanceof OdinAssignmentStatement statement) {

                List<OdinExpression> lhsExpressions = statement.getLhsExpressions().getExpressionList();
                if (lhsExpressions.size() > index) {
                    OdinExpression lhsExpression = lhsExpressions.get(index);
                    return OdinInferenceEngine.inferType(symbolTable, lhsExpression);
                }
            }

            if (grandParent instanceof OdinVariableInitializationStatement odinVariableInitializationStatement) {
                OdinType psiType = odinVariableInitializationStatement.getType();
                if (psiType != null) {
                    TsOdinType tsOdinType = OdinTypeResolver.resolveType(symbolTable, psiType);
                    return unfoldExpression(tsOdinType, rhsExpression, position);
                }
            }
        }

        if (rhsContainer instanceof OdinRhs) {
            OdinCompoundLiteral compoundLiteral = PsiTreeUtil.getParentOfType(rhsContainer, OdinCompoundLiteralTyped.class, OdinCompoundLiteralUntyped.class);
            Objects.requireNonNull(compoundLiteral);
            OdinElementEntry elemEntry = (OdinElementEntry) rhsContainer.getParent();

            TsOdinType tsOdinType = null;
            if (compoundLiteral instanceof OdinCompoundLiteralUntyped compoundLiteralUntyped) {
                tsOdinType = OdinInferenceEngine.inferExpectedType(symbolTable, (OdinExpression) compoundLiteralUntyped.getParent());

            }
            if (compoundLiteral instanceof OdinCompoundLiteralTyped compoundLiteralTyped) {
                tsOdinType = OdinTypeResolver.resolveType(symbolTable, compoundLiteralTyped.getTypeContainer().getType());
            }

            if (tsOdinType != null) {
                TsOdinType tsOdinBaseType = tsOdinType.baseType(true);
                if (tsOdinBaseType instanceof TsOdinStructType tsOdinStructType) {

                    List<OdinSymbol> structFields = OdinInsightUtils.getStructFields(tsOdinStructType);
                    // Named element entry
                    if (elemEntry.getLhs() != null) {
                        String fieldName = elemEntry.getLhs().getText();
                        OdinSymbol symbol = structFields.stream().filter(s -> s.getName().equals(fieldName))
                                .findFirst()
                                .orElse(null);
                        if (symbol != null && symbol.getPsiType() != null) {
                            TsOdinType fieldType = OdinTypeResolver.resolveType(tsOdinType.getSymbolTable(), symbol.getPsiType());
                            return unfoldExpression(fieldType, rhsExpression, position);
                        }
                    }
                    // Positional initialization
                    else {
                        int index = compoundLiteral.getCompoundValue()
                                .getCompoundValueBody()
                                .getElementEntryList()
                                .indexOf(elemEntry);

                        if (structFields.size() > index) {
                            OdinSymbol symbol = structFields.get(index);
                            if (symbol != null && symbol.getPsiType() != null) {
                                TsOdinType fieldType = OdinTypeResolver.resolveType(tsOdinType.getSymbolTable(), symbol.getPsiType());
                                return unfoldExpression(fieldType, rhsExpression, position);
                            }
                        }
                    }
                } else if (tsOdinBaseType instanceof TsOdinArrayType tsOdinArrayType) {
                    return unfoldExpression(tsOdinArrayType.getElementType(), rhsExpression, position);
                } else if (tsOdinBaseType instanceof TsOdinSliceType tsOdinSliceType) {
                    return unfoldExpression(tsOdinSliceType.getElementType(), rhsExpression, position);
                } else if (tsOdinBaseType instanceof TsOdinDynamicArray tsOdinDynamicArray) {
                    return unfoldExpression(tsOdinDynamicArray.getElementType(), rhsExpression, position);
                } else if (tsOdinBaseType instanceof TsOdinBitSetType bitSetType) {
                    return unfoldExpression(bitSetType.getElementType(), rhsExpression, position);
                }
            }
        }

        if (rhsContainer instanceof OdinArgument argument) {
            @Nullable OdinPsiElement callExpression = PsiTreeUtil.getParentOfType(argument, OdinCallExpression.class, OdinCallType.class);
            if (callExpression != null) {
                TsOdinType tsOdinType;
                List<OdinArgument> argumentList;
                if (callExpression instanceof OdinCallExpression odinCallExpression) {
                    tsOdinType = OdinInferenceEngine.doInferType(symbolTable, odinCallExpression.getExpression());
                    // Here we have to get a meta type, otherwise the call expression does not make sense
                    if (tsOdinType instanceof TsOdinMetaType tsOdinMetaType) {
                        tsOdinType = tsOdinMetaType.representedType();
                    } else if (!(tsOdinType.baseType(true) instanceof TsOdinProcedureType)) {
                        tsOdinType = TsOdinBuiltInTypes.UNKNOWN;
                    }
                    argumentList = odinCallExpression.getArgumentList();
                } else if (callExpression instanceof OdinCallType odinCallType) {
                    tsOdinType = OdinTypeResolver.resolveType(symbolTable, odinCallType.getType());

                    argumentList = odinCallType.getArgumentList();
                } else {
                    tsOdinType = TsOdinBuiltInTypes.UNKNOWN;
                    argumentList = Collections.emptyList();
                }

                TsOdinMetaType.MetaType metaType = tsOdinType.getMetaType();
                if (metaType == ALIAS) {
                    metaType = tsOdinType.baseType(true).getMetaType();
                }

                if (metaType == PROCEDURE) {
                    TsOdinProcedureType callingProcedure = (TsOdinProcedureType) tsOdinType.baseType(true);
                    Map<OdinExpression, TsOdinParameter> argumentToParameterMap = OdinInsightUtils.getArgumentToParameterMap(callingProcedure.getParameters(), argumentList);
                    if (argumentToParameterMap != null) {
                        TsOdinParameter tsOdinParameter = argumentToParameterMap.get(rhsExpression);
                        return unfoldExpression(tsOdinParameter.getType(), rhsExpression, position);
                    }
                } else if (metaType == PROCEDURE_GROUP) {
                    TsOdinProcedureGroup callingProcedureGroup = (TsOdinProcedureGroup) tsOdinType.baseType(true);
                    ProcedureRankingResult result = findBestProcedure(symbolTable, callingProcedureGroup, argumentList);
                    TsOdinProcedureType callingProcedure = result.bestProcedure();
                    if (callingProcedure != null) {
                        Map<OdinExpression, TsOdinParameter> argumentToParameterMap = OdinInsightUtils.getArgumentToParameterMap(callingProcedure.getParameters(), argumentList);
                        if (argumentToParameterMap != null) {
                            TsOdinParameter tsOdinParameter = argumentToParameterMap.get(rhsExpression);
                            return unfoldExpression(tsOdinParameter.getType(), rhsExpression, position);
                        }
                    } else {
                        boolean allUnnamed = argumentList.stream().allMatch(a -> a instanceof OdinUnnamedArgument);
                        if (!allUnnamed)
                            return TsOdinBuiltInTypes.UNKNOWN;

                        int argumentIndex = argumentList.indexOf(argument);
                        TsOdinType argumentType = null;
                        for (var entry : result.compatibleProcedures()) {
                            TsOdinProcedureType procedureType = entry.getFirst();
                            TsOdinType previousArgument = argumentType;
                            if (procedureType.getParameters().size() > argumentIndex) {
                                argumentType = procedureType.getParameters().get(argumentIndex).getType().baseType();
                            } else {
                                return TsOdinBuiltInTypes.UNKNOWN;
                            }

                            if (previousArgument != null) {
                                boolean argsCompatible = OdinTypeChecker.checkTypesStrictly(argumentType, previousArgument);
                                if (!argsCompatible) {
                                    return TsOdinBuiltInTypes.UNKNOWN;
                                }
                            }
                        }

                        return argumentType == null? TsOdinBuiltInTypes.UNKNOWN : argumentType;
                    }
                } else if (metaType == STRUCT) {
                    TsOdinStructType structType = (TsOdinStructType) tsOdinType.baseType(true);
                    Map<OdinExpression, TsOdinParameter> argumentToParameterMap = OdinInsightUtils.getArgumentToParameterMap(structType.getParameters(), argumentList);
                    if (argumentToParameterMap != null) {
                        TsOdinParameter tsOdinParameter = argumentToParameterMap.get(rhsExpression);
                        return unfoldExpression(tsOdinParameter.getType(), rhsExpression, position);
                    }
                } else if (metaType == UNION) {
                    TsOdinUnionType unionType = (TsOdinUnionType) tsOdinType.baseType(true);
                    Map<OdinExpression, TsOdinParameter> argumentToParameterMap = OdinInsightUtils.getArgumentToParameterMap(unionType.getParameters(), argumentList);
                    if (argumentToParameterMap != null) {
                        TsOdinParameter tsOdinParameter = argumentToParameterMap.get(rhsExpression);
                        return unfoldExpression(tsOdinParameter.getType(), rhsExpression, position);
                    }
                } else if (callExpression instanceof OdinCallExpression odinCallExpression &&
                        (odinCallExpression.getExpression().unwrap() instanceof OdinRefExpression
                                || odinCallExpression.getExpression().unwrap() instanceof OdinTypeDefinitionExpression)) {
                    return unfoldExpression(tsOdinType, rhsExpression, position);
                }
            }
        }

        if (rhsContainer instanceof OdinCaseClause caseClause) {
            OdinSwitchBlock switchBlock = PsiTreeUtil.getParentOfType(caseClause, OdinSwitchBlock.class);
            if (switchBlock != null) {
                if (switchBlock.getExpression() != null) {
                    return OdinInferenceEngine.doInferType(switchBlock.getExpression());
                }
            }
        }

        if (rhsContainer instanceof OdinConstantInitializationStatement constantInitializationStatement) {
            OdinType type = constantInitializationStatement.getType();
            if (type != null) {
                return OdinTypeResolver.resolveType(symbolTable, type);
            }
            return TsOdinBuiltInTypes.UNKNOWN;
        }

        if (rhsContainer instanceof OdinVariableInitializationStatement variableInitializationStatement) {
            OdinType type = variableInitializationStatement.getType();
            if (type != null) {
                return OdinTypeResolver.resolveType(symbolTable, type);
            }
            return TsOdinBuiltInTypes.UNKNOWN;
        }

        if (rhsContainer instanceof OdinParameterInitialization parameterInitialization) {
            OdinType declaredType = parameterInitialization.getTypeDefinition();
            if (declaredType != null) {
                return OdinTypeResolver.resolveType(symbolTable, declaredType);
            }
            return TsOdinBuiltInTypes.UNKNOWN;
        }

        if (rhsContainer instanceof OdinCaseClause caseClause) {
            OdinSwitchBlock switchBlock = PsiTreeUtil.getParentOfType(caseClause, OdinSwitchBlock.class);
            if (switchBlock != null) {
                OdinExpression expression = switchBlock.getExpression();
                if (expression != null) {
                    return inferType(symbolTable, expression);
                }
            }
        }

        // TODO: add index
        return TsOdinBuiltInTypes.VOID;
    }

    private static TsOdinType unfoldExpression(TsOdinType tsOdinType, OdinExpression rhsExpression, OdinExpression position) {
        if (rhsExpression == position)
            return tsOdinType;
        if (tsOdinType instanceof TsOdinPointerType pointerType && rhsExpression instanceof OdinAddressExpression addressExpression) {
            return unfoldExpression(pointerType.getDereferencedType(), addressExpression.getExpression(), position);
        }
        if (tsOdinType instanceof TsOdinArrayType tsOdinArrayType && rhsExpression instanceof OdinIndexExpression indexExpression) {
            return unfoldExpression(tsOdinArrayType.getElementType(), indexExpression.getExpression(), position);
        }
        return TsOdinBuiltInTypes.UNKNOWN;
    }

    private static @Nullable PsiElement findFirstRhsContainer(PsiElement psiElement) {
        return OdinInsightUtils.findParentOfType(
                psiElement,
                false,
                new Class<?>[]{
                        OdinReturnStatement.class,
                        OdinRhs.class,
                        OdinArgument.class,
                        OdinRhsExpressions.class,
                        OdinCaseClause.class,
                        OdinVariableInitializationStatement.class,
                        OdinConstantInitializationStatement.class,
                        OdinParameterInitialization.class,
                        OdinIndex.class,
                        OdinCaseClause.class
                },
                new Class<?>[]{
                        OdinLhsExpressions.class,
                        OdinLhs.class,
                }
        );
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
        TsOdinType tsOdinType = doInferType(symbolTable, TsOdinBuiltInTypes.UNKNOWN, 2, expression);
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