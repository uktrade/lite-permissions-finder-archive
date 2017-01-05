package journey.deciders;

import com.google.inject.Inject;
import components.common.journey.Decider;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.controls.related.RelatedControlsServiceClient;
import journey.SubJourneyContextParamProvider;
import models.GoodsType;
import models.controlcode.ControlCodeSubJourney;
import models.softtech.ApplicableSoftTechControls;

import java.util.concurrent.CompletionStage;

public class RelatedControlsDecider implements Decider<ApplicableSoftTechControls> {

  private final PermissionsFinderDao dao;
  private final SubJourneyContextParamProvider subJourneyContextParamProvider;
  private final RelatedControlsServiceClient relatedControlsServiceClient;

  @Inject
  public RelatedControlsDecider(PermissionsFinderDao dao,
                                SubJourneyContextParamProvider subJourneyContextParamProvider,
                                RelatedControlsServiceClient relatedControlsServiceClient) {
    this.dao = dao;
    this.subJourneyContextParamProvider = subJourneyContextParamProvider;
    this.relatedControlsServiceClient = relatedControlsServiceClient;
  }

  @SuppressWarnings("OptionalGetWithoutIsPresent")
  @Override
  public CompletionStage<ApplicableSoftTechControls> decide() {

    ControlCodeSubJourney subJourney = subJourneyContextParamProvider.getSubJourneyValueFromContext();

    String controlCode = dao.getSelectedControlCode(subJourney);

    GoodsType goodsType = subJourney.getSoftTechGoodsType();

    return relatedControlsServiceClient.get(goodsType, controlCode)
        .thenApplyAsync(result -> {

          ApplicableSoftTechControls applicableSoftTechControls = ApplicableSoftTechControls.fromInt(result.controlCodes.size());

          // TODO Setting DAO state here is very hacky and needs rethinking
          if (applicableSoftTechControls == ApplicableSoftTechControls.ONE) {

            dao.saveSelectedControlCode(subJourney, result.controlCodes.get(0).controlCode);

          }

          return applicableSoftTechControls;
        });
  }
}
