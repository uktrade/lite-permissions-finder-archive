package components.services.ogels.conditions;

import org.junit.Test;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WS;
import play.routing.Router;
import play.routing.RoutingDsl;
import play.server.Server;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static org.assertj.core.api.Assertions.assertThat;
import static play.mvc.Results.ok;

public class OgelConditionsServiceClientTest {

  @Test
  public void shouldGetOgelConditions() throws Exception {

    Router router = new RoutingDsl().GET("/control-code-conditions/OGL61/0C002").routeTo(() ->
      ok(Json.parse("{" +
        "\"controlCode\": \"0C002\"," +
        "\"conditionDescriptionControlCodes\": null," +
        "\"ogelId\": \"OGL61\",\n" +
        "\"conditionDescription\": \"<ul><li>separated plutonium</li></ul>\"," +
        "\"itemsAllowed\": false" +
        "}"))
    ).build();

    Server server = Server.forRouter(router);
    int port = server.httpPort();
    OgelConditionsServiceClient client = new OgelConditionsServiceClient(new HttpExecutionContext(Runnable::run), WS.newClient(port), "http://localhost:" + port, 10000);

    CompletionStage<OgelConditionsServiceResult> result = client.get("OGL61", "0C002");

    OgelConditionsServiceResult ogelConditionsServiceResult = result.toCompletableFuture().get();
    assertThat(ogelConditionsServiceResult.conditionDescription).isEqualTo("<ul><li>separated plutonium</li></ul>");
    assertThat(ogelConditionsServiceResult.ogelID).isEqualTo("OGL61");
    assertThat(ogelConditionsServiceResult.conditionDescriptionControlCodes).isEqualTo(Optional.empty());
    assertThat(ogelConditionsServiceResult.itemsAllowed).isFalse();
    assertThat(ogelConditionsServiceResult.controlCode).isEqualTo("0C002");
    assertThat(ogelConditionsServiceResult.isEmpty).isFalse();
    assertThat(ogelConditionsServiceResult.isMissingControlCodes).isFalse();

    server.stop();
  }

}