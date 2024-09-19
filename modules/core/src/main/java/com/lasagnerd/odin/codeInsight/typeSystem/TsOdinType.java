package com.lasagnerd.odin.codeInsight.typeSystem;

import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolTable;
import com.lasagnerd.odin.lang.psi.OdinDeclaration;
import com.lasagnerd.odin.lang.psi.OdinDeclaredIdentifier;
import com.lasagnerd.odin.lang.psi.OdinExpression;
import com.lasagnerd.odin.lang.psi.OdinType;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public abstract class TsOdinType {
    String name;

    // Connections to PSI tree
    private OdinDeclaration declaration;
    private OdinDeclaredIdentifier declaredIdentifier;
    private OdinType psiType;
    private OdinExpression psiTypeExpression;

    /**
     * The symbols visible from where the type is declared.
     * <p>
     * Example:
     * <p>
     * Package A:
     * s := B.B {x=...}
     * <p>
     * Package B:
     * B :: struct {x: C.C}
     * <p>
     * Package C:
     * C :: struct {y: i32}
     * <p>
     * If we are in A and want to resolve the type of s.x, we need the parent scope of B, which would tell how to resolve
     * the reference to struct C.
     * of C.
     */
    OdinSymbolTable symbolTable = new OdinSymbolTable();


    public boolean isUnknown() {
        return TsOdinBuiltInTypes.UNKNOWN == this;
    }

    @SuppressWarnings("unchecked")
    public <T extends OdinType> T type() {
        return (T) psiType;
    }

    public boolean isTypeId() {
        return this instanceof TsOdinBuiltInType builtInType && builtInType.getName().equals("typeid");
    }

    public boolean isPolymorphic() {
        return this instanceof TsOdinPolymorphicType;
    }

    public boolean isNillable() {
        // TODO continue list
        return this instanceof TsOdinUnionType
                || this instanceof TsOdinEnumType
                || this instanceof TsOdinPointerType
                || this instanceof TsOdinMultiPointerType;
    }

    public TsOdinType baseType() {
        return baseType(false);
    }

    public TsOdinType baseType(boolean ignoreDistinct) {
        if (this instanceof TsOdinTypeAlias alias) {
            if (ignoreDistinct) {
                return alias.getBaseType();
            }

            if (alias.isDistinct()) {
                return alias;
            }
            return alias.getDistinctBaseType();
        }
        return this;
    }

    public boolean isNumeric() {
        return this instanceof TsOdinNumericType;
    }

    public String getLabel() {
        return getName() == null ? "<undefined>" : getName();
    }

    public boolean isUntyped() {
        return this instanceof TsOdinUntypedType;
    }

    public abstract TsOdinMetaType.MetaType getMetaType();

    @Override
    public String toString() {
        return getLabel();
    }

    protected static String label(@Nullable TsOdinType type) {
        return label(type, "<undefined>");
    }

    protected  static String labelOrEmpty(TsOdinType type) {
        if(type != null) {
            return " " + type.getLabel();
        }
        return "";
    }

    protected static String label(@Nullable TsOdinType type, String defaultValue) {
        if(type != null) {
            return type.getLabel();
        }
        return defaultValue;
    }

}

