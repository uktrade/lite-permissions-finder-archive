package components.client;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.logging.CorrelationId;
import exceptions.ServiceException;
import filters.common.JwtRequestFilter;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import uk.gov.bis.lite.permissions.api.RegisterOgelResponse;
import uk.gov.bis.lite.permissions.api.param.RegisterParam;
import uk.gov.bis.lite.permissions.api.view.OgelRegistrationView;
import utils.RequestUtil;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class PermissionsServiceClientImpl implements PermissionsServiceClient {

  private static final String REGISTER_OGEL_PATH = "/register-ogel";
  private static final String QUERY_PARAM_CALLBACK_URL = "callbackUrl";
  private static final String OGEL_REGISTRATIONS_PATH = "/ogel-registrations/user/";

  private final WSClient wsClient;
  private final String permissionsServiceAddress;
  private final int timeout;
  private final JwtRequestFilter jwtRequestFilter;

  @Inject
  public PermissionsServiceClientImpl(WSClient wsClient, @Named("permissionsServiceAddress") String permissionsServiceAddress,
                                      @Named("permissionsServiceTimeout") int timeout, JwtRequestFilter jwtRequestFilter) {
    this.wsClient = wsClient;
    this.permissionsServiceAddress = permissionsServiceAddress;
    this.timeout = timeout;
    this.jwtRequestFilter = jwtRequestFilter;
  }

  @Override
  public CompletionStage<String> registerOgel(String userId, String customerId, String siteId, String ogelId,
                                              String callbackUrl) {

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

    return request.post(Json.toJson(param)).handle((response, error) -> {
      if (RequestUtil.hasError(response, error)) {
        String errorMessage = "Unable to register ogel for userId " + userId;
        RequestUtil.logError(request, response, error, errorMessage);
        throw new ServiceException(errorMessage);
      } else {
        RegisterOgelResponse registerOgelResponse = Json.fromJson(response.asJson(), RegisterOgelResponse.class);
        return registerOgelResponse.getRequestId();
      }
    });
  }

  @Override
  public CompletionStage<List<OgelRegistrationView>> getOgelRegistrations(String userId) {
    String url = permissionsServiceAddress + OGEL_REGISTRATIONS_PATH + userId;
    WSRequest request = wsClient.url(url)
        .setRequestFilter(CorrelationId.requestFilter)
        .setRequestFilter(jwtRequestFilter)
        .setRequestTimeout(Duration.ofMillis(timeout));

    return request.get().handle((response, error) -> {
      if (RequestUtil.hasError(response, error)) {
        String message = "Permission registration service client getOgelRegistrations failure";
        RequestUtil.logError(request, response, error, message);
        throw new ServiceException(message);
      } else {
        OgelRegistrationView[] ogelRegistrationViews = Json.fromJson(response.asJson(), OgelRegistrationView[].class);
        return Arrays.asList(ogelRegistrationViews);
      }
    });
  }

  @Override
  public CompletionStage<OgelRegistrationView> getOgelRegistration(String userId, String registrationReference) {
    String url = permissionsServiceAddress + OGEL_REGISTRATIONS_PATH + userId;
    WSRequest request = wsClient.url(url)
        .setRequestFilter(CorrelationId.requestFilter)
        .setRequestFilter(jwtRequestFilter)
        .setRequestTimeout(Duration.ofMillis(timeout))
        .addQueryParameter("registrationReference", registrationReference);

    return request.get().handle((response, error) -> {
      if (RequestUtil.hasError(response, error)) {
        String message = "Permission registration service client getOgelRegistration failure";
        RequestUtil.logError(request, response, error, message);
        throw new ServiceException(message);
      } else {
        OgelRegistrationView[] ogelRegistrationViews = Json.fromJson(response.asJson(), OgelRegistrationView[].class);
        return Arrays.asList(ogelRegistrationViews);
      }
    }).thenApply(ogelRegistrationViews -> {
      if (ogelRegistrationViews.size() == 1) {
        return ogelRegistrationViews.get(0);
      } else {
        String message = "Expected 1 ogelRegistrationView but actual count was " + ogelRegistrationViews.size();
        throw new ServiceException(message);
      }
    });
  }

}
