package components.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.logging.CorrelationId;
import filters.common.JwtRequestFilter;
import play.Logger;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import uk.gov.bis.lite.permissions.api.view.OgelRegistrationView;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class OgelRegistrationServiceClient {

  private static final Logger.ALogger LOGGER = Logger.of(OgelRegistrationServiceClient.class);

  private static final String OGEL_REGISTRATIONS_PATH = "/ogel-registrations/user/";

  private final WSClient wsClient;
  private final String permissionRegistrationAddress;
  private final int permissionRegistrationTimeout;
  private final ObjectMapper objectMapper;
  private final JwtRequestFilter jwtRequestFilter;

  @Inject
  public OgelRegistrationServiceClient(WSClient wsClient,
                                       @Named("permissionRegistrationAddress") String permissionRegistrationAddress,
                                       @Named("permissionRegistrationTimeout") int permissionRegistrationTimeout,
                                       ObjectMapper objectMapper,
                                       @Named("JwtRequestFilter") JwtRequestFilter jwtRequestFilter) {
    this.wsClient = wsClient;
    this.permissionRegistrationAddress = permissionRegistrationAddress;
    this.permissionRegistrationTimeout = permissionRegistrationTimeout;
    this.objectMapper = objectMapper;
    this.jwtRequestFilter = jwtRequestFilter;
  }

  public CompletionStage<List<OgelRegistrationView>> getOgelRegistrations(String userId) {

    String path = OGEL_REGISTRATIONS_PATH + userId;
    return get(path).thenApplyAsync(r -> {
      if (r != null) {
        try {
          return objectMapper.readValue(r, new TypeReference<List<OgelRegistrationView>>() {});
        } catch (IOException e) {
          String errorMessage = "Failed to parse Permission registration service response.";
          LOGGER.error(errorMessage + " {request path=" + path + "}", e);
        }
      }
      return new ArrayList<>();
    });
  }

  public CompletionStage<Optional<OgelRegistrationView>> getOgelRegistration(String userId,
                                                                             String registrationReference) {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("registrationReference", registrationReference);

    String path = OGEL_REGISTRATIONS_PATH + userId;
    return get(path, parameters).thenApplyAsync(r -> convertSingleRegistrationResult(registrationReference, path, r));
  }

  @VisibleForTesting
  Optional<OgelRegistrationView> convertSingleRegistrationResult(String registrationReference, String path,
                                                                 String result) {
    if (result != null) {
      try {
        List<OgelRegistrationView> ogelRegistrations = objectMapper.readValue(result, new TypeReference<List<OgelRegistrationView>>() {});
        if (ogelRegistrations.size() == 1) {
          return Optional.of(ogelRegistrations.get(0));
        } else {
          String errorMessage = "Expected only one OGEL registration in Permission registration service response. But received - {}.";
          LOGGER.error(errorMessage + " {request path=" + path + ", registrationReference=" + registrationReference + "}", ogelRegistrations.size());
        }
      } catch (IOException e) {
        LOGGER.error("Failed to parse Permission registration service response.", e);
      }
    }

    return Optional.empty();
  }

  private CompletionStage<String> get(String path) {
    return get(path, null);
  }

  private CompletionStage<String> get(String path, Map<String, String> parameters) {
    WSRequest request = wsClient.url(permissionRegistrationAddress + path)
        .setRequestFilter(CorrelationId.requestFilter)
        .setRequestFilter(jwtRequestFilter)
        .setRequestTimeout(Duration.ofMillis(permissionRegistrationTimeout));

    if (parameters != null) {
      parameters.forEach(request::addQueryParameter);
    }

    return request.get().handle((result, error) -> {
      if (error != null) {
        String errorMessage = "Permission registration service client failure.";
        LOGGER.error(errorMessage + " {request path=" + path + ", parameters=" + parameters + "}", error);
      } else if (result.getStatus() != 200) {
        String errorMessage = "Permission registration service error response - {}";
        LOGGER.error(errorMessage + " {request path=" + path + ", parameters=" + parameters + "}", result.getBody());
      } else {
        return result.asJson().toString();
      }
      return null;
    });
  }

}
