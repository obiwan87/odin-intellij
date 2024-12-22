package com.lasagnerd.odin.lang.stubs.types;

import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.lasagnerd.odin.lang.psi.OdinShortVariableDeclaration;
import com.lasagnerd.odin.lang.psi.impl.OdinShortVariableDeclarationImpl;
import com.lasagnerd.odin.lang.stubs.OdinShortVariableDeclarationStub;
import com.lasagnerd.odin.lang.stubs.OdinStubElementTypeFactory;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class OdinShortVariableDeclarationStubElementType extends OdinDeclarationStubElementType<OdinShortVariableDeclarationStub, OdinShortVariableDeclaration> {
    public OdinShortVariableDeclarationStubElementType() {
        super(OdinStubElementTypeFactory.SHORT_VARIABLE_DECLARATION);
    }

    @Override
    public OdinShortVariableDeclaration createPsi(@NotNull OdinShortVariableDeclarationStub stub) {
        return new OdinShortVariableDeclarationImpl(stub, this);
    }

    @Override
    public @NotNull OdinShortVariableDeclarationStub createStub(@NotNull OdinShortVariableDeclaration psi, StubElement<? extends PsiElement> parentStub) {
        return new OdinShortVariableDeclarationStub(parentStub, this, getNames(psi));
    }


    @Override
    public void serialize(@NotNull OdinShortVariableDeclarationStub stub, @NotNull StubOutputStream dataStream) throws IOException {
        super.serialize(stub, dataStream);
    }

    @Override
    public @NotNull OdinShortVariableDeclarationStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
        return new OdinShortVariableDeclarationStub(parentStub, this, dataStream.readName());
    }
}
