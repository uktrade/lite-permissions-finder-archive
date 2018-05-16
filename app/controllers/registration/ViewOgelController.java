package controllers.registration;

import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;
import static uk.gov.bis.lite.permissions.api.view.OgelRegistrationView.Status.DEREGISTERED;
import static uk.gov.bis.lite.permissions.api.view.OgelRegistrationView.Status.SURRENDERED;

import com.google.inject.Inject;
import components.auth.SamlAuthorizer;
import components.client.CustomerServiceClient;
import components.client.OgelRegistrationServiceClient;
import components.client.OgelServiceClient;
import components.common.CommonContextAction;
import components.common.auth.SpireAuthManager;
import components.common.auth.SpireSAML2Client;
import models.summary.LicenceInfo;
import org.pac4j.play.java.Secure;
import play.Logger;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import play.mvc.With;
import uk.gov.bis.lite.customer.api.view.CustomerView;
import uk.gov.bis.lite.customer.api.view.SiteView;
import uk.gov.bis.lite.ogel.api.view.OgelFullView;
import uk.gov.bis.lite.permissions.api.view.OgelRegistrationView;
import uk.gov.bis.lite.permissions.api.view.OgelRegistrationView.Status;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@With(CommonContextAction.class)
@Secure(clients = SpireSAML2Client.CLIENT_NAME, authorizers = SamlAuthorizer.AUTHORIZER_NAME)
public class ViewOgelController {

  private static final Logger.ALogger LOGGER = Logger.of(ViewOgelController.class);

  private final OgelRegistrationServiceClient ogelRegistrationServiceClient;
  private final OgelServiceClient ogelServiceClient;
  private final CustomerServiceClient customerServiceClient;
  private final HttpExecutionContext httpExecutionContext;
  private final SpireAuthManager authManager;
  private final views.html.organisation.errorPage errorPage;
  private final views.html.registration.viewOgel viewOgel;

  @Inject
  public ViewOgelController(OgelRegistrationServiceClient ogelRegistrationServiceClient,
                            OgelServiceClient ogelServiceClient, CustomerServiceClient customerServiceClient,
                            HttpExecutionContext httpExecutionContext, SpireAuthManager authManager,
                            views.html.organisation.errorPage errorPage, views.html.registration.viewOgel viewOgel) {
    this.ogelRegistrationServiceClient = ogelRegistrationServiceClient;
    this.ogelServiceClient = ogelServiceClient;
    this.customerServiceClient = customerServiceClient;
    this.httpExecutionContext = httpExecutionContext;
    this.authManager = authManager;
    this.errorPage = errorPage;
    this.viewOgel = viewOgel;
  }

  public CompletionStage<Result> viewOgel(String registrationReference) {

    LicenceInfo licenceInfo = new LicenceInfo();
    licenceInfo.setLicenceNumber(registrationReference);

    return getOgelRegistration(licenceInfo).thenComposeAsync(r -> {
      CompletionStage<CustomerView> customerCompletionStage = getCustomer(r);
      CompletionStage<SiteView> siteCompletionStage = getCustomerSite(r);
      CompletionStage<OgelFullView> ogelCompletionStage = getOgel(r);

      return customerCompletionStage
          .thenCombineAsync(siteCompletionStage, (c, s) -> combine(licenceInfo, c, s), httpExecutionContext.current())
          .thenCombineAsync(ogelCompletionStage, this::combine, httpExecutionContext.current())
          .thenApplyAsync(this::renderViewOgel, httpExecutionContext.current());
    }, httpExecutionContext.current());
  }

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

    return ogelRegistrationServiceClient.getOgelRegistration(userId, registrationReference)
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
        }, httpExecutionContext.current());
  }

  private CompletionStage<OgelFullView> getOgel(LicenceInfo licenceInfo) {

    if (licenceInfo.hasError()) {
      return CompletableFuture.completedFuture(null);
    }

    String ogelType = licenceInfo.getOgelType();
    return ogelServiceClient.getOgel(ogelType)
        .thenApplyAsync(ogel -> {
              if (!ogel.isPresent()) {
                LOGGER.error("OgelServiceClient - Failed to get OGEL {ogelType={}}.", ogelType);
              }
              return ogel.orElse(null);
            },
            httpExecutionContext.current());
  }

  private CompletionStage<CustomerView> getCustomer(LicenceInfo licenceInfo) {

    if (licenceInfo.hasError()) {
      return CompletableFuture.completedFuture(null);
    }

    String customerId = licenceInfo.getCustomerId();
    return customerServiceClient.getCustomer(customerId).handleAsync((customer, error) -> {
          if (error != null || !customer.isPresent()) {
            LOGGER.error("CustomerServiceClient - Failed to get Customer {customerId={}}.", customerId);
            return null;
          } else {
            return customer.get();
          }
        },
        httpExecutionContext.current());
  }

  private CompletionStage<SiteView> getCustomerSite(LicenceInfo licenceInfo) {

    if (licenceInfo.hasError()) {
      return CompletableFuture.completedFuture(null);
    }

    String siteId = licenceInfo.getSiteId();
    return customerServiceClient.getSite(siteId)
        .handleAsync((site, error) -> {
          if (error != null) {
            LOGGER.error("CustomerServiceClient - Failed to get Site {siteId={}}.", siteId);
            return null;
          } else {
            return site;
          }
        }, httpExecutionContext.current());
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

  public String getViewSummaryUrl(String ogelType) {
    return "/ogel-conditions/" + ogelType;
  }

}
