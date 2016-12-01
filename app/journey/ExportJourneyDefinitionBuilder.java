package journey;

import components.common.journey.BackLink;
import components.common.journey.JourneyDefinitionBuilder;
import components.common.journey.JourneyStage;
import components.common.journey.StandardEvents;
import controllers.routes;
import models.ArtsCulturalGoodsType;
import models.ControlCodeFlowStage;
import models.ExportCategory;
import models.GoodsType;
import models.LifeType;
import models.NonMilitaryFirearmExportBySelfType;
import models.VirtualEUOgelStage;
import models.softtech.ApplicableSoftTechControls;
import models.softtech.CatchallSoftTechControlsFlow;
import models.softtech.ControlsRelatedToPhysicalGoodsFlow;
import models.softtech.Relationship;
import models.softtech.SoftTechCatchallControlsNotApplicableFlow;
import models.softtech.SoftwareControlsNotApplicableFlow;
import models.softtech.SoftwareExemptionsFlow;

public class ExportJourneyDefinitionBuilder extends JourneyDefinitionBuilder {

  private final JourneyStage exportCategory = defineStage("exportCategory", "What are you exporting?",
      controllers.categories.routes.ExportCategoryController.renderForm());
  private final JourneyStage goodsType = defineStage("goodsType", "Are you exporting goods, software or technical information?",
      routes.GoodsTypeController.renderForm());
  private final JourneyStage physicalGoodsSearch = defineStage("physicalGoodsSearch", "Describe your items",
      controllers.search.routes.PhysicalGoodsSearchController.renderSearchForm());
  private final JourneyStage destinationCountries = defineStage("destinationCountries", "Countries and territories",
      routes.DestinationCountryController.renderForm());
  private final JourneyStage ogelQuestions = defineStage("ogelQuestions", "Refining your licence results",
      controllers.ogel.routes.OgelQuestionsController.renderForm());
  private final JourneyStage notImplemented = defineStage("notImplemented", "This section is currently under development",
      routes.StaticContentController.renderNotImplemented());
  private final JourneyStage notApplicable = defineStage("notApplicable", "You cannot use this service to get an export licence",
      routes.StaticContentController.renderNotApplicable());

  /** Software **/
  private final JourneyStage softwareExemptions = defineStage("softwareExemptions", "Some types of software do not need a licence",
      controllers.softtech.routes.ExemptionsController.renderForm());
  private final JourneyStage physicalGoodsSearchRelatedToSoftware = defineStage("physicalGoodsSearchRelatedToSoftware", "Describe your items",
      controllers.search.routes.PhysicalGoodsSearchController.renderSearchRelatedToForm(GoodsType.SOFTWARE.toUrlString()));
  private final JourneyStage controlCodeForSoftwareControls = defineStage("controlCodeSoftwareControls", "Summary",
      controllers.controlcode.routes.ControlCodeController.renderSoftwareControlsForm());
  private final JourneyStage controlCodeForRelatedSoftwareControls = defineStage("controlCodeForRelatedSoftwareControls", "Summary",
      controllers.controlcode.routes.ControlCodeController.renderRelatedSoftwareControlsForm());
  private final JourneyStage controlCodeSoftwareCatchallControls = defineStage("controlCodeSoftwareCatchallControls", "Summary",
      controllers.controlcode.routes.ControlCodeController.renderSoftwareCatchallControlsForm());
  private final JourneyStage softwareCategoryControls = defineStage("softwareCategoryControls", "Showing controls related to software category",
      controllers.softtech.controls.routes.SoftTechControlsController.renderSofwareCategoryForm());
  private final JourneyStage softwareRelatedToPhysicalGoodControls = defineStage("softwareRelatedToPhysicalGoodControls", "Showing controls related to your selected physical good",
      controllers.softtech.controls.routes.SoftTechControlsController.renderRelatedToPhysicalGoodForm());
  private final JourneyStage softwareCatchallControls= defineStage("softwareCatchallControls", "Showing catchall controls related to your items category",
      controllers.softtech.controls.routes.SoftTechControlsController.renderSoftwareCatchallForm());
  private JourneyStage relatedToEquipmentOrMaterials = defineStage("relatedToEquipmentOrMaterials", "Is your software any of the following?",
      controllers.softtech.routes.RelatedEquipmentController.renderForm());
  private JourneyStage noSoftwareControlsExist = defineStage("noSoftTechControlsExist", "No software controls exist for item",
      controllers.softtech.controls.routes.NoSoftTechControlsExistController.renderForm());
  private JourneyStage softwareControlsNLR = defineStage("softwareControlsNLR", "No software controls exist for the selected item",
      routes.StaticContentController.renderSoftwareControlsNLR());
  private JourneyStage softwareRelationshipNLR = defineStage("softwareRelationshipNLR", "No software controls exist for the selected item",
      routes.StaticContentController.renderSoftwareRelationshipNLR());
  private JourneyStage softwareTechnologyRelationship = defineStage("softwareTechnologyRelationship", "Is export covered by software technology relationship?",
      controllers.softtech.routes.RelationshipController.renderForm());

  public ExportJourneyDefinitionBuilder() {}

  @Override
  protected void journeys() {
    // *** Stages/transitions ***

    goodsCategoryStages();

    atStage(goodsType)
        .onEvent(Events.GOODS_TYPE_SELECTED)
        .branch()
        .when(GoodsType.PHYSICAL, moveTo(physicalGoodsSearch))
        .when(GoodsType.SOFTWARE, moveTo(softwareExemptions))
        .when(GoodsType.TECHNOLOGY, moveTo(notImplemented));

    physicalGoodsStages();

    softwareStages();

    // *** Journeys ***

    defineJourney(JourneyDefinitionNames.EXPORT, exportCategory, BackLink.to(routes.TradeTypeController.renderForm(),
        "Where are your items going?"));

    defineJourney(JourneyDefinitionNames.CHANGE_CONTROL_CODE, physicalGoodsSearch,
        BackLink.to(routes.SummaryController.renderForm(), "Summary"));
    defineJourney(JourneyDefinitionNames.CHANGE_DESTINATION_COUNTRIES, destinationCountries,
        BackLink.to(routes.SummaryController.renderForm(), "Summary"));
    defineJourney(JourneyDefinitionNames.CHANGE_OGEL_TYPE, ogelQuestions,
        BackLink.to(routes.SummaryController.renderForm(), "Summary"));
  }

