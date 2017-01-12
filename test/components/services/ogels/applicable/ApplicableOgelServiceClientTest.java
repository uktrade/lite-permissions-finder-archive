package components.services.ogels.applicable;

import org.junit.Test;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WS;
import play.routing.Router;
import play.routing.RoutingDsl;
import play.server.Server;

import java.util.Arrays;
import java.util.concurrent.CompletionStage;

import static org.assertj.core.api.Assertions.assertThat;
import static play.mvc.Results.ok;

public class ApplicableOgelServiceClientTest {

  @Test
  public void shouldGetApplicableOgel() throws Exception {

    Router router = new RoutingDsl().GET("/applicable-ogels").routeTo(() ->
      ok(Json.parse("[{\"id\":\"OGL991\",\"name\":\"OGEL 991\",\"usageSummary\":[\"A\",\"B\",\"C\"]}]"))
    ).build();

    Server server = Server.forRouter(router);
    int port = server.httpPort();
    ApplicableOgelServiceClient client = new ApplicableOgelServiceClient(new HttpExecutionContext(Runnable::run), WS.newClient(port), "http://localhost:" + port, 10000);

    CompletionStage<ApplicableOgelServiceResult> result = client.get("ML1a", "UK", Arrays.asList("France, Spain"), Arrays.asList("test"), false);

    ApplicableOgelServiceResult ogelServiceResult = result.toCompletableFuture().get();
    assertThat(ogelServiceResult.results.size()).isEqualTo(1);
    assertThat(ogelServiceResult.results.get(0).getId()).isEqualTo("OGL991");
    assertThat(ogelServiceResult.results.get(0).getName()).isEqualTo("OGEL 991");
    assertThat(ogelServiceResult.results.get(0).getUsageSummary().size()).isEqualTo(3);

    server.stop();
  }
}