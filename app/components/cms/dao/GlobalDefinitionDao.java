package components.cms.dao;

import models.cms.GlobalDefinition;

import java.util.List;

public interface GlobalDefinitionDao {

  GlobalDefinition getGlobalDefinition(long id);

  GlobalDefinition getGlobalDefinitionByTermAndJourneyId(String term, long journeyId);

  List<Long> getAllIds();

  Long insert(GlobalDefinition globalDefinition);

  void insert(List<GlobalDefinition> globalDefinitions);

  void deleteAllGlobalDefinitions();

}
