package controllers;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import models.template.AnalyticsConfig;
import org.apache.commons.lang3.StringUtils;
import play.mvc.Result;
import views.js.analytics.analytics;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class AnalyticsController {

  private final AnalyticsConfig analyticsConfig;

  @Inject
  public AnalyticsController(AnalyticsConfig analyticsConfig) {
    this.analyticsConfig = analyticsConfig;
  }

  public Result getAnalyticsJs() {
    String dateTime = DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now().plusDays(1));
    if (StringUtils.isNotBlank(analyticsConfig.getGoogleAnalyticsId())) {
      return ok(analytics.render(analyticsConfig.getGoogleAnalyticsId())).as("text/javascript").withHeader("Expires", dateTime);
    } else {
      return ok().as("text/javascript");
    }
  }
}
