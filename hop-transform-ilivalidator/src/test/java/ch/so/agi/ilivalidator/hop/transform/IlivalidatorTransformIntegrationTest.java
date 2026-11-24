package ch.so.agi.ilivalidator.hop.transform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.so.agi.ilivalidator.core.validator.IlivalidatorOptions;
import ch.so.agi.ilivalidator.core.validator.IlivalidatorResult;
import ch.so.agi.ilivalidator.core.validator.IlivalidatorService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Integration tests for Ilivalidator transform with real INTERLIS files.
 */
class IlivalidatorTransformIntegrationTest {

  private static final Path FIXTURES = Path.of("..", "e2e", "fixtures").toAbsolutePath();
  private final IlivalidatorService service = new IlivalidatorService();

  @Test
  void shouldValidateValidTransferFile() {
    IlivalidatorResult result = service.validate(FIXTURES.resolve("valid.xtf"), options().build());

    assertTrue(result.isValid(), () -> result.getIssues().toString());
  }

  @Test
  void shouldReportInvalidXmlFile(@TempDir Path directory) throws IOException {
    Path invalidFile = directory.resolve("invalid.xml");
    Files.writeString(invalidFile, "<MyRoot><Header></MyRoot>");

    IlivalidatorResult result = service.validate(invalidFile, IlivalidatorOptions.defaults());

    assertFalse(result.isValid());
    assertTrue(result.getIssues().stream()
        .anyMatch(issue -> "PARSER_ERROR".equals(issue.getCode())));
  }

  @Test
  void shouldHandleMissingFile(@TempDir Path directory) {
    IlivalidatorResult result =
        service.validate(directory.resolve("missing.xtf"), IlivalidatorOptions.defaults());

    assertFalse(result.isValid());
    assertTrue(result.getIssues().stream()
        .anyMatch(issue -> "IO_ERROR".equals(issue.getCode())));
  }

  @Test
  void shouldCreateLogFile(@TempDir Path directory) throws IOException {
    IlivalidatorResult result = service.validate(
        FIXTURES.resolve("valid.xtf"), options().logDirectory(directory.toString()).build());

    assertTrue(result.isValid());
    Path log = directory.resolve("valid.xtf.log");
    assertEquals(log.toString(), result.getLogFilePath());
    assertFalse(Files.readString(log).isBlank());
  }

  @Test
  void shouldValidateMultipleFilesSequentially(@TempDir Path directory) throws IOException {
    Path valid = FIXTURES.resolve("valid.xtf");
    Path invalid = directory.resolve("invalid.xtf");
    Files.writeString(invalid, Files.readString(valid).replace("<Name>Example</Name>", ""));
    IlivalidatorOptions options = options().build();

    assertFalse(service.validate(invalid, options).isValid());
    assertTrue(service.validate(valid, options).isValid());
  }

  @Test
  void shouldHandleLargeTransfer(@TempDir Path directory) throws IOException {
    Path largeFile = directory.resolve("large.xtf");
    String items = IntStream.range(0, 1000)
        .mapToObj(i -> "<TransferInputTest.Data.Item TID=\"i" + i
            + "\"><Name>Example</Name></TransferInputTest.Data.Item>")
        .collect(Collectors.joining("\n"));
    Files.writeString(largeFile, Files.readString(FIXTURES.resolve("valid.xtf"))
        .replace("<TransferInputTest.Data.Item TID=\"i1\"><Name>Example</Name></TransferInputTest.Data.Item>",
            items));

    IlivalidatorResult result = service.validate(largeFile, options().build());

    assertTrue(result.isValid(), () -> result.getIssues().toString());
  }

  private IlivalidatorOptions.Builder options() {
    return IlivalidatorOptions.builder().repositoryUrls(List.of(FIXTURES.toString()));
  }

  @Test
  void shouldSplitSemicolonValues() {
    IlivalidatorMeta meta = new IlivalidatorMeta();
    meta.setModelNames("ModelA;ModelB;ModelC");

    assertEquals("ModelA;ModelB;ModelC", meta.getModelNames());
  }

  @Test
  void shouldCreateOutputFields() throws Exception {
    IlivalidatorMeta meta = new IlivalidatorMeta();
    meta.setOutputIsValidField("is_valid");
    meta.setOutputValidationMessageField("validation_message");

    org.apache.hop.core.row.RowMeta rowMeta = new org.apache.hop.core.row.RowMeta();
    meta.getFields(rowMeta, "origin", null, null, null, null);

    assertTrue(rowMeta.indexOfValue("is_valid") >= 0);
    assertTrue(rowMeta.indexOfValue("validation_message") >= 0);
  }
}
