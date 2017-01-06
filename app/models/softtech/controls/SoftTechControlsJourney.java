package models.softtech.controls;

import models.GoodsType;
import models.controlcode.ControlCodeSubJourney;

public enum SoftTechControlsJourney {
  SOFTWARE_CATEGORY(ControlCodeSubJourney.SOFTWARE_CONTROLS),
  SOFTWARE_RELATED_TO_A_PHYSICAL_GOOD(ControlCodeSubJourney.SOFTWARE_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD),
  SOFTWARE_CATCHALL(ControlCodeSubJourney.SOFTWARE_CATCHALL_CONTROLS),
  TECHNOLOGY_CATEGORY(ControlCodeSubJourney.TECHNOLOGY_CONTROLS),
  TECHNOLOGY_RELATED_TO_A_PHYSICAL_GOOD(ControlCodeSubJourney.TECHNOLOGY_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD),
  TECHNOLOGY_CATCHALL(ControlCodeSubJourney.TECHNOLOGY_CATCHALL_CONTROLS);

  private final ControlCodeSubJourney mappedControlCodeSubJourney;

  SoftTechControlsJourney(ControlCodeSubJourney mappedControlCodeSubJourney) {
    this.mappedControlCodeSubJourney = mappedControlCodeSubJourney;
  }

  public ControlCodeSubJourney getMappedControlCodeSubJourney() {
    return mappedControlCodeSubJourney;
  }

  public boolean isCategoryVariant() {
    return isCategoryVariant(this);
  }

  public boolean isRelatedToPhysicalGoodsVariant() {
    return isRelatedToPhysicalGoodsVariant(this);
  }

  public boolean isCatchallVariant() {
    return isCatchallVariant(this);
  }

  public GoodsType getSoftTechGoodsType() {
    return getSoftTechGoodsType(this);
  }

  private static SoftTechControlsJourney validateAndConvertGoodsType(GoodsType goodsType,
                                                                     SoftTechControlsJourney softwareJourney,
                                                                     SoftTechControlsJourney technologyJourney){
    if (goodsType == GoodsType.SOFTWARE) {
      return softwareJourney;
    } else if (goodsType == GoodsType.TECHNOLOGY) {
      return technologyJourney;
    }
    else {
      if (goodsType == null) {
        throw new IllegalArgumentException(String.format("Argument goodsType should not be null"));
      }
      else {
        throw new RuntimeException(String.format("Unexpected member of GoodsType enum: \"%s\""
            , goodsType.toString()));
      }
    }
  }

  public static SoftTechControlsJourney getCategoryVariant(GoodsType goodsType){
    return validateAndConvertGoodsType(goodsType, SOFTWARE_CATEGORY, TECHNOLOGY_CATEGORY);
  }

  public static SoftTechControlsJourney getRelatedToPhysicalGoodsVariant(GoodsType goodsType){
    return validateAndConvertGoodsType(goodsType, SOFTWARE_RELATED_TO_A_PHYSICAL_GOOD, TECHNOLOGY_RELATED_TO_A_PHYSICAL_GOOD);
  }

  public static SoftTechControlsJourney getCatchallVariant(GoodsType goodsType){
    return validateAndConvertGoodsType(goodsType, SOFTWARE_CATCHALL, TECHNOLOGY_CATCHALL);
  }

  public static boolean isCategoryVariant(SoftTechControlsJourney journey) {
    return journey == SOFTWARE_CATEGORY || journey == TECHNOLOGY_CATEGORY;
  }

  public static boolean isRelatedToPhysicalGoodsVariant(SoftTechControlsJourney journey) {
    return journey == SOFTWARE_RELATED_TO_A_PHYSICAL_GOOD || journey == TECHNOLOGY_RELATED_TO_A_PHYSICAL_GOOD;
  }

  public static boolean isCatchallVariant(SoftTechControlsJourney journey) {
    return journey == SOFTWARE_CATCHALL || journey == TECHNOLOGY_CATCHALL;
  }

  public static GoodsType getSoftTechGoodsType(SoftTechControlsJourney journey){
    if (journey == SOFTWARE_CATEGORY || journey == SOFTWARE_RELATED_TO_A_PHYSICAL_GOOD || journey == SOFTWARE_CATCHALL) {
      return GoodsType.SOFTWARE;
    }
    else if (journey == TECHNOLOGY_CATEGORY || journey == TECHNOLOGY_RELATED_TO_A_PHYSICAL_GOOD || journey == TECHNOLOGY_CATCHALL) {
      return GoodsType.TECHNOLOGY;
    }
    else {
      if (journey == null) {
        throw new IllegalArgumentException(String.format("Argument journey should not be null"));
      }
      else {
        throw new RuntimeException(String.format("Unexpected member of SoftTechControlsJourney enum: \"%s\""
            , journey.toString()));
      }
    }
  }
}
