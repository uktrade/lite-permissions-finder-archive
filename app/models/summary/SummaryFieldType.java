package models.summary;

import java.util.EnumSet;
import java.util.Optional;

public enum SummaryFieldType {
  CONTROL_CODE("Goods rating", "controlCode"),
  OGEL_TYPE("Licence Type", "ogelType"),
  DESTINATION_COUNTRIES("Destination Countries", "destinationCountries");

  /**
   * Represent the heading which describes the fields contents
   */
  public final String heading;

  /**
   * Represent the value of a button name/value pair
   */
  public final String fieldValue;

  SummaryFieldType(String heading, String fieldValue) {
    this.heading = heading;
    this.fieldValue = fieldValue;
  }

  public static Optional<SummaryFieldType> getMatchedByFieldValue(String fieldValue) {
    return EnumSet.allOf(SummaryFieldType.class).stream().filter(e -> e.fieldValue.equals(fieldValue)).findFirst();
  }
}
