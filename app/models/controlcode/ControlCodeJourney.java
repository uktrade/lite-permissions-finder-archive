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

  public static boolean isPhysicalGoodsSearchVariant(ControlCodeJourney controlCodeJourney) {
    return controlCodeJourney == PHYSICAL_GOODS_SEARCH ||
        controlCodeJourney == PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE ||
        controlCodeJourney == PHYSICAL_GOODS_SEARCH_RELATED_TO_TECHNOLOGY;
  }

  public static boolean isSoftTechControlsVariant(ControlCodeJourney controlCodeJourney) {
    return controlCodeJourney == SOFTWARE_CONTROLS || controlCodeJourney == TECHNOLOGY_CONTROLS;
  }

  public static boolean isSoftTechControlsRelatedToPhysicalGoodVariant(ControlCodeJourney controlCodeJourney) {
    return controlCodeJourney == SOFTWARE_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD || controlCodeJourney == TECHNOLOGY_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD;
  }

  public static boolean isSoftTechCatchallControlsVariant(ControlCodeJourney controlCodeJourney) {
    return controlCodeJourney == SOFTWARE_CATCHALL_CONTROLS || controlCodeJourney == TECHNOLOGY_CATCHALL_CONTROLS;
  }

}