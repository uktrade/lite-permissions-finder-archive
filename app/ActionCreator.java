import com.google.inject.Inject;
import com.google.inject.Provider;
import components.common.logging.CorrelationId;
import play.mvc.Action;
import play.mvc.Http;

import java.lang.reflect.Method;

/**
 * ActionCreator for setting up the HTTP context with common attributes required by all requests, with Pac4j security.
 */
public class ActionCreator implements play.http.ActionCreator {

  private final Provider<ContextAction> contextAction;

  @Inject
  public ActionCreator(Provider<ContextAction> contextAction) {
    this.contextAction = contextAction;
  }

  @Override
  public Action createAction(Http.Request request, Method actionMethod) {
    // Set up the Correlation ID for this request
    CorrelationId.setUp(request);

    //Ensure a new action instance is created for each request (Play requirement)
    return contextAction.get();
  }
}
