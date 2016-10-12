package components.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static play.mvc.Results.internalServerError;
import static play.mvc.Results.ok;

import components.services.ogels.virtualeu.VirtualEUOgelClient;
import components.services.ogels.virtualeu.VirtualEUOgelResult;
import exceptions.ServiceException;
import org.junit.Test;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WS;
import play.routing.Router;
import play.routing.RoutingDsl;
import play.server.Server;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class VirtualEUOgelClientTest {

  @Test
  public void shouldGetVirtualEu() throws Exception {
    String ogelId = "OGL61";
    Router router = new RoutingDsl().GET("/virtual-eu").routeTo(() -> {
      return ok(Json.parse("{\"virtualEu\":true, \"ogelId\":\"" + ogelId + "\"}"));
    }).build();
    Server server = Server.forRouter(router);
    int port = server.httpPort();
    VirtualEUOgelClient client = new VirtualEUOgelClient(new HttpExecutionContext(Runnable::run), WS.newClient(port), "localhost", port, 10000);

    VirtualEUOgelResult result = client.sendServiceRequest("ML1a", "CTRY0",
        Collections.singletonList("CTRY1"), Collections.singletonList("MIL_GOV"))
        .toCompletableFuture()
        .get();

    assertThat(result.virtualEu).isTrue();

    assertThat(result.ogelId).isEqualTo(ogelId);

    server.stop();
  }

  @Test
  public void shouldThowServiceException() throws Exception {
    Router router = new RoutingDsl().GET("/virtual-eu").routeTo(() -> {
      return internalServerError();
    }).build();
    Server server = Server.forRouter(router);
    int port = server.httpPort();
    VirtualEUOgelClient client = new VirtualEUOgelClient(new HttpExecutionContext(Runnable::run), WS.newClient(port), "localhost", port, 10000);

    CompletableFuture<VirtualEUOgelResult> resultFuture = client.sendServiceRequest("ML1a", "CTRY0",
        Collections.singletonList("CTRY1"), Collections.singletonList("MIL_GOV"))
        .toCompletableFuture();

    assertThatThrownBy(resultFuture::get).hasRootCauseExactlyInstanceOf(ServiceException.class);

    server.stop();
  }

}
