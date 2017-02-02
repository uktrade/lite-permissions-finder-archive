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
import journey.deciders.CatchallControlsDecider;
import journey.deciders.CategoryControlsDecider;
import journey.deciders.ExportCategoryDecider;
import journey.deciders.RelatedCodesDecider;
import journey.deciders.RelatedControlsDecider;
import journey.deciders.RelationshipWithSoftwareDecider;
import journey.deciders.RelationshipWithTechnologyDecider;
import journey.deciders.controlcode.AdditionalSpecificationsDecider;
import journey.deciders.controlcode.DecontrolsDecider;
import journey.deciders.controlcode.TechnicalNotesDecider;
import models.ArtsCulturalGoodsType;
import models.ExportCategory;
import models.GoodsType;
import models.LifeType;
import models.RadioactiveStage;
import models.VirtualEUOgelStage;
import models.controlcode.BackType;
import models.controlcode.ControlCodeVariant;
import models.softtech.ApplicableSoftTechControls;

public class ExportJourneyDefinitionBuilder extends JourneyDefinitionBuilder {

  private final JourneyStage exportCategory = defineStage("exportCategory", "What are you exporting?",
      controllers.categories.routes.ExportCategoryController.renderForm());
  private final JourneyStage goodsType = defineStage("goodsType", "Are you exporting goods, software or technical information?",
      routes.GoodsTypeController.renderForm());
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

  /** Physical **/
  private final JourneyStage search = defineStage("search", "Describe your item",
      controllers.search.routes.SearchController.renderForm(GoodsType.PHYSICAL.urlString()));
  private final DecisionStage<Boolean> searchRelatedCodesDecision;

  /** Software **/
  private final DecisionStage<ExportCategory> dualUseOrMilitarySoftwareDecision;
  private final DecisionStage<ApplicableSoftTechControls> softwareApplicableControlsDecision;
  private final DecisionStage<ApplicableSoftTechControls> softwareApplicableRelatedControlsDecision;
  private final DecisionStage<ApplicableSoftTechControls> softwareApplicableCatchallControlsDecision;
  private final DecisionStage<Boolean> softwareRelationshipWithTechnologyExistsDecision;
  private final DecisionStage<Boolean> softwareRelationshipWithSoftwareExistsDecision;
  private final DecisionStage<Boolean> softwareSearchRelatedCodesDecision;

  private JourneyStage softwareJourneyEndNLR = defineStage("softwareJourneyEndNLR", "No licence available",
      routes.StaticContentController.renderSoftwareJourneyEndNLR());

  /** Technology **/
  private final DecisionStage<ExportCategory> dualUseOrMilitaryTechnologyDecision;
  private final DecisionStage<ExportCategory> dualUseOrMilitaryNonExemptTechnologyDecision;
  private final DecisionStage<ApplicableSoftTechControls> technologyApplicableControlsDecision;
  private final DecisionStage<ApplicableSoftTechControls> technologyApplicableRelatedControlsDecision;
  private final DecisionStage<ApplicableSoftTechControls> technologyApplicableCatchallControlsDecision;
  private final DecisionStage<Boolean> technologyRelationshipWithSoftwareExistsDecision;
  private final DecisionStage<Boolean> technologySearchRelatedCodesDecision;

  private final JourneyStage technologyExemptions = defineStage("technologyExemptions", "Technology exemptions",
      controllers.softtech.routes.TechnologyExemptionsController.renderForm());

  private final JourneyStage technologyExemptionsNLR = defineStage("technologyExemptionsNLR", "Technology exemptions apply",
      controllers.routes.StaticContentController.renderTechnologyExemptionsNLR());

  /** Deciders **/
  private final AdditionalSpecificationsDecider additionalSpecificationsDecider;
  private final DecontrolsDecider decontrolsDecider;
  private final TechnicalNotesDecider technicalNotesDecider;
  private final ExportCategoryDecider exportCategoryDecider;
  private final CategoryControlsDecider categoryControlsDecider;
  private final RelatedControlsDecider relatedControlsDecider;
  private final CatchallControlsDecider catchallControlsDecider;
  private final RelationshipWithTechnologyDecider relationshipWithTechnologyDecider;
  private final RelationshipWithSoftwareDecider relationshipWithSoftwareDecider;
  private final RelatedCodesDecider relatedCodesDecider;

  @Inject
  public ExportJourneyDefinitionBuilder(AdditionalSpecificationsDecider additionalSpecificationsDecider,
                                        DecontrolsDecider decontrolsDecider,
                                        TechnicalNotesDecider technicalNotesDecider,
                                        ExportCategoryDecider exportCategoryDecider,
                                        CategoryControlsDecider categoryControlsDecider,
                                        RelatedControlsDecider relatedControlsDecider,
                                        CatchallControlsDecider catchallControlsDecider,
                                        RelationshipWithTechnologyDecider relationshipWithTechnologyDecider,
                                        RelationshipWithSoftwareDecider relationshipWithSoftwareDecider,
                                        RelatedCodesDecider relatedCodesDecider) {
    this.additionalSpecificationsDecider = additionalSpecificationsDecider;
    this.decontrolsDecider = decontrolsDecider;
    this.technicalNotesDecider = technicalNotesDecider;
    this.exportCategoryDecider = exportCategoryDecider;
    this.categoryControlsDecider = categoryControlsDecider;
    this.relatedControlsDecider = relatedControlsDecider;
    this.catchallControlsDecider = catchallControlsDecider;
    this.relationshipWithTechnologyDecider = relationshipWithTechnologyDecider;
    this.relationshipWithSoftwareDecider = relationshipWithSoftwareDecider;
    this.relatedCodesDecider = relatedCodesDecider;
    this.dualUseOrMilitarySoftwareDecision = defineDecisionStage("dualUseOrMilitarySoftwareDecision", this.exportCategoryDecider);
    this.dualUseOrMilitaryTechnologyDecision = defineDecisionStage("dualUseOrMilitaryTechnologyDecision", this.exportCategoryDecider);
    this.dualUseOrMilitaryNonExemptTechnologyDecision = defineDecisionStage("dualUseOrMilitaryNonExemptTechnologyDecision", this.exportCategoryDecider);
    this.softwareApplicableControlsDecision = defineDecisionStage("softwareApplicableControlsDecision", this.categoryControlsDecider);
    this.technologyApplicableControlsDecision = defineDecisionStage("technologyApplicableControlsDecision", this.categoryControlsDecider);
    this.softwareApplicableRelatedControlsDecision = defineDecisionStage("softwareApplicableRelatedControlsDecision", this.relatedControlsDecider);
    this.technologyApplicableRelatedControlsDecision = defineDecisionStage("technologyApplicableRelatedControlsDecision", this.relatedControlsDecider);
    this.softwareApplicableCatchallControlsDecision = defineDecisionStage("softwareApplicableCatchallControlsDecision", this.catchallControlsDecider);
    this.technologyApplicableCatchallControlsDecision = defineDecisionStage("technologyApplicableCatchallControlsDecision", this.catchallControlsDecider);
    this.softwareRelationshipWithTechnologyExistsDecision = defineDecisionStage("softwareRelationshipWithTechnologyExistsDecision", this.relationshipWithTechnologyDecider);
    this.softwareRelationshipWithSoftwareExistsDecision = defineDecisionStage("softwareRelationshipWithSoftwareExistsDecision", this.relationshipWithSoftwareDecider);
    this.technologyRelationshipWithSoftwareExistsDecision = defineDecisionStage("technologyRelationshipWithSoftwareExistsDecision", this.relationshipWithSoftwareDecider);
    this.searchRelatedCodesDecision = defineDecisionStage("searchRelatedCodesDecision", this.relatedCodesDecider);
    this.softwareSearchRelatedCodesDecision = defineDecisionStage("softwareSearchRelatedCodesDecision", this.relatedCodesDecider);
    this.technologySearchRelatedCodesDecision = defineDecisionStage("technologySearchRelatedCodesDecision", this.relatedCodesDecider);
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
        .when(GoodsType.TECHNOLOGY, moveTo(technologyExemptions));

    physicalGoodsStages();

    softwareStages();

    technologyStages();

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
        .then(moveTo(notImplemented)); // TODO This should go through to the technical information search (when implemented) LITE-453

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
        controllers.search.routes.SearchResultsController.renderForm(GoodsType.PHYSICAL.urlString()));

