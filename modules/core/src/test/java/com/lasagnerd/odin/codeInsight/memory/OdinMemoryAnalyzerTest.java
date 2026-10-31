package com.lasagnerd.odin.codeInsight.memory;

import com.lasagnerd.odin.codeInsight.memory.OdinMemoryAnalyzer.Kind;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class OdinMemoryAnalyzerTest {
    private static List<Kind> kinds(String body) {
        return OdinMemoryAnalyzer.analyze("main :: proc() {\n" + body + "\n}\n").stream()
                .map(OdinMemoryAnalyzer.Problem::kind).toList();
    }

    @Test
    public void reportsLeak() {
        Assert.assertEquals(List.of(Kind.LEAK), kinds("x := make([]int, 4)"));
    }

    @Test
    public void deferredDeleteIsNotALeak() {
        Assert.assertEquals(List.of(), kinds("x := make([]int, 4)\ndefer delete(x)\nx[0] = 1"));
    }

    @Test
    public void tempAllocatorAndReturnAreNotLeaks() {
        Assert.assertEquals(List.of(), kinds("x := make([]int, 4, context.temp_allocator)"));
        Assert.assertEquals(List.of(), kinds("x := new(int)\nreturn x"));
    }

    @Test
    public void commentedCodeIsIgnored() {
        Assert.assertEquals(List.of(), kinds("// x := make([]int, 4)\ns := \"y := new(int)\""));
    }

    @Test
    public void reportsDanglingPointer() {
        Assert.assertEquals(List.of(Kind.DANGLING_POINTER), kinds("x := 4\nreturn &x"));
    }

    @Test
    public void reportsUseAfterFreeAndDoubleFree() {
        Assert.assertEquals(List.of(Kind.USE_AFTER_FREE), kinds("x := new(int)\nfree(x)\nx^ = 1"));
        Assert.assertEquals(List.of(Kind.DOUBLE_FREE), kinds("x := new(int)\nfree(x)\nfree(x)"));
    }

    @Test
    public void reportsOnlyUncheckedAssertions() {
        Assert.assertEquals(List.of(Kind.UNCHECKED_ASSERTION), kinds("v := u.(int)"));
        Assert.assertEquals(List.of(), kinds("v, ok := u.(int)"));
    }

    @Test
    public void reassignmentAndBranchesAreNotReported() {
        Assert.assertEquals(List.of(), kinds("x := new(int)\nfree(x)\nx = new(int)\nfree(x)"));
        Assert.assertEquals(List.of(), kinds("x := new(int)\nif a { free(x) } else { free(x) }"));
    }
}