  private void goodsCategoryStages() {

    JourneyStage categoryArtsCultural = defineStage("categoryArtsCultural", "Arts and cultural goods",
        controllers.categories.routes.ArtsCulturalController.renderForm());

    JourneyStage categoryArtsCulturalHistoric = defineStage("categoryArtsCulturalHistoric",
        "You may need an Arts Council licence",
        routes.StaticContentController.renderCategoryArtsCulturalHistoric());

    JourneyStage categoryArtsCulturalNonHistoric = defineStage("categoryArtsCulturalNonHistoric",
        "You need an Arts Council licence to export specific items",
        routes.StaticContentController.renderCategoryArtsCulturalNonHistoric());

    JourneyStage categoryArtsCulturalFirearmHistoric = defineStage("categoryArtsCulturalFirearmHistoric",
        "You may need an Arts Council licence, and an export licence",
        controllers.categories.routes.ArtsCulturalFirearmHistoricController.renderForm());

    JourneyStage categoryChemicalsCosmetics = defineStage("categoryChemicalsCosmetics",
        "Cosmetics, chemicals and pesticides", controllers.categories.routes.ChemicalsCosmeticsController.renderForm());

    JourneyStage categoryDualUse = defineStage("categoryDualUse", "Do your items have a dual use?",
        controllers.categories.routes.DualUseController.renderForm());

    JourneyStage categoryFinancialTechnicalAssistance = defineStage("categoryFinancialTechnicalAssistance",
        "You should contact the Export Control Organisation to find out if you need a licence",
        controllers.categories.routes.FinancialTechnicalAssistanceController.renderForm());

    JourneyStage categoryFoodStatic = defineStage("categoryFood", "You need to check the rules for your export destination",
        routes.StaticContentController.renderCategoryFood());

    JourneyStage categoryMedicinesDrugs = defineStage("categoryMedicinesDrugs", "Medicines and drugs",
        controllers.categories.routes.MedicinesDrugsController.renderForm());

    JourneyStage categoryNonMilitary = defineStage("categoryNonMilitary",
        "Will you be taking the firearms or ammunition out of the UK yourself?",
        controllers.categories.routes.NonMilitaryController.renderForm());

    JourneyStage categoryNonMilitaryCheckDestination = defineStage("categoryNonMilitaryCheckDestination",
        "You need to check the rules for your destination country",
        routes.StaticContentController.renderCategoryNonMilitaryFirearmsCheckDestination());

    JourneyStage categoryNonMilitaryNeedExportLicence = defineStage("categoryNonMilitaryNeedExportLicence",
        "You need an export licence", routes.StaticContentController.renderCategoryNonMilitaryFirearmsNeedExportLicence());

    JourneyStage categoryPlantsAnimals = defineStage("categoryPlantsAnimals", "Plants and animals",
        controllers.categories.routes.PlantsAnimalsController.renderForm());

    JourneyStage categoryEndangeredAnimalStatic = defineStage("categoryEndangeredAnimal", "You may need a CITES permit",
        routes.StaticContentController.renderCategoryEndangeredAnimals());

    JourneyStage categoryNonEndangeredAnimalStatic = defineStage("categoryNonEndangeredAnimal",
        "You may need approval from the destination country",
        routes.StaticContentController.renderCategoryNonEndangeredAnimals());

    JourneyStage categoryPlantStatic = defineStage("categoryPlant", "You may need approval from the destination country",
        routes.StaticContentController.renderCategoryPlants());

    JourneyStage categoryMedicinesDrugsStatic = defineStage("categoryMedicinesDrugsStatic",
        "You need a licence to export most drugs and medicines",
        routes.StaticContentController.renderCategoryMedicinesDrugs());

    JourneyStage categoryTortureRestraint = defineStage("categoryTortureRestraint",
        "You may not be allowed to export your goods", controllers.categories.routes.TortureRestraintController.renderForm());

    JourneyStage categoryRadioactive = defineStage("categoryRadioactive",
        "You need a licence to export radioactive materials above certain activity thresholds",
        controllers.categories.routes.RadioactiveController.renderForm());

    JourneyStage categoryWaste = defineStage("categoryWaste", "You must have a licence to export most types of waste",
        routes.StaticContentController.renderCategoryWaste());

    atStage(exportCategory)
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
        .when(ExportCategory.NON_MILITARY, moveTo(categoryNonMilitary))
        .when(ExportCategory.PLANTS_ANIMALS, moveTo(categoryPlantsAnimals))
        .when(ExportCategory.RADIOACTIVE, moveTo(categoryRadioactive))
        .when(ExportCategory.TECHNICAL_ASSISTANCE, moveTo(categoryFinancialTechnicalAssistance))
        .when(ExportCategory.TORTURE_RESTRAINT, moveTo(categoryTortureRestraint))
        .when(ExportCategory.WASTE, moveTo(categoryWaste));

    atStage(exportCategory)
        .onEvent(Events.EXPORT_CATEGORY_COULD_BE_DUAL_USE)
        .then(moveTo(categoryDualUse));

    atStage(categoryArtsCultural)
        .onEvent(Events.ARTS_CULTURAL_CATEGORY_SELECTED)
        .branch()
        .when(ArtsCulturalGoodsType.HISTORIC, moveTo(categoryArtsCulturalHistoric))
        .when(ArtsCulturalGoodsType.NON_HISTORIC, moveTo(categoryArtsCulturalNonHistoric))
        .when(ArtsCulturalGoodsType.FIREARM_HISTORIC, moveTo(categoryArtsCulturalFirearmHistoric))
        .when(ArtsCulturalGoodsType.FIREARM_NON_HISTORIC, moveTo(categoryNonMilitary));

    // Note use of EXPORT_CATEGORY_SELECTED for single value
    atStage(categoryArtsCulturalFirearmHistoric)
        .onEvent(Events.EXPORT_CATEGORY_SELECTED)
        .branch()
        .when(ExportCategory.NON_MILITARY, moveTo(categoryNonMilitary));

    atStage(categoryChemicalsCosmetics)
        .onEvent(Events.SEARCH_PHYSICAL_GOODS)
        .then(moveTo(physicalGoodsSearch));

    atStage(categoryDualUse)
        .onEvent(Events.IS_DUAL_USE)
        .branch()
        .when(true, moveTo(goodsType))
        .when(false, moveTo(notApplicable));

    atStage(categoryFinancialTechnicalAssistance)
        .onEvent(StandardEvents.NEXT)
        .then(moveTo(notImplemented)); // TODO This should go through to the technical information search (when implemented)

    atStage(categoryMedicinesDrugs)
        .onEvent(Events.IS_USED_FOR_EXECUTION_TORTURE)
        .branch()
        .when(true, moveTo(categoryTortureRestraint))
        .when(false, moveTo(categoryMedicinesDrugsStatic));

    atStage(categoryNonMilitary)
        .onEvent(Events.NON_MILITARY_FIREARMS_QUESTION_ANSWERERD)
        .branch()
        .when(NonMilitaryFirearmExportBySelfType.YES, moveTo(categoryNonMilitaryCheckDestination))
        .when(NonMilitaryFirearmExportBySelfType.NO_INCLUDED_IN_PERSONAL_EFFECTS, moveTo(categoryNonMilitaryCheckDestination))
        .when(NonMilitaryFirearmExportBySelfType.NO_TRANSFER_TO_THIRD_PARTY, moveTo(categoryNonMilitaryNeedExportLicence));

    atStage(categoryPlantsAnimals)
        .onEvent(Events.LIFE_TYPE_SELECTED)
        .branch()
        .when(LifeType.ENDANGERED, moveTo(categoryEndangeredAnimalStatic))
        .when(LifeType.NON_ENDANGERED, moveTo(categoryNonEndangeredAnimalStatic))
        .when(LifeType.PLANT, moveTo(categoryPlantStatic));

    atStage(categoryTortureRestraint)
        .onEvent(Events.SEARCH_PHYSICAL_GOODS)
        .then(moveTo(physicalGoodsSearch));

    atStage(categoryRadioactive)
        .onEvent(StandardEvents.NEXT)
        .then(moveTo(destinationCountries));

  }

