package com.lasagnerd.odin.codeInsight.evaluation;

import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiUtilCore;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.symbols.OdinSdkService;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbol;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolTable;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolTableResolver;
import com.lasagnerd.odin.codeInsight.typeInference.OdinInferenceEngine;
import com.lasagnerd.odin.codeInsight.typeInference.OdinTypeChecker;
import com.lasagnerd.odin.codeInsight.typeInference.OdinTypeConverter;
import com.lasagnerd.odin.codeInsight.typeSystem.*;
import com.lasagnerd.odin.lang.psi.*;
import groovy.json.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * This class evaluates compile time expressions, such as where-constraints and constant expressions
 */
public class OdinExpressionEvaluator extends OdinVisitor {
    EvOdinValue value;
    OdinSymbolTable symbolTable;

    public static EvOdinValue evaluate(OdinSymbolTable symbolTable, OdinExpression expression) {
        OdinExpressionEvaluator expressionEvaluator = new OdinExpressionEvaluator();
        expressionEvaluator.symbolTable = symbolTable;
        expression.accept(expressionEvaluator);
        return expressionEvaluator.value == null? EvOdinValue.NULL : expressionEvaluator.value;
    }

    public static EvOdinValue evaluate(OdinExpression expression) {
        OdinSymbolTable symbolTable = OdinSymbolTableResolver.computeSymbolTable(expression);
        return evaluate(symbolTable, expression);
    }

    @Override
    public void visitRefExpression(@NotNull OdinRefExpression o) {
        OdinSymbolTable localSymbolTable;
        if (o.getType() != null)
            return;

        if (o.getIdentifier() == null)
            return;

        // Only refExpression allowed
        TsOdinType expressionType = TsOdinBuiltInTypes.UNKNOWN;
        if (o.getExpression() instanceof OdinRefExpression) {
            EvOdinValue refExpressionValue = evaluate(symbolTable, o.getExpression());
            expressionType = refExpressionValue.asBaseType();
            if (expressionType instanceof TsOdinPackageReferenceType ||
                    expressionType instanceof TsOdinEnumType) {
                localSymbolTable = OdinInsightUtils.getTypeElements(o.getProject(), expressionType);
            } else {
                return;
            }

        } else if (o.getExpression() != null) {
            return;
        } else {
            localSymbolTable = symbolTable;
        }

        OdinSymbol symbol = localSymbolTable.getSymbol(o.getIdentifier().getText());

        if (symbol == null || !(symbol.getDeclaration() instanceof OdinConstantDeclaration declaration))
            return;

        if (symbol.isBuiltin() && symbol.getName().startsWith("ODIN_")) {
            OdinSdkService sdkService = OdinSdkService.getInstance(o.getProject());
            EvOdinValue builtinValue = sdkService.getValue(symbol.getName());
            if (builtinValue != null) {
                this.value = builtinValue;
                return;
            }
        }
        PsiNamedElement declaredIdentifier = symbol.getDeclaredIdentifier();
        if (!(declaredIdentifier instanceof OdinDeclaredIdentifier odinDeclaredIdentifier)) {
            return;
        }

        if (declaration instanceof OdinConstantInitializationStatement constantInitializationStatement) {

            TsOdinMetaType declaredType = OdinInferenceEngine.findMetaType(
                    localSymbolTable,
                    odinDeclaredIdentifier,
                    constantInitializationStatement);

            if (declaredType != null) {
                // This is a type declaration, set EVOdinValue accordingly
                this.value = new EvOdinValue(declaredType.representedType(), declaredType);
                return;
            }

            // This is not a type
            int index = constantInitializationStatement.getIdentifierList()
                    .getDeclaredIdentifierList()
                    .indexOf(declaredIdentifier);

            List<OdinExpression> expressionList = constantInitializationStatement
                    .getExpressionList();

            if (expressionList.size() > index) {
                OdinExpression expression = expressionList.get(index);
                this.value = evaluate(symbolTable, expression);
            }
        } else if (declaration instanceof OdinEnumValueDeclaration && expressionType instanceof TsOdinEnumType enumType) {
            EvEnumValue enumValue = getEnumValue(enumType, declaredIdentifier.getName());
            if (enumValue == null) return;
            this.value = new EvOdinValue(enumValue, enumType);
        }
    }

