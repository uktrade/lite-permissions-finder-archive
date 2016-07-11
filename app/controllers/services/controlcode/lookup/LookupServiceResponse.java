package controllers.services.controlcode.lookup;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.services.controlcode.ServiceResponseStatus;
import play.libs.Json;

public class LookupServiceResponse {

  private final LookupServiceResult lookupServiceResult;

  private final LookupServiceError lookupServiceError;

  private final ServiceResponseStatus status;

  public static class Builder {

    private LookupServiceResult lookupServiceResult;

    private LookupServiceError lookupServiceError;

    private ServiceResponseStatus status;

    public LookupServiceResponse build() {
      return new LookupServiceResponse(this);
    }

    public Builder setLookupServiceResult(JsonNode responseJson) {
      this.lookupServiceResult = Json.fromJson(responseJson, LookupServiceResult.class);
      return this;
    }

    public Builder setLookupServiceError(JsonNode responseJson) {
      this.lookupServiceError = Json.fromJson(responseJson, LookupServiceError.class);
      return this;
    }

    public Builder setStatus(ServiceResponseStatus status) {
      this.status = status;
      return this;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public LookupServiceResponse(Builder builder) {
    this.lookupServiceResult = builder.lookupServiceResult;
    this.lookupServiceError = builder.lookupServiceError;
    this.status = builder.status;
  }

  public LookupServiceResult getLookupServiceResult() {
    return lookupServiceResult;
  }

  public LookupServiceError getLookupServiceError() {
    return lookupServiceError;
  }

  public ServiceResponseStatus getStatus() {
    return status;
  }

  public boolean isOk() {
    return this.status == ServiceResponseStatus.SUCCESS;
  }

}