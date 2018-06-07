package components.cms.dao;

import models.cms.GlobalDefinition;

import java.util.List;

public interface GlobalDefinitionDao {

  GlobalDefinition getGlobalDefinition(long id);

  GlobalDefinition getGlobalDefinitionByTerm(String term);

  List<Long> getAllIds();

  Long insertGlobalDefinition(GlobalDefinition globalDefinition);

  void deleteGlobalDefinition(long id);

  void deleteAllGlobalDefinitions();

}
