package com.lasagnerd.odin.codeInsight.typeInference;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.symbols.*;
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
    OdinImportDeclarationStatement importDeclarationStatement;
    boolean isImport;

    // Input fields
    final OdinSymbolTable symbolTable;
    private final TsOdinType expectedType;
    private final int lhsValuesCount;


    public OdinInferenceEngine(OdinSymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.lhsValuesCount = 1;
        this.expectedType = TsOdinType.UNKNOWN;
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
        if (odinInferenceEngine.isImport) {
            return createPackageReferenceType(symbolTable.getPackagePath(), odinInferenceEngine.importDeclarationStatement);
        } else {
            return odinInferenceEngine.type != null ? odinInferenceEngine.type : TsOdinType.UNKNOWN;
        }
    }

    public static TsOdinType doInferType(OdinSymbolTable symbolTable, @NotNull OdinExpression expression) {
        return doInferType(symbolTable, TsOdinType.UNKNOWN, 1, expression);
    }

    public static TsOdinType doInferType(OdinSymbolTable symbolTable, TsOdinType expectedType, int lhsValuesCount, @NotNull OdinExpression expression) {
        OdinInferenceEngine odinInferenceEngine = new OdinInferenceEngine(symbolTable, expectedType, lhsValuesCount);
        expression.accept(odinInferenceEngine);

        if (odinInferenceEngine.isImport) {
            OdinImportDeclarationStatement importDeclarationStatement = odinInferenceEngine.importDeclarationStatement;
            return createPackageReferenceType(symbolTable.getPackagePath(), importDeclarationStatement);
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

    public static TsOdinType doInferType(OdinSymbolTable symbolTable, int lhsValuesCount, @NotNull OdinExpression expression) {
        return doInferType(symbolTable, TsOdinType.UNKNOWN, lhsValuesCount, expression);
    }

    public static TsOdinType doInferType(OdinExpression odinExpression) {
        OdinSymbolTable symbolTable = OdinSymbolTableResolver.computeSymbolTable(odinExpression);
        return doInferType(symbolTable, odinExpression);
    }

    @Override
    public void visitBinaryExpression(@NotNull OdinBinaryExpression o) {
        TsOdinType leftType = inferType(this.symbolTable, o.getLeft());
        TsOdinType rightType = inferType(this.symbolTable, Objects.requireNonNull(o.getRight()));


        IElementType elementType = PsiUtilCore.getElementType(o.getOperator());

        TokenSet tokenSet = TokenSet.create(OdinTypes.PLUS, OdinTypes.STAR, OdinTypes.DIV, OdinTypes.MOD);
        if (tokenSet.contains(elementType)) {
            this.type = OdinTypeConverter.convertTypeOfBinaryExpression(leftType, rightType);
        }
    }

    @Override
    public void visitTypeDefinitionExpression(@NotNull OdinTypeDefinitionExpression o) {
        this.type = OdinTypeResolver.findMetaType(symbolTable, o.getType());
    }

    @Override
    public void visitDirectiveExpression(@NotNull OdinDirectiveExpression o) {
        PsiElement identifierToken = o.getDirective().getDirectiveHead().getIdentifierToken();
        if (identifierToken.getText().equals("caller_location")) {
            OdinBuiltinSymbolService builtinSymbolService = OdinBuiltinSymbolService.getInstance(o.getProject());
            if (builtinSymbolService != null) {
                List<OdinSymbol> runtimeCoreSymbols = builtinSymbolService.getRuntimeCoreSymbols();
                OdinSymbolTable odinSymbolTable = OdinSymbolTable.from(runtimeCoreSymbols);
                OdinSymbol symbol = odinSymbolTable.getSymbol("Source_Code_Location");
                if (symbol != null) {
                    OdinDeclaration declaration = symbol.getDeclaration();
                    if (declaration instanceof OdinStructDeclarationStatement structDeclarationStatement) {
                        this.type = OdinTypeResolver.resolveType(odinSymbolTable, structDeclarationStatement.getStructType());
                    }
                }
            }
        }
    }

    @Override
    public void visitRefExpression(@NotNull OdinRefExpression refExpression) {
        OdinSymbolTable localScope;
        OdinSymbolTable globalScope;

        TsOdinType tsOdinRefExpressionType = TsOdinType.UNKNOWN;
        if (refExpression.getExpression() != null) {
            // solve for expression first. This defines the scope
            // extract symbol table
            tsOdinRefExpressionType = doInferType(symbolTable, refExpression.getExpression());
            if(tsOdinRefExpressionType instanceof TsOdinTypeAlias typeAlias) {
                tsOdinRefExpressionType = typeAlias.getBaseType();
            }
            OdinSymbolTable typeSymbols = OdinInsightUtils.getTypeElements(tsOdinRefExpressionType);

            if (tsOdinRefExpressionType instanceof TsOdinPackageReferenceType) {
                localScope = typeSymbols;
                globalScope = typeSymbols;
            } else if (tsOdinRefExpressionType instanceof TsOdinArrayType tsOdinArrayType) {
                List<OdinSymbol> swizzleFields = OdinInsightUtils.getSwizzleFields(tsOdinArrayType);
                localScope = OdinSymbolTable.from(swizzleFields);
                globalScope = tsOdinRefExpressionType.getSymbolTable();
            } else {
                // TODO do we need to set symbol for every type?
                globalScope = tsOdinRefExpressionType.getSymbolTable();
                localScope = typeSymbols;
            }
            // The resolved polymorphic types must be taken over from type scope
            this.symbolTable.addTypes(localScope);
        } else {
            localScope = this.symbolTable;
            globalScope = this.symbolTable;
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
                        this.type = OdinTypeConverter.convertToTyped(resolveTypeOfDeclaration(refExpression.getIdentifier(), globalScope, declaredIdentifier, odinDeclaration));

                    }
                } else if (symbol.isImplicitlyDeclared()) {
                    // TODO make this a bit less hard coded
                    if (symbol.getName().equals("context")) {
                        Project project = refExpression.getProject();
                        OdinBuiltinSymbolService builtinSymbolService = OdinBuiltinSymbolService.getInstance(project);
                        if (builtinSymbolService != null) {
                            this.type = builtinSymbolService.getContextStructType();
                        }
                    }
                    // Accept only swizzle fields of length one
                    else if (symbol.getSymbolType() == OdinSymbolType.SWIZZLE_FIELD && symbol.getName().length() == 1) {
                        if (tsOdinRefExpressionType instanceof TsOdinArrayType tsOdinArrayType) {
                            this.type = tsOdinArrayType.getElementType();
                        }
                    }
                    else if(symbol.getSymbolType() == OdinSymbolType.BUILTIN_TYPE) {
                        this.type = createBuiltinMetaType(name);
                    }
                }
            } else {
                TsOdinType polyParameter = symbolTable.getType(name);
                if (polyParameter != null) {
                    TsOdinMetaType tsOdinMetaType = new TsOdinMetaType(POLYMORPHIC);
                    tsOdinMetaType.setSymbolTable(symbolTable);
                    tsOdinMetaType.setDeclaration(polyParameter.getDeclaration());
                    tsOdinMetaType.setType(polyParameter.getType());
                    tsOdinMetaType.setDeclaredIdentifier(polyParameter.getDeclaredIdentifier());
                    tsOdinMetaType.setName(name);
                    this.type = tsOdinMetaType;
                } else if (TsOdinBuiltInTypes.RESERVED_TYPES.contains(name)) {
                    this.type = createBuiltinMetaType(name);
                }
            }
            /* TODO: rand.odin causes stack overflow these being the end and the start of the overflow error respectively
             * .resolveTypeOfDeclaration(OdinInferenceEngine.java:498)
             * 	at com.lasagnerd.odin.codeInsight.typeInference.OdinInferenceEngine.visitRefExpression(OdinInferenceEngine.java:128)
             */
        }

        if (refExpression.getType() != null) {
            TsOdinType tsOdinType = OdinTypeResolver.resolveType(symbolTable, refExpression.getType());
            if (this.lhsValuesCount == 2) {
                this.type = createOptionalOkTuple(tsOdinType);
            } else {
                this.type = tsOdinType;
            }
        }
    }

    private static @NotNull TsOdinMetaType createBuiltinMetaType(String name) {
        TsOdinBuiltInType builtInType = TsOdinBuiltInTypes.getBuiltInType(name);
        TsOdinMetaType tsOdinMetaType = new TsOdinMetaType(builtInType.getMetaType());
        tsOdinMetaType.setName(name);
        tsOdinMetaType.setRepresentedType(TsOdinBuiltInTypes.getBuiltInType(name));
        return tsOdinMetaType;
    }

    @Override
    public void visitCompoundLiteralExpression(@NotNull OdinCompoundLiteralExpression o) {
        if (o.getCompoundLiteral() instanceof OdinCompoundLiteralTyped typed) {
            OdinType psiType = typed.getType();
            this.type = OdinTypeResolver.resolveType(this.symbolTable, psiType);
        }
    }

    @Override
    public void visitCallExpression(@NotNull OdinCallExpression o) {
        // Get type of expression. If it is callable, retrieve the return type and set that as result
        TsOdinType tsOdinType = doInferType(symbolTable, o.getExpression());
        if (tsOdinType instanceof TsOdinMetaType tsOdinMetaType) {
            if (tsOdinMetaType.getRepresentedMetaType() == PROCEDURE) {
                TsOdinProcedureType procedureType = (TsOdinProcedureType) OdinTypeResolver.resolveMetaType(tsOdinType.getSymbolTable(), tsOdinMetaType);
                if (!procedureType.getReturnTypes().isEmpty()) {
                    TsOdinProcedureType specializedType = OdinTypeSpecializer
                            .specializeProcedure(symbolTable, o.getArgumentList(), procedureType);
                    if (specializedType.getReturnTypes().size() == 1) {
                        this.type = specializedType.getReturnTypes().getFirst();
                    } else if (specializedType.getReturnTypes().size() > 1) {
                        this.type = new TsOdinTuple(specializedType.getReturnTypes());
                    } else {
                        this.type = TsOdinType.VOID;
                    }
                }
            }

            if (tsOdinMetaType.getRepresentedMetaType() == STRUCT) {
                TsOdinStructType structType = (TsOdinStructType) OdinTypeResolver.resolveMetaType(symbolTable, tsOdinMetaType);
                TsOdinStructType specializedStructType = OdinTypeSpecializer.specializeStructOrGetCached(symbolTable, structType, o.getArgumentList());
                TsOdinMetaType resultType = new TsOdinMetaType(STRUCT);
                resultType.setRepresentedType(specializedStructType);
                this.type = resultType;
            }

            if (tsOdinMetaType.getRepresentedMetaType() == UNION) {
                TsOdinUnionType unionType = (TsOdinUnionType) OdinTypeResolver.resolveMetaType(symbolTable, tsOdinMetaType);
                TsOdinType specializedUnion = OdinTypeSpecializer.specializeUnionOrGetCached(symbolTable, unionType, o.getArgumentList());
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
        TsOdinType tsOdinType = doInferType(symbolTable, expression);

        if(tsOdinType instanceof TsOdinTypeAlias tsOdinTypeAlias) {
            tsOdinType = tsOdinTypeAlias.getBaseType();
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
            this.type = matrixType.getElementType();
        }

        if (tsOdinType instanceof TsOdinMultiPointerType multiPointerType) {
            this.type = multiPointerType.getDereferencedType();
        }
    }

    @Override
    public void visitSliceExpression(@NotNull OdinSliceExpression o) {
        OdinExpression expression = o.getExpression();
        TsOdinType tsOdinType = doInferType(symbolTable, expression);
        if (tsOdinType instanceof TsOdinSliceType) {
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
        var procedureType = o.getProcedureDefinition().getProcedureType();
        TsOdinMetaType tsOdinMetaType = new TsOdinMetaType(PROCEDURE);
        tsOdinMetaType.setType(procedureType);

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
        return TsOdinType.UNKNOWN;
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
        TsOdinType expectedUnionType = TsOdinType.UNKNOWN;
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

    public static TsOdinType resolveTypeOfDeclaration(@NotNull OdinIdentifier identifier,
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

            int index = initializationStatement.getIdentifierList().getDeclaredIdentifierList().indexOf(declaredIdentifier);
            List<OdinExpression> expressionList = Objects
                    .requireNonNull(initializationStatement.getExpressionsList())
                    .getExpressionList();

            int lhsValuesCount = initializationStatement.getIdentifierList().getDeclaredIdentifierList().size();

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
            return TsOdinType.UNKNOWN;
        }

        // If we get a type here, then it's an alias
        if (odinDeclaration instanceof OdinConstantInitializationStatement initializationStatement) {
            if (initializationStatement.getType() != null) {
                OdinType mainType = initializationStatement.getType();
                return OdinTypeResolver.resolveType(parentSymbolTable, mainType);
            }

            int index = initializationStatement.getIdentifierList()
                    .getDeclaredIdentifierList()
                    .indexOf(declaredIdentifier);

            List<OdinExpression> expressionList = initializationStatement
                    .getExpressionsList()
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
                if(tsOdinType instanceof TsOdinMetaType aliasedMetaType) {
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
            return TsOdinType.UNKNOWN;
        }

        if (odinDeclaration instanceof OdinFieldDeclarationStatement fieldDeclarationStatement) {
            return OdinTypeResolver.resolveType(parentSymbolTable, fieldDeclarationStatement.getType());
        }

        if (odinDeclaration instanceof OdinParameterDeclarator parameterDeclaration) {
            return OdinTypeResolver.resolveType(parentSymbolTable, parameterDeclaration.getTypeDefinitionContainer().getType());
        }

        if (odinDeclaration instanceof OdinParameterInitialization parameterInitialization) {
            OdinType type = parameterInitialization.getTypeDefinition();
            if (type != null) {
                return OdinTypeResolver.resolveType(parentSymbolTable, type);
            }

            OdinExpression odinExpression = parameterInitialization.getExpression();
            return doInferType(parentSymbolTable, odinExpression);
        }

        // Meta types
        if (odinDeclaration instanceof OdinProcedureDeclarationStatement procedure) {
            TsOdinMetaType tsOdinMetaType = new TsOdinMetaType(PROCEDURE);
            tsOdinMetaType.setSymbolTable(parentSymbolTable);
            tsOdinMetaType.setDeclaration(procedure);
            tsOdinMetaType.setType(procedure.getProcedureDefinition().getProcedureType());
            tsOdinMetaType.setDeclaredIdentifier(declaredIdentifier);
            tsOdinMetaType.setName(declaredIdentifier.getName());
            return tsOdinMetaType;
        }

        if (odinDeclaration instanceof OdinStructDeclarationStatement structDeclarationStatement) {
            TsOdinMetaType tsOdinMetaType = new TsOdinMetaType(STRUCT);
            tsOdinMetaType.setSymbolTable(parentSymbolTable);
            tsOdinMetaType.setDeclaration(structDeclarationStatement);
            tsOdinMetaType.setType(structDeclarationStatement.getStructType());
            tsOdinMetaType.setDeclaredIdentifier(declaredIdentifier);
            tsOdinMetaType.setName(declaredIdentifier.getName());
            return tsOdinMetaType;
        }

        if (odinDeclaration instanceof OdinEnumDeclarationStatement enumDeclarationStatement) {
            TsOdinMetaType tsOdinMetaType = new TsOdinMetaType(ENUM);
            tsOdinMetaType.setSymbolTable(parentSymbolTable);
            tsOdinMetaType.setDeclaration(enumDeclarationStatement);
            tsOdinMetaType.setType(enumDeclarationStatement.getEnumType());
            tsOdinMetaType.setDeclaredIdentifier(declaredIdentifier);
            tsOdinMetaType.setName(declaredIdentifier.getName());
            return tsOdinMetaType;
        }

        if (odinDeclaration instanceof OdinUnionDeclarationStatement unionDeclarationStatement) {
            TsOdinMetaType tsOdinMetaType = new TsOdinMetaType(UNION);
            tsOdinMetaType.setSymbolTable(parentSymbolTable);
            tsOdinMetaType.setDeclaration(unionDeclarationStatement);
            tsOdinMetaType.setType(unionDeclarationStatement.getUnionType());
            tsOdinMetaType.setDeclaredIdentifier(declaredIdentifier);
            tsOdinMetaType.setName(declaredIdentifier.getName());
            return tsOdinMetaType;
        }

        if (odinDeclaration instanceof OdinPolymorphicType polymorphicType) {
            TsOdinMetaType tsOdinMetaType = new TsOdinMetaType(POLYMORPHIC);
            tsOdinMetaType.setSymbolTable(parentSymbolTable);
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
                return OdinTypeResolver.resolveType(parentSymbolTable, enumDeclaredIdentifier, enumDeclarationStatement, enumType);
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
                TsOdinType tsOdinType = inferType(parentSymbolTable, expression);
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
                if (expression instanceof OdinRangeExclusiveExpression || expression instanceof OdinRangeInclusiveExpression) {
                    return tsOdinType;
                }
                return TsOdinType.UNKNOWN;
            }

        }

        if (odinDeclaration instanceof OdinSwitchTypeVariableDeclaration) {
            OdinSwitchInBlock switchInBlock = PsiTreeUtil.getParentOfType(odinDeclaration, OdinSwitchInBlock.class, true);
            if (switchInBlock != null) {
                TsOdinType tsOdinType = inferType(parentSymbolTable, switchInBlock.getExpression());

                OdinSwitchCase caseBlock = PsiTreeUtil.getParentOfType(identifier, OdinSwitchCase.class, true);
                if (caseBlock != null) {
                    @NotNull List<OdinExpression> expressionList = caseBlock.getExpressionList();

                    if (expressionList.size() == 1) {
                        OdinExpression odinExpression = expressionList.getFirst();
                        TsOdinType caseType = inferType(parentSymbolTable, odinExpression);
                        if (caseType instanceof TsOdinMetaType metaType) {
                            return OdinTypeResolver.resolveMetaType(parentSymbolTable, metaType);
                        }
                    }
                }
                return tsOdinType;
            }
        }

        if (odinDeclaration instanceof OdinProcedureOverloadDeclarationStatement) {
            TsOdinMetaType tsOdinMetaType = new TsOdinMetaType(PROCEDURE_OVERLOAD);
            tsOdinMetaType.setDeclaredIdentifier(declaredIdentifier);
            tsOdinMetaType.setName(declaredIdentifier.getName());
            tsOdinMetaType.setSymbolTable(parentSymbolTable);
            tsOdinMetaType.setDeclaration(odinDeclaration);
            return tsOdinMetaType;
        }

        return TsOdinType.UNKNOWN;
    }

    @SuppressWarnings("unused")
    public static TsOdinType createReferenceType(TsOdinType dereferencedType, boolean isReference) {
        return dereferencedType;
    }

    /**
     * Types are expected at in/out nodes such as return statements, case blocks, arguments, assignments of typed variables, etc.
     *
     * @param symbolTable The symbol table used for resolving the expected type
     * @param expression  The expression for which we want to find out, whether it is expected to have a certain type
     * @return The expected type
     */
    public static TsOdinType inferExpectedType(OdinSymbolTable symbolTable, OdinExpression expression) {
        PsiElement parent = expression.getParent();

        if (parent instanceof OdinReturnStatement returnStatement) {
            OdinProcedureDefinition procedureDefinition = PsiTreeUtil.getParentOfType(returnStatement, OdinProcedureDefinition.class);
            if (procedureDefinition != null) {
                int position = returnStatement.getExpressionList().indexOf(expression);
                OdinReturnParameters returnParameters = procedureDefinition.getProcedureType().getReturnParameters();
                if (returnParameters != null) {
                    OdinParamEntries paramEntries = returnParameters.getParamEntries();
                    if (paramEntries != null) {
                        if (paramEntries.getParamEntryList().size() > position) {
                            OdinParamEntry paramEntry = paramEntries.getParamEntryList().get(position);
                            return OdinTypeResolver.resolveType(symbolTable, paramEntry.getParameterDeclaration().getTypeDefinition());
                        }
                    } else if (position == 0) {
                        OdinType psiType = returnParameters.getType();
                        if (psiType != null) {
                            return OdinTypeResolver.resolveType(symbolTable, psiType);
                        }
                    }
                }
            }
        }

        if (parent instanceof OdinRhsExpressions rhsExpressions) {
            int index = rhsExpressions.getExpressionList().indexOf(expression);
            PsiElement grandParent = rhsExpressions.getParent();

            if (grandParent instanceof OdinAssignmentStatement statement) {

                List<OdinExpression> lhsExpressions = statement.getLhsExpressions().getExpressionList();
                if (lhsExpressions.size() > index) {
                    OdinExpression rhsExpression = lhsExpressions.get(index);
                    return OdinInferenceEngine.inferType(symbolTable, rhsExpression);
                }
            }
        }

        return TsOdinType.VOID;
    }

    @Override
    public void visitOrBreakExpression(@NotNull OdinOrBreakExpression o) {
        OdinExpression expression = o.getExpression();
        this.type = inferType(symbolTable, expression);
    }

    @Override
    public void visitOrContinueExpression(@NotNull OdinOrContinueExpression o) {
        OdinExpression expression = o.getExpression();
        this.type = inferType(symbolTable, expression);
    }

    @Override
    public void visitOrReturnExpression(@NotNull OdinOrReturnExpression o) {
        OdinExpression expression = o.getExpression();
        TsOdinType tsOdinType = inferType(symbolTable, expression);
        if (tsOdinType instanceof TsOdinTuple tsOdinTuple) {
            if (tsOdinTuple.getTypes().size() > 1) {
                this.type = tsOdinTuple.getTypes().getFirst();
            }
        }
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