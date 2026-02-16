package ch.so.agi.ilivalidator.core.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class IlivalidatorResultBuilder {

  private final List<IlivalidatorIssue> issues = new ArrayList<>();
  private boolean valid = true;
  private final String checkedFile;
  private String logFilePath;

  public IlivalidatorResultBuilder(String checkedFile) {
    this.checkedFile = checkedFile;
  }

  public void addIssue(IlivalidatorIssue issue) {
    IlivalidatorIssue safeIssue = Objects.requireNonNull(issue, "issue");
    issues.add(safeIssue);
    if (safeIssue.getSeverity() == IlivalidatorIssue.Severity.ERROR) {
      valid = false;
    }
  }

  public void addIssue(
      String code,
      String message,
      IlivalidatorIssue.Severity severity,
      Integer line,
      Integer column) {
    addIssue(new IlivalidatorIssue(code, message, severity, line, column));
  }

  public void invalidate() {
    valid = false;
  }

  public void setLogFilePath(String logFilePath) {
    this.logFilePath = logFilePath;
  }

  public IlivalidatorResult build() {
    boolean hasErrors =
        issues.stream().anyMatch(issue -> issue.getSeverity() == IlivalidatorIssue.Severity.ERROR);
    boolean finalValid = valid && !hasErrors;
    return new IlivalidatorResult(finalValid, List.copyOf(issues), checkedFile, logFilePath);
  }
}
