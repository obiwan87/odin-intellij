package com.lasagnerd.odin.lang.psi;

import java.util.List;

public interface OdinAttributesOwner extends OdinPsiElement {
    List<OdinAttributesDefinition> getAttributesDefinitionList();
}
