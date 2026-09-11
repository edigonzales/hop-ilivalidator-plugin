package ch.so.agi.ilivalidator.hop.action;

import ch.so.agi.ilivalidator.core.validator.IlivalidatorExternalLogLevel;
import ch.so.agi.ilivalidator.core.validator.IlivalidatorIssue;
import ch.so.agi.ilivalidator.core.validator.IlivalidatorOptions;
import ch.so.agi.ilivalidator.core.validator.IlivalidatorResult;
import ch.so.agi.ilivalidator.core.validator.IlivalidatorService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.apache.hop.core.ICheckResult;
import org.apache.hop.core.Result;
import org.apache.hop.core.ResultFile;
import org.apache.hop.core.annotations.Action;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.exception.HopWorkflowException;
import org.apache.hop.core.vfs.HopVfs;
import org.apache.hop.metadata.api.HopMetadataProperty;
import org.apache.hop.metadata.api.IHopMetadataProvider;
import org.apache.hop.workflow.WorkflowMeta;
import org.apache.hop.workflow.action.ActionBase;
import org.apache.hop.workflow.action.IAction;

@Action(
    id = "INTERLIS_ILIVALIDATOR_ACTION",
    name = "i18n::ActionIlivalidator.Name",
    description = "i18n::ActionIlivalidator.Description",
    image = "ch/so/agi/ilivalidator/hop/action/icons/ilivalidator.svg",
    categoryDescription = "i18n:org.apache.hop.workflow:ActionCategory.Category.General",
    documentationUrl = "/workflow/actions/ilivalidator.html",
    keywords = {"i18n::ActionIlivalidator.keyword", "interlis", "validation"})
public class ActionIlivalidator extends ActionBase implements IAction {

  public static final String INPUT_MODE_SINGLE = "SINGLE";
  public static final String INPUT_MODE_FOLDER = "FOLDER";

  private static final Class<?> PKG = ActionIlivalidator.class;

  @HopMetadataProperty private String inputMode = INPUT_MODE_SINGLE;
  @HopMetadataProperty private String filePath;
  @HopMetadataProperty private String folderPath;
  @HopMetadataProperty private boolean recursive;
  @HopMetadataProperty private String includeMask;
  @HopMetadataProperty private String excludeMask;

  @HopMetadataProperty private String modelNames;
  @HopMetadataProperty private String repositoryUrls;
  @HopMetadataProperty private boolean failFast;
  @HopMetadataProperty private boolean stopOnFirstInvalid;
  @HopMetadataProperty private boolean allObjectsAccessible;

  @HopMetadataProperty private boolean writeInvalidAsResultFiles;

  @HopMetadataProperty private String logDirectory;
  @HopMetadataProperty private boolean logFileTimestamp;

  public ActionIlivalidator() {
    this("");
  }

  public ActionIlivalidator(String name) {
    super(name, "");
  }

  @Override
  public Result execute(Result previousResult, int nr) throws HopException {
    Result result = previousResult == null ? new Result() : previousResult;
    List<Path> files = collectInputFiles();

    if (files.isEmpty()) {
      logError("No input files found for INTERLIS validation");
      result.setResult(false);
      result.increaseErrors(1);
      return result;
    }

    IlivalidatorService service = new IlivalidatorService(this::logExternalMessage);
    IlivalidatorOptions options = toOptions();

    boolean overallValid = true;
    for (Path file : files) {
      IlivalidatorResult validationResult = service.validate(file, options);
      overallValid &= validationResult.isValid();

      logValidationResult(file, validationResult);

      if (validationResult.getLogFilePath() != null) {
        registerLogFile(result, validationResult.getLogFilePath());
      }

      if (!validationResult.isValid() && writeInvalidAsResultFiles) {
        registerInvalidResultFile(result, file);
      }

      if (!validationResult.isValid()) {
        result.increaseErrors(1);
        if (stopOnFirstInvalid || failFast) {
          break;
        }
      }
    }

    result.setResult(overallValid);
    if (overallValid) {
      result.setNrErrors(0);
    }
    return result;
  }

