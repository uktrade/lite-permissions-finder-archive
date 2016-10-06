package components.services;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import components.common.client.NotificationServiceClient;
import play.libs.concurrent.HttpExecutionContext;

import java.util.Map;

public class PermissionsFinderNotificationClient {

  private final NotificationServiceClient notificationServiceClient;
  private final HttpExecutionContext httpExecutionContext;

  @Inject
  public PermissionsFinderNotificationClient(NotificationServiceClient notificationServiceClient,
                                             HttpExecutionContext httpExecutionContext) {
    this.notificationServiceClient = notificationServiceClient;
    this.httpExecutionContext = httpExecutionContext;
  }

  public void sendApplicationReferenceEmail(String emailAddress, String applicationReference) {
    Map<String, String> params = ImmutableMap.of("applicationReference", applicationReference);
    notificationServiceClient.sendEmail("permissionsFinder:applicationReference", emailAddress, params, httpExecutionContext);
  }
}
