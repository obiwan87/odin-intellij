package com.lasagnerd.odin.codeInsight.dataflow.conditions;

import com.intellij.psi.tree.IElementType;
import com.lasagnerd.odin.lang.psi.OdinExpression;
import com.lasagnerd.odin.lang.psi.OdinTypes;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class AtomicCondition extends Condition {
    @Getter
    private final OdinExpression expression;

    Operand[] operands = new Operand[2];
    IElementType operator;

    private AtomicCondition() {
        this.expression = null;
    }

    public AtomicCondition(OdinExpression expression) {
        this.expression = expression;
    }

    public Operand getLeft() {
        return operands[0];
    }

    public Operand getRight() {
        return operands[1];
    }

    public void setLeft(Operand operand) {
        operands[0] = operand;
    }

    public void setRight(Operand operand) {
        operands[1] = operand;
    }

    @Override
    public Condition toCNF() {
        return this;
    }

    public boolean hasOnlySymbols() {
        return Arrays.stream(this.operands).allMatch(o -> o instanceof SymbolicOperand);
    }

    public boolean hasSymbol() {
        return Arrays.stream(this.operands).anyMatch(o -> o instanceof SymbolicOperand);

    }

    public boolean hasValue() {
        return Arrays.stream(this.operands).anyMatch(o -> o instanceof ValueOperand);
    }

    public boolean hasOnlyValues() {
        return Arrays.stream(this.operands).allMatch(o -> o instanceof ValueOperand);
    }

    public ValueOperand getFirstValueOperand() {
        return (ValueOperand) Arrays.stream(this.operands).filter(o -> o instanceof ValueOperand)
                .findFirst()
                .orElse(null);
    }

    public SymbolicOperand getFirstSymbolicOperand() {
        return (SymbolicOperand) Arrays.stream(this.operands).filter(o -> o instanceof SymbolicOperand)
                .findFirst()
                .orElse(null);
    }

    public List<SymbolicOperand> getSymbolicOperands() {
        return Arrays.stream(this.operands).filter(o -> o instanceof SymbolicOperand)
                .map(SymbolicOperand.class::cast)
                .toList();
    }

    @Override
    public String toString() {
        String op;

        if (operator == OdinTypes.EQEQ)
            op = "==";

        else if (operator == OdinTypes.NEQ)
            op = "!=";
        else
            op = "<UNSUPPORTED OPERATOR>";

        return operands[0] + " " + op + " " + operands[1];
    }

    public boolean negates(AtomicCondition other) {
        return false;
    }

    public boolean isEquivalent(AtomicCondition other) {
        return false;
    }

    public boolean isTautology() {
        // Check if left and right are the same

        if (operands[0] == operands[1])
            return true;

        if (operands[0] == null || operands[1] == null)
            return false;

        // Checks for same symbols or for same values
        boolean equals = operands[0].equals(operands[1]);
        if (operator == OdinTypes.EQEQ)
            return equals;

        if (operator == OdinTypes.NEQ)
            return !equals;

        throw new UnsupportedOperationException("Operator of type %s is not supported".formatted(operator));
    }
}
