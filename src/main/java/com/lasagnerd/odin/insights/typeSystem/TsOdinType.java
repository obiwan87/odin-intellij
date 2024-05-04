package com.lasagnerd.odin.insights.typeSystem;

import com.lasagnerd.odin.insights.OdinScope;
import com.lasagnerd.odin.lang.psi.OdinDeclaration;
import com.lasagnerd.odin.lang.psi.OdinDeclaredIdentifier;
import com.lasagnerd.odin.lang.psi.OdinType;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public abstract class TsOdinType {
    /**
     * Readable name
     */
    String name;
    OdinDeclaration declaration;
    OdinDeclaredIdentifier declaredIdentifier;
    List<TsOdinParameter> parameters = new ArrayList<>();

    /**
     * These are only valid within the type itself and are not passed on to the top level scopes
     */
    OdinScope localScope = new OdinScope();

    Map<String, TsOdinType> resolvedPolymorphicParameters = new HashMap<>();
    Map<String, TsOdinType> unresolvedPolymorphicParameters = new HashMap<>();

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

    public boolean isSameTypeAs(TsOdinType other) {
        if(other == null)
            return false;
        if(getClass().equals(other.getClass())) {
            return other.getDeclaredIdentifier() == this.declaredIdentifier;
        }
        return false;
    }

    public boolean isPolymorphic() {
        return this instanceof TsOdinPolymorphicType;
    }
}

