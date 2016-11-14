package components.services.controlcode.category.controls;

import com.google.common.net.UrlEscapers;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.logging.CorrelationId;
import models.software.SoftwareCategory;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;

import java.util.concurrent.ExecutionException;

public class CategoryControlsServiceClient {

  private final HttpExecutionContext httpExecutionContext;
  private final WSClient wsClient;
  private final int webServiceTimeout;
  private final String webServiceUrl;

  @Inject
  public CategoryControlsServiceClient(HttpExecutionContext httpExecutionContext,
                               WSClient wsClient,
                               @Named("controlCodeServiceAddress") String webServiceAddress,
                               @Named("controlCodeServiceTimeout") int webServiceTimeout){
    this.httpExecutionContext = httpExecutionContext;
    this.wsClient = wsClient;
    this.webServiceTimeout = webServiceTimeout;
    this.webServiceUrl = webServiceAddress + "/category-controls";
  }

  public CategoryControlsServiceResult get(SoftwareCategory softwareCategory) {
    try {
      return wsClient.url(webServiceUrl + "/" + UrlEscapers.urlFragmentEscaper().escape(softwareCategory.toString()))
          .withRequestFilter(CorrelationId.requestFilter)
          .setRequestTimeout(webServiceTimeout)
          .get()
          .thenApplyAsync(response -> (CategoryControlsServiceResult) null, httpExecutionContext.current())
          .toCompletableFuture()
          .get();
    }
    catch (InterruptedException|ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  public CategoryControlsServiceResult get(SoftwareCategory softwareCategory, int count) {
    try {
      return wsClient.url(webServiceUrl + "/" + UrlEscapers.urlFragmentEscaper().escape(softwareCategory.toString()))
          .withRequestFilter(CorrelationId.requestFilter)
          .setRequestTimeout(webServiceTimeout)
          .get()
          .thenApplyAsync(response -> new CategoryControlsServiceResult(count), httpExecutionContext.current())
          .toCompletableFuture()
          .get();
    }
    catch (InterruptedException|ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

}
