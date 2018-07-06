package components.services;

import models.view.licencefinder.ResultsView;
import uk.gov.bis.lite.permissions.api.view.CallbackView;

import java.util.Optional;

public interface LicenceFinderService {

  void persistCustomerAndSiteData(String sessionId);

  void registerOgel(String sessionId);

  void handleCallback(String sessionId, CallbackView callbackView);

  Optional<String> getRegistrationReference(String sessionId);

  ResultsView getResultsView(String sessionId);

  ResultsView getNoResultsView(String sessionId);

  boolean isValidOgelId(String sessionId, String ogelId);

  void updateUsersOgelIdRefMap(String sessionId, String userId);

  boolean isOgelIdAlreadyRegistered(String sessionId, String ogelId);

  Optional<String> getUserOgelReference(String sessionId, String ogelId);

}
