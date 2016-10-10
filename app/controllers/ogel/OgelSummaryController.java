package controllers.ogel;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import components.services.ogels.conditions.OgelConditionsServiceClient;
import components.services.ogels.ogel.OgelServiceClient;
import exceptions.BusinessRuleException;
import exceptions.FormStateException;
import exceptions.ServiceResponseException;
import journey.Events;
import play.data.Form;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import views.html.ogel.ogelSummary;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class OgelSummaryController {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;
  private final HttpExecutionContext httpExecutionContext;
  private final OgelServiceClient ogelServiceClient;
  private final OgelConditionsServiceClient ogelConditionsServiceClient;

  @Inject
  public OgelSummaryController(JourneyManager journeyManager,
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
    return renderWithForm(formFactory.form(OgelSummaryForm.class));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<OgelSummaryForm> form = formFactory.form(OgelSummaryForm.class).bindFromRequest();
    if (form.hasErrors()) {
      return renderWithForm(form);
    }
    String action = form.get().action;
    return ogelConditionsServiceClient.get(permissionsFinderDao.getOgelId(),
        permissionsFinderDao.getPhysicalGoodControlCode())
        .thenApplyAsync(response -> {
          if (!response.isOk()) {
            throw new ServiceResponseException("OGEL conditions service returned an invalid response");
          }
          if ("register".equals(action)) {
            if (!response.getResult().isPresent()) {
              return journeyManager.performTransition(Events.OGEL_REGISTERED);
            }
            else if (OgelConditionsServiceClient.isItemAllowed(response.getResult(), permissionsFinderDao.getOgelConditionsApply().get())) {
              return journeyManager.performTransition(Events.OGEL_REGISTERED);
            }
            else {
              // Should not be able to register when the item is not allowed
              throw new BusinessRuleException("Can not register for OGEL with applicable conditions");
            }
          }
          else if ("chooseAgain".equals(action)) {
            return journeyManager.performTransition(Events.OGEL_CHOOSE_AGAIN);
          }
          else {
            throw new FormStateException("Unhandled form state");
          }
        }, httpExecutionContext.current())
        .thenCompose(Function.identity());
  }

  public CompletionStage<Result> renderWithForm(Form<OgelSummaryForm> form) {
    String ogelId = permissionsFinderDao.getOgelId();
    String physicalGoodsControlCode = permissionsFinderDao.getPhysicalGoodControlCode();

    return ogelConditionsServiceClient.get(ogelId, physicalGoodsControlCode)
        .thenApplyAsync(ogelConditionsResponse -> {
          if (!ogelConditionsResponse.isOk()) {
            throw new ServiceResponseException("OGEL conditions service returned an invalid response");
          }
          return ogelServiceClient.get(permissionsFinderDao.getOgelId())
              .thenApplyAsync(ogelResponse -> {
                if (!ogelResponse.isOk()) {
                  throw new ServiceResponseException("OGEL service returned an invalid response");
                }

                // True when no restriction service result, otherwise check with isItemAllowed.
                // Assume getOgelConditionsApply is empty if there is no result from the OGEL condition service
                boolean allowedToProceed = !ogelConditionsResponse.getResult().isPresent() ||
                    OgelConditionsServiceClient.isItemAllowed(ogelConditionsResponse.getResult(), permissionsFinderDao.getOgelConditionsApply().get());

                return ok(ogelSummary.render(form, ogelResponse.getResult(), physicalGoodsControlCode, allowedToProceed));
              }, httpExecutionContext.current());
        }, httpExecutionContext.current())
        .thenCompose(Function.identity());
  }

  public static class OgelSummaryForm {

    public String action;

  }

}
