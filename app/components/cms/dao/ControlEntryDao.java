package components.cms.dao;

import models.cms.ControlEntry;

import java.util.List;

public interface ControlEntryDao {

  ControlEntry getControlEntry(long id);

  List<ControlEntry> getChildControlEntries(long parentId);

  Long insertControlEntry(ControlEntry controlEntry);

  void deleteControlEntry(long id);

  void deleteAllControlEntries();

}