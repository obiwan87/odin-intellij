package com.lasagnerd.odin.lang;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.parser.GeneratedParserUtilBase;
import com.intellij.openapi.util.Key;
import com.intellij.psi.tree.IElementType;
import com.lasagnerd.odin.lang.psi.OdinTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.coverage.gnu.trove.TObjectIntHashMap;

public class OdinParserUtil extends GeneratedParserUtilBase {

    private static final Key<TObjectIntHashMap<String>> MODES_KEY = Key.create("MODES_KEY");

    public static boolean closingBracket(PsiBuilder builder, int level) {
        IElementType iElementType = builder.getTokenType();
        return iElementType == OdinTypes.RPAREN || iElementType == OdinTypes.RBRACE;
    }

    public static boolean multilineBlockComment(PsiBuilder builder, int level) {
        IElementType iElementType = builder.getTokenType();
        if (iElementType == OdinTypes.BLOCK_COMMENT) {
            String tokenText = builder.getTokenText();
            if (tokenText != null) {
                return tokenText.contains("\n");
            }
        }
        return false;
    }

    public static boolean enterMode(PsiBuilder builder, int level, String mode) {
        TObjectIntHashMap<String> flags = getParsingModes(builder);
        flags.put(mode, flags.get(mode) + 1);
        return true;
    }

    public static boolean exitMode(PsiBuilder builder, int level, String mode) {
        TObjectIntHashMap<String> flags = getParsingModes(builder);

        flags.put(mode, flags.get(mode) - 1);

        if (flags.get(mode) <= 0) {
            flags.remove(mode);
        }
        return true;
    }

    public static boolean isModeOn(PsiBuilder builder, int level, String mode) {
        TObjectIntHashMap<String> flags = getParsingModes(builder);

        return flags.get(mode) > 0;
    }

    public static boolean isModeOff(PsiBuilder builder, int level, String mode) {
        TObjectIntHashMap<String> flags = getParsingModes(builder);
        return flags.get(mode) <= 0;
    }


    @NotNull
    private static TObjectIntHashMap<String> getParsingModes(@NotNull PsiBuilder builder_) {
        TObjectIntHashMap<String> flags = builder_.getUserData(MODES_KEY);
        if (flags == null) builder_.putUserData(MODES_KEY, flags = new TObjectIntHashMap<>());
        return flags;
    }
}
