package journey.deciders.relatedcodes;

import com.google.inject.Inject;
import components.common.journey.Decider;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.category.CategoryControlsServiceClient;
import journey.SubJourneyContextParamProvider;
import models.GoodsType;
import models.controlcode.ControlCodeSubJourney;
import models.softtech.SoftTechCategory;
import play.libs.concurrent.HttpExecutionContext;

import java.util.concurrent.CompletionStage;

public class CategoryRelatedControlsDecider implements Decider<Boolean> {
  private final PermissionsFinderDao dao;
  private final SubJourneyContextParamProvider subJourneyContextParamProvider;
  private final CategoryControlsServiceClient categoryControlsServiceClient;
  private final HttpExecutionContext httpExecutionContext;

  @Inject
  public CategoryRelatedControlsDecider(PermissionsFinderDao dao,
                                        SubJourneyContextParamProvider subJourneyContextParamProvider,
                                        CategoryControlsServiceClient categoryControlsServiceClient,
                                        HttpExecutionContext httpExecutionContext) {
    this.dao = dao;
    this.subJourneyContextParamProvider = subJourneyContextParamProvider;
    this.categoryControlsServiceClient = categoryControlsServiceClient;
    this.httpExecutionContext = httpExecutionContext;
  }

  @Override
  public CompletionStage<Boolean> decide() {
    ControlCodeSubJourney subJourney = subJourneyContextParamProvider.getSubJourneyValueFromContext();

    GoodsType goodsType = subJourney.getGoodsType();

    SoftTechCategory softTechCategory = dao.getSoftTechCategory(goodsType).get();

    String resultsControlCode = dao.getSoftTechControlsResultsLastChosenControlCode(subJourney);

    return categoryControlsServiceClient.get(goodsType, softTechCategory)
        .thenApplyAsync(result -> {
          if (result.hasRelatedCodes(resultsControlCode)) {
            return true;
          }
          else {
            dao.clearAndUpdateControlCodeSubJourneyDaoFieldsIfChanged(subJourney, resultsControlCode);
            return false;
          }
        }, httpExecutionContext.current());
  }
}
