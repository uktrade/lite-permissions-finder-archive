package journey;

import com.google.inject.Inject;
import components.common.journey.Decider;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.ControlCodeData;
import components.services.controlcode.FrontendServiceClient;
import models.controlcode.ControlCodeJourney;
import play.Logger;

import java.util.Collection;
import java.util.EnumSet;
import java.util.concurrent.CompletionStage;

public class ControlCodeDecider implements Decider<Collection<ControlCodeDecider.ControlCodeDataType>> {

  public enum ControlCodeDataType {
    TECHNICAL_NOTES, DECONTROLS, ADDITIONAL_SPECS;
  }

  private final PermissionsFinderDao dao;
  private final FrontendServiceClient client;

  @Inject
  public ControlCodeDecider(PermissionsFinderDao dao, FrontendServiceClient client) {
    this.dao = dao;
    this.client = client;
  }

  @Override
  public CompletionStage<Collection<ControlCodeDataType>> decide() {

    //TODO this needs improving
    String controlCode = dao.getSelectedControlCode(ControlCodeJourney.PHYSICAL_GOODS_SEARCH);

    return client.get(controlCode).thenApply(e -> containsData(e.controlCodeData));
  }

  private static Collection<ControlCodeDataType> containsData(ControlCodeData data) {
    Collection<ControlCodeDataType> r = EnumSet.noneOf(ControlCodeDataType.class);

    if (data.canShowDecontrols()) {
      r.add(ControlCodeDataType.DECONTROLS);
    }
    if (data.canShowTechnicalNotes()) {
      r.add(ControlCodeDataType.TECHNICAL_NOTES);
    }
    if (data.canShowAdditionalSpecifications()) {
      r.add(ControlCodeDataType.ADDITIONAL_SPECS);
    }

    Logger.info("Decision for {} = {}", data.controlCode, r);

    return r;
  }

}
