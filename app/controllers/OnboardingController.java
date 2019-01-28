package controllers;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;
import static play.mvc.Results.redirect;

import com.google.inject.Inject;
import components.cms.dao.JourneyDao;
import components.cms.parser.workbook.NavigationParser;
import controllers.guard.SessionGuardAction;
import lombok.AllArgsConstructor;
import models.cms.Journey;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints;
import play.mvc.Result;
import play.mvc.With;
import triage.session.SessionService;
import triage.session.TriageSession;
import utils.ListNameToFriendlyNameUtil;
import utils.common.SelectOption;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

@With(SessionGuardAction.class)
@AllArgsConstructor(onConstructor = @__({ @Inject }))
public class OnboardingController {

  private final FormFactory formFactory;
  private final SessionService sessionService;
  private final views.html.onboardingContent onboardingContent;
  private final JourneyDao journeyDao;

  public CompletionStage<Result> renderForm(String sessionId) {
    String resumeCode = sessionService.getSessionById(sessionId).getResumeCode();
    return completedFuture(ok(onboardingContent.render(formFactory.form(OnboardingForm.class), getSelectOptions(),
        sessionId, resumeCode)));
  }

  public Result handleSubmit(String sessionId) {
    Form<OnboardingForm> form = formFactory.form(OnboardingForm.class).bindFromRequest();
    TriageSession triageSession = sessionService.getSessionById(sessionId);
    String resumeCode = triageSession.getResumeCode();

    if (form.hasErrors()) {
      return ok(onboardingContent.render(form, getSelectOptions(), sessionId, resumeCode));
    }

    SpeciallyDesigned isSpecialParam = form.get().speciallyDesigned;
    Journey journey;

    switch (isSpecialParam) {
      case UK_MILITARY_LIST:
        journey = journeyDao.getJourneysByJourneyName(NavigationParser.sheetIndices.get(2)).get(0);
        sessionService.updateJourneyId(sessionId, journey.getId().toString());
        return redirect(routes.StageController.handleSubmit(journey.getInitialStageId().toString(), sessionId));
      case DUAL_USE_LIST:
        journey = journeyDao.getJourneysByJourneyName(NavigationParser.sheetIndices.get(3)).get(0);
        sessionService.updateJourneyId(sessionId, journey.getId().toString());
        return redirect(routes.StageController.handleSubmit(journey.getInitialStageId().toString(), sessionId));
      default:
        return redirect(routes.StaticContentController.renderMoreInformationRequired(sessionId));
    }
  }

  private List<SelectOption> getSelectOptions() {
    List<SelectOption> optionList = new ArrayList<>();
    optionList.add(new SelectOption(SpeciallyDesigned.UK_MILITARY_LIST.toString(),
            ListNameToFriendlyNameUtil.GetFriendlyNameFromListName(SpeciallyDesigned.UK_MILITARY_LIST.toString())));
    optionList.add(new SelectOption(SpeciallyDesigned.DUAL_USE_LIST.toString(),
            ListNameToFriendlyNameUtil.GetFriendlyNameFromListName(SpeciallyDesigned.DUAL_USE_LIST.toString())));
    optionList.add(new SelectOption(SpeciallyDesigned.DONT_KNOW.toString(), "I don't know"));
    return optionList;
  }

  public enum SpeciallyDesigned {
    UK_MILITARY_LIST,
    DUAL_USE_LIST,
    DONT_KNOW
  }

  public static class OnboardingForm {
    @Constraints.Required(message = "Select one option")
    public SpeciallyDesigned speciallyDesigned;
  }

}
