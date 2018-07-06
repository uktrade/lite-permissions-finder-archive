package controllers.licencefinder;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import com.spotify.futures.CompletableFutures;
import components.common.auth.SamlAuthorizer;
import components.common.auth.SpireAuthManager;
import components.common.auth.SpireSAML2Client;
import components.services.CustomerService;
import components.services.OgelService;
import components.services.PermissionsService;
import exceptions.UnknownParameterException;
import models.summary.LicenceInfo;
import org.pac4j.play.java.Secure;
import org.slf4j.LoggerFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import uk.gov.bis.lite.customer.api.view.CustomerView;
import uk.gov.bis.lite.customer.api.view.SiteView;
import uk.gov.bis.lite.ogel.api.view.OgelFullView;
import uk.gov.bis.lite.permissions.api.view.OgelRegistrationView;

import java.util.concurrent.CompletionStage;

@Secure(clients = SpireSAML2Client.CLIENT_NAME, authorizers = SamlAuthorizer.AUTHORIZER_NAME)
public class ViewOgelController {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ViewOgelController.class);

  private final PermissionsService permissionsService;
  private final OgelService ogelService;
  private final CustomerService customerService;
  private final HttpExecutionContext httpContext;
  private final SpireAuthManager authManager;
  private final views.html.licencefinder.viewOgel viewOgel;

  @Inject
  public ViewOgelController(PermissionsService permissionsService,
                            OgelService ogelService, CustomerService customerService,
                            HttpExecutionContext httpContext, SpireAuthManager authManager,
                            views.html.licencefinder.viewOgel viewOgel) {
    this.permissionsService = permissionsService;
    this.ogelService = ogelService;
    this.customerService = customerService;
    this.httpContext = httpContext;
    this.authManager = authManager;
    this.viewOgel = viewOgel;
  }

  public CompletionStage<Result> viewOgel(String registrationReference) {
    String userId = authManager.getAuthInfoFromContext().getId();
    return getOgelRegistration(userId, registrationReference).thenComposeAsync(view -> {
      CompletionStage<CustomerView> customerStage = customerService.getCustomer(view.getCustomerId());
      CompletionStage<SiteView> siteStage = customerService.getSite(view.getSiteId());
      CompletionStage<OgelFullView> ogelStage = ogelService.getById(view.getOgelType());
      return CompletableFutures.combine(customerStage, siteStage, ogelStage, (customerView, siteView, ogelFullView) -> {
        LicenceInfo info = new LicenceInfo();
        info.setLicenceNumber(view.getRegistrationReference());
        info.setRegistrationDate(view.getRegistrationDate());
        info.setCustomerId(view.getCustomerId());
        info.setSiteId(view.getSiteId());
        info.setOgelType(view.getOgelType());
        info.setCompanyName(customerView.getCompanyName());
        info.setCompanyNumber(customerView.getCompanyNumber());
        info.setSiteName(siteView.getSiteName());
        info.setSiteAddress(siteView.getAddress().getPlainText());
        info.setLicenceType(ogelFullView.getName());
        info.setLicenceUrl(ogelFullView.getLink());
        return ok(viewOgel.render(info));
      });
    }, httpContext.current());
  }

  private CompletionStage<OgelRegistrationView> getOgelRegistration(String userId, String reference) {
    return permissionsService.getOgelRegistration(userId, reference).exceptionally(error -> {
      LOGGER.error("Unknown ogel reference {}", reference, error);
      throw UnknownParameterException.unknownOgelReference(reference);
    });
  }

}
