package com.lasagnerd.odin.lang.stubs.types;

import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.lasagnerd.odin.lang.psi.OdinVariableInitializationStatement;
import com.lasagnerd.odin.lang.stubs.OdinVariableInitializationStub;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class OdinVariableInitializationStubElementType extends OdinStubElementType<OdinVariableInitializationStub, OdinVariableInitializationStatement> {
    public OdinVariableInitializationStubElementType(@NotNull String debugName) {
        super(debugName);
    }


    @Override
    public OdinVariableInitializationStatement createPsi(@NotNull OdinVariableInitializationStub stub) {
//        return new OdinVariableInitializationStatementImpl(stub, this);
        return null;
    }

    @Override
    public @NotNull OdinVariableInitializationStub createStub(@NotNull OdinVariableInitializationStatement psi, StubElement<? extends PsiElement> parentStub) {
        return new OdinVariableInitializationStub(parentStub, this);
    }

    @Override
    public void serialize(@NotNull OdinVariableInitializationStub stub, @NotNull StubOutputStream dataStream) throws IOException {

    }

    @Override
    public @NotNull OdinVariableInitializationStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
        return new OdinVariableInitializationStub(parentStub, this);
    }

}
