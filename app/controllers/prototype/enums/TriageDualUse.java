package controllers.prototype.enums;

public enum TriageDualUse {

  CATEGORY_0A("Nuclear equipment, facilities and materials"),
  CATEGORY_1A("Special materials and related equipment"),
  CATEGORY_2A("Materials processing"),
  CATEGORY_3A("Electronics"),
  CATEGORY_4A("Computers"),
  CATEGORY_5A("Telecommunications and information security"),
  CATEGORY_6A("Sensors and lasers"),
  CATEGORY_7A("Navigation and avionics"),
  CATEGORY_8A("Marine"),
  CATEGORY_9A("Aerospace and propulsion");

  private String value;

  TriageDualUse(String value) {
    this.value = value;
  }

  public String value() {
    return this.value;
  }

}
