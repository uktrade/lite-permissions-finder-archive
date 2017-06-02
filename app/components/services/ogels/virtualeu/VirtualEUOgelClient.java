package components.services.ogels.virtualeu;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.logging.CorrelationId;
import components.common.logging.ServiceClientLogger;
import exceptions.ServiceException;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import uk.gov.bis.lite.ogel.api.view.VirtualEuView;

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

  public CompletionStage<VirtualEuView> sendServiceRequest(String controlCode, String sourceCountry, List<String> destinationCountries){
    WSRequest request = wsClient.url(webServiceUrl)
        .withRequestFilter(CorrelationId.requestFilter)
        .withRequestFilter(ServiceClientLogger.requestFilter("OGEL", "GET", httpExecutionContext))
        .setRequestTimeout(webServiceTimeout)
        .setQueryParameter("controlCode", controlCode)
        .setQueryParameter("sourceCountry", sourceCountry);

    destinationCountries.forEach(country -> request.setQueryParameter("destinationCountry", country));

    return request.get()
        .handleAsync((response, error) -> {
          if (error != null) {
            throw new ServiceException("OGEL service request failed", error);
          }
          else if (response.getStatus() != 200) {
            throw new ServiceException(String.format("Unexpected HTTP status code from OGEL service /virtual-eu: %s",
                response.getStatus()));
          }
          else {
            return Json.fromJson(response.asJson(), VirtualEuView.class);
          }
        }, httpExecutionContext.current());
  }

}
