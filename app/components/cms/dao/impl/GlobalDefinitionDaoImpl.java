package components.cms.dao.impl;

import com.google.inject.Inject;
import components.cms.dao.GlobalDefinitionDao;
import components.cms.jdbi.GlobalDefinitionJDBIDao;
import models.cms.GlobalDefinition;
import org.skife.jdbi.v2.DBI;

public class GlobalDefinitionDaoImpl implements GlobalDefinitionDao {

  private final GlobalDefinitionJDBIDao globalDefinitionJDBIDao;

  @Inject
  public GlobalDefinitionDaoImpl(DBI dbi) {
    this.globalDefinitionJDBIDao = dbi.onDemand(GlobalDefinitionJDBIDao.class);
  }

  @Override
  public GlobalDefinition getGlobalDefinition(long id) {
    return this.globalDefinitionJDBIDao.get(id);
  }

  @Override
  public Long insertGlobalDefinition(GlobalDefinition globalDefinition) {
    return this.globalDefinitionJDBIDao.insert(
        globalDefinition.getJourneyId(),
        globalDefinition.getTerm(),
        globalDefinition.getDefinitionText());
  }

  @Override
  public void deleteGlobalDefinition(long id) {
    this.globalDefinitionJDBIDao.delete(id);
  }

  @Override
  public void deleteAllGlobalDefinitions() {
    this.globalDefinitionJDBIDao.truncate();
  }
}
