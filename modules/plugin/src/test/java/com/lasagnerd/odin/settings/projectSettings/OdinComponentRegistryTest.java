package com.lasagnerd.odin.settings.projectSettings;

import junit.framework.TestCase;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;

public class OdinComponentRegistryTest extends TestCase {
    public void testEquivalentSdksAreReused() {
        OdinSdkRegistryServiceImpl service=new OdinSdkRegistryServiceImpl();
        OdinSdkState first=new OdinSdkState();first.compilerPath="C:/bin/odin.exe";first.libraryPath="C:/Odin/";
        OdinSdkState second=new OdinSdkState();second.compilerPath="C:\\bin\\odin.exe";second.libraryPath="C:\\Odin";
        assertSame(service.findOrAddEquivalent(first),service.findOrAddEquivalent(second));
        assertEquals(1,service.getSdks().size());
    }
    public void testArchiveBaseNameAndDefaultLocation() {
        assertEquals("odin-linux-amd64-dev-2026-07a", OdinSdkDownloader.archiveBaseName("odin-linux-amd64-dev-2026-07a.tar.gz"));
        assertEquals("odin-windows-amd64-dev-2026-07a", OdinSdkDownloader.archiveBaseName("odin-windows-amd64-dev-2026-07a.zip"));
        assertTrue(OdinSdkDownloader.defaultInstallDirectory("odin-linux-amd64-dev-2026-07a.tar.gz").toString().contains(".odin-sdks"));
        assertEquals("Linux / AMD64", OdinDownloadSdkDialog.platformLabel("odin-linux-amd64-dev-2026-07a.tar.gz"));
    }
    public void testDistDirectoryBecomesContentRoot() throws Exception {
        Path extracted=Files.createTempDirectory("odin-sdk-layout");
        try {
            Path dist=Files.createDirectories(extracted.resolve("dist"));
            Files.createDirectory(dist.resolve("base"));
            assertEquals(dist,OdinSdkDownloader.selectContentRoot(extracted));
        } finally {
            try(var paths=Files.walk(extracted)){paths.sorted(java.util.Comparator.reverseOrder()).forEach(p->{try{Files.deleteIfExists(p);}catch(Exception ignored){}});}
        }
    }
    public void testEquivalentDebuggersAreReused() {
        OdinDebuggerRegistryServiceImpl service=new OdinDebuggerRegistryServiceImpl();
        OdinDebuggerState first=new OdinDebuggerState();first.implementationId="lldb-dap";first.executablePath="C:/LLVM/lldb-dap.exe";
        OdinDebuggerState second=new OdinDebuggerState();second.implementationId="lldb-dap";second.executablePath="C:\\LLVM\\lldb-dap.exe";
        assertSame(service.findOrAddEquivalent(first),service.findOrAddEquivalent(second));
    }
    public void testReleaseAssetMatchingIsPlatformSpecific() {
        OdinRelease.Asset windows=new OdinRelease.Asset(1,"odin-windows-amd64-dev-2026-07.zip",1,"url",null);
        OdinRelease.Asset linux=new OdinRelease.Asset(2,"odin-linux-amd64-dev-2026-07.tar.gz",1,"url",null);
        OdinRelease release=new OdinRelease(1,"dev-2026-07","", "","",false,false,List.of(windows,linux));
        assertSame(windows,OdinReleaseService.compatibleAsset(release,"windows","amd64"));
        assertSame(linux,OdinReleaseService.compatibleAsset(release,"linux","amd64"));
        assertNull(OdinReleaseService.compatibleAsset(release,"macos","arm64"));
    }
}
