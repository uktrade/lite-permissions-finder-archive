package components.services;

import com.google.common.base.Stopwatch;
import com.google.inject.Inject;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import play.Logger;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSRequest;
import play.libs.ws.WSRequestFilter;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ServiceClientLogger {
  private final HttpExecutionContext httpExecutionContext;

  @Inject
  public ServiceClientLogger(HttpExecutionContext httpExecutionContext) {
    this.httpExecutionContext = httpExecutionContext;
  }

  /**
   * Builds a URL from a {@link play.libs.ws.WSRequest} object
   *
   * @param request
   * @return the URL with parameters, defaults to the requests {@link play.libs.ws.WSRequest#getUrl()} if the build is unsuccessful
   */
  private String requestToURL(WSRequest request) {
    String url = request.getUrl();
    Map<String, Collection<String>> queryParameters = request.getQueryParameters();
    if (!queryParameters.isEmpty()) {
      try {
        URIBuilder uriBuilder = new URIBuilder(request.getUrl());
        List<NameValuePair> nvpList = request.getQueryParameters().entrySet()
            .stream()
            .flatMap(entry -> entry.getValue()
                .stream()
                .map(value -> new BasicNameValuePair(entry.getKey(), value)))
            .collect(Collectors.toList());
        uriBuilder.addParameters(nvpList);
        url = uriBuilder.build().toURL().toString();
      }
      catch (URISyntaxException e) {
        Logger.error("Failed to build URI for request", e);
      }
      catch (MalformedURLException e) {
        Logger.error("Failed to build URL for request", e);
      }
    }
    return url;
  }

  /**
   * Request filter for use with WSClient which logs outgoing http requests for use in service clients.
   *
   * @param serviceName name of the service which the request is sent too (used for logging purposes only)
   * @param method HTTP method used in this request (used for logging purposes only)
   * @return a request filter which logs the request
   */
  public WSRequestFilter requestFilter(String serviceName, String method) {
    return executor -> request -> {
      String url = requestToURL(request);
      Logger.info(String.format("%s service request - URL: %s, method: %s", serviceName, url, method));
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
