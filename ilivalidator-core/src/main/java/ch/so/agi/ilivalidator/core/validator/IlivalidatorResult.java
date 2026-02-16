package ch.so.agi.ilivalidator.core.validator;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class IlivalidatorResult {

  private final boolean valid;
  private final List<IlivalidatorIssue> issues;
  private final String checkedFile;
  private final String logFilePath;

  public IlivalidatorResult(boolean valid, List<IlivalidatorIssue> issues, String checkedFile, String logFilePath) {
    this.valid = valid;
    this.issues = Collections.unmodifiableList(Objects.requireNonNullElse(issues, List.of()));
    this.checkedFile = checkedFile;
    this.logFilePath = logFilePath;
  }

  public boolean isValid() {
    return valid;
  }

  public List<IlivalidatorIssue> getIssues() {
    return issues;
  }

  public String getCheckedFile() {
    return checkedFile;
  }

  public String getLogFilePath() {
    return logFilePath;
  }
}
