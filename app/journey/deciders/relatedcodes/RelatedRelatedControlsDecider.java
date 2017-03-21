package journey.deciders.relatedcodes;

import com.google.inject.Inject;
import components.common.journey.Decider;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.related.RelatedControlsServiceClient;
import journey.SubJourneyContextParamProvider;
import models.GoodsType;
import models.controlcode.ControlCodeSubJourney;
import play.libs.concurrent.HttpExecutionContext;

import java.util.concurrent.CompletionStage;

public class RelatedRelatedControlsDecider implements Decider<Boolean> {
  private final PermissionsFinderDao dao;
  private final SubJourneyContextParamProvider subJourneyContextParamProvider;
  private final RelatedControlsServiceClient relatedControlsServiceClient;
  private final HttpExecutionContext httpExecutionContext;

  @Inject
  public RelatedRelatedControlsDecider(PermissionsFinderDao dao,
                                       SubJourneyContextParamProvider subJourneyContextParamProvider,
                                       RelatedControlsServiceClient relatedControlsServiceClient,
                                       HttpExecutionContext httpExecutionContext) {
    this.dao = dao;
    this.subJourneyContextParamProvider = subJourneyContextParamProvider;
    this.relatedControlsServiceClient = relatedControlsServiceClient;
    this.httpExecutionContext = httpExecutionContext;
  }

  @Override
  public CompletionStage<Boolean> decide() {
    // This decision requires the list of results from the prior 'search' sub journey, this is derived from the current
    // sub journey's goods type. This hard coded journey selection is ok as this decider is only used at a single fixed
    // point in the software and technology flow.
    ControlCodeSubJourney currentSubJourney = subJourneyContextParamProvider.getSubJourneyValueFromContext();

    GoodsType goodsType = currentSubJourney.getGoodsType();

    ControlCodeSubJourney priorSubJourney = ControlCodeSubJourney.getPhysicalGoodsSearchVariant(goodsType);

    String priorControlCode = dao.getSelectedControlCode(priorSubJourney);

    String resultsControlCode = dao.getSoftTechControlsResultsLastChosenControlCode(currentSubJourney);

    return relatedControlsServiceClient.get(goodsType, priorControlCode)
        .thenApplyAsync(result -> {
          if (result.hasRelatedCodes(resultsControlCode)) {
            return true;
          }
          else {
            dao.clearAndUpdateControlCodeSubJourneyDaoFieldsIfChanged(currentSubJourney, resultsControlCode);
            return false;
          }
        }, httpExecutionContext.current());
  }
}
