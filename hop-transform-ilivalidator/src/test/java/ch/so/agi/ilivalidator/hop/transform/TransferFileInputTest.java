package ch.so.agi.ilivalidator.hop.transform;

import static org.junit.jupiter.api.Assertions.*;

import ch.so.agi.hop.commons.core.*;
import ch.so.agi.hop.commons.ui.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.*;
import org.apache.hop.core.HopClientEnvironment;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.value.ValueMetaString;
import org.apache.hop.core.variables.*;
import org.apache.hop.core.xml.XmlHandler;
import org.apache.hop.metadata.api.IHopMetadataProvider;
import org.apache.hop.pipeline.*;
import org.apache.hop.pipeline.transform.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.*;
import org.junit.jupiter.api.*;

class TransferFileInputTest {
  static Display display;
  Shell shell;

  @BeforeAll
  static void init() throws Exception {
    HopClientEnvironment.init();
    org.apache.hop.ui.hopgui.HopGuiEnvironment.init();
    org.apache.hop.core.plugins.TransformPluginType.getInstance()
        .registerClassPathPlugin(IlivalidatorMeta.class);
    display = new Display();
  }

  @AfterAll
  static void end() {
    display.dispose();
  }

  @BeforeEach
  void open() {
    shell = new Shell(display);
    shell.setLayout(new FormLayout());
    shell.setSize(850, 300);
  }

  @AfterEach
  void close() {
    shell.dispose();
  }

  @Test
  void freshMetaStartsInConfiguredMode() {
    IlivalidatorMeta meta = new IlivalidatorMeta();
    meta.setDefault();
    assertFalse(meta.isUseFilePathField());

    TransferFileInput input =
        input(() -> new String[] {"file_path"}, Optional.empty(), new AtomicInteger());
    input.load(meta);

    assertEquals(SourceMode.CONFIGURED, widget().getValue().mode());
  }

  @Test
  void mapsExistingMetadataWithoutChangesAndPreservesBothTexts() throws Exception {
    IlivalidatorMeta meta = meta();
    AtomicInteger changes = new AtomicInteger();
    TransferFileInput input = input(() -> new String[] {"file_path"}, Optional.empty(), changes);
    input.load(meta);
    ValueOrFieldControl widget = widget();
    assertEquals(0, changes.get());
    assertEquals(" ${ROOT}/one.xtf ", widget.getValue().configuredValue());
    mode(1);
    field().setText("new_path");
    assertEquals("old_path", meta.getFilePathField());
    input.applyTo(meta);
    assertTrue(meta.isUseFilePathField());
    assertEquals("new_path", meta.getFilePathField());
    assertEquals(" ${ROOT}/one.xtf ", meta.getStaticFilePath());
    IlivalidatorMeta copy = new IlivalidatorMeta();
    copy.loadXml(
        XmlHandler.loadXmlString("<transform>" + meta.getXml() + "</transform>")
            .getDocumentElement(),
        null);
    input.load(copy);
    assertEquals(
        widget.getValue(),
        new ValueOrField(SourceMode.FIELD, meta.getStaticFilePath(), "new_path"));
    mode(0);
    assertEquals(" ${ROOT}/one.xtf ", widget.getValue().configuredValue());
    // Discarding/reloading the editor does not commit the pending mode change.
    input.load(copy);
    assertEquals(SourceMode.FIELD, widget.getValue().mode());
  }

  @Test
  void suggestionsComeFromUpstreamTransformMetadata() throws Exception {
    PipelineMeta pipeline = new PipelineMeta();
    TransformMeta source = new TransformMeta("source", new FieldsMeta());
    TransformMeta target = new TransformMeta("validator", meta());
    pipeline.addTransform(source);
    pipeline.addTransform(target);
    pipeline.addPipelineHop(new PipelineHopMeta(source, target));
    Variables vars = new Variables();
    TransferFileInput input =
        input(
            () -> pipeline.getPrevTransformFields(vars, target).getFieldNames(),
            Optional.empty(),
            new AtomicInteger());
    input.load(meta());
    mode(1);
    assertArrayEquals(new String[] {"file_path", "other"}, field().getItems());
    assertEquals("old_path", field().getText());
  }