  private void logValidationResult(Path file, IlivalidatorResult validationResult) {
    if (validationResult.isValid()) {
      if (isBasic()) {
        logBasic("INTERLIS validation successful: " + file);
      }
      return;
    }

    logError(
        "INTERLIS validation failed for "
            + file
            + " with "
            + validationResult.getIssues().size()
            + " issue(s)");
    IlivalidatorIssue firstErrorIssue = firstErrorIssue(validationResult);
    if (firstErrorIssue != null) {
      logError("Error: " + toErrorLogMessage(firstErrorIssue));
    }

    if (isDetailed()) {
      for (IlivalidatorIssue issue : validationResult.getIssues()) {
        logDetailed(
            "Issue "
                + issue.getCode()
                + " ["
                + issue.getSeverity()
                + "] "
                + issue.getMessage());
      }
    }
  }

  private void registerInvalidResultFile(Result result, Path file) {
    try {
      ResultFile resultFile =
          new ResultFile(
              ResultFile.FILE_TYPE_ERROR,
              HopVfs.getFileObject(file.toAbsolutePath().toString()),
              getName(),
              getParentWorkflowMeta() == null ? "" : getParentWorkflowMeta().getName());
      result.getResultFiles().put(file.toString(), resultFile);
    } catch (Exception e) {
      logError("Unable to attach invalid result file " + file + ": " + e.getMessage());
    }
  }

  private void registerLogFile(Result result, String logFilePath) {
    try {
      ResultFile resultFile =
          new ResultFile(
              ResultFile.FILE_TYPE_LOG,
              HopVfs.getFileObject(logFilePath),
              getName(),
              getParentWorkflowMeta() == null ? "" : getParentWorkflowMeta().getName());
      result.getResultFiles().put(logFilePath, resultFile);
    } catch (Exception e) {
      logError("Unable to attach log file " + logFilePath + ": " + e.getMessage());
    }
  }

  private static IlivalidatorIssue firstErrorIssue(IlivalidatorResult validationResult) {
    for (IlivalidatorIssue issue : validationResult.getIssues()) {
      if (issue.getSeverity() == IlivalidatorIssue.Severity.ERROR) {
        return issue;
      }
    }
    return null;
  }

  private static String toErrorLogMessage(IlivalidatorIssue issue) {
    if (issue.getMessage() != null && !issue.getMessage().isBlank()) {
      return issue.getMessage();
    }
    return issue.getCode() == null || issue.getCode().isBlank() ? "Validation failed" : issue.getCode();
  }

  private void logExternalMessage(
      IlivalidatorExternalLogLevel level, String message, Throwable throwable) {
    if (level == null || message == null || message.isBlank()) {
      return;
    }
    switch (level) {
      case ERROR -> {
        if (throwable == null) {
          logError(message);
        } else {
          logError(message, throwable);
        }
      }
      case WARN, INFO -> {
        if (isBasic()) {
          logBasic(message);
        }
      }
      case DEBUG -> {
        if (isDetailed()) {
          logDetailed(message);
        }
      }
    }
  }

  private IlivalidatorOptions toOptions() {
    return IlivalidatorOptions.builder()
        .modelNames(splitSemicolon(resolve(modelNames)))
        .repositoryUrls(splitSemicolon(resolve(repositoryUrls)))
        .allObjectsAccessible(allObjectsAccessible)
        .logDirectory(resolve(logDirectory))
        .logFileTimestamp(logFileTimestamp)
        .build();
  }

  List<Path> collectInputFiles() throws HopWorkflowException {
    String effectiveMode = Objects.requireNonNullElse(inputMode, INPUT_MODE_SINGLE).toUpperCase(Locale.ROOT);

    if (INPUT_MODE_FOLDER.equals(effectiveMode)) {
      return collectFolderFiles(resolve(folderPath), recursive, resolve(includeMask), resolve(excludeMask));
    }

    String resolvedFilePath = resolve(filePath);
    if (resolvedFilePath == null || resolvedFilePath.isBlank()) {
      return List.of();
    }
    return List.of(Path.of(resolvedFilePath));
  }

