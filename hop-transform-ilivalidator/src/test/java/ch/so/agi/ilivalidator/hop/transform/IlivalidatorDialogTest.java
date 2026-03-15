package ch.so.agi.ilivalidator.hop.transform;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import org.apache.hop.ui.core.gui.WindowProperty;
import org.eclipse.swt.graphics.Rectangle;
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
    Field wbConfigValue = clazz.getDeclaredField("wbConfigValue");
    Field wbMetaConfigValue = clazz.getDeclaredField("wbMetaConfigValue");
    Field wbLogDirectory = clazz.getDeclaredField("wbLogDirectory");

    assertNotNull(wbStaticFilePath);
    assertNotNull(wbConfigValue);
    assertNotNull(wbMetaConfigValue);
    assertNotNull(wbLogDirectory);
  }

  @Test
  void shouldHaveValidationConfigAndOptionsWidgets() throws Exception {
    Class<?> clazz = IlivalidatorDialog.class;

    Field wStaticFilePath = clazz.getDeclaredField("wStaticFilePath");
    Field wConfigMode = clazz.getDeclaredField("wConfigMode");
    Field wConfigValue = clazz.getDeclaredField("wConfigValue");
    Field wConfigField = clazz.getDeclaredField("wConfigField");
    Field wMetaConfigMode = clazz.getDeclaredField("wMetaConfigMode");
    Field wMetaConfigValue = clazz.getDeclaredField("wMetaConfigValue");
    Field wMetaConfigField = clazz.getDeclaredField("wMetaConfigField");
    Field wLogDirectory = clazz.getDeclaredField("wLogDirectory");
    Field wOptions = clazz.getDeclaredField("wOptions");

    assertNotNull(wStaticFilePath);
    assertNotNull(wConfigMode);
    assertNotNull(wConfigValue);
    assertNotNull(wConfigField);
    assertNotNull(wMetaConfigMode);
    assertNotNull(wMetaConfigValue);
    assertNotNull(wMetaConfigField);
    assertNotNull(wLogDirectory);
    assertNotNull(wOptions);
  }

  @Test
  void shouldCreateDefaultWindowProperty() {
    WindowProperty windowProperty = IlivalidatorDialog.createDefaultWindowProperty("Dialog");

    assertEquals("Dialog", windowProperty.getName());
    assertFalse(windowProperty.isMaximized());
    assertEquals(980, windowProperty.getWidth());
    assertEquals(760, windowProperty.getHeight());
  }

  @Test
  void shouldNormalizeOversizedAndMaximizedWindowProperty() {
    WindowProperty windowProperty = new WindowProperty("Dialog", true, -1, -1, 4000, 3000);

    boolean changed =
        IlivalidatorDialog.normalizeWindowProperty(windowProperty, new Rectangle(0, 0, 1200, 900));

    assertTrue(changed);
    assertFalse(windowProperty.isMaximized());
    assertEquals(980, windowProperty.getWidth());
    assertEquals(760, windowProperty.getHeight());
  }

  @Test
  void shouldNormalizeInvalidWindowPropertySize() {
    WindowProperty windowProperty = new WindowProperty("Dialog", false, -1, -1, 0, -1);

    boolean changed =
        IlivalidatorDialog.normalizeWindowProperty(windowProperty, new Rectangle(0, 0, 1200, 900));

    assertTrue(changed);
    assertEquals(980, windowProperty.getWidth());
    assertEquals(760, windowProperty.getHeight());
  }

  @Test
  void shouldLeaveValidWindowPropertyUntouched() {
    WindowProperty windowProperty = new WindowProperty("Dialog", false, -1, -1, 900, 700);

    boolean changed =
        IlivalidatorDialog.normalizeWindowProperty(windowProperty, new Rectangle(0, 0, 1200, 900));

    assertFalse(changed);
    assertEquals(900, windowProperty.getWidth());
    assertEquals(700, windowProperty.getHeight());
  }
}
