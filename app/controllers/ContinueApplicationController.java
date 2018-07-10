package controllers;

import static play.mvc.Results.ok;
import static play.mvc.Results.redirect;

import com.google.inject.Inject;
import models.view.form.ContinueApplicationForm;
import org.apache.commons.lang3.StringUtils;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Result;
import triage.session.SessionService;
import triage.session.TriageSession;

public class ContinueApplicationController {

  private final FormFactory formFactory;
  private final SessionService sessionService;
  private final views.html.continueApplication continueApplication;

  @Inject
  public ContinueApplicationController(FormFactory formFactory, SessionService sessionService,
                                       views.html.continueApplication continueApplication) {
    this.formFactory = formFactory;
    this.sessionService = sessionService;
    this.continueApplication = continueApplication;
  }

  public Result renderForm() {
    return ok(continueApplication.render(formFactory.form(ContinueApplicationForm.class)));
  }

  public Result handleSubmit() {
    Form<ContinueApplicationForm> form = formFactory.form(ContinueApplicationForm.class).bindFromRequest();
    if (form.hasErrors()) {
      return ok(continueApplication.render(form));
    } else {
      String resumeCode = form.get().resumeCode;
      if (StringUtils.isNoneBlank(resumeCode)) {
        String alphanumericResumeCode = resumeCode.replaceAll("[^0-9a-zA-Z]", "").toUpperCase();
        TriageSession triageSession = sessionService.getSessionByResumeCode(alphanumericResumeCode);
        if (triageSession != null) {
          String sessionId = triageSession.getId();
          Long lastStageId = triageSession.getLastStageId();
          if (lastStageId != null) {
            return redirect(routes.StageController.render(Long.toString(lastStageId), sessionId));
          } else {
            return redirect(routes.OnboardingController.renderForm(sessionId));
          }
        } else {
          Form formWithError = form.withError("resumeCode", "You have entered an invalid reference code");
          return ok(continueApplication.render(formWithError));
        }
      } else {
        Form formWithError = form.withError("resumeCode", "You have entered an invalid reference code");
        return ok(continueApplication.render(formWithError));
      }
    }
  }

}

