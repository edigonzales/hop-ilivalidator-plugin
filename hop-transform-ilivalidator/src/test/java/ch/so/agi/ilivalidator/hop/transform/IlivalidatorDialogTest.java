package ch.so.agi.ilivalidator.hop.transform;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for IlivalidatorDialog.
 * <p>
 * Note: Full GUI testing requires SWT bot or similar framework.
 * These tests verify the dialog structure using reflection.
 */
class IlivalidatorDialogTest {

  @Test
  void shouldHaveBrowseButtonFields() throws Exception {
    Class<?> clazz = IlivalidatorDialog.class;

    Field wbStaticFilePath = clazz.getDeclaredField("wbStaticFilePath");
    Field wbLogDirectory = clazz.getDeclaredField("wbLogDirectory");

    assertNotNull(wbStaticFilePath);
    assertNotNull(wbLogDirectory);
  }

  @Test
  void shouldHaveTextVarFields() throws Exception {
    Class<?> clazz = IlivalidatorDialog.class;

    Field wStaticFilePath = clazz.getDeclaredField("wStaticFilePath");
    Field wLogDirectory = clazz.getDeclaredField("wLogDirectory");

    assertNotNull(wStaticFilePath);
    assertNotNull(wLogDirectory);
  }
}
