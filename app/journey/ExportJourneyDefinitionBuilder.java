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
  private final JourneyStage search = defineStage("search", "Describe your items",
      controllers.search.routes.SearchController.renderForm(GoodsType.PHYSICAL.urlString()));
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
  private final DecisionStage<ApplicableSoftTechControls> applicableCatchallControlsDecision;
  private final DecisionStage<Boolean> softwareRelationshipWithTechnologyExistsDecision;
  private final DecisionStage<Boolean> softwareRelationshipWithSoftwareExistsDecision;

  private final JourneyStage dualUseSoftwareCategories = defineStage("dualUseSoftTechCategories", "What is your software for?",
      controllers.softtech.routes.DualUseSoftTechCategoriesController.renderForm(GoodsType.SOFTWARE.urlString()));

  private final JourneyStage searchRTS = defineStage("searchRTS", "Describe your items",
      controllers.search.routes.SearchController.renderForm(GoodsType.SOFTWARE.urlString()));

  private final JourneyStage controlCodeSummarySC = defineStage("controlCodeSummarySC", "Summary",
      controllers.controlcode.routes.ControlCodeSummaryController.renderForm(ControlCodeVariant.CONTROLS.urlString(), GoodsType.SOFTWARE.urlString()));
  private final JourneyStage controlCodeSummarySCRTPG = defineStage("controlCodeSummarySCRTPG", "Summary",
      controllers.controlcode.routes.ControlCodeSummaryController.renderForm(ControlCodeVariant.CONTROLS_RELATED_TO_A_PHYSICAL_GOOD.urlString(), GoodsType.SOFTWARE.urlString()));
  private final JourneyStage controlCodeSummarySCC = defineStage("controlCodeSummarySCC", "Summary",
      controllers.controlcode.routes.ControlCodeSummaryController.renderForm(ControlCodeVariant.CATCHALL_CONTROLS.urlString(), GoodsType.SOFTWARE.urlString()));

  private final JourneyStage controlsListSC = defineStage("controlsListSC", "Showing controls related to software category",
      controllers.softtech.controls.routes.SoftTechControlsController.renderForm(ControlCodeVariant.CONTROLS.urlString(), GoodsType.SOFTWARE.urlString()));
  private final JourneyStage controlsListSCRTPG = defineStage("controlsListSCRTPG", "Showing controls related to your selected physical good",
      controllers.softtech.controls.routes.SoftTechControlsController.renderForm(ControlCodeVariant.CONTROLS_RELATED_TO_A_PHYSICAL_GOOD.urlString(),GoodsType.SOFTWARE.urlString()));
  private final JourneyStage controlsListSCC = defineStage("controlsListSCC", "Showing catchall controls related to your items category",
      controllers.softtech.controls.routes.SoftTechControlsController.renderForm(ControlCodeVariant.CATCHALL_CONTROLS.urlString(),GoodsType.SOFTWARE.urlString()));

  private JourneyStage softwareRelatedToEquipmentOrMaterials = defineStage("softwareRelatedToEquipmentOrMaterials", "Is your software any of the following?",
      controllers.softtech.routes.RelatedEquipmentController.renderForm(GoodsType.SOFTWARE.urlString()));

  private JourneyStage softwareJourneyEndNLR = defineStage("softwareJourneyEndNLR", "No licence available",
      routes.StaticContentController.renderSoftwareJourneyEndNLR());

  private JourneyStage softwareRelatedToTechnologyQuestion = defineStage("softwareRelatedToTechnologyQuestion", "Is your software related to a technology?",
      controllers.softtech.routes.GoodsRelationshipController.renderForm(GoodsType.SOFTWARE.urlString(), GoodsType.TECHNOLOGY.urlString()));

  private JourneyStage softwareRelatedToSoftwareQuestion = defineStage("softwareRelatedToSoftwareQuestion", "Is your software related to other software?",
      controllers.softtech.routes.GoodsRelationshipController.renderForm(GoodsType.SOFTWARE.urlString(), GoodsType.SOFTWARE.urlString()));

  private final AdditionalSpecificationsDecider additionalSpecificationsDecider;
  private final DecontrolsDecider decontrolsDecider;
  private final TechnicalNotesDecider technicalNotesDecider;
  private final ExportCategoryDecider exportCategoryDecider;
  private final CategoryControlsDecider categoryControlsDecider;
  private final RelatedControlsDecider relatedControlsDecider;
  private final CatchallControlsDecider catchallControlsDecider;
  private final RelationshipWithTechnologyDecider relationshipWithTechnologyDecider;
  private final RelationshipWithSoftwareDecider relationshipWithSoftwareDecider;

  @Inject
  public ExportJourneyDefinitionBuilder(AdditionalSpecificationsDecider additionalSpecificationsDecider,
                                        DecontrolsDecider decontrolsDecider,
                                        TechnicalNotesDecider technicalNotesDecider,
                                        ExportCategoryDecider exportCategoryDecider,
                                        CategoryControlsDecider categoryControlsDecider,
                                        RelatedControlsDecider relatedControlsDecider,
                                        CatchallControlsDecider catchallControlsDecider,
                                        RelationshipWithTechnologyDecider relationshipWithTechnologyDecider,
                                        RelationshipWithSoftwareDecider relationshipWithSoftwareDecider) {
    this.additionalSpecificationsDecider = additionalSpecificationsDecider;
    this.decontrolsDecider = decontrolsDecider;
    this.technicalNotesDecider = technicalNotesDecider;
    this.exportCategoryDecider = exportCategoryDecider;
    this.categoryControlsDecider = categoryControlsDecider;
    this.relatedControlsDecider = relatedControlsDecider;
    this.catchallControlsDecider = catchallControlsDecider;
    this.relationshipWithTechnologyDecider = relationshipWithTechnologyDecider;
    this.relationshipWithSoftwareDecider = relationshipWithSoftwareDecider;
    this.dualUseOrMilitarySoftwareDecision = defineDecisionStage("isDualUseSoftwareDecision", this.exportCategoryDecider);
    this.applicableSoftwareControlsDecision = defineDecisionStage("applicableSoftwareControlsDecision", this.categoryControlsDecider);
    this.applicableRelatedControlsDecision = defineDecisionStage("applicableRelatedControlsDecision", this.relatedControlsDecider);
    this.applicableCatchallControlsDecision = defineDecisionStage("applicableCatchallControlsDecision", this.catchallControlsDecider);
    this.softwareRelationshipWithTechnologyExistsDecision = defineDecisionStage("softwareRelationshipWithTechnologyExistsDecision", this.relationshipWithTechnologyDecider);
    this.softwareRelationshipWithSoftwareExistsDecision = defineDecisionStage("softwareRelationshipWithSoftwareExistsDecision", this.relationshipWithSoftwareDecider);
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
        controllers.search.routes.SearchResultsController.renderForm(GoodsType.PHYSICAL.urlString()));

    JourneyStage controlCodeSummary = defineStage("controlCodeSummary", "Summary",
        controllers.controlcode.routes.ControlCodeSummaryController.renderForm(ControlCodeVariant.SEARCH.urlString(), GoodsType.PHYSICAL.urlString()));

    JourneyStage controlCodeNotApplicable = defineStage("controlCodeNotApplicable", "Rating is not applicable",
        controllers.controlcode.routes.NotApplicableController.renderForm(ControlCodeVariant.SEARCH.urlString(), GoodsType.PHYSICAL.urlString(), Boolean.FALSE.toString()));

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
        .then(moveTo(decontrolsDecision));

    atStage(searchResults)
        .onEvent(Events.NONE_MATCHED)
        .then(moveTo(notApplicable));

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
        controllers.softtech.routes.ExemptionsController.renderFormQ1());

    JourneyStage softwareExemptionsQ2 = defineStage("softwareExemptionsQ2", "Some types of software do not need a licence",
        controllers.softtech.routes.ExemptionsController.renderFormQ2());

    JourneyStage softwareExemptionsQ3 = defineStage("softwareExemptionsQ3", "Some types of software do not need a licence",
        controllers.softtech.routes.ExemptionsController.renderFormQ3());

    JourneyStage softwareExemptionsNLR1 = defineStage("softwareExemptionsNLR1", "Software exemptions apply",
        controllers.routes.StaticContentController.renderSoftwareExemptionsNLR1());

    JourneyStage softwareExemptionsNLR2 = defineStage("softwareExemptionsNLR2", "Software exemptions apply",
        controllers.routes.StaticContentController.renderSoftwareExemptionsNLR2());

    atDecisionStage(dualUseOrMilitarySoftwareDecision)
        .decide()
        .when(ExportCategory.MILITARY, moveTo(applicableSoftwareControlsDecision))
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
        applicableSoftwareControlsDecision,
        softwareExemptionsQ3
    );

    // softwareExemptionsQ3 journey transitions
    bindYesNoJourneyTransition(
        softwareExemptionsQ3,
        softwareExemptionsNLR2,
        dualUseSoftwareCategories
    );

    atStage(dualUseSoftwareCategories)
        .onEvent(StandardEvents.NEXT)
        .then(moveTo(applicableSoftwareControlsDecision));

    atStage(dualUseSoftwareCategories)
        .onEvent(Events.NONE_MATCHED)
        .then(moveTo(softwareRelatedToEquipmentOrMaterials));

    atDecisionStage(applicableSoftwareControlsDecision)
        .decide()
        .when(ApplicableSoftTechControls.ZERO, moveTo(softwareRelatedToEquipmentOrMaterials))
        .when(ApplicableSoftTechControls.ONE, moveTo(controlCodeSummarySC))
        .when(ApplicableSoftTechControls.GREATER_THAN_ONE, moveTo(controlsListSC));

    softwareCategoryControls();

    atStage(softwareRelatedToEquipmentOrMaterials)
        .onEvent(StandardEvents.YES)
        .then(moveTo(searchRTS));

    atStage(softwareRelatedToEquipmentOrMaterials)
        .onEvent(StandardEvents.NO)
        .then(moveTo(applicableCatchallControlsDecision));

    searchRelatedToSoftware();

    atDecisionStage(applicableRelatedControlsDecision)
        .decide()
        .when(ApplicableSoftTechControls.ZERO, moveTo(softwareRelationshipWithTechnologyExistsDecision))
        .when(ApplicableSoftTechControls.ONE, moveTo(controlCodeSummarySCRTPG))
        .when(ApplicableSoftTechControls.GREATER_THAN_ONE, moveTo(controlsListSCRTPG));

    softwareControlsRelatedToAPhysicalGood();

    atDecisionStage(applicableCatchallControlsDecision)
        .decide()
        .when(ApplicableSoftTechControls.ZERO, moveTo(softwareRelationshipWithTechnologyExistsDecision))
        .when(ApplicableSoftTechControls.ONE, moveTo(controlCodeSummarySCC))
        .when(ApplicableSoftTechControls.GREATER_THAN_ONE, moveTo(controlsListSCC));

    softwareCatchallControls();

    atDecisionStage(softwareRelationshipWithTechnologyExistsDecision)
        .decide()
        .when(true, moveTo(softwareRelatedToTechnologyQuestion))
        .when(false, moveTo(softwareRelationshipWithSoftwareExistsDecision));

    softwareRelatedToTechnology();

    atDecisionStage(softwareRelationshipWithSoftwareExistsDecision)
        .decide()
        .when(true, moveTo(softwareRelatedToSoftwareQuestion))
        .when(false, moveTo(softwareJourneyEndNLR));

    softwareRelatedToSoftware();

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

    JourneyStage decontrolsApplySC = defineStage("decontrolsApplySC", "Choose a different item type",
        controllers.controlcode.routes.DecontrolsApplyController.renderForm(ControlCodeVariant.CONTROLS.urlString(), GoodsType.SOFTWARE.urlString()));

    JourneyStage technicalNotesSC = defineStage("technicalNotesSC", "Technical notes",
        controllers.controlcode.routes.TechnicalNotesController.renderForm(ControlCodeVariant.CONTROLS.urlString(), GoodsType.SOFTWARE.urlString()));

    /** Software controls decision stages */
    DecisionStage<Boolean> additionalSpecificationsDecisionSC = defineDecisionStage("additionalSpecsDecisionSC", additionalSpecificationsDecider);

    DecisionStage<Boolean> decontrolsDecisionSC = defineDecisionStage("decontrolsDecisionSC", decontrolsDecider);

    DecisionStage<Boolean> technicalNotesDecisionSC = defineDecisionStage("technicalNotesDecisionSC", technicalNotesDecider);

    /** Software control journey stage transitions */
    bindControlCodeListStageJourneyTransitions(
        controlsListSC,
        decontrolsDecisionSC,
        softwareRelatedToEquipmentOrMaterials
    );

    bindControlCodeStageTransitions(
        decontrolsSC,
        decontrolsApplySC,
        controlCodeSummarySC,
        controlCodeNotApplicableSC,
        additionalSpecificationsSC,
        technicalNotesSC,
        destinationCountries,
        decontrolsDecisionSC, additionalSpecificationsDecisionSC,
        technicalNotesDecisionSC
    );

    bindControlCodeNotApplicableFromListStageJourneyTransitions(
        controlCodeNotApplicableSC,
        controlsListSC,
        softwareRelatedToEquipmentOrMaterials
    );

    atStage(decontrolsApplySC)
        .onEvent(Events.BACK)
        .branch()
        .when(BackType.MATCHES, backTo(controlsListSC))
        .when(BackType.SOFT_TECH_CATEGORY, backTo(dualUseSoftwareCategories))
        .when(BackType.EXPORT_CATEGORY, backTo(exportCategory));
  }

  /**
   * Search related to software journey
   */
  private void searchRelatedToSoftware() {
    /** Search related to software journey stages */
    JourneyStage searchResultsRTS = defineStage("searchResultsRTS", "Possible matches",
        controllers.search.routes.SearchResultsController.renderForm(GoodsType.SOFTWARE.urlString()));

    JourneyStage controlCodeSummaryRTS = defineStage("controlCodeSummaryRTS", "Summary",
        controllers.controlcode.routes.ControlCodeSummaryController.renderForm(ControlCodeVariant.SEARCH.urlString(), GoodsType.SOFTWARE.urlString()));

    JourneyStage controlCodeNotApplicableRTS = defineStage("controlCodeNotApplicableRTS", "Description not applicable",
        controllers.controlcode.routes.NotApplicableController.renderForm(ControlCodeVariant.SEARCH.urlString(), GoodsType.SOFTWARE.urlString(), Boolean.FALSE.toString()));

    JourneyStage additionalSpecificationsRTS = defineStage("additionalSpecificationsRTS", "Additional specifications",
        controllers.controlcode.routes.AdditionalSpecificationsController.renderForm(ControlCodeVariant.SEARCH.urlString(), GoodsType.SOFTWARE.urlString()));

    JourneyStage decontrolsRTS = defineStage("decontrolsRTS", "Decontrols",
        controllers.controlcode.routes.DecontrolsController.renderForm(ControlCodeVariant.SEARCH.urlString(), GoodsType.SOFTWARE.urlString()));

    JourneyStage decontrolsApplyRTS = defineStage("decontrolsApplyRTS", "Choose a different item type",
        controllers.controlcode.routes.DecontrolsApplyController.renderForm(ControlCodeVariant.SEARCH.urlString(), GoodsType.SOFTWARE.urlString()));

    JourneyStage technicalNotesRTS = defineStage("technicalNotesRTS", "Technical notes",
        controllers.controlcode.routes.TechnicalNotesController.renderForm(ControlCodeVariant.SEARCH.urlString(), GoodsType.SOFTWARE.urlString()));

    /** Search related to software decision stages */
    DecisionStage<Boolean> additionalSpecsDecisionRTS = defineDecisionStage("additionalSpecsDecisionRTS", additionalSpecificationsDecider);

    DecisionStage<Boolean> decontrolsDecisionRTS = defineDecisionStage("decontrolsDecisionRTS", decontrolsDecider);

    DecisionStage<Boolean> technicalNotesDecisionRTS = defineDecisionStage("technicalNotesDecisionRTS", technicalNotesDecider);

    /** Search related to software journey stage transitions */
    atStage(searchRTS)
        .onEvent(Events.SEARCH_PHYSICAL_GOODS)
        .then(moveTo(searchResultsRTS));

    atStage(searchResultsRTS)
        .onEvent(Events.CONTROL_CODE_SELECTED)
        .then(moveTo(decontrolsDecisionRTS));

    atStage(searchResultsRTS)
        .onEvent(Events.BACK)
        .branch()
        .when(BackType.SEARCH, backTo(searchRTS));

    atStage(searchResultsRTS)
        .onEvent(StandardEvents.NEXT)
        .then(moveTo(applicableCatchallControlsDecision));

    atStage(searchResultsRTS)
        .onEvent(Events.NONE_MATCHED)
        .then(moveTo(applicableCatchallControlsDecision));

    bindControlCodeStageTransitions(
        decontrolsRTS,
        decontrolsApplyRTS,
        controlCodeSummaryRTS,
        controlCodeNotApplicableRTS,
        additionalSpecificationsRTS,
        technicalNotesRTS,
        applicableRelatedControlsDecision,
        decontrolsDecisionRTS, additionalSpecsDecisionRTS,
        technicalNotesDecisionRTS
    );

    bindControlCodeNotApplicableFromSearchStageJourneyTransitions(
        controlCodeNotApplicableRTS,
        searchRTS,
        searchResultsRTS
    );

    atStage(decontrolsApplyRTS)
        .onEvent(Events.BACK)
        .branch()
        .when(BackType.SEARCH, backTo(searchRTS))
        .when(BackType.RESULTS, backTo(searchResultsRTS))
        .when(BackType.EXPORT_CATEGORY, backTo(exportCategory));

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

    JourneyStage decontrolsSCRTPG = defineStage("decontrolsSCRTPG", "Decontrols",
        controllers.controlcode.routes.DecontrolsController.renderForm(ControlCodeVariant.CONTROLS_RELATED_TO_A_PHYSICAL_GOOD.urlString(), GoodsType.SOFTWARE.urlString()));

    JourneyStage decontrolsApplySCRTPG = defineStage("decontrolsApplySCRTPG", "Choose a different item type",
        controllers.controlcode.routes.DecontrolsApplyController.renderForm(ControlCodeVariant.CONTROLS_RELATED_TO_A_PHYSICAL_GOOD.urlString(), GoodsType.SOFTWARE.urlString()));

    JourneyStage technicalNotesRelatedSCRTPG = defineStage("technicalNotesRelatedSCRTPG", "Technical notes",
        controllers.controlcode.routes.TechnicalNotesController.renderForm(ControlCodeVariant.CONTROLS_RELATED_TO_A_PHYSICAL_GOOD.urlString(), GoodsType.SOFTWARE.urlString()));

    /** Software controls related to physical goods decision stages */
    DecisionStage<Boolean> additionalSpecificationsDecisionSCRTPG = defineDecisionStage("additionalSpecificationsDecisionSCRTPG", additionalSpecificationsDecider);

    DecisionStage<Boolean> decontrolsDecisionSCRTPG = defineDecisionStage("decontrolsDecisionSCRTPG", decontrolsDecider);

    DecisionStage<Boolean> technicalNotesDecisionSCRTPG = defineDecisionStage("technicalNotesDecisionSCRTPG", technicalNotesDecider);

    /** Software control journey stage transitions */
    bindControlCodeListStageJourneyTransitions(
        controlsListSCRTPG,
        decontrolsDecisionSCRTPG,
        applicableCatchallControlsDecision
    );

    bindControlCodeStageTransitions(
        decontrolsSCRTPG,
        decontrolsApplySCRTPG,
        controlCodeSummarySCRTPG,
        controlCodeNotApplicableSCRTPG,
        additionalSpecificationsSCRTPG,
        technicalNotesRelatedSCRTPG,
        destinationCountries,
        decontrolsDecisionSCRTPG, additionalSpecificationsDecisionSCRTPG,
        technicalNotesDecisionSCRTPG
    );

    bindControlCodeNotApplicableFromListStageJourneyTransitions(
        controlCodeNotApplicableSCRTPG,
        controlsListSCRTPG,
        applicableCatchallControlsDecision
    );

    atStage(controlsListSCRTPG)
        .onEvent(Events.BACK)
        .branch()
        .when(BackType.MATCHES, backTo(controlsListSCC))
        .when(BackType.EXPORT_CATEGORY, backTo(exportCategory));

  }

  /**
   * Software catchall controls
   */
  private void softwareCatchallControls() {
    /** Software catchall controls journey stages */
    JourneyStage controlCodeNotApplicableSCC = defineStage("controlCodeNotApplicableSCC", "Description not applicable",
        controllers.controlcode.routes.NotApplicableController.renderForm(ControlCodeVariant.CATCHALL_CONTROLS.urlString(), GoodsType.SOFTWARE.urlString(), Boolean.FALSE.toString()));

    JourneyStage additionalSpecificationSCC = defineStage("additionalSpecificationSCC", "Additional specifications",
        controllers.controlcode.routes.AdditionalSpecificationsController.renderForm(ControlCodeVariant.CATCHALL_CONTROLS.urlString(), GoodsType.SOFTWARE.urlString()));

    JourneyStage decontrolsSCC = defineStage("decontrolsSCC", "Decontrols",
        controllers.controlcode.routes.DecontrolsController.renderForm(ControlCodeVariant.CATCHALL_CONTROLS.urlString(), GoodsType.SOFTWARE.urlString()));

    JourneyStage decontrolsApplySCC = defineStage("decontrolsApplySCC", "Choose a different item type",
        controllers.controlcode.routes.DecontrolsApplyController.renderForm(ControlCodeVariant.CATCHALL_CONTROLS.urlString(), GoodsType.SOFTWARE.urlString()));

    JourneyStage technicalNotesSCC = defineStage("technicalNotesSCC", "Technical notes",
        controllers.controlcode.routes.TechnicalNotesController.renderForm(ControlCodeVariant.CATCHALL_CONTROLS.urlString(), GoodsType.SOFTWARE.urlString()));

    /** Software catchall controls decision stages */
    DecisionStage<Boolean> additionalSpecificationsDecisionSCC = defineDecisionStage("additionalSpecificationsDecisionSCC", additionalSpecificationsDecider);

    DecisionStage<Boolean> decontrolsDecisionSCC = defineDecisionStage("decontrolsDecisionSCC", decontrolsDecider);

    DecisionStage<Boolean> technicalNotesDecisionSCC = defineDecisionStage("technicalNotesDecisionSCC", technicalNotesDecider);

    /** Software catchall controls stage transitions */
    bindControlCodeListStageJourneyTransitions(
        controlsListSCC,
        decontrolsDecisionSCC,
        softwareRelationshipWithTechnologyExistsDecision
    );

    bindControlCodeStageTransitions(
        decontrolsSCC,
        decontrolsApplySCC,
        controlCodeSummarySCC,
        controlCodeNotApplicableSCC,
        additionalSpecificationSCC,
        technicalNotesSCC,
        destinationCountries,
        decontrolsDecisionSCC, additionalSpecificationsDecisionSCC,
        technicalNotesDecisionSCC
    );

    bindControlCodeNotApplicableFromListStageJourneyTransitions(
        controlCodeNotApplicableSCC,
        controlsListSCC,
        softwareRelationshipWithTechnologyExistsDecision
    );

    atStage(decontrolsApplySCC)
        .onEvent(Events.BACK)
        .branch()
        .when(BackType.MATCHES, backTo(controlsListSCC))
        .when(BackType.EXPORT_CATEGORY, backTo(exportCategory));
  }

  private void softwareRelatedToTechnology() {
    JourneyStage softwareGoodsRelatedToTechnologyQuestions = defineStage("softwareGoodsRelatedToTechnologyQuestions", "Software related to technology",
        controllers.softtech.routes.GoodsRelationshipQuestionsController.renderForm(GoodsType.SOFTWARE.urlString(), GoodsType.TECHNOLOGY.urlString()));

    bindYesNoJourneyTransition(
        softwareRelatedToTechnologyQuestion,
        softwareGoodsRelatedToTechnologyQuestions,
        softwareRelationshipWithSoftwareExistsDecision
    );

    bindYesNoJourneyTransition(
        softwareGoodsRelatedToTechnologyQuestions,
        destinationCountries,
        softwareRelatedToSoftwareQuestion
    );
  }

  private void softwareRelatedToSoftware() {
    JourneyStage softwareGoodsRelatedToSoftwareQuestions = defineStage("softwareGoodsRelatedToSoftwareQuestions", "Software related to software",
        controllers.softtech.routes.GoodsRelationshipQuestionsController.renderForm(GoodsType.SOFTWARE.urlString(), GoodsType.SOFTWARE.urlString()));

    bindYesNoJourneyTransition(
        softwareRelatedToSoftwareQuestion,
        softwareGoodsRelatedToSoftwareQuestions,
        softwareJourneyEndNLR
    );

    bindYesNoJourneyTransition(
        softwareGoodsRelatedToSoftwareQuestions,
        destinationCountries,
        softwareJourneyEndNLR
    );
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
