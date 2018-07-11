package components.services.ogels.applicable;

import static org.assertj.core.api.Assertions.assertThat;
import static play.mvc.Results.ok;

import com.fasterxml.jackson.databind.JsonNode;
import components.client.ApplicableOgelServiceClientImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;
import play.routing.RoutingDsl;
import play.server.Server;
import play.test.WSTestClient;
import uk.gov.bis.lite.ogel.api.view.ApplicableOgelView;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class ApplicableOgelServiceClientImplTest {
  private ApplicableOgelServiceClientImpl client;
  private WSClient ws;
  private Server server;

  @Before
  public void setUp() {
    InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("services/ogels/applicable-ogels.json");
    JsonNode applicableOgels = Json.parse(inputStream);

    server = Server.forRouter(builtInComponents -> RoutingDsl.fromComponents(builtInComponents)
        .GET("/applicable-ogels")
        .routeTo(() -> ok(applicableOgels))
        .build());
    int port = server.httpPort();
    ws = WSTestClient.newClient(port);
    client = new ApplicableOgelServiceClientImpl(new HttpExecutionContext(Runnable::run),
        ws,
        "http://localhost:" + port,
        10000,
        "service:password");
  }

  @Test
  public void shouldGetApplicableOgel() throws Exception {
    CompletionStage<List<ApplicableOgelView>> result = client.get("ML1a", "UK", Arrays.asList("France, Spain"), Arrays.asList("test"), true);

    List<ApplicableOgelView> ogelViews = result.toCompletableFuture().get();
    assertThat(ogelViews.size()).isEqualTo(1);
    assertThat(ogelViews.get(0).getId()).isEqualTo("OGL991");
    assertThat(ogelViews.get(0).getName()).isEqualTo("OGEL 991");
    assertThat(ogelViews.get(0).getUsageSummary().size()).isEqualTo(3);
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