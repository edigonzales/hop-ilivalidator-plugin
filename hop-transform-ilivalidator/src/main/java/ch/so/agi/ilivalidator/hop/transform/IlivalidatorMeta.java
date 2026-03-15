package ch.so.agi.ilivalidator.hop.transform;

import ch.so.agi.ilivalidator.core.validator.IlivalidatorOptionCodec;
import ch.so.agi.ilivalidator.core.validator.IlivalidatorOptionEntry;
import java.util.List;
import org.apache.hop.core.CheckResult;
import org.apache.hop.core.ICheckResult;
import org.apache.hop.core.annotations.Transform;
import org.apache.hop.core.exception.HopTransformException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.value.ValueMetaBoolean;
import org.apache.hop.core.row.value.ValueMetaString;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.metadata.api.HopMetadataProperty;
import org.apache.hop.metadata.api.IHopMetadataProvider;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransformMeta;
import org.apache.hop.pipeline.transform.TransformMeta;

@Transform(
    id = "INTERLIS_ILIVALIDATOR_TRANSFORM",
    name = "i18n::IlivalidatorMeta.Name",
    description = "i18n::IlivalidatorMeta.Description",
    image = "ch/so/agi/ilivalidator/hop/transform/icons/xml-validator.svg",
    categoryDescription = "i18n:org.apache.hop.pipeline.transform:BaseTransform.Category.Flow",
    documentationUrl = "/pipeline/transforms/ilivalidator.html",
    keywords = {"i18n::IlivalidatorMeta.keyword", "interlis", "validation"})
public class IlivalidatorMeta extends BaseTransformMeta<Ilivalidator, IlivalidatorData> {

  private static final Class<?> PKG = IlivalidatorMeta.class;

  @HopMetadataProperty private boolean useFilePathField = true;
  @HopMetadataProperty private String filePathField;
  @HopMetadataProperty private String staticFilePath;

  @HopMetadataProperty private String modelNames;
  @HopMetadataProperty private String repositoryUrls;
  @HopMetadataProperty private String configMode = "STATIC";
  @HopMetadataProperty private String configValue;
  @HopMetadataProperty private String configField;
  @HopMetadataProperty private String metaConfigMode = "STATIC";
  @HopMetadataProperty private String metaConfigValue;
  @HopMetadataProperty private String metaConfigField;
  @HopMetadataProperty private String serializedOptions;
  @HopMetadataProperty private boolean allObjectsAccessible;

  @HopMetadataProperty private boolean failPipelineOnInvalid;

  @HopMetadataProperty private String outputIsValidField = "is_valid";
  @HopMetadataProperty private String outputValidationMessageField = "validation_message";
  @HopMetadataProperty private String outputLogFilePathField = "log_file_path";

  @HopMetadataProperty private String logDirectory;
  @HopMetadataProperty private boolean logFileTimestamp;

  @Override
  public void setDefault() {
    useFilePathField = true;
    filePathField = "";
    staticFilePath = "";
    modelNames = "";
    repositoryUrls = "";
    configMode = "STATIC";
    configValue = "";
    configField = "";
    metaConfigMode = "STATIC";
    metaConfigValue = "";
    metaConfigField = "";
    serializedOptions = "";
    allObjectsAccessible = false;
    failPipelineOnInvalid = false;
    outputIsValidField = "is_valid";
    outputValidationMessageField = "validation_message";
    outputLogFilePathField = "log_file_path";
    logDirectory = "";
    logFileTimestamp = false;
  }

  @Override
  public void getFields(
      IRowMeta rowMeta,
      String origin,
      IRowMeta[] info,
      TransformMeta nextTransform,
      IVariables variables,
      IHopMetadataProvider metadataProvider)
      throws HopTransformException {
    rowMeta.addValueMeta(new ValueMetaBoolean(resolveFieldName(outputIsValidField)));
    rowMeta.addValueMeta(new ValueMetaString(resolveFieldName(outputValidationMessageField)));
    if (shouldAddLogFilePathField()) {
      rowMeta.addValueMeta(new ValueMetaString(resolveFieldName(outputLogFilePathField)));
    }
  }

  private String resolveFieldName(String fieldName) {
    return fieldName == null || fieldName.isBlank() ? "field" : fieldName;
  }

