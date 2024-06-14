package com.lasagnerd.odin.codeInsight.typeSystem;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * NOTE: Even though a procedure can be used with polymorphic parameters,
 * it is not a generic type in the sense that it can't be specialized to a
 * specialized type. It is therefore not part of this hierarchy.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class TsOdinGenericType extends TsOdinType {

    public static TsOdinGenericType NO_GENERIC_TYPE = new TsOdinGenericType() {
        @Override
        public TsOdinMetaType.MetaType getMetaType() {
            return TsOdinMetaType.MetaType.UNKNOWN;
        }
    };

    private TsOdinGenericType genericType;

    public TsOdinGenericType() {
        this.genericType = NO_GENERIC_TYPE;
    }

    /**
     * Stores the polymorphic parameters of this type, e.g.:
     * For a struct "A struct ($K, $V) {}" this map would contain
     * K -> TsOdinPolymorphicType,
     * V -> TsOdinPolymorphicType
     */
    Map<String, TsOdinType> polymorphicParameters = new HashMap<>();

    /**
     * For specialized types, this represents a mapping of a polymorphic
     * parameter to the types passed at instantiation time. e.g. (continued from above):
     * For specialized struct "V :: A(i32, string)" this map would contain
     * K -> i32
     * V -> i32
     * <p>
     * For a specialized type the length of polymorphicParameters and resolvedPolymorphicParameters
     * must be the same.
     * <p>
     * If a type only contains polymorphic parameters but no resolved ones, then it is considered
     * a generic type. Otherwise, it is considered a specialized type.
     */
    Map<String, TsOdinType> resolvedPolymorphicParameters = new HashMap<>();

    public boolean isGeneric() {
        return !isSpecialized();
    }

    /**
     * A type is considered specialized if the set of resolved parameters is equal to the set of polymorphic
     * parameters of the generic type.
     *
     * @return True if the type is specialized
     * @see #isGeneric()
     */
    public boolean isSpecialized() {
        return getPolymorphicParameters().isEmpty() &&
               genericType.getPolymorphicParameters()
                        .keySet().stream().allMatch(k -> getResolvedPolymorphicParameters().containsKey(k));
    }

}
