package importcontent.models;

public enum ImportWhere {

  MILITARY_COUNTRIES("Military Countries"),
  CHARCOAL_COUNTRIES("Charcoal Countries"),
  CRIMEA_REGION("Crimea Region"),
  EU_COUNTRIES("EU Countries"),
  SYRIA_COUNTRY("Syria"),
  OTHER_COUNTRIES("Other Countries");

  private String description;

  ImportWhere(String description) {
    this.description = description;
  }

  public String getDescription() {
    return this.description;
  }


}