    private static EvEnumValue getEnumValue(TsOdinEnumType enumType, @Nullable String name) {
        EvOdinValue numericValue = EvOdinValue.NULL;
        TsOdinType backingType = TsOdinBuiltInTypes.I32;
        if (enumType.getBackingType() != null) {
            backingType = enumType.getBackingType();
        }

        OdinType declaredType = enumType.baseType(true).getPsiType();
        if (!(declaredType instanceof OdinEnumType psiEnumType)) {
            return null;
        }
        OdinEnumBlock enumBlock = psiEnumType.getEnumBlock();
        if (enumBlock == null)
            return null;
        OdinEnumBody enumBody = enumBlock.getEnumBody();
        if (enumBody == null)
            return null;
        OdinEnumValueDeclaration enumValueDeclaration1 = enumBody.getEnumValueDeclarationList().stream().filter(
                e -> Objects.equals(e.getDeclaredIdentifier().getName(), name)
        ).findFirst().orElse(null);

        if (enumValueDeclaration1 == null)
            return null;

        if (enumValueDeclaration1.getExpression() != null) {
            numericValue = evaluate(enumValueDeclaration1.getExpression());
        } else {

            // Go back until you find a set expression.
            List<OdinEnumValueDeclaration> enumValueDeclarations = enumBody.getEnumValueDeclarationList();
            int currentIndex = enumValueDeclarations.indexOf(enumValueDeclaration1);
            for (int index = currentIndex - 1; index > 0; index--) {
                OdinEnumValueDeclaration previousDeclaration = enumValueDeclarations.get(index);
                if (previousDeclaration.getExpression() != null) {
                    EvOdinValue previousValue = evaluate(previousDeclaration.getExpression());

                    // Enum may be set to other enum value
                    EvEnumValue previousEnumValue = previousValue.asEnum();
                    if (previousEnumValue != null) {
                        numericValue = new EvOdinValue(previousEnumValue.getValue() + 1, backingType);
                    } else {
                        // ... or to a number
                        Integer previousIntValue = previousValue.asInt();
                        if (previousIntValue != null) {
                            numericValue = new EvOdinValue(previousIntValue + 1, backingType);
                        }
                    }
                }
            }

            // If no expression was set on previous enums, set the current index as value
            if (numericValue.isNull()) {
                numericValue = new EvOdinValue(currentIndex, backingType);
            }
        }
        return new EvEnumValue(name, numericValue.toInt(0));
    }

    @Override
    public void visitImplicitSelectorExpression(@NotNull OdinImplicitSelectorExpression o) {
        TsOdinType tsOdinType = OdinInferenceEngine.inferType(symbolTable, o);
        if (tsOdinType instanceof TsOdinEnumType tsOdinEnumType) {
            EvEnumValue enumValue = getEnumValue(tsOdinEnumType, o.getIdentifier().getText());
            this.value = new EvOdinValue(enumValue, tsOdinType);
        }
    }

    @Override
    public void visitLiteralExpression(@NotNull OdinLiteralExpression o) {
        o.getBasicLiteral().accept(this);
    }

    @Override
    public void visitNumericLiteral(@NotNull OdinNumericLiteral o) {
        if (o.getIntegerDecLiteral() != null) {
            String text = o.getIntegerDecLiteral().getText();
            text = text.replaceAll("^0h(_*)", "0x");
            text = text.replaceAll("^0b(_*)", "0b");
            text = text.replaceAll("^0o(_*)", "0");

            long intVal = Long.parseLong(text);
            this.value = new EvOdinValue(intVal, TsOdinBuiltInTypes.UNTYPED_INT);
        }
    }

    @Override
    public void visitStringLiteral(@NotNull OdinStringLiteral o) {
        if (o.getDqStringLiteral() != null) {
            String unquotedString = StringUtil.unquoteString(o.getText());
            String value = StringEscapeUtils.unescapeJava(unquotedString);
            this.value = new EvOdinValue(value, TsOdinBuiltInTypes.UNTYPED_STRING);
        }

        if (o.getSqStringLiteral() != null) {
            String unquotedString = StringUtil.unquoteString(o.getText(), '\'');
            String value = StringEscapeUtils.unescapeJava(unquotedString);
            if (!value.isEmpty()) {
                this.value = new EvOdinValue(value.charAt(0), TsOdinBuiltInTypes.UNTYPED_RUNE);
            }
        }

        if (o.getRawStringLiteral() != null) {
            String value = StringUtil.unquoteString(o.getText(), '`');
            this.value = new EvOdinValue(value, TsOdinBuiltInTypes.UNTYPED_STRING);
        }
    }

