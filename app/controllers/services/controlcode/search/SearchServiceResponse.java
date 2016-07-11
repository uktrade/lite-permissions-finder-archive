package controllers.services.controlcode.search;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.services.controlcode.ServiceResponseStatus;
import play.libs.Json;

public class SearchServiceResponse {

  private final SearchServiceResults searchResults;

  private final ServiceResponseStatus status;

  public static class Builder {

    private SearchServiceResults searchResults;

    private ServiceResponseStatus status;

    public SearchServiceResponse build() {
      return new SearchServiceResponse(this);
    }

    public Builder setSearchResults(JsonNode searchResults){
      this.searchResults = Json.fromJson(searchResults, SearchServiceResults.class);
      return this;
    }

    public Builder setStatus(ServiceResponseStatus status){
      this.status = status;
      return this;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private SearchServiceResponse(Builder builder) {
    this.searchResults = builder.searchResults;
    this.status = builder.status;
  }

  public SearchServiceResults getSearchResults() {
    return searchResults;
  }

  public ServiceResponseStatus getStatus() {
    return status;
  }

  public boolean isOk() {
    return this.status == ServiceResponseStatus.SUCCESS;
  }

}
