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
import views.html.controlcode.decontrols;

import java.util.concurrent.CompletionStage;

public class DecontrolsController {

  private final FormFactory formFactory;

  private final PermissionsFinderDao dao;

  private final HttpExecutionContext ec;

  private final FrontendServiceClient frontendServiceClient;

  private final ErrorController errorController;

  private final TechnicalNotesController technicalNotesController;

  private final DestinationCountryController destinationCountryController;

  private final DecontrolledItemController decontrolledItemController;

  @Inject
  public DecontrolsController(FormFactory formFactory,
                              PermissionsFinderDao dao,
                              HttpExecutionContext ec,
                              FrontendServiceClient frontendServiceClient,
                              ErrorController errorController,
                              TechnicalNotesController technicalNotesController,
                              DestinationCountryController destinationCountryController,
                              DecontrolledItemController decontrolledItemController) {
    this.formFactory = formFactory;
    this.dao = dao;
    this.ec = ec;
    this.frontendServiceClient = frontendServiceClient;
    this.errorController = errorController;
    this.technicalNotesController = technicalNotesController;
    this.destinationCountryController = destinationCountryController;
    this.decontrolledItemController = decontrolledItemController;
  }

  public Result renderForm(FrontendServiceResult frontendServiceResult) {
    return ok(decontrols.render(formFactory.form(DecontrolsController.DecontrolsForm.class), frontendServiceResult));
  }

  public CompletionStage<Result> handleSubmit(){
    return completedFuture(dao.getPhysicalGoodControlCode())
        .thenComposeAsync(frontendServiceClient::get)
        .thenApplyAsync(response -> {
          if (!response.isOk()) {
            return errorController.renderForm("An issue occurred while processing your request, please try again later.");
          }
          else {
            Form<DecontrolsForm> form = formFactory.form(DecontrolsForm.class).bindFromRequest();

            if (form.hasErrors()) {
              return ok(decontrols.render(form, response.getFrontendServiceResult()));
            }

            String decontrolsDescribeItem = form.field("decontrolsDescribeItem").value();

            if ("true".equals(form.get().decontrolsDescribeItem)) {
              return nextScreenTrue(response.getFrontendServiceResult());
            }
            else if ("false".equals(form.get().decontrolsDescribeItem)) {
              return nextScreenFalse(response.getFrontendServiceResult());
            }

            // TODO Handle this branch condition better
            return ok(decontrols.render(form, response.getFrontendServiceResult()));
          }
        }, ec.current());
  }

  public Result nextScreenTrue(FrontendServiceResult frontendServiceResult){
    return decontrolledItemController.render(frontendServiceResult);
  }

  public Result nextScreenFalse(FrontendServiceResult frontendServiceResult){
    if (frontendServiceResult.controlCodeData.canShowTechnicalNotes()){
      return technicalNotesController.renderForm(frontendServiceResult);
    }
    return destinationCountryController.renderForm();
  }

  public static class DecontrolsForm {

    @Required(message = "You must answer this question")
    public String decontrolsDescribeItem;

    public DecontrolsForm(){}

  }

}
