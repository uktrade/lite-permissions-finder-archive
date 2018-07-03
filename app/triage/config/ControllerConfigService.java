package triage.config;

public interface ControllerConfigService {

  StageConfig getStageConfig(String stageId);

  ControlEntryConfig getControlEntryConfig(String controlEntryId);

  DefinitionConfig getGlobalDefinitionConfig(String globalDefinitionId);

  DefinitionConfig getLocalDefinitionConfig(String localDefinitionId);
}
