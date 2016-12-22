package models.controlcode;

import java.util.EnumSet;
import java.util.Optional;

public enum ControlCodeVariant {
  SEARCH("search"),
  CONTROLS("controls"),
  CONTROLS_RELATED_TO_A_PHYSICAL_GOOD("controls-related-to-physical-good"),
  CATCHALL_CONTROLS("catchall");

  private final String value;
  private final String urlString;

  ControlCodeVariant(String urlString) {
    this.value = this.toString();
    this.urlString = urlString;
  }

  public String value() {
    return this.value;
  }

  public String urlString() {
    return this.urlString;
  }

  public static Optional<ControlCodeVariant> getMatchedByValue(String controlCodeVariantValue) {
    return EnumSet.allOf(ControlCodeVariant.class).stream().filter(e -> e.urlString().equals(controlCodeVariantValue)).findFirst();
  }

  public static Optional<ControlCodeVariant> getMatchedByUrlString(String controlCodeVariantUrlString) {
    return EnumSet.allOf(ControlCodeVariant.class).stream().filter(e -> e.urlString().equals(controlCodeVariantUrlString)).findFirst();
  }
}
