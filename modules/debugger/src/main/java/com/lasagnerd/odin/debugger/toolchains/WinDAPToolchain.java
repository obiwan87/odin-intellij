package com.lasagnerd.odin.debugger.toolchains;

import com.jetbrains.cidr.execution.debugger.backend.DebuggerDriverConfiguration;
import com.lasagnerd.odin.debugger.drivers.windap.WinDAPDriver;
import com.lasagnerd.odin.debugger.drivers.windap.WinDAPDriverConfiguration;
import com.lasagnerd.odin.extensions.OdinDebuggerToolchain;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Base64;
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
        return false;
    }

    @Override
    public String getVersion() {
        return "";
    }

    @Override
    public DebuggerDriverConfiguration getDebuggerDriverConfiguration(String path) {
        return new WinDAPDriverConfiguration(Path.of(path), this::handshakeAlgorithm);
    }

    private @NotNull WinDAPDriver.HandshakeResponse handshakeAlgorithm(WinDAPDriver.HandshakeRequest handshake) {
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
}
