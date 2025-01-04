package com.lasagnerd.odin.debugger.runConfiguration;

import com.intellij.execution.configuration.EnvironmentVariablesTextFieldWithBrowseButton;
import com.intellij.execution.impl.CheckableRunConfigurationEditor;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ssh.config.unified.SshConfig;
import com.intellij.ssh.config.unified.SshConfigManager;
import com.intellij.ssh.ui.unified.SshConfigComboBox;
import com.intellij.ssh.ui.unified.SshConfigVisibility;
import com.intellij.ssh.ui.unified.SshUiData;
import com.intellij.ui.RawCommandLineEditor;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.JBCheckBox;
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OdinRemoteDebugRunConfigurationSettingsEditor extends SettingsEditor<OdinRemoteDebugRunConfiguration> implements CheckableRunConfigurationEditor<OdinRemoteDebugRunConfiguration> {

    public static final FileChooserDescriptor CHOOSE_FOLDER = new FileChooserDescriptor(false, true, false, false, false, false);
    private final JPanel panel;
    private final JBTextField lldbServerArgsField;
    private final SshConfigComboBox sshComboBox;
    private final JBCheckBox buildOnTargetCheckbox;
    private final JBTextField gdbRemoteArgsField;
    private final RawCommandLineEditor programArgumentsField;
    private final RawCommandLineEditor remoteCompilerOptions;
    private final EnvironmentVariablesTextFieldWithBrowseButton environmentVariables;
    private TextFieldWithBrowseButton remoteOutputPath;
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
        lldbServerPathField = new TextFieldWithBrowseButton(new ExtendableTextField(10), new OpenServerBrowserDialogActionListener(project, s -> lldbServerPathField.setText(s)));
        lldbServerArgsField = new JBTextField();

        programArgumentsField = new RawCommandLineEditor();
        remoteWorkingDirectory = new TextFieldWithBrowseButton(new ExtendableTextField(10), new OpenServerBrowserDialogActionListener(project, s -> remoteWorkingDirectory.setText(s)));

        buildOnTargetCheckbox = new JBCheckBox();

        odinExecutablePathField = new TextFieldWithBrowseButton(new ExtendableTextField(10), new OpenServerBrowserDialogActionListener(project, s -> odinExecutablePathField.setText(s)));
        remotePackageDirectoryPathField = new TextFieldWithBrowseButton(new ExtendableTextField(10), new OpenServerBrowserDialogActionListener(project, s -> remotePackageDirectoryPathField.setText(s)));
        remoteCompilerOptions = new RawCommandLineEditor();
        remoteOutputPath = new TextFieldWithBrowseButton(new ExtendableTextField(10), new OpenServerBrowserDialogActionListener(project, s -> remoteOutputPath.setText(s)));
        environmentVariables = new EnvironmentVariablesTextFieldWithBrowseButton();

        targetExecutableUploadDirPathField = new TextFieldWithBrowseButton(new ExtendableTextField(10), new OpenServerBrowserDialogActionListener(project, s -> targetExecutableUploadDirPathField.setText(s)));
        targetExecutableDownloadPathField = new TextFieldWithBrowseButton(new ExtendableTextField(10),
                new OpenLocalFileSystemBrowserDialogAction(s -> targetExecutableDownloadPathField.setText(s), CHOOSE_FOLDER));
        localExecutablePathField = new TextFieldWithBrowseButton(new ExtendableTextField(10), new OpenLocalFileSystemBrowserDialogAction(s -> localExecutablePathField.setText(s)));


        panel = FormBuilder.createFormBuilder()
                .addLabeledComponent("Debugger path", debuggerPathField)
                .addLabeledComponent("'gdb-remote' args", gdbRemoteArgsField)
                .addLabeledComponent("Package directory", localPackageDirectoryPathField)

                .addLabeledComponent("Credentials", sshComboBox)
                .addLabeledComponent("LLDB server", lldbServerPathField)
                .addLabeledComponent("LLDB server args", lldbServerArgsField)

                .addComponent(new TitledSeparator("Program Execution"))
                .addLabeledComponent("Program arguments", programArgumentsField)
                .addLabeledComponent("Program Working directory", remoteWorkingDirectory)
                .addLabeledComponent("Environment variables", environmentVariables)
//                .addLabeledComponent(result.hostLabel(), result.hostPortPanel())

                .addLabeledComponent("Build on target?", buildOnTargetCheckbox)

                .addComponent(new TitledSeparator("Target Build"))
                .addLabeledComponent("Odin executable", odinExecutablePathField)
                .addLabeledComponent("Compiler options", remoteCompilerOptions)
                .addLabeledComponent("Output path", remoteOutputPath)
                .addLabeledComponent("Remote package directory", remotePackageDirectoryPathField)
                .addLabeledComponent("Download path", targetExecutableDownloadPathField)

                .addComponent(new TitledSeparator("Local Build"))
                .addLabeledComponent("Upload path", targetExecutableUploadDirPathField)
                .addLabeledComponent("Local executable ", localExecutablePathField)

                .getPanel();
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
        buildOnTargetCheckbox.setSelected(options.isBuildOnTarget());
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
        Map<Object, Object> envs = options.getEnvironmentVariables();
        Map<String, String> envsString = new HashMap<>();
        if (envs != null) {
            envs.forEach((key, value) -> envsString.put(key.toString(), value.toString()));
        }
        environmentVariables.setEnvs(envsString);
        remoteCompilerOptions.setText(options.getRemoteCompilerOptions());
        remoteWorkingDirectory.setText(options.getRemoteWorkingDirectory());
        remoteOutputPath.setText(options.getTargetExecutableOutputPath());
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
        options.setBuildOnTarget(buildOnTargetCheckbox.isSelected());
        options.setLldbServerPath(lldbServerPathField.getText());
        options.setTargetExecutableDownloadPath(targetExecutableDownloadPathField.getText());
        options.setLocalPackageDirectoryPath(localPackageDirectoryPathField.getText());
        options.setLldbServerArgs(lldbServerArgsField.getText());
        options.setGdbRemoteArgs(gdbRemoteArgsField.getText());
        options.setProgramArguments(programArgumentsField.getText());

        Map<Object, Object> envs = new HashMap<>(environmentVariables.getData().getEnvs());
        options.setEnvironmentVariables(envs);
        options.setRemoteCompilerOptions(remoteCompilerOptions.getText());
        options.setRemoteWorkingDirectory(remoteWorkingDirectory.getText());
        options.setRemoteOutputPath(remoteOutputPath.getText());
        options.setRemotePackageDirectoryPath(remotePackageDirectoryPathField.getText());
        options.setTargetExecutableOutputPath(remoteOutputPath.getText());

    }

    @Override
    protected @NotNull JComponent createEditor() {
        return panel;
    }

    private record HostPortPanelResult(JLabel hostLabel, JPanel hostPortPanel) {
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