  private void physicalGoodsStages() {

    JourneyStage physicalGoodsSearchResults = defineStage("physicalGoodsSearchResults", "Possible matches",
        controllers.search.routes.PhysicalGoodsSearchResultsController.renderSearchForm());

    JourneyStage controlCode = defineStage("controlCode", "Summary",
        controllers.controlcode.routes.ControlCodeController.renderSearchForm());

    JourneyStage controlCodeNotApplicable = defineStage("controlCodeNotApplicable", "Rating is not applicable",
        controllers.controlcode.routes.NotApplicableController.renderSearchForm(Boolean.FALSE.toString()));

    JourneyStage controlCodeNotApplicableExtended = defineStage("controlCodeNotApplicableExtended", "Rating is not applicable",
        controllers.controlcode.routes.NotApplicableController.renderSearchForm(Boolean.TRUE.toString()));

    JourneyStage additionalSpecifications = defineStage("additionalSpecifications", "Additional specifications",
        controllers.controlcode.routes.AdditionalSpecificationsController.renderSearchForm());

    JourneyStage decontrols = defineStage("decontrols", "Decontrols",
        controllers.controlcode.routes.DecontrolsController.renderSearchForm());

    JourneyStage technicalNotes = defineStage("technicalNotes", "Technical notes",
        controllers.controlcode.routes.TechnicalNotesController.renderSearchForm());

    JourneyStage ogelResults = defineStage("ogelResults", "Licences applicable to your answers",
        controllers.ogel.routes.OgelResultsController.renderForm());

    JourneyStage ogelConditions = defineStage("ogelConditions", "Conditions apply to your licence",
        controllers.ogel.routes.OgelConditionsController.renderForm());

    JourneyStage virtualEU = defineStage("virtualEU", "You do not need a licence",
        routes.StaticContentController.renderVirtualEU());

    JourneyStage ogelSummary = defineStage("ogelSummary", "Licence summary",
        controllers.ogel.routes.OgelSummaryController.renderForm());

    atStage(physicalGoodsSearch)
        .onEvent(Events.SEARCH_PHYSICAL_GOODS)
        .then(moveTo(physicalGoodsSearchResults));

    atStage(physicalGoodsSearchResults)
        .onEvent(Events.CONTROL_CODE_SELECTED)
        .then(moveTo(controlCode));

    atStage(physicalGoodsSearchResults)
        .onEvent(Events.NONE_MATCHED)
        .then(moveTo(notApplicable));

    atStage(controlCode)
        .onEvent(Events.CONTROL_CODE_FLOW_NEXT)
        .branch()
        .when(ControlCodeFlowStage.NOT_APPLICABLE, moveTo(controlCodeNotApplicable))
        .when(ControlCodeFlowStage.ADDITIONAL_SPECIFICATIONS, moveTo(additionalSpecifications))
        .when(ControlCodeFlowStage.DECONTROLS, moveTo(decontrols))
        .when(ControlCodeFlowStage.TECHNICAL_NOTES, moveTo(technicalNotes))
        .when(ControlCodeFlowStage.CONFIRMED, moveTo(destinationCountries))
        .when(ControlCodeFlowStage.BACK_TO_SEARCH, moveTo(physicalGoodsSearch))
        .when(ControlCodeFlowStage.BACK_TO_SEARCH_RESULTS, moveTo(physicalGoodsSearchResults));

    atStage(additionalSpecifications)
        .onEvent(Events.CONTROL_CODE_FLOW_NEXT)
        .branch()
        .when(ControlCodeFlowStage.NOT_APPLICABLE, moveTo(controlCodeNotApplicableExtended))
        .when(ControlCodeFlowStage.DECONTROLS, moveTo(decontrols))
        .when(ControlCodeFlowStage.TECHNICAL_NOTES, moveTo(technicalNotes))
        .when(ControlCodeFlowStage.CONFIRMED, moveTo(destinationCountries));

    atStage(decontrols)
        .onEvent(Events.CONTROL_CODE_FLOW_NEXT)
        .branch()
        .when(ControlCodeFlowStage.NOT_APPLICABLE, moveTo(controlCodeNotApplicableExtended))
        .when(ControlCodeFlowStage.TECHNICAL_NOTES, moveTo(technicalNotes))
        .when(ControlCodeFlowStage.CONFIRMED, moveTo(destinationCountries));

    atStage(technicalNotes)
        .onEvent(Events.CONTROL_CODE_FLOW_NEXT)
        .branch()
        .when(ControlCodeFlowStage.NOT_APPLICABLE, moveTo(controlCodeNotApplicableExtended))
        .when(ControlCodeFlowStage.CONFIRMED, moveTo(destinationCountries));

    atStage(controlCodeNotApplicable)
        .onEvent(Events.CONTROL_CODE_FLOW_NEXT)
        .branch()
        .when(ControlCodeFlowStage.BACK_TO_SEARCH, moveTo(physicalGoodsSearch))
        .when(ControlCodeFlowStage.BACK_TO_SEARCH_RESULTS, moveTo(physicalGoodsSearchResults));

    atStage(controlCodeNotApplicableExtended)
        .onEvent(Events.CONTROL_CODE_FLOW_NEXT)
        .branch()
        .when(ControlCodeFlowStage.BACK_TO_SEARCH, moveTo(physicalGoodsSearch))
        .when(ControlCodeFlowStage.BACK_TO_SEARCH_RESULTS, moveTo(physicalGoodsSearchResults));

    atStage(destinationCountries)
        .onEvent(Events.DESTINATION_COUNTRIES_SELECTED)
        .then(moveTo(ogelQuestions));

    atStage(ogelQuestions)
        .onEvent(Events.VIRTUAL_EU_OGEL_STAGE)
        .branch()
        .when(VirtualEUOgelStage.NO_VIRTUAL_EU, moveTo(ogelResults))
        .when(VirtualEUOgelStage.VIRTUAL_EU_WITH_CONDITIONS, moveTo(ogelConditions))
        .when(VirtualEUOgelStage.VIRTUAL_EU_WITHOUT_CONDITIONS, moveTo(virtualEU));

    atStage(ogelResults)
        .onEvent(Events.OGEL_SELECTED)
        .then(moveTo(ogelSummary));

    atStage(ogelResults)
        .onEvent(Events.OGEL_CONDITIONS_APPLY)
        .then(moveTo(ogelConditions));

    atStage(ogelConditions)
        .onEvent(Events.OGEL_DO_CONDITIONS_APPLY)
        .then(moveTo(ogelSummary));

    atStage(ogelConditions)
        .onEvent(Events.VIRTUAL_EU_OGEL_STAGE)
        .branch()
        .when(VirtualEUOgelStage.VIRTUAL_EU_CONDITIONS_DO_APPLY, moveTo(virtualEU))
        .when(VirtualEUOgelStage.VIRTUAL_EU_CONDITIONS_DO_NOT_APPLY, moveTo(ogelResults));

    atStage(ogelSummary)
        .onEvent(Events.OGEL_CHOOSE_AGAIN)
        .then(moveTo(ogelResults));
  }

