package components.services;

import com.google.common.base.Stopwatch;
import com.google.inject.Inject;
import play.Logger;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSRequestFilter;

import java.util.concurrent.TimeUnit;

public class ServiceClientLogger {
  private final HttpExecutionContext httpExecutionContext;

  @Inject
  public ServiceClientLogger(HttpExecutionContext httpExecutionContext) {
    this.httpExecutionContext = httpExecutionContext;
  }

  public WSRequestFilter requestFilter(String serviceName, String method) {
    return executor -> request -> {
      Logger.info(String.format("%s service request - URL: %s, method: %s", serviceName, request.getUrl(), method));
      Stopwatch stopwatch = Stopwatch.createStarted();
      return executor.apply(request)
          .thenApplyAsync(response -> {
            Logger.info(String.format("%s service response - URL: %s, status code: %s, status text: %s, completed in %dms",
                serviceName, response.getUri(), response.getStatus(), response.getStatusText(), stopwatch.elapsed(TimeUnit.MILLISECONDS)));
            return response;
          }, httpExecutionContext.current());
    };
  }
}
