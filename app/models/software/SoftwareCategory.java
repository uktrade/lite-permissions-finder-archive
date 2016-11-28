package models.software;

public enum SoftwareCategory {
  AEROSPACE,
  COMPUTERS,
  ELECTRONICS,
  MARINE,
  MATERIALS_PROCESSING,
  MILITARY,
  NAVIGATION,
  NUCLEAR,
  SENSORS,
  SPECIAL_MATERIALS,
  TELECOMS;

  public String toUrlString() {
    return this.toString().replace('_','-').toLowerCase();
  }

  public static boolean isDualUseSoftwareCategory(SoftwareCategory softwareCategory) {
    return softwareCategory.isDualUseSoftwareCategory();
  }

  public boolean isDualUseSoftwareCategory() {
    return this != MILITARY;
  }

}
