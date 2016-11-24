package importcontent;

import utils.common.SelectOption;

import java.util.List;

public class ImportQuestionDefinition {

  private String questionText;

  private List<SelectOption> selectOptions;

  public String getQuestionText() {
    return questionText;
  }

  public void setQuestionText(String questionText) {
    this.questionText = questionText;
  }

  public List<SelectOption> getSelectOptions() {
    return selectOptions;
  }

  public void setSelectOptions(List<SelectOption> selectOptions) {
    this.selectOptions = selectOptions;
  }
}
