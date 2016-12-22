package journey;

import components.common.state.ContextParamProvider;
import models.controlcode.ControlCodeSubJourney;

public class SubJourneyContextParamProvider extends ContextParamProvider {

  @Override
  public String getParamName() {
    return "ctx_sub_journey";
  }

  public ControlCodeSubJourney getSubJourneyValueFromRequest() {
    return getSubJourneyOrElseNull(getParamValueFromRequest());
  }

  public ControlCodeSubJourney getSubJourneyValueFromContext() {
    return getSubJourneyOrElseNull(getParamValueFromRequest());
  }

  public void updateSubJourneyValueOnContext(ControlCodeSubJourney controlCodeSubJourney) {
    updateParamValueOnContext(controlCodeSubJourney.value());
  }

  private ControlCodeSubJourney getSubJourneyOrElseNull(String controlCodeSubJourney) {
    return ControlCodeSubJourney.getMatched(controlCodeSubJourney).orElse(null);
  }

}
