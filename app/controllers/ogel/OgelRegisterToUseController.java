package controllers.ogel;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.persistence.PermissionsFinderDao;
import components.services.ogels.conditions.OgelConditionsServiceClient;
import components.services.ogels.ogel.OgelServiceClient;
import models.view.AnswerView;
import models.view.RegisterResultView;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class OgelRegisterToUseController {

  private final FormFactory formFactory;
  private final PermissionsFinderDao dao;
  private final HttpExecutionContext httpContext;
  private final OgelServiceClient ogelServiceClient;
  private final OgelConditionsServiceClient ogelConditionsServiceClient;
  private final String dashboardUrl;
  private final views.html.ogel.ogelRegisterResult ogelRegisterResult;
  private final views.html.ogel.ogelRegisterToUse ogelRegisterToUse;

  @Inject
  public OgelRegisterToUseController(FormFactory formFactory,
                                     PermissionsFinderDao dao,
                                     HttpExecutionContext httpContext,
                                     OgelServiceClient ogelServiceClient,
                                     OgelConditionsServiceClient ogelConditionsServiceClient,
                                     @Named("dashboardUrl") String dashboardUrl,
                                     views.html.ogel.ogelRegisterResult ogelRegisterResult,
                                     views.html.ogel.ogelRegisterToUse ogelRegisterToUse) {
    this.formFactory = formFactory;
    this.dao = dao;
    this.httpContext = httpContext;
    this.ogelServiceClient = ogelServiceClient;
    this.ogelConditionsServiceClient = ogelConditionsServiceClient;
    this.dashboardUrl = dashboardUrl;
    this.ogelRegisterResult = ogelRegisterResult;
    this.ogelRegisterToUse = ogelRegisterToUse;
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

  private CompletionStage<Result> renderWithForm(Form<OgelRegisterToUseForm> form) {
    String ogelId = dao.getOgelId();
    String controlCode = dao.getControlCodeForRegistration();

    return ogelConditionsServiceClient.get(ogelId, controlCode)
        .thenApplyAsync(conditionsResult ->
                ogelServiceClient.get(dao.getOgelId())
                    .thenApplyAsync(ogelResult -> {
                      // True when no restriction service result, otherwise check with isItemAllowed.
                      // Assume getOgelConditionsApply is empty if there is no result from the OGEL condition service or the re are missing control codes
                      //boolean allowedToProceed = conditionsResult.isEmpty || (!conditionsResult.isMissingControlCodes
                      // && OgelConditionsServiceClient.isItemAllowed(conditionsResult, dao.getOgelConditionsApply().get()));
                      boolean allowedToProceed = true; // We do not now apply conditions TODO check

                      return ok(ogelRegisterToUse.render(form, ogelResult, controlCode, allowedToProceed, getLicenceFinderAnswers()));
                    }, httpContext.current())
            , httpContext.current())
        .thenCompose(Function.identity());
  }

  private final String CONTROL_CODE_QUESTION = "What Control list entry describes your goods?";

  private final String GOODS_GOING_QUESTION = "Where are your goods going?";

  private final String DESTINATION_QUESTION = "What is the final destination of your goods?";

  private final String TERRITORY_QUESTION = "Will your items be received by anyone in a different country or territory, such as a consignee, before reaching their final destination?";

  private final String REPAIR_QUESTION = "Are you exporting goods for or after repair or replacement?";

  private final String EXHIBITION_QUESTION = "Are you exporting goods for or after exhibition or demonstration?";

  private final String BEFORE_OR_LESS_QUESTION = "Were your goods manufactured before 1897, and worth less than £30,000?";

  private List<AnswerView> getLicenceFinderAnswers() {
    List<AnswerView> answerViews = new ArrayList<>();
    /*
    answerViews.add(new AnswerView(CONTROL_CODE_QUESTION, dao.getControlCodeForRegistration(), null));
    Optional<TradeType> optTradeType =  dao.getTradeType();

    if(optTradeType.isPresent()) {
      answerViews.add(new AnswerView(GOODS_GOING_QUESTION, optTradeType.get().getTitle(), null));
    }

    answerViews.add(new AnswerView(DESTINATION_QUESTION, countryProvider.getCountry(dao.getFinalDestinationCountry()).getCountryName(), null));


    Optional<Boolean> optMultipleCountries = dao.getMultipleCountries();
    if(optMultipleCountries.isPresent()) {
      answerViews.add(new AnswerView(TERRITORY_QUESTION, optMultipleCountries.get() ? "Yes" : "No", null));
    }


    Optional<OgelQuestionsController.OgelQuestionsForm> optForm = dao.getOgelQuestionsForm();
    if(optForm.isPresent()) {
      OgelQuestionsController.OgelQuestionsForm form = optForm.get();
      answerViews.add(new AnswerView(REPAIR_QUESTION, form.forRepair.equals("true") ? "Yes" : "No", null));
      answerViews.add(new AnswerView(EXHIBITION_QUESTION, form.forExhibition.equals("true") ? "Yes" : "No", null));
      answerViews.add(new AnswerView(BEFORE_OR_LESS_QUESTION, form.beforeOrLess.equals("true") ? "Yes" : "No", null));
    }
    */

    return answerViews;
  }

  public static class OgelRegisterToUseForm {

    @Constraints.Required(message = "Confirm you have read the OGEL and its criteria in full.")
    public String confirmRead;

    @Constraints.Required(message = "Confirm your export complies with the OGEL criteria stated.")
    public String confirmComplies;

  }

}
