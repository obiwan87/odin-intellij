package com.lasagnerd.odin.debugger;

import com.jetbrains.cidr.execution.debugger.backend.DebuggerDriver;

public class OdinDebuggerLanguage implements DebuggerDriver.DebuggerLanguage {
    public static final OdinDebuggerLanguage INSTANCE = new OdinDebuggerLanguage();

    private OdinDebuggerLanguage() {
    }

    @Override
    public String toString() {
        return "Odin";
    }
}
