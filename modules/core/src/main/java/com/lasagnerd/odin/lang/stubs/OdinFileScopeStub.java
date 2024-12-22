package com.lasagnerd.odin.lang.stubs;

import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.lasagnerd.odin.lang.psi.OdinFileScope;
import org.jetbrains.annotations.Nullable;

public class OdinFileScopeStub extends StubBase<OdinFileScope> {
    public OdinFileScopeStub(@Nullable StubElement parent, IStubElementType<?, ?> elementType) {
        super(parent, elementType);
    }
}
