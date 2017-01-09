package controllers.softtech.controls;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.common.journey.StandardEvents;
import components.persistence.PermissionsFinderDao;
import exceptions.FormStateException;
import journey.helpers.SoftTechJourneyHelper;
import models.GoodsType;
import models.softtech.ApplicableSoftTechControls;
import models.softtech.SoftTechCategory;
import models.softtech.controls.NoSoftTechControlsExistDisplay;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import views.html.softtech.controls.noSoftTechControlsExist;

import java.util.concurrent.CompletionStage;

public class NoSoftTechControlsExistController {
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;
  private final JourneyManager journeyManager;
  private final SoftTechJourneyHelper softTechJourneyHelper;
  private final HttpExecutionContext httpExecutionContext;

  @Inject
  public NoSoftTechControlsExistController(FormFactory formFactory,
                                           PermissionsFinderDao permissionsFinderDao,
                                           JourneyManager journeyManager,
                                           SoftTechJourneyHelper softTechJourneyHelper,
                                           HttpExecutionContext httpExecutionContext) {
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
    this.journeyManager = journeyManager;
    this.softTechJourneyHelper = softTechJourneyHelper;
    this.httpExecutionContext = httpExecutionContext;
  }

  public CompletionStage<Result> renderForm(String goodsTypeText) {
    return SoftTechJourneyHelper.validateGoodsTypeAndGetResult(goodsTypeText, this::renderFormInternal);
  }

  private CompletionStage<Result> renderFormInternal(GoodsType goodsType) {
    return renderWithForm(goodsType, formFactory.form(NoSoftwareControlsExistForm.class));
  }

  public CompletionStage<Result> handleSubmit(String goodsTypeText) {
    return SoftTechJourneyHelper.validateGoodsTypeAndGetResult(goodsTypeText, this::handleSubmitInternal);
  }

  private CompletionStage<Result> handleSubmitInternal(GoodsType goodsType) {
    Form<NoSoftwareControlsExistForm> form = formFactory.form(NoSoftwareControlsExistForm.class).bindFromRequest();
    if (form.hasErrors()) {
      return renderWithForm(goodsType, form);
    }
    String action = form.get().action;
    if ("continue".equals(action)) {
      // TODO this transition is a stub
      return journeyManager.performTransition(StandardEvents.NEXT);
    }
    else {
      throw new FormStateException(String.format("Unknown value for action: \"%s\"", action));
    }
  }

  private CompletionStage<Result> renderWithForm(GoodsType goodsType, Form<NoSoftwareControlsExistForm> form) {
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    SoftTechCategory softTechCategory = permissionsFinderDao.getSoftTechCategory(goodsType).get();
    return softTechJourneyHelper.checkCatchtallSoftwareControls(goodsType, softTechCategory)
        .thenApplyAsync(control -> {
          if (control == ApplicableSoftTechControls.ONE || control == ApplicableSoftTechControls.GREATER_THAN_ONE) {
            return ok(noSoftTechControlsExist.render(form, new NoSoftTechControlsExistDisplay(goodsType)));
          }
          else {
            throw new RuntimeException(String.format("Unexpected member of ApplicableSoftTechControls enum: \"%s\""
                , control.toString()));
          }
        }, httpExecutionContext.current());
  }

  public static class NoSoftwareControlsExistForm {
    @Required(message = "This is required")
    public String action;
  }

}