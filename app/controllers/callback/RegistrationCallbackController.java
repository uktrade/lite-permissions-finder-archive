package controllers.callback;

import static models.callback.CallbackResponse.errorResponse;
import static models.callback.CallbackResponse.okResponse;
import static play.mvc.Results.badRequest;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.CommonContextAction;
import play.Logger;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Result;
import play.mvc.With;

@With(CommonContextAction.class)
public class RegistrationCallbackController {

  private static final Logger.ALogger LOGGER = Logger.of(RegistrationCallbackController.class);

  private final String sharedSecret;

  @Inject
  public RegistrationCallbackController(@Named("sharedSecret") String sharedSecret) {
    this.sharedSecret = sharedSecret;
  }

  @BodyParser.Of(BodyParser.Json.class)
  public Result registration(String transactionId, String securityToken) {
    if (!matchesSharedSecret(securityToken)) {
      String errorMessage = String.format("Registration callback error - Security token verification failed for transactionId %s", transactionId);
      LOGGER.error(errorMessage);
      return badRequest(Json.toJson(errorResponse(errorMessage)));
    }

    // TODO update status of transaction
    return badRequest(Json.toJson(okResponse()));
  }

  public boolean matchesSharedSecret(String sharedSecret) {
    return this.sharedSecret.equals(sharedSecret);
  }
}
