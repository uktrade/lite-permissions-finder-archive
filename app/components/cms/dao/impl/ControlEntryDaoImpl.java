package components.cms.dao.impl;

import com.google.inject.Inject;
import components.cms.dao.ControlEntryDao;
import components.cms.jdbi.ControlEntryJDBIDao;
import models.cms.ControlEntry;
import org.skife.jdbi.v2.DBI;

import java.util.List;

public class ControlEntryDaoImpl implements ControlEntryDao {

  private final ControlEntryJDBIDao controlEntryJDBIDao;

  @Inject
  public ControlEntryDaoImpl(DBI dbi) {
    this.controlEntryJDBIDao = dbi.onDemand(ControlEntryJDBIDao.class);
  }

  @Override
  public ControlEntry getControlEntry(long id) {
    return controlEntryJDBIDao.get(id);
  }

  @Override
  public List<ControlEntry> getChildControlEntries(long parentId) {
    return controlEntryJDBIDao.getChildren(parentId);
  }

  @Override
  public List<ControlEntry> getAllControlEntries() {
    return controlEntryJDBIDao.getAll();
  }

  @Override
  public ControlEntry getControlEntryByControlCode(String controlCode) {
    return controlEntryJDBIDao.getByControlCode(controlCode);
  }

  @Override
  public List<ControlEntry> getRelatedControlCodeEntries(long controlEntryId) {
    return controlEntryJDBIDao.getRelatedControlCodeEntries(controlEntryId);
  }

  @Override
  public Long insertControlEntry(ControlEntry controlEntry) {
    return controlEntryJDBIDao.insert(
        controlEntry.getParentControlEntryId(),
        controlEntry.getControlCode(),
        controlEntry.getFullDescription(),
        controlEntry.getSummaryDescription(),
        controlEntry.isNested(),
        controlEntry.getDisplayOrder());
  }

  @Override
  public void deleteControlEntry(long id) {
    controlEntryJDBIDao.delete(id);
  }

  @Override
  public void deleteAllControlEntries() {
    controlEntryJDBIDao.truncate();
  }
}
