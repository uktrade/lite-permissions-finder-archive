package journey;

import components.common.state.ContextParamProvider;
import models.controlcode.ControlCodeJourney;

public class SubJourneyContextParamProvider extends ContextParamProvider {

  @Override
  public String getParamName() {
    return "ctx_sub_journey";
  }

  public ControlCodeJourney getSubJourneyValueFromRequest() {
    return getSubJourneyOrElseNull(getParamValueFromRequest());
  }

  public ControlCodeJourney getSubJourneyValueFromContext() {
    return getSubJourneyOrElseNull(getParamValueFromRequest());
  }

  public void updateSubJourneyValueOnContext(ControlCodeJourney controlCodeJourney) {
    updateParamValueOnContext(controlCodeJourney.value());
  }

  private ControlCodeJourney getSubJourneyOrElseNull(String controlCodeJourney) {
    return ControlCodeJourney.getMatched(controlCodeJourney).orElse(null);
  }

}
