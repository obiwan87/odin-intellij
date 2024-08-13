package com.lasagnerd.odin.sdkConfig;

import com.intellij.openapi.projectRoots.*;
import com.lasagnerd.odin.OdinIcons;
import org.jdom.Element;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OdinSdkType extends SdkType {

    public OdinSdkType() {
        super("Odin SDK");
    }

    @Override
    public @Nullable String suggestHomePath() {
        return null;
    }

    @Override
    public boolean isValidSdkHome(@NotNull String path) {
        return isOdinExecutable(path);
    }

    private static boolean isOdinExecutable(String path) {
        return Files.exists(Path.of(path, "odin")) || Files.exists(Path.of(path, "odin.exe"));
    }

    @Override
    public boolean setupSdkPaths(@NotNull Sdk sdk, @NotNull SdkModel sdkModel) {
        SdkModificator modificator = sdk.getSdkModificator();
        modificator.setVersionString(getVersionString(sdk));
        modificator.commitChanges(); // save
        return true;
    }

    @Override
    public @Nullable AdditionalDataConfigurable createAdditionalDataConfigurable(@NotNull SdkModel sdkModel, @NotNull SdkModificator sdkModificator) {
        return null;
    }

    @Override
    public Icon getIcon() {
        return OdinIcons.OdinSdk;
    }

    @Override
    public @NotNull String suggestSdkName(@Nullable String currentSdkName, @NotNull String sdkHome) {
        String versionString = getVersionString(sdkHome);
        if (versionString == null) {
            return "Odin SDK";
        }
        // Pattern is dev-yyyy-mm
        Pattern pattern = Pattern.compile("dev-(\\d{4}-\\d{2})");
        Matcher matcher = pattern.matcher(versionString);
        if (matcher.find()) {
            versionString = matcher.group(1);
        }
        return versionString;
    }

    @Override
    public @NotNull @Nls(capitalization = Nls.Capitalization.Title) String getPresentableName() {
        return "Odin SDK";
    }

    @Override
    public void saveAdditionalData(@NotNull SdkAdditionalData additionalData, @NotNull Element additional) {

    }

    @Override
    public @NotNull Comparator<Sdk> versionComparator() {
        return super.versionComparator();
    }

    @Override
    public @NotNull Comparator<String> versionStringComparator() {
        return Comparator.naturalOrder();
    }

    @Override
    public @Nullable String getVersionString(@NotNull String sdkHome) {
        ProcessBuilder processBuilder = new ProcessBuilder("odin", "version");
        processBuilder.directory(Path.of(sdkHome).toFile());
        try {
            Process start = processBuilder.start();
            byte[] bytes = start.getInputStream().readAllBytes();
            String stdOut = new String(bytes);

            Pattern pattern = Pattern.compile("version (.*)");
            Matcher matcher = pattern.matcher(stdOut);

            String version;
            if (matcher.find()) {
                version = matcher.group(1);

            } else {
                return "Unknown";
            }

            return "Odin version \""+version+"\"";
        } catch (IOException e) {
            return "Unknown";
        }
    }

    @Override
    public boolean allowWslSdkForLocalProject() {
        return true;
    }
}
