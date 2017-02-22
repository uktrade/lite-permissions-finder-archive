package importcontent;


import controllers.importcontent.forms.ImportFoodWhatForm;
import controllers.importcontent.forms.ImportForm;
import controllers.importcontent.forms.ImportWhatForm;

public enum ImportQuestion {
  WHERE("importWhere", "Where are you importing from?"),
  WHAT("importWhat", "What are you importing?", ImportWhatForm.class),
  CHARCOAL("importCharcoal", "Are you importing charcoal or charcoal products?"),
  MILITARY("importMilitary", "Are you importing military goods or technology?"),
  SHOT("importShot", "Are you importing single-shot rifles or shotguns?"),
  SUBSTANCES("importSubstances", "Are you importing substances that potentially cause cancer, eg asbestos?"),
  OZONE("importOzone", "Are you importing ozone-depleting substances?"),
  DRUGS("importDrugs", "Are you importing controlled drugs?"),
  FOOD_WHAT("importFoodWhat", "What are you importing?", ImportFoodWhatForm.class),
  ENDANGERED("importEndangered", "Are the animals endangered?"),
  BELARUS_TEXTILES("importBelarusTextiles", "Are you importing textiles that you previously sent to Belarus from the UK for processing?");

  private final String key;
  private final String question;
  private final Class<?> formClass;

  ImportQuestion(String key, String question, Class<?> formClass) {
    this.key = key;
    this.question = question;
    this.formClass = formClass;
  }

  ImportQuestion(String key, String question) {
    this.key = key;
    this.question = question;
    this.formClass = ImportForm.class;
  }

  public String question() {
    return this.question;
  }

  public String key() {
    return this.key;
  }

  public Class<?> formClass() {
    return this.formClass;
  }
}
