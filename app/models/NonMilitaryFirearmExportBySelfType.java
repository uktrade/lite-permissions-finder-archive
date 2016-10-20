package models;

import java.util.EnumSet;
import java.util.Optional;

public enum NonMilitaryFirearmExportBySelfType {
  YES("yes"),
  NO_INCLUDED_IN_PERSONAL_EFFECTS("noIncludedInPersonalEffects"),
  NO_TRANSFER_TO_THIRD_PARTY("noTransferToThirdsParty");

  private String value;

  NonMilitaryFirearmExportBySelfType(String value) {
    this.value = value;
  }

  public String value() {
    return this.value;
  }

  public static Optional<NonMilitaryFirearmExportBySelfType> getMatched(String value) {
    return EnumSet.allOf(NonMilitaryFirearmExportBySelfType.class).stream()
        .filter(e -> e.value().equals(value))
        .findFirst();
  }
}
