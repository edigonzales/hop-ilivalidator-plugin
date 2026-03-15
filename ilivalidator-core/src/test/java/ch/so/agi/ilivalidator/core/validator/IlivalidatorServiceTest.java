package ch.so.agi.ilivalidator.core.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.ehi.basics.logging.LogEvent;
import ch.ehi.basics.settings.Settings;
import ch.interlis.iox.IoxLogEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.interlis2.validator.Validator;

class IlivalidatorServiceTest {

  private final IlivalidatorService service = new IlivalidatorService();

  @Test
  void shouldReportIoErrorForMissingFile() {
    IlivalidatorResult result =
        service.validate(
            Path.of("/path/that/does/not/exist.xtf"),
            IlivalidatorOptions.builder().runIlivalidator(false).build());

    assertFalse(result.isValid());
    assertTrue(result.getIssues().stream().anyMatch(issue -> "IO_ERROR".equals(issue.getCode())));
  }

  @Test
  void shouldReportParserErrorForMalformedXml() throws IOException {
    Path malformed = Files.createTempFile("malformed-", ".xml");
    Files.writeString(malformed, "<MyRoot><Header></MyRoot>");

    try {
      IlivalidatorResult result =
          service.validate(malformed, IlivalidatorOptions.builder().runIlivalidator(false).build());

      assertFalse(result.isValid());
      assertTrue(
          result.getIssues().stream().anyMatch(issue -> "PARSER_ERROR".equals(issue.getCode())));
    } finally {
      Files.deleteIfExists(malformed);
    }
  }

  @Test
  void shouldReturnValidForWellFormedXmlWhenValidatorIsDisabled() {
    Path validXml = Path.of("src/test/resources/ch/so/agi/ilivalidator/core/validator/rules-valid.xml");

    IlivalidatorResult result =
        service.validate(validXml, IlivalidatorOptions.builder().runIlivalidator(false).build());

    assertTrue(result.isValid());
  }

  @Test
  void shouldMapSeverityForLogAndIoxEventKinds() {
    assertEquals(
        IlivalidatorIssue.Severity.ERROR,
        IlivalidatorService.toSeverity(new TestLogEvent(LogEvent.ERROR, "error")));
    assertEquals(
        IlivalidatorIssue.Severity.WARNING,
        IlivalidatorService.toSeverity(new TestLogEvent(LogEvent.ADAPTION, "warning")));
    assertEquals(
        IlivalidatorIssue.Severity.INFO,
        IlivalidatorService.toSeverity(new TestLogEvent(LogEvent.STATE, "info")));

    assertEquals(
        IlivalidatorIssue.Severity.ERROR,
        IlivalidatorService.toSeverity(new TestIoxLogEvent(IoxLogEvent.ERROR, "iox error")));
    assertEquals(
        IlivalidatorIssue.Severity.WARNING,
        IlivalidatorService.toSeverity(new TestIoxLogEvent(IoxLogEvent.WARNING, "iox warning")));
    assertEquals(
        IlivalidatorIssue.Severity.INFO,
        IlivalidatorService.toSeverity(new TestIoxLogEvent(IoxLogEvent.INFO, "iox info")));
    assertEquals(
        IlivalidatorIssue.Severity.INFO,
        IlivalidatorService.toSeverity(new TestIoxLogEvent(IoxLogEvent.DETAIL_INFO, "iox detail")));
  }

  @Test
  void shouldCreateConcreteErrorIssueWhenValidationFailsWithoutErrorEvents() {
    IlivalidatorResultBuilder out = new IlivalidatorResultBuilder("test.xtf");
    String message = "value YYYYYYYYYRichtplan is not a member of the enumeration in attribute Stand";

    IlivalidatorService.addValidationFailureIssueIfNeeded(false, 0, message, 37, out);
    IlivalidatorResult result = out.build();

    assertFalse(result.isValid());
    assertTrue(
        result.getIssues().stream()
            .anyMatch(
                issue ->
                    "ILI_VALIDATION_ERROR".equals(issue.getCode())
                        && issue.getSeverity() == IlivalidatorIssue.Severity.ERROR
                        && message.equals(issue.getMessage())
                        && Integer.valueOf(37).equals(issue.getLine())));
  }

  @Test
  void shouldNotCreateSyntheticErrorIssueWhenErrorEventAlreadyExists() {
    IlivalidatorResultBuilder out = new IlivalidatorResultBuilder("test.xtf");
    out.addIssue(
        "ILI_EVENT",
        "Explicit validator error",
        IlivalidatorIssue.Severity.ERROR,
        null,
        null);

    IlivalidatorService.addValidationFailureIssueIfNeeded(
        false, 1, "would be fallback but should not be used", 10, out);
    IlivalidatorResult result = out.build();

    assertFalse(result.isValid());
    assertEquals(1, result.getIssues().size());
    assertEquals("ILI_EVENT", result.getIssues().get(0).getCode());
  }

