package journey;

import components.common.journey.JourneyEvent;
import components.common.journey.ParameterisedJourneyEvent;
import model.TradeType;

public class Events {

  public static final JourneyEvent START_APPLICATION = new JourneyEvent("START_APPLICATION");

  public static final JourneyEvent CONTINUE_APPLICATION = new JourneyEvent("CONTINUE_APPLICATION");

  public static final JourneyEvent APPLICATION_FOUND = new JourneyEvent("APPLICATION_FOUND");

  public static final JourneyEvent APPLICATION_NOT_FOUND = new JourneyEvent("APPLICATION_NOT_FOUND");

  public static final ParameterisedJourneyEvent TRADE_TYPE_SELECTED =
      new ParameterisedJourneyEvent("TRADE_TYPE_SELECTED", TradeType.class);

}
