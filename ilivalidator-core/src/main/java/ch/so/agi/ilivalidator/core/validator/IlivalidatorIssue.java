package ch.so.agi.ilivalidator.core.validator;

import java.util.Objects;

public final class IlivalidatorIssue {

  public enum Severity {
    ERROR,
    WARNING,
    INFO
  }

  private final String code;
  private final String message;
  private final Severity severity;
  private final Integer line;
  private final Integer column;

  public IlivalidatorIssue(
      String code, String message, Severity severity, Integer line, Integer column) {
    this.code = Objects.requireNonNullElse(code, "UNKNOWN");
    this.message = Objects.requireNonNullElse(message, "");
    this.severity = Objects.requireNonNullElse(severity, Severity.ERROR);
    this.line = line;
    this.column = column;
  }

  public String getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }

  public Severity getSeverity() {
    return severity;
  }

  public Integer getLine() {
    return line;
  }

  public Integer getColumn() {
    return column;
  }
}
