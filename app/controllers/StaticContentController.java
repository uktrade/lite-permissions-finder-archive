package controllers;


import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.inject.Inject;
import play.mvc.Controller;
import play.mvc.Result;
import play.twirl.api.Html;
import scala.Option;
import views.html.util.heading;
import views.html.util.headingBanner;

import java.io.IOException;
import java.net.URL;
import java.util.function.Function;

public class StaticContentController extends Controller {

  private final views.html.staticContent staticContent;

  @Inject
  public StaticContentController(views.html.staticContent staticContent) {
    this.staticContent = staticContent;
  }

  public enum StaticHtml {
    BROKERING("tradetypes/brokering.html", "Trade controls, trafficking and brokering"),
    NOT_APPLICABLE("notApplicable.html", "No licence available", HEADING_BANNER_FUNC.apply("You have reached the end of this service")),
    NOT_IMPLEMENTED("notImplemented.html", "This section is currently under development"),
    OTHER_CONTROL_LIST("otherControlList.html", "Check your item against another control list"),
    TOO_MANY_CUSTOMERS_OR_SITES("tooManyCustomersOrSites.html", "Too many customers or sites"),
    TRANSHIPMENT("tradetypes/transhipment.html", "Transhipment"),
    UNKNOWN_OUTCOME("unknownOutcome.html", "Unknown outcome");

    private final String filename;
    private final String title;
    private final Html pageHeading;

    StaticHtml(String filename, String title, Html pageHeading) {
      this.filename = filename;
      this.title = title;
      this.pageHeading = pageHeading;
    }

    StaticHtml(String filename, String title) {
      // Standard page headings are shown by default
      this(filename, title, HEADING_STANDARD_FUNC.apply(title));
    }

  }

  private static final Function<String, Html> HEADING_STANDARD_FUNC = title -> heading.render(title, "heading-large", false);
  private static final Function<String, Html> HEADING_BANNER_FUNC = headingBanner::render;

  public Result renderStaticHtml(StaticHtml staticHtml) {
    try {
      URL resource = getClass().getClassLoader().getResource("static/html/" + staticHtml.filename);
      if (resource == null) {
        throw new RuntimeException("Not a file: " + staticHtml.filename);
      }

      return ok(staticContent.render(staticHtml.title, Option.apply(staticHtml.pageHeading), new Html(Resources.toString(resource, Charsets.UTF_8))));

    } catch (IOException e) {
      throw new RuntimeException("Failed to read", e);
    }
  }

  public Result renderBrokering() {
    return renderStaticHtml(StaticHtml.BROKERING);
  }

  public Result renderInvalidUserAccount() {
    return renderStaticHtml(StaticHtml.TOO_MANY_CUSTOMERS_OR_SITES);
  }

  public Result renderNotImplemented() {
    return renderStaticHtml(StaticHtml.NOT_IMPLEMENTED);
  }

  public Result renderOtherControlList() {
    return renderStaticHtml(StaticHtml.OTHER_CONTROL_LIST);
  }

  public Result renderTranshipment() {
    return renderStaticHtml(StaticHtml.TRANSHIPMENT);
  }

  public Result renderUnknownOutcome() {
    return renderStaticHtml(StaticHtml.UNKNOWN_OUTCOME);
  }

}
