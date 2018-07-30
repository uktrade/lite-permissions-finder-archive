package controllers.licencefinder;

import static models.callback.RegistrationCallbackResponse.errorResponse;
import static models.callback.RegistrationCallbackResponse.okResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.persistence.LicenceFinderDao;
import components.services.notification.PermissionsFinderNotificationClient;
import exceptions.UnknownParameterException;
import models.persistence.RegisterLicence;
import models.view.licencefinder.Customer;
import models.view.licencefinder.Site;
import org.apache.commons.lang3.StringUtils;
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
                                @Named("permissionsFinderUrl") String permissionsFinderUrl) {
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
      LOGGER.info("Received registration callback for sessionId {} with json {}", sessionId, json);
    } catch (Exception exception) {
      String errorMessage = String.format("Unable to parse registration callback for sessionId %s with body %s",
          sessionId, request().body().asText());
      LOGGER.error(errorMessage, exception);
      return badRequest(Json.toJson(errorResponse(errorMessage)));
    }
    try {
      handleCallback(sessionId, callbackView);
      return ok(Json.toJson(okResponse()));
    } catch (Exception exception) {
      String errorMessage = String.format("Invalid registration callback for sessionId %s with json %s",
          sessionId, request().body().asJson());
      LOGGER.error(errorMessage, exception);
      return badRequest(Json.toJson(errorResponse(errorMessage + " " + exception.getMessage())));
    }
  }

  private void handleCallback(String sessionId, CallbackView callbackView) {
    RegisterLicence registerLicence = licenceFinderDao.getRegisterLicence(sessionId).orElseThrow(UnknownParameterException::unknownLicenceFinderOrder);
    Customer customer = licenceFinderDao.getCustomer(sessionId).orElseThrow(UnknownParameterException::unknownLicenceFinderOrder);
    Site site = licenceFinderDao.getSite(sessionId).orElseThrow(UnknownParameterException::unknownLicenceFinderOrder);
    String resumeCode = licenceFinderDao.getResumeCode(sessionId).orElseThrow(UnknownParameterException::unknownLicenceFinderOrder);

    String registrationReference = callbackView.getRegistrationReference();

    if (StringUtils.isNoneBlank(registrationReference)) {
      registerLicence.setRegistrationReference(registrationReference);
      licenceFinderDao.saveRegisterLicence(sessionId, registerLicence);

      // Send confirmation email to user
      String ogelUrl = permissionsFinderUrl + routes.ViewOgelController.viewOgel(registrationReference);
      String userEmailAddress = registerLicence.getUserEmailAddress();
      String applicantName = registerLicence.getUserFullName();
      permissionsFinderNotificationClient.sendRegisteredOgelToUserEmail(userEmailAddress, applicantName, ogelUrl);

      // Send confirmation email to Ecju
      String companyName = customer.getCompanyName();
      String siteAddress = site.getAddress();
      permissionsFinderNotificationClient.sendRegisteredOgelEmailToEcju(userEmailAddress, applicantName, resumeCode,
          companyName, siteAddress, ogelUrl);
    } else {
      LOGGER.error("CallbackView for sessionId {} with requestId {} and result {} is missing registrationReference",
          sessionId, callbackView.getRequestId(), callbackView.getResult());
      throw UnknownParameterException.unknownCallbackViewResult(callbackView.getResult());
    }
  }

}
