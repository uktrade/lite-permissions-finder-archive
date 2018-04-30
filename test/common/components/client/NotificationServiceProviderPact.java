package common.components.client;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import au.com.dius.pact.provider.PactVerifyProvider;
import au.com.dius.pact.provider.junit.PactRunner;
import au.com.dius.pact.provider.junit.Provider;
import au.com.dius.pact.provider.junit.loader.PactBroker;
import au.com.dius.pact.provider.junit.target.AmqpTarget;
import au.com.dius.pact.provider.junit.target.Target;
import au.com.dius.pact.provider.junit.target.TestTarget;
import com.amazonaws.services.sqs.AmazonSQS;
import com.google.common.collect.ImmutableMap;
import components.common.client.NotificationServiceClient;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

import java.util.Collections;

@RunWith(PactRunner.class)
@Provider("lite-permissions-finder")
@PactBroker(host = "pact-broker.ci.uktrade.io", port = "80")
public class NotificationServiceProviderPact {

  private static final AmazonSQS amazonSQS = mock(AmazonSQS.class);

  @TestTarget
  public final Target target = new AmqpTarget(Collections.singletonList("common.components.client.*"));

  @PactVerifyProvider("a valid email notification")
  public String validEmailNotification() {
    NotificationServiceClient notificationServiceClient = new NotificationServiceClient("url", amazonSQS);
    notificationServiceClient.sendEmail("validTemplate", "user@test.com",
        ImmutableMap.of("validParamOne", "valueOne", "validParamTwo", "valueTwo"));

    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    verify(amazonSQS).sendMessage(eq("url"), captor.capture());

    return captor.getValue();
  }

}
