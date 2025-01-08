package com.lasagnerd.odin.debugger.runConfiguration;

public enum ExecutableProvisioning {
    LOCAL_EXECUTABLE("Upload local executable"),
    BUILD_AT_TARGET("Build at target"),
    PROVIDED_AT_TARGET("Provided at target");

    private final String label;

    ExecutableProvisioning(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
