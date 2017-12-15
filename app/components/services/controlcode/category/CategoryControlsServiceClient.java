package components.services.controlcode.category;

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

public class CategoryControlsServiceClient {
  private final HttpExecutionContext httpExecutionContext;
  private final WSClient wsClient;
  private final int webServiceTimeout;
  private final String webServiceUrl;
  private final String credentials;

  @Inject
  public CategoryControlsServiceClient(HttpExecutionContext httpExecutionContext,
                                       WSClient wsClient,
                                       @Named("controlCodeServiceAddress") String webServiceAddress,
                                       @Named("controlCodeServiceTimeout") int webServiceTimeout,
                                       @Named("controlCodeServiceCredentials") String credentials) {
    this.httpExecutionContext = httpExecutionContext;
    this.wsClient = wsClient;
    this.webServiceTimeout = webServiceTimeout;
    this.webServiceUrl = webServiceAddress + "/specific-controls";
    this.credentials = credentials;
  }

  public CompletionStage<CategoryControlsServiceResult> get(GoodsType goodsType, SoftTechCategory softTechCategory) {
    if (goodsType != GoodsType.SOFTWARE && goodsType != GoodsType.TECHNOLOGY) {
      throw new RuntimeException(String.format("Unexpected member of GoodsType enum: \"%s\"", goodsType.toString()));
    }
    String url;
    if (softTechCategory.isDualUseSoftTechCategory()) {
      url = webServiceUrl + "/" + goodsType.urlString() + "/dual-use/" + softTechCategory.toUrlString();
    } else {
      url = webServiceUrl + "/" + goodsType.urlString() + "/military";
    }
    return wsClient.url(url)
        .setAuth(credentials)
        .withRequestFilter(CorrelationId.requestFilter)
        .withRequestFilter(ServiceClientLogger.requestFilter("Control Code", "GET", httpExecutionContext))
        .setRequestTimeout(webServiceTimeout)
        .get()
        .handleAsync((response, error) -> {
          if (error != null) {
            throw new ServiceException("Control Code service request failed", error);
          } else if (response.getStatus() != 200) {
            String errorMessage = response.asJson() != null ? response.asJson().get("message").asText() : "";
            throw new ServiceException(String.format("Unexpected HTTP status code from Control Code service /specific" +
                "-controls: %s %s", response.getStatus(), errorMessage));
          } else {
            return new CategoryControlsServiceResult(response.asJson());
          }
        }, httpExecutionContext.current());
  }

}
