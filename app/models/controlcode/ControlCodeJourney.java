package models.controlcode;

import models.GoodsType;

import java.util.EnumSet;
import java.util.Optional;

public enum ControlCodeJourney {
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

  ControlCodeJourney(ControlCodeVariant controlCodeVariant, GoodsType goodsType) {
    this.controlCodeVariant = controlCodeVariant;
    this.goodsType = goodsType;
    this.value = controlCodeVariant.urlString() + ":" + goodsType.urlString();
  }

  public String value() {
    return this.value;
  }

  public static Optional<ControlCodeJourney> getMatched(String controlCodeVariantText, String goodsTypeText) {
    Optional<ControlCodeVariant> controlCodeVariantOptional = ControlCodeVariant.getMatchedByUrlString(controlCodeVariantText);
    Optional<GoodsType> goodsTypeOptional =  GoodsType.getMatchedByUrlString(goodsTypeText);
    if (controlCodeVariantOptional.isPresent() && goodsTypeOptional.isPresent()) {
      return EnumSet.allOf(ControlCodeJourney.class).stream()
          .filter(e -> e.controlCodeVariant == controlCodeVariantOptional.get() && e.goodsType == goodsTypeOptional.get())
          .findFirst();
    }
    else {
      return Optional.empty();
    }
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