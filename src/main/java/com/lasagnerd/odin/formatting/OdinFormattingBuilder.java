package com.lasagnerd.odin.formatting;

import com.intellij.formatting.*;
import com.intellij.psi.codeStyle.CodeStyleSettings;
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

                .after(OdinTypes.PACKAGE_CLAUSE)
                .blankLines(1)

                .between(OdinTypes.ELSE, OdinTypes.BLOCK)
                .spacing(1, 1, 0, false, 0)

                .withinPairInside(OdinTypes.CONDITION, OdinTypes.BLOCK, OdinTypes.IF_STATEMENT)
                .spacing(1, 1, 0, false, 0)

                .withinPairInside(OdinTypes.CONDITION, OdinTypes.BLOCK, OdinTypes.ELSE_IF_BLOCK)
                .spacing(1, 1, 0, false, 0)

                .withinPairInside(OdinTypes.SWITCH_HEAD, OdinTypes.SWITCH_BODY, OdinTypes.SWITCH_STATEMENT)
                .spacing(1, 1, 0, false, 0)

                .withinPairInside(OdinTypes.FOR_HEAD, OdinTypes.BLOCK, OdinTypes.FOR_STATEMENT)
                .spacing(1, 1, 0, false, 0)

                .withinPair(OdinTypes.PROCEDURE_TYPE, OdinTypes.PROCEDURE_BODY)
                .spacing(1, 1, 0, false, 0)

                .between(OdinTypes.ELSE, OdinTypes.IF)
                .spacing(1, 1, 0, false, 0)

                .between(OdinTypes.BLOCK, OdinTypes.ELSE_IF_BLOCK)
                .spacing(1, 1, 0, false, 0)

                .between(OdinTypes.BLOCK, OdinTypes.ELSE_BLOCK)
                .spacing(1, 1, 0, false, 0)

                .between(OdinTypes.COLON, OdinTypes.EQ)
                .spaces(0)

                .between(OdinTypes.COLON, OdinTypes.COLON)
                .spaces(0)

                .before(OdinTypes.COLON_OPENING)
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

                .aroundInside(OdinTypes.EQ, OdinTypes.COMPOUND_LITERAL_VALUE_BODY)
                .spaces(1)

                .aroundInside(OdinTypes.EQ, OdinTypes.ENUM_BODY)
                .spaces(1)

                .afterInside(OdinTypes.COMPOUND_VALUE_START, OdinTypes.COMPOUND_LITERAL_VALUE)
                .spaces(1)

                .beforeInside(OdinTypes.COMPOUND_VALUE_END, OdinTypes.COMPOUND_LITERAL_VALUE)
                .spaces(1)
                ;
    }

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
