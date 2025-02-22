package com.lasagnerd.odin.lang.stubs.types;

import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubBase;
import com.lasagnerd.odin.lang.OdinLanguage;
import com.lasagnerd.odin.lang.psi.OdinPsiElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public abstract class OdinStubElementType<S extends StubBase<T>, T extends OdinPsiElement> extends IStubElementType<S, T> {
    public OdinStubElementType(@NotNull @NonNls String debugName) {
        super(debugName, OdinLanguage.INSTANCE);
    }

    @Override
    @NotNull
    public String getExternalId() {
        return "odin." + super.toString();
    }

    @Override
    public void indexStub(@NotNull S stub, @NotNull IndexSink sink) {
    }


}
