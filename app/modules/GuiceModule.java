package modules;

import static components.common.journey.JourneyDefinitionBuilder.moveTo;
import static play.mvc.Results.redirect;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import components.common.CommonGuiceModule;
import components.common.journey.JourneyDefinitionBuilder;
import components.common.journey.JourneyManager;
import components.common.journey.JourneyStage;
import components.common.journey.StandardEvents;
import components.common.state.ContextParamManager;
import controllers.routes;
import journey.Events;
import model.ExportCategory;
import model.TradeType;
import play.Configuration;
import play.Environment;

import java.util.concurrent.CompletableFuture;

public class GuiceModule extends AbstractModule{

  private Environment environment;

  private Configuration configuration;

  public GuiceModule(Environment environment, Configuration configuration) {
    this.environment = environment;
    this.configuration = configuration;
  }

  @Override
  protected void configure() {

    install(new CommonGuiceModule(environment, configuration));

    // controlCodeSearchService
    bindConstant().annotatedWith(Names.named("controlCodeSearchServiceHost"))
        .to(configuration.getString("controlCodeSearchService.hostname"));
    bindConstant().annotatedWith(Names.named("controlCodeSearchServicePort"))
        .to(configuration.getString("controlCodeSearchService.port"));
    bindConstant().annotatedWith(Names.named("controlCodeSearchServiceTimeout"))
        .to(configuration.getString("controlCodeSearchService.timeout"));

    // controlCodeFrontendService
    bindConstant().annotatedWith(Names.named("controlCodeFrontendServiceHost"))
        .to(configuration.getString("controlCodeFrontendService.hostname"));
    bindConstant().annotatedWith(Names.named("controlCodeFrontendServicePort"))
        .to(configuration.getString("controlCodeFrontendService.port"));
    bindConstant().annotatedWith(Names.named("controlCodeFrontendServiceTimeout"))
        .to(configuration.getString("controlCodeFrontendService.timeout"));

    // countryService
    bindConstant().annotatedWith(Names.named("countryServiceHost"))
        .to(configuration.getString("countryService.hostname"));
    bindConstant().annotatedWith(Names.named("countryServicePort"))
        .to(configuration.getString("countryService.port"));
    bindConstant().annotatedWith(Names.named("countryServiceTimeout"))
        .to(configuration.getString("countryService.timeout"));

    // ogelService
    bindConstant().annotatedWith(Names.named("ogelServiceHost"))
        .to(configuration.getString("ogelService.hostname"));
    bindConstant().annotatedWith(Names.named("ogelServicePort"))
        .to(configuration.getString("ogelService.port"));
    bindConstant().annotatedWith(Names.named("ogelServiceTimeout"))
        .to(configuration.getString("ogelService.timeout"));

  }

