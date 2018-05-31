package models.callback;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

public class CallbackResponse {

  @JsonIgnore
  private static final String STATUS_OK = "ok";

  @JsonIgnore
  private static final String STATUS_ERROR = "error";

  private String status;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String errorMessage;

  public CallbackResponse status(String status) {
    this.status = status;
    return this;
  }

  public CallbackResponse errorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
    return this;
  }

  public String getStatus() {
    return status;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public static CallbackResponse okResponse() {
    return new CallbackResponse().status(STATUS_OK);
  }

  public static CallbackResponse errorResponse(String errorMessage) {
    return new CallbackResponse().status(STATUS_ERROR)
        .errorMessage(errorMessage);
  }
}