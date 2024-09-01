package com.lasagnerd.odin.liveTemplates;

import com.intellij.codeInsight.template.TemplateActionContext;
import com.intellij.codeInsight.template.TemplateContextType;
import com.lasagnerd.odin.lang.psi.OdinFile;
import org.jetbrains.annotations.NotNull;

public class OdinLiveTemplateContextType extends TemplateContextType {
    protected OdinLiveTemplateContextType() {
        super("Odin");
    }

    @Override
    public boolean isInContext(@NotNull TemplateActionContext templateActionContext) {
        return templateActionContext.getFile() instanceof OdinFile;
    }
}
