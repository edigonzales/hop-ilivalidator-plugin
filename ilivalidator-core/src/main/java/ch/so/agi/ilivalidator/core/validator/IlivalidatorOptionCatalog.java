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
    defs.add(new IlivalidatorOptionDefinition("models", IlivalidatorOptionType.STRING));
    defs.add(new IlivalidatorOptionDefinition("modeldir", IlivalidatorOptionType.STRING));
    defs.add(new IlivalidatorOptionDefinition("refdata", IlivalidatorOptionType.STRING));
    defs.add(new IlivalidatorOptionDefinition("refmapping", IlivalidatorOptionType.STRING));
    defs.add(new IlivalidatorOptionDefinition("config", IlivalidatorOptionType.STRING));
    defs.add(new IlivalidatorOptionDefinition("metaConfig", IlivalidatorOptionType.STRING));
    defs.add(new IlivalidatorOptionDefinition("runtimeParams", IlivalidatorOptionType.STRING));
    defs.add(new IlivalidatorOptionDefinition("scope", IlivalidatorOptionType.STRING));
    defs.add(new IlivalidatorOptionDefinition("mandatoryBaskets", IlivalidatorOptionType.STRING));
    defs.add(new IlivalidatorOptionDefinition("optionalBaskets", IlivalidatorOptionType.STRING));
    defs.add(new IlivalidatorOptionDefinition("bannedBaskets", IlivalidatorOptionType.STRING));
    defs.add(new IlivalidatorOptionDefinition("forceTypeValidation", IlivalidatorOptionType.BOOLEAN));
    defs.add(new IlivalidatorOptionDefinition("disableAreaValidation", IlivalidatorOptionType.BOOLEAN));
    defs.add(new IlivalidatorOptionDefinition("disableConstraintValidation", IlivalidatorOptionType.BOOLEAN));
    defs.add(new IlivalidatorOptionDefinition("multiplicityOff", IlivalidatorOptionType.BOOLEAN));
    defs.add(new IlivalidatorOptionDefinition("allObjectsAccessible", IlivalidatorOptionType.BOOLEAN));
    defs.add(new IlivalidatorOptionDefinition("allowItfAreaHoles", IlivalidatorOptionType.BOOLEAN));
    defs.add(new IlivalidatorOptionDefinition("simpleBoundary", IlivalidatorOptionType.BOOLEAN));
    defs.add(new IlivalidatorOptionDefinition("skipPolygonBuilding", IlivalidatorOptionType.BOOLEAN));
    defs.add(new IlivalidatorOptionDefinition("singlePass", IlivalidatorOptionType.BOOLEAN));
    defs.add(new IlivalidatorOptionDefinition("log", IlivalidatorOptionType.STRING));
    defs.add(new IlivalidatorOptionDefinition("logtime", IlivalidatorOptionType.BOOLEAN));
    defs.add(new IlivalidatorOptionDefinition("xtflog", IlivalidatorOptionType.STRING));
    defs.add(new IlivalidatorOptionDefinition("csvlog", IlivalidatorOptionType.STRING));
    defs.add(new IlivalidatorOptionDefinition("plugins", IlivalidatorOptionType.STRING));
    defs.add(new IlivalidatorOptionDefinition("proxy", IlivalidatorOptionType.STRING));
    defs.add(new IlivalidatorOptionDefinition("proxyPort", IlivalidatorOptionType.INTEGER));
    defs.add(new IlivalidatorOptionDefinition("verbose", IlivalidatorOptionType.BOOLEAN));
    return List.copyOf(defs);
  }
}
