package controllers.controlcode;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.FrontendServiceClient;
import exceptions.FormStateException;
import journey.helpers.ControlCodeJourneyHelper;
import models.controlcode.ControlCodeSubJourney;
import models.controlcode.TechnicalNotesDisplay;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import views.html.controlcode.technicalNotes;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class TechnicalNotesController {
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;
  private final HttpExecutionContext httpExecutionContext;
  private final FrontendServiceClient frontendServiceClient;
  private final ControlCodeJourneyHelper controlCodeJourneyHelper;

  @Inject
  public TechnicalNotesController(FormFactory formFactory,
                                  PermissionsFinderDao permissionsFinderDao,
                                  HttpExecutionContext httpExecutionContext,
                                  FrontendServiceClient frontendServiceClient,
                                  ControlCodeJourneyHelper controlCodeJourneyHelper) {
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
    this.httpExecutionContext = httpExecutionContext;
    this.frontendServiceClient = frontendServiceClient;
    this.controlCodeJourneyHelper = controlCodeJourneyHelper;
  }

  private CompletionStage<Result> renderForm(ControlCodeSubJourney controlCodeSubJourney) {
    Optional<Boolean> technicalNotesApply = permissionsFinderDao.getControlCodeTechnicalNotesApply(controlCodeSubJourney);
    TechnicalNotesForm templateForm = new TechnicalNotesForm();
    templateForm.stillDescribesItems = technicalNotesApply.isPresent() ? technicalNotesApply.get().toString() : "";
    return frontendServiceClient.get(permissionsFinderDao.getSelectedControlCode(controlCodeSubJourney))
        .thenApplyAsync(result -> ok(technicalNotes.render(formFactory.form(TechnicalNotesForm.class).fill(templateForm),
            new TechnicalNotesDisplay(controlCodeSubJourney, result))), httpExecutionContext.current());
  }

  public CompletionStage<Result> renderSearchForm() {
    return renderForm(models.controlcode.ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH);
  }

  public CompletionStage<Result> renderSearchRelatedToForm(String goodsTypeText) {
    return ControlCodeJourneyHelper.getSearchRelatedToPhysicalGoodsResult(goodsTypeText, this::renderForm);
  }

  public CompletionStage<Result> renderControlsForm(String goodsTypeText) {
    return ControlCodeJourneyHelper.getControlsResult(goodsTypeText, this::renderForm);
  }

  public CompletionStage<Result> renderRelatedControlsForm(String goodsTypeText) {
    return ControlCodeJourneyHelper.getRelatedControlsResult(goodsTypeText, this::renderForm);
  }

  public CompletionStage<Result> renderCatchallControlsForm(String goodsTypeText) {
    return ControlCodeJourneyHelper.getCatchAllControlsResult(goodsTypeText, this::renderForm);
  }

  private CompletionStage<Result> handleSubmit(ControlCodeSubJourney controlCodeSubJourney) {
    Form<TechnicalNotesForm> form = formFactory.form(TechnicalNotesForm.class).bindFromRequest();
    String controlCode = permissionsFinderDao.getSelectedControlCode(controlCodeSubJourney);
    return frontendServiceClient.get(controlCode)
        .thenApplyAsync(result -> {
          if (form.hasErrors()) {
            return completedFuture(ok(technicalNotes.render(form, new TechnicalNotesDisplay(controlCodeSubJourney, result))));
          }
          else {
            String stillDescribesItems = form.get().stillDescribesItems;
            if("true".equals(stillDescribesItems)) {
              permissionsFinderDao.saveControlCodeTechnicalNotesApply(controlCodeSubJourney, true);
              return controlCodeJourneyHelper.confirmedJourneyTransition(controlCodeSubJourney, controlCode);
            }
            else if ("false".equals(stillDescribesItems)) {
              permissionsFinderDao.saveControlCodeTechnicalNotesApply(controlCodeSubJourney, false);
              return controlCodeJourneyHelper.notApplicableJourneyTransition(controlCodeSubJourney);
            }
            else {
              throw new FormStateException("Unhandled form state");
            }
          }
        }, httpExecutionContext.current()).thenCompose(Function.identity());
  }

  public CompletionStage<Result> handleSearchSubmit() {
    return handleSubmit(models.controlcode.ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH);
  }

  public CompletionStage<Result> handleSearchRelatedToSubmit(String goodsTypeText) {
    return ControlCodeJourneyHelper.getSearchRelatedToPhysicalGoodsResult(goodsTypeText, this::handleSubmit);
  }

  public CompletionStage<Result> handleControlsSubmit(String goodsTypeText) {
    return ControlCodeJourneyHelper.getControlsResult(goodsTypeText, this::handleSubmit);
  }

  public CompletionStage<Result> handleRelatedControlsSubmit(String goodsTypeText) {
    return ControlCodeJourneyHelper.getRelatedControlsResult(goodsTypeText, this::handleSubmit);
  }

  public CompletionStage<Result> handleCatchallControlsSubmit(String goodsTypeText) {
    return ControlCodeJourneyHelper.getCatchAllControlsResult(goodsTypeText, this::handleSubmit);
  }

  public static class TechnicalNotesForm {

    @Required(message = "You must answer this question")
    public String stillDescribesItems;

  }

}
