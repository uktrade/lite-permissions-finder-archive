package components.services.ogels.applicable;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.logging.CorrelationId;
import components.common.logging.ServiceClientLogger;
import exceptions.ServiceException;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import uk.gov.bis.lite.ogel.api.view.ApplicableOgelView;

import java.util.List;
import java.util.concurrent.CompletionStage;

public class ApplicableOgelServiceClient {

  private final HttpExecutionContext httpExecutionContext;
  private final WSClient wsClient;
  private final int webServiceTimeout;
  private final String webServiceUrl;
  private final ServiceClientLogger serviceClientLogger;

  @Inject
  public ApplicableOgelServiceClient(HttpExecutionContext httpExecutionContext,
                                     WSClient wsClient,
                                     @Named("ogelServiceAddress") String webServiceAddress,
                                     @Named("ogelServiceTimeout") int webServiceTimeout,
                                     ServiceClientLogger serviceClientLogger) {
    this.httpExecutionContext = httpExecutionContext;
    this.wsClient = wsClient;
    this.webServiceTimeout = webServiceTimeout;
    this.webServiceUrl = webServiceAddress + "/applicable-ogels";
    this.serviceClientLogger = serviceClientLogger;
  }

  public CompletionStage<ApplicableOgelServiceResult> get(String controlCode, String sourceCountry,
                                                          List<String> destinationCountries, List<String> activityTypes,
                                                          boolean showHistoricOgel){

    WSRequest req = wsClient.url(webServiceUrl)
        .withRequestFilter(CorrelationId.requestFilter)
        .withRequestFilter(serviceClientLogger.requestFilter("OGEL", "GET"))
        .setRequestTimeout(webServiceTimeout)
        .setQueryParameter("controlCode", controlCode)
        .setQueryParameter("sourceCountry", sourceCountry);

    destinationCountries.forEach(country -> req.setQueryParameter("destinationCountry", country));

    activityTypes.forEach(activityType -> req.setQueryParameter("activityType", activityType));

    return req.get().handleAsync((response, error) -> {
      if (error != null) {
        throw new ServiceException("OGEL service request failed", error);
      }
      else if (response.getStatus() != 200) {
        throw new ServiceException(String.format("Unexpected HTTP status code from OGEL service /applicable-ogels: %s",
            response.getStatus()));
      }
      else {
        ApplicableOgelView[] applicableOgelView = Json.fromJson(response.asJson(), ApplicableOgelView[].class);
        return new ApplicableOgelServiceResult(applicableOgelView, showHistoricOgel);
      }
    }, httpExecutionContext.current());
  }


}
