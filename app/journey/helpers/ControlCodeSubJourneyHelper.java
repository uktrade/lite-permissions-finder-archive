package journey.helpers;

import journey.SubJourneyContextParamProvider;
import models.controlcode.ControlCodeSubJourney;

public class ControlCodeSubJourneyHelper {

  public static ControlCodeSubJourney resolveUrlToSubJourneyAndUpdateContext(String controlCodeVariantText, String goodsTypeText) {
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    ControlCodeSubJourney controlCodeSubJourney = ControlCodeSubJourney.getMatched(controlCodeVariantText, goodsTypeText).get();
    updateSubJourneyContext(controlCodeSubJourney);
    return controlCodeSubJourney;
  }

  public static void updateSubJourneyContext(ControlCodeSubJourney controlCodeSubJourney) {
    SubJourneyContextParamProvider subJourneyContextParamProvider = new SubJourneyContextParamProvider();
    subJourneyContextParamProvider.updateSubJourneyValueOnContext(controlCodeSubJourney);
  }

  public static ControlCodeSubJourney resolveContextToSubJourney() {
    SubJourneyContextParamProvider subJourneyContextParamProvider = new SubJourneyContextParamProvider();
    return subJourneyContextParamProvider.getSubJourneyValueFromRequest();
  }

}
