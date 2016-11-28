package components.services.controlcode.controls;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ControlCode {
  public final String friendlyDescription;
  public final String controlCode;
  public final int displayOrder;

  public ControlCode(@JsonProperty("friendlyDescription") String friendlyDescription,
                     @JsonProperty("controlCode") String controlCode,
                     @JsonProperty("displayOrder") String displayOrder) {
    this.friendlyDescription = friendlyDescription;
    this.controlCode = controlCode;
    this.displayOrder = Integer.parseInt(displayOrder);
  }
}
