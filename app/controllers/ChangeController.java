package controllers;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import journey.JourneyDefinitionNames;
import play.mvc.Result;

import java.util.concurrent.CompletionStage;

public class ChangeController {

  private final JourneyManager journeyManager;

  @Inject
  public ChangeController(JourneyManager journeyManager) {
    this.journeyManager = journeyManager;
  }

  public CompletionStage<Result> changeControlCode() {
    return journeyManager.startJourney(JourneyDefinitionNames.CHANGE_CONTROL_CODE);
  }

  public CompletionStage<Result> changeDestinationCountries() {
    return journeyManager.startJourney(JourneyDefinitionNames.CHANGE_DESTINATION_COUNTRIES);
  }

  public CompletionStage<Result> changeOgelType() {
    return journeyManager.startJourney(JourneyDefinitionNames.CHANGE_OGEL_TYPE);
  }

}
