package components.services.ogels.ogel;

import org.junit.Test;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WS;
import play.routing.Router;
import play.routing.RoutingDsl;
import play.server.Server;
import uk.gov.bis.lite.ogel.api.view.OgelFullView;

import java.util.concurrent.CompletionStage;

import static org.assertj.core.api.Assertions.assertThat;
import static play.mvc.Results.ok;

public class OgelServiceClientTest {

  @Test
  public void shouldGetOgel() throws Exception {

    String ogelId = "OGL61";
    Router router = new RoutingDsl().GET("/ogels/OGL61").routeTo(() ->
      ok(Json.parse("{\"id\":\"OGL991\",\"name\":\"OGEL 991\",\"description\":\"OGEL 991 Description\"," +
        "\"link\":\"link\",\"summary\":{\"canList\":[],\"cantList\":[],\"mustList\":[],\"howToUseList\":[]}}"))
    ).build();

    Server server = Server.forRouter(router);
    int port = server.httpPort();
    OgelServiceClient client = new OgelServiceClient(new HttpExecutionContext(Runnable::run), WS.newClient(port), "http://localhost:" + port, 10000);

    CompletionStage<OgelFullView> result = client.get(ogelId);

    OgelFullView ogel = result.toCompletableFuture().get();
    assertThat(ogel.getId()).isEqualTo("OGL991");

    server.stop();
  }

}