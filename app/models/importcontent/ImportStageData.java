package models.importcontent;

import components.common.journey.JourneyManager;
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

  /**
   * Use where SelectOptions required
   */
  public ImportStageData(String question, List<SelectOption> options,
                         BiFunction<JourneyManager, String, CompletionStage<Result>> journeyTransitionFunction) {
    this.question = question;
    this.options = options;
    this.journeyTransitionFunction = journeyTransitionFunction;
  }

  /**
   * Use for a yes/no question - no SelectOptions required
   */
  public ImportStageData(String question, BiFunction<JourneyManager, String, CompletionStage<Result>> journeyTransitionFunction) {
    this.question = question;
    this.journeyTransitionFunction = journeyTransitionFunction;
    this.yesNoQuestion = true;
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

}
