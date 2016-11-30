package models.importcontent;

import components.common.journey.JourneyManager;
import play.mvc.Result;
import utils.common.SelectOption;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;

public class ImportStageData {

  private String question;
  private List<SelectOption> options;
  private BiFunction<JourneyManager, String, CompletionStage<Result>> journeyTransitionFunction;

  public ImportStageData(String question, List<SelectOption> options,
                         BiFunction<JourneyManager, String, CompletionStage<Result>> journeyTransitionFunction) {
    this.question = question;
    this.options = options;
    this.journeyTransitionFunction = journeyTransitionFunction;
  }

  public CompletionStage<Result> completeTransition(JourneyManager journeyManager, String selectedOption) {
    return journeyTransitionFunction.apply(journeyManager, selectedOption);
  }

  public boolean isValidStageOption(String option) {
    return options.stream().anyMatch(so -> so.value.equals(option));
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

  public void setOptions(List<SelectOption> options) {
    this.options = options;
  }

}
