package com.lasagnerd.odin.codeInsight.dataflow.conditions;

public abstract class Condition {
    //    public abstract List<Condition> toCNF();
    public static final Condition EMPTY = new Condition() {
        @Override
        public boolean isEmpty() {
            return true;
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
}
