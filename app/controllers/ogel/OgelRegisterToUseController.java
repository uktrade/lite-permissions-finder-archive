package controllers.ogel;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.journey.JourneyManager;
import components.common.state.ContextParamManager;
import components.persistence.PermissionsFinderDao;
import components.services.ogels.conditions.OgelConditionsServiceClient;
import components.services.ogels.ogel.OgelServiceClient;
import models.view.RegisterResultView;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import views.html.ogel.ogelRegisterResult;
import views.html.ogel.ogelRegisterToUse;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class OgelRegisterToUseController {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao dao;
  private final HttpExecutionContext httpContext;
  private final OgelServiceClient ogelServiceClient;
  private final OgelConditionsServiceClient ogelConditionsServiceClient;
  private final ContextParamManager contextParamManager;
  private final String dashboardUrl;

  @Inject
  public OgelRegisterToUseController(JourneyManager journeyManager,
                                     FormFactory formFactory,
                                     PermissionsFinderDao dao,
                                     HttpExecutionContext httpContext,
                                     OgelServiceClient ogelServiceClient,
                                     OgelConditionsServiceClient ogelConditionsServiceClient,
                                     ContextParamManager contextParamManager,
                                     @Named("dashboardUrl") String dashboardUrl) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.dao = dao;
    this.httpContext = httpContext;
    this.ogelServiceClient = ogelServiceClient;
    this.ogelConditionsServiceClient = ogelConditionsServiceClient;
    this.contextParamManager = contextParamManager;
    this.dashboardUrl = dashboardUrl;
  }

  public CompletionStage<Result> renderForm() {

    Logger.info("renderForm");

    return renderWithForm(formFactory.form(OgelRegisterToUseForm.class));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<OgelRegisterToUseForm> form = formFactory.form(OgelRegisterToUseForm.class).bindFromRequest();
    if (form.hasErrors()) {
      return renderWithForm(form);
    }

    String ogelId = dao.getOgelId();
    String controlCode = dao.getControlCodeForRegistration();

    return ogelConditionsServiceClient.get(ogelId, controlCode)
        .thenApplyAsync(conditionsResult ->
                ogelServiceClient.get(dao.getOgelId())
                    .thenApplyAsync(ogelFullView -> {
                      RegisterResultView view = new RegisterResultView("You have successfully registered to use Open general export licence (" + ogelFullView.getName() + ")");
                      return ok(ogelRegisterResult.render(view, ogelFullView, dashboardUrl));
                    }, httpContext.current())
            , httpContext.current())
        .thenCompose(Function.identity());
  }

  public CompletionStage<Result> renderWithForm(Form<OgelRegisterToUseForm> form) {
    String ogelId = dao.getOgelId();
    String controlCode = dao.getControlCodeForRegistration();

    return ogelConditionsServiceClient.get(ogelId, controlCode)
        .thenApplyAsync(conditionsResult ->
          ogelServiceClient.get(dao.getOgelId())
              .thenApplyAsync(ogelResult -> {
                // True when no restriction service result, otherwise check with isItemAllowed.
                // Assume getOgelConditionsApply is empty if there is no result from the OGEL condition service or the re are missing control codes
                boolean allowedToProceed = conditionsResult.isEmpty || (!conditionsResult.isMissingControlCodes
                    && OgelConditionsServiceClient.isItemAllowed(conditionsResult, dao.getOgelConditionsApply().get()));

                return ok(ogelRegisterToUse.render(form, ogelResult, controlCode, allowedToProceed));
              }, httpContext.current())
        , httpContext.current())
        .thenCompose(Function.identity());
  }

  public static class OgelRegisterToUseForm {

    @Constraints.Required(message = "Confirm you have read the OGEL and its criteria in full.")
    public String confirmRead;

    @Constraints.Required(message = "Confirm your export complies with the OGEL criteria stated.")
    public String confirmComplies;

  }

}
