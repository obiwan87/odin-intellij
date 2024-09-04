package com.lasagnerd.odin.lang.stubs.types;

import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.lasagnerd.odin.lang.psi.OdinProcedureDeclarationStatement;
import com.lasagnerd.odin.lang.psi.impl.OdinProcedureDeclarationStatementImpl;
import com.lasagnerd.odin.lang.stubs.OdinProcedureDeclarationStub;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class OdinProcedureDeclarationStubElementType extends OdinStubElementType<OdinProcedureDeclarationStub, OdinProcedureDeclarationStatement> {
    public OdinProcedureDeclarationStubElementType(@NotNull String debugName) {
        super(debugName);
    }

    @Override
    public OdinProcedureDeclarationStatement createPsi(@NotNull OdinProcedureDeclarationStub stub) {
        return new OdinProcedureDeclarationStatementImpl(stub, this);
    }

    @Override
    public @NotNull OdinProcedureDeclarationStub createStub(@NotNull OdinProcedureDeclarationStatement psi, StubElement<? extends PsiElement> parentStub) {
        return new OdinProcedureDeclarationStub(parentStub, this);
    }

    @Override
    public void serialize(@NotNull OdinProcedureDeclarationStub stub, @NotNull StubOutputStream dataStream) throws IOException {

    }

    @Override
    public @NotNull OdinProcedureDeclarationStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
        return new OdinProcedureDeclarationStub(parentStub, this);
    }
}
