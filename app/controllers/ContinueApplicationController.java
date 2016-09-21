package controllers;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import journey.Events;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.mvc.Result;
import views.html.continueApplication;

import java.util.concurrent.CompletionStage;

public class ContinueApplicationController {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;

  @Inject
  public ContinueApplicationController(JourneyManager journeyManager, FormFactory formFactory,
                                       PermissionsFinderDao permissionsFinderDao) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
  }

  public Result renderForm() {
    // TODO Do not restore this information after JourneyManager persistence is added
    ContinueApplicationForm formTemplate = new ContinueApplicationForm();
    formTemplate.applicationNumber = permissionsFinderDao.getApplicationCode();
    formTemplate.memorableWord = permissionsFinderDao.getMemorableWord();
    return ok(continueApplication.render(formFactory.form(ContinueApplicationForm.class).fill(formTemplate)));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<ContinueApplicationForm> form = formFactory.form(ContinueApplicationForm.class).bindFromRequest();
    if ("true".equals(form.field("startApplication").value())) {
      // TODO Convert to injected link in view
      return journeyManager.performTransition(Events.START_APPLICATION);
    }
    if (form.hasErrors()) {
      return completedFuture(ok(continueApplication.render(form)));
    }
    String applicationNumber = form.get().applicationNumber;
    String memorableWord = form.get().memorableWord;
    if (applicationNumber != null && !applicationNumber.isEmpty() && memorableWord != null && !memorableWord.isEmpty()) {
      if (applicationNumber.equals(permissionsFinderDao.getApplicationCode()) && memorableWord.equals(permissionsFinderDao.getMemorableWord())) {
        journeyManager.performTransition(Events.APPLICATION_FOUND);
      }
      return journeyManager.performTransition(Events.APPLICATION_NOT_FOUND);
    }
    return completedFuture(badRequest("Unhandled form state"));
  }

  public static class ContinueApplicationForm {

    @Required(message = "You must enter your application number")
    public String applicationNumber;

    @Required(message = "You must enter your memorable word")
    public String memorableWord;

  }

}

