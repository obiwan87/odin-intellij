package com.lasagnerd.odin.runConfiguration.test;

import com.intellij.execution.testframework.TestConsoleProperties;
import com.intellij.execution.testframework.sm.runner.GeneralTestEventsProcessor;
import com.intellij.execution.testframework.sm.runner.OutputToGeneralTestEventsConverter;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.io.FileUtil;
import jetbrains.buildServer.messages.serviceMessages.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OdinTestEventsConverter extends OutputToGeneralTestEventsConverter {

    public static final Pattern TEST_STATE = Pattern.compile("Test #([0-9]+) (\\S+) changed state to (\\S+?)\\.");
    public static final Pattern TEST_FINISHED = Pattern.compile("Finished .*? tests in");
    public static final Pattern ONE_TEST_FINISHED = Pattern.compile("Finished 1 test in");
    private final OdinTestProceduresLocations testProceduresLocations;
    Map<String, Long> durations = new HashMap<>();
    private String previousPackageName;

    public OdinTestEventsConverter(@NotNull String testFrameworkName,
                                   OdinTestProceduresLocations testProceduresLocations,
                                   @NotNull TestConsoleProperties consoleProperties) {
        super(testFrameworkName, consoleProperties);
        this.testProceduresLocations = testProceduresLocations;

    }

    @Override
    public void process(String text, Key outputType) {
        super.process(text, outputType);
    }

    private static @Nullable String createLocation(Path path, String procedure) {
        String location = path != null ? "odin://" + FileUtil.toSystemIndependentName(path.toString()) : null;
        if (location != null) {
            location += "#" + procedure;
        }
        return location;
    }

    @Override
    protected boolean processServiceMessages(@NotNull String text, @NotNull Key<?> outputType, @NotNull ServiceMessageVisitor visitor) throws ParseException {
        Matcher m = TEST_STATE.matcher(text);
        if (m.find()) {
            String procedure = m.group(2);
            String testOutcome = m.group(3);
            String packageName = procedure.split("\\.")[0];

            if (testOutcome.equals("Running")) {
                durations.put(procedure, System.nanoTime());

                if (previousPackageName == null || !previousPackageName.equals(packageName)) {
                    if (previousPackageName != null) {
                        visitor.visitTestSuiteFinished(new TestSuiteFinished(previousPackageName));
                    }
                    visitor.visitTestSuiteStarted(new TestSuiteStarted(packageName));
                    previousPackageName = packageName;
                }

                Path path = testProceduresLocations.procedureToFilePath().get(procedure);
                String location = createLocation(path, procedure);
                TestStarted testStartedEvent = new TestStarted(procedure, true, location);
                visitor.visitTestStarted(testStartedEvent);
            }

            if (testOutcome.equals("Successful")) {
                long l = getDuration(procedure);
                visitor.visitTestFinished(new TestFinished(procedure, (int) l));
            }

            if (testOutcome.equals("Failed")) {
                long l = getDuration(procedure);
                visitor.visitTestFailed(new TestFailed(procedure, "Test failed"));
                visitor.visitTestFinished(new TestFinished(procedure, (int) l));
            }

            return false;
        } else {
            if (TEST_FINISHED.matcher(text).find() || ONE_TEST_FINISHED.matcher(text).find()) {
                if (previousPackageName != null) {
                    visitor.visitTestSuiteFinished(new TestSuiteFinished(previousPackageName));
                    previousPackageName = null;
                }
                return false;
            }
        }
        if (!text.startsWith("\u001B]2;Odin test runner")) {
            return super.processServiceMessages(text, outputType, visitor);
        }
        return true;
    }

    private long getDuration(String procedure) {
        Long startTime = durations.get(procedure);
        long duration = -1L;
        if (startTime != null) {
            duration = System.nanoTime() - startTime;
        }
        return duration / 1_000_000;
    }

    @Override
    public synchronized void setTestingStartedHandler(@NotNull Runnable testingStartedHandler) {
        super.setTestingStartedHandler(testingStartedHandler);
    }

    @Override
    public void setProcessor(@Nullable GeneralTestEventsProcessor processor) {
        super.setProcessor(processor);
    }
}
