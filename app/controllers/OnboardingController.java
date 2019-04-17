package controllers;

import com.google.inject.Inject;
import components.services.JourneyService;
import controllers.guard.SessionGuardAction;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import models.cms.Journey;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints;
import play.mvc.Result;
import play.mvc.With;
import triage.session.SessionService;
import triage.session.TriageSession;
import utils.common.SelectOption;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;
import static play.mvc.Results.redirect;

@With(SessionGuardAction.class)
@AllArgsConstructor(onConstructor = @__({ @Inject }))
public class OnboardingController {

  private final FormFactory formFactory;
  private final SessionService sessionService;
  private final views.html.onboardingContent onboardingContent;
  private final JourneyService journeyService;

  private final String DONT_KNOW = "DONT_KNOW";
  private final String FIREARMS = "FIREARMS";
  private final String SOFTWARE_AND_TECHNOLOGY = "SOFTWARE_AND_TECHNOLOGY";

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

    String isSpecialParam = form.get().speciallyDesigned;
    Journey journey = journeyService.getByJourneyName(isSpecialParam);

    if (journey == null) {
      return redirect(routes.StaticContentController.renderMoreInformationRequired(sessionId));
    }

    sessionService.bindSessionToJourney(sessionId, journey);
    return redirect(routes.StageController.handleSubmit(journey.getInitialStageId().toString(), sessionId));
  }

  private List<SelectOption> getSelectOptions() {
    List<SelectOption> optionList = new ArrayList<>();

    optionList.add(new SelectOption(FIREARMS, "Firearms", true));
    optionList.add(new SelectOption(SOFTWARE_AND_TECHNOLOGY, "Software and Technology", true));

    for (Journey journey : journeyService.getAllJourneys()) {
      optionList.add(new SelectOption(journey.getJourneyName(), journey.getFriendlyJourneyName(), false));
    }

    optionList.add(new SelectOption(DONT_KNOW, "I don't know", true));
    return optionList;
  }

  public static class OnboardingForm {
    @Constraints.Required(message = "Select one option")
    public String speciallyDesigned;
  }
}
