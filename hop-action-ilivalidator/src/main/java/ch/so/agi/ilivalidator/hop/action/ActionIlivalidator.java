package ch.so.agi.ilivalidator.hop.action;

import ch.so.agi.ilivalidator.core.validator.IlivalidatorIssue;
import ch.so.agi.ilivalidator.core.validator.IlivalidatorOptions;
import ch.so.agi.ilivalidator.core.validator.IlivalidatorResult;
import ch.so.agi.ilivalidator.core.validator.IlivalidatorService;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
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
    image = "ch/so/agi/ilivalidator/hop/action/icons/xml-validator.svg",
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

    IlivalidatorService service = new IlivalidatorService();
    IlivalidatorOptions options = toOptions();

    boolean overallValid = true;
    for (Path file : files) {
      IlivalidatorResult validationResult = service.validate(file, options);
      overallValid &= validationResult.isValid();

      logValidationResult(file, validationResult);

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

  private IlivalidatorOptions toOptions() {
    return IlivalidatorOptions.builder()
        .modelNames(splitSemicolon(resolve(modelNames)))
        .repositoryUrls(splitSemicolon(resolve(repositoryUrls)))
        .allObjectsAccessible(allObjectsAccessible)
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

  private static final class MaskMatcher {

    private final boolean defaultMatch;
    private final Pattern regex;
    private final PathMatcher glob;

    private MaskMatcher(boolean defaultMatch, Pattern regex, PathMatcher glob) {
      this.defaultMatch = defaultMatch;
      this.regex = regex;
      this.glob = glob;
    }

    static MaskMatcher of(String mask, boolean defaultMatch) throws HopWorkflowException {
      if (mask == null || mask.isBlank()) {
        return new MaskMatcher(defaultMatch, null, null);
      }

      if (mask.startsWith("glob:")) {
        return new MaskMatcher(defaultMatch, null, FileSystems.getDefault().getPathMatcher(mask));
      }

      String normalizedMask = mask.startsWith("regex:") ? mask.substring("regex:".length()) : mask;
      try {
        return new MaskMatcher(defaultMatch, Pattern.compile(normalizedMask), null);
      } catch (PatternSyntaxException e) {
        throw new HopWorkflowException("Invalid include/exclude regex: " + mask, e);
      }
    }

    boolean matches(String value) {
      if (regex == null && glob == null) {
        return defaultMatch;
      }
      if (glob != null) {
        return glob.matches(Path.of(value));
      }
      return regex.matcher(value).matches();
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
}
