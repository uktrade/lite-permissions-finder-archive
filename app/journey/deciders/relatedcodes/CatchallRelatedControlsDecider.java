package journey.deciders.relatedcodes;

import com.google.inject.Inject;
import components.common.journey.Decider;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.catchall.CatchallControlsServiceClient;
import journey.SubJourneyContextParamProvider;
import models.GoodsType;
import models.controlcode.ControlCodeSubJourney;
import models.softtech.SoftTechCategory;
import play.libs.concurrent.HttpExecutionContext;

import java.util.concurrent.CompletionStage;

public class CatchallRelatedControlsDecider implements Decider<Boolean> {
  private final PermissionsFinderDao dao;
  private final SubJourneyContextParamProvider subJourneyContextParamProvider;
  private final CatchallControlsServiceClient catchallControlsServiceClient;
  private final HttpExecutionContext httpExecutionContext;

  @Inject
  public CatchallRelatedControlsDecider(PermissionsFinderDao dao,
                                        SubJourneyContextParamProvider subJourneyContextParamProvider,
                                        CatchallControlsServiceClient catchallControlsServiceClient,
                                        HttpExecutionContext httpExecutionContext) {
    this.dao = dao;
    this.subJourneyContextParamProvider = subJourneyContextParamProvider;
    this.catchallControlsServiceClient = catchallControlsServiceClient;
    this.httpExecutionContext = httpExecutionContext;
  }

  @Override
  public CompletionStage<Boolean> decide() {
    ControlCodeSubJourney subJourney = subJourneyContextParamProvider.getSubJourneyValueFromContext();

    GoodsType goodsType = dao.getGoodsType().get();

    SoftTechCategory softTechCategory = dao.getSoftTechCategory(goodsType).get();

    String resultsControlCode = dao.getSoftTechControlsResultsLastChosenControlCode(subJourney);

    return catchallControlsServiceClient.get(goodsType, softTechCategory)
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
