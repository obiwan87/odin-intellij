package com.lasagnerd.odin.lang.stubs;

import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.lasagnerd.odin.lang.psi.OdinProcedureDeclarationStatement;
import org.jetbrains.annotations.Nullable;

public class OdinProcedureDeclarationStub extends StubBase<OdinProcedureDeclarationStatement> {
    public OdinProcedureDeclarationStub(@Nullable StubElement parent, IStubElementType elementType) {
        super(parent, elementType);
    }
}
