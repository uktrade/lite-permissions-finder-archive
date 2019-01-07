package controllers;

import com.google.inject.Inject;
import components.services.notification.PermissionsFinderNotificationClient;
import models.view.form.StartApplicationForm;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Result;
import triage.session.SessionService;
import triage.session.TriageSession;

import static play.mvc.Controller.flash;
import static play.mvc.Results.ok;
import static play.mvc.Results.redirect;

public class StartApplicationController {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(StartApplicationController.class);

    private final FormFactory formFactory;
    private final SessionService sessionService;
    private final PermissionsFinderNotificationClient permissionsFinderNotificationClient;
    private final views.html.startApplication startApplication;

    @Inject
    public StartApplicationController(FormFactory formFactory, SessionService sessionService,
                                      PermissionsFinderNotificationClient permissionsFinderNotificationClient,
                                      views.html.startApplication startApplication) {
        this.formFactory = formFactory;
        this.sessionService = sessionService;
        this.permissionsFinderNotificationClient = permissionsFinderNotificationClient;
        this.startApplication = startApplication;
    }

    public Result createApplication() {
        if (StringUtils.isNoneBlank(flash("error"))) {
            flash("error", flash("error"));
            flash("detail", flash("detail"));
        }

        TriageSession triageSession = sessionService.createNewSession();
        return redirect(routes.StartApplicationController.renderStartApplication(triageSession.getId()));
    }

    public Result renderStartApplication(String sessionId) {
        TriageSession triageSession = sessionService.getSessionById(sessionId);

        // If the session isn't valid, log and redirect to new application
        if (triageSession == null) {
            LOGGER.error("Unknown sessionId {}", sessionId);
            return redirect(routes.StartApplicationController.createApplication());
        }

        return ok(startApplication.render(formFactory.form(StartApplicationForm.class), triageSession.getId(),
                triageSession.getResumeCode()));
    }

    public Result handleSubmit(String sessionId) {
        TriageSession triageSession = sessionService.getSessionById(sessionId);
        Form<StartApplicationForm> form = formFactory.form(StartApplicationForm.class).bindFromRequest();
        String emailAddress = form.get().emailAddress;

        // If the session isn't valid, log and redirect to new application
        if (triageSession == null) {
            LOGGER.error("Unknown sessionId {}", sessionId);
            return redirect(routes.StartApplicationController.createApplication());
        }

        // If the form has errors, return to form and show error
        if (form.hasErrors()) {
            return ok(startApplication.render(form, triageSession.getId(), triageSession.getResumeCode()));
        }

        // If the user has entered an email address, send them the application reference
        if (StringUtils.isNoneBlank(emailAddress)) {
            String resumeCode = triageSession.getResumeCode();
            permissionsFinderNotificationClient.sendApplicationReferenceEmail(emailAddress.trim(), resumeCode);
        }

        return redirect(routes.OnboardingController.renderForm(sessionId));
    }

}
