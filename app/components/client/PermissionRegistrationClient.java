package components.client;

import static components.client.PermissionRegistrationClient.RegistrationResponse.failure;
import static components.client.PermissionRegistrationClient.RegistrationResponse.success;

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

import java.time.Duration;
import java.util.concurrent.CompletionStage;

public class PermissionRegistrationClient {

  private static final Logger.ALogger LOGGER = Logger.of(PermissionRegistrationClient.class);

  private static final String REGISTER_OGEL_PATH = "/register-ogel";
  private static final String QUERY_PARAM_CALLBACK_URL = "callbackUrl";

  private final WSClient wsClient;
  private final String permissionRegistrationAddress;
  private final int permissionRegistrationTimeout;
  private final JwtRequestFilter jwtRequestFilter;

  @Inject
  public PermissionRegistrationClient(WSClient wsClient,
                                      @Named("permissionRegistrationAddress") String permissionRegistrationAddress,
                                      @Named("permissionRegistrationTimeout") int permissionRegistrationTimeout,
                                      @Named("JwtRequestFilter") JwtRequestFilter jwtRequestFilter) {
    this.wsClient = wsClient;
    this.permissionRegistrationAddress = permissionRegistrationAddress;
    this.permissionRegistrationTimeout = permissionRegistrationTimeout;
    this.jwtRequestFilter = jwtRequestFilter;
  }

  public CompletionStage<RegistrationResponse> registerOgel(String userId, String customerId, String siteId, String ogelId, String callbackUrl) {

    RegisterParam registerParam = new RegisterParam();
    registerParam.setUserId(userId);
    registerParam.setExistingCustomer(customerId);
    registerParam.setExistingSite(siteId);
    registerParam.setOgelType(ogelId);

    WSRequest request = wsClient.url(permissionRegistrationAddress + REGISTER_OGEL_PATH)
        .setRequestFilter(CorrelationId.requestFilter)
        .setRequestFilter(jwtRequestFilter)
        .setRequestTimeout(Duration.ofMillis(permissionRegistrationTimeout))
        .addQueryParameter(QUERY_PARAM_CALLBACK_URL, callbackUrl);

    LOGGER.info("Sending register OGEL request");

    return request.post(Json.toJson(registerParam)).handle((result, error) -> handleResponse(result, error, callbackUrl));
  }

  @VisibleForTesting
  public static RegistrationResponse handleResponse(WSResponse response, Throwable throwable, String callbackUrl) {
    if (throwable != null) {
      String errorMessage = "Permission registration service client failure.";
      LOGGER.error(errorMessage + " {request path=" + REGISTER_OGEL_PATH + ", callbackUrl=" + callbackUrl + "}", throwable);
      return failure();
    } else if (response.getStatus() != 200) {
      String errorMessage = "Permission registration service error response - {}";
      LOGGER.error(errorMessage + " {request path=" + REGISTER_OGEL_PATH + ", callbackUrl=" + callbackUrl + "}", response.getBody());
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