  private void softwareStages() {

    JourneyStage softwareExemptionsNLR = defineStage("softwareExemptionsNLR", "Software exemptions apply",
        controllers.routes.StaticContentController.renderSoftwareExemptionsNLR());

    JourneyStage dualUseSoftwareCategories = defineStage("dualUseSoftTechCategories", "What is your software for?",
        controllers.softtech.routes.DualUseSoftTechCategoriesController.renderForm());

    JourneyStage softwareRelationshipContactECO = defineStage("softwareRelationshipContactECO", "Contact ECO",
        routes.StaticContentController.renderSoftwareRelationshipContactECO());

    atStage(softwareExemptions)
        .onEvent(Events.SOFTWARE_EXEMPTIONS_FLOW)
        .branch()
        .when(SoftwareExemptionsFlow.EXEMPTIONS_APPLY, moveTo(softwareExemptionsNLR))
        .when(SoftwareExemptionsFlow.DUAL_USE, moveTo(dualUseSoftwareCategories))
        .when(SoftwareExemptionsFlow.MILITARY_ZERO_CONTROLS, moveTo(relatedToEquipmentOrMaterials))
        .when(SoftwareExemptionsFlow.MILITARY_ONE_CONTROL, moveTo(controlCodeForSoftwareControls))
        .when(SoftwareExemptionsFlow.MILITARY_GREATER_THAN_ONE_CONTROL, moveTo(softwareCategoryControls));

    atStage(dualUseSoftwareCategories)
        .onEvent(Events.DUAL_USE_SOFTWARE_CATEGORY_SELECTED)
        .branch()
        .when(ApplicableSoftTechControls.ZERO, moveTo(relatedToEquipmentOrMaterials))
        .when(ApplicableSoftTechControls.ONE, moveTo(controlCodeForSoftwareControls))
        .when(ApplicableSoftTechControls.GREATER_THAN_ONE, moveTo(softwareCategoryControls));

    atStage(dualUseSoftwareCategories)
        .onEvent(Events.NONE_MATCHED)
        .then(moveTo(relatedToEquipmentOrMaterials));

    atStage(relatedToEquipmentOrMaterials)
        .onEvent(StandardEvents.YES).then(moveTo(physicalGoodsSearchRelatedToSoftware));

    bindCatchallSoftwareControls(relatedToEquipmentOrMaterials);

    atStage(softwareCategoryControls)
        .onEvent(Events.CONTROL_CODE_SELECTED)
        .then(moveTo(controlCodeForSoftwareControls));

    atStage(softwareCategoryControls)
        .onEvent(Events.NONE_MATCHED)
        .then(moveTo(relatedToEquipmentOrMaterials));

    softwareCategoryControls();

    physicalGoodsSearchRelatedToSoftware();

    softwareControlsRelatedToAPhysicalGood();

    softwareCatchallControls();

    atStage(softwareTechnologyRelationship)
        .onEvent(StandardEvents.YES).then(moveTo(softwareRelationshipContactECO));
    atStage(softwareTechnologyRelationship)
        .onEvent(StandardEvents.NO).then(moveTo(softwareRelationshipNLR));
  }

