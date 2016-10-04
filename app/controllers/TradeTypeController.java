package controllers;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import journey.Events;
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
    formTemplate.tradeType = tradeTypeOptional.isPresent() ? tradeTypeOptional.get().value() : "";
    return completedFuture(ok(tradeType.render(formFactory.form(TradeTypeForm.class).fill(formTemplate))));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<TradeTypeForm> form = formFactory.form(TradeTypeForm.class).bindFromRequest();

    if (form.hasErrors()) {
      return completedFuture(ok(tradeType.render(form)));
    }

    String tradeTypeParam = form.get().tradeType;
    Optional<TradeType> tradeTypeOption = TradeType.getMatched(tradeTypeParam);

    if(tradeTypeOption.isPresent()) {
      permissionsFinderDao.saveTradeType(tradeTypeOption.get());
      if (tradeTypeOption.get() == TradeType.EXPORT) {
        permissionsFinderDao.saveSourceCountry(UNITED_KINGDOM);
      }
      return journeyManager.performTransition(Events.TRADE_TYPE_SELECTED, tradeTypeOption.get());
    }
    return completedFuture(badRequest("Unknown trade type " + tradeTypeParam));
  }

  public static class TradeTypeForm {

    @Required(message = "You must select a trade activity")
    public String tradeType;

  }

}
