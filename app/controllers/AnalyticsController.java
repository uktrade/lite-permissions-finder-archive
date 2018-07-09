package controllers;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import models.template.AnalyticsSnippets;
import play.mvc.Result;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class AnalyticsController {

  private final AnalyticsSnippets analyticsSnippets;

  @Inject
  public AnalyticsController(AnalyticsSnippets analyticsSnippets) {
    this.analyticsSnippets = analyticsSnippets;
  }

  public Result getAnalyticsJs() {
    String dateTime = DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now().plusDays(1));
    return ok(analyticsSnippets.getHeadJs()).as("text/javascript").withHeader("Expires", dateTime);
  }
}