  /**
   * Physical good search to then find related software controls with
   */
  private void physicalGoodsSearchRelatedToSoftware() {

    JourneyStage physicalGoodsSearchResultsRelatedToSoftware = defineStage("physicalGoodsSearchResultsRelatedToSoftware", "Possible matches",
        controllers.search.routes.PhysicalGoodsSearchResultsController.renderSearchRelatedToForm(GoodsType.SOFTWARE.toUrlString()));

    JourneyStage controlCodeforRelatedToSoftware = defineStage("controlCodeRelatedToSoftware", "Summary",
        controllers.controlcode.routes.ControlCodeController.renderSearchRelatedToForm(GoodsType.SOFTWARE.toUrlString()));

    JourneyStage controlCodeNotApplicableRelatedToSoftware = defineStage("controlCodeNotApplicableRelatedToSoftware", "Rating is not applicable",
        controllers.controlcode.routes.NotApplicableController.renderSearchRelatedToForm(GoodsType.SOFTWARE.toUrlString(), Boolean.FALSE.toString()));

    JourneyStage controlCodeNotApplicableExtendedRelatedToSoftware = defineStage("controlCodeNotApplicableExtendedRelatedToSoftware", "Rating is not applicable",
        controllers.controlcode.routes.NotApplicableController.renderSearchRelatedToForm(GoodsType.SOFTWARE.toUrlString(), Boolean.TRUE.toString()));

    JourneyStage additionalSpecificationsRelatedToSoftware = defineStage("additionalSpecificationsRelatedToSoftware", "Additional specifications",
        controllers.controlcode.routes.AdditionalSpecificationsController.renderSearchRelatedToForm(GoodsType.SOFTWARE.toUrlString()));

    JourneyStage decontrolsRelatedToSoftware = defineStage("decontrolsRelatedToSoftware", "Decontrols",
        controllers.controlcode.routes.DecontrolsController.renderSearchRelatedToForm(GoodsType.SOFTWARE.toUrlString()));

    JourneyStage technicalNotesRelatedToSoftware = defineStage("technicalNotesRelatedToSoftware", "Technical notes",
        controllers.controlcode.routes.TechnicalNotesController.renderSearchRelatedToSoftwareForm());

    atStage(physicalGoodsSearchRelatedToSoftware)
        .onEvent(Events.SEARCH_PHYSICAL_GOODS)
        .then(moveTo(physicalGoodsSearchResultsRelatedToSoftware));

    atStage(physicalGoodsSearchResultsRelatedToSoftware)
        .onEvent(Events.CONTROL_CODE_SELECTED)
        .then(moveTo(controlCodeforRelatedToSoftware));

    bindCatchallSoftwareControls(physicalGoodsSearchResultsRelatedToSoftware); // None matched

    atStage(controlCodeforRelatedToSoftware)
        .onEvent(Events.CONTROL_CODE_FLOW_NEXT)
        .branch()
        .when(ControlCodeFlowStage.NOT_APPLICABLE, moveTo(controlCodeNotApplicableRelatedToSoftware))
        .when(ControlCodeFlowStage.ADDITIONAL_SPECIFICATIONS, moveTo(additionalSpecificationsRelatedToSoftware))
        .when(ControlCodeFlowStage.DECONTROLS, moveTo(decontrolsRelatedToSoftware))
        .when(ControlCodeFlowStage.TECHNICAL_NOTES, moveTo(technicalNotesRelatedToSoftware))
        .when(ControlCodeFlowStage.BACK_TO_SEARCH, moveTo(physicalGoodsSearchRelatedToSoftware))
        .when(ControlCodeFlowStage.BACK_TO_SEARCH_RESULTS, moveTo(physicalGoodsSearchResultsRelatedToSoftware));

    bindControlsRelatedToPhysicalGoods(controlCodeforRelatedToSoftware);

    atStage(additionalSpecificationsRelatedToSoftware)
        .onEvent(Events.CONTROL_CODE_FLOW_NEXT)
        .branch()
        .when(ControlCodeFlowStage.NOT_APPLICABLE, moveTo(controlCodeNotApplicableExtendedRelatedToSoftware))
        .when(ControlCodeFlowStage.DECONTROLS, moveTo(decontrolsRelatedToSoftware))
        .when(ControlCodeFlowStage.TECHNICAL_NOTES, moveTo(technicalNotesRelatedToSoftware));

    bindControlsRelatedToPhysicalGoods(additionalSpecificationsRelatedToSoftware);

    atStage(decontrolsRelatedToSoftware)
        .onEvent(Events.CONTROL_CODE_FLOW_NEXT)
        .branch()
        .when(ControlCodeFlowStage.NOT_APPLICABLE, moveTo(controlCodeNotApplicableExtendedRelatedToSoftware))
        .when(ControlCodeFlowStage.TECHNICAL_NOTES, moveTo(technicalNotesRelatedToSoftware));

    bindControlsRelatedToPhysicalGoods(decontrolsRelatedToSoftware);

    atStage(technicalNotesRelatedToSoftware)
        .onEvent(Events.CONTROL_CODE_FLOW_NEXT)
        .branch()
        .when(ControlCodeFlowStage.NOT_APPLICABLE, moveTo(controlCodeNotApplicableExtendedRelatedToSoftware));

    bindControlsRelatedToPhysicalGoods(technicalNotesRelatedToSoftware);

    atStage(controlCodeNotApplicableRelatedToSoftware)
        .onEvent(Events.CONTROL_CODE_FLOW_NEXT)
        .branch()
        .when(ControlCodeFlowStage.BACK_TO_SEARCH, moveTo(physicalGoodsSearchRelatedToSoftware))
        .when(ControlCodeFlowStage.BACK_TO_SEARCH_RESULTS, moveTo(physicalGoodsSearchResultsRelatedToSoftware));

    atStage(controlCodeNotApplicableExtendedRelatedToSoftware)
        .onEvent(Events.CONTROL_CODE_FLOW_NEXT)
        .branch()
        .when(ControlCodeFlowStage.BACK_TO_SEARCH, moveTo(physicalGoodsSearchRelatedToSoftware))
        .when(ControlCodeFlowStage.BACK_TO_SEARCH_RESULTS, moveTo(physicalGoodsSearchResultsRelatedToSoftware));
  }