  @Test
  void shouldCreateValidatorSettingsWithOptionOverrides() {
    IlivalidatorOptions options =
        IlivalidatorOptions.builder()
            .modelNames(List.of("DefaultModel"))
            .repositoryUrls(List.of("https://models.example.org"))
            .configFile("config/default.ini")
            .metaConfigFile("meta/default.ini")
            .allObjectsAccessible(true)
            .logDirectory("logs")
            .optionEntries(
                List.of(
                    new IlivalidatorOptionEntry("models", true, "OverrideModel"),
                    new IlivalidatorOptionEntry("modeldir", true, "ilidata:ch.so.agi.models"),
                    new IlivalidatorOptionEntry("config", true, "ilidata:ch.so.agi.config"),
                    new IlivalidatorOptionEntry("metaConfig", true, "ilidata:ch.so.agi.meta"),
                    new IlivalidatorOptionEntry("allObjectsAccessible", true, "false"),
                    new IlivalidatorOptionEntry("log", true, "logs/override.log")))
            .build();

    IlivalidatorResultBuilder out = new IlivalidatorResultBuilder("dataset.xtf");
    Settings settings = IlivalidatorService.createValidatorSettings(Path.of("dataset.xtf"), options, out);

    assertEquals("OverrideModel", settings.getValue(Validator.SETTING_MODELNAMES));
    assertEquals("ilidata:ch.so.agi.models", settings.getValue(Validator.SETTING_ILIDIRS));
    assertEquals("ilidata:ch.so.agi.config", settings.getValue(Validator.SETTING_CONFIGFILE));
    assertEquals("ilidata:ch.so.agi.meta", settings.getValue(Validator.SETTING_META_CONFIGFILE));
    assertEquals("false", settings.getValue(Validator.SETTING_ALL_OBJECTS_ACCESSIBLE));
    assertEquals("logs/override.log", settings.getValue(Validator.SETTING_LOGFILE));
    assertEquals("logs/override.log", out.build().getLogFilePath());
  }

  @Test
  void shouldKeepIliDataConfigValuesUntouched() {
    IlivalidatorOptions options =
        IlivalidatorOptions.builder()
            .configFile("ilidata:ch.so.agi.validation")
            .metaConfigFile("ilidata:ch.so.agi.meta")
            .build();

    IlivalidatorResultBuilder out = new IlivalidatorResultBuilder("dataset.xtf");
    Settings settings = IlivalidatorService.createValidatorSettings(Path.of("dataset.xtf"), options, out);

    assertEquals("ilidata:ch.so.agi.validation", settings.getValue(Validator.SETTING_CONFIGFILE));
    assertEquals("ilidata:ch.so.agi.meta", settings.getValue(Validator.SETTING_META_CONFIGFILE));
  }

  @Test
  void shouldRejectInvalidIntegerOptionValues() {
    Settings settings = new Settings();

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                IlivalidatorOptionApplier.apply(
                    settings, List.of(new IlivalidatorOptionEntry("proxyPort", true, "abc"))));

    assertTrue(exception.getMessage().contains("proxyPort"));
  }

  private static class TestLogEvent implements LogEvent {

    private final int eventKind;
    private final String message;

    private TestLogEvent(int eventKind, String message) {
      this.eventKind = eventKind;
      this.message = message;
    }

    @Override
    public int getEventKind() {
      return eventKind;
    }

    @Override
    public String getEventMsg() {
      return message;
    }

    @Override
    public Throwable getException() {
      return null;
    }

    @Override
    public StackTraceElement getOrigin() {
      return null;
    }

    @Override
    public int getCustomLevel() {
      return LogEvent.LEVEL_UNDEFINED;
    }
  }

  private static final class TestIoxLogEvent extends TestLogEvent implements IoxLogEvent {

    private final String message;

    private TestIoxLogEvent(int eventKind, String message) {
      super(eventKind, message);
      this.message = message;
    }

    @Override
    public String getRawEventMsg() {
      return message;
    }

    @Override
    public Date getEventDateTime() {
      return null;
    }

    @Override
    public String getEventId() {
      return null;
    }

    @Override
    public String getDataSource() {
      return null;
    }

    @Override
    public Integer getSourceLineNr() {
      return null;
    }

    @Override
    public String getSourceObjectTag() {
      return null;
    }

    @Override
    public String getSourceObjectTechId() {
      return null;
    }

    @Override
    public String getSourceObjectXtfId() {
      return null;
    }

    @Override
    public String getSourceObjectUsrId() {
      return null;
    }

    @Override
    public String getModelEleQName() {
      return null;
    }

    @Override
    public Double getGeomC1() {
      return null;
    }

    @Override
    public Double getGeomC2() {
      return null;
    }

    @Override
    public Double getGeomC3() {
      return null;
    }
  }
}
