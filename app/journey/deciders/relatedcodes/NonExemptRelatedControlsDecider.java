package journey.deciders.relatedcodes;

import com.google.inject.Inject;
import components.common.journey.Decider;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.nonexempt.NonExemptControlServiceClient;
import journey.SubJourneyContextParamProvider;
import models.GoodsType;
import models.controlcode.ControlCodeSubJourney;
import models.softtech.SoftTechCategory;
import play.libs.concurrent.HttpExecutionContext;

import java.util.concurrent.CompletionStage;

public class NonExemptRelatedControlsDecider implements Decider<Boolean> {
  private final PermissionsFinderDao dao;
  private final SubJourneyContextParamProvider subJourneyContextParamProvider;
  private final NonExemptControlServiceClient nonExemptControlServiceClient;
  private final HttpExecutionContext httpExecutionContext;

  @Inject
  public NonExemptRelatedControlsDecider(PermissionsFinderDao dao,
                                         SubJourneyContextParamProvider subJourneyContextParamProvider,
                                         NonExemptControlServiceClient nonExemptControlServiceClient,
                                         HttpExecutionContext httpExecutionContext) {
    this.dao = dao;
    this.subJourneyContextParamProvider = subJourneyContextParamProvider;
    this.nonExemptControlServiceClient = nonExemptControlServiceClient;
    this.httpExecutionContext = httpExecutionContext;
  }

  @Override
  public CompletionStage<Boolean> decide() {
    ControlCodeSubJourney subJourney = subJourneyContextParamProvider.getSubJourneyValueFromContext();

    GoodsType goodsType = dao.getGoodsType().get();

    SoftTechCategory softTechCategory = dao.getSoftTechCategory(goodsType).get();

    String resultsControlCode = dao.getSoftTechControlsResultsLastChosenControlCode(subJourney);

    return nonExemptControlServiceClient.get(goodsType, softTechCategory)
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
