package ch.so.agi.ilivalidator.hop.transform;

import org.apache.hop.core.util.Utils;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.ui.core.PropsUi;
import org.apache.hop.ui.core.dialog.BaseDialog;
import org.apache.hop.ui.core.widget.ComboVar;
import org.apache.hop.ui.core.widget.TextVar;
import org.apache.hop.ui.pipeline.transform.BaseTransformDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class IlivalidatorDialog extends BaseTransformDialog {

  private static final Class<?> PKG = IlivalidatorMeta.class;

  private final IlivalidatorMeta input;

  private Button wUseFilePathField;
  private ComboVar wFilePathField;
  private TextVar wStaticFilePath;
  private Button wbStaticFilePath;

  private TextVar wModelNames;
  private TextVar wRepositoryUrls;
  private Button wAllObjectsAccessible;

  private Button wFailPipelineOnInvalid;

  private Text wOutputIsValidField;
  private Text wOutputValidationMessageField;
  private Text wOutputLogFilePathField;

  private TextVar wLogDirectory;
  private Button wbLogDirectory;
  private Button wLogFileTimestamp;

  public IlivalidatorDialog(
      Shell parent, IVariables variables, IlivalidatorMeta transformMeta, PipelineMeta pipelineMeta) {
    super(parent, variables, transformMeta, pipelineMeta);
    this.input = transformMeta;
  }

  @Override
  public String open() {
    Shell parent = getParent();

    shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
    shell.setMinimumSize(780, 620);
    PropsUi.setLook(shell);
    setShellImage(shell, input);

    changed = input.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = PropsUi.getFormMargin();
    formLayout.marginHeight = PropsUi.getFormMargin();
    shell.setLayout(formLayout);
    shell.setText(BaseMessages.getString(PKG, "IlivalidatorDialog.Shell.Title"));

    int margin = PropsUi.getMargin();

    wlTransformName = new Label(shell, SWT.RIGHT);
    wlTransformName.setText(BaseMessages.getString(PKG, "System.TransformName.Label"));
    wlTransformName.setToolTipText(BaseMessages.getString(PKG, "System.TransformName.Tooltip"));
    PropsUi.setLook(wlTransformName);
    fdlTransformName = new FormData();
    fdlTransformName.left = new FormAttachment(0, 0);
    fdlTransformName.right = new FormAttachment(props.getMiddlePct(), -margin);
    fdlTransformName.top = new FormAttachment(0, margin);
    wlTransformName.setLayoutData(fdlTransformName);

    wTransformName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wTransformName.setText(transformName);
    PropsUi.setLook(wTransformName);
    wTransformName.addModifyListener(e -> input.setChanged());
    fdTransformName = new FormData();
    fdTransformName.left = new FormAttachment(props.getMiddlePct(), 0);
    fdTransformName.right = new FormAttachment(100, 0);
    fdTransformName.top = new FormAttachment(0, margin);
    wTransformName.setLayoutData(fdTransformName);

    Button wOk = new Button(shell, SWT.PUSH);
    wOk.setText(BaseMessages.getString(PKG, "System.Button.OK"));
    Button wCancel = new Button(shell, SWT.PUSH);
    wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

    setButtonPositions(new Button[] {wOk, wCancel}, margin, null);

    CTabFolder tabFolder = new CTabFolder(shell, SWT.BORDER);
    PropsUi.setLook(tabFolder);
    tabFolder.setSimple(false);
    tabFolder.setUnselectedCloseVisible(false);

    FormData fdTabs = new FormData();
    fdTabs.left = new FormAttachment(0, 0);
    fdTabs.top = new FormAttachment(wTransformName, margin * 2);
    fdTabs.right = new FormAttachment(100, 0);
    fdTabs.bottom = new FormAttachment(wOk, -margin * 2);
    tabFolder.setLayoutData(fdTabs);

    createInputTab(tabFolder);
    createValidationTab(tabFolder);
    createOutputTab(tabFolder);
    tabFolder.setSelection(0);

    wOk.addListener(SWT.Selection, e -> ok());
    wCancel.addListener(SWT.Selection, e -> cancel());

    getData();
    enableDisableControls();
    input.setChanged(changed);

    BaseDialog.defaultShellHandling(shell, c -> ok(), c -> cancel());

    return transformName;
  }

  private void createInputTab(CTabFolder tabFolder) {
    CTabItem inputTab = new CTabItem(tabFolder, SWT.NONE);
    inputTab.setText(BaseMessages.getString(PKG, "IlivalidatorDialog.Tab.Input"));

    Composite inputComposite = new Composite(tabFolder, SWT.NONE);
    PropsUi.setLook(inputComposite);
    FormLayout layout = new FormLayout();
    layout.marginWidth = PropsUi.getFormMargin();
    layout.marginHeight = PropsUi.getFormMargin();
    inputComposite.setLayout(layout);

    Control lastControl = null;

    wUseFilePathField = new Button(inputComposite, SWT.CHECK);
    placeControl(
        inputComposite,
        BaseMessages.getString(PKG, "IlivalidatorDialog.UseFilePathField.Label"),
        wUseFilePathField,
        lastControl);
    wUseFilePathField.addListener(
        SWT.Selection,
        e -> {
          input.setChanged();
          enableDisableControls();
        });
    lastControl = wUseFilePathField;

    wFilePathField = new ComboVar(variables, inputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wFilePathField.addModifyListener(e -> input.setChanged());
    placeControl(
        inputComposite,
        BaseMessages.getString(PKG, "IlivalidatorDialog.FilePathField.Label"),
        wFilePathField,
        lastControl);
    BaseTransformDialog.getFieldsFromPrevious(variables, wFilePathField, pipelineMeta, transformMeta);
    lastControl = wFilePathField;

    wStaticFilePath = new TextVar(variables, inputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wStaticFilePath.addModifyListener(e -> input.setChanged());
    wbStaticFilePath = new Button(inputComposite, SWT.PUSH | SWT.CENTER);
    placeControlWithBrowse(
        inputComposite,
        BaseMessages.getString(PKG, "IlivalidatorDialog.StaticFilePath.Label"),
        wStaticFilePath,
        lastControl,
        wbStaticFilePath,
        e -> browseFile(wStaticFilePath));

    inputTab.setControl(inputComposite);
  }

  private void createValidationTab(CTabFolder tabFolder) {
    CTabItem validationTab = new CTabItem(tabFolder, SWT.NONE);
    validationTab.setText(BaseMessages.getString(PKG, "IlivalidatorDialog.Tab.Validation"));

    Composite validationComposite = new Composite(tabFolder, SWT.NONE);
    PropsUi.setLook(validationComposite);
    FormLayout layout = new FormLayout();
    layout.marginWidth = PropsUi.getFormMargin();
    layout.marginHeight = PropsUi.getFormMargin();
    validationComposite.setLayout(layout);

    Control lastControl = null;

    wModelNames = new TextVar(variables, validationComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wModelNames.addModifyListener(e -> input.setChanged());
    placeControl(
        validationComposite,
        BaseMessages.getString(PKG, "IlivalidatorDialog.ModelNames.Label"),
        wModelNames,
        lastControl);
    lastControl = wModelNames;

    wRepositoryUrls = new TextVar(variables, validationComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wRepositoryUrls.addModifyListener(e -> input.setChanged());
    placeControl(
        validationComposite,
        BaseMessages.getString(PKG, "IlivalidatorDialog.RepositoryUrls.Label"),
        wRepositoryUrls,
        lastControl);
    lastControl = wRepositoryUrls;

    wAllObjectsAccessible = new Button(validationComposite, SWT.CHECK);
    placeControl(
        validationComposite,
        BaseMessages.getString(PKG, "IlivalidatorDialog.AllObjectsAccessible.Label"),
        wAllObjectsAccessible,
        lastControl);
    wAllObjectsAccessible.addListener(SWT.Selection, e -> input.setChanged());
    lastControl = wAllObjectsAccessible;

    wFailPipelineOnInvalid = new Button(validationComposite, SWT.CHECK);
    placeControl(
        validationComposite,
        BaseMessages.getString(PKG, "IlivalidatorDialog.FailPipelineOnInvalid.Label"),
        wFailPipelineOnInvalid,
        lastControl);
    wFailPipelineOnInvalid.addListener(SWT.Selection, e -> input.setChanged());

    validationTab.setControl(validationComposite);
  }

  private void createOutputTab(CTabFolder tabFolder) {
    CTabItem outputTab = new CTabItem(tabFolder, SWT.NONE);
    outputTab.setText(BaseMessages.getString(PKG, "IlivalidatorDialog.Tab.Output"));

    Composite outputComposite = new Composite(tabFolder, SWT.NONE);
    PropsUi.setLook(outputComposite);
    FormLayout layout = new FormLayout();
    layout.marginWidth = PropsUi.getFormMargin();
    layout.marginHeight = PropsUi.getFormMargin();
    outputComposite.setLayout(layout);

    Control lastControl = null;

    wOutputIsValidField = new Text(outputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wOutputIsValidField.addModifyListener(e -> input.setChanged());
    placeControl(
        outputComposite,
        BaseMessages.getString(PKG, "IlivalidatorDialog.OutputIsValidField.Label"),
        wOutputIsValidField,
        lastControl);
    lastControl = wOutputIsValidField;

    wOutputValidationMessageField = new Text(outputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wOutputValidationMessageField.addModifyListener(e -> input.setChanged());
    placeControl(
        outputComposite,
        BaseMessages.getString(PKG, "IlivalidatorDialog.OutputValidationMessageField.Label"),
        wOutputValidationMessageField,
        lastControl);
    lastControl = wOutputValidationMessageField;

    wOutputLogFilePathField = new Text(outputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wOutputLogFilePathField.addModifyListener(e -> input.setChanged());
    placeControl(
        outputComposite,
        BaseMessages.getString(PKG, "IlivalidatorDialog.OutputLogFilePathField.Label"),
        wOutputLogFilePathField,
        lastControl);
    lastControl = wOutputLogFilePathField;

    wLogDirectory = new TextVar(variables, outputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wLogDirectory.addModifyListener(e -> input.setChanged());
    wbLogDirectory = new Button(outputComposite, SWT.PUSH | SWT.CENTER);
    placeControlWithBrowse(
        outputComposite,
        BaseMessages.getString(PKG, "IlivalidatorDialog.LogDirectory.Label"),
        wLogDirectory,
        lastControl,
        wbLogDirectory,
        e -> browseDirectory(wLogDirectory));
    lastControl = wLogDirectory;

    wLogFileTimestamp = new Button(outputComposite, SWT.CHECK);
    wLogFileTimestamp.addListener(SWT.Selection, e -> input.setChanged());
    placeControl(
        outputComposite,
        BaseMessages.getString(PKG, "IlivalidatorDialog.LogFileTimestamp.Label"),
        wLogFileTimestamp,
        lastControl);

    outputTab.setControl(outputComposite);
  }

  private void placeControl(Composite parent, String labelText, Control control, Control under) {
    int middle = props.getMiddlePct();
    int margin = PropsUi.getMargin();

    Label label = new Label(parent, SWT.RIGHT);
    label.setText(labelText);
    PropsUi.setLook(label);
    FormData fdLabel = new FormData();
    fdLabel.left = new FormAttachment(0, 0);
    fdLabel.right = new FormAttachment(middle, -margin);
    fdLabel.top = under == null ? new FormAttachment(0, 0) : new FormAttachment(under, margin);
    label.setLayoutData(fdLabel);

    PropsUi.setLook(control);
    FormData fdControl = new FormData();
    fdControl.left = new FormAttachment(middle, 0);
    fdControl.right = new FormAttachment(100, 0);
    fdControl.top = under == null ? new FormAttachment(0, 0) : new FormAttachment(under, margin);
    control.setLayoutData(fdControl);
  }

  private void placeControlWithBrowse(
      Composite parent,
      String labelText,
      Control control,
      Control under,
      Button browseButton,
      org.eclipse.swt.widgets.Listener browseListener) {
    int middle = props.getMiddlePct();
    int margin = PropsUi.getMargin();

    Label label = new Label(parent, SWT.RIGHT);
    label.setText(labelText);
    PropsUi.setLook(label);
    FormData fdLabel = new FormData();
    fdLabel.left = new FormAttachment(0, 0);
    fdLabel.right = new FormAttachment(middle, -margin);
    fdLabel.top = under == null ? new FormAttachment(0, 0) : new FormAttachment(under, margin);
    label.setLayoutData(fdLabel);

    browseButton.setText("Browse...");
    PropsUi.setLook(browseButton);
    FormData fdBrowse = new FormData();
    fdBrowse.right = new FormAttachment(100, 0);
    fdBrowse.top = under == null ? new FormAttachment(0, 0) : new FormAttachment(under, 0);
    browseButton.setLayoutData(fdBrowse);
    browseButton.addListener(SWT.Selection, browseListener);

    PropsUi.setLook(control);
    FormData fdControl = new FormData();
    fdControl.left = new FormAttachment(middle, 0);
    fdControl.right = new FormAttachment(browseButton, -margin);
    fdControl.top = under == null ? new FormAttachment(0, 0) : new FormAttachment(under, margin);
    control.setLayoutData(fdControl);
  }

  private void browseFile(TextVar textVar) {
    FileDialog dialog = new FileDialog(shell, SWT.OPEN);
    String currentPath = textVar.getText();
    if (currentPath != null && !currentPath.isEmpty()) {
      dialog.setFileName(currentPath);
    }
    String path = dialog.open();
    if (path != null) {
      textVar.setText(path);
      input.setChanged();
    }
  }

  private void browseDirectory(TextVar textVar) {
    DirectoryDialog dialog = new DirectoryDialog(shell, SWT.OPEN);
    String currentPath = textVar.getText();
    if (currentPath != null && !currentPath.isEmpty()) {
      dialog.setFilterPath(currentPath);
    }
    String path = dialog.open();
    if (path != null) {
      textVar.setText(path);
      input.setChanged();
    }
  }

  private void enableDisableControls() {
    boolean useField = wUseFilePathField.getSelection();
    wFilePathField.setEnabled(useField);
    wStaticFilePath.setEnabled(!useField);
    wbStaticFilePath.setEnabled(!useField);
  }

  private void getData() {
    wTransformName.setText(transformName == null ? "" : transformName);

    wUseFilePathField.setSelection(input.isUseFilePathField());
    wFilePathField.setText(input.getFilePathField() == null ? "" : input.getFilePathField());
    wStaticFilePath.setText(input.getStaticFilePath() == null ? "" : input.getStaticFilePath());

    wModelNames.setText(input.getModelNames() == null ? "" : input.getModelNames());
    wRepositoryUrls.setText(input.getRepositoryUrls() == null ? "" : input.getRepositoryUrls());
    wAllObjectsAccessible.setSelection(input.isAllObjectsAccessible());
    wFailPipelineOnInvalid.setSelection(input.isFailPipelineOnInvalid());

    wOutputIsValidField.setText(input.getOutputIsValidField() == null ? "is_valid" : input.getOutputIsValidField());
    wOutputValidationMessageField.setText(
        input.getOutputValidationMessageField() == null
            ? "validation_message"
            : input.getOutputValidationMessageField());
    wOutputLogFilePathField.setText(
        input.getOutputLogFilePathField() == null
            ? "log_file_path"
            : input.getOutputLogFilePathField());

    wLogDirectory.setText(input.getLogDirectory() == null ? "" : input.getLogDirectory());
    wLogFileTimestamp.setSelection(input.isLogFileTimestamp());

    wTransformName.selectAll();
    wTransformName.setFocus();
  }

  private void cancel() {
    transformName = null;
    input.setChanged(changed);
    dispose();
  }

  private void ok() {
    if (Utils.isEmpty(wTransformName.getText())) {
      return;
    }

    transformName = wTransformName.getText();

    input.setUseFilePathField(wUseFilePathField.getSelection());
    input.setFilePathField(wFilePathField.getText());
    input.setStaticFilePath(wStaticFilePath.getText());

    input.setModelNames(wModelNames.getText());
    input.setRepositoryUrls(wRepositoryUrls.getText());
    input.setAllObjectsAccessible(wAllObjectsAccessible.getSelection());

    input.setFailPipelineOnInvalid(wFailPipelineOnInvalid.getSelection());

    input.setOutputIsValidField(wOutputIsValidField.getText());
    input.setOutputValidationMessageField(wOutputValidationMessageField.getText());
    input.setOutputLogFilePathField(wOutputLogFilePathField.getText());

    input.setLogDirectory(wLogDirectory.getText());
    input.setLogFileTimestamp(wLogFileTimestamp.getSelection());

    dispose();
  }
}
