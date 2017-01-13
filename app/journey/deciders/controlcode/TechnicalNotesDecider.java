package journey.deciders.controlcode;

import com.google.inject.Inject;
import components.common.journey.Decider;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.FrontendServiceClient;
import journey.SubJourneyContextParamProvider;
import models.controlcode.ControlCodeSubJourney;
import play.libs.concurrent.HttpExecutionContext;

import java.util.concurrent.CompletionStage;

public class TechnicalNotesDecider implements Decider<Boolean> {

  private final PermissionsFinderDao dao;
  private final FrontendServiceClient client;
  private final SubJourneyContextParamProvider subJourneyContextParamProvider;
  private final HttpExecutionContext httpExecutionContext;

  @Inject
  public TechnicalNotesDecider(PermissionsFinderDao dao, FrontendServiceClient client, HttpExecutionContext httpExecutionContext) {
    this.dao = dao;
    this.client = client;
    this.subJourneyContextParamProvider = new SubJourneyContextParamProvider();
    this.httpExecutionContext = httpExecutionContext;
  }

  @Override
  public CompletionStage<Boolean> decide() {

    ControlCodeSubJourney controlCodeSubJourney = subJourneyContextParamProvider.getSubJourneyValueFromContext();

    String controlCode = dao.getSelectedControlCode(controlCodeSubJourney);

    return client.get(controlCode).thenApplyAsync(result ->  {
      boolean canShowTechnicalNotes = result.getControlCodeData().canShowTechnicalNotes();
      if (canShowTechnicalNotes) {
        return true;
      }
      else {
        // TODO This is a hack to set the DAO state on leaving the control code sub journey
        if (controlCodeSubJourney.shouldSetDAOStateOnJourneyTransition()) {
          dao.saveControlCodeForRegistration(controlCode);
        }
        return false;
      }
    }, httpExecutionContext.current());
  }
}
