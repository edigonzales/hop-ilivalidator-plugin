package ch.so.agi.ilivalidator.hop.transform;

import ch.so.agi.ilivalidator.core.validator.IlivalidatorExternalLogLevel;
import ch.so.agi.ilivalidator.core.validator.IlivalidatorIssue;
import ch.so.agi.ilivalidator.core.validator.IlivalidatorOptionCodec;
import ch.so.agi.ilivalidator.core.validator.IlivalidatorOptionEntry;
import ch.so.agi.ilivalidator.core.validator.IlivalidatorOptions;
import ch.so.agi.ilivalidator.core.validator.IlivalidatorResult;
import ch.so.agi.ilivalidator.core.validator.IlivalidatorService;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.exception.HopTransformException;
import org.apache.hop.core.exception.HopValueException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.RowDataUtil;
import org.apache.hop.core.row.RowMeta;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.pipeline.Pipeline;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransform;
import org.apache.hop.pipeline.transform.TransformMeta;

public class Ilivalidator extends BaseTransform<IlivalidatorMeta, IlivalidatorData> {

  private static final Class<?> PKG = IlivalidatorMeta.class;
  private static final String MODE_FIELD = "FIELD";
  private static final Set<String> TECHNICAL_ERROR_CODES =
      Set.of(
          "IO_ERROR",
          "PARSER_ERROR",
          "ILI_RUNTIME_ERROR",
          "ILI_COMPILER_ERROR",
          "ILI_MODEL_RESOLUTION_ERROR");

  private final IlivalidatorService service;

  public Ilivalidator(
      TransformMeta transformMeta,
      IlivalidatorMeta meta,
      IlivalidatorData data,
      int copyNr,
      PipelineMeta pipelineMeta,
      Pipeline pipeline) {
    this(transformMeta, meta, data, copyNr, pipelineMeta, pipeline, null);
  }

  Ilivalidator(
      TransformMeta transformMeta,
      IlivalidatorMeta meta,
      IlivalidatorData data,
      int copyNr,
      PipelineMeta pipelineMeta,
      Pipeline pipeline,
      IlivalidatorService service) {
    super(transformMeta, meta, data, copyNr, pipelineMeta, pipeline);
    this.service = service == null ? new IlivalidatorService(this::logExternalMessage) : service;
  }

  @Override
  public boolean processRow() throws HopException {
    Object[] row = getRow();

    if (!data.initialized) {
      initializeData();
    }

    if (row == null) {
      if (canEmitSingleStaticRow()) {
        data.emittedSingleStaticRow = true;
        IlivalidatorResult result = getStaticValidationResult();
        putRow(data.outputRowMeta, createOutputRow(new Object[0], result));
        return true;
      }

      setOutputDone();
      return false;
    }

    IlivalidatorResult result =
        meta.isUseFilePathField() ? validateFromInputRow(row) : getStaticValidationResult();

    if (isTechnicalFailure(result)) {
      throw new HopTransformException(
          BaseMessages.getString(
              PKG,
              "Ilivalidator.Transform.TechnicalFailureException",
              result.getCheckedFile(),
              toValidationMessage(result)));
    }

    if (!result.isValid() && meta.isFailPipelineOnInvalid()) {
      throw new HopTransformException(
          BaseMessages.getString(PKG, "Ilivalidator.Transform.InvalidDataException", result.getCheckedFile()));
    }

    putRow(data.outputRowMeta, createOutputRow(row, result));

    if (!result.isValid() && isBasic()) {
      logBasic(
          BaseMessages.getString(
              PKG,
              "Ilivalidator.Transform.InvalidBasicLog",
              result.getCheckedFile(),
              Integer.toString(result.getIssues().size())));
    }
    if (!result.isValid()) {
      IlivalidatorIssue firstErrorIssue = firstErrorIssue(result);
      if (firstErrorIssue != null) {
        logError("Error: " + toErrorLogMessage(firstErrorIssue));
      }
    }
    if (!result.isValid() && isDetailed()) {
      for (IlivalidatorIssue issue : result.getIssues()) {
        logDetailed(issue.getSeverity() + " " + issue.getCode() + " - " + issue.getMessage());
      }
    }

    return true;
  }

