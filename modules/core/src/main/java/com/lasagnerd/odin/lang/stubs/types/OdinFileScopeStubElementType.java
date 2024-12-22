package com.lasagnerd.odin.lang.stubs.types;

import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.lasagnerd.odin.lang.psi.OdinFileScope;
import com.lasagnerd.odin.lang.psi.impl.OdinFileScopeImpl;
import com.lasagnerd.odin.lang.stubs.OdinFileScopeStub;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class OdinFileScopeStubElementType extends OdinStubElementType<OdinFileScopeStub, OdinFileScope> {


    public OdinFileScopeStubElementType(@NotNull String debugName) {
        super(debugName);
    }

    @Override
    public OdinFileScope createPsi(@NotNull OdinFileScopeStub stub) {
        return new OdinFileScopeImpl(stub, this);
    }

    @Override
    public @NotNull OdinFileScopeStub createStub(@NotNull OdinFileScope psi, StubElement<? extends PsiElement> parentStub) {
        return new OdinFileScopeStub(parentStub, this);
    }

    @Override
    public void serialize(@NotNull OdinFileScopeStub stub, @NotNull StubOutputStream dataStream) {

    }

    @Override
    public @NotNull OdinFileScopeStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
        return new OdinFileScopeStub(parentStub, this);
    }
}
