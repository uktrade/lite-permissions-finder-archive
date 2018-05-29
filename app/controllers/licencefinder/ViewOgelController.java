package controllers.licencefinder;

import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;
import static uk.gov.bis.lite.permissions.api.view.OgelRegistrationView.Status.DEREGISTERED;
import static uk.gov.bis.lite.permissions.api.view.OgelRegistrationView.Status.SURRENDERED;

import com.google.inject.Inject;
import components.auth.SamlAuthorizer;
import components.client.CustomerServiceClient;
import components.client.OgelServiceClient;
import components.client.PermissionsService;
import components.common.auth.SpireAuthManager;
import components.common.auth.SpireSAML2Client;
import models.summary.LicenceInfo;
import org.pac4j.play.java.Secure;
import play.Logger;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import uk.gov.bis.lite.customer.api.view.CustomerView;
import uk.gov.bis.lite.customer.api.view.SiteView;
import uk.gov.bis.lite.ogel.api.view.OgelFullView;
import uk.gov.bis.lite.permissions.api.view.OgelRegistrationView;
import uk.gov.bis.lite.permissions.api.view.OgelRegistrationView.Status;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Secure(clients = SpireSAML2Client.CLIENT_NAME, authorizers = SamlAuthorizer.AUTHORIZER_NAME)
public class ViewOgelController {

  private static final Logger.ALogger LOGGER = Logger.of(ViewOgelController.class);

  private final PermissionsService permissionsService;
  private final OgelServiceClient ogelService;
  private final CustomerServiceClient customerService;
  private final HttpExecutionContext httpContext;
  private final SpireAuthManager authManager;
  private final views.html.licencefinder.errorPage errorPage;
  private final views.html.licencefinder.viewOgel viewOgel;

  @Inject
  public ViewOgelController(PermissionsService permissionsService,
                            OgelServiceClient ogelService, CustomerServiceClient customerService,
                            HttpExecutionContext httpContext, SpireAuthManager authManager,
                            views.html.licencefinder.errorPage errorPage, views.html.licencefinder.viewOgel viewOgel) {
    this.permissionsService = permissionsService;
    this.ogelService = ogelService;
    this.customerService = customerService;
    this.httpContext = httpContext;
    this.authManager = authManager;
    this.errorPage = errorPage;
    this.viewOgel = viewOgel;
  }

  /**
   * viewOgel
   */
  public CompletionStage<Result> viewOgel(String registrationReference) {

    LicenceInfo licenceInfo = new LicenceInfo();
    licenceInfo.setLicenceNumber(registrationReference);

    return getOgelRegistration(licenceInfo).thenComposeAsync(r -> {
      CompletionStage<CustomerView> customerCompletionStage = getCustomer(r);
      CompletionStage<SiteView> siteCompletionStage = getCustomerSite(r);
      CompletionStage<OgelFullView> ogelCompletionStage = getOgel(r);

      return customerCompletionStage
          .thenCombineAsync(siteCompletionStage, (c, s) -> combine(licenceInfo, c, s), httpContext.current())
          .thenCombineAsync(ogelCompletionStage, this::combine, httpContext.current())
          .thenApplyAsync(this::renderViewOgel, httpContext.current());
    }, httpContext.current());
  }

  /**
   * Private methods
   */

  private LicenceInfo combine(LicenceInfo licenceInfo, CustomerView customer, SiteView site) {
    if (licenceInfo.hasError()) {
      return licenceInfo;
    }

    if (customer == null || site == null) {
      return licenceInfo.setDefaultError();
    }

    licenceInfo.setCompanyName(customer.getCompanyName());
    licenceInfo.setCompanyNumber(customer.getCompanyNumber());
    licenceInfo.setSiteName(site.getSiteName());
    licenceInfo.setSiteAddress(site.getAddress().getPlainText());
    return licenceInfo;
  }

