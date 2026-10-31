package com.lasagnerd.odin.ols;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Installs the latest stable OLS release into ~/ols, where {@link OlsLocator} looks for it.
 */
public final class OlsInstaller {
    private static final String BASE_URL = "https://github.com/DanielGavin/ols/releases/latest/download/";

    private OlsInstaller() {
    }

    static String assetName(String osName, String arch) {
        String os = osName.toLowerCase(Locale.ROOT);
        boolean arm = arch.contains("aarch64") || arch.contains("arm64");
        if (os.contains("windows")) {
            return "ols-x86_64-pc-windows-msvc.zip";
        }
        if (os.contains("mac")) {
            return arm ? "ols-arm64-darwin.zip" : "ols-x86_64-darwin.zip";
        }
        return arm ? "ols-arm64-unknown-linux-gnu.zip" : "ols-x86_64-unknown-linux-gnu.zip";
    }

    /**
     * Release archives name their tools by platform, such as ols-x86_64-pc-windows-msvc.exe.
     */
    static String installedName(String entryName) {
        String extension = entryName.endsWith(".exe") ? ".exe" : "";
        if (entryName.startsWith("ols-")) {
            return "ols" + extension;
        }
        if (entryName.startsWith("odinfmt-")) {
            return "odinfmt" + extension;
        }
        return entryName;
    }

    public static @NotNull Path install() throws IOException, InterruptedException {
        Path target = Path.of(System.getProperty("user.home"), "ols").toAbsolutePath().normalize();
        String asset = assetName(System.getProperty("os.name", ""), System.getProperty("os.arch", ""));
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + asset))
                .timeout(Duration.ofMinutes(5)).header("User-Agent", "odin-intellij").GET().build();
        HttpResponse<InputStream> response = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build()
                .send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() != 200) {
            throw new IOException("GitHub returned HTTP " + response.statusCode());
        }
        Files.createDirectories(target);
        try (ZipInputStream zip = new ZipInputStream(response.body())) {
            for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                Path destination = target.resolve(installedName(entry.getName())).normalize();
                if (!destination.startsWith(target)) {
                    throw new IOException("Unsafe path in OLS archive: " + entry.getName());
                }
                if (entry.isDirectory()) {
                    Files.createDirectories(destination);
                } else {
                    Files.createDirectories(destination.getParent());
                    Files.copy(zip, destination, StandardCopyOption.REPLACE_EXISTING);
                    if (destination.getFileName().toString().matches("(ols|odinfmt)(\\.exe)?")) {
                        destination.toFile().setExecutable(true);
                    }
                }
            }
        }
        return target;
    }
}
