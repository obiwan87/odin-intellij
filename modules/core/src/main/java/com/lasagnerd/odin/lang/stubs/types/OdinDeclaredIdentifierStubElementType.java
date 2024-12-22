package com.lasagnerd.odin.lang.stubs.types;

import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.lasagnerd.odin.lang.psi.OdinDeclaredIdentifier;
import com.lasagnerd.odin.lang.psi.impl.OdinDeclaredIdentifierImpl;
import com.lasagnerd.odin.lang.stubs.OdinDeclaredIdentifierStub;
import com.lasagnerd.odin.lang.stubs.OdinStubElementTypeFactory;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class OdinDeclaredIdentifierStubElementType extends OdinStubElementType<OdinDeclaredIdentifierStub, OdinDeclaredIdentifier> {

    public OdinDeclaredIdentifierStubElementType() {
        super(OdinStubElementTypeFactory.DECLARED_IDENTIFIER);
    }

    @Override
    public OdinDeclaredIdentifier createPsi(@NotNull OdinDeclaredIdentifierStub stub) {
        return new OdinDeclaredIdentifierImpl(stub, this);
    }

    @Override
    public @NotNull OdinDeclaredIdentifierStub createStub(@NotNull OdinDeclaredIdentifier psi, StubElement<? extends PsiElement> parentStub) {
        return new OdinDeclaredIdentifierStub(parentStub, this, psi.getName());
    }

    @Override
    public void serialize(@NotNull OdinDeclaredIdentifierStub stub, @NotNull StubOutputStream dataStream) throws IOException {
        dataStream.writeName(stub.getName());
    }

    @Override
    public @NotNull OdinDeclaredIdentifierStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
        return new OdinDeclaredIdentifierStub(parentStub, this, dataStream.readName());
    }
}
