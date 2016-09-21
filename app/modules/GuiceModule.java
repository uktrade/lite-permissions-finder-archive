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
import model.ControlCodeFlowStage;
import model.ExportCategory;
import model.GoodsType;
import model.LifeType;
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

    install(new CommonGuiceModule(configuration));

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

    JourneyStage categoryPlantsAnimals = jdb.defineStage("categoryPlantsAnimals", "Plants and animals",
        () -> cpm.addParamsAndRedirect(controllers.categories.routes.PlantsAnimalsController.renderForm()));

    JourneyStage categoryEndangeredAnimalStatic = jdb.defineStage("categoryEndangeredAnimal", "You may need a CITES permit",
        () -> cpm.addParamsAndRedirect(routes.StaticContentController.renderCategoryEndangeredAnimals()));

    JourneyStage categoryNonEndangeredAnimalStatic = jdb.defineStage("categoryNonEndangeredAnimal", "You may need approval from the destination country",
        () -> cpm.addParamsAndRedirect(routes.StaticContentController.renderCategoryNonEndangeredAnimals()));

    JourneyStage categoryPlantStatic = jdb.defineStage("categoryPlant", "You may need approval from the destination country",
        () -> cpm.addParamsAndRedirect(routes.StaticContentController.renderCategoryPlants()));

    JourneyStage categoryMedicinesDrugsStatic = jdb.defineStage("categoryMedicinesDrugsStatic", "You need a licence to export most drugs and medicines",
        () -> cpm.addParamsAndRedirect(routes.StaticContentController.renderCategoryMedicinesDrugs()));

    JourneyStage categoryTortureRestraint = jdb.defineStage("categoryTortureRestraint", "You may not be allowed to export your goods",
        () -> cpm.addParamsAndRedirect(controllers.categories.routes.TortureRestraintController.renderForm()));

    JourneyStage categoryRadioactive = jdb.defineStage("categoryRadioactive", "You need a licence to export radioactive materials above certain activity thresholds",
        () -> cpm.addParamsAndRedirect(controllers.categories.routes.RadioactiveController.renderForm()));

    JourneyStage noneDescribed = jdb.defineStage("noneDescribed", "You may not need a licence",
        () -> cpm.addParamsAndRedirect(controllers.search.routes.NoneDescribedController.render()));

    JourneyStage goodsType = jdb.defineStage("goodsType", "Are you exporting goods, software or technology?",
        () -> cpm.addParamsAndRedirect(routes.GoodsTypeController.renderForm()));

    JourneyStage physicalGoodsSearch = jdb.defineStage("physicalGoodsSearch", "Describe your goods",
        () -> cpm.addParamsAndRedirect(controllers.search.routes.PhysicalGoodsSearchController.renderForm()));

    JourneyStage physicalGoodsSearchResults = jdb.defineStage("physicalGoodsSearchResults", "Possible matches",
        () -> cpm.addParamsAndRedirect(controllers.search.routes.PhysicalGoodsSearchResultsController.renderForm()));

    JourneyStage controlCode = jdb.defineStage("controlCode", "Summary",
        () -> cpm.addParamsAndRedirect(controllers.controlcode.routes.ControlCodeController.renderForm()));

    JourneyStage additionalSpecifications = jdb.defineStage("additionalSpecifications", "Additional specifications",
        () -> cpm.addParamsAndRedirect(controllers.controlcode.routes.AdditionalSpecificationsController.renderForm()));

    JourneyStage decontrols = jdb.defineStage("decontrols", "Decontrols",
        () -> cpm.addParamsAndRedirect(controllers.controlcode.routes.DecontrolsController.renderForm()));

    JourneyStage technicalNotes = jdb.defineStage("technicalNotes", "Technical notes",
        () -> cpm.addParamsAndRedirect(controllers.controlcode.routes.TechnicalNotesController.renderForm()));

    JourneyStage decontrolledItem = jdb.defineStage("decontrolledItem", "Decontrolled item",
        () -> cpm.addParamsAndRedirect(controllers.controlcode.routes.DecontrolledItemController.renderForm()));

    JourneyStage searchAgain = jdb.defineStage("searchAgain", "Search again",
        () -> cpm.addParamsAndRedirect(controllers.controlcode.routes.SearchAgainController.renderForm()));

    JourneyStage destinationCountries = jdb.defineStage("destinationCountries", "Where are your items being sent to?",
        () -> cpm.addParamsAndRedirect(routes.DestinationCountryController.renderForm()));

    JourneyStage ogelQuestions = jdb.defineStage("ogelQuestions", "Are you exporting for any of these reasons",
        () -> cpm.addParamsAndRedirect(controllers.ogel.routes.OgelQuestionsController.renderForm()));

    JourneyStage ogelResults = jdb.defineStage("ogelResults", "Licences applicable to your answers",
        () -> cpm.addParamsAndRedirect(controllers.ogel.routes.OgelResultsController.renderForm()));

    JourneyStage ogelSummary = jdb.defineStage("ogelSummary", "Licence summary",
        () -> cpm.addParamsAndRedirect(controllers.ogel.routes.OgelSummaryController.renderForm()));

    JourneyStage summary = jdb.defineStage("summary", "Check your answers so far",
        () -> cpm.addParamsAndRedirect(routes.SummaryController.renderForm()));

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

    jdb.atStage(continueApplication)
        .onEvent(Events.APPLICATION_NOT_FOUND)
        .then(moveTo(null)); // TODO Add in additional screen catering for this condition IELS-606

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
        .when(ExportCategory.PLANTS_ANIMALS, moveTo(categoryPlantsAnimals))
        .when(ExportCategory.RADIOACTIVE, moveTo(categoryRadioactive))
        .when(ExportCategory.TECHNICAL_ASSISTANCE, moveTo(categoryFinancialTechnicalAssistance))
        .when(ExportCategory.TORTURE_RESTRAINT, moveTo(categoryTortureRestraint));

    jdb.atStage(exportCategory)
        .onEvent(Events.EXPORT_CATEGORY_COULD_BE_DUAL_USE)
        .then(moveTo(categoryDualUse));

    jdb.atStage(categoryArtsCultural)
        .onEvent(Events.IS_CONTROLLED_HISTORIC_GOOD)
        .branch()
        .when(true, moveTo(goodsType))
        .when(false, moveTo(categoryArtsCulturalNoLicence));

    jdb.atStage(categoryChemicalsCosmetics)
        .onEvent(StandardEvents.NEXT)
        .then(moveTo(goodsType));

    jdb.atStage(categoryDualUse)
        .onEvent(Events.IS_DUAL_USE)
        .branch()
        .when(true, moveTo(goodsType))
        .when(false, moveTo(noneDescribed));

    jdb.atStage(categoryFinancialTechnicalAssistance)
        .onEvent(StandardEvents.NEXT)
        .then(moveTo(goodsType));

    jdb.atStage(categoryMedicinesDrugs)
        .onEvent(Events.IS_USED_FOR_EXECUTION_TORTURE)
        .branch()
        .when(true, moveTo(categoryTortureRestraint))
        .when(false, moveTo(categoryMedicinesDrugsStatic));

    jdb.atStage(categoryPlantsAnimals)
        .onEvent(Events.LIFE_TYPE_SELECTED)
        .branch()
        .when(LifeType.ENDANGERED, moveTo(categoryEndangeredAnimalStatic))
        .when(LifeType.NON_ENDANGERED, moveTo(categoryNonEndangeredAnimalStatic))
        .when(LifeType.PLANT, moveTo(categoryPlantStatic));

    jdb.atStage(categoryTortureRestraint)
        .onEvent(StandardEvents.NEXT)
        .then(moveTo(goodsType));

    jdb.atStage(categoryRadioactive)
        .onEvent(StandardEvents.NEXT)
        .then(moveTo(goodsType));

    jdb.atStage(goodsType)
        .onEvent(Events.GOODS_TYPE_SELECTED)
        .branch()
        .when(GoodsType.PHYSICAL, moveTo(physicalGoodsSearch))
        .when(GoodsType.SOFTWARE, moveTo(null)) // TODO Not implemented screen
        .when(GoodsType.TECHNOLOGY, moveTo(null)); // TODO Not implemented screen

    jdb.atStage(physicalGoodsSearch)
        .onEvent(Events.SEARCH_PHYSICAL_GOODS)
        .then(moveTo(physicalGoodsSearchResults));

    jdb.atStage(physicalGoodsSearchResults)
        .onEvent(Events.CONTROL_CODE_SELECTED)
        .then(moveTo(controlCode));

    jdb.atStage(physicalGoodsSearchResults)
        .onEvent(Events.NONE_MATCHED)
        .then(moveTo(noneDescribed));

    jdb.atStage(controlCode)
        .onEvent(Events.CONTROL_CODE_FLOW_NEXT)
        .branch()
        .when(ControlCodeFlowStage.ADDITIONAL_SPECIFICATIONS, moveTo(additionalSpecifications))
        .when(ControlCodeFlowStage.DECONTROLS, moveTo(decontrols))
        .when(ControlCodeFlowStage.TECHNICAL_NOTES, moveTo(technicalNotes))
        .when(ControlCodeFlowStage.CONFIRMED, moveTo(destinationCountries))
        .when(ControlCodeFlowStage.SEARCH_AGAIN, moveTo(searchAgain))
        .when(ControlCodeFlowStage.BACK_TO_SEARCH, moveTo(physicalGoodsSearch))
        .when(ControlCodeFlowStage.BACK_TO_SEARCH_RESULTS, moveTo(physicalGoodsSearchResults));

    jdb.atStage(additionalSpecifications)
        .onEvent(Events.CONTROL_CODE_FLOW_NEXT)
        .branch()
        .when(ControlCodeFlowStage.DECONTROLS, moveTo(decontrols))
        .when(ControlCodeFlowStage.TECHNICAL_NOTES, moveTo(technicalNotes))
        .when(ControlCodeFlowStage.CONFIRMED, moveTo(destinationCountries))
        .when(ControlCodeFlowStage.SEARCH_AGAIN, moveTo(searchAgain));

    jdb.atStage(decontrols)
        .onEvent(Events.CONTROL_CODE_FLOW_NEXT)
        .branch()
        .when(ControlCodeFlowStage.DECONTROLLED_ITEM, moveTo(decontrolledItem))
        .when(ControlCodeFlowStage.TECHNICAL_NOTES, moveTo(technicalNotes))
        .when(ControlCodeFlowStage.CONFIRMED, moveTo(destinationCountries));

    jdb.atStage(technicalNotes)
        .onEvent(Events.CONTROL_CODE_FLOW_NEXT)
        .branch()
        .when(ControlCodeFlowStage.CONFIRMED, moveTo(destinationCountries))
        .when(ControlCodeFlowStage.SEARCH_AGAIN, moveTo(searchAgain));

    jdb.atStage(decontrolledItem)
        .onEvent(Events.CONTROL_CODE_FLOW_NEXT)
        .branch()
        .when(ControlCodeFlowStage.BACK_TO_SEARCH, moveTo(physicalGoodsSearch))
        .when(ControlCodeFlowStage.BACK_TO_SEARCH_RESULTS, moveTo(physicalGoodsSearchResults));

    jdb.atStage(searchAgain)
        .onEvent(Events.CONTROL_CODE_FLOW_NEXT)
        .branch()
        .when(ControlCodeFlowStage.BACK_TO_SEARCH, moveTo(physicalGoodsSearch))
        .when(ControlCodeFlowStage.BACK_TO_SEARCH_RESULTS, moveTo(physicalGoodsSearchResults));

    jdb.atStage(destinationCountries)
        .onEvent(Events.DESTINATION_COUNTRIES_SELECTED)
        .then(moveTo(ogelQuestions));

    jdb.atStage(ogelQuestions)
        .onEvent(Events.OGEL_QUESTIONS_ANSWERED)
        .then(moveTo(ogelResults));

    jdb.atStage(ogelResults)
        .onEvent(Events.OGEL_SELECTED)
        .then(moveTo(ogelSummary));

    jdb.atStage(ogelSummary)
        .onEvent(Events.OGEL_REGISTERED)
        .then(moveTo(summary));

    jdb.atStage(summary)
        .onEvent(Events.CHANGE_CONTROL_CODE)
        .then(moveTo(physicalGoodsSearch));

    jdb.atStage(summary)
        .onEvent(Events.CHANGE_OGEL_TYPE)
        .then(moveTo(ogelQuestions));

    jdb.atStage(summary)
        .onEvent(Events.CHANGE_DESTINATION_COUNTRIES)
        .then(moveTo(destinationCountries));

    jdb.atStage(summary)
        .onEvent(Events.HANDOFF_TO_OGEL_REGISTRATION)
        .then(moveTo(null)); // TODO handoff to OGEL registration

    return new JourneyManager(jdb.build("default", index));
  }

}