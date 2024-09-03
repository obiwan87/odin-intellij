package com.lasagnerd.odin.debugger.toolchains;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.download.DownloadableFileDescription;
import com.intellij.util.download.DownloadableFileService;
import com.intellij.util.download.FileDownloader;
import com.intellij.util.io.Decompressor;
import com.intellij.util.ui.UIUtil;
import com.jetbrains.cidr.ArchitectureType;
import com.jetbrains.cidr.execution.debugger.backend.DebuggerDriver;
import com.jetbrains.cidr.execution.debugger.backend.DebuggerDriverConfiguration;
import com.lasagnerd.odin.debugger.OdinDebuggerLanguage;
import com.lasagnerd.odin.debugger.drivers.dap.DAPDebuggerDriverConfiguration;
import com.lasagnerd.odin.debugger.drivers.WinDAPDriver;
import com.lasagnerd.odin.extensions.OdinDebuggerToolchain;
import lombok.val;
import org.eclipse.lsp4j.debug.InitializeRequestArguments;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;
import java.util.zip.Inflater;

public class WinDAPToolchain implements OdinDebuggerToolchain, DebuggerDriverConfigurationProvider {
    @Override
    public String getId() {
        return "odin-win-db-dap";
    }

    @Override
    public String getLabel() {
        return "Windows Debugger";
    }

    @Override
    public String[] getExecutableNames() {
        return new String[]{
                "vsdbg.exe"
        };
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public boolean isDownloadable() {
        return true;
    }

    @Override
    public String getVersion() {
        return "";
    }

    @Override
    public DebuggerDriverConfiguration createDebuggerDriverConfiguration(String path) {
        return new WinDAPDriverConfiguration(Path.of(path), this::handshake);
    }

    private @NotNull WinDAPDriver.HandshakeResponse handshake(WinDAPDriver.HandshakeRequest handshake) {
        try {
            final MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(handshake.getValue().getBytes(StandardCharsets.UTF_8));
            Inflater inflater = new Inflater(true);
            final byte[] odinsGift = prayToOdin();
            inflater.setInput(odinsGift, odinsGift.length - 0x80, 77);
            inflater.finished();
            byte[] b = new byte[1];
            while (inflater.inflate(b) > 0) {
                messageDigest.update(b);
            }

            return new WinDAPDriver.HandshakeResponse(new String(odinsGift,
                    odinsGift.length - 0x33,
                    3) + Base64.getEncoder().encodeToString(messageDigest.digest()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] prayToOdin() throws IOException {
        final byte[] wanderer;
        try (InputStream inputStream = WinDAPToolchain.class.getResourceAsStream("/wanderer.jpg")) {
            if (inputStream == null) {
                throw new RuntimeException("Odin has not answered your prayers.");
            }
            wanderer = inputStream.readAllBytes();

        }
        return wanderer;
    }

    public static class WinDAPDriverConfiguration extends DAPDebuggerDriverConfiguration {
        private final Path path;
        private final WinDAPDriver.HandshakeStrategy handshakeStrategy;

        public WinDAPDriverConfiguration(Path path, WinDAPDriver.HandshakeStrategy handshakeStrategy) {
            this.path = path;
            this.handshakeStrategy = handshakeStrategy;
        }

        protected Path getDebuggerExecutable() {
            return path;
        }

        @Override
        public @NotNull String getDriverName() {
            return "Odin Windows Debugger";
        }

        @Override
        public @NotNull DebuggerDriver createDriver(DebuggerDriver.@NotNull Handler handler, @NotNull ArchitectureType architectureType)
                throws ExecutionException {
            return new WinDAPDriver(handshakeStrategy, handler, this, OdinDebuggerLanguage.INSTANCE);
        }

        @Override
        public @NotNull GeneralCommandLine createDriverCommandLine(
                @NotNull DebuggerDriver debuggerDriver, @NotNull ArchitectureType architectureType) {
            val path = getDebuggerExecutable();
            val cli = new GeneralCommandLine();
            cli.setExePath(path.toString());
            cli.addParameters("--interpreter=vscode", "--extConfigDir=%USERPROFILE%\\.cppvsdbg\\extensions");
            cli.setWorkDirectory(path.getParent().toString());
            return cli;
        }

        @Override
        public void customizeInitializeArguments(InitializeRequestArguments initArgs) {
            initArgs.setPathFormat("path");
            initArgs.setAdapterID("cppvsdbg");
        }
    }

    private static @NotNull String getDownloadUrl() {
        String x64 = "x64";
        return "https://github.com/microsoft/vscode-cpptools/releases/download/v1.21.6/cpptools-windows-" + x64 + ".vsix";
    }

    @Override
    public String detect() {
        Path debuggerPath = PathManager.getSystemDir().resolve("odin/debuggers/" + getId() + "/extension/debugAdapters/vsdbg/bin/vsdbg.exe");
        if(debuggerPath.toFile().exists())
            return debuggerPath.toAbsolutePath().toString();
        return null;
    }

    @Override
    public String download(Project project) {
        String debuggerExecutablePath = null;

        String detectedPath = detect();
        if(detectedPath != null) {
            int answer = Messages.showOkCancelDialog("A windows debugger was detected on your system. Would you like to use that instead?",
                    "Debugger Detected",
                    "Yes",
                    "No, Download Again",
                    UIUtil.getInformationIcon());

            if(answer == Messages.OK) {
                return detectedPath;
            }
        }

        DownloadableFileDescription downloadableFileDescription = DownloadableFileService
                .getInstance()
                .createFileDescription(getDownloadUrl(), "cpptools.vsix");

        FileDownloader fileDownloader = DownloadableFileService
                .getInstance()
                .createDownloader(List.of(downloadableFileDescription), "Debugger");

        Path downloadDir = Path.of(PathManager.getTempPath())
                .resolve("odin/debuggers/" + getId());
        try {
            FileUtil.deleteRecursively(downloadDir);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        boolean result = downloadDir.toFile().mkdirs();
        if (!result)
            throw new RuntimeException("Could not create temp dirs");

        @Nullable List<Pair<VirtualFile, DownloadableFileDescription>> downloads = fileDownloader
                .downloadWithProgress(downloadDir.toString(), project, null);
        if (downloads != null) {
            for (Pair<VirtualFile, DownloadableFileDescription> download : downloads) {
                var file = download.getFirst();
                Decompressor.Zip zip = new Decompressor.Zip(file.toNioPath());
                Path extractionTargetPath = PathManager.getSystemDir().resolve("odin/debuggers/" + getId());
                boolean extraction = ProgressManager.getInstance().runProcessWithProgressSynchronously(
                        () -> {
                            try {

                                zip.extract(extractionTargetPath);
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                        },
                        "Extracting Downloaded Contents",
                        true,
                        project
                );
                if (extraction) {
                    debuggerExecutablePath = extractionTargetPath.resolve("extension/debugAdapters/vsdbg/bin/vsdbg.exe").toString();
                }

            }
            try {
                FileUtil.deleteRecursively(downloadDir);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

        }
        return debuggerExecutablePath;
    }

}
