package com.lasagnerd.odin.lang.stubs.types;

import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.lasagnerd.odin.lang.psi.OdinConstantInitDeclaration;
import com.lasagnerd.odin.lang.psi.impl.OdinConstantInitDeclarationImpl;
import com.lasagnerd.odin.lang.stubs.OdinConstantInitDeclarationStub;
import com.lasagnerd.odin.lang.stubs.OdinStubElementTypeFactory;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class OdinConstantInitDeclarationStubElementType extends OdinDeclarationStubElementType<OdinConstantInitDeclarationStub, OdinConstantInitDeclaration> {
    public OdinConstantInitDeclarationStubElementType() {
        super(OdinStubElementTypeFactory.CONSTANT_INIT_DECLARATION);
    }

    @Override
    public OdinConstantInitDeclaration createPsi(@NotNull OdinConstantInitDeclarationStub stub) {
        return new OdinConstantInitDeclarationImpl(stub, this);
    }

    @Override
    public @NotNull OdinConstantInitDeclarationStub createStub(@NotNull OdinConstantInitDeclaration psi, StubElement<? extends PsiElement> parentStub) {
        return new OdinConstantInitDeclarationStub(parentStub, this, getNames(psi));
    }

    @Override
    public @NotNull OdinConstantInitDeclarationStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
        return new OdinConstantInitDeclarationStub(parentStub, this, dataStream.readName());
    }
}
