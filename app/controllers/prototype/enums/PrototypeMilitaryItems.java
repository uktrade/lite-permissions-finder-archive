package controllers.prototype.enums;

import java.util.HashMap;
import java.util.Map;

public enum PrototypeMilitaryItems {

  EQUIPMENT("equipment"),
  MATERIALS("materials and substances"),
  SOFTWARE("software"),
  TECHNOLOGY("technology");

  private String value;
  // Reverse-lookup map
  private static final Map<String, PrototypeMilitaryItems> lookup = new HashMap<>();

  static {
    for (PrototypeMilitaryItems d : PrototypeMilitaryItems.values()) {
      lookup.put(d.value(), d);
    }
  }

  PrototypeMilitaryItems(String value) {
    this.value = value;
  }

  public String value() {
    return this.value;
  }

  public static PrototypeMilitaryItems get(String value) {
    return lookup.get(value);
  }

}
