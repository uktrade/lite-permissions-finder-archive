package controllers;

import static play.mvc.Controller.flash;
import static play.mvc.Results.ok;
import static play.mvc.Results.redirect;

import com.google.inject.Inject;
import components.services.notification.PermissionsFinderNotificationClient;
import components.services.ogels.applicable.ApplicableOgelServiceClient;
import models.view.form.StartApplicationForm;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import play.data.Form;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import triage.session.SessionService;
import triage.session.TriageSession;
import utils.MyLogger;
import utils.MyLoggerFactory;

import java.util.concurrent.CompletionStage;

public class StartApplicationController {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(StartApplicationController.class);
  private static final MyLogger MY_LOGGER = MyLoggerFactory.getLogger(StartApplicationController.class);

  private final ApplicableOgelServiceClient applicableOgelServiceClient;
  private final FormFactory formFactory;
  private final SessionService sessionService;
  private final PermissionsFinderNotificationClient permissionsFinderNotificationClient;
  private final HttpExecutionContext httpExecutionContext;
  private final views.html.startApplication startApplication;

  @Inject
  public StartApplicationController(
      ApplicableOgelServiceClient applicableOgelServiceClient,
      FormFactory formFactory, SessionService sessionService,
      PermissionsFinderNotificationClient permissionsFinderNotificationClient,
      HttpExecutionContext httpExecutionContext, views.html.startApplication startApplication) {
    this.applicableOgelServiceClient = applicableOgelServiceClient;
    this.formFactory = formFactory;
    this.sessionService = sessionService;
    this.permissionsFinderNotificationClient = permissionsFinderNotificationClient;
    this.httpExecutionContext = httpExecutionContext;
    this.startApplication = startApplication;
  }

  public CompletionStage<Result> createApplication() {
    MY_LOGGER.error("createApplication 1");
    if (StringUtils.isNoneBlank(flash("error"))) {
      flash("error", flash("error"));
      flash("detail", flash("detail"));
    }

    TriageSession triageSession = sessionService.createNewSession();
    return applicableOgelServiceClient.get(null, null, null, null)
        .handleAsync((response, error) -> {
              MY_LOGGER.error("createApplication 2");
              return redirect(routes.StartApplicationController.renderStartApplication(triageSession.getId()));
            },
            httpExecutionContext.current())
        .toCompletableFuture();
  }

  public Result renderStartApplication(String sessionId) {
    TriageSession triageSession = sessionService.getSessionById(sessionId);
    if (triageSession == null) {
      LOGGER.error("Unknown sessionId " + sessionId);
      return redirect(routes.StartApplicationController.createApplication());
    } else {
      return ok(startApplication.render(formFactory.form(StartApplicationForm.class), triageSession.getId(),
          triageSession.getResumeCode()));
    }
  }

  public Result handleSubmit(String sessionId) {
    TriageSession triageSession = sessionService.getSessionById(sessionId);
    if (triageSession == null) {
      LOGGER.error("Unknown sessionId " + sessionId);
      return redirect(routes.StartApplicationController.createApplication());
    } else {
      Form<StartApplicationForm> form = formFactory.form(StartApplicationForm.class).bindFromRequest();
      if (form.hasErrors()) {
        return ok(startApplication.render(form, triageSession.getId(), triageSession.getResumeCode()));
      } else {
        String emailAddress = form.get().emailAddress;
        if (StringUtils.isNoneBlank(emailAddress)) {
          String resumeCode = triageSession.getResumeCode();
          permissionsFinderNotificationClient.sendApplicationReferenceEmail(emailAddress.trim(), resumeCode);
        }
        return redirect(routes.OnboardingController.renderForm(sessionId));
      }
    }
  }

}
