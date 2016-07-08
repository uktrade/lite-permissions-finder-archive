package controllers.search;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;

public class ControlCodeSearchResponse {

  private final ControlCodeSearchResults searchResults;

  private final ControlCodeResponseStatus status;

  public static class Builder {

    private ControlCodeSearchResults searchResults;

    private ControlCodeResponseStatus status;

    public ControlCodeSearchResponse build() {
      return new ControlCodeSearchResponse(this);
    }

    public Builder setSearchResults(JsonNode searchResults){
      this.searchResults = Json.fromJson(searchResults, ControlCodeSearchResults.class);
      return this;
    }

    public Builder setStatus(ControlCodeResponseStatus status){
      this.status = status;
      return this;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private ControlCodeSearchResponse(Builder builder) {
    this.searchResults = builder.searchResults;
    this.status = builder.status;
  }

  public ControlCodeSearchResults getSearchResults() {
    return searchResults;
  }

  public ControlCodeResponseStatus getStatus() {
    return status;
  }

  public boolean isOk() {
    return this.status == ControlCodeResponseStatus.SUCCESS;
  }

}
