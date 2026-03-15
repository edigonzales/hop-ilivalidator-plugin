package ch.so.agi.ilivalidator.hop.transform;

import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.so.agi.ilivalidator.core.validator.IlivalidatorOptionCodec;
import ch.so.agi.ilivalidator.core.validator.IlivalidatorOptionEntry;
import java.util.List;
import org.apache.hop.core.row.RowMeta;
import org.junit.jupiter.api.Test;

class IlivalidatorMetaTest {

  @Test
  void shouldAddConfiguredOutputFields() throws Exception {
    IlivalidatorMeta meta = new IlivalidatorMeta();
    meta.setOutputIsValidField("is_valid");
    meta.setOutputValidationMessageField("validation_message");

    RowMeta rowMeta = new RowMeta();
    meta.getFields(rowMeta, "origin", null, null, null, null);

    assertTrue(rowMeta.indexOfValue("is_valid") >= 0);
    assertTrue(rowMeta.indexOfValue("validation_message") >= 0);
  }

  @Test
  void shouldAddLogOutputFieldWhenLogOptionIsEnabled() throws Exception {
    IlivalidatorMeta meta = new IlivalidatorMeta();
    meta.setDefault();
    meta.setSerializedOptions(
        IlivalidatorOptionCodec.encode(List.of(new IlivalidatorOptionEntry("log", true, "validator.log"))));

    RowMeta rowMeta = new RowMeta();
    meta.getFields(rowMeta, "origin", null, null, null, null);

    assertTrue(rowMeta.indexOfValue("log_file_path") >= 0);
  }
}
