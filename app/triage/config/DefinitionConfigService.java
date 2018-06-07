package triage.config;

public interface DefinitionConfigService {

  DefinitionConfig getGlobalDefinition(String id);

  DefinitionConfig getLocalDefinition(String id);

  void flushCache();
}
