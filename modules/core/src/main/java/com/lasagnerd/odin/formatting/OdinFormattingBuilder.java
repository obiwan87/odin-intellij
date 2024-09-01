package com.lasagnerd.odin.formatting;

import com.intellij.formatting.*;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.tree.TokenSet;
import com.lasagnerd.odin.formatting.block.OdinFormatterBlock;
import com.lasagnerd.odin.lang.OdinLanguage;
import com.lasagnerd.odin.lang.psi.OdinTypes;
import org.jetbrains.annotations.NotNull;

public class OdinFormattingBuilder implements FormattingModelBuilder {
    private static SpacingBuilder createSpaceBuilder(CodeStyleSettings settings) {
//        CommonCodeStyleSettings odinSettings = settings.getCommonSettings(OdinLanguage.INSTANCE.getID());

        return new SpacingBuilder(settings, OdinLanguage.INSTANCE)

                .around(OdinTypes.ASSIGNMENT_OPERATOR)
                .spaces(1)

                .after(OdinTypes.BLOCK_START)
                .lineBreakInCode()

                .before(OdinTypes.BLOCK_END)
                .lineBreakInCode()

                .between(OdinTypes.ELSE, OdinTypes.STATEMENT_BODY)
                .spacing(1, 1, 0, false, 0)

                .withinPairInside(OdinTypes.CONDITION, OdinTypes.BLOCK, OdinTypes.CONDITIONAL_STATEMENT)
                .spacing(1, 1, 0, false, 0)

                .withinPairInside(OdinTypes.SWITCH, OdinTypes.SWITCH_BODY, OdinTypes.SWITCH_STATEMENT)
                .spacing(1, 1, 0, false, 0)

                // TODO re-visit formatting
//                .withinPairInside(OdinTypes.FOR_HEAD, OdinTypes.BLOCK, OdinTypes.FOR_STATEMENT)
//                .spacing(1, 1, 0, false, 0)

                .withinPair(OdinTypes.PROCEDURE_TYPE, OdinTypes.PROCEDURE_BODY)
                .spacing(1, 1, 0, false, 0)

                .between(OdinTypes.ELSE, OdinTypes.IF_BLOCK)
                .spacing(1, 1, 0, false, 0)

                .between(OdinTypes.COLON, OdinTypes.EQ)
                .spaces(0)

                .between(OdinTypes.COLON, OdinTypes.COLON)
                .spaces(0)

                .before(OdinTypes.COLON_OPENING)
                .spaces(1)

                .afterInside(OdinTypes.COLON, OdinTypes.FIELD_DECLARATION_STATEMENT)
                .spaces(1)

                .afterInside(OdinTypes.EQ, OdinTypes.VARIABLE_INITIALIZATION_STATEMENT)
                .spaces(1)

                .aroundInside(OdinTypes.TYPE_DEFINITION_EXPRESSION, OdinTypes.VARIABLE_INITIALIZATION_STATEMENT)
                .spaces(1)

                .aroundInside(OdinTypes.TYPE_DEFINITION_EXPRESSION, OdinTypes.CONSTANT_INITIALIZATION_STATEMENT)
                .spaces(1)

                .after(OdinTypes.COLON_CLOSING)
                .spaces(1)

                .around(OdinTypes.PLUS)
                .spaces(1)

                .after(OdinTypes.COMMA)
                .spaces(1)

                .aroundInside(OdinTypes.EQ, OdinTypes.COMPOUND_VALUE_BODY)
                .spaces(1)

                .aroundInside(OdinTypes.EQ, OdinTypes.ENUM_BODY)
                .spaces(1)

                .afterInside(OdinTypes.COMPOUND_VALUE_START, OdinTypes.COMPOUND_VALUE)
                .spaces(1)

                .beforeInside(OdinTypes.COMPOUND_VALUE_END, OdinTypes.COMPOUND_VALUE)
                .spaces(1)

                .aroundInside(BINARY_OPERATORS, BINARY_EXPRESSIONS)
                .spaces(1)

                .between(OdinTypes.IMPORT_DECLARATION_STATEMENT, OdinTypes.IMPORT_DECLARATION_STATEMENT)
                .spacing(0, 0, 1, false, 0)

                .withinPair(OdinTypes.PACKAGE_DECLARATION, OdinTypes.IMPORT_DECLARATION_STATEMENT)
                .spacing(0, 0, 2, false, 0)

                .withinPair(OdinTypes.IMPORT_DECLARATION_STATEMENT, OdinTypes.FILE_SCOPE_STATEMENT_LIST)
                .spacing(0, 0, 2, false, 0)
                ;
    }

