package exceptions;

import static play.mvc.Results.badRequest;
import static play.mvc.Results.notFound;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import controllers.common.ErrorHandler;
import org.apache.commons.lang.StringUtils;
import play.Environment;
import play.Logger;
import play.api.OptionalSourceMapper;
import play.mvc.Http;
import play.mvc.Result;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class PermissionsFinderErrorHandler extends ErrorHandler {

  private final views.html.licencefinder.errorPage errorPage;

  @Inject
  public PermissionsFinderErrorHandler(Environment environment, OptionalSourceMapper sourceMapper,
                                       Config config, views.html.licencefinder.errorPage errorPage) {
    super(environment, sourceMapper, config);
    this.errorPage = errorPage;
  }

  @Override
  public CompletionStage<Result> onClientError(Http.RequestHeader request, int statusCode, String message) {
    if (statusCode == Http.Status.NOT_FOUND ||
        (statusCode == Http.Status.BAD_REQUEST && StringUtils.startsWith(message, "Missing parameter"))) {
      Logger.error(statusCode + " " + message);
      return CompletableFuture.completedFuture(notFound(errorPage.render("This page could not be found")));
    } else {
      return super.onClientError(request, statusCode, message);
    }
  }

  @Override
  public CompletionStage<Result> onServerError(Http.RequestHeader request, Throwable exception) {
    if (exception instanceof UnknownParameterException) {
      Logger.warn("onServerError", exception);
      return CompletableFuture.completedFuture(badRequest(errorPage.render("This page could not be found")));
    } else {
      return super.onServerError(request, exception);
    }
  }

}
