package ch.so.agi.ilivalidator.hop.transform;

import ch.so.agi.hop.commons.ui.BrowseStrategy;
import ch.so.agi.hop.commons.ui.EditorKind;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Optional;
import org.apache.hop.core.variables.IVariables;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

/** Native local-file selection matching the validator's Path-based input. */
final class LocalTransferFileBrowser implements BrowseStrategy {
  @Override
  public Optional<String> browse(
      Shell shell,
      IVariables variables,
      String currentValue,
      EditorKind editor,
      String[] extensions,
      String[] names) {
    FileDialog picker = new FileDialog(shell, SWT.OPEN);
    picker.setFilterExtensions(extensions);
    picker.setFilterNames(names);
    String initial = variables.resolve(currentValue);
    if (initial != null && !initial.isEmpty()) {
      try {
        Path path = Path.of(initial);
        if (path.getParent() != null) picker.setFilterPath(path.getParent().toString());
        if (path.getFileName() != null) picker.setFileName(path.getFileName().toString());
      } catch (InvalidPathException ignored) {
        // An invalid preselection must not prevent the user from choosing another file.
      }
    }
    return Optional.ofNullable(picker.open());
  }
}
