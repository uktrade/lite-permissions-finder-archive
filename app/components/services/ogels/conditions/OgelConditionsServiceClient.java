package components.services.ogels.conditions;

import com.google.common.net.UrlEscapers;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.logging.CorrelationId;
import exceptions.ServiceException;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;
import uk.gov.bis.lite.ogel.api.view.ControlCodeConditionFullView;

import java.util.concurrent.CompletionStage;

public class OgelConditionsServiceClient {

  private final HttpExecutionContext httpExecutionContext;
  private final WSClient wsClient;
  private final int webServiceTimeout;
  private final String webServiceUrl;

  @Inject
  public OgelConditionsServiceClient(HttpExecutionContext httpExecutionContext,
                                     WSClient wsClient,
                                     @Named("ogelServiceAddress") String webServiceAddress,
                                     @Named("ogelServiceTimeout") int webServiceTimeout) {
    this.httpExecutionContext = httpExecutionContext;
    this.wsClient = wsClient;
    this.webServiceTimeout = webServiceTimeout;
    this.webServiceUrl = webServiceAddress + "/control-code-conditions";
  }

  public CompletionStage<OgelConditionsServiceResult> get(String ogelId, String controlCode) {
    return wsClient.url(webServiceUrl + "/" + UrlEscapers.urlFragmentEscaper().escape(ogelId) + "/" +
        UrlEscapers.urlFragmentEscaper().escape(controlCode))
        .withRequestFilter(CorrelationId.requestFilter)
        .setRequestTimeout(webServiceTimeout)
        .get()
        .thenApplyAsync(response -> {
          if (response.getStatus() == 200 || response.getStatus() == 206) {
            // Condition apply (204) or conditions apply, but with missing control codes (206)
            return OgelConditionsServiceResult.buildFrom(response.asJson());
          }
          else if (response.getStatus() == 204) {
            // No conditions apply
            return OgelConditionsServiceResult.buildEmpty();
          }
          else {
            throw new ServiceException(String.format("Unexpected HTTP status code from OGEL service /control-code-conditions: %s",
                response.getStatus()));
          }
        }, httpExecutionContext.current());
  }

  /**
   * Check the OgelConditionsServiceResult returned by this client against a user provided answer 'conditionsApply'
   * The result of this check indicates if the OGEL, control code, and 'conditionsApply' tuple are valid for licence registration
   * @param result The result of this client
   * @param conditionsApply Whether the conditions in the result apply to the users item
   * @return Business logic result of the OGEL, control code and answer tuple
   */
  public static boolean isItemAllowed(OgelConditionsServiceResult result, boolean conditionsApply) {
    return result.itemsAllowed == conditionsApply;
  }

}
