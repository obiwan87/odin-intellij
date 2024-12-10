package com.lasagnerd.odin.codeInsight.dataflow.conditions;

import com.lasagnerd.odin.codeInsight.OdinContext;

public abstract class Condition {
    //    public abstract List<Condition> toCNF();
    public static final Condition EMPTY = new Condition() {
        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public boolean canBeTrue(OdinContext context) {
            return false;
        }

        @Override
        public Condition toCNF() {
            return this;
        }
    };

    public abstract Condition toCNF();

    public boolean isEmpty() {
        return false;
    }

    public abstract boolean canBeTrue(OdinContext context);
}
