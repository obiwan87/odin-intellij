package com.lasagnerd.odin.projectStructure;

import com.intellij.lang.Language;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.file.PsiPackageBase;
import com.intellij.psi.search.GlobalSearchScope;
import com.lasagnerd.odin.lang.OdinLanguage;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class OdinPsiPackage extends PsiPackageBase {
    public OdinPsiPackage(PsiManager manager, String qualifiedName) {
        super(manager, qualifiedName);
    }

    @Override
    protected Collection<PsiDirectory> getAllDirectories(GlobalSearchScope scope) {
        return List.of();
    }

    @Override
    protected PsiPackageBase findPackage(String qName) {
        return null;
    }

    @Override
    public @NotNull Language getLanguage() {
        return OdinLanguage.INSTANCE;
    }
}
