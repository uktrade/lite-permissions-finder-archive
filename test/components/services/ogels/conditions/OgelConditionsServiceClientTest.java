package components.services.ogels.conditions;

import static org.assertj.core.api.Assertions.assertThat;
import static play.mvc.Results.ok;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WS;
import play.libs.ws.WSClient;
import play.routing.Router;
import play.routing.RoutingDsl;
import play.server.Server;

import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class OgelConditionsServiceClientTest {
  private OgelConditionsServiceClient client;
  private WSClient ws;
  private Server server;


  @Before
  public void setUp() throws Exception {
    InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("services/ogels/ogel-conditions.json");
    JsonNode ogelConditions = Json.parse(inputStream);

    Router router = new RoutingDsl().GET("/control-code-conditions/OGL61/0C002")
        .routeTo(() -> ok(ogelConditions))
        .build();

    server = Server.forRouter(router);
    int port = server.httpPort();
    ws = WS.newClient(port);
    client = new OgelConditionsServiceClient(new HttpExecutionContext(Runnable::run),
        ws,
        "http://localhost:" + port,
        10000,
        "service:password");
  }

  @Test
  public void shouldGetOgelConditions() throws Exception {
    CompletionStage<OgelConditionsServiceResult> result = client.get("OGL61", "0C002");

    OgelConditionsServiceResult ogelConditionsServiceResult = result.toCompletableFuture().get();

    assertThat(ogelConditionsServiceResult.conditionDescription).isEqualTo("<ul><li>separated plutonium</li></ul>");
    assertThat(ogelConditionsServiceResult.ogelID).isEqualTo("OGL61");
    assertThat(ogelConditionsServiceResult.conditionDescriptionControlCodes).isEqualTo(Optional.empty());
    assertThat(ogelConditionsServiceResult.itemsAllowed).isFalse();
    assertThat(ogelConditionsServiceResult.controlCode).isEqualTo("0C002");
    assertThat(ogelConditionsServiceResult.isEmpty).isFalse();
    assertThat(ogelConditionsServiceResult.isMissingControlCodes).isFalse();

  }

  @After
  public void tearDown() throws Exception {
    try {
      ws.close();
    } finally {
      server.stop();
    }
  }

}