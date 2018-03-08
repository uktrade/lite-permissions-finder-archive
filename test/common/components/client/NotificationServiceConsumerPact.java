package common.components.client;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRule;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.PactFragment;
import components.common.client.NotificationServiceClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import pact.consumer.components.common.client.NotificationClientConsumerPact;
import play.libs.ws.WSClient;
import play.test.WSTestClient;

public class NotificationServiceConsumerPact {
  private static final String CONSUMER = "lite-permissions-finder";
  private static final String PROVIDER = "lite-notification-service";

  public WSClient ws;

  @Rule
  public PactProviderRule mockProvider = new PactProviderRule(PROVIDER, this);

  @Before
  public void setUp() throws Exception {
    ws = WSTestClient.newClient(mockProvider.getConfig().getPort());
  }

  @After
  public void tearDown() throws Exception {
    ws.close();
  }

  @Pact(provider = PROVIDER, consumer = CONSUMER)
  public PactFragment successfulNotification(PactDslWithProvider builder) {
    return NotificationClientConsumerPact.successfulNotification(builder);
  }

  @Test
  @PactVerification(value = PROVIDER, fragment = "successfulNotification")
  public void successfulNotificationTest() {
    NotificationServiceClient client = NotificationClientConsumerPact.buildNotificationServiceClient(mockProvider, ws);
    NotificationClientConsumerPact.doSuccessfulNotificationSend(client);
  }

  @Pact(provider = PROVIDER, consumer = CONSUMER)
  public PactFragment unsuccessfulNotification(PactDslWithProvider builder) {
    return NotificationClientConsumerPact.unsuccessfulNotification(builder);
  }

  @Test
  @PactVerification(value = PROVIDER, fragment = "unsuccessfulNotification")
  public void unsuccessfulNotificationTest() {
    NotificationServiceClient client = NotificationClientConsumerPact.buildNotificationServiceClient(mockProvider, ws);
    NotificationClientConsumerPact.doUnsuccessfulNotificationSend(client);
  }
}
