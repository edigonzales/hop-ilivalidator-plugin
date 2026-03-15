package ch.so.agi.ilivalidator.core.validator;

import ch.ehi.basics.settings.Settings;
import java.util.List;
import java.util.Locale;
import org.interlis2.validator.Validator;

public final class IlivalidatorOptionApplier {

  private static final String HTTP_PROXY_HOST = "ch.interlis.ili2c.http_proxy_host";
  private static final String HTTP_PROXY_PORT = "ch.interlis.ili2c.http_proxy_port";
  private static final String SKIP_POLYGON_BUILDING_KEY = "ch.interlis.iox_j.validator.doItfLinetables";
  private static final String SKIP_POLYGON_BUILDING_VALUE = "doItfLinetables";
  private static final String SINGLE_PASS_KEY = "ch.interlis.iox_j.validator.doSinglePass";
  private static final String SINGLE_PASS_VALUE = "doSinglePass";
  private static final String VERBOSE_KEY = "ch.interlis.iox_j.validator.verbose";

  private IlivalidatorOptionApplier() {}

  public static void apply(Settings settings, List<IlivalidatorOptionEntry> optionEntries) {
    if (settings == null || optionEntries == null || optionEntries.isEmpty()) {
      return;
    }

    for (IlivalidatorOptionEntry entry : optionEntries) {
      if (entry == null || !entry.isEnabled() || entry.getKey() == null || entry.getKey().isBlank()) {
        continue;
      }
      IlivalidatorOptionDefinition definition = IlivalidatorOptionCatalog.findByKey(entry.getKey());
      if (definition != null) {
        validateEntryType(definition, entry.getValue());
      }
      applySingle(settings, entry.getKey(), entry.getValue());
    }
  }

  private static void validateEntryType(IlivalidatorOptionDefinition definition, String value) {
    if (definition.getType() == IlivalidatorOptionType.STRING) {
      return;
    }
    if (definition.getType() == IlivalidatorOptionType.INTEGER) {
      if (value == null || value.isBlank()) {
        throw new IllegalArgumentException(
            "Missing integer value for option '" + definition.getKey() + "'");
      }
      try {
        Integer.parseInt(value.trim());
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException(
            "Invalid integer value for option '" + definition.getKey() + "': " + value, e);
      }
      return;
    }
    if (value == null || value.isBlank()) {
      return;
    }
    if (!isBooleanToken(value)) {
      throw new IllegalArgumentException(
          "Invalid boolean value for option '" + definition.getKey() + "': " + value);
    }
  }

  private static boolean isBooleanToken(String value) {
    String normalized = value.trim().toLowerCase(Locale.ROOT);
    return "true".equals(normalized)
        || "1".equals(normalized)
        || "y".equals(normalized)
        || "yes".equals(normalized)
        || "on".equals(normalized)
        || "false".equals(normalized)
        || "0".equals(normalized)
        || "n".equals(normalized)
        || "no".equals(normalized)
        || "off".equals(normalized);
  }

