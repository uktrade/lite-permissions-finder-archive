package components.services.controlcode.controls.related;

import com.google.common.net.UrlEscapers;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.logging.CorrelationId;
import models.software.SoftwareCategory;
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
    this.webServiceUrl = webServiceAddress + "/related-controls";
  }

  public CompletionStage<RelatedControlsServiceResult> get(SoftwareCategory softwareCategory, String controlCode) {
    return wsClient.url(webServiceUrl + "/" + UrlEscapers.urlFragmentEscaper().escape(softwareCategory.toString() + "/" +
        UrlEscapers.urlFragmentEscaper().escape(controlCode)))
        .withRequestFilter(CorrelationId.requestFilter)
        .setRequestTimeout(webServiceTimeout)
        .get()
        .thenApplyAsync(response -> (RelatedControlsServiceResult) null, httpExecutionContext.current());
  }

  public CompletionStage<RelatedControlsServiceResult> get(SoftwareCategory softwareCategory, String controlCode, int count) {
    return wsClient.url(webServiceUrl + "/" + UrlEscapers.urlFragmentEscaper().escape(softwareCategory.toString() + "/" +
        UrlEscapers.urlFragmentEscaper().escape(controlCode)))
        .withRequestFilter(CorrelationId.requestFilter)
        .setRequestTimeout(webServiceTimeout)
        .get()
        .thenApplyAsync(response -> new RelatedControlsServiceResult(count), httpExecutionContext.current());
  }
}
