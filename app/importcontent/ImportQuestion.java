package importcontent;


public enum ImportQuestion {
  WHERE("importWhere", "Where are you importing from?"),
  WHAT("importWhat", "What are you importing?"),
  CHARCOAL("importCharcoal", "Are you importing charcoal or charcoal products?"),
  MILITARY("importMilitary", "Are you importing military goods or technology?"),
  SHOT("importShot", "Are you importing single-shot rifles or shotguns?"),
  SUBSTANCES("importSubstances", "Are you importing substances that potentially cause cancer, eg asbestos?"),
  OZONE("importOzone", "Are you importing ozone-depleting substances?"),
  DRUGS("importDrugs", "Are you importing controlled drugs?"),
  FOOD_WHAT("importFoodWhat", "What are you importing?"),
  ENDANGERED("importEndangered", "Are the animals endangered?"),
  BELARUS_TEXTILES("importBelarusTextiles", "Are you importing textiles that you previously sent to Belarus from the UK for processing?");

  private String key;
  private String question;

  ImportQuestion(String key, String question) {
    this.key = key;
    this.question = question;
  }

  public String question() {
    return this.question;
  }

  public String key() {
    return this.key;
  }

}