  @Override
  public void check(
      List<ICheckResult> remarks,
      PipelineMeta pipelineMeta,
      TransformMeta transformMeta,
      IRowMeta prev,
      String[] input,
      String[] output,
      IRowMeta info,
      IVariables variables,
      IHopMetadataProvider metadataProvider) {

    if (useFilePathField && (filePathField == null || filePathField.isBlank())) {
      remarks.add(
          new CheckResult(
              ICheckResult.TYPE_RESULT_ERROR,
              BaseMessages.getString(PKG, "IlivalidatorMeta.CheckResult.FilePathFieldMissing"),
              transformMeta));
    }

    if (!useFilePathField && (staticFilePath == null || staticFilePath.isBlank())) {
      remarks.add(
          new CheckResult(
              ICheckResult.TYPE_RESULT_ERROR,
              BaseMessages.getString(PKG, "IlivalidatorMeta.CheckResult.StaticPathMissing"),
              transformMeta));
    }

    if (isFieldMode(configMode) && (configField == null || configField.isBlank())) {
      remarks.add(
          new CheckResult(
              ICheckResult.TYPE_RESULT_ERROR,
              BaseMessages.getString(PKG, "IlivalidatorMeta.CheckResult.ConfigFieldMissing"),
              transformMeta));
    }

    if (isFieldMode(metaConfigMode) && (metaConfigField == null || metaConfigField.isBlank())) {
      remarks.add(
          new CheckResult(
              ICheckResult.TYPE_RESULT_ERROR,
              BaseMessages.getString(PKG, "IlivalidatorMeta.CheckResult.MetaConfigFieldMissing"),
              transformMeta));
    }

    if (input.length > 0 || !useFilePathField) {
      remarks.add(
          new CheckResult(
              ICheckResult.TYPE_RESULT_OK,
              BaseMessages.getString(PKG, "IlivalidatorMeta.CheckResult.Ok"),
              transformMeta));
    }
  }

  public boolean isUseFilePathField() {
    return useFilePathField;
  }

  public void setUseFilePathField(boolean useFilePathField) {
    this.useFilePathField = useFilePathField;
  }

  public String getFilePathField() {
    return filePathField;
  }

  public void setFilePathField(String filePathField) {
    this.filePathField = filePathField;
  }

  public String getStaticFilePath() {
    return staticFilePath;
  }

  public void setStaticFilePath(String staticFilePath) {
    this.staticFilePath = staticFilePath;
  }

  public String getModelNames() {
    return modelNames;
  }

  public void setModelNames(String modelNames) {
    this.modelNames = modelNames;
  }

  public String getRepositoryUrls() {
    return repositoryUrls;
  }

  public void setRepositoryUrls(String repositoryUrls) {
    this.repositoryUrls = repositoryUrls;
  }

  public String getConfigMode() {
    return configMode;
  }

  public void setConfigMode(String configMode) {
    this.configMode = configMode;
  }

  public String getConfigValue() {
    return configValue;
  }

  public void setConfigValue(String configValue) {
    this.configValue = configValue;
  }

  public String getConfigField() {
    return configField;
  }

  public void setConfigField(String configField) {
    this.configField = configField;
  }

  public String getMetaConfigMode() {
    return metaConfigMode;
  }

  public void setMetaConfigMode(String metaConfigMode) {
    this.metaConfigMode = metaConfigMode;
  }

  public String getMetaConfigValue() {
    return metaConfigValue;
  }

  public void setMetaConfigValue(String metaConfigValue) {
    this.metaConfigValue = metaConfigValue;
  }

  public String getMetaConfigField() {
    return metaConfigField;
  }

  public void setMetaConfigField(String metaConfigField) {
    this.metaConfigField = metaConfigField;
  }

  public String getSerializedOptions() {
    return serializedOptions;
  }

  public void setSerializedOptions(String serializedOptions) {
    this.serializedOptions = serializedOptions;
  }

  public boolean isAllObjectsAccessible() {
    return allObjectsAccessible;
  }

  public void setAllObjectsAccessible(boolean allObjectsAccessible) {
    this.allObjectsAccessible = allObjectsAccessible;
  }

  public boolean isFailPipelineOnInvalid() {
    return failPipelineOnInvalid;
  }

  public void setFailPipelineOnInvalid(boolean failPipelineOnInvalid) {
    this.failPipelineOnInvalid = failPipelineOnInvalid;
  }

  public String getOutputIsValidField() {
    return outputIsValidField;
  }

  public void setOutputIsValidField(String outputIsValidField) {
    this.outputIsValidField = outputIsValidField;
  }

  public String getOutputValidationMessageField() {
    return outputValidationMessageField;
  }

  public void setOutputValidationMessageField(String outputValidationMessageField) {
    this.outputValidationMessageField = outputValidationMessageField;
  }

  public String getOutputLogFilePathField() {
    return outputLogFilePathField;
  }

  public void setOutputLogFilePathField(String outputLogFilePathField) {
    this.outputLogFilePathField = outputLogFilePathField;
  }

  public String getLogDirectory() {
    return logDirectory;
  }

  public void setLogDirectory(String logDirectory) {
    this.logDirectory = logDirectory;
  }

  public boolean isLogFileTimestamp() {
    return logFileTimestamp;
  }

  public void setLogFileTimestamp(boolean logFileTimestamp) {
    this.logFileTimestamp = logFileTimestamp;
  }

  private boolean shouldAddLogFilePathField() {
    if (logDirectory != null && !logDirectory.isBlank()) {
      return true;
    }
    for (IlivalidatorOptionEntry entry : IlivalidatorOptionCodec.decode(serializedOptions)) {
      if (entry == null || !entry.isEnabled() || entry.getKey() == null) {
        continue;
      }
      if ("log".equalsIgnoreCase(entry.getKey().trim())) {
        return true;
      }
    }
    return false;
  }

  private static boolean isFieldMode(String mode) {
    return "FIELD".equalsIgnoreCase(mode);
  }
}
