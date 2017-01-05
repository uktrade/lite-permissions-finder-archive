package journey;

import com.google.inject.Inject;
import components.common.journey.BackLink;
import components.common.journey.CommonStage;
import components.common.journey.DecisionStage;
import components.common.journey.JourneyDefinitionBuilder;
import components.common.journey.JourneyStage;
import components.common.journey.StandardEvents;
import controllers.categories.NonMilitaryController;
import controllers.routes;
import journey.deciders.CategoryControlsDecider;
import journey.deciders.ControlCodeDecider;
import journey.deciders.ExportCategoryDecider;
import journey.deciders.RelatedControlsDecider;
import models.ArtsCulturalGoodsType;
import models.ControlCodeFlowStage;
import models.ExportCategory;
import models.GoodsType;
import models.LifeType;
import models.RadioactiveStage;
import models.VirtualEUOgelStage;
import models.controlcode.BackType;
import models.controlcode.ControlCodeVariant;
import models.softtech.ApplicableSoftTechControls;
import models.softtech.CatchallSoftTechControlsFlow;
import models.softtech.Relationship;

public class ExportJourneyDefinitionBuilder extends JourneyDefinitionBuilder {

  private final JourneyStage exportCategory = defineStage("exportCategory", "What are you exporting?",
      controllers.categories.routes.ExportCategoryController.renderForm());
  private final JourneyStage goodsType = defineStage("goodsType", "Are you exporting goods, software or technical information?",
      routes.GoodsTypeController.renderForm());
  private final JourneyStage search = defineStage("search", "Describe your items",
      controllers.search.routes.SearchController.renderSearchForm());
  private final JourneyStage destinationCountries = defineStage("destinationCountries", "Countries and territories",
      routes.DestinationCountryController.renderForm());
  private final JourneyStage ogelQuestions = defineStage("ogelQuestions", "Refining your licence results",
      controllers.ogel.routes.OgelQuestionsController.renderForm());
  private final JourneyStage ogelNotApplicable = defineStage("ogelNotApplicable", "The licence is not applicable to your item",
      controllers.ogel.routes.OgelNotApplicableController.renderForm());
  private final JourneyStage notImplemented = defineStage("notImplemented", "This section is currently under development",
      routes.StaticContentController.renderNotImplemented());
  private final JourneyStage notApplicable = defineStage("notApplicable", "No licence available",
      routes.StaticContentController.renderNotApplicable());

  /** Software **/
  private final DecisionStage<ExportCategory> dualUseOrMilitarySoftwareDecision;
  private final DecisionStage<ApplicableSoftTechControls> applicableSoftwareControlsDecision;
  private final DecisionStage<ApplicableSoftTechControls> applicableRelatedControlsDecision;
  private final JourneyStage softwareExemptionsQ1 = defineStage("softwareExemptionsQ1", "Some types of software do not need a licence",
      controllers.softtech.routes.ExemptionsController.renderFormQ1());
  private final JourneyStage softwareExemptionsQ2 = defineStage("softwareExemptionsQ2", "Some types of software do not need a licence",
      controllers.softtech.routes.ExemptionsController.renderFormQ2());
  private final JourneyStage searchRTS = defineStage("searchRTS", "Describe your items",
      controllers.search.routes.SearchController.renderSearchRelatedToForm(GoodsType.SOFTWARE.urlString()));
  private final JourneyStage controlCodeSummarySC = defineStage("controlCodeSummarySC", "Summary",
      controllers.controlcode.routes.ControlCodeSummaryController.renderForm(ControlCodeVariant.CONTROLS.urlString(), GoodsType.SOFTWARE.urlString()));
  private final JourneyStage controlCodeSummarySCRTPG = defineStage("controlCodeSummarySCRTPG", "Summary",
      controllers.controlcode.routes.ControlCodeSummaryController.renderForm(ControlCodeVariant.CONTROLS_RELATED_TO_A_PHYSICAL_GOOD.urlString(), GoodsType.SOFTWARE.urlString()));
  private final JourneyStage controlCodeSoftwareCatchallControls = defineStage("controlCodeSoftwareCatchallControls", "Summary",
      controllers.controlcode.routes.ControlCodeSummaryController.renderForm(ControlCodeVariant.CATCHALL_CONTROLS.urlString(), GoodsType.SOFTWARE.urlString()));
  private final JourneyStage categoryControlsListSC = defineStage("categoryControlsListSC", "Showing controls related to software category",
      controllers.softtech.controls.routes.SoftTechControlsController.renderCategoryForm(GoodsType.SOFTWARE.urlString()));
  private final JourneyStage softwareRelatedToPhysicalGoodControls = defineStage("softwareRelatedToPhysicalGoodControls", "Showing controls related to your selected physical good",
      controllers.softtech.controls.routes.SoftTechControlsController.renderRelatedToPhysicalGoodForm(GoodsType.SOFTWARE.urlString()));
  private final JourneyStage softwareCatchallControls= defineStage("softwareCatchallControls", "Showing catchall controls related to your items category",
      controllers.softtech.controls.routes.SoftTechControlsController.renderCatchallControlsForm(GoodsType.SOFTWARE.urlString()));
  private JourneyStage softwareRelatedToEquipmentOrMaterials = defineStage("softwareRelatedToEquipmentOrMaterials", "Is your software any of the following?",
      controllers.softtech.routes.RelatedEquipmentController.renderForm(GoodsType.SOFTWARE.urlString()));
  private JourneyStage noSoftwareControlsExist = defineStage("noSoftTechControlsExist", "No software controls exist for item",
      controllers.softtech.controls.routes.NoSoftTechControlsExistController.renderForm(GoodsType.SOFTWARE.urlString()));
  private JourneyStage softwareControlsNLR = defineStage("softwareControlsNLR", "No software controls exist for the selected item",
      routes.StaticContentController.renderSoftwareControlsNLR());
  private JourneyStage softwareRelationshipNLR = defineStage("softwareRelationshipNLR", "No software controls exist for the selected item",
      routes.StaticContentController.renderSoftwareRelationshipNLR());
  private JourneyStage softwareTechnologyRelationship = defineStage("softwareTechnologyRelationship", "Is export covered by software technology relationship?",
      controllers.softtech.routes.RelationshipController.renderForm());

