package com.lasagnerd.odin.runConfiguration;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public enum OdinBuildProfile {
    DEV("dev", List.of("-debug", "-o:none")),
    RELEASE("release", List.of("-o:speed")),
    AGGRESSIVE("aggressive", List.of("-o:aggressive"));

    private static final String PROPERTY = "odin.build.profile";
    private static final String SANITIZE_PROPERTY = "odin.build.sanitizeAddress";

    private final String label;
    private final List<String> flags;

    OdinBuildProfile(String label, List<String> flags) {
        this.label = label;
        this.flags = flags;
    }

    public String label() {
        return label;
    }

    public static OdinBuildProfile current(@NotNull Project project) {
        String stored = PropertiesComponent.getInstance(project).getValue(PROPERTY, DEV.name());
        for (OdinBuildProfile profile : values()) {
            if (profile.name().equals(stored)) {
                return profile;
            }
        }
        return DEV;
    }

    public static boolean sanitizeAddress(@NotNull Project project) {
        return PropertiesComponent.getInstance(project).getBoolean(SANITIZE_PROPERTY);
    }

    public static void setSanitizeAddress(@NotNull Project project, boolean enabled) {
        PropertiesComponent.getInstance(project).setValue(SANITIZE_PROPERTY, enabled);
    }

    public void select(@NotNull Project project) {
        PropertiesComponent.getInstance(project).setValue(PROPERTY, name());
    }

    /**
     * Adds this profile's flags, unless the run configuration already sets an optimization level.
     */
    public void addTo(@NotNull List<String> command) {
        if (command.stream().noneMatch(argument -> argument.startsWith("-o:"))) {
            for (String flag : flags) {
                if (!command.contains(flag)) {
                    command.add(flag);
                }
            }
        }
    }
}
