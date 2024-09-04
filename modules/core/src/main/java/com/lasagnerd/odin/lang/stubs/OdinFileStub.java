package com.lasagnerd.odin.lang.stubs;

import com.intellij.psi.stubs.PsiFileStubImpl;
import com.intellij.util.io.StringRef;
import com.lasagnerd.odin.lang.psi.OdinFile;

public class OdinFileStub extends PsiFileStubImpl<OdinFile> {
    StringRef buildFlag;
    public OdinFileStub(OdinFile file) {
        super(file);
    }

}
