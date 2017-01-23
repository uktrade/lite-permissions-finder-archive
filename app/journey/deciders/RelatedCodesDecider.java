package journey.deciders;

import com.google.inject.Inject;
import components.common.journey.Decider;
import components.persistence.PermissionsFinderDao;
import components.services.search.relatedcodes.RelatedCodesServiceClient;
import journey.SubJourneyContextParamProvider;
import models.controlcode.ControlCodeSubJourney;
import play.libs.concurrent.HttpExecutionContext;

import java.util.concurrent.CompletionStage;

public class RelatedCodesDecider implements Decider<Boolean> {

  private final PermissionsFinderDao dao;
  private final SubJourneyContextParamProvider subJourneyContextParamProvider;
  private final RelatedCodesServiceClient relatedCodesServiceClient;
  private final HttpExecutionContext httpExecutionContext;

  @Inject
  public RelatedCodesDecider(PermissionsFinderDao dao,
                             SubJourneyContextParamProvider subJourneyContextParamProvider,
                             RelatedCodesServiceClient relatedCodesServiceClient,
                             HttpExecutionContext httpExecutionContext) {
    this.dao = dao;
    this.subJourneyContextParamProvider = subJourneyContextParamProvider;
    this.relatedCodesServiceClient = relatedCodesServiceClient;
    this.httpExecutionContext = httpExecutionContext;
  }

  @Override
  public CompletionStage<Boolean> decide() {
    ControlCodeSubJourney subJourney = subJourneyContextParamProvider.getSubJourneyValueFromContext();

    String resultsControlCode = dao.getSearchResultsLastChosenControlCode(subJourney);

    return relatedCodesServiceClient.get(resultsControlCode)
        .thenApplyAsync(result -> {

          if (result.relatedCodes.isEmpty() || (result.relatedCodes.size() == 1 && resultsControlCode.equals(result.relatedCodes.get(0).getControlCode()))) {
            dao.clearAndUpdateControlCodeSubJourneyDaoFieldsIfChanged(subJourney, resultsControlCode);
            return false;
          }
          else {
            return true;
          }
        }, httpExecutionContext.current());
  }
}