  @Test
  void errorsWrapWithoutMovingTheInputAndRefreshIsSilent() {
    AtomicReference<String> error = new AtomicReference<>();
    AtomicReference<String[]> fields = new AtomicReference<>(new String[] {"file_path"});
    AtomicInteger changes = new AtomicInteger();
    TransferFileInput input =
        input(
            () -> {
              if (error.get() != null) throw new HopException(error.get());
              return fields.get();
            },
            Optional.empty(),
            changes);
    input.load(meta());
    mode(1);
    int before = changes.get();
    for (int width : new int[] {520, 1050}) {
      shell.setSize(width, 400);
      shell.layout(true, true);
      var bounds = widget().getBounds();
      Label caption =
          Arrays.stream(shell.getChildren())
              .filter(Label.class::isInstance)
              .map(Label.class::cast)
              .findFirst()
              .orElseThrow();
      var labelBounds = caption.getBounds();
      assertTrue(
          Math.abs(labelBounds.y + labelBounds.height / 2.0 - bounds.y - bounds.height / 2.0) <= 1);
      for (String message :
          new String[] {"Unavailable", "Cannot determine upstream field names. ".repeat(18)}) {
        error.set(message);
        widget().refreshFields();
        shell.layout(true, true);
        assertEquals(bounds, widget().getBounds());
        assertEquals(labelBounds, caption.getBounds());
        Label status =
            all(shell, Label.class).stream()
                .filter(l -> l != caption && !l.getText().isEmpty())
                .findFirst()
                .orElseThrow();
        assertTrue(status.getVisible());
        var point = status.toDisplay(0, 0);
        assertTrue(point.y >= widget().toDisplay(0, bounds.height).y);
        if (message.length() > 100) assertTrue(status.getSize().y > caption.getSize().y);
        assertArrayEquals(new String[] {"file_path"}, field().getItems());
        assertEquals("old_path", field().getText());
      }
      error.set(null);
      widget().refreshFields();
      assertEquals(bounds, widget().getBounds());
    }
    fields.set(new String[0]);
    widget().refreshFields();
    assertEquals(0, field().getItemCount());
    assertEquals("old_path", field().getText());
    assertEquals(before, changes.get());
  }

  @Test
  void injectedLocalSelectionAndCancellationOnlyChangeOnSelection() {
    AtomicInteger changes = new AtomicInteger();
    AtomicReference<Optional<String>> selection = new AtomicReference<>(Optional.empty());
    TransferFileInput input =
        new TransferFileInput(
            shell,
            new Variables(),
            25,
            () -> null,
            (s, v, c, k, e, n) -> selection.get(),
            changes::incrementAndGet);
    input.load(meta());
    Button browse = all(widget(), Button.class).getFirst();
    browse.notifyListeners(SWT.Selection, new Event());
    assertEquals(0, changes.get());
    selection.set(Optional.of("/tmp/selected.xtf"));
    browse.notifyListeners(SWT.Selection, new Event());
    assertEquals(1, changes.get());
    assertEquals("/tmp/selected.xtf", widget().getValue().configuredValue());
    assertEquals("old_path", widget().getValue().fieldName());
  }

  @Test
  void actualDialogCommitsOnlyOnOkAndRestoresChangedOnAllCancellationPaths() {
    IlivalidatorMeta meta = meta();
    for (int cancelKind = 0; cancelKind < 3; cancelKind++) {
      final int kind = cancelKind;
      meta.setChanged(false);
      visitDialog(
          meta,
          dialogShell -> {
            ValueOrFieldControl control = all(dialogShell, ValueOrFieldControl.class).getFirst();
            assertFalse(meta.hasChanged());
            Combo source = all(control, Combo.class).getFirst();
            source.select(1);
            source.notifyListeners(SWT.Selection, new Event());
            all(control, Combo.class).get(1).setText("edited_path");
            assertTrue(meta.hasChanged());
            if (kind == 0) dialogShell.close();
            else if (kind == 1) {
              Event escape = new Event();
              escape.detail = SWT.TRAVERSE_ESCAPE;
              dialogShell.notifyListeners(SWT.Traverse, escape);
            } else
              all(dialogShell, Button.class).stream()
                  .filter(
                      b ->
                          b.getText()
                              .equals(
                                  org.apache.hop.i18n.BaseMessages.getString(
                                      IlivalidatorMeta.class, "System.Button.Cancel")))
                  .findFirst()
                  .orElseThrow()
                  .notifyListeners(SWT.Selection, new Event());
          });
      assertFalse(meta.hasChanged());
      assertFalse(meta.isUseFilePathField());
      assertEquals("old_path", meta.getFilePathField());
    }
    visitDialog(
        meta,
        dialogShell -> {
          ValueOrFieldControl control = all(dialogShell, ValueOrFieldControl.class).getFirst();
          Combo source = all(control, Combo.class).getFirst();
          source.select(1);
          source.notifyListeners(SWT.Selection, new Event());
          all(control, Combo.class).get(1).setText("edited_path");
          all(dialogShell, Button.class).stream()
              .filter(
                  b ->
                      b.getText()
                          .equals(
                              org.apache.hop.i18n.BaseMessages.getString(
                                  IlivalidatorMeta.class, "System.Button.OK")))
              .findFirst()
              .orElseThrow()
              .notifyListeners(SWT.Selection, new Event());
        });
    assertTrue(meta.isUseFilePathField());
    assertEquals("edited_path", meta.getFilePathField());
    assertEquals(" ${ROOT}/one.xtf ", meta.getStaticFilePath());
    visitDialog(
        meta,
        dialogShell -> {
          ValueOrField value = all(dialogShell, ValueOrFieldControl.class).getFirst().getValue();
          assertEquals(
              new ValueOrField(SourceMode.FIELD, meta.getStaticFilePath(), "edited_path"), value);
          dialogShell.close();
        });
  }

