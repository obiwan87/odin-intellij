package com.lasagnerd.odin.insights;

import lombok.Getter;

@Getter
public enum OdinTypeType {
    STRUCT("Struct"),
    ENUM("Enum"),
    UNION("Union"),
    PROCEDURE("Procedure"),
    PROCEDURE_OVERLOAD("Procedure overload"),
    VARIABLE("Variable"),
    CONSTANT("Constant"),
    PACKAGE("Package"),
    FIELD("Field"),
    PARAMETER("Parameter"),
    UNKNOWN("Unknown");

    private final String humanReadableName;

    OdinTypeType(String name) {

        this.humanReadableName = name;
    }

}
