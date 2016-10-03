package components.services.ogels.registration;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OgelRegistrationServiceResult {

  public final String status;

  public final String redirectUrl;

  public OgelRegistrationServiceResult(@JsonProperty("status") String status,
                                       @JsonProperty("redirectUrl") String redirectUrl) {
    this.status = status;
    this.redirectUrl = redirectUrl;
  }

}
