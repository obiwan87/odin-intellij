package com.lasagnerd.odin.ols;

import com.intellij.openapi.project.Project;
import com.redhat.devtools.lsp4ij.LanguageServerEnablementSupport;
import com.redhat.devtools.lsp4ij.LanguageServerFactory;
import com.redhat.devtools.lsp4ij.server.ProcessStreamConnectionProvider;
import com.redhat.devtools.lsp4ij.server.StreamConnectionProvider;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;

public class OlsLanguageServerFactory implements LanguageServerFactory, LanguageServerEnablementSupport {
    private boolean disabledByUser;

    // Without an OLS executable the server stays off, so LSP4IJ never tries to start it
    @Override
    public boolean isEnabled(@NotNull Project project) {
        return !disabledByUser && OlsLocator.find().isPresent();
    }

    @Override
    public void setEnabled(boolean enabled, @NotNull Project project) {
        disabledByUser = !enabled;
    }

    @Override
    public @NotNull StreamConnectionProvider createConnectionProvider(@NotNull Project project) {
        // Falls back to the bare name, so a missing OLS surfaces as a server start error in LSP4IJ's console
        String command = OlsLocator.find().map(Path::toString).orElse("ols");
        return new ProcessStreamConnectionProvider() {
            {
                setCommands(List.of(command));
                setWorkingDirectory(project.getBasePath());
            }
        };
    }
}
