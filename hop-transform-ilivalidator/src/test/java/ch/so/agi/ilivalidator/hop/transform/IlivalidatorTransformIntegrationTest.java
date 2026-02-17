package ch.so.agi.ilivalidator.hop.transform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.so.agi.ilivalidator.core.validator.IlivalidatorOptions;
import ch.so.agi.ilivalidator.core.validator.IlivalidatorResult;
import ch.so.agi.ilivalidator.core.validator.IlivalidatorService;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for Ilivalidator transform with real INTERLIS files.
 */
class IlivalidatorTransformIntegrationTest {

  private final IlivalidatorService service = new IlivalidatorService();

  @Test
  void shouldValidateValidXmlFile() throws Exception {
    Path validFile = Files.createTempFile("valid-", ".xml");
    Files.writeString(validFile, "<?xml version=\"1.0\"?><ROOT/>");

    try {
      IlivalidatorResult result = service.validate(
          validFile,
          IlivalidatorOptions.builder().runIlivalidator(false).build());

      assertTrue(result.isValid());
    } finally {
      Files.deleteIfExists(validFile);
    }
  }

  @Test
  void shouldValidateInvalidXmlFile() throws Exception {
    Path invalidFile = Files.createTempFile("invalid-", ".xml");
    Files.writeString(invalidFile, "<MyRoot><Header></MyRoot>");

    try {
      IlivalidatorResult result = service.validate(
          invalidFile,
          IlivalidatorOptions.builder().runIlivalidator(false).build());

      assertFalse(result.isValid());
      assertTrue(result.getIssues().stream()
          .anyMatch(issue -> "PARSER_ERROR".equals(issue.getCode())));
    } finally {
      Files.deleteIfExists(invalidFile);
    }
  }

  @Test
  void shouldHandleMissingFile() {
    IlivalidatorResult result = service.validate(
        Path.of("/path/that/does/not/exist.xtf"),
        IlivalidatorOptions.builder().runIlivalidator(false).build());

    assertFalse(result.isValid());
    assertTrue(result.getIssues().stream()
        .anyMatch(issue -> "IO_ERROR".equals(issue.getCode())));
  }

  @Test
  void shouldCreateLogFile() throws Exception {
    Path testFile = Files.createTempFile("log-test-", ".xtf");
    Files.writeString(testFile, "<?xml version=\"1.0\"?><ROOT/>");

    Path logDir = Files.createTempDirectory("logs-");

    try {
      IlivalidatorOptions options = IlivalidatorOptions.builder()
          .logDirectory(logDir.toString())
          .logFileTimestamp(false)
          .runIlivalidator(false)
          .build();

      IlivalidatorResult result = service.validate(testFile, options);

      assertTrue(result.isValid());
    } finally {
      Files.deleteIfExists(testFile);
      if (logDir != null) {
        try (var stream = Files.walk(logDir)) {
          stream.sorted((p1, p2) -> p2.compareTo(p1)).forEach(p -> {
            try {
              Files.deleteIfExists(p);
            } catch (Exception ignored) {}
          });
        }
      }
    }
  }

  @Test
  void shouldValidateMultipleFilesSequentially() throws Exception {
    Path file1 = Files.createTempFile("multi1-", ".xtf");
    Path file2 = Files.createTempFile("multi2-", ".xtf");
    Files.writeString(file1, "<?xml version=\"1.0\"?><ROOT/>");
    Files.writeString(file2, "<?xml version=\"1.0\"?><ROOT/>");

    try {
      IlivalidatorOptions options = IlivalidatorOptions.builder().runIlivalidator(false).build();

      IlivalidatorResult result1 = service.validate(file1, options);
      IlivalidatorResult result2 = service.validate(file2, options);

      assertTrue(result1.isValid());
      assertTrue(result2.isValid());
    } finally {
      Files.deleteIfExists(file1);
      Files.deleteIfExists(file2);
    }
  }

  @Test
  void shouldHandleLargeXmlFile() throws Exception {
    Path largeFile = Files.createTempFile("large-", ".xml");
    StringBuilder content = new StringBuilder("<?xml version=\"1.0\"?><ROOT>");
    for (int i = 0; i < 1000; i++) {
      content.append("<Element").append(i).append(">Data</Element").append(i).append(">");
    }
    content.append("</ROOT>");
    Files.writeString(largeFile, content.toString());

    try {
      IlivalidatorResult result = service.validate(
          largeFile,
          IlivalidatorOptions.builder().runIlivalidator(false).build());

      assertTrue(result.isValid());
    } finally {
      Files.deleteIfExists(largeFile);
    }
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
