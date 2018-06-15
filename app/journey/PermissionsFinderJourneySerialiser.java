package journey;

import com.google.inject.Inject;
import components.common.journey.JourneySerialiser;

public class PermissionsFinderJourneySerialiser implements JourneySerialiser {


  @Inject
  public PermissionsFinderJourneySerialiser() {
  }

  @Override
  public String readJourneyString(String journeyName) {
    return "DUMMY_JOURNEY";
  }

  @Override
  public void writeJourneyString(String journeyName, String journeyString) {
  }
}