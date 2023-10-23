package com.lasagnerd.odin.formatter;

import com.intellij.formatting.*;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.lasagnerd.odin.lang.OdinLanguage;
import com.lasagnerd.odin.lang.psi.OdinTypes;
import org.jetbrains.annotations.NotNull;

public class OdinFormattingBuilder implements FormattingModelBuilder {
    private static SpacingBuilder createSpaceBuilder(CodeStyleSettings settings) {
        CommonCodeStyleSettings odinSettings = settings.getCommonSettings(OdinLanguage.INSTANCE.getID());
        return new SpacingBuilder(settings, OdinLanguage.INSTANCE)
                .around(OdinTypes.ASSIGNMENT_OPERATOR)
                .spaces(1)
                .after(OdinTypes.BLOCK_START)
                .lineBreakInCode()
                .before(OdinTypes.BLOCK_END)
                .lineBreakInCode()
                .after(OdinTypes.PACKAGE_CLAUSE)
                .blankLines(1);

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
