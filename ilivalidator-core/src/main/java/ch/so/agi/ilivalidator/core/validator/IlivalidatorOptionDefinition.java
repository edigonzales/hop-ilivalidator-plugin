package ch.so.agi.ilivalidator.core.validator;

public class IlivalidatorOptionDefinition {
  private final String key;
  private final String label;
  private final IlivalidatorOptionType type;

  public IlivalidatorOptionDefinition(String key, String label, IlivalidatorOptionType type) {
    this.key = key;
    this.label = label;
    this.type = type;
  }

  public static IlivalidatorOptionDefinition of(
      String key, String label, IlivalidatorOptionType type) {
    return new IlivalidatorOptionDefinition(key, label, type);
  }

  public String getKey() {
    return key;
  }

  public String getLabel() {
    return label;
  }

  public IlivalidatorOptionType getType() {
    return type;
  }

  public boolean appliesToCurrentContext() {
    return true;
  }
}
