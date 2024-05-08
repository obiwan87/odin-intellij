package com.lasagnerd.odin.insights.typeSystem;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@EqualsAndHashCode(callSuper = true)
@Data
public class TsOdinStructType extends TsOdinType {

    Map<String, TsOdinType> fields = new HashMap<>();
}