package models;

import java.util.EnumSet;
import java.util.Optional;

public enum ArtsCulturalGoodsAgeRange {
  LESS_THAN_50("LT50"),
  BETWEEN_50_AND_100("GT50LT100"),
  GREATER_THAN_100("GT100");

  private String value;

  ArtsCulturalGoodsAgeRange(String value) {
    this.value = value;
  }

  public String value() {
    return this.value;
  }

  public boolean equals(String value) {
    return this.value.equals(value);
  }

  public static Optional<ArtsCulturalGoodsAgeRange> getMatched(String ageRange) {
    return EnumSet.allOf(ArtsCulturalGoodsAgeRange.class).stream().filter(e -> e.value().equals(ageRange)).findFirst();
  }


}
