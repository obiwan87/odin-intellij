package com.lasagnerd.odin.lang.stubs;

import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.lasagnerd.odin.lang.psi.OdinVariableInitializationStatement;
import org.jetbrains.annotations.Nullable;

public class OdinVariableInitializationStub extends StubBase<OdinVariableInitializationStatement> {
    public OdinVariableInitializationStub(@Nullable StubElement parent, IStubElementType elementType) {
        super(parent, elementType);
    }
}
