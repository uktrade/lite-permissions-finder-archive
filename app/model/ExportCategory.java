package model;

import java.util.EnumSet;
import java.util.Optional;

public enum ExportCategory {
  ARTS_CULTURAL("ARTS_CULTURAL"),
  CHEMICALS_COSMETICS("CHEMICALS_COSMETICS"),
  DUAL_USE("DUAL_USE"),
  FINANCIAL_ASSISTANCE("FINANCIAL_ASSISTANCE"),
  FOOD("FOOD"),
  MEDICINES_DRUGS("MEDICINES_DRUGS"),
  MILITARY("MILITARY"),
  NONE("NONE"),
  PLANTS_ANIMALS("PLANTS_ANIMALS"),
  RADIOACTIVE("RADIOACTIVE"),
  TECHNICAL_ASSISTANCE("TECHNICAL_ASSISTANCE"),
  TORTURE_RESTRAINT("TORTURE_RESTRAINT");

  private String value;

  ExportCategory(String value) {
    this.value = value;
  }

  public String value() {
    return this.value;
  }

  public static Optional<ExportCategory> getMatched(String exportCategory) {
    return EnumSet.allOf(ExportCategory.class).stream().filter(e -> e.value().equals(exportCategory)).findFirst();
  }
}
