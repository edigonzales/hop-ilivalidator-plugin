package ch.so.agi.ilivalidator.hop.transform;

import ch.so.agi.ilivalidator.core.validator.IlivalidatorOptionCatalog;
import ch.so.agi.ilivalidator.core.validator.IlivalidatorOptionCodec;
import ch.so.agi.ilivalidator.core.validator.IlivalidatorOptionDefinition;
import ch.so.agi.ilivalidator.core.validator.IlivalidatorOptionEntry;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.hop.core.util.Utils;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.ui.core.PropsUi;
import org.apache.hop.ui.core.dialog.BaseDialog;
import org.apache.hop.ui.core.gui.WindowProperty;
import org.apache.hop.ui.core.widget.ColumnInfo;
import org.apache.hop.ui.core.widget.ComboVar;
import org.apache.hop.ui.core.widget.TableView;
import org.apache.hop.ui.core.widget.TextVar;
import org.apache.hop.ui.pipeline.transform.BaseTransformDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class IlivalidatorDialog extends BaseTransformDialog {

  private static final Class<?> PKG = IlivalidatorMeta.class;
  private static final int DIALOG_MIN_WIDTH = 900;
  private static final int DIALOG_MIN_HEIGHT = 680;
  private static final int DIALOG_WIDTH = 980;
  private static final int DIALOG_HEIGHT = 760;
  private static final double MAX_WIDTH_FACTOR = 0.95d;
  private static final double MAX_HEIGHT_FACTOR = 0.90d;

  private final IlivalidatorMeta input;

  private Button wUseFilePathField;
  private ComboVar wFilePathField;
  private TextVar wStaticFilePath;
  private Button wbStaticFilePath;

  private TextVar wModelNames;
  private TextVar wRepositoryUrls;
  private ComboVar wConfigMode;
  private TextVar wConfigValue;
  private Button wbConfigValue;
  private ComboVar wConfigField;
  private ComboVar wMetaConfigMode;
  private TextVar wMetaConfigValue;
  private Button wbMetaConfigValue;
  private ComboVar wMetaConfigField;
  private Button wAllObjectsAccessible;

  private Button wFailPipelineOnInvalid;

  private TableView wOptions;

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
    shell.setMinimumSize(DIALOG_MIN_WIDTH, DIALOG_MIN_HEIGHT);
    shell.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
    PropsUi.setLook(shell);
    setShellImage(shell, input);

    changed = input.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = PropsUi.getFormMargin();
    formLayout.marginHeight = PropsUi.getFormMargin();
    shell.setLayout(formLayout);
    shell.setText(BaseMessages.getString(PKG, "IlivalidatorDialog.Shell.Title"));
    normalizeStoredWindowSize();

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
    createOptionsTab(tabFolder);
    createOutputTab(tabFolder);
    tabFolder.setSelection(0);

    wOk.addListener(SWT.Selection, e -> ok());
    wCancel.addListener(SWT.Selection, e -> cancel());

    getData();
    enableDisableControls();
    input.setChanged(changed);

    openWithFixedShellSize(DIALOG_WIDTH, DIALOG_HEIGHT);

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

    wConfigMode = new ComboVar(variables, validationComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wConfigMode.add("STATIC");
    wConfigMode.add("FIELD");
    wConfigMode.addModifyListener(
        e -> {
          input.setChanged();
          enableDisableControls();
        });
    placeControl(
        validationComposite,
        BaseMessages.getString(PKG, "IlivalidatorDialog.ConfigMode.Label"),
        wConfigMode,
        lastControl);
    lastControl = wConfigMode;

    wConfigValue = new TextVar(variables, validationComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wConfigValue.addModifyListener(e -> input.setChanged());
    wbConfigValue = new Button(validationComposite, SWT.PUSH | SWT.CENTER);
    placeControlWithBrowse(
        validationComposite,
        BaseMessages.getString(PKG, "IlivalidatorDialog.ConfigValue.Label"),
        wConfigValue,
        lastControl,
        wbConfigValue,
        e -> browseFile(wConfigValue));
    lastControl = wConfigValue;

    wConfigField = new ComboVar(variables, validationComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wConfigField.addModifyListener(e -> input.setChanged());
    placeControl(
        validationComposite,
        BaseMessages.getString(PKG, "IlivalidatorDialog.ConfigField.Label"),
        wConfigField,
        lastControl);
    BaseTransformDialog.getFieldsFromPrevious(variables, wConfigField, pipelineMeta, transformMeta);
    lastControl = wConfigField;

    wMetaConfigMode = new ComboVar(variables, validationComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wMetaConfigMode.add("STATIC");
    wMetaConfigMode.add("FIELD");
    wMetaConfigMode.addModifyListener(
        e -> {
          input.setChanged();
          enableDisableControls();
        });
    placeControl(
        validationComposite,
        BaseMessages.getString(PKG, "IlivalidatorDialog.MetaConfigMode.Label"),
        wMetaConfigMode,
        lastControl);
    lastControl = wMetaConfigMode;

    wMetaConfigValue = new TextVar(variables, validationComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wMetaConfigValue.addModifyListener(e -> input.setChanged());
    wbMetaConfigValue = new Button(validationComposite, SWT.PUSH | SWT.CENTER);
    placeControlWithBrowse(
        validationComposite,
        BaseMessages.getString(PKG, "IlivalidatorDialog.MetaConfigValue.Label"),
        wMetaConfigValue,
        lastControl,
        wbMetaConfigValue,
        e -> browseFile(wMetaConfigValue));
    lastControl = wMetaConfigValue;

    wMetaConfigField = new ComboVar(variables, validationComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wMetaConfigField.addModifyListener(e -> input.setChanged());
    placeControl(
        validationComposite,
        BaseMessages.getString(PKG, "IlivalidatorDialog.MetaConfigField.Label"),
        wMetaConfigField,
        lastControl);
    BaseTransformDialog.getFieldsFromPrevious(variables, wMetaConfigField, pipelineMeta, transformMeta);
    lastControl = wMetaConfigField;

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

  private void createOptionsTab(CTabFolder tabFolder) {
    CTabItem optionsTab = new CTabItem(tabFolder, SWT.NONE);
    optionsTab.setText(BaseMessages.getString(PKG, "IlivalidatorDialog.Tab.Options"));

    Composite optionsComposite = new Composite(tabFolder, SWT.NONE);
    PropsUi.setLook(optionsComposite);
    FormLayout layout = new FormLayout();
    layout.marginWidth = PropsUi.getFormMargin();
    layout.marginHeight = PropsUi.getFormMargin();
    optionsComposite.setLayout(layout);

    ColumnInfo[] columns = new ColumnInfo[5];
    columns[0] =
        new ColumnInfo(
            BaseMessages.getString(PKG, "IlivalidatorDialog.Options.Column.Key"),
            ColumnInfo.COLUMN_TYPE_TEXT);
    columns[0].setReadOnly(true);

    columns[1] =
        new ColumnInfo(
            BaseMessages.getString(PKG, "IlivalidatorDialog.Options.Column.Active"),
            ColumnInfo.COLUMN_TYPE_CCOMBO,
            new String[] {"Y", "N"});

    columns[2] =
        new ColumnInfo(
            BaseMessages.getString(PKG, "IlivalidatorDialog.Options.Column.Value"),
            ColumnInfo.COLUMN_TYPE_TEXT);

    columns[3] =
        new ColumnInfo(
            BaseMessages.getString(PKG, "IlivalidatorDialog.Options.Column.Type"),
            ColumnInfo.COLUMN_TYPE_TEXT);
    columns[3].setReadOnly(true);

    columns[4] =
        new ColumnInfo(
            BaseMessages.getString(PKG, "IlivalidatorDialog.Options.Column.Applicable"),
            ColumnInfo.COLUMN_TYPE_TEXT);
    columns[4].setReadOnly(true);

    wOptions =
        new TableView(
            variables,
            optionsComposite,
            SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE,
            columns,
            12,
            e -> input.setChanged(),
            props);

    FormData fdOptions = new FormData();
    fdOptions.left = new FormAttachment(0, 0);
    fdOptions.top = new FormAttachment(0, 0);
    fdOptions.right = new FormAttachment(100, 0);
    fdOptions.bottom = new FormAttachment(100, 0);
    wOptions.setLayoutData(fdOptions);

    optionsTab.setControl(optionsComposite);
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

    browseButton.setText(BaseMessages.getString(PKG, "System.Button.Browse"));
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

    boolean configFromField = "FIELD".equalsIgnoreCase(wConfigMode.getText());
    wConfigValue.setEnabled(!configFromField);
    wbConfigValue.setEnabled(!configFromField);
    wConfigField.setEnabled(configFromField);

    boolean metaConfigFromField = "FIELD".equalsIgnoreCase(wMetaConfigMode.getText());
    wMetaConfigValue.setEnabled(!metaConfigFromField);
    wbMetaConfigValue.setEnabled(!metaConfigFromField);
    wMetaConfigField.setEnabled(metaConfigFromField);
  }

  private void getData() {
    wTransformName.setText(transformName == null ? "" : transformName);

    wUseFilePathField.setSelection(input.isUseFilePathField());
    wFilePathField.setText(input.getFilePathField() == null ? "" : input.getFilePathField());
    wStaticFilePath.setText(input.getStaticFilePath() == null ? "" : input.getStaticFilePath());

    wModelNames.setText(input.getModelNames() == null ? "" : input.getModelNames());
    wRepositoryUrls.setText(input.getRepositoryUrls() == null ? "" : input.getRepositoryUrls());
    wConfigMode.setText(input.getConfigMode() == null ? "STATIC" : input.getConfigMode());
    wConfigValue.setText(input.getConfigValue() == null ? "" : input.getConfigValue());
    wConfigField.setText(input.getConfigField() == null ? "" : input.getConfigField());
    wMetaConfigMode.setText(input.getMetaConfigMode() == null ? "STATIC" : input.getMetaConfigMode());
    wMetaConfigValue.setText(input.getMetaConfigValue() == null ? "" : input.getMetaConfigValue());
    wMetaConfigField.setText(input.getMetaConfigField() == null ? "" : input.getMetaConfigField());
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

    populateOptionsTable(IlivalidatorOptionCodec.decode(input.getSerializedOptions()));

    wTransformName.selectAll();
    wTransformName.setFocus();
  }

  private void populateOptionsTable(List<IlivalidatorOptionEntry> selectedValues) {
    Map<String, IlivalidatorOptionEntry> existing = new LinkedHashMap<>();
    for (IlivalidatorOptionEntry entry : selectedValues) {
      if (entry != null && entry.getKey() != null) {
        existing.put(entry.getKey().toLowerCase(Locale.ROOT), entry);
      }
    }

    wOptions.removeAll();

    for (IlivalidatorOptionDefinition definition : IlivalidatorOptionCatalog.allDefinitions()) {
      IlivalidatorOptionEntry existingEntry = existing.get(definition.getKey().toLowerCase(Locale.ROOT));
      boolean applies = definition.appliesToCurrentContext();
      boolean enabled = existingEntry != null && existingEntry.isEnabled() && applies;
      String value = existingEntry == null || existingEntry.getValue() == null ? "" : existingEntry.getValue();

      TableItem item =
          wOptions.add(
              definition.getKey(),
              enabled ? "Y" : "N",
              value,
              definition.getType().name(),
              applies
                  ? BaseMessages.getString(PKG, "IlivalidatorDialog.Options.Applicable.Yes")
                  : BaseMessages.getString(PKG, "IlivalidatorDialog.Options.Applicable.No"));
      if (!applies) {
        item.setForeground(shell.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
      }
    }

    wOptions.removeEmptyRows();
    wOptions.optWidth(true);
  }

  private List<IlivalidatorOptionEntry> readOptionsFromTable() {
    List<IlivalidatorOptionEntry> entries = new java.util.ArrayList<>();
    for (TableItem item : wOptions.getNonEmptyItems()) {
      String key = item.getText(1);
      if (key == null || key.isBlank()) {
        continue;
      }
      String active = item.getText(2);
      String value = item.getText(3);
      boolean enabled = "Y".equalsIgnoreCase(active) || "TRUE".equalsIgnoreCase(active);

      if (!enabled && (value == null || value.isBlank())) {
        continue;
      }

      entries.add(new IlivalidatorOptionEntry(key, enabled, value));
    }
    return entries;
  }

  private void cancel() {
    transformName = null;
    input.setChanged(changed);
    dispose();
  }

  private void openWithFixedShellSize(int width, int height) {
    shell.addListener(
        SWT.Close,
        event -> {
          event.doit = false;
          cancel();
        });
    BaseDialog.addDefaultListeners(shell, c -> ok());
    BaseDialog.addSpacesOnTabs(shell);

    shell.layout(true, true);
    shell.setMaximized(false);
    shell.setMinimized(false);
    shell.setSize(width, height);

    Rectangle clientArea = shell.getDisplay().getPrimaryMonitor().getClientArea();
    if (shell.getParent() != null) {
      clientArea = shell.getParent().getMonitor().getClientArea();
    }
    int x = clientArea.x + Math.max(0, (clientArea.width - width) / 2);
    int y = clientArea.y + Math.max(0, (clientArea.height - height) / 2);
    shell.setLocation(x, y);

    shell.open();
    Display display = shell.getDisplay();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) {
        display.sleep();
      }
    }
  }

  private void normalizeStoredWindowSize() {
    WindowProperty windowProperty = props.getScreen(shell.getText());
    if (windowProperty == null) {
      props.setScreen(createDefaultWindowProperty(shell.getText()));
      return;
    }

    Rectangle clientArea = shell.getDisplay().getPrimaryMonitor().getClientArea();
    if (normalizeWindowProperty(windowProperty, clientArea)) {
      props.setScreen(windowProperty);
    }
  }

  static WindowProperty createDefaultWindowProperty(String shellText) {
    return new WindowProperty(shellText, false, -1, -1, DIALOG_WIDTH, DIALOG_HEIGHT);
  }

  static boolean normalizeWindowProperty(WindowProperty windowProperty, Rectangle clientArea) {
    if (windowProperty == null || clientArea == null) {
      return false;
    }

    int maxHeight = (int) (clientArea.height * MAX_HEIGHT_FACTOR);
    int maxWidth = (int) (clientArea.width * MAX_WIDTH_FACTOR);
    boolean changed = false;

    if (windowProperty.getHeight() > maxHeight || windowProperty.getHeight() <= 0) {
      windowProperty.setHeight(DIALOG_HEIGHT);
      changed = true;
    }
    if (windowProperty.getWidth() > maxWidth || windowProperty.getWidth() <= 0) {
      windowProperty.setWidth(DIALOG_WIDTH);
      changed = true;
    }
    if (windowProperty.isMaximized()) {
      windowProperty.setMaximized(false);
      changed = true;
    }

    return changed;
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
    input.setConfigMode(wConfigMode.getText());
    input.setConfigValue(wConfigValue.getText());
    input.setConfigField(wConfigField.getText());
    input.setMetaConfigMode(wMetaConfigMode.getText());
    input.setMetaConfigValue(wMetaConfigValue.getText());
    input.setMetaConfigField(wMetaConfigField.getText());
    input.setSerializedOptions(IlivalidatorOptionCodec.encode(readOptionsFromTable()));
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
