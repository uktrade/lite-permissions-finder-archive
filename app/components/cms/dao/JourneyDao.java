package components.cms.dao;

import models.cms.Journey;

import java.util.List;

public interface JourneyDao {

  Journey getJourney(long id);

  List<Journey> getAllJourneys();

  Journey getByJourneyName(String journeyName);

  Long insertJourney(Journey journey);

  void updateJourney(long id, Journey journey);

  void deleteAllJourneys();

}
