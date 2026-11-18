package com.lasagnerd.odin.ols;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class OlsConfigurationTest {
    @Test
    public void readsOlsFieldNamesAndIgnoresUnknownFields() {
        OlsConfiguration configuration = OlsConfiguration.read("""
                {
                  "$schema": "https://example.com/ols.schema.json",
                  "thread_pool_count": 4,
                  "enable_format": false,
                  "enable_inlay_hints_params": false,
                  "checker_args": "-vet",
                  "odin_command": "/opt/odin/odin",
                  "fileLog": true,
                  "profile": "linux",
                  "collections": [{"name": "shared", "path": "../shared"}],
                  "profiles": [{
                    "name": "linux",
                    "os": "linux",
                    "checker_path": ["src", "tests"],
                    "future_profile_option": true
                  }],
                  "future_option": true
                }
                """);

        assertNotNull(configuration);
        assertEquals("https://example.com/ols.schema.json", configuration.getSchema());
        assertEquals(Integer.valueOf(4), configuration.getThreadPoolCount());
        assertEquals(Boolean.FALSE, configuration.getEnableFormat());
        assertEquals(Boolean.FALSE, configuration.getEnableInlayHintsParams());
        assertEquals("-vet", configuration.getCheckerArgs());
        assertEquals("/opt/odin/odin", configuration.getOdinCommand());
        assertEquals(Boolean.TRUE, configuration.getFileLog());
        assertEquals("linux", configuration.getProfile());
        assertEquals("shared", configuration.getCollections().getFirst().getName());
        assertEquals("../shared", configuration.getCollections().getFirst().getPath());
        OlsProfile profile = configuration.getProfiles().getFirst();
        assertEquals("linux", profile.getName());
        assertEquals("linux", profile.getOs());
        assertEquals(List.of("src", "tests"), profile.getCheckerPath());
    }

    @Test
    public void preservesDefaultsForMissingOptions() {
        OlsConfiguration configuration = OlsConfiguration.read("{}");

        assertNotNull(configuration);
        assertEquals(Boolean.TRUE, configuration.getEnableFormat());
        assertEquals(Boolean.TRUE, configuration.getEnableInlayHintsParams());
        assertEquals(Boolean.TRUE, configuration.getEnableInlayHintsDefaultParams());
        assertEquals(Boolean.TRUE, configuration.getEnableProcedureSnippet());
        assertNull(configuration.getCheckerArgs());
        assertNull(configuration.getProfiles());
    }

    @Test
    public void returnsNullForInvalidConfiguration() {
        assertNull(OlsConfiguration.read("{\"profile\":"));
        assertNull(OlsConfiguration.read("[]"));
        assertNull(OlsConfiguration.read("null"));
    }
}
