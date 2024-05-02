package com.lasagnerd.odin.insights;

import com.intellij.psi.PsiNamedElement;
import com.lasagnerd.odin.lang.psi.OdinDeclaredIdentifier;
import com.lasagnerd.odin.lang.psi.OdinExpression;
import com.lasagnerd.odin.lang.psi.OdinTypeDefinitionExpression;
import lombok.Data;

@Data
public class OdinDeclarationSpec {
    OdinDeclaredIdentifier declaredIdentifier;
    OdinExpression valueExpression;
    OdinTypeDefinitionExpression typeDefinitionExpression;

    boolean hasUsing;
    boolean isVariadic;

    public boolean isPolymorphic() {
        return declaredIdentifier.getDollar() != null;
    }
}
