package com.lasagnerd.odin.codeInsight;

import com.lasagnerd.odin.codeInsight.evaluation.EvOdinValue;
import com.lasagnerd.odin.lang.psi.OdinAttributesDefinition;
import com.lasagnerd.odin.lang.psi.OdinDeclaration;
import com.lasagnerd.odin.lang.psi.OdinType;

import java.util.List;

public record TsOdinObjcMemberInfo(
        OdinDeclaration declaration,
        List<OdinAttributesDefinition> attributes,
        EvOdinValue objcType,
        EvOdinValue objcName,
        EvOdinValue isClassMethod
) {
    public boolean isValidInstanceMember() {
        return this.objcType() != null
                && this.objcName() != null
                && (this.isClassMethod() == null || this.isClassMethod().asBool() != Boolean.TRUE);
    }

    public boolean isValidClassMethod() {
        return this.objcType() != null
                && this.objcName() != null
                && (this.isClassMethod() != null && this.isClassMethod().asBool() == Boolean.TRUE);
    }

    public OdinType type() {
        return OdinInsightUtils.getDeclaredType(declaration);
    }
}
