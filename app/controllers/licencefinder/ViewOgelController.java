package controllers.licencefinder;

import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;
import static uk.gov.bis.lite.permissions.api.view.OgelRegistrationView.Status.DEREGISTERED;
import static uk.gov.bis.lite.permissions.api.view.OgelRegistrationView.Status.SURRENDERED;

import com.google.inject.Inject;
import components.common.auth.SamlAuthorizer;
import components.common.auth.SpireAuthManager;
import components.common.auth.SpireSAML2Client;
import components.services.CustomerService;
import components.services.OgelService;
import components.services.PermissionsService;
import models.summary.LicenceInfo;
import org.pac4j.play.java.Secure;
import org.slf4j.LoggerFactory;
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

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ViewOgelController.class);

  private final PermissionsService permissionsService;
  private final OgelService ogelService;
  private final CustomerService customerService;
  private final HttpExecutionContext httpContext;
  private final SpireAuthManager authManager;
  private final views.html.licencefinder.errorPage errorPage;
  private final views.html.licencefinder.viewOgel viewOgel;

  @Inject
  public ViewOgelController(PermissionsService permissionsService,
                            OgelService ogelService, CustomerService customerService,
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
      CompletionStage<CustomerView> customerStage = getCustomer(r);
      CompletionStage<SiteView> siteStage = getCustomerSite(r);
      CompletionStage<OgelFullView> ogelStage = getOgel(r);

      return customerStage
          .thenCombineAsync(siteStage, (c, s) -> combine(licenceInfo, c, s), httpContext.current())
          .thenCombineAsync(ogelStage, this::combine, httpContext.current())
          .thenApplyAsync(this::renderViewOgel, httpContext.current());
    }, httpContext.current());
  }

  /**
   * Private methods
   */
  private LicenceInfo combine(LicenceInfo info, CustomerView customer, SiteView site) {
    if (info.hasError()) {
      return info;
    }
    if (customer == null || site == null) {
      return info.setDefaultError();
    }
    info.setCompanyName(customer.getCompanyName());
    info.setCompanyNumber(customer.getCompanyNumber());
    info.setSiteName(site.getSiteName());
    info.setSiteAddress(site.getAddress().getPlainText());
    return info;
  }

  private LicenceInfo combine(LicenceInfo info, OgelFullView ogel) {
    if (info.hasError()) {
      return info;
    } else if (ogel == null) {
      return info.setDefaultError();
    } else {
      info.setLicenceType(ogel.getName());
      info.setLicenceUrl(ogel.getLink());
      return info;
    }
  }

  private CompletionStage<LicenceInfo> getOgelRegistration(LicenceInfo info) {

    String userId = authManager.getAuthInfoFromContext().getId();
    String registrationReference = info.getLicenceNumber();

    return permissionsService.getOgelRegistration(userId, registrationReference)
        .thenApplyAsync(result -> {
          if (!result.isPresent()) {
            LOGGER.error("OgelRegistrationServiceClient - Failed to get OGEL registration {userId={}, registrationReference={}}.", userId, registrationReference);
            return info.setDefaultError();
          }

          OgelRegistrationView view = result.get();
          Status status = view.getStatus();
          if (status == DEREGISTERED || status == SURRENDERED) {
            LOGGER.error("OGEL registration is no longer active - {userId={}, registrationReference={}, status={}}.", userId, registrationReference, status);
            return info.setError("This OGEL is no longer active.");
          } else {
            info.setRegistrationDate(view.getRegistrationDate());
            info.setCustomerId(view.getCustomerId());
            info.setSiteId(view.getSiteId());
            info.setOgelType(view.getOgelType());
            return info;
          }
        }, httpContext.current());
  }

  private CompletionStage<OgelFullView> getOgel(LicenceInfo info) {
    if (info.hasError()) {
      return CompletableFuture.completedFuture(null);
    }
    String ogelType = info.getOgelType();
    return ogelService.getOgel(ogelType)
        .thenApplyAsync(ogel -> {
              if (!ogel.isPresent()) {
                LOGGER.error("OgelServiceClient - Failed to get OGEL {ogelType={}}.", ogelType);
              }
              return ogel.orElse(null);
            },
            httpContext.current());
  }

  private CompletionStage<CustomerView> getCustomer(LicenceInfo info) {

    if (info.hasError()) {
      return CompletableFuture.completedFuture(null);
    }

    String customerId = info.getCustomerId();
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

  private CompletionStage<SiteView> getCustomerSite(LicenceInfo info) {

    if (info.hasError()) {
      return CompletableFuture.completedFuture(null);
    }

    String siteId = info.getSiteId();
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
  private Result renderViewOgel(LicenceInfo info) {
    if (info.hasError()) {
      return badRequest(errorPage.render("Open general export licence not found. This OGEL does not exist or cannot be found."));
    } else {
      return ok(viewOgel.render(info));
    }
  }

}
