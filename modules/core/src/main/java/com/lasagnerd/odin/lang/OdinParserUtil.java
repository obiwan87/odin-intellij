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

import static com.lasagnerd.odin.lang.psi.OdinTypes.DIRECTIVE_IDENTIFIER;

@SuppressWarnings("unused")
public class OdinParserUtil extends GeneratedParserUtilBase {

    public static final TokenSet WHITESPACE_OR_COMMENT = TokenSet.orSet(
            OdinSyntaxHighlighter.COMMENT_TOKENS,
            OdinSyntaxHighlighter.WHITE_SPACES
    );
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

    @Nullable
    private static IElementType lookBehindUntilNoWhitespace(PsiBuilder builder) {
        return lookbehindWhileSkipping(builder, OdinSyntaxHighlighter.WHITE_SPACES);
    }

    @Nullable
    private static IElementType lookbehindUntilNoWhitespaceOrComment(PsiBuilder builder) {
        return lookbehindWhileSkipping(builder, WHITESPACE_OR_COMMENT);
    }

    private static @Nullable IElementType lookbehindWhileSkipping(PsiBuilder builder, TokenSet tokensToSkip) {
        int i = 0;
        IElementType tokenType;
        int currentOffset = builder.getCurrentOffset();
        do {
            i--;
            tokenType = builder.rawLookup(i);
        } while (tokensToSkip.contains(tokenType) && currentOffset + i > 0);
        return tokenType;
    }

    public static boolean enterNoBlockMode(PsiBuilder builder, int level) {
        // Save all current flags on a stack
        Object2IntOpenHashMap<String> flags = getParsingModes(builder);
        Object2IntOpenHashMap<String> flagsCopy = new Object2IntOpenHashMap<>(flags);
        Stack<Object2IntOpenHashMap<String>> stack = builder.getUserData(MODES_STACK_KEY);

        log("Entering NO_BLOCK at offset " + builder.getCurrentOffset() + ". Current Token: " + builder.getTokenText(), builder, flags);

        if (stack == null) {
            stack = new Stack<>();
            builder.putUserData(MODES_STACK_KEY, stack);
        }
        stack.push(flagsCopy);
        flags.clear();
        flags.put("NO_BLOCK", 1);
        return true;

    }

    private static @NotNull Stack<Object2IntOpenHashMap<String>> getParsingModesStack(PsiBuilder builder) {
        Stack<Object2IntOpenHashMap<String>> stack = builder.getUserData(MODES_STACK_KEY);

        if (stack == null) {
            stack = new Stack<>();
            builder.putUserData(MODES_STACK_KEY, stack);
        }
        return stack;
    }


    public static boolean beforeColon(PsiBuilder builder, int level) {
        IElementType elementType = lookbehindUntilNoWhitespaceOrComment(builder);
        return elementType == OdinTypes.COLON;
    }
    public static boolean isModeOff(PsiBuilder builder, int level, String mode) {
        Object2IntOpenHashMap<String> flags = getParsingModes(builder);


        return flags.getOrDefault(mode, 0) <= 0;
    }

    public static boolean exitNoBlockMode(PsiBuilder builder, int level) {
        // Restore all flags from the stack
        Object2IntOpenHashMap<String> flags = getParsingModes(builder);
        log("Exiting NO_BLOCK at offset " + builder.getCurrentOffset() + ". Current Token: " + builder.getTokenText(), builder, flags);
        if (flags.getOrDefault("NO_BLOCK", 0) <= 0)
            return false;

        Stack<Object2IntOpenHashMap<String>> stack = builder.getUserData(MODES_STACK_KEY);
        if (stack != null && !stack.isEmpty()) {
            Object2IntOpenHashMap<String> flagsCopy = stack.pop();
            flags.clear();
            flags.putAll(flagsCopy);
        }
        return true;
    }

    private static void log(String builder, PsiBuilder builder1, Object2IntOpenHashMap<String> flags) {
//        System.out.println(builder);
//        System.out.println("Stack size: " + getParsingModesStack(builder1).size());
//        System.out.println("Flags: ");
//        flags.forEach((k, v) -> System.out.print(k + "=" + v + ","));
//        System.out.println();
    }

    public static boolean enterMode(PsiBuilder builder, int level, String mode) {
        Object2IntOpenHashMap<String> flags = getParsingModes(builder);
        log("Switching ON mode " + mode + " at offset " + builder.getCurrentOffset() + ". Current Token: " + builder.getTokenText(), builder, flags);
        int currentCount = flags.getOrDefault(mode, 0);
        flags.put(mode, currentCount + 1);
        return true;
    }

    public static boolean exitMode(PsiBuilder builder, int level, String mode) {
        Object2IntOpenHashMap<String> flags = getParsingModes(builder);

        log("Switching OFF mode " + mode + " at offset " + builder.getCurrentOffset() + ". Current Token: " + builder.getTokenText(), builder, flags);

        flags.put(mode, flags.getOrDefault(mode, 0) - 1);

        if (flags.getOrDefault(mode, 0) <= 0) {
            flags.removeInt(mode);
        }
        return true;
    }

    public static boolean isModeOn(PsiBuilder builder, int level, String mode) {
        Object2IntOpenHashMap<String> flags = getParsingModes(builder);

        return flags.getOrDefault(mode, 0) > 0;
    }

    public static boolean afterNewLine(PsiBuilder builder, int level) {
        int i = 0;
        IElementType tokenType;
        int currentOffset = builder.getCurrentOffset();
        boolean newLineEncountered = false;
        do {
            i--;
            tokenType = builder.rawLookup(i);
            newLineEncountered |= tokenType == OdinTypes.NEW_LINE;
        } while ((tokenType == TokenType.WHITE_SPACE || tokenType == OdinTypes.NEW_LINE) && currentOffset + i > 0);

        return newLineEncountered;
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

    public static boolean guardedExpression(PsiBuilder builder,
                                            int level,
                                            Parser incompleteGuard,
                                            Parser expression) {
        boolean r = report_error_(builder, incompleteGuard.parse(builder, level));
        PsiBuilder.Marker marker = builder.mark();
        r = r && expression.parse(builder, level);
        if (r) {
            marker.drop();
        } else {
            marker.rollbackTo();
        }

        return r;
    }

    public static boolean relativeTypeRule(PsiBuilder builder,
                                            int level,
                                            Parser type)
    {
        PsiBuilder.Marker m = enter_section_(builder);
        boolean r = consumeToken(builder, OdinTypes.HASH);
        if(r && builder.getTokenType() == OdinTypes.IDENTIFIER_TOKEN) {
            if(!"relative".equals(builder.getTokenText())) {
                return false;
            }
        }
        r = r && consumeToken(builder, OdinTypes.IDENTIFIER_TOKEN);
        exit_section_(builder, m, DIRECTIVE_IDENTIFIER, r);
        r = r && consumeToken(builder, OdinTypes.LPAREN);
        r = r && report_error_(builder, type.parse(builder, level));
        r = r && consumeToken(builder, OdinTypes.RPAREN);
        return r;
    }
}