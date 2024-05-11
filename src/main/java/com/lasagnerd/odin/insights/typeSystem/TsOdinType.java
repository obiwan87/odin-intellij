package com.lasagnerd.odin.insights.typeSystem;

import com.lasagnerd.odin.insights.OdinScope;
import com.lasagnerd.odin.lang.psi.OdinDeclaration;
import com.lasagnerd.odin.lang.psi.OdinDeclaredIdentifier;
import com.lasagnerd.odin.lang.psi.OdinType;
import lombok.Data;

import java.util.*;

@Data
public abstract class TsOdinType {
    String name;

    // Connections to PSI tree
    @Deprecated
    OdinDeclaration declaration;
    @Deprecated
    OdinDeclaredIdentifier declaredIdentifier;

    /**
     * Maintains the scope visible from where the type is declared.
     * <p>
     * Example:
     * <p>
     * File A:
     * s := B {x=...}
     * <p>
     * File B:
     * B :: struct {x: C}
     * <p>
     * File C:
     * C :: struct {y: i32}
     * <p>
     * If we are in A and want to resolve the type of s.x, we need the parent scope of B, which would tell use about the existence
     * of C.
     * TODO: this is now used as a container for both local and global symbols. Change it! This will eventually lead to bugs.
     */
    OdinScope scope = new OdinScope();

    Map<String, TsOdinType> polymorphicParameters = new HashMap<>();

    public OdinType type;

    public static final TsOdinType UNKNOWN = new TsOdinType() {
        {
            this.scope = OdinScope.EMPTY;
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
            this.scope = OdinScope.EMPTY;
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