  static List<Path> collectFolderFiles(
      String folder,
      boolean recursive,
      String includeMask,
      String excludeMask)
      throws HopWorkflowException {
    if (folder == null || folder.isBlank()) {
      return List.of();
    }

    Path basePath = Path.of(folder);
    if (!Files.exists(basePath) || !Files.isDirectory(basePath)) {
      throw new HopWorkflowException("Configured folder does not exist or is not a directory: " + folder);
    }

    MaskMatcher includeMatcher = MaskMatcher.of(includeMask, true);
    MaskMatcher excludeMatcher = MaskMatcher.of(excludeMask, false);

    try (var stream = recursive ? Files.walk(basePath) : Files.list(basePath)) {
      return stream
          .filter(Files::isRegularFile)
          .filter(path -> includeMatcher.matches(path.getFileName().toString()))
          .filter(path -> !excludeMatcher.matches(path.getFileName().toString()))
          .map(Path::toAbsolutePath)
          .sorted(Comparator.comparing(Path::toString))
          .toList();
    } catch (IOException e) {
      throw new HopWorkflowException("Unable to enumerate folder files: " + folder, e);
    }
  }

  private static List<String> splitSemicolon(String value) {
    if (value == null || value.isBlank()) {
      return List.of();
    }

    String[] split = value.split(";");
    List<String> values = new ArrayList<>(split.length);
    for (String item : split) {
      if (item != null && !item.isBlank()) {
        values.add(item.trim());
      }
    }
    return values;
  }

  static final class MaskMatcher {

    private final boolean defaultMatch;
    private final Pattern pattern;

    private MaskMatcher(boolean defaultMatch, Pattern pattern) {
      this.defaultMatch = defaultMatch;
      this.pattern = pattern;
    }

    static MaskMatcher of(String mask, boolean defaultMatch) throws HopWorkflowException {
      if (mask == null || mask.isBlank()) {
        return new MaskMatcher(defaultMatch, null);
      }

      if (mask.startsWith("glob:")) {
        return new MaskMatcher(defaultMatch, Pattern.compile(globToRegex(mask.substring("glob:".length()))));
      }

      String normalizedMask = mask.startsWith("regex:") ? mask.substring("regex:".length()) : mask;
      try {
        return new MaskMatcher(defaultMatch, Pattern.compile(normalizedMask));
      } catch (PatternSyntaxException e) {
        throw new HopWorkflowException("Invalid include/exclude regex: " + mask, e);
      }
    }

    boolean matches(String value) {
      if (pattern == null) {
        return defaultMatch;
      }
      return pattern.matcher(value).matches();
    }

    private static String globToRegex(String glob) {
      StringBuilder regex = new StringBuilder();
      int end = appendGlobRegex(glob, 0, "", regex);
      if (end != glob.length()) {
        throw new PatternSyntaxException("Unexpected glob token", glob, end);
      }
      return regex.toString();
    }

    private static int appendGlobRegex(
        String glob, int index, String terminators, StringBuilder regex) {
      while (index < glob.length()) {
        char current = glob.charAt(index);
        if (terminators.indexOf(current) >= 0) {
          return index;
        }

        switch (current) {
          case '*':
            int starEnd = index + 1;
            while (starEnd < glob.length() && glob.charAt(starEnd) == '*') {
              starEnd++;
            }
            regex.append(starEnd - index > 1 ? ".*" : "[^/\\\\]*");
            index = starEnd;
            break;
          case '?':
            regex.append("[^/\\\\]");
            index++;
            break;
          case '[':
            index = appendCharacterClass(glob, index, regex);
            break;
          case '{':
            index = appendAlternatives(glob, index, regex);
            break;
          case '\\':
            if (index + 1 >= glob.length()) {
              throw new PatternSyntaxException("Dangling escape", glob, index);
            }
            regex.append(Pattern.quote(String.valueOf(glob.charAt(index + 1))));
            index += 2;
            break;
          default:
            regex.append(Pattern.quote(String.valueOf(current)));
            index++;
            break;
        }
      }
      return index;
    }

