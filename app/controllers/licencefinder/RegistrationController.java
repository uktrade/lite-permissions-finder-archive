package controllers.licencefinder;

import static models.callback.RegistrationCallbackResponse.errorResponse;
import static models.callback.RegistrationCallbackResponse.okResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import components.persistence.LicenceFinderDao;
import components.services.notification.PermissionsFinderNotificationClient;
import exceptions.UnknownParameterException;
import models.persistence.RegisterLicence;
import models.view.licencefinder.Customer;
import models.view.licencefinder.Site;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import uk.gov.bis.lite.permissions.api.view.CallbackView;

public class RegistrationController extends Controller {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(RegistrationController.class);

  private final LicenceFinderDao licenceFinderDao;
  private final PermissionsFinderNotificationClient permissionsFinderNotificationClient;
  private final String permissionsFinderUrl;

  @Inject
  public RegistrationController(LicenceFinderDao licenceFinderDao,
                                PermissionsFinderNotificationClient permissionsFinderNotificationClient,
                                String permissionsFinderUrl) {
    this.licenceFinderDao = licenceFinderDao;
    this.permissionsFinderNotificationClient = permissionsFinderNotificationClient;
    this.permissionsFinderUrl = permissionsFinderUrl;
  }

  /**
   * Endpoint for PermissionsService register callback
   */
  @BodyParser.Of(BodyParser.Json.class)
  public Result handleRegistrationCallback(String sessionId) {

    CallbackView callbackView;
    try {
      JsonNode json = request().body().asJson();
      callbackView = Json.fromJson(json, CallbackView.class);
      LOGGER.info("Registration callback received {transactionId={}, callbackView={}}", sessionId, json.toString());
    } catch (RuntimeException e) {
      String errorMessage = String.format("Registration callback error - Invalid callback registration request for sessionId %s, callbackBody=\"%s\"", sessionId, request().body().asText());
      LOGGER.error(errorMessage, e);
      return badRequest(Json.toJson(errorResponse(errorMessage)));
    }

    try {
      handleCallback(sessionId, callbackView);
      return ok(Json.toJson(okResponse()));
    } catch (Exception e) {
      String errorMessage = String.format("Registration callback handling error for sessionId %s", sessionId);
      LOGGER.error(errorMessage, e);
      return badRequest(Json.toJson(errorResponse(errorMessage + " - " + e.getMessage())));
    }
  }

  private void handleCallback(String sessionId, CallbackView callbackView) {
    RegisterLicence registerLicence = licenceFinderDao.getRegisterLicence(sessionId).orElseThrow(UnknownParameterException::unknownLicenceFinderOrder);
    Customer customer = licenceFinderDao.getCustomer(sessionId).orElseThrow(UnknownParameterException::unknownLicenceFinderOrder);
    Site site = licenceFinderDao.getSite(sessionId).orElseThrow(UnknownParameterException::unknownLicenceFinderOrder);
    String resumeCode = licenceFinderDao.getResumeCode(sessionId).orElseThrow(UnknownParameterException::unknownLicenceFinderOrder);

    String registrationReference = callbackView.getRegistrationReference();
    registerLicence.setRegistrationReference(registrationReference);
    licenceFinderDao.saveRegisterLicence(sessionId, registerLicence);

    // Send confirmation email to user
    String ogelUrl = permissionsFinderUrl + controllers.licencefinder.routes.ViewOgelController.viewOgel(registrationReference);
    String userEmailAddress = registerLicence.getUserEmailAddress();
    String applicantName = registerLicence.getUserFullName();
    permissionsFinderNotificationClient.sendRegisteredOgelToUserEmail(userEmailAddress, applicantName, ogelUrl);

    // Send confirmation email to Ecju
    String companyName = customer.getCompanyName();
    String siteAddress = site.getAddress();
    permissionsFinderNotificationClient.sendRegisteredOgelEmailToEcju(userEmailAddress, applicantName, resumeCode, companyName, siteAddress, ogelUrl);
  }

}
