package common.components.client;

import static org.mockito.Mockito.mock;

import au.com.dius.pact.provider.PactVerifyProvider;
import au.com.dius.pact.provider.junit.PactRunner;
import au.com.dius.pact.provider.junit.Provider;
import au.com.dius.pact.provider.junit.loader.PactBroker;
import au.com.dius.pact.provider.junit.target.AmqpTarget;
import au.com.dius.pact.provider.junit.target.Target;
import au.com.dius.pact.provider.junit.target.TestTarget;
import com.amazonaws.services.sqs.AmazonSQS;
import org.junit.runner.RunWith;
import pact.provider.components.common.client.CommonNotificationServiceProviderPact;

import java.util.Collections;

@RunWith(PactRunner.class)
@Provider("lite-permissions-finder")
@PactBroker(host = "pact-broker.ci.uktrade.io", protocol = "https", port = "443")
public class NotificationServiceProviderPact {

  private final AmazonSQS amazonSQS = mock(AmazonSQS.class);

  @TestTarget
  public final Target target = new AmqpTarget(Collections.singletonList("common.components.client.*"));

  @PactVerifyProvider("a valid email notification")
  public String validEmailNotification() {
    return CommonNotificationServiceProviderPact.validEmailNotification(amazonSQS);
  }

}
