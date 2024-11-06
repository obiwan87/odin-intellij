package com.lasagnerd.odin.projectStructure.module.rootTypes.source;

import com.intellij.ide.projectView.actions.MarkSourceRootAction;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NotNull;

public class OdinMarkSourceRootAction extends MarkSourceRootAction {
    public OdinMarkSourceRootAction() {
        super(OdinSourceRootType.INSTANCE);
    }

    @Override
    protected boolean isEnabled(@NotNull RootsSelection selection, @NotNull Module module) {
        return true;
    }


}
