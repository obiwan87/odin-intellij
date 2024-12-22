package com.lasagnerd.odin.lang.stubs.types;

import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.lasagnerd.odin.lang.psi.OdinInitVariableDeclaration;
import com.lasagnerd.odin.lang.psi.impl.OdinInitVariableDeclarationImpl;
import com.lasagnerd.odin.lang.stubs.OdinInitVariableDeclarationStub;
import com.lasagnerd.odin.lang.stubs.OdinStubElementTypeFactory;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class OdinInitVariableDeclarationStubElementType extends OdinDeclarationStubElementType<OdinInitVariableDeclarationStub, OdinInitVariableDeclaration> {
    public OdinInitVariableDeclarationStubElementType() {
        super(OdinStubElementTypeFactory.INIT_VARIABLE_DECLARATION);
    }

    @Override
    public OdinInitVariableDeclaration createPsi(@NotNull OdinInitVariableDeclarationStub stub) {
        return new OdinInitVariableDeclarationImpl(stub, this);
    }

    @Override
    public @NotNull OdinInitVariableDeclarationStub createStub(@NotNull OdinInitVariableDeclaration psi, StubElement<? extends PsiElement> parentStub) {
        return new OdinInitVariableDeclarationStub(parentStub, this, getNames(psi));
    }

    @Override
    public void serialize(@NotNull OdinInitVariableDeclarationStub stub, @NotNull StubOutputStream dataStream) throws IOException {
        super.serialize(stub, dataStream);
    }

    @Override
    public @NotNull OdinInitVariableDeclarationStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
        return new OdinInitVariableDeclarationStub(parentStub, this, dataStream.readName());
    }
}
