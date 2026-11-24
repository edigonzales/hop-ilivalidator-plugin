package ch.so.agi.ilivalidator.hop.transform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.hop.ui.core.gui.WindowProperty;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.Test;

class IlivalidatorDialogTest {

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
