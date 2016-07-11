package controllers.services.controlcode.lookup;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import controllers.services.controlcode.ServiceResponseStatus;
import play.Logger;
import play.libs.ws.WSClient;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class LookupServiceClient {

  private static final long REQUEST_TIMEOUT_MS = 10000; //10 Seconds

  private final String webServiceUrl;

  private final WSClient ws;

  @Inject
  public LookupServiceClient(WSClient ws, @Named("controlCodeLookupServiceHostname") String webServiceUrl){
    this.ws = ws;
    this.webServiceUrl = "http://" + webServiceUrl + "/control-codes";
  }

  public CompletionStage<LookupServiceResponse> lookup(String controlCode) {
    return ws.url(webServiceUrl + "/" + controlCode)
        .setRequestTimeout(REQUEST_TIMEOUT_MS).get()
        .handle((response, error) -> {
          if (error != null) {
            Logger.error("Unchecked exception in ControlCodeLookupService");
            Logger.error(error.getMessage(), error);
            return CompletableFuture.completedFuture(
                LookupServiceResponse.builder()
                    .setStatus(ServiceResponseStatus.UNCHECKED_EXCEPTION)
                    .build()
            );
          }
          else if (response.getStatus() != 200) {
            LookupServiceResponse serviceResponse;
            String errorMessage = "";
            if(response.asJson() != null) {
              serviceResponse = LookupServiceResponse.builder()
                  .setLookupServiceError(response.asJson())
                  .setStatus(ServiceResponseStatus.UNEXPECTED_HTTP_STATUS_CODE)
                  .build();
              errorMessage = serviceResponse.getLookupServiceError().getMessage();
            }
            else {
              serviceResponse = LookupServiceResponse.builder()
                  .setStatus(ServiceResponseStatus.UNEXPECTED_HTTP_STATUS_CODE)
                  .build();
            }
            Logger.error("Unexpected HTTP status code from ControlCodeLookupService: {} {}", response.getStatus(), errorMessage);
            return CompletableFuture.completedFuture(serviceResponse);
          }
          else {
            return CompletableFuture.completedFuture(
                LookupServiceResponse.builder()
                    .setLookupServiceResult(response.asJson())
                    .setStatus(ServiceResponseStatus.SUCCESS)
                    .build()
            );
          }
        })
        .thenCompose(Function.identity());
  }
}