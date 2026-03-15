package ch.so.agi.ilivalidator.hop.transform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.so.agi.ilivalidator.core.validator.IlivalidatorOptions;
import ch.so.agi.ilivalidator.core.validator.IlivalidatorResult;
import ch.so.agi.ilivalidator.core.validator.IlivalidatorService;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import org.apache.hop.core.IRowSet;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.exception.HopTransformException;
import org.apache.hop.core.exception.HopValueException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.RowMeta;
import org.apache.hop.core.row.value.ValueMetaString;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.IRowHandler;
import org.apache.hop.pipeline.transform.TransformMeta;
import org.junit.jupiter.api.Test;

class IlivalidatorConfigModeTest {

  @Test
  void shouldUseFieldBasedConfigAndMetaConfigPerRow() throws Exception {
    IlivalidatorMeta meta = createDefaultMeta();
    meta.setConfigMode("FIELD");
    meta.setConfigField("config_field");
    meta.setMetaConfigMode("FIELD");
    meta.setMetaConfigField("meta_config_field");

    RowMeta inputRowMeta = new RowMeta();
    inputRowMeta.addValueMeta(new ValueMetaString("input_file"));
    inputRowMeta.addValueMeta(new ValueMetaString("config_field"));
    inputRowMeta.addValueMeta(new ValueMetaString("meta_config_field"));

    TestRowHandler rowHandler =
        new TestRowHandler(
            new Object[] {"data/first.xtf", "ilidata:ch.so.agi.config.one", "meta/one.ini"},
            new Object[] {"data/second.xtf", "config/two.ini", "ilidata:ch.so.agi.meta.two"});
    CapturingService service = new CapturingService();

    Ilivalidator transform = createTransform(meta, inputRowMeta, rowHandler, service);
    initializeTransform(transform);

    assertTrue(transform.processRow());
    assertTrue(transform.processRow());
    assertFalse(transform.processRow());

    assertEquals(2, service.calls.size());
    assertEquals("ilidata:ch.so.agi.config.one", service.calls.get(0).options().getConfigFile());
    assertEquals("meta/one.ini", service.calls.get(0).options().getMetaConfigFile());
    assertEquals("config/two.ini", service.calls.get(1).options().getConfigFile());
    assertEquals("ilidata:ch.so.agi.meta.two", service.calls.get(1).options().getMetaConfigFile());
    assertEquals(Boolean.TRUE, getOutputValue(rowHandler, 0, "is_valid"));
    assertEquals("OK", getOutputValue(rowHandler, 0, "validation_message"));
  }

  @Test
  void shouldPassThroughStaticIliDataConfigValues() throws Exception {
    IlivalidatorMeta meta = createDefaultMeta();
    meta.setUseFilePathField(false);
    meta.setStaticFilePath("data/static.xtf");
    meta.setConfigMode("STATIC");
    meta.setConfigValue("ilidata:ch.so.agi.validation");
    meta.setMetaConfigMode("STATIC");
    meta.setMetaConfigValue("ilidata:ch.so.agi.meta");

    TestRowHandler rowHandler = new TestRowHandler();
    CapturingService service = new CapturingService();

    Ilivalidator transform = createTransform(meta, null, rowHandler, service);
    initializeTransform(transform);

    assertTrue(transform.processRow());
    assertFalse(transform.processRow());

    assertEquals(1, service.calls.size());
    assertEquals("ilidata:ch.so.agi.validation", service.calls.get(0).options().getConfigFile());
    assertEquals("ilidata:ch.so.agi.meta", service.calls.get(0).options().getMetaConfigFile());
  }

  @Test
  void shouldRequireInputMetadataForConfigFieldMode() throws Exception {
    IlivalidatorMeta meta = createDefaultMeta();
    meta.setUseFilePathField(false);
    meta.setStaticFilePath("data/static.xtf");
    meta.setConfigMode("FIELD");
    meta.setConfigField("config_field");

    Ilivalidator transform = createTransform(meta, null, new TestRowHandler(), new CapturingService());

    HopTransformException exception =
        assertThrows(HopTransformException.class, () -> initializeTransform(transform));

    assertEquals(
        BaseMessages.getString(IlivalidatorMeta.class, "Ilivalidator.Transform.NoInputRowMeta"),
        exception.getMessage().trim());
  }

  @Test
  void shouldRequireExistingConfigField() throws Exception {
    IlivalidatorMeta meta = createDefaultMeta();
    meta.setConfigMode("FIELD");
    meta.setConfigField("config_field");

    RowMeta inputRowMeta = new RowMeta();
    inputRowMeta.addValueMeta(new ValueMetaString("input_file"));

    Ilivalidator transform =
        createTransform(meta, inputRowMeta, new TestRowHandler(), new CapturingService());

    HopTransformException exception =
        assertThrows(HopTransformException.class, () -> initializeTransform(transform));

    assertEquals(
        BaseMessages.getString(
            IlivalidatorMeta.class, "Ilivalidator.Transform.ConfigFieldNotFound", "config_field"),
        exception.getMessage().trim());
  }

