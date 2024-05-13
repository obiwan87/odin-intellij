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
    OdinScope scope = new OdinScope();

    /**
     * Stores the polymorphic parameters of this type, e.g.:
     * For a struct "A struct ($K, $V) {}" this map would contain
     *  K -> TsOdinPolymorphicType,
     *  V -> TsOdinPolymorphicType
     */
    Map<String, TsOdinType> polymorphicParameters = new HashMap<>();

    /**
     * For instantiated types, this represents a mapping of a polymorphic
     * parameters to the typed passed at instantiation time. e.g. (continued from above):
     * For instantiated struct "V :: A(i32, string)" this map would contain
     * K -> i32
     * V -> i32
     * <p>
     * For an instantiated type the length of polymorphicParameters and resolvedPolymorphicParameters
     * must be the same.
     * <p>
     * If a type only contains polymorphic parameters but no resolved ones, then it is considered
     * a generic type. Otherwise, it is considered a specialized type.
     */
    Map<String, TsOdinType> resolvedPolymorphicParameters = new HashMap<>();

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

    public boolean isGeneric() {
        return !getPolymorphicParameters().isEmpty();
    }

    public boolean isSpecialized() {
        return getPolymorphicParameters().isEmpty();
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

