package com.lasagnerd.odin.codeInsight.evaluation;

import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinBuiltInTypes;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinType;

import java.util.HashMap;
import java.util.Map;

public class EvOdinValues {
    public static final EvOdinValue NULL = new EvNullValue<>();
    public static final EvOdinValueSet BOTTOM = new EvNullSet();
    public static final Map<String, EvOdinValue> BUILTIN_IDENTIFIERS = new HashMap<>();

    public static <V, T extends TsOdinType> EvOdinValue nullValue() {
        return NULL;
    }

    public static EvOdinValueSet bottom() {
        return BOTTOM;
    }

    static {
        EvOdinValues.BUILTIN_IDENTIFIERS.put("true", new EvOdinValue(true, TsOdinBuiltInTypes.BOOL));
        EvOdinValues.BUILTIN_IDENTIFIERS.put("false", new EvOdinValue(false, TsOdinBuiltInTypes.BOOL));
    }


    public static class EvNullValue<T> extends EvOdinValue {

        public EvNullValue() {
            super(null, TsOdinBuiltInTypes.UNKNOWN);
        }

        @Override
        public TsOdinType getType() {
            return TsOdinBuiltInTypes.UNKNOWN;
        }

        @Override
        public void setType(TsOdinType type) {

        }

        @Override
        public T getValue() {
            return null;
        }

        @Override
        public void setValue(Object value) {

        }

        @Override
        public boolean isNull() {
            return true;
        }
    }

    public static class EvNullSet extends EvOdinValueSet {
        public EvNullSet() {
            super(null, null, null);
        }

        @Override
        public EvOdinValueSet doCombine(EvOdinValueSet other) {
            return bottom();
        }

        @Override
        public EvOdinValueSet doIntersect(EvOdinValueSet other) {
            return bottom();
        }

        @Override
        public EvOdinValueSet complement() {
            return bottom();
        }

        @Override
        public EvOdinValueSet any() {
            return bottom();
        }

        @Override
        public EvOdinValueSet doDiff(EvOdinValueSet other) {
            return bottom();
        }

        @Override
        public boolean isSubset(EvOdinValueSet set) {
            return true;
        }

        @Override
        public boolean isBottom() {
            return true;
        }

        @Override
        public boolean isNull() {
            return true;
        }
    }
}
