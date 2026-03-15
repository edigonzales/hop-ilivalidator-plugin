package ch.so.agi.ilivalidator.core.validator;

import ch.ehi.basics.logging.EhiLogger;
import ch.ehi.basics.logging.LogEvent;
import ch.ehi.basics.logging.LogListener;
import ch.ehi.basics.settings.Settings;
import ch.interlis.iox.IoxLogEvent;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.interlis2.validator.Validator;
import org.xml.sax.SAXException;

public class IlivalidatorService {

  private static final String DEFAULT_ILIDIRS = "%ITF_DIR;http://models.interlis.ch/;%JAR_DIR/ilimodels";
  private static final String DEFAULT_VALIDATION_FAILURE_MESSAGE =
      "INTERLIS validation failed without detailed error event";
  private static final Object VALIDATOR_LOCK = new Object();
  private final IlivalidatorExternalLogSink externalLogSink;

  public IlivalidatorService() {
    this(null);
  }

  public IlivalidatorService(IlivalidatorExternalLogSink externalLogSink) {
    this.externalLogSink = externalLogSink;
  }

  public IlivalidatorResult validate(Path path, IlivalidatorOptions options) {
    Objects.requireNonNull(path, "path");
    IlivalidatorOptions effectiveOptions = options == null ? IlivalidatorOptions.defaults() : options;
    return validateInternal(path, path.toString(), effectiveOptions);
  }

  private IlivalidatorResult validateInternal(
      Path path, String checkedFile, IlivalidatorOptions options) {
    IlivalidatorResultBuilder out = new IlivalidatorResultBuilder(checkedFile);

    try {
      parseSecure(path);
    } catch (IOException e) {
      out.addIssue(
          "IO_ERROR",
          "Failed to read file: " + e.getMessage(),
          IlivalidatorIssue.Severity.ERROR,
          null,
          null);
      return out.build();
    } catch (ParserConfigurationException | SAXException e) {
      out.addIssue(
          "PARSER_ERROR",
          "Failed to parse XML safely: " + e.getMessage(),
          IlivalidatorIssue.Severity.ERROR,
          null,
          null);
      return out.build();
    }

    if (options.isRunIlivalidator()) {
      runIlivalidator(path, options, out);
    }

    return out.build();
  }

  private void runIlivalidator(Path path, IlivalidatorOptions options, IlivalidatorResultBuilder out) {
    Settings settings = createValidatorSettings(path, options, out);

    EhiLogger logger = EhiLogger.getInstance();
    LogListener externalLogListener = createExternalLogListener();
    CollectingLogListener logListener = new CollectingLogListener(out);

    synchronized (VALIDATOR_LOCK) {
      logger.addListener(logListener);
      if (externalLogListener != null) {
        logger.addListener(externalLogListener);
      }

      try {
        boolean validatorResult = Validator.runValidation(new String[] {path.toString()}, settings);
        addValidationFailureIssueIfNeeded(
            validatorResult,
            logListener.getErrorCount(),
            logListener.getFirstNonInfoMessage(),
            logListener.getFirstNonInfoLine(),
            out);
      } catch (RuntimeException e) {
        out.addIssue(
            "ILI_RUNTIME_ERROR",
            "INTERLIS validator runtime failure: " + e.getMessage(),
            IlivalidatorIssue.Severity.ERROR,
            null,
            null);
      } finally {
        if (externalLogListener != null) {
          logger.removeListener(externalLogListener);
        }
        logger.removeListener(logListener);
      }
    }
  }

