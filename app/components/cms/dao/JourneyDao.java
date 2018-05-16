package components.cms.dao;

import models.cms.Journey;

public interface JourneyDao {

  Journey getJourney(long id);

  Long insertJourney(Journey journey);

  void deleteJourney(long id);

  void deleteAllJournies();

}
