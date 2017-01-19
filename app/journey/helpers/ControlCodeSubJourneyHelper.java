package journey.helpers;

import journey.SubJourneyContextParamProvider;
import models.controlcode.ControlCodeSubJourney;
import play.mvc.Result;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

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

  public static CompletionStage<Result> resolveContextToSubJourney(Function<ControlCodeSubJourney, CompletionStage<Result>> resultFunction) {
    SubJourneyContextParamProvider subJourneyContextParamProvider = new SubJourneyContextParamProvider();
    ControlCodeSubJourney controlCodeSubJourney = subJourneyContextParamProvider.getSubJourneyValueFromRequest();
    return resultFunction.apply(controlCodeSubJourney);
  }

}
