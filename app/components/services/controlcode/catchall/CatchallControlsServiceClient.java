package components.services.controlcode.catchall;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.logging.CorrelationId;
import components.common.logging.ServiceClientLogger;
import exceptions.ServiceException;
import models.GoodsType;
import models.softtech.SoftTechCategory;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;

import java.util.concurrent.CompletionStage;

public class CatchallControlsServiceClient {

  private final HttpExecutionContext httpExecutionContext;
  private final WSClient wsClient;
  private final int webServiceTimeout;
  private final String webServiceUrl;
  private final ServiceClientLogger serviceClientLogger;

  @Inject
  public CatchallControlsServiceClient(HttpExecutionContext httpExecutionContext,
                                       WSClient wsClient,
                                       @Named("controlCodeServiceAddress") String webServiceAddress,
                                       @Named("controlCodeServiceTimeout") int webServiceTimeout,
                                       ServiceClientLogger serviceClientLogger) {
    this.httpExecutionContext = httpExecutionContext;
    this.wsClient = wsClient;
    this.webServiceTimeout = webServiceTimeout;
    this.webServiceUrl = webServiceAddress + "/catch-all-controls";
    this.serviceClientLogger = serviceClientLogger;
  }

  public CompletionStage<CatchallControlsServiceResult> get(GoodsType goodsType, SoftTechCategory softTechCategory) {
    if (goodsType != GoodsType.SOFTWARE && goodsType != GoodsType.TECHNOLOGY) {
      throw new RuntimeException(String.format("Unexpected member of GoodsType enum: \"%s\"", goodsType.toString()));
    }
    String url;
    if (softTechCategory.isDualUseSoftTechCategory()) {
      url = webServiceUrl + "/" + goodsType.urlString() +  "/dual-use";
    }
    else {
      url = webServiceUrl + "/" + goodsType.urlString() +  "/military";
    }
    return wsClient.url(url)
        .withRequestFilter(CorrelationId.requestFilter)
        .withRequestFilter(serviceClientLogger.requestFilter("Control Code", "GET"))
        .setRequestTimeout(webServiceTimeout)
        .get()
        .thenApplyAsync(response -> {
          if (response.getStatus() != 200) {
            String errorMessage = response.asJson() != null ? response.asJson().get("message").asText() : "";
            throw new ServiceException(String.format("Unexpected HTTP status code from Control Code service /specific" +
                "-controls: %s %s", response.getStatus(), errorMessage));
          }
          else {
            return new CatchallControlsServiceResult(response.asJson());
          }
        }, httpExecutionContext.current());
  }
}
