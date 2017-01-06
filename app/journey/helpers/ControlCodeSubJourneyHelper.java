package journey.helpers;

import journey.SubJourneyContextParamProvider;
import models.GoodsType;
import models.controlcode.ControlCodeSubJourney;
import org.apache.commons.lang3.StringUtils;
import play.mvc.Result;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class ControlCodeSubJourneyHelper {

  public static CompletionStage<Result> resolveUrlToSubJourneyAndUpdateContext(String controlCodeVariantText, String goodsTypeText, Function<ControlCodeSubJourney, CompletionStage<Result>> resultFunction) {
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    ControlCodeSubJourney controlCodeSubJourney = models.controlcode.ControlCodeSubJourney.getMatched(controlCodeVariantText, goodsTypeText).get();
    SubJourneyContextParamProvider subJourneyContextParamProvider = new SubJourneyContextParamProvider();
    subJourneyContextParamProvider.updateSubJourneyValueOnContext(controlCodeSubJourney);
    return resultFunction.apply(controlCodeSubJourney);
  }

  public static CompletionStage<Result> resolveContextToSubJourney(Function<ControlCodeSubJourney, CompletionStage<Result>> resultFunction) {
    SubJourneyContextParamProvider subJourneyContextParamProvider = new SubJourneyContextParamProvider();
    ControlCodeSubJourney controlCodeSubJourney = subJourneyContextParamProvider.getSubJourneyValueFromRequest();
    return resultFunction.apply(controlCodeSubJourney);
  }

  // TODO remove this function
  private static CompletionStage<Result> validateGoodsTypeAndGetResult(String goodsTypeText,
                                                                       ControlCodeSubJourney softwareJourney,
                                                                       ControlCodeSubJourney technologyJourney,
                                                                       Function<ControlCodeSubJourney, CompletionStage<Result>> resultFunc) {
    if (StringUtils.isNotEmpty(goodsTypeText)) {
      GoodsType goodsType = GoodsType.valueOf(goodsTypeText.toUpperCase());
      if (goodsType == GoodsType.SOFTWARE) {
        return resultFunc.apply(softwareJourney);
      }
      else if (goodsType == GoodsType.TECHNOLOGY) {
        return resultFunc.apply(technologyJourney);
      }
      else {
        throw new RuntimeException(String.format("Unexpected member of GoodsType enum: \"%s\""
            , goodsType.toString()));
      }
    }
    else {
      throw new RuntimeException(String.format("Expected goodsTypeText to not be empty"));
    }
  }

  // TODO remove this function
  public static CompletionStage<Result> getSearchRelatedToPhysicalGoodsResult(String goodsTypeText, Function<ControlCodeSubJourney, CompletionStage<Result>> resultFunc) {
    return validateGoodsTypeAndGetResult(goodsTypeText, ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE,
        ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_TECHNOLOGY, resultFunc);
  }

}
