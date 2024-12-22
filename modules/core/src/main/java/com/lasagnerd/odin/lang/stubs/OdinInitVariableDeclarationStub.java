package com.lasagnerd.odin.lang.stubs;

import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.io.StringRef;
import com.lasagnerd.odin.lang.psi.OdinInitVariableDeclaration;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class OdinInitVariableDeclarationStub extends OdinDeclarationStub<OdinInitVariableDeclaration> {
    public OdinInitVariableDeclarationStub(@Nullable StubElement parent, IStubElementType<?, ?> elementType, List<String> names) {
        super(parent, elementType, names);
    }

    public OdinInitVariableDeclarationStub(@Nullable StubElement parent, IStubElementType<?, ?> elementType, StringRef names) {
        super(parent, elementType, names);
    }
}