    JourneyStage searchRelatedCodes = defineStage("searchRelatedCodes", "Related to your item",
        controllers.search.routes.SearchRelatedCodesController.renderForm(GoodsType.PHYSICAL.urlString()));

    JourneyStage controlCodeSummary = defineStage("controlCodeSummary", "Summary",
        controllers.controlcode.routes.ControlCodeSummaryController.renderForm(ControlCodeVariant.SEARCH.urlString(), GoodsType.PHYSICAL.urlString()));

    JourneyStage controlCodeNotApplicable = defineStage("controlCodeNotApplicable", "Rating is not applicable",
        controllers.controlcode.routes.NotApplicableController.renderForm(ControlCodeVariant.SEARCH.urlString(), GoodsType.PHYSICAL.urlString()));

    JourneyStage additionalSpecifications = defineStage("additionalSpecifications", "Additional specifications",
        controllers.controlcode.routes.AdditionalSpecificationsController.renderForm(ControlCodeVariant.SEARCH.urlString(), GoodsType.PHYSICAL.urlString()));

    JourneyStage decontrols = defineStage("decontrols", "Decontrols",
        controllers.controlcode.routes.DecontrolsController.renderForm(ControlCodeVariant.SEARCH.urlString(), GoodsType.PHYSICAL.urlString()));

    JourneyStage decontrolsApply = defineStage("decontrolsApply", "Choose a different item type",
        controllers.controlcode.routes.DecontrolsApplyController.renderForm(ControlCodeVariant.SEARCH.urlString(), GoodsType.PHYSICAL.urlString()));

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

    DecisionStage<Boolean> additionalSpecsDecision = defineDecisionStage("hasAdditionalSpecs", additionalSpecificationsDecider);

    DecisionStage<Boolean> decontrolsDecision = defineDecisionStage("hasDecontrols", decontrolsDecider);

    DecisionStage<Boolean> technicalNotesDecision = defineDecisionStage("hasTechNotes", technicalNotesDecider);

    atStage(search)
        .onEvent(Events.SEARCH_PHYSICAL_GOODS)
        .then(moveTo(searchResults));

    atStage(searchResults)
        .onEvent(Events.CONTROL_CODE_SELECTED)
        .then(moveTo(searchRelatedCodesDecision));

    atStage(searchResults)
        .onEvent(Events.NONE_MATCHED)
        .then(moveTo(notApplicable));

    atDecisionStage(searchRelatedCodesDecision)
        .decide()
        .when(true, moveTo(searchRelatedCodes))
        .when(false, moveTo(decontrolsDecision));

    atStage(searchRelatedCodes)
        .onEvent(Events.CONTROL_CODE_SELECTED)
        .then(moveTo(decontrolsDecision));

    atStage(searchRelatedCodes)
        .onEvent(Events.BACK)
        .branch()
        .when(BackType.RESULTS, backTo(searchResults));

    bindControlCodeStageTransitions(
        decontrols,
        decontrolsApply,
        controlCodeSummary,
        controlCodeNotApplicable,
        additionalSpecifications,
        technicalNotes,
        destinationCountries,
        decontrolsDecision,
        additionalSpecsDecision,
        technicalNotesDecision
    );

    atStage(decontrolsApply)
        .onEvent(Events.BACK)
        .branch()
        .when(BackType.SEARCH, backTo(search))
        .when(BackType.RESULTS, backTo(searchResults))
        .when(BackType.EXPORT_CATEGORY, backTo(exportCategory));

    bindControlCodeNotApplicableFromSearchStageJourneyTransitions(
        controlCodeNotApplicable,
        search,
        searchResults
    );

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

    JourneyStage softwareExemptionsQ1 = defineStage("softwareExemptionsQ1", "Some types of software do not need a licence",
        controllers.softtech.routes.SoftwareExemptionsController.renderFormQ1());

    JourneyStage softwareExemptionsQ2 = defineStage("softwareExemptionsQ2", "Some types of software do not need a licence",
        controllers.softtech.routes.SoftwareExemptionsController.renderFormQ2());

    JourneyStage softwareExemptionsQ3 = defineStage("softwareExemptionsQ3", "Some types of software do not need a licence",
        controllers.softtech.routes.SoftwareExemptionsController.renderFormQ3());

    JourneyStage softwareExemptionsNLR1 = defineStage("softwareExemptionsNLR1", "Software exemptions apply",
        controllers.routes.StaticContentController.renderSoftwareExemptionsNLR1());

    JourneyStage softwareExemptionsNLR2 = defineStage("softwareExemptionsNLR2", "Software exemptions apply",
        controllers.routes.StaticContentController.renderSoftwareExemptionsNLR2());

    JourneyStage relatedToEquipmentOrMaterials = defineStage("softwareRelatedToEquipmentOrMaterials", "Is your software any of the following?",
        controllers.softtech.routes.RelatedEquipmentController.renderForm(GoodsType.SOFTWARE.urlString()));

