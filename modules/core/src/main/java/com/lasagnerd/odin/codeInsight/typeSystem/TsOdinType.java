package com.lasagnerd.odin.codeInsight.typeSystem;

import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolTable;
import com.lasagnerd.odin.lang.psi.OdinDeclaration;
import com.lasagnerd.odin.lang.psi.OdinDeclaredIdentifier;
import com.lasagnerd.odin.lang.psi.OdinExpression;
import com.lasagnerd.odin.lang.psi.OdinType;

public interface TsOdinType {
    // Getters and Setters for fields
    String getName();

    void setName(String name);

    OdinDeclaration getDeclaration();

    void setDeclaration(OdinDeclaration declaration);

    OdinDeclaredIdentifier getDeclaredIdentifier();

    void setDeclaredIdentifier(OdinDeclaredIdentifier declaredIdentifier);

    OdinType getPsiType();

    void setPsiType(OdinType psiType);

    OdinExpression getPsiTypeExpression();

    void setPsiTypeExpression(OdinExpression psiTypeExpression);

    boolean isDistinct();

    void setDistinct(boolean distinct);

    OdinSymbolTable getSymbolTable();

    void setSymbolTable(OdinSymbolTable symbolTable);

    // Public methods
    boolean isUnknown();

    boolean isUndecided();

    <T extends OdinType> T type();

    boolean isTypeId();

    boolean isPolymorphic();

    boolean isNillable();

    TsOdinType baseType();

    TsOdinType baseType(boolean ignoreDistinct);

    boolean isNumeric();

    String getLabel();

    boolean isAnyType();

    boolean isUntyped();

    TsOdinMetaType.MetaType getMetaType();

    boolean isInteger();

    TsOdinType typed();

    boolean isBool();

    @Override
    String toString();
}
