package ch.so.agi.ilivalidator.hop.action;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ActionIlivalidatorDialog.
 * <p>
 * Note: Full GUI testing requires SWT bot or similar framework.
 * These tests verify the dialog structure using reflection.
 */
class ActionIlivalidatorDialogTest {

  @Test
  void shouldHaveBrowseButtonFields() throws Exception {
    Class<?> clazz = ActionIlivalidatorDialog.class;

    Field wbFilePath = clazz.getDeclaredField("wbFilePath");
    Field wbFolderPath = clazz.getDeclaredField("wbFolderPath");
    Field wbLogDirectory = clazz.getDeclaredField("wbLogDirectory");

    assertNotNull(wbFilePath);
    assertNotNull(wbFolderPath);
    assertNotNull(wbLogDirectory);
  }

  @Test
  void shouldHaveTextVarFields() throws Exception {
    Class<?> clazz = ActionIlivalidatorDialog.class;

    Field wFilePath = clazz.getDeclaredField("wFilePath");
    Field wFolderPath = clazz.getDeclaredField("wFolderPath");
    Field wLogDirectory = clazz.getDeclaredField("wLogDirectory");

    assertNotNull(wFilePath);
    assertNotNull(wFolderPath);
    assertNotNull(wLogDirectory);
  }
}
