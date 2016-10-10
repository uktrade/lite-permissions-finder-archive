package components.services.controlcode.frontend;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import exceptions.ServiceException;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;

import java.util.concurrent.CompletionStage;

public class FrontendServiceClient {

  private final HttpExecutionContext httpExecutionContext;
  private final WSClient ws;
  private final String webServiceHost;
  private final String webServicePort;
  private final int webServiceTimeout;
  private final String webServiceUrl;

  @Inject
  public FrontendServiceClient(HttpExecutionContext httpExecutionContext,
                               WSClient ws,
                               @Named("controlCodeFrontendServiceHost") String webServiceHost,
                               @Named("controlCodeFrontendServicePort") String webServicePort,
                               @Named("controlCodeFrontendServiceTimeout") int webServiceTimeout){
    this.httpExecutionContext = httpExecutionContext;
    this.ws = ws;
    this.webServiceHost = webServiceHost;
    this.webServicePort = webServicePort;
    this.webServiceTimeout = webServiceTimeout;
    this.webServiceUrl = "http://" + webServiceHost + ":" + webServicePort + "/frontend-control-codes";
  }

  public CompletionStage<FrontendServiceResult> get(String controlCode) {
    return ws.url(webServiceUrl + "/" + controlCode)
        .setRequestTimeout(webServiceTimeout)
        .withRequestFilter(CorrelationId.requestFilter)
        .get()
        .thenApplyAsync(response -> {
          if (response.getStatus() != 200) {
            String errorMessage = response.asJson() != null ? errorMessage = response.asJson().get("message").asText() : "";
            throw new ServiceException(String.format("Unexpected HTTP status code from ControlCodeFrontendService: %s %s", response.getStatus(), errorMessage));
          }
          else {
            return Json.fromJson(response.asJson(), FrontendServiceResult.class);
          }
       }, httpExecutionContext.current());
  }

}