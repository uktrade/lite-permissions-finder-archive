package components.cms.dao.impl;

import com.google.inject.Inject;
import components.cms.dao.LocalDefinitionDao;
import components.cms.jdbi.LocalDefinitionJDBIDao;
import models.cms.LocalDefinition;
import org.skife.jdbi.v2.DBI;

import java.util.List;

public class LocalDefinitionDaoImpl implements LocalDefinitionDao {

  private final LocalDefinitionJDBIDao localDefinitionJDBIDao;

  @Inject
  public LocalDefinitionDaoImpl(DBI dbi) {
    this.localDefinitionJDBIDao = dbi.onDemand(LocalDefinitionJDBIDao.class);
  }

  @Override
  public LocalDefinition getLocalDefinition(long id) {
    return localDefinitionJDBIDao.get(id);
  }

  @Override
  public LocalDefinition getLocalDefinitionByTerm(String term, long controlEntryId) {
    return this.localDefinitionJDBIDao.getByTerm(term, controlEntryId);
  }

  @Override
  public List<Long> getAllIds() {
    return localDefinitionJDBIDao.getAllIds();
  }

  @Override
  public Long insertLocalDefinition(LocalDefinition localDefinition) {
    return localDefinitionJDBIDao.insert(
        localDefinition.getControlEntryId(),
        localDefinition.getTerm(),
        localDefinition.getDefinitionText());
  }

  @Override
  public void deleteLocalDefinition(long id) {
    localDefinitionJDBIDao.delete(id);
  }

  @Override
  public void deleteAllLocalDefinitions() {
    localDefinitionJDBIDao.truncate();
  }
}
