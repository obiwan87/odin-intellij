package com.lasagnerd.odin.runConfiguration;

import com.intellij.execution.filters.*;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public class OdinBuildConsoleFilterProvider implements ConsoleFilterProvider {

    public static final Pattern PATTERN = Pattern.compile("((.+?)\\((\\d+):(\\d+)\\))");
    public static final PatternHyperlinkFormat PATTERN_HYPERLINK_FORMAT = new PatternHyperlinkFormat(PATTERN,
            false,
            false,
            PatternHyperlinkPart.HYPERLINK,
            PatternHyperlinkPart.PATH,
            PatternHyperlinkPart.LINE,
            PatternHyperlinkPart.COLUMN);
    public static final PatternBasedFileHyperlinkRawDataFinder FINDER = new PatternBasedFileHyperlinkRawDataFinder(
            new PatternHyperlinkFormat[]{PATTERN_HYPERLINK_FORMAT}
    );

    @Override
    public Filter @NotNull [] getDefaultFilters(@NotNull Project project) {

        PatternBasedFileHyperlinkFilter filter = new PatternBasedFileHyperlinkFilter(project, project.getBasePath(), FINDER);
        return new Filter[]{filter};
    }
}
