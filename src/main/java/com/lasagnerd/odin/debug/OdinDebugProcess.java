package com.lasagnerd.odin.debug;

import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.intellij.xdebugger.frame.XSuspendContext;
import com.intellij.xdebugger.stepping.XSmartStepIntoHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdinDebugProcess extends XDebugProcess {
    /**
     * @param session pass {@code session} parameter of {@link com.intellij.xdebugger.XDebugProcessStarter#start} method to this constructor
     */
    protected OdinDebugProcess(@NotNull XDebugSession session) {
        super(session);
    }

    @Override
    public @NotNull XDebuggerEditorsProvider getEditorsProvider() {
        return null;
    }

    @Override
    public void sessionInitialized() {
        super.sessionInitialized();
    }

    @Override
    public void startPausing() {
        super.startPausing();
    }

    @Override
    public void startForceStepInto(@Nullable XSuspendContext context) {
        super.startForceStepInto(context);
    }

    @Override
    public void startStepOut(@Nullable XSuspendContext context) {
        super.startStepOut(context);
    }

    @Override
    public @Nullable XSmartStepIntoHandler<?> getSmartStepIntoHandler() {
        return super.getSmartStepIntoHandler();
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    public void runToPosition(@NotNull XSourcePosition position, @Nullable XSuspendContext context) {
        super.runToPosition(position, context);
    }

    @Override
    public void startStepInto(@Nullable XSuspendContext context) {
        super.startStepInto(context);
    }
}
