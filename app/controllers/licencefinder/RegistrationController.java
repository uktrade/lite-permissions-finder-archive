package controllers.licencefinder;

import com.google.inject.Inject;
import components.auth.SamlAuthorizer;
import components.common.auth.SpireSAML2Client;
import components.common.cache.CountryProvider;
import components.common.transaction.TransactionManager;
import components.persistence.LicenceFinderDao;
import components.services.controlcode.frontend.FrontendServiceClient;
import components.services.ogels.applicable.ApplicableOgelServiceClient;
import components.services.ogels.conditions.OgelConditionsServiceClient;
import components.services.ogels.ogel.OgelServiceClient;
import org.pac4j.play.java.Secure;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;

import javax.inject.Named;

@Secure(clients = SpireSAML2Client.CLIENT_NAME, authorizers = SamlAuthorizer.AUTHORIZER_NAME)
public class RegistrationController extends Controller {

  private final FormFactory formFactory;
  private final LicenceFinderDao dao;
  private final TransactionManager transactionManager;
  private final CountryProvider countryProvider;
  private final OgelConditionsServiceClient conditionsClient;
  private final HttpExecutionContext httpContext;
  private final FrontendServiceClient frontendClient;
  private final ApplicableOgelServiceClient applicableClient;
  private final String dashboardUrl;
  private final OgelServiceClient ogelClient;


  @Inject
  public RegistrationController(TransactionManager transactionManager, FormFactory formFactory,
                                 HttpExecutionContext httpContext,
                                 LicenceFinderDao dao, @Named("countryProviderExport") CountryProvider countryProvider,
                                 OgelConditionsServiceClient conditionsClient, FrontendServiceClient frontendClient,
                                 ApplicableOgelServiceClient applicableClient,
                                 @com.google.inject.name.Named("dashboardUrl") String dashboardUrl,
                                 OgelServiceClient ogelClient) {
    this.transactionManager = transactionManager;
    this.formFactory = formFactory;
    this.httpContext = httpContext;
    this.dao = dao;
    this.countryProvider = countryProvider;
    this.conditionsClient = conditionsClient;
    this.frontendClient = frontendClient;
    this.applicableClient = applicableClient;
    this.dashboardUrl = dashboardUrl;
    this.ogelClient = ogelClient;
  }
}