  /**
   * Software controls per software category
   */
  private void softwareCategoryControls() {

    JourneyStage controlCodeNotApplicableSoftwareControls = defineStage("controlCodeNotApplicableSoftwareControls", "Rating is not applicable",
        controllers.controlcode.routes.NotApplicableController.renderSoftwareControlsForm(Boolean.FALSE.toString()));

    JourneyStage controlCodeNotApplicableExtendedSoftwareControls = defineStage("controlCodeNotApplicableExtendedSoftwareControls", "Rating is not applicable",
        controllers.controlcode.routes.NotApplicableController.renderSoftwareControlsForm(Boolean.TRUE.toString()));

    JourneyStage additionalSpecificationsSoftwareControls = defineStage("additionalSpecificationsSoftwareControls", "Additional specifications",
        controllers.controlcode.routes.AdditionalSpecificationsController.renderSoftwareControlsForm());

    JourneyStage decontrolsSoftwareControls = defineStage("decontrolsSoftwareControls", "Decontrols",
        controllers.controlcode.routes.DecontrolsController.renderSoftwareControlsForm());

    JourneyStage technicalNotesSoftwareControls = defineStage("technicalNotesSoftwareControls", "Technical notes",
        controllers.controlcode.routes.TechnicalNotesController.renderSoftwareControlsForm());

    atStage(controlCodeForSoftwareControls)
        .onEvent(Events.CONTROL_CODE_FLOW_NEXT)
        .branch()
        .when(ControlCodeFlowStage.ADDITIONAL_SPECIFICATIONS, moveTo(additionalSpecificationsSoftwareControls))
        .when(ControlCodeFlowStage.DECONTROLS, moveTo(decontrolsSoftwareControls))
        .when(ControlCodeFlowStage.TECHNICAL_NOTES, moveTo(technicalNotesSoftwareControls))
        .when(ControlCodeFlowStage.CONFIRMED, moveTo(destinationCountries));

    atStage(controlCodeForSoftwareControls)
        .onEvent(Events.CONTROL_CODE_SOFTWARE_CONTROLS_NOT_APPLICABLE)
        .branch()
        .when(ApplicableSoftTechControls.ONE, moveTo(controlCodeNotApplicableSoftwareControls))
        .when(ApplicableSoftTechControls.GREATER_THAN_ONE, moveTo(controlCodeNotApplicableSoftwareControls));

    atStage(additionalSpecificationsSoftwareControls)
        .onEvent(Events.CONTROL_CODE_FLOW_NEXT)
        .branch()
        .when(ControlCodeFlowStage.DECONTROLS, moveTo(decontrolsSoftwareControls))
        .when(ControlCodeFlowStage.TECHNICAL_NOTES, moveTo(technicalNotesSoftwareControls))
        .when(ControlCodeFlowStage.CONFIRMED, moveTo(destinationCountries));

    atStage(additionalSpecificationsSoftwareControls)
        .onEvent(Events.CONTROL_CODE_SOFTWARE_CONTROLS_NOT_APPLICABLE)
        .branch()
        .when(ApplicableSoftTechControls.ONE, moveTo(controlCodeNotApplicableExtendedSoftwareControls))
        .when(ApplicableSoftTechControls.GREATER_THAN_ONE, moveTo(controlCodeNotApplicableExtendedSoftwareControls));

    atStage(decontrolsSoftwareControls)
        .onEvent(Events.CONTROL_CODE_FLOW_NEXT)
        .branch()
        .when(ControlCodeFlowStage.TECHNICAL_NOTES, moveTo(technicalNotesSoftwareControls))
        .when(ControlCodeFlowStage.CONFIRMED, moveTo(destinationCountries));

    atStage(decontrolsSoftwareControls)
        .onEvent(Events.CONTROL_CODE_SOFTWARE_CONTROLS_NOT_APPLICABLE)
        .branch()
        .when(ApplicableSoftTechControls.ONE, moveTo(controlCodeNotApplicableExtendedSoftwareControls))
        .when(ApplicableSoftTechControls.GREATER_THAN_ONE, moveTo(controlCodeNotApplicableExtendedSoftwareControls));

    atStage(technicalNotesSoftwareControls)
        .onEvent(Events.CONTROL_CODE_FLOW_NEXT)
        .branch()
        .when(ControlCodeFlowStage.CONFIRMED, moveTo(destinationCountries));

    atStage(technicalNotesSoftwareControls)
        .onEvent(Events.CONTROL_CODE_SOFTWARE_CONTROLS_NOT_APPLICABLE)
        .branch()
        .when(ApplicableSoftTechControls.ONE, moveTo(controlCodeNotApplicableExtendedSoftwareControls))
        .when(ApplicableSoftTechControls.GREATER_THAN_ONE, moveTo(controlCodeNotApplicableExtendedSoftwareControls));

    atStage(controlCodeNotApplicableSoftwareControls)
        .onEvent(Events.CONTROL_CODE_SOFTWARE_CONTROLS_NOT_APPLICABLE_FLOW)
        .branch()
        .when(SoftwareControlsNotApplicableFlow.RETURN_TO_SOFTWARE_CONTROLS, moveTo(softwareCategoryControls))
        .when(SoftwareControlsNotApplicableFlow.CONTINUE_NO_CONTROLS, moveTo(relatedToEquipmentOrMaterials));

    atStage(controlCodeNotApplicableExtendedSoftwareControls)
        .onEvent(Events.CONTROL_CODE_SOFTWARE_CONTROLS_NOT_APPLICABLE_FLOW)
        .branch()
        .when(SoftwareControlsNotApplicableFlow.RETURN_TO_SOFTWARE_CONTROLS, moveTo(softwareCategoryControls))
        .when(SoftwareControlsNotApplicableFlow.CONTINUE_NO_CONTROLS, moveTo(relatedToEquipmentOrMaterials));
  }

