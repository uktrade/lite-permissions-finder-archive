package controllers.licencefinder;

import static models.callback.RegistrationCallbackResponse.errorResponse;
import static models.callback.RegistrationCallbackResponse.okResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import components.services.LicenceFinderService;
import play.Logger;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import uk.gov.bis.lite.permissions.api.view.CallbackView;

public class RegistrationController extends Controller {

  private final LicenceFinderService licenceFinderService;

  @Inject
  public RegistrationController(LicenceFinderService licenceFinderService) {
    this.licenceFinderService = licenceFinderService;
  }

  /**
   * Endpoint for PermissionsService register callback
   */
  @BodyParser.Of(BodyParser.Json.class)
  public Result handleRegistrationCallback(String transactionId) {

    CallbackView callbackView;
    try {
      JsonNode json = request().body().asJson();
      callbackView = Json.fromJson(json, CallbackView.class);
      Logger.info("Registration callback received {transactionId={}, callbackView={}}", transactionId, json.toString());
    } catch (RuntimeException e) {
      String errorMessage = String.format("Registration callback error - Invalid callback registration request for transactionId %s, callbackBody=\"%s\"", transactionId, request().body().asText());
      Logger.error(errorMessage, e);
      return badRequest(Json.toJson(errorResponse(errorMessage)));
    }

    try {
      licenceFinderService.handleCallback(transactionId, callbackView);
      return ok(Json.toJson(okResponse()));
    } catch (Exception e) {
      String errorMessage = String.format("Registration callback handling error for transactionId %s", transactionId);
      Logger.error(errorMessage, e);
      return badRequest(Json.toJson(errorResponse(errorMessage + " - " + e.getMessage())));
    }
  }

}
