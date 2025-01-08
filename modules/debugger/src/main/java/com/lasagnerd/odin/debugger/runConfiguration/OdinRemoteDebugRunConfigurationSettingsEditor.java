package com.lasagnerd.odin.debugger.runConfiguration;

import com.intellij.execution.configuration.EnvironmentVariablesTextFieldWithBrowseButton;
import com.intellij.execution.impl.CheckableRunConfigurationEditor;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ssh.config.unified.SshConfig;
import com.intellij.ssh.config.unified.SshConfigManager;
import com.intellij.ssh.ui.unified.SshConfigComboBox;
import com.intellij.ssh.ui.unified.SshConfigVisibility;
import com.intellij.ssh.ui.unified.SshUiData;
import com.intellij.ui.RawCommandLineEditor;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.fields.ExtendableTextField;
import com.intellij.util.Consumer;
import com.intellij.util.ui.FormBuilder;
import com.jetbrains.plugins.webDeployment.config.AccessType;
import com.jetbrains.plugins.webDeployment.config.FileTransferConfig;
import com.jetbrains.plugins.webDeployment.config.ServerPasswordSafeDeployable;
import com.jetbrains.plugins.webDeployment.config.WebServerConfig;
import com.jetbrains.plugins.webDeployment.ui.ServerBrowserDialog;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OdinRemoteDebugRunConfigurationSettingsEditor extends SettingsEditor<OdinRemoteDebugRunConfiguration> implements CheckableRunConfigurationEditor<OdinRemoteDebugRunConfiguration> {

    private final JPanel executableProvisioningPanel;
    private final CardLayout cardLayout;
    private final ComboBox<ExecutableProvisioning> executableProvisioningComboBox;
    private TextFieldWithBrowseButton targetProvisionedExecutableDownloadDirPath;

    public static final FileChooserDescriptor CHOOSE_FOLDER = new FileChooserDescriptor(false, true, false, false, false, false);
    private final JPanel panel;
    private final JBTextField lldbServerArgsField;
    private final SshConfigComboBox sshComboBox;
    private TextFieldWithBrowseButton targetProvisionedExecutablePath;
    private final JBTextField gdbRemoteArgsField;
    private final RawCommandLineEditor programArgumentsField;
    private final RawCommandLineEditor remoteCompilerOptions;
    private final EnvironmentVariablesTextFieldWithBrowseButton environmentVariables;
    private TextFieldWithBrowseButton targetExecutableOutputPath;
    private TextFieldWithBrowseButton remoteWorkingDirectory;
    private TextFieldWithBrowseButton localPackageDirectoryPathField;
    private TextFieldWithBrowseButton remotePackageDirectoryPathField;
    private TextFieldWithBrowseButton odinExecutablePathField;
    private TextFieldWithBrowseButton debuggerPathField;
    private TextFieldWithBrowseButton targetExecutableDownloadPathField;
    private TextFieldWithBrowseButton localExecutablePathField;
    private TextFieldWithBrowseButton targetExecutableUploadDirPathField;
    private TextFieldWithBrowseButton lldbServerPathField;

    public OdinRemoteDebugRunConfigurationSettingsEditor(Project project) {
        SshConfigManager sshConfigManager = SshConfigManager.getInstance(project);
        for (SshConfig config : sshConfigManager.getConfigs()) {
            System.out.println(config.getPresentableFullName());
        }

        debuggerPathField = new TextFieldWithBrowseButton(new ExtendableTextField(10),
                new OpenLocalFileSystemBrowserDialogAction(s -> debuggerPathField.setText(s))
        );
        gdbRemoteArgsField = new JBTextField();
        localPackageDirectoryPathField = new TextFieldWithBrowseButton(new ExtendableTextField(10),
                new OpenLocalFileSystemBrowserDialogAction(s -> localPackageDirectoryPathField.setText(s), CHOOSE_FOLDER));

        sshComboBox = new SshConfigComboBox(project, this, SshConfigVisibility.App);
        sshComboBox.reload();
        lldbServerPathField = new TextFieldWithBrowseButton(new ExtendableTextField(10),
                new OpenServerBrowserDialogActionListener(project, s -> lldbServerPathField.setText(s)));
        lldbServerArgsField = new JBTextField();

        programArgumentsField = new RawCommandLineEditor();
        remoteWorkingDirectory = new TextFieldWithBrowseButton(new ExtendableTextField(10),
                new OpenServerBrowserDialogActionListener(project, s -> remoteWorkingDirectory.setText(s)));

        odinExecutablePathField = new TextFieldWithBrowseButton(new ExtendableTextField(10),
                new OpenServerBrowserDialogActionListener(project, s -> odinExecutablePathField.setText(s)));
        remotePackageDirectoryPathField = new TextFieldWithBrowseButton(new ExtendableTextField(10),
                new OpenServerBrowserDialogActionListener(project, s -> remotePackageDirectoryPathField.setText(s)));
        remoteCompilerOptions = new RawCommandLineEditor();
        targetExecutableOutputPath = new TextFieldWithBrowseButton(new ExtendableTextField(10),
                new OpenServerBrowserDialogActionListener(project, s -> targetExecutableOutputPath.setText(s)));
        environmentVariables = new EnvironmentVariablesTextFieldWithBrowseButton();

        targetExecutableUploadDirPathField = new TextFieldWithBrowseButton(new ExtendableTextField(10),
                new OpenServerBrowserDialogActionListener(project, s -> targetExecutableUploadDirPathField.setText(s)));
        targetExecutableDownloadPathField = new TextFieldWithBrowseButton(new ExtendableTextField(10),
                new OpenLocalFileSystemBrowserDialogAction(s -> targetExecutableDownloadPathField.setText(s), CHOOSE_FOLDER));
        localExecutablePathField = new TextFieldWithBrowseButton(new ExtendableTextField(10),
                new OpenLocalFileSystemBrowserDialogAction(s -> localExecutablePathField.setText(s)));


        targetProvisionedExecutablePath = new TextFieldWithBrowseButton(new ExtendableTextField(10),
                new OpenServerBrowserDialogActionListener(project, s -> targetProvisionedExecutablePath.setText(s)));

        targetProvisionedExecutableDownloadDirPath = new TextFieldWithBrowseButton(new ExtendableTextField(10),
                new OpenLocalFileSystemBrowserDialogAction(s -> targetProvisionedExecutableDownloadDirPath.setText(s)));


        JPanel localBuildPanel = FormBuilder
                .createFormBuilder()
                .addLabeledComponent("Upload path", targetExecutableUploadDirPathField)
                .addLabeledComponent("Local executable ", localExecutablePathField)
                .getPanel();

        JPanel buildAtTargetPanel = FormBuilder
                .createFormBuilder()
                .addLabeledComponent("Odin executable", odinExecutablePathField)
                .addLabeledComponent("Compiler options", remoteCompilerOptions)
                .addLabeledComponent("Output path", targetExecutableOutputPath)
                .addLabeledComponent("Remote package directory", remotePackageDirectoryPathField)
                .addLabeledComponent("Download path", targetExecutableDownloadPathField)
                .getPanel();

        JPanel provisionedAtTargetPanel = FormBuilder
                .createFormBuilder()
                .addLabeledComponent("Remote executable path", targetProvisionedExecutablePath)
                .addLabeledComponent("Download path", targetProvisionedExecutableDownloadDirPath)
                .getPanel();


        DefaultComboBoxModel<ExecutableProvisioning> model = new DefaultComboBoxModel<>();
        model.addAll(Arrays.stream(ExecutableProvisioning.values()).toList());
        executableProvisioningComboBox = new ComboBox<>(model);
        executableProvisioningComboBox.setSelectedItem(ExecutableProvisioning.LOCAL_EXECUTABLE);

        cardLayout = new CardLayout();
        executableProvisioningPanel = new JPanel(cardLayout);

        executableProvisioningPanel.add(wrapPanelWithTopAlignment(localBuildPanel), "LOCAL_EXECUTABLE");
        executableProvisioningPanel.add(wrapPanelWithTopAlignment(buildAtTargetPanel), "BUILD_AT_TARGET");
        executableProvisioningPanel.add(wrapPanelWithTopAlignment(provisionedAtTargetPanel), "PROVIDED_AT_TARGET");

        executableProvisioningComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                ExecutableProvisioning item = (ExecutableProvisioning) e.getItem();
                cardLayout.show(executableProvisioningPanel, item.name());
            }
        });

        cardLayout.show(executableProvisioningPanel, ExecutableProvisioning.LOCAL_EXECUTABLE.name());

        panel = FormBuilder.createFormBuilder()
                .addLabeledComponent("'lldb-dap' path", debuggerPathField)
                .addLabeledComponent("'gdb-remote' args", gdbRemoteArgsField)
                .addLabeledComponent("Package directory", localPackageDirectoryPathField)
                .addSeparator()
                .addLabeledComponent("Credentials", sshComboBox)
                .addLabeledComponent("LLDB server", lldbServerPathField)
                .addLabeledComponent("LLDB server args", lldbServerArgsField)
                .addSeparator()
                .addLabeledComponent("Program arguments", programArgumentsField)
                .addLabeledComponent("Program Working directory", remoteWorkingDirectory)
                .addLabeledComponent("Environment variables", environmentVariables)
                .addSeparator()
                .addLabeledComponent("Executable provisioning", executableProvisioningComboBox)
                .addComponent(executableProvisioningPanel)
                .getPanel();
    }

    private JPanel wrapPanelWithTopAlignment(JPanel panel) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(panel, BorderLayout.NORTH); // Align to the top
        return wrapper;
    }

    @Override
    public void checkEditorData(OdinRemoteDebugRunConfiguration s) {

    }

    @Override
    protected void resetEditorFrom(@NotNull OdinRemoteDebugRunConfiguration s) {
        OdinRemoteDebugRunConfigurationOptions options = s.getOptions();
        debuggerPathField.setText(options.getDebuggerPath());
        odinExecutablePathField.setText(options.getRemoteOdinSdkPath());
        localExecutablePathField.setText(options.getLocalExecutablePath());
        targetExecutableUploadDirPathField.setText(options.getTargetExecutableUploadDirPath());
        String executableProvisioning = options.getExecutableProvisioning();
        if (executableProvisioning != null && !executableProvisioning.isBlank()) {
            executableProvisioningComboBox.setSelectedItem(ExecutableProvisioning.valueOf(executableProvisioning));
        }
        lldbServerPathField.setText(options.getLldbServerPath());
        targetExecutableDownloadPathField.setText(options.getTargetExecutableDownloadPath());
        lldbServerArgsField.setText(options.getLldbServerArgs());
        gdbRemoteArgsField.setText(options.getGdbRemoteArgs());
        List<SshConfig> configs = SshConfigManager.getInstance(s.getProject()).getConfigs();
        configs.stream().filter(c -> c.getId().equals(options.getSshConfigId())).findFirst().ifPresent(
                sshComboBox::select
        );
        localPackageDirectoryPathField.setText(options.getLocalPackageDirectoryPath());
        remotePackageDirectoryPathField.setText(options.getRemotePackageDirectoryPath());
        programArgumentsField.setText(options.getProgramArguments());

        Map<String, String> envs = options.getEnvironmentVariables();
        if (envs != null) {
            environmentVariables.setEnvs(new HashMap<>(envs));
        }
        remoteCompilerOptions.setText(options.getRemoteCompilerOptions());
        remoteWorkingDirectory.setText(options.getRemoteWorkingDirectory());
        targetExecutableOutputPath.setText(options.getTargetExecutableOutputPath());
        Object selectedItem = executableProvisioningComboBox.getSelectedItem();
        if (selectedItem instanceof ExecutableProvisioning item) {
            cardLayout.show(executableProvisioningPanel, item.name());
        }

        targetProvisionedExecutablePath.setText(options.getTargetProvisionedExecutablePath());
        targetProvisionedExecutableDownloadDirPath.setText(options.getTargetProvisionedExecutableDownloadDirPath());
    }

    @Override
    protected void applyEditorTo(@NotNull OdinRemoteDebugRunConfiguration s) throws ConfigurationException {
        OdinRemoteDebugRunConfigurationOptions options = s.getOptions();
        options.setDebuggerPath(debuggerPathField.getText());
        options.setRemoteOdinSdkPath(odinExecutablePathField.getText());
        options.setLocalExecutablePath(localExecutablePathField.getText());
        SshConfig selectedSshConfig = sshComboBox.getSelectedSshConfig();
        if (selectedSshConfig != null) {
            options.setSshConfigId(selectedSshConfig.getId());
        }
        options.setTargetExecutableUploadDirPath(targetExecutableUploadDirPathField.getText());
        ExecutableProvisioning selectedItem = (ExecutableProvisioning) executableProvisioningComboBox.getSelectedItem();
        if (selectedItem != null) {
            options.setExecutableProvisioning(selectedItem.name());
        }
        options.setLldbServerPath(lldbServerPathField.getText());
        options.setTargetExecutableDownloadPath(targetExecutableDownloadPathField.getText());
        options.setLocalPackageDirectoryPath(localPackageDirectoryPathField.getText());
        options.setLldbServerArgs(lldbServerArgsField.getText());
        options.setGdbRemoteArgs(gdbRemoteArgsField.getText());
        options.setProgramArguments(programArgumentsField.getText());
        options.setEnvironmentVariables(environmentVariables.getEnvs());
        options.setRemoteCompilerOptions(remoteCompilerOptions.getText());
        options.setRemoteWorkingDirectory(remoteWorkingDirectory.getText());
        options.setRemotePackageDirectoryPath(remotePackageDirectoryPathField.getText());
        options.setTargetExecutableOutputPath(targetExecutableOutputPath.getText());

        options.setTargetProvisionedExecutablePath(targetProvisionedExecutablePath.getText());
        options.setTargetProvisionedExecutableDownloadDirPath(targetProvisionedExecutableDownloadDirPath.getText());
    }

    @Override
    protected @NotNull JComponent createEditor() {
        return panel;
    }

    private static class OpenLocalFileSystemBrowserDialogAction implements ActionListener {
        private final Consumer<String> setter;
        private final FileChooserDescriptor descriptor;

        public OpenLocalFileSystemBrowserDialogAction(Consumer<String> setter, FileChooserDescriptor descriptor) {
            this.setter = setter;
            this.descriptor = descriptor;
        }

        public OpenLocalFileSystemBrowserDialogAction(Consumer<String> setter) {
            this.setter = setter;
            descriptor = new FileChooserDescriptor(
                    true,
                    false,
                    false,
                    false,
                    false,
                    false
            );
        }

        @Override
        public void actionPerformed(ActionEvent e) {

            VirtualFile virtualFile = FileChooser.chooseFile(
                    descriptor,
                    null,
                    null
            );
            if (virtualFile == null) {
                return;
            }
            setter.accept(virtualFile.getPath());
        }
    }

    private class OpenServerBrowserDialogActionListener implements ActionListener {
        private final Project project;
        private final Consumer<String> setter;

        private OpenServerBrowserDialogActionListener(Project project, Consumer<String> setter) {
            this.project = project;
            this.setter = setter;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            SshConfig selectedSshConfig = sshComboBox.getSelectedSshConfig();
            if (selectedSshConfig != null) {

                SshUiData sshUiData = new SshUiData(selectedSshConfig);

                FileTransferConfig fileTransferConfig = new FileTransferConfig(sshUiData);
                fileTransferConfig.setAccessType(AccessType.SFTP);
                WebServerConfig webServerConfig = new WebServerConfig();
                webServerConfig.setName("Remote host");
                webServerConfig.setUrl("http://");
                webServerConfig.setFileTransferConfig(fileTransferConfig);

                ServerPasswordSafeDeployable deployable = new ServerPasswordSafeDeployable(webServerConfig, sshUiData);
                ServerBrowserDialog serverBrowserDialog = new ServerBrowserDialog(
                        project, deployable, "Select Something", false, FileTransferConfig.Origin.ForceRoot, new WebServerConfig.RemotePath("/")
                );
                boolean gotten = serverBrowserDialog.showAndGet();
                if (gotten) {
                    WebServerConfig.RemotePath path = serverBrowserDialog.getPath();
                    if (path != null) {
                        setter.consume(path.path);
                    }
                }
            }

        }
    }
}
