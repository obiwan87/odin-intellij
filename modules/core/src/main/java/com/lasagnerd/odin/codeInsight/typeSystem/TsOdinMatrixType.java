package com.lasagnerd.odin.codeInsight.typeSystem;

import com.lasagnerd.odin.lang.psi.OdinExpression;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class TsOdinMatrixType extends TsOdinTypeBase implements TsOdinElementOwner {
    private TsOdinType elementType;
    Integer rows;
    Integer columns;

    OdinExpression rowsExpression;
    OdinExpression columnsExpression;

    @Override
    public TsOdinTypeKind getTypeReferenceKind() {
        return TsOdinTypeKind.MATRIX;
    }

    @Override
    public String getLabel() {
        return "matrix[" + rows + ", " + columns + "]" + labelOrEmpty(elementType).trim();
    }

    public boolean isSquare() {
        return rows != null && rows.equals(columns);
    }

    public boolean sizeKnown() {
        return rows != null && columns != null;
    }
}
