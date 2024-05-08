package com.lasagnerd.odin.insights.typeSystem;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class TsOdinUnionType extends TsOdinType {
    List<TsOdinUnionVariant> variants = new ArrayList<>();
}
