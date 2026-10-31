package com.lasagnerd.odin.codeInsight.memory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Finds uses of well-known core packages that the file does not import.
 */
public final class OdinMissingImportAnalyzer {
    public record MissingImport(int offset, String alias, String importPath) {
    }

    private static final Map<String, String> PACKAGES = Map.ofEntries(
            Map.entry("fmt", "core:fmt"), Map.entry("mem", "core:mem"), Map.entry("os", "core:os"),
            Map.entry("strings", "core:strings"), Map.entry("strconv", "core:strconv"), Map.entry("math", "core:math"),
            Map.entry("slice", "core:slice"), Map.entry("time", "core:time"), Map.entry("log", "core:log"),
            Map.entry("testing", "core:testing"), Map.entry("runtime", "base:runtime"),
            Map.entry("sync", "core:sync"), Map.entry("thread", "core:thread"), Map.entry("filepath", "core:path/filepath"));

    private static final Pattern IMPORT = Pattern.compile("\\bimport\\s+(?:(\\w+)\\s+)?\"([^\"]+)\"");
    private static final Pattern USE = Pattern.compile("(?<![\\w.])(\\w+)\\.\\w");

    private OdinMissingImportAnalyzer() {
    }

    public static List<MissingImport> analyze(String source) {
        Set<String> imported = new HashSet<>();
        Matcher imports = IMPORT.matcher(source);
        while (imports.find()) {
            String path = imports.group(2);
            imported.add(imports.group(1) != null ? imports.group(1) : path.substring(path.lastIndexOf('/') + 1).replaceFirst(".*:", ""));
        }

        String code = OdinMemoryAnalyzer.blankCommentsAndStrings(source);
        Set<String> reported = new HashSet<>();
        List<MissingImport> result = new ArrayList<>();
        Matcher use = USE.matcher(code);
        while (use.find()) {
            String alias = use.group(1);
            String path = PACKAGES.get(alias);
            if (path != null && !imported.contains(alias) && reported.add(alias)
                    && !Pattern.compile("\\b" + alias + "\\s*(?:,\\s*\\w+\\s*)?:").matcher(code).find()) {
                result.add(new MissingImport(use.start(1), alias, path));
            }
        }
        return result;
    }
}
