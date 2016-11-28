package components.services.controlcode.controls.related;

import com.google.common.net.UrlEscapers;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.logging.CorrelationId;
import exceptions.ServiceException;
import models.GoodsType;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;

import java.util.concurrent.CompletionStage;

public class RelatedControlsServiceClient {

  private final HttpExecutionContext httpExecutionContext;
  private final WSClient wsClient;
  private final int webServiceTimeout;
  private final String webServiceUrl;

  @Inject
  public RelatedControlsServiceClient(HttpExecutionContext httpExecutionContext,
                                       WSClient wsClient,
                                       @Named("controlCodeServiceAddress") String webServiceAddress,
                                       @Named("controlCodeServiceTimeout") int webServiceTimeout){
    this.httpExecutionContext = httpExecutionContext;
    this.wsClient = wsClient;
    this.webServiceTimeout = webServiceTimeout;
    this.webServiceUrl = webServiceAddress + "/mapped-controls";
  }

  public CompletionStage<RelatedControlsServiceResult> get(GoodsType goodsType, String controlCode) {
    if (goodsType != GoodsType.SOFTWARE && goodsType != GoodsType.TECHNOLOGY) {
      throw new RuntimeException(String.format("Unexpected member of GoodsType enum: \"%s\"", goodsType.toString()));
    }
    String url = webServiceUrl + "/" + goodsType.toUrlString() +  "/" + UrlEscapers.urlFragmentEscaper().escape(controlCode);
    return wsClient.url(url)
        .withRequestFilter(CorrelationId.requestFilter)
        .setRequestTimeout(webServiceTimeout)
        .get()
        .thenApplyAsync(response -> {
          if (response.getStatus() != 200) {
            String errorMessage = response.asJson() != null ? errorMessage = response.asJson().get("message").asText() : "";
            throw new ServiceException(String.format("Unexpected HTTP status code from Control Code service /mapped-" +
                "controls: %s %s", response.getStatus(), errorMessage));
          }
          else {
            return new RelatedControlsServiceResult(response.asJson());
          }
        }, httpExecutionContext.current());
  }
}
