package com.lasagnerd.odin.codeInsight.typeSystem;

import com.lasagnerd.odin.codeInsight.OdinSymbolTable;
import com.lasagnerd.odin.lang.psi.OdinDeclaration;
import com.lasagnerd.odin.lang.psi.OdinDeclaredIdentifier;
import com.lasagnerd.odin.lang.psi.OdinType;
import lombok.Data;

@Data
public abstract class TsOdinType {
    String name;

    // Connections to PSI tree
    OdinDeclaration declaration;
    OdinDeclaredIdentifier declaredIdentifier;

    /**
     * Maintains the scope visible from where the type is declared.
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



    public OdinType type;

    public static final TsOdinType UNKNOWN = new TsOdinType() {
        {
            this.symbolTable = OdinSymbolTable.EMPTY;
        }

        @Override
        public String getLabel() {
            return "UNKNOWN";
        }

        @Override
        public TsOdinMetaType.MetaType getMetaType() {
            return TsOdinMetaType.MetaType.UNKNOWN;
        }

        @Override
        public String getName() {
            return "UNKNOWN";
        }
    };

    public static final TsOdinType VOID = new TsOdinType() {
        {
            this.symbolTable = OdinSymbolTable.EMPTY;
        }

        @Override
        public String getName() {
            return "VOID";
        }

        @Override
        public String getLabel() {
            return "VOID";
        }

        @Override
        public TsOdinMetaType.MetaType getMetaType() {
            return TsOdinMetaType.MetaType.VOID;
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

    public boolean isPolymorphic() {
        return this instanceof TsOdinPolymorphicType;
    }

    public boolean isNillable() {
        // TODO continue list
        return this instanceof TsOdinUnionType || this instanceof TsOdinEnumType;
    }

    public String getLabel() {
        return getName() == null ? "<undefined>" : getName();
    }

    public abstract TsOdinMetaType.MetaType getMetaType();


}

