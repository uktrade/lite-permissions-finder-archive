package controllers.controlcode;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.frontend.FrontendServiceClient;
import components.services.controlcode.frontend.FrontendServiceResult;
import controllers.DestinationCountryController;
import controllers.ErrorController;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import views.html.controlcode.additionalSpecifications;

import java.util.concurrent.CompletionStage;

public class AdditionalSpecificationsController {

  private final FormFactory formFactory;

  private final PermissionsFinderDao dao;

  private final HttpExecutionContext ec;

  private final FrontendServiceClient frontendServiceClient;

  private final ErrorController errorController;

  private final DecontrolsController decontrolsController;

  private final TechnicalNotesController technicalNotesController;

  private final SearchAgainController searchAgainController;

  private final DestinationCountryController destinationCountryController;


  @Inject
  public AdditionalSpecificationsController(FormFactory formFactory,
                                            PermissionsFinderDao dao,
                                            HttpExecutionContext ec,
                                            FrontendServiceClient frontendServiceClient,
                                            ErrorController errorController,
                                            DecontrolsController decontrolsController,
                                            TechnicalNotesController technicalNotesController,
                                            SearchAgainController searchAgainController,
                                            DestinationCountryController destinationCountryController) {
    this.formFactory = formFactory;
    this.dao = dao;
    this.ec = ec;
    this.frontendServiceClient = frontendServiceClient;
    this.errorController = errorController;
    this.decontrolsController = decontrolsController;
    this.technicalNotesController = technicalNotesController;
    this.searchAgainController = searchAgainController;
    this.destinationCountryController = destinationCountryController;
  }

  public Result renderForm(FrontendServiceResult frontendServiceResult) {
    return ok(additionalSpecifications.render(formFactory.form(AdditionalSpecificationsForm.class), frontendServiceResult));
  }

  public CompletionStage<Result> handleSubmit() {
    return completedFuture(dao.getPhysicalGoodControlCode())
        .thenComposeAsync(frontendServiceClient::get)
        .thenApplyAsync(response -> {
          if (!response.isOk()) {
            return errorController.renderForm("An issue occurred while processing your request, please try again later.");
          }
          else {
            Form<AdditionalSpecificationsForm> form = formFactory.form(AdditionalSpecificationsForm.class).bindFromRequest();

            if (form.hasErrors()) {
              return ok(additionalSpecifications.render(form, response.getFrontendServiceResult()));
            }

            if ("true".equals(form.get().stillDescribesItems)) {
              return nextScreenTrue(response.getFrontendServiceResult());
            }
            else if ("false".equals(form.get().stillDescribesItems)) {
              return nextScreenFalse(response.getFrontendServiceResult());
            }

            // TODO Handle this branch condition better
            return ok(additionalSpecifications.render(form, response.getFrontendServiceResult()));
          }
        }, ec.current());
  }

  public Result nextScreenTrue(FrontendServiceResult frontendServiceResult){
    if (frontendServiceResult.controlCodeData.canShowDecontrols()) {
      return decontrolsController.renderForm(frontendServiceResult);
    }
    else if (frontendServiceResult.controlCodeData.canShowTechnicalNotes()) {
      return technicalNotesController.renderForm(frontendServiceResult);
    }
    return destinationCountryController.renderForm();
  }

  public Result nextScreenFalse(FrontendServiceResult frontendServiceResult){
    return searchAgainController.render(frontendServiceResult);
  }

  public static class AdditionalSpecificationsForm {

    @Required(message = "You must answer this question")
    public String stillDescribesItems;

    public AdditionalSpecificationsForm(){}

  }
}
