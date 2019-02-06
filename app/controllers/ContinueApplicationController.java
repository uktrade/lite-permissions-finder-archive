package controllers;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;
import static play.mvc.Results.redirect;

import com.google.inject.Inject;
import components.cms.dao.SpreadsheetVersionDao;
import components.services.FlashService;
import controllers.guard.StageGuardAction;
import lombok.AllArgsConstructor;
import models.view.form.ContinueApplicationForm;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Result;
import triage.session.SessionService;
import triage.session.TriageSession;

import java.util.concurrent.CompletionStage;

@AllArgsConstructor(onConstructor = @__({ @Inject }))
public class ContinueApplicationController {

  private final FlashService flashService;
  private final FormFactory formFactory;
  private final SessionService sessionService;
  private final SpreadsheetVersionDao spreadsheetVersionDao;
  private final views.html.continueApplication continueApplication;

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ContinueApplicationController.class);

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

          long spreadsheetVersionId = spreadsheetVersionDao.getLatestSpreadsheetVersion().getId();
          long latestSpreadsheetVersionId = triageSession.getSpreadsheetVersionId();

          if (spreadsheetVersionId != latestSpreadsheetVersionId) {
            LOGGER.warn("SessionId {} has spreadsheetVersionId {} which doesn't match latest spreadsheetVersionId {}",
              sessionId, spreadsheetVersionId, latestSpreadsheetVersionId);
            return unknownSession();
          }

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

  private Result unknownSession() {
    flashService.flashInvalidSession();
    return redirect(routes.StartApplicationController.createApplication());
  }

}

