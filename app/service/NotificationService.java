package service;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import play.Logger;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class NotificationService {

  private final WSClient ws;
  private final String wsHost;
  private final int wsPort;
  private final int wsTimeout;
  private final String wsUrl;

  private static final String APPLICATION_REFERENCE_TEMPLATE = "permissionsFinder:applicationReference";
  private static final String TEMPLATE_QUERY_NAME = "template";
  private static final String RECIPIENT_EMAIL_QUERY_NAME = "recipientEmail";

  @Inject
  public NotificationService(WSClient ws,
                             @Named("notificationServiceHost") String wsHost,
                             @Named("notificationServicePort") int wsPort,
                             @Named("notificationServiceTimeout") int wsTimeout
  ) {
    this.ws = ws;
    this.wsHost = wsHost;
    this.wsPort = wsPort;
    this.wsTimeout = wsTimeout;
    this.wsUrl = "http://" + wsHost + ":" + wsPort + "/notification/send-email";
  }

  /**
   * Calls Notification Service using template: permissionsFinder:applicationReference
   */
  public void sendApplicationReferenceEmail(String emailAddress, String applicationReference) {

    Logger.info("notification [" + APPLICATION_REFERENCE_TEMPLATE + "|" + emailAddress + "|" + applicationReference + "]");

    final JsonNode nameValueJson = Json.newObject()
        .put("applicationReference", applicationReference);

    CompletionStage<WSResponse> wsResponse = ws.url(wsUrl)
        .setHeader("Content-Type", "application/json")
        .setRequestTimeout(wsTimeout)
        .setQueryParameter(TEMPLATE_QUERY_NAME, APPLICATION_REFERENCE_TEMPLATE)
        .setQueryParameter(RECIPIENT_EMAIL_QUERY_NAME, emailAddress)
        .post(nameValueJson);

    CompletionStage<Boolean> result = wsResponse.thenCompose(r -> {
      Logger.info("notification [response status: " + r.getStatus() + "]");
      if (r.getStatus() == 200) {
        return CompletableFuture.supplyAsync(() -> true);
      } else {
        return CompletableFuture.supplyAsync(() -> false);
      }
    });
  }

}
