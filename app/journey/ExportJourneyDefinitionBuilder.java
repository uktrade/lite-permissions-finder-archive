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
import journey.deciders.relatedcodes.CatchallRelatedControlsDecider;
import journey.deciders.relatedcodes.CategoryRelatedControlsDecider;
import journey.deciders.relatedcodes.NonExemptRelatedControlsDecider;
import journey.deciders.relatedcodes.RelatedRelatedControlsDecider;
import journey.deciders.relatedcodes.SearchRelatedControlsDecider;
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

  private final JourneyStage exportCategory = defineStage("exportCategory",
      controllers.categories.routes.ExportCategoryController.renderForm());
  private final JourneyStage goodsType = defineStage("goodsType",
      routes.GoodsTypeController.renderForm());
  private final JourneyStage destinationCountries = defineStage("destinationCountries",
      routes.DestinationCountryController.renderForm());
  private final JourneyStage ogelQuestions = defineStage("ogelQuestions",
      controllers.ogel.routes.OgelQuestionsController.renderForm());
  private final JourneyStage ogelNotApplicable = defineStage("ogelNotApplicable",
      controllers.ogel.routes.OgelNotApplicableController.renderForm());
  private final JourneyStage notImplemented = defineStage("notImplemented",
      routes.StaticContentController.renderNotImplemented());
  private final JourneyStage notApplicable = defineStage("notApplicable",
      routes.StaticContentController.renderNotApplicable());

  /** Physical **/
  private final JourneyStage search = defineStage("search",
      controllers.search.routes.SearchController.renderForm(GoodsType.PHYSICAL.urlString()));
  private final DecisionStage<Boolean> hasSearchRelatedCodes;

  /** Software **/
  private final DecisionStage<ExportCategory> isDualUseOrMilitarySoftware;
  private final DecisionStage<ApplicableSoftTechControls> hasSoftwareApplicableControls;
  private final DecisionStage<ApplicableSoftTechControls> hasSoftwareApplicableRelatedControls;
  private final DecisionStage<ApplicableSoftTechControls> hasSoftwareApplicableCatchallControls;
  private final DecisionStage<Boolean> doesSoftwareRelationshipWithTechnologyExists;
  private final DecisionStage<Boolean> doesSoftwareRelationshipWithSoftwareExists;
  private final DecisionStage<Boolean> hasSoftwareSearchRelatedControls;
  private final DecisionStage<Boolean> hasSoftwareCategoryRelatedControls;
  private final DecisionStage<Boolean> hasSoftwareCatchallRelatedControls;
  private final DecisionStage<Boolean> hasSoftwareRelatedControlsRelatedControls;

  private JourneyStage softwareJourneyEndNLR = defineStage("softwareJourneyEndNLR",
      routes.StaticContentController.renderSoftwareJourneyEndNLR());

  /** Technology **/
  private final DecisionStage<ExportCategory> isDualUseOrMilitaryTechnology;
  private final DecisionStage<ExportCategory> isDualUseOrMilitaryNonExemptTechnology;
  private final DecisionStage<ApplicableSoftTechControls> hasTechnologyApplicableControls;
  private final DecisionStage<ApplicableSoftTechControls> hasTechnologyApplicableRelatedControls;
  private final DecisionStage<ApplicableSoftTechControls> hasTechnologyApplicableCatchallControls;
  private final DecisionStage<Boolean> doesTechnologyRelationshipWithSoftwareExists;
  private final DecisionStage<Boolean> hasTechnologySearchRelatedControls;
  private final DecisionStage<Boolean> hasTechnologyNonExemptRelatedControls;
  private final DecisionStage<Boolean> hasTechnologyCategoryRelatedControls;
  private final DecisionStage<Boolean> hasTechnologyCatchallRelatedControls;
  private final DecisionStage<Boolean> hasTechnologyRelatedControlsRelatedControls;

  private final JourneyStage technologyExemptions = defineStage("technologyExemptions",
      controllers.softtech.routes.TechnologyExemptionsController.renderForm());

  private final JourneyStage technologyExemptionsNLR = defineStage("technologyExemptionsNLR",
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
  private final SearchRelatedControlsDecider searchRelatedControlsDecider;
  private final CatchallRelatedControlsDecider catchallRelatedControlsDecider;
  private final CategoryRelatedControlsDecider categoryRelatedControlsDecider;
  private final RelatedRelatedControlsDecider relatedRelatedControlsDecider;
  private final NonExemptRelatedControlsDecider nonExemptRelatedControlsDecider;

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
                                        SearchRelatedControlsDecider searchRelatedControlsDecider,
                                        CatchallRelatedControlsDecider catchallRelatedControlsDecider,
                                        CategoryRelatedControlsDecider categoryRelatedControlsDecider,
                                        RelatedRelatedControlsDecider relatedRelatedControlsDecider,
                                        NonExemptRelatedControlsDecider nonExemptRelatedControlsDecider) {
    this.additionalSpecificationsDecider = additionalSpecificationsDecider;
    this.decontrolsDecider = decontrolsDecider;
    this.technicalNotesDecider = technicalNotesDecider;
    this.exportCategoryDecider = exportCategoryDecider;
    this.categoryControlsDecider = categoryControlsDecider;
    this.relatedControlsDecider = relatedControlsDecider;
    this.catchallControlsDecider = catchallControlsDecider;
    this.relationshipWithTechnologyDecider = relationshipWithTechnologyDecider;
    this.relationshipWithSoftwareDecider = relationshipWithSoftwareDecider;
    this.searchRelatedControlsDecider = searchRelatedControlsDecider;
    this.catchallRelatedControlsDecider = catchallRelatedControlsDecider;
    this.categoryRelatedControlsDecider = categoryRelatedControlsDecider;
    this.relatedRelatedControlsDecider = relatedRelatedControlsDecider;
    this.nonExemptRelatedControlsDecider = nonExemptRelatedControlsDecider;
    this.isDualUseOrMilitarySoftware = defineDecisionStage("isDualUseOrMilitarySoftware", this.exportCategoryDecider);
    this.isDualUseOrMilitaryTechnology = defineDecisionStage("isDualUseOrMilitaryTechnology", this.exportCategoryDecider);
    this.isDualUseOrMilitaryNonExemptTechnology = defineDecisionStage("isDualUseOrMilitaryNonExemptTechnology", this.exportCategoryDecider);
    this.hasSoftwareApplicableControls = defineDecisionStage("hasSoftwareApplicableControls", this.categoryControlsDecider);
    this.hasTechnologyApplicableControls = defineDecisionStage("hasTechnologyApplicableControls", this.categoryControlsDecider);
    this.hasSoftwareApplicableRelatedControls = defineDecisionStage("hasSoftwareApplicableRelatedControls", this.relatedControlsDecider);
    this.hasTechnologyApplicableRelatedControls = defineDecisionStage("hasTechnologyApplicableRelatedControls", this.relatedControlsDecider);
    this.hasSoftwareApplicableCatchallControls = defineDecisionStage("hasSoftwareApplicableCatchallControls", this.catchallControlsDecider);
    this.hasTechnologyApplicableCatchallControls = defineDecisionStage("hasTechnologyApplicableCatchallControls", this.catchallControlsDecider);
    this.doesSoftwareRelationshipWithTechnologyExists = defineDecisionStage("doesSoftwareRelationshipWithTechnologyExists", this.relationshipWithTechnologyDecider);
    this.doesSoftwareRelationshipWithSoftwareExists = defineDecisionStage("doesSoftwareRelationshipWithSoftwareExists", this.relationshipWithSoftwareDecider);
    this.doesTechnologyRelationshipWithSoftwareExists = defineDecisionStage("doesTechnologyRelationshipWithSoftwareExists", this.relationshipWithSoftwareDecider);
    this.hasSearchRelatedCodes = defineDecisionStage("hasSearchRelatedCodes", this.searchRelatedControlsDecider);
    this.hasSoftwareSearchRelatedControls = defineDecisionStage("hasSoftwareSearchRelatedControls", this.searchRelatedControlsDecider);
    this.hasSoftwareCatchallRelatedControls = defineDecisionStage("hasSoftwareCatchallRelatedControls", this.catchallRelatedControlsDecider);
    this.hasSoftwareCategoryRelatedControls = defineDecisionStage("hasSoftwareCategoryRelatedControls", this.categoryRelatedControlsDecider);
    this.hasSoftwareRelatedControlsRelatedControls = defineDecisionStage("hasSoftwareRelatedControlsRelatedControls", this.relatedRelatedControlsDecider);
    this.hasTechnologySearchRelatedControls = defineDecisionStage("hasTechnologySearchRelatedControls", this.searchRelatedControlsDecider);
    this.hasTechnologyCatchallRelatedControls = defineDecisionStage("hasTechnologyCatchallRelatedControls", this.catchallRelatedControlsDecider);
    this.hasTechnologyCategoryRelatedControls = defineDecisionStage("hasTechnologyCategoryRelatedControls", this.categoryRelatedControlsDecider);
    this.hasTechnologyRelatedControlsRelatedControls = defineDecisionStage("hasTechnologyRelatedControlsRelatedControls", this.relatedRelatedControlsDecider);
    this.hasTechnologyNonExemptRelatedControls = defineDecisionStage("hasTechnologyNonExemptRelatedControls", this.nonExemptRelatedControlsDecider);
  }

  @Override
  protected void journeys() {
    // *** Stages/transitions ***

    goodsCategoryStages();

    atStage(goodsType)
        .onEvent(Events.GOODS_TYPE_SELECTED)
        .branch()
        .when(GoodsType.PHYSICAL, moveTo(search))
        .when(GoodsType.SOFTWARE, moveTo(isDualUseOrMilitarySoftware))
        .when(GoodsType.TECHNOLOGY, moveTo(technologyExemptions));

    physicalGoodsStages();

    softwareStages();

    technologyStages();

    // *** Journeys ***

    defineJourney(JourneyDefinitionNames.EXPORT, goodsType,
        BackLink.to(routes.GoodsTypeController.renderForm(), "Back"));
    defineJourney(JourneyDefinitionNames.CHANGE_CONTROL_CODE, search,
        BackLink.to(routes.SummaryController.renderForm(), "Back"));
    defineJourney(JourneyDefinitionNames.CHANGE_DESTINATION_COUNTRIES, destinationCountries,
        BackLink.to(routes.SummaryController.renderForm(), "Back"));
    defineJourney(JourneyDefinitionNames.CHANGE_OGEL_TYPE, ogelQuestions,
        BackLink.to(routes.SummaryController.renderForm(), "Back"));
  }

  private void goodsCategoryStages() {

    JourneyStage categoryArtsCultural = defineStage("categoryArtsCultural",
        controllers.categories.routes.ArtsCulturalController.renderForm());

    JourneyStage categoryArtsCulturalHistoric = defineStage("categoryArtsCulturalHistoric",
        routes.StaticContentController.renderCategoryArtsCulturalHistoric());

    JourneyStage categoryArtsCulturalNonHistoric = defineStage("categoryArtsCulturalNonHistoric",
        routes.StaticContentController.renderCategoryArtsCulturalNonHistoric());

    JourneyStage categoryArtsCulturalFirearmHistoric = defineStage("categoryArtsCulturalFirearmHistoric",
        controllers.categories.routes.ArtsCulturalFirearmHistoricController.renderForm());

    JourneyStage categoryChemicalsCosmetics = defineStage("categoryChemicalsCosmetics",
        controllers.categories.routes.ChemicalsCosmeticsController.renderForm());

    JourneyStage categoryDualUse = defineStage("categoryDualUse",
        controllers.categories.routes.DualUseController.renderForm());

    JourneyStage categoryFinancialTechnicalAssistance = defineStage("categoryFinancialTechnicalAssistance",
        controllers.categories.routes.FinancialTechnicalAssistanceController.renderForm());

    JourneyStage categoryFoodStatic = defineStage("categoryFood",
        routes.StaticContentController.renderCategoryFood());

    JourneyStage categoryMedicinesDrugs = defineStage("categoryMedicinesDrugs",
        controllers.categories.routes.MedicinesDrugsController.renderForm());

    JourneyStage categoryNonMilitaryTakeYourself = defineStage(NonMilitaryController.TAKE_YOURSELF_KEY,
        controllers.categories.routes.NonMilitaryController.renderTakeYourselfForm());
    JourneyStage categoryNonMilitaryPersonalEffects = defineStage(NonMilitaryController.PERSONAL_EFFECTS_KEY,
        controllers.categories.routes.NonMilitaryController.renderPersonalEffectsForm());

    JourneyStage categoryNonMilitaryTakingStatic = defineStage("categoryNonMilitaryTaking", routes.StaticContentController.renderCategoryNonMilitaryTaking());
    JourneyStage categoryNonMilitarySendingStatic = defineStage("categoryNonMilitarySending", routes.StaticContentController.renderCategoryNonMilitarySending());
    JourneyStage categoryNonMilitaryNeedLicenceStatic = defineStage("categoryNonMilitaryNeedLicence", routes.StaticContentController.renderCategoryNonMilitaryNeedLicence());

    JourneyStage categoryPlantsAnimals = defineStage("categoryPlantsAnimals",
        controllers.categories.routes.PlantsAnimalsController.renderForm());

    JourneyStage categoryEndangeredAnimalStatic = defineStage("categoryEndangeredAnimal",
        routes.StaticContentController.renderCategoryEndangeredAnimals());

    JourneyStage categoryNonEndangeredAnimalStatic = defineStage("categoryNonEndangeredAnimal",
        routes.StaticContentController.renderCategoryNonEndangeredAnimals());

    JourneyStage categoryPlantStatic = defineStage("categoryPlant",
        routes.StaticContentController.renderCategoryPlants());

    JourneyStage categoryMedicinesDrugsStatic = defineStage("categoryMedicinesDrugsStatic",
        routes.StaticContentController.renderCategoryMedicinesDrugs());

    JourneyStage categoryTortureRestraint = defineStage("categoryTortureRestraint",
        controllers.categories.routes.TortureRestraintController.renderForm());

    JourneyStage categoryRadioactive = defineStage("categoryRadioactive",
        controllers.categories.routes.RadioactiveController.renderForm());

    JourneyStage categoryWaste = defineStage("categoryWaste",
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
        .then(moveTo(technologyExemptions)); // Move to technology flow

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

    JourneyStage searchResults = defineStage("searchResults",
        controllers.search.routes.SearchResultsController.renderForm(GoodsType.PHYSICAL.urlString()));

    JourneyStage searchRelatedCodes = defineStage("searchRelatedCodes",
        controllers.search.routes.SearchRelatedCodesController.renderForm(GoodsType.PHYSICAL.urlString()));

    JourneyStage controlCodeSummary = defineStage("controlCodeSummary",
        controllers.controlcode.routes.ControlCodeSummaryController.renderForm(ControlCodeVariant.SEARCH.urlString(), GoodsType.PHYSICAL.urlString()));

    JourneyStage controlCodeNotApplicable = defineStage("controlCodeNotApplicable",
        controllers.controlcode.routes.NotApplicableController.renderForm(ControlCodeVariant.SEARCH.urlString(), GoodsType.PHYSICAL.urlString()));

    JourneyStage additionalSpecifications = defineStage("additionalSpecifications",
        controllers.controlcode.routes.AdditionalSpecificationsController.renderForm(ControlCodeVariant.SEARCH.urlString(), GoodsType.PHYSICAL.urlString()));

    JourneyStage decontrols = defineStage("decontrols",
        controllers.controlcode.routes.DecontrolsController.renderForm(ControlCodeVariant.SEARCH.urlString(), GoodsType.PHYSICAL.urlString()));

    JourneyStage decontrolsApply = defineStage("decontrolsApply",
        controllers.controlcode.routes.DecontrolsApplyController.renderForm(ControlCodeVariant.SEARCH.urlString(), GoodsType.PHYSICAL.urlString()));

    JourneyStage technicalNotes = defineStage("technicalNotes",
        controllers.controlcode.routes.TechnicalNotesController.renderForm(ControlCodeVariant.SEARCH.urlString(), GoodsType.PHYSICAL.urlString()));

    JourneyStage ogelResults = defineStage("ogelResults",
        controllers.ogel.routes.OgelResultsController.renderForm());

    JourneyStage ogelConditions = defineStage("ogelConditions",
        controllers.ogel.routes.OgelConditionsController.renderForm());

    JourneyStage virtualEU = defineStage("virtualEU",
        routes.StaticContentController.renderVirtualEU());

    JourneyStage ogelSummary = defineStage("ogelSummary",
        controllers.ogel.routes.OgelSummaryController.renderForm());

    DecisionStage<Boolean> additionalSpecsDecision = defineDecisionStage("hasAdditionalSpecs", additionalSpecificationsDecider);

    DecisionStage<Boolean> decontrolsDecision = defineDecisionStage("hasDecontrols", decontrolsDecider);

    DecisionStage<Boolean> technicalNotesDecision = defineDecisionStage("hasTechNotes", technicalNotesDecider);

    atStage(search)
        .onEvent(Events.SEARCH_PHYSICAL_GOODS)
        .then(moveTo(searchResults));

    atStage(searchResults)
        .onEvent(Events.CONTROL_CODE_SELECTED)
        .then(moveTo(hasSearchRelatedCodes));

    atStage(searchResults)
        .onEvent(Events.NONE_MATCHED)
        .then(moveTo(notApplicable));

    atDecisionStage(hasSearchRelatedCodes)
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

    JourneyStage softwareExemptionsQ1 = defineStage("softwareExemptionsQ1",
        controllers.softtech.routes.SoftwareExemptionsController.renderFormQ1());

    JourneyStage softwareExemptionsQ2 = defineStage("softwareExemptionsQ2",
        controllers.softtech.routes.SoftwareExemptionsController.renderFormQ2());

    JourneyStage softwareExemptionsQ3 = defineStage("softwareExemptionsQ3",
        controllers.softtech.routes.SoftwareExemptionsController.renderFormQ3());

    JourneyStage softwareExemptionsNLR1 = defineStage("softwareExemptionsNLR1",
        controllers.routes.StaticContentController.renderSoftwareExemptionsNLR1());

    JourneyStage softwareExemptionsNLR2 = defineStage("softwareExemptionsNLR2",
        controllers.routes.StaticContentController.renderSoftwareExemptionsNLR2());

    JourneyStage relatedToEquipmentOrMaterials = defineStage("softwareRelatedToEquipmentOrMaterials",
        controllers.softtech.routes.RelatedEquipmentController.renderForm(GoodsType.SOFTWARE.urlString()));

    JourneyStage dualUseCategories = defineStage("softwareDualUseCategories",
        controllers.softtech.routes.DualUseSoftTechCategoriesController.renderForm(GoodsType.SOFTWARE.urlString()));

    JourneyStage categoryControlsList = defineStage("softwareCategoryControlsList",
        controllers.softtech.controls.routes.SoftTechControlsController.renderForm(ControlCodeVariant.CONTROLS.urlString(), GoodsType.SOFTWARE.urlString()));

    JourneyStage categoryRelatedControlsList = defineStage("softwareCategoryRelatedControlsList",
        controllers.softtech.controls.routes.SoftTechRelatedControlsController.renderForm(ControlCodeVariant.CONTROLS.urlString(), GoodsType.SOFTWARE.urlString()));

    JourneyStage catchallControlsList = defineStage("softwareCatchallControlsList",
        controllers.softtech.controls.routes.SoftTechControlsController.renderForm(ControlCodeVariant.CATCHALL_CONTROLS.urlString(),GoodsType.SOFTWARE.urlString()));

    JourneyStage catchallRelatedControlsList = defineStage("softwareCatchallRelatedControlsList",
        controllers.softtech.controls.routes.SoftTechRelatedControlsController.renderForm(ControlCodeVariant.CATCHALL_CONTROLS.urlString(),GoodsType.SOFTWARE.urlString()));

    JourneyStage relatedToPhysicalGoodsControlsList = defineStage("softwareControlsRelatedToPhysicalGoodsControlsList",
        controllers.softtech.controls.routes.SoftTechControlsController.renderForm(ControlCodeVariant.CONTROLS_RELATED_TO_A_PHYSICAL_GOOD.urlString(),GoodsType.SOFTWARE.urlString()));

    JourneyStage relatedToPhysicalGoodsControlsRelatedControlsList = defineStage("softwareRelatedToPhysicalGoodsControlsRelatedControlsList",
        controllers.softtech.controls.routes.SoftTechControlsController.renderForm(ControlCodeVariant.CONTROLS_RELATED_TO_A_PHYSICAL_GOOD.urlString(),GoodsType.SOFTWARE.urlString()));

    JourneyStage categoryControlCodeSummary = defineStage("softwareCategoryControlCodeSummary",
        controllers.controlcode.routes.ControlCodeSummaryController.renderForm(ControlCodeVariant.CONTROLS.urlString(), GoodsType.SOFTWARE.urlString()));

    JourneyStage relatedToPhysicalGoodsControlCodeSummary = defineStage("softwareRelatedToPhysicalGoodsControlCodeSummary",
        controllers.controlcode.routes.ControlCodeSummaryController.renderForm(ControlCodeVariant.CONTROLS_RELATED_TO_A_PHYSICAL_GOOD.urlString(), GoodsType.SOFTWARE.urlString()));

    JourneyStage catchallControlCodeSummary = defineStage("softwareCatchallControlCodeSummary",
        controllers.controlcode.routes.ControlCodeSummaryController.renderForm(ControlCodeVariant.CATCHALL_CONTROLS.urlString(), GoodsType.SOFTWARE.urlString()));

    JourneyStage searchRelatedTo = defineStage("softwareSearchRelatedTo",
        controllers.search.routes.SearchController.renderForm(GoodsType.SOFTWARE.urlString()));

    JourneyStage relatedToTechnologyQuestion = defineStage("softwareRelatedToTechnologyQuestion",
        controllers.softtech.routes.GoodsRelationshipController.renderForm(GoodsType.SOFTWARE.urlString(), GoodsType.TECHNOLOGY.urlString()));

    JourneyStage goodsRelatedToTechnologyQuestions = defineStage("softwareGoodsRelatedToTechnologyQuestions",
        controllers.softtech.routes.GoodsRelationshipQuestionsController.renderForm(GoodsType.SOFTWARE.urlString(), GoodsType.TECHNOLOGY.urlString()));

    JourneyStage relatedToSoftwareQuestion = defineStage("softwareRelatedToSoftwareQuestion",
        controllers.softtech.routes.GoodsRelationshipController.renderForm(GoodsType.SOFTWARE.urlString(), GoodsType.SOFTWARE.urlString()));

    JourneyStage goodsRelatedToSoftwareQuestions = defineStage("softwareGoodsRelatedToSoftwareQuestions",
        controllers.softtech.routes.GoodsRelationshipQuestionsController.renderForm(GoodsType.SOFTWARE.urlString(), GoodsType.SOFTWARE.urlString()));

    atDecisionStage(isDualUseOrMilitarySoftware)
        .decide()
        .when(ExportCategory.MILITARY, moveTo(hasSoftwareApplicableControls))
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
        hasSoftwareApplicableControls,
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
        .then(moveTo(hasSoftwareApplicableControls));

    atStage(dualUseCategories)
        .onEvent(Events.NONE_MATCHED)
        .then(moveTo(relatedToEquipmentOrMaterials));

    atDecisionStage(hasSoftwareApplicableControls)
        .decide()
        .when(ApplicableSoftTechControls.ZERO, moveTo(relatedToEquipmentOrMaterials))
        .when(ApplicableSoftTechControls.ONE, moveTo(categoryControlCodeSummary))
        .when(ApplicableSoftTechControls.GREATER_THAN_ONE, moveTo(categoryControlsList));

    softTechCategoryControls(
        GoodsType.SOFTWARE,
        categoryControlsList,
        categoryRelatedControlsList,
        hasSoftwareCategoryRelatedControls,
        categoryControlCodeSummary,
        relatedToEquipmentOrMaterials,
        dualUseCategories
    );

    atStage(relatedToEquipmentOrMaterials)
        .onEvent(StandardEvents.YES)
        .then(moveTo(searchRelatedTo));

    atStage(relatedToEquipmentOrMaterials)
        .onEvent(StandardEvents.NO)
        .then(moveTo(hasSoftwareApplicableCatchallControls));

    softTechSearchRelatedTo(
        GoodsType.SOFTWARE,
        searchRelatedTo,
        hasSoftwareSearchRelatedControls,
        hasSoftwareApplicableCatchallControls,
        hasSoftwareApplicableRelatedControls
    );

    atDecisionStage(hasSoftwareApplicableRelatedControls)
        .decide()
        .when(ApplicableSoftTechControls.ZERO, moveTo(doesSoftwareRelationshipWithTechnologyExists))
        .when(ApplicableSoftTechControls.ONE, moveTo(relatedToPhysicalGoodsControlCodeSummary))
        .when(ApplicableSoftTechControls.GREATER_THAN_ONE, moveTo(relatedToPhysicalGoodsControlsList));

    softTechControlsRelatedToAPhysicalGood(
        GoodsType.SOFTWARE,
        relatedToPhysicalGoodsControlsList,
        relatedToPhysicalGoodsControlsRelatedControlsList,
        relatedToPhysicalGoodsControlCodeSummary,
        hasSoftwareApplicableCatchallControls,
        hasSoftwareRelatedControlsRelatedControls
    );

    atDecisionStage(hasSoftwareApplicableCatchallControls)
        .decide()
        .when(ApplicableSoftTechControls.ZERO, moveTo(doesSoftwareRelationshipWithTechnologyExists))
        .when(ApplicableSoftTechControls.ONE, moveTo(catchallControlCodeSummary))
        .when(ApplicableSoftTechControls.GREATER_THAN_ONE, moveTo(catchallControlsList));

    softTechCatchallControls(
        GoodsType.SOFTWARE,
        catchallControlsList,
        catchallRelatedControlsList,
        catchallControlCodeSummary,
        doesSoftwareRelationshipWithTechnologyExists,
        hasSoftwareCatchallRelatedControls
    );

    /** Software related to technology **/

    atDecisionStage(doesSoftwareRelationshipWithTechnologyExists)
        .decide()
        .when(true, moveTo(relatedToTechnologyQuestion))
        .when(false, moveTo(doesSoftwareRelationshipWithSoftwareExists));

    bindYesNoJourneyTransition(
        relatedToTechnologyQuestion,
        goodsRelatedToTechnologyQuestions,
        doesSoftwareRelationshipWithSoftwareExists
    );

    bindYesNoJourneyTransition(
        goodsRelatedToTechnologyQuestions,
        destinationCountries,
        doesSoftwareRelationshipWithSoftwareExists
    );

    /** Software related to software **/

    atDecisionStage(doesSoftwareRelationshipWithSoftwareExists)
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

    JourneyStage technologyPublicDomainExemption = defineStage("technologyPublicDomainExemption",
        controllers.softtech.routes.TechnologyPublicDomainExemptionNLRController.renderForm());

    JourneyStage technologyNonExempt = defineStage("technologyNonExempt",
        controllers.softtech.routes.TechnologyNonExemptController.renderForm());

    JourneyStage relatedToEquipmentOrMaterials = defineStage("technologyRelatedToEquipmentOrMaterials",
        controllers.softtech.routes.RelatedEquipmentController.renderForm(GoodsType.TECHNOLOGY.urlString()));

    JourneyStage dualUseCategories = defineStage("technologyDualUseCategories",
        controllers.softtech.routes.DualUseSoftTechCategoriesController.renderForm(GoodsType.TECHNOLOGY.urlString()));

    JourneyStage nonExemptControlsControlsList = defineStage("technologyNonExemptControlsControlsList",
        controllers.softtech.controls.routes.SoftTechControlsController.renderForm(ControlCodeVariant.NON_EXEMPT.urlString(), GoodsType.TECHNOLOGY.urlString()));

    JourneyStage nonExemptControlsRelatedControlsList = defineStage("nonExemptControlsRelatedControlsList",
        controllers.softtech.controls.routes.SoftTechRelatedControlsController.renderForm(ControlCodeVariant.NON_EXEMPT.urlString(), GoodsType.TECHNOLOGY.urlString()));

    JourneyStage categoryControlsList = defineStage("technologyCategoryControlsList",
        controllers.softtech.controls.routes.SoftTechControlsController.renderForm(ControlCodeVariant.CONTROLS.urlString(), GoodsType.TECHNOLOGY.urlString()));

    JourneyStage categoryRelatedControlsList = defineStage("technologyCategoryRelatedControlsList",
        controllers.softtech.controls.routes.SoftTechRelatedControlsController.renderForm(ControlCodeVariant.CONTROLS.urlString(), GoodsType.TECHNOLOGY.urlString()));

    JourneyStage catchallControlsList = defineStage("technologyCatchallControlsList",
        controllers.softtech.controls.routes.SoftTechControlsController.renderForm(ControlCodeVariant.CATCHALL_CONTROLS.urlString(),GoodsType.TECHNOLOGY.urlString()));

    JourneyStage catchallRelatedControlsList = defineStage("technologyCatchallRelatedControlsList",
        controllers.softtech.controls.routes.SoftTechRelatedControlsController.renderForm(ControlCodeVariant.CATCHALL_CONTROLS.urlString(),GoodsType.TECHNOLOGY.urlString()));

    JourneyStage relatedToPhysicalGoodsControlsList = defineStage("technologyControlsRelatedToPhysicalGoodsControlsList",
        controllers.softtech.controls.routes.SoftTechControlsController.renderForm(ControlCodeVariant.CONTROLS_RELATED_TO_A_PHYSICAL_GOOD.urlString(),GoodsType.TECHNOLOGY.urlString()));

    JourneyStage relatedToPhysicalGoodsControlsRelatedControlsList = defineStage("technologyControlsRelatedToPhysicalGoodsRelatedControlsList",
        controllers.softtech.controls.routes.SoftTechRelatedControlsController.renderForm(ControlCodeVariant.CONTROLS_RELATED_TO_A_PHYSICAL_GOOD.urlString(),GoodsType.TECHNOLOGY.urlString()));

    JourneyStage categoryControlCodeSummary = defineStage("technologyCategoryControlCodeSummary",
        controllers.controlcode.routes.ControlCodeSummaryController.renderForm(ControlCodeVariant.CONTROLS.urlString(), GoodsType.TECHNOLOGY.urlString()));

    JourneyStage relatedToPhysicalGoodsControlCodeSummary = defineStage("technologyRelatedToPhysicalGoodsControlCodeSummary",
        controllers.controlcode.routes.ControlCodeSummaryController.renderForm(ControlCodeVariant.CONTROLS_RELATED_TO_A_PHYSICAL_GOOD.urlString(), GoodsType.TECHNOLOGY.urlString()));

    JourneyStage catchallControlCodeSummary = defineStage("technologyCatchallControlCodeSummary",
        controllers.controlcode.routes.ControlCodeSummaryController.renderForm(ControlCodeVariant.CATCHALL_CONTROLS.urlString(), GoodsType.TECHNOLOGY.urlString()));

    JourneyStage searchRelatedTo = defineStage("technologySearchRelatedTo",
        controllers.search.routes.SearchController.renderForm(GoodsType.TECHNOLOGY.urlString()));

    JourneyStage relatedToSoftwareQuestion = defineStage("technologyRelatedToSoftwareQuestion",
        controllers.softtech.routes.GoodsRelationshipController.renderForm(GoodsType.TECHNOLOGY.urlString(), GoodsType.SOFTWARE.urlString()));

    JourneyStage goodsRelatedToSoftwareQuestions = defineStage("technologyGoodsRelatedToSoftwareQuestions",
        controllers.softtech.routes.GoodsRelationshipQuestionsController.renderForm(GoodsType.TECHNOLOGY.urlString(), GoodsType.SOFTWARE.urlString()));

    JourneyStage journeyEndNLR = defineStage("technologyJourneyEndNLR",
        routes.StaticContentController.renderTechnologyJourneyEndNLR());

    bindYesNoJourneyTransition(
        technologyExemptions,
        technologyPublicDomainExemption,
        technologyNonExempt
    );

    atStage(technologyPublicDomainExemption)
        .onEvent(StandardEvents.NEXT)
        .then(moveTo(technologyNonExempt));

    bindYesNoJourneyTransition(
        technologyNonExempt,
        isDualUseOrMilitaryNonExemptTechnology,
        isDualUseOrMilitaryTechnology
    );

    atDecisionStage(isDualUseOrMilitaryNonExemptTechnology)
        .decide()
        .when(ExportCategory.MILITARY, moveTo(technologyExemptionsNLR))
        .when(ExportCategory.DUAL_USE, moveTo(nonExemptControlsControlsList));

    atDecisionStage(isDualUseOrMilitaryTechnology)
        .decide()
        .when(ExportCategory.MILITARY, moveTo(hasTechnologyApplicableControls))
        .when(ExportCategory.DUAL_USE, moveTo(dualUseCategories));

    technologyNonExemptControls(nonExemptControlsControlsList, nonExemptControlsRelatedControlsList);

    atStage(dualUseCategories)
        .onEvent(StandardEvents.NEXT)
        .then(moveTo(hasTechnologyApplicableControls));

    atStage(dualUseCategories)
        .onEvent(Events.NONE_MATCHED)
        .then(moveTo(relatedToEquipmentOrMaterials));

    atDecisionStage(hasTechnologyApplicableControls)
        .decide()
        .when(ApplicableSoftTechControls.ZERO, moveTo(relatedToEquipmentOrMaterials))
        .when(ApplicableSoftTechControls.ONE, moveTo(categoryControlCodeSummary))
        .when(ApplicableSoftTechControls.GREATER_THAN_ONE, moveTo(categoryControlsList));

    softTechCategoryControls(
        GoodsType.TECHNOLOGY,
        categoryControlsList,
        categoryRelatedControlsList,
        hasTechnologyCategoryRelatedControls,
        categoryControlCodeSummary,
        relatedToEquipmentOrMaterials,
        dualUseCategories
    );

    atStage(relatedToEquipmentOrMaterials)
        .onEvent(StandardEvents.YES)
        .then(moveTo(searchRelatedTo));

    atStage(relatedToEquipmentOrMaterials)
        .onEvent(StandardEvents.NO)
        .then(moveTo(hasTechnologyApplicableCatchallControls));

    softTechSearchRelatedTo(
        GoodsType.TECHNOLOGY,
        searchRelatedTo,
        hasTechnologySearchRelatedControls,
        hasTechnologyApplicableCatchallControls,
        hasTechnologyApplicableRelatedControls
    );

    atDecisionStage(hasTechnologyApplicableRelatedControls)
        .decide()
        .when(ApplicableSoftTechControls.ZERO, moveTo(doesTechnologyRelationshipWithSoftwareExists))
        .when(ApplicableSoftTechControls.ONE, moveTo(relatedToPhysicalGoodsControlCodeSummary))
        .when(ApplicableSoftTechControls.GREATER_THAN_ONE, moveTo(relatedToPhysicalGoodsControlsList));

    softTechControlsRelatedToAPhysicalGood(
        GoodsType.TECHNOLOGY,
        relatedToPhysicalGoodsControlsList,
        relatedToPhysicalGoodsControlsRelatedControlsList,
        relatedToPhysicalGoodsControlCodeSummary,
        hasTechnologyApplicableCatchallControls,
        hasTechnologyRelatedControlsRelatedControls
    );

    atDecisionStage(hasTechnologyApplicableCatchallControls)
        .decide()
        .when(ApplicableSoftTechControls.ZERO, moveTo(doesTechnologyRelationshipWithSoftwareExists))
        .when(ApplicableSoftTechControls.ONE, moveTo(catchallControlCodeSummary))
        .when(ApplicableSoftTechControls.GREATER_THAN_ONE, moveTo(catchallControlsList));

    softTechCatchallControls(
        GoodsType.TECHNOLOGY,
        catchallControlsList,
        catchallRelatedControlsList,
        catchallControlCodeSummary,
        doesTechnologyRelationshipWithSoftwareExists,
        hasTechnologyCatchallRelatedControls
    );

    atDecisionStage(doesTechnologyRelationshipWithSoftwareExists)
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

  private void technologyNonExemptControls(JourneyStage controlsList, JourneyStage relatedControlsList) {
    /** Technology non exempt controls journey stages */
    JourneyStage controlCodeSummary = defineStage("technologyNonExemptControlsControlCodeSummary",
        controllers.controlcode.routes.ControlCodeSummaryController.renderForm(ControlCodeVariant.NON_EXEMPT.urlString(), GoodsType.TECHNOLOGY.urlString()));

    JourneyStage controlCodeNotApplicable = defineStage("technologyNonExemptControlsControlCodeNotApplicable",
        controllers.controlcode.routes.NotApplicableController.renderForm(ControlCodeVariant.NON_EXEMPT.urlString(), GoodsType.TECHNOLOGY.urlString()));

    JourneyStage additionalSpecifications = defineStage("technologyNonExemptControlsAdditionalSpecifications",
        controllers.controlcode.routes.AdditionalSpecificationsController.renderForm(ControlCodeVariant.NON_EXEMPT.urlString(), GoodsType.TECHNOLOGY.urlString()));

    JourneyStage decontrols = defineStage("technologyNonExemptControlsDecontrols",
        controllers.controlcode.routes.DecontrolsController.renderForm(ControlCodeVariant.NON_EXEMPT.urlString(), GoodsType.TECHNOLOGY.urlString()));

    JourneyStage decontrolsApply = defineStage("technologyNonExemptControlsDecontrolsApply",
        controllers.controlcode.routes.DecontrolsApplyController.renderForm(ControlCodeVariant.NON_EXEMPT.urlString(), GoodsType.TECHNOLOGY.urlString()));

    JourneyStage technicalNotes = defineStage("technologyNonExemptControlsTechnicalNotes",
        controllers.controlcode.routes.TechnicalNotesController.renderForm(ControlCodeVariant.NON_EXEMPT.urlString(), GoodsType.TECHNOLOGY.urlString()));

    /** Software controls decision stages */
    DecisionStage<Boolean> additionalSpecificationsDecision = defineDecisionStage("technologyNonExemptControlsAdditionalSpecificationsDecision", additionalSpecificationsDecider);

    DecisionStage<Boolean> decontrolsDecision = defineDecisionStage("technologyNonExemptControlsDecontrolsDecision", decontrolsDecider);

    DecisionStage<Boolean> technicalNotesDecision = defineDecisionStage("technologyNonExemptControlsTechnicalNotesDecision", technicalNotesDecider);

    /** Software control journey stage transitions */
    bindControlCodeListStageJourneyTransitions(
        controlsList,
        relatedControlsList,
        hasTechnologyNonExemptRelatedControls,
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
  private void softTechCategoryControls(GoodsType goodsType, JourneyStage controlsList, JourneyStage relatedCodesList, DecisionStage<Boolean> hasRelatedCodes, JourneyStage controlCodeSummary, JourneyStage relatedToEquipmentOrMaterials, JourneyStage dualUseCategories) {
    String goodsTypeText = goodsType.value().toLowerCase();

    /** Software/Technology controls journey stages */
    JourneyStage controlCodeNotApplicable = defineStage(goodsTypeText + "CategoryControlCodeNotApplicable",
        controllers.controlcode.routes.NotApplicableController.renderForm(ControlCodeVariant.CONTROLS.urlString(), goodsType.urlString()));

    JourneyStage additionalSpecifications = defineStage(goodsTypeText + "CategoryAdditionalSpecifications",
        controllers.controlcode.routes.AdditionalSpecificationsController.renderForm(ControlCodeVariant.CONTROLS.urlString(), goodsType.urlString()));

    JourneyStage decontrols = defineStage(goodsTypeText + "CategoryDecontrols",
        controllers.controlcode.routes.DecontrolsController.renderForm(ControlCodeVariant.CONTROLS.urlString(), goodsType.urlString()));

    JourneyStage decontrolsApply = defineStage(goodsTypeText + "CategoryDecontrolsApply",
        controllers.controlcode.routes.DecontrolsApplyController.renderForm(ControlCodeVariant.CONTROLS.urlString(), goodsType.urlString()));

    JourneyStage technicalNotes = defineStage(goodsTypeText + "CategoryTechnicalNotes",
        controllers.controlcode.routes.TechnicalNotesController.renderForm(ControlCodeVariant.CONTROLS.urlString(), goodsType.urlString()));

    /** Software/Technology controls decision stages */
    DecisionStage<Boolean> additionalSpecificationsDecision = defineDecisionStage(goodsTypeText + "CategoryAdditionalSpecsDecision", additionalSpecificationsDecider);

    DecisionStage<Boolean> decontrolsDecision = defineDecisionStage(goodsTypeText + "CategoryDecontrolsDecision", decontrolsDecider);

    DecisionStage<Boolean> technicalNotesDecision = defineDecisionStage(goodsTypeText + "CategoryTechnicalNotesDecision", technicalNotesDecider);

    /** Software/Technology control journey stage transitions */
    bindControlCodeListStageJourneyTransitions(
        controlsList,
        relatedCodesList,
        hasRelatedCodes,
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
    JourneyStage searchResults = defineStage(goodsTypeText + "SearchResultsRelatedTo",
        controllers.search.routes.SearchResultsController.renderForm(goodsType.urlString()));

    JourneyStage searchRelatedCodes = defineStage(goodsTypeText + "SearchRelatedCodesRelatedTo",
        controllers.search.routes.SearchRelatedCodesController.renderForm(goodsType.urlString()));

    JourneyStage controlCodeSummary = defineStage(goodsTypeText + "ControlCodeSummaryRelatedTo",
        controllers.controlcode.routes.ControlCodeSummaryController.renderForm(ControlCodeVariant.SEARCH.urlString(), goodsType.urlString()));

    JourneyStage controlCodeNotApplicable = defineStage(goodsTypeText + "ControlCodeNotApplicableRelatedTo",
        controllers.controlcode.routes.NotApplicableController.renderForm(ControlCodeVariant.SEARCH.urlString(), goodsType.urlString()));

    JourneyStage additionalSpecifications = defineStage(goodsTypeText + "AdditionalSpecificationsRelatedTo",
        controllers.controlcode.routes.AdditionalSpecificationsController.renderForm(ControlCodeVariant.SEARCH.urlString(), goodsType.urlString()));

    JourneyStage decontrols = defineStage(goodsTypeText + "DecontrolsRelatedTo",
        controllers.controlcode.routes.DecontrolsController.renderForm(ControlCodeVariant.SEARCH.urlString(), goodsType.urlString()));

    JourneyStage decontrolsApply = defineStage(goodsTypeText + "DecontrolsApplyRelatedTo",
        controllers.controlcode.routes.DecontrolsApplyController.renderForm(ControlCodeVariant.SEARCH.urlString(), goodsType.urlString()));

    JourneyStage technicalNotes = defineStage(goodsTypeText + "TechnicalNotesRelatedTo",
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
  private void softTechControlsRelatedToAPhysicalGood(GoodsType goodsType,
                                                      JourneyStage relatedToPhysicalGoodsControlsList,
                                                      JourneyStage relatedToPhysicalGoodsRelatedControlsList,
                                                      JourneyStage relatedToPhysicalGoodsControlCodeSummary,
                                                      DecisionStage<ApplicableSoftTechControls> applicableCatchallControlsDecision,
                                                      DecisionStage<Boolean> relatedToPhysicalGoodsRelatedControlsDecision) {
    String goodsTypeText = goodsType.value().toLowerCase();

    /** Software/Technology controls related to physical goods  */
    JourneyStage controlCodeNotApplicable = defineStage(goodsTypeText + "ControlsRelatedToPhysicalGoodsControlCodeNotApplicable",
        controllers.controlcode.routes.NotApplicableController.renderForm(ControlCodeVariant.CONTROLS_RELATED_TO_A_PHYSICAL_GOOD.urlString(), goodsType.urlString()));

    JourneyStage additionalSpecifications = defineStage(goodsTypeText + "ControlsRelatedToPhysicalGoodsAdditionalSpecifications",
        controllers.controlcode.routes.AdditionalSpecificationsController.renderForm(ControlCodeVariant.CONTROLS_RELATED_TO_A_PHYSICAL_GOOD.urlString(), goodsType.urlString()));

    JourneyStage decontrols = defineStage(goodsTypeText + "ControlsRelatedToPhysicalGoodsDecontrols",
        controllers.controlcode.routes.DecontrolsController.renderForm(ControlCodeVariant.CONTROLS_RELATED_TO_A_PHYSICAL_GOOD.urlString(), goodsType.urlString()));

    JourneyStage decontrolsApply = defineStage(goodsTypeText + "ControlsRelatedToPhysicalGoodsDecontrolsApply",
        controllers.controlcode.routes.DecontrolsApplyController.renderForm(ControlCodeVariant.CONTROLS_RELATED_TO_A_PHYSICAL_GOOD.urlString(), goodsType.urlString()));

    JourneyStage technicalNotes = defineStage(goodsTypeText + "ControlsRelatedToPhysicalGoodsTechnicalNotes",
        controllers.controlcode.routes.TechnicalNotesController.renderForm(ControlCodeVariant.CONTROLS_RELATED_TO_A_PHYSICAL_GOOD.urlString(), goodsType.urlString()));

    /** Software/Technology controls related to physical goods decision stages */
    DecisionStage<Boolean> additionalSpecificationsDecision = defineDecisionStage(goodsTypeText + "ControlsRelatedToPhysicalGoodsAdditionalSpecificationsDecision", additionalSpecificationsDecider);

    DecisionStage<Boolean> decontrolsDecision = defineDecisionStage(goodsTypeText + "ControlsRelatedToPhysicalGoodsDecontrolsDecision", decontrolsDecider);

    DecisionStage<Boolean> technicalNotesDecision = defineDecisionStage(goodsTypeText + "ControlsRelatedToPhysicalGoodsTechnicalNotesDecision", technicalNotesDecider);

    /** Software/Technology control journey stage transitions */
    bindControlCodeListStageJourneyTransitions(
        relatedToPhysicalGoodsControlsList,
        relatedToPhysicalGoodsRelatedControlsList,
        relatedToPhysicalGoodsRelatedControlsDecision,
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
        decontrolsDecision,
        additionalSpecificationsDecision,
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
  private void softTechCatchallControls(GoodsType goodsType,
                                        JourneyStage controlsList,
                                        JourneyStage relatedControlsList,
                                        JourneyStage controlCodeSummary,
                                        DecisionStage<Boolean> relationshipWithSoftTechExists,
                                        DecisionStage<Boolean> hasRelatedControls) {
    String goodsTypeText = goodsType.value().toLowerCase();

    /** Software/Technology catchall controls journey stages */
    JourneyStage controlCodeNotApplicable = defineStage(goodsTypeText + "ControlCodeNotApplicable",
        controllers.controlcode.routes.NotApplicableController.renderForm(ControlCodeVariant.CATCHALL_CONTROLS.urlString(), goodsType.urlString()));

    JourneyStage additionalSpecification = defineStage(goodsTypeText + "AdditionalSpecification",
        controllers.controlcode.routes.AdditionalSpecificationsController.renderForm(ControlCodeVariant.CATCHALL_CONTROLS.urlString(), goodsType.urlString()));

    JourneyStage decontrols = defineStage(goodsTypeText + "Decontrols",
        controllers.controlcode.routes.DecontrolsController.renderForm(ControlCodeVariant.CATCHALL_CONTROLS.urlString(), goodsType.urlString()));

    JourneyStage decontrolsApply = defineStage(goodsTypeText + "DecontrolsApply",
        controllers.controlcode.routes.DecontrolsApplyController.renderForm(ControlCodeVariant.CATCHALL_CONTROLS.urlString(), goodsType.urlString()));

    JourneyStage technicalNotes = defineStage(goodsTypeText + "TechnicalNotes",
        controllers.controlcode.routes.TechnicalNotesController.renderForm(ControlCodeVariant.CATCHALL_CONTROLS.urlString(), goodsType.urlString()));

    /** Software/Technology catchall controls decision stages */
    DecisionStage<Boolean> additionalSpecificationsDecision = defineDecisionStage(goodsTypeText + "AdditionalSpecificationsDecision", additionalSpecificationsDecider);

    DecisionStage<Boolean> decontrolsDecision = defineDecisionStage(goodsTypeText + "DecontrolsDecision", decontrolsDecider);

    DecisionStage<Boolean> technicalNotesDecision = defineDecisionStage(goodsTypeText + "TechnicalNotesDecision", technicalNotesDecider);

    /** Software/Technology catchall controls stage transitions */
    bindControlCodeListStageJourneyTransitions(
        controlsList,
        relatedControlsList,
        hasRelatedControls,
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
        decontrolsDecision,
        additionalSpecificationsDecision,
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
                                                          JourneyStage relatedControlsListStage,
                                                          DecisionStage<Boolean> relatedControlsDecisionStage,
                                                          CommonStage controlCodeSummaryStage,
                                                          CommonStage controlCodeNoneMatchedStage) {
    atStage(controlCodeListStage)
        .onEvent(Events.CONTROL_CODE_SELECTED)
        .then(moveTo(relatedControlsDecisionStage));

    atStage(controlCodeListStage)
        .onEvent(Events.NONE_MATCHED)
        .then(moveTo(controlCodeNoneMatchedStage));

    atDecisionStage(relatedControlsDecisionStage)
        .decide()
        .when(true, moveTo(relatedControlsListStage))
        .when(false, moveTo(controlCodeSummaryStage));

    atStage(relatedControlsListStage)
        .onEvent(Events.CONTROL_CODE_SELECTED)
        .then(moveTo(controlCodeSummaryStage));

    atStage(relatedControlsListStage)
        .onEvent(Events.BACK)
        .branch()
        .when(BackType.RESULTS, moveTo(controlCodeListStage));
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
