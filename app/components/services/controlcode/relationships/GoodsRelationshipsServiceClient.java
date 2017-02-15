package components.services.controlcode.relationships;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.logging.CorrelationId;
import exceptions.ServiceException;
import models.GoodsType;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;

import java.util.concurrent.CompletionStage;

public class GoodsRelationshipsServiceClient {
  private final HttpExecutionContext httpExecutionContext;
  private final WSClient wsClient;
  private final int webServiceTimeout;
  private final String webServiceUrl;

  @Inject
  public GoodsRelationshipsServiceClient(HttpExecutionContext httpExecutionContext,
                                         WSClient wsClient,
                                         @Named("controlCodeServiceAddress") String webServiceAddress,
                                         @Named("controlCodeServiceTimeout") int webServiceTimeout){
    this.httpExecutionContext = httpExecutionContext;
    this.wsClient = wsClient;
    this.webServiceTimeout = webServiceTimeout;
    this.webServiceUrl = webServiceAddress + "/goods-relationships";
  }

  public CompletionStage<GoodsRelationshipsServiceResult> get(GoodsType goodsType, GoodsType relatedToGoodsType) {
    if (goodsType != GoodsType.SOFTWARE && goodsType != GoodsType.TECHNOLOGY) {
      throw new RuntimeException(String.format("Unexpected member of GoodsType enum: \"%s\" for parameter goodsType", goodsType.toString()));
    }

    if (relatedToGoodsType != GoodsType.SOFTWARE && relatedToGoodsType != GoodsType.TECHNOLOGY) {
      throw new RuntimeException(String.format("Unexpected member of GoodsType enum: \"%s\" for parameter relatedToGoodsType", relatedToGoodsType.toString()));
    }

    String url = webServiceUrl + "/" + goodsType.urlString() + "/for/" + relatedToGoodsType.urlString();

    return wsClient.url(url)
        .withRequestFilter(CorrelationId.requestFilter)
        .setRequestTimeout(webServiceTimeout)
        .get()
        .thenApplyAsync(response -> {
          if (response.getStatus() != 200 ) {
            String errorMessage = response.asJson() != null ? errorMessage = response.asJson().get("message").asText() : "";
            throw new ServiceException(String.format("Unexpected HTTP status code from Control Code service /goods-relationships: %s %s", response.getStatus(), errorMessage));
          }
          else {
            return new GoodsRelationshipsServiceResult(response.asJson());
          }
        }, httpExecutionContext.current());
  }
}
