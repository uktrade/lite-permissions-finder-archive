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
import views.html.onboardingContent;

import java.util.concurrent.CompletionStage;

public class OnboardingController {

  private final FormFactory formFactory;

  @Inject
  public OnboardingController(FormFactory formFactory, SessionService sessionService) {
    this.formFactory = formFactory;
  }

  public CompletionStage<Result> renderForm(String sessionId) {
    return completedFuture(ok(onboardingContent.render(formFactory.form(OnboardingForm.class), sessionId)));
  }

  public CompletionStage<Result> handleSubmit(String sessionId) {
    Form<OnboardingForm> form = formFactory.form(OnboardingForm.class).bindFromRequest();

    if (form.hasErrors()) {
      return completedFuture(ok(onboardingContent.render(form, sessionId)));
    }

    String isSpecialParam = form.get().isSpecial;

    switch (isSpecialParam) {
      case "YES":
        return completedFuture(redirect(routes.StageController.index(sessionId)));
      case "NO":
        return completedFuture(ok("Link to EU Dual-Use List holding page - link TBC"));
      default:
        return completedFuture(ok("Link to ‘I don’t know’ page - link TBC"));
    }

  }

  public static class OnboardingForm {

    @Constraints.Required(message = "Select one option")
    public String isSpecial;
  }

}
