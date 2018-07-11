package components.services.ogels.ogel;

import static org.assertj.core.api.Assertions.assertThat;
import static play.mvc.Results.ok;

import com.fasterxml.jackson.databind.JsonNode;
import components.client.OgelServiceClientImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;
import play.routing.RoutingDsl;
import play.server.Server;
import play.test.WSTestClient;
import uk.gov.bis.lite.ogel.api.view.OgelFullView;

import java.io.InputStream;
import java.util.concurrent.CompletionStage;

public class OgelServiceTest {
  private OgelServiceClientImpl client;
  private WSClient ws;
  private Server server;

  @Before
  public void setUp() {
    InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("services/ogels/ogel.json");
    JsonNode ogel = Json.parse(inputStream);

    server = Server.forRouter(builtInComponents -> RoutingDsl.fromComponents(builtInComponents)
        .GET("/ogels/OGL61")
        .routeTo(() -> ok(ogel))
        .build());
    int port = server.httpPort();
    ws = WSTestClient.newClient(port);
    client = new OgelServiceClientImpl(
        ws,
        "http://localhost:" + port,
        10000,
        "service:password",new HttpExecutionContext(Runnable::run));
  }

  @Test
  public void shouldGetOgel() throws Exception {
    CompletionStage<OgelFullView> result = client.getById("OGL61");
    OgelFullView ogel = result.toCompletableFuture().get();
    assertThat(ogel.getId()).isEqualTo("OGL991");
  }

  @After
  public void cleanUp() throws Exception {
    try {
      ws.close();
    } finally {
      server.stop();
    }
  }
}