package models.importcontent;

import components.common.journey.JourneyManager;
import importcontent.ImportQuestion;
import play.mvc.Result;
import utils.common.SelectOption;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;

public class ImportStageData {

  private String question;
  private boolean yesNoQuestion = false;
  private List<SelectOption> options;
  private BiFunction<JourneyManager, String, CompletionStage<Result>> journeyTransitionFunction;
  private Class<?> formClass;

  /**
   * Use where SelectOptions required
   */
  public ImportStageData(ImportQuestion question, List<SelectOption> options,
                         BiFunction<JourneyManager, String, CompletionStage<Result>> journeyTransitionFunction) {
    this.question = question.question();
    this.options = options;
    this.journeyTransitionFunction = journeyTransitionFunction;
    this.formClass = question.formClass();
  }

  /**
   * Use for a yes/no question - no SelectOptions required
   */
  public ImportStageData(ImportQuestion question, BiFunction<JourneyManager, String, CompletionStage<Result>> journeyTransitionFunction) {
    this.question = question.question();
    this.journeyTransitionFunction = journeyTransitionFunction;
    this.yesNoQuestion = true;
    this.formClass = question.formClass();
  }

  public CompletionStage<Result> completeTransition(JourneyManager journeyManager, String selectedOption) {
    return journeyTransitionFunction.apply(journeyManager, selectedOption);
  }

  public boolean isValidStageOption(String option) {
    boolean valid;
    if(yesNoQuestion) {
      valid = option.equals("true") || option.equals("false") ;
    } else {
      valid = options.stream().anyMatch(so -> so.value.equals(option));
    }
    return valid;
  }

  public boolean isYesNoQuestion() {
    return yesNoQuestion;
  }

  public String getQuestion() {
    return question;
  }

  public void setQuestion(String question) {
    this.question = question;
  }

  public List<SelectOption> getOptions() {
    return options;
  }

  public Class<?> getFormClass() {
    return formClass;
  }
}
