package ch.so.agi.ilivalidator.core.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class IlivalidatorOptionCatalog {

  private static final List<IlivalidatorOptionDefinition> DEFINITIONS = buildDefinitions();
  private static final Map<String, IlivalidatorOptionDefinition> BY_KEY = byKey(DEFINITIONS);

  private IlivalidatorOptionCatalog() {}

  public static List<IlivalidatorOptionDefinition> allDefinitions() {
    return DEFINITIONS;
  }

  public static IlivalidatorOptionDefinition findByKey(String key) {
    if (key == null) {
      return null;
    }
    return BY_KEY.get(normalizeKey(key));
  }

  private static Map<String, IlivalidatorOptionDefinition> byKey(
      List<IlivalidatorOptionDefinition> definitions) {
    Map<String, IlivalidatorOptionDefinition> map = new ConcurrentHashMap<>();
    for (IlivalidatorOptionDefinition definition : definitions) {
      map.put(normalizeKey(definition.getKey()), definition);
    }
    return map;
  }

  private static String normalizeKey(String key) {
    return key.trim().toLowerCase(Locale.ROOT);
  }

  private static List<IlivalidatorOptionDefinition> buildDefinitions() {
    List<IlivalidatorOptionDefinition> defs = new ArrayList<>();
    defs.add(IlivalidatorOptionDefinition.of("models", "Model names", IlivalidatorOptionType.STRING));
    defs.add(IlivalidatorOptionDefinition.of("modeldir", "Model directory", IlivalidatorOptionType.STRING));
    defs.add(IlivalidatorOptionDefinition.of("refdata", "Reference data file", IlivalidatorOptionType.STRING));
    defs.add(
        IlivalidatorOptionDefinition.of(
            "refmapping", "Reference mapping file", IlivalidatorOptionType.STRING));
    defs.add(IlivalidatorOptionDefinition.of("config", "Validation config file", IlivalidatorOptionType.STRING));
    defs.add(IlivalidatorOptionDefinition.of("metaConfig", "Meta-config file", IlivalidatorOptionType.STRING));
    defs.add(
        IlivalidatorOptionDefinition.of(
            "runtimeParams", "Runtime parameters", IlivalidatorOptionType.STRING));
    defs.add(IlivalidatorOptionDefinition.of("scope", "Validation scope", IlivalidatorOptionType.STRING));
    defs.add(
        IlivalidatorOptionDefinition.of(
            "mandatoryBaskets", "Mandatory baskets", IlivalidatorOptionType.STRING));
    defs.add(
        IlivalidatorOptionDefinition.of(
            "optionalBaskets", "Optional baskets", IlivalidatorOptionType.STRING));
    defs.add(
        IlivalidatorOptionDefinition.of(
            "bannedBaskets", "Banned baskets", IlivalidatorOptionType.STRING));
    defs.add(
        IlivalidatorOptionDefinition.of(
            "forceTypeValidation", "Force type validation", IlivalidatorOptionType.BOOLEAN));
    defs.add(
        IlivalidatorOptionDefinition.of(
            "disableAreaValidation", "Disable AREA validation", IlivalidatorOptionType.BOOLEAN));
    defs.add(
        IlivalidatorOptionDefinition.of(
            "disableConstraintValidation",
            "Disable constraint validation",
            IlivalidatorOptionType.BOOLEAN));
    defs.add(
        IlivalidatorOptionDefinition.of(
            "multiplicityOff", "Disable multiplicity validation", IlivalidatorOptionType.BOOLEAN));
    defs.add(
        IlivalidatorOptionDefinition.of(
            "allObjectsAccessible", "All objects accessible", IlivalidatorOptionType.BOOLEAN));
    defs.add(
        IlivalidatorOptionDefinition.of(
            "allowItfAreaHoles", "Allow ITF area holes", IlivalidatorOptionType.BOOLEAN));
    defs.add(
        IlivalidatorOptionDefinition.of(
            "simpleBoundary", "Simple boundary", IlivalidatorOptionType.BOOLEAN));
    defs.add(
        IlivalidatorOptionDefinition.of(
            "skipPolygonBuilding", "Skip polygon building", IlivalidatorOptionType.BOOLEAN));
    defs.add(
        IlivalidatorOptionDefinition.of(
            "singlePass", "Single pass validation", IlivalidatorOptionType.BOOLEAN));
    defs.add(IlivalidatorOptionDefinition.of("log", "Log file", IlivalidatorOptionType.STRING));
    defs.add(
        IlivalidatorOptionDefinition.of(
            "logtime", "Log timestamps", IlivalidatorOptionType.BOOLEAN));
    defs.add(IlivalidatorOptionDefinition.of("xtflog", "XTF log file", IlivalidatorOptionType.STRING));
    defs.add(IlivalidatorOptionDefinition.of("csvlog", "CSV log file", IlivalidatorOptionType.STRING));
    defs.add(IlivalidatorOptionDefinition.of("plugins", "Plugin folder", IlivalidatorOptionType.STRING));
    defs.add(IlivalidatorOptionDefinition.of("proxy", "Proxy host", IlivalidatorOptionType.STRING));
    defs.add(IlivalidatorOptionDefinition.of("proxyPort", "Proxy port", IlivalidatorOptionType.INTEGER));
    defs.add(IlivalidatorOptionDefinition.of("verbose", "Verbose logging", IlivalidatorOptionType.BOOLEAN));
    return List.copyOf(defs);
  }
}
