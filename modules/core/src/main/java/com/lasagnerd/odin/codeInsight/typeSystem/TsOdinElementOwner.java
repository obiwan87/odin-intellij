package com.lasagnerd.odin.codeInsight.typeSystem;

public interface TsOdinElementOwner extends TsOdinType {
    TsOdinType getElementType();

    void setElementType(TsOdinType type);
}
