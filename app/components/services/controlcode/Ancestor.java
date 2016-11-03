package components.services.controlcode;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Ancestor {

  public final String controlCode;

  public final String friendlyDescription;

  public final String alias;

  public Ancestor(@JsonProperty("controlCode") String controlCode,
                  @JsonProperty("friendlyDescription") String friendlyDescription,
                  @JsonProperty("alias") String alias) {
    this.controlCode = controlCode;
    this.friendlyDescription = friendlyDescription;
    this.alias = alias;
  }

}
