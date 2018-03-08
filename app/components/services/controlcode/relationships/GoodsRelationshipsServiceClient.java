package components.services.controlcode.relationships;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.logging.CorrelationId;
import components.common.logging.ServiceClientLogger;
import exceptions.ServiceException;
import models.GoodsType;
import models.softtech.SoftTechCategory;
import org.apache.commons.lang3.StringUtils;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

public class GoodsRelationshipsServiceClient {
  private final HttpExecutionContext httpExecutionContext;
  private final WSClient wsClient;
  private final int webServiceTimeout;
  private final String webServiceUrl;
  private final String credentials;

  @Inject
  public GoodsRelationshipsServiceClient(HttpExecutionContext httpExecutionContext,
                                         WSClient wsClient,
                                         @Named("controlCodeServiceAddress") String webServiceAddress,
                                         @Named("controlCodeServiceTimeout") int webServiceTimeout,
                                         @Named("controlCodeServiceCredentials") String credentials) {
    this.httpExecutionContext = httpExecutionContext;
    this.wsClient = wsClient;
    this.webServiceTimeout = webServiceTimeout;
    this.webServiceUrl = webServiceAddress + "/goods-relationships";
    this.credentials = credentials;
  }

  public CompletionStage<GoodsRelationshipsServiceResult> get(GoodsType goodsType, GoodsType relatedToGoodsType, SoftTechCategory softTechCategory) {

    if (goodsType != GoodsType.SOFTWARE && goodsType != GoodsType.TECHNOLOGY) {
      throw new RuntimeException(String.format("Unexpected member of GoodsType enum: \"%s\" for parameter goodsType", goodsType.toString()));
    }

    if (relatedToGoodsType != GoodsType.SOFTWARE && relatedToGoodsType != GoodsType.TECHNOLOGY) {
      throw new RuntimeException(String.format("Unexpected member of GoodsType enum: \"%s\" for parameter relatedToGoodsType", relatedToGoodsType.toString()));
    }

    StringBuilder url = new StringBuilder(webServiceUrl + "/" + goodsType.urlString() + "/for/" + relatedToGoodsType.urlString());

    if (softTechCategory.isDualUseSoftTechCategory()) {
      url.append("/dual-use");
      String categoryUrl = softTechCategory.toUrlString();
      if (StringUtils.isNotEmpty(categoryUrl)) {
        url.append("/" + categoryUrl);
      }
    } else {
      url.append("/military");
    }

    return wsClient.url(url.toString())
        .setAuth(credentials)
        .setRequestFilter(CorrelationId.requestFilter)
        .setRequestFilter(ServiceClientLogger.requestFilter("Control Code", "GET", httpExecutionContext))
        .setRequestTimeout(Duration.ofMillis(webServiceTimeout))
        .get()
        .handleAsync((response, error) -> {
          if (error != null) {
            throw new ServiceException("Control Code service request failed", error);
          } else if (response.getStatus() != 200) {
            String errorMessage = response.asJson() != null ? response.asJson().get("message").asText() : "";
            throw new ServiceException(String.format("Unexpected HTTP status code from Control Code service /goods-relationships: %s %s", response.getStatus(), errorMessage));
          } else {
            return new GoodsRelationshipsServiceResult(response.asJson());
          }
        }, httpExecutionContext.current());
  }
}