  private LicenceInfo combine(LicenceInfo licenceInfo, OgelFullView ogel) {
    if (licenceInfo.hasError()) {
      return licenceInfo;
    }

    if (ogel == null) {
      return licenceInfo.setDefaultError();
    }

    licenceInfo.setLicenceType(ogel.getName());
    return licenceInfo;
  }

  private CompletionStage<LicenceInfo> getOgelRegistration(LicenceInfo licenceInfo) {

    String userId = authManager.getAuthInfoFromContext().getId();
    String registrationReference = licenceInfo.getLicenceNumber();

    return permissionsService.getOgelRegistration(userId, registrationReference)
        .thenApplyAsync(result -> {
          if (!result.isPresent()) {
            LOGGER.error("OgelRegistrationServiceClient - Failed to get OGEL registration {userId={}, registrationReference={}}.",
                userId, registrationReference);
            return licenceInfo.setDefaultError();
          }

          OgelRegistrationView ogelRegistration = result.get();
          Status status = ogelRegistration.getStatus();
          if (status == DEREGISTERED || status == SURRENDERED) {
            LOGGER.error("OGEL registration is no longer active - {userId={}, registrationReference={}, status={}}.",
                userId, registrationReference, status);
            return licenceInfo.setError("This OGEL is no longer active.");
          } else {
            licenceInfo.setRegistrationDate(ogelRegistration.getRegistrationDate());
            licenceInfo.setCustomerId(ogelRegistration.getCustomerId());
            licenceInfo.setSiteId(ogelRegistration.getSiteId());
            licenceInfo.setOgelType(ogelRegistration.getOgelType());
            return licenceInfo;
          }
        }, httpContext.current());
  }

  private CompletionStage<OgelFullView> getOgel(LicenceInfo licenceInfo) {

    if (licenceInfo.hasError()) {
      return CompletableFuture.completedFuture(null);
    }

    String ogelType = licenceInfo.getOgelType();
    return ogelService.getOgel(ogelType)
        .thenApplyAsync(ogel -> {
              if (!ogel.isPresent()) {
                LOGGER.error("OgelServiceClient - Failed to get OGEL {ogelType={}}.", ogelType);
              }
              return ogel.orElse(null);
            },
            httpContext.current());
  }

  private CompletionStage<CustomerView> getCustomer(LicenceInfo licenceInfo) {

    if (licenceInfo.hasError()) {
      return CompletableFuture.completedFuture(null);
    }

    String customerId = licenceInfo.getCustomerId();
    return customerService.getCustomer(customerId).handleAsync((customer, error) -> {
          if (error != null || !customer.isPresent()) {
            LOGGER.error("CustomerServiceClient - Failed to get Customer {customerId={}}.", customerId);
            return null;
          } else {
            return customer.get();
          }
        },
        httpContext.current());
  }

  private CompletionStage<SiteView> getCustomerSite(LicenceInfo licenceInfo) {

    if (licenceInfo.hasError()) {
      return CompletableFuture.completedFuture(null);
    }

    String siteId = licenceInfo.getSiteId();
    return customerService.getSite(siteId)
        .handleAsync((site, error) -> {
          if (error != null) {
            LOGGER.error("CustomerServiceClient - Failed to get Site {siteId={}}.", siteId);
            return null;
          } else {
            return site;
          }
        }, httpContext.current());
  }

  /**
   * Renders view ogel if no errors have occurred whilst retrieving OGEL information, otherwise renders the error page.
   */
  private Result renderViewOgel(LicenceInfo licenceInfo) {

    if (licenceInfo.hasError()) {
      return badRequest(errorPage.render("errorMessage"));
    } else {
      String viewOgelConditionsUrl = getViewSummaryUrl(licenceInfo.getOgelType());
      return ok(viewOgel.render(licenceInfo, viewOgelConditionsUrl));
    }
  }

  private String getViewSummaryUrl(String ogelType) {
    return "/ogel-conditions/" + ogelType;
  }

}