    @Override
    public void visitBinaryExpression(@NotNull OdinBinaryExpression o) {
        IElementType operatorType = PsiUtilCore.getElementType(o.getOperator());
        EvOdinValue left = evaluate(symbolTable, o.getLeft());
        if (o.getRight() != null) {
            EvOdinValue right = evaluate(symbolTable, o.getRight());

            TsOdinType tsOdinType = OdinTypeConverter.inferTypeOfArithmeticExpression(left.type, right.type);
            if (!tsOdinType.isUnknown()) {
                if (TsOdinBuiltInTypes.getIntegerTypes().contains(tsOdinType)) {
                    this.value = evaluateBinaryIntegerOperation(operatorType, left, right, tsOdinType);
                } else {
                    TsOdinBuiltInTypes.getFloatingPointTypes();
                }
            } else {
                if (left.isEnum() || right.isEnum()) {
                    this.value = evaluateBinaryEnumOperation(left, right, operatorType);
                }
            }
        }
    }

    private static @NotNull EvOdinValue evaluateBinaryEnumOperation(EvOdinValue left, EvOdinValue right, IElementType operatorType) {
        EvOdinValue value = EvOdinValue.NULL;
        if (OdinTypeChecker.checkTypesStrictly(left.getType(), right.getType())) {
            Object result = null;
            if (operatorType == OdinTypes.EQEQ) {
                result = left.asEnum().getName().equals(right.asEnum().getName());
            }

            if (operatorType == OdinTypes.NEQ) {
                result = !left.asEnum().getName().equals(right.asEnum().getName());
            }

            TsOdinType expressionType = left.getType();
            if (OdinPsiUtil.COMPARISON_OPERATORS.contains(operatorType)) {
                expressionType = TsOdinBuiltInTypes.BOOL;
            }

            value = new EvOdinValue(result, expressionType);
        }
        return value;
    }

    private static @NotNull EvOdinValue evaluateBinaryIntegerOperation(IElementType operatorType, EvOdinValue left, EvOdinValue right, TsOdinType commonType) {
        EvOdinValue value = EvOdinValue.NULL;
        Long leftLong = left.toLong();
        Long rightLong = right.toLong();
        if (leftLong != null && right.asLong() != null) {
            Object result = null;

            if (operatorType == OdinTypes.PLUS) {
                result = leftLong + rightLong;
            } else if (operatorType == OdinTypes.MINUS) {
                result = leftLong - rightLong;
            } else if (operatorType == OdinTypes.STAR) {
                result = leftLong * rightLong;
            } else if (operatorType == OdinTypes.DIV) {
                if (rightLong != 0) {
                    result = leftLong / rightLong;
                }
            } else if (operatorType == OdinTypes.MOD) {
                if (rightLong != 0) {
                    result = leftLong % rightLong;
                }
            } else if (operatorType == OdinTypes.REMAINDER) {
                if (rightLong != 0) {
                    result = leftLong - rightLong * Math.floorDiv(leftLong, rightLong);
                }
            } else if (operatorType == OdinTypes.OR) {
                result = leftLong | rightLong;
            } else if (operatorType == OdinTypes.AND) {
                result = leftLong & rightLong;
            } else if (operatorType == OdinTypes.ANDNOT) {
                result = ~(leftLong & rightLong);
            } else if (operatorType == OdinTypes.TILDE) {
                result = leftLong ^ rightLong;
            } else if (operatorType == OdinTypes.EQEQ) {
                result = leftLong.equals(rightLong);
            } else if (operatorType == OdinTypes.NEQ) {
                result = !leftLong.equals(rightLong);
            } else if (operatorType == OdinTypes.LT) {
                result = leftLong < rightLong;
            } else if (operatorType == OdinTypes.LTE) {
                result = leftLong <= rightLong;
            } else if (operatorType == OdinTypes.GT) {
                result = leftLong > rightLong;
            } else if (operatorType == OdinTypes.GTE) {
                result = leftLong >= rightLong;
            }


            TsOdinType expressionType = commonType;
            if (OdinPsiUtil.COMPARISON_OPERATORS.contains(operatorType)) {
                expressionType = TsOdinBuiltInTypes.BOOL;
            }

            value = new EvOdinValue(result, expressionType);
        }
        return value;
    }

}
