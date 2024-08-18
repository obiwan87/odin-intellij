package com.lasagnerd.odin.lang;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.parser.GeneratedParserUtilBase;
import com.intellij.openapi.util.Key;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.lasagnerd.odin.lang.psi.OdinTypes;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Stack;

@SuppressWarnings("unused")
public class OdinParserUtil extends GeneratedParserUtilBase {

    private static final Key<Object2IntOpenHashMap<String>> MODES_KEY = Key.create("MODES_KEY");
    private static final Key<Stack<Object2IntOpenHashMap<String>>> MODES_STACK_KEY = Key.create("MODES_STACK_KEY");

    public static boolean afterClosingBrace(PsiBuilder builder, int level) {
        IElementType tokenType = lookBehindUntilNoWhitespace(builder);

        return tokenType == OdinTypes.RBRACE;
    }

    public static boolean multilineBlockComment(PsiBuilder builder, int level) {
        IElementType tokenType = lookBehindUntilNoWhitespace(builder);

        return tokenType == OdinTypes.MULTILINE_BLOCK_COMMENT;
    }

    public static boolean atClosingBrace(PsiBuilder builder, int level) {
        IElementType tokenType = builder.getTokenType();

        return tokenType == OdinTypes.RBRACE;
    }

    @Nullable
    private static IElementType lookBehindUntilNoWhitespace(PsiBuilder builder) {

        int i = 0;
        IElementType tokenType;
        int currentOffset = builder.getCurrentOffset();
        do {
            i--;
            tokenType = builder.rawLookup(i);
        } while ((tokenType == TokenType.WHITE_SPACE || tokenType == OdinTypes.NEW_LINE) && currentOffset + i > 0);
        return tokenType;
    }

    public static boolean enterNoBlockMode(PsiBuilder builder, int level) {
        // Save all current flags on a stack
        Object2IntOpenHashMap<String> flags = getParsingModes(builder);
        Object2IntOpenHashMap<String> flagsCopy = new Object2IntOpenHashMap<>(flags);
        Stack<Object2IntOpenHashMap<String>> stack = builder.getUserData(MODES_STACK_KEY);

        if (stack == null) {
            stack = new Stack<>();
            builder.putUserData(MODES_STACK_KEY, stack);
        }

        stack.push(flagsCopy);
        // Clear all flags
        flags.clear();

        flags.put("NO_BLOCK", flags.getInt("NO_BLOCK") + 1);
        return true;

    }

    public static boolean isModeOff(PsiBuilder builder, int level, String mode) {
        Object2IntOpenHashMap<String> flags = getParsingModes(builder);
        return flags.getInt(mode) <= 0;
    }

    public static boolean exitNoBlockMode(PsiBuilder builder, int level) {
        // Restore all flags from the stack
        Object2IntOpenHashMap<String> flags = getParsingModes(builder);
        Stack<Object2IntOpenHashMap<String>> stack = builder.getUserData(MODES_STACK_KEY);
        if(stack != null && !stack.isEmpty()) {
            Object2IntOpenHashMap<String> flagsCopy = stack.pop();
            flags.clear();
			flags.putAll(flagsCopy);
        }
        return true;
    }

    public static boolean enterMode(PsiBuilder builder, int level, String mode) {
        Object2IntOpenHashMap<String> flags = getParsingModes(builder);
        flags.put(mode, flags.getInt(mode) + 1);
        return true;
    }

    public static boolean exitMode(PsiBuilder builder, int level, String mode) {
        Object2IntOpenHashMap<String> flags = getParsingModes(builder);

        flags.put(mode, flags.getInt(mode) - 1);

        if (flags.getInt(mode) <= 0) {
            flags.removeInt(mode);
        }
        return true;
    }

    public static boolean isModeOn(PsiBuilder builder, int level, String mode) {
        Object2IntOpenHashMap<String> flags = getParsingModes(builder);

        return flags.getInt(mode) > 0;
    }


    @NotNull
    private static Object2IntOpenHashMap<String> getParsingModes(@NotNull PsiBuilder builder_) {
        Object2IntOpenHashMap<String> flags = builder_.getUserData(MODES_KEY);
        if (flags == null) builder_.putUserData(MODES_KEY, flags = new Object2IntOpenHashMap<>());
        return flags;
    }

    public static boolean beforeOperator(PsiBuilder builder, int level) {
        return OPERATORS.contains(builder.lookAhead(0));
    }

    public static boolean beforeComma(PsiBuilder builder, int level) {
        return builder.getTokenType() == OdinTypes.COMMA;
    }

    public static final TokenSet OPERATORS = TokenSet.create(
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
            OdinTypes.RANGE_EXCLUSIVE,
            OdinTypes.NOT,
            OdinTypes.RANGE,
            OdinTypes.DOT,
            OdinTypes.OR_ELSE,
            OdinTypes.OR_BREAK,
            OdinTypes.OR_CONTINUE,
            OdinTypes.OR_RETURN,
            OdinTypes.QUESTION,
            OdinTypes.IF,
            OdinTypes.WHEN,
            OdinTypes.COMMA
    );
}
