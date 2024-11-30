package com.lasagnerd.odin.codeInsight.evaluation;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiUtilCore;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.symbols.OdinSdkService;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbol;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolTable;
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
    public static Logger LOG = Logger.getInstance(OdinExpressionEvaluator.class);

    EvOdinValue value;
    OdinSymbolTable symbolTable;

    public static EvOdinValue evaluate(OdinExpression expression) {
        try {
            OdinExpressionEvaluator expressionEvaluator = new OdinExpressionEvaluator();
            expression.accept(expressionEvaluator);
            if (expressionEvaluator.value == null) {
                return TsOdinBuiltInTypes.NULL;
            } else if (expressionEvaluator.value.type == null && expressionEvaluator.value.value == null) {
                return TsOdinBuiltInTypes.NULL;
            }

            return expressionEvaluator.value;
        } catch (StackOverflowError e) {
            OdinInsightUtils.logStackOverFlowError(expression, LOG);
        }
        return TsOdinBuiltInTypes.NULL;
    }

//    public static EvOdinValue evaluate(OdinExpression expression) {
//        return evaluate( expression);
//    }

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
            EvOdinValue refExpressionValue = evaluate(o.getExpression());
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

        OdinSymbol symbol = o.getIdentifier().getReferencedSymbol();
        if (symbol != null) {
            this.value = evaluateConstantDeclaration(o.getProject(), symbol, localSymbolTable, expressionType);
        } else {
            if (o.getExpression() == null) {
                String name = o.getIdentifier().getText();
                this.value = TsOdinBuiltInTypes.BUILTIN_IDENTIFIERS.getOrDefault(name, TsOdinBuiltInTypes.NULL);
            }
        }
    }

    public static EvOdinValue evaluateConstantDeclaration(@NotNull Project project,
                                                          OdinSymbol symbol,
                                                          OdinSymbolTable symbolTable,
                                                          TsOdinType expressionType) {
        if (symbol == null) {
            return TsOdinBuiltInTypes.NULL;
        }

        OdinDeclaration declaration = symbol.getDeclaration();
        boolean isConstantDeclaration = !(declaration instanceof OdinConstantDeclaration) && !(declaration instanceof OdinEnumValueDeclaration);
        if (isConstantDeclaration) {
            return TsOdinBuiltInTypes.NULL;
        }

        if (symbol.isBuiltin() && symbol.getName().startsWith("ODIN_")) {
            OdinSdkService sdkService = OdinSdkService.getInstance(project);
            EvOdinValue builtinValue = sdkService.getValue(symbol.getName());
            if (builtinValue != null) {
                return builtinValue;
            }
        }
        PsiNamedElement declaredIdentifier = symbol.getDeclaredIdentifier();
        if (!(declaredIdentifier instanceof OdinDeclaredIdentifier odinDeclaredIdentifier)) {
            return TsOdinBuiltInTypes.NULL;
        }

        if (declaration instanceof OdinConstantInitializationStatement constantInitializationStatement) {

            TsOdinMetaType declaredType = OdinInferenceEngine.findMetaType(
                    symbolTable,
                    odinDeclaredIdentifier,
                    constantInitializationStatement);

            if (declaredType != null) {
                // This is a type declaration, set EVOdinValue accordingly
                return new EvOdinValue(declaredType.representedType(), declaredType);
            }

            // This is not a type
            int index = constantInitializationStatement
                    .getDeclaredIdentifierList()
                    .indexOf(declaredIdentifier);

            List<OdinExpression> expressionList = constantInitializationStatement
                    .getExpressionList();

            if (expressionList.size() > index) {
                OdinExpression expression = expressionList.get(index);
                return evaluate(expression);
            }
        } else if (declaration instanceof OdinEnumValueDeclaration && expressionType instanceof TsOdinEnumType enumType) {
            EvEnumValue enumValue = getEnumValue(enumType, declaredIdentifier.getName());
            if (enumValue == null) return TsOdinBuiltInTypes.NULL;
            return new EvOdinValue(enumValue, enumType);
        }

        return TsOdinBuiltInTypes.NULL;
    }

    private static EvEnumValue getEnumValue(TsOdinEnumType enumType, @Nullable String name) {
        EvOdinValue numericValue = TsOdinBuiltInTypes.NULL;
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
        OdinEnumValueDeclaration enumValueDeclaration = enumBody.getEnumValueDeclarationList().stream().filter(
                e -> Objects.equals(e.getDeclaredIdentifier().getName(), name)
        ).findFirst().orElse(null);

        if (enumValueDeclaration == null)
            return null;

        if (enumValueDeclaration.getExpression() != null) {
            numericValue = evaluate(enumValueDeclaration.getExpression());
        } else {

            // Go back until you find a set expression.
            List<OdinEnumValueDeclaration> enumValueDeclarations = enumBody.getEnumValueDeclarationList();
            int currentIndex = enumValueDeclarations.indexOf(enumValueDeclaration);
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
        TsOdinType tsOdinType = o.getInferredType();
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
            text = text.replaceAll("^0h", "0x");
            text = text.replaceAll("^0o", "0");
            text = text.replaceAll("_", "");

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
        EvOdinValue left = evaluate(o.getLeft());
        if (o.getRight() != null) {
            EvOdinValue right = evaluate(o.getRight());
            if (left.type == null || right.type == null) {
                return;
            }
            TsOdinType tsOdinType = OdinTypeConverter.inferTypeOfSymmetricalBinaryExpression(left.type, right.type);
            if (!tsOdinType.isUnknown()) {
                if (TsOdinBuiltInTypes.getIntegerTypes().contains(tsOdinType)) {
                    this.value = evaluateBinaryIntegerOperation(operatorType, left, right, tsOdinType);
                } else if (TsOdinBuiltInTypes.getStringTypes().contains(tsOdinType)) {
                    this.value = evaluateBinaryStringOperation(operatorType, left, right, tsOdinType);
                } else if (tsOdinType == TsOdinBuiltInTypes.BOOL) {
                    this.value = evaluateBinaryBoolOperation(operatorType, left.asBool(), right.asBool(), tsOdinType);
                } else {
                    if (left.isEnum() || right.isEnum()) {
                        this.value = evaluateBinaryEnumOperation(operatorType, left, right);
                    }
                }

            } else {
                if (left.isEnum() || right.isEnum()) {
                    this.value = evaluateBinaryEnumOperation(operatorType, left, right);
                }
            }
        }
    }

    @Override
    public void visitUnaryExpression(@NotNull OdinUnaryExpression o) {
        IElementType operatorType = PsiUtilCore.getElementType(o.getOperator());
        EvOdinValue value = evaluate(o.getExpression());

        TsOdinType tsOdinType = value.getType();
        if (tsOdinType.isUnknown())
            return;

        TsOdinType baseType = tsOdinType.typed().baseType(true);
        if (baseType.isInteger()) {
            this.value = evaluateUnaryIntegerOperation(operatorType, value);
        }

        if (baseType.isBool()) {
            this.value = evaluateUnaryBoolOperation(operatorType, value);
        }
    }

    private EvOdinValue evaluateUnaryBoolOperation(IElementType operatorType, EvOdinValue value) {
        EvOdinValue newValue = TsOdinBuiltInTypes.NULL;
        Boolean n = newValue.asBool();
        if (n != null) {
            Boolean result = null;
            if (operatorType == OdinTypes.NOT) {
                result = !n;
            }

            if (result != null) {
                newValue = new EvOdinValue(result, value.getType());
            }
        }
        return newValue;
    }

    private static @NotNull EvOdinValue evaluateUnaryIntegerOperation(IElementType operatorType, EvOdinValue value) {
        EvOdinValue newValue = TsOdinBuiltInTypes.NULL;
        Long n = newValue.toLong();
        if (n != null) {
            Long result = null;
            if (operatorType == OdinTypes.PLUS) {
                result = n;
            } else if (operatorType == OdinTypes.MINUS) {
                result = -n;
            } else if (operatorType == OdinTypes.TILDE) {
                result = ~n;
            }

            if (result != null) {
                newValue = new EvOdinValue(result, value.getType());
            }
        }
        return newValue;
    }

    private EvOdinValue evaluateBinaryBoolOperation(IElementType operatorType, Boolean left, Boolean right, TsOdinType tsOdinType) {
        if (left == null || right == null)
            return TsOdinBuiltInTypes.NULL;

        Boolean result = null;
        if (operatorType == OdinTypes.ANDAND) {
            result = left && right;
        } else if (operatorType == OdinTypes.OROR) {
            result = left || right;
        }

        if (result == null)
            return TsOdinBuiltInTypes.NULL;

        return new EvOdinValue(result, TsOdinBuiltInTypes.BOOL);
    }

    private EvOdinValue evaluateBinaryStringOperation(IElementType operatorType, EvOdinValue left, EvOdinValue right, TsOdinType tsOdinType) {
        Object result = null;

        String leftString = left.asString();
        String rightString = right.asString();

        if (leftString == null || rightString == null)
            return TsOdinBuiltInTypes.NULL;

        else if (operatorType == OdinTypes.EQEQ) {
            result = Objects.equals(leftString, rightString);
        } else if (operatorType == OdinTypes.NEQ) {
            result = !Objects.equals(leftString, rightString);
        } else if (operatorType == OdinTypes.PLUS) {
            result = leftString + rightString;
        }

        if (result == null)
            return TsOdinBuiltInTypes.NULL;

        TsOdinType expressionType = left.getType();
        if (OdinPsiUtil.COMPARISON_OPERATORS.contains(operatorType)) {
            expressionType = TsOdinBuiltInTypes.BOOL;
        }

        return new EvOdinValue(result, expressionType);
    }

    private static @NotNull EvOdinValue evaluateBinaryEnumOperation(IElementType operatorType, EvOdinValue left, EvOdinValue right) {
        EvOdinValue value = TsOdinBuiltInTypes.NULL;
        if (OdinTypeChecker.checkTypesStrictly(left.getType(), right.getType())) {
            Object result = null;
            EvEnumValue leftEnum = left.asEnum();
            EvEnumValue rightEnum = right.asEnum();

            if (leftEnum == null || rightEnum == null) {
                return TsOdinBuiltInTypes.NULL;
            }

            if (operatorType == OdinTypes.EQEQ) {
                result = leftEnum.getName().equals(rightEnum.getName());
            }

            if (operatorType == OdinTypes.NEQ) {
                result = !leftEnum.getName().equals(rightEnum.getName());
            }

            if (result == null)
                return TsOdinBuiltInTypes.NULL;

            TsOdinType expressionType = left.getType();
            if (OdinPsiUtil.COMPARISON_OPERATORS.contains(operatorType)) {
                expressionType = TsOdinBuiltInTypes.BOOL;
            }

            value = new EvOdinValue(result, expressionType);
        }
        return value;
    }

    private static @NotNull EvOdinValue evaluateBinaryIntegerOperation(IElementType operatorType, EvOdinValue left, EvOdinValue right, TsOdinType commonType) {
        EvOdinValue value = TsOdinBuiltInTypes.NULL;
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

            if (result == null)
                return TsOdinBuiltInTypes.NULL;

            TsOdinType expressionType = commonType;
            if (OdinPsiUtil.COMPARISON_OPERATORS.contains(operatorType)) {
                expressionType = TsOdinBuiltInTypes.BOOL;
            }

            value = new EvOdinValue(result, expressionType);
        }
        return value;
    }

}
