package journey;

import com.google.inject.Inject;
import components.common.journey.JourneySerialiser;
import components.persistence.PermissionsFinderDao;

public class PermissionsFinderJourneySerialiser implements JourneySerialiser {

  private final PermissionsFinderDao permissionsFinderDao;

  @Inject
  public PermissionsFinderJourneySerialiser(PermissionsFinderDao permissionsFinderDao) {
    this.permissionsFinderDao = permissionsFinderDao;
  }

  @Override
  public String readJourneyString(String journeyName) {
    return permissionsFinderDao.readJourneyString();
  }

  @Override
  public void writeJourneyString(String journeyName, String journeyString) {
    permissionsFinderDao.writeJourneyString(journeyString);
  }
}