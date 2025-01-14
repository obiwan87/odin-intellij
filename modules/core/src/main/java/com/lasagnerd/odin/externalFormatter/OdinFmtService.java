package com.lasagnerd.odin.externalFormatter;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.CapturingProcessAdapter;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandlerFactory;
import com.intellij.formatting.service.AsyncDocumentFormattingService;
import com.intellij.formatting.service.AsyncFormattingRequest;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.psi.PsiFile;
import com.lasagnerd.odin.lang.psi.OdinFile;
import com.lasagnerd.odin.settings.projectSettings.OdinProjectSettingsService;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Set;

@SuppressWarnings("UnstableApiUsage")
public class OdinFmtService extends AsyncDocumentFormattingService {

    public static final String ODIN_FMT = "odinfmt";

    @SneakyThrows
    @Override
    protected @Nullable FormattingTask createFormattingTask(@NotNull AsyncFormattingRequest formattingRequest) {
        Project project = formattingRequest.getContext().getProject();

        OdinProjectSettingsService projectSettingsService = OdinProjectSettingsService.getInstance(project);
        String odinFmtPath = projectSettingsService.getState().getOdinFmtPath();
        String odinFmtJsonPath = projectSettingsService.getState().getOdinFmtJsonPath();
        GeneralCommandLine commandLine = new GeneralCommandLine();
        commandLine.setExePath(odinFmtPath);
        commandLine.addParameter("-stdin");
        commandLine.addParameter(odinFmtJsonPath);

        OSProcessHandler handler = ProcessHandlerFactory
                .getInstance()
                .createProcessHandler(commandLine.withCharset(StandardCharsets.UTF_8));


        return new FormattingTask() {
            @Override
            public boolean cancel() {
                handler.destroyProcess();
                return true;
            }

            @SneakyThrows
            @Override
            public void run() {

                handler.addProcessListener(new CapturingProcessAdapter(
                ) {
                    @Override
                    public void startNotified(@NotNull ProcessEvent event) {
                        super.startNotified(event);
                    }

                    @Override
                    public void processTerminated(@NotNull ProcessEvent event) {
                        if (event.getExitCode() == 0) {
                            formattingRequest.onTextReady(getOutput().getStdout());
                        } else {
                            formattingRequest.onError("Error while formatting", "Exit code " + event.getExitCode() + ": " + getOutput().getStderr());
                        }
                        super.processTerminated(event);
                    }
                });
                handler.startNotify();
                handler.getProcessInput().write(formattingRequest.getDocumentText().getBytes(StandardCharsets.UTF_8));
                handler.getProcessInput().flush();
                handler.getProcessInput().close();
            }

            @Override
            public boolean isRunUnderProgress() {
                return true;
            }
        };
    }

    @Override
    protected @NotNull String getNotificationGroupId() {
        return "notification.odin";
    }

    @Override
    protected @NotNull @NlsSafe String getName() {
        return ODIN_FMT;
    }

    @Override
    public @NotNull Set<Feature> getFeatures() {
        return Set.of(Feature.AD_HOC_FORMATTING);
    }

    @Override
    public boolean canFormat(@NotNull PsiFile file) {
        Project project = file.getProject();
        OdinProjectSettingsService projectSettingsService = OdinProjectSettingsService.getInstance(project);
        if (projectSettingsService.isUseBuiltinFormatter() || projectSettingsService.getState().getOdinFmtPath().isBlank()) {
            return false;
        }
        return file instanceof OdinFile;
    }
}
