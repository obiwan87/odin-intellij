package com.lasagnerd.odin.lang.typeSystem;

import com.lasagnerd.odin.insights.Scope;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TsOdinPointerType extends TsOdinType {
    private TsOdinType dereferencedType;
}
