import com.google.inject.Inject;
import com.google.inject.Provider;
import components.common.CommonContextAction;
import components.common.logging.CorrelationId;
import play.mvc.Action;
import play.mvc.Http;

import java.lang.reflect.Method;

/**
 * ActionCreator for setting up the HTTP context with common attributes required by all requests, with Pac4j security.
 */
public class ActionCreator implements play.http.ActionCreator {

  private final Provider<CommonContextAction> commonContextActionProvider;

  @Inject
  public ActionCreator(Provider<CommonContextAction> commonContextActionProvider) {
    this.commonContextActionProvider = commonContextActionProvider;
  }

  @Override
  public Action createAction(Http.Request request, Method actionMethod) {
    // Set up the Correlation ID for this request
    CorrelationId.setUp(request);

    //Ensure a new action instance is created for each request (Play requirement)
    return commonContextActionProvider.get();
  }
}
