package importcontent;


public enum ImportQuestion {
  WHERE("import-where", "Where are you importing from?"),
  WHAT("import-what", "What are you importing?"),
  CHARCOAL("import-charcoal", "Are you importing charcoal or charcoal products?"),
  MILITARY("import-military", "Are you importing military goods or technology?"),
  SHOT("import-shot", "Are you importing single-shot rifles or shotguns?"),
  SUBSTANCES("import-substances", "Are you importing substances that potentially cause cancer, eg asbestos?"),
  OZONE("import-ozone", "Are you importing ozone-depleting substances?"),
  DRUGS("import-drugs", "Are you importing controlled drugs?"),
  FOOD_WHAT("import-food-what", "What are you importing?"),
  ENDANGERED("import-endangered", "Are the animals endangered?"),
  BELARUS_TEXTILES("import-belarus-textiles", "Are you sending textiles to Belarus for processing before being returned to the UK?");

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
