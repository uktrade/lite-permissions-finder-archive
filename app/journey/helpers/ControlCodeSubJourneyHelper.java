package journey.helpers;

import journey.SubJourneyContextParamProvider;
import models.controlcode.ControlCodeSubJourney;
import play.mvc.Result;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class ControlCodeSubJourneyHelper {

  public static CompletionStage<Result> resolveUrlToSubJourneyAndUpdateContext(String controlCodeVariantText, String goodsTypeText, Function<ControlCodeSubJourney, CompletionStage<Result>> resultFunction) {
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    ControlCodeSubJourney controlCodeSubJourney = ControlCodeSubJourney.getMatched(controlCodeVariantText, goodsTypeText).get();
    return resolveUrlToSubJourneyAndUpdateContext(controlCodeSubJourney, resultFunction);
  }

  public static CompletionStage<Result> resolveUrlToSubJourneyAndUpdateContext(ControlCodeSubJourney controlCodeSubJourney, Function<ControlCodeSubJourney, CompletionStage<Result>> resultFunction) {
    SubJourneyContextParamProvider subJourneyContextParamProvider = new SubJourneyContextParamProvider();
    subJourneyContextParamProvider.updateSubJourneyValueOnContext(controlCodeSubJourney);
    return resultFunction.apply(controlCodeSubJourney);
  }

  public static CompletionStage<Result> resolveContextToSubJourney(Function<ControlCodeSubJourney, CompletionStage<Result>> resultFunction) {
    SubJourneyContextParamProvider subJourneyContextParamProvider = new SubJourneyContextParamProvider();
    ControlCodeSubJourney controlCodeSubJourney = subJourneyContextParamProvider.getSubJourneyValueFromRequest();
    return resultFunction.apply(controlCodeSubJourney);
  }

}
