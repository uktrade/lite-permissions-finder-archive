package components.services.controlcode;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;

import java.util.List;
import java.util.Optional;

public class FrontendServiceResult {
  private final FrontendControlCode frontendControlCode;

  public FrontendServiceResult(JsonNode responseJson) {
    this.frontendControlCode = Json.fromJson(responseJson, FrontendControlCode.class);
  }

  public FrontendControlCode getFrontendControlCode() {
    return this.frontendControlCode;
  }

  public ControlCodeData getControlCodeData() {
    return this.frontendControlCode.controlCodeData;
  }

  public Optional<Ancestor> getGreatestAncestor() {
    return this.frontendControlCode.greatestAncestor;
  }

  public List<Ancestor> getOtherAncestors() {
    return this.frontendControlCode.otherAncestors;
  }
}
