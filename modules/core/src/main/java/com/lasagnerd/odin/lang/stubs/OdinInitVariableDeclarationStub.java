package com.lasagnerd.odin.lang.stubs;

import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.lasagnerd.odin.lang.psi.OdinInitVariableDeclaration;
import org.jetbrains.annotations.Nullable;

public class OdinInitVariableDeclarationStub extends StubBase<OdinInitVariableDeclaration> {
    public OdinInitVariableDeclarationStub(@Nullable StubElement parent, IStubElementType<?, ?> elementType) {
        super(parent, elementType);
    }
}