    JourneyStage dualUseCategories = defineStage("softwareDualUseCategories", "What is your software for?",
        controllers.softtech.routes.DualUseSoftTechCategoriesController.renderForm(GoodsType.SOFTWARE.urlString()));

    JourneyStage categoryControlsList = defineStage("softwareCategoryControlsList", "Showing controls related to software category",
        controllers.softtech.controls.routes.SoftTechControlsController.renderForm(ControlCodeVariant.CONTROLS.urlString(), GoodsType.SOFTWARE.urlString()));

    JourneyStage catchallControlsList = defineStage("softwareCatchallControlsList", "Showing catchall controls related to your items category",
        controllers.softtech.controls.routes.SoftTechControlsController.renderForm(ControlCodeVariant.CATCHALL_CONTROLS.urlString(),GoodsType.SOFTWARE.urlString()));

    JourneyStage relatedToPhysicalGoodsControlsList = defineStage("softwareControlsRelatedToPhysicalGoodsControlsList", "Showing controls related to your selected physical good",
        controllers.softtech.controls.routes.SoftTechControlsController.renderForm(ControlCodeVariant.CONTROLS_RELATED_TO_A_PHYSICAL_GOOD.urlString(),GoodsType.SOFTWARE.urlString()));

    JourneyStage categoryControlCodeSummary = defineStage("softwareCategoryControlCodeSummary", "Summary",
        controllers.controlcode.routes.ControlCodeSummaryController.renderForm(ControlCodeVariant.CONTROLS.urlString(), GoodsType.SOFTWARE.urlString()));

    JourneyStage relatedToPhysicalGoodsControlCodeSummary = defineStage("softwareRelatedToPhysicalGoodsControlCodeSummary", "Summary",
        controllers.controlcode.routes.ControlCodeSummaryController.renderForm(ControlCodeVariant.CONTROLS_RELATED_TO_A_PHYSICAL_GOOD.urlString(), GoodsType.SOFTWARE.urlString()));

    JourneyStage catchallControlCodeSummary = defineStage("softwareCatchallControlCodeSummary", "Summary",
        controllers.controlcode.routes.ControlCodeSummaryController.renderForm(ControlCodeVariant.CATCHALL_CONTROLS.urlString(), GoodsType.SOFTWARE.urlString()));

    JourneyStage searchRelatedTo = defineStage("softwareSearchRelatedTo", "Describe the equipment or materials your software is related to",
        controllers.search.routes.SearchController.renderForm(GoodsType.SOFTWARE.urlString()));

    JourneyStage relatedToTechnologyQuestion = defineStage("softwareRelatedToTechnologyQuestion", "Is your software related to a technology?",
        controllers.softtech.routes.GoodsRelationshipController.renderForm(GoodsType.SOFTWARE.urlString(), GoodsType.TECHNOLOGY.urlString()));

    JourneyStage goodsRelatedToTechnologyQuestions = defineStage("softwareGoodsRelatedToTechnologyQuestions", "Software related to technology",
        controllers.softtech.routes.GoodsRelationshipQuestionsController.renderForm(GoodsType.SOFTWARE.urlString(), GoodsType.TECHNOLOGY.urlString()));

    JourneyStage relatedToSoftwareQuestion = defineStage("softwareRelatedToSoftwareQuestion", "Is your software related to other software?",
        controllers.softtech.routes.GoodsRelationshipController.renderForm(GoodsType.SOFTWARE.urlString(), GoodsType.SOFTWARE.urlString()));

    JourneyStage goodsRelatedToSoftwareQuestions = defineStage("softwareGoodsRelatedToSoftwareQuestions", "Software related to software",
        controllers.softtech.routes.GoodsRelationshipQuestionsController.renderForm(GoodsType.SOFTWARE.urlString(), GoodsType.SOFTWARE.urlString()));

    atDecisionStage(dualUseOrMilitarySoftwareDecision)
        .decide()
        .when(ExportCategory.MILITARY, moveTo(softwareApplicableControlsDecision))
        .when(ExportCategory.DUAL_USE, moveTo(softwareExemptionsQ1));

    // softwareExemptionsQ1 journey transitions
    bindYesNoJourneyTransition(
        softwareExemptionsQ1,
        softwareExemptionsNLR1,
        softwareExemptionsQ2
    );

    // softwareExemptionsQ2 journey transitions
    bindYesNoJourneyTransition(
        softwareExemptionsQ2,
        softwareApplicableControlsDecision,
        softwareExemptionsQ3
    );

    // softwareExemptionsQ3 journey transitions
    bindYesNoJourneyTransition(
        softwareExemptionsQ3,
        softwareExemptionsNLR2,
        dualUseCategories
    );

    atStage(dualUseCategories)
        .onEvent(StandardEvents.NEXT)
        .then(moveTo(softwareApplicableControlsDecision));

    atStage(dualUseCategories)
        .onEvent(Events.NONE_MATCHED)
        .then(moveTo(relatedToEquipmentOrMaterials));

    atDecisionStage(softwareApplicableControlsDecision)
        .decide()
        .when(ApplicableSoftTechControls.ZERO, moveTo(relatedToEquipmentOrMaterials))
        .when(ApplicableSoftTechControls.ONE, moveTo(categoryControlCodeSummary))
        .when(ApplicableSoftTechControls.GREATER_THAN_ONE, moveTo(categoryControlsList));

    softTechCategoryControls(
        GoodsType.SOFTWARE,
        categoryControlsList,
        categoryControlCodeSummary,
        relatedToEquipmentOrMaterials,
        dualUseCategories
    );

    atStage(relatedToEquipmentOrMaterials)
        .onEvent(StandardEvents.YES)
        .then(moveTo(searchRelatedTo));

    atStage(relatedToEquipmentOrMaterials)
        .onEvent(StandardEvents.NO)
        .then(moveTo(softwareApplicableCatchallControlsDecision));

    softTechSearchRelatedTo(
        GoodsType.SOFTWARE,
        searchRelatedTo,
        softwareSearchRelatedCodesDecision,
        softwareApplicableCatchallControlsDecision,
        softwareApplicableRelatedControlsDecision
    );

    atDecisionStage(softwareApplicableRelatedControlsDecision)
        .decide()
        .when(ApplicableSoftTechControls.ZERO, moveTo(softwareRelationshipWithTechnologyExistsDecision))
        .when(ApplicableSoftTechControls.ONE, moveTo(relatedToPhysicalGoodsControlCodeSummary))
        .when(ApplicableSoftTechControls.GREATER_THAN_ONE, moveTo(relatedToPhysicalGoodsControlsList));

    softTechControlsRelatedToAPhysicalGood(
        GoodsType.SOFTWARE,
        relatedToPhysicalGoodsControlsList,
        relatedToPhysicalGoodsControlCodeSummary,
        softwareApplicableCatchallControlsDecision
    );

