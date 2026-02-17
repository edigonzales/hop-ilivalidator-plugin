package ch.so.agi.ilivalidator.hop.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.hop.core.exception.HopWorkflowException;
import org.junit.jupiter.api.Test;

class ActionIlivalidatorExecuteTest {

  @Test
  void shouldCollectFilesInFolderMode() throws Exception {
    Path baseDir = Files.createTempDirectory("action-folder-");
    try {
      Files.createFile(baseDir.resolve("file1.xtf"));
      Files.createFile(baseDir.resolve("file2.xtf"));
      Files.createFile(baseDir.resolve("file3.txt"));

      var files = ActionIlivalidator.collectFolderFiles(
          baseDir.toString(), false, "glob:*.xtf", "");

      assertEquals(2, files.size());
      assertTrue(files.stream().allMatch(p -> p.toString().endsWith(".xtf")));
    } finally {
      deleteRecursively(baseDir);
    }
  }

  @Test
  void shouldCollectFilesRecursively() throws Exception {
    Path baseDir = Files.createTempDirectory("action-recursive-");
    try {
      Path subDir = Files.createDirectory(baseDir.resolve("subdir"));
      Files.createFile(baseDir.resolve("file1.xtf"));
      Files.createFile(subDir.resolve("file2.xtf"));

      var files = ActionIlivalidator.collectFolderFiles(
          baseDir.toString(), true, "glob:*.xtf", "");

      assertEquals(2, files.size());
    } finally {
      deleteRecursively(baseDir);
    }
  }

  @Test
  void shouldFilterWithIncludeMask() throws Exception {
    Path baseDir = Files.createTempDirectory("action-include-");
    try {
      Files.createFile(baseDir.resolve("data.xtf"));
      Files.createFile(baseDir.resolve("data.xml"));
      Files.createFile(baseDir.resolve("readme.txt"));

      var files = ActionIlivalidator.collectFolderFiles(
          baseDir.toString(), false, "regex:.*\\.xtf$", "");

      assertEquals(1, files.size());
      assertTrue(files.get(0).toString().endsWith(".xtf"));
    } finally {
      deleteRecursively(baseDir);
    }
  }

  @Test
  void shouldFilterWithExcludeMask() throws Exception {
    Path baseDir = Files.createTempDirectory("action-exclude-");
    try {
      Files.createFile(baseDir.resolve("keep1.xtf"));
      Files.createFile(baseDir.resolve("keep2.xtf"));
      Files.createFile(baseDir.resolve("exclude.xtf.bak"));

      var files = ActionIlivalidator.collectFolderFiles(
          baseDir.toString(), false, "glob:*.xtf*", "glob:*.bak");

      assertEquals(2, files.size());
      assertTrue(files.stream().noneMatch(p -> p.toString().endsWith(".bak")));
    } finally {
      deleteRecursively(baseDir);
    }
  }

  @Test
  void shouldThrowForMissingFolder() throws Exception {
    assertThrows(
        HopWorkflowException.class,
        () -> ActionIlivalidator.collectFolderFiles(
            "/path/that/does/not/exist", false, "", ""));
  }

  @Test
  void shouldReturnEmptyListForEmptyFilePath() throws Exception {
    ActionIlivalidator action = new ActionIlivalidator("TestAction");
    action.setInputMode(ActionIlivalidator.INPUT_MODE_SINGLE);
    action.setFilePath("");

    var files = action.collectInputFiles();

    assertTrue(files.isEmpty());
  }

  @Test
  void shouldCollectSingleFile() throws Exception {
    Path file = Files.createTempFile("single-", ".xtf");
    try {
      ActionIlivalidator action = new ActionIlivalidator("TestAction");
      action.setInputMode(ActionIlivalidator.INPUT_MODE_SINGLE);
      action.setFilePath(file.toString());

      var files = action.collectInputFiles();

      assertEquals(1, files.size());
    } finally {
      Files.deleteIfExists(file);
    }
  }

  @Test
  void shouldSplitSemicolonValues() {
    ActionIlivalidator action = new ActionIlivalidator("TestAction");
    action.setModelNames("ModelA;ModelB;ModelC");
    action.setRepositoryUrls("http://example.com");

    assertEquals("ModelA;ModelB;ModelC", action.getModelNames());
  }

  private void deleteRecursively(Path baseDir) throws IOException {
    try (var stream = Files.walk(baseDir)) {
      stream.sorted((p1, p2) -> p2.compareTo(p1)).forEach(
          path -> {
            try {
              Files.deleteIfExists(path);
            } catch (IOException ignored) {
              // cleanup best effort
            }
          });
    }
  }
}