  static Settings createValidatorSettings(
      Path path, IlivalidatorOptions options, IlivalidatorResultBuilder out) {
    Settings settings = new Settings();

    if (options.getRepositoryUrls().isEmpty()) {
      settings.setValue(Validator.SETTING_ILIDIRS, DEFAULT_ILIDIRS);
    } else {
      settings.setValue(
          Validator.SETTING_ILIDIRS,
          options.getRepositoryUrls().stream().collect(Collectors.joining(";")));
    }

    if (!options.getModelNames().isEmpty()) {
      settings.setValue(
          Validator.SETTING_MODELNAMES,
          options.getModelNames().stream().collect(Collectors.joining(";")));
    }

    if (options.isAllObjectsAccessible()) {
      settings.setValue(Validator.SETTING_ALL_OBJECTS_ACCESSIBLE, Validator.TRUE);
    }

    if (options.getConfigFile() != null && !options.getConfigFile().isBlank()) {
      settings.setValue(Validator.SETTING_CONFIGFILE, options.getConfigFile());
    }

    if (options.getMetaConfigFile() != null && !options.getMetaConfigFile().isBlank()) {
      settings.setValue(Validator.SETTING_META_CONFIGFILE, options.getMetaConfigFile());
    }

    if (options.getLogDirectory() != null && !options.getLogDirectory().isBlank()) {
      java.nio.file.Path logDir = java.nio.file.Path.of(options.getLogDirectory());
      java.nio.file.Path logFile = logDir.resolve(path.getFileName() + ".log");
      settings.setValue(Validator.SETTING_LOGFILE, logFile.toString());
      if (options.isLogFileTimestamp()) {
        settings.setValue(Validator.SETTING_LOGFILE_TIMESTAMP, Validator.TRUE);
      }
    }

    IlivalidatorOptionApplier.apply(settings, options.getOptionEntries());

    String effectiveLogFilePath = settings.getValue(Validator.SETTING_LOGFILE);
    if (out != null) {
      out.setLogFilePath(
          effectiveLogFilePath == null || effectiveLogFilePath.isBlank()
              ? null
              : effectiveLogFilePath);
    }

    return settings;
  }

  private LogListener createExternalLogListener() {
    if (externalLogSink == null) {
      return null;
    }
    return event -> {
      if (event == null) {
        return;
      }
      IlivalidatorExternalLogLevel level = toExternalLevel(event.getEventKind());
      String message = formatMessage(level, event.getEventMsg());
      externalLogSink.log(level, message, event.getException());
    };
  }

  private void parseSecure(Path path)
      throws ParserConfigurationException, IOException, SAXException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    factory.setXIncludeAware(false);
    factory.setExpandEntityReferences(false);

    factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
    factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

