package components.cms.dao.impl;

import com.google.inject.Inject;
import components.cms.dao.JourneyDao;
import components.cms.jdbi.JourneyJDBIDao;
import models.cms.Journey;
import org.skife.jdbi.v2.DBI;

import java.util.List;

public class JourneyDaoImpl implements JourneyDao {

  private final JourneyJDBIDao journeyJDBIDao;

  @Inject
  public JourneyDaoImpl(DBI dbi) {
    this.journeyJDBIDao = dbi.onDemand(JourneyJDBIDao.class);
  }

  @Override
  public Journey getJourney(long id) {
    return journeyJDBIDao.get(id);
  }

  @Override
  public List<Journey> getAllJourneys() {
    return journeyJDBIDao.getAll();
  }

  @Override
  public List<Journey> getJourneysByJourneyName(String journeyName) {
    return journeyJDBIDao.getByJourneyName(journeyName);
  }

  @Override
  public Long insertJourney(Journey journey) {
    return journeyJDBIDao.insert(journey.getJourneyName(), journey.getInitialStageId());
  }

  @Override
  public void updateJourney(long id, Journey journey) {
    journeyJDBIDao.update(id, journey.getJourneyName(), journey.getInitialStageId());
  }

  @Override
  public void deleteJourney(long id) {
    journeyJDBIDao.delete(id);
  }

  @Override
  public void deleteAllJournies() {
    journeyJDBIDao.truncate();
  }
}
