package com.lasagnerd.odin.lang.stubs.indexes;

import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import com.lasagnerd.odin.lang.psi.OdinDeclaration;
import org.jetbrains.annotations.NotNull;

public class OdinAllPublicNamesIndex extends StringStubIndexExtension<OdinDeclaration> {
    public static final StubIndexKey<String, OdinDeclaration> ALL_PUBLIC_NAMES = StubIndexKey.createIndexKey("odin.all.name");

    @Override
    public @NotNull StubIndexKey<String, OdinDeclaration> getKey() {
        return ALL_PUBLIC_NAMES;
    }
}
