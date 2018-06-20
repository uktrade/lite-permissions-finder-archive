package components.services;

import models.persistence.RegisterLicence;
import models.view.licencefinder.ResultsView;
import uk.gov.bis.lite.permissions.api.view.CallbackView;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface LicenceFinderService {

  void persistCustomerAndSiteData(String sessionId);

  CompletionStage<Void> registerOgel(String sessionId);

  void handleCallback(String sessionId, CallbackView callbackView);

  Optional<String> getRegistrationReference(String sessionId);

  Optional<RegisterLicence> getRegisterLicence(String sessionId);

  ResultsView getResultsView(String sessionId);

  ResultsView getNoResultsView(String sessionId);

  void updateUsersOgelIdRefMap(String sessionId, String userId);

  boolean isOgelIdAlreadyRegistered(String sessionId, String ogelId);

  Optional<String> getUserOgelReference(String sessionId, String ogelId);

}
