package ch.so.agi.ilivalidator.core.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ch.ehi.basics.settings.Settings;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.interlis2.validator.Validator;

class IlivalidatorOptionApplierTest {

  @Test
  void shouldApplyBooleanAndTransientOptions() {
    Settings settings = new Settings();

    IlivalidatorOptionApplier.apply(
        settings,
        List.of(
            new IlivalidatorOptionEntry("verbose", true, "true"),
            new IlivalidatorOptionEntry("singlePass", true, ""),
            new IlivalidatorOptionEntry("skipPolygonBuilding", true, "false"),
            new IlivalidatorOptionEntry("logtime", true, "false"),
            new IlivalidatorOptionEntry("forceTypeValidation", true, "true")));

    assertEquals("true", settings.getTransientValue("ch.interlis.iox_j.validator.verbose"));
    assertEquals("doSinglePass", settings.getValue("ch.interlis.iox_j.validator.doSinglePass"));
    assertEquals("off", settings.getValue("ch.interlis.iox_j.validator.doItfLinetables"));
    assertEquals(Validator.FALSE, settings.getValue(Validator.SETTING_LOGFILE_TIMESTAMP));
    assertEquals(Validator.TRUE, settings.getValue(Validator.SETTING_FORCE_TYPE_VALIDATION));
  }
}
