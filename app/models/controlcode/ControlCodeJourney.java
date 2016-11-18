package models.controlcode;

import java.util.EnumSet;
import java.util.Optional;

public enum ControlCodeJourney {
  PHYSICAL_GOODS_SEARCH("physicalGoodsSearch"),
  PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE("physicalGoodsSearchRelatedToSoftware"),
  SOFTWARE_CONTROLS("softwareControls"),
  SOFTWARE_CONTROLS_RELATED_TO_PHYSICAL_GOODS("relatedSoftwareControls");

  private String value;

  ControlCodeJourney(String value) {
    this.value = value;
  }

  public String value() {
    return this.value;
  }

  public static Optional<ControlCodeJourney> getMatched(String controlCodeJourney) {
    return EnumSet.allOf(ControlCodeJourney.class).stream().filter(e -> e.value().equals(controlCodeJourney)).findFirst();
  }

}