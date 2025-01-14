package com.lasagnerd.odin.settings.formatterSettings;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.ui.components.fields.ExtendableTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class OdinFormatterSettings implements Disposable {
    public static final FileChooserDescriptor FILE_CHOOSER_DESCRIPTOR = new FileChooserDescriptor(true, false, false, false, false, false);
    JComponent panel;
    private JPanel odinFmtPanel;
    private ButtonGroup formatterTypeGroup;
    private TextFieldWithBrowseButton odinFmtPathField;
    private TextFieldWithBrowseButton odinFmtJsonPathField;
    private JBRadioButton useBuiltinFormatter;
    private JBRadioButton useOdinFmt;

    public OdinFormatterSettings() {
        useBuiltinFormatter = new JBRadioButton("Use built-in formatter");
        useBuiltinFormatter.addActionListener(e -> {
            toggleOdinFmtSettings(false);
        });
        useOdinFmt = new JBRadioButton("Use 'odinfmt'");
        useOdinFmt.addActionListener(e -> {
            toggleOdinFmtSettings(true);
        });

        formatterTypeGroup = new ButtonGroup();
        formatterTypeGroup.add(useBuiltinFormatter);
        formatterTypeGroup.add(useOdinFmt);


        odinFmtPathField = new TextFieldWithBrowseButton(new ExtendableTextField(10));
        odinFmtPathField.addBrowseFolderListener(new TextBrowseFolderListener(FILE_CHOOSER_DESCRIPTOR));

        odinFmtJsonPathField = new TextFieldWithBrowseButton(new ExtendableTextField(10));
        odinFmtJsonPathField.addBrowseFolderListener(new TextBrowseFolderListener(FILE_CHOOSER_DESCRIPTOR));


        odinFmtPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent("Path to 'odinfmt'", odinFmtPathField)
                .addLabeledComponent("Path to 'odinfmt.json'", odinFmtJsonPathField)
                .getPanel();

        panel = FormBuilder.createFormBuilder()
                .addComponent(useBuiltinFormatter)
                .addComponent(useOdinFmt)
                .addComponent(odinFmtPanel)
                .addComponentFillVertically(new JPanel(), 1)
                .getPanel();
    }

    private void toggleOdinFmtSettings(boolean enabled) {
        for (Component component : odinFmtPanel.getComponents()) {
            if (component instanceof JComponent jComponent) {
                jComponent.setEnabled(enabled);
            }
        }
    }


    public @Nullable JComponent getComponent() {
        return panel;
    }

    public String getOdinFmtPath() {
        return odinFmtPathField.getText();
    }

    public void setOdinFmtPath(String odinFmtPath) {
        odinFmtPathField.setText(odinFmtPath);
    }

    public String getOdinFmtJsonPath() {
        return odinFmtJsonPathField.getText();
    }

    public void setOdinFmtJsonPath(String odinFmtJsonPath) {
        odinFmtJsonPathField.setText(odinFmtJsonPath);
    }

    public boolean isUseBuiltinFormatter() {
        return useBuiltinFormatter.isSelected();
    }

    public void setUseBuiltinFormatter(boolean useBuiltinFormatter) {
        this.useBuiltinFormatter.setSelected(useBuiltinFormatter);
        this.useOdinFmt.setSelected(!useBuiltinFormatter);
        toggleOdinFmtSettings(!useBuiltinFormatter);
    }


    @Override
    public void dispose() {
        panel = null;
        if (odinFmtPathField != null) {
            odinFmtPathField.dispose();
            odinFmtPathField = null;
        }

        if (odinFmtJsonPathField != null) {
            odinFmtJsonPathField.dispose();
            odinFmtJsonPathField = null;
        }

        formatterTypeGroup.remove(useBuiltinFormatter);
        formatterTypeGroup.remove(useOdinFmt);
        formatterTypeGroup = null;
        useBuiltinFormatter = null;
        useOdinFmt = null;
//        formatterTypeGroup.getSelection().removeChangeListener();
    }
}
