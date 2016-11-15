package journey;

import com.google.inject.Inject;
import components.services.controlcode.category.controls.CategoryControlsServiceClient;
import models.software.ApplicableSoftwareControls;
import models.software.SoftwareCategory;
import play.libs.concurrent.HttpExecutionContext;

import java.util.concurrent.CompletionStage;

public class SoftwareJourneyHelper {

  private final CategoryControlsServiceClient categoryControlsServiceClient;
  private final HttpExecutionContext httpExecutionContext;

  @Inject
  public SoftwareJourneyHelper(CategoryControlsServiceClient categoryControlsServiceClient,
                               HttpExecutionContext httpExecutionContext) {
    this.categoryControlsServiceClient = categoryControlsServiceClient;
    this.httpExecutionContext = httpExecutionContext;
  }

  public CompletionStage<ApplicableSoftwareControls> checkSoftwareControls(SoftwareCategory softwareCategory) {
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
}
