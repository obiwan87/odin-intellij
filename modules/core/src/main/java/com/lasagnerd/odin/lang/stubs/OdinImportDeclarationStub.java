package com.lasagnerd.odin.lang.stubs;

import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.io.StringRef;
import com.lasagnerd.odin.lang.psi.OdinImportDeclaration;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class OdinImportDeclarationStub extends OdinDeclarationStub<OdinImportDeclaration> {
    public OdinImportDeclarationStub(@Nullable StubElement parent, IStubElementType<?, ?> elementType, List<String> names) {
        super(parent, elementType, names);
    }

    public OdinImportDeclarationStub(@Nullable StubElement parent, IStubElementType<?, ?> elementType, StringRef names) {
        super(parent, elementType, names);
    }
}
