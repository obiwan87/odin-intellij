package com.lasagnerd.odin.codeInsight.memory;

import org.junit.Assert;
import org.junit.Test;

public class OdinMissingImportAnalyzerTest {
    @Test
    public void reportsMissingImportOnce() {
        var missing = OdinMissingImportAnalyzer.analyze("package m\nmain :: proc() { fmt.println(1); fmt.println(2) }");
        Assert.assertEquals(1, missing.size());
        Assert.assertEquals("core:fmt", missing.getFirst().importPath());
    }

    @Test
    public void ignoresImportedAliasedAndShadowedNames() {
        Assert.assertTrue(OdinMissingImportAnalyzer.analyze("import \"core:fmt\"\nx := fmt.tprint()").isEmpty());
        Assert.assertTrue(OdinMissingImportAnalyzer.analyze("import fmt \"my:lib\"\nx := fmt.a").isEmpty());
        Assert.assertTrue(OdinMissingImportAnalyzer.analyze("f :: proc(time: T) { x := time.y }").isEmpty());
        Assert.assertTrue(OdinMissingImportAnalyzer.analyze("// fmt.println()").isEmpty());
    }
}
