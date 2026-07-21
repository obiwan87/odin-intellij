package com.lasagnerd.odin.settings.projectSettings;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.util.io.Decompressor;
import org.jetbrains.annotations.NotNull;
import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;

public final class OdinSdkDownloader {
    private OdinSdkDownloader() {}

    public static @NotNull Path defaultInstallDirectory(String archiveName) {
        String userHome = System.getProperty("user.home");
        if (userHome == null || userHome.isBlank()) userHome = System.getProperty("user.dir", ".");
        return Path.of(userHome).resolve(".odin-sdks").resolve(archiveBaseName(archiveName));
    }

    static @NotNull String archiveBaseName(String archiveName) {
        if (archiveName.endsWith(".tar.gz")) return archiveName.substring(0, archiveName.length() - 7);
        if (archiveName.endsWith(".zip")) return archiveName.substring(0, archiveName.length() - 4);
        return archiveName;
    }

    public static @NotNull OdinSdkState download(OdinRelease release, OdinRelease.Asset asset,
                                                  Path destination, ProgressIndicator indicator) throws Exception {
        Path parent = destination.toAbsolutePath().normalize().getParent();
        if (parent == null) throw new IOException("Download location has no parent directory");
        Files.createDirectories(parent);
        if (Files.exists(destination) && directoryHasEntries(destination))
            throw new IOException("Download location is not empty: " + destination);
        Path temporary = Files.createTempDirectory(parent, ".odin-download-");
        boolean installed = false;
        try {
            indicator.setText("Downloading " + asset.name());
            Path archive = temporary.resolve(asset.name());
            HttpRequest request = HttpRequest.newBuilder(URI.create(asset.downloadUrl())).build();
            HttpResponse<Path> response = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build()
                    .send(request, HttpResponse.BodyHandlers.ofFile(archive));
            if (response.statusCode() != 200) throw new IOException("Download returned HTTP " + response.statusCode());
            verifyDigest(archive, asset.digest());
            indicator.checkCanceled();

            Path extracted = temporary.resolve("extracted");
            Files.createDirectories(extracted);
            indicator.setText("Extracting " + asset.name());
            if (asset.name().endsWith(".zip")) new Decompressor.Zip(archive).extract(extracted);
            else new Decompressor.Tar(archive).extract(extracted);
            indicator.checkCanceled();

            Path contentRoot = selectContentRoot(extracted);
            Files.createDirectories(destination);
            moveChildren(contentRoot, destination);
            installed = true;

            Path compiler = findCompiler(destination);
            Path libraries = findLibraries(destination);
            OdinSdkState sdk = new OdinSdkState();
            sdk.name = archiveBaseName(asset.name());
            sdk.compilerPath = compiler.toString();
            sdk.libraryPath = libraries.toString();
            sdk.version = ""; // The selected asset may target another OS and cannot always be executed locally.
            sdk.origin = "DOWNLOADED";
            sdk.releaseTag = release.tagName(); sdk.releaseId = release.id(); sdk.releaseAssetId = asset.id(); sdk.releaseAssetUrl = asset.downloadUrl();
            return sdk;
        } finally {
            deleteRecursively(temporary);
            if (!installed && Files.exists(destination) && !directoryHasEntries(destination)) Files.deleteIfExists(destination);
        }
    }

    static Path selectContentRoot(Path extracted) throws IOException {
        Path dist = extracted.resolve("dist");
        if (Files.isDirectory(dist)) return dist;
        try (var children = Files.list(extracted)) {
            List<Path> entries = children.toList();
            if (entries.size() == 1 && Files.isDirectory(entries.get(0))) {
                Path only = entries.get(0);
                if (Files.isDirectory(only.resolve("dist"))) return only.resolve("dist");
                if (containsSdkLayout(only)) return only;
            }
        }
        return extracted;
    }

    private static boolean containsSdkLayout(Path root) {
        return Files.isDirectory(root.resolve("base")) || Files.isRegularFile(root.resolve("odin")) || Files.isRegularFile(root.resolve("odin.exe"));
    }

    private static void moveChildren(Path source, Path destination) throws IOException {
        try (var children = Files.list(source)) {
            for (Path child : children.toList()) Files.move(child, destination.resolve(child.getFileName()), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static Path findCompiler(Path root) throws IOException {
        Path windows = root.resolve("odin.exe"), unix = root.resolve("odin");
        if (Files.isRegularFile(windows)) return windows;
        if (Files.isRegularFile(unix)) return unix;
        throw new IOException("Downloaded archive does not contain an Odin executable");
    }

    private static Path findLibraries(Path root) throws IOException {
        if (Files.isDirectory(root.resolve("base"))) return root;
        throw new IOException("Downloaded archive does not contain the Odin base library");
    }

    private static boolean directoryHasEntries(Path path) throws IOException {
        if (!Files.isDirectory(path)) return Files.exists(path);
        try (var entries = Files.list(path)) { return entries.findAny().isPresent(); }
    }

    private static void verifyDigest(Path file, String digest) throws Exception {
        if (digest == null || !digest.startsWith("sha256:")) return;
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        try (InputStream in = Files.newInputStream(file)) { byte[] b = new byte[8192]; for (int n; (n = in.read(b)) >= 0;) md.update(b, 0, n); }
        String actual = HexFormat.of().formatHex(md.digest());
        if (!actual.equalsIgnoreCase(digest.substring(7))) throw new IOException("Downloaded SDK checksum does not match");
    }

    private static void deleteRecursively(Path root) {
        if (root == null || !Files.exists(root)) return;
        try (var paths = Files.walk(root)) { paths.sorted(Comparator.reverseOrder()).forEach(p -> { try { Files.deleteIfExists(p); } catch (IOException ignored) {} }); }
        catch (IOException ignored) {}
    }
}