  private void initializeData() throws HopTransformException {
    IRowMeta inputRowMeta = getInputRowMeta();
    data.outputRowMeta = inputRowMeta == null ? new RowMeta() : inputRowMeta.clone();

    meta.getFields(
        data.outputRowMeta,
        getTransformName(),
        null,
        null,
        this,
        getPipelineMeta().getMetadataProvider());

    if (meta.isUseFilePathField()) {
      if (inputRowMeta == null) {
        throw new HopTransformException(
            BaseMessages.getString(PKG, "Ilivalidator.Transform.NoInputRowMeta"));
      }
      data.inputFilePathIndex = inputRowMeta.indexOfValue(meta.getFilePathField());
      if (data.inputFilePathIndex < 0) {
        throw new HopTransformException(
            BaseMessages.getString(PKG, "Ilivalidator.Transform.FilePathFieldNotFound", meta.getFilePathField()));
      }
    }

    if (isFieldMode(meta.getConfigMode())) {
      if (inputRowMeta == null) {
        throw new HopTransformException(
            BaseMessages.getString(PKG, "Ilivalidator.Transform.NoInputRowMeta"));
      }
      data.inputConfigFieldIndex = inputRowMeta.indexOfValue(meta.getConfigField());
      if (data.inputConfigFieldIndex < 0) {
        throw new HopTransformException(
            BaseMessages.getString(PKG, "Ilivalidator.Transform.ConfigFieldNotFound", meta.getConfigField()));
      }
    }

    if (isFieldMode(meta.getMetaConfigMode())) {
      if (inputRowMeta == null) {
        throw new HopTransformException(
            BaseMessages.getString(PKG, "Ilivalidator.Transform.NoInputRowMeta"));
      }
      data.inputMetaConfigFieldIndex = inputRowMeta.indexOfValue(meta.getMetaConfigField());
      if (data.inputMetaConfigFieldIndex < 0) {
        throw new HopTransformException(
            BaseMessages.getString(
                PKG, "Ilivalidator.Transform.MetaConfigFieldNotFound", meta.getMetaConfigField()));
      }
    }

    data.outputIsValidIndex = data.outputRowMeta.indexOfValue(meta.getOutputIsValidField());
    data.outputValidationMessageIndex =
        data.outputRowMeta.indexOfValue(meta.getOutputValidationMessageField());
    data.outputLogFilePathIndex = data.outputRowMeta.indexOfValue(meta.getOutputLogFilePathField());
    data.optionEntries = decodeAndResolveOptions(meta.getSerializedOptions());
    data.initialized = true;
  }

  private IlivalidatorOptions createOptions(Object[] row) throws HopTransformException {
    return IlivalidatorOptions.builder()
        .modelNames(splitSemicolon(resolve(meta.getModelNames())))
        .repositoryUrls(splitSemicolon(resolve(meta.getRepositoryUrls())))
        .configFile(resolveConfigValue(row))
        .metaConfigFile(resolveMetaConfigValue(row))
        .optionEntries(data.optionEntries)
        .allObjectsAccessible(meta.isAllObjectsAccessible())
        .logDirectory(resolve(meta.getLogDirectory()))
        .logFileTimestamp(meta.isLogFileTimestamp())
        .build();
  }

  private IlivalidatorResult validateFromInputRow(Object[] row) throws HopException {
    String inputFilePath = getInputRowMeta().getString(row, data.inputFilePathIndex);
    if (inputFilePath == null || inputFilePath.isBlank()) {
      throw new HopTransformException(
          BaseMessages.getString(PKG, "Ilivalidator.Transform.EmptyInputPath", meta.getFilePathField()));
    }

    String resolvedPath = resolve(inputFilePath);
    return service.validate(Path.of(resolvedPath), createOptions(row));
  }

  private IlivalidatorResult getStaticValidationResult() throws HopTransformException {
    if (data.cachedStaticResult == null) {
      String staticPath = resolve(meta.getStaticFilePath());
      data.cachedStaticResult = service.validate(Path.of(staticPath), createOptions(null));
    }
    return data.cachedStaticResult;
  }

  private boolean canEmitSingleStaticRow() {
    return !meta.isUseFilePathField() && !data.emittedSingleStaticRow && getInputRowMeta() == null;
  }

  private String resolveConfigValue(Object[] row) throws HopTransformException {
    if (!isFieldMode(meta.getConfigMode())) {
      return resolve(meta.getConfigValue());
    }
    return readOptionalFieldValue(
        row,
        data.inputConfigFieldIndex,
        meta.getConfigField(),
        "Ilivalidator.Transform.ConfigFieldReadError");
  }

  private String resolveMetaConfigValue(Object[] row) throws HopTransformException {
    if (!isFieldMode(meta.getMetaConfigMode())) {
      return resolve(meta.getMetaConfigValue());
    }
    return readOptionalFieldValue(
        row,
        data.inputMetaConfigFieldIndex,
        meta.getMetaConfigField(),
        "Ilivalidator.Transform.MetaConfigFieldReadError");
  }

