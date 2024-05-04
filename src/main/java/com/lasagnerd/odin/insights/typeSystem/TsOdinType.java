package com.lasagnerd.odin.insights.typeSystem;

import com.lasagnerd.odin.insights.OdinScope;
import com.lasagnerd.odin.lang.psi.OdinDeclaration;
import com.lasagnerd.odin.lang.psi.OdinDeclaredIdentifier;
import com.lasagnerd.odin.lang.psi.OdinType;
import lombok.Data;

@Data
public abstract class TsOdinType {
    /**
     * Readable name
     */
    String name;
    OdinDeclaration declaration;
    OdinDeclaredIdentifier declaredIdentifier;
    // TODO remove this, as this should not be needed
    OdinScope parentScope;

    /**
     * Holds types introduced by the type itself, i.e. polymorphic
     */
    OdinScope polymorphicScope = new OdinScope();

    public OdinType type;

    public static final TsOdinType UNKNOWN = new TsOdinType() {
        @Override
        public String getName() {
            return "UNKNOWN";
        }
    };

    public boolean isUnknown() {
        return UNKNOWN == this;
    }

    public <T extends OdinType> T type() {
        return (T) type;
    }

    public boolean isTypeId() {
        return this instanceof TsOdinBuiltInType builtInType && builtInType.getName().equals("typeid");
    }
}

