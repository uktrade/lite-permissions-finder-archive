package controllers;

import static play.mvc.Results.ok;
import static play.mvc.Results.redirect;

import com.google.inject.Inject;
import components.common.client.NotificationServiceClient;
import components.services.notification.PermissionsFinderNotificationClient;
import models.view.form.StartApplicationForm;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Result;
import triage.session.TriageSession;
import triage.session.SessionService;
import views.html.startApplication;

public class StartApplicationController {

  private final FormFactory formFactory;
  private final SessionService sessionService;
  private final PermissionsFinderNotificationClient permissionsFinderNotificationClient;

  @Inject
  public StartApplicationController(FormFactory formFactory, SessionService sessionService,
                                    NotificationServiceClient notificationServiceClient,
                                    PermissionsFinderNotificationClient permissionsFinderNotificationClient) {
    this.formFactory = formFactory;
    this.sessionService = sessionService;
    this.permissionsFinderNotificationClient = permissionsFinderNotificationClient;
  }

  public Result renderForm() {
    TriageSession triageSession = sessionService.createNewSession();
    return ok(startApplication.render(formFactory.form(StartApplicationForm.class), triageSession.getSessionId(),
        triageSession.getResumeCode()));
  }

  public Result handleSubmit(String sessionId) {
    TriageSession triageSession = sessionService.getSessionById(sessionId);
    if (triageSession == null) {
      Logger.error("Unknown sessionId " + sessionId);
      return redirect(routes.StartApplicationController.renderForm());
    } else {
      Form<StartApplicationForm> form = formFactory.form(StartApplicationForm.class).bindFromRequest();
      if (form.hasErrors()) {
        return ok(startApplication.render(form, triageSession.getSessionId(), triageSession.getResumeCode()));
      } else {
        String emailAddress = form.get().emailAddress;
        if (StringUtils.isNoneBlank(emailAddress)) {
          String resumeCode = sessionService.getSessionById(sessionId).getResumeCode();
          permissionsFinderNotificationClient.sendApplicationReferenceEmail(emailAddress.trim(), resumeCode);
        }
        return redirect(routes.StageController.index(sessionId));
      }
    }
  }

}
