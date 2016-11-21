package journey.helpers;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.controls.catchall.CatchallControlsServiceClient;
import components.services.controlcode.controls.category.CategoryControlsServiceClient;
import components.services.controlcode.controls.related.RelatedControlsServiceClient;
import components.services.controlcode.controls.relationship.SoftwareAndTechnologyRelationshipServiceClient;
import journey.Events;
import models.controlcode.ControlCodeJourney;
import models.software.ApplicableSoftwareControls;
import models.software.CatchallSoftwareControlsFlow;
import models.software.SoftwareCategory;
import org.apache.commons.lang3.StringUtils;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;

import java.util.concurrent.CompletionStage;

public class SoftwareJourneyHelper {

  private final CategoryControlsServiceClient categoryControlsServiceClient;
  private final RelatedControlsServiceClient relatedControlsServiceClient;
  private final CatchallControlsServiceClient catchallControlsServiceClient;
  private final SoftwareAndTechnologyRelationshipServiceClient softwareAndTechnologyRelationshipServiceClient;
  private final PermissionsFinderDao permissionsFinderDao;
  private final HttpExecutionContext httpExecutionContext;
  private final JourneyManager journeyManager;

  @Inject
  public SoftwareJourneyHelper(CategoryControlsServiceClient categoryControlsServiceClient,
                               RelatedControlsServiceClient relatedControlsServiceClient,
                               CatchallControlsServiceClient catchallControlsServiceClient,
                               SoftwareAndTechnologyRelationshipServiceClient softwareAndTechnologyRelationshipServiceClient,
                               PermissionsFinderDao permissionsFinderDao,
                               HttpExecutionContext httpExecutionContext,
                               JourneyManager journeyManager) {
    this.categoryControlsServiceClient = categoryControlsServiceClient;
    this.relatedControlsServiceClient = relatedControlsServiceClient;
    this.catchallControlsServiceClient = catchallControlsServiceClient;
    this.softwareAndTechnologyRelationshipServiceClient = softwareAndTechnologyRelationshipServiceClient;
    this.permissionsFinderDao = permissionsFinderDao;
    this.httpExecutionContext = httpExecutionContext;
    this.journeyManager = journeyManager;
  }

  /**
   * Check for software controls of the given software category
   * Note: Writes to DAO if ONE would be returns and saveToDao is true. This is a small shortcut, preventing a
   * separate call to CategoryControlsServiceClient request the single control code to save.
   * @param softwareCategory The software category to check the controls of
   * @param saveToDao Whether to save the single control code to the DAO, if a single control code were to be returned
   *                  by the CategoryControlsServiceClient request
   * @return The applicable software controls
   */
  public CompletionStage<ApplicableSoftwareControls> checkSoftwareControls(SoftwareCategory softwareCategory, boolean saveToDao) {
    // Count is specific to stubbed CategoryControlsServiceClient
    int count =
        softwareCategory == SoftwareCategory.MILITARY ? 0
            : softwareCategory == SoftwareCategory.DUMMY ? 1
            : softwareCategory == SoftwareCategory.RADIOACTIVE ? 2
            : 0;

    return categoryControlsServiceClient.get(softwareCategory, count)
        .thenApplyAsync(result -> {
          int size = result.controlCodes.size();
          if (size == 0) {
            return ApplicableSoftwareControls.ZERO;
          }
          else if (size == 1) {
            // Saving to the DAO here prevents a separate call to the CategoryControlsServiceClient, if not a little hacky
            if (saveToDao) {
              // TODO fit in controlCodeJourneyHelper.clearControlCodeJourneyDaoFieldsIfChanged(ControlCodeJourney.SOFTWARE_CONTROLS, "PL9009a2g");
              // TODO for now, duplicate code found in controlCodeJourneyHelper.clearControlCodeJourneyDaoFieldsIfChanged
              if (StringUtils.equals("PL9009a2g", permissionsFinderDao.getSelectedControlCode(ControlCodeJourney.SOFTWARE_CONTROLS))) {
                permissionsFinderDao.saveSelectedControlCode(ControlCodeJourney.SOFTWARE_CONTROLS, "PL9009a2g");
                permissionsFinderDao.clearControlCodeApplies(ControlCodeJourney.SOFTWARE_CONTROLS);
                permissionsFinderDao.clearControlCodeDecontrolsApply(ControlCodeJourney.SOFTWARE_CONTROLS);
                permissionsFinderDao.clearControlCodeAdditionalSpecificationsApply(ControlCodeJourney.SOFTWARE_CONTROLS);
                permissionsFinderDao.clearControlCodeTechnicalNotesApply(ControlCodeJourney.SOFTWARE_CONTROLS);
              }
            }
            return ApplicableSoftwareControls.ONE;
          }
          else if (size > 1) {
            return ApplicableSoftwareControls.GREATER_THAN_ONE;
          }
          else {
            throw new RuntimeException(String.format("Invalid value for size: \"%d\"", size));
          }
        }, httpExecutionContext.current());
  }

  /**
   * Check for software controls of the given software category
   * @param softwareCategory The software category to check the controls of
   * @return The applicable software controls
   */
  public CompletionStage<ApplicableSoftwareControls> checkSoftwareControls(SoftwareCategory softwareCategory) {
    return checkSoftwareControls(softwareCategory, false);
  }


