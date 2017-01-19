package models;

import java.util.EnumSet;
import java.util.Optional;

public enum ExportCategory {
  ARTS_CULTURAL("Arts and cultural goods"),
  CHEMICALS_COSMETICS("Cosmetics, chemicals, and pesticides"),
  DUAL_USE("Dual-use goods, software and technical information"),
  FINANCIAL_ASSISTANCE("Financing and financial assistance"),
  FOOD("Food"),
  MEDICINES_DRUGS("Medicines and drugs"),
  MILITARY("Military weapons and equipment"),
  NONE("None of the above"),
  NON_MILITARY("Personal-use firearms"),
  PLANTS_ANIMALS("Plants and animals"),
  RADIOACTIVE("Radioactive goods"),
  TECHNICAL_ASSISTANCE("Technical assistance"),
  TORTURE_RESTRAINT("Goods that could be used for torture, restraint or execution"),
  WASTE("Waste");

  private final String heading;

  ExportCategory(String heading) {
    this.heading = heading;
  }

  public String getHeading() {
    return heading;
  }

  public static Optional<ExportCategory> getMatched(String name) {
    return EnumSet.allOf(ExportCategory.class).stream()
        .filter(e -> e.toString().equals(name))
        .findFirst();
  }
}
