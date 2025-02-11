package com.lasagnerd.odin.debugger.dapDrivers;

import com.intellij.openapi.util.Expirable;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.UserDataHolderEx;
import com.jetbrains.cidr.execution.debugger.backend.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdinEvaluationContext extends EvaluationContext {
    @NotNull
    private final DebuggerDriver debuggerDriver;

    public OdinEvaluationContext(@NotNull DebuggerDriver debuggerDriver,
                                 @Nullable Expirable expirable,
                                 @NotNull LLThread llThread,
                                 @NotNull LLFrame llFrame,
                                 @NotNull UserDataHolderEx userDataHolderEx) {
        super(debuggerDriver, expirable, llThread, llFrame, userDataHolderEx);
        this.debuggerDriver = debuggerDriver;
    }

    @Override
    public @NotNull String convertToRValue(@NotNull LLValueData llValueData, @NotNull Pair<LLValue, String> pair) {
        return cast(pair.getSecond(), pair.getFirst().getType());
    }

    public DebuggerDriver getDebuggerDriver() {
        return debuggerDriver;
    }
}
