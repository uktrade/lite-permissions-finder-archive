package components.services.controlcode.category.controls;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CategoryControlsServiceResult {
  public final List<ControlCode> controlCodes;

  public CategoryControlsServiceResult(JsonNode responseJson) {
    this.controlCodes = Arrays.asList(Json.fromJson(responseJson, ControlCode[].class));
  }

  public CategoryControlsServiceResult(int count) {
    this.controlCodes = new ArrayList<>();
    if (count > 0) {
      this.controlCodes.add(new ControlCode("Sound suppressors or moderators, special gun-mountings, optical weapon sights and flash suppressors for smooth-bore weapons", "ML1d"));
      if (count > 1) {
        this.controlCodes.add(new ControlCode("Equipment designed or modified for special fibre surface treatment or for producing resin impregnated fibre prepregs and metal coated fibre preforms, for composite structures, laminates and manufactures made either with organic matrix or metal matrix utilising fibrous or filamentary reinforcements", "1B101d"));
        if (count > 2) {
          this.controlCodes.add(new ControlCode("Unmanned aerial vehicle (UAV) recovery systems", "PL9009a2g"));
        }
      }
    }
  }
}
