import com.google.inject.Inject;
import com.google.inject.Provider;
import components.auth.AuthManager;
import components.common.CommonContextActionSetup;
import components.common.logging.CorrelationId;
import org.pac4j.core.config.Config;
import org.pac4j.play.java.SecureAction;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import java.lang.reflect.Method;
import java.util.concurrent.CompletionStage;

/**
 * ActionCreator for setting up the HTTP context with common attributes required by all requests, with Pac4j security.
 */
public class ActionCreator implements play.http.ActionCreator {

  private final Provider<NoAnnotationSecureAction> secureActionProvider;

  @Inject
  public ActionCreator(Provider<NoAnnotationSecureAction> secureActionProvider) {
    this.secureActionProvider = secureActionProvider;
  }

  @Override
  public Action createAction(Http.Request request, Method actionMethod) {
    // Set up the Correlation ID for this request
    CorrelationId.setUp(request);

    //Ensure a new action instance is created for each request (Play requirement)
    return secureActionProvider.get();
  }

  /**
   * A Pac4J SecureAction which can be executed without relying on Action method annotations.
   * This is a minor hack approved by the Pac4j author. Can be removed when the following issue is resolved:
   * https://github.com/pac4j/play-pac4j/issues/134
   */
  public static class NoAnnotationSecureAction extends SecureAction {

    private final AuthManager authManager;
    private final CommonContextActionSetup commonContextActionSetup;

    @Inject
    public NoAnnotationSecureAction(Config config, AuthManager authManager,
                                    CommonContextActionSetup commonContextActionSetup) {
      super(config);
      this.authManager = authManager;
      this.commonContextActionSetup = commonContextActionSetup;
    }

    @Override
    public CompletionStage<Result> call(Http.Context ctx) {

      //Always set up the context with all common library requirements
      commonContextActionSetup.setupContext(ctx);

      //Always set a reference to the AuthManager for retrieval in views
      authManager.setAsContextArgument(ctx);

      return delegate.call(ctx);
    }
  }
}
