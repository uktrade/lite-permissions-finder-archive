package exceptions;

import static play.mvc.Results.badRequest;
import static play.mvc.Results.notFound;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import controllers.common.ErrorHandler;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.LoggerFactory;
import play.Environment;
import play.api.OptionalSourceMapper;
import play.mvc.Http;
import play.mvc.Result;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class PermissionsFinderErrorHandler extends ErrorHandler {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(PermissionsFinderErrorHandler.class);

  private final views.html.licencefinder.errorPage errorPage;

  @Inject
  public PermissionsFinderErrorHandler(Environment environment, OptionalSourceMapper sourceMapper,
                                       Config config, views.html.licencefinder.errorPage errorPage) {
    super(environment, sourceMapper, config);
    this.errorPage = errorPage;
  }

  @Override
  public CompletionStage<Result> onClientError(Http.RequestHeader request, int statusCode, String message) {
    if (statusCode == Http.Status.NOT_FOUND || statusCode == Http.Status.BAD_REQUEST) {
      LOGGER.warn(statusCode + " " + message);
      return CompletableFuture.completedFuture(notFound(errorPage.render("This page could not be found")));
    } else {
      return super.onClientError(request, statusCode, message);
    }
  }

  @Override
  public CompletionStage<Result> onServerError(Http.RequestHeader request, Throwable exception) {
    if (ExceptionUtils.indexOfThrowable(exception, UnknownParameterException.class) != -1) {
      LOGGER.warn("Unknown parameter", exception);
      return CompletableFuture.completedFuture(badRequest(errorPage.render("This page could not be found")));
    } else {
      return super.onServerError(request, exception);
    }
  }

}
