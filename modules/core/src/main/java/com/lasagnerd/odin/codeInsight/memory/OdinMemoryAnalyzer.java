package com.lasagnerd.odin.codeInsight.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Text-based, per-procedure heuristics for common Odin memory mistakes.
 * Deliberately conservative: anything that escapes the procedure is not reported as a leak.
 */
public final class OdinMemoryAnalyzer {
    public enum Kind {LEAK, DANGLING_POINTER, USE_AFTER_FREE, DOUBLE_FREE, NO_BOUNDS_CHECK, UNCHECKED_ASSERTION}

    /**
     * @param name     the variable concerned, or null
     * @param deleter  the procedure that releases it, for leaks only
     */
    public record Problem(Kind kind, int offset, int length, String name, String deleter, String message) {
    }

    private static final Pattern PROC = Pattern.compile("::\\s*proc\\b[^{;]*\\{");
    private static final Pattern ALLOCATION =
            Pattern.compile("\\b(\\w+)\\s*(?:,\\s*\\w+\\s*)?:=\\s*(make|new)\\s*\\(");
    private static final Pattern LOCAL = Pattern.compile("\\b(\\w+)\\s*(?::\\s*[^=\\n]*)?:?=");
    private static final Pattern RETURN_ADDRESS = Pattern.compile("\\breturn\\s+&(\\w+)\\b(?![.\\[])");
    private static final Pattern RELEASE = Pattern.compile("\\b(delete|free)\\s*\\(\\s*(\\w+)\\s*[,)]");
    private static final Pattern NO_BOUNDS_CHECK = Pattern.compile("#no_bounds_check\\b");

    // Anchored to the line start so that the `value, ok := x.(T)` form does not match
    private static final Pattern UNCHECKED_ASSERTION =
            Pattern.compile("(?m)^[ \\t]*\\w+\\s*:=\\s*[\\w.\\[\\]]+\\.\\([^)?\\n]+\\)");

    private OdinMemoryAnalyzer() {
    }

    public static List<Problem> analyze(String source) {
        String code = blankCommentsAndStrings(source);
        List<Problem> problems = new ArrayList<>();

        Matcher noBounds = NO_BOUNDS_CHECK.matcher(code);
        while (noBounds.find()) {
            problems.add(new Problem(Kind.NO_BOUNDS_CHECK, noBounds.start(), noBounds.end() - noBounds.start(),
                    null, null, "Bounds checks are disabled here: out-of-range access is undefined behaviour"));
        }

        Matcher assertion = UNCHECKED_ASSERTION.matcher(code);
        while (assertion.find()) {
            problems.add(new Problem(Kind.UNCHECKED_ASSERTION, assertion.end() - 1, 1, null, null,
                    "Type assertion panics on mismatch: use 'value, ok := x.(T)' to handle it"));
        }

        Matcher proc = PROC.matcher(code);
        int position = 0;
        while (proc.find(position)) {
            int open = proc.end() - 1;
            int close = matching(code, open, '{', '}');
            if (close < 0) {
                break;
            }
            analyzeBody(code, open + 1, close, problems);
            position = close + 1;
        }
        return problems;
    }

    private static void analyzeBody(String code, int start, int end, List<Problem> problems) {
        String body = code.substring(start, end);
        findLeaks(body, start, problems);
        findDanglingPointers(body, start, problems);
        findFreedUses(body, start, problems);
    }

    private static void findLeaks(String body, int base, List<Problem> problems) {
        if (body.contains("context.allocator") || body.contains("context.temp_allocator =")) {
            return;
        }
        Matcher allocation = ALLOCATION.matcher(body);
        while (allocation.find()) {
            int open = allocation.end() - 1;
            int close = matching(body, open, '(', ')');
            if (close < 0 || body.substring(open, close).contains("allocator")) {
                continue;
            }
            String name = allocation.group(1);
            String word = "\\b" + name + "\\b";
            boolean handled = find(body, "\\b(delete|free)\\s*\\(\\s*" + word)
                    || find(body, "\\breturn\\b[^\\n]*" + word)
                    || find(body, "[.\\]]\\s*\\w*\\s*=\\s*" + word)
                    || find(body, "\\bappend\\s*\\([^\\n]*" + word)
                    || find(body, "\\{[^\\n{}]*" + word + "[^\\n{}]*\\}");
            if (!handled) {
                String deleter = allocation.group(2).equals("new") ? "free" : "delete";
                problems.add(new Problem(Kind.LEAK, base + allocation.start(1), name.length(), name, deleter,
                        "'" + name + "' is allocated but never released with " + deleter + "()"));
            }
        }
    }

