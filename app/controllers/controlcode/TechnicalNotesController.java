package controllers.controlcode;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.frontend.FrontendServiceClient;
import components.services.controlcode.frontend.FrontendServiceResult;
import controllers.ErrorController;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import views.html.controlcode.technicalNotes;

import java.util.concurrent.CompletionStage;

public class TechnicalNotesController {

  private final FormFactory formFactory;

  private final PermissionsFinderDao dao;

  private final HttpExecutionContext ec;

  private final FrontendServiceClient frontendServiceClient;

  private final ErrorController errorController;

  private final SearchAgainController searchAgainController;

  private final ConfirmationController confirmationController;

  @Inject
  public TechnicalNotesController(FormFactory formFactory,
                                  PermissionsFinderDao dao,
                                  HttpExecutionContext ec,
                                  FrontendServiceClient frontendServiceClient,
                                  ErrorController errorController,
                                  SearchAgainController searchAgainController,
                                  ConfirmationController confirmationController) {
    this.formFactory = formFactory;
    this.dao = dao;
    this.ec = ec;
    this.frontendServiceClient = frontendServiceClient;
    this.errorController = errorController;
    this.searchAgainController = searchAgainController;
    this.confirmationController = confirmationController;
  }

  public Result renderForm(FrontendServiceResult frontendServiceResult){
    return ok(technicalNotes.render(formFactory.form(TechnicalNotesController.TechnicalNotesForm.class), frontendServiceResult));
  }

  public CompletionStage<Result> handleSubmit() {
    return completedFuture(dao.getPhysicalGoodControlCode())
        .thenComposeAsync(frontendServiceClient::get)
        .thenApplyAsync(response -> {
          if (!response.isOk()) {
            return errorController.renderForm("An issue occurred while processing your request, please try again later.");
          }
          else {
            Form<TechnicalNotesForm> form = formFactory.form(TechnicalNotesForm.class).bindFromRequest();
            if (form.hasErrors()) {
              return ok(technicalNotes.render(form, response.getFrontendServiceResult()));
            }

            if ("true".equals(form.get().stillDescribesItems)) {
              return nextScreenTrue(response.getFrontendServiceResult());
            }
            else if ("false".equals(form.get().stillDescribesItems)) {
              return nextScreenFalse(response.getFrontendServiceResult());
            }

            // TODO Handle this branch condition better
            return ok(technicalNotes.render(form, response.getFrontendServiceResult()));
          }
        }, ec.current());
  }

  public Result nextScreenTrue(FrontendServiceResult frontendServiceResult) {
    return confirmationController.renderForm(frontendServiceResult);
  }

  public Result nextScreenFalse(FrontendServiceResult frontendServiceResult) {
    return searchAgainController.renderForm(frontendServiceResult);
  }

  public static class TechnicalNotesForm {

    @Required(message = "You must answer this question")
    public String stillDescribesItems;

    public TechnicalNotesForm(){}

  }

}