  @Test
  void upstreamMetadataFailureDoesNotPreventTheActualDialogFromOpening() {
    IlivalidatorMeta meta = meta();
    meta.setUseFilePathField(true);
    PipelineMeta pipeline = new PipelineMeta();
    TransformMeta upstream = new TransformMeta("Broken source", new BrokenFieldsMeta());
    TransformMeta target = new TransformMeta("Validator", meta);
    pipeline.addTransform(upstream);
    pipeline.addTransform(target);
    pipeline.addPipelineHop(new PipelineHopMeta(upstream, target));
    visitDialog(
        meta,
        pipeline,
        dialogShell -> {
          assertEquals(
              "old_path",
              all(dialogShell, ValueOrFieldControl.class).getFirst().getValue().fieldName());
          assertTrue(
              all(dialogShell, Label.class).stream()
                  .anyMatch(l -> l.getText().contains("Unavailable test source")));
          dialogShell.close();
        });
  }

  static final class BrokenFieldsMeta extends BaseTransformMeta<Ilivalidator, IlivalidatorData> {
    @Override
    public void getFields(
        IRowMeta row,
        String name,
        IRowMeta[] info,
        TransformMeta next,
        IVariables vars,
        IHopMetadataProvider provider) {
      throw new IllegalStateException("Unavailable test source");
    }
  }

  void visitDialog(IlivalidatorMeta meta, java.util.function.Consumer<Shell> action) {
    PipelineMeta pipeline = new PipelineMeta();
    pipeline.addTransform(new TransformMeta("Validator", meta));
    visitDialog(meta, pipeline, action);
  }

  void visitDialog(
      IlivalidatorMeta meta, PipelineMeta pipeline, java.util.function.Consumer<Shell> action) {
    AtomicReference<Throwable> failure = new AtomicReference<>();
    long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(10);
    display.timerExec(
        50,
        new Runnable() {
          public void run() {
            Shell dialogShell =
                Arrays.stream(display.getShells())
                    .filter(
                        s ->
                            s != shell
                                && s.isVisible()
                                && !all(s, ValueOrFieldControl.class).isEmpty())
                    .findFirst()
                    .orElse(null);
            if (dialogShell == null && System.nanoTime() < deadline) {
              display.timerExec(50, this);
              return;
            }
            try {
              assertNotNull(dialogShell, "Dialog did not become ready");
              action.accept(dialogShell);
            } catch (Throwable t) {
              failure.set(t);
            } finally {
              for (Shell s : display.getShells()) if (s != shell && !s.isDisposed()) s.close();
            }
          }
        });
    new IlivalidatorDialog(shell, new Variables(), meta, pipeline).open();
    if (failure.get() != null) throw new AssertionError(failure.get());
  }

  static final class FieldsMeta extends BaseTransformMeta<Ilivalidator, IlivalidatorData> {
    @Override
    public void getFields(
        IRowMeta row,
        String name,
        IRowMeta[] info,
        TransformMeta next,
        IVariables vars,
        IHopMetadataProvider provider) {
      row.addValueMeta(new ValueMetaString("file_path"));
      row.addValueMeta(new ValueMetaString("other"));
    }
  }

  static IlivalidatorMeta meta() {
    IlivalidatorMeta meta = new IlivalidatorMeta();
    meta.setDefault();
    meta.setUseFilePathField(false);
    meta.setStaticFilePath(" ${ROOT}/one.xtf ");
    meta.setFilePathField("old_path");
    return meta;
  }

  TransferFileInput input(
      InputFieldProvider provider, Optional<String> selection, AtomicInteger changes) {
    return new TransferFileInput(
        shell,
        new Variables(),
        25,
        provider,
        (s, v, c, k, e, n) -> selection,
        changes::incrementAndGet);
  }

  ValueOrFieldControl widget() {
    return all(shell, ValueOrFieldControl.class).getFirst();
  }

  Combo field() {
    return all(widget(), Combo.class).get(1);
  }

  void mode(int index) {
    Combo combo = all(widget(), Combo.class).getFirst();
    combo.select(index);
    combo.notifyListeners(SWT.Selection, new Event());
  }

  static <T> List<T> all(Composite parent, Class<T> type) {
    List<T> result = new ArrayList<>();
    for (Control c : parent.getChildren()) {
      if (type.isInstance(c)) result.add(type.cast(c));
      if (c instanceof Composite p) result.addAll(all(p, type));
    }
    return result;
  }
}
