package com.lasagnerd.odin.codeInsight.evaluation;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class EvEnumValue {
    private String name;
    private int value;

    @Override
    public String toString() {
        return name + "=" + value;
    }
}
