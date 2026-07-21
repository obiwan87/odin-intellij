package com.lasagnerd.odin.settings.projectSettings;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nullable;
import javax.swing.*;

final class OdinLocalSdkDialog extends DialogWrapper {
    private final JTextField name = new JTextField("Local Odin SDK");
    private final TextFieldWithBrowseButton compiler = new TextFieldWithBrowseButton();
    private final TextFieldWithBrowseButton libraries = new TextFieldWithBrowseButton();

    OdinLocalSdkDialog() {
        super(true);
        setTitle("Add Local Odin SDK");
        compiler.addBrowseFolderListener(new TextBrowseFolderListener(FileChooserDescriptorFactory.createSingleFileDescriptor()));
        libraries.addBrowseFolderListener(new TextBrowseFolderListener(FileChooserDescriptorFactory.createSingleFolderDescriptor()));
        init();
    }
    @Override protected @Nullable JComponent createCenterPanel() {
        return FormBuilder.createFormBuilder().addLabeledComponent("Name:", name)
                .addLabeledComponent("Odin executable:", compiler)
                .addLabeledComponent("Odin libraries:", libraries).getPanel();
    }
    String sdkName() { return name.getText().trim(); }
    String compilerPath() { return compiler.getText().trim(); }
    String libraryPath() { return libraries.getText().trim(); }
}
