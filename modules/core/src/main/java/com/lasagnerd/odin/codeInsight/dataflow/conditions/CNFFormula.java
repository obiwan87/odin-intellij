package com.lasagnerd.odin.codeInsight.dataflow.conditions;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
public class CNFFormula {
    // A CNF is a conjunction of clauses.
    // Each clause is a disjunction (OR) of AtomicConditions.
    // Represent clauses as a List of Sets:
    //    CNF: (Clause1) && (Clause2) && ...
    //    Clause: (AtomicCondition1 || AtomicCondition2 || ...)

    private final List<Set<AtomicCondition>> clauses = new ArrayList<>();

    public void addClause(Set<AtomicCondition> clause) {
        // Avoid adding empty or duplicate clauses if desired.
        if (!clause.isEmpty()) {
            clauses.add(clause);
        }
    }

    // Utility to combine two CNF formulas with AND
    public CNFFormula and(CNFFormula other) {
        CNFFormula result = new CNFFormula();
        result.clauses.addAll(this.clauses);
        result.clauses.addAll(other.clauses);
        return result;
    }

    // Utility to create CNF from a single clause
    public static CNFFormula fromSingleClause(Set<AtomicCondition> clause) {
        CNFFormula cnf = new CNFFormula();
        cnf.addClause(clause);
        return cnf;
    }

    public static CNFFormula fromConjunctions(List<Condition> conditions) {
        CNFConverter cnfConverter = new CNFConverter();

        CNFFormula cnf = new CNFFormula();
        for (Condition condition : conditions) {
            CNFFormula currentCNF = cnfConverter.toCNF(condition);
            cnf = cnf.and(currentCNF);
        }

        return cnf;
    }

    // X && Y && Z = (x1 || x2 ...) && (y1 || y2 ... ) && (z1 || z2 ...)
    public CNFFormula simplify() {
        CNFFormula simplified = new CNFFormula();

        // Prune tautologies
        outer:
        for (Set<AtomicCondition> disjunctions : this.getClauses()) {
            List<AtomicCondition> disjunctionsList = new ArrayList<>(disjunctions);
            for (int i = 0; i < disjunctionsList.size() - 1; i++) {
                AtomicCondition c1 = disjunctionsList.get(i);

                if (c1.isTautology()) {
                    continue outer;
                }

                // Check for tautologies
                for (int j = i; j < disjunctionsList.size(); j++) {
                    AtomicCondition c2 = disjunctionsList.get(j);
                    // Tautology: C1 || !C1 || X <=> true
                    if (c1.negates(c2)) {
                        continue outer;
                    }
                }

            }
            simplified.addClause(disjunctions);
        }

        // TODO Apply rule: (P || Q) && !P <=> Q

        return simplified;
    }

    // For debugging
    @Override
    public String toString() {
        return clauses.toString();
    }
}
