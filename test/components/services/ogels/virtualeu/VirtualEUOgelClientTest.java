package components.services.ogels.virtualeu;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static play.mvc.Results.ok;

import exceptions.ServiceException;
import org.junit.Test;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import play.mvc.Results;
import play.routing.RoutingDsl;
import play.server.Server;
import play.test.WSTestClient;
import uk.gov.bis.lite.ogel.api.view.VirtualEuView;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class VirtualEUOgelClientTest {

  @Test
  public void shouldGetVirtualEu() throws Exception {
    String ogelId = "OGL61";
    Server server = Server.forRouter(builtInComponents -> RoutingDsl.fromComponents(builtInComponents)
        .GET("/virtual-eu")
        .routeTo(() -> ok(Json.parse("{\"virtualEu\":true, \"ogelId\":\"" + ogelId + "\"}")))
        .build());
    int port = server.httpPort();
    VirtualEUOgelClient client = new VirtualEUOgelClient(new HttpExecutionContext(Runnable::run),
        WSTestClient.newClient(port),
        "http://localhost:" + port,
        10000,
        "service:password");

    VirtualEuView result = client.sendServiceRequest("ML1a", "CTRY0", Collections.singletonList("CTRY1"))
        .toCompletableFuture()
        .get();

    assertThat(result.isVirtualEu()).isTrue();

    assertThat(result.getOgelId()).isEqualTo(ogelId);

    server.stop();
  }

  @Test
  public void shouldThrowServiceException() throws Exception {
    Server server = Server.forRouter(builtInComponents -> RoutingDsl.fromComponents(builtInComponents)
        .GET("/virtual-eu")
        .routeTo((Supplier<Result>) Results::internalServerError)
        .build());
    int port = server.httpPort();
    VirtualEUOgelClient client = new VirtualEUOgelClient(new HttpExecutionContext(Runnable::run),
        WSTestClient.newClient(port),
        "http://localhost:" + port,
        10000,
        "service:password");

    CompletableFuture<VirtualEuView> resultFuture = client.sendServiceRequest("ML1a", "CTRY0",
        Collections.singletonList("CTRY1"))
        .toCompletableFuture();

    assertThatThrownBy(resultFuture::get).hasRootCauseExactlyInstanceOf(ServiceException.class);

    server.stop();
  }

}
