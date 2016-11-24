package importcontent;

import importcontent.models.ImportWhat;

public class QuestionImportWhat {

  public ImportQuestionDefinition getQuestionDefinition() {
    ImportQuestionDefinition def = new ImportQuestionDefinition();
    def.setQuestionText("What are you importing?");
    def.setSelectOptions(ImportWhat.getSelectOptions());
    return def;
  }

}
