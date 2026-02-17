package ch.so.agi.ilivalidator.hop.action;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.hop.core.exception.HopWorkflowException;
import org.junit.jupiter.api.Test;

class ActionIlivalidatorMaskMatcherTest {

  @Test
  void shouldMatchGlobPattern() throws Exception {
    ActionIlivalidator.MaskMatcher matcher = ActionIlivalidator.MaskMatcher.of("glob:*.xtf", false);

    assertTrue(matcher.matches("data.xtf"));
    assertTrue(matcher.matches("test.xtf"));
    assertFalse(matcher.matches("data.xml"));
    assertFalse(matcher.matches("readme.txt"));
  }

  @Test
  void shouldMatchGlobPatternWithSubfolder() throws Exception {
    ActionIlivalidator.MaskMatcher matcher = ActionIlivalidator.MaskMatcher.of("glob:*.xtf", false);

    assertTrue(matcher.matches("data.xtf"));
    assertFalse(matcher.matches("subdir/data.xtf"));
  }

  @Test
  void shouldMatchRegexPattern() throws Exception {
    ActionIlivalidator.MaskMatcher matcher = ActionIlivalidator.MaskMatcher.of("regex:.*\\.xtf$", false);

    assertTrue(matcher.matches("data.xtf"));
    assertTrue(matcher.matches("test.xtf"));
    assertFalse(matcher.matches("data.xml"));
    assertFalse(matcher.matches("readme.txt"));
  }

  @Test
  void shouldMatchRegexPatternWithPrefix() throws Exception {
    ActionIlivalidator.MaskMatcher matcher = ActionIlivalidator.MaskMatcher.of("regex:.*\\.xtf$", false);

    assertTrue(matcher.matches("data.xtf"));
    assertFalse(matcher.matches("data.xtf.bak"));
  }

  @Test
  void shouldMatchDefaultWhenMaskIsNull() throws Exception {
    ActionIlivalidator.MaskMatcher matcher = ActionIlivalidator.MaskMatcher.of(null, true);

    assertTrue(matcher.matches("anything.xtf"));
    assertTrue(matcher.matches("anything.xml"));
  }

  @Test
  void shouldMatchDefaultWhenMaskIsEmpty() throws Exception {
    ActionIlivalidator.MaskMatcher matcher = ActionIlivalidator.MaskMatcher.of("", true);

    assertTrue(matcher.matches("anything.xtf"));
  }

  @Test
  void shouldNotMatchDefaultWhenDefaultMatchIsFalse() throws Exception {
    ActionIlivalidator.MaskMatcher matcher = ActionIlivalidator.MaskMatcher.of(null, false);

    assertFalse(matcher.matches("anything.xtf"));
  }

  @Test
  void shouldThrowForInvalidRegex() {
    assertThrows(
        HopWorkflowException.class,
        () -> ActionIlivalidator.MaskMatcher.of("regex:[invalid", false));
  }

  @Test
  void shouldThrowForInvalidGlobPattern() {
    assertThrows(
        java.util.regex.PatternSyntaxException.class,
        () -> ActionIlivalidator.MaskMatcher.of("glob:[invalid", false));
  }

  @Test
  void shouldHandleRegexWithoutPrefix() throws Exception {
    ActionIlivalidator.MaskMatcher matcher = ActionIlivalidator.MaskMatcher.of(".*\\.xtf$", false);

    assertTrue(matcher.matches("data.xtf"));
    assertFalse(matcher.matches("data.xml"));
  }

  @Test
  void shouldMatchCaseSensitive() throws Exception {
    ActionIlivalidator.MaskMatcher matcher = ActionIlivalidator.MaskMatcher.of("glob:*.XTF", false);

    assertFalse(matcher.matches("data.xtf"));
    assertTrue(matcher.matches("data.XTF"));
  }

  @Test
  void shouldMatchComplexGlobPattern() throws Exception {
    ActionIlivalidator.MaskMatcher matcher = ActionIlivalidator.MaskMatcher.of("glob:data-*.xtf", false);

    assertTrue(matcher.matches("data-2024.xtf"));
    assertTrue(matcher.matches("data-test.xtf"));
    assertFalse(matcher.matches("test-data.xtf"));
  }
}