  /**
   * Software controls for a selected physical good
   */
  private void softwareControlsRelatedToAPhysicalGood() {
    JourneyStage controlCodeNotApplicableRelatedSoftwareControls = defineStage("controlCodeNotApplicableRelatedSoftwareControls", "Rating is not applicable",
        controllers.controlcode.routes.NotApplicableController.renderRelatedSoftwareControlsForm(Boolean.FALSE.toString()));

    JourneyStage controlCodeNotApplicableExtendedRelatedSoftwareControls = defineStage("controlCodeNotApplicableExtendedRelatedSoftwareControls", "Rating is not applicable",
        controllers.controlcode.routes.NotApplicableController.renderRelatedSoftwareControlsForm(Boolean.TRUE.toString()));

    JourneyStage additionalSpecificationsRelatedSoftwareControls = defineStage("additionalSpecificationsRelatedSoftwareControls", "Additional specifications",
        controllers.controlcode.routes.AdditionalSpecificationsController.renderRelatedSoftwareControlsForm());

    JourneyStage decontrolsRelatedSoftwareControls = defineStage("decontrolsRelatedSoftwareControls", "Decontrols",
        controllers.controlcode.routes.DecontrolsController.renderRelatedSoftwareControlsForm());

    JourneyStage technicalNotesRelatedSoftwareControls = defineStage("technicalNotesRelatedSoftwareControls", "Technical notes",
        controllers.controlcode.routes.TechnicalNotesController.renderRelatedSoftwareControlsForm());

    atStage(softwareRelatedToPhysicalGoodControls)
        .onEvent(Events.CONTROL_CODE_SELECTED)
        .then(moveTo(controlCodeForRelatedSoftwareControls));

    bindCatchallSoftwareControls(softwareRelatedToPhysicalGoodControls);

    atStage(controlCodeForRelatedSoftwareControls)
        .onEvent(Events.CONTROL_CODE_FLOW_NEXT)
        .branch()
        .when(ControlCodeFlowStage.ADDITIONAL_SPECIFICATIONS, moveTo(additionalSpecificationsRelatedSoftwareControls))
        .when(ControlCodeFlowStage.DECONTROLS, moveTo(decontrolsRelatedSoftwareControls))
        .when(ControlCodeFlowStage.TECHNICAL_NOTES, moveTo(technicalNotesRelatedSoftwareControls))
        .when(ControlCodeFlowStage.CONFIRMED, moveTo(destinationCountries));

    atStage(controlCodeForRelatedSoftwareControls)
        .onEvent(Events.CONTROL_CODE_SOFTWARE_CONTROLS_NOT_APPLICABLE)
        .branch()
        .when(ApplicableSoftTechControls.ONE, moveTo(controlCodeNotApplicableRelatedSoftwareControls))
        .when(ApplicableSoftTechControls.GREATER_THAN_ONE, moveTo(controlCodeNotApplicableRelatedSoftwareControls));

    atStage(additionalSpecificationsRelatedSoftwareControls)
        .onEvent(Events.CONTROL_CODE_FLOW_NEXT)
        .branch()
        .when(ControlCodeFlowStage.DECONTROLS, moveTo(decontrolsRelatedSoftwareControls))
        .when(ControlCodeFlowStage.TECHNICAL_NOTES, moveTo(technicalNotesRelatedSoftwareControls))
        .when(ControlCodeFlowStage.CONFIRMED, moveTo(destinationCountries));

    atStage(additionalSpecificationsRelatedSoftwareControls)
        .onEvent(Events.CONTROL_CODE_SOFTWARE_CONTROLS_NOT_APPLICABLE)
        .branch()
        .when(ApplicableSoftTechControls.ONE, moveTo(controlCodeNotApplicableExtendedRelatedSoftwareControls))
        .when(ApplicableSoftTechControls.GREATER_THAN_ONE, moveTo(controlCodeNotApplicableExtendedRelatedSoftwareControls));

    atStage(decontrolsRelatedSoftwareControls)
        .onEvent(Events.CONTROL_CODE_FLOW_NEXT)
        .branch()
        .when(ControlCodeFlowStage.TECHNICAL_NOTES, moveTo(technicalNotesRelatedSoftwareControls))
        .when(ControlCodeFlowStage.CONFIRMED, moveTo(destinationCountries));

    atStage(decontrolsRelatedSoftwareControls)
        .onEvent(Events.CONTROL_CODE_SOFTWARE_CONTROLS_NOT_APPLICABLE)
        .branch()
        .when(ApplicableSoftTechControls.ONE, moveTo(controlCodeNotApplicableExtendedRelatedSoftwareControls))
        .when(ApplicableSoftTechControls.GREATER_THAN_ONE, moveTo(controlCodeNotApplicableExtendedRelatedSoftwareControls));

    atStage(technicalNotesRelatedSoftwareControls)
        .onEvent(Events.CONTROL_CODE_FLOW_NEXT)
        .branch()
        .when(ControlCodeFlowStage.CONFIRMED, moveTo(destinationCountries));

    atStage(technicalNotesRelatedSoftwareControls)
        .onEvent(Events.CONTROL_CODE_SOFTWARE_CONTROLS_NOT_APPLICABLE)
        .branch()
        .when(ApplicableSoftTechControls.ONE, moveTo(controlCodeNotApplicableExtendedRelatedSoftwareControls))
        .when(ApplicableSoftTechControls.GREATER_THAN_ONE, moveTo(controlCodeNotApplicableExtendedRelatedSoftwareControls));

    atStage(controlCodeNotApplicableRelatedSoftwareControls)
        .onEvent(Events.CONTROL_CODE_SOFTWARE_CONTROLS_NOT_APPLICABLE_FLOW)
        .branch()
        .when(SoftwareControlsNotApplicableFlow.RETURN_TO_SOFTWARE_CONTROLS, moveTo(softwareRelatedToPhysicalGoodControls));

    bindCatchallSoftwareControls(controlCodeNotApplicableRelatedSoftwareControls);

    atStage(controlCodeNotApplicableExtendedRelatedSoftwareControls)
        .onEvent(Events.CONTROL_CODE_SOFTWARE_CONTROLS_NOT_APPLICABLE_FLOW)
        .branch()
        .when(SoftwareControlsNotApplicableFlow.RETURN_TO_SOFTWARE_CONTROLS, moveTo(softwareRelatedToPhysicalGoodControls));

    bindCatchallSoftwareControls(controlCodeNotApplicableExtendedRelatedSoftwareControls);

    // Expecting CatchallSoftTechControlsFlow.CATCHALL_ONE or CatchallSoftTechControlsFlow.CATCHALL_GREATER_THAN_ONE
    bindCatchallSoftwareControls(noSoftwareControlsExist);

  }

