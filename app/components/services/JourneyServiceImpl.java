package components.services;

import com.google.inject.Inject;
import components.cms.dao.JourneyDao;
import lombok.AllArgsConstructor;
import models.cms.Journey;

import java.util.List;

@AllArgsConstructor(onConstructor = @__({@Inject}))
public class JourneyServiceImpl implements JourneyService {

  private final JourneyDao journeyDao;

  public List<Journey> getAllJourneys() {
    return journeyDao.getAllJourneys();
  }

  public Journey getById(Long id) { return journeyDao.getJourney(id); }

  public Journey getByJourneyName(String journeyName) { return journeyDao.getByJourneyName(journeyName); }
}
