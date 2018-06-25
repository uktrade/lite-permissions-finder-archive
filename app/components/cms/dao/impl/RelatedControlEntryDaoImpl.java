package components.cms.dao.impl;

import com.google.inject.Inject;
import components.cms.dao.RelatedControlEntryDao;
import components.cms.jdbi.RelatedControlEntryJDBIDao;
import models.cms.RelatedControlEntry;
import org.skife.jdbi.v2.DBI;

public class RelatedControlEntryDaoImpl implements RelatedControlEntryDao {

  private final RelatedControlEntryJDBIDao relatedControlEntryJDBIDao;

  @Inject
  public RelatedControlEntryDaoImpl(DBI dbi) {
    this.relatedControlEntryJDBIDao = dbi.onDemand(RelatedControlEntryJDBIDao.class);
  }

  @Override
  public void insertRelatedControlEntry(RelatedControlEntry relatedControlEntry) {
    relatedControlEntryJDBIDao.insert(relatedControlEntry.getControlEntryId(), relatedControlEntry.getRelatedControlEntryId());
  }

  @Override
  public void deleteAllRelatedControlEntries() {
    relatedControlEntryJDBIDao.truncate();
  }
}