  private void softwareCatchallControls() {

    JourneyStage controlCodeNotApplicableSoftwareCatchallControls = defineStage("controlCodeNotApplicableSoftwareCatchallControls", "Rating is not applicable",
        controllers.controlcode.routes.NotApplicableController.renderSoftwareCatchallControlsForm(Boolean.FALSE.toString()));

    JourneyStage controlCodeNotApplicableExtendedSoftwareCatchallControls = defineStage("controlCodeNotApplicableExtendedSoftwareCatchallControls", "Rating is not applicable",
        controllers.controlcode.routes.NotApplicableController.renderSoftwareCatchallControlsForm(Boolean.TRUE.toString()));

    JourneyStage additionalSpecificationSoftwareCatchallControls = defineStage("additionalSpecificationSoftwareCatchallControls", "Additional specifications",
        controllers.controlcode.routes.AdditionalSpecificationsController.renderSoftwareCatchallControlsForm());

    JourneyStage decontrolsSoftwareCatchallControls = defineStage("decontrolsSoftwareCatchallControls", "Decontrols",
        controllers.controlcode.routes.DecontrolsController.renderSoftwareCatchallControlsForm());

    JourneyStage technicalNotesSoftwareCatchallControls = defineStage("technicalNotesSoftwareCatchallControls", "Technical notes",
        controllers.controlcode.routes.TechnicalNotesController.renderSoftwareCatchallControlsForm());

    atStage(softwareCatchallControls)
        .onEvent(Events.CONTROL_CODE_SELECTED)
        .then(moveTo(controlCodeSoftwareCatchallControls));

    atStage(softwareCatchallControls)
        .onEvent(Events.CONTROL_CODE_SOFTWARE_CATCHALL_RELATIONSHIP)
        .branch()
        .when(Relationship.RELATIONSHIP_EXISTS, moveTo(softwareTechnologyRelationship))
        .when(Relationship.RELATIONSHIP_DOES_NOT_EXIST, moveTo(softwareRelationshipNLR));

    atStage(controlCodeSoftwareCatchallControls)
        .onEvent(Events.CONTROL_CODE_FLOW_NEXT)
        .branch()
        .when(ControlCodeFlowStage.ADDITIONAL_SPECIFICATIONS, moveTo(additionalSpecificationSoftwareCatchallControls))
        .when(ControlCodeFlowStage.DECONTROLS, moveTo(decontrolsSoftwareCatchallControls))
        .when(ControlCodeFlowStage.TECHNICAL_NOTES, moveTo(technicalNotesSoftwareCatchallControls))
        .when(ControlCodeFlowStage.CONFIRMED, moveTo(destinationCountries));

    atStage(controlCodeSoftwareCatchallControls)
        .onEvent(Events.CONTROL_CODE_SOFTWARE_CATCHALL_CONTROLS_NOT_APPLICABLE_FLOW)
        .branch()
        .when(SoftTechCatchallControlsNotApplicableFlow.RETURN_TO_SOFTWARE_CATCHALL_CONTROLS, moveTo(controlCodeNotApplicableSoftwareCatchallControls))
        .when(SoftTechCatchallControlsNotApplicableFlow.RELATIONSHIP_EXISTS, moveTo(softwareTechnologyRelationship))
        .when(SoftTechCatchallControlsNotApplicableFlow.RELATIONSHIP_NOT_EXISTS, moveTo(softwareRelationshipNLR));

    atStage(additionalSpecificationSoftwareCatchallControls)
        .onEvent(Events.CONTROL_CODE_FLOW_NEXT)
        .branch()
        .when(ControlCodeFlowStage.DECONTROLS, moveTo(decontrolsSoftwareCatchallControls))
        .when(ControlCodeFlowStage.TECHNICAL_NOTES, moveTo(technicalNotesSoftwareCatchallControls))
        .when(ControlCodeFlowStage.CONFIRMED, moveTo(destinationCountries));

    atStage(additionalSpecificationSoftwareCatchallControls)
        .onEvent(Events.CONTROL_CODE_SOFTWARE_CATCHALL_CONTROLS_NOT_APPLICABLE_FLOW)
        .branch()
        .when(SoftTechCatchallControlsNotApplicableFlow.RETURN_TO_SOFTWARE_CATCHALL_CONTROLS, moveTo(controlCodeNotApplicableExtendedSoftwareCatchallControls))
        .when(SoftTechCatchallControlsNotApplicableFlow.RELATIONSHIP_EXISTS, moveTo(softwareTechnologyRelationship))
        .when(SoftTechCatchallControlsNotApplicableFlow.RELATIONSHIP_NOT_EXISTS, moveTo(softwareRelationshipNLR));

    atStage(decontrolsSoftwareCatchallControls)
        .onEvent(Events.CONTROL_CODE_FLOW_NEXT)
        .branch()
        .when(ControlCodeFlowStage.TECHNICAL_NOTES, moveTo(technicalNotesSoftwareCatchallControls))
        .when(ControlCodeFlowStage.CONFIRMED, moveTo(destinationCountries));

    atStage(decontrolsSoftwareCatchallControls)
        .onEvent(Events.CONTROL_CODE_SOFTWARE_CATCHALL_CONTROLS_NOT_APPLICABLE_FLOW)
        .branch()
        .when(SoftTechCatchallControlsNotApplicableFlow.RETURN_TO_SOFTWARE_CATCHALL_CONTROLS, moveTo(controlCodeNotApplicableExtendedSoftwareCatchallControls))
        .when(SoftTechCatchallControlsNotApplicableFlow.RELATIONSHIP_EXISTS, moveTo(softwareTechnologyRelationship))
        .when(SoftTechCatchallControlsNotApplicableFlow.RELATIONSHIP_NOT_EXISTS, moveTo(softwareRelationshipNLR));

    atStage(technicalNotesSoftwareCatchallControls)
        .onEvent(Events.CONTROL_CODE_FLOW_NEXT)
        .branch()
        .when(ControlCodeFlowStage.CONFIRMED, moveTo(destinationCountries));

    atStage(technicalNotesSoftwareCatchallControls)
        .onEvent(Events.CONTROL_CODE_SOFTWARE_CATCHALL_CONTROLS_NOT_APPLICABLE_FLOW)
        .branch()
        .when(SoftTechCatchallControlsNotApplicableFlow.RETURN_TO_SOFTWARE_CATCHALL_CONTROLS, moveTo(controlCodeNotApplicableExtendedSoftwareCatchallControls))
        .when(SoftTechCatchallControlsNotApplicableFlow.RELATIONSHIP_EXISTS, moveTo(softwareTechnologyRelationship))
        .when(SoftTechCatchallControlsNotApplicableFlow.RELATIONSHIP_NOT_EXISTS, moveTo(softwareRelationshipNLR));

    atStage(controlCodeNotApplicableSoftwareCatchallControls)
        .onEvent(Events.CONTROL_CODE_SOFTWARE_CATCHALL_CONTROLS_NOT_APPLICABLE_FLOW)
        .branch()
        .when(SoftTechCatchallControlsNotApplicableFlow.RETURN_TO_SOFTWARE_CATCHALL_CONTROLS, moveTo(softwareCatchallControls));

    atStage(controlCodeNotApplicableExtendedSoftwareCatchallControls)
        .onEvent(Events.CONTROL_CODE_SOFTWARE_CATCHALL_CONTROLS_NOT_APPLICABLE_FLOW)
        .branch()
        .when(SoftTechCatchallControlsNotApplicableFlow.RETURN_TO_SOFTWARE_CATCHALL_CONTROLS, moveTo(softwareCatchallControls));
  }

  /**
   * Physical good related to software
   * @param journeyStage
   */
  private void bindControlsRelatedToPhysicalGoods(JourneyStage journeyStage) {
    atStage(journeyStage)
        .onEvent(Events.CONTROLS_RELATED_PHYSICAL_GOOD)
        .branch()
        .when(ControlsRelatedToPhysicalGoodsFlow.SOFTWARE_CONTROL_ONE, moveTo(controlCodeForRelatedSoftwareControls))
        .when(ControlsRelatedToPhysicalGoodsFlow.SOFTWARE_CONTROL_GREATER_THAN_ONE, moveTo(softwareRelatedToPhysicalGoodControls))
        .when(ControlsRelatedToPhysicalGoodsFlow.SOFTWARE_CONTROL_CATCHALL_ZERO, moveTo(softwareControlsNLR))
        .when(ControlsRelatedToPhysicalGoodsFlow.SOFTWARE_CONTROL_CATCHALL_CONTROL_GREATER_THAN_ZERO, moveTo(noSoftwareControlsExist));
  }
  /**
   * Catch all software controls
   * @param journeyStage
   */
  private void bindCatchallSoftwareControls(JourneyStage journeyStage) {
    atStage(journeyStage)
        .onEvent(Events.CATCHALL_SOFTWARE_CONTROLS_FLOW)
        .branch()
        .when(CatchallSoftTechControlsFlow.CATCHALL_ONE, moveTo(controlCodeSoftwareCatchallControls))
        .when(CatchallSoftTechControlsFlow.CATCHALL_GREATER_THAN_ONE, moveTo(softwareCatchallControls))
        .when(CatchallSoftTechControlsFlow.RELATIONSHIP_EXISTS, moveTo(softwareTechnologyRelationship))
        .when(CatchallSoftTechControlsFlow.RELATIONSHIP_DOES_NOT_EXIST, moveTo(softwareRelationshipNLR));
  }

}
