package components.services.notification;

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
    notificationServiceClient.sendEmail("permissionsFinder:applicationReference", emailAddress, params);
  }

  public void sendNlrDocumentToUserEmail(String emailAddress, String applicantName, String nlrDocumentUrl) {
    Map<String, String> params = ImmutableMap.of("applicantName", applicantName, "nlrDocumentUrl", nlrDocumentUrl);
    notificationServiceClient.sendEmail("permissionsFinder:nlrDocumentToUser", emailAddress, params);
  }

  public void sendNlrDocumentToEcjuEmail(String ecjuEmailAddress, String userEmailAddress, String applicantName,
                                         String nlrDocumentUrl, String applicationReference, String companyName,
                                         String siteAddress) {
    Map<String, String> params = new ImmutableMap.Builder<String, String>()
        .put("emailAddress", userEmailAddress)
        .put("applicantName", applicantName)
        .put("nlrDocumentUrl", nlrDocumentUrl)
        .put("siteAddress", siteAddress)
        .put("applicationReference", applicationReference)
        .put("companyName", companyName)
        .build();

    notificationServiceClient.sendEmail("permissionsFinder:nlrDocumentToEcju", ecjuEmailAddress, params);
  }

  public void sendRegisteredOgelToUserEmail(String emailAddress, String applicantName, String ogelUrl) {
    Map<String, String> params = ImmutableMap.of("applicantName", applicantName, "ogelUrl", ogelUrl);
    notificationServiceClient.sendEmail("permissionsFinder:registeredOgelToUser", emailAddress, params);
  }

  public void sendRegisteredOgelToEcjuEmail(String emailAddress, String applicantName, String applicationReference,
                                            String companyName, String siteAddress, String ogelUrl) {
    Map<String, String> params = new ImmutableMap.Builder<String, String>()
        .put("emailAddress", emailAddress)
        .put("applicantName", applicantName)
        .put("ogelUrl", ogelUrl)
        .put("siteAddress", siteAddress)
        .put("applicationReference", applicationReference)
        .put("companyName", companyName)
        .build();

    notificationServiceClient.sendEmail("permissionsFinder:registeredOgelToEcju", emailAddress, params);
  }

}
