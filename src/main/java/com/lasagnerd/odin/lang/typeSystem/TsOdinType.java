package com.lasagnerd.odin.lang.typeSystem;

import com.lasagnerd.odin.insights.Scope;
import com.lasagnerd.odin.lang.psi.OdinDeclaration;
import lombok.Data;

@Data
public abstract class TsOdinType {
    String name;
    OdinDeclaration declaration;
    Scope parentScope;

    public static final TsOdinType UNKNOWN = new TsOdinType() {
        @Override
        public String getName() {
            return "UNKNOWN";
        }
    };

    public boolean isUnknown() {
        return UNKNOWN == this;
    }
}

