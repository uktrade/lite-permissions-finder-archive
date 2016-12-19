package models.controlcode;

import models.GoodsType;

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

  public boolean isPhysicalGoodsSearchVariant() {
    return isPhysicalGoodsSearchVariant(this);
  }

  public static boolean isSoftTechControlsVariant(ControlCodeJourney controlCodeJourney) {
    return controlCodeJourney == SOFTWARE_CONTROLS || controlCodeJourney == TECHNOLOGY_CONTROLS;
  }

  public boolean isSoftTechControlsVariant() {
    return isSoftTechControlsVariant(this);
  }

  public static boolean isSoftTechControlsRelatedToPhysicalGoodVariant(ControlCodeJourney controlCodeJourney) {
    return controlCodeJourney == SOFTWARE_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD || controlCodeJourney == TECHNOLOGY_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD;
  }

  public boolean isSoftTechControlsRelatedToPhysicalGoodVariant() {
    return isSoftTechControlsRelatedToPhysicalGoodVariant(this);
  }

  public static boolean isSoftTechCatchallControlsVariant(ControlCodeJourney controlCodeJourney) {
    return controlCodeJourney == SOFTWARE_CATCHALL_CONTROLS || controlCodeJourney == TECHNOLOGY_CATCHALL_CONTROLS;
  }

  public boolean isSoftTechCatchallControlsVariant() {
    return isSoftTechCatchallControlsVariant(this);
  }

  /**
   * Returns the {@link GoodsType} mapping for a given {@link ControlCodeJourney}. When no mapping is found returns null
   * @param controlCodeJourney
   * @return the {@link GoodsType} mapping or null
   */
  public static GoodsType getGoodsType(ControlCodeJourney controlCodeJourney) {
    if (controlCodeJourney == PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE ||
        controlCodeJourney == SOFTWARE_CONTROLS ||
        controlCodeJourney == SOFTWARE_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD ||
        controlCodeJourney == SOFTWARE_CATCHALL_CONTROLS) {
      return GoodsType.SOFTWARE;
    }
    else if (controlCodeJourney == PHYSICAL_GOODS_SEARCH_RELATED_TO_TECHNOLOGY ||
        controlCodeJourney == TECHNOLOGY_CONTROLS ||
        controlCodeJourney == TECHNOLOGY_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD ||
        controlCodeJourney == TECHNOLOGY_CATCHALL_CONTROLS) {
      return GoodsType.TECHNOLOGY;
    }
    else if (controlCodeJourney == PHYSICAL_GOODS_SEARCH){
      return GoodsType.PHYSICAL;
    }
    else {
      return null;
    }
  }

  /**
   * @see #getGoodsType(ControlCodeJourney)
   */
  public GoodsType getGoodsType() {
    return getGoodsType(this);
  }

  /**
   * Returns the software or technology {@link GoodsType} mapping
   * @param controlCodeJourney
   * @return either {@link GoodsType#SOFTWARE} or {@link GoodsType#TECHNOLOGY} or null
   */
  public static GoodsType getSoftTechGoodsType(ControlCodeJourney controlCodeJourney) {
    GoodsType goodsType = getGoodsType(controlCodeJourney);
    if (goodsType == GoodsType.SOFTWARE || goodsType == GoodsType.TECHNOLOGY) {
      return goodsType;
    }
    else {
      return null;
    }
  }

  /**
   * @see #getSoftTechGoodsType(ControlCodeJourney)
   */
  public GoodsType getSoftTechGoodsType() {
    return getSoftTechGoodsType(this);
  }

}