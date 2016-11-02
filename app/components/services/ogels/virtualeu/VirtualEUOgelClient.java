package components.services.ogels.virtualeu;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.logging.CorrelationId;
import exceptions.ServiceException;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;

import java.util.List;
import java.util.concurrent.CompletionStage;

public class VirtualEUOgelClient {

  private final HttpExecutionContext httpExecutionContext;
  private final WSClient wsClient;
  private final String webServiceUrl;
  private final int webServiceTimeout;

  @Inject
  public VirtualEUOgelClient(HttpExecutionContext httpExecutionContext,
                             WSClient wsClient,
                             @Named("ogelServiceAddress") String webServiceAddress,
                             @Named("ogelServiceTimeout") int webServiceTimeout) {
    this.httpExecutionContext = httpExecutionContext;
    this.wsClient = wsClient;
    this.webServiceUrl = webServiceAddress + "/virtual-eu";
    this.webServiceTimeout = webServiceTimeout;
  }

  public CompletionStage<VirtualEUOgelResult> sendServiceRequest(String controlCode, String sourceCountry,
                                                                 List<String> destinationCountries, List<String> activityTypes){

    WSRequest request = wsClient.url(webServiceUrl)
        .withRequestFilter(CorrelationId.requestFilter)
        .setRequestTimeout(webServiceTimeout)
        .setQueryParameter("controlCode", controlCode)
        .setQueryParameter("sourceCountry", sourceCountry);

    destinationCountries.forEach(country -> request.setQueryParameter("destinationCountry", country));

    activityTypes.forEach(activityType -> request.setQueryParameter("activityType", activityType));

    return request.get().thenApplyAsync(response -> {
      if (response.getStatus() != 200) {
        throw new ServiceException(String.format("Unexpected HTTP status code from OGEL service /virtual-eu: %s",
            response.getStatus()));
      }
      else {
        return VirtualEUOgelResult.build(response.asJson());
      }
    }, httpExecutionContext.current());
  }

}
