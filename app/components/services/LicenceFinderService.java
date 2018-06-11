package components.services;

import models.view.licencefinder.ResultsView;
import uk.gov.bis.lite.permissions.api.view.CallbackView;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface LicenceFinderService {

  void persistCustomerAndSiteData();

  CompletionStage<Void> registerOgel(String transactionId);

  void handleCallback(String transactionId, CallbackView callbackView);

  Optional<String> getRegistrationReference(String transactionId);

  ResultsView getResultsView();

  ResultsView getNoResultsView();

  void updateUsersOgelIdRefMap(String userId);

  boolean isOgelIdAlreadyRegistered(String ogelId);

  Optional<String> getUserOgelReference(String ogelId);

}
