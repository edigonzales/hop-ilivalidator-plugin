package ch.so.agi.ilivalidator.core.validator;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class IlivalidatorResult {

  private final boolean valid;
  private final List<IlivalidatorIssue> issues;
  private final String checkedFile;

  public IlivalidatorResult(boolean valid, List<IlivalidatorIssue> issues, String checkedFile) {
    this.valid = valid;
    this.issues = Collections.unmodifiableList(Objects.requireNonNullElse(issues, List.of()));
    this.checkedFile = checkedFile;
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
}
