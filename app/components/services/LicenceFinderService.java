package components.services;

import controllers.licencefinder.QuestionsController;
import models.view.licencefinder.OgelView;
import uk.gov.bis.lite.customer.api.view.CustomerView;
import uk.gov.bis.lite.customer.api.view.SiteView;
import uk.gov.bis.lite.ogel.api.view.ApplicableOgelView;
import uk.gov.bis.lite.permissions.api.view.CallbackView;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface LicenceFinderService {

  void persistCustomerAndSiteData(String sessionId, CustomerView customerView, SiteView siteView);

  void registerOgel(String sessionId, String userId, String customerId, String siteId, String ogelId);

  void handleCallback(String sessionId, CallbackView callbackView);

  List<ApplicableOgelView> getApplicableOgelViews(String controlCode, String sourceCountry,
                                                  List<String> destinationCountries,
                                                  QuestionsController.QuestionsForm questionsForm);

  Optional<String> getRegistrationReference(String sessionId);

  void saveDestinations(String sessionId, String destinationCountry, String firstConsigneeCountry,
                        boolean multipleCountries);

  boolean canAccessTradeController(String sessionId);

  boolean canAccessDestinationController(String sessionId);

  boolean canAccessQuestionController(String sessionId);

  boolean canAccessChooseOgelController(String sessionId);

  boolean canAccessRegisterToUseController(String sessionId);

  Map<String, String> getUserOgelIdReferenceMap(String sessionId);

  void updateUserOgelIdReferenceMap(String sessionId, String userId);

  List<OgelView> getOgelViews(List<ApplicableOgelView> applicableViews, Set<String> existingOgels);
}
