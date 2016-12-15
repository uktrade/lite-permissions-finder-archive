package controllers.importcontent;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.Inject;
import components.common.cache.CountryProvider;
import components.common.journey.JourneyManager;
import components.persistence.ImportJourneyDao;
import exceptions.FormStateException;
import importcontent.ImportEvents;
import importcontent.models.ImportWhere;
import models.common.Country;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.importcontent.importCountry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import javax.inject.Named;

public class ImportWhereController extends Controller {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final ImportJourneyDao importJourneyDao;
  private final CountryProvider countryProviderExport;
  private final CountryProvider countryProviderEu;

  public static final String COUNTRY_FIELD_NAME = "importCountry";

  @Inject
  public ImportWhereController(JourneyManager journeyManager, FormFactory formFactory, ImportJourneyDao importJourneyDao,
                               @Named("countryProviderExport") CountryProvider countryProviderExport,
                               @Named("countryProviderEu") CountryProvider countryProviderEu) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.importJourneyDao = importJourneyDao;
    this.countryProviderExport = countryProviderExport;
    this.countryProviderEu = countryProviderEu;
  }

  public Result renderForm() {
    return ok(importCountry.render(formFactory.form(), new ArrayList<>(countryProviderExport.getCountries())));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<ImportCountryForm> form = formFactory.form(ImportCountryForm.class).bindFromRequest();

    List<Country> countries = new ArrayList<Country>(countryProviderExport.getCountries());

    if (form.hasErrors()) {
      return completedFuture(ok(importCountry.render(form, countries)));
    }

    String importCountry = form.get().importCountry;
    Optional<Country> optCountry = countries.stream().filter(country -> importCountry.equals(country.getCountryRef())).findFirst();

    if (optCountry.isPresent()) {
      importJourneyDao.saveImportCountrySelected(importCountry);
      return journeyManager.performTransition(ImportEvents.IMPORT_WHERE_SELECTED, getImportWhereRoute(optCountry.get().getCountryRef()));
    } else {
      throw new FormStateException("Country not recognised: " + importCountry);
    }
  }

  /**
   * Returns appropriate ImportWhere enum from selected Spire country code
   */
  private ImportWhere getImportWhereRoute(String spireCode) {
    if (ImportController.MILITARY_COUNTRY_SPIRE_CODES.contains(spireCode)) {
      return ImportWhere.MILITARY_COUNTRIES;
    } else if (spireCode.equals(ImportController.SOMALIA_SPIRE_CODE)) {
      return ImportWhere.CHARCOAL_COUNTRIES;
    } else if (spireCode.equals(ImportController.SYRIA_SPIRE_CODE)) {
      return ImportWhere.SYRIA_COUNTRY;
    } else {
      if (isEuCountry(spireCode)) {
        return ImportWhere.EU_COUNTRIES;
      }
    }
    return ImportWhere.OTHER_COUNTRIES;
  }

  private boolean isEuCountry(String spireCountryCode) {
    List<Country> euCountries = new ArrayList<Country>(countryProviderEu.getCountries());
    return euCountries.stream().anyMatch(c -> spireCountryCode.equals(c.getCountryRef()));
  }

  /**
   * ImportCountryForm
   */
  public static class ImportCountryForm {
    @Constraints.Required(message = "Please select one option")
    public String importCountry;
  }
}



