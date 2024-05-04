package com.lasagnerd.odin.insights.typeSystem;

import com.lasagnerd.odin.lang.psi.OdinDeclaredIdentifier;
import com.lasagnerd.odin.lang.psi.OdinPolymorphicType;
import lombok.Data;

@Data
public class TsOdinPolyParameter {
    String valueName;
    OdinDeclaredIdentifier valueDeclaredIdentifier;
    boolean isValuePolymorphic;

    TsOdinType type;

    int index;

    public OdinDeclaredIdentifier getPolymorphicTypeDeclaredIdentifier() {
        if(type instanceof OdinPolymorphicType polymorphicType) {
            return polymorphicType.getDeclaredIdentifier();
        }
        return null;
    }

    public boolean isTypePolymorphic() {
        return type instanceof OdinPolymorphicType;
    }
}
