package controllers;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.inject.Inject;
import components.services.FlashService;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import play.twirl.api.Html;
import scala.Option;
import triage.session.SessionService;
import triage.session.TriageSession;
import views.html.util.heading;
import views.html.util.headingBanner;

import java.io.IOException;
import java.net.URL;
import java.util.function.Function;

public class StaticContentController extends Controller {

  private final FlashService flashService;
  private final SessionService sessionService;
  private final views.html.staticContent staticContent;

  @Inject
  public StaticContentController(FlashService flashService, SessionService sessionService,
                                 views.html.staticContent staticContent) {
    this.flashService = flashService;
    this.sessionService = sessionService;
    this.staticContent = staticContent;
  }

  public enum StaticHtml {
    BROKERING("tradetypes/brokering.html", "Trade controls, trafficking and brokering", false),
    MORE_INFORMATION_REQUIRED("moreInformationRequired.html", "Find out more about your item", true),
    NOT_APPLICABLE("notApplicable.html", "No licence available", false, HEADING_BANNER_FUNC.apply("You have reached the end of this service")),
    OTHER_CONTROL_LIST("otherControlList.html", "Check your item against another control list", true),
    TOO_MANY_CUSTOMERS_OR_SITES("tooManyCustomersOrSites.html", "Too many customers or sites", false),
    TRANSHIPMENT("tradetypes/transhipment.html", "Transhipment", false),
    UNKNOWN_OUTCOME("unknownOutcome.html", "Unknown outcome", false);

    private final String filename;
    private final String title;
    private final Html pageHeading;
    private final boolean showBackLink;

    StaticHtml(String filename, String title, boolean showBackLink, Html pageHeading) {
      this.filename = filename;
      this.title = title;
      this.pageHeading = pageHeading;
      this.showBackLink = showBackLink;
    }

    StaticHtml(String filename, String title, boolean showBackLink) {
      // Standard page headings are shown by default
      this(filename, title, showBackLink, HEADING_STANDARD_FUNC.apply(title));
    }

  }

  private static final Function<String, Html> HEADING_STANDARD_FUNC = title -> heading.render(title, "heading-large", false);
  private static final Function<String, Html> HEADING_BANNER_FUNC = headingBanner::render;

  public Result renderStaticHtml(StaticHtml staticHtml, String sessionId) {
    String resumeCode;
    if (sessionId != null) {
      TriageSession triageSession = sessionService.getSessionById(sessionId);
      if (triageSession != null) {
        resumeCode = triageSession.getResumeCode();
      } else {
        return unknownSession(sessionId);
      }
    } else {
      resumeCode = "";
    }

    try {
      URL resource = getClass().getClassLoader().getResource("static/html/" + staticHtml.filename);
      if (resource == null) {
        throw new RuntimeException("Not a file: " + staticHtml.filename);
      } else {
        return ok(staticContent.render(staticHtml.title,
            Option.apply(staticHtml.pageHeading),
            new Html(Resources.toString(resource, Charsets.UTF_8)),
            staticHtml.showBackLink,
            resumeCode));
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to read", e);
    }
  }

  public Result renderBrokering() {
    return renderStaticHtml(StaticHtml.BROKERING, null);
  }

  public Result renderInvalidUserAccount() {
    return renderStaticHtml(StaticHtml.TOO_MANY_CUSTOMERS_OR_SITES, null);
  }

  public Result renderMoreInformationRequired(String sessionId) {
    return renderStaticHtml(StaticHtml.MORE_INFORMATION_REQUIRED, sessionId);
  }

  public Result renderOtherControlList(String sessionId) {
    return renderStaticHtml(StaticHtml.OTHER_CONTROL_LIST, sessionId);
  }

  public Result renderTranshipment() {
    return renderStaticHtml(StaticHtml.TRANSHIPMENT, null);
  }

  public Result renderUnknownOutcome() {
    return renderStaticHtml(StaticHtml.UNKNOWN_OUTCOME, null);
  }

  private Result unknownSession(String sessionId) {
    flashService.flashInvalidSession();
    Logger.error("Unknown or blank sessionId " + sessionId);
    return redirect(routes.StartApplicationController.createApplication());
  }

}
