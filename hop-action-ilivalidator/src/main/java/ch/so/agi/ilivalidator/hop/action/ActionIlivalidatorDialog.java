package ch.so.agi.ilivalidator.hop.action;

import org.apache.hop.core.util.Utils;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.ui.core.PropsUi;
import org.apache.hop.ui.core.dialog.BaseDialog;
import org.apache.hop.ui.core.widget.TextVar;
import org.apache.hop.ui.pipeline.transform.BaseTransformDialog;
import org.apache.hop.ui.workflow.action.ActionDialog;
import org.apache.hop.ui.workflow.dialog.WorkflowDialog;
import org.apache.hop.workflow.WorkflowMeta;
import org.apache.hop.workflow.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ActionIlivalidatorDialog extends ActionDialog {

  private static final Class<?> PKG = ActionIlivalidator.class;

  private final ActionIlivalidator action;
  private boolean backupChanged;

  private Text wName;

  private Button wModeSingle;
  private Button wModeFolder;
  private TextVar wFilePath;
  private TextVar wFolderPath;
  private Button wRecursive;
  private TextVar wIncludeMask;
  private TextVar wExcludeMask;

  private TextVar wModelNames;
  private TextVar wRepositoryUrls;
  private Button wFailFast;
  private Button wStopOnFirstInvalid;
  private Button wAllObjectsAccessible;

  private Button wWriteInvalidResultFiles;

  public ActionIlivalidatorDialog(
      Shell parent, ActionIlivalidator action, WorkflowMeta workflowMeta, IVariables variables) {
    super(parent, workflowMeta, variables);
    this.action = action;
  }

  @Override
  public IAction open() {
    shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.MIN | SWT.MAX | SWT.RESIZE);
    shell.setMinimumSize(760, 560);
    PropsUi.setLook(shell);
    WorkflowDialog.setShellImage(shell, action);

    backupChanged = action.hasChanged();

    FormLayout shellLayout = new FormLayout();
    shellLayout.marginWidth = PropsUi.getFormMargin();
    shellLayout.marginHeight = PropsUi.getFormMargin();
    shell.setLayout(shellLayout);
    shell.setText(BaseMessages.getString(PKG, "ActionIlivalidatorDialog.Title"));

    int margin = PropsUi.getMargin();
    int middle = props.getMiddlePct();

    Label wlName = new Label(shell, SWT.RIGHT);
    wlName.setText(BaseMessages.getString(PKG, "System.ActionName.Label"));
    wlName.setToolTipText(BaseMessages.getString(PKG, "System.ActionName.Tooltip"));
    PropsUi.setLook(wlName);
    FormData fdlName = new FormData();
    fdlName.left = new FormAttachment(0, 0);
    fdlName.top = new FormAttachment(0, margin);
    fdlName.right = new FormAttachment(middle, -margin);
    wlName.setLayoutData(fdlName);

    wName = new Text(shell, SWT.SINGLE | SWT.BORDER | SWT.LEFT);
    PropsUi.setLook(wName);
    wName.addModifyListener(e -> action.setChanged());
    FormData fdName = new FormData();
    fdName.left = new FormAttachment(middle, 0);
    fdName.top = new FormAttachment(0, margin);
    fdName.right = new FormAttachment(100, 0);
    wName.setLayoutData(fdName);

    Button wOk = new Button(shell, SWT.PUSH);
    wOk.setText(BaseMessages.getString(PKG, "System.Button.OK"));
    Button wCancel = new Button(shell, SWT.PUSH);
    wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

    BaseTransformDialog.positionBottomButtons(shell, new Button[] {wOk, wCancel}, margin, null);

    CTabFolder tabFolder = new CTabFolder(shell, SWT.BORDER);
    PropsUi.setLook(tabFolder);
    tabFolder.setSimple(false);
    tabFolder.setUnselectedCloseVisible(false);
    FormData fdTabs = new FormData();
    fdTabs.left = new FormAttachment(0, 0);
    fdTabs.top = new FormAttachment(wName, margin * 2);
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
    enableInputModeControls();

    BaseDialog.defaultShellHandling(shell, c -> ok(), c -> cancel());

    return action;
  }

  private void createInputTab(CTabFolder tabFolder) {
    CTabItem inputTab = new CTabItem(tabFolder, SWT.NONE);
    inputTab.setText(BaseMessages.getString(PKG, "ActionIlivalidatorDialog.Tab.Input"));

    Composite inputComposite = new Composite(tabFolder, SWT.NONE);
    PropsUi.setLook(inputComposite);
    FormLayout layout = new FormLayout();
    layout.marginWidth = PropsUi.getFormMargin();
    layout.marginHeight = PropsUi.getFormMargin();
    inputComposite.setLayout(layout);

    int margin = PropsUi.getMargin();
    int middle = props.getMiddlePct();

    Label wlMode = new Label(inputComposite, SWT.RIGHT);
    wlMode.setText(BaseMessages.getString(PKG, "ActionIlivalidatorDialog.Input.Mode"));
    wlMode.setToolTipText(BaseMessages.getString(PKG, "ActionIlivalidatorDialog.Input.Mode.Tooltip"));
    PropsUi.setLook(wlMode);
    FormData fdlMode = new FormData();
    fdlMode.left = new FormAttachment(0, 0);
    fdlMode.top = new FormAttachment(0, margin);
    fdlMode.right = new FormAttachment(middle, -margin);
    wlMode.setLayoutData(fdlMode);

    Composite modeComposite = new Composite(inputComposite, SWT.NONE);
    PropsUi.setLook(modeComposite);
    modeComposite.setLayout(new FormLayout());
    FormData fdModeComposite = new FormData();
    fdModeComposite.left = new FormAttachment(middle, 0);
    fdModeComposite.top = new FormAttachment(0, margin);
    fdModeComposite.right = new FormAttachment(100, 0);
    modeComposite.setLayoutData(fdModeComposite);

    wModeSingle = new Button(modeComposite, SWT.RADIO);
    wModeSingle.setText(BaseMessages.getString(PKG, "ActionIlivalidatorDialog.Input.Mode.Single"));
    PropsUi.setLook(wModeSingle);
    FormData fdModeSingle = new FormData();
    fdModeSingle.left = new FormAttachment(0, 0);
    fdModeSingle.top = new FormAttachment(0, 0);
    wModeSingle.setLayoutData(fdModeSingle);

    wModeFolder = new Button(modeComposite, SWT.RADIO);
    wModeFolder.setText(BaseMessages.getString(PKG, "ActionIlivalidatorDialog.Input.Mode.Folder"));
    PropsUi.setLook(wModeFolder);
    FormData fdModeFolder = new FormData();
    fdModeFolder.left = new FormAttachment(wModeSingle, margin * 3);
    fdModeFolder.top = new FormAttachment(0, 0);
    wModeFolder.setLayoutData(fdModeFolder);

    wModeSingle.addListener(SWT.Selection, e -> enableInputModeControls());
    wModeFolder.addListener(SWT.Selection, e -> enableInputModeControls());

    Label wlFilePath = new Label(inputComposite, SWT.RIGHT);
    wlFilePath.setText(BaseMessages.getString(PKG, "ActionIlivalidatorDialog.Input.FilePath"));
    wlFilePath.setToolTipText(
        BaseMessages.getString(PKG, "ActionIlivalidatorDialog.Input.FilePath.Tooltip"));
    PropsUi.setLook(wlFilePath);
    FormData fdlFilePath = new FormData();
    fdlFilePath.left = new FormAttachment(0, 0);
    fdlFilePath.top = new FormAttachment(modeComposite, margin);
    fdlFilePath.right = new FormAttachment(middle, -margin);
    wlFilePath.setLayoutData(fdlFilePath);

    wFilePath = new TextVar(variables, inputComposite, SWT.SINGLE | SWT.BORDER | SWT.LEFT);
    PropsUi.setLook(wFilePath);
    wFilePath.addModifyListener(e -> action.setChanged());
    FormData fdFilePath = new FormData();
    fdFilePath.left = new FormAttachment(middle, 0);
    fdFilePath.top = new FormAttachment(modeComposite, margin);
    fdFilePath.right = new FormAttachment(100, 0);
    wFilePath.setLayoutData(fdFilePath);

    Label wlFolderPath = new Label(inputComposite, SWT.RIGHT);
    wlFolderPath.setText(BaseMessages.getString(PKG, "ActionIlivalidatorDialog.Input.FolderPath"));
    wlFolderPath.setToolTipText(
        BaseMessages.getString(PKG, "ActionIlivalidatorDialog.Input.FolderPath.Tooltip"));
    PropsUi.setLook(wlFolderPath);
    FormData fdlFolderPath = new FormData();
    fdlFolderPath.left = new FormAttachment(0, 0);
    fdlFolderPath.top = new FormAttachment(wFilePath, margin);
    fdlFolderPath.right = new FormAttachment(middle, -margin);
    wlFolderPath.setLayoutData(fdlFolderPath);

    wFolderPath = new TextVar(variables, inputComposite, SWT.SINGLE | SWT.BORDER | SWT.LEFT);
    PropsUi.setLook(wFolderPath);
    wFolderPath.addModifyListener(e -> action.setChanged());
    FormData fdFolderPath = new FormData();
    fdFolderPath.left = new FormAttachment(middle, 0);
    fdFolderPath.top = new FormAttachment(wFilePath, margin);
    fdFolderPath.right = new FormAttachment(100, 0);
    wFolderPath.setLayoutData(fdFolderPath);

    Label wlRecursive = new Label(inputComposite, SWT.RIGHT);
    wlRecursive.setText(BaseMessages.getString(PKG, "ActionIlivalidatorDialog.Input.Recursive"));
    PropsUi.setLook(wlRecursive);
    FormData fdlRecursive = new FormData();
    fdlRecursive.left = new FormAttachment(0, 0);
    fdlRecursive.top = new FormAttachment(wFolderPath, margin);
    fdlRecursive.right = new FormAttachment(middle, -margin);
    wlRecursive.setLayoutData(fdlRecursive);

    wRecursive = new Button(inputComposite, SWT.CHECK);
    PropsUi.setLook(wRecursive);
    wRecursive.addListener(SWT.Selection, e -> action.setChanged());
    FormData fdRecursive = new FormData();
    fdRecursive.left = new FormAttachment(middle, 0);
    fdRecursive.top = new FormAttachment(wFolderPath, margin);
    fdRecursive.right = new FormAttachment(100, 0);
    wRecursive.setLayoutData(fdRecursive);

    Label wlIncludeMask = new Label(inputComposite, SWT.RIGHT);
    wlIncludeMask.setText(BaseMessages.getString(PKG, "ActionIlivalidatorDialog.Input.IncludeMask"));
    wlIncludeMask.setToolTipText(
        BaseMessages.getString(PKG, "ActionIlivalidatorDialog.Input.Mask.Tooltip"));
    PropsUi.setLook(wlIncludeMask);
    FormData fdlIncludeMask = new FormData();
    fdlIncludeMask.left = new FormAttachment(0, 0);
    fdlIncludeMask.top = new FormAttachment(wRecursive, margin);
    fdlIncludeMask.right = new FormAttachment(middle, -margin);
    wlIncludeMask.setLayoutData(fdlIncludeMask);

    wIncludeMask = new TextVar(variables, inputComposite, SWT.SINGLE | SWT.BORDER | SWT.LEFT);
    PropsUi.setLook(wIncludeMask);
    wIncludeMask.addModifyListener(e -> action.setChanged());
    FormData fdIncludeMask = new FormData();
    fdIncludeMask.left = new FormAttachment(middle, 0);
    fdIncludeMask.top = new FormAttachment(wRecursive, margin);
    fdIncludeMask.right = new FormAttachment(100, 0);
    wIncludeMask.setLayoutData(fdIncludeMask);

    Label wlExcludeMask = new Label(inputComposite, SWT.RIGHT);
    wlExcludeMask.setText(BaseMessages.getString(PKG, "ActionIlivalidatorDialog.Input.ExcludeMask"));
    wlExcludeMask.setToolTipText(
        BaseMessages.getString(PKG, "ActionIlivalidatorDialog.Input.Mask.Tooltip"));
    PropsUi.setLook(wlExcludeMask);
    FormData fdlExcludeMask = new FormData();
    fdlExcludeMask.left = new FormAttachment(0, 0);
    fdlExcludeMask.top = new FormAttachment(wIncludeMask, margin);
    fdlExcludeMask.right = new FormAttachment(middle, -margin);
    wlExcludeMask.setLayoutData(fdlExcludeMask);

    wExcludeMask = new TextVar(variables, inputComposite, SWT.SINGLE | SWT.BORDER | SWT.LEFT);
    PropsUi.setLook(wExcludeMask);
    wExcludeMask.addModifyListener(e -> action.setChanged());
    FormData fdExcludeMask = new FormData();
    fdExcludeMask.left = new FormAttachment(middle, 0);
    fdExcludeMask.top = new FormAttachment(wIncludeMask, margin);
    fdExcludeMask.right = new FormAttachment(100, 0);
    wExcludeMask.setLayoutData(fdExcludeMask);

    inputTab.setControl(inputComposite);
  }

  private void createValidationTab(CTabFolder tabFolder) {
    CTabItem validationTab = new CTabItem(tabFolder, SWT.NONE);
    validationTab.setText(BaseMessages.getString(PKG, "ActionIlivalidatorDialog.Tab.Validation"));

    Composite validationComposite = new Composite(tabFolder, SWT.NONE);
    PropsUi.setLook(validationComposite);
    FormLayout layout = new FormLayout();
    layout.marginWidth = PropsUi.getFormMargin();
    layout.marginHeight = PropsUi.getFormMargin();
    validationComposite.setLayout(layout);

    int margin = PropsUi.getMargin();
    int middle = props.getMiddlePct();

    Label wlModelNames = new Label(validationComposite, SWT.RIGHT);
    wlModelNames.setText(BaseMessages.getString(PKG, "ActionIlivalidatorDialog.Validation.ModelNames"));
    wlModelNames.setToolTipText(
        BaseMessages.getString(PKG, "ActionIlivalidatorDialog.Validation.Semicolon.Tooltip"));
    PropsUi.setLook(wlModelNames);
    FormData fdlModelNames = new FormData();
    fdlModelNames.left = new FormAttachment(0, 0);
    fdlModelNames.top = new FormAttachment(0, margin);
    fdlModelNames.right = new FormAttachment(middle, -margin);
    wlModelNames.setLayoutData(fdlModelNames);

    wModelNames = new TextVar(variables, validationComposite, SWT.SINGLE | SWT.BORDER | SWT.LEFT);
    PropsUi.setLook(wModelNames);
    wModelNames.addModifyListener(e -> action.setChanged());
    FormData fdModelNames = new FormData();
    fdModelNames.left = new FormAttachment(middle, 0);
    fdModelNames.top = new FormAttachment(0, margin);
    fdModelNames.right = new FormAttachment(100, 0);
    wModelNames.setLayoutData(fdModelNames);

    Label wlRepositoryUrls = new Label(validationComposite, SWT.RIGHT);
    wlRepositoryUrls.setText(
        BaseMessages.getString(PKG, "ActionIlivalidatorDialog.Validation.RepositoryUrls"));
    wlRepositoryUrls.setToolTipText(
        BaseMessages.getString(PKG, "ActionIlivalidatorDialog.Validation.Semicolon.Tooltip"));
    PropsUi.setLook(wlRepositoryUrls);
    FormData fdlRepositoryUrls = new FormData();
    fdlRepositoryUrls.left = new FormAttachment(0, 0);
    fdlRepositoryUrls.top = new FormAttachment(wModelNames, margin);
    fdlRepositoryUrls.right = new FormAttachment(middle, -margin);
    wlRepositoryUrls.setLayoutData(fdlRepositoryUrls);

    wRepositoryUrls =
        new TextVar(variables, validationComposite, SWT.SINGLE | SWT.BORDER | SWT.LEFT);
    PropsUi.setLook(wRepositoryUrls);
    wRepositoryUrls.addModifyListener(e -> action.setChanged());
    FormData fdRepositoryUrls = new FormData();
    fdRepositoryUrls.left = new FormAttachment(middle, 0);
    fdRepositoryUrls.top = new FormAttachment(wModelNames, margin);
    fdRepositoryUrls.right = new FormAttachment(100, 0);
    wRepositoryUrls.setLayoutData(fdRepositoryUrls);

    wFailFast =
        createCheckbox(
            validationComposite,
            middle,
            margin,
            BaseMessages.getString(PKG, "ActionIlivalidatorDialog.Validation.FailFast"),
            wRepositoryUrls);

    wStopOnFirstInvalid =
        createCheckbox(
            validationComposite,
            middle,
            margin,
            BaseMessages.getString(PKG, "ActionIlivalidatorDialog.Validation.StopOnFirstInvalid"),
            wFailFast);

    wAllObjectsAccessible =
        createCheckbox(
            validationComposite,
            middle,
            margin,
            BaseMessages.getString(PKG, "ActionIlivalidatorDialog.Validation.AllObjectsAccessible"),
            wStopOnFirstInvalid);

    validationTab.setControl(validationComposite);
  }

  private void createOutputTab(CTabFolder tabFolder) {
    CTabItem outputTab = new CTabItem(tabFolder, SWT.NONE);
    outputTab.setText(BaseMessages.getString(PKG, "ActionIlivalidatorDialog.Tab.Output"));

    Composite outputComposite = new Composite(tabFolder, SWT.NONE);
    PropsUi.setLook(outputComposite);
    FormLayout layout = new FormLayout();
    layout.marginWidth = PropsUi.getFormMargin();
    layout.marginHeight = PropsUi.getFormMargin();
    outputComposite.setLayout(layout);

    int margin = PropsUi.getMargin();
    int middle = props.getMiddlePct();

    Label wlWriteInvalidResultFiles = new Label(outputComposite, SWT.RIGHT);
    wlWriteInvalidResultFiles.setText(
        BaseMessages.getString(PKG, "ActionIlivalidatorDialog.Output.WriteInvalidResultFiles"));
    PropsUi.setLook(wlWriteInvalidResultFiles);
    FormData fdlWriteInvalidResultFiles = new FormData();
    fdlWriteInvalidResultFiles.left = new FormAttachment(0, 0);
    fdlWriteInvalidResultFiles.top = new FormAttachment(0, margin);
    fdlWriteInvalidResultFiles.right = new FormAttachment(middle, -margin);
    wlWriteInvalidResultFiles.setLayoutData(fdlWriteInvalidResultFiles);

    wWriteInvalidResultFiles = new Button(outputComposite, SWT.CHECK);
    PropsUi.setLook(wWriteInvalidResultFiles);
    wWriteInvalidResultFiles.addListener(SWT.Selection, e -> action.setChanged());
    FormData fdWriteInvalidResultFiles = new FormData();
    fdWriteInvalidResultFiles.left = new FormAttachment(middle, 0);
    fdWriteInvalidResultFiles.top = new FormAttachment(0, margin);
    fdWriteInvalidResultFiles.right = new FormAttachment(100, 0);
    wWriteInvalidResultFiles.setLayoutData(fdWriteInvalidResultFiles);

    outputTab.setControl(outputComposite);
  }

  private Button createCheckbox(
      Composite parent,
      int middle,
      int margin,
      String labelText,
      org.eclipse.swt.widgets.Control topControl) {
    Label label = new Label(parent, SWT.RIGHT);
    label.setText(labelText);
    PropsUi.setLook(label);
    FormData fdLabel = new FormData();
    fdLabel.left = new FormAttachment(0, 0);
    fdLabel.top = new FormAttachment(topControl, margin);
    fdLabel.right = new FormAttachment(middle, -margin);
    label.setLayoutData(fdLabel);

    Button check = new Button(parent, SWT.CHECK);
    PropsUi.setLook(check);
    check.addListener(SWT.Selection, e -> action.setChanged());
    FormData fdCheck = new FormData();
    fdCheck.left = new FormAttachment(middle, 0);
    fdCheck.top = new FormAttachment(topControl, margin);
    fdCheck.right = new FormAttachment(100, 0);
    check.setLayoutData(fdCheck);

    return check;
  }

  private void getData() {
    wName.setText(action.getName() == null ? "" : action.getName());

    boolean isFolderMode = ActionIlivalidator.INPUT_MODE_FOLDER.equalsIgnoreCase(action.getInputMode());
    wModeSingle.setSelection(!isFolderMode);
    wModeFolder.setSelection(isFolderMode);

    wFilePath.setText(action.getFilePath() == null ? "" : action.getFilePath());
    wFolderPath.setText(action.getFolderPath() == null ? "" : action.getFolderPath());
    wRecursive.setSelection(action.isRecursive());
    wIncludeMask.setText(action.getIncludeMask() == null ? "" : action.getIncludeMask());
    wExcludeMask.setText(action.getExcludeMask() == null ? "" : action.getExcludeMask());

    wModelNames.setText(action.getModelNames() == null ? "" : action.getModelNames());
    wRepositoryUrls.setText(action.getRepositoryUrls() == null ? "" : action.getRepositoryUrls());
    wFailFast.setSelection(action.isFailFast());
    wStopOnFirstInvalid.setSelection(action.isStopOnFirstInvalid());
    wAllObjectsAccessible.setSelection(action.isAllObjectsAccessible());

    wWriteInvalidResultFiles.setSelection(action.isWriteInvalidAsResultFiles());

    wName.setFocus();
  }

  private void enableInputModeControls() {
    boolean folder = wModeFolder.getSelection();
    wFilePath.setEnabled(!folder);
    wFolderPath.setEnabled(folder);
    wRecursive.setEnabled(folder);
    wIncludeMask.setEnabled(folder);
    wExcludeMask.setEnabled(folder);
  }

  private void cancel() {
    action.setChanged(backupChanged);
    dispose();
  }

  private void ok() {
    if (Utils.isEmpty(wName.getText())) {
      return;
    }

    action.setName(wName.getText());
    action.setInputMode(
        wModeFolder.getSelection() ? ActionIlivalidator.INPUT_MODE_FOLDER : ActionIlivalidator.INPUT_MODE_SINGLE);

    action.setFilePath(wFilePath.getText());
    action.setFolderPath(wFolderPath.getText());
    action.setRecursive(wRecursive.getSelection());
    action.setIncludeMask(wIncludeMask.getText());
    action.setExcludeMask(wExcludeMask.getText());

    action.setModelNames(wModelNames.getText());
    action.setRepositoryUrls(wRepositoryUrls.getText());
    action.setFailFast(wFailFast.getSelection());
    action.setStopOnFirstInvalid(wStopOnFirstInvalid.getSelection());
    action.setAllObjectsAccessible(wAllObjectsAccessible.getSelection());

    action.setWriteInvalidAsResultFiles(wWriteInvalidResultFiles.getSelection());

    action.setChanged();
    dispose();
  }
}
