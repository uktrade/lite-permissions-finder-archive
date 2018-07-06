package triage.config;

public interface ControllerConfigService {

  StageConfig getStageConfig(String stageId);

  ControlEntryConfig getControlEntryConfig(String controlEntryId);

  ControlEntryConfig getControlEntryConfigByControlCode(String controlCode);

  DefinitionConfig getGlobalDefinitionConfig(String globalDefinitionId);

  DefinitionConfig getLocalDefinitionConfig(String localDefinitionId);
}
