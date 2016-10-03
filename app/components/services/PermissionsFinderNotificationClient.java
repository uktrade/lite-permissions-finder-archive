package components.services;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import components.common.client.NotificationServiceClient;

import java.util.Map;

public class PermissionsFinderNotificationClient {

  private final NotificationServiceClient notificationServiceClient;

  @Inject
  public PermissionsFinderNotificationClient(NotificationServiceClient notificationServiceClient) {
    this.notificationServiceClient = notificationServiceClient;
  }

  public void sendApplicationReferenceEmail(String emailAddress, String applicationReference) {
    Map<String, String> params = ImmutableMap.of("applicationReference", applicationReference);
    notificationServiceClient.sendEmail("permissionsFinder:applicationReference", emailAddress, params);
  }
}
