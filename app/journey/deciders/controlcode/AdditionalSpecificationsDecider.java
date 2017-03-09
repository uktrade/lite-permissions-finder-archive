package journey.deciders.controlcode;

import com.google.inject.Inject;
import components.common.journey.Decider;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.frontend.FrontendServiceClient;
import components.services.controlcode.frontend.FrontendServiceResult;
import journey.SubJourneyContextParamProvider;
import models.controlcode.ControlCodeSubJourney;
import play.libs.concurrent.HttpExecutionContext;

import java.util.concurrent.CompletionStage;

public class AdditionalSpecificationsDecider implements Decider<Boolean> {

  private final PermissionsFinderDao dao;
  private final FrontendServiceClient client;
  private final SubJourneyContextParamProvider subJourneyContextParamProvider;
  private final HttpExecutionContext httpExecutionContext;

  @Inject
  public AdditionalSpecificationsDecider(PermissionsFinderDao dao, FrontendServiceClient client, HttpExecutionContext httpExecutionContext) {
    this.dao = dao;
    this.client = client;
    this.subJourneyContextParamProvider = new SubJourneyContextParamProvider();
    this.httpExecutionContext = httpExecutionContext;
  }

  @Override
  public CompletionStage<Boolean> decide() {

    ControlCodeSubJourney controlCodeSubJourney = subJourneyContextParamProvider.getSubJourneyValueFromContext();

    String controlCode = dao.getSelectedControlCode(controlCodeSubJourney);

    return client.get(controlCode)
        .thenApplyAsync(FrontendServiceResult::canShowAdditionalSpecifications, httpExecutionContext.current());
  }
}
