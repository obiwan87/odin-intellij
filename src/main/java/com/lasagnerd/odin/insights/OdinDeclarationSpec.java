package com.lasagnerd.odin.insights;

import com.lasagnerd.odin.lang.psi.OdinDeclaredIdentifier;
import com.lasagnerd.odin.lang.psi.OdinExpression;
import com.lasagnerd.odin.lang.psi.OdinPolymorphicType;
import com.lasagnerd.odin.lang.psi.OdinTypeDefinitionExpression;
import lombok.Data;

@Data
public class OdinDeclarationSpec {
    OdinDeclaredIdentifier valueDeclaredIdentifier;
    OdinExpression valueExpression;
    OdinTypeDefinitionExpression typeDefinitionExpression;

    boolean hasUsing;
    boolean isVariadic;

    public boolean isPolymorphic() {
        return valueDeclaredIdentifier.getDollar() != null || getTypeDefinitionExpression().getType() instanceof OdinPolymorphicType;
    }

    public boolean isValuePolymorphic() {
        return valueDeclaredIdentifier.getDollar() != null;
    }

    public boolean isTypePolymorphic() {
        return getTypeDefinitionExpression().getType() instanceof OdinPolymorphicType;
    }

    // TODO fix this, this should in its own declaration spec
    public OdinDeclaredIdentifier getPolymorphicTypeDeclaredIdentifier() {
        if(typeDefinitionExpression.getType() instanceof OdinPolymorphicType polymorphicType) {
            return polymorphicType.getDeclaredIdentifier();
        }
        return null;
    }

}
