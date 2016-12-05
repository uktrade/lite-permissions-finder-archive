package models.softtech;

public enum SoftTechCategory {
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

  public static boolean isDualUseSoftTechCategory(SoftTechCategory softTechCategory) {
    return softTechCategory.isDualUseSoftTechCategory();
  }

  public boolean isDualUseSoftTechCategory() {
    return this != MILITARY;
  }

}