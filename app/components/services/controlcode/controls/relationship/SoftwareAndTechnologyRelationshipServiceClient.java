package components.services.controlcode.controls.relationship;

import com.google.common.net.UrlEscapers;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.logging.CorrelationId;
import models.softtech.SoftTechCategory;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;

import java.util.concurrent.CompletionStage;

public class SoftwareAndTechnologyRelationshipServiceClient {

  private final HttpExecutionContext httpExecutionContext;
  private final WSClient wsClient;
  private final int webServiceTimeout;
  private final String webServiceUrl;

  @Inject
  public SoftwareAndTechnologyRelationshipServiceClient(HttpExecutionContext httpExecutionContext,
                                                        WSClient wsClient,
                                                        @Named("controlCodeServiceAddress") String webServiceAddress,
                                                        @Named("controlCodeServiceTimeout") int webServiceTimeout){
    this.httpExecutionContext = httpExecutionContext;
    this.wsClient = wsClient;
    this.webServiceTimeout = webServiceTimeout;
    this.webServiceUrl = webServiceAddress + "/related-controls";
  }

  public CompletionStage<SoftwareAndTechnologyRelationshipServiceResult> get(SoftTechCategory softTechCategory) {
    return wsClient.url(webServiceUrl + "/" + UrlEscapers.urlFragmentEscaper().escape(softTechCategory.toString()))
        .withRequestFilter(CorrelationId.requestFilter)
        .setRequestTimeout(webServiceTimeout)
        .get()
        .thenApplyAsync(response -> (SoftwareAndTechnologyRelationshipServiceResult) null, httpExecutionContext.current());
  }

  public CompletionStage<SoftwareAndTechnologyRelationshipServiceResult> get(SoftTechCategory softTechCategory, boolean relationshipExists) {
      return wsClient.url(webServiceUrl + "/" + UrlEscapers.urlFragmentEscaper().escape(softTechCategory.toString()))
        .withRequestFilter(CorrelationId.requestFilter)
        .setRequestTimeout(webServiceTimeout)
        .get()
        .thenApplyAsync(response -> new SoftwareAndTechnologyRelationshipServiceResult(relationshipExists), httpExecutionContext.current());
  }
}
