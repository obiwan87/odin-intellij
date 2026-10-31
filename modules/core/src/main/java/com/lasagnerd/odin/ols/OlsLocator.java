package com.lasagnerd.odin.ols;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Finds the OLS executable. Search order: OLS_PATH, PATH, ODIN_ROOT, then the usual install folders.
 */
public final class OlsLocator {
    private OlsLocator() {
    }

    public static Optional<Path> find() {
        boolean windows = System.getProperty("os.name", "").toLowerCase().contains("windows");
        return find(System.getenv(), System.getProperty("user.home", ""), windows, Files::isRegularFile);
    }

    static Optional<Path> find(@NotNull Map<String, String> env,
                               @NotNull String home,
                               boolean windows,
                               @NotNull Predicate<Path> exists) {
        return candidates(env, home, windows).stream().filter(exists).findFirst();
    }

    private static List<Path> candidates(Map<String, String> env, String home, boolean windows) {
        String executable = windows ? "ols.exe" : "ols";
        String separator = windows ? ";" : ":";
        List<Path> result = new ArrayList<>();

        String explicit = env.get("OLS_PATH");
        if (explicit != null && !explicit.isBlank()) {
            result.add(Path.of(explicit));
        }

        String path = env.get(windows ? "Path" : "PATH");
        if (path == null) {
            path = env.get("PATH");
        }
        if (path != null) {
            for (String dir : path.split(separator)) {
                if (!dir.isBlank()) {
                    result.add(Path.of(dir.trim(), executable));
                }
            }
        }

        String odinRoot = env.get("ODIN_ROOT");
        if (odinRoot != null && !odinRoot.isBlank()) {
            result.add(Path.of(odinRoot, executable));
        }

        if (!home.isBlank()) {
            result.add(Path.of(home, "ols", executable));
            result.add(Path.of(home, ".local", "bin", executable));
            result.add(Path.of(home, "bin", executable));
        }
        return result;
    }
}
