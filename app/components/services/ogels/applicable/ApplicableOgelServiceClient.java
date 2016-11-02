package components.services.ogels.applicable;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.logging.CorrelationId;
import exceptions.ServiceException;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;

import java.util.List;
import java.util.concurrent.CompletionStage;

public class ApplicableOgelServiceClient {

  private final HttpExecutionContext httpExecutionContext;
  private final WSClient wsClient;
  private final int webServiceTimeout;
  private final String webServiceUrl;

  @Inject
  public ApplicableOgelServiceClient(HttpExecutionContext httpExecutionContext,
                                     WSClient wsClient,
                                     @Named("ogelServiceAddress") String webServiceAddress,
                                     @Named("ogelServiceTimeout") int webServiceTimeout) {
    this.httpExecutionContext = httpExecutionContext;
    this.wsClient = wsClient;
    this.webServiceTimeout = webServiceTimeout;
    this.webServiceUrl= webServiceAddress + "/applicable-ogels";
  }

  public CompletionStage<ApplicableOgelServiceResult> get(String controlCode, String sourceCountry,
                                                          List<String> destinationCountries, List<String> activityTypes,
                                                          boolean showHistoricOgel){

    WSRequest req = wsClient.url(webServiceUrl)
        .withRequestFilter(CorrelationId.requestFilter)
        .setRequestTimeout(webServiceTimeout)
        .setQueryParameter("controlCode", controlCode)
        .setQueryParameter("sourceCountry", sourceCountry);

    destinationCountries.forEach(country -> req.setQueryParameter("destinationCountry", country));

    activityTypes.forEach(activityType -> req.setQueryParameter("activityType", activityType));

    return req.get().thenApplyAsync(response -> {
      if (response.getStatus() != 200) {
        throw new ServiceException(String.format("Unexpected HTTP status code from OGEL service /applicable-ogels: %s",
            response.getStatus()));
      }
      else {
        return new ApplicableOgelServiceResult(response.asJson(), showHistoricOgel);
      }
    }, httpExecutionContext.current());
  }


}
