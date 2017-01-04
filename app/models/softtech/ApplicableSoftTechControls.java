package models.softtech;

public enum ApplicableSoftTechControls {
  ZERO,
  ONE,
  GREATER_THAN_ONE;

  /**
   * Returns this enums representation of a given int value
   * @param i a value >= 0
   * @return the enum member which represents this value
   */
  public static ApplicableSoftTechControls fromInt(int i){
    if (i == 0) {
      return ApplicableSoftTechControls.ZERO;
    }
    else if (i == 1) {
      return ApplicableSoftTechControls.ONE;
    }
    else if (i > 1) {
      return ApplicableSoftTechControls.GREATER_THAN_ONE;
    }
    else {
      throw new IllegalArgumentException(String.format("Expected argument i to be >= 0 but found %d", i));
    }
  }
}
