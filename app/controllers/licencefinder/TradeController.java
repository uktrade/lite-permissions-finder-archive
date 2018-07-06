package controllers.licencefinder;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.Inject;
import components.common.auth.SamlAuthorizer;
import components.common.auth.SpireSAML2Client;
import components.persistence.LicenceFinderDao;
import components.services.LicenceFinderService;
import controllers.guard.LicenceFinderAwaitGuardAction;
import controllers.guard.LicenceFinderUserGuardAction;
import exceptions.UnknownParameterException;
import models.TradeType;
import org.pac4j.play.java.Secure;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import utils.EnumUtil;

import java.util.concurrent.CompletionStage;

@Secure(clients = SpireSAML2Client.CLIENT_NAME, authorizers = SamlAuthorizer.AUTHORIZER_NAME)
@With({LicenceFinderUserGuardAction.class, LicenceFinderAwaitGuardAction.class})
public class TradeController extends Controller {

  private final FormFactory formFactory;
  private final LicenceFinderService licenceFinderService;
  private final LicenceFinderDao licenceFinderDao;
  private final views.html.licencefinder.trade trade;

  private static final String UNITED_KINGDOM = "CTRY0";

  @Inject
  public TradeController(FormFactory formFactory, LicenceFinderService licenceFinderService,
                         LicenceFinderDao licenceFinderDao,
                         views.html.licencefinder.trade trade) {
    this.formFactory = formFactory;
    this.licenceFinderService = licenceFinderService;
    this.licenceFinderDao = licenceFinderDao;
    this.trade = trade;
  }

  /**
   * renderTradeForm
   */
  public CompletionStage<Result> renderTradeForm(String sessionId) {
    if (licenceFinderService.canAccessTradeController(sessionId)) {
      TradeTypeForm form = new TradeTypeForm();
      licenceFinderDao.getTradeType(sessionId).ifPresent(tradeType -> form.tradeType = tradeType.toString());
      String controlCode = licenceFinderDao.getControlCode(sessionId)
          .orElseThrow(UnknownParameterException::unknownOgelRegistrationOrder);
      return completedFuture(ok(trade.render(formFactory.form(TradeTypeForm.class).fill(form), controlCode, sessionId)));
    } else {
      throw UnknownParameterException.unknownOgelRegistrationOrder();
    }
  }

  /**
   * handleTradeSubmit
   */
  public CompletionStage<Result> handleTradeSubmit(String sessionId) {
    if (licenceFinderService.canAccessTradeController(sessionId)) {
      Form<TradeTypeForm> form = formFactory.form(TradeTypeForm.class).bindFromRequest();
      String controlCode = licenceFinderDao.getControlCode(sessionId)
          .orElseThrow(UnknownParameterException::unknownOgelRegistrationOrder);
      if (form.hasErrors()) {
        return completedFuture(ok(trade.render(form, controlCode, sessionId)));
      } else {
        String tradeTypeParam = form.rawData().get("tradeType");
        TradeType tradeType = EnumUtil.parse(tradeTypeParam, TradeType.class);
        if (tradeType == null) {
          throw UnknownParameterException.unknownTradeType(tradeTypeParam);
        } else {
          licenceFinderDao.saveTradeType(sessionId, tradeType);
          switch (tradeType) {
            case EXPORT:
              licenceFinderDao.saveSourceCountry(sessionId, UNITED_KINGDOM);
              return completedFuture(redirect(routes.DestinationController.renderDestinationForm(sessionId)));
            case TRANSSHIPMENT:
              return completedFuture(redirect(controllers.routes.StaticContentController.renderTranshipment()));
            case BROKERING:
              return completedFuture(redirect(controllers.routes.StaticContentController.renderBrokering()));
            default:
              throw UnknownParameterException.unknownTradeType(tradeTypeParam);
          }
        }
      }
    } else {
      throw UnknownParameterException.unknownOgelRegistrationOrder();
    }
  }

  public static class TradeTypeForm {
    @Required(message = "Select where your items are going")
    public String tradeType;
  }

}

