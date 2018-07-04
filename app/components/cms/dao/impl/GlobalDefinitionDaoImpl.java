package components.cms.dao.impl;

import com.google.inject.Inject;
import components.cms.dao.GlobalDefinitionDao;
import components.cms.jdbi.GlobalDefinitionJDBIDao;
import models.cms.GlobalDefinition;
import org.skife.jdbi.v2.DBI;

import java.util.List;

public class GlobalDefinitionDaoImpl implements GlobalDefinitionDao {

  private final GlobalDefinitionJDBIDao globalDefinitionJDBIDao;

  @Inject
  public GlobalDefinitionDaoImpl(DBI dbi) {
    this.globalDefinitionJDBIDao = dbi.onDemand(GlobalDefinitionJDBIDao.class);
  }

  @Override
  public GlobalDefinition getGlobalDefinition(long id) {
    return globalDefinitionJDBIDao.get(id);
  }

  @Override
  public GlobalDefinition getGlobalDefinitionByTermAndJourneyId(String term, long journeyId) {
    return globalDefinitionJDBIDao.getByTerm(term);
  }

  @Override
  public List<Long> getAllIds() {
    return globalDefinitionJDBIDao.getAllIds();
  }

  @Override
  public Long insertGlobalDefinition(GlobalDefinition globalDefinition) {
    return globalDefinitionJDBIDao.insert(
        globalDefinition.getJourneyId(),
        globalDefinition.getTerm(),
        globalDefinition.getDefinitionText());
  }

  @Override
  public void deleteGlobalDefinition(long id) {
    globalDefinitionJDBIDao.delete(id);
  }

  @Override
  public void deleteAllGlobalDefinitions() {
    globalDefinitionJDBIDao.truncate();
  }

}