    private static void findDanglingPointers(String body, int base, List<Problem> problems) {
        Matcher address = RETURN_ADDRESS.matcher(body);
        while (address.find()) {
            String name = address.group(1);
            Matcher local = LOCAL.matcher(body.substring(0, address.start()));
            while (local.find()) {
                if (local.group(1).equals(name) && local.group(0).contains(":")) {
                    problems.add(new Problem(Kind.DANGLING_POINTER, base + address.start(),
                            address.end() - address.start(), name, null,
                            "Returning the address of local '" + name + "': it dangles once the procedure returns"));
                    break;
                }
            }
        }
    }

    private static void findFreedUses(String body, int base, List<Problem> problems) {
        Matcher release = RELEASE.matcher(body);
        while (release.find()) {
            int lineStart = body.lastIndexOf('\n', release.start()) + 1;
            if (body.substring(lineStart, release.start()).contains("defer")) {
                continue;
            }
            String name = release.group(2);
            int close = matching(body, body.indexOf('(', release.start()), '(', ')');
            if (close < 0) {
                continue;
            }
            // Only the rest of the current block: a later branch may not run after this release
            String rest = body.substring(close + 1);
            Matcher boundary = Pattern.compile("[{}]|\\breturn\\b|\\bbreak\\b|\\bcontinue\\b").matcher(rest);
            String block = boundary.find() ? rest.substring(0, boundary.start()) : rest;
            Matcher use = Pattern.compile("\\b" + name + "\\b").matcher(block);
            if (!use.find()) {
                continue;
            }
            String after = block.substring(use.end()).stripLeading();
            if (after.startsWith(":=") || (after.startsWith("=") && !after.startsWith("=="))) {
                continue;
            }
            String before = block.substring(0, use.start());
            boolean freedAgain = before.matches("(?s).*\\b(delete|free)\\s*\\(\\s*$");
            problems.add(new Problem(freedAgain ? Kind.DOUBLE_FREE : Kind.USE_AFTER_FREE,
                    base + close + 1 + use.start(), name.length(), name, null,
                    freedAgain ? "'" + name + "' is released twice" : "'" + name + "' is used after being released"));
        }
    }

    private static boolean find(String text, String regex) {
        return Pattern.compile(regex).matcher(text).find();
    }

    private static int matching(String text, int open, char opening, char closing) {
        int depth = 0;
        for (int i = open; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == opening) {
                depth++;
            } else if (c == closing && --depth == 0) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Replaces comments and string contents with spaces, keeping offsets and newlines.
     */
    static String blankCommentsAndStrings(String source) {
        char[] out = source.toCharArray();
        int i = 0;
        while (i < out.length) {
            char c = source.charAt(i);
            if (c == '/' && i + 1 < out.length && source.charAt(i + 1) == '/') {
                while (i < out.length && source.charAt(i) != '\n') {
                    out[i++] = ' ';
                }
            } else if (c == '/' && i + 1 < out.length && source.charAt(i + 1) == '*') {
                // Odin block comments nest
                int depth = 0;
                do {
                    if (source.startsWith("/*", i)) {
                        depth++;
                        out[i] = out[i + 1] = ' ';
                        i += 2;
                    } else if (source.startsWith("*/", i)) {
                        depth--;
                        out[i] = out[i + 1] = ' ';
                        i += 2;
                    } else {
                        if (out[i] != '\n') {
                            out[i] = ' ';
                        }
                        i++;
                    }
                } while (depth > 0 && i < out.length - 1);
            } else if (c == '"' || c == '`' || c == '\'') {
                i++;
                while (i < out.length && source.charAt(i) != c && source.charAt(i) != '\n') {
                    if (c != '`' && source.charAt(i) == '\\' && i + 1 < out.length) {
                        out[i++] = ' ';
                    }
                    out[i++] = ' ';
                }
                i++;
            } else {
                i++;
            }
        }
        return new String(out);
    }
}