    atDecisionStage(softwareApplicableCatchallControlsDecision)
        .decide()
        .when(ApplicableSoftTechControls.ZERO, moveTo(softwareRelationshipWithTechnologyExistsDecision))
        .when(ApplicableSoftTechControls.ONE, moveTo(catchallControlCodeSummary))
        .when(ApplicableSoftTechControls.GREATER_THAN_ONE, moveTo(catchallControlsList));

    softTechCatchallControls(
        GoodsType.SOFTWARE,
        catchallControlsList,
        catchallControlCodeSummary,
        softwareRelationshipWithTechnologyExistsDecision
    );

    /** Software related to technology **/

    atDecisionStage(softwareRelationshipWithTechnologyExistsDecision)
        .decide()
        .when(true, moveTo(relatedToTechnologyQuestion))
        .when(false, moveTo(softwareRelationshipWithSoftwareExistsDecision));

    bindYesNoJourneyTransition(
        relatedToTechnologyQuestion,
        goodsRelatedToTechnologyQuestions,
        softwareRelationshipWithSoftwareExistsDecision
    );

    bindYesNoJourneyTransition(
        goodsRelatedToTechnologyQuestions,
        destinationCountries,
        relatedToSoftwareQuestion
    );

    /** Software related to software **/

    atDecisionStage(softwareRelationshipWithSoftwareExistsDecision)
        .decide()
        .when(true, moveTo(relatedToSoftwareQuestion))
        .when(false, moveTo(softwareJourneyEndNLR));

    bindYesNoJourneyTransition(
        relatedToSoftwareQuestion,
        goodsRelatedToSoftwareQuestions,
        softwareJourneyEndNLR
    );

