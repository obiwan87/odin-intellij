package com.lasagnerd.odin.lang.stubs;

import com.intellij.psi.stubs.PsiFileStubImpl;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.io.StringRef;
import com.lasagnerd.odin.lang.psi.OdinFile;
import com.lasagnerd.odin.lang.psi.OdinPackageClause;
import com.lasagnerd.odin.lang.stubs.types.OdinFileScopeStubElementType;
import com.lasagnerd.odin.lang.stubs.types.OdinPackageClauseStubElementType;
import org.jetbrains.annotations.Nullable;

public class OdinFileStub extends PsiFileStubImpl<OdinFile> {

    private final String name;

    public OdinFileStub(OdinFile file, String name) {
        super(file);
        this.name = name;
    }

    public OdinFileStub(OdinFile file, StringRef name) {
        super(file);
        this.name = name != null ? name.getString() : null;
    }

    @Nullable
    public String getPackageName() {
        if (name != null) {
            return name;
        }
        return getPackageClauseName();
    }

    @Nullable
    public String getPackageClauseName() {
        StubElement<OdinPackageClause> stub = getPackageClauseStub();
        return stub instanceof OdinPackageClauseStub packageClauseStub ? packageClauseStub.getName() : null;
    }

    private StubElement<OdinPackageClause> getPackageClauseStub() {
        OdinFileScopeStub fileScopeStub = findChildStubByType(OdinFileScopeStubElementType.INSTANCE);
        if (fileScopeStub != null) {
            return fileScopeStub.findChildStubByType(OdinPackageClauseStubElementType.INSTANCE);
        }
        return null;
    }
}
