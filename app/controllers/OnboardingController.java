package controllers;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;
import static play.mvc.Results.redirect;

import com.google.inject.Inject;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints;
import play.mvc.Result;
import triage.session.SessionService;
import utils.common.SelectOption;
import views.html.onboardingContent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class OnboardingController {

  private final FormFactory formFactory;
  private final SessionService sessionService;

  @Inject
  public OnboardingController(FormFactory formFactory, SessionService sessionService) {
    this.formFactory = formFactory;
    this.sessionService = sessionService;
  }

  public CompletionStage<Result> renderForm(String sessionId) {
    String resumeCode = sessionService.getSessionById(sessionId).getResumeCode();
    return completedFuture(ok(onboardingContent.render(formFactory.form(OnboardingForm.class), getSelectOptions(),
        sessionId, resumeCode)));
  }

  public CompletionStage<Result> handleSubmit(String sessionId) {
    Form<OnboardingForm> form = formFactory.form(OnboardingForm.class).bindFromRequest();
    String resumeCode = sessionService.getSessionById(sessionId).getResumeCode();

    if (form.hasErrors()) {
      return completedFuture(ok(onboardingContent.render(form, getSelectOptions(), sessionId, resumeCode)));
    }

    SpeciallyDesigned isSpecialParam = form.get().speciallyDesigned;

    switch (isSpecialParam) {
      case YES:
        return completedFuture(redirect(routes.StageController.index(sessionId)));
      case NO:
        return completedFuture(ok("Link to EU Dual-Use List holding page - link TBC"));
      default:
        return completedFuture(ok("Link to don't know page - link TBC"));
    }
  }

  private List<SelectOption> getSelectOptions() {
    List<SelectOption> optionList = new ArrayList<>();
    optionList.add(new SelectOption(SpeciallyDesigned.YES.toString(), "Yes"));
    optionList.add(new SelectOption(SpeciallyDesigned.NO.toString(), "No"));
    optionList.add(new SelectOption(SpeciallyDesigned.DONT_KNOW.toString(), "I don't know"));
    return optionList;
  }

  public enum SpeciallyDesigned {
    YES,
    NO,
    DONT_KNOW
  }

  public static class OnboardingForm {
    @Constraints.Required(message = "Select one option")
    public SpeciallyDesigned speciallyDesigned;
  }

}
