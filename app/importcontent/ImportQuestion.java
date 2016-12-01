package importcontent;


public enum ImportQuestion {

  WHERE("import-where", "Where are you importing from?"),
  WHAT("import-what", "What are you importing?"),
  CHARCOAL("import-charcoal", "Are you importing charcoal or charcoal products?"),
  MILITARY_IRAN("import-military-iran", "Are you importing military goods or technology?"),
  MILITARY_RUSSIA("import-military-russia", "Are you importing military goods or technology?"),
  MILITARY_MYANMAR("import-military-myanmar", "Are you importing military goods or technology?"),
  SHOT("import-shot", "Are you importing single-shot rifles or shotguns?"),
  SUBSTANCES("import-substances", "Are you importing substances that potentially cause cancer, eg asbestos?"),
  OZONE("import-ozone", "Are you importing ozone-depleting substances?"),
  DRUGS("import-drugs", "Are you importing controlled drugs?"),
  FOOD_WHAT("import-food-what", "What are you importing?"),
  ENDANGERED("import-endangered", "Are the animals endangered?"),
  WHAT_WHERE_IRON("import-what-where-iron", "Where are you importing the iron/steel from?"),
  WHAT_WHERE_TEXTILES("import-what-where-textiles", "Where are you importing the textiles from?"),
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
