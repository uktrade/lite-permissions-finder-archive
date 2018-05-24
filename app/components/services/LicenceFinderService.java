package components.services;

import uk.gov.bis.lite.permissions.api.view.CallbackView;

public interface LicenceFinderService {

  void persistCustomerAndSiteData();

  void registerOgel();

  void handleCallback(String transactionId, CallbackView callbackView);

}