  public CompletionStage<ApplicableSoftwareControls> checkRelatedSoftwareControls(SoftwareCategory softwareCategory, String controlCode, boolean saveToDao) {
    // Count is specific to stubbed CategoryControlsServiceClient
    int count =
        softwareCategory == SoftwareCategory.MILITARY ? 0
            : softwareCategory == SoftwareCategory.DUMMY ? 1
            : softwareCategory == SoftwareCategory.RADIOACTIVE ? 2
            : 0;

    return relatedControlsServiceClient.get(softwareCategory, controlCode, count)
        .thenApplyAsync(result -> {
          int size = result.controlCodes.size();
          if (size == 0) {
            return ApplicableSoftwareControls.ZERO;
          }
          else if (size == 1) {
            // Saving to the DAO here prevents a separate call to the CategoryControlsServiceClient, if not a little hacky
            if (saveToDao) {
              // TODO fit in controlCodeJourneyHelper.clearControlCodeJourneyDaoFieldsIfChanged(ControlCodeJourney.SOFTWARE_CONTROLS, "PL9009a2g");
              // TODO for now, duplicate code found in controlCodeJourneyHelper.clearControlCodeJourneyDaoFieldsIfChanged
              if (StringUtils.equals("PL9009a2g", permissionsFinderDao.getSelectedControlCode(ControlCodeJourney.SOFTWARE_CONTROLS_RELATED_TO_PHYSICAL_GOODS))) {
                permissionsFinderDao.saveSelectedControlCode(ControlCodeJourney.SOFTWARE_CONTROLS_RELATED_TO_PHYSICAL_GOODS, "PL9009a2g");
                permissionsFinderDao.clearControlCodeApplies(ControlCodeJourney.SOFTWARE_CONTROLS_RELATED_TO_PHYSICAL_GOODS);
                permissionsFinderDao.clearControlCodeDecontrolsApply(ControlCodeJourney.SOFTWARE_CONTROLS_RELATED_TO_PHYSICAL_GOODS);
                permissionsFinderDao.clearControlCodeAdditionalSpecificationsApply(ControlCodeJourney.SOFTWARE_CONTROLS_RELATED_TO_PHYSICAL_GOODS);
                permissionsFinderDao.clearControlCodeTechnicalNotesApply(ControlCodeJourney.SOFTWARE_CONTROLS_RELATED_TO_PHYSICAL_GOODS);
              }
            }
            return ApplicableSoftwareControls.ONE;
          }
          else if (size > 1) {
            return ApplicableSoftwareControls.GREATER_THAN_ONE;
          }
          else {
            throw new RuntimeException(String.format("Invalid value for size: \"%d\"", size));
          }
        }, httpExecutionContext.current());
  }

  public CompletionStage<ApplicableSoftwareControls> checkCatchtallSoftwareControls(SoftwareCategory softwareCategory) {
    // Count is specific to stubbed CategoryControlsServiceClient
    int count =
        softwareCategory == SoftwareCategory.MILITARY ? 0
            : softwareCategory == SoftwareCategory.DUMMY ? 1
            : softwareCategory == SoftwareCategory.RADIOACTIVE ? 2
            : 0;

    return catchallControlsServiceClient.get(softwareCategory, count)
        .thenApplyAsync(result -> {
          int size = result.controlCodes.size();
          if (size == 0) {
            return ApplicableSoftwareControls.ZERO;
          }
          else if (size == 1) {
            return ApplicableSoftwareControls.ONE;
          }
          else if (size > 1) {
            return ApplicableSoftwareControls.GREATER_THAN_ONE;
          }
          else {
            throw new RuntimeException(String.format("Invalid value for size: \"%d\"", size));
          }
        }, httpExecutionContext.current());
  }

  public CompletionStage<Result> performCatchallSoftwareControlsTransition() {
    SoftwareCategory softwareCategory = permissionsFinderDao.getSoftwareCategory().get();

    // Stubbed relationship for SoftwareAndTechnologyRelationshipServiceClient
    boolean relationshipExists = softwareCategory == SoftwareCategory.MILITARY;

    return checkCatchtallSoftwareControls(softwareCategory)
        .thenComposeAsync(controls -> {
          if (controls == ApplicableSoftwareControls.ZERO) {
            return softwareAndTechnologyRelationshipServiceClient.get(softwareCategory, relationshipExists)
                .thenComposeAsync(result -> {
                  if (result.relationshipExists) {
                    return journeyManager.performTransition(Events.CATCHALL_SOFTWARE_CONTROLS,
                        CatchallSoftwareControlsFlow.RELATIONSHIP_EXISTS);
                  }
                  else {
                    return journeyManager.performTransition(Events.CATCHALL_SOFTWARE_CONTROLS,
                        CatchallSoftwareControlsFlow.RELATIONSHIP_DOES_NOT_EXIST);
                  }
                }, httpExecutionContext.current());
          }
          else if (controls == ApplicableSoftwareControls.ONE) {
            return journeyManager.performTransition(Events.CATCHALL_SOFTWARE_CONTROLS,
                CatchallSoftwareControlsFlow.CATCHALL_ONE);
          }
          else if (controls == ApplicableSoftwareControls.GREATER_THAN_ONE) {
            return journeyManager.performTransition(Events.CATCHALL_SOFTWARE_CONTROLS,
                CatchallSoftwareControlsFlow.CATCHALL_GREATER_THAN_ONE);
          }
          else {
            throw new RuntimeException(String.format("Unexpected member of ApplicableSoftwareControls enum: \"%s\""
                , controls.toString()));
          }
        }, httpExecutionContext.current());
  }

}
