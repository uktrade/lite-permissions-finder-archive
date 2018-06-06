package controllers.licencefinder;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.Inject;
import components.auth.SamlAuthorizer;
import components.common.auth.SpireSAML2Client;
import components.common.cache.CountryProvider;
import components.common.state.ContextParamManager;
import components.persistence.LicenceFinderDao;
import components.services.controlcode.frontend.FrontendServiceClient;
import components.services.controlcode.frontend.FrontendServiceResult;
import components.services.ogels.applicable.ApplicableOgelServiceClient;
import models.OgelActivityType;
import models.ogel.OgelResultsDisplay;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.play.java.Secure;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import uk.gov.bis.lite.countryservice.api.CountryView;
import utils.CountryUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.inject.Named;

@Secure(clients = SpireSAML2Client.CLIENT_NAME, authorizers = SamlAuthorizer.AUTHORIZER_NAME)
public class ResultsController extends Controller {

  private final FormFactory formFactory;
  private final LicenceFinderDao dao;
  private final CountryProvider countryProvider;
  private final HttpExecutionContext httpContext;
  private final FrontendServiceClient frontendClient;
  private final ApplicableOgelServiceClient applicableClient;
  private final ContextParamManager contextParam;
  private final views.html.licencefinder.results results;

  public static final String NONE_ABOVE_KEY = "NONE_ABOVE_KEY";

  @Inject
  public ResultsController(FormFactory formFactory,
                           HttpExecutionContext httpContext,
                           LicenceFinderDao dao, @Named("countryProviderExport") CountryProvider countryProvider,
                           FrontendServiceClient frontendClient,
                           ApplicableOgelServiceClient applicableClient, ContextParamManager contextParam,
                           views.html.licencefinder.results results) {
    this.formFactory = formFactory;
    this.httpContext = httpContext;
    this.dao = dao;
    this.countryProvider = countryProvider;
    this.frontendClient = frontendClient;
    this.applicableClient = applicableClient;
    this.contextParam = contextParam;
    this.results = results;
  }


  /************************************************************************************************
   * 'Results' page
   *******************************************************************************************/
  public CompletionStage<Result> renderResultsForm() {
    return renderWithForm(formFactory.form(ResultsForm.class));
  }

  public CompletionStage<Result> handleResultsSubmit() {
    Form<ResultsForm> form = formFactory.form(ResultsForm.class).bindFromRequest();
    if (form.hasErrors()) {
      return renderWithForm(form);
    }
    String chosenOgel = form.get().chosenOgel;
    dao.saveOgelId(chosenOgel);

    // Return No licences available when 'None of the above' chosen
    if (chosenOgel.equals(NONE_ABOVE_KEY)) {
      String controlCode = dao.getControlCode();
      String destinationCountryName = countryProvider.getCountry(dao.getSourceCountry()).getCountryName();
      try {
        FrontendServiceResult result = frontendClient.get(controlCode).toCompletableFuture().get();
        OgelResultsDisplay display = new OgelResultsDisplay(Collections.emptyList(), result.getFrontendControlCode(), null, controlCode, destinationCountryName);
        return completedFuture(ok(results.render(form, display)));
      } catch (InterruptedException | ExecutionException e) {
        Logger.error("NONE_ABOVE_KEY error", e);
      }
    }

    return contextParam.addParamsAndRedirect(routes.RegisterToUseController.renderRegisterToUseForm());
  }

  private CompletionStage<Result> renderWithForm(Form<ResultsForm> form) {

    String controlCode = dao.getControlCode();
    String destinationCountry = dao.getDestinationCountry();
    String destinationCountryName = countryProvider.getCountry(destinationCountry).getCountryName();
    List<String> exportRouteCountries = getExportRouteCountries();

    List<String> activities = Collections.emptyList();
    boolean showHistoricOgel = true; // set as default
    Optional<QuestionsController.QuestionsForm> optQuestionsForm = dao.getQuestionsForm();
    if (optQuestionsForm.isPresent()) {
      QuestionsController.QuestionsForm questionsForm = optQuestionsForm.get();
      activities = getActivityTypes(questionsForm);
      showHistoricOgel = questionsForm.beforeOrLess;
    }

    CompletionStage<FrontendServiceResult> frontendServiceStage = frontendClient.get(controlCode);

    return applicableClient.get(controlCode, dao.getSourceCountry(), exportRouteCountries, activities, showHistoricOgel)
        .thenCombineAsync(frontendServiceStage, (applicableOgelView, frontendServiceResult) -> {
          if (!applicableOgelView.isEmpty()) {
            OgelResultsDisplay display = new OgelResultsDisplay(applicableOgelView, frontendServiceResult.getFrontendControlCode(),
                null, controlCode, destinationCountryName);
            return ok(results.render(form, display));
          } else {
            List<String> countryNames = CountryUtils.getFilteredCountries(CountryUtils.getSortedCountries(countryProvider.getCountries()), exportRouteCountries).stream()
                .map(CountryView::getCountryName)
                .collect(Collectors.toList());
            OgelResultsDisplay display = new OgelResultsDisplay(applicableOgelView, frontendServiceResult.getFrontendControlCode(),
                countryNames, controlCode, destinationCountryName);

            return ok(results.render(form, display));
          }
        }, httpContext.current());
  }

  private List<String> getExportRouteCountries() {
    List<String> countries = new ArrayList<>();
    String destination = dao.getDestinationCountry();
    if (!StringUtils.isBlank(destination)) {
      countries.add(destination);
    }
    String first = dao.getFirstConsigneeCountry();
    if (!StringUtils.isBlank(first)) {
      countries.add(first);
    }
    return countries;
  }

  private List<String> getActivityTypes(QuestionsController.QuestionsForm questionsForm) {
    Map<OgelActivityType, String> map = new HashMap<>();
    map.put(OgelActivityType.DU_ANY, OgelActivityType.DU_ANY.value());
    map.put(OgelActivityType.EXHIBITION, OgelActivityType.EXHIBITION.value());
    map.put(OgelActivityType.MIL_ANY, OgelActivityType.MIL_ANY.value());
    map.put(OgelActivityType.MIL_GOV, OgelActivityType.MIL_GOV.value());
    map.put(OgelActivityType.REPAIR, OgelActivityType.REPAIR.value());
    if (!questionsForm.forRepair) {
      map.remove(OgelActivityType.REPAIR);
    }
    if (!questionsForm.forExhibition) {
      map.remove(OgelActivityType.EXHIBITION);
    }
    return map.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList());
  }

  public static class ResultsForm {
    @Required(message = "You must choose from the list of results below")
    public String chosenOgel;
  }

}

