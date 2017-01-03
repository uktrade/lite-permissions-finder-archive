package journey;

import components.common.journey.JourneyEvent;
import components.common.journey.ParameterisedJourneyEvent;
import models.ArtsCulturalGoodsType;
import models.ControlCodeFlowStage;
import models.ExportCategory;
import models.GoodsType;
import models.LifeType;
import models.RadioactiveStage;
import models.VirtualEUOgelStage;
import models.controlcode.BackType;
import models.softtech.ApplicableSoftTechControls;
import models.softtech.CatchallSoftTechControlsFlow;
import models.softtech.ControlsRelatedToPhysicalGoodsFlow;
import models.softtech.Relationship;
import models.softtech.SoftTechControlsNotApplicableFlow;
import models.softtech.SoftwareExemptionsFlow;

public class Events {

  public static final ParameterisedJourneyEvent<ExportCategory> EXPORT_CATEGORY_SELECTED =
      new ParameterisedJourneyEvent<>("EXPORT_CATEGORY_SELECTED", ExportCategory.class);

  public static final JourneyEvent EXPORT_CATEGORY_COULD_BE_DUAL_USE = new JourneyEvent("EXPORT_CATEGORY_COULD_BE_DUAL_USE");

  public static final ParameterisedJourneyEvent<ArtsCulturalGoodsType> ARTS_CULTURAL_CATEGORY_SELECTED =
      new ParameterisedJourneyEvent<>("ARTS_CULTURAL_CATEGORY_SELECTED", ArtsCulturalGoodsType.class);

  public static final ParameterisedJourneyEvent<Boolean> IS_DUAL_USE =
      new ParameterisedJourneyEvent<>("IS_DUAL_USE", Boolean.class);

  public static final ParameterisedJourneyEvent<Boolean> IS_USED_FOR_EXECUTION_TORTURE =
      new ParameterisedJourneyEvent<>("IS_USED_FOR_EXECUTION_TORTURE", Boolean.class);

  public static final ParameterisedJourneyEvent<LifeType> LIFE_TYPE_SELECTED =
      new ParameterisedJourneyEvent<>("LIFE_TYPE_SELECTED", LifeType.class);

  public static final ParameterisedJourneyEvent<GoodsType> GOODS_TYPE_SELECTED =
      new ParameterisedJourneyEvent<>("GOODS_TYPE_SELECTED", GoodsType.class);

  public static final JourneyEvent SEARCH_PHYSICAL_GOODS = new JourneyEvent("SEARCH_PHYSICAL_GOODS");

  public static final JourneyEvent NONE_MATCHED = new JourneyEvent("NONE_MATCHED");

  public static final JourneyEvent EDIT_SEARCH_DESCRIPTION = new JourneyEvent("RETURN_TO_SEARCH");

  public static final JourneyEvent CONTROL_CODE_SELECTED = new JourneyEvent("CONTROL_CODE_SELECTED");

  public static final ParameterisedJourneyEvent<ControlCodeFlowStage> CONTROL_CODE_FLOW_NEXT =
      new ParameterisedJourneyEvent<>("CONTROL_CODE_FLOW_NEXT", ControlCodeFlowStage.class);

  public static final JourneyEvent DESTINATION_COUNTRIES_SELECTED = new JourneyEvent("DESTINATION_COUNTRIES_SELECTED");

  public static final ParameterisedJourneyEvent<VirtualEUOgelStage> VIRTUAL_EU_OGEL_STAGE =
      new ParameterisedJourneyEvent<>("VIRTUAL_EU_OGEL_STAGE", VirtualEUOgelStage.class);

  public static final JourneyEvent OGEL_SELECTED = new JourneyEvent("OGEL_SELECTED");

  public static final JourneyEvent OGEL_CONTINUE_TO_NON_APPLICABLE_LICENCE = new JourneyEvent("OGEL_CONTINUE_TO_NON_APPLICABLE_LICENCE");

  public static final JourneyEvent OGEL_CHOOSE_AGAIN = new JourneyEvent("OGEL_CHOOSE_AGAIN");

  public static final JourneyEvent OGEL_CONDITIONS_APPLY = new JourneyEvent("OGEL_CONDITIONS_APPLY");

  public static final JourneyEvent OGEL_CONDITIONS_DO_APPLY = new JourneyEvent("OGEL_CONDITIONS_DO_APPLY");

  public static final JourneyEvent OGEL_CONDITIONS_DO_NOT_APPLY = new JourneyEvent("OGEL_CONDITIONS_DO_NOT_APPLY");

  public static final ParameterisedJourneyEvent<Boolean> NON_MILITARY_FIREARMS_OPTION_SELECTED =
      new ParameterisedJourneyEvent<>("NON_MILITARY_FIREARMS_OPTION_SELECTED", Boolean.class);

  public static final ParameterisedJourneyEvent<SoftwareExemptionsFlow> SOFTWARE_EXEMPTIONS_FLOW =
      new ParameterisedJourneyEvent<>("SOFTWARE_EXEMPTIONS_FLOW", SoftwareExemptionsFlow.class);

  public static final ParameterisedJourneyEvent<ApplicableSoftTechControls> DUAL_USE_SOFT_TECH_CATEGORY_SELECTED =
      new ParameterisedJourneyEvent<>("DUAL_USE_SOFT_TECH_CATEGORY_SELECTED", ApplicableSoftTechControls.class);

  public static final ParameterisedJourneyEvent<ApplicableSoftTechControls> CONTROL_CODE_SOFT_TECH_CONTROLS_NOT_APPLICABLE =
      new ParameterisedJourneyEvent<>("CONTROL_CODE_SOFT_TECH_CONTROLS_NOT_APPLICABLE", ApplicableSoftTechControls.class);

  public static final ParameterisedJourneyEvent<SoftTechControlsNotApplicableFlow> CONTROL_CODE_SOFT_TECH_CONTROLS_NOT_APPLICABLE_FLOW =
      new ParameterisedJourneyEvent<>("CONTROL_CODE_SOFT_TECH_CONTROLS_NOT_APPLICABLE_FLOW", SoftTechControlsNotApplicableFlow.class);

  public static final ParameterisedJourneyEvent<ControlsRelatedToPhysicalGoodsFlow> CONTROLS_RELATED_PHYSICAL_GOOD =
      new ParameterisedJourneyEvent<>("CONTROLS_RELATED_PHYSICAL_GOOD", ControlsRelatedToPhysicalGoodsFlow.class);

  public static final ParameterisedJourneyEvent<CatchallSoftTechControlsFlow> CATCHALL_SOFT_TECH_CONTROLS_FLOW =
      new ParameterisedJourneyEvent<>("CATCHALL_SOFT_TECH_CONTROLS_FLOW", CatchallSoftTechControlsFlow.class);

  public static final ParameterisedJourneyEvent<Relationship> CONTROL_CODE_SOFT_TECH_CATCHALL_RELATIONSHIP =
      new ParameterisedJourneyEvent<>("CONTROL_CODE_SOFT_TECH_CATCHALL_RELATIONSHIP", Relationship.class);

  public static final ParameterisedJourneyEvent<RadioactiveStage> RADIOACTIVE_NEXT =
      new ParameterisedJourneyEvent<>("RADIOACTIVE_NEXT", RadioactiveStage.class);

  public static final JourneyEvent CONTROL_CODE_NOT_APPLICABLE = new JourneyEvent("CONTROL_CODE_NOT_APPLICABLE");

  public static final ParameterisedJourneyEvent<BackType> BACK = new ParameterisedJourneyEvent<>("BACK", BackType.class);

}
