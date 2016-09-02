package model;

import java.util.EnumSet;
import java.util.Optional;

public enum TradeType {
  IMPORT("IMPORT"),
  EXPORT("EXPORT"),
  BROKERING("BROKERING"),
  TRANSSHIPMENT("TRANSSHIPMENT");

  private String value;

  TradeType(String value) {
    this.value = value;
  }

  public String value() {
    return this.value;
  }

  public static Optional<TradeType> getMatched(String ogelActivity) {
    return EnumSet.allOf(TradeType.class).stream().filter(e -> e.value().equals(ogelActivity)).findFirst();
  }
}
