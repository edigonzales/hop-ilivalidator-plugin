package ch.so.agi.ilivalidator.core.validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class IlivalidatorOptions {

  private final List<String> modelNames;
  private final List<String> repositoryUrls;
  private final List<IlivalidatorOptionEntry> optionEntries;
  private final boolean runIlivalidator;
  private final boolean allObjectsAccessible;
  private final String configFile;
  private final String metaConfigFile;
  private final String logDirectory;
  private final boolean logFileTimestamp;

  private IlivalidatorOptions(Builder builder) {
    this.modelNames = Collections.unmodifiableList(new ArrayList<>(builder.modelNames));
    this.repositoryUrls = Collections.unmodifiableList(new ArrayList<>(builder.repositoryUrls));
    this.optionEntries = Collections.unmodifiableList(new ArrayList<>(builder.optionEntries));
    this.runIlivalidator = builder.runIlivalidator;
    this.allObjectsAccessible = builder.allObjectsAccessible;
    this.configFile = builder.configFile;
    this.metaConfigFile = builder.metaConfigFile;
    this.logDirectory = builder.logDirectory;
    this.logFileTimestamp = builder.logFileTimestamp;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static IlivalidatorOptions defaults() {
    return builder().build();
  }

  public List<String> getModelNames() {
    return modelNames;
  }

  public List<String> getRepositoryUrls() {
    return repositoryUrls;
  }

  public boolean isRunIlivalidator() {
    return runIlivalidator;
  }

  public boolean isAllObjectsAccessible() {
    return allObjectsAccessible;
  }

  public String getConfigFile() {
    return configFile;
  }

  public String getMetaConfigFile() {
    return metaConfigFile;
  }

  public List<IlivalidatorOptionEntry> getOptionEntries() {
    return optionEntries;
  }

  public String getLogDirectory() {
    return logDirectory;
  }

  public boolean isLogFileTimestamp() {
    return logFileTimestamp;
  }

  public static final class Builder {

    private final List<String> modelNames = new ArrayList<>();
    private final List<String> repositoryUrls = new ArrayList<>();
    private final List<IlivalidatorOptionEntry> optionEntries = new ArrayList<>();
    private boolean runIlivalidator = true;
    private boolean allObjectsAccessible;
    private String configFile;
    private String metaConfigFile;
    private String logDirectory;
    private boolean logFileTimestamp;

    private Builder() {}

    public Builder modelNames(List<String> modelNames) {
      this.modelNames.clear();
      if (modelNames != null) {
        modelNames.stream()
            .filter(value -> value != null && !value.isBlank())
            .map(String::trim)
            .forEach(this.modelNames::add);
      }
      return this;
    }

    public Builder repositoryUrls(List<String> repositoryUrls) {
      this.repositoryUrls.clear();
      if (repositoryUrls != null) {
        repositoryUrls.stream()
            .filter(value -> value != null && !value.isBlank())
            .map(String::trim)
            .forEach(this.repositoryUrls::add);
      }
      return this;
    }

    public Builder runIlivalidator(boolean runIlivalidator) {
      this.runIlivalidator = runIlivalidator;
      return this;
    }

    public Builder allObjectsAccessible(boolean allObjectsAccessible) {
      this.allObjectsAccessible = allObjectsAccessible;
      return this;
    }

    public Builder configFile(String configFile) {
      this.configFile = configFile;
      return this;
    }

    public Builder metaConfigFile(String metaConfigFile) {
      this.metaConfigFile = metaConfigFile;
      return this;
    }

    public Builder optionEntries(List<IlivalidatorOptionEntry> optionEntries) {
      this.optionEntries.clear();
      if (optionEntries != null) {
        optionEntries.stream()
            .filter(entry -> entry != null && entry.getKey() != null && !entry.getKey().isBlank())
            .map(entry -> new IlivalidatorOptionEntry(entry.getKey(), entry.isEnabled(), entry.getValue()))
            .forEach(this.optionEntries::add);
      }
      return this;
    }

    public Builder logDirectory(String logDirectory) {
      this.logDirectory = logDirectory;
      return this;
    }

    public Builder logFileTimestamp(boolean logFileTimestamp) {
      this.logFileTimestamp = logFileTimestamp;
      return this;
    }

    public IlivalidatorOptions build() {
      return new IlivalidatorOptions(this);
    }
  }
}
