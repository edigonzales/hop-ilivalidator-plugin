package ch.so.agi.ilivalidator.core.validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class IlivalidatorOptions {

  private final List<String> modelNames;
  private final List<String> repositoryUrls;
  private final boolean runIlivalidator;
  private final boolean allObjectsAccessible;

  private IlivalidatorOptions(Builder builder) {
    this.modelNames = Collections.unmodifiableList(new ArrayList<>(builder.modelNames));
    this.repositoryUrls = Collections.unmodifiableList(new ArrayList<>(builder.repositoryUrls));
    this.runIlivalidator = builder.runIlivalidator;
    this.allObjectsAccessible = builder.allObjectsAccessible;
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

  public static final class Builder {

    private final List<String> modelNames = new ArrayList<>();
    private final List<String> repositoryUrls = new ArrayList<>();
    private boolean runIlivalidator = true;
    private boolean allObjectsAccessible;

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

    public IlivalidatorOptions build() {
      return new IlivalidatorOptions(this);
    }
  }
}
