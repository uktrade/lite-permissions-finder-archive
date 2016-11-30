package importcontent;


public enum ImportQuestion {

  WHERE("importWhere", "Where are you importing from?"),
  WHAT("importWhat", "What are you importing?"),
  CHARCOAL("importCharcoal", "Are you importing charcoal or charcoal products?"),
  MILITARY_IRAN("importMilitaryIran", "Are you importing military goods or technology?"),
  MILITARY_RUSSIA("importMilitaryRussia", "Are you importing military goods or technology?"),
  MILITARY_MYANMAR("importMilitaryMyanmar", "Are you importing military goods or technology?"),
  SHOT("importShot", "Are you importing single-shot rifles or shotguns?"),
  SUBSTANCES("importSubstances", "Are you importing substances that potentially cause cancer, eg asbestos?"),
  OZONE("importOzone", "Are you importing ozone-depleting substances?"),
  DRUGS("importDrugs", "Are you importing controlled drugs?"),
  FOOD_WHAT("importFoodWhat", "What are you importing?"),
  ENDANGERED("importEndangered", "Are the animals endangered?"),
  WHAT_WHERE_IRON("importWhatWhereIron", "Where are you importing the iron/steel from?"),
  WHAT_WHERE_TEXTILES("importWhatWhereTextiles", "Where are you importing the textiles from?"),
  BELARUS_TEXTILES("importBelarusTextiles", "Are you sending textiles to Belarus for processing before being returned to the UK?");

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
