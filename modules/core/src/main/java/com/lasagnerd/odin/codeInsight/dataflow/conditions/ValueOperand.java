package com.lasagnerd.odin.codeInsight.dataflow.conditions;

import com.lasagnerd.odin.codeInsight.evaluation.EvOdinValue;
import lombok.Getter;

@Getter
public class ValueOperand extends Operand {
    EvOdinValue value;

    public ValueOperand(EvOdinValue value) {
        this.value = value;
    }

    @Override
    public String toString() {
        if (value != null)
            return value.toString();
        return "<undefined>";
    }
}
