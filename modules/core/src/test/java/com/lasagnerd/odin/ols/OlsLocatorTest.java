package com.lasagnerd.odin.ols;

import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

public class OlsLocatorTest {
    @Test
    public void explicitPathWinsOverPath() {
        Optional<Path> found = OlsLocator.find(
                Map.of("OLS_PATH", "/custom/ols", "PATH", "/usr/bin"), "/home/u", false, p -> true);
        Assert.assertEquals(Path.of("/custom/ols"), found.orElseThrow());
    }

    @Test
    public void searchesPathThenHome() {
        Path expected = Path.of("/home/u", ".local", "bin", "ols");
        Optional<Path> found = OlsLocator.find(
                Map.of("PATH", "/usr/bin:/opt"), "/home/u", false, expected::equals);
        Assert.assertEquals(expected, found.orElseThrow());
    }

    @Test
    public void emptyWhenNothingExists() {
        Assert.assertTrue(OlsLocator.find(Map.of("PATH", "/usr/bin"), "/home/u", false, p -> false).isEmpty());
    }
}
