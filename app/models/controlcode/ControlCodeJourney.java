package models.controlcode;

import java.util.EnumSet;
import java.util.Optional;

public enum ControlCodeJourney {
  PHYSICAL_GOODS_SEARCH("physicalGoodsSearch"),
  PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE("physicalGoodsSearchRelatedToSoftware"),
  PHYSICAL_GOODS_SEARCH_RELATED_TO_TECHNOLOGY("physicalGoodsSearchRelatedToTechnology"),
  SOFTWARE_CONTROLS("softwareControls"),
  SOFTWARE_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD("softwareControlsRelatedToPhysicalGood"),
  SOFTWARE_CATCHALL_CONTROLS("softwareCatchallControls"),
  TECHNOLOGY_CONTROLS("technologyControls"),
  TECHNOLOGY_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD("technologyControlsRelatedToPhysicalGood"),
  TECHNOLOGY_CATCHALL_CONTROLS("technologyCatchallControls");

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