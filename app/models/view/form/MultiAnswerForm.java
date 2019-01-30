package models.view.form;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class MultiAnswerForm {

  private @Getter @Setter List<String> answers;
  private @Getter @Setter String action;

}