  private String readOptionalFieldValue(
      Object[] row, int fieldIndex, String fieldName, String messageKey) throws HopTransformException {
    if (row == null || fieldIndex < 0) {
      return null;
    }
    try {
      return getInputRowMeta().getString(row, fieldIndex);
    } catch (HopValueException e) {
      throw new HopTransformException(BaseMessages.getString(PKG, messageKey, fieldName), e);
    }
  }

  private List<IlivalidatorOptionEntry> decodeAndResolveOptions(String serialized) {
    List<IlivalidatorOptionEntry> source = IlivalidatorOptionCodec.decode(serialized);
    List<IlivalidatorOptionEntry> resolved = new ArrayList<>(source.size());
    for (IlivalidatorOptionEntry entry : source) {
      if (entry == null) {
        continue;
      }
      String value = entry.getValue() == null ? null : resolve(entry.getValue());
      resolved.add(new IlivalidatorOptionEntry(entry.getKey(), entry.isEnabled(), value));
    }
    return resolved;
  }

  private static List<String> splitSemicolon(String value) {
    if (value == null || value.isBlank()) {
      return List.of();
    }

    String[] split = value.split(";");
    List<String> values = new ArrayList<>(split.length);
    for (String item : split) {
      if (item != null && !item.isBlank()) {
        values.add(item.trim());
      }
    }
    return values;
  }

  private Object[] createOutputRow(Object[] inputRow, IlivalidatorResult result) {
    Object[] outputRow = RowDataUtil.createResizedCopy(inputRow, data.outputRowMeta.size());

    outputRow[data.outputIsValidIndex] = result.isValid();
    outputRow[data.outputValidationMessageIndex] = toValidationMessage(result);
    if (data.outputLogFilePathIndex >= 0) {
      outputRow[data.outputLogFilePathIndex] = result.getLogFilePath();
    }

    return outputRow;
  }

  private void logExternalMessage(
      IlivalidatorExternalLogLevel level, String message, Throwable throwable) {
    if (level == null || message == null || message.isBlank()) {
      return;
    }
    switch (level) {
      case ERROR -> {
        if (throwable == null) {
          logError(message);
        } else {
          logError(message, throwable);
        }
      }
      case WARN, INFO -> {
        if (isBasic()) {
          logBasic(message);
        }
      }
      case DEBUG -> {
        if (isDetailed()) {
          logDetailed(message);
        }
      }
    }
  }

  private String toValidationMessage(IlivalidatorResult result) {
    if (result.isValid()) {
      return "OK";
    }
    if (result.getIssues().isEmpty()) {
      return "Validation failed";
    }
    IlivalidatorIssue firstErrorIssue = firstErrorIssue(result);
    IlivalidatorIssue selectedIssue =
        firstErrorIssue == null ? result.getIssues().get(0) : firstErrorIssue;
    return selectedIssue.getCode() + ": " + selectedIssue.getMessage();
  }

  private static IlivalidatorIssue firstErrorIssue(IlivalidatorResult result) {
    for (IlivalidatorIssue issue : result.getIssues()) {
      if (issue.getSeverity() == IlivalidatorIssue.Severity.ERROR) {
        return issue;
      }
    }
    return null;
  }

  private static String toErrorLogMessage(IlivalidatorIssue issue) {
    if (issue.getMessage() != null && !issue.getMessage().isBlank()) {
      return issue.getMessage();
    }
    return issue.getCode() == null || issue.getCode().isBlank() ? "Validation failed" : issue.getCode();
  }

  private boolean isTechnicalFailure(IlivalidatorResult result) {
    for (IlivalidatorIssue issue : result.getIssues()) {
      if (issue.getSeverity() != IlivalidatorIssue.Severity.ERROR) {
        continue;
      }

      String code = Objects.requireNonNullElse(issue.getCode(), "").toUpperCase(Locale.ROOT);
      if (TECHNICAL_ERROR_CODES.contains(code)) {
        return true;
      }

      String message = Objects.requireNonNullElse(issue.getMessage(), "").toLowerCase(Locale.ROOT);
      if (message.contains("compiler failed")
          || message.contains("only interlis version")
          || message.contains("failed to get ili file")
          || message.contains("cannot be cast to class antlr")) {
        return true;
      }
    }
    return false;
  }

  private static boolean isFieldMode(String mode) {
    return MODE_FIELD.equalsIgnoreCase(mode);
  }
}
