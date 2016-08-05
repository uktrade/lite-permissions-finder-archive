package controllers.controlcode;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.Inject;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.frontend.ControlCodeData;
import components.services.controlcode.frontend.FrontendServiceClient;
import components.services.controlcode.frontend.FrontendServiceResult;
import controllers.ErrorController;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.controlcode.controlCode;

import java.util.concurrent.CompletionStage;

public class ControlCodeController extends Controller {

  private final FormFactory formFactory;

  private final PermissionsFinderDao dao;

  private final HttpExecutionContext ec;

  private final FrontendServiceClient frontendServiceClient;

  private final ErrorController errorController;

  private final AdditionalSpecificationsController additionalSpecificationsController;

  private final DecontrolsController decontrolsController;

  private final TechnicalNotesController technicalNotesController;

  @Inject
  public ControlCodeController(FormFactory formFactory,
                               PermissionsFinderDao dao,
                               HttpExecutionContext ec,
                               FrontendServiceClient frontendServiceClient,
                               ErrorController errorController,
                               AdditionalSpecificationsController additionalSpecificationsController,
                               DecontrolsController decontrolsController,
                               TechnicalNotesController technicalNotesController) {
    this.formFactory = formFactory;
    this.dao = dao;
    this.ec = ec;
    this.frontendServiceClient = frontendServiceClient;
    this.errorController = errorController;
    this.additionalSpecificationsController = additionalSpecificationsController;
    this.decontrolsController = decontrolsController;
    this.technicalNotesController = technicalNotesController;
  }


  public Result renderForm(FrontendServiceResult frontendServiceResult) {
    return ok(controlCode.render(formFactory.form(ControlCodeForm.class), frontendServiceResult));
  }

  public CompletionStage<Result> handleSubmit() {
    return completedFuture(dao.getPhysicalGoodControlCode())
        .thenComposeAsync(frontendServiceClient::get)
        .thenApplyAsync(response -> {
          if(!response.isOk()) {
            return errorController.renderForm("An issue occurred while processing your request, please try again later.");
          }
          else {
            Form<ControlCodeForm> form = formFactory.form(ControlCodeForm.class).bindFromRequest();

            if (form.hasErrors()) {
              return ok(controlCode.render(form, response.getFrontendServiceResult()));
            }

            String couldDescribeItems = form.field("couldDescribeItems").value();

            if (couldDescribeItems.equals("true")) {
              return nextScreenTrue(response.getFrontendServiceResult());
            }
            else if (couldDescribeItems.equals("false")) {
              return nextScreenFalse(response.getFrontendServiceResult());
            }

            // TODO Handle this branch condition better
            return ok(controlCode.render(form, response.getFrontendServiceResult()));
          }
        }, ec.current());
  }

  public Result nextScreenTrue(FrontendServiceResult frontendServiceResult) {
    ControlCodeData controlCodeData = frontendServiceResult.controlCodeData;
    if (controlCodeData.canShow()) {
      if (controlCodeData.canShowAdditionalSpecifications()) {
        return additionalSpecificationsController.renderForm(frontendServiceResult);
      }
      else if (controlCodeData.canShowDecontrols()) {
        return decontrolsController.renderForm(frontendServiceResult);
      }
      else {
        return technicalNotesController.renderForm(frontendServiceResult);
      }
    }
    else {
      return ok("SHOW COUNTRY SELECT");
    }
  }

  public Result nextScreenFalse(FrontendServiceResult frontendServiceResult) {
    return ok("SEARCH AGAIN SCREEN");
  }

  public static class ControlCodeForm {

    @Required(message = "You must answer this question")
    public String couldDescribeItems;

  }

}