    DocumentBuilder builder = factory.newDocumentBuilder();
    try (InputStream inputStream = Files.newInputStream(path)) {
      builder.parse(inputStream);
    }
  }

  static IlivalidatorIssue.Severity toSeverity(LogEvent event) {
    int eventKind = event.getEventKind();
    if (event instanceof IoxLogEvent) {
      if (eventKind == IoxLogEvent.ERROR) {
        return IlivalidatorIssue.Severity.ERROR;
      }
      if (eventKind == IoxLogEvent.WARNING) {
        return IlivalidatorIssue.Severity.WARNING;
      }
      if (eventKind == IoxLogEvent.INFO || eventKind == IoxLogEvent.DETAIL_INFO) {
        return IlivalidatorIssue.Severity.INFO;
      }
    }

    if (eventKind == LogEvent.ERROR) {
      return IlivalidatorIssue.Severity.ERROR;
    }
    if (eventKind == LogEvent.ADAPTION) {
      return IlivalidatorIssue.Severity.WARNING;
    }
    if (eventKind == LogEvent.STATE) {
      return IlivalidatorIssue.Severity.INFO;
    }
    return IlivalidatorIssue.Severity.INFO;
  }

  private static IlivalidatorExternalLogLevel toExternalLevel(int eventKind) {
    if (eventKind == LogEvent.ERROR || eventKind == IoxLogEvent.ERROR) {
      return IlivalidatorExternalLogLevel.ERROR;
    }
    if (eventKind == LogEvent.ADAPTION || eventKind == IoxLogEvent.WARNING) {
      return IlivalidatorExternalLogLevel.WARN;
    }
    if (eventKind == IoxLogEvent.DETAIL_INFO
        || eventKind == LogEvent.DEBUG_TRACE
        || eventKind == LogEvent.STATE_TRACE
        || eventKind == LogEvent.UNUSUAL_STATE_TRACE
        || eventKind == LogEvent.BACKEND_CMD) {
      return IlivalidatorExternalLogLevel.DEBUG;
    }
    return IlivalidatorExternalLogLevel.INFO;
  }

  private static String formatMessage(IlivalidatorExternalLogLevel level, String eventMessage) {
    String prefix =
        switch (level) {
          case ERROR -> "Error";
          case WARN -> "Warning";
          case INFO -> "Info";
          case DEBUG -> "Debug";
        };

    if (eventMessage == null || eventMessage.isBlank()) {
      return prefix;
    }
    String trimmed = eventMessage.trim();
    if (trimmed.startsWith(prefix + ":")) {
      return trimmed;
    }
    return prefix + ": " + trimmed;
  }

  static void addValidationFailureIssueIfNeeded(
      boolean validatorResult,
      int errorCount,
      String firstNonInfoMessage,
      Integer firstNonInfoLine,
      IlivalidatorResultBuilder out) {
    if (validatorResult) {
      return;
    }

    out.invalidate();
    if (errorCount > 0) {
      return;
    }

    out.addIssue(
        "ILI_VALIDATION_ERROR",
        resolveValidationFailureMessage(firstNonInfoMessage),
        IlivalidatorIssue.Severity.ERROR,
        firstNonInfoLine,
        null);
  }

  static String resolveValidationFailureMessage(String firstNonInfoMessage) {
    if (firstNonInfoMessage == null || firstNonInfoMessage.isBlank()) {
      return DEFAULT_VALIDATION_FAILURE_MESSAGE;
    }
    return firstNonInfoMessage;
  }

  private static final class CollectingLogListener implements LogListener {

    private final IlivalidatorResultBuilder out;
    private int errorCount;
    private String firstNonInfoMessage;
    private Integer firstNonInfoLine;

    private CollectingLogListener(IlivalidatorResultBuilder out) {
      this.out = out;
    }

    @Override
    public void logEvent(LogEvent event) {
      IlivalidatorIssue.Severity severity = IlivalidatorService.toSeverity(event);

      // Keep the historical NORMAL behavior: include warnings/errors, skip plain info events.
      if (severity == IlivalidatorIssue.Severity.INFO) {
        return;
      }

      Integer line = null;
      Integer column = null;
      String code = "ILI_EVENT";
      String message = event.getEventMsg();

      if (event instanceof IoxLogEvent ioxLogEvent) {
        line = ioxLogEvent.getSourceLineNr();
        code =
            ioxLogEvent.getEventId() == null || ioxLogEvent.getEventId().isBlank()
                ? "ILI_EVENT"
                : ioxLogEvent.getEventId();
      }

      if (firstNonInfoMessage == null && message != null && !message.isBlank()) {
        firstNonInfoMessage = message;
      }
      if (firstNonInfoLine == null && line != null) {
        firstNonInfoLine = line;
      }

      if (severity == IlivalidatorIssue.Severity.ERROR && message != null) {
        String lower = message.toLowerCase(Locale.ROOT);
        if (lower.contains("compiler failed") || lower.contains("only interlis version")) {
          code = "ILI_COMPILER_ERROR";
        } else if (lower.contains("failed to get ili file")) {
          code = "ILI_MODEL_RESOLUTION_ERROR";
        }
      }

      out.addIssue(code, message, severity, line, column);
      if (severity == IlivalidatorIssue.Severity.ERROR) {
        errorCount++;
      }
    }

    private int getErrorCount() {
      return errorCount;
    }

    private String getFirstNonInfoMessage() {
      return firstNonInfoMessage;
    }

    private Integer getFirstNonInfoLine() {
      return firstNonInfoLine;
    }
  }
}
