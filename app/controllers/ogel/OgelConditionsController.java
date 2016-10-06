package controllers.ogel;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import components.services.ogels.conditions.OgelConditionsServiceResult;
import components.services.ogels.ogel.OgelServiceClient;
import components.services.ogels.conditions.OgelConditionsServiceClient;
import journey.Events;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import views.html.ogel.ogelConditions;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class OgelConditionsController {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;
  private final HttpExecutionContext httpExecutionContext;
  private final OgelServiceClient ogelServiceClient;
  private final OgelConditionsServiceClient ogelConditionsServiceClient;

  @Inject
  public OgelConditionsController(JourneyManager journeyManager,
                                  FormFactory formFactory,
                                  PermissionsFinderDao permissionsFinderDao,
                                  HttpExecutionContext httpExecutionContext,
                                  OgelServiceClient ogelServiceClient,
                                  OgelConditionsServiceClient ogelConditionsServiceClient) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
    this.httpExecutionContext = httpExecutionContext;
    this.ogelServiceClient = ogelServiceClient;
    this.ogelConditionsServiceClient = ogelConditionsServiceClient;
  }

  public CompletionStage<Result> renderForm() {
    OgelConditionsForm templateForm = new OgelConditionsForm();
    Optional<Boolean> doesRestrictionApplyOptional = permissionsFinderDao.getOgelConditionsApply();
    templateForm.doConditionsApply = doesRestrictionApplyOptional.isPresent()
        ? Boolean.toString(doesRestrictionApplyOptional.get())
        : "";

    return renderWithForm(formFactory.form(OgelConditionsForm.class).fill(templateForm));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<OgelConditionsForm> form = formFactory.form(OgelConditionsForm.class).bindFromRequest();
    if (form.hasErrors()) {
      return renderWithForm(form);
    }

    String doesRestrictionApply = form.get().doConditionsApply;

    // Check for missing control codes
    if ("true".equals(doesRestrictionApply) || "false".equals(doesRestrictionApply)) {
      permissionsFinderDao.saveOgelConditionsApply(Boolean.parseBoolean(doesRestrictionApply));
      return ogelConditionsServiceClient.get(permissionsFinderDao.getOgelId(),
          permissionsFinderDao.getPhysicalGoodControlCode(), httpExecutionContext)
          .thenApplyAsync(response -> {
            // To view this screen the additional conditions service should have returned a result
            if(!response.isOk() || !response.getResult().isPresent()) {
              return completedFuture(badRequest("Invalid response from OGEL conditions service"));
            }
            else {
              OgelConditionsServiceResult result = response.getResult().get();
              if (result.conditionDescriptionControlCodes.isPresent() && !result.conditionDescription.isEmpty()) {
                return completedFuture(badRequest("Invalid form state, should not be able to progress with missing control codes"));
              }
              else {
                return journeyManager.performTransition(Events.OGEL_DOES_RESTRICTION_APPLY);
              }
            }
          }, httpExecutionContext.current())
          .thenCompose(Function.identity());
    }
    else {
      return completedFuture(badRequest("Invalid value for doConditionsApply: \"" + doesRestrictionApply + "\""));
    }
  }

  private CompletionStage<Result> renderWithForm(Form<OgelConditionsForm> form) {
    return ogelConditionsServiceClient.get(permissionsFinderDao.getOgelId(),
        permissionsFinderDao.getPhysicalGoodControlCode(), httpExecutionContext)
        .thenApplyAsync(ogelConditionsResponse -> {
          if (!ogelConditionsResponse.isOk() || !ogelConditionsResponse.getResult().isPresent()) {
            return completedFuture(badRequest("Invalid response from OGEL conditions service"));
          }
          OgelConditionsServiceResult result = ogelConditionsResponse.getResult().get();
          boolean missingControlCodes = result.conditionDescriptionControlCodes.isPresent()
              && !result.conditionDescriptionControlCodes.get().missingControlCodes.isEmpty();
          if (missingControlCodes) {
            Logger.error("OGEL conditions service returned a response with missing control codes. " +
                "OGEL ID: {}, control code: {}.", ogelConditionsResponse.getResult().get().ogelID, ogelConditionsResponse.getResult().get().controlCode);
            form.reject("We have encountered a problem with this particular licence. Unfortunately you will not be " +
                "able to progress through the service any further with the requested licence. This issue has been logged, " +
                "we thank your for your patience.");
          }
          return ogelServiceClient.get(permissionsFinderDao.getOgelId(), httpExecutionContext)
              .thenApplyAsync(ogelResponse -> {
                if (!ogelResponse.isOk()) {
                  return badRequest("Invalid response from OGEL service");
                }
                return ok(ogelConditions.render(form, ogelResponse.getResult(), ogelConditionsResponse.getResult().get(), missingControlCodes));
              }, httpExecutionContext.current());
        }, httpExecutionContext.current())
        .thenCompose(Function.identity());
  }

  public static class OgelConditionsForm {

    @Required(message = "You must answer this question")
    public String doConditionsApply;

  }

}