  @Provides
  @Singleton
  public JourneyManager provideJourneyManager (ContextParamManager cpm) {

    JourneyDefinitionBuilder jdb = new JourneyDefinitionBuilder();

    JourneyStage index = jdb.defineStage("index", "Import / export licensing service",
        () -> CompletableFuture.completedFuture(redirect(routes.EntryPointController.index())));

    JourneyStage startApplication = jdb.defineStage("startApplication", "Saving your application",
        () -> cpm.addParamsAndRedirect(routes.StartApplicationController.renderForm()));

    JourneyStage continueApplication = jdb.defineStage("continueApplication", "Return to your application",
        () -> cpm.addParamsAndRedirect(routes.ContinueApplicationController.renderForm()));

    JourneyStage tradeType = jdb.defineStage("tradeType", "Where are your items going?",
        () -> cpm.addParamsAndRedirect(routes.TradeTypeController.renderForm()));

    JourneyStage importStatic = jdb.defineStage("importStatic", "Import licences",
        () -> cpm.addParamsAndRedirect(routes.StaticContentController.renderImport()));

    JourneyStage categoryArtsCulturalNoLicence = jdb.defineStage("categoryArtsCulturalNoLicence", "???",
        () -> cpm.addParamsAndRedirect(routes.StaticContentController.renderNoLicenceCultural()));

    JourneyStage brokeringTranshipmentStatic = jdb.defineStage("brokeringStatic", "Trade controls, trafficking and brokering",
        () -> cpm.addParamsAndRedirect(routes.StaticContentController.renderBrokeringTranshipment()));

    JourneyStage exportCategory = jdb.defineStage("exportCategory", "What are you exporting?",
        () -> cpm.addParamsAndRedirect(controllers.categories.routes.ExportCategoryController.renderForm()));

    JourneyStage categoryArtsCultural = jdb.defineStage("categoryArtsCultural", "Arts and cultural goods",
        () -> cpm.addParamsAndRedirect(controllers.categories.routes.ArtsCulturalController.renderForm()));

    JourneyStage categoryChemicalsCosmetics = jdb.defineStage("categoryChemicalsCosmetics", "You may need a licence if you are exporting dual-use goods",
        () -> cpm.addParamsAndRedirect(controllers.categories.routes.ChemicalsCosmeticsController.renderForm()));

    JourneyStage categoryDualUse = jdb.defineStage("categoryDualUse", "Do your items have a dual use?",
        () -> cpm.addParamsAndRedirect(controllers.categories.routes.DualUseController.renderForm()));

    JourneyStage categoryFinancialTechnicalAssistance = jdb.defineStage("categoryFinancialTechnicalAssistance", "You should contact the Export Control Organisation to find out if you need a licence",
        () -> cpm.addParamsAndRedirect(controllers.categories.routes.FinancialTechnicalAssistanceController.renderForm()));

    JourneyStage categoryFoodStatic = jdb.defineStage("categoryFood", "You need to check the rules for your export destination",
        () -> cpm.addParamsAndRedirect(routes.StaticContentController.renderCategoryFood()));

    JourneyStage categoryMedicinesDrugs = jdb.defineStage("categoryMedicinesDrugs", "Medicines and drugs",
        () -> cpm.addParamsAndRedirect(controllers.categories.routes.MedicinesDrugsController.renderForm()));

    JourneyStage categoryEndangeredAnimalStatic = jdb.defineStage("categoryEndangeredAnimal", "You may need a CITES permit",
        () -> cpm.addParamsAndRedirect(routes.StaticContentController.renderCategoryEndangeredAnimals()));

    JourneyStage categoryNonEndangeredAnimalStatic = jdb.defineStage("categoryNonEndangeredAnimal", "You may need approval from the destination country",
        () -> cpm.addParamsAndRedirect(routes.StaticContentController.renderCategoryNonEndangeredAnimals()));

    JourneyStage categoryPlantStatic = jdb.defineStage("categoryPlant", "You may need approval from the destination country",
        () -> cpm.addParamsAndRedirect(routes.StaticContentController.renderCategoryPlants()));

    JourneyStage categoryMedicinesDrugsStatic = jdb.defineStage("categoryMedicinesDrugsStatic", "You need a licence to export most drugs and medicines",
        () -> cpm.addParamsAndRedirect(routes.StaticContentController.renderCategoryMedicinesDrugs()));

    // TODO Finish this stubbed stage.
    JourneyStage goodsType = jdb.defineStage("goodsType", "Are you exporting goods, software or technology?",
        () -> null);

    JourneyStage noneDescribed = jdb.defineStage("noneDescribed", "You may not need a licence",
        () -> cpm.addParamsAndRedirect(controllers.search.routes.NoneDescribedController.render()));

    jdb.atStage(index)
        .onEvent(Events.START_APPLICATION)
        .then(moveTo(startApplication));

    jdb.atStage(index)
        .onEvent(Events.CONTINUE_APPLICATION)
        .then(moveTo(continueApplication));

    jdb.atStage(startApplication)
        .onEvent(StandardEvents.NEXT)
        .then(moveTo(tradeType));

    // TODO Convert to injected link in view
    jdb.atStage(continueApplication)
        .onEvent(Events.START_APPLICATION)
        .then(moveTo(startApplication));

    // TODO Restore to last position, requires JourneyManager persistence
    jdb.atStage(continueApplication)
        .onEvent(Events.APPLICATION_FOUND)
        .then(moveTo(tradeType));

    // TODO Add in additional screen catering for this condition IELS-606
    jdb.atStage(continueApplication)
        .onEvent(Events.APPLICATION_NOT_FOUND)
        .then(moveTo(null));

    jdb.atStage(tradeType)
        .onEvent(Events.TRADE_TYPE_SELECTED)
        .branch()
        .when(TradeType.EXPORT, moveTo(exportCategory))
        .when(TradeType.IMPORT, moveTo(importStatic))
        .when(TradeType.BROKERING, moveTo(brokeringTranshipmentStatic))
        .when(TradeType.TRANSSHIPMENT, moveTo(brokeringTranshipmentStatic));

    jdb.atStage(exportCategory)
        .onEvent(Events.EXPORT_CATEGORY_SELECTED)
        .branch()
        .when(ExportCategory.ARTS_CULTURAL, moveTo(categoryArtsCultural))
        .when(ExportCategory.CHEMICALS_COSMETICS, moveTo(categoryChemicalsCosmetics))
        .when(ExportCategory.DUAL_USE, moveTo(goodsType))
        .when(ExportCategory.FINANCIAL_ASSISTANCE, moveTo(categoryFinancialTechnicalAssistance))
        .when(ExportCategory.FOOD, moveTo(categoryFoodStatic))
        .when(ExportCategory.MEDICINES_DRUGS, moveTo(categoryMedicinesDrugs))
        .when(ExportCategory.MILITARY, moveTo(goodsType))
        .when(ExportCategory.NONE, moveTo(categoryDualUse))
        .when(ExportCategory.PLANTS_ANIMALS, moveTo(null))
        .when(ExportCategory.RADIOACTIVE, moveTo(null))
        .when(ExportCategory.TECHNICAL_ASSISTANCE, moveTo(categoryFinancialTechnicalAssistance))
        .when(ExportCategory.TORTURE_RESTRAINT, moveTo(null));

    jdb.atStage(exportCategory)
        .onEvent(Events.EXPORT_CATEGORY_COULD_BE_DUAL_USE)
        .then(moveTo(categoryDualUse));

    jdb.atStage(categoryArtsCultural)
        .onEvent(Events.GOOD_CONTROLLED)
        .then(moveTo(goodsType));

    jdb.atStage(categoryArtsCultural)
        .onEvent(Events.GOOD_NOT_CONTROLLED)
        .then(moveTo(categoryArtsCulturalNoLicence));

    jdb.atStage(categoryChemicalsCosmetics)
        .onEvent(StandardEvents.NEXT)
        .then(moveTo(goodsType));

    jdb.atStage(categoryDualUse)
        .onEvent(Events.GOOD_CONTROLLED)
        .then(moveTo(goodsType));

    jdb.atStage(categoryDualUse)
        .onEvent(Events.GOOD_NOT_CONTROLLED)
        .then(moveTo(noneDescribed));

    jdb.atStage(categoryFinancialTechnicalAssistance)
        .onEvent(StandardEvents.NEXT)
        .then(moveTo(goodsType));

    jdb.atStage(categoryMedicinesDrugs)
        .onEvent(Events.IS_USED_FOR_EXECUTION_TORTURE)
        .branch()
        .when(true, moveTo(null))
        .when(false, moveTo(categoryMedicinesDrugsStatic));

    return new JourneyManager(jdb.build("default", index));
  }

}