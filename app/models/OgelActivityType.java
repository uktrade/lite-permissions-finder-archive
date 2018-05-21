package models;

import java.util.EnumSet;
import java.util.Optional;

public enum OgelActivityType {
  DU_ANY("DU_ANY"),
  EXHIBITION("EXHIBITION"),
  MIL_ANY("MIL_ANY"),
  MIL_GOV("MIL_GOV"),
  REPAIR("REPAIR"),
  TECH("TECH");

  private String value;

  OgelActivityType(String value) {
    this.value = value;
  }

  public String value() {
    return this.value;
  }

  public static Optional<OgelActivityType> getMatched(String ogelActivity) {
    return EnumSet.allOf(OgelActivityType.class).stream().filter(e -> e.value().equals(ogelActivity)).findFirst();
  }

}
