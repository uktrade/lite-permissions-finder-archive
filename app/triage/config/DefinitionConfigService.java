package triage.config;

import java.util.Optional;

public interface DefinitionConfigService {

  Optional<DefinitionConfig> getGlobalDefinition(String id);

  Optional<DefinitionConfig> getLocalDefinition(String id);

  void flushCache();
}