    public static final TokenSet BINARY_EXPRESSIONS = TokenSet.create(
            OdinTypes.AND_EXPRESSION,
            OdinTypes.OR_EXPRESSION,
            OdinTypes.LT_EXPRESSION,
            OdinTypes.LTE_EXPRESSION,
            OdinTypes.GT_EXPRESSION,
            OdinTypes.GTE_EXPRESSION,
            OdinTypes.NEQ_EXPRESSION,
            OdinTypes.EQEQ_EXPRESSION,
            OdinTypes.IN_EXPRESSION,
            OdinTypes.NOT_IN_EXPRESSION,

            OdinTypes.BITWISE_AND_EXPRESSION,
            OdinTypes.BITWISE_OR_EXPRESSION,
            OdinTypes.BITWISE_AND_NOT_EXPRESSION,
            OdinTypes.BITWISE_XOR_EXPRESSION,
            OdinTypes.LSHIFT_EXPRESSION,
            OdinTypes.RSHIFT_EXPRESSION,

            OdinTypes.RANGE_EXCLUSIVE_EXPRESSION,
            OdinTypes.RANGE_INCLUSIVE_EXPRESSION,

            OdinTypes.ADD_EXPRESSION,
            OdinTypes.MUL_EXPRESSION,
            OdinTypes.DIV_EXPRESSION,
            OdinTypes.REMAINDER_EXPRESSION,
            OdinTypes.MOD_EXPRESSION,
            OdinTypes.SUB_EXPRESSION,

            OdinTypes.OR_ELSE_EXPRESSION
    );
    private static final TokenSet BINARY_OPERATORS = TokenSet.create(
            OdinTypes.AND,
            OdinTypes.OROR,
            OdinTypes.EQ,
            OdinTypes.NEQ,
            OdinTypes.LT,
            OdinTypes.STAR,
            OdinTypes.DIV,
            OdinTypes.MOD,
            OdinTypes.REMAINDER,
            OdinTypes.PLUS,
            OdinTypes.MINUS,
            OdinTypes.AND,
            OdinTypes.PIPE,
            OdinTypes.TILDE,
            OdinTypes.ANDNOT,
            OdinTypes.ANDAND,
            OdinTypes.OROR,
            OdinTypes.OR_ELSE,
            OdinTypes.IN,
            OdinTypes.NOT_IN,
            OdinTypes.LT,
            OdinTypes.GT,
            OdinTypes.LTE,
            OdinTypes.GTE,
            OdinTypes.EQEQ,
            OdinTypes.NEQ,
            OdinTypes.LSHIFT,
            OdinTypes.RSHIFT,
            OdinTypes.RANGE_INCLUSIVE,
            OdinTypes.RANGE_EXCLUSIVE
    );

    @Override
    public @NotNull FormattingModel createModel(@NotNull FormattingContext formattingContext) {
        final CodeStyleSettings codeStyleSettings = formattingContext.getCodeStyleSettings();
        return FormattingModelProvider
                .createFormattingModelForPsiFile(formattingContext.getContainingFile(),
                        new OdinFormatterBlock(formattingContext.getNode(),
                                Wrap.createWrap(WrapType.NONE, false),
                                null,
                                Indent.getNoneIndent(),
                                createSpaceBuilder(codeStyleSettings)),
                        codeStyleSettings);
    }
}
