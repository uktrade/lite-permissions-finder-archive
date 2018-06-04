package controllers.licencefinder;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.Inject;
import components.auth.SamlAuthorizer;
import components.common.auth.SpireSAML2Client;
import components.common.state.ContextParamManager;
import components.common.transaction.TransactionManager;
import components.persistence.LicenceFinderDao;
import exceptions.FormStateException;
import models.TradeType;
import org.pac4j.play.java.Secure;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.licencefinder.trade;

import java.util.concurrent.CompletionStage;

@Secure(clients = SpireSAML2Client.CLIENT_NAME, authorizers = SamlAuthorizer.AUTHORIZER_NAME)
public class TradeController extends Controller {

  private final FormFactory formFactory;
  private final LicenceFinderDao dao;
  private final TransactionManager transactionManager;
  private final ContextParamManager contextParam;

  private static final String UNITED_KINGDOM = "CTRY0";

  @Inject
  public TradeController(TransactionManager transactionManager, FormFactory formFactory,
                         LicenceFinderDao dao, ContextParamManager contextParam) {
    this.transactionManager = transactionManager;
    this.formFactory = formFactory;
    this.dao = dao;
    this.contextParam = contextParam;
  }

  /**
   * Test entry point
   */
  public CompletionStage<Result> testEntry(String controlCode) {
    transactionManager.createTransaction();
    dao.saveControlCode(controlCode);
    dao.saveApplicationCode("ABCD-1234");
    return renderTradeForm();
  }

  /************************************************************************************************
   * 'Trade' page
   *******************************************************************************************/
  public CompletionStage<Result> renderTradeForm() {
    TradeTypeForm form = new TradeTypeForm();
    dao.getTradeType().ifPresent((e) -> form.tradeType = e.toString());
    return completedFuture(ok(trade.render(formFactory.form(TradeTypeForm.class).fill(form), dao.getControlCode())));
  }

  public CompletionStage<Result> handleTradeSubmit() {
    Form<TradeTypeForm> form = formFactory.form(TradeTypeForm.class).bindFromRequest();
    String controlCode = dao.getControlCode();
    if (form.hasErrors()) {
      return completedFuture(ok(trade.render(form, controlCode)));
    }

    TradeType tradeType = TradeType.valueOf(form.get().tradeType);
    dao.saveTradeType(tradeType);

    switch (tradeType) {
      case EXPORT:
        dao.saveSourceCountry(UNITED_KINGDOM);
        return contextParam.addParamsAndRedirect(routes.DestinationController.renderDestinationForm());
      case TRANSSHIPMENT:
        return completedFuture(redirect(controllers.routes.StaticContentController.renderTranshipment()));
      case BROKERING:
        return completedFuture(redirect(controllers.routes.StaticContentController.renderBrokering()));
      default:
        throw new FormStateException("Unknown trade type " + tradeType);
    }
  }

  public static class TradeTypeForm {
    @Required(message = "Select where your items are going")
    public String tradeType;
  }

}

