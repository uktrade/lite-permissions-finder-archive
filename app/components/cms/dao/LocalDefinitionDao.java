package components.cms.dao;

import models.cms.LocalDefinition;

import java.util.List;

public interface LocalDefinitionDao {

  LocalDefinition getLocalDefinition(long id);

  LocalDefinition getLocalDefinitionByTerm(String term, long controlEntryId);

  List<Long> getAllIds();

  Long insertLocalDefinition(LocalDefinition localDefinition);

  void deleteLocalDefinition(long id);

  void deleteAllLocalDefinitions();

}
