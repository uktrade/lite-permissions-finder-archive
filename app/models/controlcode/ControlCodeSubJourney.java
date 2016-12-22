package models.controlcode;

import models.GoodsType;

import java.util.EnumSet;
import java.util.Optional;

public enum ControlCodeSubJourney {
  PHYSICAL_GOODS_SEARCH(ControlCodeVariant.SEARCH, GoodsType.PHYSICAL),
  PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE(ControlCodeVariant.SEARCH, GoodsType.SOFTWARE),
  PHYSICAL_GOODS_SEARCH_RELATED_TO_TECHNOLOGY(ControlCodeVariant.SEARCH, GoodsType.TECHNOLOGY),
  SOFTWARE_CONTROLS(ControlCodeVariant.CONTROLS, GoodsType.SOFTWARE),
  SOFTWARE_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD(ControlCodeVariant.CONTROLS_RELATED_TO_A_PHYSICAL_GOOD, GoodsType.SOFTWARE),
  SOFTWARE_CATCHALL_CONTROLS(ControlCodeVariant.CATCHALL_CONTROLS, GoodsType.SOFTWARE),
  TECHNOLOGY_CONTROLS(ControlCodeVariant.CONTROLS, GoodsType.TECHNOLOGY),
  TECHNOLOGY_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD(ControlCodeVariant.CONTROLS_RELATED_TO_A_PHYSICAL_GOOD, GoodsType.TECHNOLOGY),
  TECHNOLOGY_CATCHALL_CONTROLS(ControlCodeVariant.CATCHALL_CONTROLS, GoodsType.SOFTWARE);

  private final ControlCodeVariant controlCodeVariant;
  private final GoodsType goodsType;
  private final String value;

  ControlCodeSubJourney(ControlCodeVariant controlCodeVariant, GoodsType goodsType) {
    this.controlCodeVariant = controlCodeVariant;
    this.goodsType = goodsType;
    this.value = controlCodeVariant.urlString() + ":" + goodsType.urlString();
  }

  public String value() {
    return this.value;
  }

  public static Optional<ControlCodeSubJourney> getMatched(String controlCodeVariantText, String goodsTypeText) {
    Optional<ControlCodeVariant> controlCodeVariantOptional = ControlCodeVariant.getMatchedByUrlString(controlCodeVariantText);
    Optional<GoodsType> goodsTypeOptional =  GoodsType.getMatchedByUrlString(goodsTypeText);
    if (controlCodeVariantOptional.isPresent() && goodsTypeOptional.isPresent()) {
      return EnumSet.allOf(ControlCodeSubJourney.class).stream()
          .filter(e -> e.controlCodeVariant == controlCodeVariantOptional.get() && e.goodsType == goodsTypeOptional.get())
          .findFirst();
    }
    else {
      return Optional.empty();
    }
  }

  public static Optional<ControlCodeSubJourney> getMatched(String controlCodeSubJourney) {
    return EnumSet.allOf(ControlCodeSubJourney.class).stream().filter(e -> e.value().equals(controlCodeSubJourney)).findFirst();
  }

  public static boolean isPhysicalGoodsSearchVariant(ControlCodeSubJourney controlCodeSubJourney) {
    return controlCodeSubJourney == PHYSICAL_GOODS_SEARCH ||
        controlCodeSubJourney == PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE ||
        controlCodeSubJourney == PHYSICAL_GOODS_SEARCH_RELATED_TO_TECHNOLOGY;
  }

  public boolean isPhysicalGoodsSearchVariant() {
    return isPhysicalGoodsSearchVariant(this);
  }

  public static boolean isSoftTechControlsVariant(ControlCodeSubJourney controlCodeSubJourney) {
    return controlCodeSubJourney == SOFTWARE_CONTROLS || controlCodeSubJourney == TECHNOLOGY_CONTROLS;
  }

  public boolean isSoftTechControlsVariant() {
    return isSoftTechControlsVariant(this);
  }

  public static boolean isSoftTechControlsRelatedToPhysicalGoodVariant(ControlCodeSubJourney controlCodeSubJourney) {
    return controlCodeSubJourney == SOFTWARE_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD || controlCodeSubJourney == TECHNOLOGY_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD;
  }

  public boolean isSoftTechControlsRelatedToPhysicalGoodVariant() {
    return isSoftTechControlsRelatedToPhysicalGoodVariant(this);
  }

  public static boolean isSoftTechCatchallControlsVariant(ControlCodeSubJourney controlCodeSubJourney) {
    return controlCodeSubJourney == SOFTWARE_CATCHALL_CONTROLS || controlCodeSubJourney == TECHNOLOGY_CATCHALL_CONTROLS;
  }

  public boolean isSoftTechCatchallControlsVariant() {
    return isSoftTechCatchallControlsVariant(this);
  }

  /**
   * Returns the {@link GoodsType} mapping for a given {@link ControlCodeSubJourney}. When no mapping is found returns null
   * @param controlCodeSubJourney
   * @return the {@link GoodsType} mapping or null
   */
  public static GoodsType getGoodsType(ControlCodeSubJourney controlCodeSubJourney) {
    if (controlCodeSubJourney == PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE ||
        controlCodeSubJourney == SOFTWARE_CONTROLS ||
        controlCodeSubJourney == SOFTWARE_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD ||
        controlCodeSubJourney == SOFTWARE_CATCHALL_CONTROLS) {
      return GoodsType.SOFTWARE;
    }
    else if (controlCodeSubJourney == PHYSICAL_GOODS_SEARCH_RELATED_TO_TECHNOLOGY ||
        controlCodeSubJourney == TECHNOLOGY_CONTROLS ||
        controlCodeSubJourney == TECHNOLOGY_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD ||
        controlCodeSubJourney == TECHNOLOGY_CATCHALL_CONTROLS) {
      return GoodsType.TECHNOLOGY;
    }
    else if (controlCodeSubJourney == PHYSICAL_GOODS_SEARCH){
      return GoodsType.PHYSICAL;
    }
    else {
      return null;
    }
  }

  /**
   * @see #getGoodsType(ControlCodeSubJourney)
   */
  public GoodsType getGoodsType() {
    return getGoodsType(this);
  }

  /**
   * Returns the software or technology {@link GoodsType} mapping
   * @param controlCodeSubJourney
   * @return either {@link GoodsType#SOFTWARE} or {@link GoodsType#TECHNOLOGY} or null
   */
  public static GoodsType getSoftTechGoodsType(ControlCodeSubJourney controlCodeSubJourney) {
    GoodsType goodsType = getGoodsType(controlCodeSubJourney);
    if (goodsType == GoodsType.SOFTWARE || goodsType == GoodsType.TECHNOLOGY) {
      return goodsType;
    }
    else {
      return null;
    }
  }

  /**
   * @see #getSoftTechGoodsType(ControlCodeSubJourney)
   */
  public GoodsType getSoftTechGoodsType() {
    return getSoftTechGoodsType(this);
  }

}