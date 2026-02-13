package ch.so.agi.ilivalidator.hop.action;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class ActionIlivalidatorTest {

  @Test
  void shouldCollectFolderFilesDeterministically() throws Exception {
    Path baseDir = Files.createTempDirectory("action-ilivalidator-");
    try {
      Path c = Files.createFile(baseDir.resolve("c.xtf"));
      Path a = Files.createFile(baseDir.resolve("a.xtf"));
      Files.createFile(baseDir.resolve("b.txt"));

      List<Path> files =
          ActionIlivalidator.collectFolderFiles(
              baseDir.toString(), false, "regex:.*\\.xtf", "");

      assertEquals(List.of(a.toAbsolutePath(), c.toAbsolutePath()), files);
    } finally {
      deleteRecursively(baseDir);
    }
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
