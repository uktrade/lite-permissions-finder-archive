package components.client;

import static components.client.PermissionsServiceImpl.RegistrationResponse.failure;
import static components.client.PermissionsServiceImpl.RegistrationResponse.success;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.logging.CorrelationId;
import filters.common.JwtRequestFilter;
import play.Logger;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import uk.gov.bis.lite.permissions.api.RegisterOgelResponse;
import uk.gov.bis.lite.permissions.api.param.RegisterParam;
import uk.gov.bis.lite.permissions.api.view.OgelRegistrationView;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class PermissionsServiceImpl implements PermissionsService {

  private static final String REGISTER_OGEL_PATH = "/register-ogel";
  private static final String QUERY_PARAM_CALLBACK_URL = "callbackUrl";
  private static final String OGEL_REGISTRATIONS_PATH = "/ogel-registrations/user/";

  private final WSClient wsClient;
  private final String permissionsServiceAddress;
  private final int timeout;
  private final JwtRequestFilter jwtRequestFilter;
  private final ObjectMapper mapper;

  @Inject
  public PermissionsServiceImpl(WSClient wsClient,
                                @Named("permissionsServiceAddress") String permissionsServiceAddress,
                                @Named("permissionsServiceTimeout") int timeout,
                                @Named("jwtRequestFilter") JwtRequestFilter jwtRequestFilter,
                                ObjectMapper mapper) {
    this.wsClient = wsClient;
    this.permissionsServiceAddress = permissionsServiceAddress;
    this.timeout = timeout;
    this.jwtRequestFilter = jwtRequestFilter;
    this.mapper = mapper;
  }

  /**
   * registerOgel
   */
  public CompletionStage<RegistrationResponse> registerOgel(String userId, String customerId, String siteId, String ogelId, String callbackUrl) {

    RegisterParam param = new RegisterParam();
    param.setUserId(userId);
    param.setExistingCustomer(customerId);
    param.setExistingSite(siteId);
    param.setOgelType(ogelId);

    WSRequest request = wsClient.url(permissionsServiceAddress + REGISTER_OGEL_PATH)
        .setRequestFilter(CorrelationId.requestFilter)
        .setRequestFilter(jwtRequestFilter)
        .setRequestTimeout(Duration.ofMillis(timeout))
        .addQueryParameter(QUERY_PARAM_CALLBACK_URL, callbackUrl);

    Logger.info("Sending register OGEL request: " + permissionsServiceAddress + REGISTER_OGEL_PATH);

    return request.post(Json.toJson(param)).handle((result, error) -> handleResponse(result, error, callbackUrl));
  }

  public CompletionStage<List<OgelRegistrationView>> getOgelRegistrations(String userId) {

    String path = OGEL_REGISTRATIONS_PATH + userId;
    return get(path).thenApplyAsync(r -> {
      if (r != null) {
        try {
          return mapper.readValue(r, new TypeReference<List<OgelRegistrationView>>() {});
        } catch (IOException e) {
          String errorMessage = "Failed to parse Permission registration service response.";
          Logger.error(errorMessage + " {request path=" + path + "}", e);
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
        List<OgelRegistrationView> ogelRegistrations = mapper.readValue(result, new TypeReference<List<OgelRegistrationView>>() {});
        if (ogelRegistrations.size() == 1) {
          return Optional.of(ogelRegistrations.get(0));
        } else {
          String errorMessage = "Expected only one OGEL registration in Permission registration service response. But received - {}.";
          Logger.error(errorMessage + " {request path=" + path + ", registrationReference=" + registrationReference + "}", ogelRegistrations.size());
        }
      } catch (IOException e) {
        Logger.error("Failed to parse Permission registration service response.", e);
      }
    }

    return Optional.empty();
  }

  private CompletionStage<String> get(String path) {
    return get(path, null);
  }

  private CompletionStage<String> get(String path, Map<String, String> parameters) {
    WSRequest request = wsClient.url(permissionsServiceAddress + path)
        .setRequestFilter(CorrelationId.requestFilter)
        .setRequestFilter(jwtRequestFilter)
        .setRequestTimeout(Duration.ofMillis(timeout));

    if (parameters != null) {
      parameters.forEach(request::addQueryParameter);
    }

    return request.get().handle((result, error) -> {
      if (error != null) {
        String errorMessage = "Permission registration service client failure.";
        Logger.error(errorMessage + " {request path=" + path + ", parameters=" + parameters + "}", error);
      } else if (result.getStatus() != 200) {
        String errorMessage = "Permission registration service error response - {}";
        Logger.error(errorMessage + " {request path=" + path + ", parameters=" + parameters + "}", result.getBody());
      } else {
        return result.asJson().toString();
      }
      return null;
    });
  }

  @VisibleForTesting
  public static RegistrationResponse handleResponse(WSResponse response, Throwable throwable, String callbackUrl) {
    if (throwable != null) {
      String errorMessage = "Permission registration service client failure.";
      Logger.error(errorMessage + " {request path=" + REGISTER_OGEL_PATH + ", callbackUrl=" + callbackUrl + "}", throwable);
      return failure();
    } else if (response.getStatus() != 200) {
      String errorMessage = "Permission registration service error response - {}";
      Logger.error(errorMessage + " {request path=" + REGISTER_OGEL_PATH + ", callbackUrl=" + callbackUrl + "}", response.getBody());
      return failure();
    } else {
      RegisterOgelResponse registerOgelResponse = Json.fromJson(response.asJson(), RegisterOgelResponse.class);
      return success(registerOgelResponse.getRequestId());
    }
  }

  public static class RegistrationResponse {
    private final boolean success;
    private final String requestId;
    private RegistrationResponse(boolean success, String requestId) {
      this.success = success;
      this.requestId = requestId;
    }
    static RegistrationResponse success(String requestId) {
      return new RegistrationResponse(true, requestId);
    }
    static RegistrationResponse failure() {
      return new RegistrationResponse(false, "");
    }
    public boolean isSuccess() {
      return success;
    }
    public String getRequestId() {
      return requestId;
    }
  }

}
