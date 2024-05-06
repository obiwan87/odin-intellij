package com.lasagnerd.odin.insights.typeSystem;

import com.lasagnerd.odin.lang.psi.OdinDeclaredIdentifier;
import com.lasagnerd.odin.lang.psi.OdinTypeDefinitionExpression;
import lombok.Data;

@Data
public class TsOdinParameter {
    String valueName;
    OdinDeclaredIdentifier valueDeclaredIdentifier;
    boolean isValuePolymorphic;
    OdinTypeDefinitionExpression typeDefinitionExpression;
    TsOdinType type;

    int index;

    public boolean isTypePolymorphic() {
        return type instanceof TsOdinPolymorphicType;
    }

    public boolean isPolymorphic() {
        return isValuePolymorphic || isTypePolymorphic();
    }
}