package ch.so.agi.ilivalidator.core.validator;

import java.util.ArrayList;
import java.util.List;

public final class IlivalidatorOptionCodec {
  private IlivalidatorOptionCodec() {}

  public static String encode(List<IlivalidatorOptionEntry> entries) {
    if (entries == null || entries.isEmpty()) {
      return "";
    }

    StringBuilder out = new StringBuilder();
    for (IlivalidatorOptionEntry entry : entries) {
      if (entry == null || entry.getKey() == null || entry.getKey().isBlank()) {
        continue;
      }
      if (out.length() > 0) {
        out.append('\n');
      }
      out.append(escape(entry.getKey()));
      out.append('\t');
      out.append(entry.isEnabled() ? "Y" : "N");
      out.append('\t');
      out.append(escape(entry.getValue()));
    }
    return out.toString();
  }

  public static List<IlivalidatorOptionEntry> decode(String encoded) {
    List<IlivalidatorOptionEntry> entries = new ArrayList<>();
    if (encoded == null || encoded.isBlank()) {
      return entries;
    }

    String[] lines = encoded.split("\\n");
    for (String line : lines) {
      if (line == null || line.isBlank()) {
        continue;
      }
      String[] parts = line.split("\\t", 3);
      if (parts.length < 2) {
        continue;
      }
      String key = unescape(parts[0]);
      boolean enabled = "Y".equalsIgnoreCase(parts[1]) || "true".equalsIgnoreCase(parts[1]);
      String value = parts.length >= 3 ? unescape(parts[2]) : "";
      entries.add(new IlivalidatorOptionEntry(key, enabled, value));
    }

    return entries;
  }

  private static String escape(String value) {
    if (value == null) {
      return "";
    }
    return value.replace("\\", "\\\\").replace("\t", "\\t").replace("\n", "\\n");
  }

  private static String unescape(String value) {
    if (value == null || value.isEmpty()) {
      return "";
    }
    StringBuilder out = new StringBuilder(value.length());
    boolean escaped = false;
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      if (!escaped) {
        if (c == '\\') {
          escaped = true;
        } else {
          out.append(c);
        }
      } else {
        if (c == 't') {
          out.append('\t');
        } else if (c == 'n') {
          out.append('\n');
        } else {
          out.append(c);
        }
        escaped = false;
      }
    }
    if (escaped) {
      out.append('\\');
    }
    return out.toString();
  }
}
