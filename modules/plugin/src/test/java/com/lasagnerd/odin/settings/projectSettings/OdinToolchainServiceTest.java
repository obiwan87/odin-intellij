package com.lasagnerd.odin.settings.projectSettings;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class OdinToolchainServiceTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void equivalentToolchainsAreReused() {
        OdinToolchainServiceImpl service = new OdinToolchainServiceImpl();
        OdinToolchainState first = toolchain("C:/Odin/odin.exe", "C:/Odin/", "lldb", "C:/LLVM/lldb.exe");
        OdinToolchainState second = toolchain("C:\\Odin\\odin.exe", "C:\\Odin", "lldb", "C:\\LLVM\\lldb.exe");

        OdinToolchainState stored = service.findOrAddEquivalent(first);

        assertSame(stored, service.findOrAddEquivalent(second));
        assertEquals(1, service.getToolchains().size());
    }

    @Test
    public void differentCompilerPathsAreNotMerged() {
        OdinToolchainServiceImpl service = new OdinToolchainServiceImpl();
        service.findOrAddEquivalent(toolchain("/opt/odin-dev/bin/odin", "/opt/odin/share", "lldb", "/usr/bin/lldb-dap"));
        service.findOrAddEquivalent(toolchain("/opt/odin-stable/bin/odin", "/opt/odin/share", "lldb", "/usr/bin/lldb-dap"));

        assertEquals(2, service.getToolchains().size());
    }

    @Test
    public void legacyProjectKeepsItsLibraryAndDebuggerValues() {
        OdinProjectSettingsState legacy = new OdinProjectSettingsState();
        legacy.sdkPath = "C:/Odin";
        legacy.debuggerId = "lldb-dap";
        legacy.debuggerPath = "C:/LLVM/lldb-dap.exe";
        OdinToolchainState migrated = new OdinToolchainState();

        OdinProjectToolchainService.copyLegacyToolchainFields(legacy, migrated);

        assertEquals("C:/Odin", migrated.libraryPath);
        assertEquals(OdinSdkUtils.getOdinBinaryPath("C:/Odin"), migrated.compilerPath);
        assertEquals("lldb-dap", migrated.debuggerId);
        assertEquals("C:/LLVM/lldb-dap.exe", migrated.debuggerPath);
        assertEquals("C:/Odin", legacy.sdkPath);
    }

    @Test
    public void infersLibraryRootBesideCompiler() throws IOException {
        File root = temporaryFolder.newFolder("odin");
        new File(root, "base").mkdir();
        File compiler = new File(root, com.intellij.openapi.util.SystemInfo.isWindows ? "odin.exe" : "odin");
        compiler.createNewFile();

        assertEquals(root.toPath().toString(), OdinToolchainsConfigurable.inferLibraryPath(compiler.getPath()));
    }

    @Test
    public void infersCompilerBesideLibraryRoot() throws IOException {
        File root = temporaryFolder.newFolder("odin");
        new File(root, "base").mkdir();
        File compiler = new File(root, com.intellij.openapi.util.SystemInfo.isWindows ? "odin.exe" : "odin");
        compiler.createNewFile();

        assertEquals(compiler.toPath().toString(), OdinToolchainsConfigurable.inferCompilerPath(root.getPath()));
    }

    private static OdinToolchainState toolchain(String compiler, String libraries, String debuggerId, String debugger) {
        OdinToolchainState state = new OdinToolchainState();
        state.compilerPath = compiler;
        state.libraryPath = libraries;
        state.debuggerId = debuggerId;
        state.debuggerPath = debugger;
        return state;
    }
}
