package components.cms.dao;

import models.cms.RelatedControlEntry;

public interface RelatedControlEntryDao {
  void insertRelatedControlEntry(RelatedControlEntry relatedControlEntry);

  void deleteAllRelatedControlEntries();
}
