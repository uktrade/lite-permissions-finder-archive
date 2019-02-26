package components.services;

import models.cms.Journey;

import java.util.List;

public interface JourneyService {

  List<Journey> getAllJourneys();

  Journey getById(Long id);

  Journey getByJourneyName(String name);
}
