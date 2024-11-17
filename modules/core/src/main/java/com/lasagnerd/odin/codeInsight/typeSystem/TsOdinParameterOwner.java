package com.lasagnerd.odin.codeInsight.typeSystem;

import java.util.List;

public interface TsOdinParameterOwner extends TsOdinType {
    List<TsOdinParameter> getParameters();
}
