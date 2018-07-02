package triage.config;

public interface DefinitionConfigService {

  DefinitionConfig getGlobalDefinitionNotNull(String id);

  DefinitionConfig getGlobalDefinition(String id);

  DefinitionConfig getLocalDefinitionNotNull(String id);

  DefinitionConfig getLocalDefinition(String id);

  void flushCache();
}
