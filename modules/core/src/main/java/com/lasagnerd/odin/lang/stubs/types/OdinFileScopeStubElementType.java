package com.lasagnerd.odin.lang.stubs.types;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.lasagnerd.odin.lang.psi.OdinFileScope;
import com.lasagnerd.odin.lang.psi.impl.OdinFileScopeImpl;
import com.lasagnerd.odin.lang.stubs.OdinFileScopeStub;
import com.lasagnerd.odin.lang.stubs.OdinStubElementTypeFactory;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class OdinFileScopeStubElementType extends OdinStubElementType<OdinFileScopeStub, OdinFileScope> {


    public static final OdinFileScopeStubElementType INSTANCE = new OdinFileScopeStubElementType();

    public OdinFileScopeStubElementType() {
        super(OdinStubElementTypeFactory.FILE_SCOPE);
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
    public void serialize(@NotNull OdinFileScopeStub stub, @NotNull StubOutputStream dataStream) throws IOException {
//        dataStream.writeName("file_scope");
    }

    @Override
    public @NotNull OdinFileScopeStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
        return new OdinFileScopeStub(parentStub, this);
    }

    @Override
    public boolean shouldCreateStub(ASTNode node) {
        return super.shouldCreateStub(node);
    }
}
