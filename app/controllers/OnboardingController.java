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

    optionList.add(new SelectOption(FIREARMS, "A firearm (such a shotgun or rifle) or related to firearms (such as ammunition or accessories)?", true));
    optionList.add(new SelectOption(SOFTWARE_AND_TECHNOLOGY, "Technology, software or an item that holds software? (eg manuals, blueprints, training and consultancy, data storage)", true));

    for (Journey journey : journeyService.getAllJourneys()) {
      optionList.add(new SelectOption(journey.getJourneyName(), alterJourneyNameForTriage(journey.getFriendlyJourneyName()), false));
    }

    optionList.add(new SelectOption(DONT_KNOW, "I don't know", true));
    return optionList;
  }

  private String alterJourneyNameForTriage(String name) {
      if (name.equals("UK Military List")) {
        return "Goods designed originally for military (armed forces) use? (eg vehicles, protective clothing, imaging equipment)";
      } else if (name.equals("Dual-Use List")) {
        return "Goods that can be used for both public and military use (dual-use)? (eg detection equipment, SatNav/GPS)";
      } else if (name.equals("Human Rights")) {
        return "Goods that may be used for human torture or capital punishment? (eg restraints, products for lethal injection)";
      } else if (name.equals("Paramilitary")) {
        return "Goods designed for paramilitary, security or police? (eg riot control equipment. This includes reproduction firearms)";
      } else if (name.equals("Controlled Radioactive Sources")){
        return "Radioactive substances? (eg Co-60 (Cobalt 60), Yb-169 (Ytterbium 169))";
      } else {
        return name;
      }
  }

  public static class OnboardingForm {
    @Constraints.Required(message = "Select one option")
    public String speciallyDesigned;
  }
}
