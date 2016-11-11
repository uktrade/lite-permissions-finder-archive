package models.software;

public enum SoftwareCategory {
  MILITARY,
  DUMMY,
  RADIOACTIVE;

  public static boolean isDualUseSoftwareCategory(SoftwareCategory softwareCategory) {
    return softwareCategory == DUMMY || softwareCategory == RADIOACTIVE;
  }
}
