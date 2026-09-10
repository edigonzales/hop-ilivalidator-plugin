package ch.so.agi.ilivalidator.hop.transform;

import ch.so.agi.hop.commons.core.SourceMode;
import ch.so.agi.hop.commons.core.ValueOrField;
import ch.so.agi.hop.commons.ui.BrowseStrategy;
import ch.so.agi.hop.commons.ui.EditorKind;
import ch.so.agi.hop.commons.ui.InputFieldProvider;
import ch.so.agi.hop.commons.ui.ValueOrFieldControl;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.ui.core.PropsUi;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

/** Dialog-local adapter: edits remain in the control until the dialog accepts them. */
final class TransferFileInput {
  private final ValueOrFieldControl control;

  TransferFileInput(
      Composite parent,
      IVariables variables,
      int middle,
      InputFieldProvider fields,
      BrowseStrategy browse,
      Runnable onChange) {
    int margin = PropsUi.getMargin();
    // The status callback runs during build(), so its target must already exist.
    Composite statusArea = new Composite(parent, SWT.NONE);
    GridLayout statusLayout = new GridLayout(1, false);
    statusLayout.marginWidth = statusLayout.marginHeight = 0;
    statusArea.setLayout(statusLayout);
    PropsUi.setLook(statusArea);
    Label status = new Label(statusArea, SWT.WRAP);
    PropsUi.setLook(status);
    status.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_RED));
    GridData statusData = new GridData(SWT.FILL, SWT.CENTER, true, false);
    statusData.widthHint = 0;
    statusData.exclude = true;
    status.setLayoutData(statusData);
    status.setVisible(false);

    Label label = new Label(parent, SWT.RIGHT);
    PropsUi.setLook(label);
    label.setText(
        BaseMessages.getString(IlivalidatorMeta.class, "IlivalidatorDialog.TransferFile.Label"));
    control =
        ValueOrFieldControl.builder(parent, variables)
            .editor(EditorKind.FILE_OPEN)
            .fileFilters(
                new String[] {"*.xtf;*.itf;*.xml", "*.*"},
                new String[] {
                  BaseMessages.getString(
                      IlivalidatorMeta.class, "IlivalidatorDialog.TransferFile.Filter"),
                  BaseMessages.getString(
                      IlivalidatorMeta.class, "IlivalidatorDialog.AllFiles.Filter")
                })
            .fieldProvider(fields)
            .browseStrategy(browse)
            .onChange(onChange)
            .onStatus(
                message -> {
                  status.setText(message);
                  statusData.exclude = message.isEmpty();
                  status.setVisible(!message.isEmpty());
                  parent.layout(true, true);
                })
            .build();
    FormData inputData = new FormData();
    inputData.left = new FormAttachment(middle, 0);
    inputData.right = new FormAttachment(100, 0);
    inputData.top = new FormAttachment(0, 0);
    control.setLayoutData(inputData);
    FormData labelData = new FormData();
    labelData.left = new FormAttachment(0, 0);
    labelData.right = new FormAttachment(middle, -margin);
    labelData.top = new FormAttachment(control, 0, SWT.CENTER);
    label.setLayoutData(labelData);
    FormData statusAreaData = new FormData();
    statusAreaData.left = new FormAttachment(middle, 0);
    statusAreaData.right = new FormAttachment(100, 0);
    statusAreaData.top = new FormAttachment(control, margin);
    statusArea.setLayoutData(statusAreaData);
    parent.setTabList(new Control[] {control});
  }

  void load(IlivalidatorMeta meta) {
    control.setValue(
        new ValueOrField(
            meta.isUseFilePathField() ? SourceMode.FIELD : SourceMode.CONFIGURED,
            meta.getStaticFilePath(),
            meta.getFilePathField()));
  }

  void applyTo(IlivalidatorMeta meta) {
    ValueOrField value = control.getValue();
    meta.setUseFilePathField(value.mode() == SourceMode.FIELD);
    meta.setStaticFilePath(value.configuredValue());
    meta.setFilePathField(value.fieldName());
  }
}
