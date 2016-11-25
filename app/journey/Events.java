package journey;

import components.common.journey.JourneyEvent;
import components.common.journey.ParameterisedJourneyEvent;
import models.ArtsCulturalGoodsType;
import models.ControlCodeFlowStage;
import models.ExportCategory;
import models.GoodsType;
import models.LifeType;
import models.NonMilitaryFirearmExportBySelfType;
import models.VirtualEUOgelStage;
import models.software.ApplicableSoftwareControls;
import models.software.CatchallSoftwareControlsFlow;
import models.software.ControlsRelatedToPhysicalGoodsFlow;
import models.software.Relationship;
import models.software.SoftwareCatchallControlsNotApplicableFlow;
import models.software.SoftwareControlsNotApplicableFlow;
import models.software.SoftwareExemptionsFlow;

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

  public static final JourneyEvent OGEL_DO_CONDITIONS_APPLY = new JourneyEvent("OGEL_DO_CONDITIONS_APPLY");

  public static final ParameterisedJourneyEvent<NonMilitaryFirearmExportBySelfType> NON_MILITARY_FIREARMS_QUESTION_ANSWERERD =
      new ParameterisedJourneyEvent<>("NON_MILITARY_FIREARMS_QUESTION_ANSWERERD", NonMilitaryFirearmExportBySelfType.class);

  public static final ParameterisedJourneyEvent<SoftwareExemptionsFlow> SOFTWARE_EXEMPTIONS_FLOW =
      new ParameterisedJourneyEvent<>("SOFTWARE_EXEMPTIONS_FLOW", SoftwareExemptionsFlow.class);

  public static final ParameterisedJourneyEvent<ApplicableSoftwareControls> DUAL_USE_SOFTWARE_CATEGORY_SELECTED =
      new ParameterisedJourneyEvent<>("DUAL_USE_SOFTWARE_CATEGORY_SELECTED", ApplicableSoftwareControls.class);

  public static final ParameterisedJourneyEvent<ApplicableSoftwareControls> CONTROL_CODE_SOFTWARE_CONTROLS_NOT_APPLICABLE =
      new ParameterisedJourneyEvent<>("CONTROL_CODE_SOFTWARE_CONTROLS_NOT_APPLICABLE", ApplicableSoftwareControls.class);

  public static final ParameterisedJourneyEvent<SoftwareControlsNotApplicableFlow> CONTROL_CODE_SOFTWARE_CONTROLS_NOT_APPLICABLE_FLOW =
      new ParameterisedJourneyEvent<>("CONTROL_CODE_SOFTWARE_CONTROLS_NOT_APPLICABLE_FLOW", SoftwareControlsNotApplicableFlow.class);

  public static final ParameterisedJourneyEvent<ControlsRelatedToPhysicalGoodsFlow> CONTROLS_RELATED_PHYSICAL_GOOD =
      new ParameterisedJourneyEvent<>("CONTROLS_RELATED_PHYSICAL_GOOD", ControlsRelatedToPhysicalGoodsFlow.class);

  public static final ParameterisedJourneyEvent<CatchallSoftwareControlsFlow> CATCHALL_SOFTWARE_CONTROLS_FLOW =
      new ParameterisedJourneyEvent<>("CATCHALL_SOFTWARE_CONTROLS_FLOW", CatchallSoftwareControlsFlow.class);

  public static final ParameterisedJourneyEvent<SoftwareCatchallControlsNotApplicableFlow> CONTROL_CODE_SOFTWARE_CATCHALL_CONTROLS_NOT_APPLICABLE_FLOW =
      new ParameterisedJourneyEvent<>("CONTROL_CODE_SOFTWARE_CATCHALL_CONTROLS_NOT_APPLICABLE_FLOW", SoftwareCatchallControlsNotApplicableFlow.class);

  public static final ParameterisedJourneyEvent<Relationship> CONTROL_CODE_SOFTWARE_CATCHALL_RELATIONSHIP =
      new ParameterisedJourneyEvent<>("CONTROL_CODE_SOFTWARE_CATCHALL_RELATIONSHIP", Relationship.class);

}
