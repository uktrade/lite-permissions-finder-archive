package components.cms.dao;

import models.cms.ControlEntry;

public interface ControlEntryDao {

  ControlEntry getControlEntry(long id);

  Long insertControlEntry(ControlEntry controlEntry);

  void deleteControlEntry(long id);

  void deleteAllControlEntries();

}
