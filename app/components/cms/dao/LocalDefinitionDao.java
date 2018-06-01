package components.cms.dao;

import models.cms.LocalDefinition;

public interface LocalDefinitionDao {

  LocalDefinition getLocalDefinition(long id);

  LocalDefinition getLocalDefinitionByTerm(String term, long controlEntryId);

  Long insertLocalDefinition(LocalDefinition localDefinition);

  void deleteLocalDefinition(long id);

  void deleteAllLocalDefinitions();

}
