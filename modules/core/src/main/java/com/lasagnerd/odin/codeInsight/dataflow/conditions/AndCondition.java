package com.lasagnerd.odin.codeInsight.dataflow.conditions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AndCondition extends Condition {
    Condition left;
    Condition right;

    @Override
    public Condition toCNF() {
        return null;
    }
}
