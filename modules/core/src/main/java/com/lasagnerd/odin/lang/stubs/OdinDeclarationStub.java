package com.lasagnerd.odin.lang.stubs;

import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.io.StringRef;
import com.lasagnerd.odin.lang.psi.OdinDeclaration;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

@Getter
public class OdinDeclarationStub<S extends OdinDeclaration> extends StubBase<S> {

    private List<String> names;

    protected OdinDeclarationStub(@Nullable StubElement parent, IStubElementType<?, ?> elementType, List<String> names) {
        super(parent, elementType);
        this.names = names;
    }

    protected OdinDeclarationStub(@Nullable StubElement parent, IStubElementType<?, ?> elementType, StringRef names) {
        super(parent, elementType);
        if (names != null) {
            this.names = Arrays.stream(names.getString().split(",")).map(String::trim).toList();
        }
    }

    public boolean isPublic() {
        return true;
    }
}
