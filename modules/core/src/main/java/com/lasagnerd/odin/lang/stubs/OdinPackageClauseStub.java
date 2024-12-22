package com.lasagnerd.odin.lang.stubs;

import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.io.StringRef;
import com.lasagnerd.odin.lang.psi.OdinPackageClause;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
public class OdinPackageClauseStub extends StubBase<OdinPackageClause> {
    private final String name;

    public OdinPackageClauseStub(@Nullable StubElement parent, IStubElementType<?, ?> elementType, String name) {
        super(parent, elementType);
        this.name = name;
    }

    public OdinPackageClauseStub(@Nullable StubElement parent, IStubElementType<?, ?> elementType, StringRef ref) {
        super(parent, elementType);
        this.name = ref != null ? ref.getString() : null;
    }
}