    bindYesNoJourneyTransition(
        goodsRelatedToSoftwareQuestions,
        destinationCountries,
        softwareJourneyEndNLR
    );

  }

  private void technologyStages() {

    JourneyStage technologyNonExempt = defineStage("technologyNonExempt", "Minimum required technology",
        controllers.softtech.routes.TechnologyNonExemptController.renderForm());

    JourneyStage relatedToEquipmentOrMaterials = defineStage("technologyRelatedToEquipmentOrMaterials", "Is your software any of the following?",
        controllers.softtech.routes.RelatedEquipmentController.renderForm(GoodsType.TECHNOLOGY.urlString()));

    JourneyStage dualUseCategories = defineStage("technologyDualUseCategories", "What is your software for?",
        controllers.softtech.routes.DualUseSoftTechCategoriesController.renderForm(GoodsType.TECHNOLOGY.urlString()));

    JourneyStage nonExemptControlsControlsList = defineStage("technologyNonExemptControlsControlsList", "Showing technology that is not covered by exemptions",
        controllers.softtech.controls.routes.SoftTechControlsController.renderForm(ControlCodeVariant.NON_EXEMPT.urlString(), GoodsType.TECHNOLOGY.urlString()));

    JourneyStage categoryControlsList = defineStage("technologyCategoryControlsList", "Showing controls related to software category",
        controllers.softtech.controls.routes.SoftTechControlsController.renderForm(ControlCodeVariant.CONTROLS.urlString(), GoodsType.TECHNOLOGY.urlString()));

    JourneyStage catchallControlsList = defineStage("technologyCatchallControlsList", "Showing catchall controls related to your items category",
        controllers.softtech.controls.routes.SoftTechControlsController.renderForm(ControlCodeVariant.CATCHALL_CONTROLS.urlString(),GoodsType.TECHNOLOGY.urlString()));

    JourneyStage relatedToPhysicalGoodsControlsList = defineStage("technologyControlsRelatedToPhysicalGoodsControlsList", "Showing controls related to your selected physical good",
        controllers.softtech.controls.routes.SoftTechControlsController.renderForm(ControlCodeVariant.CONTROLS_RELATED_TO_A_PHYSICAL_GOOD.urlString(),GoodsType.TECHNOLOGY.urlString()));

    JourneyStage categoryControlCodeSummary = defineStage("technologyCategoryControlCodeSummary", "Summary",
        controllers.controlcode.routes.ControlCodeSummaryController.renderForm(ControlCodeVariant.CONTROLS.urlString(), GoodsType.TECHNOLOGY.urlString()));

    JourneyStage relatedToPhysicalGoodsControlCodeSummary = defineStage("technologyRelatedToPhysicalGoodsControlCodeSummary", "Summary",
        controllers.controlcode.routes.ControlCodeSummaryController.renderForm(ControlCodeVariant.CONTROLS_RELATED_TO_A_PHYSICAL_GOOD.urlString(), GoodsType.TECHNOLOGY.urlString()));

    JourneyStage catchallControlCodeSummary = defineStage("technologyCatchallControlCodeSummary", "Summary",
        controllers.controlcode.routes.ControlCodeSummaryController.renderForm(ControlCodeVariant.CATCHALL_CONTROLS.urlString(), GoodsType.TECHNOLOGY.urlString()));

    JourneyStage searchRelatedTo = defineStage("technologySearchRelatedTo", "Describe the equipment or materials your software is related to",
        controllers.search.routes.SearchController.renderForm(GoodsType.TECHNOLOGY.urlString()));

    JourneyStage relatedToSoftwareQuestion = defineStage("technologyRelatedToSoftwareQuestion", "Is your technology related to other software?",
        controllers.softtech.routes.GoodsRelationshipController.renderForm(GoodsType.TECHNOLOGY.urlString(), GoodsType.SOFTWARE.urlString()));

    JourneyStage goodsRelatedToSoftwareQuestions = defineStage("technologyGoodsRelatedToSoftwareQuestions", "Software related to software",
        controllers.softtech.routes.GoodsRelationshipQuestionsController.renderForm(GoodsType.TECHNOLOGY.urlString(), GoodsType.SOFTWARE.urlString()));

    JourneyStage journeyEndNLR = defineStage("technologyJourneyEndNLR", "No licence available",
        routes.StaticContentController.renderTechnologyJourneyEndNLR());

    bindYesNoJourneyTransition(
        technologyExemptions,
        technologyExemptionsNLR,
        technologyNonExempt
    );

    bindYesNoJourneyTransition(
        technologyNonExempt,
        dualUseOrMilitaryNonExemptTechnologyDecision,
        dualUseOrMilitaryTechnologyDecision
    );

    atDecisionStage(dualUseOrMilitaryNonExemptTechnologyDecision)
        .decide()
        .when(ExportCategory.MILITARY, moveTo(technologyExemptionsNLR)) // TODO is this the same NLR?
        .when(ExportCategory.DUAL_USE, moveTo(nonExemptControlsControlsList));

    atDecisionStage(dualUseOrMilitaryTechnologyDecision)
        .decide()
        .when(ExportCategory.MILITARY, moveTo(technologyApplicableControlsDecision)) // TODO is this the same NLR?
        .when(ExportCategory.DUAL_USE, moveTo(dualUseCategories));

    technologyNonExemptControls(nonExemptControlsControlsList);

    atStage(dualUseCategories)
        .onEvent(StandardEvents.NEXT)
        .then(moveTo(technologyApplicableControlsDecision));

    atStage(dualUseCategories)
        .onEvent(Events.NONE_MATCHED)
        .then(moveTo(relatedToEquipmentOrMaterials));

    atDecisionStage(technologyApplicableControlsDecision)
        .decide()
        .when(ApplicableSoftTechControls.ZERO, moveTo(relatedToEquipmentOrMaterials))
        .when(ApplicableSoftTechControls.ONE, moveTo(categoryControlCodeSummary))
        .when(ApplicableSoftTechControls.GREATER_THAN_ONE, moveTo(categoryControlsList));

    softTechCategoryControls(
        GoodsType.TECHNOLOGY,
        categoryControlsList,
        categoryControlCodeSummary,
        relatedToEquipmentOrMaterials,
        dualUseCategories
    );

    atStage(relatedToEquipmentOrMaterials)
        .onEvent(StandardEvents.YES)
        .then(moveTo(searchRelatedTo));

    atStage(relatedToEquipmentOrMaterials)
        .onEvent(StandardEvents.NO)
        .then(moveTo(technologyApplicableCatchallControlsDecision));

    softTechSearchRelatedTo(
        GoodsType.TECHNOLOGY,
        searchRelatedTo,
        technologySearchRelatedCodesDecision,
        technologyApplicableCatchallControlsDecision,
        technologyApplicableRelatedControlsDecision
    );

    atDecisionStage(technologyApplicableRelatedControlsDecision)
        .decide()
        .when(ApplicableSoftTechControls.ZERO, moveTo(technologyRelationshipWithSoftwareExistsDecision))
        .when(ApplicableSoftTechControls.ONE, moveTo(relatedToPhysicalGoodsControlCodeSummary))
        .when(ApplicableSoftTechControls.GREATER_THAN_ONE, moveTo(relatedToPhysicalGoodsControlsList));

    softTechControlsRelatedToAPhysicalGood(
        GoodsType.TECHNOLOGY,
        relatedToPhysicalGoodsControlsList,
        relatedToPhysicalGoodsControlCodeSummary,
        technologyApplicableCatchallControlsDecision
    );

    atDecisionStage(technologyApplicableCatchallControlsDecision)
        .decide()
        .when(ApplicableSoftTechControls.ZERO, moveTo(technologyRelationshipWithSoftwareExistsDecision))
        .when(ApplicableSoftTechControls.ONE, moveTo(catchallControlCodeSummary))
        .when(ApplicableSoftTechControls.GREATER_THAN_ONE, moveTo(catchallControlsList));

    softTechCatchallControls(
        GoodsType.TECHNOLOGY,
        catchallControlsList,
        catchallControlCodeSummary,
        technologyRelationshipWithSoftwareExistsDecision
    );

    atDecisionStage(technologyRelationshipWithSoftwareExistsDecision)
        .decide()
        .when(true, moveTo(relatedToSoftwareQuestion))
        .when(false, moveTo(journeyEndNLR));

    bindYesNoJourneyTransition(
        relatedToSoftwareQuestion,
        goodsRelatedToSoftwareQuestions,
        journeyEndNLR
    );

    bindYesNoJourneyTransition(
        goodsRelatedToSoftwareQuestions,
        destinationCountries,
        journeyEndNLR
    );
  }

  private void technologyNonExemptControls(JourneyStage controlsList) {
    /** Technology non exempt controls journey stages */
    JourneyStage controlCodeSummary = defineStage("technologyNonExemptControlsControlCodeSummary", "Summary",
        controllers.controlcode.routes.ControlCodeSummaryController.renderForm(ControlCodeVariant.NON_EXEMPT.urlString(), GoodsType.TECHNOLOGY.urlString()));

    JourneyStage controlCodeNotApplicable = defineStage("technologyNonExemptControlsControlCodeNotApplicable", "Description not applicable",
        controllers.controlcode.routes.NotApplicableController.renderForm(ControlCodeVariant.NON_EXEMPT.urlString(), GoodsType.TECHNOLOGY.urlString()));

    JourneyStage additionalSpecifications = defineStage("technologyNonExemptControlsAdditionalSpecifications", "Additional specifications",
        controllers.controlcode.routes.AdditionalSpecificationsController.renderForm(ControlCodeVariant.NON_EXEMPT.urlString(), GoodsType.TECHNOLOGY.urlString()));

    JourneyStage decontrols = defineStage("technologyNonExemptControlsDecontrols", "Decontrols",
        controllers.controlcode.routes.DecontrolsController.renderForm(ControlCodeVariant.NON_EXEMPT.urlString(), GoodsType.TECHNOLOGY.urlString()));

    JourneyStage decontrolsApply = defineStage("technologyNonExemptControlsDecontrolsApply", "Choose a different item type",
        controllers.controlcode.routes.DecontrolsApplyController.renderForm(ControlCodeVariant.NON_EXEMPT.urlString(), GoodsType.TECHNOLOGY.urlString()));

    JourneyStage technicalNotes = defineStage("technologyNonExemptControlsTechnicalNotes", "Technical notes",
        controllers.controlcode.routes.TechnicalNotesController.renderForm(ControlCodeVariant.NON_EXEMPT.urlString(), GoodsType.TECHNOLOGY.urlString()));

    /** Software controls decision stages */
    DecisionStage<Boolean> additionalSpecificationsDecision = defineDecisionStage("technologyNonExemptControlsAdditionalSpecificationsDecision", additionalSpecificationsDecider);

    DecisionStage<Boolean> decontrolsDecision = defineDecisionStage("technologyNonExemptControlsDecontrolsDecision", decontrolsDecider);

    DecisionStage<Boolean> technicalNotesDecision = defineDecisionStage("technologyNonExemptControlsTechnicalNotesDecision", technicalNotesDecider);

    /** Software control journey stage transitions */
    bindControlCodeListStageJourneyTransitions(
        controlsList,
        decontrolsDecision,
        technologyExemptionsNLR
    );

    bindControlCodeStageTransitions(
        decontrols,
        decontrolsApply,
        controlCodeSummary,
        controlCodeNotApplicable,
        additionalSpecifications,
        technicalNotes,
        destinationCountries,
        decontrolsDecision,
        additionalSpecificationsDecision,
        technicalNotesDecision
    );

    bindControlCodeNotApplicableFromListStageJourneyTransitions(
        controlCodeNotApplicable,
        controlsList,
        technologyExemptionsNLR
      );

    atStage(decontrolsApply)
        .onEvent(Events.BACK)
        .branch()
        .when(BackType.MATCHES, backTo(controlsList))
        .when(BackType.EXPORT_CATEGORY, backTo(exportCategory));
  }

  /**
   * Software/Technology controls per software category
   */
  private void softTechCategoryControls(GoodsType goodsType, JourneyStage controlsList, JourneyStage controlCodeSummary, JourneyStage relatedToEquipmentOrMaterials, JourneyStage dualUseCategories) {
    String goodsTypeText = goodsType.value().toLowerCase();

    /** Software/Technology controls journey stages */
    JourneyStage controlCodeNotApplicable = defineStage(goodsTypeText + "CategoryControlCodeNotApplicable", "Description not applicable",
        controllers.controlcode.routes.NotApplicableController.renderForm(ControlCodeVariant.CONTROLS.urlString(), goodsType.urlString()));

    JourneyStage additionalSpecifications = defineStage(goodsTypeText + "CategoryAdditionalSpecifications", "Additional specifications",
        controllers.controlcode.routes.AdditionalSpecificationsController.renderForm(ControlCodeVariant.CONTROLS.urlString(), goodsType.urlString()));

    JourneyStage decontrols = defineStage(goodsTypeText + "CategoryDecontrols", "Decontrols",
        controllers.controlcode.routes.DecontrolsController.renderForm(ControlCodeVariant.CONTROLS.urlString(), goodsType.urlString()));

    JourneyStage decontrolsApply = defineStage(goodsTypeText + "CategoryDecontrolsApply", "Choose a different item type",
        controllers.controlcode.routes.DecontrolsApplyController.renderForm(ControlCodeVariant.CONTROLS.urlString(), goodsType.urlString()));

    JourneyStage technicalNotes = defineStage(goodsTypeText + "CategoryTechnicalNotes", "Technical notes",
        controllers.controlcode.routes.TechnicalNotesController.renderForm(ControlCodeVariant.CONTROLS.urlString(), goodsType.urlString()));

    /** Software/Technology controls decision stages */
    DecisionStage<Boolean> additionalSpecificationsDecision = defineDecisionStage(goodsTypeText + "CategoryAdditionalSpecsDecision", additionalSpecificationsDecider);

    DecisionStage<Boolean> decontrolsDecision = defineDecisionStage(goodsTypeText + "CategoryDecontrolsDecision", decontrolsDecider);

    DecisionStage<Boolean> technicalNotesDecision = defineDecisionStage(goodsTypeText + "CategoryTechnicalNotesDecision", technicalNotesDecider);

    /** Software/Technology control journey stage transitions */
    bindControlCodeListStageJourneyTransitions(
        controlsList,
        decontrolsDecision,
        relatedToEquipmentOrMaterials
    );

    bindControlCodeStageTransitions(
        decontrols,
        decontrolsApply,
        controlCodeSummary,
        controlCodeNotApplicable,
        additionalSpecifications,
        technicalNotes,
        destinationCountries,
        decontrolsDecision,
        additionalSpecificationsDecision,
        technicalNotesDecision
    );

    bindControlCodeNotApplicableFromListStageJourneyTransitions(
        controlCodeNotApplicable,
        controlsList,
        relatedToEquipmentOrMaterials
    );

    atStage(decontrolsApply)
        .onEvent(Events.BACK)
        .branch()
        .when(BackType.MATCHES, backTo(controlsList))
        .when(BackType.SOFT_TECH_CATEGORY, backTo(dualUseCategories))
        .when(BackType.EXPORT_CATEGORY, backTo(exportCategory));
  }

  /**
   * Search related to software/technology journey
   */
  private void softTechSearchRelatedTo(GoodsType goodsType, JourneyStage searchRelatedTo, DecisionStage<Boolean> searchRelatedCodesDecision, DecisionStage<ApplicableSoftTechControls> applicableCatchallControlsDecision, DecisionStage<ApplicableSoftTechControls> applicableRelatedControlsDecision) {
    String goodsTypeText = goodsType.value().toLowerCase();

    /** Search related to software/technology journey stages */
    JourneyStage searchResults = defineStage(goodsTypeText + "SearchResultsRelatedTo", "Possible matches",
        controllers.search.routes.SearchResultsController.renderForm(goodsType.urlString()));

    JourneyStage searchRelatedCodes = defineStage(goodsTypeText + "SearchRelatedCodesRelatedTo", "Related to your item",
        controllers.search.routes.SearchRelatedCodesController.renderForm(goodsType.urlString()));

    JourneyStage controlCodeSummary = defineStage(goodsTypeText + "ControlCodeSummaryRelatedTo", "Summary",
        controllers.controlcode.routes.ControlCodeSummaryController.renderForm(ControlCodeVariant.SEARCH.urlString(), goodsType.urlString()));

    JourneyStage controlCodeNotApplicable = defineStage(goodsTypeText + "ControlCodeNotApplicableRelatedTo", "Description not applicable",
        controllers.controlcode.routes.NotApplicableController.renderForm(ControlCodeVariant.SEARCH.urlString(), goodsType.urlString()));

    JourneyStage additionalSpecifications = defineStage(goodsTypeText + "AdditionalSpecificationsRelatedTo", "Additional specifications",
        controllers.controlcode.routes.AdditionalSpecificationsController.renderForm(ControlCodeVariant.SEARCH.urlString(), goodsType.urlString()));

    JourneyStage decontrols = defineStage(goodsTypeText + "DecontrolsRelatedTo", "Decontrols",
        controllers.controlcode.routes.DecontrolsController.renderForm(ControlCodeVariant.SEARCH.urlString(), goodsType.urlString()));

    JourneyStage decontrolsApply = defineStage(goodsTypeText + "DecontrolsApplyRelatedTo", "Choose a different item type",
        controllers.controlcode.routes.DecontrolsApplyController.renderForm(ControlCodeVariant.SEARCH.urlString(), goodsType.urlString()));

    JourneyStage technicalNotes = defineStage(goodsTypeText + "TechnicalNotesRelatedTo", "Technical notes",
        controllers.controlcode.routes.TechnicalNotesController.renderForm(ControlCodeVariant.SEARCH.urlString(), goodsType.urlString()));

    /** Search related to software/technology decision stages */
    DecisionStage<Boolean> additionalSpecsDecision = defineDecisionStage(goodsTypeText + "AdditionalSpecsDecisionRelatedTo", additionalSpecificationsDecider);

    DecisionStage<Boolean> decontrolsDecision = defineDecisionStage(goodsTypeText + "DecontrolsDecisionRelatedTo", decontrolsDecider);

    DecisionStage<Boolean> technicalNotesDecision = defineDecisionStage(goodsTypeText + "TechnicalNotesDecisionRelatedTo", technicalNotesDecider);

    /** Search related to software/technology journey stage transitions */
    atStage(searchRelatedTo)
        .onEvent(Events.SEARCH_PHYSICAL_GOODS)
        .then(moveTo(searchResults));

    atStage(searchResults)
        .onEvent(Events.CONTROL_CODE_SELECTED)
        .then(moveTo(searchRelatedCodesDecision));

    atStage(searchResults)
        .onEvent(Events.BACK)
        .branch()
        .when(BackType.SEARCH, backTo(searchRelatedTo));

    atStage(searchResults)
        .onEvent(StandardEvents.NEXT)
        .then(moveTo(applicableCatchallControlsDecision));

    atStage(searchResults)
        .onEvent(Events.NONE_MATCHED)
        .then(moveTo(applicableCatchallControlsDecision));

    atDecisionStage(searchRelatedCodesDecision)
        .decide()
        .when(true, moveTo(searchRelatedCodes))
        .when(false, moveTo(decontrolsDecision));

    atStage(searchRelatedCodes)
        .onEvent(Events.CONTROL_CODE_SELECTED)
        .then(moveTo(decontrolsDecision));

    atStage(searchRelatedCodes)
        .onEvent(Events.BACK)
        .branch()
        .when(BackType.RESULTS, backTo(searchResults));

    bindControlCodeStageTransitions(
        decontrols,
        decontrolsApply,
        controlCodeSummary,
        controlCodeNotApplicable,
        additionalSpecifications,
        technicalNotes,
        applicableRelatedControlsDecision,
        decontrolsDecision, additionalSpecsDecision,
        technicalNotesDecision
    );

    bindControlCodeNotApplicableFromSearchStageJourneyTransitions(
        controlCodeNotApplicable,
        searchRelatedTo,
        searchResults
    );

    atStage(decontrolsApply)
        .onEvent(Events.BACK)
        .branch()
        .when(BackType.SEARCH, backTo(searchRelatedTo))
        .when(BackType.RESULTS, backTo(searchResults))
        .when(BackType.EXPORT_CATEGORY, backTo(exportCategory));
  }


  /**
   * Software/Technology controls for a selected physical good
   */
  private void softTechControlsRelatedToAPhysicalGood(GoodsType goodsType, JourneyStage relatedToPhysicalGoodsControlsList, JourneyStage relatedToPhysicalGoodsControlCodeSummary, DecisionStage<ApplicableSoftTechControls> applicableCatchallControlsDecision) {
    String goodsTypeText = goodsType.value().toLowerCase();

    /** Software/Technology controls related to physical goods  */
    JourneyStage controlCodeNotApplicable = defineStage(goodsTypeText + "ControlsRelatedToPhysicalGoodsControlCodeNotApplicable", "Description not applicable",
        controllers.controlcode.routes.NotApplicableController.renderForm(ControlCodeVariant.CONTROLS_RELATED_TO_A_PHYSICAL_GOOD.urlString(), goodsType.urlString()));

    JourneyStage additionalSpecifications = defineStage(goodsTypeText + "ControlsRelatedToPhysicalGoodsAdditionalSpecifications", "Additional specifications",
        controllers.controlcode.routes.AdditionalSpecificationsController.renderForm(ControlCodeVariant.CONTROLS_RELATED_TO_A_PHYSICAL_GOOD.urlString(), goodsType.urlString()));

    JourneyStage decontrols = defineStage(goodsTypeText + "ControlsRelatedToPhysicalGoodsDecontrols", "Decontrols",
        controllers.controlcode.routes.DecontrolsController.renderForm(ControlCodeVariant.CONTROLS_RELATED_TO_A_PHYSICAL_GOOD.urlString(), goodsType.urlString()));

    JourneyStage decontrolsApply = defineStage(goodsTypeText + "ControlsRelatedToPhysicalGoodsDecontrolsApply", "Choose a different item type",
        controllers.controlcode.routes.DecontrolsApplyController.renderForm(ControlCodeVariant.CONTROLS_RELATED_TO_A_PHYSICAL_GOOD.urlString(), goodsType.urlString()));

    JourneyStage technicalNotes = defineStage(goodsTypeText + "ControlsRelatedToPhysicalGoodsTechnicalNotes", "Technical notes",
        controllers.controlcode.routes.TechnicalNotesController.renderForm(ControlCodeVariant.CONTROLS_RELATED_TO_A_PHYSICAL_GOOD.urlString(), goodsType.urlString()));

    /** Software/Technology controls related to physical goods decision stages */
    DecisionStage<Boolean> additionalSpecificationsDecision = defineDecisionStage(goodsTypeText + "ControlsRelatedToPhysicalGoodsAdditionalSpecificationsDecision", additionalSpecificationsDecider);

    DecisionStage<Boolean> decontrolsDecision = defineDecisionStage(goodsTypeText + "ControlsRelatedToPhysicalGoodsDecontrolsDecision", decontrolsDecider);

    DecisionStage<Boolean> technicalNotesDecision = defineDecisionStage(goodsTypeText + "ControlsRelatedToPhysicalGoodsTechnicalNotesDecision", technicalNotesDecider);

    /** Software/Technology control journey stage transitions */
    bindControlCodeListStageJourneyTransitions(
        relatedToPhysicalGoodsControlsList,
        decontrolsDecision,
        applicableCatchallControlsDecision
    );

    bindControlCodeStageTransitions(
        decontrols,
        decontrolsApply,
        relatedToPhysicalGoodsControlCodeSummary,
        controlCodeNotApplicable,
        additionalSpecifications,
        technicalNotes,
        destinationCountries,
        decontrolsDecision, additionalSpecificationsDecision,
        technicalNotesDecision
    );

    bindControlCodeNotApplicableFromListStageJourneyTransitions(
        controlCodeNotApplicable,
        relatedToPhysicalGoodsControlsList,
        applicableCatchallControlsDecision
    );

    atStage(relatedToPhysicalGoodsControlsList)
        .onEvent(Events.BACK)
        .branch()
        .when(BackType.MATCHES, backTo(relatedToPhysicalGoodsControlsList))
        .when(BackType.EXPORT_CATEGORY, backTo(exportCategory));
  }

  /**
   * Software/Technology catchall controls
   */
  private void softTechCatchallControls(GoodsType goodsType, JourneyStage controlsList, JourneyStage controlCodeSummary, DecisionStage<Boolean> relationshipWithSoftTechExists) {
    String goodsTypeText = goodsType.value().toLowerCase();

    /** Software/Technology catchall controls journey stages */
    JourneyStage controlCodeNotApplicable = defineStage(goodsTypeText + "ControlCodeNotApplicable", "Description not applicable",
        controllers.controlcode.routes.NotApplicableController.renderForm(ControlCodeVariant.CATCHALL_CONTROLS.urlString(), goodsType.urlString()));

    JourneyStage additionalSpecification = defineStage(goodsTypeText + "AdditionalSpecification", "Additional specifications",
        controllers.controlcode.routes.AdditionalSpecificationsController.renderForm(ControlCodeVariant.CATCHALL_CONTROLS.urlString(), goodsType.urlString()));

    JourneyStage decontrols = defineStage(goodsTypeText + "Decontrols", "Decontrols",
        controllers.controlcode.routes.DecontrolsController.renderForm(ControlCodeVariant.CATCHALL_CONTROLS.urlString(), goodsType.urlString()));

    JourneyStage decontrolsApply = defineStage(goodsTypeText + "DecontrolsApply", "Choose a different item type",
        controllers.controlcode.routes.DecontrolsApplyController.renderForm(ControlCodeVariant.CATCHALL_CONTROLS.urlString(), goodsType.urlString()));

    JourneyStage technicalNotes = defineStage(goodsTypeText + "TechnicalNotes", "Technical notes",
        controllers.controlcode.routes.TechnicalNotesController.renderForm(ControlCodeVariant.CATCHALL_CONTROLS.urlString(), goodsType.urlString()));

    /** Software/Technology catchall controls decision stages */
    DecisionStage<Boolean> additionalSpecificationsDecision = defineDecisionStage(goodsTypeText + "AdditionalSpecificationsDecision", additionalSpecificationsDecider);

    DecisionStage<Boolean> decontrolsDecision = defineDecisionStage(goodsTypeText + "DecontrolsDecision", decontrolsDecider);

    DecisionStage<Boolean> technicalNotesDecision = defineDecisionStage(goodsTypeText + "TechnicalNotesDecision", technicalNotesDecider);

    /** Software/Technology catchall controls stage transitions */
    bindControlCodeListStageJourneyTransitions(
        controlsList,
        decontrolsDecision,
        relationshipWithSoftTechExists
    );

    bindControlCodeStageTransitions(
        decontrols,
        decontrolsApply,
        controlCodeSummary,
        controlCodeNotApplicable,
        additionalSpecification,
        technicalNotes,
        destinationCountries,
        decontrolsDecision, additionalSpecificationsDecision,
        technicalNotesDecision
    );

    bindControlCodeNotApplicableFromListStageJourneyTransitions(
        controlCodeNotApplicable,
        controlsList,
        relationshipWithSoftTechExists
    );

    atStage(decontrolsApply)
        .onEvent(Events.BACK)
        .branch()
        .when(BackType.MATCHES, backTo(controlsList))
        .when(BackType.EXPORT_CATEGORY, backTo(exportCategory));
  }

  private void bindControlCodeNotApplicableFromSearchStageJourneyTransitions(JourneyStage controlCodeNotApplicableStage,
                                                                             JourneyStage searchStage,
                                                                             JourneyStage searchResultsStage) {
    atStage(controlCodeNotApplicableStage)
        .onEvent(Events.BACK)
        .branch()
        .when(BackType.RESULTS, backTo(searchResultsStage))
        .when(BackType.SEARCH, backTo(searchStage));
  }

  private void bindControlCodeNotApplicableFromListStageJourneyTransitions(JourneyStage controlCodeNotApplicableStage,
                                                                           JourneyStage backToMatchesStage,
                                                                           CommonStage continueStage) {
    atStage(controlCodeNotApplicableStage)
        .onEvent(Events.BACK)
        .branch()
        .when(BackType.MATCHES, backTo(backToMatchesStage));

    atStage(controlCodeNotApplicableStage)
        .onEvent(StandardEvents.NEXT)
        .then(moveTo(continueStage));
  }

  private void bindControlCodeStageTransitions(JourneyStage decontrolsStage,
                                               JourneyStage decontrolsApplyStage,
                                               JourneyStage controlCodeSummaryStage,
                                               JourneyStage controlCodeNotApplicableStage,
                                               JourneyStage additionalSpecificationsStage,
                                               JourneyStage technicalNotesStage,
                                               CommonStage exitStage,
                                               DecisionStage<Boolean> decontrolsDecisionStage,
                                               DecisionStage<Boolean> additionalSpecificationsDecisionStage,
                                               DecisionStage<Boolean> technicalNotesDecisionStage) {
    atDecisionStage(decontrolsDecisionStage)
        .decide()
        .when(true, moveTo(decontrolsStage))
        .when(false, moveTo(controlCodeSummaryStage));

    atStage(decontrolsStage)
        .onEvent(StandardEvents.NEXT)
        .then(moveTo(controlCodeSummaryStage));

    atStage(decontrolsStage)
        .onEvent(Events.CONTROL_CODE_NOT_APPLICABLE)
        .then(moveTo(decontrolsApplyStage));

    atStage(controlCodeSummaryStage)
        .onEvent(StandardEvents.NEXT)
        .then(moveTo(additionalSpecificationsDecisionStage));

    atStage(controlCodeSummaryStage)
        .onEvent(Events.CONTROL_CODE_NOT_APPLICABLE)
        .then(moveTo(controlCodeNotApplicableStage));

    atStage(additionalSpecificationsStage)
        .onEvent(StandardEvents.NEXT)
        .then(moveTo(technicalNotesDecisionStage));

    atStage(additionalSpecificationsStage)
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
        .when(false, moveTo(technicalNotesDecisionStage));

    atDecisionStage(technicalNotesDecisionStage)
        .decide()
        .when(true, moveTo(technicalNotesStage))
        .when(false, moveTo(exitStage));
  }

  private void bindControlCodeListStageJourneyTransitions(JourneyStage controlCodeListStage,
                                                          CommonStage controlCodeSummaryStage,
                                                          CommonStage controlCodeNoneMatchedStage) {
    atStage(controlCodeListStage)
        .onEvent(Events.CONTROL_CODE_SELECTED)
        .then(moveTo(controlCodeSummaryStage));

    atStage(controlCodeListStage)
        .onEvent(Events.NONE_MATCHED)
        .then(moveTo(controlCodeNoneMatchedStage));
  }

  private void bindYesNoJourneyTransition(JourneyStage currentStage, CommonStage yesStage, CommonStage noStage) {
    atStage(currentStage)
        .onEvent(StandardEvents.YES)
        .then(moveTo(yesStage));

    atStage(currentStage)
        .onEvent(StandardEvents.NO)
        .then(moveTo(noStage));
  }
}
