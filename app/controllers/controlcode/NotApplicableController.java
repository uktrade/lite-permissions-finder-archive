package controllers.controlcode;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.common.journey.StandardEvents;
import controllers.controlcode.notapplicable.NotApplicableControllerHelper;
import exceptions.FormStateException;
import journey.Events;
import journey.helpers.ControlCodeSubJourneyHelper;
import models.controlcode.BackType;
import models.controlcode.ControlCodeSubJourney;
import models.controlcode.NotApplicableDisplay;
import models.controlcode.NotApplicableDisplayCommon;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Result;
import views.html.controlcode.notApplicable;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class NotApplicableController {

  private final FormFactory formFactory;
  private final JourneyManager journeyManager;
  private final NotApplicableControllerHelper notApplicableControllerHelper;

  @Inject
  public NotApplicableController(FormFactory formFactory,
                                 JourneyManager journeyManager,
                                 NotApplicableControllerHelper notApplicableControllerHelper) {
    this.formFactory = formFactory;
    this.journeyManager = journeyManager;
    this.notApplicableControllerHelper = notApplicableControllerHelper;
  }

  // TODO remove showExtendedContent
  public CompletionStage<Result> renderForm(String controlCodeVariantText, String goodsTypeText, String showExtendedContent) {
    return ControlCodeSubJourneyHelper.resolveUrlToSubJourneyAndUpdateContext(controlCodeVariantText, goodsTypeText,
        controlCodeSubJourney -> notApplicableControllerHelper.renderFormInternal(controlCodeSubJourney, this::renderNotApplicable));
  }

  private Result renderNotApplicable(NotApplicableDisplayCommon displayCommon) {
    return ok(notApplicable.render(formFactory.form(NotApplicableForm.class), new NotApplicableDisplay(displayCommon)));
  }

  public CompletionStage<Result> handleSubmit() {
    return ControlCodeSubJourneyHelper.resolveContextToSubJourney(this::handleSubmitInternal);
  }

  private CompletionStage<Result> handleSubmitInternal(ControlCodeSubJourney controlCodeSubJourney) {
    Form<NotApplicableForm> form = formFactory.form(NotApplicableForm.class).bindFromRequest();
    if (!form.hasErrors()) {
      String action = form.get().action;
      if (controlCodeSubJourney.isPhysicalGoodsSearchVariant() ||
          controlCodeSubJourney.isSoftTechControlsVariant() ||
          controlCodeSubJourney.isSoftTechControlsRelatedToPhysicalGoodVariant() ||
          controlCodeSubJourney.isSoftTechCatchallControlsVariant()) {

        Optional<BackType> backTypeOptional = BackType.getMatched(action);

        if (backTypeOptional.isPresent()) {
          return journeyManager.performTransition(Events.BACK, backTypeOptional.get());
        }
        else if ("continue".equals(action)) {
          return journeyManager.performTransition(StandardEvents.NEXT);
        }
        else {
          throw new FormStateException("Unknown value for action: \"" + action + "\"");
        }
      }
      else {
        throw new RuntimeException(String.format("Unexpected member of ControlCodeSubJourney enum: \"%s\""
            , controlCodeSubJourney.toString()));
      }
    }
    throw new FormStateException("Unhandled form state");
  }

  public static class NotApplicableForm {
    public String action;
  }
}
