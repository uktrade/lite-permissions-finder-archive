package journey;

import components.common.journey.JourneyEvent;
import components.common.journey.ParameterisedJourneyEvent;
import model.ExportCategory;
import model.TradeType;

public class Events {

  public static final JourneyEvent START_APPLICATION = new JourneyEvent("START_APPLICATION");

  public static final JourneyEvent CONTINUE_APPLICATION = new JourneyEvent("CONTINUE_APPLICATION");

  public static final JourneyEvent APPLICATION_FOUND = new JourneyEvent("APPLICATION_FOUND");

  public static final JourneyEvent APPLICATION_NOT_FOUND = new JourneyEvent("APPLICATION_NOT_FOUND");

  public static final ParameterisedJourneyEvent<TradeType> TRADE_TYPE_SELECTED =
      new ParameterisedJourneyEvent<>("TRADE_TYPE_SELECTED", TradeType.class);

  public static final ParameterisedJourneyEvent<ExportCategory> EXPORT_CATEGORY_SELECTED =
      new ParameterisedJourneyEvent<>("EXPORT_CATEGORY_SELECTED", ExportCategory.class);

  public static final JourneyEvent EXPORT_CATEGORY_COULD_BE_DUAL_USE = new JourneyEvent("EXPORT_CATEGORY_COULD_BE_DUAL_USE");

  public static final JourneyEvent GOOD_CONTROLLED = new JourneyEvent("GOOD_CONTROLLED");

  public static final JourneyEvent GOOD_NOT_CONTROLLED = new JourneyEvent("GOOD_NOT_CONTROLLED");

  public static final ParameterisedJourneyEvent<Boolean> IS_USED_FOR_EXECUTION_TORTURE =
      new ParameterisedJourneyEvent<>("IS_USED_FOR_EXECUTION_TORTURE", Boolean.class);

}
