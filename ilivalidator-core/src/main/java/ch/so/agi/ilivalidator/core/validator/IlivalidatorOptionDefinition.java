package ch.so.agi.ilivalidator.core.validator;

public class IlivalidatorOptionDefinition {
  private final String key;
  private final IlivalidatorOptionType type;

  public IlivalidatorOptionDefinition(String key, IlivalidatorOptionType type) {
    this.key = key;
    this.type = type;
  }

  public String getKey() {
    return key;
  }

  public IlivalidatorOptionType getType() {
    return type;
  }
}
