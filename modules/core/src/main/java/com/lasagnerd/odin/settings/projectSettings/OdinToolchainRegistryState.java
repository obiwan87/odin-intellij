package com.lasagnerd.odin.settings.projectSettings;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OdinToolchainRegistryState {
    public int version = 2;
    public List<OdinToolchainState> toolchains = new ArrayList<>();
}
