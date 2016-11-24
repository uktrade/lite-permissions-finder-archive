package importcontent;


import importcontent.models.ImportWhere;

public class QuestionImportWhere {

  public ImportQuestionDefinition getQuestionDefinition() {
    ImportQuestionDefinition def = new ImportQuestionDefinition();
    def.setQuestionText("Where are you importing from?");
    def.setSelectOptions(ImportWhere.getSelectOptions());
    return def;
  }
}
