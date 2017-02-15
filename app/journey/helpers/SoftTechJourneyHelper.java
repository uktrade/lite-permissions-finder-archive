package journey.helpers;

import com.google.inject.Inject;
import components.services.controlcode.catchall.CatchallControlsServiceClient;
import components.services.controlcode.category.CategoryControlsServiceClient;
import components.services.controlcode.nonexempt.NonExemptControlServiceClient;
import components.services.controlcode.nonexempt.NonExemptControlsServiceResult;
import components.services.controlcode.related.RelatedControlsServiceClient;
import models.GoodsType;
import models.softtech.ApplicableSoftTechControls;
import models.softtech.SoftTechCategory;
import org.apache.commons.lang3.StringUtils;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import uk.gov.bis.lite.controlcode.api.view.ControlCodeFullView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class SoftTechJourneyHelper {

  private final CategoryControlsServiceClient categoryControlsServiceClient;
  private final RelatedControlsServiceClient relatedControlsServiceClient;
  private final CatchallControlsServiceClient catchallControlsServiceClient;
  private final NonExemptControlServiceClient nonExemptControlServiceClient;
  private final HttpExecutionContext httpExecutionContext;

  @Inject
  public SoftTechJourneyHelper(CategoryControlsServiceClient categoryControlsServiceClient,
                               RelatedControlsServiceClient relatedControlsServiceClient,
                               CatchallControlsServiceClient catchallControlsServiceClient,
                               NonExemptControlServiceClient nonExemptControlServiceClient,
                               HttpExecutionContext httpExecutionContext) {
    this.categoryControlsServiceClient = categoryControlsServiceClient;
    this.relatedControlsServiceClient = relatedControlsServiceClient;
    this.catchallControlsServiceClient = catchallControlsServiceClient;
    this.nonExemptControlServiceClient = nonExemptControlServiceClient;
    this.httpExecutionContext = httpExecutionContext;
  }

  public CompletionStage<ApplicableSoftTechControls> checkSoftTechControls(GoodsType goodsType, SoftTechCategory softTechCategory) {
    return categoryControlsServiceClient.get(goodsType, softTechCategory)
        .thenApplyAsync(result -> ApplicableSoftTechControls.fromInt(result.controlCodes.size()), httpExecutionContext.current());
  }


  public CompletionStage<ApplicableSoftTechControls> checkRelatedSoftwareControls(GoodsType goodsType, String controlCode) {
    return relatedControlsServiceClient.get(goodsType, controlCode)
        .thenApplyAsync(result -> ApplicableSoftTechControls.fromInt(result.controlCodes.size()), httpExecutionContext.current());
  }

  public CompletionStage<ApplicableSoftTechControls> checkCatchtallSoftwareControls(GoodsType goodsType, SoftTechCategory softTechCategory) {
    return catchallControlsServiceClient.get(goodsType, softTechCategory)
        .thenApplyAsync(result -> ApplicableSoftTechControls.fromInt(result.controlCodes.size()), httpExecutionContext.current());
  }

  public CompletionStage<ApplicableSoftTechControls> checkNonExemptTechnologyControls() {
    CompletionStage<NonExemptControlsServiceResult> specialMaterialsStage = nonExemptControlServiceClient.get(GoodsType.TECHNOLOGY, SoftTechCategory.SPECIAL_MATERIALS);
    CompletionStage<NonExemptControlsServiceResult> marineStage = nonExemptControlServiceClient.get(GoodsType.TECHNOLOGY, SoftTechCategory.MARINE);
    return specialMaterialsStage.thenCombineAsync(marineStage, (specialMaterialsResult, marineResult) -> {
      List<ControlCodeFullView> controlCodes = new ArrayList<>(specialMaterialsResult.controlCodes);
      controlCodes.addAll(marineResult.controlCodes);
      return ApplicableSoftTechControls.fromInt(controlCodes.size());
    }, httpExecutionContext.current());
  }

  // TODO remove this function
  public static CompletionStage<Result> validateGoodsTypeAndGetResult(String goodsTypeText, Function<GoodsType, CompletionStage<Result>> resultFunc) {
    if (StringUtils.isNotEmpty(goodsTypeText)) {
      GoodsType goodsType = GoodsType.valueOf(goodsTypeText.toUpperCase());
      if (goodsType == GoodsType.SOFTWARE || goodsType == GoodsType.TECHNOLOGY) {
        return resultFunc.apply(goodsType);
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
}
