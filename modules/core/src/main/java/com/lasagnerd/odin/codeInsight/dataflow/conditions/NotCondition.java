package com.lasagnerd.odin.codeInsight.dataflow.conditions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotCondition extends Condition {
    Condition inner;

    @Override
    public Condition toCNF() {
        if (inner instanceof NotCondition notCondition) {
            return notCondition.toCNF();
        }
        if (inner instanceof AndCondition andCondition) {
            return new OrCondition(new NotCondition(andCondition.getLeft()), new NotCondition(andCondition.getRight())).toCNF();
        }
        return null;
    }
}
