package components.cms.dao;

import models.cms.GlobalDefinition;

public interface GlobalDefinitionDao {

  GlobalDefinition getGlobalDefinition(long id);

  Long insertGlobalDefinition(GlobalDefinition globalDefinition);

  void deleteGlobalDefinition(long id);

  void deleteAllGlobalDefinitions();

}
