package triage.config;

import com.google.inject.Inject;
import exceptions.UnknownParameterException;

public class ControllerConfigServiceImpl implements ControllerConfigService {

  private final DefinitionConfigService definitionConfigService;
  private final JourneyConfigService journeyConfigService;

  @Inject
  public ControllerConfigServiceImpl(DefinitionConfigService definitionConfigService,
                                     JourneyConfigService journeyConfigService) {
    this.definitionConfigService = definitionConfigService;
    this.journeyConfigService = journeyConfigService;
  }

  @Override
  public StageConfig getStageConfig(String stageId) {
    return journeyConfigService.getStageConfigById(stageId)
        .orElseThrow(() -> UnknownParameterException.unknownStageId(stageId));
  }

  @Override
  public ControlEntryConfig getControlEntryConfig(String controlEntryId) {
    return journeyConfigService.getControlEntryConfigById(controlEntryId)
        .orElseThrow(() -> UnknownParameterException.unknownControlEntryId(controlEntryId));
  }

  @Override
  public ControlEntryConfig getControlEntryConfigByControlCode(String controlCode) {
    return journeyConfigService.getControlEntryConfigByControlCode(controlCode)
        .orElseThrow(() -> UnknownParameterException.unknownControlCode(controlCode));
  }

  @Override
  public DefinitionConfig getGlobalDefinitionConfig(String globalDefinitionId) {
    return definitionConfigService.getGlobalDefinition(globalDefinitionId)
        .orElseThrow(() -> UnknownParameterException.unknownGlobalDefinitionId(globalDefinitionId));
  }

  @Override
  public DefinitionConfig getLocalDefinitionConfig(String localDefinitionId) {
    return definitionConfigService.getLocalDefinition(localDefinitionId)
        .orElseThrow(() -> UnknownParameterException.unknownLocalDefinitionId(localDefinitionId));
  }

}
