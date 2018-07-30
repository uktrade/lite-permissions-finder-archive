package models.callback;

import com.fasterxml.jackson.annotation.JsonInclude;
import play.mvc.Http;

public class RegistrationCallbackResponse {

  private int status;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String errorMessage;

  public RegistrationCallbackResponse setStatus(int status) {
    this.status = status;
    return this;
  }

  public RegistrationCallbackResponse setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
    return this;
  }

  public int getStatus() {
    return status;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public static RegistrationCallbackResponse okResponse() {
    return new RegistrationCallbackResponse().setStatus(Http.Status.OK);
  }

  public static RegistrationCallbackResponse errorResponse(String errorMessage) {
    return new RegistrationCallbackResponse().setStatus(Http.Status.BAD_REQUEST)
        .setErrorMessage(errorMessage);
  }
}
