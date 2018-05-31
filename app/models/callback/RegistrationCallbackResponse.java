package models.callback;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

public class RegistrationCallbackResponse {

  @JsonIgnore
  private static final String STATUS_OK = "ok";

  @JsonIgnore
  private static final String STATUS_ERROR = "error";

  private String status;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String errorMessage;

  public RegistrationCallbackResponse status(String status) {
    this.status = status;
    return this;
  }

  public RegistrationCallbackResponse errorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
    return this;
  }

  public String getStatus() {
    return status;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public static RegistrationCallbackResponse okResponse() {
    return new RegistrationCallbackResponse().status(STATUS_OK);
  }

  public static RegistrationCallbackResponse errorResponse(String errorMessage) {
    return new RegistrationCallbackResponse().status(STATUS_ERROR)
        .errorMessage(errorMessage);
  }
}
