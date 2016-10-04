package models;

import java.util.EnumSet;
import java.util.Optional;

public enum LifeType {
  ENDANGERED("ENDANGERED"),
  NON_ENDANGERED("NON_ENDANGERED"),
  PLANT("PLANT");

  private String value;

  LifeType(String value) {
    this.value = value;
  }

  public String value() {
    return this.value;
  }

  public static Optional<LifeType> getMatched(String lifeType) {
    return EnumSet.allOf(LifeType.class).stream().filter(e -> e.value().equals(lifeType)).findFirst();
  }
}
