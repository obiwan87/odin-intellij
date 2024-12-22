package com.lasagnerd.odin.lang.stubs;

import com.intellij.psi.stubs.PsiFileStubImpl;
import com.intellij.psi.stubs.StubElement;
import com.lasagnerd.odin.lang.psi.OdinFile;
import com.lasagnerd.odin.lang.psi.OdinPackageClause;
import com.lasagnerd.odin.lang.stubs.types.OdinFileScopeStubElementType;
import com.lasagnerd.odin.lang.stubs.types.OdinPackageClauseStubElementType;
import org.jetbrains.annotations.Nullable;

public class OdinFileStub extends PsiFileStubImpl<OdinFile> {

    public OdinFileStub(OdinFile file) {
        super(file);
    }

    @Nullable
    public String getPackageName() {
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
