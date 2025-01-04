package com.lasagnerd.odin.codeInsight.typeSystem;

import com.lasagnerd.odin.lang.psi.OdinRefExpression;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class TsOdinPseudoMethodType extends TsOdinTypeBase implements TsOdinParameterOwner {
    TsOdinStructType containingStruct;
    TsOdinProcedureType procedureType;
    OdinRefExpression refExpression;

    @Override
    public TsOdinTypeKind getTypeReferenceKind() {
        return TsOdinTypeKind.PSEUDO_METHOD;
    }

    @Override
    public List<TsOdinParameter> getParameters() {
        if (procedureType != null) {
            return procedureType.getParameters().stream().skip(1)
                    .map(p -> p.withIndex(p.getIndex() - 1))
                    .toList();
        }
        return Collections.emptyList();
    }
}
