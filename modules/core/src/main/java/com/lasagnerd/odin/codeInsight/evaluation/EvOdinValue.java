package com.lasagnerd.odin.codeInsight.evaluation;

import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinBuiltInTypes;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinType;
import lombok.Data;

@Data
public class EvOdinValue {

    TsOdinType type;
    Object value;

    public EvOdinValue(Object value, TsOdinType type) {
        this.value = value;
        this.type = type;
    }

    public boolean isNull() {
        return this == TsOdinBuiltInTypes.NULL;
    }

    public TsOdinType asType() {
        if(value instanceof TsOdinType tsOdinType) {
            return tsOdinType;
        }
        return null;
    }

    public Boolean asBool() {
        if(value instanceof Boolean) {
            return (Boolean) value;
        }

        return false;
    }

    public TsOdinType asBaseType() {
        if(value instanceof TsOdinType tsOdinType) {
            return tsOdinType.baseType(true);
        }
        return null;
    }

    public Integer asInt() {
        if(value instanceof Long l) {
            return l.intValue();
        }
        return null;
    }

    public Long asLong() {
        if(value instanceof Long l) {
            return l;
        }
        return null;
    }

    public Long toLong() {
        if(value instanceof Number n) {
            return n.longValue();
        }
        return null;
    }

    public EvEnumValue asEnum() {
        if(value instanceof EvEnumValue enumValue)
            return enumValue;

        return null;
    }

    public boolean isEnum() {
        return asEnum() != null;
    }

    public Integer toInt() {
        if(value instanceof Number n) {
            return n.intValue();
        }

        return null;
    }

    public int toInt(int defaultValue) {
        Integer intValue = toInt();
        if(intValue == null)
            return defaultValue;
        return intValue;
    }

    public String asString() {
        if(value instanceof String) {
            return (String) value;
        }
        return null;
    }
}