  private final ControlCodeDecider controlCodeDecider;
  private final ExportCategoryDecider exportCategoryDecider;
  private final CategoryControlsDecider categoryControlsDecider;
  private final RelatedControlsDecider relatedControlsDecider;

  @Inject
  public ExportJourneyDefinitionBuilder(ControlCodeDecider controlCodeDecider,
                                        ExportCategoryDecider exportCategoryDecider,
                                        CategoryControlsDecider categoryControlsDecider,
                                        RelatedControlsDecider relatedControlsDecider) {
    this.controlCodeDecider = controlCodeDecider;
    this.exportCategoryDecider = exportCategoryDecider;
    this.categoryControlsDecider = categoryControlsDecider;
    this.relatedControlsDecider = relatedControlsDecider;
    this.dualUseOrMilitarySoftwareDecision = defineDecisionStage("isDualUseSoftwareDecision", this.exportCategoryDecider);
    this.applicableSoftwareControlsDecision = defineDecisionStage("applicableSoftwareControlsDecision", this.categoryControlsDecider);
    this.applicableRelatedControlsDecision = defineDecisionStage("applicableRelatedControlsDecision", this.relatedControlsDecider);

  }

  @Override
  protected void journeys() {
    // *** Stages/transitions ***

    goodsCategoryStages();

    atStage(goodsType)
        .onEvent(Events.GOODS_TYPE_SELECTED)
        .branch()
        .when(GoodsType.PHYSICAL, moveTo(search))
        .when(GoodsType.SOFTWARE, moveTo(dualUseOrMilitarySoftwareDecision))
        .when(GoodsType.TECHNOLOGY, moveTo(notImplemented));

    physicalGoodsStages();

    softwareStages();

    // *** Journeys ***

    defineJourney(JourneyDefinitionNames.EXPORT, exportCategory, BackLink.to(routes.TradeTypeController.renderForm(),
        "Where are your items going?"));

    defineJourney(JourneyDefinitionNames.CHANGE_CONTROL_CODE, search,
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

    JourneyStage categoryNonMilitaryTakeYourself = defineStage(NonMilitaryController.TAKE_YOURSELF_KEY,
        NonMilitaryController.TAKE_YOURSELF_QUESTION, controllers.categories.routes.NonMilitaryController.renderTakeYourselfForm());
    JourneyStage categoryNonMilitaryPersonalEffects = defineStage(NonMilitaryController.PERSONAL_EFFECTS_KEY,
        NonMilitaryController.PERSONAL_EFFECTS_QUESTION, controllers.categories.routes.NonMilitaryController.renderPersonalEffectsForm());

    JourneyStage categoryNonMilitaryTakingStatic = defineStage("categoryNonMilitaryTaking", "", routes.StaticContentController.renderCategoryNonMilitaryTaking());
    JourneyStage categoryNonMilitarySendingStatic = defineStage("categoryNonMilitarySending", "", routes.StaticContentController.renderCategoryNonMilitarySending());
    JourneyStage categoryNonMilitaryNeedLicenceStatic = defineStage("categoryNonMilitaryNeedLicence", "", routes.StaticContentController.renderCategoryNonMilitaryNeedLicence());

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
        .when(ExportCategory.NON_MILITARY, moveTo(categoryNonMilitaryTakeYourself))
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
        .when(ArtsCulturalGoodsType.FIREARM_NON_HISTORIC, moveTo(categoryNonMilitaryTakeYourself));

    // Note use of EXPORT_CATEGORY_SELECTED for single value
    atStage(categoryArtsCulturalFirearmHistoric)
        .onEvent(Events.EXPORT_CATEGORY_SELECTED)
        .branch()
        .when(ExportCategory.NON_MILITARY, moveTo(categoryNonMilitaryTakeYourself));

    atStage(categoryChemicalsCosmetics)
        .onEvent(Events.SEARCH_PHYSICAL_GOODS)
        .then(moveTo(search));

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

    atStage(categoryNonMilitaryTakeYourself)
        .onEvent(Events.NON_MILITARY_FIREARMS_OPTION_SELECTED)
        .branch()
        .when(true, moveTo(categoryNonMilitaryTakingStatic))
        .when(false, moveTo(categoryNonMilitaryPersonalEffects));

    atStage(categoryNonMilitaryPersonalEffects)
        .onEvent(Events.NON_MILITARY_FIREARMS_OPTION_SELECTED)
        .branch()
        .when(true, moveTo(categoryNonMilitarySendingStatic))
        .when(false, moveTo(categoryNonMilitaryNeedLicenceStatic));

    atStage(categoryPlantsAnimals)
        .onEvent(Events.LIFE_TYPE_SELECTED)
        .branch()
        .when(LifeType.ENDANGERED, moveTo(categoryEndangeredAnimalStatic))
        .when(LifeType.NON_ENDANGERED, moveTo(categoryNonEndangeredAnimalStatic))
        .when(LifeType.PLANT, moveTo(categoryPlantStatic));

    atStage(categoryTortureRestraint)
        .onEvent(Events.SEARCH_PHYSICAL_GOODS)
        .then(moveTo(search));

    atStage(categoryRadioactive)
        .onEvent(Events.RADIOACTIVE_NEXT)
        .branch()
        .when(RadioactiveStage.CRS_SELECTED, moveTo(destinationCountries))
        .when(RadioactiveStage.CONTINUE, moveTo(search));

  }

  private void physicalGoodsStages() {

    JourneyStage searchResults = defineStage("searchResults", "Possible matches",
        controllers.search.routes.SearchResultsController.renderSearchForm());

    JourneyStage controlCodeSummary = defineStage("controlCodeSummary", "Summary",
        controllers.controlcode.routes.ControlCodeSummaryController.renderForm(ControlCodeVariant.SEARCH.urlString(), GoodsType.PHYSICAL.urlString()));

    JourneyStage controlCodeNotApplicable = defineStage("controlCodeNotApplicable", "Rating is not applicable",
        controllers.controlcode.routes.NotApplicableController.renderForm(ControlCodeVariant.SEARCH.urlString(), GoodsType.PHYSICAL.urlString(), Boolean.FALSE.toString()));

    JourneyStage controlCodeNotApplicableExtended = defineStage("controlCodeNotApplicableExtended", "Rating is not applicable",
        controllers.controlcode.routes.NotApplicableController.renderForm(ControlCodeVariant.SEARCH.urlString(), GoodsType.PHYSICAL.urlString(), Boolean.TRUE.toString()));

    JourneyStage additionalSpecifications = defineStage("additionalSpecifications", "Additional specifications",
        controllers.controlcode.routes.AdditionalSpecificationsController.renderForm(ControlCodeVariant.SEARCH.urlString(), GoodsType.PHYSICAL.urlString()));

    JourneyStage decontrols = defineStage("decontrols", "Decontrols",
        controllers.controlcode.routes.DecontrolsController.renderForm(ControlCodeVariant.SEARCH.urlString(), GoodsType.PHYSICAL.urlString()));

    JourneyStage technicalNotes = defineStage("technicalNotes", "Technical notes",
        controllers.controlcode.routes.TechnicalNotesController.renderForm(ControlCodeVariant.SEARCH.urlString(), GoodsType.PHYSICAL.urlString()));

    JourneyStage ogelResults = defineStage("ogelResults", "Licences applicable to your answers",
        controllers.ogel.routes.OgelResultsController.renderForm());

    JourneyStage ogelConditions = defineStage("ogelConditions", "Conditions apply to your licence",
        controllers.ogel.routes.OgelConditionsController.renderForm());

    JourneyStage virtualEU = defineStage("virtualEU", "You do not need a licence",
        routes.StaticContentController.renderVirtualEU());

    JourneyStage ogelSummary = defineStage("ogelSummary", "Licence summary",
        controllers.ogel.routes.OgelSummaryController.renderForm());

    atStage(search)
        .onEvent(Events.SEARCH_PHYSICAL_GOODS)
        .then(moveTo(searchResults));

    atStage(searchResults)
        .onEvent(Events.CONTROL_CODE_SELECTED)
        .then(moveTo(controlCodeSummary));

    atStage(searchResults)
        .onEvent(Events.NONE_MATCHED)
        .then(moveTo(notApplicable));

    DecisionStage<Boolean> decontrolsDecision = defineDecisionStage("hasDecontrols", controlCodeDecider,
        r -> r.contains(ControlCodeDecider.ControlCodeDataType.DECONTROLS));

    DecisionStage<Boolean> technicalNotesDecision = defineDecisionStage("hasTechNotes", controlCodeDecider,
        r -> r.contains(ControlCodeDecider.ControlCodeDataType.TECHNICAL_NOTES));

    DecisionStage<Boolean> additionalSpecsDecision = defineDecisionStage("hasAdditionalSpecs", controlCodeDecider,
        r -> r.contains(ControlCodeDecider.ControlCodeDataType.ADDITIONAL_SPECS));

    atStage(controlCodeSummary)
        .onEvent(StandardEvents.NEXT)
        .then(moveTo(decontrolsDecision));

    atStage(controlCodeSummary)
        .onEvent(Events.CONTROL_CODE_NOT_APPLICABLE)
        .then(moveTo(controlCodeNotApplicable));

    atStage(decontrols)
        .onEvent(StandardEvents.NEXT)
        .then(moveTo(technicalNotesDecision));

    atStage(decontrols)
        .onEvent(Events.CONTROL_CODE_NOT_APPLICABLE)
        .then(moveTo(controlCodeNotApplicableExtended));

    atStage(technicalNotes)
        .onEvent(StandardEvents.NEXT)
        .then(moveTo(additionalSpecsDecision));

    atStage(technicalNotes)
        .onEvent(Events.CONTROL_CODE_NOT_APPLICABLE)
        .then(moveTo(controlCodeNotApplicableExtended));

    atStage(additionalSpecifications)
        .onEvent(StandardEvents.NEXT)
        .then(moveTo(destinationCountries));

    atStage(additionalSpecifications)
        .onEvent(Events.CONTROL_CODE_NOT_APPLICABLE)
        .then(moveTo(controlCodeNotApplicableExtended));

    atDecisionStage(decontrolsDecision)
        .decide()
        .when(true, moveTo(decontrols))
        .when(false, moveTo(technicalNotesDecision));

    atDecisionStage(technicalNotesDecision)
        .decide()
        .when(true, moveTo(technicalNotes))
        .when(false, moveTo(additionalSpecsDecision));

    atDecisionStage(additionalSpecsDecision)
        .decide()
        .when(true, moveTo(additionalSpecifications))
        .when(false, moveTo(destinationCountries));

    bindControlCodeNotApplicableStage(controlCodeNotApplicable, search, searchResults);

    bindControlCodeNotApplicableStage(controlCodeNotApplicableExtended, search, searchResults);

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
        .onEvent(Events.OGEL_CONDITIONS_DO_APPLY)
        .then(moveTo(ogelSummary));

    atStage(ogelConditions)
        .onEvent(Events.OGEL_CONDITIONS_DO_NOT_APPLY)
        .then(moveTo(ogelNotApplicable));

    atStage(ogelConditions)
        .onEvent(Events.VIRTUAL_EU_OGEL_STAGE)
        .branch()
        .when(VirtualEUOgelStage.VIRTUAL_EU_CONDITIONS_DO_APPLY, moveTo(virtualEU))
        .when(VirtualEUOgelStage.VIRTUAL_EU_CONDITIONS_DO_NOT_APPLY, moveTo(ogelResults));

    atStage(ogelNotApplicable)
        .onEvent(Events.OGEL_CONTINUE_TO_NON_APPLICABLE_LICENCE)
        .then(moveTo(ogelSummary));

    atStage(ogelNotApplicable)
        .onEvent(Events.OGEL_CHOOSE_AGAIN)
        .then(moveTo(ogelResults));

    atStage(ogelSummary)
        .onEvent(Events.OGEL_CHOOSE_AGAIN)
        .then(moveTo(ogelResults));
  }

  private void softwareStages() {

    JourneyStage softwareExemptionsNLR1 = defineStage("softwareExemptionsNLR1", "Software exemptions apply",
        controllers.routes.StaticContentController.renderSoftwareExemptionsNLR1());

    JourneyStage softwareExemptionsNLR2 = defineStage("softwareExemptionsNLR2", "Software exemptions apply",
        controllers.routes.StaticContentController.renderSoftwareExemptionsNLR2());

    JourneyStage dualUseSoftwareCategories = defineStage("dualUseSoftTechCategories", "What is your software for?",
        controllers.softtech.routes.DualUseSoftTechCategoriesController.renderForm(GoodsType.SOFTWARE.urlString()));

    JourneyStage softwareRelationshipContactECO = defineStage("softwareRelationshipContactECO", "Contact ECO",
        routes.StaticContentController.renderSoftwareRelationshipContactECO());

    atDecisionStage(dualUseOrMilitarySoftwareDecision)
        .decide()
        .when(ExportCategory.MILITARY, moveTo(applicableSoftwareControlsDecision))
        .when(ExportCategory.DUAL_USE, moveTo(dualUseSoftwareCategories));
//        .when(ExportCategory.DUAL_USE, moveTo(softwareExemptionsQ1));

    /** Software Exemptions */
//    atStage(softwareExemptionsQ1)
//        .onEvent(Events.SOFTWARE_EXEMPTIONS_FLOW)
//        .branch()
//        .when(SoftwareExemptionsFlow.Q1_EXEMPTIONS_APPLY, moveTo(softwareExemptionsNLR1))
//        .when(SoftwareExemptionsFlow.Q1_EXEMPTIONS_DO_NOT_APPLY, moveTo(softwareExemptionsQ2));

//    atStage(softwareExemptionsQ2)
//        .onEvent(Events.SOFTWARE_EXEMPTIONS_FLOW)
//        .branch()
//        .when(SoftwareExemptionsFlow.Q1_AND_Q2_EXEMPTIONS_APPLY, moveTo(softwareExemptionsNLR2))
//        .when(SoftwareExemptionsFlow.DUAL_USE, moveTo(dualUseSoftwareCategories))
//        .when(SoftwareExemptionsFlow.MILITARY_ZERO_CONTROLS, moveTo(softwareRelatedToEquipmentOrMaterials))
//        .when(SoftwareExemptionsFlow.MILITARY_ONE_CONTROL, moveTo(controlCodeForSoftwareControls))
//        .when(SoftwareExemptionsFlow.MILITARY_GREATER_THAN_ONE_CONTROL, moveTo(softwareCategoryControls));
//
    atStage(dualUseSoftwareCategories)
        .onEvent(StandardEvents.NEXT)
        .then(moveTo(applicableSoftwareControlsDecision));

    atStage(dualUseSoftwareCategories)
        .onEvent(Events.NONE_MATCHED)
        .then(moveTo(softwareRelatedToEquipmentOrMaterials));

    atStage(softwareRelatedToEquipmentOrMaterials)
        .onEvent(StandardEvents.YES)
        .then(moveTo(searchRTS));

    atStage(softwareRelatedToEquipmentOrMaterials)
        .onEvent(StandardEvents.NO)
        .then(moveTo(notImplemented));

    atDecisionStage(applicableSoftwareControlsDecision)
        .decide()
        .when(ApplicableSoftTechControls.ZERO, moveTo(softwareRelatedToEquipmentOrMaterials))
        .when(ApplicableSoftTechControls.ONE, moveTo(controlCodeSummarySC))
        .when(ApplicableSoftTechControls.GREATER_THAN_ONE, moveTo(categoryControlsListSC));

    softwareCategoryControls();

    searchRelatedToSoftware();

    softwareControlsRelatedToAPhysicalGood();

    softwareCatchallControls();

    atStage(softwareTechnologyRelationship)
        .onEvent(StandardEvents.YES).then(moveTo(softwareRelationshipContactECO));
    atStage(softwareTechnologyRelationship)
        .onEvent(StandardEvents.NO).then(moveTo(softwareRelationshipNLR));
  }

  /**
   * Software controls per software category
   */
  private void softwareCategoryControls() {
    /** Software controls journey stages */
    JourneyStage controlCodeNotApplicableSC = defineStage("controlCodeNotApplicableSC", "Description not applicable",
        controllers.controlcode.routes.NotApplicableController.renderForm(ControlCodeVariant.CONTROLS.urlString(), GoodsType.SOFTWARE.urlString(), Boolean.FALSE.toString()));

    JourneyStage additionalSpecificationsSC = defineStage("additionalSpecificationsSC", "Additional specifications",
        controllers.controlcode.routes.AdditionalSpecificationsController.renderForm(ControlCodeVariant.CONTROLS.urlString(), GoodsType.SOFTWARE.urlString()));

    JourneyStage decontrolsSC = defineStage("decontrolsSC", "Decontrols",
        controllers.controlcode.routes.DecontrolsController.renderForm(ControlCodeVariant.CONTROLS.urlString(), GoodsType.SOFTWARE.urlString()));

    JourneyStage technicalNotesSC = defineStage("technicalNotesSC", "Technical notes",
        controllers.controlcode.routes.TechnicalNotesController.renderForm(ControlCodeVariant.CONTROLS.urlString(), GoodsType.SOFTWARE.urlString()));

    /** Software controls decision stages */
    DecisionStage<Boolean> additionalSpecificationsDecisionSC = defineDecisionStage("additionalSpecsDecisionSC", controlCodeDecider,
        r -> r.contains(ControlCodeDecider.ControlCodeDataType.ADDITIONAL_SPECS));

    DecisionStage<Boolean> decontrolsDecisionSC = defineDecisionStage("decontrolsDecisionSC", controlCodeDecider,
        r -> r.contains(ControlCodeDecider.ControlCodeDataType.DECONTROLS));

    DecisionStage<Boolean> technicalNotesDecisionSC = defineDecisionStage("technicalNotesDecisionSC", controlCodeDecider,
        r -> r.contains(ControlCodeDecider.ControlCodeDataType.TECHNICAL_NOTES));

    /** Software control journey stage transitions */
    atStage(categoryControlsListSC)
        .onEvent(Events.CONTROL_CODE_SELECTED)
        .then(moveTo(controlCodeSummarySC));

    atStage(categoryControlsListSC)
        .onEvent(Events.NONE_MATCHED)
        .then(moveTo(softwareRelatedToEquipmentOrMaterials));

    bindControlCodeStageTransitions(
        controlCodeSummarySC,
        controlCodeNotApplicableSC,
        additionalSpecificationsSC,
        decontrolsSC,
        technicalNotesSC,
        destinationCountries,
        additionalSpecificationsDecisionSC,
        decontrolsDecisionSC,
        technicalNotesDecisionSC
    );

    atStage(controlCodeNotApplicableSC)
        .onEvent(Events.BACK)
        .branch()
        .when(BackType.MATCHES, backTo(categoryControlsListSC));

    atStage(controlCodeNotApplicableSC)
        .onEvent(StandardEvents.NEXT)
        .then(moveTo(softwareRelatedToEquipmentOrMaterials));
  }

  /**
   * Search related to software journey
   */
  private void searchRelatedToSoftware() {
    /** Search related to software journey stages */
    JourneyStage searchResultsRTS = defineStage("searchResultsRTS", "Possible matches",
        controllers.search.routes.SearchResultsController.renderSearchRelatedToForm(GoodsType.SOFTWARE.urlString()));

    JourneyStage controlCodeSummaryRTS = defineStage("controlCodeSummaryRTS", "Summary",
        controllers.controlcode.routes.ControlCodeSummaryController.renderForm(ControlCodeVariant.SEARCH.urlString(), GoodsType.SOFTWARE.urlString()));

    JourneyStage controlCodeNotApplicableRTS = defineStage("controlCodeNotApplicableRTS", "Description not applicable",
        controllers.controlcode.routes.NotApplicableController.renderForm(ControlCodeVariant.SEARCH.urlString(), GoodsType.SOFTWARE.urlString(), Boolean.FALSE.toString()));

    JourneyStage additionalSpecificationsRTS = defineStage("additionalSpecificationsRTS", "Additional specifications",
        controllers.controlcode.routes.AdditionalSpecificationsController.renderForm(ControlCodeVariant.SEARCH.urlString(), GoodsType.SOFTWARE.urlString()));

    JourneyStage decontrolsRTS = defineStage("decontrolsRTS", "Decontrols",
        controllers.controlcode.routes.DecontrolsController.renderForm(ControlCodeVariant.SEARCH.urlString(), GoodsType.SOFTWARE.urlString()));

    JourneyStage technicalNotesRTS = defineStage("technicalNotesRTS", "Technical notes",
        controllers.controlcode.routes.TechnicalNotesController.renderForm(ControlCodeVariant.SEARCH.urlString(), GoodsType.SOFTWARE.urlString()));

    /** Search related to software decision stages */
    DecisionStage<Boolean> decontrolsDecisionRTS = defineDecisionStage("decontrolsDecisionRTS", controlCodeDecider,
        r -> r.contains(ControlCodeDecider.ControlCodeDataType.DECONTROLS));

    DecisionStage<Boolean> technicalNotesDecisionRTS = defineDecisionStage("technicalNotesDecisionRTS", controlCodeDecider,
        r -> r.contains(ControlCodeDecider.ControlCodeDataType.TECHNICAL_NOTES));

    DecisionStage<Boolean> additionalSpecsDecisionRTS = defineDecisionStage("additionalSpecsDecisionRTS", controlCodeDecider,
        r -> r.contains(ControlCodeDecider.ControlCodeDataType.ADDITIONAL_SPECS));

    /** Search related to software journey stage transitions */
    atStage(searchRTS)
        .onEvent(Events.SEARCH_PHYSICAL_GOODS)
        .then(moveTo(searchResultsRTS));

    atStage(searchResultsRTS)
        .onEvent(Events.CONTROL_CODE_SELECTED)
        .then(moveTo(controlCodeSummaryRTS));

    atStage(searchResultsRTS)
        .onEvent(Events.BACK)
        .branch()
        .when(BackType.SEARCH, backTo(searchRTS));

    atStage(searchResultsRTS)
        .onEvent(StandardEvents.NEXT)
        .then(moveTo(notImplemented)); //TODO catchall decision

    atStage(searchResultsRTS)
        .onEvent(Events.NONE_MATCHED)
        .then(moveTo(notImplemented)); //TODO catchall decision

    bindControlCodeStageTransitions(
        controlCodeSummaryRTS,
        controlCodeNotApplicableRTS,
        additionalSpecificationsRTS,
        decontrolsRTS,
        technicalNotesRTS,
        applicableRelatedControlsDecision,
        additionalSpecsDecisionRTS,
        decontrolsDecisionRTS,
        technicalNotesDecisionRTS
    );

    bindControlCodeNotApplicableStage(controlCodeNotApplicableRTS, searchRTS, searchResultsRTS);

    atDecisionStage(applicableRelatedControlsDecision)
        .decide()
        .when(ApplicableSoftTechControls.ZERO, moveTo(notImplemented)) // TODO check software controls related to military? else software related to dual use?
        .when(ApplicableSoftTechControls.ONE, moveTo(controlCodeSummarySCRTPG)) // TODO state of DAO needs being set
        .when(ApplicableSoftTechControls.GREATER_THAN_ONE, moveTo(softwareRelatedToPhysicalGoodControls));
  }


  /**
   * Software controls for a selected physical good
   */
  private void softwareControlsRelatedToAPhysicalGood() {
    /** Software controls related to physical goods  */
    JourneyStage controlCodeNotApplicableSCRTPG= defineStage("controlCodeNotApplicableSCRTPG", "Description not applicable",
        controllers.controlcode.routes.NotApplicableController.renderForm(ControlCodeVariant.CONTROLS_RELATED_TO_A_PHYSICAL_GOOD.urlString(), GoodsType.SOFTWARE.urlString(), Boolean.FALSE.toString()));

    JourneyStage additionalSpecificationsSCRTPG = defineStage("additionalSpecificationsSCRTPG", "Additional specifications",
        controllers.controlcode.routes.AdditionalSpecificationsController.renderForm(ControlCodeVariant.CONTROLS_RELATED_TO_A_PHYSICAL_GOOD.urlString(), GoodsType.SOFTWARE.urlString()));

    JourneyStage decontrolsRelatedSCRTPG = defineStage("decontrolsRelatedSCRTPG", "Decontrols",
        controllers.controlcode.routes.DecontrolsController.renderForm(ControlCodeVariant.CONTROLS_RELATED_TO_A_PHYSICAL_GOOD.urlString(), GoodsType.SOFTWARE.urlString()));

    JourneyStage technicalNotesRelatedSCRTPG = defineStage("technicalNotesRelatedSCRTPG", "Technical notes",
        controllers.controlcode.routes.TechnicalNotesController.renderForm(ControlCodeVariant.CONTROLS_RELATED_TO_A_PHYSICAL_GOOD.urlString(), GoodsType.SOFTWARE.urlString()));

    /** Software controls related to physical goods decision stages */
    DecisionStage<Boolean> additionalSpecificationsDecisionSCRTPG = defineDecisionStage("additionalSpecificationsDecisionSCRTPG", controlCodeDecider,
        r -> r.contains(ControlCodeDecider.ControlCodeDataType.ADDITIONAL_SPECS));

    DecisionStage<Boolean> decontrolsDecisionSCRTPG = defineDecisionStage("decontrolsDecisionSCRTPG", controlCodeDecider,
        r -> r.contains(ControlCodeDecider.ControlCodeDataType.DECONTROLS));

    DecisionStage<Boolean> technicalNotesDecisionSCRTPG = defineDecisionStage("technicalNotesDecisionSCRTPG", controlCodeDecider,
        r -> r.contains(ControlCodeDecider.ControlCodeDataType.TECHNICAL_NOTES));

    /** Software control journey stage transitions */
    atStage(softwareRelatedToPhysicalGoodControls)
        .onEvent(Events.CONTROL_CODE_SELECTED)
        .then(moveTo(controlCodeSummarySCRTPG));

    atStage(controlCodeSummarySCRTPG)
        .onEvent(Events.NONE_MATCHED)
        .then(moveTo(notImplemented)); //TODO catchall decision

    bindControlCodeStageTransitions(
        controlCodeSummarySCRTPG,
        controlCodeNotApplicableSCRTPG,
        additionalSpecificationsSCRTPG,
        decontrolsRelatedSCRTPG,
        technicalNotesRelatedSCRTPG,
        destinationCountries,
        additionalSpecificationsDecisionSCRTPG,
        decontrolsDecisionSCRTPG,
        technicalNotesDecisionSCRTPG
    );

    atStage(controlCodeNotApplicableSCRTPG)
        .onEvent(Events.BACK)
        .branch()
        .when(BackType.MATCHES, backTo(softwareRelatedToPhysicalGoodControls));

    atStage(controlCodeNotApplicableSCRTPG)
        .onEvent(StandardEvents.NEXT)
        .then(moveTo(notImplemented)); //TODO catchall decision

  }

  private void softwareCatchallControls() {

    JourneyStage controlCodeNotApplicableSoftwareCatchallControls = defineStage("controlCodeNotApplicableSoftwareCatchallControls", "Description not applicable",
        controllers.controlcode.routes.NotApplicableController.renderForm(ControlCodeVariant.CATCHALL_CONTROLS.urlString(), GoodsType.SOFTWARE.urlString(), Boolean.FALSE.toString()));

    JourneyStage additionalSpecificationSoftwareCatchallControls = defineStage("additionalSpecificationSoftwareCatchallControls", "Additional specifications",
        controllers.controlcode.routes.AdditionalSpecificationsController.renderForm(ControlCodeVariant.CATCHALL_CONTROLS.urlString(), GoodsType.SOFTWARE.urlString()));

    JourneyStage decontrolsSoftwareCatchallControls = defineStage("decontrolsSoftwareCatchallControls", "Decontrols",
        controllers.controlcode.routes.DecontrolsController.renderForm(ControlCodeVariant.CATCHALL_CONTROLS.urlString(), GoodsType.SOFTWARE.urlString()));

    JourneyStage technicalNotesSoftwareCatchallControls = defineStage("technicalNotesSoftwareCatchallControls", "Technical notes",
        controllers.controlcode.routes.TechnicalNotesController.renderForm(ControlCodeVariant.CATCHALL_CONTROLS.urlString(), GoodsType.SOFTWARE.urlString()));

    atStage(softwareCatchallControls)
        .onEvent(Events.CONTROL_CODE_SELECTED)
        .then(moveTo(controlCodeSoftwareCatchallControls));

    atStage(softwareCatchallControls)
        .onEvent(Events.CONTROL_CODE_SOFT_TECH_CATCHALL_RELATIONSHIP)
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
        .onEvent(Events.CONTROL_CODE_SOFT_TECH_CONTROLS_NOT_APPLICABLE)
        .branch()
        .when(ApplicableSoftTechControls.ONE, moveTo(controlCodeNotApplicableSoftwareCatchallControls))
        .when(ApplicableSoftTechControls.GREATER_THAN_ONE, moveTo(controlCodeNotApplicableSoftwareCatchallControls));

    atStage(additionalSpecificationSoftwareCatchallControls)
        .onEvent(Events.CONTROL_CODE_FLOW_NEXT)
        .branch()
        .when(ControlCodeFlowStage.DECONTROLS, moveTo(decontrolsSoftwareCatchallControls))
        .when(ControlCodeFlowStage.TECHNICAL_NOTES, moveTo(technicalNotesSoftwareCatchallControls))
        .when(ControlCodeFlowStage.CONFIRMED, moveTo(destinationCountries));

    atStage(additionalSpecificationSoftwareCatchallControls)
        .onEvent(Events.CONTROL_CODE_SOFT_TECH_CONTROLS_NOT_APPLICABLE)
        .branch()
        .when(ApplicableSoftTechControls.ONE, moveTo(controlCodeNotApplicableSoftwareCatchallControls))
        .when(ApplicableSoftTechControls.GREATER_THAN_ONE, moveTo(controlCodeNotApplicableSoftwareCatchallControls));

    atStage(decontrolsSoftwareCatchallControls)
        .onEvent(Events.CONTROL_CODE_FLOW_NEXT)
        .branch()
        .when(ControlCodeFlowStage.TECHNICAL_NOTES, moveTo(technicalNotesSoftwareCatchallControls))
        .when(ControlCodeFlowStage.CONFIRMED, moveTo(destinationCountries));

    atStage(decontrolsSoftwareCatchallControls)
        .onEvent(Events.CONTROL_CODE_SOFT_TECH_CONTROLS_NOT_APPLICABLE)
        .branch()
        .when(ApplicableSoftTechControls.ONE, moveTo(controlCodeNotApplicableSoftwareCatchallControls))
        .when(ApplicableSoftTechControls.GREATER_THAN_ONE, moveTo(controlCodeNotApplicableSoftwareCatchallControls));


    atStage(technicalNotesSoftwareCatchallControls)
        .onEvent(Events.CONTROL_CODE_FLOW_NEXT)
        .branch()
        .when(ControlCodeFlowStage.CONFIRMED, moveTo(destinationCountries));

    atStage(technicalNotesSoftwareCatchallControls)
        .onEvent(Events.CONTROL_CODE_SOFT_TECH_CONTROLS_NOT_APPLICABLE)
        .branch()
        .when(ApplicableSoftTechControls.ONE, moveTo(controlCodeNotApplicableSoftwareCatchallControls))
        .when(ApplicableSoftTechControls.GREATER_THAN_ONE, moveTo(controlCodeNotApplicableSoftwareCatchallControls));

    atStage(controlCodeNotApplicableSoftwareCatchallControls)
        .onEvent(Events.CONTROL_CODE_SOFT_TECH_CATCHALL_RELATIONSHIP)
        .branch()
        .when(Relationship.RELATIONSHIP_EXISTS, moveTo(softwareTechnologyRelationship))
        .when(Relationship.RELATIONSHIP_DOES_NOT_EXIST, moveTo(softwareRelationshipNLR));

    atStage(controlCodeNotApplicableSoftwareCatchallControls)
        .onEvent(Events.CONTROL_CODE_FLOW_NEXT)
        .branch()
        .when(ControlCodeFlowStage.BACK_TO_MATCHES, moveTo(softwareCatchallControls));

  }

  /**
   * Catch all software controls
   * @param journeyStage
   */
  private void bindCatchallSoftwareControls(JourneyStage journeyStage) {
    atStage(journeyStage)
        .onEvent(Events.CATCHALL_SOFT_TECH_CONTROLS_FLOW)
        .branch()
        .when(CatchallSoftTechControlsFlow.CATCHALL_ONE, moveTo(controlCodeSoftwareCatchallControls))
        .when(CatchallSoftTechControlsFlow.CATCHALL_GREATER_THAN_ONE, moveTo(softwareCatchallControls))
        .when(CatchallSoftTechControlsFlow.RELATIONSHIP_EXISTS, moveTo(softwareTechnologyRelationship))
        .when(CatchallSoftTechControlsFlow.RELATIONSHIP_DOES_NOT_EXIST, moveTo(softwareRelationshipNLR));
  }

  private void bindControlCodeNotApplicableStage(JourneyStage controlCodeNotApplicableStage,
                                                 JourneyStage searchStage,
                                                 JourneyStage searchResultsStage) {
    atStage(controlCodeNotApplicableStage)
        .onEvent(Events.BACK)
        .branch()
        .when(BackType.RESULTS, backTo(searchResultsStage))
        .when(BackType.SEARCH, backTo(searchStage));
  }

  private void bindControlCodeStageTransitions(JourneyStage controlCodeSummaryStage,
                                               JourneyStage controlCodeNotApplicableStage,
                                               JourneyStage additionalSpecificationsStage,
                                               JourneyStage decontrolsStage,
                                               JourneyStage technicalNotesStage,
                                               CommonStage exitStage,
                                               DecisionStage<Boolean> additionalSpecificationsDecisionStage,
                                               DecisionStage<Boolean> decontrolsDecisionStage,
                                               DecisionStage<Boolean> technicalNotesDecisionStage) {
    atStage(controlCodeSummaryStage)
        .onEvent(StandardEvents.NEXT)
        .then(moveTo(additionalSpecificationsDecisionStage));

    atStage(controlCodeSummaryStage)
        .onEvent(Events.CONTROL_CODE_NOT_APPLICABLE)
        .then(moveTo(controlCodeNotApplicableStage));

    atStage(additionalSpecificationsStage)
        .onEvent(StandardEvents.NEXT)
        .then(moveTo(decontrolsDecisionStage));

    atStage(additionalSpecificationsStage)
        .onEvent(Events.CONTROL_CODE_NOT_APPLICABLE)
        .then(moveTo(controlCodeNotApplicableStage));

    atStage(decontrolsStage)
        .onEvent(StandardEvents.NEXT)
        .then(moveTo(technicalNotesDecisionStage));

    atStage(decontrolsStage)
        .onEvent(Events.CONTROL_CODE_NOT_APPLICABLE)
        .then(moveTo(controlCodeNotApplicableStage));

    atStage(technicalNotesStage)
        .onEvent(StandardEvents.NEXT)
        .then(moveTo(exitStage));

    atStage(technicalNotesStage)
        .onEvent(Events.CONTROL_CODE_NOT_APPLICABLE)
        .then(moveTo(controlCodeNotApplicableStage));

    /** Related to software decision stage transitions */
    atDecisionStage(additionalSpecificationsDecisionStage)
        .decide()
        .when(true, moveTo(additionalSpecificationsStage))
        .when(false, moveTo(decontrolsDecisionStage));

    atDecisionStage(decontrolsDecisionStage)
        .decide()
        .when(true, moveTo(decontrolsStage))
        .when(false, moveTo(technicalNotesDecisionStage));

    atDecisionStage(technicalNotesDecisionStage)
        .decide()
        .when(true, moveTo(technicalNotesStage))
        .when(false, moveTo(exitStage));
  }
}