    private static int appendCharacterClass(String glob, int start, StringBuilder regex) {
      int index = start + 1;
      if (index >= glob.length()) {
        throw new PatternSyntaxException("Missing closing bracket", glob, start);
      }

      StringBuilder characterClass = new StringBuilder("[");
      if (glob.charAt(index) == '!' || glob.charAt(index) == '^') {
        characterClass.append('^');
        index++;
      }
      if (index < glob.length() && glob.charAt(index) == ']') {
        characterClass.append("\\]");
        index++;
      }

      boolean closed = false;
      while (index < glob.length()) {
        char current = glob.charAt(index);
        if (current == ']') {
          closed = true;
          index++;
          break;
        }
        if (current == '\\') {
          if (index + 1 >= glob.length()) {
            throw new PatternSyntaxException("Dangling escape in character class", glob, index);
          }
          characterClass.append('\\').append(glob.charAt(index + 1));
          index += 2;
        } else {
          characterClass.append(current);
          index++;
        }
      }

      if (!closed) {
        throw new PatternSyntaxException("Missing closing bracket", glob, start);
      }
      characterClass.append(']');
      regex.append(characterClass);
      return index;
    }

    private static int appendAlternatives(String glob, int start, StringBuilder regex) {
      StringBuilder alternatives = new StringBuilder("(?:");
      int branchStart = start + 1;

      while (true) {
        StringBuilder branch = new StringBuilder();
        int delimiter = appendGlobRegex(glob, branchStart, ",}", branch);
        if (delimiter >= glob.length()) {
          throw new PatternSyntaxException("Missing closing brace", glob, start);
        }

        alternatives.append(branch);
        if (glob.charAt(delimiter) == '}') {
          alternatives.append(')');
          regex.append(alternatives);
          return delimiter + 1;
        }

        alternatives.append('|');
        branchStart = delimiter + 1;
      }
    }
  }

  @Override
  public void check(
      List<ICheckResult> remarks,
      WorkflowMeta workflowMeta,
      org.apache.hop.core.variables.IVariables variables,
      IHopMetadataProvider metadataProvider) {
    // Runtime checks are handled in execute.
  }

  public String getInputMode() {
    return inputMode;
  }

  public void setInputMode(String inputMode) {
    this.inputMode = inputMode;
  }

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  public String getFolderPath() {
    return folderPath;
  }

  public void setFolderPath(String folderPath) {
    this.folderPath = folderPath;
  }

  public boolean isRecursive() {
    return recursive;
  }

  public void setRecursive(boolean recursive) {
    this.recursive = recursive;
  }

  public String getIncludeMask() {
    return includeMask;
  }

  public void setIncludeMask(String includeMask) {
    this.includeMask = includeMask;
  }

  public String getExcludeMask() {
    return excludeMask;
  }

  public void setExcludeMask(String excludeMask) {
    this.excludeMask = excludeMask;
  }

  public String getModelNames() {
    return modelNames;
  }

  public void setModelNames(String modelNames) {
    this.modelNames = modelNames;
  }

  public String getRepositoryUrls() {
    return repositoryUrls;
  }

  public void setRepositoryUrls(String repositoryUrls) {
    this.repositoryUrls = repositoryUrls;
  }

  public boolean isFailFast() {
    return failFast;
  }

  public void setFailFast(boolean failFast) {
    this.failFast = failFast;
  }

  public boolean isStopOnFirstInvalid() {
    return stopOnFirstInvalid;
  }

  public void setStopOnFirstInvalid(boolean stopOnFirstInvalid) {
    this.stopOnFirstInvalid = stopOnFirstInvalid;
  }

  public boolean isAllObjectsAccessible() {
    return allObjectsAccessible;
  }

  public void setAllObjectsAccessible(boolean allObjectsAccessible) {
    this.allObjectsAccessible = allObjectsAccessible;
  }

  public boolean isWriteInvalidAsResultFiles() {
    return writeInvalidAsResultFiles;
  }

  public void setWriteInvalidAsResultFiles(boolean writeInvalidAsResultFiles) {
    this.writeInvalidAsResultFiles = writeInvalidAsResultFiles;
  }

  public String getLogDirectory() {
    return logDirectory;
  }

  public void setLogDirectory(String logDirectory) {
    this.logDirectory = logDirectory;
  }

  public boolean isLogFileTimestamp() {
    return logFileTimestamp;
  }

  public void setLogFileTimestamp(boolean logFileTimestamp) {
    this.logFileTimestamp = logFileTimestamp;
  }
}
