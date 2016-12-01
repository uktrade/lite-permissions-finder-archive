package controllers;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import exceptions.FormStateException;
import journey.JourneyDefinitionNames;
import models.TradeType;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.tradeType;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class TradeTypeController extends Controller {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;

  private static final String UNITED_KINGDOM = "CTRY0";

  @Inject
  public TradeTypeController(JourneyManager journeyManager, FormFactory formFactory,
                             PermissionsFinderDao permissionsFinderDao) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
  }

  public CompletionStage<Result> renderForm() {
    TradeTypeForm formTemplate = new TradeTypeForm();
    Optional<TradeType> tradeTypeOptional = permissionsFinderDao.getTradeType();
    tradeTypeOptional.ifPresent((e) -> formTemplate.tradeType = e.toString());
    return completedFuture(ok(tradeType.render(formFactory.form(TradeTypeForm.class).fill(formTemplate))));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<TradeTypeForm> form = formFactory.form(TradeTypeForm.class).bindFromRequest();

    if (form.hasErrors()) {
      return completedFuture(ok(tradeType.render(form)));
    }

    String tradeTypeParam = form.get().tradeType;
    TradeType tradeType = TradeType.valueOf(tradeTypeParam);

    permissionsFinderDao.saveTradeType(tradeType);
    switch (tradeType) {
      case EXPORT:
        permissionsFinderDao.saveSourceCountry(UNITED_KINGDOM);
        return journeyManager.startJourney(JourneyDefinitionNames.EXPORT);
      case IMPORT:
        return journeyManager.startJourney(JourneyDefinitionNames.IMPORT);
      case TRANSSHIPMENT:
      case BROKERING:
        return completedFuture(redirect(routes.StaticContentController.renderBrokeringTranshipment()));
      default:
        throw new FormStateException("Unknown trade type " + tradeType);
    }
  }

  public static class TradeTypeForm {

    @Required(message = "You must select a trade activity")
    public String tradeType;

  }

}
