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
import views.html.continueApplication;

public class ContinueApplicationController {

  private final FormFactory formFactory;
  private final SessionService sessionService;

  @Inject
  public ContinueApplicationController(FormFactory formFactory, SessionService sessionService) {
    this.formFactory = formFactory;
    this.sessionService = sessionService;
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
        TriageSession triageSession = sessionService.getSessionByResumeCode(resumeCode);
        if (triageSession != null) {
          String sessionId = triageSession.getSessionId();
          String stageId = sessionService.getStageId(sessionId);
          if (stageId != null) {
            return redirect(routes.StageController.render(stageId, sessionId));
          } else {
            return redirect(routes.StageController.index(sessionId));
          }
        } else {
          Form formWithError = form.withError("resumeCode", "You have entered an invalid resume code");
          return ok(continueApplication.render(formWithError));
        }
      } else {
        Form formWithError = form.withError("resumeCode", "You have entered an invalid resume code");
        return ok(continueApplication.render(formWithError));
      }
    }
  }

}

