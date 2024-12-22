package com.lasagnerd.odin.lang.stubs.types;

import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.lasagnerd.odin.lang.psi.OdinPackageClause;
import com.lasagnerd.odin.lang.psi.impl.OdinPackageClauseImpl;
import com.lasagnerd.odin.lang.stubs.OdinPackageClauseStub;
import com.lasagnerd.odin.lang.stubs.OdinStubElementTypeFactory;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class OdinPackageClauseStubElementType extends OdinStubElementType<OdinPackageClauseStub, OdinPackageClause> {
    public static final OdinPackageClauseStubElementType INSTANCE = new OdinPackageClauseStubElementType();

    public OdinPackageClauseStubElementType() {
        super(OdinStubElementTypeFactory.PACKAGE_CLAUSE);
    }

    @Override
    public OdinPackageClause createPsi(@NotNull OdinPackageClauseStub stub) {
        return new OdinPackageClauseImpl(stub, this);
    }

    @Override
    public @NotNull OdinPackageClauseStub createStub(@NotNull OdinPackageClause psi, StubElement<? extends PsiElement> parentStub) {
        return new OdinPackageClauseStub(parentStub, this, psi.getName());
    }

    @Override
    public void serialize(@NotNull OdinPackageClauseStub stub, @NotNull StubOutputStream dataStream) throws IOException {
        dataStream.writeName(stub.getName());
    }

    @Override
    public @NotNull OdinPackageClauseStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
        return new OdinPackageClauseStub(parentStub, this, dataStream.readName());
    }
}
