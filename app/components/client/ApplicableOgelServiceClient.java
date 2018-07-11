package components.client;

import uk.gov.bis.lite.ogel.api.view.ApplicableOgelView;

import java.util.List;
import java.util.concurrent.CompletionStage;

public interface ApplicableOgelServiceClient {
  CompletionStage<List<ApplicableOgelView>> get(String controlCode, String sourceCountry,
                                                List<String> destinationCountries,
                                                List<String> activityTypes, boolean showHistoricOgel);
}
