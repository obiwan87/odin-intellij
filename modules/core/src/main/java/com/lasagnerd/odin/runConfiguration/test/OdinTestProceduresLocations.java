package com.lasagnerd.odin.runConfiguration.test;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public record OdinTestProceduresLocations(Map<String, Path> procedureToFilePath, Map<Path, List<String>> fileToProcedureName) {
    static OdinTestProceduresLocations EMPTY = new OdinTestProceduresLocations(Collections.emptyMap(), Collections.emptyMap());
}
