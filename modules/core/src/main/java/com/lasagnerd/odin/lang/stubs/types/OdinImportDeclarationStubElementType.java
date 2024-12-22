package com.lasagnerd.odin.lang.stubs.types;

import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.lasagnerd.odin.lang.psi.OdinImportDeclaration;
import com.lasagnerd.odin.lang.psi.impl.OdinImportDeclarationImpl;
import com.lasagnerd.odin.lang.stubs.OdinImportDeclarationStub;
import com.lasagnerd.odin.lang.stubs.OdinStubElementTypeFactory;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class OdinImportDeclarationStubElementType extends OdinDeclarationStubElementType<OdinImportDeclarationStub, OdinImportDeclaration> {
    public OdinImportDeclarationStubElementType() {
        super(OdinStubElementTypeFactory.IMPORT_DECLARATION);
    }

    @Override
    public OdinImportDeclaration createPsi(@NotNull OdinImportDeclarationStub stub) {
        return new OdinImportDeclarationImpl(stub, this);
    }

    @Override
    public @NotNull OdinImportDeclarationStub createStub(@NotNull OdinImportDeclaration psi, StubElement<? extends PsiElement> parentStub) {
        return new OdinImportDeclarationStub(parentStub, this, getNames(psi));
    }

    @Override
    public void serialize(@NotNull OdinImportDeclarationStub stub, @NotNull StubOutputStream dataStream) throws IOException {
        super.serialize(stub, dataStream);
    }

    @Override
    public @NotNull OdinImportDeclarationStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
        return new OdinImportDeclarationStub(parentStub, this, dataStream.readName());
    }
}
