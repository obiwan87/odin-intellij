package com.lasagnerd.odin.lang.stubs;

import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.NamedStubBase;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.io.StringRef;
import com.lasagnerd.odin.lang.psi.OdinDeclaredIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdinDeclaredIdentifierStub extends NamedStubBase<OdinDeclaredIdentifier> {

    public OdinDeclaredIdentifierStub(StubElement parent, @NotNull IStubElementType elementType, @Nullable StringRef name) {
        super(parent, elementType, name);

    }

    public OdinDeclaredIdentifierStub(StubElement parent, @NotNull IStubElementType elementType, @Nullable String name) {
        super(parent, elementType, name);
    }
}