  @Test
  void shouldFailWhenConfigFieldCannotBeRead() throws Exception {
    IlivalidatorMeta meta = createDefaultMeta();
    meta.setConfigMode("FIELD");
    meta.setConfigField("config_field");

    RowMeta inputRowMeta = new RowMeta();
    inputRowMeta.addValueMeta(new ValueMetaString("input_file"));
    inputRowMeta.addValueMeta(new FailingValueMetaString("config_field"));

    Ilivalidator transform =
        createTransform(
            meta,
            inputRowMeta,
            new TestRowHandler(new Object[] {"data/input.xtf", "ignored"}),
            new CapturingService());
    initializeTransform(transform);

    HopTransformException exception =
        assertThrows(HopTransformException.class, transform::processRow);

    assertTrue(
        exception
            .getMessage()
            .contains(
                BaseMessages.getString(
                    IlivalidatorMeta.class,
                    "Ilivalidator.Transform.ConfigFieldReadError",
                    "config_field")));
  }

  private static IlivalidatorMeta createDefaultMeta() {
    IlivalidatorMeta meta = new IlivalidatorMeta();
    meta.setDefault();
    meta.setUseFilePathField(true);
    meta.setFilePathField("input_file");
    return meta;
  }

  private static Ilivalidator createTransform(
      IlivalidatorMeta meta,
      IRowMeta inputRowMeta,
      IRowHandler rowHandler,
      IlivalidatorService service) {
    TransformMeta transformMeta = new TransformMeta("ilivalidator", meta);
    PipelineMeta pipelineMeta = new PipelineMeta();
    pipelineMeta.addTransform(transformMeta);

    Ilivalidator transform =
        new QuietIlivalidator(transformMeta, meta, new IlivalidatorData(), 0, pipelineMeta, null, service);
    transform.setInputRowMeta(inputRowMeta);
    transform.setRowHandler(rowHandler);
    return transform;
  }

  private static void initializeTransform(Ilivalidator transform) throws Exception {
    Method method = Ilivalidator.class.getDeclaredMethod("initializeData");
    method.setAccessible(true);
    try {
      method.invoke(transform);
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      if (cause instanceof Exception exception) {
        throw exception;
      }
      throw e;
    }
  }

  private static Object getOutputValue(TestRowHandler rowHandler, int rowIndex, String fieldName) {
    IRowMeta rowMeta = rowHandler.outputRowMetas.get(rowIndex);
    int fieldIndex = rowMeta.indexOfValue(fieldName);
    return rowHandler.outputRows.get(rowIndex)[fieldIndex];
  }

  private record ValidationCall(Path path, IlivalidatorOptions options) {}

  private static final class CapturingService extends IlivalidatorService {
    private final List<ValidationCall> calls = new ArrayList<>();

    @Override
    public IlivalidatorResult validate(Path path, IlivalidatorOptions options) {
      calls.add(new ValidationCall(path, options));
      return new IlivalidatorResult(true, List.of(), path == null ? null : path.toString(), null);
    }
  }

  private static final class TestRowHandler implements IRowHandler {
    private final Deque<Object[]> inputRows = new ArrayDeque<>();
    private final List<Object[]> outputRows = new ArrayList<>();
    private final List<IRowMeta> outputRowMetas = new ArrayList<>();

    private TestRowHandler(Object[]... rows) {
      for (Object[] row : rows) {
        inputRows.addLast(row);
      }
    }

    @Override
    public Object[] getRow() throws HopException {
      return inputRows.pollFirst();
    }

    @Override
    public void putRow(IRowMeta rowMeta, Object[] row) throws HopTransformException {
      outputRowMetas.add(rowMeta);
      outputRows.add(row.clone());
    }

    @Override
    public void putError(
        IRowMeta rowMeta,
        Object[] row,
        long nrErrors,
        String errorDescriptions,
        String fieldNames,
        String errorCodes)
        throws HopTransformException {
      throw new UnsupportedOperationException();
    }

    @Override
    public void putRowTo(IRowMeta rowMeta, Object[] row, IRowSet rowSet)
        throws HopTransformException {
      throw new UnsupportedOperationException();
    }

    @Override
    public Object[] getRowFrom(IRowSet rowSet) throws HopTransformException {
      throw new UnsupportedOperationException();
    }
  }

  private static final class FailingValueMetaString extends ValueMetaString {
    private FailingValueMetaString(String name) {
      super(name);
    }

    @Override
    public String getString(Object object) throws HopValueException {
      throw new HopValueException("boom");
    }
  }

  private static final class QuietIlivalidator extends Ilivalidator {
    private QuietIlivalidator(
        TransformMeta transformMeta,
        IlivalidatorMeta meta,
        IlivalidatorData data,
        int copyNr,
        PipelineMeta pipelineMeta,
        org.apache.hop.pipeline.Pipeline pipeline,
        IlivalidatorService service) {
      super(transformMeta, meta, data, copyNr, pipelineMeta, pipeline, service);
    }

    @Override
    public boolean isBasic() {
      return false;
    }
  }
}
