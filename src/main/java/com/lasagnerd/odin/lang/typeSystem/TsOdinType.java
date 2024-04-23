package com.lasagnerd.odin.lang.typeSystem;

import com.lasagnerd.odin.insights.Scope;
import com.lasagnerd.odin.lang.psi.OdinDeclaredIdentifier;
import lombok.Data;

@Data
public abstract class TsOdinType {
    String name;
    OdinDeclaredIdentifier declaration;
    Scope scope;

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

