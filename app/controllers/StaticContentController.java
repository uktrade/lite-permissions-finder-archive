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
    TRANSHIPMENT("tradetypes/transhipment.html", "Transhipment"),
    NOT_APPLICABLE("notApplicable.html", "No licence available", headingBannerFunc.apply("You have reached the end of this service")),
    NOT_IMPLEMENTED("notImplemented.html", "This section is currently under development"),
    VIRTUAL_EU("virtualEU.html", "You do not need a licence"),
    TOO_MANY_CUSTOMERS_OR_SITES("tooManyCustomersOrSites.html", "Too many customers or sites"),
    UNKNOWN_OUTCOME("unknownOutcome.html", "Unknown outcome");

    StaticHtml(String filename, String title, Html pageHeading) {
      this.filename = filename;
      this.title = title;
      this.pageHeading = pageHeading;
    }

    StaticHtml(String filename, String title) {
      // Standard page headings are shown by default
      this(filename, title, headingStandardFunc.apply(title));
    }

    private final String filename;
    private final String title;
    private final Html pageHeading;

  }

  private static final Function<String, Html> headingStandardFunc = title -> heading.render(title, "heading-large", false);
  private final static Function<String, Html> headingBannerFunc = headingBanner::render;

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

  public Result renderTranshipment() {
    return renderStaticHtml(StaticHtml.TRANSHIPMENT);
  }

  public Result renderNotImplemented() {
    return renderStaticHtml(StaticHtml.NOT_IMPLEMENTED);
  }

  public Result renderVirtualEU() {
    return renderStaticHtml(StaticHtml.VIRTUAL_EU);
  }

  public Result renderInvalidUserAccount() {
    return renderStaticHtml(StaticHtml.TOO_MANY_CUSTOMERS_OR_SITES);
  }

  public Result renderUnknownOutcome() {
    return renderStaticHtml(StaticHtml.UNKNOWN_OUTCOME);
  }

}
