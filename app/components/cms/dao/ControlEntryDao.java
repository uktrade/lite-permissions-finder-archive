package components.cms.dao;

import models.cms.ControlEntry;

import java.util.List;

public interface ControlEntryDao {

  ControlEntry getControlEntry(long id);

  List<ControlEntry> getChildControlEntries(long parentId);

  List<ControlEntry> getAllControlEntries();

  ControlEntry getControlEntryByControlCode(String controlCode);

  List<ControlEntry> findControlEntriesByControlCode(String value);

  List<ControlEntry> getRelatedControlCodeEntries(long controlEntryId);

  Long insertControlEntry(ControlEntry controlEntry);

  void deleteAllControlEntries();

}
