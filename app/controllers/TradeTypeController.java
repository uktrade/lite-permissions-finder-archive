package controllers;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.Inject;
import components.auth.SamlAuthorizer;
import components.common.auth.SpireSAML2Client;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import exceptions.FormStateException;
import journey.Events;
import models.TradeType;
import org.pac4j.play.java.Secure;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

@Secure(clients = SpireSAML2Client.CLIENT_NAME, authorizers = SamlAuthorizer.AUTHORIZER_NAME)
public class TradeTypeController extends Controller {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;
  private final views.html.tradeType tradeType;

  private static final String UNITED_KINGDOM = "CTRY0";

  @Inject
  public TradeTypeController(JourneyManager journeyManager, FormFactory formFactory,
                             PermissionsFinderDao permissionsFinderDao, views.html.tradeType tradeType) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
    this.tradeType = tradeType;
  }

  public CompletionStage<Result> renderForm() {
    TradeTypeForm formTemplate = new TradeTypeForm();
    Optional<TradeType> tradeTypeOptional = permissionsFinderDao.getTradeType();

    tradeTypeOptional.ifPresent((e) -> formTemplate.tradeType = e.toString());
    return completedFuture(ok(tradeType.render(formFactory.form(TradeTypeForm.class).fill(formTemplate), permissionsFinderDao.getControlCodeForRegistration())));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<TradeTypeForm> form = formFactory.form(TradeTypeForm.class).bindFromRequest();
    String controlCode = permissionsFinderDao.getControlCodeForRegistration();
    if (form.hasErrors()) {
      return completedFuture(ok(tradeType.render(form, controlCode)));
    }

    String tradeTypeParam = form.get().tradeType;
    TradeType tradeType = TradeType.valueOf(tradeTypeParam);

    permissionsFinderDao.saveTradeType(tradeType);
    // TODO this is a placeholder
    //permissionsFinderDao.saveControlCodeForRegistration("ML12b");
    switch (tradeType) {
      case EXPORT:
        permissionsFinderDao.saveSourceCountry(UNITED_KINGDOM);
        return journeyManager.performTransition(Events.EXPORT_TRADE_TYPE);
      case TRANSSHIPMENT:
        return completedFuture(redirect(routes.StaticContentController.renderTranshipment()));
      case BROKERING:
        return completedFuture(redirect(routes.StaticContentController.renderBrokering()));
      default:
        throw new FormStateException("Unknown trade type " + tradeType);
    }
  }

  public static class TradeTypeForm {

    @Required(message = "Select where your items are going")
    public String tradeType;

  }

}
