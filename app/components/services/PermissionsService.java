package components.services;

import uk.gov.bis.lite.permissions.api.view.OgelRegistrationView;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface PermissionsService {


  CompletionStage<Boolean> ping();

  CompletionStage<PermissionsServiceImpl.RegistrationResponse> registerOgel(String userId, String customerId, String siteId, String ogelId, String callbackUrl);

  CompletionStage<List<OgelRegistrationView>> getOgelRegistrations(String userId);

  CompletionStage<Optional<OgelRegistrationView>> getOgelRegistration(String userId, String registrationReference);

}
