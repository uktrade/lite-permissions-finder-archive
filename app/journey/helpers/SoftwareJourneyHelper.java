package journey.helpers;

import com.google.inject.Inject;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.category.controls.CategoryControlsServiceClient;
import models.controlcode.ControlCodeJourney;
import models.software.ApplicableSoftwareControls;
import models.software.SoftwareCategory;
import org.apache.commons.lang3.StringUtils;
import play.libs.concurrent.HttpExecutionContext;

import java.util.concurrent.CompletionStage;

public class SoftwareJourneyHelper {

  private final CategoryControlsServiceClient categoryControlsServiceClient;
  private final PermissionsFinderDao permissionsFinderDao;
  private final HttpExecutionContext httpExecutionContext;

  @Inject
  public SoftwareJourneyHelper(CategoryControlsServiceClient categoryControlsServiceClient,
                               PermissionsFinderDao permissionsFinderDao,
                               HttpExecutionContext httpExecutionContext) {
    this.categoryControlsServiceClient = categoryControlsServiceClient;
    this.permissionsFinderDao = permissionsFinderDao;
    this.httpExecutionContext = httpExecutionContext;
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

}
