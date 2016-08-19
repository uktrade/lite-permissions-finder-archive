package components.services.controlcode.frontend;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Ancestor {

  public final String controlCode;

  public final String friendlyDescription;

  public Ancestor(@JsonProperty("controlCode") String controlCode,
                  @JsonProperty("friendlyDescription") String friendlyDescription) {
    this.controlCode = controlCode;
    this.friendlyDescription = friendlyDescription;
  }

}
