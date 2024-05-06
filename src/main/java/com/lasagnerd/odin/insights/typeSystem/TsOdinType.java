package com.lasagnerd.odin.insights.typeSystem;

import com.lasagnerd.odin.insights.OdinScope;
import com.lasagnerd.odin.lang.psi.OdinDeclaration;
import com.lasagnerd.odin.lang.psi.OdinDeclaredIdentifier;
import com.lasagnerd.odin.lang.psi.OdinType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public abstract class TsOdinType {
    String name;

    OdinDeclaration declaration;
    OdinDeclaredIdentifier declaredIdentifier;
    List<TsOdinParameter> parameters = new ArrayList<>();

    /**
     * These are only valid within the type itself and are not passed on to the top level scopes
     * <p>
     * Why we need this:
     * <p>
     * List := struct($Item: typeid) {
     *     items: []Item
     * }
     * <p>
     * The declared identifier "Item" that points to the polymorphic parameter is known only in List and in List
     * only.
     * <p>
     */
    @Getter(AccessLevel.NONE)
    OdinScope localScope = new OdinScope();

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

    Map<String, TsOdinType> resolvedPolymorphicParameters = new HashMap<>();

    public OdinType type;

    public static final TsOdinType UNKNOWN = new TsOdinType() {
        @Override
        public String getLabel() {
            return "UNKNOWN";
        }

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

    public boolean isPolymorphic() {
        return this instanceof TsOdinPolymorphicType;
    }

    public String getLabel() {
        if (type != null) {
            return type.getText();
        }
        if (name != null)
            return name;

        return "<undefined>";
    }
}

