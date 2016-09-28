package components.services.ogels.conditions;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ControlCode {
  public final String id;
  public final String controlCode;
  public final String friendlyDescription;

  public ControlCode(@JsonProperty("id") String id,
                     @JsonProperty("controlCode") String controlCode,
                     @JsonProperty("friendlyDescription") String friendlyDescription) {
    this.id = id;
    this.controlCode = controlCode;
    this.friendlyDescription = friendlyDescription;
  }
}