  private static void applySingle(Settings settings, String rawKey, String value) {
    String key = normalize(rawKey);
    switch (key) {
      case "models" -> settings.setValue(Validator.SETTING_MODELNAMES, nullToEmpty(value));
      case "modeldir" -> settings.setValue(Validator.SETTING_ILIDIRS, nullToEmpty(value));
      case "refdata" -> settings.setValue(Validator.SETTING_REF_DATA, nullToEmpty(value));
      case "refmapping" -> settings.setValue(Validator.SETTING_REF_MAPPING_DATA, nullToEmpty(value));
      case "config" -> settings.setValue(Validator.SETTING_CONFIGFILE, nullToEmpty(value));
      case "metaconfig" -> settings.setValue(Validator.SETTING_META_CONFIGFILE, nullToEmpty(value));
      case "runtimeparams" -> settings.setValue(Validator.SETTING_RUNTIME_PARAMETERS, nullToEmpty(value));
      case "scope" -> settings.setValue(Validator.SETTING_VALIDATION_SCOPE, nullToEmpty(value));
      case "mandatorybaskets" -> settings.setValue(Validator.SETTING_MANDATORY_BASKETS, nullToEmpty(value));
      case "optionalbaskets" -> settings.setValue(Validator.SETTING_OPTIONAL_BASKETS, nullToEmpty(value));
      case "bannedbaskets" -> settings.setValue(Validator.SETTING_BANNED_BASKETS, nullToEmpty(value));
      case "forcetypevalidation" ->
          settings.setValue(Validator.SETTING_FORCE_TYPE_VALIDATION, booleanString(value, true));
      case "disableareavalidation" ->
          settings.setValue(Validator.SETTING_DISABLE_AREA_VALIDATION, booleanString(value, true));
      case "disableconstraintvalidation" ->
          settings.setValue(Validator.SETTING_DISABLE_CONSTRAINT_VALIDATION, booleanString(value, true));
      case "multiplicityoff" ->
          settings.setValue(
              Validator.SETTING_MULTIPLICITY_VALIDATION,
              parseBoolean(value, true) ? "off" : "on");
      case "allobjectsaccessible" ->
          settings.setValue(Validator.SETTING_ALL_OBJECTS_ACCESSIBLE, booleanString(value, true));
      case "allowitfareaholes" ->
          settings.setValue(Validator.SETTING_ALLOW_ITF_AREA_HOLES, booleanString(value, true));
      case "simpleboundary" ->
          settings.setValue(Validator.SETTING_SIMPLE_BOUNDARY, booleanString(value, true));
      case "skippolygonbuilding" ->
          settings.setValue(
              SKIP_POLYGON_BUILDING_KEY,
              parseBoolean(value, true) ? SKIP_POLYGON_BUILDING_VALUE : "off");
      case "singlepass" ->
          settings.setValue(
              SINGLE_PASS_KEY,
              parseBoolean(value, true) ? SINGLE_PASS_VALUE : "off");
      case "log" -> settings.setValue(Validator.SETTING_LOGFILE, nullToEmpty(value));
      case "logtime" ->
          settings.setValue(Validator.SETTING_LOGFILE_TIMESTAMP, booleanString(value, true));
      case "xtflog" -> settings.setValue(Validator.SETTING_XTFLOG, nullToEmpty(value));
      case "csvlog" -> settings.setValue(Validator.SETTING_CSVLOG, nullToEmpty(value));
      case "plugins" -> settings.setValue(Validator.SETTING_PLUGINFOLDER, nullToEmpty(value));
      case "proxy" -> settings.setValue(HTTP_PROXY_HOST, nullToEmpty(value));
      case "proxyport" -> settings.setValue(HTTP_PROXY_PORT, Integer.toString(parseInteger(value)));
      case "verbose" -> settings.setTransientValue(VERBOSE_KEY, booleanString(value, true));
      default -> {
        // Ignore unknown catalog entries for forward compatibility.
      }
    }
  }

  private static String normalize(String key) {
    return key == null ? "" : key.trim().toLowerCase(Locale.ROOT);
  }

  private static String booleanString(String value, boolean defaultValue) {
    return parseBoolean(value, defaultValue) ? Validator.TRUE : Validator.FALSE;
  }

  private static boolean parseBoolean(String value, boolean defaultValue) {
    if (value == null || value.isBlank()) {
      return defaultValue;
    }
    String normalized = value.trim().toLowerCase(Locale.ROOT);
    return "true".equals(normalized)
        || "1".equals(normalized)
        || "y".equals(normalized)
        || "yes".equals(normalized)
        || "on".equals(normalized);
  }

  private static int parseInteger(String value) {
    return Integer.parseInt(value.trim());
  }

  private static String nullToEmpty(String value) {
    return value == null ? "" : value;
  }
}
